package com.example.connectifyproject.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.connectifyproject.models.OfertaTour;
import com.example.connectifyproject.models.TourAsignado;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TourFirebaseService {
    private static final String TAG = "TourFirebaseService";
    private static final String COLLECTION_OFERTAS = "tours_ofertas";
    private static final String SUBCOLLECTION_GUIAS = "guias_ofertados";
    private static final String COLLECTION_ASIGNADOS = "tours_asignados";
    private static final String COLLECTION_USUARIOS = "usuarios";
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    public TourFirebaseService() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }
    
    /**
     * 🔐 OBTENER USUARIO ACTUAL
     */
    private FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    
    // Interfaces para callbacks (siguiendo el patrón existente)
    public interface TourCallback {
        void onSuccess(List<OfertaTour> tours);
        void onError(String error);
    }
    
    public interface OperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    /**
     * Obtener ofertas disponibles para guías específicos - SOLO para guías incluidos en subcolección
     */
    public void getOfertasDisponibles(TourCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        
        String guiaId = currentUser.getUid();
        Log.d(TAG, "Obteniendo ofertas disponibles para guía: " + guiaId);
        
        // Primero obtener todas las ofertas publicadas (filtrar habilitado localmente)
        db.collection(COLLECTION_OFERTAS)
                .whereEqualTo("estado", "publicado")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OfertaTour> ofertasParaGuia = new ArrayList<>();
                    
                    // Filtrar documentos habilitados localmente
                    java.util.List<DocumentSnapshot> ofertasHabilitadas = new java.util.ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean habilitado = doc.getBoolean("habilitado");
                        if (habilitado != null && habilitado) {
                            ofertasHabilitadas.add(doc);
                        }
                    }
                    
                    final int totalOfertas = ofertasHabilitadas.size();
                    
                    if (totalOfertas == 0) {
                        Log.d(TAG, "No hay ofertas publicadas");
                        callback.onSuccess(ofertasParaGuia);
                        return;
                    }
                    
                    // Counter para saber cuándo hemos verificado todas las ofertas
                    final int[] verificadas = {0};
                    
                    for (DocumentSnapshot document : ofertasHabilitadas) {
                        String ofertaId = document.getId();
                        
                        // Verificar si el guía está en la subcolección de esta oferta
                        db.collection(COLLECTION_OFERTAS)
                                .document(ofertaId)
                                .collection(SUBCOLLECTION_GUIAS)
                                .document(guiaId)
                                .get()
                                .addOnSuccessListener(guiaDoc -> {
                                    verificadas[0]++;
                                    
                                    // Si el guía está en la subcolección de esta oferta
                                    if (guiaDoc.exists()) {
                                        // Verificar si el guía NO ha rechazado la oferta
                                        Boolean rechazado = guiaDoc.getBoolean("rechazado");
                                        String estadoOferta = guiaDoc.getString("estadoOferta");
                                        
                                        // Solo incluir si NO está rechazado y NO está aceptado
                                        if ((rechazado == null || !rechazado) && 
                                            (estadoOferta == null || (!estadoOferta.equals("rechazado") && !estadoOferta.equals("aceptado")))) {
                                            
                                            try {
                                                OfertaTour oferta = document.toObject(OfertaTour.class);
                                                if (oferta != null) {
                                                    oferta.setId(document.getId());
                                                    ofertasParaGuia.add(oferta);
                                                    Log.d(TAG, "Oferta incluida para guía: " + oferta.getTitulo());
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error al parsear documento: " + document.getId(), e);
                                            }
                                        } else {
                                            Log.d(TAG, "Oferta excluida - rechazado: " + rechazado + ", estado: " + estadoOferta + " para oferta: " + ofertaId);
                                        }
                                    } else {
                                        Log.d(TAG, "Guía no incluido en oferta: " + ofertaId);
                                    }
                                    
                                    // Si ya verificamos todas las ofertas, procesar el resultado
                                    if (verificadas[0] == totalOfertas) {
                                        procesarResultadosOfertas(ofertasParaGuia, callback);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    verificadas[0]++;
                                    Log.e(TAG, "Error al verificar guía en subcolección: " + ofertaId, e);
                                    
                                    // Si ya verificamos todas las ofertas, procesar el resultado
                                    if (verificadas[0] == totalOfertas) {
                                        procesarResultadosOfertas(ofertasParaGuia, callback);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener ofertas: ", e);
                    callback.onError("Error al cargar las ofertas de tours");
                });
    }
    
    /**
     * Procesar y ordenar los resultados de ofertas
     */
    private void procesarResultadosOfertas(List<OfertaTour> ofertas, TourCallback callback) {
        // Ordenar por fecha de realización del tour (ahora Timestamp)
        ofertas.sort((o1, o2) -> {
            try {
                // Para ofertas, fechaRealizacion sigue siendo String, así que mantenemos el parseo
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date fecha1 = sdf.parse(o1.getFechaRealizacion());
                Date fecha2 = sdf.parse(o2.getFechaRealizacion());
                
                if (fecha1 != null && fecha2 != null) {
                    int dateCompare = fecha1.compareTo(fecha2);
                    if (dateCompare == 0) {
                        // Si las fechas son iguales, ordenar por hora de inicio
                        return o1.getHoraInicio().compareTo(o2.getHoraInicio());
                    }
                    return dateCompare;
                }
                return 0;
            } catch (ParseException e) {
                Log.e(TAG, "Error al parsear fechas para ordenamiento", e);
                return 0;
            }
        });
        
        Log.d(TAG, "Total ofertas disponibles para este guía: " + ofertas.size());
        callback.onSuccess(ofertas);
    }
    
    /**
     * Aceptar una oferta por parte de un guía
     */
    public void aceptarOferta(String ofertaId, OperationCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        
        String guiaId = currentUser.getUid();
        Log.d(TAG, "Guía " + guiaId + " intentando aceptar oferta: " + ofertaId);
        
        // Verificar que el guía está en la subcolección de la oferta
        db.collection(COLLECTION_OFERTAS)
                .document(ofertaId)
                .collection(SUBCOLLECTION_GUIAS)
                .document(guiaId)
                .get()
                .addOnSuccessListener(guiaDoc -> {
                    if (guiaDoc.exists()) {
                        // El guía está autorizado, proceder con la aceptación
                        procesarAceptacionOferta(ofertaId, guiaId, callback);
                    } else {
                        callback.onError("No tienes permisos para aceptar esta oferta");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar permisos del guía: ", e);
                    callback.onError("Error al verificar permisos");
                });
    }
    
    /**
     * Rechazar una oferta por parte de un guía
     */
    public void rechazarOferta(String ofertaId, OperationCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        
        String guiaId = currentUser.getUid();
        Log.d(TAG, "Guía " + guiaId + " rechazando oferta: " + ofertaId);
        
        // Verificar que el guía está en la subcolección y actualizar su estado
        db.collection(COLLECTION_OFERTAS)
                .document(ofertaId)
                .collection(SUBCOLLECTION_GUIAS)
                .document(guiaId)
                .get()
                .addOnSuccessListener(guiaDoc -> {
                    if (guiaDoc.exists()) {
                        // Actualizar el estado del guía en la subcolección
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("estadoOferta", "rechazado");
                        updates.put("fechaRespuesta", Timestamp.now());
                        updates.put("rechazado", true);
                        
                        db.collection(COLLECTION_OFERTAS)
                                .document(ofertaId)
                                .collection(SUBCOLLECTION_GUIAS)
                                .document(guiaId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Oferta rechazada por guía: " + guiaId);
                                    
                                    // IMPORTANTE: Actualizar guiaSeleccionadoActual a null en el documento principal
                                    Map<String, Object> ofertaUpdates = new HashMap<>();
                                    ofertaUpdates.put("guiaSeleccionadoActual", null);
                                    ofertaUpdates.put("fechaActualizacion", Timestamp.now());
                                    
                                    db.collection(COLLECTION_OFERTAS)
                                            .document(ofertaId)
                                            .update(ofertaUpdates)
                                            .addOnSuccessListener(aVoid2 -> {
                                                Log.d(TAG, "guiaSeleccionadoActual actualizado a null");
                                                callback.onSuccess("Oferta rechazada correctamente");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error al actualizar guiaSeleccionadoActual: ", e);
                                                // Aún así consideramos exitoso el rechazo
                                                callback.onSuccess("Oferta rechazada correctamente");
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al rechazar oferta: ", e);
                                    callback.onError("Error al rechazar la oferta");
                                });
                    } else {
                        callback.onError("No tienes permisos para rechazar esta oferta");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar permisos: ", e);
                    callback.onError("Error al procesar la solicitud");
                });
    }
    
    /**
 * Procesar la aceptación de la oferta
 */
private void procesarAceptacionOferta(String ofertaId, String guiaId, OperationCallback callback) {
    // Verificar que la oferta sigue disponible y obtener todos los datos
    db.collection(COLLECTION_OFERTAS).document(ofertaId)
            .get()
            .addOnSuccessListener(ofertaDoc -> {
                if (ofertaDoc.exists()) {
                    // Primero obtener datos del guía
                    db.collection(COLLECTION_USUARIOS)
                            .document(guiaId)
                            .get()
                            .addOnSuccessListener(guiaDoc -> {
                                if (guiaDoc.exists()) {
                                    // Crear documento en tours_asignados
                                    crearTourAsignadoDesdeDocumento(ofertaDoc, guiaDoc, guiaId, callback);
                                } else {
                                    callback.onError("Datos del guía no encontrados");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al obtener datos del guía: ", e);
                                callback.onError("Error al verificar datos del guía");
                            });
                } else {
                    callback.onError("La oferta no existe");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al verificar oferta: ", e);
                callback.onError("Error al procesar la solicitud");
            });
}

/**
 * Crear tour asignado desde DocumentSnapshot (MÉTODO ÚNICO Y ACTUALIZADO)
 */
private void crearTourAsignadoDesdeDocumento(DocumentSnapshot ofertaDoc, DocumentSnapshot guiaDoc, String guiaId, OperationCallback callback) {
    // Crear estructura del guía asignado
    Map<String, Object> guiaAsignado = new HashMap<>();
    guiaAsignado.put("identificadorUsuario", guiaId);
    
    // Concatenar nombre completo
    String nombre = guiaDoc.getString("nombre");
    String apellido = guiaDoc.getString("apellido");
    String nombresCompletos = "";
    if (nombre != null && apellido != null) {
        nombresCompletos = nombre + " " + apellido;
    } else if (nombre != null) {
        nombresCompletos = nombre;
    } else {
        nombresCompletos = guiaDoc.getString("nombresApellidos"); // Fallback
    }
    guiaAsignado.put("nombresCompletos", nombresCompletos);
    
    guiaAsignado.put("correoElectronico", guiaDoc.getString("email"));
    guiaAsignado.put("numeroTelefono", guiaDoc.getString("telefono"));
    guiaAsignado.put("fechaAsignacion", Timestamp.now());
    
    // Copiar itinerario y agregar campos de control
    List<Map<String, Object>> itinerarioOferta = (List<Map<String, Object>>) ofertaDoc.get("itinerario");
    List<Map<String, Object>> itinerarioConSeguimiento = new ArrayList<>();
    if (itinerarioOferta != null) {
        for (Map<String, Object> punto : itinerarioOferta) {
            Map<String, Object> puntoConSeguimiento = new HashMap<>(punto);
            puntoConSeguimiento.put("completado", false);
            puntoConSeguimiento.put("horaLlegada", null);
            puntoConSeguimiento.put("horaSalida", null);
            itinerarioConSeguimiento.add(puntoConSeguimiento);
        }
    }
    
    // Lista vacía de participantes
    List<Map<String, Object>> participantes = new ArrayList<>();
    
    // Convertir fechaRealizacion de String a Timestamp si es necesario
    Timestamp fechaRealizacionTimestamp;
    Object fechaRealizacionObj = ofertaDoc.get("fechaRealizacion");
    if (fechaRealizacionObj instanceof Timestamp) {
        fechaRealizacionTimestamp = (Timestamp) fechaRealizacionObj;
    } else if (fechaRealizacionObj instanceof String) {
        fechaRealizacionTimestamp = convertirFechaStringATimestamp((String) fechaRealizacionObj);
    } else {
        fechaRealizacionTimestamp = Timestamp.now();
    }
    
    // ✅ IMÁGENES - COPIAR ANTES DE TODO
    String imagenPrincipal = ofertaDoc.getString("imagenPrincipal");
    List<String> imagenesUrls = (List<String>) ofertaDoc.get("imagenesUrls");
    
    Log.d(TAG, "=== COPIANDO IMÁGENES ===");
    Log.d(TAG, "Imagen principal: " + imagenPrincipal);
    Log.d(TAG, "Array imágenes: " + imagenesUrls);
    
    // Crear documento del tour asignado
    Map<String, Object> tourAsignado = new HashMap<>();
    tourAsignado.put("ofertaTourId", ofertaDoc.getId());
    tourAsignado.put("titulo", ofertaDoc.getString("titulo"));
    tourAsignado.put("descripcion", ofertaDoc.getString("descripcion"));
    tourAsignado.put("precio", ofertaDoc.getDouble("precio"));
    tourAsignado.put("duracion", ofertaDoc.getString("duracion"));
    tourAsignado.put("fechaRealizacion", fechaRealizacionTimestamp);
    tourAsignado.put("horaInicio", ofertaDoc.getString("horaInicio"));
    tourAsignado.put("horaFin", ofertaDoc.getString("horaFin"));
    tourAsignado.put("itinerario", itinerarioConSeguimiento);
    tourAsignado.put("serviciosAdicionales", ofertaDoc.get("serviciosAdicionales"));
    tourAsignado.put("guiaAsignado", guiaAsignado);
    tourAsignado.put("empresaId", ofertaDoc.getString("empresaId"));
    tourAsignado.put("nombreEmpresa", ofertaDoc.getString("nombreEmpresa"));
    tourAsignado.put("correoEmpresa", ofertaDoc.getString("correoEmpresa"));
    tourAsignado.put("pagoGuia", ofertaDoc.getDouble("pagoGuia"));
    tourAsignado.put("idiomasRequeridos", ofertaDoc.get("idiomasRequeridos"));
    tourAsignado.put("consideraciones", ofertaDoc.getString("consideraciones"));
    
    // ✅ GUARDAR IMÁGENES
    tourAsignado.put("imagenPrincipal", imagenPrincipal != null ? imagenPrincipal : "");
    tourAsignado.put("imagenesUrls", imagenesUrls != null ? imagenesUrls : new ArrayList<>());
    
    Log.d(TAG, "Imágenes guardadas en tourAsignado");
    Log.d(TAG, "========================");
    
    tourAsignado.put("participantes", participantes);
    tourAsignado.put("ciudad", ofertaDoc.getString("ciudad"));
    tourAsignado.put("estado", "confirmado");
    tourAsignado.put("numeroParticipantesTotal", 0);
    tourAsignado.put("checkInRealizado", false);
    tourAsignado.put("checkOutRealizado", false);
    tourAsignado.put("horaCheckIn", null);
    tourAsignado.put("horaCheckOut", null);
    tourAsignado.put("reseniasClientes", new ArrayList<>());
    tourAsignado.put("calificacionPromedio", 0.0);
    tourAsignado.put("comentariosGuia", "");
    tourAsignado.put("fechaAsignacion", Timestamp.now());
    tourAsignado.put("fechaCreacion", Timestamp.now());
    tourAsignado.put("fechaActualizacion", Timestamp.now());
    tourAsignado.put("habilitado", true);
    
    // Insertar en tours_asignados
    db.collection(COLLECTION_ASIGNADOS)
            .add(tourAsignado)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Tour asignado creado con ID: " + documentReference.getId());
                
                // Actualizar la oferta original
                Map<String, Object> ofertaUpdates = new HashMap<>();
                ofertaUpdates.put("estado", "asignado");
                ofertaUpdates.put("guiaAsignadoId", guiaId);
                ofertaUpdates.put("fechaAsignacion", Timestamp.now());
                ofertaUpdates.put("fechaActualizacion", Timestamp.now());
                
                db.collection(COLLECTION_OFERTAS)
                        .document(ofertaDoc.getId())
                        .update(ofertaUpdates)
                        .addOnSuccessListener(aVoid -> {
                            // Actualizar subcolección
                            Map<String, Object> guiaUpdates = new HashMap<>();
                            guiaUpdates.put("estadoOferta", "aceptado");
                            guiaUpdates.put("fechaRespuesta", Timestamp.now());
                            
                            db.collection(COLLECTION_OFERTAS)
                                    .document(ofertaDoc.getId())
                                    .collection(SUBCOLLECTION_GUIAS)
                                    .document(guiaId)
                                    .update(guiaUpdates)
                                    .addOnSuccessListener(aVoid2 -> {
                                        Log.d(TAG, "Oferta y tour asignado creados exitosamente");
                                        callback.onSuccess("¡Tour aceptado exitosamente!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al actualizar subcolección: ", e);
                                        callback.onError("Error al completar la aceptación");
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al actualizar oferta: ", e);
                            callback.onError("Error al aceptar la oferta");
                        });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al crear tour asignado: ", e);
                callback.onError("Error al crear el tour asignado");
            });
}
    
    /**
     * Crear tour asignado basado en la oferta aceptada
     */
    private void crearTourAsignado(OfertaTour oferta, DocumentSnapshot guiaDoc, String ofertaId, String guiaId, OperationCallback callback) {
        // Crear estructura del guía asignado
        Map<String, Object> guiaAsignado = new HashMap<>();
        guiaAsignado.put("identificadorUsuario", guiaId);
        guiaAsignado.put("nombresCompletos", guiaDoc.getString("nombresApellidos"));
        guiaAsignado.put("correoElectronico", guiaDoc.getString("email"));
        guiaAsignado.put("numeroTelefono", guiaDoc.getString("telefono"));
        guiaAsignado.put("fechaAsignacion", Timestamp.now());
        
        // Crear itinerario con seguimiento
        List<Map<String, Object>> itinerarioConSeguimiento = new ArrayList<>();
        if (oferta.getItinerario() != null) {
            for (Map<String, Object> punto : oferta.getItinerario()) {
                Map<String, Object> puntoConSeguimiento = new HashMap<>(punto);
                puntoConSeguimiento.put("completado", false);
                puntoConSeguimiento.put("horaLlegada", null);
                puntoConSeguimiento.put("horaSalida", null);
                itinerarioConSeguimiento.add(puntoConSeguimiento);
            }
        }
        
        // ✅ CORRECCIÓN: Al aceptar oferta NO crear participantes automáticamente
        // Los participantes se agregarán cuando los clientes se registren al tour
        List<Map<String, Object>> participantes = new ArrayList<>(); // ✅ Lista vacía inicialmente
        
        // ✅ CORRECCIÓN: Convertir fecha String a Timestamp
        Timestamp fechaRealizacionTimestamp = convertirFechaStringATimestamp(oferta.getFechaRealizacion());
        
        // Crear documento del tour asignado
        Map<String, Object> tourAsignado = new HashMap<>();
        tourAsignado.put("ofertaTourId", ofertaId);
        tourAsignado.put("titulo", oferta.getTitulo());
        tourAsignado.put("descripcion", oferta.getDescripcion());
        tourAsignado.put("precio", oferta.getPrecio());
        tourAsignado.put("duracion", oferta.getDuracion());
        tourAsignado.put("fechaRealizacion", fechaRealizacionTimestamp); // ✅ Usar Timestamp
        tourAsignado.put("horaInicio", oferta.getHoraInicio());
        tourAsignado.put("horaFin", oferta.getHoraFin());
        tourAsignado.put("itinerario", itinerarioConSeguimiento);
        tourAsignado.put("serviciosAdicionales", oferta.getServiciosAdicionales());
        tourAsignado.put("guiaAsignado", guiaAsignado);
        tourAsignado.put("empresaId", oferta.getEmpresaId());
        tourAsignado.put("nombreEmpresa", oferta.getNombreEmpresa());
        tourAsignado.put("correoEmpresa", oferta.getCorreoEmpresa());
        tourAsignado.put("pagoGuia", oferta.getPagoGuia());
        tourAsignado.put("idiomasRequeridos", oferta.getIdiomasRequeridos());
        tourAsignado.put("consideraciones", oferta.getConsideraciones());
        tourAsignado.put("ciudad", oferta.getCiudad());
        tourAsignado.put("participantes", participantes);
        tourAsignado.put("estado", "confirmado"); // ✅ Estado inicial único
        tourAsignado.put("numeroParticipantesTotal", participantes.size());
        tourAsignado.put("checkInRealizado", false);
        tourAsignado.put("checkOutRealizado", false);
        tourAsignado.put("horaCheckIn", null);
        tourAsignado.put("horaCheckOut", null);
        tourAsignado.put("reseniasClientes", new ArrayList<>());
        tourAsignado.put("calificacionPromedio", 0.0);
        tourAsignado.put("comentariosGuia", "");
        tourAsignado.put("fechaAsignacion", Timestamp.now());
        tourAsignado.put("fechaCreacion", Timestamp.now());
        tourAsignado.put("fechaActualizacion", Timestamp.now());
        tourAsignado.put("habilitado", true);
        
        // Insertar en tours_asignados
        db.collection(COLLECTION_ASIGNADOS)
                .add(tourAsignado)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Tour asignado creado con ID: " + documentReference.getId());
                    
                    // Ahora actualizar la oferta original
                    actualizarOfertaOriginal(ofertaId, guiaId, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear tour asignado: ", e);
                    callback.onError("Error al crear el tour asignado");
                });
    }
    
    /**
     * Actualizar la oferta original después de crear el tour asignado
     */
    private void actualizarOfertaOriginal(String ofertaId, String guiaId, OperationCallback callback) {
        // Actualizar la oferta principal con el guía asignado
        Map<String, Object> ofertaUpdates = new HashMap<>();
        ofertaUpdates.put("estado", "asignado");
        ofertaUpdates.put("guiaAsignadoId", guiaId);
        ofertaUpdates.put("fechaAsignacion", Timestamp.now());
        ofertaUpdates.put("fechaActualizacion", Timestamp.now());
        
        // Actualizar el estado del guía en la subcolección
        Map<String, Object> guiaUpdates = new HashMap<>();
        guiaUpdates.put("estadoOferta", "aceptado");
        guiaUpdates.put("fechaRespuesta", Timestamp.now());
        
        // Ejecutar ambas actualizaciones
        db.collection(COLLECTION_OFERTAS).document(ofertaId)
                .update(ofertaUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar subcolección
                    db.collection(COLLECTION_OFERTAS)
                            .document(ofertaId)
                            .collection(SUBCOLLECTION_GUIAS)
                            .document(guiaId)
                            .update(guiaUpdates)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "Oferta y tour asignado creados exitosamente");
                                callback.onSuccess("¡Tour aceptado exitosamente!");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al actualizar subcolección: ", e);
                                callback.onError("Error al completar la aceptación");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar oferta: ", e);
                    callback.onError("Error al aceptar la oferta");
                });
    }
    
    /**
     * Obtener tours asignados para un guía específico
     */
    public void getToursAsignados(TourAsignadoCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        
        String guiaId = currentUser.getUid();
        Log.d(TAG, "Obteniendo tours asignados para guía: " + guiaId);
        
        db.collection(COLLECTION_ASIGNADOS)
                .whereEqualTo("guiaAsignado.identificadorUsuario", guiaId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TourAsignado> toursAsignados = new ArrayList<>();
                    
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Filtrar habilitado localmente
                            Boolean habilitado = document.getBoolean("habilitado");
                            if (habilitado == null || !habilitado) {
                                continue;
                            }
                            
                            TourAsignado tour = document.toObject(TourAsignado.class);
                            if (tour != null) {
                                tour.setId(document.getId());
                                toursAsignados.add(tour);
                                Log.d(TAG, "Tour asignado cargado: " + tour.getTitulo());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear tour asignado: " + document.getId(), e);
                        }
                    }
                    
                    // Ordenar por fechaRealizacion localmente
                    toursAsignados.sort((t1, t2) -> {
                        if (t1.getFechaRealizacion() == null) return 1;
                        if (t2.getFechaRealizacion() == null) return -1;
                        return t1.getFechaRealizacion().compareTo(t2.getFechaRealizacion());
                    });
                    
                    Log.d(TAG, "Total tours asignados cargados: " + toursAsignados.size());
                    callback.onSuccess(toursAsignados);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener tours asignados: ", e);
                    callback.onError("Error al cargar los tours asignados");
                });
    }
    
    // Interface para callback de tours asignados
    public interface TourAsignadoCallback {
        void onSuccess(List<TourAsignado> tours);
        void onError(String error);
    }
    
    public interface TourPrioritarioCallback {
        void onSuccess(TourAsignado tour);
        void onError(String error);
    }
    
    /**
     * 🎯 OBTENER TOUR PRIORITARIO CON ESTADOS UNIFICADOS
     * 
     * PRIORIDAD 1: Tour "en_curso" o "check_out" (máxima prioridad)
     * PRIORIDAD 2: Tour "check_in" que es HOY
     * PRIORIDAD 3: Tour "pendiente" más próximo
     */
    public void getTourPrioritario(TourPrioritarioCallback callback) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        
        String guiaId = currentUser.getUid();
        
        db.collection(COLLECTION_ASIGNADOS)
            .whereEqualTo("guiaAsignado.identificadorUsuario", guiaId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<TourAsignado> tours = new ArrayList<>();
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        // Filtrar habilitado localmente
                        Boolean habilitado = doc.getBoolean("habilitado");
                        if (habilitado == null || !habilitado) {
                            continue;
                        }
                        
                        TourAsignado tour = doc.toObject(TourAsignado.class);
                        if (tour != null) {
                            tour.setId(doc.getId());
                            tours.add(tour);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error convirtiendo tour: " + doc.getId(), e);
                    }
                }
                
                // Ordenar por fechaRealizacion localmente
                tours.sort((t1, t2) -> {
                    if (t1.getFechaRealizacion() == null) return 1;
                    if (t2.getFechaRealizacion() == null) return -1;
                    return t1.getFechaRealizacion().compareTo(t2.getFechaRealizacion());
                });
                
                TourAsignado tourPrioritario = null;
                TourAsignado tourCheckInDisponible = null;
                TourAsignado tourProximoInicio = null;
                
                Date ahora = new Date();
                
                for (TourAsignado tour : tours) {
                    String estado = tour.getEstado();
                    
                    // 🔥 PRIORIDAD 1: Tour en curso (máxima prioridad)
                    if ("en_curso".equals(estado)) {
                        callback.onSuccess(tour);
                        return;
                    }
                    
                    // 🛑 PRIORIDAD 2: Tour en check-out (alta prioridad)
                    if ("check_out".equals(estado)) {
                        callback.onSuccess(tour);
                        return;
                    }
                    
                    // ✅ PRIORIDAD 3: Tours con check-in habilitado
                    if ("check_in".equals(estado)) {
                        if (tourCheckInDisponible == null) {
                            tourCheckInDisponible = tour;
                        }
                    }
                    
                    // ⏰ PRIORIDAD 4: Tours confirmados que faltan 10 minutos o menos para iniciar
                    if ("confirmado".equals(estado) || "pendiente".equals(estado) || "programado".equals(estado)) {
                        long minutosParaInicio = calcularMinutosParaInicio(tour, ahora);
                        
                        // Solo considerar tours que faltan 10 minutos o menos para iniciar
                        if (minutosParaInicio >= 0 && minutosParaInicio <= 10) {
                            if (tourProximoInicio == null || minutosParaInicio < calcularMinutosParaInicio(tourProximoInicio, ahora)) {
                                tourProximoInicio = tour;
                            }
                        }
                    }
                }
                
                // 🎯 LÓGICA DE SELECCIÓN:
                // 1. Tour con check-in habilitado
                // 2. Tour que falta <= 10 minutos para iniciar
                // 3. No hay tours prioritarios
                if (tourCheckInDisponible != null) {
                    callback.onSuccess(tourCheckInDisponible);
                } else if (tourProximoInicio != null) {
                    callback.onSuccess(tourProximoInicio);
                } else {
                    callback.onError("No hay tours prioritarios en este momento");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error obteniendo tour prioritario", e);
                callback.onError("Error cargando tours: " + e.getMessage());
            });
    }
    
    /**
     * ⏰ CALCULAR MINUTOS QUE FALTAN PARA EL INICIO DEL TOUR
     * @return minutos (positivo = falta tiempo, negativo = ya pasó la hora)
     */
    private long calcularMinutosParaInicio(TourAsignado tour, Date ahora) {
        try {
            if (tour.getFechaRealizacion() == null || tour.getHoraInicio() == null) {
                return Long.MAX_VALUE; // Retornar valor alto si no hay datos
            }
            
            // Obtener fecha del tour
            Date fechaTour = tour.getFechaRealizacion().toDate();
            
            // Combinar fecha con hora de inicio
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            
            String fechaStr = dateFormat.format(fechaTour);
            String horaStr = tour.getHoraInicio();
            
            Date fechaHoraInicio = fullFormat.parse(fechaStr + " " + horaStr);
            
            if (fechaHoraInicio == null) {
                return Long.MAX_VALUE;
            }
            
            // Calcular diferencia en milisegundos y convertir a minutos
            long diffMs = fechaHoraInicio.getTime() - ahora.getTime();
            return diffMs / (60 * 1000);
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculando minutos para inicio", e);
            return Long.MAX_VALUE;
        }
    }
    
    /**
     * 🕐 VERIFICAR SI ES TOUR DE HOY
     */
    private boolean esTourDeHoy(TourAsignado tour) {
        if (tour.getFechaRealizacion() == null) return false;
        
        Date fechaTour = tour.getFechaRealizacion().toDate();
        Date hoy = new Date();
        
        // Comparar solo la fecha (sin hora)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(hoy).equals(sdf.format(fechaTour));
    }
    
    /**
     * ⏰ VERIFICAR SI YA ES HORA DE INICIO
     */
    private boolean yaEsHoraDeInicio(TourAsignado tour) {
        try {
            if (tour.getFechaRealizacion() == null || tour.getHoraInicio() == null) {
                return false;
            }
            
            // Combinar fecha (Timestamp) con hora de inicio (String)
            Date fechaTour = tour.getFechaRealizacion().toDate();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date horaInicio = timeFormat.parse(tour.getHoraInicio());
            
            // Crear fecha completa con fecha del tour + hora de inicio
            Date fechaHoraInicio = new Date(fechaTour.getTime() + horaInicio.getTime() - timeFormat.parse("00:00").getTime());
            Date ahora = new Date();
            
            // Es hora si ya pasó la hora de inicio
            return ahora.getTime() >= fechaHoraInicio.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "Error parseando fecha/hora", e);
            return false;
        }
    }
    
    /**
     * 📅 VERIFICAR SI ES TOUR FUTURO
     */
    private boolean esTourFuturo(TourAsignado tour) {
        if (tour.getFechaRealizacion() == null) return false;
        
        Date fechaTour = tour.getFechaRealizacion().toDate();
        Date hoy = new Date();
        
        // Comparar solo fechas (sin hora) para determinar si es futuro
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date fechaTourSinHora = sdf.parse(sdf.format(fechaTour));
            Date hoySinHora = sdf.parse(sdf.format(hoy));
            
            return fechaTourSinHora.getTime() > hoySinHora.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "Error comparando fechas", e);
            return fechaTour.getTime() > hoy.getTime();
        }
    }
    
    /**
     * 🔄 ACTUALIZAR ESTADO DE TOUR
     */
    public void actualizarEstadoTour(String tourId, String nuevoEstado, OperationCallback callback) {
        db.collection(COLLECTION_ASIGNADOS)
            .document(tourId)
            .update("estado", nuevoEstado)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Estado actualizado a: " + nuevoEstado);
                callback.onSuccess("Estado actualizado correctamente");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error actualizando estado", e);
                callback.onError("Error actualizando estado: " + e.getMessage());
            });
    }
    
    /**
     * ▶️ INICIAR TOUR (check_in → en_curso) - CON VALIDACIÓN DE ÚNICO TOUR ACTIVO
     */
    public void iniciarTour(String tourId, OperationCallback callback) {
        // Primero verificar si ya hay un tour en curso
        verificarTourActivoAntesCambio(tourId, "en_curso", new OperationCallback() {
            @Override
            public void onSuccess(String message) {
                // No hay conflictos, proceder con el cambio
                Map<String, Object> updates = new HashMap<>();
                updates.put("estado", "en_curso");
                updates.put("checkInRealizado", true);
                updates.put("horaCheckIn", Timestamp.now());
                updates.put("fechaActualizacion", Timestamp.now());
                
                db.collection(COLLECTION_ASIGNADOS)
                    .document(tourId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Tour iniciado exitosamente");
                        callback.onSuccess("Tour iniciado exitosamente");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error iniciando tour", e);
                        callback.onError("Error iniciando tour: " + e.getMessage());
                    });
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * 🔄 HABILITAR CHECK-IN (pendiente → check_in) - CON VALIDACIÓN DE ÚNICO TOUR ACTIVO
     */
    public void habilitarCheckIn(String tourId, OperationCallback callback) {
        // Primero verificar si ya hay un tour activo
        verificarTourActivoAntesCambio(tourId, "check_in", new OperationCallback() {
            @Override
            public void onSuccess(String message) {
                // No hay conflictos, proceder con el cambio
                Map<String, Object> updates = new HashMap<>();
                updates.put("estado", "check_in");
                updates.put("fechaActualizacion", Timestamp.now());
                
                db.collection(COLLECTION_ASIGNADOS)
                    .document(tourId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess("Check-in habilitado"))
                    .addOnFailureListener(e -> callback.onError("Error habilitando check-in: " + e.getMessage()));
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * 🔚 HABILITAR CHECK-OUT (en_curso → check_out)
     */
    public void habilitarCheckOut(String tourId, OperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "check_out");
        updates.put("fechaActualizacion", Timestamp.now());
        
        db.collection(COLLECTION_ASIGNADOS)
            .document(tourId)
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess("Check-out habilitado"))
            .addOnFailureListener(e -> callback.onError("Error habilitando check-out: " + e.getMessage()));
    }
    
    /**
     * 🏁 TERMINAR TOUR (check_out → completado)
     */
    public void terminarTour(String tourId, OperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "completado");
        updates.put("checkOutRealizado", true);
        updates.put("horaCheckOut", Timestamp.now());
        updates.put("fechaActualizacion", Timestamp.now());
        
        db.collection(COLLECTION_ASIGNADOS)
            .document(tourId)
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess("Tour completado exitosamente"))
            .addOnFailureListener(e -> callback.onError("Error completando tour: " + e.getMessage()));
    }
    
    /**
     * 🔄 ACTUALIZAR MOMENTO DEL TOUR
     * Método específico para cambiar momentoTour: pendiente → check_in → en_curso → check_out → terminado
     */
    public void actualizarMomentoTour(String tourId, String nuevoMomento, OperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("momentoTour", nuevoMomento);
        updates.put("fechaActualizacion", Timestamp.now());
        
        // Actualizar campos relacionados según el momento
        switch (nuevoMomento.toLowerCase()) {
            case "check_in":
                updates.put("horaCheckIn", Timestamp.now());
                updates.put("checkInRealizado", true);
                break;
            case "en_curso":
                updates.put("estado", "en_curso");
                if (!updates.containsKey("horaCheckIn")) {
                    updates.put("horaCheckIn", Timestamp.now());
                    updates.put("checkInRealizado", true);
                }
                break;
            case "check_out":
                updates.put("horaCheckOut", Timestamp.now());
                updates.put("checkOutRealizado", true);
                break;
            case "terminado":
                updates.put("estado", "completado");
                if (!updates.containsKey("horaCheckOut")) {
                    updates.put("horaCheckOut", Timestamp.now());
                    updates.put("checkOutRealizado", true);
                }
                break;
        }
        
        db.collection(COLLECTION_ASIGNADOS)
            .document(tourId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Momento tour actualizado a: " + nuevoMomento);
                callback.onSuccess("Momento del tour actualizado correctamente");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error actualizando momento tour", e);
                callback.onError("Error actualizando momento del tour: " + e.getMessage());
            });
    }
    
    /**
     * ▶️ CAMBIAR DE PENDIENTE A CHECK_IN - CON VALIDACIÓN DE ÚNICO TOUR ACTIVO
     * Para tours pendientes que están listos para comenzar
     */
    public void cambiarPendienteACheckIn(String tourId, OperationCallback callback) {
        // Primero verificar si ya hay un tour activo
        verificarTourActivoAntesCambio(tourId, "check_in", new OperationCallback() {
            @Override
            public void onSuccess(String message) {
                // No hay conflictos, proceder con el cambio
                Map<String, Object> updates = new HashMap<>();
                updates.put("estado", "check_in");
                updates.put("fechaActualizacion", Timestamp.now());
                
                db.collection(COLLECTION_ASIGNADOS)
                    .document(tourId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Tour cambiado a estado check_in: " + tourId);
                        callback.onSuccess("Tour listo para check-in");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error cambiando a check_in", e);
                        callback.onError("Error cambiando estado: " + e.getMessage());
                    });
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * 🛑 CAMBIAR DE EN_CURSO A CHECK_OUT
     * Para tours en curso que están listos para terminar
     */
    public void cambiarEnCursoACheckOut(String tourId, OperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "check_out");
        updates.put("fechaActualizacion", Timestamp.now());
        
        db.collection(COLLECTION_ASIGNADOS)
            .document(tourId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Tour cambiado a estado check_out: " + tourId);
                callback.onSuccess("Tour listo para check-out");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error cambiando a check_out", e);
                callback.onError("Error cambiando estado: " + e.getMessage());
            });
    }

    /**
     * �️ VERIFICAR TOUR ACTIVO ANTES DE CAMBIO DE ESTADO
     * Asegura que solo un tour puede estar en estado activo (check_in, en_curso, check_out) a la vez
     */
    private void verificarTourActivoAntesCambio(String tourId, String nuevoEstado, OperationCallback callback) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        
        String guiaId = currentUser.getUid();
        
        // Obtener todos los tours del guía y verificar si hay alguno activo
        db.collection(COLLECTION_ASIGNADOS)
            .whereEqualTo("guiaAsignado.identificadorUsuario", guiaId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                String tourActivoExistente = null;
                String tituloTourActivo = null;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    // Filtrar habilitado localmente
                    Boolean habilitado = doc.getBoolean("habilitado");
                    if (habilitado == null || !habilitado) {
                        continue;
                    }
                    
                    String docId = doc.getId();
                    String estado = doc.getString("estado");
                    
                    // Si es el mismo tour que queremos cambiar, saltarlo
                    if (docId.equals(tourId)) {
                        continue;
                    }
                    
                    // Verificar si hay otro tour en estado activo
                    if (estado != null && esEstadoActivo(estado)) {
                        tourActivoExistente = docId;
                        tituloTourActivo = doc.getString("titulo");
                        break;
                    }
                }
                
                // Si no hay tour activo existente, permitir el cambio
                if (tourActivoExistente == null) {
                    callback.onSuccess("Ningún tour activo, cambio permitido");
                } else {
                    // Si ya hay un tour activo, no permitir el cambio
                    String mensaje = String.format(
                        "Ya tienes un tour activo: %s. Solo puedes tener un tour activo a la vez.", 
                        tituloTourActivo != null ? tituloTourActivo : "Tour sin título"
                    );
                    callback.onError(mensaje);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error verificando tours activos", e);
                callback.onError("Error verificando tours activos: " + e.getMessage());
            });
    }
    
    /**
     * 🔍 VERIFICAR SI UN ESTADO ES ACTIVO
     * Estados activos: check_in, en_curso, check_out
     * Estados no activos: pendiente, completado, cancelado
     */
    private boolean esEstadoActivo(String estado) {
        if (estado == null) return false;
        
        String estadoLower = estado.toLowerCase();
        return estadoLower.equals("check_in") || 
               estadoLower.equals("en_curso") || 
               estadoLower.equals("check_out");
    }

    /**
     * �🔧 HELPER: Convertir fecha String a Timestamp
     * Convierte fechas en formato "dd/MM/yyyy" a Timestamp para compatibilidad
     */
    private Timestamp convertirFechaStringATimestamp(String fechaString) {
        if (fechaString == null || fechaString.isEmpty()) {
            return Timestamp.now(); // Fallback a fecha actual
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fecha = sdf.parse(fechaString);
            return new Timestamp(fecha);
        } catch (ParseException e) {
            Log.e(TAG, "Error parseando fecha: " + fechaString, e);
            return Timestamp.now(); // Fallback a fecha actual
        }
    }
    
    // ==================== ACEPTAR/RECHAZAR OFERTAS DE TOUR ====================
    
    /**
     * Acepta una oferta de tour
     * - Actualiza estado en guias_ofertados a "aceptado"
     * - Crea documento en tours_asignados
     * - Actualiza estadísticas del guía
     */
    public void aceptarOfertaTour(@NonNull String ofertaId, @NonNull AccionOfertaCallback callback) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        
        String guiaId = currentUser.getUid();
        Log.d(TAG, "Guía " + guiaId + " aceptando oferta " + ofertaId);
        
        // 1. Cargar la oferta completa
        db.collection(COLLECTION_OFERTAS)
            .document(ofertaId)
            .get()
            .addOnSuccessListener(ofertaDoc -> {
                if (!ofertaDoc.exists()) {
                    callback.onError("La oferta de tour no existe");
                    return;
                }
                
                // Validar tiempo: debe quedar al menos 12 horas
                Object fechaRealizacion = ofertaDoc.get("fechaRealizacion");
                String horaInicio = ofertaDoc.getString("horaInicio");
                
                if (!com.example.connectifyproject.utils.TourTimeValidator.puedeAceptarOferta(fechaRealizacion, horaInicio)) {
                    String mensaje = com.example.connectifyproject.utils.TourTimeValidator.getMensajeTourPendienteBloqueado(fechaRealizacion, horaInicio);
                    callback.onError("No se puede aceptar: " + mensaje);
                    return;
                }
                
                // 2. Actualizar estado en guias_ofertados
                Map<String, Object> actualizacionOfrecimiento = new HashMap<>();
                actualizacionOfrecimiento.put("estadoOferta", "aceptado");
                actualizacionOfrecimiento.put("fechaRespuesta", Timestamp.now());
                actualizacionOfrecimiento.put("vistoAdmin", false); // Admin debe ver la aceptación
                
                db.collection(COLLECTION_OFERTAS)
                    .document(ofertaId)
                    .collection("guias_ofertados")
                    .document(guiaId)
                    .update(actualizacionOfrecimiento)
                    .addOnSuccessListener(aVoid -> {
                        // 3. Rechazar automáticamente otras ofertas con conflicto de horario
                        rechazarOfertasConConflicto(guiaId, ofertaDoc, () -> {
                            // 4. Crear tour asignado
                            crearTourAsignado(ofertaDoc, guiaId, callback);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error actualizando ofrecimiento", e);
                        callback.onError("Error al aceptar oferta: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error cargando oferta", e);
                callback.onError("Error al cargar oferta: " + e.getMessage());
            });
    }
    
    /**
     * Rechaza automáticamente todas las ofertas del guía que tengan conflicto de horario
     * con la oferta que acaba de aceptar
     */
    private void rechazarOfertasConConflicto(String guiaId, DocumentSnapshot ofertaAceptada, Runnable onComplete) {
        Object fechaAceptada = ofertaAceptada.get("fechaRealizacion");
        String horaInicioAceptada = ofertaAceptada.getString("horaInicio");
        String horaFinAceptada = ofertaAceptada.getString("horaFin");
        
        if (fechaAceptada == null || horaInicioAceptada == null || horaFinAceptada == null) {
            onComplete.run();
            return;
        }
        
        Log.d(TAG, "Buscando ofertas con conflicto para guía " + guiaId);
        
        // Buscar todas las ofertas pendientes del guía
        db.collection(COLLECTION_OFERTAS)
            .whereEqualTo("habilitado", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int ofertasRechazadas = 0;
                
                for (DocumentSnapshot ofertaDoc : querySnapshot.getDocuments()) {
                    String ofertaId = ofertaDoc.getId();
                    
                    // Verificar si el guía tiene esta oferta pendiente
                    db.collection(COLLECTION_OFERTAS)
                        .document(ofertaId)
                        .collection("guias_ofertados")
                        .document(guiaId)
                        .get()
                        .addOnSuccessListener(guiaOfertadoDoc -> {
                            if (guiaOfertadoDoc.exists()) {
                                String estadoOferta = guiaOfertadoDoc.getString("estadoOferta");
                                
                                // Solo procesar si está pendiente
                                if ("pendiente".equals(estadoOferta)) {
                                    Object fechaOferta = ofertaDoc.get("fechaRealizacion");
                                    String horaInicioOferta = ofertaDoc.getString("horaInicio");
                                    String horaFinOferta = ofertaDoc.getString("horaFin");
                                    
                                    // Verificar si es el mismo día
                                    if (esMismaFecha(fechaAceptada, fechaOferta)) {
                                        // Verificar conflicto de horario
                                        if (hayConflictoHorario(horaInicioAceptada, horaFinAceptada, 
                                                               horaInicioOferta, horaFinOferta)) {
                                            
                                            Log.d(TAG, "Conflicto detectado con oferta " + ofertaId + 
                                                      " - Rechazando automáticamente");
                                            
                                            // Rechazar la oferta con conflicto
                                            Map<String, Object> actualizacion = new HashMap<>();
                                            actualizacion.put("estadoOferta", "rechazado");
                                            actualizacion.put("fechaRespuesta", Timestamp.now());
                                            actualizacion.put("motivoRechazo", "Conflicto de horario - El guía aceptó otro tour");
                                            actualizacion.put("vistoAdmin", false);
                                            
                                            db.collection(COLLECTION_OFERTAS)
                                                .document(ofertaId)
                                                .collection("guias_ofertados")
                                                .document(guiaId)
                                                .update(actualizacion)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Oferta " + ofertaId + " rechazada automáticamente");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error al rechazar oferta " + ofertaId, e);
                                                });
                                        }
                                    }
                                }
                            }
                        });
                }
                
                // Continuar inmediatamente (las actualizaciones son asíncronas)
                onComplete.run();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error buscando ofertas con conflicto", e);
                onComplete.run(); // Continuar aunque falle
            });
    }
    
    /**
     * Verifica si dos fechas son el mismo día
     */
    private boolean esMismaFecha(Object fecha1, Object fecha2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fecha1Str = "";
            String fecha2Str = "";
            
            if (fecha1 instanceof Timestamp) {
                fecha1Str = sdf.format(((Timestamp) fecha1).toDate());
            } else if (fecha1 instanceof String) {
                fecha1Str = (String) fecha1;
            }
            
            if (fecha2 instanceof Timestamp) {
                fecha2Str = sdf.format(((Timestamp) fecha2).toDate());
            } else if (fecha2 instanceof String) {
                fecha2Str = (String) fecha2;
            }
            
            return fecha1Str.equals(fecha2Str);
        } catch (Exception e) {
            Log.e(TAG, "Error comparando fechas", e);
            return false;
        }
    }
    
    /**
     * Verifica si dos rangos de horarios se solapan
     */
    private boolean hayConflictoHorario(String inicio1, String fin1, String inicio2, String fin2) {
        if (inicio1 == null || fin1 == null || inicio2 == null || fin2 == null) {
            return false;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date i1 = sdf.parse(inicio1);
            Date f1 = sdf.parse(fin1);
            Date i2 = sdf.parse(inicio2);
            Date f2 = sdf.parse(fin2);
            
            // Solapamiento: (inicio1 < fin2) AND (fin1 > inicio2)
            return i1.before(f2) && f1.after(i2);
        } catch (Exception e) {
            Log.e(TAG, "Error comparando horarios", e);
            return false;
        }
    }
    
    /**
     * Crea un tour asignado a partir de una oferta aceptada
     */
    private void crearTourAsignado(DocumentSnapshot ofertaDoc, String guiaId, AccionOfertaCallback callback) {
    // Cargar datos del guía
    db.collection("usuarios")
        .document(guiaId)
        .get()
        .addOnSuccessListener(guiaDoc -> {
            if (!guiaDoc.exists()) {
                callback.onError("Datos del guía no encontrados");
                return;
            }
            
            // Crear documento de tour asignado
            Map<String, Object> tourAsignado = new HashMap<>();
            
            // Copiar datos de la oferta
            tourAsignado.put("ofertaTourId", ofertaDoc.getId());
            tourAsignado.put("titulo", ofertaDoc.getString("titulo"));
            tourAsignado.put("descripcion", ofertaDoc.getString("descripcion"));
            tourAsignado.put("precio", ofertaDoc.getDouble("precio"));
            tourAsignado.put("duracion", ofertaDoc.getString("duracion"));
            
            // Convertir fechaRealizacion de String a Timestamp si es necesario
            Object fechaRealizacionObj = ofertaDoc.get("fechaRealizacion");
            if (fechaRealizacionObj instanceof Timestamp) {
                tourAsignado.put("fechaRealizacion", fechaRealizacionObj);
            } else if (fechaRealizacionObj instanceof String) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = sdf.parse((String) fechaRealizacionObj);
                    tourAsignado.put("fechaRealizacion", new Timestamp(date));
                } catch (Exception e) {
                    tourAsignado.put("fechaRealizacion", Timestamp.now());
                }
            }
            
            tourAsignado.put("horaInicio", ofertaDoc.getString("horaInicio"));
            tourAsignado.put("horaFin", ofertaDoc.getString("horaFin"));
            tourAsignado.put("empresaId", ofertaDoc.getString("empresaId"));
            tourAsignado.put("nombreEmpresa", ofertaDoc.getString("nombreEmpresa"));
            tourAsignado.put("correoEmpresa", ofertaDoc.getString("correoEmpresa"));
            tourAsignado.put("pagoGuia", ofertaDoc.getDouble("pagoGuia"));
            
            // Copiar itinerario y agregar campos de control
            List<Map<String, Object>> itinerarioOferta = (List<Map<String, Object>>) ofertaDoc.get("itinerario");
            if (itinerarioOferta != null) {
                List<Map<String, Object>> itinerarioAsignado = new ArrayList<>();
                for (Map<String, Object> punto : itinerarioOferta) {
                    Map<String, Object> puntoAsignado = new HashMap<>(punto);
                    puntoAsignado.put("completado", false);
                    puntoAsignado.put("horaLlegada", null);
                    puntoAsignado.put("horaSalida", null);
                    itinerarioAsignado.add(puntoAsignado);
                }
                tourAsignado.put("itinerario", itinerarioAsignado);
            }
            
            tourAsignado.put("serviciosAdicionales", ofertaDoc.get("serviciosAdicionales"));
            tourAsignado.put("idiomasRequeridos", ofertaDoc.get("idiomasRequeridos"));
            tourAsignado.put("consideraciones", ofertaDoc.getString("consideraciones"));
            
            // ✅ IMÁGENES - SIEMPRE COPIAR ANTES DE GUARDAR
            String imagenPrincipal = ofertaDoc.getString("imagenPrincipal");
            List<String> imagenesUrls = (List<String>) ofertaDoc.get("imagenesUrls");
            
            Log.d(TAG, "=== COPIANDO IMÁGENES ===");
            Log.d(TAG, "Imagen principal: " + imagenPrincipal);
            Log.d(TAG, "Array imágenes: " + imagenesUrls);
            
            // Guardar imagen principal (siempre, aunque sea null o vacío)
            tourAsignado.put("imagenPrincipal", imagenPrincipal != null ? imagenPrincipal : "");
            
            // Guardar array de imágenes (siempre, aunque esté vacío)
            if (imagenesUrls == null) {
                imagenesUrls = new ArrayList<>();
            }
            tourAsignado.put("imagenesUrls", imagenesUrls);
            
            Log.d(TAG, "Total imágenes copiadas: " + imagenesUrls.size());
            Log.d(TAG, "========================");
            
            // Datos del guía asignado (completos)
            Map<String, Object> guiaAsignado = new HashMap<>();
            guiaAsignado.put("identificadorUsuario", guiaId);
            
            // Concatenar nombre completo
            String nombre = guiaDoc.getString("nombre");
            String apellido = guiaDoc.getString("apellido");
            String nombresCompletos = "";
            if (nombre != null && apellido != null) {
                nombresCompletos = nombre + " " + apellido;
            } else if (nombre != null) {
                nombresCompletos = nombre;
            }
            guiaAsignado.put("nombresCompletos", nombresCompletos);
            
            guiaAsignado.put("correoElectronico", guiaDoc.getString("email"));
            guiaAsignado.put("numeroTelefono", guiaDoc.getString("telefono"));
            guiaAsignado.put("fechaAsignacion", Timestamp.now());
            tourAsignado.put("guiaAsignado", guiaAsignado);
            
            // Estado y metadatos
            tourAsignado.put("estado", "confirmado");
            tourAsignado.put("habilitado", true);
            tourAsignado.put("fechaAsignacion", Timestamp.now());
            tourAsignado.put("fechaCreacion", Timestamp.now());
            tourAsignado.put("fechaActualizacion", Timestamp.now());
            tourAsignado.put("checkInRealizado", false);
            tourAsignado.put("checkOutRealizado", false);
            tourAsignado.put("horaCheckIn", null);
            tourAsignado.put("horaCheckOut", null);
            
            // Campos adicionales
            tourAsignado.put("numeroParticipantesTotal", 0);
            tourAsignado.put("participantes", new ArrayList<>());
            tourAsignado.put("reseniasClientes", new ArrayList<>());
            tourAsignado.put("comentariosGuia", "");
            tourAsignado.put("calificacionPromedio", 0);
            
            // ✅ LOG COMPLETO ANTES DE GUARDAR
            Log.d(TAG, "=== Tour Asignado a crear ===");
            Log.d(TAG, "imagenPrincipal: " + tourAsignado.get("imagenPrincipal"));
            Log.d(TAG, "imagenesUrls: " + tourAsignado.get("imagenesUrls"));
            Log.d(TAG, "============================");
            
            // Guardar tour asignado
            db.collection(COLLECTION_ASIGNADOS)
                .add(tourAsignado)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Tour asignado creado: " + docRef.getId());
                    
                    // Actualizar oferta: limpiar guiaSeleccionadoActual
                    Map<String, Object> actualizacionOferta = new HashMap<>();
                    actualizacionOferta.put("guiaSeleccionadoActual", null);
                    actualizacionOferta.put("fechaActualizacion", Timestamp.now());
                    
                    db.collection(COLLECTION_OFERTAS)
                        .document(ofertaDoc.getId())
                        .update(actualizacionOferta)
                        .addOnSuccessListener(aVoid -> {
                            callback.onSuccess("Tour aceptado exitosamente");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error actualizando oferta", e);
                            callback.onSuccess("Tour aceptado (advertencia al actualizar oferta)");
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando tour asignado", e);
                    callback.onError("Error al crear tour asignado: " + e.getMessage());
                });
        })
        .addOnFailureListener(e -> {
            Log.e(TAG, "Error cargando datos del guía", e);
            callback.onError("Error al cargar datos del guía: " + e.getMessage());
        });
}
    
    /**
     * Rechaza una oferta de tour
     * - Actualiza estado en guias_ofertados a "rechazado"
     * - Guarda motivo del rechazo
     * - Notifica al admin
     */
    public void rechazarOfertaTour(@NonNull String ofertaId, @Nullable String motivoRechazo, @NonNull AccionOfertaCallback callback) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            callback.onError("Usuario no autenticado");
            return;
        }
        
        String guiaId = currentUser.getUid();
        Log.d(TAG, "Guía " + guiaId + " rechazando oferta " + ofertaId);
        
        // Actualizar estado en guias_ofertados
        Map<String, Object> actualizacionOfrecimiento = new HashMap<>();
        actualizacionOfrecimiento.put("estadoOferta", "rechazado");
        actualizacionOfrecimiento.put("fechaRespuesta", Timestamp.now());
        actualizacionOfrecimiento.put("motivoRechazo", motivoRechazo != null ? motivoRechazo : "Sin motivo especificado");
        actualizacionOfrecimiento.put("vistoAdmin", false); // Admin debe ver el rechazo
        
        db.collection(COLLECTION_OFERTAS)
            .document(ofertaId)
            .collection("guias_ofertados")
            .document(guiaId)
            .update(actualizacionOfrecimiento)
            .addOnSuccessListener(aVoid -> {
                // Limpiar guiaSeleccionadoActual en la oferta
                Map<String, Object> actualizacionOferta = new HashMap<>();
                actualizacionOferta.put("guiaSeleccionadoActual", null);
                actualizacionOferta.put("fechaActualizacion", Timestamp.now());
                
                db.collection(COLLECTION_OFERTAS)
                    .document(ofertaId)
                    .update(actualizacionOferta)
                    .addOnSuccessListener(aVoid2 -> {
                        Log.d(TAG, "Oferta rechazada exitosamente");
                        callback.onSuccess("Tour rechazado. El administrador será notificado.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error actualizando oferta", e);
                        callback.onSuccess("Tour rechazado (advertencia al actualizar oferta)");
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error rechazando oferta", e);
                callback.onError("Error al rechazar oferta: " + e.getMessage());
            });
    }
    
    // Callback para aceptar/rechazar ofertas
    public interface AccionOfertaCallback {
        void onSuccess(String mensaje);
        void onError(String error);
    }
    
    /**
     * 🚫 AUTO-CANCELACIÓN DE TOURS SIN PARTICIPANTES
     * Verificar a la hora de inicio si hay participantes inscritos.
     * Si no hay, cancelar el tour y ajustar el pago del guía al 15%
     */
    public void verificarYCancelarTourSinParticipantes(String tourId, OperationCallback callback) {
        db.collection(COLLECTION_ASIGNADOS)
            .document(tourId)
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    callback.onError("Tour no encontrado");
                    return;
                }
                
                String estado = doc.getString("estado");
                List<Map<String, Object>> participantes = (List<Map<String, Object>>) doc.get("participantes");
                
                // Solo cancelar si está en estado pendiente/confirmado/programado
                if (estado != null && (estado.equals("pendiente") || estado.equals("confirmado") || estado.equals("programado"))) {
                    
                    // Verificar si hay participantes
                    if (participantes == null || participantes.isEmpty()) {
                        // ✅ NUEVA REGLA: Validar que falten EXACTAMENTE 2 horas o menos
                        Object fechaRealizacion = doc.get("fechaRealizacion");
                        String horaInicio = doc.getString("horaInicio");
                        
                        double horasRestantes = com.example.connectifyproject.utils.TourTimeValidator
                            .calcularHorasHastaInicio(fechaRealizacion, horaInicio);
                        
                        // Solo cancelar si faltan 2 horas o menos (y aún no ha iniciado)
                        if (horasRestantes > 2.0) {
                            callback.onSuccess("Aún faltan más de 2 horas, no se cancela todavía");
                            return;
                        }
                        
                        if (horasRestantes < 0) {
                            callback.onSuccess("El tour ya inició, no se puede cancelar");
                            return;
                        }
                        
                        // Obtener datos para crear pago y mover reservas
                        String guiaId = null;
                        Map<String, Object> guiaAsignado = (Map<String, Object>) doc.get("guiaAsignado");
                        if (guiaAsignado != null) {
                            guiaId = (String) guiaAsignado.get("id");
                        }
                        
                        Double pagoOriginal = doc.getDouble("pagoGuia");
                        double pagoReducido = pagoOriginal != null ? pagoOriginal * 0.15 : 0;
                        String titulo = doc.getString("titulo");
                        String empresaId = doc.getString("empresaId");
                        
                        // 1. Actualizar estado del tour
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("estado", "cancelado");
                        updates.put("pagoGuia", pagoReducido);
                        updates.put("motivoCancelacion", "Cancelación automática por falta de participantes");
                        updates.put("fechaCancelacion", Timestamp.now());
                        updates.put("fechaActualizacion", Timestamp.now());
                        updates.put("habilitado", false);
                        
                        final String guiaIdFinal = guiaId;
                        final String empresaIdFinal = empresaId;
                        
                        db.collection(COLLECTION_ASIGNADOS)
                            .document(tourId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Tour cancelado automáticamente. Pago reducido a 15%");
                                
                                // 2. Crear registro de pago (15% al guía)
                                if (guiaIdFinal != null && pagoReducido > 0) {
                                    crearPagoCancelacion(tourId, guiaIdFinal, empresaIdFinal, pagoReducido, titulo, callback);
                                } else {
                                    callback.onSuccess("Tour cancelado. Pago del guía: S/. " + String.format("%.2f", pagoReducido));
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error cancelando tour", e);
                                callback.onError("Error al cancelar tour: " + e.getMessage());
                            });
                    } else {
                        callback.onSuccess("El tour tiene participantes inscritos, no se cancela");
                    }
                } else {
                    callback.onSuccess("El tour no está en estado válido para cancelación automática");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error verificando tour", e);
                callback.onError("Error al verificar tour: " + e.getMessage());
            });
    }
    
    /**
     * Crear registro de pago cuando se cancela un tour (15% al guía)
     */
    private void crearPagoCancelacion(String tourId, String guiaId, String empresaId, 
                                      double monto, String nombreTour, OperationCallback callback) {
        Map<String, Object> pago = new HashMap<>();
        pago.put("fecha", Timestamp.now());
        pago.put("monto", monto);
        pago.put("nombreTour", nombreTour != null ? nombreTour : "Tour cancelado");
        pago.put("tipoPago", "A Guia");
        pago.put("uidUsuarioPaga", empresaId); // La empresa/admin paga
        pago.put("uidUsuarioRecibe", guiaId);  // El guía recibe
        pago.put("tourId", tourId);
        pago.put("motivoPago", "Compensación por cancelación de tour (15%)");
        
        db.collection("pagos")
            .add(pago)
            .addOnSuccessListener(docRef -> {
                Log.d(TAG, "✅ Pago de cancelación registrado: S/. " + monto);
                callback.onSuccess("Tour cancelado. Pago compensatorio registrado: S/. " + String.format("%.2f", monto));
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "⚠️ Error registrando pago de cancelación", e);
                callback.onSuccess("Tour cancelado, pero hubo error al registrar el pago");
            });
    }
    
    /**
     * ❌ CANCELACIÓN MANUAL DE TOUR POR GUÍA
     * Permite al guía cancelar un tour manualmente. 
     * Crea pago del 15%, mueve participantes a reservas_canceladas
     */
    public void cancelarTourManual(String tourId, String motivoCancelacion, OperationCallback callback) {
        if (motivoCancelacion == null || motivoCancelacion.trim().isEmpty()) {
            motivoCancelacion = "Cancelación manual";
        }
        
        final String motivoFinal = motivoCancelacion;
        
        db.collection(COLLECTION_ASIGNADOS)
            .document(tourId)
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    callback.onError("Tour no encontrado");
                    return;
                }
                
                String estado = doc.getString("estado");
                
                // Solo permitir cancelación si está en estados válidos
                if (estado == null || (!estado.equals("confirmado") && !estado.equals("pendiente") && !estado.equals("programado"))) {
                    callback.onError("No se puede cancelar un tour en estado: " + estado);
                    return;
                }
                
                // Obtener datos necesarios
                List<Map<String, Object>> participantes = (List<Map<String, Object>>) doc.get("participantes");
                String guiaId = null;
                Map<String, Object> guiaAsignado = (Map<String, Object>) doc.get("guiaAsignado");
                if (guiaAsignado != null) {
                    guiaId = (String) guiaAsignado.get("id");
                }
                
                Double pagoOriginal = doc.getDouble("pagoGuia");
                double pagoReducido = pagoOriginal != null ? pagoOriginal * 0.15 : 0;
                String titulo = doc.getString("titulo");
                String empresaId = doc.getString("empresaId");
                Object fechaRealizacion = doc.get("fechaRealizacion");
                String horaInicio = doc.getString("horaInicio");
                String horaFin = doc.getString("horaFin");
                
                // 1. Actualizar estado del tour
                Map<String, Object> updates = new HashMap<>();
                updates.put("estado", "cancelado");
                updates.put("pagoGuia", pagoReducido);
                updates.put("motivoCancelacion", motivoFinal);
                updates.put("fechaCancelacion", Timestamp.now());
                updates.put("fechaActualizacion", Timestamp.now());
                updates.put("habilitado", false);
                
                final String guiaIdFinal = guiaId;
                final String empresaIdFinal = empresaId;
                final List<Map<String, Object>> participantesFinal = participantes;
                
                db.collection(COLLECTION_ASIGNADOS)
                    .document(tourId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Tour cancelado manualmente. Pago reducido a 15%");
                        
                        // 2. Crear registro de pago (15% al guía)
                        if (guiaIdFinal != null && pagoReducido > 0) {
                            crearPagoManualCancelacion(tourId, guiaIdFinal, empresaIdFinal, pagoReducido, titulo, 
                                participantesFinal, fechaRealizacion, horaInicio, horaFin, motivoFinal, callback);
                        } else {
                            // Mover participantes sin crear pago
                            if (participantesFinal != null && !participantesFinal.isEmpty()) {
                                moverParticipantesACanceladas(tourId, participantesFinal, titulo, fechaRealizacion, horaInicio, horaFin, motivoFinal, callback);
                            } else {
                                callback.onSuccess("Tour cancelado exitosamente");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error cancelando tour manualmente", e);
                        callback.onError("Error al cancelar tour: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error obteniendo tour para cancelar", e);
                callback.onError("Error al obtener datos del tour: " + e.getMessage());
            });
    }
    
    /**
     * Crear registro de pago para cancelación manual
     */
    private void crearPagoManualCancelacion(String tourId, String guiaId, String empresaId, 
                                            double monto, String nombreTour,
                                            List<Map<String, Object>> participantes,
                                            Object fechaRealizacion, String horaInicio, String horaFin,
                                            String motivo, OperationCallback callback) {
        Map<String, Object> pago = new HashMap<>();
        pago.put("fecha", Timestamp.now());
        pago.put("monto", monto);
        pago.put("nombreTour", nombreTour != null ? nombreTour : "Tour cancelado");
        pago.put("tipoPago", "A Guia");
        pago.put("uidUsuarioPaga", empresaId);
        pago.put("uidUsuarioRecibe", guiaId);
        pago.put("tourId", tourId);
        pago.put("motivoPago", "Compensación por cancelación manual (15%)");
        
        db.collection("pagos")
            .add(pago)
            .addOnSuccessListener(docRef -> {
                Log.d(TAG, "✅ Pago de cancelación manual registrado: S/. " + monto);
                
                // 3. Mover participantes a reservas_canceladas
                if (participantes != null && !participantes.isEmpty()) {
                    moverParticipantesACanceladas(tourId, participantes, nombreTour, fechaRealizacion, horaInicio, horaFin, motivo, callback);
                } else {
                    callback.onSuccess("Tour cancelado. Pago registrado: S/. " + String.format("%.2f", monto));
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "⚠️ Error registrando pago de cancelación manual", e);
                callback.onError("Error al registrar el pago: " + e.getMessage());
            });
    }
    
    /**
     * Mover participantes a la colección reservas_canceladas
     */
    private void moverParticipantesACanceladas(String tourId, List<Map<String, Object>> participantes,
                                              String nombreTour, Object fechaRealizacion,
                                              String horaInicio, String horaFin,
                                              String motivoCancelacion, OperationCallback callback) {
        if (participantes == null || participantes.isEmpty()) {
            callback.onSuccess("Tour cancelado sin participantes");
            return;
        }
        
        int totalParticipantes = participantes.size();
        final int[] procesados = {0};
        final boolean[] errorOcurrido = {false};
        
        for (Map<String, Object> participante : participantes) {
            Map<String, Object> reservaCancelada = new HashMap<>();
            reservaCancelada.put("tourId", tourId);
            reservaCancelada.put("tourTitulo", nombreTour);
            reservaCancelada.put("fechaRealizacion", fechaRealizacion);
            reservaCancelada.put("horaInicio", horaInicio);
            reservaCancelada.put("horaFin", horaFin);
            reservaCancelada.put("fechaCancelacion", Timestamp.now());
            reservaCancelada.put("motivoCancelacion", motivoCancelacion);
            
            // Copiar datos del participante
            reservaCancelada.put("clienteId", participante.get("clienteId"));
            reservaCancelada.put("nombre", participante.get("nombre"));
            reservaCancelada.put("correo", participante.get("correo"));
            reservaCancelada.put("numeroPersonas", participante.get("numeroPersonas"));
            reservaCancelada.put("montoTotal", participante.get("montoTotal"));
            
            db.collection("reservas_canceladas")
                .add(reservaCancelada)
                .addOnSuccessListener(docRef -> {
                    procesados[0]++;
                    Log.d(TAG, "✅ Reserva movida a canceladas: " + procesados[0] + "/" + totalParticipantes);
                    
                    if (procesados[0] == totalParticipantes && !errorOcurrido[0]) {
                        callback.onSuccess("Tour cancelado. " + totalParticipantes + " reservas movidas a canceladas.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!errorOcurrido[0]) {
                        errorOcurrido[0] = true;
                        Log.e(TAG, "⚠️ Error moviendo reservas a canceladas", e);
                        callback.onError("Tour cancelado, pero error al mover reservas: " + e.getMessage());
                    }
                });
        }
    }
    
    /**
     * 🔍 VERIFICAR TOURS A PUNTO DE INICIAR (PARA CRON/SCHEDULER)
     * Obtener todos los tours que deben iniciar en los próximos minutos
     * y verificar si tienen participantes
     */
    public void verificarToursParaAutoCancelacion(OperationCallback callback) {
        Date ahora = new Date();
        Date hace5Minutos = new Date(ahora.getTime() - (5 * 60 * 1000)); // 5 minutos atrás
        
        db.collection(COLLECTION_ASIGNADOS)
            .whereIn("estado", java.util.Arrays.asList("pendiente", "confirmado", "programado"))
            .whereEqualTo("habilitado", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int toursVerificados = 0;
                int toursCancelados = 0;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                        String horaInicio = doc.getString("horaInicio");
                        
                        if (fechaRealizacion != null && horaInicio != null) {
                            // Combinar fecha con hora
                            Date fechaTour = fechaRealizacion.toDate();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                            
                            String fechaStr = dateFormat.format(fechaTour);
                            Date fechaHoraInicio = fullFormat.parse(fechaStr + " " + horaInicio);
                            
                            // Verificar si la hora de inicio ya pasó (hace menos de 5 minutos)
                            if (fechaHoraInicio != null && 
                                fechaHoraInicio.after(hace5Minutos) && 
                                fechaHoraInicio.before(ahora)) {
                                
                                // Este tour debería haber iniciado, verificar participantes
                                String tourId = doc.getId();
                                toursVerificados++;
                                
                                verificarYCancelarTourSinParticipantes(tourId, new OperationCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        if (message.contains("cancelado automáticamente")) {
                                            Log.d(TAG, "Tour " + tourId + " cancelado: " + message);
                                        }
                                    }
                                    
                                    @Override
                                    public void onError(String error) {
                                        Log.e(TAG, "Error verificando tour " + tourId + ": " + error);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando tour: " + doc.getId(), e);
                    }
                }
                
                callback.onSuccess("Verificados " + toursVerificados + " tours");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error verificando tours para auto-cancelación", e);
                callback.onError("Error: " + e.getMessage());
            });
    }
}

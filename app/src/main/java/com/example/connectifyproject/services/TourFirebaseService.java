package com.example.connectifyproject.services;

import android.util.Log;
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
        
        // Primero obtener todas las ofertas publicadas
        db.collection(COLLECTION_OFERTAS)
                .whereEqualTo("estado", "publicado")
                .whereEqualTo("habilitado", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OfertaTour> ofertasParaGuia = new ArrayList<>();
                    int totalOfertas = queryDocumentSnapshots.size();
                    
                    if (totalOfertas == 0) {
                        Log.d(TAG, "No hay ofertas publicadas");
                        callback.onSuccess(ofertasParaGuia);
                        return;
                    }
                    
                    // Counter para saber cuándo hemos verificado todas las ofertas
                    final int[] verificadas = {0};
                    
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
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
        // Ordenar por fecha de realización del tour
        ofertas.sort((o1, o2) -> {
            try {
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
                                    callback.onSuccess("Oferta rechazada correctamente");
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
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        OfertaTour oferta = documentSnapshot.toObject(OfertaTour.class);
                        if (oferta != null && "publicado".equals(oferta.getEstado())) {
                            
                            // Primero obtener datos del guía
                            db.collection(COLLECTION_USUARIOS)
                                    .document(guiaId)
                                    .get()
                                    .addOnSuccessListener(guiaDoc -> {
                                        if (guiaDoc.exists()) {
                                            // Crear documento en tours_asignados
                                            crearTourAsignado(oferta, guiaDoc, ofertaId, guiaId, callback);
                                        } else {
                                            callback.onError("Datos del guía no encontrados");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al obtener datos del guía: ", e);
                                        callback.onError("Error al verificar datos del guía");
                                    });
                            
                        } else {
                            callback.onError("Esta oferta ya no está disponible");
                        }
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
        
        // Crear documento del tour asignado
        Map<String, Object> tourAsignado = new HashMap<>();
        tourAsignado.put("ofertaTourId", ofertaId);
        tourAsignado.put("titulo", oferta.getTitulo());
        tourAsignado.put("descripcion", oferta.getDescripcion());
        tourAsignado.put("precio", oferta.getPrecio());
        tourAsignado.put("duracion", oferta.getDuracion());
        tourAsignado.put("fechaRealizacion", oferta.getFechaRealizacion());
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
        tourAsignado.put("participantes", participantes);
        tourAsignado.put("estado", "confirmado");
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
                .whereEqualTo("habilitado", true)
                .orderBy("fechaRealizacion")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TourAsignado> toursAsignados = new ArrayList<>();
                    
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
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
}
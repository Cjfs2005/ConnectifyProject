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
        tourAsignado.put("participantes", participantes);
        tourAsignado.put("estado", "pendiente"); // ✅ Estado inicial único
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
            .whereEqualTo("habilitado", true)
            .orderBy("fechaRealizacion")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<TourAsignado> tours = new ArrayList<>();
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        TourAsignado tour = doc.toObject(TourAsignado.class);
                        if (tour != null) {
                            tour.setId(doc.getId());
                            tours.add(tour);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error convirtiendo tour: " + doc.getId(), e);
                    }
                }
                
                TourAsignado tourPrioritario = null;
                TourAsignado tourPendienteMasCercano = null;
                
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
                    
                    // ✅ PRIORIDAD 3: Tours listos para check-in (sin importar fecha)
                    if ("check_in".equals(estado)) {
                        if (tourPrioritario == null) tourPrioritario = tour;
                    }
                    
                    // 📅 PRIORIDAD 4: Tour pendiente más próximo (solo si no hay tours activos)
                    if ("pendiente".equals(estado) && (esTourDeHoy(tour) || esTourFuturo(tour))) {
                        if (tourPendienteMasCercano == null || 
                            tour.getFechaRealizacion().compareTo(tourPendienteMasCercano.getFechaRealizacion()) < 0) {
                            tourPendienteMasCercano = tour;
                        }
                    }
                }
                
                // 🎯 LÓGICA DE SELECCIÓN CORREGIDA:
                // 1. Si hay tour con check-in habilitado, ESE tiene prioridad
                // 2. Si no hay tours activos, entonces el pendiente más cercano
                if (tourPrioritario != null) {
                    callback.onSuccess(tourPrioritario);
                } else if (tourPendienteMasCercano != null) {
                    callback.onSuccess(tourPendienteMasCercano);
                } else {
                    callback.onError("No hay tours asignados");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error obteniendo tour prioritario", e);
                callback.onError("Error cargando tours: " + e.getMessage());
            });
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
            .whereEqualTo("habilitado", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                String tourActivoExistente = null;
                String tituloTourActivo = null;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
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
                        // 3. Crear tour asignado
                        crearTourAsignado(ofertaDoc, guiaId, callback);
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
                    // Intentar parsear la fecha
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        java.util.Date date = sdf.parse((String) fechaRealizacionObj);
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
                
                // Copiar imágenes
                List<String> imagenesUrls = (List<String>) ofertaDoc.get("imagenesUrls");
                if (imagenesUrls == null) {
                    imagenesUrls = new ArrayList<>();
                }
                tourAsignado.put("imagenesUrls", imagenesUrls);
                
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
                tourAsignado.put("estado", "pendiente"); // Cambiar a "pendiente" en lugar de "confirmado"
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
}

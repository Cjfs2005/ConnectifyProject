package com.example.connectifyproject.services;

import android.util.Log;
import com.example.connectifyproject.models.OfertaTour;
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
        // Verificar que la oferta sigue disponible
        db.collection(COLLECTION_OFERTAS).document(ofertaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        OfertaTour oferta = documentSnapshot.toObject(OfertaTour.class);
                        if (oferta != null && "publicado".equals(oferta.getEstado())) {
                            
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
                                                    Log.d(TAG, "Oferta y subcolección actualizadas exitosamente");
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
}
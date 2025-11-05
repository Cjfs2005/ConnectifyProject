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
     * Obtener ofertas disponibles para guías - ORDENADAS POR FECHA
     */
    public void getOfertasDisponibles(TourCallback callback) {
        Log.d(TAG, "Obteniendo ofertas disponibles...");
        
        db.collection(COLLECTION_OFERTAS)
                .whereEqualTo("estado", "publicado")
                .whereEqualTo("habilitado", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OfertaTour> ofertas = new ArrayList<>();
                    
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            OfertaTour oferta = document.toObject(OfertaTour.class);
                            if (oferta != null) {
                                oferta.setId(document.getId());
                                ofertas.add(oferta);
                                Log.d(TAG, "Oferta cargada: " + oferta.getTitulo());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear documento: " + document.getId(), e);
                        }
                    }
                    
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
                    
                    Log.d(TAG, "Total ofertas cargadas y ordenadas: " + ofertas.size());
                    callback.onSuccess(ofertas);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener ofertas: ", e);
                    callback.onError("Error al cargar las ofertas de tours");
                });
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
        
        // Primero obtener datos del guía desde usuarios
        db.collection(COLLECTION_USUARIOS)
                .document(guiaId)
                .get()
                .addOnSuccessListener(guiaDoc -> {
                    if (guiaDoc.exists()) {
                        String nombreGuia = guiaDoc.getString("nombresApellidos");
                        String emailGuia = guiaDoc.getString("email");
                        String telefonoGuia = guiaDoc.getString("telefono");
                        
                        // Proceder con la aceptación
                        procesarAceptacionOferta(ofertaId, guiaId, nombreGuia, emailGuia, telefonoGuia, callback);
                    } else {
                        callback.onError("Datos del guía no encontrados");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener datos del guía: ", e);
                    callback.onError("Error al verificar datos del guía");
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
        
        // Verificar que la oferta existe y está disponible
        db.collection(COLLECTION_OFERTAS).document(ofertaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        OfertaTour oferta = documentSnapshot.toObject(OfertaTour.class);
                        if (oferta != null && "publicado".equals(oferta.getEstado())) {
                            
                            // Cambiar estado a "rechazado" (opcional - podrías simplemente no hacer nada)
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("fechaActualizacion", Timestamp.now());
                            
                            db.collection(COLLECTION_OFERTAS).document(ofertaId)
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
     * Procesar la aceptación de la oferta
     */
    private void procesarAceptacionOferta(String ofertaId, String guiaId, String nombreGuia, 
                                        String emailGuia, String telefonoGuia, OperationCallback callback) {
        
        // Verificar que la oferta sigue disponible
        db.collection(COLLECTION_OFERTAS).document(ofertaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        OfertaTour oferta = documentSnapshot.toObject(OfertaTour.class);
                        if (oferta != null && "publicado".equals(oferta.getEstado())) {
                            
                            // Actualizar la oferta con el guía asignado
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("estado", "asignado");
                            updates.put("guiaAsignadoId", guiaId);
                            updates.put("fechaAsignacion", Timestamp.now());
                            updates.put("fechaActualizacion", Timestamp.now());
                            
                            db.collection(COLLECTION_OFERTAS).document(ofertaId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Oferta actualizada exitosamente");
                                        callback.onSuccess("¡Tour aceptado exitosamente!");
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
package com.example.connectifyproject.services;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.connectifyproject.models.TourBorrador;
import com.example.connectifyproject.models.OfertaTour;
import com.example.connectifyproject.utils.StorageHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para operaciones administrativas de tours:
 * - Gestión de borradores (CRUD)
 * - Subida y gestión de imágenes
 * - Publicación de ofertas
 * - Selección de guías y gestión de ofrecimientos
 */
public class AdminTourService {
    
    private static final String TAG = "AdminTourService";
    private static final String COLLECTION_BORRADORES = "tours_borradores";
    private static final String COLLECTION_OFERTAS = "tours_ofertas";
    private static final String SUBCOLLECTION_GUIAS = "guias_ofertados";
    
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;
    private final StorageHelper storageHelper;
    
    public AdminTourService() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.storageHelper = new StorageHelper();
    }
    
    // ==================== GESTIÓN DE BORRADORES ====================
    
    /**
     * Guarda un borrador de tour (crear o actualizar)
     */
    public Task<String> guardarBorrador(@NonNull TourBorrador borrador) {
        Log.d(TAG, "Guardando borrador de tour: " + borrador.getTitulo());
        
        if (borrador.getId() == null || borrador.getId().isEmpty()) {
            // Crear nuevo borrador
            return crearBorrador(borrador);
        } else {
            // Actualizar borrador existente
            return actualizarBorrador(borrador);
        }
    }
    
    private Task<String> crearBorrador(@NonNull TourBorrador borrador) {
        DocumentReference docRef = db.collection(COLLECTION_BORRADORES).document();
        borrador.setId(docRef.getId());
        borrador.setFechaCreacion(Timestamp.now());
        borrador.setFechaActualizacion(Timestamp.now());
        
        return docRef.set(borrador)
            .continueWith(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Borrador creado con ID: " + docRef.getId());
                    return docRef.getId();
                } else {
                    throw task.getException() != null ? task.getException() 
                        : new Exception("Error al crear borrador");
                }
            });
    }
    
    private Task<String> actualizarBorrador(@NonNull TourBorrador borrador) {
        borrador.setFechaActualizacion(Timestamp.now());
        
        return db.collection(COLLECTION_BORRADORES)
            .document(borrador.getId())
            .set(borrador)
            .continueWith(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Borrador actualizado: " + borrador.getId());
                    return borrador.getId();
                } else {
                    throw task.getException() != null ? task.getException() 
                        : new Exception("Error al actualizar borrador");
                }
            });
    }
    
    /**
     * Carga un borrador específico por ID
     */
    public Task<TourBorrador> cargarBorrador(@NonNull String borradorId) {
        Log.d(TAG, "Cargando borrador: " + borradorId);
        
        return db.collection(COLLECTION_BORRADORES)
            .document(borradorId)
            .get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        TourBorrador borrador = doc.toObject(TourBorrador.class);
                        if (borrador != null) {
                            borrador.setId(doc.getId());
                            return borrador;
                        }
                    }
                    throw new Exception("Borrador no encontrado");
                } else {
                    throw task.getException() != null ? task.getException() 
                        : new Exception("Error al cargar borrador");
                }
            });
    }
    
    /**
     * Lista todos los borradores de una empresa
     */
    public Task<List<TourBorrador>> listarBorradores(@NonNull String empresaId) {
        Log.d(TAG, "Listando borradores de empresa: " + empresaId);
        
        return db.collection(COLLECTION_BORRADORES)
            .whereEqualTo("empresaId", empresaId)
            .orderBy("fechaActualizacion", Query.Direction.DESCENDING)
            .get()
            .continueWith(task -> {
                List<TourBorrador> borradores = new ArrayList<>();
                if (task.isSuccessful() && task.getResult() != null) {
                    QuerySnapshot snapshot = task.getResult();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        TourBorrador borrador = doc.toObject(TourBorrador.class);
                        if (borrador != null) {
                            borrador.setId(doc.getId());
                            borradores.add(borrador);
                        }
                    }
                    Log.d(TAG, "Borradores encontrados: " + borradores.size());
                }
                return borradores;
            });
    }
    
    /**
     * Elimina un borrador y sus imágenes asociadas
     */
    public Task<Void> eliminarBorrador(@NonNull String borradorId, @NonNull String empresaId) {
        Log.d(TAG, "Eliminando borrador: " + borradorId);
        
        // Primero eliminar las imágenes del Storage
        return eliminarImagenesBorrador(empresaId, borradorId)
            .continueWithTask(task -> {
                // Luego eliminar el documento de Firestore
                return db.collection(COLLECTION_BORRADORES)
                    .document(borradorId)
                    .delete();
            })
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Borrador eliminado exitosamente"))
            .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar borrador", e));
    }
    
    // ==================== GESTIÓN DE IMÁGENES ====================
    
    /**
     * Sube una imagen para un borrador de tour
     * Path: /tour_images/{empresaId}/borradores/{tourId}/{index}.jpg
     */
    public Task<String> subirImagenBorrador(
            @NonNull Uri imageUri, 
            @NonNull String empresaId, 
            @NonNull String borradorId,
            int imageIndex) {
        
        Log.d(TAG, "Subiendo imagen de borrador. Index: " + imageIndex);
        
        String path = String.format("tour_images/%s/borradores/%s/%d.jpg", 
                empresaId, borradorId, imageIndex);
        
        StorageReference storageRef = storage.getReference().child(path);
        
        return storageRef.putFile(imageUri)
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    return storageRef.getDownloadUrl();
                } else {
                    throw task.getException() != null ? task.getException() 
                        : new Exception("Error al subir imagen");
                }
            })
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String downloadUrl = task.getResult().toString();
                    Log.d(TAG, "Imagen subida exitosamente: " + downloadUrl);
                    return downloadUrl;
                } else {
                    throw task.getException() != null ? task.getException() 
                        : new Exception("Error al obtener URL de imagen");
                }
            });
    }
    
    /**
     * Elimina todas las imágenes de un borrador
     */
    private Task<Void> eliminarImagenesBorrador(@NonNull String empresaId, @NonNull String borradorId) {
        Log.d(TAG, "Eliminando imágenes del borrador: " + borradorId);
        
        String folderPath = String.format("tour_images/%s/borradores/%s/", empresaId, borradorId);
        StorageReference folderRef = storage.getReference().child(folderPath);
        
        return folderRef.listAll()
            .continueWithTask(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (StorageReference fileRef : task.getResult().getItems()) {
                        deleteTasks.add(fileRef.delete());
                    }
                    return Tasks.whenAll(deleteTasks);
                } else {
                    // Si no hay imágenes o error al listar, continuar sin error
                    return Tasks.forResult(null);
                }
            });
    }
    
    /**
     * Copia imágenes de carpeta borradores a publicados
     * Retorna nueva lista de URLs
     */
    private Task<List<String>> moverImagenesAPublicados(
            @NonNull String empresaId, 
            @NonNull String tourId,
            @NonNull List<String> imagenesUrlsBorrador) {
        
        Log.d(TAG, "Moviendo imágenes a carpeta publicados");
        
        List<Task<String>> copyTasks = new ArrayList<>();
        
        for (int i = 0; i < imagenesUrlsBorrador.size(); i++) {
            final int index = i;
            String urlBorrador = imagenesUrlsBorrador.get(i);
            
            // Path origen y destino
            String pathOrigen = String.format("tour_images/%s/borradores/%s/%d.jpg", empresaId, tourId, index);
            String pathDestino = String.format("tour_images/%s/publicados/%s/%d.jpg", empresaId, tourId, index);
            
            StorageReference origenRef = storage.getReference().child(pathOrigen);
            StorageReference destinoRef = storage.getReference().child(pathDestino);
            
            // Descargar y re-subir (Firebase Storage no tiene función copy directa)
            Task<String> copyTask = origenRef.getBytes(Long.MAX_VALUE)
                .continueWithTask(downloadTask -> {
                    if (downloadTask.isSuccessful() && downloadTask.getResult() != null) {
                        byte[] data = downloadTask.getResult();
                        return destinoRef.putBytes(data);
                    } else {
                        throw downloadTask.getException() != null ? downloadTask.getException()
                            : new Exception("Error al descargar imagen origen");
                    }
                })
                .continueWithTask(uploadTask -> {
                    if (uploadTask.isSuccessful()) {
                        return destinoRef.getDownloadUrl();
                    } else {
                        throw uploadTask.getException() != null ? uploadTask.getException()
                            : new Exception("Error al subir imagen destino");
                    }
                })
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toString();
                    } else {
                        throw task.getException() != null ? task.getException()
                            : new Exception("Error al obtener URL destino");
                    }
                });
            
            copyTasks.add(copyTask);
        }
        
        return Tasks.whenAllSuccess(copyTasks);
    }
    
    // ==================== PUBLICACIÓN DE OFERTAS ====================
    
    /**
     * Publica un borrador como oferta de tour disponible
     * - Copia imágenes a carpeta publicados
     * - Crea documento en tours_ofertas
     * - Elimina borrador
     */
    public Task<String> publicarOferta(@NonNull String borradorId) {
        Log.d(TAG, "Publicando oferta desde borrador: " + borradorId);
        
        return cargarBorrador(borradorId)
            .continueWithTask(task -> {
                if (!task.isSuccessful() || task.getResult() == null) {
                    throw new Exception("No se pudo cargar el borrador");
                }
                
                TourBorrador borrador = task.getResult();
                
                // Validar que el borrador esté completo
                if (!borrador.esValido()) {
                    throw new Exception("El borrador no está completo para publicar");
                }
                
                // Mover imágenes a carpeta publicados
                return moverImagenesAPublicados(
                    borrador.getEmpresaId(), 
                    borrador.getId(), 
                    borrador.getImagenesUrls()
                ).continueWith(moveTask -> {
                    if (!moveTask.isSuccessful()) {
                        throw moveTask.getException() != null ? moveTask.getException()
                            : new Exception("Error al mover imágenes");
                    }
                    
                    List<String> nuevasUrls = moveTask.getResult();
                    
                    // Crear OfertaTour desde TourBorrador
                    OfertaTour oferta = new OfertaTour();
                    oferta.setId(borrador.getId());
                    oferta.setTitulo(borrador.getTitulo());
                    oferta.setDescripcion(borrador.getDescripcion());
                    oferta.setPrecio(borrador.getPrecio());
                    oferta.setDuracion(borrador.getDuracion());
                    oferta.setFechaRealizacion(borrador.getFechaRealizacion());
                    oferta.setItinerario(borrador.getItinerario());
                    oferta.setServiciosAdicionales(borrador.getServiciosAdicionales());
                    oferta.setImagenesUrls(nuevasUrls);
                    oferta.setImagenPrincipal(nuevasUrls != null && !nuevasUrls.isEmpty() ? nuevasUrls.get(0) : null);
                    oferta.setIdiomasRequeridos(borrador.getIdiomasRequeridos());
                    oferta.setPagoGuia(borrador.getPagoGuia());
                    oferta.setEmpresaId(borrador.getEmpresaId());
                    oferta.setEstado("publicado");
                    oferta.setGuiaSeleccionadoActual(null);
                    oferta.setFechaUltimoOfrecimiento(null);
                    oferta.setFechaCreacion(borrador.getFechaCreacion());
                    oferta.setFechaActualizacion(Timestamp.now());
                    oferta.setHabilitado(true);
                    
                    return oferta;
                });
            })
            .continueWithTask(task -> {
                if (!task.isSuccessful() || task.getResult() == null) {
                    throw task.getException() != null ? task.getException()
                        : new Exception("Error al preparar oferta");
                }
                
                OfertaTour oferta = task.getResult();
                
                // Guardar oferta en Firestore
                return db.collection(COLLECTION_OFERTAS)
                    .document(oferta.getId())
                    .set(oferta)
                    .continueWith(setTask -> {
                        if (setTask.isSuccessful()) {
                            return oferta.getId();
                        } else {
                            throw setTask.getException() != null ? setTask.getException()
                                : new Exception("Error al guardar oferta");
                        }
                    });
            })
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException() != null ? task.getException()
                        : new Exception("Error en publicación");
                }
                
                String ofertaId = task.getResult();
                
                // Eliminar borrador y sus imágenes originales
                return cargarBorrador(borradorId)
                    .continueWithTask(loadTask -> {
                        if (loadTask.isSuccessful() && loadTask.getResult() != null) {
                            String empresaId = loadTask.getResult().getEmpresaId();
                            return eliminarBorrador(borradorId, empresaId)
                                .continueWith(deleteTask -> ofertaId);
                        } else {
                            // Si no se puede cargar borrador, continuar con el ID de la oferta
                            return Tasks.forResult(ofertaId);
                        }
                    });
            })
            .addOnSuccessListener(ofertaId -> 
                Log.d(TAG, "Oferta publicada exitosamente: " + ofertaId))
            .addOnFailureListener(e -> 
                Log.e(TAG, "Error al publicar oferta", e));
    }
    
    // ==================== SELECCIÓN DE GUÍAS ====================
    
    /**
     * Ofrece un tour a un guía específico
     * - Crea entrada en subcollection guias_ofertados
     * - Actualiza guiaSeleccionadoActual y fechaUltimoOfrecimiento
     */
    public Task<Void> seleccionarGuia(
            @NonNull String ofertaId, 
            @NonNull String guiaId,
            @Nullable String motivoSeleccion) {
        
        Log.d(TAG, "Ofreciendo tour " + ofertaId + " al guía " + guiaId);
        
        // Datos del ofrecimiento
        Map<String, Object> ofrecimiento = new HashMap<>();
        ofrecimiento.put("guiaId", guiaId);
        ofrecimiento.put("estadoOferta", "pendiente");
        ofrecimiento.put("fechaOfrecimiento", Timestamp.now());
        ofrecimiento.put("fechaRespuesta", null);
        ofrecimiento.put("motivoRechazo", null);
        ofrecimiento.put("motivoSeleccion", motivoSeleccion);
        ofrecimiento.put("vistoAdmin", true);
        
        // Actualizar oferta principal
        Map<String, Object> actualizacionOferta = new HashMap<>();
        actualizacionOferta.put("guiaSeleccionadoActual", guiaId);
        actualizacionOferta.put("fechaUltimoOfrecimiento", Timestamp.now());
        actualizacionOferta.put("fechaActualizacion", Timestamp.now());
        
        // Crear entrada en subcollection y actualizar documento principal
        return db.collection(COLLECTION_OFERTAS)
            .document(ofertaId)
            .collection(SUBCOLLECTION_GUIAS)
            .document(guiaId)
            .set(ofrecimiento)
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    return db.collection(COLLECTION_OFERTAS)
                        .document(ofertaId)
                        .update(actualizacionOferta);
                } else {
                    throw task.getException() != null ? task.getException()
                        : new Exception("Error al crear ofrecimiento");
                }
            })
            .addOnSuccessListener(aVoid -> 
                Log.d(TAG, "Guía seleccionado exitosamente"))
            .addOnFailureListener(e -> 
                Log.e(TAG, "Error al seleccionar guía", e));
    }
    
    /**
     * Cancela el ofrecimiento actual a un guía
     * - Limpia guiaSeleccionadoActual
     * - Mantiene registro en subcollection con estado "cancelado"
     */
    public Task<Void> cancelarOfrecimiento(@NonNull String ofertaId, @NonNull String guiaId) {
        Log.d(TAG, "Cancelando ofrecimiento de tour " + ofertaId + " al guía " + guiaId);
        
        // Actualizar estado en subcollection
        Map<String, Object> actualizacionOfrecimiento = new HashMap<>();
        actualizacionOfrecimiento.put("estadoOferta", "cancelado_admin");
        actualizacionOfrecimiento.put("fechaRespuesta", Timestamp.now());
        
        // Limpiar guiaSeleccionadoActual en oferta
        Map<String, Object> actualizacionOferta = new HashMap<>();
        actualizacionOferta.put("guiaSeleccionadoActual", null);
        actualizacionOferta.put("fechaActualizacion", Timestamp.now());
        
        return db.collection(COLLECTION_OFERTAS)
            .document(ofertaId)
            .collection(SUBCOLLECTION_GUIAS)
            .document(guiaId)
            .update(actualizacionOfrecimiento)
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    return db.collection(COLLECTION_OFERTAS)
                        .document(ofertaId)
                        .update(actualizacionOferta);
                } else {
                    throw task.getException() != null ? task.getException()
                        : new Exception("Error al cancelar ofrecimiento");
                }
            })
            .addOnSuccessListener(aVoid -> 
                Log.d(TAG, "Ofrecimiento cancelado exitosamente"))
            .addOnFailureListener(e -> 
                Log.e(TAG, "Error al cancelar ofrecimiento", e));
    }
    
    /**
     * Marca como visto el rechazo de un guía
     */
    public Task<Void> marcarRechazoVisto(@NonNull String ofertaId, @NonNull String guiaId) {
        Log.d(TAG, "Marcando rechazo como visto");
        
        Map<String, Object> actualizacion = new HashMap<>();
        actualizacion.put("vistoAdmin", true);
        
        return db.collection(COLLECTION_OFERTAS)
            .document(ofertaId)
            .collection(SUBCOLLECTION_GUIAS)
            .document(guiaId)
            .update(actualizacion)
            .addOnSuccessListener(aVoid -> 
                Log.d(TAG, "Rechazo marcado como visto"))
            .addOnFailureListener(e -> 
                Log.e(TAG, "Error al marcar rechazo", e));
    }
    
    /**
     * Obtiene el historial de ofrecimientos de un tour
     */
    public Task<List<Map<String, Object>>> obtenerHistorialOfrecimientos(@NonNull String ofertaId) {
        Log.d(TAG, "Obteniendo historial de ofrecimientos para tour: " + ofertaId);
        
        return db.collection(COLLECTION_OFERTAS)
            .document(ofertaId)
            .collection(SUBCOLLECTION_GUIAS)
            .orderBy("fechaOfrecimiento", Query.Direction.DESCENDING)
            .get()
            .continueWith(task -> {
                List<Map<String, Object>> historial = new ArrayList<>();
                if (task.isSuccessful() && task.getResult() != null) {
                    QuerySnapshot snapshot = task.getResult();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Map<String, Object> ofrecimiento = new HashMap<>(doc.getData());
                        ofrecimiento.put("id", doc.getId());
                        historial.add(ofrecimiento);
                    }
                    Log.d(TAG, "Ofrecimientos encontrados: " + historial.size());
                }
                return historial;
            });
    }
}

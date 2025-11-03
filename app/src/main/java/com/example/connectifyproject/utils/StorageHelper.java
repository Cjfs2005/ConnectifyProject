package com.example.connectifyproject.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Helper para subir imágenes a Firebase Storage
 */
public class StorageHelper {
    
    private static final String TAG = "StorageHelper";
    private static final int MAX_IMAGE_SIZE = 1024; // Tamaño máximo en px (ancho/alto)
    private static final int COMPRESSION_QUALITY = 85; // Calidad de compresión JPEG (0-100)
    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB
    
    private final FirebaseStorage storage;
    
    public StorageHelper() {
        this.storage = FirebaseStorage.getInstance();
    }
    
    /**
     * Interface para callbacks de subida
     */
    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
        void onProgress(double progress);
    }
    
    /**
     * Sube una foto de perfil de usuario a Firebase Storage
     * La imagen se comprime automáticamente si es muy grande
     * 
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen seleccionada
     * @param userId ID del usuario (uid de Firebase Auth)
     * @param callback Callback para el resultado
     */
    public void uploadProfilePhoto(Context context, Uri imageUri, String userId, UploadCallback callback) {
        try {
            // Leer y comprimir la imagen
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onFailure(new Exception("No se pudo leer la imagen"));
                return;
            }
            
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (originalBitmap == null) {
                callback.onFailure(new Exception("Imagen inválida"));
                return;
            }
            
            // Comprimir y redimensionar
            Bitmap compressedBitmap = compressImage(originalBitmap);
            byte[] imageData = bitmapToByteArray(compressedBitmap);
            
            Log.d(TAG, "Imagen comprimida: " + imageData.length + " bytes");
            
            // Subir a Firebase Storage
            StorageReference profileRef = storage.getReference()
                    .child("usuarios")
                    .child(userId)
                    .child("perfil.jpg");
            
            UploadTask uploadTask = profileRef.putBytes(imageData);
            
            // Monitorear progreso
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                callback.onProgress(progress);
            });
            
            // Resultado final
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Obtener URL de descarga
                profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "Imagen subida exitosamente: " + downloadUrl);
                    callback.onSuccess(downloadUrl);
                }).addOnFailureListener(callback::onFailure);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al subir imagen", e);
                callback.onFailure(e);
            });
            
            // Liberar memoria
            if (!originalBitmap.isRecycled()) {
                originalBitmap.recycle();
            }
            if (!compressedBitmap.isRecycled()) {
                compressedBitmap.recycle();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error procesando imagen", e);
            callback.onFailure(e);
        }
    }
    
    /**
     * Comprime y redimensiona una imagen si es necesario
     */
    private Bitmap compressImage(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Si la imagen ya es pequeña, no redimensionar
        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return original;
        }
        
        // Calcular nuevo tamaño manteniendo aspect ratio
        float scaleFactor;
        if (width > height) {
            scaleFactor = (float) MAX_IMAGE_SIZE / width;
        } else {
            scaleFactor = (float) MAX_IMAGE_SIZE / height;
        }
        
        int newWidth = Math.round(width * scaleFactor);
        int newHeight = Math.round(height * scaleFactor);
        
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
    
    /**
     * Convierte un Bitmap a byte array con compresión JPEG
     */
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos);
        return baos.toByteArray();
    }
    
    /**
     * Obtiene la URL pública de la imagen por defecto
     */
    public void getDefaultPhotoUrl(UploadCallback callback) {
        StorageReference defaultRef = storage.getReferenceFromUrl(AuthConstants.DEFAULT_PHOTO_URL);
        defaultRef.getDownloadUrl()
                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                .addOnFailureListener(callback::onFailure);
    }
    
    /**
     * Sube una foto promocional de empresa
     * 
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen seleccionada
     * @param userId ID del usuario (uid de Firebase Auth)
     * @param photoIndex Índice de la foto (0, 1, 2, etc.)
     * @param callback Callback para el resultado
     */
    public void uploadCompanyPhoto(Context context, Uri imageUri, String userId, int photoIndex, UploadCallback callback) {
        try {
            // Leer y comprimir la imagen
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onFailure(new Exception("No se pudo leer la imagen"));
                return;
            }
            
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (originalBitmap == null) {
                callback.onFailure(new Exception("Imagen inválida"));
                return;
            }
            
            // Comprimir y redimensionar
            Bitmap compressedBitmap = compressImage(originalBitmap);
            byte[] imageData = bitmapToByteArray(compressedBitmap);
            
            Log.d(TAG, "Imagen empresarial comprimida: " + imageData.length + " bytes");
            
            // Subir a Firebase Storage en carpeta empresas
            StorageReference photoRef = storage.getReference()
                    .child("empresas")
                    .child(userId)
                    .child("promo_" + photoIndex + ".jpg");
            
            UploadTask uploadTask = photoRef.putBytes(imageData);
            
            // Monitorear progreso
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                callback.onProgress(progress);
            });
            
            // Resultado final
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Obtener URL de descarga
                photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "Foto empresarial subida exitosamente: " + downloadUrl);
                    callback.onSuccess(downloadUrl);
                }).addOnFailureListener(callback::onFailure);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al subir foto empresarial", e);
                callback.onFailure(e);
            });
            
            // Liberar memoria
            if (!originalBitmap.isRecycled()) {
                originalBitmap.recycle();
            }
            if (!compressedBitmap.isRecycled()) {
                compressedBitmap.recycle();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error procesando imagen empresarial", e);
            callback.onFailure(e);
        }
    }
    
    /**
     * Elimina la foto de perfil de un usuario
     */
    public void deleteProfilePhoto(String userId, UploadCallback callback) {
        StorageReference profileRef = storage.getReference()
                .child("usuarios")
                .child(userId)
                .child("perfil.jpg");
        
        profileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Foto de perfil eliminada");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar foto", e);
                    callback.onFailure(e);
                });
    }
}

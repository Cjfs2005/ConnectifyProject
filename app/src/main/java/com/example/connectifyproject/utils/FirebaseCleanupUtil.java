package com.example.connectifyproject.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Arrays;
import java.util.List;

/**
 * 🧹 UTILIDAD PARA LIMPIAR TOURS PROBLEMÁTICOS EN FIREBASE
 * Elimina los tours que tienen problemas de formato String/Timestamp
 */
public class FirebaseCleanupUtil {
    private static final String TAG = "FirebaseCleanup";
    private static final String COLLECTION_ASIGNADOS = "tours_asignados";
    
    /**
     * 🗑️ ELIMINAR TOURS PROBLEMÁTICOS CON IDs ESPECÍFICOS
     * Estos tours fueron creados con formato String en fechaRealizacion
     * y causan errores de deserialización
     */
    public static void eliminarToursProblematicos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // IDs de los tours problemáticos detectados en los logs
        List<String> idsProblematicos = Arrays.asList(
            "p3zxW3Yl0GknTED1MSZG", // Tour recién creado al aceptar oferta
            "tacpbQNvbiC49AFOfKm8", // Tour recién creado al aceptar oferta
            "FQ6A0wsTXvQlLe98L0Wr", // Tour con formato incorrecto
            "UHsLOHcdC5DFy1Nib8ym", // Tour con formato incorrecto
            "cpRjIJJBhJ8Zq2W6pmjS", // Tour con formato incorrecto
            "h67qdcVm89UksEfPbW0k", // Tour con formato incorrecto
            "rbW4GzOeDDrpmzXIBCXL"  // Tour con formato incorrecto
        );
        
        Log.d(TAG, "🧹 Iniciando limpieza de " + idsProblematicos.size() + " tours problemáticos...");
        
        for (String tourId : idsProblematicos) {
            db.collection(COLLECTION_ASIGNADOS)
                .document(tourId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Tour problemático eliminado: " + tourId);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "⚠️ No se pudo eliminar tour (tal vez ya no existe): " + tourId, e);
                });
        }
        
        Log.d(TAG, "🏁 Limpieza iniciada. Los tours problemáticos serán eliminados...");
    }
    
    /**
     * 🔍 VERIFICAR TOURS CON PROBLEMAS DE FORMATO
     * Este método puede ayudar a identificar otros tours problemáticos
     */
    public static void verificarToursProblematicos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection(COLLECTION_ASIGNADOS)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "🔍 Verificando " + queryDocumentSnapshots.size() + " tours...");
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        // Intentar obtener fechaRealizacion como Timestamp
                        Object fechaRealizacion = document.get("fechaRealizacion");
                        
                        if (fechaRealizacion instanceof String) {
                            Log.w(TAG, "⚠️ Tour con formato String detectado: " + document.getId() + 
                                     " - fecha: " + fechaRealizacion);
                        } else if (fechaRealizacion != null) {
                            Log.d(TAG, "✅ Tour con formato correcto: " + document.getId());
                        }
                        
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error verificando tour: " + document.getId(), e);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error obteniendo tours para verificación", e);
            });
    }
}
package com.example.connectifyproject.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ SCRIPT DE PRUEBA - Crear tours con diferentes momentoTour para testing
 * Ejecutar UNA SOLA VEZ para probar el sistema de momentoTour
 */
public class TestMomentoTourData {
    private static final String TAG = "TestMomentoTour";
    private static final String COLLECTION_ASIGNADOS = "tours_asignados";
    
    /**
     * 🧪 CREAR TOURS DE PRUEBA CON DIFERENTES ESTADOS DE momentoTour
     * Este método crea 5 tours con diferentes momentos para testing completo
     */
    public static void crearToursParaTestingMomentoTour() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "🧪 Iniciando creación de tours para testing momentoTour...");
        
        // 1. Tour Pendiente
        crearTourTesting(db, "pendiente", "Tour Lima Pendiente", "Museo Nacional");
        
        // 2. Tour Check-in disponible  
        crearTourTesting(db, "check_in", "Tour Cusco Check-in", "Plaza de Armas Cusco");
        
        // 3. Tour En Curso
        crearTourTesting(db, "en_curso", "Tour Arequipa En Curso", "Monasterio Santa Catalina");
        
        // 4. Tour Check-out disponible
        crearTourTesting(db, "check_out", "Tour Ica Check-out", "Laguna Huacachina");
        
        // 5. Tour Terminado
        crearTourTesting(db, "terminado", "Tour Trujillo Terminado", "Huacas del Sol y la Luna");
        
        Log.d(TAG, "✅ Tours de testing momentoTour creados exitosamente");
    }
    
    /**
     * 🔧 MÉTODO HELPER PARA CREAR TOUR DE TESTING
     */
    private static void crearTourTesting(FirebaseFirestore db, String momentoTour, String titulo, String lugar) {
        Map<String, Object> tour = new HashMap<>();
        
        // Información básica
        tour.put("ofertaTourId", "test_" + momentoTour + "_001");
        tour.put("titulo", titulo);
        tour.put("descripcion", "Tour de prueba para testing momentoTour: " + momentoTour);
        tour.put("precio", 75.0);
        tour.put("duracion", "3 horas");
        tour.put("fechaRealizacion", "06/11/2025"); // Hoy
        tour.put("horaInicio", "10:00");
        tour.put("horaFin", "13:00");
        
        // Información del guía (tu usuario)
        Map<String, Object> guiaAsignado = new HashMap<>();
        guiaAsignado.put("identificadorUsuario", "YbmULw4iJXT41CdCLXV1ktCrfek1");
        guiaAsignado.put("nombresCompletos", "Gianfranco Enriquez Soel");
        guiaAsignado.put("correoElectronico", "a20224926@pucp.edu.pe");
        tour.put("guiaAsignado", guiaAsignado);
        
        // Empresa
        tour.put("empresaId", "empresa_testing_" + momentoTour);
        tour.put("nombreEmpresa", "Testing Tours SAC");
        tour.put("correoEmpresa", "testing@tours.com");
        tour.put("pagoGuia", 65.0);
        
        // Estados según momentoTour
        tour.put("momentoTour", momentoTour); // ✅ CAMPO PRINCIPAL
        
        switch (momentoTour) {
            case "pendiente":
                tour.put("estado", "programado");
                tour.put("checkInRealizado", false);
                tour.put("checkOutRealizado", false);
                break;
            case "check_in":
                tour.put("estado", "programado");
                tour.put("checkInRealizado", false);
                tour.put("checkOutRealizado", false);
                break;
            case "en_curso":
                tour.put("estado", "en_curso");
                tour.put("checkInRealizado", true);
                tour.put("checkOutRealizado", false);
                break;
            case "check_out":
                tour.put("estado", "en_curso");
                tour.put("checkInRealizado", true);
                tour.put("checkOutRealizado", false);
                break;
            case "terminado":
                tour.put("estado", "completado");
                tour.put("checkInRealizado", true);
                tour.put("checkOutRealizado", true);
                break;
        }
        
        // Participantes
        tour.put("numeroParticipantesTotal", 2);
        tour.put("participantes", new java.util.ArrayList<>());
        
        // Metadatos
        tour.put("fechaCreacion", com.google.firebase.Timestamp.now());
        tour.put("fechaActualizacion", com.google.firebase.Timestamp.now());
        tour.put("habilitado", true);
        
        // Insertar en Firebase
        db.collection(COLLECTION_ASIGNADOS)
            .add(tour)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "✅ Tour " + momentoTour + " creado: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Error creando tour " + momentoTour + ": ", e);
            });
    }
    
    /**
     * 🗑️ LIMPIAR TOURS DE TESTING
     * Eliminar todos los tours que empiecen con "test_"
     */
    public static void limpiarToursDeTestingAntes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection(COLLECTION_ASIGNADOS)
            .whereGreaterThanOrEqualTo("ofertaTourId", "test_")
            .whereLessThan("ofertaTourId", "test_\uf8ff")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    doc.getReference().delete()
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "🗑️ Tour testing eliminado: " + doc.getId()))
                        .addOnFailureListener(e -> Log.e(TAG, "❌ Error eliminando tour testing", e));
                }
                Log.d(TAG, "✅ Limpieza de tours testing completada");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Error en limpieza de tours testing", e);
            });
    }
}
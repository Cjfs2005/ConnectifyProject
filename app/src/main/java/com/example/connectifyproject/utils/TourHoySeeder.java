package com.example.connectifyproject.utils;

import android.util.Log;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;

/**
 * 🧪 SEEDER PARA TOUR DE HOY - Testing de estados
 */
public class TourHoySeeder {
    private static final String TAG = "TourHoySeeder";
    private static final String COLLECTION_ASIGNADOS = "tours_asignados";
    
    /**
     * 📅 CREAR TOUR PENDIENTE PARA HOY
     */
    public static void crearTourPendienteHoy() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "🧪 Creando tour pendiente para HOY...");
        
        // Obtener fecha de HOY
        Calendar calendar = Calendar.getInstance();
        Date hoy = calendar.getTime();
        Timestamp timestampHoy = new Timestamp(hoy);
        
        // Información del guía (tu usuario)
        Map<String, Object> guiaAsignado = new HashMap<>();
        guiaAsignado.put("identificadorUsuario", "YbmULw4iJXT41CdCLXV1ktCrfek1");
        guiaAsignado.put("nombresCompletos", "Gianfranco Enriquez Soel");
        guiaAsignado.put("correoElectronico", "a20224926@pucp.edu.pe");
        guiaAsignado.put("numeroTelefono", "987 654 321");
        guiaAsignado.put("fechaAsignacion", Timestamp.now());
        
        // Itinerario simple
        List<Map<String, Object>> itinerario = new ArrayList<>();
        Map<String, Object> punto1 = new HashMap<>();
        punto1.put("orden", 1);
        punto1.put("lugar", "Plaza San Martín");
        punto1.put("actividad", "Punto de encuentro inicial");
        punto1.put("horaEstimada", "14:00");
        punto1.put("completado", false);
        itinerario.add(punto1);
        
        Map<String, Object> punto2 = new HashMap<>();
        punto2.put("orden", 2);
        punto2.put("lugar", "Jirón de la Unión");
        punto2.put("actividad", "Caminata por centro comercial");
        punto2.put("horaEstimada", "14:30");
        punto2.put("completado", false);
        itinerario.add(punto2);
        
        // Participantes
        List<Map<String, Object>> participantes = new ArrayList<>();
        Map<String, Object> participante1 = new HashMap<>();
        participante1.put("nombre", "María Fernanda López");
        participante1.put("numeroDocumento", "12345678");
        participante1.put("checkIn", false);
        participantes.add(participante1);
        
        Map<String, Object> participante2 = new HashMap<>();
        participante2.put("nombre", "Carlos Eduardo Ruiz");
        participante2.put("numeroDocumento", "87654321");
        participante2.put("checkIn", false);
        participantes.add(participante2);
        
        // Crear documento del tour
        Map<String, Object> tour = new HashMap<>();
        tour.put("ofertaTourId", "test_tour_hoy_001");
        tour.put("titulo", "Tour Testing Estados - HOY");
        tour.put("descripcion", "Tour para probar ciclo de vida de estados");
        tour.put("precio", 55.0);
        tour.put("duracion", "3 horas");
        tour.put("fechaRealizacion", timestampHoy); // ✅ HOY
        tour.put("horaInicio", "14:00");
        tour.put("horaFin", "17:00");
        tour.put("itinerario", itinerario);
        tour.put("serviciosAdicionales", new ArrayList<>());
        tour.put("guiaAsignado", guiaAsignado);
        tour.put("empresaId", "empresa_testing_001");
        tour.put("nombreEmpresa", "Testing Tours SAC");
        tour.put("correoEmpresa", "testing@tours.com");
        tour.put("pagoGuia", 75.0);
        tour.put("idiomasRequeridos", List.of("Español"));
        tour.put("consideraciones", "Tour de prueba para verificar estados");
        tour.put("participantes", participantes);
        
        // ✅ ESTADO INICIAL: PENDIENTE
        tour.put("estado", "pendiente");
        tour.put("numeroParticipantesTotal", participantes.size());
        tour.put("checkInRealizado", false);
        tour.put("checkOutRealizado", false);
        tour.put("horaCheckIn", null);
        tour.put("horaCheckOut", null);
        tour.put("reseniasClientes", new ArrayList<>());
        tour.put("calificacionPromedio", 0.0);
        tour.put("comentariosGuia", "");
        tour.put("fechaAsignacion", Timestamp.now());
        tour.put("fechaCreacion", Timestamp.now());
        tour.put("fechaActualizacion", Timestamp.now());
        tour.put("habilitado", true);
        
        // Insertar en Firebase
        db.collection(COLLECTION_ASIGNADOS)
            .add(tour)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "✅ Tour de HOY creado: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Error creando tour de HOY: ", e);
            });
    }
    
    /**
     * 🗑️ LIMPIAR TOURS DE TESTING
     */
    public static void limpiarToursDeTestingHoy() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection(COLLECTION_ASIGNADOS)
            .whereEqualTo("ofertaTourId", "test_tour_hoy_001")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    doc.getReference().delete()
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "🗑️ Tour de testing HOY eliminado"))
                        .addOnFailureListener(e -> Log.e(TAG, "❌ Error eliminando tour", e));
                }
            });
    }
}
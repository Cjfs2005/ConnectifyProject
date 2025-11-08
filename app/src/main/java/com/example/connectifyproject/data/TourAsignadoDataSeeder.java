package com.example.connectifyproject.data;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Seeder para crear datos de prueba de tours asignados en Firebase
 */
public class TourAsignadoDataSeeder {
    private static final String TAG = "TourAsignadoSeeder";
    private FirebaseFirestore db;

    public TourAsignadoDataSeeder() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Crear tours asignados de ejemplo para pruebas
     * ✅ CONFIGURACIÓN CORRECTA: Solo un tour en estado avanzado
     */
    public void crearToursAsignadosDePrueba() {
        Log.d(TAG, "Iniciando creación de tours asignados de prueba...");
        
        // ✅ SOLO UN TOUR EN ESTADO AVANZADO - El resto en pendiente
        crearTourAsignadoLima(); // HOY - Estado pendiente
        crearTourEnCurso(); // HOY - Estado en_curso (SOLO UNO)
        crearTourAsignadoCusco(); // FUTURO - Estado pendiente
        crearTourAsignadoArequipa(); // FUTURO - Estado pendiente
        crearTourAsignadoIca(); // FUTURO - Estado pendiente
    }

    /**
     * Tour asignado en Lima - Tour histórico completo
     */
    private void crearTourAsignadoLima() {
        // Crear guía asignado para Lima - USAR EL GUÍA ACTUAL
        Map<String, Object> guiaAsignado = new HashMap<>();
        guiaAsignado.put("identificadorUsuario", "YbmULw4iJXT41CdCLXV1ktCrfek1");
        guiaAsignado.put("nombresCompletos", "Gianfranco Enriquez Soel");
        guiaAsignado.put("correoElectronico", "a20224926@pucp.edu.pe");
        guiaAsignado.put("numeroTelefono", "987 654 321");
        guiaAsignado.put("fechaAsignacion", Timestamp.now());

        // Crear itinerario con seguimiento para Lima - COMPATIBLE CON OFERTAS
        List<Map<String, Object>> itinerarioLima = new ArrayList<>();
        
        Map<String, Object> punto1 = new HashMap<>();
        punto1.put("orden", 1);
        punto1.put("lugar", "Plaza Mayor de Lima");  // ✅ Usar "lugar" como en ofertas
        punto1.put("actividad", "Recorrido por la plaza principal y catedral");  // ✅ Usar "actividad" como en ofertas
        punto1.put("horaEstimada", "09:00");
        // Campos adicionales para seguimiento del tour asignado
        punto1.put("completado", true);
        punto1.put("horaLlegada", "09:05");
        punto1.put("horaSalida", "10:15");
        itinerarioLima.add(punto1);

        Map<String, Object> punto2 = new HashMap<>();
        punto2.put("orden", 2);
        punto2.put("lugar", "Palacio de Gobierno");  // ✅ Usar "lugar" como en ofertas
        punto2.put("actividad", "Visita externa y cambio de guardia");  // ✅ Usar "actividad" como en ofertas
        punto2.put("horaEstimada", "10:30");
        // Campos adicionales para seguimiento del tour asignado
        punto2.put("completado", true);
        punto2.put("horaLlegada", "10:25");
        punto2.put("horaSalida", "11:00");
        itinerarioLima.add(punto2);

        Map<String, Object> punto3 = new HashMap<>();
        punto3.put("orden", 3);
        punto3.put("lugar", "Convento de San Francisco");  // ✅ Usar "lugar" como en ofertas
        punto3.put("actividad", "Recorrido por catacumbas y biblioteca");  // ✅ Usar "actividad" como en ofertas
        punto3.put("horaEstimada", "11:15");
        // Campos adicionales para seguimiento del tour asignado
        punto3.put("completado", false);
        punto3.put("horaLlegada", null);
        punto3.put("horaSalida", null);
        itinerarioLima.add(punto3);

        // Crear participantes para Lima
        List<Map<String, Object>> participantesLima = new ArrayList<>();
        
        Map<String, Object> participante1 = new HashMap<>();
        participante1.put("clienteId", "LJ02gZgzedNIXxi3Yr3ppaxfElF3");
        participante1.put("nombreCliente", "Ana García Rodríguez");
        participante1.put("emailCliente", "ana.garcia@email.com");
        participante1.put("telefonoCliente", "981 030 557");
        participante1.put("fechaInscripcion", Timestamp.now());
        participante1.put("montoTotal", 150.0);
        participante1.put("estadoPago", "confirmado");
        
        List<Map<String, Object>> servicios1 = new ArrayList<>();
        Map<String, Object> servicio1 = new HashMap<>();
        servicio1.put("nombre", "Almuerzo en restaurante típico");
        servicio1.put("precio", 35.0);
        servicios1.add(servicio1);
        participante1.put("serviciosContratados", servicios1);
        participantesLima.add(participante1);

        Map<String, Object> participante2 = new HashMap<>();
        participante2.put("clienteId", "7AreAi73UAWbyH2de9FDJryrr4B3");
        participante2.put("nombreCliente", "Roberto Silva Mendoza");
        participante2.put("emailCliente", "roberto.silva@email.com");
        participante2.put("telefonoCliente", "976 431 852");
        participante2.put("fechaInscripcion", Timestamp.now());
        participante2.put("montoTotal", 115.0);
        participante2.put("estadoPago", "confirmado");
        participante2.put("serviciosContratados", new ArrayList<>());
        participantesLima.add(participante2);

        // Crear servicios adicionales
        List<Map<String, Object>> serviciosAdicionalesLima = new ArrayList<>();
        Map<String, Object> servicioAd1 = new HashMap<>();
        servicioAd1.put("nombre", "Almuerzo en restaurante típico");
        servicioAd1.put("descripcion", "Comida tradicional peruana en el centro histórico");
        servicioAd1.put("precio", 35.0);
        servicioAd1.put("disponible", true);
        serviciosAdicionalesLima.add(servicioAd1);

        Map<String, Object> servicioAd2 = new HashMap<>();
        servicioAd2.put("nombre", "Fotografía profesional");
        servicioAd2.put("descripcion", "Sesión fotográfica en lugares emblemáticos");
        servicioAd2.put("precio", 50.0);
        servicioAd2.put("disponible", true);
        serviciosAdicionalesLima.add(servicioAd2);

        // Crear documento del tour asignado Lima - MAÑANA (para testing botones)
        Map<String, Object> tourLima = new HashMap<>();
        tourLima.put("ofertaTourId", "tour_lima_historico_001");
        tourLima.put("titulo", "Lima Histórica - Centro Colonial");
        tourLima.put("descripcion", "Descubre la rica historia colonial de Lima recorriendo sus principales monumentos y plazas históricas del centro de la ciudad.");
        tourLima.put("precio", 115.0);
        tourLima.put("duracion", "4 horas");
        // ✅ FECHA PARA MAÑANA (tour pendiente)
        tourLima.put("fechaRealizacion", crearTimestampParaFecha("07/11/2025"));
        tourLima.put("horaInicio", "09:00");
        tourLima.put("horaFin", "13:00");
        tourLima.put("itinerario", itinerarioLima);
        tourLima.put("serviciosAdicionales", serviciosAdicionalesLima);
        tourLima.put("guiaAsignado", guiaAsignado);
        tourLima.put("empresaId", "empresa_tours_lima_001");
        tourLima.put("nombreEmpresa", "Lima Tours Culturales SAC");
        tourLima.put("correoEmpresa", "contacto@limatoursculturales.com");
        tourLima.put("pagoGuia", 85.0);
        
        List<String> idiomasLima = new ArrayList<>();
        idiomasLima.add("Español");
        idiomasLima.add("Inglés");
        tourLima.put("idiomasRequeridos", idiomasLima);
        
        tourLima.put("consideraciones", "Se requiere caminar aproximadamente 2km. Llevar zapatos cómodos y protector solar.");
        tourLima.put("participantes", participantesLima);
        tourLima.put("estado", "pendiente"); // ✅ Estado inicial pendiente
        tourLima.put("numeroParticipantesTotal", participantesLima.size()); // ✅ DINÁMICO basado en participantes reales
        tourLima.put("checkInRealizado", false);
        tourLima.put("checkOutRealizado", false);
        tourLima.put("horaCheckIn", null);
        tourLima.put("horaCheckOut", null);
        tourLima.put("reseniasClientes", new ArrayList<>());
        tourLima.put("calificacionPromedio", 0.0);
        tourLima.put("comentariosGuia", "");
        tourLima.put("fechaAsignacion", Timestamp.now());
        tourLima.put("fechaCreacion", Timestamp.now());
        tourLima.put("fechaActualizacion", Timestamp.now());
        tourLima.put("habilitado", true);

        // Insertar en Firebase
        db.collection("tours_asignados")
                .add(tourLima)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Tour Lima creado: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear tour Lima: ", e);
                });
    }

    /**
     * Tour asignado en Cusco - Machu Picchu
     */
    private void crearTourAsignadoCusco() {
        // Crear guía asignado para Cusco - USAR EL GUÍA ACTUAL
        Map<String, Object> guiaAsignado = new HashMap<>();
        guiaAsignado.put("identificadorUsuario", "YbmULw4iJXT41CdCLXV1ktCrfek1");
        guiaAsignado.put("nombresCompletos", "Gianfranco Enriquez Soel");
        guiaAsignado.put("correoElectronico", "a20224926@pucp.edu.pe");
        guiaAsignado.put("numeroTelefono", "984 567 123");
        guiaAsignado.put("fechaAsignacion", Timestamp.now());

        // Crear itinerario para Cusco
        List<Map<String, Object>> itinerarioCusco = new ArrayList<>();
        
        Map<String, Object> punto1 = new HashMap<>();
        punto1.put("orden", 1);
        punto1.put("lugar", "Estación San Pedro");  // ✅ Usar "lugar" como en ofertas
        punto1.put("actividad", "Punto de encuentro y abordaje del tren");  // ✅ Usar "actividad" como en ofertas
        punto1.put("horaEstimada", "06:30");
        // Campos adicionales para seguimiento del tour asignado
        punto1.put("completado", false);
        punto1.put("horaLlegada", null);
        punto1.put("horaSalida", null);
        itinerarioCusco.add(punto1);

        Map<String, Object> punto2 = new HashMap<>();
        punto2.put("orden", 2);
        punto2.put("lugar", "Aguas Calientes");  // ✅ Usar "lugar" como en ofertas
        punto2.put("actividad", "Llegada y traslado en bus a Machu Picchu");  // ✅ Usar "actividad" como en ofertas
        punto2.put("horaEstimada", "09:45");
        // Campos adicionales para seguimiento del tour asignado
        punto2.put("completado", false);
        punto2.put("horaLlegada", null);
        punto2.put("horaSalida", null);
        itinerarioCusco.add(punto2);

        Map<String, Object> punto3 = new HashMap<>();
        punto3.put("orden", 3);
        punto3.put("lugar", "Ciudadela de Machu Picchu");  // ✅ Usar "lugar" como en ofertas
        punto3.put("actividad", "Recorrido guiado por la ciudadela inca");  // ✅ Usar "actividad" como en ofertas
        punto3.put("horaEstimada", "10:30");
        // Campos adicionales para seguimiento del tour asignado
        punto3.put("completado", false);
        punto3.put("horaLlegada", null);
        punto3.put("horaSalida", null);
        itinerarioCusco.add(punto3);

        // Crear participantes para Cusco
        List<Map<String, Object>> participantesCusco = new ArrayList<>();
        
        Map<String, Object> participante1 = new HashMap<>();
        participante1.put("clienteId", "cliente_cusco_001");
        participante1.put("nombreCliente", "John Smith");
        participante1.put("emailCliente", "john.smith@email.com");
        participante1.put("telefonoCliente", "+1 555 123 456");
        participante1.put("fechaInscripcion", Timestamp.now());
        participante1.put("montoTotal", 450.0);
        participante1.put("estadoPago", "confirmado");
        
        List<Map<String, Object>> servicios1 = new ArrayList<>();
        Map<String, Object> servicio1 = new HashMap<>();
        servicio1.put("nombre", "Almuerzo en Aguas Calientes");
        servicio1.put("precio", 45.0);
        servicios1.add(servicio1);
        participante1.put("serviciosContratados", servicios1);
        participantesCusco.add(participante1);

        // Crear servicios adicionales
        List<Map<String, Object>> serviciosAdicionalesCusco = new ArrayList<>();
        Map<String, Object> servicioAd1 = new HashMap<>();
        servicioAd1.put("nombre", "Almuerzo en Aguas Calientes");
        servicioAd1.put("descripcion", "Almuerzo típico en restaurante local");
        servicioAd1.put("precio", 45.0);
        servicioAd1.put("disponible", true);
        serviciosAdicionalesCusco.add(servicioAd1);

        // Crear documento del tour asignado Cusco
        Map<String, Object> tourCusco = new HashMap<>();
        tourCusco.put("ofertaTourId", "tour_cusco_machu_001");
        tourCusco.put("titulo", "Machu Picchu - Día Completo");
        tourCusco.put("descripcion", "Visita a la maravillosa ciudadela inca de Machu Picchu con guía especializado en historia andina.");
        tourCusco.put("precio", 405.0);
        tourCusco.put("duracion", "12 horas");
        tourCusco.put("fechaRealizacion", crearTimestampParaFecha("20/11/2025"));
        tourCusco.put("horaInicio", "06:30");
        tourCusco.put("horaFin", "18:30");
        tourCusco.put("itinerario", itinerarioCusco);
        tourCusco.put("serviciosAdicionales", serviciosAdicionalesCusco);
        tourCusco.put("guiaAsignado", guiaAsignado);
        tourCusco.put("empresaId", "empresa_cusco_tours_001");
        tourCusco.put("nombreEmpresa", "Inca Trail Adventures");
        tourCusco.put("correoEmpresa", "info@incatrailadventures.com");
        tourCusco.put("pagoGuia", 120.0);
        
        List<String> idiomasCusco = new ArrayList<>();
        idiomasCusco.add("Español");
        idiomasCusco.add("Inglés");
        idiomasCusco.add("Quechua");
        tourCusco.put("idiomasRequeridos", idiomasCusco);
        
        tourCusco.put("consideraciones", "Incluye transporte en tren. Documentos requeridos: pasaporte o DNI. Altitud de 2400m.");
        tourCusco.put("participantes", participantesCusco);
        tourCusco.put("estado", "pendiente"); // ✅ Estado inicial pendiente
        tourCusco.put("numeroParticipantesTotal", participantesCusco.size());
        tourCusco.put("checkInRealizado", false);
        tourCusco.put("checkOutRealizado", false);
        tourCusco.put("horaCheckIn", null);
        tourCusco.put("horaCheckOut", null);
        tourCusco.put("reseniasClientes", new ArrayList<>());
        tourCusco.put("calificacionPromedio", 0.0);
        tourCusco.put("comentariosGuia", "");
        tourCusco.put("fechaAsignacion", Timestamp.now());
        tourCusco.put("fechaCreacion", Timestamp.now());
        tourCusco.put("fechaActualizacion", Timestamp.now());
        tourCusco.put("habilitado", true);

        // Insertar en Firebase
        db.collection("tours_asignados")
                .add(tourCusco)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Tour Cusco creado: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear tour Cusco: ", e);
                });
    }

    /**
     * Tour asignado en Arequipa - Ciudad Blanca
     */
    private void crearTourAsignadoArequipa() {
        // Crear guía asignado para Arequipa - USAR EL GUÍA ACTUAL
        Map<String, Object> guiaAsignado = new HashMap<>();
        guiaAsignado.put("identificadorUsuario", "YbmULw4iJXT41CdCLXV1ktCrfek1");
        guiaAsignado.put("nombresCompletos", "Gianfranco Enriquez Soel");
        guiaAsignado.put("correoElectronico", "a20224926@pucp.edu.pe");
        guiaAsignado.put("numeroTelefono", "958 741 123");
        guiaAsignado.put("fechaAsignacion", Timestamp.now());

        // Crear itinerario para Arequipa - COMPATIBLE CON OFERTAS
        List<Map<String, Object>> itinerarioArequipa = new ArrayList<>();
        
        Map<String, Object> punto1 = new HashMap<>();
        punto1.put("orden", 1);
        punto1.put("lugar", "Plaza de Armas");  // ✅ Usar "lugar" como en ofertas
        punto1.put("actividad", "Centro histórico y catedral");  // ✅ Usar "actividad" como en ofertas
        punto1.put("horaEstimada", "10:00");
        // Campos adicionales para seguimiento del tour asignado
        punto1.put("completado", false);
        punto1.put("horaLlegada", null);
        punto1.put("horaSalida", null);
        itinerarioArequipa.add(punto1);

        Map<String, Object> punto2 = new HashMap<>();
        punto2.put("orden", 2);
        punto2.put("lugar", "Monasterio de Santa Catalina");  // ✅ Usar "lugar" como en ofertas
        punto2.put("actividad", "Recorrido por el monasterio colonial");  // ✅ Usar "actividad" como en ofertas
        punto2.put("horaEstimada", "11:00");
        // Campos adicionales para seguimiento del tour asignado
        punto2.put("completado", false);
        punto2.put("horaLlegada", null);
        punto2.put("horaSalida", null);
        itinerarioArequipa.add(punto2);

        // Crear participantes para Arequipa
        List<Map<String, Object>> participantesArequipa = new ArrayList<>();
        
        Map<String, Object> participante1 = new HashMap<>();
        participante1.put("clienteId", "cliente_arequipa_001");
        participante1.put("nombreCliente", "Carmen Fernández López");
        participante1.put("emailCliente", "carmen.fernandez@email.com");
        participante1.put("telefonoCliente", "987 654 321");
        participante1.put("fechaInscripcion", Timestamp.now());
        participante1.put("montoTotal", 80.0);
        participante1.put("estadoPago", "confirmado");
        participante1.put("serviciosContratados", new ArrayList<>());
        participantesArequipa.add(participante1);

        // Crear servicios adicionales
        List<Map<String, Object>> serviciosAdicionalesArequipa = new ArrayList<>();
        Map<String, Object> servicioAd1 = new HashMap<>();
        servicioAd1.put("nombre", "Degustación de picanterías");
        servicioAd1.put("descripcion", "Prueba de platos típicos arequipeños");
        servicioAd1.put("precio", 25.0);
        servicioAd1.put("disponible", true);
        serviciosAdicionalesArequipa.add(servicioAd1);

        // Crear documento del tour asignado Arequipa
        Map<String, Object> tourArequipa = new HashMap<>();
        tourArequipa.put("ofertaTourId", "tour_arequipa_colonial_001");
        tourArequipa.put("titulo", "Arequipa Colonial - Ciudad Blanca");
        tourArequipa.put("descripcion", "Descubre la arquitectura colonial de Arequipa, patrimonio mundial de la UNESCO.");
        tourArequipa.put("precio", 80.0);
        tourArequipa.put("duracion", "3 horas");
        tourArequipa.put("fechaRealizacion", crearTimestampParaFecha("25/11/2025"));
        tourArequipa.put("horaInicio", "10:00");
        tourArequipa.put("horaFin", "13:00");
        tourArequipa.put("itinerario", itinerarioArequipa);
        tourArequipa.put("serviciosAdicionales", serviciosAdicionalesArequipa);
        tourArequipa.put("guiaAsignado", guiaAsignado);
        tourArequipa.put("empresaId", "empresa_arequipa_tours_001");
        tourArequipa.put("nombreEmpresa", "Arequipa Heritage Tours");
        tourArequipa.put("correoEmpresa", "contacto@arequipaheritagetours.com");
        tourArequipa.put("pagoGuia", 55.0);
        
        List<String> idiomasArequipa = new ArrayList<>();
        idiomasArequipa.add("Español");
        idiomasArequipa.add("Inglés");
        tourArequipa.put("idiomasRequeridos", idiomasArequipa);
        
        tourArequipa.put("consideraciones", "Recorrido peatonal por el centro histórico. Protección solar recomendada.");
        tourArequipa.put("participantes", participantesArequipa);
        tourArequipa.put("estado", "pendiente"); // ✅ Estado inicial pendiente
        tourArequipa.put("numeroParticipantesTotal", participantesArequipa.size());
        tourArequipa.put("checkInRealizado", false);
        tourArequipa.put("checkOutRealizado", false);
        tourArequipa.put("horaCheckIn", null);
        tourArequipa.put("horaCheckOut", null);
        tourArequipa.put("reseniasClientes", new ArrayList<>());
        tourArequipa.put("calificacionPromedio", 0.0);
        tourArequipa.put("comentariosGuia", "");
        tourArequipa.put("fechaAsignacion", Timestamp.now());
        tourArequipa.put("fechaCreacion", Timestamp.now());
        tourArequipa.put("fechaActualizacion", Timestamp.now());
        tourArequipa.put("habilitado", true);

        // Insertar en Firebase
        db.collection("tours_asignados")
                .add(tourArequipa)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Tour Arequipa creado: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear tour Arequipa: ", e);
                });
    }

    /**
     * Tour asignado en Ica - Oasis de Huacachina
     */
    private void crearTourAsignadoIca() {
        // Crear guía asignado para Ica - USAR EL GUÍA ACTUAL
        Map<String, Object> guiaAsignado = new HashMap<>();
        guiaAsignado.put("identificadorUsuario", "YbmULw4iJXT41CdCLXV1ktCrfek1");
        guiaAsignado.put("nombresCompletos", "Gianfranco Enriquez Soel");
        guiaAsignado.put("correoElectronico", "a20224926@pucp.edu.pe");
        guiaAsignado.put("numeroTelefono", "965 874 123");
        guiaAsignado.put("fechaAsignacion", Timestamp.now());

        // Crear itinerario para Ica - COMPATIBLE CON OFERTAS
        List<Map<String, Object>> itinerarioIca = new ArrayList<>();
        
        Map<String, Object> punto1 = new HashMap<>();
        punto1.put("orden", 1);
        punto1.put("lugar", "Laguna de Huacachina");  // ✅ Usar "lugar" como en ofertas
        punto1.put("actividad", "Recorrido por el oasis natural");  // ✅ Usar "actividad" como en ofertas
        punto1.put("horaEstimada", "15:00");
        // Campos adicionales para seguimiento del tour asignado
        punto1.put("completado", false);
        punto1.put("horaLlegada", null);
        punto1.put("horaSalida", null);
        itinerarioIca.add(punto1);

        Map<String, Object> punto2 = new HashMap<>();
        punto2.put("orden", 2);
        punto2.put("lugar", "Sandboarding");  // ✅ Usar "lugar" como en ofertas
        punto2.put("actividad", "Actividad de deslizamiento en dunas");  // ✅ Usar "actividad" como en ofertas
        punto2.put("horaEstimada", "16:00");
        // Campos adicionales para seguimiento del tour asignado
        punto2.put("completado", false);
        punto2.put("horaLlegada", null);
        punto2.put("horaSalida", null);
        itinerarioIca.add(punto2);

        // Crear participantes para Ica
        List<Map<String, Object>> participantesIca = new ArrayList<>();
        
        Map<String, Object> participante1 = new HashMap<>();
        participante1.put("clienteId", "cliente_ica_001");
        participante1.put("nombreCliente", "Diego Morales Castro");
        participante1.put("emailCliente", "diego.morales@email.com");
        participante1.put("telefonoCliente", "956 123 789");
        participante1.put("fechaInscripcion", Timestamp.now());
        participante1.put("montoTotal", 95.0);
        participante1.put("estadoPago", "pendiente");
        
        List<Map<String, Object>> servicios1 = new ArrayList<>();
        Map<String, Object> servicio1 = new HashMap<>();
        servicio1.put("nombre", "Equipo de sandboarding");
        servicio1.put("precio", 15.0);
        servicios1.add(servicio1);
        participante1.put("serviciosContratados", servicios1);
        participantesIca.add(participante1);

        // Crear servicios adicionales
        List<Map<String, Object>> serviciosAdicionalesIca = new ArrayList<>();
        Map<String, Object> servicioAd1 = new HashMap<>();
        servicioAd1.put("nombre", "Equipo de sandboarding");
        servicioAd1.put("descripcion", "Tabla y equipo de protección completo");
        servicioAd1.put("precio", 15.0);
        servicioAd1.put("disponible", true);
        serviciosAdicionalesIca.add(servicioAd1);

        Map<String, Object> servicioAd2 = new HashMap<>();
        servicioAd2.put("nombre", "Paseo en buggy");
        servicioAd2.put("descripcion", "Recorrido por las dunas en vehículo especializado");
        servicioAd2.put("precio", 30.0);
        servicioAd2.put("disponible", true);
        serviciosAdicionalesIca.add(servicioAd2);

        // Crear documento del tour asignado Ica
        Map<String, Object> tourIca = new HashMap<>();
        tourIca.put("ofertaTourId", "tour_ica_huacachina_001");
        tourIca.put("titulo", "Oasis de Huacachina - Aventura en el desierto");
        tourIca.put("descripcion", "Vive la emoción del desierto con sandboarding y recorridos por las dunas de Huacachina.");
        tourIca.put("precio", 80.0);
        tourIca.put("duracion", "4 horas");
        tourIca.put("fechaRealizacion", crearTimestampParaFecha("30/11/2025"));
        tourIca.put("horaInicio", "15:00");
        tourIca.put("horaFin", "19:00");
        tourIca.put("itinerario", itinerarioIca);
        tourIca.put("serviciosAdicionales", serviciosAdicionalesIca);
        tourIca.put("guiaAsignado", guiaAsignado);
        tourIca.put("empresaId", "empresa_ica_adventures_001");
        tourIca.put("nombreEmpresa", "Desert Adventures Ica");
        tourIca.put("correoEmpresa", "info@desertadventuresica.com");
        tourIca.put("pagoGuia", 60.0);
        
        List<String> idiomasIca = new ArrayList<>();
        idiomasIca.add("Español");
        idiomasIca.add("Inglés");
        tourIca.put("idiomasRequeridos", idiomasIca);
        
        tourIca.put("consideraciones", "Actividad física moderada. Llevar ropa cómoda y protección solar. No recomendado para personas con problemas cardíacos.");
        tourIca.put("participantes", participantesIca);
        tourIca.put("estado", "pendiente"); // ✅ Estado inicial pendiente
        tourIca.put("numeroParticipantesTotal", participantesIca.size());
        tourIca.put("checkInRealizado", false);
        tourIca.put("checkOutRealizado", false);
        tourIca.put("horaCheckIn", null);
        tourIca.put("horaCheckOut", null);
        tourIca.put("reseniasClientes", new ArrayList<>());
        tourIca.put("calificacionPromedio", 0.0);
        tourIca.put("comentariosGuia", "");
        tourIca.put("fechaAsignacion", Timestamp.now());
        tourIca.put("fechaCreacion", Timestamp.now());
        tourIca.put("fechaActualizacion", Timestamp.now());
        tourIca.put("habilitado", true);

        // Insertar en Firebase
        db.collection("tours_asignados")
                .add(tourIca)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Tour Ica creado: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear tour Ica: ", e);
                });
    }

    /**
     * Tour EN CURSO para testing - Huacachina hoy
     */
    private void crearTourEnCurso() {
        // Crear guía asignado - USAR EL GUÍA ACTUAL
        Map<String, Object> guiaAsignado = new HashMap<>();
        guiaAsignado.put("identificadorUsuario", "YbmULw4iJXT41CdCLXV1ktCrfek1");
        guiaAsignado.put("nombresCompletos", "Gianfranco Enriquez Soel");
        guiaAsignado.put("correoElectronico", "a20224926@pucp.edu.pe");
        guiaAsignado.put("numeroTelefono", "987 654 321");
        guiaAsignado.put("fechaAsignacion", Timestamp.now());

        // Crear itinerario con seguimiento - COMPATIBLE CON OFERTAS
        List<Map<String, Object>> itinerarioHuacachina = new ArrayList<>();
        
        Map<String, Object> punto1 = new HashMap<>();
        punto1.put("orden", 1);
        punto1.put("lugar", "Laguna de Huacachina");  // ✅ Usar "lugar" como en ofertas
        punto1.put("actividad", "Paseo en tubulares por las dunas");  // ✅ Usar "actividad" como en ofertas
        punto1.put("horaEstimada", "10:00");
        punto1.put("completado", true);
        punto1.put("horaLlegada", "10:05");
        punto1.put("horaSalida", "11:30");
        itinerarioHuacachina.add(punto1);

        Map<String, Object> punto2 = new HashMap<>();
        punto2.put("orden", 2);
        punto2.put("lugar", "Dunas del desierto");  // ✅ Usar "lugar" como en ofertas
        punto2.put("actividad", "Sandboarding y fotografías");  // ✅ Usar "actividad" como en ofertas
        punto2.put("horaEstimada", "11:45");
        punto2.put("completado", true);
        punto2.put("horaLlegada", "11:40");
        punto2.put("horaSalida", "13:00");
        itinerarioHuacachina.add(punto2);

        Map<String, Object> punto3 = new HashMap<>();
        punto3.put("orden", 3);
        punto3.put("lugar", "Oasis de Huacachina");  // ✅ Usar "lugar" como en ofertas
        punto3.put("actividad", "Almuerzo y tiempo libre");  // ✅ Usar "actividad" como en ofertas
        punto3.put("horaEstimada", "13:15");
        punto3.put("completado", false);
        punto3.put("horaLlegada", null);
        punto3.put("horaSalida", null);
        itinerarioHuacachina.add(punto3);

        // Crear participantes para tour en curso
        List<Map<String, Object>> participantesHuacachina = new ArrayList<>();
        
        Map<String, Object> participante1 = new HashMap<>();
        participante1.put("nombre", "Ana Lucía Rodriguez");
        participante1.put("correo", "ana.rodriguez@gmail.com");
        participante1.put("telefono", "987 123 456");
        participante1.put("tipoDocumento", "DNI");
        participante1.put("numeroDocumento", "70123456");
        participante1.put("fechaNacimiento", "1995-03-22");
        participante1.put("nacionalidad", "Peruana");
        participante1.put("contactoEmergencia", "Maria Rodriguez - 987 654 321");
        participante1.put("checkIn", true);
        participantesHuacachina.add(participante1);

        Map<String, Object> participante2 = new HashMap<>();
        participante2.put("nombre", "Carlos Miguel Torres");
        participante2.put("correo", "carlos.torres@outlook.com");
        participante2.put("telefono", "987 987 987");
        participante2.put("tipoDocumento", "Pasaporte");
        participante2.put("numeroDocumento", "ARG123456789");
        participante2.put("fechaNacimiento", "1988-11-15");
        participante2.put("nacionalidad", "Argentina");
        participante2.put("contactoEmergencia", "Lucia Torres - 987 321 654");
        participante2.put("checkIn", true);
        participantesHuacachina.add(participante2);

        Map<String, Object> participante3 = new HashMap<>();
        participante3.put("nombre", "Sophie Chen");
        participante3.put("correo", "sophie.chen@email.com");
        participante3.put("telefono", "+1 555 123 4567");
        participante3.put("tipoDocumento", "Pasaporte");
        participante3.put("numeroDocumento", "USA987654321");
        participante3.put("fechaNacimiento", "1992-07-08");
        participante3.put("nacionalidad", "Estadounidense");
        participante3.put("contactoEmergencia", "David Chen - +1 555 987 6543");
        participante3.put("checkIn", true);
        participantesHuacachina.add(participante3);

        // Servicios adicionales
        List<Map<String, Object>> serviciosAdicionalesHuacachina = new ArrayList<>();
        Map<String, Object> servicioAd1 = new HashMap<>();
        servicioAd1.put("nombre", "Almuerzo típico");
        servicioAd1.put("descripcion", "Almuerzo en restaurante del oasis");
        servicioAd1.put("precio", 25.0);
        servicioAd1.put("disponible", true);
        serviciosAdicionalesHuacachina.add(servicioAd1);

        // Crear documento del tour EN CURSO
        Map<String, Object> tourEnCurso = new HashMap<>();
        tourEnCurso.put("ofertaTourId", "tour_huacachina_aventura_003");
        tourEnCurso.put("titulo", "Huacachina Aventura - Dunas y Oasis");
        tourEnCurso.put("descripcion", "Experiencia completa en el oasis de Huacachina con tubulares, sandboarding y paisajes únicos del desierto peruano.");
        tourEnCurso.put("precio", 85.0);
        tourEnCurso.put("duracion", "6 horas");
        // ✅ FECHA PARA HOY (tour EN CURSO)
        tourEnCurso.put("fechaRealizacion", crearTimestampParaFecha("06/11/2025"));
        tourEnCurso.put("horaInicio", "09:30");
        tourEnCurso.put("horaFin", "15:30");
        tourEnCurso.put("itinerario", itinerarioHuacachina);
        tourEnCurso.put("serviciosAdicionales", serviciosAdicionalesHuacachina);
        tourEnCurso.put("guiaAsignado", guiaAsignado);
        tourEnCurso.put("empresaId", "empresa_ica_adventures_003");
        tourEnCurso.put("nombreEmpresa", "Ica Desert Adventures");
        tourEnCurso.put("correoEmpresa", "contacto@icaadventures.com");
        tourEnCurso.put("pagoGuia", 70.0);
        tourEnCurso.put("idiomasRequeridos", List.of("Español", "Inglés"));
        tourEnCurso.put("consideraciones", "Actividad de aventura. Requerido experiencia mínima. Incluye equipo de seguridad.");
        tourEnCurso.put("participantes", participantesHuacachina);
        // ✅ ESTADO EN CURSO PARA MOSTRAR BOTONES ACTIVOS
        tourEnCurso.put("estado", "en_curso");
        tourEnCurso.put("numeroParticipantesTotal", participantesHuacachina.size()); // ✅ DINÁMICO basado en participantes reales
        tourEnCurso.put("checkInRealizado", true);
        tourEnCurso.put("checkOutRealizado", false);
        tourEnCurso.put("horaCheckIn", crearTimestampParaFecha("06/11/2025 09:45"));
        tourEnCurso.put("horaCheckOut", null);
        tourEnCurso.put("reseniasClientes", new ArrayList<>());
        tourEnCurso.put("calificacionPromedio", 0.0);
        tourEnCurso.put("comentariosGuia", "Tour en progreso. Todos los participantes llegaron puntuales. Excelente clima para la actividad.");
        tourEnCurso.put("fechaAsignacion", Timestamp.now());
        tourEnCurso.put("fechaCreacion", Timestamp.now());
        tourEnCurso.put("fechaActualizacion", Timestamp.now());
        tourEnCurso.put("habilitado", true);

        // Insertar en Firebase
        db.collection("tours_asignados")
                .add(tourEnCurso)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Tour EN CURSO creado: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear tour EN CURSO: ", e);
                });
    }
    
    /**
     * Helper para crear Timestamp desde fecha en formato String
     */
    private Timestamp crearTimestampParaFecha(String fechaString) {
        try {
            SimpleDateFormat sdf;
            // Verificar si el string incluye hora
            if (fechaString.contains(" ")) {
                sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            } else {
                sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            }
            Date fecha = sdf.parse(fechaString);
            return new Timestamp(fecha);
        } catch (ParseException e) {
            Log.e(TAG, "Error al parsear fecha: " + fechaString, e);
            return Timestamp.now(); // Fallback a fecha actual
        }
    }
}
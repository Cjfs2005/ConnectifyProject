package com.example.connectifyproject.utils;

import android.util.Log;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TourDataSeeder {
    private static final String TAG = "TourDataSeeder";
    private static final String COLLECTION_OFERTAS = "tours_ofertas";
    
    public static void crearOfertasDePrueba() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Oferta 1: City Tour Lima Centro Histórico
        Map<String, Object> oferta1 = new HashMap<>();
        oferta1.put("titulo", "City Tour Lima Centro Histórico");
        oferta1.put("descripcion", "Explora el corazón colonial de Lima visitando lugares emblemáticos");
        oferta1.put("precio", 65.0);
        oferta1.put("duracion", "4 horas");
        oferta1.put("fechaRealizacion", "15/11/2025");
        oferta1.put("horaInicio", "09:00");
        oferta1.put("horaFin", "13:00");
        
        // Itinerario Oferta 1
        List<Map<String, Object>> itinerario1 = new ArrayList<>();
        Map<String, Object> punto1 = new HashMap<>();
        punto1.put("orden", 1);
        punto1.put("lugar", "Plaza Mayor");
        punto1.put("horaEstimada", "09:00");
        punto1.put("actividad", "Inicio del tour en el corazón de Lima colonial");
        itinerario1.add(punto1);
        
        Map<String, Object> punto2 = new HashMap<>();
        punto2.put("orden", 2);
        punto2.put("lugar", "Catedral de Lima");
        punto2.put("horaEstimada", "09:30");
        punto2.put("actividad", "Visita a la catedral metropolitana");
        itinerario1.add(punto2);
        
        Map<String, Object> punto3 = new HashMap<>();
        punto3.put("orden", 3);
        punto3.put("lugar", "Palacio de Gobierno");
        punto3.put("horaEstimada", "10:30");
        punto3.put("actividad", "Tour por la Casa de Pizarro");
        itinerario1.add(punto3);
        
        oferta1.put("itinerario", itinerario1);
        
        // Servicios Adicionales Oferta 1
        List<Map<String, Object>> servicios1 = new ArrayList<>();
        Map<String, Object> servicio1 = new HashMap<>();
        servicio1.put("nombre", "Almuerzo en restaurante típico");
        servicio1.put("descripcion", "Comida tradicional peruana en restaurante del centro");
        servicio1.put("esPagado", true);
        servicio1.put("precio", 25.0);
        servicios1.add(servicio1);
        
        Map<String, Object> servicio2 = new HashMap<>();
        servicio2.put("nombre", "Transporte en bus turístico");
        servicio2.put("descripcion", "Traslado cómodo entre puntos del itinerario");
        servicio2.put("esPagado", false);
        servicio2.put("precio", 0.0);
        servicios1.add(servicio2);
        
        Map<String, Object> servicio3 = new HashMap<>();
        servicio3.put("nombre", "Audífonos para grupo");
        servicio3.put("descripcion", "Equipo de audio para mejor experiencia del tour");
        servicio3.put("esPagado", false);
        servicio3.put("precio", 0.0);
        servicios1.add(servicio3);
        
        oferta1.put("serviciosAdicionales", servicios1);
        
        // Información de empresa (Trujillo Tours)
        oferta1.put("empresaId", "YkFFwgnA5Mg5apyDZxPRLDF3OZF3");
        oferta1.put("nombreEmpresa", "Trujillo Tours");
        oferta1.put("correoEmpresa", "trujillotours@gmail.com");
        
        // Requisitos para guías
        oferta1.put("pagoGuia", 450.0);
        oferta1.put("idiomasRequeridos", Arrays.asList("Espanol", "Ingles"));
        oferta1.put("consideraciones", "Minimo 1 ano como guia turistico. Conocimiento en historia colonial de Lima.");
        
        // Control de estado
        oferta1.put("estado", "publicado");
        oferta1.put("guiaAsignadoId", null);
        oferta1.put("fechaAsignacion", null);
        
        // Metadatos
        oferta1.put("fechaCreacion", Timestamp.now());
        oferta1.put("fechaActualizacion", Timestamp.now());
        oferta1.put("habilitado", true);
        oferta1.put("perfilCompleto", true);
        
        // Oferta 2: Tour Barranco Bohemio
        Map<String, Object> oferta2 = new HashMap<>();
        oferta2.put("titulo", "Tour Barranco Bohemio");
        oferta2.put("descripcion", "Descubre el distrito artístico y bohemio de Lima");
        oferta2.put("precio", 50.0);
        oferta2.put("duracion", "3 horas");
        oferta2.put("fechaRealizacion", "16/11/2025");
        oferta2.put("horaInicio", "15:00");
        oferta2.put("horaFin", "18:00");
        
        // Itinerario Oferta 2
        List<Map<String, Object>> itinerario2 = new ArrayList<>();
        Map<String, Object> punto2_1 = new HashMap<>();
        punto2_1.put("orden", 1);
        punto2_1.put("lugar", "Puente de los Suspiros");
        punto2_1.put("horaEstimada", "15:00");
        punto2_1.put("actividad", "Inicio del tour en el icónico puente");
        itinerario2.add(punto2_1);
        
        Map<String, Object> punto2_2 = new HashMap<>();
        punto2_2.put("orden", 2);
        punto2_2.put("lugar", "Galería de Arte");
        punto2_2.put("horaEstimada", "15:30");
        punto2_2.put("actividad", "Visita a galería de arte local");
        itinerario2.add(punto2_2);
        
        Map<String, Object> punto2_3 = new HashMap<>();
        punto2_3.put("orden", 3);
        punto2_3.put("lugar", "Malecón de Barranco");
        punto2_3.put("horaEstimada", "16:30");
        punto2_3.put("actividad", "Caminata con vista al océano Pacífico");
        itinerario2.add(punto2_3);
        
        oferta2.put("itinerario", itinerario2);
        
        // Servicios Adicionales Oferta 2
        List<Map<String, Object>> servicios2 = new ArrayList<>();
        Map<String, Object> servicio2_1 = new HashMap<>();
        servicio2_1.put("nombre", "Café en terraza con vista");
        servicio2_1.put("descripcion", "Café y postres en terraza con vista al mar");
        servicio2_1.put("esPagado", true);
        servicio2_1.put("precio", 15.0);
        servicios2.add(servicio2_1);
        
        Map<String, Object> servicio2_2 = new HashMap<>();
        servicio2_2.put("nombre", "Guía especializada en arte");
        servicio2_2.put("descripcion", "Acompañamiento de experto en arte contemporáneo");
        servicio2_2.put("esPagado", false);
        servicio2_2.put("precio", 0.0);
        servicios2.add(servicio2_2);
        
        oferta2.put("serviciosAdicionales", servicios2);
        
        // Información de empresa (Lima Tours)
        oferta2.put("empresaId", "SovoYlsMA5UEC4P8MydLFaKuYVw2");
        oferta2.put("nombreEmpresa", "Lima Tours");
        oferta2.put("correoEmpresa", "limatours@gmail.com");
        
        // Requisitos para guías
        oferta2.put("pagoGuia", 300.0);
        oferta2.put("idiomasRequeridos", Arrays.asList("Espanol"));
        oferta2.put("consideraciones", "Conocimiento de arte y cultura bohemia.");
        
        // Control de estado
        oferta2.put("estado", "publicado");
        oferta2.put("guiaAsignadoId", null);
        oferta2.put("fechaAsignacion", null);
        
        // Metadatos
        oferta2.put("fechaCreacion", Timestamp.now());
        oferta2.put("fechaActualizacion", Timestamp.now());
        oferta2.put("habilitado", true);
        oferta2.put("perfilCompleto", true);
        
        // Oferta 3: Circuito Gastronómico
        Map<String, Object> oferta3 = new HashMap<>();
        oferta3.put("titulo", "Circuito Gastronómico Lima");
        oferta3.put("descripcion", "Descubre los sabores tradicionales de la capital");
        oferta3.put("precio", 80.0);
        oferta3.put("duracion", "5 horas");
        oferta3.put("fechaRealizacion", "17/11/2025");
        oferta3.put("horaInicio", "11:00");
        oferta3.put("horaFin", "16:00");
        
        // Itinerario Oferta 3
        List<Map<String, Object>> itinerario3 = new ArrayList<>();
        Map<String, Object> punto3_1 = new HashMap<>();
        punto3_1.put("orden", 1);
        punto3_1.put("lugar", "Mercado Central");
        punto3_1.put("horaEstimada", "11:00");
        punto3_1.put("actividad", "Degustación de frutas exóticas");
        itinerario3.add(punto3_1);
        
        Map<String, Object> punto3_2 = new HashMap<>();
        punto3_2.put("orden", 2);
        punto3_2.put("lugar", "Restaurante Criollo");
        punto3_2.put("horaEstimada", "13:00");
        punto3_2.put("actividad", "Almuerzo tradicional peruano");
        itinerario3.add(punto3_2);
        
        oferta3.put("itinerario", itinerario3);
        
        // Servicios Adicionales Oferta 3
        List<Map<String, Object>> servicios3 = new ArrayList<>();
        Map<String, Object> servicio3_1 = new HashMap<>();
        servicio3_1.put("nombre", "Degustaciones incluidas");
        servicio3_1.put("descripcion", "Todas las degustaciones del recorrido");
        servicio3_1.put("esPagado", false);
        servicio3_1.put("precio", 0.0);
        servicios3.add(servicio3_1);
        
        oferta3.put("serviciosAdicionales", servicios3);
        
        // Información de empresa (Santa Anita Tours)
        oferta3.put("empresaId", "VKg9uzGmS5UgcrE7wwGrisG6LpZ2");
        oferta3.put("nombreEmpresa", "Santa Anita Tours");
        oferta3.put("correoEmpresa", "a@gmail.com");
        
        // Requisitos para guías
        oferta3.put("pagoGuia", 400.0);
        oferta3.put("idiomasRequeridos", Arrays.asList("Espanol", "Ingles"));
        oferta3.put("consideraciones", "Conocimiento de gastronomía peruana tradicional.");
        
        // Control de estado
        oferta3.put("estado", "publicado");
        oferta3.put("guiaAsignadoId", null);
        oferta3.put("fechaAsignacion", null);
        
        // Metadatos
        oferta3.put("fechaCreacion", Timestamp.now());
        oferta3.put("fechaActualizacion", Timestamp.now());
        oferta3.put("habilitado", true);
        oferta3.put("perfilCompleto", true);
        
        // Insertar ofertas en Firebase
        db.collection(COLLECTION_OFERTAS).add(oferta1)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Oferta 1 creada con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear oferta 1: ", e);
                });
        
        db.collection(COLLECTION_OFERTAS).add(oferta2)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Oferta 2 creada con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear oferta 2: ", e);
                });
                
        db.collection(COLLECTION_OFERTAS).add(oferta3)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Oferta 3 creada con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear oferta 3: ", e);
                });
    }
}
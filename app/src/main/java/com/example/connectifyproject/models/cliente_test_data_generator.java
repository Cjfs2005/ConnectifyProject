package com.example.connectifyproject.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generador de datos de prueba para tours y empresas
 */
public class cliente_test_data_generator {
    
    /**
     * Genera una lista de empresas de prueba
     */
    public static List<cliente_test_empresa> generarEmpresasPrueba() {
        List<cliente_test_empresa> empresas = new ArrayList<>();
        
        empresas.add(new cliente_test_empresa("emp_001", "Lima Tours", 
            "Av. Garcilaso 369, Lima", "+51 999 888 777", "contacto@limatours.com",
            "Especialistas en tours por Lima y sus alrededores con más de 10 años de experiencia.",
            "logo_lima_tours", "imagen_lima_tours", 4.5, 120));
            
        empresas.add(new cliente_test_empresa("emp_002", "Cusco Adventures", 
            "Calle Plateros 315, Cusco", "+51 988 777 666", "info@cuscoadventures.com",
            "Tours de aventura en Cusco, Machu Picchu y el Valle Sagrado.",
            "logo_cusco_adventures", "imagen_cusco_adventures", 4.8, 89));
            
        empresas.add(new cliente_test_empresa("emp_003", "Arequipa Explorer", 
            "Av. La Marina 234, Arequipa", "+51 977 666 555", "tours@arequipaexplorer.com",
            "Descubre la ciudad blanca y sus maravillas naturales con nosotros.",
            "logo_arequipa_explorer", "imagen_arequipa_explorer", 4.3, 67));
            
        return empresas;
    }
    
    /**
     * Genera una lista de tours recién agregados (máximo 10)
     */
    public static List<cliente_test_tour> generarToursRecientes() {
        List<cliente_test_tour> tours = new ArrayList<>();
        List<cliente_test_empresa> empresas = generarEmpresasPrueba();
        
        tours.add(new cliente_test_tour("tour_001", "City Tour Lima Centro Histórico",
            "Explora el corazón colonial de Lima visitando la Plaza de Armas, Catedral y Palacio de Gobierno.",
            "4 horas", "2024-10-15", "2024-10-15", 65.00, "S/.", "Lima Centro",
            "-12.0464,-77.0428", "imagen_lima_centro", 4.6, 45, "Cultural",
            "Fácil", 25, 18, empresas.get(0)));
            
        tours.add(new cliente_test_tour("tour_002", "Barranco y Miraflores Tour",
            "Recorre los distritos bohemios y modernos de Lima con vistas al océano.",
            "3 horas", "2024-10-16", "2024-10-16", 55.00, "S/.", "Barranco - Miraflores",
            "-12.1203,-77.0282", "imagen_barranco_miraflores", 4.4, 38, "Cultural",
            "Fácil", 30, 22, empresas.get(0)));
            
        tours.add(new cliente_test_tour("tour_003", "Machu Picchu Full Day",
            "Visita la maravilla del mundo en un tour completo desde Cusco.",
            "16 horas", "2024-10-20", "2024-10-20", 250.00, "S/.", "Machu Picchu",
            "-13.1631,-72.5450", "imagen_machu_picchu", 4.9, 156, "Arqueológico",
            "Moderado", 15, 8, empresas.get(1)));
            
        tours.add(new cliente_test_tour("tour_004", "Valle Sagrado Adventure",
            "Aventura completa por Pisaq, Ollantaytambo y pueblos tradicionales.",
            "12 horas", "2024-10-18", "2024-10-18", 180.00, "S/.", "Valle Sagrado",
            "-13.2594,-71.9708", "imagen_valle_sagrado", 4.7, 73, "Aventura",
            "Moderado", 20, 14, empresas.get(1)));
            
        tours.add(new cliente_test_tour("tour_005", "Cañón del Colca 2D/1N",
            "Observa el vuelo de los cóndores en uno de los cañones más profundos del mundo.",
            "2 días", "2024-10-25", "2024-10-26", 320.00, "S/.", "Cañón del Colca",
            "-15.5989,-71.8890", "imagen_canon_colca", 4.5, 29, "Naturaleza",
            "Moderado", 12, 7, empresas.get(2)));
            
        // Marcar algunos como recién agregados
        tours.get(0).setEsRecienteAgregado(true);
        tours.get(0).setFechaCreacion("2024-09-28");
        tours.get(2).setEsRecienteAgregado(true);
        tours.get(2).setFechaCreacion("2024-09-29");
        tours.get(4).setEsRecienteAgregado(true);
        tours.get(4).setFechaCreacion("2024-09-30");
        
        return tours;
    }
    
    /**
     * Genera una lista de tours cercanos a la ubicación (simulado)
     */
    public static List<cliente_test_tour> generarToursCercanos() {
        List<cliente_test_tour> tours = new ArrayList<>();
        List<cliente_test_empresa> empresas = generarEmpresasPrueba();
        
        tours.add(new cliente_test_tour("tour_006", "Circuito Mágico del Agua",
            "Espectáculo nocturno de fuentes danzantes con luces y música.",
            "2 horas", "2024-10-14", "2024-10-14", 35.00, "S/.", "Parque de la Reserva",
            "-12.0708,-77.0363", "imagen_circuito_agua", 4.3, 92, "Entretenimiento",
            "Fácil", 40, 32, empresas.get(0)));
            
        tours.add(new cliente_test_tour("tour_007", "Museo Larco y Pueblos",
            "Visita al famoso museo y recorrido por pueblos tradicionales limeños.",
            "5 horas", "2024-10-15", "2024-10-15", 85.00, "S/.", "Pueblo Libre",
            "-12.0775,-77.0706", "imagen_museo_larco", 4.6, 67, "Cultural",
            "Fácil", 20, 11, empresas.get(0)));
            
        tours.add(new cliente_test_tour("tour_008", "Callao Monumental Tour",
            "Descubre el arte urbano y la historia del primer puerto del Perú.",
            "3 horas", "2024-10-16", "2024-10-16", 45.00, "S/.", "Callao",
            "-12.0564,-77.1181", "imagen_callao", 4.2, 34, "Cultural",
            "Fácil", 25, 19, empresas.get(0)));
            
        tours.add(new cliente_test_tour("tour_009", "Islas Palomino - Leones Marinos",
            "Excursión marítima para nadar con leones marinos en su hábitat natural.",
            "6 horas", "2024-10-17", "2024-10-17", 120.00, "S/.", "Islas Palomino",
            "-11.9833,-77.2167", "imagen_islas_palomino", 4.7, 88, "Naturaleza",
            "Fácil", 16, 9, empresas.get(0)));
            
        return tours;
    }
    
    /**
     * Obtiene todos los tours disponibles
     */
    public static List<cliente_test_tour> obtenerTodosLosTours() {
        List<cliente_test_tour> todosTours = new ArrayList<>();
        todosTours.addAll(generarToursRecientes());
        todosTours.addAll(generarToursCercanos());
        return todosTours;
    }
}
package com.example.connectifyproject.utils;

import android.util.Log;
import com.google.firebase.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utilidad para validaciones de tiempo en tours
 * - Validar 24h al publicar
 * - Validar 18h al asignar guía
 * - Validar 12h para aceptación de guía
 */
public class TourTimeValidator {
    private static final String TAG = "TourTimeValidator";
    
    /**
     * Calcula las horas restantes hasta el inicio del tour
     * @param fechaRealizacion Fecha del tour (dd/MM/yyyy o Timestamp)
     * @param horaInicio Hora de inicio (HH:mm)
     * @return Horas restantes (puede ser negativo si ya pasó)
     */
    public static double calcularHorasHastaInicio(Object fechaRealizacion, String horaInicio) {
        try {
            Date fechaHoraInicio = combinarFechaHora(fechaRealizacion, horaInicio);
            if (fechaHoraInicio == null) {
                return -1;
            }
            
            Date ahora = new Date();
            long diffMs = fechaHoraInicio.getTime() - ahora.getTime();
            return diffMs / (1000.0 * 60 * 60); // Convertir a horas con decimales
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculando horas hasta inicio", e);
            return -1;
        }
    }
    
    /**
     * Combina fecha y hora en un objeto Date
     */
    private static Date combinarFechaHora(Object fechaRealizacion, String horaInicio) throws ParseException {
        if (horaInicio == null || horaInicio.isEmpty()) {
            return null;
        }
        
        Date fechaTour;
        
        // Manejar Timestamp o String
        if (fechaRealizacion instanceof Timestamp) {
            fechaTour = ((Timestamp) fechaRealizacion).toDate();
        } else if (fechaRealizacion instanceof String) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            fechaTour = sdf.parse((String) fechaRealizacion);
        } else {
            return null;
        }
        
        // Combinar fecha con hora
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        
        String fechaStr = dateFormat.format(fechaTour);
        return fullFormat.parse(fechaStr + " " + horaInicio);
    }
    
    /**
     * Valida si el tour puede ser publicado (≥ 24 horas)
     */
    public static boolean puedePublicarTour(Object fechaRealizacion, String horaInicio) {
        double horas = calcularHorasHastaInicio(fechaRealizacion, horaInicio);
        return horas >= 24.0;
    }
    
    /**
     * Valida si se puede asignar un guía (≥ 18 horas)
     */
    public static boolean puedeAsignarGuia(Object fechaRealizacion, String horaInicio) {
        double horas = calcularHorasHastaInicio(fechaRealizacion, horaInicio);
        return horas >= 18.0;
    }
    
    /**
     * Valida si un guía puede aceptar la oferta (≥ 12 horas)
     */
    public static boolean puedeAceptarOferta(Object fechaRealizacion, String horaInicio) {
        double horas = calcularHorasHastaInicio(fechaRealizacion, horaInicio);
        return horas >= 12.0;
    }
    
    /**
     * Determina el estado de bloqueo de un tour sin asignar
     * @return "visible" (>18h), "bloqueado" (0-18h), "oculto" (<0h)
     */
    public static String getEstadoTourSinAsignar(Object fechaRealizacion, String horaInicio) {
        double horas = calcularHorasHastaInicio(fechaRealizacion, horaInicio);
        
        if (horas < 0) {
            return "oculto"; // Ya pasó la hora de inicio
        } else if (horas < 18) {
            return "bloqueado"; // Menos de 18h, no se puede asignar guía
        } else {
            return "visible"; // Más de 18h, se puede asignar guía
        }
    }
    
    /**
     * Determina el estado de bloqueo de un tour pendiente (con guía seleccionado pero no confirmado)
     * @return "visible" (>12h), "bloqueado" (0-12h), "oculto" (<0h)
     */
    public static String getEstadoTourPendiente(Object fechaRealizacion, String horaInicio) {
        double horas = calcularHorasHastaInicio(fechaRealizacion, horaInicio);
        
        if (horas < 0) {
            return "oculto"; // Ya pasó la hora de inicio
        } else if (horas < 12) {
            return "bloqueado"; // Menos de 12h, guía no aceptó a tiempo
        } else {
            return "visible"; // Más de 12h, aún hay tiempo
        }
    }
    
    /**
     * Obtiene mensaje explicativo para tour bloqueado sin asignar
     */
    public static String getMensajeTourSinAsignarBloqueado(Object fechaRealizacion, String horaInicio) {
        double horas = calcularHorasHastaInicio(fechaRealizacion, horaInicio);
        
        if (horas < 0) {
            return "Este tour debía haber iniciado y se ha cancelado automáticamente. Dejará de aparecer en esta lista.";
        } else {
            long horasRestantes = (long) Math.ceil(horas);
            return String.format(Locale.getDefault(),
                "Debía asignar un guía al menos 18 horas antes del tour. " +
                "Quedan solo %d horas. Este tour se cancelará al llegar a su hora de inicio.",
                horasRestantes);
        }
    }
    
    /**
     * Obtiene mensaje explicativo para tour pendiente bloqueado
     */
    public static String getMensajeTourPendienteBloqueado(Object fechaRealizacion, String horaInicio) {
        double horas = calcularHorasHastaInicio(fechaRealizacion, horaInicio);
        
        if (horas < 0) {
            return "Ningún guía aceptó este tour a tiempo. El tour se ha cancelado y dejará de aparecer en esta lista.";
        } else {
            long horasRestantes = (long) Math.ceil(horas);
            return String.format(Locale.getDefault(),
                "Ningún guía ha aceptado por lo menos 12 horas antes del tour. " +
                "Quedan solo %d horas. Este tour se cancelará al llegar a su hora de inicio.",
                horasRestantes);
        }
    }
}

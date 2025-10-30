package com.example.connectifyproject.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class GuiaPreferencesManager {
    private static final String PREF_NAME = "guia_preferences";
    
    // Configuraciones de notificaciones
    private static final String PREF_NOTIF_NEW_OFFERS = "notif_new_offers";
    private static final String PREF_NOTIF_TOUR_REMINDERS = "notif_tour_reminders";
    private static final String PREF_NOTIF_LOCATION_REMINDERS = "notif_location_reminders";
    private static final String PREF_NOTIF_CHECKIN_REMINDERS = "notif_checkin_reminders";
    private static final String PREF_NOTIF_CHECKOUT_REMINDERS = "notif_checkout_reminders";
    
    // Configuraciones de recordatorios
    private static final String PREF_REMINDER_DAYS_AHEAD = "reminder_days_ahead"; // 1, 2 o 3 días
    
    // Información del guía (persistente hasta migrar a base de datos)
    private static final String PREF_GUIDE_LANGUAGES = "guide_languages";
    private static final String PREF_GUIDE_EXPERIENCE_LEVEL = "guide_experience_level";
    private static final String PREF_GUIDE_PAYMENT_METHODS = "guide_payment_methods";
    private static final String PREF_MIN_PAYMENT_ACCEPTED = "min_payment_accepted";

    private SharedPreferences sharedPreferences;

    public GuiaPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // === CONFIGURACIONES DE NOTIFICACIONES ===
    
    public void setNotificationEnabled(String type, boolean enabled) {
        String key = getNotificationKey(type);
        if (key != null) {
            sharedPreferences.edit().putBoolean(key, enabled).apply();
            Log.d("Preferences", "Notificación " + type + " configurada: " + enabled);
        }
    }

    public boolean isNotificationEnabled(String type) {
        String key = getNotificationKey(type);
        if (key != null) {
            return sharedPreferences.getBoolean(key, true); // Por defecto activadas
        }
        return true;
    }

    private String getNotificationKey(String type) {
        switch (type) {
            case "new_offers": return PREF_NOTIF_NEW_OFFERS;
            case "tour_reminders": return PREF_NOTIF_TOUR_REMINDERS;
            case "location_reminders": return PREF_NOTIF_LOCATION_REMINDERS;
            case "checkin_reminders": return PREF_NOTIF_CHECKIN_REMINDERS;
            case "checkout_reminders": return PREF_NOTIF_CHECKOUT_REMINDERS;
            default: return null;
        }
    }

    public void saveAllNotificationSettings(boolean newOffers, boolean tourReminders, 
                                          boolean locationReminders, boolean checkinReminders, 
                                          boolean checkoutReminders) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_NOTIF_NEW_OFFERS, newOffers);
        editor.putBoolean(PREF_NOTIF_TOUR_REMINDERS, tourReminders);
        editor.putBoolean(PREF_NOTIF_LOCATION_REMINDERS, locationReminders);
        editor.putBoolean(PREF_NOTIF_CHECKIN_REMINDERS, checkinReminders);
        editor.putBoolean(PREF_NOTIF_CHECKOUT_REMINDERS, checkoutReminders);
        editor.apply();
        Log.d("Preferences", "Todas las configuraciones de notificaciones guardadas");
    }

    // === CONFIGURACIONES DE RECORDATORIOS ===
    
    public void setReminderDaysAhead(int days) {
        if (days >= 1 && days <= 3) {
            sharedPreferences.edit().putInt(PREF_REMINDER_DAYS_AHEAD, days).apply();
        }
    }

    public int getReminderDaysAhead() {
        return sharedPreferences.getInt(PREF_REMINDER_DAYS_AHEAD, 2); // Por defecto 2 días
    }

    // === INFORMACIÓN DEL GUÍA ===
    
    public void saveGuideLanguages(Set<String> languages) {
        sharedPreferences.edit().putStringSet(PREF_GUIDE_LANGUAGES, languages).apply();
        Log.d("Preferences", "Idiomas del guía guardados: " + languages.toString());
    }

    public Set<String> getGuideLanguages() {
        Set<String> defaultLanguages = new HashSet<>();
        defaultLanguages.add("Español");
        return sharedPreferences.getStringSet(PREF_GUIDE_LANGUAGES, defaultLanguages);
    }

    public void setExperienceLevel(String level) {
        sharedPreferences.edit().putString(PREF_GUIDE_EXPERIENCE_LEVEL, level).apply();
    }

    public String getExperienceLevel() {
        return sharedPreferences.getString(PREF_GUIDE_EXPERIENCE_LEVEL, "Principiante");
    }

    // Métodos de pago del guía (como en el perfil)
    public void saveGuidePaymentMethods(Set<String> paymentMethods) {
        sharedPreferences.edit().putStringSet(PREF_GUIDE_PAYMENT_METHODS, paymentMethods).apply();
        Log.d("Preferences", "Métodos de pago del guía guardados: " + paymentMethods.toString());
    }

    public Set<String> getGuidePaymentMethods() {
        Set<String> defaultMethods = new HashSet<>();
        defaultMethods.add("Transferencia bancaria");
        return sharedPreferences.getStringSet(PREF_GUIDE_PAYMENT_METHODS, defaultMethods);
    }

    public void setMinPaymentAccepted(double amount) {
        // Convertir a float para SharedPreferences
        sharedPreferences.edit().putFloat(PREF_MIN_PAYMENT_ACCEPTED, (float) amount).apply();
    }

    public double getMinPaymentAccepted() {
        return sharedPreferences.getFloat(PREF_MIN_PAYMENT_ACCEPTED, 0.0f);
    }



    // === MÉTODOS UTILITARIOS ===
    
    public void clearAllPreferences() {
        sharedPreferences.edit().clear().apply();
        Log.d("Preferences", "Todas las preferencias borradas");
    }

    public void exportPreferencesToLog() {
        Log.d("Preferences", "=== CONFIGURACIONES ACTUALES ===");
        Log.d("Preferences", "Nuevas ofertas: " + isNotificationEnabled("new_offers"));
        Log.d("Preferences", "Recordatorios de tours: " + isNotificationEnabled("tour_reminders"));
        Log.d("Preferences", "Recordatorios de ubicación: " + isNotificationEnabled("location_reminders"));
        Log.d("Preferences", "Recordatorios check-in: " + isNotificationEnabled("checkin_reminders"));
        Log.d("Preferences", "Recordatorios check-out: " + isNotificationEnabled("checkout_reminders"));
        Log.d("Preferences", "Días de anticipación: " + getReminderDaysAhead());
        Log.d("Preferences", "Idiomas: " + getGuideLanguages().toString());
        Log.d("Preferences", "Métodos de pago: " + getGuidePaymentMethods().toString());
        Log.d("Preferences", "Experiencia: " + getExperienceLevel());
        Log.d("Preferences", "Pago mínimo: S/ " + getMinPaymentAccepted());
        Log.d("Preferences", "================================");
    }
}
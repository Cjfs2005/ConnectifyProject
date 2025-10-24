package com.example.connectifyproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestor de SharedPreferences específico para cliente
 * Maneja configuraciones de login, filtros y preferencias
 */
public class Cliente_PreferencesManager {

    private static final String PREFS_NAME = "cliente_preferences";
    
    // Keys para login
    private static final String KEY_REMEMBER_LOGIN = "remember_login";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";
    private static final String KEY_FIRST_LOGIN = "first_login";
    
    // Keys para filtros
    private static final String KEY_FILTER_LOCATION = "filter_location";
    private static final String KEY_FILTER_MIN_PRICE = "filter_min_price";
    private static final String KEY_FILTER_MAX_PRICE = "filter_max_price";
    private static final String KEY_FILTER_DURATION = "filter_duration";
    private static final String KEY_FILTER_CATEGORY = "filter_category";

    private SharedPreferences prefs;

    public Cliente_PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ==================== MÉTODOS DE LOGIN ====================

    /**
     * Guardar credenciales de login cuando se marca "Recordarme"
     */
    public void saveLoginCredentials(String email, String password, boolean remember) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_REMEMBER_LOGIN, remember);
        
        if (remember) {
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putString(KEY_SAVED_PASSWORD, password);
        } else {
            // Si no quiere recordar, limpiar credenciales guardadas
            editor.remove(KEY_SAVED_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
        }
        
        editor.apply();
    }

    /**
     * Obtener si el usuario quiere que se recuerden sus credenciales
     */
    public boolean shouldRememberLogin() {
        return prefs.getBoolean(KEY_REMEMBER_LOGIN, false);
    }

    /**
     * Obtener email guardado
     */
    public String getSavedEmail() {
        return prefs.getString(KEY_SAVED_EMAIL, "");
    }

    /**
     * Obtener contraseña guardada
     */
    public String getSavedPassword() {
        return prefs.getString(KEY_SAVED_PASSWORD, "");
    }

    /**
     * Limpiar todas las credenciales guardadas
     */
    public void clearLoginCredentials() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_REMEMBER_LOGIN);
        editor.remove(KEY_SAVED_EMAIL);
        editor.remove(KEY_SAVED_PASSWORD);
        editor.apply();
    }

    /**
     * Verificar si es el primer login del usuario
     */
    public boolean isFirstLogin() {
        return prefs.getBoolean(KEY_FIRST_LOGIN, true);
    }

    /**
     * Marcar que ya no es el primer login
     */
    public void setFirstLoginCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LOGIN, false).apply();
    }

    // ==================== MÉTODOS DE FILTROS ====================

    /**
     * Guardar filtros de búsqueda de tours
     */
    public void saveFilterPreferences(String location, int minPrice, int maxPrice, 
                                    String duration, String category) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_FILTER_LOCATION, location);
        editor.putInt(KEY_FILTER_MIN_PRICE, minPrice);
        editor.putInt(KEY_FILTER_MAX_PRICE, maxPrice);
        editor.putString(KEY_FILTER_DURATION, duration);
        editor.putString(KEY_FILTER_CATEGORY, category);
        editor.apply();
    }

    /**
     * Obtener ubicación del filtro guardado
     */
    public String getFilterLocation() {
        return prefs.getString(KEY_FILTER_LOCATION, "");
    }

    /**
     * Obtener precio mínimo del filtro
     */
    public int getFilterMinPrice() {
        return prefs.getInt(KEY_FILTER_MIN_PRICE, 0);
    }

    /**
     * Obtener precio máximo del filtro
     */
    public int getFilterMaxPrice() {
        return prefs.getInt(KEY_FILTER_MAX_PRICE, 1000);
    }

    /**
     * Obtener duración del filtro
     */
    public String getFilterDuration() {
        return prefs.getString(KEY_FILTER_DURATION, "");
    }

    /**
     * Obtener categoría del filtro
     */
    public String getFilterCategory() {
        return prefs.getString(KEY_FILTER_CATEGORY, "");
    }

    /**
     * Limpiar todos los filtros guardados
     */
    public void clearFilters() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_FILTER_LOCATION);
        editor.remove(KEY_FILTER_MIN_PRICE);
        editor.remove(KEY_FILTER_MAX_PRICE);
        editor.remove(KEY_FILTER_DURATION);
        editor.remove(KEY_FILTER_CATEGORY);
        editor.apply();
    }

    /**
     * Verificar si hay filtros guardados
     */
    public boolean hasFilters() {
        return !getFilterLocation().isEmpty() || 
               getFilterMinPrice() > 0 || 
               getFilterMaxPrice() < 1000 ||
               !getFilterDuration().isEmpty() ||
               !getFilterCategory().isEmpty();
    }

    // ==========================================
    // GESTIÓN DE PERMISOS
    // ==========================================

    private static final String KEY_PERMISSION_LOCATION = "permission_location";
    private static final String KEY_PERMISSION_NOTIFICATIONS = "permission_notifications";

    /**
     * Guardar estado de permiso
     */
    public void savePermissionState(String permissionType, boolean granted) {
        SharedPreferences.Editor editor = prefs.edit();
        if ("location".equals(permissionType)) {
            editor.putBoolean(KEY_PERMISSION_LOCATION, granted);
        } else if ("notifications".equals(permissionType)) {
            editor.putBoolean(KEY_PERMISSION_NOTIFICATIONS, granted);
        }
        editor.apply();
    }

    /**
     * Obtener estado de permiso de ubicación
     */
    public boolean getLocationPermissionState() {
        return prefs.getBoolean(KEY_PERMISSION_LOCATION, false);
    }

    /**
     * Obtener estado de permiso de notificaciones
     */
    public boolean getNotificationPermissionState() {
        return prefs.getBoolean(KEY_PERMISSION_NOTIFICATIONS, false);
    }

    /**
     * Verificar si todos los permisos están concedidos
     */
    public boolean areAllPermissionsGranted() {
        return getLocationPermissionState() && getNotificationPermissionState();
    }
}
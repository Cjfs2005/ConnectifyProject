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
}
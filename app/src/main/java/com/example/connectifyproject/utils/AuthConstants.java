package com.example.connectifyproject.utils;

/**
 * Constantes de autenticación
 * Contiene emails hardcodeados de SuperAdmin
 */
public class AuthConstants {
    
    // Email del SuperAdmin hardcodeado
    public static final String SUPER_ADMIN_EMAIL = "superadmin_tourly@gmail.com";
    
    /**
     * Verificar si un email pertenece al SuperAdmin
     */
    public static boolean isSuperAdmin(String email) {
        if (email == null) return false;
        return SUPER_ADMIN_EMAIL.equalsIgnoreCase(email.trim());
    }
    
    // Roles de usuarios en Firestore
    public static final String ROLE_CLIENTE = "Cliente";
    public static final String ROLE_GUIA = "Guia";
    
    // Colecciones de Firestore
    public static final String COLLECTION_USUARIOS = "usuarios";
    
    // Campos de documentos
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_ROL = "rol";
    public static final String FIELD_NOMBRE = "nombre";
}

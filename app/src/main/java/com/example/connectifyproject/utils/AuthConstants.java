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
    public static final String ROLE_ADMIN = "Administrador";
    
    // Colecciones de Firestore
    public static final String COLLECTION_USUARIOS = "usuarios";
    
    // Campos de documentos de usuario
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_ROL = "rol";
    public static final String FIELD_NOMBRE_COMPLETO = "nombresApellidos";
    public static final String FIELD_TIPO_DOCUMENTO = "tipoDocumento";
    public static final String FIELD_NUMERO_DOCUMENTO = "numeroDocumento";
    public static final String FIELD_FECHA_NACIMIENTO = "fechaNacimiento";
    public static final String FIELD_TELEFONO = "telefono";
    public static final String FIELD_CODIGO_PAIS = "codigoPais";
    public static final String FIELD_DOMICILIO = "domicilio";
    public static final String FIELD_PHOTO_URL = "photoUrl";
    public static final String FIELD_HABILITADO = "habilitado";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_FECHA_CREACION = "fechaCreacion"; // Timestamp de creación del usuario
    public static final String FIELD_PERFIL_COMPLETO = "perfilCompleto"; // Indica si completó el registro
    
    // Campos específicos de Guía
    public static final String FIELD_IDIOMAS = "idiomas";
    
    // Campos específicos de Administrador
    public static final String FIELD_NOMBRE_EMPRESA = "nombreEmpresa";
    public static final String FIELD_DESCRIPCION_EMPRESA = "descripcionEmpresa";
    public static final String FIELD_UBICACION_EMPRESA = "ubicacionEmpresa";
    public static final String FIELD_CORREO_EMPRESA = "correoEmpresa";
    public static final String FIELD_TELEFONO_EMPRESA = "telefonoEmpresa";
    public static final String FIELD_FOTOS_EMPRESA = "fotosEmpresa"; // Array de URLs
    public static final String FIELD_SUMA_RESENIAS = "sumaResenias"; // Suma total de puntuaciones
    public static final String FIELD_NUMERO_RESENIAS = "numeroResenias"; // Cantidad de reseñas
    
    // Tipos de documento
    public static final String[] TIPOS_DOCUMENTO = {
        "DNI",
        "Pasaporte",
        "Carnet de Extranjería"
    };
    
    // Idiomas disponibles para guías
    public static final String[] IDIOMAS_DISPONIBLES = {
        "Español",
        "Inglés",
        "Francés",
        "Alemán",
        "Italiano",
        "Chino",
        "Japonés"
    };
    
    // URL de imagen por defecto en Firebase Storage
    public static final String DEFAULT_PHOTO_URL = "gs://iot-proyecto-f817d.firebasestorage.app/default.png";
}


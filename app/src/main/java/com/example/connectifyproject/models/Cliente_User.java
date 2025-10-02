package com.example.connectifyproject.models;

import java.io.Serializable;

/**
 * Modelo para representar la información del usuario cliente
 * Utilizado en el perfil y funciones relacionadas con datos del usuario
 */
public class Cliente_User implements Serializable {
    
    private String id;
    private String nombre;
    private String apellidos;
    private String tipoDocumento; // "DNI", "Pasaporte", "Carnet de extranjería"
    private String numeroDocumento;
    private String correo;
    private String telefono;
    private String fechaNacimiento;
    private String domicilio;
    private String fotoPerfilUrl;
    private boolean notificacionesActivas;
    private boolean gpsActivo;
    private boolean camaraActiva;

    // Constructor completo
    public Cliente_User(String id, String nombre, String apellidos, String tipoDocumento, 
                       String numeroDocumento, String correo, String telefono, 
                       String fechaNacimiento, String domicilio, String fotoPerfilUrl,
                       boolean notificacionesActivas, boolean gpsActivo, boolean camaraActiva) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.correo = correo;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.domicilio = domicilio;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.notificacionesActivas = notificacionesActivas;
        this.gpsActivo = gpsActivo;
        this.camaraActiva = camaraActiva;
    }

    // Constructor básico para datos esenciales
    public Cliente_User(String id, String nombre, String apellidos, String tipoDocumento, 
                       String numeroDocumento, String correo, String telefono, 
                       String fechaNacimiento, String domicilio) {
        this(id, nombre, apellidos, tipoDocumento, numeroDocumento, correo, telefono, 
             fechaNacimiento, domicilio, null, true, false, false);
    }

    // Constructor vacío
    public Cliente_User() {
        this("", "", "", "DNI", "", "", "", "", "", null, true, false, false);
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getDomicilio() { return domicilio; }
    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public boolean isNotificacionesActivas() { return notificacionesActivas; }
    public boolean isGpsActivo() { return gpsActivo; }
    public boolean isCamaraActiva() { return camaraActiva; }

    // Método para obtener nombre completo
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    // Método para obtener estado de permisos como texto
    public String getEstadoPermisos() {
        StringBuilder estado = new StringBuilder();
        if (gpsActivo) estado.append("GPS Activo");
        if (camaraActiva) {
            if (estado.length() > 0) estado.append(", ");
            estado.append("Cámara Activa");
        }
        if (estado.length() == 0) estado.append("Sin permisos activos");
        return estado.toString();
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }
    public void setNotificacionesActivas(boolean notificacionesActivas) { this.notificacionesActivas = notificacionesActivas; }
    public void setGpsActivo(boolean gpsActivo) { this.gpsActivo = gpsActivo; }
    public void setCamaraActiva(boolean camaraActiva) { this.camaraActiva = camaraActiva; }

    // Método estático para crear usuario de ejemplo (hardcodeado)
    public static Cliente_User crearUsuarioEjemplo() {
        return new Cliente_User(
            "usr_001",
            "Jorge",
            "Romero Paredes",
            "DNI",
            "70910370",
            "jromero@gmail.com",
            "970 123 456",
            "16/04/2001",
            "Calle Arequipa 234, Lima, Perú",
            null, // Sin foto de perfil
            true, // Notificaciones activas
            true, // GPS activo
            true  // Cámara activa
        );
    }
}
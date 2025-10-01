package com.example.connectifyproject.models;

/**
 * Modelo para representar una empresa de turismo
 */
public class cliente_test_empresa {
    private String id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private String descripcion;
    private String logoUrl;
    private String imagenUrl;
    private double calificacion;
    private int numeroReviews;
    
    public cliente_test_empresa(String id, String nombre, String direccion, String telefono, 
                               String email, String descripcion, String logoUrl, String imagenUrl,
                               double calificacion, int numeroReviews) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.descripcion = descripcion;
        this.logoUrl = logoUrl;
        this.imagenUrl = imagenUrl;
        this.calificacion = calificacion;
        this.numeroReviews = numeroReviews;
    }
    
    // Constructor vacío
    public cliente_test_empresa() {}
    
    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getDescripcion() { return descripcion; }
    public String getLogoUrl() { return logoUrl; }
    public String getImagenUrl() { return imagenUrl; }
    public double getCalificacion() { return calificacion; }
    public int getNumeroReviews() { return numeroReviews; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public void setCalificacion(double calificacion) { this.calificacion = calificacion; }
    public void setNumeroReviews(int numeroReviews) { this.numeroReviews = numeroReviews; }
}
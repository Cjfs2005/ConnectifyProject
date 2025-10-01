package com.example.connectifyproject.models;

/**
 * Modelo para representar un tour con toda la información necesaria
 * Incluye datos de la empresa que lo ofrece
 */
public class cliente_test_tour {
    private String id;
    private String titulo;
    private String descripcion;
    private String duracion;
    private String fechaInicio;
    private String fechaFin;
    private double precio;
    private String moneda;
    private String ubicacion;
    private String coordenadasLatLng;
    private String imagenPrincipalUrl;
    private String[] imagenesGaleriaUrls;
    private double calificacion;
    private int numeroReviews;
    private String categoria;
    private String dificultad;
    private int capacidadMaxima;
    private int plazasDisponibles;
    private String[] serviciosIncluidos;
    private String[] serviciosNoIncluidos;
    private boolean esRecienteAgregado;
    private String fechaCreacion;
    private String estado; // "disponible", "agotado", "cancelado"
    
    // Datos de la empresa (referencia completa)
    private cliente_test_empresa empresa;
    
    public cliente_test_tour(String id, String titulo, String descripcion, String duracion,
                            String fechaInicio, String fechaFin, double precio, String moneda,
                            String ubicacion, String coordenadasLatLng, String imagenPrincipalUrl,
                            double calificacion, int numeroReviews, String categoria,
                            String dificultad, int capacidadMaxima, int plazasDisponibles,
                            cliente_test_empresa empresa) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.duracion = duracion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precio = precio;
        this.moneda = moneda;
        this.ubicacion = ubicacion;
        this.coordenadasLatLng = coordenadasLatLng;
        this.imagenPrincipalUrl = imagenPrincipalUrl;
        this.calificacion = calificacion;
        this.numeroReviews = numeroReviews;
        this.categoria = categoria;
        this.dificultad = dificultad;
        this.capacidadMaxima = capacidadMaxima;
        this.plazasDisponibles = plazasDisponibles;
        this.empresa = empresa;
        this.estado = "disponible";
        this.esRecienteAgregado = false;
        this.fechaCreacion = "";
    }
    
    // Constructor vacío
    public cliente_test_tour() {}
    
    // Getters
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getDuracion() { return duracion; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public double getPrecio() { return precio; }
    public String getMoneda() { return moneda; }
    public String getUbicacion() { return ubicacion; }
    public String getCoordenadasLatLng() { return coordenadasLatLng; }
    public String getImagenPrincipalUrl() { return imagenPrincipalUrl; }
    public String[] getImagenesGaleriaUrls() { return imagenesGaleriaUrls; }
    public double getCalificacion() { return calificacion; }
    public int getNumeroReviews() { return numeroReviews; }
    public String getCategoria() { return categoria; }
    public String getDificultad() { return dificultad; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public int getPlazasDisponibles() { return plazasDisponibles; }
    public String[] getServiciosIncluidos() { return serviciosIncluidos; }
    public String[] getServiciosNoIncluidos() { return serviciosNoIncluidos; }
    public boolean isEsRecienteAgregado() { return esRecienteAgregado; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getEstado() { return estado; }
    public cliente_test_empresa getEmpresa() { return empresa; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setCoordenadasLatLng(String coordenadasLatLng) { this.coordenadasLatLng = coordenadasLatLng; }
    public void setImagenPrincipalUrl(String imagenPrincipalUrl) { this.imagenPrincipalUrl = imagenPrincipalUrl; }
    public void setImagenesGaleriaUrls(String[] imagenesGaleriaUrls) { this.imagenesGaleriaUrls = imagenesGaleriaUrls; }
    public void setCalificacion(double calificacion) { this.calificacion = calificacion; }
    public void setNumeroReviews(int numeroReviews) { this.numeroReviews = numeroReviews; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public void setPlazasDisponibles(int plazasDisponibles) { this.plazasDisponibles = plazasDisponibles; }
    public void setServiciosIncluidos(String[] serviciosIncluidos) { this.serviciosIncluidos = serviciosIncluidos; }
    public void setServiciosNoIncluidos(String[] serviciosNoIncluidos) { this.serviciosNoIncluidos = serviciosNoIncluidos; }
    public void setEsRecienteAgregado(boolean esRecienteAgregado) { this.esRecienteAgregado = esRecienteAgregado; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setEmpresa(cliente_test_empresa empresa) { this.empresa = empresa; }
    
    // Métodos de utilidad
    public String getPrecioFormateado() {
        return moneda + " " + String.format("%.2f", precio);
    }
    
    public String getCalificacionFormateada() {
        return String.format("%.1f", calificacion);
    }
    
    public boolean estaDisponible() {
        return "disponible".equals(estado) && plazasDisponibles > 0;
    }
    
    public String getNombreEmpresa() {
        return empresa != null ? empresa.getNombre() : "";
    }
}
package com.example.connectifyproject.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.firebase.Timestamp;

/**
 * Modelo para tours en estado de borrador
 * Almacenados en colección tours_borradores
 */
public class TourBorrador implements Serializable {
    private String id;
    private String titulo;
    private String descripcion;
    private double precio;
    private String duracion;
    private String fechaRealizacion; // Formato: dd/MM/yyyy
    private String horaInicio;
    private String horaFin;
    
    // Itinerario y servicios
    private List<Map<String, Object>> itinerario;
    private List<Map<String, Object>> serviciosAdicionales;
    
    // 🆕 Imágenes (mínimo 1, máximo 3)
    private List<String> imagenesUrls;
    private String imagenPrincipal;
    
    // Información de la empresa
    private String empresaId;
    private String nombreEmpresa;
    private String correoEmpresa;
    
    // Requisitos para guías
    private double pagoGuia;
    private List<String> idiomasRequeridos; // OBLIGATORIO - Mínimo 1 idioma
    private String consideraciones;
    
    // Metadatos
    private Timestamp fechaCreacion;
    private Timestamp fechaActualizacion;
    private String creadoPor; // UID del admin/empresa que creó el borrador

    // Constructor vacío requerido por Firebase
    public TourBorrador() {
        this.itinerario = new ArrayList<>();
        this.serviciosAdicionales = new ArrayList<>();
        this.imagenesUrls = new ArrayList<>();
        this.idiomasRequeridos = new ArrayList<>();
    }

    // Constructor completo
    public TourBorrador(String titulo, String descripcion, double precio, String duracion,
                       String fechaRealizacion, String horaInicio, String horaFin,
                       String empresaId, String nombreEmpresa, double pagoGuia) {
        this();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.duracion = duracion;
        this.fechaRealizacion = fechaRealizacion;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.empresaId = empresaId;
        this.nombreEmpresa = nombreEmpresa;
        this.pagoGuia = pagoGuia;
        this.fechaCreacion = Timestamp.now();
        this.fechaActualizacion = Timestamp.now();
    }

    // Validaciones
    public boolean tieneImagenObligatoria() {
        return imagenesUrls != null && !imagenesUrls.isEmpty();
    }

    public boolean tieneIdiomasRequeridos() {
        return idiomasRequeridos != null && !idiomasRequeridos.isEmpty();
    }

    public boolean esValido() {
        return titulo != null && !titulo.isEmpty() &&
               descripcion != null && !descripcion.isEmpty() &&
               precio > 0 &&
               duracion != null && !duracion.isEmpty() &&
               fechaRealizacion != null && !fechaRealizacion.isEmpty() &&
               pagoGuia > 0 &&
               tieneImagenObligatoria() &&
               tieneIdiomasRequeridos();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(String fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public List<Map<String, Object>> getItinerario() { return itinerario; }
    public void setItinerario(List<Map<String, Object>> itinerario) { this.itinerario = itinerario; }

    public List<Map<String, Object>> getServiciosAdicionales() { return serviciosAdicionales; }
    public void setServiciosAdicionales(List<Map<String, Object>> serviciosAdicionales) { 
        this.serviciosAdicionales = serviciosAdicionales; 
    }

    public List<String> getImagenesUrls() { return imagenesUrls; }
    public void setImagenesUrls(List<String> imagenesUrls) { 
        this.imagenesUrls = imagenesUrls;
        if (imagenesUrls != null && !imagenesUrls.isEmpty() && imagenPrincipal == null) {
            this.imagenPrincipal = imagenesUrls.get(0); // Primera imagen por defecto
        }
    }

    public String getImagenPrincipal() { return imagenPrincipal; }
    public void setImagenPrincipal(String imagenPrincipal) { this.imagenPrincipal = imagenPrincipal; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getCorreoEmpresa() { return correoEmpresa; }
    public void setCorreoEmpresa(String correoEmpresa) { this.correoEmpresa = correoEmpresa; }

    public double getPagoGuia() { return pagoGuia; }
    public void setPagoGuia(double pagoGuia) { this.pagoGuia = pagoGuia; }

    public List<String> getIdiomasRequeridos() { return idiomasRequeridos; }
    public void setIdiomasRequeridos(List<String> idiomasRequeridos) { this.idiomasRequeridos = idiomasRequeridos; }

    public String getConsideraciones() { return consideraciones; }
    public void setConsideraciones(String consideraciones) { this.consideraciones = consideraciones; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Timestamp getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Timestamp fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }

    // Helper methods
    public String getResumenItinerario() {
        if (itinerario == null || itinerario.isEmpty()) {
            return "Sin itinerario definido";
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> punto : itinerario) {
            String lugar = (String) punto.get("lugar");
            if (lugar != null) {
                if (sb.length() > 0) sb.append(" → ");
                sb.append(lugar);
            }
        }
        return sb.toString();
    }

    public String getIdiomasTexto() {
        if (idiomasRequeridos == null || idiomasRequeridos.isEmpty()) {
            return "No especificado";
        }
        return String.join(", ", idiomasRequeridos);
    }

    public int getCantidadImagenes() {
        return imagenesUrls != null ? imagenesUrls.size() : 0;
    }
}

package com.example.connectifyproject.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.google.firebase.Timestamp;

public class OfertaTour implements Serializable {
    private String id;
    private String titulo;
    private String descripcion;
    private double precio;
    private String duracion;
    private String fechaRealizacion;
    private String horaInicio;
    private String horaFin;
    private String ciudad; // Ciudad donde se realiza el tour
    private List<Map<String, Object>> itinerario;
    private List<Map<String, Object>> serviciosAdicionales;
    private String empresaId;
    private String nombreEmpresa;
    private String correoEmpresa;
    private double pagoGuia;
    private List<String> idiomasRequeridos;
    private String consideraciones;
    private String estado;
    private String guiaAsignadoId;
    private Timestamp fechaAsignacion;
    private Timestamp fechaCreacion;
    private Timestamp fechaActualizacion;
    private boolean habilitado;
    private boolean perfilCompleto;

    // 🆕 Campos de imágenes
    private List<String> imagenesUrls;
    private String imagenPrincipal;
    private Integer cantidadImagenes2; // ✅ AGREGAR ESTE

    // 🆕 Campos de tracking de guías
    private String guiaSeleccionadoActual;
    private Timestamp fechaUltimoOfrecimiento;

    // ✅ AGREGAR ESTOS - Campos calculados que Firebase también guarda
    private String idiomasTexto;
    private String serviciosResumen;
    private String resumenItinerario;

    // Constructor vacío requerido por Firebase
    public OfertaTour() {}

    // Constructor para crear nueva oferta
    public OfertaTour(String titulo, String descripcion, double precio, String duracion,
                      String fechaRealizacion, String horaInicio, String horaFin,
                      String empresaId, String nombreEmpresa, double pagoGuia) {
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
        this.estado = "publicado";
        this.habilitado = true;
        this.perfilCompleto = true;
        this.fechaCreacion = Timestamp.now();
        this.fechaActualizacion = Timestamp.now();
    }

    // Métodos helper para obtener información del itinerario
    public String calcularResumenItinerario() { // ✅ Renombré para no confundir con getter
        if (itinerario == null || itinerario.isEmpty()) {
            return "Sin itinerario definido";
        }

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> punto : itinerario) {
            String lugar = (String) punto.get("lugar");
            String hora = (String) punto.get("horaEstimada");
            if (lugar != null && hora != null) {
                if (sb.length() > 0) sb.append(" → ");
                sb.append(hora).append(" ").append(lugar);
            }
        }
        return sb.toString();
    }

    public String calcularServiciosResumen() { // ✅ Renombré para no confundir con getter
        if (serviciosAdicionales == null || serviciosAdicionales.isEmpty()) {
            return "Sin servicios adicionales";
        }

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> servicio : serviciosAdicionales) {
            String nombre = (String) servicio.get("nombre");
            Boolean esPagado = (Boolean) servicio.get("esPagado");
            if (nombre != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(nombre);
                if (esPagado != null && !esPagado) {
                    sb.append(" (incluido)");
                }
            }
        }
        return sb.toString();
    }

    public String calcularIdiomasTexto() { // ✅ Renombré para no confundir con getter
        if (idiomasRequeridos == null || idiomasRequeridos.isEmpty()) {
            return "No especificado";
        }
        return String.join(", ", idiomasRequeridos);
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

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public List<Map<String, Object>> getItinerario() { return itinerario; }
    public void setItinerario(List<Map<String, Object>> itinerario) { this.itinerario = itinerario; }

    public List<Map<String, Object>> getServiciosAdicionales() { return serviciosAdicionales; }
    public void setServiciosAdicionales(List<Map<String, Object>> serviciosAdicionales) {
        this.serviciosAdicionales = serviciosAdicionales;
    }

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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getGuiaAsignadoId() { return guiaAsignadoId; }
    public void setGuiaAsignadoId(String guiaAsignadoId) { this.guiaAsignadoId = guiaAsignadoId; }

    public Timestamp getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(Timestamp fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Timestamp getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Timestamp fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

    public boolean isPerfilCompleto() { return perfilCompleto; }
    public void setPerfilCompleto(boolean perfilCompleto) { this.perfilCompleto = perfilCompleto; }

    // 🆕 Getters y Setters para campos de imágenes
    public List<String> getImagenesUrls() { return imagenesUrls; }
    public void setImagenesUrls(List<String> imagenesUrls) {
        this.imagenesUrls = imagenesUrls;
        if (imagenesUrls != null && !imagenesUrls.isEmpty() && imagenPrincipal == null) {
            this.imagenPrincipal = imagenesUrls.get(0);
        }
    }

    public String getImagenPrincipal() { return imagenPrincipal; }
    public void setImagenPrincipal(String imagenPrincipal) { this.imagenPrincipal = imagenPrincipal; }

    // ✅ NUEVO - Getter/Setter para cantidadImagenes2
    public Integer getCantidadImagenes2() { return cantidadImagenes2; }
    public void setCantidadImagenes2(Integer cantidadImagenes2) { this.cantidadImagenes2 = cantidadImagenes2; }

    // 🆕 Getters y Setters para tracking de guías
    public String getGuiaSeleccionadoActual() { return guiaSeleccionadoActual; }
    public void setGuiaSeleccionadoActual(String guiaSeleccionadoActual) {
        this.guiaSeleccionadoActual = guiaSeleccionadoActual;
    }

    public Timestamp getFechaUltimoOfrecimiento() { return fechaUltimoOfrecimiento; }
    public void setFechaUltimoOfrecimiento(Timestamp fechaUltimoOfrecimiento) {
        this.fechaUltimoOfrecimiento = fechaUltimoOfrecimiento;
    }

    // ✅ NUEVOS - Getters/Setters para campos calculados
    public String getIdiomasTexto() {
        // Si el campo existe en Firebase, usarlo; si no, calcularlo
        return idiomasTexto != null ? idiomasTexto : calcularIdiomasTexto();
    }
    public void setIdiomasTexto(String idiomasTexto) { this.idiomasTexto = idiomasTexto; }

    public String getServiciosResumen() {
        // Si el campo existe en Firebase, usarlo; si no, calcularlo
        return serviciosResumen != null ? serviciosResumen : calcularServiciosResumen();
    }
    public void setServiciosResumen(String serviciosResumen) { this.serviciosResumen = serviciosResumen; }

    public String getResumenItinerario() {
        // Si el campo existe en Firebase, usarlo; si no, calcularlo
        return resumenItinerario != null ? resumenItinerario : calcularResumenItinerario();
    }
    public void setResumenItinerario(String resumenItinerario) { this.resumenItinerario = resumenItinerario; }

    // 🆕 Helper methods para imágenes
    public int getCantidadImagenes() {
        return imagenesUrls != null ? imagenesUrls.size() : 0;
    }

    public boolean tieneImagenes() {
        return imagenesUrls != null && !imagenesUrls.isEmpty();
    }
}
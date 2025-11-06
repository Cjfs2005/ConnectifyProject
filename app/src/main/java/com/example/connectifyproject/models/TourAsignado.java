package com.example.connectifyproject.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.google.firebase.Timestamp;
import androidx.annotation.Nullable;

public class TourAsignado implements Serializable {
    private String id;
    private String ofertaTourId; // Referencia a tours_ofertas
    private String titulo;
    private String descripcion;
    private double precio;
    private String duracion;
    private Timestamp fechaRealizacion;
    private String horaInicio;
    private String horaFin;
    
    // Itinerario con seguimiento
    private List<Map<String, Object>> itinerario;
    private List<Map<String, Object>> serviciosAdicionales;
    
    // Información del guía asignado
    private Map<String, Object> guiaAsignado;
    
    // Información de la empresa
    private String empresaId;
    private String nombreEmpresa;
    private String correoEmpresa;
    
    // Pago y requisitos
    private double pagoGuia;
    private List<String> idiomasRequeridos;
    private String consideraciones;
    
    // Participantes del tour
    private List<Map<String, Object>> participantes;
    
    // Control del tour
    private String estado; // confirmado, en_curso, completado, cancelado
    private Integer numeroParticipantesTotal;
    private boolean checkInRealizado;
    private boolean checkOutRealizado;
    @Nullable
    private Timestamp horaCheckIn;
    @Nullable
    private Timestamp horaCheckOut;
    
    // Evaluación post-tour
    private List<Map<String, Object>> reseniasClientes;
    private double calificacionPromedio;
    private String comentariosGuia;
    
    // Metadatos
    private Timestamp fechaAsignacion;
    private Timestamp fechaCreacion;
    private Timestamp fechaActualizacion;
    private boolean habilitado;

    // Constructor vacío requerido por Firebase
    public TourAsignado() {}

    // Constructor para crear tour asignado desde oferta
    public TourAsignado(String ofertaTourId, String titulo, String descripcion, double precio, 
                       String duracion, Timestamp fechaRealizacion, String horaInicio, String horaFin,
                       String empresaId, String nombreEmpresa, double pagoGuia) {
        this.ofertaTourId = ofertaTourId;
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
        this.estado = "confirmado";
        this.numeroParticipantesTotal = 0;
        this.checkInRealizado = false;
        this.checkOutRealizado = false;
        this.calificacionPromedio = 0.0;
        this.fechaCreacion = Timestamp.now();
        this.fechaActualizacion = Timestamp.now();
        this.fechaAsignacion = Timestamp.now();
        this.habilitado = true;
    }

    // Métodos helper para obtener información del itinerario
    public String getResumenItinerario() {
        if (itinerario == null || itinerario.isEmpty()) {
            return "Itinerario no disponible";
        }
        
        StringBuilder resumen = new StringBuilder();
        for (Map<String, Object> punto : itinerario) {
            String lugar = (String) punto.get("lugar");
            String horaEstimada = (String) punto.get("horaEstimada");
            if (lugar != null && horaEstimada != null) {
                if (resumen.length() > 0) resumen.append(" → ");
                resumen.append(horaEstimada).append(" ").append(lugar);
            }
        }
        return resumen.toString();
    }
    
    public String getServiciosResumen() {
        if (serviciosAdicionales == null || serviciosAdicionales.isEmpty()) {
            return "Sin servicios adicionales";
        }
        
        StringBuilder resumen = new StringBuilder();
        for (Map<String, Object> servicio : serviciosAdicionales) {
            String nombre = (String) servicio.get("nombre");
            Boolean esPagado = (Boolean) servicio.get("esPagado");
            if (nombre != null) {
                if (resumen.length() > 0) resumen.append(", ");
                resumen.append(nombre);
                if (esPagado != null && esPagado) {
                    resumen.append(" (Pagado)");
                }
            }
        }
        return resumen.toString();
    }
    
    public String getIdiomasTexto() {
        if (idiomasRequeridos == null || idiomasRequeridos.isEmpty()) {
            return "No especificado";
        }
        return String.join(", ", idiomasRequeridos);
    }

    public String getEstadoDisplay() {
        if (estado == null) return "Sin estado";
        switch (estado.toLowerCase()) {
            case "confirmado": return "Confirmado";
            case "en_curso": return "En Curso";
            case "completado": return "Completado";
            case "cancelado": return "Cancelado";
            default: return estado;
        }
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getOfertaTourId() { return ofertaTourId; }
    public void setOfertaTourId(String ofertaTourId) { this.ofertaTourId = ofertaTourId; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    
    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }
    
    public Timestamp getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(Timestamp fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }
    
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    
    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
    
    public List<Map<String, Object>> getItinerario() { return itinerario; }
    public void setItinerario(List<Map<String, Object>> itinerario) { this.itinerario = itinerario; }
    
    public List<Map<String, Object>> getServiciosAdicionales() { return serviciosAdicionales; }
    public void setServiciosAdicionales(List<Map<String, Object>> serviciosAdicionales) { this.serviciosAdicionales = serviciosAdicionales; }
    
    public Map<String, Object> getGuiaAsignado() { return guiaAsignado; }
    public void setGuiaAsignado(Map<String, Object> guiaAsignado) { this.guiaAsignado = guiaAsignado; }
    
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
    
    public List<Map<String, Object>> getParticipantes() { return participantes; }
    public void setParticipantes(List<Map<String, Object>> participantes) { this.participantes = participantes; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Integer getNumeroParticipantesTotal() { return numeroParticipantesTotal; }
    public void setNumeroParticipantesTotal(Integer numeroParticipantesTotal) { this.numeroParticipantesTotal = numeroParticipantesTotal; }
    
    public boolean isCheckInRealizado() { return checkInRealizado; }
    public void setCheckInRealizado(boolean checkInRealizado) { this.checkInRealizado = checkInRealizado; }
    
    public boolean isCheckOutRealizado() { return checkOutRealizado; }
    public void setCheckOutRealizado(boolean checkOutRealizado) { this.checkOutRealizado = checkOutRealizado; }
    
    @Nullable
    public Timestamp getHoraCheckIn() { return horaCheckIn; }
    public void setHoraCheckIn(@Nullable Timestamp horaCheckIn) { this.horaCheckIn = horaCheckIn; }
    
    @Nullable
    public Timestamp getHoraCheckOut() { return horaCheckOut; }
    public void setHoraCheckOut(@Nullable Timestamp horaCheckOut) { this.horaCheckOut = horaCheckOut; }
    
    public List<Map<String, Object>> getReseniasClientes() { return reseniasClientes; }
    public void setReseniasClientes(List<Map<String, Object>> reseniasClientes) { this.reseniasClientes = reseniasClientes; }
    
    public double getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }
    
    public String getComentariosGuia() { return comentariosGuia; }
    public void setComentariosGuia(String comentariosGuia) { this.comentariosGuia = comentariosGuia; }
    
    public Timestamp getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(Timestamp fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    
    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public Timestamp getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Timestamp fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}
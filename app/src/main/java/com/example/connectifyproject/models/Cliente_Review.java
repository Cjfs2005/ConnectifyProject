package com.example.connectifyproject.models;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modelo para representar una reseña de cliente
 * Utilizado en la visualización de reseñas de empresas de tours
 * 
 * Estructura en Firebase:
 * usuarios/{empresaId}/resenas/{resenaId}
 *   - usuarioId: String
 *   - nombreUsuario: String
 *   - fotoUsuario: String (URL)
 *   - calificacion: Number (1-5)
 *   - comentario: String
 *   - fecha: Timestamp
 */
public class Cliente_Review {
    private String id;
    private String usuarioId;
    private String nombreUsuario;
    private String fotoUsuario;
    private double calificacion;
    private String comentario;
    private Timestamp fecha;
    
    // Constructor vacío requerido por Firestore
    public Cliente_Review() {
    }
    
    // Constructor completo
    public Cliente_Review(String id, String usuarioId, String nombreUsuario, String fotoUsuario, 
                         double calificacion, String comentario, Timestamp fecha) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.fotoUsuario = fotoUsuario;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    // Getters
    public String getId() { return id; }
    public String getUsuarioId() { return usuarioId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getFotoUsuario() { return fotoUsuario; }
    public double getCalificacion() { return calificacion; }
    public String getComentario() { return comentario; }
    public Timestamp getFecha() { return fecha; }
    
    // Getters de compatibilidad para UI existente
    public String getUserName() { return nombreUsuario; }
    public String getReviewText() { return comentario; }
    
    public String getRatingText() { 
        return String.format(Locale.getDefault(), "%.1f", calificacion); 
    }
    
    public String getRatingStars() { 
        return String.format(Locale.getDefault(), "%.1f", calificacion); 
    }
    
    public String getDate() {
        if (fecha != null) {
            Date date = fecha.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-ES"));
            return sdf.format(date);
        }
        return "";
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public void setFotoUsuario(String fotoUsuario) { this.fotoUsuario = fotoUsuario; }
    public void setCalificacion(double calificacion) { this.calificacion = calificacion; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
}
package com.example.connectifyproject.views.superadmin.logs;

public class SaLogFirestoreItem {
    public final String titulo;
    public final String descripcion;
    public final long timestamp;

    public SaLogFirestoreItem(String titulo, String descripcion, long timestamp) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.timestamp = timestamp;
    }
}

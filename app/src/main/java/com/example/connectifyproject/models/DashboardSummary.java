package com.example.connectifyproject.models;

public class DashboardSummary {
    private final int toursEnCurso;
    private final int proximosTours;
    private final int ventasTotales; // en USD
    private final int ventasTours;   // en USD
    private final int notificaciones;
    private final String nombre;

    public DashboardSummary(int toursEnCurso, int proximosTours, int ventasTotales, int ventasTours, int notificaciones, String nombre) {
        this.toursEnCurso = toursEnCurso;
        this.proximosTours = proximosTours;
        this.ventasTotales = ventasTotales;
        this.ventasTours = ventasTours;
        this.notificaciones = notificaciones;
        this.nombre = nombre;
    }

    public int getToursEnCurso() { return toursEnCurso; }
    public int getProximosTours() { return proximosTours; }
    public int getVentasTotales() { return ventasTotales; }
    public int getVentasTours() { return ventasTours; }
    public int getNotificaciones() { return notificaciones; }
    public String getNombre() { return nombre; }
}
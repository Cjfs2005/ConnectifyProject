package com.example.connectifyproject.views.superadmin.reports;

/**
 * Resumen de métricas del Dashboard de SuperAdmin.
 */
public class ReportsSummary {
    public int totalMes;           // Recaudación total del mes (en soles)
    public int empresasActivas;    // Número de empresas con habilitado=true
    public double promedioDia;     // Recaudación promedio por día (totalMes / díasDelMes)
    
    // Métricas de tours
    public int toursCompletados;   // Tours con estado="completado"
    public int toursPendientes;    // Tours con estado != "completado" y != "cancelado"

    public ReportsSummary(int totalMes, int empresasActivas, double promedioDia) {
        this.totalMes = totalMes;
        this.empresasActivas = empresasActivas;
        this.promedioDia = promedioDia;
        this.toursCompletados = 0;
        this.toursPendientes = 0;
    }
}

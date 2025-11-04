package com.example.connectifyproject.views.superadmin.reports;

public class ReportsSummary {
    public int totalMes;        // total de reservas (todas las empresas) en el mes
    public int empresasActivas; // #empresas con active==true en la base
    public double promedioDia;  // totalMes / díasDelMes (con 2 decimales)

    public ReportsSummary(int totalMes, int empresasActivas, double promedioDia) {
        this.totalMes = totalMes;
        this.empresasActivas = empresasActivas;
        this.promedioDia = promedioDia;
    }
}

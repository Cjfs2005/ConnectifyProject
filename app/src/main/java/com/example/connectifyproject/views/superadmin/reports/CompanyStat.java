package com.example.connectifyproject.views.superadmin.reports;

public class CompanyStat {
    public String companyId;
    public String name;
    public boolean active;
    public int monthTotal;   // reservas en el mes seleccionado

    public CompanyStat(String companyId, String name, boolean active, int monthTotal) {
        this.companyId = companyId;
        this.name = name;
        this.active = active;
        this.monthTotal = monthTotal;
    }
}

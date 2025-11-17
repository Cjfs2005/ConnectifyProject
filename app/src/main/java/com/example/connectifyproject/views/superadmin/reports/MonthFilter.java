package com.example.connectifyproject.views.superadmin.reports;

public enum MonthFilter {
    // 1..12. Guarda mes “humano” para consulta y cálculo de días.
    JAN(1), FEB(2), MAR(3), APR(4), MAY(5), JUN(6),
    JUL(7), AUG(8), SEP(9), OCT(10), NOV(11), DEC(12);

    public final int number;
    MonthFilter(int n){ this.number = n; }

    public static MonthFilter fromNumber(int n){
        for(MonthFilter m: values()) if(m.number==n) return m;
        return null;
    }
}

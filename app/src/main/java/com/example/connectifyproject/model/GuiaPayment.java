package com.example.connectifyproject.model;

public class GuiaPayment {

    private String id;              // ID del documento de pago
    private double amount;          // Monto pagado al guía
    private String dateTime;        // Fecha + hora formateadas
    private String status;          // Pendiente / Realizado (opcional)
    private String tourName;        // Nombre del tour
    private String companyName;     // Nombre de la empresa / cliente que paga
    private String uidUsuarioPaga;  // UID del usuario que paga

    public GuiaPayment() {
    }

    public GuiaPayment(String id,
                       double amount,
                       String dateTime,
                       String status,
                       String tourName,
                       String companyName,
                       String uidUsuarioPaga) {
        this.id = id;
        this.amount = amount;
        this.dateTime = dateTime;
        this.status = status;
        this.tourName = tourName;
        this.companyName = companyName;
        this.uidUsuarioPaga = uidUsuarioPaga;
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getTourName() {
        return tourName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getUidUsuarioPaga() {
        return uidUsuarioPaga;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}

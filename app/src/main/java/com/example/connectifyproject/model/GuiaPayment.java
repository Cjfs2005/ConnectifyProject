package com.example.connectifyproject.model;

public class GuiaPayment {
    private String id;
    private double amount;
    private String date;
    private String status;  // "Pendiente", "Realizado"
    private String method;  // "ABC Bank ATM", etc.

    public GuiaPayment(String id, double amount, String date, String status, String method) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.method = method;
    }

    // Getters
    public String getId() { return id; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getMethod() { return method; }
}
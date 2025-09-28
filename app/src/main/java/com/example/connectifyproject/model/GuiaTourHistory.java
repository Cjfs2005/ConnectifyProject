package com.example.connectifyproject.model;

public class GuiaTourHistory {
    private String id;
    private String name;
    private String date;
    private String status;  // "Pendiente", "Realizado", "Cancelado"
    private String rating;  // "★★★★★ Confirmado" or ""

    public GuiaTourHistory(String id, String name, String date, String status, String rating) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.status = status;
        this.rating = rating;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getRating() { return rating; }
}
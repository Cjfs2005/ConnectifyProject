package com.example.connectifyproject.models;

import java.io.Serializable;

public class Cliente_Tour implements Serializable {
    private String id;
    private String title;
    private String company;
    private String duration;
    private String date;
    private String startTime;
    private String endTime;
    private double price;
    private String location;
    private String description;
    private String imageUrl;
    private float calificacion;
    private String companyName;

    public Cliente_Tour(String id, String title, String company, String duration, 
                       String date, double price, String location, String description) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.duration = duration;
        this.date = date;
        this.startTime = "13:10"; // Valor por defecto
        this.endTime = "18:40"; // Valor por defecto
        this.price = price;
        this.location = location;
        this.description = description;
        this.companyName = company;
    }
    
    // Constructor completo con horarios
    public Cliente_Tour(String id, String title, String company, String duration, 
                       String date, String startTime, String endTime, double price, 
                       String location, String description) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.duration = duration;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.location = location;
        this.description = description;
        this.companyName = company;
    }
    
    // Nuevo constructor para el formato usado en el generador
    public Cliente_Tour(String id, String titulo, String descripcion, String duracion, 
                       double precio, String ubicacion, float calificacion, String nombreEmpresa) {
        this.id = id;
        this.title = titulo;
        this.description = descripcion;
        this.duration = duracion;
        this.price = precio;
        this.location = ubicacion;
        this.calificacion = calificacion;
        this.companyName = nombreEmpresa;
        this.company = nombreEmpresa;
        this.date = "Hoy"; // Valor por defecto
        this.startTime = "13:10"; // Valor por defecto
        this.endTime = "18:40"; // Valor por defecto
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getDuration() { return duration; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public double getPrice() { return price; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public float getCalificacion() { return calificacion; }
    public String getCompanyName() { return companyName; }
    
    // Métodos alternativos para compatibilidad
    public String getTitulo() { return title; }
    public String getUbicacion() { return location; }
    public String getDuracion() { return duration; }
    public double getPrecio() { return price; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCompany(String company) { this.company = company; this.companyName = company; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setPrice(double price) { this.price = price; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCalificacion(float calificacion) { this.calificacion = calificacion; }
    public void setCompanyName(String companyName) { this.companyName = companyName; this.company = companyName; }
}
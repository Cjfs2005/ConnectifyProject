package com.example.connectifyproject.models;

public class TourService {
    private String name;
    private boolean isPaid;
    private double price;
    private String description;

    public TourService(String name, boolean isPaid, double price, String description) {
        this.name = name;
        this.isPaid = isPaid;
        this.price = price;
        this.description = description;
    }

    // Getters y Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
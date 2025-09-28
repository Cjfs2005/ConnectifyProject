package com.example.connectifyproject.models;

public class Cliente_ServicioAdicional {
    private String id;
    private String name;
    private String description;
    private double price;
    private boolean selected;

    public Cliente_ServicioAdicional(String id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.selected = false;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public boolean isSelected() { return selected; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
package com.example.connectifyproject.models;

public class Cliente_Tour {
    private String id;
    private String title;
    private String company;
    private String duration;
    private String date;
    private double price;
    private String location;
    private String description;
    private String imageUrl;

    public Cliente_Tour(String id, String title, String company, String duration, 
                       String date, double price, String location, String description) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.duration = duration;
        this.date = date;
        this.price = price;
        this.location = location;
        this.description = description;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getDuration() { return duration; }
    public String getDate() { return date; }
    public double getPrice() { return price; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCompany(String company) { this.company = company; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setDate(String date) { this.date = date; }
    public void setPrice(double price) { this.price = price; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
package com.example.connectifyproject.models;

public class Cliente_ItinerarioItem {
    private String id;
    private String placeName;
    private String duration;
    private String visitTime;
    private String description;
    private double latitude;
    private double longitude;
    private boolean isLastItem;

    public Cliente_ItinerarioItem(String id, String placeName, String duration, 
                                 String visitTime, String description, 
                                 double latitude, double longitude) {
        this.id = id;
        this.placeName = placeName;
        this.duration = duration;
        this.visitTime = visitTime;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isLastItem = false;
    }

    // Constructor alternativo para compatibilidad
    public Cliente_ItinerarioItem(String visitTime, String placeName, String description, 
                                 double latitude, double longitude) {
        this.id = "";
        this.placeName = placeName;
        this.duration = "";
        this.visitTime = visitTime;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isLastItem = false;
    }

    // Getters
    public String getId() { return id; }
    public String getPlaceName() { return placeName; }
    public String getDuration() { return duration; }
    public String getVisitTime() { return visitTime; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isLastItem() { return isLastItem; }
    
    // Métodos de compatibilidad
    public String getTime() { return visitTime; }
    public String getTitle() { return placeName; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setVisitTime(String visitTime) { this.visitTime = visitTime; }
    public void setDescription(String description) { this.description = description; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setLastItem(boolean lastItem) { isLastItem = lastItem; }
}
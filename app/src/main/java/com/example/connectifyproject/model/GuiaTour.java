package com.example.connectifyproject.model;

public class GuiaTour {
    private String name;
    private String location;
    private double price;
    private String duration;
    private String languages;
    private String startTime;
    private String date;
    private String description; // Detalles/Itinerario
    private String benefits;
    private String schedule;
    private String meetingPoint;
    private String empresa;
    private String itinerario; // e.g., "Plaza de Armas (1hrs30min), ..."
    private String experienciaMinima; // e.g., "1 año como guía turístico"
    private String puntualidad; // e.g., "Puntualidad en puntos de encuentro"
    private boolean transporteIncluido;
    private boolean almuerzoIncluido;
    private String firebaseId; // ID de Firebase para referencia
    private String ciudad; // Ciudad donde se realiza el tour

    public GuiaTour(String name, String location, double price, String duration, String languages,
                    String startTime, String date, String description, String benefits,
                    String schedule, String meetingPoint, String empresa, String itinerario,
                    String experienciaMinima, String puntualidad, boolean transporteIncluido, boolean almuerzoIncluido) {
        this.name = name;
        this.location = location;
        this.price = price;
        this.duration = duration;
        this.languages = languages;
        this.startTime = startTime;
        this.date = date;
        this.description = description;
        this.benefits = benefits;
        this.schedule = schedule;
        this.meetingPoint = meetingPoint;
        this.empresa = empresa;
        this.itinerario = itinerario;
        this.experienciaMinima = experienciaMinima;
        this.puntualidad = puntualidad;
        this.transporteIncluido = transporteIncluido;
        this.almuerzoIncluido = almuerzoIncluido;
    }

    // Getters
    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public String getDuration() { return duration; }
    public String getLanguages() { return languages; }
    public String getStartTime() { return startTime; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getBenefits() { return benefits; }
    public String getSchedule() { return schedule; }
    public String getMeetingPoint() { return meetingPoint; }
    public String getEmpresa() { return empresa; }
    public String getItinerario() { return itinerario; }
    public String getExperienciaMinima() { return experienciaMinima; }
    public String getPuntualidad() { return puntualidad; }
    public boolean isTransporteIncluido() { return transporteIncluido; }
    public boolean isAlmuerzoIncluido() { return almuerzoIncluido; }
    public String getFirebaseId() { return firebaseId; }
    public String getCiudad() { return ciudad; }
    
    // Setters
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
}
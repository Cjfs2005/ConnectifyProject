package com.example.connectifyproject.model;

import java.util.List;

public class GuiaAssignedTour {
    private String name;
    private String empresa;
    private String initio;
    private String duration;
    private int clients;
    private String status;
    private String date;
    private String languages;
    private String services;
    private List<String> itinerario;
    private double pagoGuia; // ✅ Añadido para compatibilidad con ofertas

    public GuiaAssignedTour(String name, String empresa, String initio, String duration, int clients, String status, String date, String languages, String services, List<String> itinerario, double pagoGuia) {
        this.name = name;
        this.empresa = empresa;
        this.initio = initio;
        this.duration = duration;
        this.clients = clients;
        this.status = status;
        this.date = date;
        this.languages = languages;
        this.services = services;
        this.itinerario = itinerario;
        this.pagoGuia = pagoGuia; // ✅ Inicializar pagoGuia
    }

    // Getters
    public String getName() { return name; }
    public String getEmpresa() { return empresa; }
    public String getInitio() { return initio; }
    public String getDuration() { return duration; }
    public int getClients() { return clients; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getLanguages() { return languages; }
    public String getServices() { return services; }
    public List<String> getItinerario() { return itinerario; }
    public double getPagoGuia() { return pagoGuia; } // ✅ Getter para pagoGuia
}
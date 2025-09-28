package com.example.connectifyproject.model;

public class GuiaAssignedItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ASSIGNED_TOUR = 1;

    private int type;
    private String header;
    private GuiaAssignedTour assignedTour;

    public GuiaAssignedItem(String header) {
        this.type = TYPE_HEADER;
        this.header = header;
    }

    public GuiaAssignedItem(GuiaAssignedTour assignedTour) {
        this.type = TYPE_ASSIGNED_TOUR;
        this.assignedTour = assignedTour;
    }

    public int getType() { return type; }
    public String getHeader() { return header; }
    public GuiaAssignedTour getAssignedTour() { return assignedTour; }
}
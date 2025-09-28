package com.example.connectifyproject.model;

public class GuiaItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TOUR = 1;

    private int type;
    private String header;
    private GuiaTour tour;

    /*private AssignedTour assignedTour; // Nuevo */

    public GuiaItem(String header) {
        this.type = TYPE_HEADER;
        this.header = header;
    }

    public GuiaItem(GuiaTour tour) {
        this.type = TYPE_TOUR;
        this.tour = tour;
    }

    /*
    public GuiaItem(AssignedTour assignedTour) {
        this.assignedTour = assignedTour;
    }
    */

    public int getType() { return type; }
    public String getHeader() { return header; }
    public GuiaTour getTour() { return tour; }

    /*
    public AssignedTour getAssignedTour() { return assignedTour; }
     */
}
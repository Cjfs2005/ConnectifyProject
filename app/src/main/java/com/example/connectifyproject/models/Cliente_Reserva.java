package com.example.connectifyproject.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cliente_Reserva implements Serializable {
    private String id;
    private Cliente_Tour tour;                // Datos base del tour
    private int personas;                     // Cantidad de personas
    private String fecha;                     // Fecha de la reserva (dd/MM/yyyy)
    private String horaInicio;                // Hora de inicio
    private String horaFin;                   // Hora de fin
    private List<Cliente_ServicioAdicional> servicios; // Lista completa con selected
    private Cliente_PaymentMethod metodoPago; // Método de pago usado
    private double totalServicios;            // Suma de servicios adicionales
    private double total;                     // Total final
    private String estado;                    // Próxima | Pasada | Cancelada

    public Cliente_Reserva(String id, Cliente_Tour tour, int personas, String fecha,
                           String horaInicio, String horaFin,
                           List<Cliente_ServicioAdicional> servicios,
                           Cliente_PaymentMethod metodoPago,
                           double totalServicios, double total,
                           String estado) {
        this.id = id;
        this.tour = tour;
        this.personas = personas;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.servicios = servicios != null ? servicios : new ArrayList<>();
        this.metodoPago = metodoPago;
        this.totalServicios = totalServicios;
        this.total = total;
        this.estado = estado;
    }

    public Cliente_Reserva() {
        this("", null, 1, "", "", "", new ArrayList<>(), null, 0.0, 0.0, "Próxima");
    }

    public String getId() { return id; }
    public Cliente_Tour getTour() { return tour; }
    public int getPersonas() { return personas; }
    public String getFecha() { return fecha; }
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }
    public List<Cliente_ServicioAdicional> getServicios() { return servicios; }
    public Cliente_PaymentMethod getMetodoPago() { return metodoPago; }
    public double getTotalServicios() { return totalServicios; }
    public double getTotal() { return total; }
    public String getEstado() { return estado; }

    public void setId(String id) { this.id = id; }
    public void setTour(Cliente_Tour tour) { this.tour = tour; }
    public void setPersonas(int personas) { this.personas = personas; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
    public void setServicios(List<Cliente_ServicioAdicional> servicios) { this.servicios = servicios; }
    public void setMetodoPago(Cliente_PaymentMethod metodoPago) { this.metodoPago = metodoPago; }
    public void setTotalServicios(double totalServicios) { this.totalServicios = totalServicios; }
    public void setTotal(double total) { this.total = total; }
    public void setEstado(String estado) { this.estado = estado; }

    // Utilidades
    public double calcularTotalServiciosSeleccionadosPorPersona() {
        double sum = 0.0;
        if (servicios != null) {
            for (Cliente_ServicioAdicional s : servicios) {
                if (s.isSelected()) sum += s.getPrice();
            }
        }
        return sum;
    }

    public double calcularTotal() {
        double base = tour != null ? tour.getPrecio() : 0.0;
        double serviciosSeleccionados = calcularTotalServiciosSeleccionadosPorPersona();
        return (base + serviciosSeleccionados) * Math.max(1, personas);
    }

    // Métodos adicionales para FileStorageManager
    public List<Cliente_ServicioAdicional> getServiciosAdicionales() {
        return servicios;
    }

    public double getSubtotal() {
        return getTotal() / 1.18; // Sin IGV
    }

    public double getIgv() {
        return getTotal() - getSubtotal(); // 18% IGV
    }

    /**
     * Crear reserva de ejemplo para testing
     */
    public static Cliente_Reserva getReservaExample() {
        Cliente_Tour tour = Cliente_Tour.getToursExample().get(0);
        List<Cliente_ServicioAdicional> servicios = new ArrayList<>();
        servicios.add(new Cliente_ServicioAdicional("1", "Almuerzo incluido", "Comida incluida en el tour", 25.0));
        servicios.add(new Cliente_ServicioAdicional("2", "Guía bilingüe", "Guía que habla español e inglés", 15.0));
        
        Cliente_PaymentMethod metodoPago = new Cliente_PaymentMethod("pm_001", "**** **** **** 1234", "12/25", "Jorge Romero", "VISA", true);
        
        return new Cliente_Reserva(
            "R001",
            tour,
            2,
            "15/11/2024",
            "09:00",
            "18:00",
            servicios,
            metodoPago,
            80.0,
            200.0,
            "Confirmada"
        );
    }
}

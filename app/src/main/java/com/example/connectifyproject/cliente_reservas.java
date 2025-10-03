package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ReservasAdapter;
import com.example.connectifyproject.models.Cliente_Reserva;
import com.example.connectifyproject.models.Cliente_Tour;
import com.example.connectifyproject.models.Cliente_ServicioAdicional;
import com.example.connectifyproject.models.Cliente_PaymentMethod;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class cliente_reservas extends AppCompatActivity {

    private ImageButton btnNotifications;
    private TabLayout tabLayoutReservas;
    private RecyclerView rvReservas;
    private Cliente_ReservasAdapter reservasAdapter;
    private List<Cliente_Reserva> allReservas;
    private List<Cliente_Reserva> filteredReservas;
    private boolean showingProximas = true;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_reservas);

        initViews();
        setupBottomNavigation();
        setupClickListeners();
        setupRecyclerView();
        loadReservasData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Reservas" esté seleccionado cuando regresamos a esta actividad
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_reservas);
        }
    }

    private void initViews() {
        btnNotifications = findViewById(R.id.btn_notifications);
        tabLayoutReservas = findViewById(R.id.tab_layout_reservas);
        rvReservas = findViewById(R.id.rv_reservas);
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_inicio) {
                Intent intent = new Intent(this, cliente_inicio.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                // Ya estamos en reservas
                return true;
            } else if (itemId == R.id.nav_tours) {
                Intent intent = new Intent(this, cliente_tours.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_chat) {
                Intent intent = new Intent(this, cliente_chat_list.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                Intent intent = new Intent(this, cliente_perfil.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });
        
        // Seleccionar "Reservas" por defecto
        bottomNavigation.setSelectedItemId(R.id.nav_reservas);
    }

    private void setupClickListeners() {
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_reservas");
            startActivity(intent);
        });
        
        setupTabs();
    }
    
    private void setupTabs() {
        // Agregar tabs
        tabLayoutReservas.addTab(tabLayoutReservas.newTab().setText("Próximas"));
        tabLayoutReservas.addTab(tabLayoutReservas.newTab().setText("Pasadas"));
        
        // Listener para cambios de tab
        tabLayoutReservas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingProximas = tab.getPosition() == 0;
                filterReservas();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }



    private void setupRecyclerView() {
    allReservas = new ArrayList<>();
    filteredReservas = new ArrayList<>();
    reservasAdapter = new Cliente_ReservasAdapter(this, filteredReservas);
        
        rvReservas.setLayoutManager(new LinearLayoutManager(this));
        rvReservas.setAdapter(reservasAdapter);
    }

    private void loadReservasData() {
        // Construir reservas ejemplo con servicios y método de pago
        
        // Reservas próximas (2 reservas futuras)
        allReservas.add(crearReservaEjemplo(
                new Cliente_Tour("1", "Tour histórico por Lima", "Lima Tours",
                        "5 hrs 30 min", "23/12/2025", 160.00, "Lima, Perú",
                        "Explora el centro histórico de Lima y sus principales atractivos"),
                2, "23/12/2025", "13:10", "18:40", "Próxima"));
        
        allReservas.add(crearReservaEjemplo(
                new Cliente_Tour("2", "Tour gastronómico por Lima", "Lima Food Tours",
                        "4 hrs", "15/01/2026", 120.00, "Lima, Perú",
                        "Descubre los sabores de la gastronomía peruana"),
                1, "15/01/2026", "18:00", "22:00", "Próxima"));

        // Reservas pasadas (7 reservas pasadas)
        allReservas.add(crearReservaEjemplo(
                new Cliente_Tour("3", "Tour histórico por Lima", "Lima Tours",
                        "5 hrs 30 min", "15/08/2024", 160.00, "Lima, Perú",
                        "Explora el centro histórico de Lima"),
                2, "15/08/2024", "09:00", "14:30", "Pasada"));
                
        allReservas.add(crearReservaEjemplo(
                new Cliente_Tour("4", "Tour por Arequipa", "Arequipa Tours",
                        "6 hrs", "10/07/2024", 180.00, "Arequipa, Perú",
                        "Descubre la ciudad blanca y su arquitectura colonial"),
                3, "10/07/2024", "08:00", "14:00", "Pasada"));
                
        allReservas.add(crearReservaEjemplo(
                new Cliente_Tour("5", "Tour por Cusco", "Cusco Adventures",
                        "7 hrs", "25/06/2024", 200.00, "Cusco, Perú",
                        "Explora la capital del imperio Inca"),
                2, "25/06/2024", "07:30", "14:30", "Pasada"));
                
        allReservas.add(crearReservaEjemplo(
                new Cliente_Tour("6", "Tour por las Líneas de Nazca", "Nazca Flights",
                        "3 hrs", "12/05/2024", 280.00, "Nazca, Perú",
                        "Sobrevuela las misteriosas líneas de Nazca"),
                1, "12/05/2024", "10:00", "13:00", "Pasada"));
                
        allReservas.add(crearReservaEjemplo(
                new Cliente_Tour("7", "Tour por Machu Picchu", "Inca Trails",
                        "8 hrs", "20/04/2024", 350.00, "Cusco, Perú",
                        "Visita la maravilla del mundo"),
                2, "20/04/2024", "06:00", "14:00", "Pasada"));
                
        allReservas.add(crearReservaEjemplo(
                new Cliente_Tour("8", "Tour por Trujillo", "Norte Tours",
                        "5 hrs", "08/03/2024", 140.00, "Trujillo, Perú",
                        "Descubre la ciudad de la eterna primavera"),
                2, "08/03/2024", "09:00", "14:00", "Pasada"));
                
        allReservas.add(crearReservaEjemplo(
                new Cliente_Tour("9", "Tour por Paracas", "Costa Tours",
                        "4 hrs", "15/02/2024", 170.00, "Paracas, Perú",
                        "Observa la fauna marina en las Islas Ballestas"),
                3, "15/02/2024", "08:30", "12:30", "Pasada"));

        // Mostrar próximas por defecto
        filterReservas();
    }    private void filterReservas() {
        filteredReservas.clear();
        
    for (Cliente_Reserva reserva : allReservas) {
        String fecha = reserva.getFecha();
        boolean esFutura = isFutureDate(fecha);
            
            if (showingProximas && esFutura) {
                filteredReservas.add(reserva);
            } else if (!showingProximas && !esFutura) {
                filteredReservas.add(reserva);
            }
        }
        
        reservasAdapter.notifyDataSetChanged();
    }
    
    private boolean isFutureDate(String dateString) {
        // Lógica simple: fechas 2025 en adelante son futuras, 2024 y anteriores son pasadas
        // En una app real, aquí se compararía con la fecha actual
        return dateString.contains("2025") || dateString.contains("2026") || 
               dateString.contains("2027") || dateString.contains("2028");
    }

    private Cliente_Reserva crearReservaEjemplo(Cliente_Tour tour, int personas,
                                                String fecha, String horaInicio, String horaFin,
                                                String estado) {
        // Servicios disponibles para ese tour (variar según el tour)
        List<Cliente_ServicioAdicional> servicios = new ArrayList<>();
        
        // Servicios base que siempre están disponibles
        Cliente_ServicioAdicional folleto = new Cliente_ServicioAdicional("sa1", "Folleto turístico", "Guía impresa con información del tour", 0.0);
        Cliente_ServicioAdicional almuerzo = new Cliente_ServicioAdicional("sa2", "Almuerzo", "Menú turístico en restaurante local", 16.0);
        Cliente_ServicioAdicional traslado = new Cliente_ServicioAdicional("sa3", "Traslado hotel", "Recojo y retorno al hotel", 10.0);
        
        // Variar la selección según el ID del tour para tener diversidad
        int tourIdNum = Integer.parseInt(tour.getId());
        if (tourIdNum % 3 == 0) {
            // Solo folleto y traslado
            folleto.setSelected(true);
            almuerzo.setSelected(false);
            traslado.setSelected(true);
        } else if (tourIdNum % 3 == 1) {
            // Todos los servicios
            folleto.setSelected(true);
            almuerzo.setSelected(true);
            traslado.setSelected(false);
        } else {
            // Solo almuerzo
            folleto.setSelected(false);
            almuerzo.setSelected(true);
            traslado.setSelected(false);
        }
        
        servicios.add(folleto);
        servicios.add(almuerzo);
        servicios.add(traslado);

        // Alternar método de pago
        Cliente_PaymentMethod metodoPago = tourIdNum % 2 == 0 ? 
            Cliente_PaymentMethod.crearEjemploVisa() : 
            Cliente_PaymentMethod.crearEjemploMastercard();

        Cliente_Reserva reserva = new Cliente_Reserva();
        reserva.setId(tour.getId());
        reserva.setTour(tour);
        reserva.setPersonas(personas);
        reserva.setFecha(fecha);
        reserva.setHoraInicio(horaInicio);
        reserva.setHoraFin(horaFin);
        reserva.setServicios(servicios);
        reserva.setMetodoPago(metodoPago);
        // Calcular totales
        double totalServiciosPorPersona = reserva.calcularTotalServiciosSeleccionadosPorPersona();
        reserva.setTotalServicios(totalServiciosPorPersona * personas);
        reserva.setTotal((tour.getPrecio() + totalServiciosPorPersona) * personas);
        reserva.setEstado(estado);
        return reserva;
    }


}
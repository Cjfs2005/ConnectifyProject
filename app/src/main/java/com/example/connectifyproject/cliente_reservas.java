package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ReservasAdapter;
import com.example.connectifyproject.models.Cliente_Tour;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class cliente_reservas extends AppCompatActivity {

    private ImageButton btnNotifications;
    private TabLayout tabLayoutReservas;
    private RecyclerView rvReservas;
    private Cliente_ReservasAdapter reservasAdapter;
    private List<Cliente_Tour> allReservas;
    private List<Cliente_Tour> filteredReservas;
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
        // Datos hardcodeados duplicados - separados en próximas y pasadas
        
        // Reservas próximas (futuras - 2025 en adelante)
        allReservas.add(new Cliente_Tour("1", "Tour histórico por Lima", "Lima Tours", 
                "5 hrs 30 min", "23/12/2025", 160.00, "Lima, Perú", 
                "Explora el centro histórico de Lima y sus principales atractivos"));
        
        allReservas.add(new Cliente_Tour("2", "Tour histórico por Arequipa", "Arequipa Tours", 
                "5 hrs 30 min", "15/01/2026", 160.00, "Arequipa, Perú", 
                "Descubre la ciudad blanca y su arquitectura colonial"));
        
        allReservas.add(new Cliente_Tour("3", "Tour por Cusco", "Cusco Adventures", 
                "6 hrs", "20/02/2026", 180.00, "Cusco, Perú", 
                "Explora la capital del imperio Inca"));
                
        allReservas.add(new Cliente_Tour("4", "Tour gastronómico por Lima", "Lima Food Tours", 
                "4 hrs", "10/11/2025", 120.00, "Lima, Perú", 
                "Descubre los sabores de la gastronomía peruana"));
                
        allReservas.add(new Cliente_Tour("5", "Tour por Machu Picchu", "Inca Trails", 
                "8 hrs", "05/03/2026", 350.00, "Cusco, Perú", 
                "Visita la maravilla del mundo"));
                
        allReservas.add(new Cliente_Tour("6", "Tour por Huacachina", "Desert Tours", 
                "6 hrs", "18/01/2026", 200.00, "Ica, Perú", 
                "Aventura en el desierto y sandboarding"));

        // Reservas pasadas (anteriores - 2024 y anteriores)
        allReservas.add(new Cliente_Tour("7", "Tour histórico por Lima", "Lima Tours", 
                "5 hrs 30 min", "15/08/2024", 160.00, "Lima, Perú", 
                "Explora el centro histórico de Lima"));
        
        allReservas.add(new Cliente_Tour("8", "Tour por Trujillo", "Norte Tours", 
                "4 hrs", "10/07/2024", 140.00, "Trujillo, Perú", 
                "Descubre la ciudad de la eterna primavera"));
        
        allReservas.add(new Cliente_Tour("9", "Tour histórico por Arequipa", "Arequipa Tours", 
                "5 hrs 30 min", "25/06/2024", 160.00, "Arequipa, Perú", 
                "Descubre la ciudad blanca"));
                
        allReservas.add(new Cliente_Tour("10", "Tour por las Líneas de Nazca", "Nazca Flights", 
                "3 hrs", "12/05/2024", 280.00, "Nazca, Perú", 
                "Sobrevuela las misteriosas líneas de Nazca"));
                
        allReservas.add(new Cliente_Tour("11", "Tour por Chachapoyas", "Amazonia Tours", 
                "7 hrs", "20/04/2024", 220.00, "Chachapoyas, Perú", 
                "Explora la fortaleza de Kuélap"));
                
        allReservas.add(new Cliente_Tour("12", "Tour por Paracas", "Costa Tours", 
                "5 hrs", "08/03/2024", 170.00, "Paracas, Perú", 
                "Observa la fauna marina en las Islas Ballestas"));

        // Mostrar próximas por defecto
        filterReservas();
    }

    private void filterReservas() {
        filteredReservas.clear();
        
        for (Cliente_Tour reserva : allReservas) {
            String fecha = reserva.getDate();
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


}
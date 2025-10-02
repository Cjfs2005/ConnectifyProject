package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ReservasAdapter;
import com.example.connectifyproject.models.Cliente_Tour;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class cliente_reservas extends AppCompatActivity {

    private ImageButton btnNotifications;
    private MaterialButton btnProximas, btnPasadas;
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
        btnProximas = findViewById(R.id.btn_proximas);
        btnPasadas = findViewById(R.id.btn_pasadas);
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
        
        btnProximas.setOnClickListener(v -> {
            if (!showingProximas) {
                showingProximas = true;
                updateFilterButtons();
                filterReservas();
            }
        });
        
        btnPasadas.setOnClickListener(v -> {
            if (showingProximas) {
                showingProximas = false;
                updateFilterButtons();
                filterReservas();
            }
        });
    }

    private void updateFilterButtons() {
        if (showingProximas) {
            // Próximas activo - filled style (como está definido en XML por defecto)
            btnProximas.setBackgroundTintList(null); // Usar el color por defecto del tema
            btnProximas.setTextColor(getColor(android.R.color.white));
            btnProximas.setStrokeWidth(0);
            
            // Pasadas inactivo - outlined style
            btnPasadas.setBackgroundTintList(getColorStateList(android.R.color.transparent));
            btnPasadas.setTextColor(getResources().getColorStateList(R.color.cliente_bottom_nav_tint, getTheme()));
            btnPasadas.setStrokeColor(getResources().getColorStateList(R.color.cliente_bottom_nav_tint, getTheme()));
            btnPasadas.setStrokeWidth(2);
        } else {
            // Pasadas activo - filled style
            btnPasadas.setBackgroundTintList(null); // Usar el color por defecto del tema
            btnPasadas.setTextColor(getColor(android.R.color.white));
            btnPasadas.setStrokeWidth(0);
            
            // Próximas inactivo - outlined style
            btnProximas.setBackgroundTintList(getColorStateList(android.R.color.transparent));
            btnProximas.setTextColor(getResources().getColorStateList(R.color.cliente_bottom_nav_tint, getTheme()));
            btnProximas.setStrokeColor(getResources().getColorStateList(R.color.cliente_bottom_nav_tint, getTheme()));
            btnProximas.setStrokeWidth(2);
        }
    }

    private void setupRecyclerView() {
        allReservas = new ArrayList<>();
        filteredReservas = new ArrayList<>();
        reservasAdapter = new Cliente_ReservasAdapter(this, filteredReservas);
        
        rvReservas.setLayoutManager(new LinearLayoutManager(this));
        rvReservas.setAdapter(reservasAdapter);
    }

    private void loadReservasData() {
        // Datos hardcodeados - separados en próximas y pasadas
        
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

        // Reservas pasadas (anteriores - 2024 y anteriores)
        allReservas.add(new Cliente_Tour("4", "Tour histórico por Lima", "Lima Tours", 
                "5 hrs 30 min", "15/08/2024", 160.00, "Lima, Perú", 
                "Explora el centro histórico de Lima"));
        
        allReservas.add(new Cliente_Tour("5", "Tour por Trujillo", "Norte Tours", 
                "4 hrs", "10/07/2024", 140.00, "Trujillo, Perú", 
                "Descubre la ciudad de la eterna primavera"));
        
        allReservas.add(new Cliente_Tour("6", "Tour histórico por Arequipa", "Arequipa Tours", 
                "5 hrs 30 min", "25/06/2024", 160.00, "Arequipa, Perú", 
                "Descubre la ciudad blanca"));

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
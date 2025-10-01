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
            // Próximas activo - usar el color primario del tema
            btnProximas.setBackgroundTintList(getColorStateList(R.color.primary_color));
            btnProximas.setTextColor(getColor(android.R.color.white));
            
            // Pasadas inactivo
            btnPasadas.setBackgroundTintList(null);
            btnPasadas.setTextColor(getColor(R.color.primary_color));
        } else {
            // Pasadas activo
            btnPasadas.setBackgroundTintList(getColorStateList(R.color.primary_color));
            btnPasadas.setTextColor(getColor(android.R.color.white));
            
            // Próximas inactivo
            btnProximas.setBackgroundTintList(null);
            btnProximas.setTextColor(getColor(R.color.primary_color));
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
        // Datos hardcodeados basados en el mockup - todas usan cliente_tour_lima
        allReservas.add(new Cliente_Tour("1", "Tour histórico por Lima", "Lima Tours", 
                "5 hrs 30 min", "23/09/2025", 160.00, "Lima, Perú", 
                "Explora el centro histórico de Lima y sus principales atractivos"));
        
        allReservas.add(new Cliente_Tour("2", "Tour histórico por Arequipa", "Arequipa Tours", 
                "5 hrs 30 min", "23/03/2026", 160.00, "Arequipa, Perú", 
                "Descubre la ciudad blanca y su arquitectura colonial"));
        
        allReservas.add(new Cliente_Tour("3", "Tour histórico por Lima", "Lima Tours", 
                "5 hrs 30 min", "23/04/2026", 160.00, "Lima, Perú", 
                "Explora el centro histórico de Lima"));
        
        allReservas.add(new Cliente_Tour("4", "Tour histórico por Arequipa", "Arequipa Tours", 
                "5 hrs 30 min", "23/05/2026", 160.00, "Arequipa, Perú", 
                "Descubre la ciudad blanca"));
        
        allReservas.add(new Cliente_Tour("5", "Tour histórico por Lima", "Lima Tours", 
                "5 hrs 30 min", "23/06/2026", 160.00, "Lima, Perú", 
                "Explora el centro histórico de Lima"));
        
        allReservas.add(new Cliente_Tour("6", "Tour histórico por Arequipa", "Arequipa Tours", 
                "5 hrs 30 min", "23/07/2026", 160.00, "Arequipa, Perú", 
                "Descubre la ciudad blanca"));

        // Mostrar próximas por defecto
        filterReservas();
    }

    private void filterReservas() {
        filteredReservas.clear();
        filteredReservas.addAll(allReservas);
        reservasAdapter.notifyDataSetChanged();
    }


}
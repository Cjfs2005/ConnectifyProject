package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ToursAdapter;
import com.example.connectifyproject.models.Cliente_Tour;
import android.widget.ImageButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class cliente_tours extends AppCompatActivity {

    private ImageButton btnNotifications;
    private ImageButton btnFiltros;
    private RecyclerView rvTours;
    private Cliente_ToursAdapter toursAdapter;
    private List<Cliente_Tour> allTours;
    private List<Cliente_Tour> filteredTours;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_tours);

        initViews();
        setupBottomNavigation();
        setupClickListeners();
        setupRecyclerView();
        loadToursData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Tours" esté seleccionado cuando regresamos a esta actividad
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_tours);
        }
    }

    private void initViews() {
        btnNotifications = findViewById(R.id.btn_notifications);
        btnFiltros = findViewById(R.id.btn_filtros);
        rvTours = findViewById(R.id.rv_tours);
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
                Intent intent = new Intent(this, cliente_reservas.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_tours) {
                // Ya estamos en tours
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
        
        // Seleccionar "Tours" por defecto
        bottomNavigation.setSelectedItemId(R.id.nav_tours);
    }

    private void setupClickListeners() {
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_tours");
            startActivity(intent);
        });
        
        btnFiltros.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_tour_filtros.class);
            startActivityForResult(intent, 1001);
        });
    }

    private void setupRecyclerView() {
        allTours = new ArrayList<>();
        filteredTours = new ArrayList<>();
        toursAdapter = new Cliente_ToursAdapter(this, filteredTours);
        
        rvTours.setLayoutManager(new LinearLayoutManager(this));
        rvTours.setAdapter(toursAdapter);
    }

    private void loadToursData() {
        // Datos hardcodeados con fechas específicas variadas
        allTours.add(new Cliente_Tour("1", "Tour histórico por Lima", "Lima Tours", 
                "5 hrs 30 min", "15/11/2025", 160.00, "Lima, Perú", 
                "Explora el centro histórico de Lima y sus principales atractivos"));
        
        allTours.add(new Cliente_Tour("2", "Tour histórico por Arequipa", "Arequipa Tours", 
                "5 hrs 30 min", "22/11/2025", 160.00, "Arequipa, Perú", 
                "Descubre la ciudad blanca y su arquitectura colonial"));
        
        allTours.add(new Cliente_Tour("3", "Tour gastronómico por Lima", "Lima Food Tours", 
                "4 hrs", "08/12/2025", 120.00, "Lima, Perú", 
                "Recorre los mejores restaurantes y mercados de Lima"));
        
        allTours.add(new Cliente_Tour("4", "Tour por Cusco Imperial", "Cusco Adventures", 
                "6 hrs", "10/01/2026", 180.00, "Cusco, Perú", 
                "Conoce la capital del imperio Inca"));
        
        allTours.add(new Cliente_Tour("5", "Tour por Machu Picchu", "Inca Trails", 
                "8 hrs", "25/12/2025", 350.00, "Cusco, Perú", 
                "Visita la maravilla del mundo moderno"));
        
        allTours.add(new Cliente_Tour("6", "Tour por Huacachina", "Desert Adventures", 
                "6 hrs", "03/02/2026", 200.00, "Ica, Perú", 
                "Aventura en el oasis del desierto con sandboarding"));

        // Inicialmente mostrar todos los tours
        filteredTours.clear();
        filteredTours.addAll(allTours);
        toursAdapter.notifyDataSetChanged();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Aplicar filtros recibidos desde cliente_tour_filtros
            String startDate = data.getStringExtra("start_date");
            String endDate = data.getStringExtra("end_date");
            double minPrice = data.getDoubleExtra("min_price", 0);
            double maxPrice = data.getDoubleExtra("max_price", Double.MAX_VALUE);
            String language = data.getStringExtra("language");
            
            applyFilters(startDate, endDate, minPrice, maxPrice, language);
        }
    }

    private void applyFilters(String startDate, String endDate, double minPrice, double maxPrice, String language) {
        filteredTours.clear();
        
        for (Cliente_Tour tour : allTours) {
            boolean matchesPrice = tour.getPrice() >= minPrice && tour.getPrice() <= maxPrice;
            // Por simplicidad, solo filtramos por precio por ahora
            // Se pueden agregar más filtros según necesidad
            
            if (matchesPrice) {
                filteredTours.add(tour);
            }
        }
        
        toursAdapter.notifyDataSetChanged();
    }
}
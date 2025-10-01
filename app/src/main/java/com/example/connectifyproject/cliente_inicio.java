package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.connectifyproject.models.cliente_test_tour;
import com.example.connectifyproject.models.cliente_test_data_generator;
import com.example.connectifyproject.adapters.cliente_test_tour_adapter;
import java.util.List;

/**
 * Actividad principal para Cliente
 * Pantalla de inicio donde el cliente puede ver tours disponibles y gestionar sus reservas
 */
public class cliente_inicio extends AppCompatActivity {
    
    // Datos hardcodeados del tour activo
    private static final String TOUR_TITLE = "Tour histórico por Lima";
    private static final String TOUR_COMPANY = "Lima Tours";
    private static final String TOUR_DURATION = "Duración: 5 hrs 30 min. Fecha: 23/09/2025.";
    private static final int TOUR_PROGRESS = 10; // 10% completado - Estado inicial
    
    // Views
    private TextView tvTourTitle;
    private TextView tvTourCompany;
    private TextView tvTourDuration;
    private TextView tvInicio;
    private TextView tvEnCurso;
    private TextView tvFin;
    private View circleInicio;
    private View circleEnCurso;
    private View circleFin;
    private View progressLineActive;
    private MaterialCardView cardTourActivo;
    private ImageButton btnNotifications;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvToursRecientes;
    private RecyclerView rvToursCercanos;
    
    // Adapters para los RecyclerViews
    private cliente_test_tour_adapter adapterToursRecientes;
    private cliente_test_tour_adapter adapterToursCercanos;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_inicio);
        
        initViews();
        setupToolbar();
        setupTourData();
        setupRecyclerViews();
        setupBottomNavigation();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Inicio" esté seleccionado cuando regresamos a esta actividad
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_inicio);
        }
    }
    
    private void initViews() {
        tvTourTitle = findViewById(R.id.tv_tour_title);
        tvTourCompany = findViewById(R.id.tv_tour_company);
        tvTourDuration = findViewById(R.id.tv_tour_duration);
        tvInicio = findViewById(R.id.tv_inicio);
        tvEnCurso = findViewById(R.id.tv_en_curso);
        tvFin = findViewById(R.id.tv_fin);
        circleInicio = findViewById(R.id.circle_inicio);
        circleEnCurso = findViewById(R.id.circle_en_curso);
        circleFin = findViewById(R.id.circle_fin);
        progressLineActive = findViewById(R.id.progress_line_active);
        cardTourActivo = findViewById(R.id.card_tour_activo);
        btnNotifications = findViewById(R.id.btn_notifications);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        rvToursRecientes = findViewById(R.id.rv_tours_recientes);
        rvToursCercanos = findViewById(R.id.rv_tours_cercanos);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    
    private void setupTourData() {
        // Cargar datos hardcodeados del tour
        tvTourTitle.setText(TOUR_TITLE);
        tvTourCompany.setText(TOUR_COMPANY);
        tvTourDuration.setText(TOUR_DURATION);
        
        // Configurar el estado del progreso (simulando que está "En curso")
        setupProgressState();
    }
    
    private void setupProgressState() {
        // Estado actual: Inicio (solo el círculo "Inicio" está activo, sin línea de progreso)
        tvInicio.setTextColor(getResources().getColor(R.color.cliente_progress_active, null));
        tvEnCurso.setTextColor(getResources().getColor(R.color.cliente_progress_inactive, null));
        tvFin.setTextColor(getResources().getColor(R.color.cliente_progress_inactive, null));
        
        // En estado inicial: solo círculo activo, sin línea de progreso
        // La línea activa está oculta (android:visibility="gone" en XML)
    }
    
    private void setupRecyclerViews() {
        // Configurar RecyclerView para tours recientes
        LinearLayoutManager layoutManagerRecientes = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvToursRecientes.setLayoutManager(layoutManagerRecientes);
        
        // Obtener datos de tours recientes y configurar adapter
        List<cliente_test_tour> toursRecientes = cliente_test_data_generator.generarToursRecientes();
        adapterToursRecientes = new cliente_test_tour_adapter(this, toursRecientes);
        adapterToursRecientes.setOnTourClickListener(tour -> {
            // TODO: Navegar a detalles del tour
            Toast.makeText(this, "Tour: " + tour.getTitulo(), Toast.LENGTH_SHORT).show();
        });
        rvToursRecientes.setAdapter(adapterToursRecientes);
        
        // Configurar RecyclerView para tours cercanos
        LinearLayoutManager layoutManagerCercanos = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvToursCercanos.setLayoutManager(layoutManagerCercanos);
        
        // Obtener datos de tours cercanos y configurar adapter
        List<cliente_test_tour> toursCercanos = cliente_test_data_generator.generarToursCercanos();
        adapterToursCercanos = new cliente_test_tour_adapter(this, toursCercanos);
        adapterToursCercanos.setOnTourClickListener(tour -> {
            // TODO: Navegar a detalles del tour
            Toast.makeText(this, "Tour cercano: " + tour.getTitulo(), Toast.LENGTH_SHORT).show();
        });
        rvToursCercanos.setAdapter(adapterToursCercanos);
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_inicio) {
                // Ya estamos en inicio
                return true;
            } else if (itemId == R.id.nav_reservas) {
                Intent intent = new Intent(this, cliente_reservas.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
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
        
        // Seleccionar "Inicio" por defecto
        bottomNavigation.setSelectedItemId(R.id.nav_inicio);
    }
    
    private void setupClickListeners() {
        // Hacer clickeable todo el card del tour activo
        cardTourActivo.setOnClickListener(v -> {
            // TODO: Redirigir a detalles del tour activo
            Toast.makeText(this, "Próximamente: Detalles del tour", Toast.LENGTH_SHORT).show();
        });
        
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_inicio");
            startActivity(intent);
        });
    }
    

}
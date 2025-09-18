package com.example.connectifyproject;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * Actividad principal para Cliente
 * Pantalla de inicio donde el cliente puede ver tours disponibles y gestionar sus reservas
 */
public class cliente_inicio extends AppCompatActivity implements cliente_fragment_menu.OnMenuItemSelectedListener {
    
    // Datos hardcodeados del tour activo
    private static final String TOUR_TITLE = "Tour histórico por Lima";
    private static final String TOUR_COMPANY = "Lima Tours";
    private static final String TOUR_DURATION = "Duración: 5 hrs 30 min. Fecha: 23/09/2025.";
    private static final int TOUR_PROGRESS = 30; // 30% completado
    
    // Views
    private TextView tvTourTitle;
    private TextView tvTourCompany;
    private TextView tvTourDuration;
    private LinearProgressIndicator progressTour;
    private MaterialButton btnVerMas;
    private ImageButton btnNotifications;
    private cliente_fragment_menu menuFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_inicio);
        
        initViews();
        setupToolbar();
        setupTourData();
        setupMenuFragment();
        setupClickListeners();
    }
    
    private void initViews() {
        tvTourTitle = findViewById(R.id.tv_tour_title);
        tvTourCompany = findViewById(R.id.tv_tour_company);
        tvTourDuration = findViewById(R.id.tv_tour_duration);
        progressTour = findViewById(R.id.progress_tour);
        btnVerMas = findViewById(R.id.btn_ver_mas);
        btnNotifications = findViewById(R.id.btn_notifications);
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
        progressTour.setProgress(TOUR_PROGRESS);
    }
    
    private void setupMenuFragment() {
        menuFragment = new cliente_fragment_menu();
        menuFragment.setOnMenuItemSelectedListener(this);
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_menu_container, menuFragment);
        transaction.commit();
        
        // Seleccionar "Inicio" por defecto
        menuFragment.setSelectedItem(R.id.nav_inicio);
    }
    
    private void setupClickListeners() {
        btnVerMas.setOnClickListener(v -> {
            // TODO: Redirigir a detalles del tour
            Toast.makeText(this, "Próximamente: Detalles del tour", Toast.LENGTH_SHORT).show();
        });
        
        btnNotifications.setOnClickListener(v -> {
            // TODO: Mostrar notificaciones
            Toast.makeText(this, "Próximamente: Notificaciones", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public boolean onMenuItemSelected(int itemId) {
        if (itemId == R.id.nav_inicio) {
            // Ya estamos en inicio
            return true;
        } else if (itemId == R.id.nav_reservas) {
            // TODO: Navegar a reservas
            Toast.makeText(this, "Próximamente: Reservas", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.nav_tours) {
            // TODO: Navegar a tours disponibles
            Toast.makeText(this, "Próximamente: Tours disponibles", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.nav_chat) {
            // TODO: Navegar a chat
            Toast.makeText(this, "Próximamente: Chat", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.nav_perfil) {
            // TODO: Navegar a perfil
            Toast.makeText(this, "Próximamente: Perfil", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}
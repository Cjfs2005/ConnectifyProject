package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.button.MaterialButton;

/**
 * Actividad principal para Cliente
 * Pantalla de inicio donde el cliente puede ver tours disponibles y gestionar sus reservas
 */
public class cliente_inicio extends AppCompatActivity implements cliente_fragment_menu.OnMenuItemSelectedListener {
    
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
        tvInicio = findViewById(R.id.tv_inicio);
        tvEnCurso = findViewById(R.id.tv_en_curso);
        tvFin = findViewById(R.id.tv_fin);
        circleInicio = findViewById(R.id.circle_inicio);
        circleEnCurso = findViewById(R.id.circle_en_curso);
        circleFin = findViewById(R.id.circle_fin);
        progressLineActive = findViewById(R.id.progress_line_active);
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
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_inicio");
            startActivity(intent);
        });
    }
    
    @Override
    public boolean onMenuItemSelected(int itemId) {
        if (itemId == R.id.nav_inicio) {
            // Ya estamos en inicio
            return true;
        } else if (itemId == R.id.nav_reservas) {
            Intent intent = new Intent(this, cliente_reservas.class);
            startActivity(intent);
            finish();
            return true;
        } else if (itemId == R.id.nav_tours) {
            Intent intent = new Intent(this, cliente_tours.class);
            startActivity(intent);
            finish();
            return true;
        } else if (itemId == R.id.nav_chat) {
            // Navegar a chat
            Intent intent = new Intent(this, cliente_chat_list.class);
            startActivity(intent);
            finish();
            return true;
        } else if (itemId == R.id.nav_perfil) {
            Intent intent = new Intent(this, cliente_perfil.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }
}
package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.GuiaCheckInBinding;
import com.example.connectifyproject.model.GuiaClient;
import com.example.connectifyproject.services.TourFirebaseService;
import com.example.connectifyproject.ui.guia.GuiaClientAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class guia_check_in extends AppCompatActivity {
    private GuiaCheckInBinding binding;
    private GuiaClientAdapter adapter;
    private List<GuiaClient> clients;
    
    // 🚀 FIREBASE SERVICE PARA MANEJAR ESTADOS
    private TourFirebaseService tourFirebaseService;
    private String tourId;
    private String tourName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaCheckInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 🔧 INICIALIZAR FIREBASE SERVICE
        tourFirebaseService = new TourFirebaseService();
        
        // 📋 OBTENER DATOS DEL TOUR DESDE INTENT
        tourId = getIntent().getStringExtra("tour_id");
        tourName = getIntent().getStringExtra("tour_name");
        int participantsCount = getIntent().getIntExtra("participants_count", 0);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Check-in: " + (tourName != null ? tourName : "Tour"));
        }

        // Hardcoded clients with rating = 0
        clients = new ArrayList<>();
        clients.add(new GuiaClient("Ana Bonino", "AFR456", "Realizó Check-in", "23-06-25 02:30 pm", "961928802", true, false, 0));
        clients.add(new GuiaClient("Ana Bonino", "AFR456", "Check-in Pendiente", "", "961928802", false, false, 0));
        clients.add(new GuiaClient("Ana Bonino", "AFR456", "Check-in Pendiente", "", "961928802", false, false, 0));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuiaClientAdapter(this, clients);
        binding.recyclerView.setAdapter(adapter);

        binding.confirmButton.setOnClickListener(v -> {
            Toast.makeText(this, "Check-in confirmado (simulado)", Toast.LENGTH_SHORT).show();
        });

        // ▶️ BOTÓN EMPEZAR TOUR - CAMBIAR ESTADO A "EN_CURSO"
        binding.startTourButton.setOnClickListener(v -> {
            empezarTour();
        });

        // Navbar eliminado - pantalla secundaria
        /*
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_tours);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class));
                return true;
            } else if (id == R.id.nav_ofertas) {
                startActivity(new Intent(this, guia_tours_ofertas.class));
                return true;
            } else if (id == R.id.nav_tours) {
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, guia_perfil.class)); // Placeholder, renombrado
                return true;
            }
            return false;
        });
        */
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * ▶️ EMPEZAR TOUR - CAMBIAR ESTADO DE "PROGRAMADO" A "EN_CURSO"
     */
    private void empezarTour() {
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "❌ Error: ID de tour no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Deshabilitar botón para evitar clicks múltiples
        binding.startTourButton.setEnabled(false);
        binding.startTourButton.setText("Iniciando...");
        
        // Usar método específico de Firebase para iniciar tour
        tourFirebaseService.iniciarTour(tourId, new TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_check_in.this, 
                        "🚀 ¡Tour iniciado exitosamente!", Toast.LENGTH_LONG).show();
                    
                    // Regresar a tours asignados para ver el nuevo estado
                    Intent intent = new Intent(guia_check_in.this, guia_assigned_tours.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_check_in.this, 
                        "❌ Error iniciando tour: " + error, Toast.LENGTH_LONG).show();
                    
                    // Restaurar botón
                    binding.startTourButton.setEnabled(true);
                    binding.startTourButton.setText("Empezar Tour");
                });
            }
        });
    }
}
package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.GuiaCheckOutBinding;
import com.example.connectifyproject.model.GuiaClient;
import com.example.connectifyproject.services.TourFirebaseService;
import com.example.connectifyproject.ui.guia.GuiaClientAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class guia_check_out extends AppCompatActivity {
    private GuiaCheckOutBinding binding;
    private GuiaClientAdapter adapter;
    private List<GuiaClient> clients;
    private TourFirebaseService tourFirebaseService;
    private String tourId;
    private String tourName;
    private int numeroParticipantes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaCheckOutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Inicializar el servicio Firebase
        tourFirebaseService = new TourFirebaseService();
        
        // Obtener datos del tour del Intent
        Intent intent = getIntent();
        tourId = intent.getStringExtra("tour_id");
        tourName = intent.getStringExtra("tour_name");
        numeroParticipantes = intent.getIntExtra("participants_count", 0); // Corregir el nombre del parámetro

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Check-out");
        }

        // Verificar datos del tour
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tour no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar participantes del tour desde Firebase
        cargarParticipantesTour();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        binding.confirmButton.setOnClickListener(v -> {
            iniciarEscaneoCheckOut();
        });

        // Add Terminar Tour button
        binding.endTourButton.setOnClickListener(v -> {
            terminarTour();
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
    
    private void terminarTour() {
        if (tourId != null) {
            tourFirebaseService.terminarTour(tourId, new TourFirebaseService.OperationCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(guia_check_out.this, "Tour completado exitosamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(guia_check_out.this, guia_assigned_tours.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(guia_check_out.this, "Error al completar tour: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Error: No se encontró ID del tour", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarParticipantesTour() {
        // Aquí cargaremos los participantes reales del tour desde Firebase
        // Por ahora usamos datos de ejemplo hasta implementar la carga real
        clients = new ArrayList<>();
        clients.add(new GuiaClient("Cargando participantes...", "", "", "", "", false, false, 0));
        
        adapter = new GuiaClientAdapter(this, clients);
        binding.recyclerView.setAdapter(adapter);
        
        // TODO: Implementar carga real de participantes desde Firebase
        // tourFirebaseService.obtenerParticipantesTour(tourId, callback);
    }

    private void iniciarEscaneoCheckOut() {
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Error: Tour ID no válido", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (numeroParticipantes <= 0) {
            Toast.makeText(this, "Error: No hay participantes registrados", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Abrir la actividad de escaneo de QR en modo check-out
        Intent intent = new Intent(this, guia_scan_qr_participants.class);
        intent.putExtra("tourId", tourId);
        intent.putExtra("tourTitulo", tourName != null ? tourName : "Tour");
        intent.putExtra("numeroParticipantes", numeroParticipantes);
        intent.putExtra("scanMode", "check_out"); // Parámetro clave para check-out
        startActivity(intent);
    }
}
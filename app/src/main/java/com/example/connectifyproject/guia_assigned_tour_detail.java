package com.example.connectifyproject;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.databinding.GuiaAssignedTourDetailBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class guia_assigned_tour_detail extends AppCompatActivity {
    private GuiaAssignedTourDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaAssignedTourDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Tour");
        }

        Intent intent = getIntent();
        String tourName = intent.getStringExtra("tour_name");
        String tourEmpresa = intent.getStringExtra("tour_empresa");
        String tourInitio = intent.getStringExtra("tour_initio");
        String tourDuration = intent.getStringExtra("tour_duration");
        int tourClients = intent.getIntExtra("tour_clients", 0);
        String tourStatus = intent.getStringExtra("tour_status");
        String tourLanguages = intent.getStringExtra("tour_languages");
        String tourServices = intent.getStringExtra("tour_services");
        ArrayList<String> tourItinerario = intent.getStringArrayListExtra("tour_itinerario");

        // ✅ NUEVA ESTRUCTURA: Configurar UI mejorada
        setupTourHeader(tourName, tourEmpresa, tourInitio, tourDuration, tourClients, tourStatus);
        setupParticipantes(tourClients);
        setupItinerario(tourItinerario);
        setupTourInfo(tourLanguages, tourServices);
        setupActionButtons(tourStatus);

        // ✅ LÓGICA INTELIGENTE: Solo mostrar acciones si es relevante
        boolean shouldShowActions = shouldShowActionButtons(tourStatus, tourInitio);
        binding.actionsCard.setVisibility(shouldShowActions ? View.VISIBLE : View.GONE);

        setupButtonClickListeners(tourName, tourStatus, tourItinerario, tourClients);
    }

    /**
     * ✅ HEADER: Configurar información principal del tour
     */
    private void setupTourHeader(String tourName, String tourEmpresa, String tourInitio, 
                                String tourDuration, int tourClients, String tourStatus) {
        binding.tourName.setText(tourName);
        binding.empresaBadge.setText(tourEmpresa);
        binding.tourInitio.setText(tourInitio);
        binding.tourDuration.setText(tourDuration);
        binding.tourClients.setText(tourClients + " personas");
        
        // Pago al guía simulado (en app real vendría del intent)
        binding.pagoGuiaAmount.setText("S/. 85");
        
        // Estado del tour con color
        binding.tourStatusBadge.setText(formatearEstado(tourStatus));
        binding.tourStatusBadge.setBackgroundColor(getStatusColor(tourStatus));
    }

    /**
     * ✅ PARTICIPANTES: Mostrar lista simulada de participantes
     */
    private void setupParticipantes(int numParticipantes) {
        LinearLayout container = binding.participantesContainer;
        container.removeAllViews();
        
        // Participantes simulados para demo
        List<String> participantesDemo = Arrays.asList(
            "Ana Lucía Rodriguez - DNI: 70123456",
            "Carlos Miguel Torres - Pasaporte: ARG123456789", 
            "Sophie Chen - Pasaporte: USA987654321"
        );
        
        for (int i = 0; i < Math.min(numParticipantes, participantesDemo.size()); i++) {
            TextView participanteView = new TextView(this);
            participanteView.setText("👤 " + participantesDemo.get(i));
            participanteView.setTextSize(14);
            participanteView.setTextColor(Color.parseColor("#212121"));
            participanteView.setPadding(0, 8, 0, 8);
            container.addView(participanteView);
        }
    }

    /**
     * ✅ ITINERARIO: Mostrar puntos del tour dinámicamente
     */
    private void setupItinerario(ArrayList<String> tourItinerario) {
        LinearLayout container = binding.itinerarioContainer;
        container.removeAllViews();
        
        if (tourItinerario != null && !tourItinerario.isEmpty()) {
            for (int i = 0; i < tourItinerario.size(); i++) {
                TextView itinerarioView = new TextView(this);
                itinerarioView.setText("📍 " + (i + 1) + ". " + tourItinerario.get(i));
                itinerarioView.setTextSize(14);
                itinerarioView.setTextColor(Color.parseColor("#212121"));
                itinerarioView.setPadding(0, 8, 0, 8);
                container.addView(itinerarioView);
            }
        } else {
            TextView emptyView = new TextView(this);
            emptyView.setText("📍 Itinerario no disponible");
            emptyView.setTextSize(14);
            emptyView.setTextColor(Color.parseColor("#757575"));
            container.addView(emptyView);
        }
    }

    /**
     * ✅ INFO: Configurar información adicional del tour
     */
    private void setupTourInfo(String tourLanguages, String tourServices) {
        binding.tourLanguages.setText("🌐 Idiomas: " + (tourLanguages != null ? tourLanguages : "No especificado"));
        binding.tourServices.setText("🎁 " + (tourServices != null ? tourServices : "Servicios incluidos"));
    }

    /**
     * ✅ BOTONES: Configurar listeners para acciones
     */
    private void setupButtonClickListeners(String tourName, String tourStatus, 
                                         ArrayList<String> tourItinerario, int tourClients) {
        binding.checkInButton.setOnClickListener(v -> {
            startActivity(new Intent(this, guia_check_in.class));
            Toast.makeText(this, "Check-in iniciado", Toast.LENGTH_SHORT).show();
        });

        binding.mapButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(this, guia_tour_map.class);
            mapIntent.putExtra("tour_name", tourName);
            mapIntent.putExtra("tour_status", tourStatus);
            mapIntent.putStringArrayListExtra("tour_itinerario", tourItinerario);
            mapIntent.putExtra("tour_clients", tourClients);
            startActivity(mapIntent);
        });

        binding.checkOutButton.setOnClickListener(v -> {
            startActivity(new Intent(this, guia_check_out.class));
            Toast.makeText(this, "Check-out iniciado", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * ✅ ESTADOS: Formatear estado para mostrar en UI
     */
    private String formatearEstado(String estado) {
        if (estado == null) return "PENDIENTE";
        
        switch (estado.toLowerCase()) {
            case "pendiente":
                return "PENDIENTE";
            case "check_in":
            case "check-in disponible":
                return "CHECK-IN DISPONIBLE";
            case "en_curso":
            case "en curso":
            case "en_progreso":
                return "EN CURSO";
            case "check_out":
            case "check-out disponible":
                return "CHECK-OUT DISPONIBLE";
            case "completado":
            case "finalizado":
                return "COMPLETADO";
            case "cancelado":
                return "CANCELADO";
            // Compatibilidad con estados antiguos
            case "programado":
                return "PROGRAMADO";
            case "confirmado":
                return "CONFIRMADO";
            default:
                return estado.toUpperCase();
        }
    }

    /**
     * ✅ COLORES: Obtener color según estado del tour
     */
    private int getStatusColor(String estado) {
        if (estado == null) return Color.parseColor("#757575");
        
        switch (estado.toLowerCase()) {
            case "pendiente":
                return Color.parseColor("#FF9800"); // Naranja para pendiente
            case "check_in":
            case "check-in disponible":
                return Color.parseColor("#03DAC6"); // Verde agua para check-in
            case "en_curso":
            case "en curso":
            case "en_progreso":
                return Color.parseColor("#4CAF50"); // Verde intenso para en curso
            case "check_out":
            case "check-out disponible":
                return Color.parseColor("#FF5722"); // Naranja rojizo para check-out
            case "completado":
            case "finalizado":
                return Color.parseColor("#9C27B0"); // Púrpura para completado
            case "cancelado":
                return Color.parseColor("#F44336"); // Rojo para cancelado
            // Compatibilidad con estados antiguos
            case "programado":
                return Color.parseColor("#2196F3"); // Azul para programado
            case "confirmado":
                return Color.parseColor("#2196F3"); // Azul para confirmado
            default:
                return Color.parseColor("#757575"); // Gris para otros estados
        }
    }

    /**
     * ✅ LÓGICA: Determinar si mostrar botones de acción
     */
    private boolean shouldShowActionButtons(String status, String fechaHora) {
        // Misma lógica que en el adapter pero simplificada para demo
        if (status != null && (status.equalsIgnoreCase("en curso") || 
                              status.equalsIgnoreCase("en_curso") ||
                              status.equalsIgnoreCase("en_progreso"))) {
            return true;
        }
        
        // Para tours programados, podríamos verificar la fecha pero 
        // por simplicidad en demo, solo mostramos para estado "en curso"
        return false;
    }

    private void setupActionButtons(String tourStatus) {
        // Esta función mantendrá la compatibilidad con código existente
        // pero la lógica real está en shouldShowActionButtons
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
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

import com.bumptech.glide.Glide;
import com.example.connectifyproject.databinding.GuiaAssignedTourDetailBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class guia_assigned_tour_detail extends AppCompatActivity {
    private GuiaAssignedTourDetailBinding binding;
    private FirebaseFirestore db;
    private String tourId;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

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

        // Inicializar Firebase y formatos de fecha
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Obtener tour ID del intent
        Intent intent = getIntent();
        tourId = intent.getStringExtra("tour_id");

        android.util.Log.d("GuiaAssignedTour", "onCreate - tourId: " + tourId);

        if (tourId != null && !tourId.isEmpty()) {
            loadTourDataFromFirebase();
        } else {
            android.util.Log.e("GuiaAssignedTour", "Error: ID del tour no encontrado o vacío");
            Toast.makeText(this, "Error: ID del tour no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Cargar datos del tour desde Firebase (colección tours_asignados)
     */
    private void loadTourDataFromFirebase() {
        android.util.Log.d("GuiaAssignedTour", "loadTourDataFromFirebase - Consultando tourId: " + tourId);
        
        db.collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                android.util.Log.d("GuiaAssignedTour", "loadTourDataFromFirebase - Success. Existe: " + documentSnapshot.exists());
                
                if (documentSnapshot.exists()) {
                    android.util.Log.d("GuiaAssignedTour", "Tour encontrado, llamando setupTourFromFirebase");
                    setupTourFromFirebase(documentSnapshot);
                } else {
                    android.util.Log.e("GuiaAssignedTour", "Tour no existe en Firebase");
                    Toast.makeText(this, "Tour no encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("GuiaAssignedTour", "Error al cargar tour: " + e.getMessage(), e);
                Toast.makeText(this, "Error al cargar tour: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    /**
     * Configurar UI con datos de Firebase
     */
    private void setupTourFromFirebase(DocumentSnapshot doc) {
        android.util.Log.d("GuiaAssignedTour", "setupTourFromFirebase - Iniciando configuración UI");
        
        // Datos básicos
        String titulo = doc.getString("titulo");
        String nombreEmpresa = doc.getString("nombreEmpresa");
        String descripcion = doc.getString("descripcion");
        String duracion = doc.getString("duracion");
        String horaInicio = doc.getString("horaInicio");
        String horaFin = doc.getString("horaFin");
        String estado = doc.getString("estado");
        Double pagoGuia = doc.getDouble("pagoGuia");
        
        android.util.Log.d("GuiaAssignedTour", "Datos cargados - Titulo: " + titulo + ", Estado: " + estado);
        
        // Fecha de realización
        Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
        String fechaFormateada = fechaRealizacion != null ? 
            dateFormat.format(fechaRealizacion.toDate()) : "Fecha no disponible";
        
        // Idiomas
        List<String> idiomasLista = (List<String>) doc.get("idiomasRequeridos");
        String idiomas = idiomasLista != null && !idiomasLista.isEmpty() ? 
            String.join(", ", idiomasLista) : "No especificado";
        
        // Servicios adicionales
        List<String> serviciosLista = (List<String>) doc.get("serviciosAdicionales");
        String servicios = serviciosLista != null && !serviciosLista.isEmpty() ? 
            String.join(", ", serviciosLista) : "No especificado";
        
        // Itinerario
        List<Map<String, Object>> itinerarioData = (List<Map<String, Object>>) doc.get("itinerario");
        ArrayList<String> itinerarioTexto = new ArrayList<>();
        if (itinerarioData != null) {
            for (Map<String, Object> punto : itinerarioData) {
                String nombrePunto = (String) punto.get("nombre");
                String direccion = (String) punto.get("direccion");
                if (nombrePunto != null) {
                    itinerarioTexto.add(nombrePunto + (direccion != null ? " - " + direccion : ""));
                }
            }
        }
        
        // Participantes
        List<Map<String, Object>> participantesData = (List<Map<String, Object>>) doc.get("participantes");
        int numParticipantes = participantesData != null ? participantesData.size() : 0;
        
        // Configurar UI
        setupTourHeader(titulo, nombreEmpresa, fechaFormateada + " " + horaInicio, 
                       duracion + " horas", numParticipantes, estado, pagoGuia);
        setupParticipantes(participantesData);
        setupItinerario(itinerarioTexto);
        setupTourInfo(idiomas, servicios, descripcion);
        setupActionButtons(estado);
        
        // Lógica de acciones
        boolean shouldShowActions = shouldShowActionButtons(estado, fechaFormateada);
        binding.actionsCard.setVisibility(shouldShowActions ? View.VISIBLE : View.GONE);
        
        setupButtonClickListeners(titulo, estado, itinerarioTexto, numParticipantes);
    }

    /**
     * ✅ HEADER: Configurar información principal del tour
     */
    private void setupTourHeader(String tourName, String tourEmpresa, String tourInitio, 
                                String tourDuration, int tourClients, String tourStatus, Double pagoGuia) {
        binding.tourName.setText(tourName != null ? tourName : "Tour sin título");
        binding.empresaBadge.setText(tourEmpresa != null ? tourEmpresa : "Empresa");
        binding.tourInitio.setText(tourInitio != null ? tourInitio : "Fecha no disponible");
        binding.tourDuration.setText(tourDuration != null ? tourDuration : "Duración");
        binding.tourClients.setText(tourClients + " personas");
        
        // Pago al guía desde Firebase
        if (pagoGuia != null) {
            binding.pagoGuiaAmount.setText("S/. " + String.format(Locale.getDefault(), "%.0f", pagoGuia));
        } else {
            binding.pagoGuiaAmount.setText("S/. 0");
        }
        
        // Estado del tour con color
        binding.tourStatusBadge.setText(formatearEstado(tourStatus));
        binding.tourStatusBadge.setBackgroundColor(getStatusColor(tourStatus));
    }

    /**
     * ✅ PARTICIPANTES: Mostrar lista de participantes desde Firebase
     */
    private void setupParticipantes(List<Map<String, Object>> participantesData) {
        LinearLayout container = binding.participantesContainer;
        container.removeAllViews();
        
        if (participantesData != null && !participantesData.isEmpty()) {
            for (Map<String, Object> participante : participantesData) {
                String nombre = (String) participante.get("nombre");
                String tipoDoc = (String) participante.get("tipoDocumento");
                String numeroDoc = (String) participante.get("numeroDocumento");
                
                TextView participanteView = new TextView(this);
                String textoCompleto = "👤 " + (nombre != null ? nombre : "Participante");
                if (tipoDoc != null && numeroDoc != null) {
                    textoCompleto += " - " + tipoDoc + ": " + numeroDoc;
                }
                participanteView.setText(textoCompleto);
                participanteView.setTextSize(14);
                participanteView.setTextColor(Color.parseColor("#212121"));
                participanteView.setPadding(0, 8, 0, 8);
                container.addView(participanteView);
            }
        } else {
            TextView emptyView = new TextView(this);
            emptyView.setText("👤 No hay participantes registrados aún");
            emptyView.setTextSize(14);
            emptyView.setTextColor(Color.parseColor("#757575"));
            container.addView(emptyView);
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
    private void setupTourInfo(String tourLanguages, String tourServices, String descripcion) {
        binding.tourLanguages.setText("🌐 Idiomas: " + (tourLanguages != null ? tourLanguages : "No especificado"));
        binding.tourServices.setText("🎁 Servicios: " + (tourServices != null && !tourServices.isEmpty() ? tourServices : "No especificado"));
        
        // Agregar descripción si existe el campo en el layout
        // (Asumiendo que hay un TextView para descripción en el layout)
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
package com.example.connectifyproject;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 📍 PANTALLA DE PROGRESO DEL TOUR
 * 
 * Muestra el itinerario del tour con checkboxes para que el guía marque
 * cada punto visitado. Incluye validación geográfica opcional.
 */
public class guia_tour_progress extends AppCompatActivity {
    
    private static final String TAG = "GuiaTourProgress";
    private static final double RADIUS_VALIDATION_METERS = 100.0; // 100 metros de tolerancia
    
    private String tourId;
    private String tourTitulo;
    
    private TextView tvTitulo;
    private TextView tvProgreso;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private Button btnFinalizarTour;
    
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private ListenerRegistration snapshotListener;
    
    private ItinerarioAdapter adapter;
    private List<PuntoItinerario> puntosItinerario = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guia_tour_progress_view);
        
        // Obtener datos
        tourId = getIntent().getStringExtra("tourId");
        tourTitulo = getIntent().getStringExtra("tourTitulo");
        
        if (tourId == null) {
            Toast.makeText(this, "Error: ID de tour no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Inicializar
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Inicializar vistas
        initViews();
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Cargar datos del tour
        cargarDatosTour();
        
        // Configurar botón finalizar
        btnFinalizarTour.setOnClickListener(v -> mostrarDialogoFinalizarTour());
    }
    
    private void initViews() {
        tvTitulo = findViewById(R.id.tv_titulo);
        tvProgreso = findViewById(R.id.tv_progreso);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_itinerario);
        btnFinalizarTour = findViewById(R.id.btn_finalizar_tour);
    }
    
    private void setupRecyclerView() {
        adapter = new ItinerarioAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    /**
     * Cargar datos del tour desde Firebase
     */
    private void cargarDatosTour() {
        snapshotListener = db.collection("tours_asignados")
            .document(tourId)
            .addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error al cargar tour", error);
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    procesarDatosTour(documentSnapshot);
                }
            });
    }
    
    /**
     * Procesar datos del tour
     */
    private void procesarDatosTour(DocumentSnapshot doc) {
        String titulo = doc.getString("titulo");
        if (titulo != null) {
            tvTitulo.setText(titulo);
        }
        
        List<Map<String, Object>> itinerario = 
            (List<Map<String, Object>>) doc.get("itinerario");
        
        if (itinerario != null) {
            puntosItinerario.clear();
            
            for (int i = 0; i < itinerario.size(); i++) {
                Map<String, Object> punto = itinerario.get(i);
                
                String lugar = (String) punto.get("lugar");
                String horaEstimada = (String) punto.get("horaEstimada");
                String actividad = (String) punto.get("actividad");
                Double latitud = (Double) punto.get("latitud");
                Double longitud = (Double) punto.get("longitud");
                Boolean completado = (Boolean) punto.get("completado");
                
                PuntoItinerario puntoObj = new PuntoItinerario(
                    i,
                    lugar != null ? lugar : "Punto " + (i + 1),
                    horaEstimada != null ? horaEstimada : "",
                    actividad != null ? actividad : "",
                    latitud != null ? latitud : 0.0,
                    longitud != null ? longitud : 0.0,
                    completado != null ? completado : false
                );
                
                puntosItinerario.add(puntoObj);
            }
            
            adapter.notifyDataSetChanged();
            actualizarProgreso();
        }
    }
    
    /**
     * Actualizar barra de progreso
     */
    private void actualizarProgreso() {
        int completados = 0;
        for (PuntoItinerario punto : puntosItinerario) {
            if (punto.completado) {
                completados++;
            }
        }
        
        int total = puntosItinerario.size();
        progressBar.setMax(total);
        progressBar.setProgress(completados);
        tvProgreso.setText(completados + " de " + total + " puntos visitados");
        
        // Habilitar botón finalizar si todos los puntos están completados
        if (completados == total && total > 0) {
            btnFinalizarTour.setEnabled(true);
            btnFinalizarTour.setAlpha(1.0f);
        } else {
            btnFinalizarTour.setEnabled(false);
            btnFinalizarTour.setAlpha(0.5f);
        }
    }
    
    /**
     * Marcar punto como visitado
     */
    private void marcarPuntoVisitado(int indice, boolean marcar) {
        if (indice < 0 || indice >= puntosItinerario.size()) {
            return;
        }
        
        PuntoItinerario punto = puntosItinerario.get(indice);
        
        // Validar ubicación si está habilitado
        if (marcar && punto.latitud != 0.0 && punto.longitud != 0.0) {
            validarUbicacionYMarcar(indice, punto);
        } else {
            // Marcar sin validación
            actualizarPuntoEnFirebase(indice, marcar);
        }
    }
    
    /**
     * Validar ubicación actual vs coordenadas del punto
     */
    private void validarUbicacionYMarcar(int indice, PuntoItinerario punto) {
        try {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double distancia = calcularDistancia(
                            location.getLatitude(), location.getLongitude(),
                            punto.latitud, punto.longitud
                        );
                        
                        Log.d(TAG, "Distancia al punto: " + distancia + " metros");
                        
                        if (distancia <= RADIUS_VALIDATION_METERS) {
                            // Dentro del rango, marcar
                            actualizarPuntoEnFirebase(indice, true);
                            Toast.makeText(this, "✓ Punto visitado confirmado", Toast.LENGTH_SHORT).show();
                        } else {
                            // Fuera del rango, preguntar si marcar de todas formas
                            new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Ubicación alejada")
                                .setMessage("Estás a " + Math.round(distancia) + " metros del punto. ¿Deseas marcarlo de todas formas?")
                                .setPositiveButton("Sí, marcar", (dialog, which) -> {
                                    actualizarPuntoEnFirebase(indice, true);
                                })
                                .setNegativeButton("Cancelar", (dialog, which) -> {
                                    // Desmarcar checkbox
                                    adapter.notifyItemChanged(indice);
                                })
                                .show();
                        }
                    } else {
                        // No hay ubicación disponible, marcar sin validar
                        Toast.makeText(this, "No se puede obtener ubicación. Marcando sin validar...", Toast.LENGTH_SHORT).show();
                        actualizarPuntoEnFirebase(indice, true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener ubicación", e);
                    Toast.makeText(this, "Error al validar ubicación. Marcando sin validar...", Toast.LENGTH_SHORT).show();
                    actualizarPuntoEnFirebase(indice, true);
                });
        } catch (SecurityException e) {
            Log.e(TAG, "Permisos de ubicación no otorgados", e);
            actualizarPuntoEnFirebase(indice, true);
        }
    }
    
    /**
     * Calcular distancia entre dos puntos GPS (Haversine)
     */
    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radio de la Tierra en metros
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distancia en metros
    }
    
    /**
     * Actualizar punto en Firebase
     */
    private void actualizarPuntoEnFirebase(int indice, boolean marcar) {
        db.collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> itinerario = 
                        (List<Map<String, Object>>) documentSnapshot.get("itinerario");
                    
                    if (itinerario != null && indice < itinerario.size()) {
                        Map<String, Object> punto = itinerario.get(indice);
                        punto.put("completado", marcar);
                        
                        if (marcar) {
                            punto.put("horaLlegada", Timestamp.now());
                        } else {
                            punto.put("horaLlegada", null);
                        }
                        
                        // Guardar en Firebase
                        db.collection("tours_asignados")
                            .document(tourId)
                            .update("itinerario", itinerario)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Punto " + indice + " actualizado: " + marcar);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al actualizar punto", e);
                                Toast.makeText(this, "Error al actualizar punto", Toast.LENGTH_SHORT).show();
                            });
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar itinerario", e);
            });
    }
    
    /**
     * Mostrar diálogo de confirmación para finalizar tour
     */
    private void mostrarDialogoFinalizarTour() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Finalizar Tour")
            .setMessage("¿Has completado todos los puntos del itinerario? Se mostrará el código QR para que los participantes hagan check-out.")
            .setPositiveButton("Sí, finalizar", (dialog, which) -> {
                navegarACheckOut();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    /**
     * Navegar a pantalla de check-out con QR
     */
    private void navegarACheckOut() {
        Intent intent = new Intent(this, guia_show_qr_checkout.class);
        intent.putExtra("tourId", tourId);
        intent.putExtra("tourTitulo", tourTitulo);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (snapshotListener != null) {
            snapshotListener.remove();
        }
    }
    
    // ========== CLASES AUXILIARES ==========
    
    /**
     * Modelo de punto de itinerario
     */
    private static class PuntoItinerario {
        int indice;
        String lugar;
        String horaEstimada;
        String actividad;
        double latitud;
        double longitud;
        boolean completado;
        
        PuntoItinerario(int indice, String lugar, String horaEstimada, String actividad, 
                       double latitud, double longitud, boolean completado) {
            this.indice = indice;
            this.lugar = lugar;
            this.horaEstimada = horaEstimada;
            this.actividad = actividad;
            this.latitud = latitud;
            this.longitud = longitud;
            this.completado = completado;
        }
    }
    
    /**
     * Adapter para RecyclerView de itinerario
     */
    private class ItinerarioAdapter extends RecyclerView.Adapter<ItinerarioAdapter.ViewHolder> {
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_itinerario_checkpoint, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PuntoItinerario punto = puntosItinerario.get(position);
            
            holder.tvLugar.setText((position + 1) + ". " + punto.lugar);
            holder.tvHoraEstimada.setText(punto.horaEstimada);
            holder.tvActividad.setText(punto.actividad);
            holder.checkBox.setChecked(punto.completado);
            
            // Cambiar color según estado
            if (punto.completado) {
                holder.tvLugar.setTextColor(getColor(R.color.success_500));
                holder.checkBox.setEnabled(true);
            } else {
                holder.tvLugar.setTextColor(getColor(R.color.text_primary));
                holder.checkBox.setEnabled(true);
            }
            
            // Listener del checkbox
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) { // Solo si es interacción del usuario
                    marcarPuntoVisitado(position, isChecked);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return puntosItinerario.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            TextView tvLugar;
            TextView tvHoraEstimada;
            TextView tvActividad;
            
            ViewHolder(View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkbox_punto);
                tvLugar = itemView.findViewById(R.id.tv_lugar);
                tvHoraEstimada = itemView.findViewById(R.id.tv_hora_estimada);
                tvActividad = itemView.findViewById(R.id.tv_actividad);
            }
        }
    }
}

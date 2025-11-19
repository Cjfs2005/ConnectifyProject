package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.connectifyproject.models.Cliente_Tour;
import com.example.connectifyproject.adapters.Cliente_GalleryTourAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * Actividad principal para Cliente
 * Pantalla de inicio donde el cliente puede ver tours disponibles y gestionar sus reservas
 */
public class cliente_inicio extends AppCompatActivity {
    
    private static final String TAG = "ClienteInicio";
    
    // Datos hardcodeados del tour activo (será reemplazado con Firebase)
    private static final String TOUR_TITLE = "Tour histórico por Lima";
    private static final String TOUR_COMPANY = "Lima Tours";
    private static final String TOUR_DURATION = "Duración: 5 hrs 30 min. Fecha: 23/09/2025.";
    private static final int TOUR_PROGRESS = 10; // 10% completado - Estado inicial
    
    private FirebaseFirestore db;
    
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
    private Cliente_GalleryTourAdapter adapterToursRecientes;
    private Cliente_GalleryTourAdapter adapterToursCercanos;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_inicio);
        
        db = FirebaseFirestore.getInstance();
        
        initViews();
        setupToolbar();
        setupTourData();
        setupRecyclerViews();
        setupBottomNavigation();
        loadToursFromFirebase();
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
        
        // Inicializar con lista vacía - se llenará desde Firebase
        adapterToursRecientes = new Cliente_GalleryTourAdapter(this, new ArrayList<>());
        adapterToursRecientes.setOnTourClickListener(tour -> {
            // Navegar al detalle del tour pasando datos individuales
            Intent intent = new Intent(this, cliente_tour_detalle.class);
            intent.putExtra("tour_id", tour.getId());
            intent.putExtra("tour_title", tour.getTitulo());
            intent.putExtra("tour_description", tour.getDescription());
            intent.putExtra("tour_company", tour.getCompanyName());
            intent.putExtra("tour_location", tour.getUbicacion());
            intent.putExtra("tour_price", tour.getPrecio());
            intent.putExtra("tour_duration", tour.getDuracion());
            intent.putExtra("tour_date", tour.getDate());
            intent.putExtra("tour_start_time", tour.getStartTime());
            intent.putExtra("tour_end_time", tour.getEndTime());
            intent.putExtra("tour_image_url", tour.getImageUrl());
            intent.putExtra("oferta_tour_id", tour.getOfertaTourId());
            intent.putExtra("empresa_id", tour.getEmpresaId());
            if (tour.getIdiomasRequeridos() != null) {
                intent.putStringArrayListExtra("idiomas", new ArrayList<>(tour.getIdiomasRequeridos()));
            }
            startActivity(intent);
        });
        rvToursRecientes.setAdapter(adapterToursRecientes);
        
        // Configurar RecyclerView para tours cercanos
        LinearLayoutManager layoutManagerCercanos = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvToursCercanos.setLayoutManager(layoutManagerCercanos);
        
        // Inicializar con lista vacía - se llenará desde Firebase
        adapterToursCercanos = new Cliente_GalleryTourAdapter(this, new ArrayList<>());
        adapterToursCercanos.setOnTourClickListener(tour -> {
            // Navegar al detalle del tour pasando datos individuales
            Intent intent = new Intent(this, cliente_tour_detalle.class);
            intent.putExtra("tour_id", tour.getId());
            intent.putExtra("tour_title", tour.getTitulo());
            intent.putExtra("tour_description", tour.getDescription());
            intent.putExtra("tour_company", tour.getCompanyName());
            intent.putExtra("tour_location", tour.getUbicacion());
            intent.putExtra("tour_price", tour.getPrecio());
            intent.putExtra("tour_duration", tour.getDuracion());
            intent.putExtra("tour_date", tour.getDate());
            intent.putExtra("tour_start_time", tour.getStartTime());
            intent.putExtra("tour_end_time", tour.getEndTime());
            intent.putExtra("tour_image_url", tour.getImageUrl());
            intent.putExtra("oferta_tour_id", tour.getOfertaTourId());
            intent.putExtra("empresa_id", tour.getEmpresaId());
            if (tour.getIdiomasRequeridos() != null) {
                intent.putStringArrayListExtra("idiomas", new ArrayList<>(tour.getIdiomasRequeridos()));
            }
            startActivity(intent);
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
    
    // ========== MÉTODOS PARA GENERAR DATOS DE PRUEBA ==========
    
    /**
     * Genera una lista de tours recién agregados
     */
    private List<Cliente_Tour> generarToursRecientes() {
        List<Cliente_Tour> tours = new ArrayList<>();
        
        tours.add(new Cliente_Tour("tour_001", "City Tour Lima Centro Histórico",
            "Explora el corazón colonial de Lima visitando la Plaza de Armas, Catedral y Palacio de Gobierno.",
            "4 horas", 65.00, "Lima Centro", 4.6f, "Lima Tours"));
            
        tours.add(new Cliente_Tour("tour_002", "Barranco y Miraflores Tour",
            "Recorre los distritos bohemios y modernos de Lima con vistas al océano.",
            "3 horas", 55.00, "Barranco - Miraflores", 4.4f, "Lima Tours"));
            
        tours.add(new Cliente_Tour("tour_003", "Machu Picchu Full Day",
            "Visita la maravilla del mundo en un tour completo desde Cusco.",
            "16 horas", 250.00, "Machu Picchu", 4.9f, "Cusco Adventures"));
            
        tours.add(new Cliente_Tour("tour_004", "Valle Sagrado Adventure",
            "Aventura completa por Pisaq, Ollantaytambo y pueblos tradicionales.",
            "12 horas", 180.00, "Valle Sagrado", 4.7f, "Cusco Adventures"));
            
        tours.add(new Cliente_Tour("tour_005", "Cañón del Colca 2D/1N",
            "Observa el vuelo de los cóndores en uno de los cañones más profundos del mundo.",
            "2 días", 320.00, "Cañón del Colca", 4.5f, "Arequipa Explorer"));
            
        return tours;
    }
    
    /**
     * Genera una lista de tours cercanos a la ubicación
     */
    private List<Cliente_Tour> generarToursCercanos() {
        List<Cliente_Tour> tours = new ArrayList<>();
        
        tours.add(new Cliente_Tour("tour_006", "Circuito Mágico del Agua",
            "Espectáculo nocturno de fuentes danzantes con luces y música.",
            "2 horas", 35.00, "Parque de la Reserva", 4.3f, "Lima Tours"));
            
        tours.add(new Cliente_Tour("tour_007", "Museo Larco y Pueblos",
            "Visita al famoso museo y recorrido por pueblos tradicionales limeños.",
            "5 horas", 85.00, "Pueblo Libre", 4.6f, "Lima Tours"));
            
        tours.add(new Cliente_Tour("tour_008", "Callao Monumental Tour",
            "Descubre el arte urbano y la historia del primer puerto del Perú.",
            "3 horas", 45.00, "Callao", 4.2f, "Lima Tours"));
            
        tours.add(new Cliente_Tour("tour_009", "Islas Palomino - Leones Marinos",
            "Excursión marítima para nadar con leones marinos en su hábitat natural.",
            "6 horas", 120.00, "Islas Palomino", 4.7f, "Lima Tours"));
            
        return tours;
    }
    
    /**
     * NUEVOS MÉTODOS: Carga tours desde Firebase
     */
    private void loadToursFromFirebase() {
        Log.d(TAG, "🔄 Cargando tours desde Firebase...");
        
        db.collection("tours_asignados")
                .whereEqualTo("habilitado", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "✅ Tours encontrados: " + querySnapshot.size());
                    
                    List<Cliente_Tour> toursRecientes = new ArrayList<>();
                    List<Cliente_Tour> toursCercanos = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        processTourDocument(doc, toursRecientes, toursCercanos);
                    }
                    
                    // Actualizar RecyclerViews
                    updateRecyclerViews(toursRecientes, toursCercanos);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando tours", e);
                    Toast.makeText(this, "Error al cargar tours: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void processTourDocument(DocumentSnapshot doc, List<Cliente_Tour> toursRecientes, List<Cliente_Tour> toursCercanos) {
        try {
            Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
            if (!isTourAvailable(fechaRealizacion)) {
                return;
            }
            
            Cliente_Tour tour = new Cliente_Tour();
            tour.setId(doc.getId());
            tour.setTitle(doc.getString("titulo"));
            tour.setDescription(doc.getString("descripcion"));
            tour.setOfertaTourId(doc.getString("ofertaTourId"));
            tour.setEmpresaId(doc.getString("empresaId"));
            tour.setFechaRealizacion(fechaRealizacion);
            
            String duracion = doc.getString("duracion");
            tour.setDuration(duracion != null ? duracion + " horas" : "");
            
            Number precio = (Number) doc.get("precio");
            tour.setPrice(precio != null ? precio.doubleValue() : 0.0);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tour.setDate(sdf.format(fechaRealizacion.toDate()));
            
            String horaInicio = doc.getString("horaInicio");
            String horaFin = doc.getString("horaFin");
            tour.setStartTime(horaInicio != null ? horaInicio : "Por confirmar");
            tour.setEndTime(horaFin != null ? horaFin : "Por confirmar");
            
            List<Map<String, Object>> itinerario = (List<Map<String, Object>>) doc.get("itinerario");
            if (itinerario != null && !itinerario.isEmpty()) {
                String direccion = (String) itinerario.get(0).get("direccion");
                tour.setLocation(direccion != null ? direccion : "");
            }
            
            List<String> idiomas = (List<String>) doc.get("idiomasRequeridos");
            tour.setIdiomasRequeridos(idiomas);
            
            String nombreEmpresa = doc.getString("nombreEmpresa");
            tour.setCompanyName(nombreEmpresa != null ? nombreEmpresa : "Empresa");
            
            loadTourImage(tour);
            
            // Primeros 5 a recientes, resto a cercanos
            if (toursRecientes.size() < 5) {
                toursRecientes.add(tour);
            } else {
                toursCercanos.add(tour);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error procesando tour: " + doc.getId(), e);
        }
    }
    
    private boolean isTourAvailable(Timestamp fechaRealizacion) {
        if (fechaRealizacion == null) return false;
        
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar minDate = (Calendar) today.clone();
        minDate.add(Calendar.DAY_OF_MONTH, 1);
        
        Date tourDate = fechaRealizacion.toDate();
        return tourDate.compareTo(minDate.getTime()) >= 0;
    }
    
    private void loadTourImage(Cliente_Tour tour) {
        if (tour.getOfertaTourId() == null) return;
        
        db.collection("tours_ofertas")
                .document(tour.getOfertaTourId())
                .get()
                .addOnSuccessListener(doc -> {
                    String imagenUrl = doc.getString("imagenPrincipal");
                    if (imagenUrl != null && !imagenUrl.isEmpty()) {
                        tour.setImageUrl(imagenUrl);
                        if (adapterToursRecientes != null) adapterToursRecientes.notifyDataSetChanged();
                        if (adapterToursCercanos != null) adapterToursCercanos.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error cargando imagen", e));
    }
    
    private void updateRecyclerViews(List<Cliente_Tour> toursRecientes, List<Cliente_Tour> toursCercanos) {
        if (adapterToursRecientes != null && !toursRecientes.isEmpty()) {
            adapterToursRecientes = new Cliente_GalleryTourAdapter(this, toursRecientes);
            adapterToursRecientes.setOnTourClickListener(tour -> {
                Intent intent = new Intent(this, cliente_tour_detalle.class);
                intent.putExtra("tour_id", tour.getId());
                intent.putExtra("tour_title", tour.getTitulo());
                intent.putExtra("tour_description", tour.getDescription());
                intent.putExtra("tour_company", tour.getCompanyName());
                intent.putExtra("tour_location", tour.getUbicacion());
                intent.putExtra("tour_price", tour.getPrecio());
                intent.putExtra("tour_duration", tour.getDuracion());
                intent.putExtra("tour_date", tour.getDate());
                intent.putExtra("tour_start_time", tour.getStartTime());
                intent.putExtra("tour_end_time", tour.getEndTime());
                intent.putExtra("tour_image_url", tour.getImageUrl());
                intent.putExtra("oferta_tour_id", tour.getOfertaTourId());
                intent.putExtra("empresa_id", tour.getEmpresaId());
                if (tour.getIdiomasRequeridos() != null) {
                    intent.putStringArrayListExtra("idiomas", new ArrayList<>(tour.getIdiomasRequeridos()));
                }
                startActivity(intent);
            });
            rvToursRecientes.setAdapter(adapterToursRecientes);
        }
        
        if (adapterToursCercanos != null && !toursCercanos.isEmpty()) {
            adapterToursCercanos = new Cliente_GalleryTourAdapter(this, toursCercanos);
            adapterToursCercanos.setOnTourClickListener(tour -> {
                Intent intent = new Intent(this, cliente_tour_detalle.class);
                intent.putExtra("tour_id", tour.getId());
                intent.putExtra("tour_title", tour.getTitulo());
                intent.putExtra("tour_description", tour.getDescription());
                intent.putExtra("tour_company", tour.getCompanyName());
                intent.putExtra("tour_location", tour.getUbicacion());
                intent.putExtra("tour_price", tour.getPrecio());
                intent.putExtra("tour_duration", tour.getDuracion());
                intent.putExtra("tour_date", tour.getDate());
                intent.putExtra("tour_start_time", tour.getStartTime());
                intent.putExtra("tour_end_time", tour.getEndTime());
                intent.putExtra("tour_image_url", tour.getImageUrl());
                intent.putExtra("oferta_tour_id", tour.getOfertaTourId());
                intent.putExtra("empresa_id", tour.getEmpresaId());
                if (tour.getIdiomasRequeridos() != null) {
                    intent.putStringArrayListExtra("idiomas", new ArrayList<>(tour.getIdiomasRequeridos()));
                }
                startActivity(intent);
            });
            rvToursCercanos.setAdapter(adapterToursCercanos);
        }
        
        Log.d(TAG, "📊 RecyclerViews actualizados - Recientes: " + toursRecientes.size() + ", Cercanos: " + toursCercanos.size());
    }

}
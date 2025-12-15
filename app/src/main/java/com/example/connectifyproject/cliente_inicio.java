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
    
    private FirebaseFirestore db;
    private String tourActivoId = null; // ID del tour activo del cliente
    
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
    private MaterialCardView cardQr;
    private MaterialCardView cardStatistics;
    private android.widget.ImageView ivQrCode;
    private TextView tvQrInstruction;
    private TextView tvPuntoActual;
    private TextView tvNoStatistics;
    private com.github.mikephil.charting.charts.PieChart pieChart;
    private ImageButton btnNotifications;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvToursRecientes;
    private TextView tvNoTours;
    
    // Adapter para RecyclerView
    private Cliente_GalleryTourAdapter adapterToursRecientes;
    
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
        cardQr = findViewById(R.id.card_qr);
        cardStatistics = findViewById(R.id.card_statistics);
        ivQrCode = findViewById(R.id.iv_qr_code);
        tvQrInstruction = findViewById(R.id.tv_qr_instruction);
        tvPuntoActual = findViewById(R.id.tv_punto_actual);
        tvNoStatistics = findViewById(R.id.tv_no_statistics);
        pieChart = findViewById(R.id.pie_chart);
        btnNotifications = findViewById(R.id.btn_notifications);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        rvToursRecientes = findViewById(R.id.rv_tours_recientes);
        tvNoTours = findViewById(R.id.tv_no_tours);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    
    private void setupTourData() {
        // Cargar tour activo del cliente desde Firebase
        cargarTourActivoDelCliente();
    }
    
    private void setupRecyclerViews() {
        // Configurar RecyclerView para tours recientes
        LinearLayoutManager layoutManagerRecientes = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvToursRecientes.setLayoutManager(layoutManagerRecientes);
        
        // Inicializar con lista vacía - se llenará desde Firebase
        adapterToursRecientes = new Cliente_GalleryTourAdapter(this, new ArrayList<>());
        adapterToursRecientes.setOnTourClickListener(tour -> {
            // ✅ VALIDAR REGLA DE 2 HORAS antes de permitir click
            boolean disponible = com.example.connectifyproject.utils.TourTimeValidator
                .tourDisponibleParaInscripcion(tour.getFechaRealizacion(), tour.getStartTime());
            
            if (!disponible) {
                String mensaje = com.example.connectifyproject.utils.TourTimeValidator
                    .getMensajeTourNoDisponible(tour.getFechaRealizacion(), tour.getStartTime());
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                return;
            }
            
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
            // Abrir pantalla de QR para check-in o check-out
            mostrarQRTourActivo();
        });
        
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_inicio");
            startActivity(intent);
        });
    }
    
    /**
     * 🔲 MOSTRAR QR DEL TOUR ACTIVO
     * Abre cliente_show_qr con los datos del tour activo
     */
    private void mostrarQRTourActivo() {
        if (tourActivoId == null) {
            Toast.makeText(this, "No hay tour activo", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, cliente_show_qr.class);
        intent.putExtra("tourId", tourActivoId);
        intent.putExtra("tipoQR", "check_in"); // Por defecto check_in, el guía decidirá
        intent.putExtra("tourTitulo", tvTourTitle.getText().toString());
        startActivity(intent);
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
        
        // ✅ OPTIMIZADO: Solo whereEqualTo, sin orderBy para evitar índice compuesto
        // Filtrado de habilitado y ordenamiento se hace localmente
        db.collection("tours_asignados")
                .whereEqualTo("estado", "confirmado") // Solo tours confirmados
                .limit(50) // Cargar más documentos para compensar filtros locales
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "✅ Tours encontrados: " + querySnapshot.size());
                    
                    // Crear lista de pares (documento, tour) para poder ordenar
                    List<TourDocPair> tourPairs = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Filtrar habilitado en código
                        Boolean habilitado = doc.getBoolean("habilitado");
                        if (habilitado == null || !habilitado) {
                            continue;
                        }
                        
                        // ✅ Filtrar por regla de 2 horas
                        Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                        String horaInicio = doc.getString("horaInicio");
                        
                        boolean disponible = com.example.connectifyproject.utils.TourTimeValidator
                            .tourDisponibleParaInscripcion(fechaRealizacion, horaInicio);
                        
                        if (disponible) {
                            Cliente_Tour tour = processTourDocument(doc);
                            if (tour != null) {
                                Timestamp fechaCreacion = doc.getTimestamp("fechaCreacion");
                                tourPairs.add(new TourDocPair(tour, fechaCreacion));
                            }
                        }
                    }
                    
                    // Ordenar por fechaCreacion DESC localmente
                    tourPairs.sort((p1, p2) -> {
                        if (p1.fechaCreacion == null) return 1;
                        if (p2.fechaCreacion == null) return -1;
                        return p2.fechaCreacion.compareTo(p1.fechaCreacion); // DESC
                    });
                    
                    // Extraer solo los tours y limitar a top 5
                    List<Cliente_Tour> toursRecientes = new ArrayList<>();
                    int limit = Math.min(5, tourPairs.size());
                    for (int i = 0; i < limit; i++) {
                        toursRecientes.add(tourPairs.get(i).tour);
                    }
                    
                    Log.d(TAG, "📊 Tours disponibles después de filtrar: " + toursRecientes.size());
                    
                    // Actualizar RecyclerView
                    updateRecyclerViews(toursRecientes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando tours", e);
                    Toast.makeText(this, "Error al cargar tours: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    // Clase auxiliar para mantener tour con su fechaCreacion
    private static class TourDocPair {
        Cliente_Tour tour;
        Timestamp fechaCreacion;
        
        TourDocPair(Cliente_Tour tour, Timestamp fechaCreacion) {
            this.tour = tour;
            this.fechaCreacion = fechaCreacion;
        }
    }
    
    private Cliente_Tour processTourDocument(DocumentSnapshot doc) {
        try {
            Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
            if (!isTourAvailable(fechaRealizacion)) {
                return null;
            }
            
            // Filtrar tours donde el cliente ya está inscrito
            com.google.firebase.auth.FirebaseUser currentUser = 
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                List<Map<String, Object>> participantes = 
                    (List<Map<String, Object>>) doc.get("participantes");
                if (participantes != null) {
                    String clienteId = currentUser.getUid();
                    for (Map<String, Object> participante : participantes) {
                        String participanteId = (String) participante.get("clienteId");
                        if (clienteId.equals(participanteId)) {
                            // Cliente ya inscrito, no mostrar este tour
                            Log.d(TAG, "⏭️ Tour " + doc.getId() + " omitido (cliente ya inscrito)");
                            return null;
                        }
                    }
                }
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
            
            return tour;
            
        } catch (Exception e) {
            Log.e(TAG, "Error procesando tour: " + doc.getId(), e);
            return null;
        }
    }
    
    private boolean isTourAvailable(Timestamp fechaRealizacion) {
        if (fechaRealizacion == null) return false;
        
        // Obtener fecha/hora actual
        Calendar now = Calendar.getInstance();
        Date currentDateTime = now.getTime();
        
        // La fecha del tour (solo fecha, sin hora)
        Date tourDate = fechaRealizacion.toDate();
        
        // Permitir tours de HOY y futuros (no mostrar tours ya pasados)
        Calendar tourDateOnly = Calendar.getInstance();
        tourDateOnly.setTime(tourDate);
        tourDateOnly.set(Calendar.HOUR_OF_DAY, 23);
        tourDateOnly.set(Calendar.MINUTE, 59);
        tourDateOnly.set(Calendar.SECOND, 59);
        
        // Mostrar si la fecha del tour es HOY o futura
        return tourDateOnly.getTime().compareTo(currentDateTime) >= 0;
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
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error cargando imagen", e));
    }
    
    private void updateRecyclerViews(List<Cliente_Tour> toursRecientes) {
        if (adapterToursRecientes != null) {
            adapterToursRecientes.updateTours(toursRecientes);
            Log.d(TAG, "📊 RecyclerView actualizado - Tours recientes: " + toursRecientes.size());
            
            // Mostrar/ocultar mensaje de no tours
            if (toursRecientes.isEmpty()) {
                rvToursRecientes.setVisibility(View.GONE);
                tvNoTours.setVisibility(View.VISIBLE);
            } else {
                rvToursRecientes.setVisibility(View.VISIBLE);
                tvNoTours.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * 🔍 CARGAR TOUR ACTIVO DEL CLIENTE DESDE FIREBASE
     * Busca tours donde el cliente esté inscrito y el estado sea 'check_in' o 'en_curso'
     */
    private void cargarTourActivoDelCliente() {
        com.google.firebase.auth.FirebaseUser currentUser = 
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            Log.d(TAG, "⚠️ Usuario no autenticado, ocultando card de tour activo");
            cardTourActivo.setVisibility(View.GONE);
            if (cardQr != null) cardQr.setVisibility(View.GONE);
            if (cardStatistics != null) cardStatistics.setVisibility(View.GONE);
            return;
        }
        
        String clienteId = currentUser.getUid();
        Log.d(TAG, "🔍 Buscando tour activo para cliente: " + clienteId);
        
        // Buscar en tours_asignados donde participantes[] contenga al clienteId
        // y el estado sea 'check_in', 'en_curso' o 'check_out'
        db.collection("tours_asignados")
                .whereIn("estado", java.util.Arrays.asList("check_in", "en_curso", "check_out"))
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ Error al cargar tour activo", error);
                        cardTourActivo.setVisibility(View.GONE);
                        if (cardStatistics != null) cardStatistics.setVisibility(View.GONE);
                        return;
                    }
                    
                    if (snapshots == null || snapshots.isEmpty()) {
                        Log.d(TAG, "ℹ️ No hay tours activos, ocultando card");
                        cardTourActivo.setVisibility(View.GONE);
                        if (cardQr != null) cardQr.setVisibility(View.GONE);
                        // ✅ FASE 5: Mostrar gráfico de estadísticas
                        cargarYMostrarEstadisticas();
                        return;
                    }
                    
                    // Buscar tour donde el cliente esté en participantes[]
                    boolean tourEncontrado = false;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        java.util.List<java.util.Map<String, Object>> participantes = 
                            (java.util.List<java.util.Map<String, Object>>) doc.get("participantes");
                        
                        if (participantes != null) {
                            for (java.util.Map<String, Object> participante : participantes) {
                                String participanteId = (String) participante.get("clienteId");
                                if (clienteId.equals(participanteId)) {
                                    // ¡Encontrado! Mostrar este tour
                                    tourActivoId = doc.getId();
                                    mostrarTourActivo(doc);
                                    tourEncontrado = true;
                                    break;
                                }
                            }
                        }
                        
                        if (tourEncontrado) break;
                    }
                    
                    if (!tourEncontrado) {
                        Log.d(TAG, "ℹ️ Cliente no está inscrito en ningún tour activo");
                        cardTourActivo.setVisibility(View.GONE);
                        if (cardQr != null) cardQr.setVisibility(View.GONE);
                        // ✅ FASE 5: Mostrar gráfico de estadísticas
                        cargarYMostrarEstadisticas();
                    } else {
                        // Ocultar estadísticas si hay tour activo
                        if (cardStatistics != null) cardStatistics.setVisibility(View.GONE);
                    }
                });
    }
    
    /**
     * 📍 MOSTRAR TOUR ACTIVO CON DATOS REALES
     * Actualiza la UI con los datos del tour desde Firebase
     */
    private void mostrarTourActivo(com.google.firebase.firestore.DocumentSnapshot doc) {
        try {
            cardTourActivo.setVisibility(View.VISIBLE);
            
            // Extraer datos del tour
            String titulo = doc.getString("titulo");
            String nombreEmpresa = doc.getString("nombreEmpresa");
            String duracion = doc.getString("duracion");
            String horaInicio = doc.getString("horaInicio");
            String estado = doc.getString("estado");
            
            com.google.firebase.Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
            com.google.firebase.Timestamp horaFin = doc.getTimestamp("horaFin");
            
            // Generar y mostrar QR dinámico según el estado del tour
            generarYMostrarQR(doc.getId(), estado, horaFin);
            
            // Mostrar punto actual del itinerario si está en_curso
            if ("en_curso".equalsIgnoreCase(estado)) {
                mostrarPuntoActualItinerario(doc);
            } else {
                // Ocultar texto de punto actual si no está en curso
                if (tvPuntoActual != null) tvPuntoActual.setVisibility(View.GONE);
            }
            
            // Actualizar textos
            if (titulo != null) {
                tvTourTitle.setText(titulo);
            }
            
            if (nombreEmpresa != null) {
                tvTourCompany.setText(nombreEmpresa);
            }
            
            // Formatear duración y fecha
            if (fechaRealizacion != null && duracion != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String fechaStr = sdf.format(fechaRealizacion.toDate());
                tvTourDuration.setText("Duración: " + duracion + " hrs. Fecha: " + fechaStr);
            }
            
            // Actualizar estado del progreso según el estado del tour
            actualizarEstadoProgreso(estado);
            
            Log.d(TAG, "✅ Tour activo mostrado: " + titulo + " (ID: " + doc.getId() + ", Estado: " + estado + ")");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al mostrar tour activo", e);
            cardTourActivo.setVisibility(View.GONE);
        }
    }
    
    /**
     * 🎨 ACTUALIZAR ESTADO DEL PROGRESO VISUAL
     * Cambia los indicadores visuales según el estado del tour
     */
    private void actualizarEstadoProgreso(String estado) {
        if (estado == null) estado = "check_in";
        
        switch (estado.toLowerCase()) {
            case "check_in":
            case "check-in disponible":
                // Inicio activo, resto inactivo
                tvInicio.setTextColor(getResources().getColor(R.color.cliente_progress_active, null));
                tvEnCurso.setTextColor(getResources().getColor(R.color.cliente_progress_inactive, null));
                tvFin.setTextColor(getResources().getColor(R.color.cliente_progress_inactive, null));
                
                circleInicio.setBackgroundResource(R.drawable.cliente_progress_circle_active);
                circleEnCurso.setBackgroundResource(R.drawable.cliente_progress_circle_inactive);
                circleFin.setBackgroundResource(R.drawable.cliente_progress_circle_inactive);
                
                progressLineActive.setVisibility(View.GONE);
                break;
                
            case "en_curso":
            case "en curso":
                // Inicio y en curso activos
                tvInicio.setTextColor(getResources().getColor(R.color.cliente_progress_active, null));
                tvEnCurso.setTextColor(getResources().getColor(R.color.cliente_progress_active, null));
                tvFin.setTextColor(getResources().getColor(R.color.cliente_progress_inactive, null));
                
                circleInicio.setBackgroundResource(R.drawable.cliente_progress_circle_active);
                circleEnCurso.setBackgroundResource(R.drawable.cliente_progress_circle_active);
                circleFin.setBackgroundResource(R.drawable.cliente_progress_circle_inactive);
                
                progressLineActive.setVisibility(View.VISIBLE);
                android.widget.RelativeLayout.LayoutParams params = 
                    (android.widget.RelativeLayout.LayoutParams) progressLineActive.getLayoutParams();
                params.width = (int) (cardTourActivo.getWidth() * 0.5); // 50% de ancho
                progressLineActive.setLayoutParams(params);
                break;
                
            case "check_out":
            case "check-out disponible":
            case "completado":
            case "finalizado":
                // Todos activos
                tvInicio.setTextColor(getResources().getColor(R.color.cliente_progress_active, null));
                tvEnCurso.setTextColor(getResources().getColor(R.color.cliente_progress_active, null));
                tvFin.setTextColor(getResources().getColor(R.color.cliente_progress_active, null));
                
                circleInicio.setBackgroundResource(R.drawable.cliente_progress_circle_active);
                circleEnCurso.setBackgroundResource(R.drawable.cliente_progress_circle_active);
                circleFin.setBackgroundResource(R.drawable.cliente_progress_circle_active);
                
                progressLineActive.setVisibility(View.VISIBLE);
                android.widget.RelativeLayout.LayoutParams params2 = 
                    (android.widget.RelativeLayout.LayoutParams) progressLineActive.getLayoutParams();
                params2.width = android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
                progressLineActive.setLayoutParams(params2);
                break;
        }
    }
    
    /**
     * 🔲 GENERAR Y MOSTRAR QR DINÁMICO SEGÚN ESTADO DEL TOUR
     * - check_in: Muestra QR para check-in
     * - check_out: Muestra QR para check-out (solo si no ha pasado horaFin)
     * - en_curso: Oculta QR
     */
    private void generarYMostrarQR(String tourId, String estado, com.google.firebase.Timestamp horaFin) {
        try {
            com.google.firebase.auth.FirebaseUser currentUser = 
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            
            if (currentUser == null || tourId == null || estado == null) {
                if (cardQr != null) cardQr.setVisibility(View.GONE);
                return;
            }
            
            String clienteId = currentUser.getUid();
            
            // ✅ FASE 3: Lógica según estado del tour
            if ("check_in".equalsIgnoreCase(estado)) {
                // Mostrar QR de CHECK-IN en formato JSON
                org.json.JSONObject qrJson = new org.json.JSONObject();
                qrJson.put("tourId", tourId);
                qrJson.put("clienteId", clienteId);
                qrJson.put("type", "check_in");
                
                String qrData = qrJson.toString();
                generarBitmapQR(qrData);
                
                if (tvQrInstruction != null) {
                    tvQrInstruction.setText("📍 Muestra este código al guía para hacer CHECK-IN");
                    tvQrInstruction.setVisibility(View.VISIBLE);
                }
                
                if (cardQr != null) cardQr.setVisibility(View.VISIBLE);
                Log.d(TAG, "✅ QR de CHECK-IN generado");
                
            } else if ("check_out".equalsIgnoreCase(estado)) {
                // ✅ VALIDAR: No mostrar QR de check-out después de horaFin
                if (horaFin != null) {
                    long horaFinMillis = horaFin.toDate().getTime();
                    long horaActualMillis = System.currentTimeMillis();
                    
                    if (horaActualMillis > horaFinMillis) {
                        // Ya pasó la hora de fin - no mostrar QR
                        Log.d(TAG, "⚠️ Tour ya finalizó - ocultando QR de check-out");
                        if (cardQr != null) cardQr.setVisibility(View.GONE);
                        return;
                    }
                }
                
                // Mostrar QR de CHECK-OUT en formato JSON
                org.json.JSONObject qrJson = new org.json.JSONObject();
                qrJson.put("tourId", tourId);
                qrJson.put("clienteId", clienteId);
                qrJson.put("type", "check_out");
                
                String qrData = qrJson.toString();
                generarBitmapQR(qrData);
                
                if (tvQrInstruction != null) {
                    tvQrInstruction.setText("🏁 Muestra este código al guía para hacer CHECK-OUT");
                    tvQrInstruction.setVisibility(View.VISIBLE);
                }
                
                if (cardQr != null) cardQr.setVisibility(View.VISIBLE);
                Log.d(TAG, "✅ QR de CHECK-OUT generado");
                
            } else if ("en_curso".equalsIgnoreCase(estado)) {
                // Durante el tour NO se muestra QR
                Log.d(TAG, "ℹ️ Tour en curso - ocultando QR");
                if (cardQr != null) cardQr.setVisibility(View.GONE);
            } else {
                // Otros estados - ocultar QR
                if (cardQr != null) cardQr.setVisibility(View.GONE);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error generando QR dinámico", e);
            if (cardQr != null) cardQr.setVisibility(View.GONE);
        }
    }
    
    /**
     * 🎨 GENERAR BITMAP DEL QR
     */
    private void generarBitmapQR(String qrData) throws Exception {
        com.google.zxing.BarcodeFormat format = com.google.zxing.BarcodeFormat.QR_CODE;
        com.google.zxing.MultiFormatWriter writer = new com.google.zxing.MultiFormatWriter();
        com.google.zxing.common.BitMatrix bitMatrix = writer.encode(qrData, format, 512, 512);
        
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 
                    android.graphics.Color.BLACK : android.graphics.Color.WHITE);
            }
        }
        
        // Mostrar QR en ImageView
        if (ivQrCode != null) {
            ivQrCode.setImageBitmap(bitmap);
        }
    }
    
    /**
     * 📍 MOSTRAR PUNTO ACTUAL DEL ITINERARIO
     * Cuando el tour está en_curso, muestra el punto donde se encuentra el guía
     */
    private void mostrarPuntoActualItinerario(com.google.firebase.firestore.DocumentSnapshot doc) {
        try {
            java.util.List<java.util.Map<String, Object>> itinerario = 
                (java.util.List<java.util.Map<String, Object>>) doc.get("itinerario");
            
            if (itinerario == null || itinerario.isEmpty()) {
                if (tvPuntoActual != null) tvPuntoActual.setVisibility(View.GONE);
                return;
            }
            
            // Buscar el último punto completado o el primero no completado
            String puntoActual = null;
            int totalPuntos = itinerario.size();
            int puntosCompletados = 0;
            
            for (int i = 0; i < itinerario.size(); i++) {
                java.util.Map<String, Object> punto = itinerario.get(i);
                Boolean completado = (Boolean) punto.get("completado");
                String nombrePunto = (String) punto.get("nombre");
                
                if (completado != null && completado) {
                    puntosCompletados++;
                    puntoActual = nombrePunto; // Último completado
                } else {
                    // Primer punto no completado - este es el destino actual
                    if (puntoActual == null) {
                        puntoActual = nombrePunto;
                    }
                    break;
                }
            }
            
            if (puntoActual != null && tvPuntoActual != null) {
                String mensaje = String.format("📍 Ubicación actual: %s (%d/%d puntos visitados)", 
                    puntoActual, puntosCompletados, totalPuntos);
                tvPuntoActual.setText(mensaje);
                tvPuntoActual.setVisibility(View.VISIBLE);
                Log.d(TAG, "✅ Mostrando punto actual: " + puntoActual);
            } else {
                if (tvPuntoActual != null) tvPuntoActual.setVisibility(View.GONE);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al mostrar punto actual", e);
            if (tvPuntoActual != null) tvPuntoActual.setVisibility(View.GONE);
        }
    }
    
    /**
     * ✅ FASE 5: CARGAR Y MOSTRAR ESTADÍSTICAS DE TOURS
     * Muestra un gráfico de torta con: confirmados, completados, cancelados
     */
    private void cargarYMostrarEstadisticas() {
        com.google.firebase.auth.FirebaseUser currentUser = 
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null || cardStatistics == null) {
            return;
        }
        
        String clienteId = currentUser.getUid();
        
        // Contadores para cada estado
        final int[] confirmados = {0};
        final int[] completados = {0};
        final int[] cancelados = {0};
        final int[] totalConsultas = {0};
        
        // 1. Contar tours confirmados (tours_asignados con estado confirmado o pendiente)
        db.collection("tours_asignados")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String estado = doc.getString("estado");
                    java.util.List<java.util.Map<String, Object>> participantes = 
                        (java.util.List<java.util.Map<String, Object>>) doc.get("participantes");
                    
                    if (participantes != null) {
                        for (java.util.Map<String, Object> participante : participantes) {
                            String participanteId = (String) participante.get("clienteId");
                            if (clienteId.equals(participanteId)) {
                                if ("confirmado".equalsIgnoreCase(estado) || 
                                    "pendiente".equalsIgnoreCase(estado)) {
                                    confirmados[0]++;
                                } else if ("cancelado".equalsIgnoreCase(estado)) {
                                    cancelados[0]++;
                                }
                                break;
                            }
                        }
                    }
                }
                
                totalConsultas[0]++;
                verificarYMostrarGrafico(confirmados[0], completados[0], cancelados[0], totalConsultas[0]);
            });
        
        // 2. Contar tours completados (tours_completados)
        db.collection("tours_completados")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String tourAsignadoId = doc.getString("tourAsignadoId");
                    
                    if (tourAsignadoId != null) {
                        // Verificar si el cliente estaba en este tour
                        db.collection("tours_asignados")
                            .document(tourAsignadoId)
                            .get()
                            .addOnSuccessListener(tourDoc -> {
                                if (tourDoc.exists()) {
                                    java.util.List<java.util.Map<String, Object>> participantes = 
                                        (java.util.List<java.util.Map<String, Object>>) tourDoc.get("participantes");
                                    
                                    if (participantes != null) {
                                        for (java.util.Map<String, Object> participante : participantes) {
                                            String participanteId = (String) participante.get("clienteId");
                                            if (clienteId.equals(participanteId)) {
                                                completados[0]++;
                                                break;
                                            }
                                        }
                                    }
                                }
                            });
                    }
                }
                
                totalConsultas[0]++;
                verificarYMostrarGrafico(confirmados[0], completados[0], cancelados[0], totalConsultas[0]);
            });
    }
    
    /**
     * Verificar que todas las consultas terminaron y mostrar el gráfico
     */
    private void verificarYMostrarGrafico(int confirmados, int completados, int cancelados, int totalConsultas) {
        // Esperar a que ambas consultas terminen (confirmados/cancelados + completados)
        if (totalConsultas < 2) {
            return;
        }
        
        int total = confirmados + completados + cancelados;
        
        if (total == 0) {
            // No hay datos - mostrar mensaje
            if (pieChart != null) pieChart.setVisibility(View.GONE);
            if (tvNoStatistics != null) tvNoStatistics.setVisibility(View.VISIBLE);
            if (cardStatistics != null) cardStatistics.setVisibility(View.VISIBLE);
            Log.d(TAG, "ℹ️ No hay estadísticas para mostrar");
            return;
        }
        
        // Configurar gráfico de torta
        if (pieChart != null) {
            configurarPieChart(confirmados, completados, cancelados);
            pieChart.setVisibility(View.VISIBLE);
            if (tvNoStatistics != null) tvNoStatistics.setVisibility(View.GONE);
            if (cardStatistics != null) cardStatistics.setVisibility(View.VISIBLE);
            Log.d(TAG, "✅ Gráfico de estadísticas mostrado: " + confirmados + " confirmados, " + 
                completados + " completados, " + cancelados + " cancelados");
        }
    }
    
    /**
     * Configurar el gráfico de torta con los datos
     */
    private void configurarPieChart(int confirmados, int completados, int cancelados) {
        java.util.ArrayList<com.github.mikephil.charting.data.PieEntry> entries = new java.util.ArrayList<>();
        
        if (confirmados > 0) {
            entries.add(new com.github.mikephil.charting.data.PieEntry(confirmados, "Confirmados"));
        }
        if (completados > 0) {
            entries.add(new com.github.mikephil.charting.data.PieEntry(completados, "Completados"));
        }
        if (cancelados > 0) {
            entries.add(new com.github.mikephil.charting.data.PieEntry(cancelados, "Cancelados"));
        }
        
        com.github.mikephil.charting.data.PieDataSet dataSet = 
            new com.github.mikephil.charting.data.PieDataSet(entries, "Tours");
        
        // Colores para cada categoría
        java.util.ArrayList<Integer> colors = new java.util.ArrayList<>();
        if (confirmados > 0) colors.add(android.graphics.Color.parseColor("#FFB300")); // Naranja para confirmados
        if (completados > 0) colors.add(android.graphics.Color.parseColor("#43A047")); // Verde para completados
        if (cancelados > 0) colors.add(android.graphics.Color.parseColor("#E53935")); // Rojo para cancelados
        
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(pieChart));
        
        com.github.mikephil.charting.data.PieData data = new com.github.mikephil.charting.data.PieData(dataSet);
        
        // Configurar apariencia del gráfico
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setHoleColor(android.graphics.Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(android.graphics.Color.BLACK);
        pieChart.setCenterText("Mis Tours\n" + (confirmados + completados + cancelados) + " total");
        pieChart.setCenterTextSize(16f);
        pieChart.setDrawEntryLabels(true);
        
        // Leyenda
        com.github.mikephil.charting.components.Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        
        // Animar
        pieChart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

}
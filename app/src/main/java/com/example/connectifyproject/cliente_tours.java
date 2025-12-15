package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ToursAdapter;
import com.example.connectifyproject.models.Cliente_Tour;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class cliente_tours extends AppCompatActivity {

    private static final String TAG = "ClienteTours";
    
    private ImageButton btnNotifications;
    private ImageButton btnFiltros;
    private RecyclerView rvTours;
    private Cliente_ToursAdapter toursAdapter;
    private List<Cliente_Tour> allTours;
    private List<Cliente_Tour> filteredTours;
    private BottomNavigationView bottomNavigation;
    private ProgressBar progressBar;
    private TextView tvEmptyMessage;
    
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_tours);

        db = FirebaseFirestore.getInstance();
        
        initViews();
        setupBottomNavigation();
        setupClickListeners();
        setupRecyclerView();
        loadToursFromFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Tours" esté seleccionado cuando regresamos a esta actividad
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_tours);
        }
    }

    private void initViews() {
        btnNotifications = findViewById(R.id.btn_notifications);
        btnFiltros = findViewById(R.id.btn_filtros);
        rvTours = findViewById(R.id.rv_tours);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        // Estos pueden ser null si no existen en el layout
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        
        if (progressBar == null) {
            Log.w(TAG, "ProgressBar no encontrado en layout");
        }
        if (tvEmptyMessage == null) {
            Log.w(TAG, "TextView empty message no encontrado en layout");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_inicio) {
                Intent intent = new Intent(this, cliente_inicio.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                Intent intent = new Intent(this, cliente_reservas.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_tours) {
                // Ya estamos en tours
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
        
        // Seleccionar "Tours" por defecto
        bottomNavigation.setSelectedItemId(R.id.nav_tours);
    }

    private void setupClickListeners() {
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_tours");
            startActivity(intent);
        });
        
        btnFiltros.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_tour_filtros.class);
            startActivityForResult(intent, 1001);
        });
    }

    private void setupRecyclerView() {
        allTours = new ArrayList<>();
        filteredTours = new ArrayList<>();
        toursAdapter = new Cliente_ToursAdapter(this, filteredTours);
        
        rvTours.setLayoutManager(new LinearLayoutManager(this));
        rvTours.setAdapter(toursAdapter);
    }

    private void loadToursFromFirebase() {
        showLoading(true);
        Log.d(TAG, "Iniciando carga de tours desde Firebase");
        
        // Primero verificar si podemos acceder a la colección
        db.collection("tours_asignados")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Tours totales encontrados: " + querySnapshot.size());
                    allTours.clear();
                    filteredTours.clear();
                    
                    int toursValidos = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Verificar habilitado manualmente
                        Boolean habilitado = doc.getBoolean("habilitado");
                        String estado = doc.getString("estado");
                        Log.d(TAG, "Tour " + doc.getId() + " - habilitado: " + habilitado + ", estado: " + estado);
                        
                        // Filtrar tours cancelados y completados
                        if (habilitado != null && habilitado && 
                            estado != null && !estado.equals("cancelado") && !estado.equals("completado")) {
                            processTourDocument(doc);
                            toursValidos++;
                        }
                    }
                    
                    Log.d(TAG, "Tours válidos procesados: " + toursValidos);
                    showLoading(false);
                    updateEmptyState();
                    
                    if (toursValidos == 0) {
                        Toast.makeText(this, "No hay tours disponibles en este momento", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando tours", e);
                    Log.e(TAG, "Mensaje: " + e.getMessage());
                    Log.e(TAG, "Tipo: " + e.getClass().getName());
                    showLoading(false);
                    Toast.makeText(this, "Error al cargar tours: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    updateEmptyState();
                });
    }
    
    private void processTourDocument(DocumentSnapshot doc) {
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
            tour.setHabilitado(Boolean.TRUE.equals(doc.getBoolean("habilitado")));
            
            // Duración
            String duracion = doc.getString("duracion");
            tour.setDuration(duracion != null ? duracion + " horas" : "");
            
            // Precio
            Number precio = (Number) doc.get("precio");
            tour.setPrice(precio != null ? precio.doubleValue() : 0.0);
            
            // Fecha formateada
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tour.setDate(sdf.format(fechaRealizacion.toDate()));
            
            // Horarios
            String horaInicio = doc.getString("horaInicio");
            String horaFin = doc.getString("horaFin");
            tour.setStartTime(horaInicio != null ? horaInicio : "Por confirmar");
            tour.setEndTime(horaFin != null ? horaFin : "Por confirmar");
            
            // Ciudad del tour
            String ciudad = doc.getString("ciudad");
            tour.setCiudad(ciudad != null ? ciudad : "");
            
            // Ubicación del primer punto del itinerario (para retrocompatibilidad)
            List<Map<String, Object>> itinerario = (List<Map<String, Object>>) doc.get("itinerario");
            if (itinerario != null && !itinerario.isEmpty()) {
                String direccion = (String) itinerario.get(0).get("direccion");
                tour.setLocation(direccion != null ? direccion : "");
            }
            tour.setItinerario(itinerario);
            
            // Idiomas
            List<String> idiomas = (List<String>) doc.get("idiomasRequeridos");
            tour.setIdiomasRequeridos(idiomas);
            
            // Consideraciones
            tour.setConsideraciones(doc.getString("consideraciones"));
            
            // Servicios adicionales
            List<Map<String, Object>> servicios = (List<Map<String, Object>>) doc.get("serviciosAdicionales");
            tour.setServiciosAdicionales(servicios);
            
            // Nombre de empresa
            String nombreEmpresa = doc.getString("nombreEmpresa");
            if (nombreEmpresa != null && !nombreEmpresa.isEmpty()) {
                tour.setCompanyName(nombreEmpresa);
                addTourToList(tour);
            } else {
                loadCompanyName(tour);
            }
            
            // Cargar imagen
            loadTourImage(tour);
            
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
    
    private void loadCompanyName(Cliente_Tour tour) {
        if (tour.getEmpresaId() == null) {
            tour.setCompanyName("Empresa");
            addTourToList(tour);
            return;
        }
        
        db.collection("usuarios")
                .document(tour.getEmpresaId())
                .get()
                .addOnSuccessListener(doc -> {
                    String nombreEmpresa = doc.getString("nombreEmpresa");
                    tour.setCompanyName(nombreEmpresa != null ? nombreEmpresa : "Empresa");
                    addTourToList(tour);
                })
                .addOnFailureListener(e -> {
                    tour.setCompanyName("Empresa");
                    addTourToList(tour);
                });
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
                        toursAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando imagen del tour", e);
                });
    }
    
    private void addTourToList(Cliente_Tour tour) {
        if (!allTours.contains(tour)) {
            allTours.add(tour);
            filteredTours.add(tour);
            toursAdapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvTours.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void updateEmptyState() {
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(filteredTours.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Aplicar filtros recibidos desde cliente_tour_filtros
            String startDate = data.getStringExtra("start_date");
            String endDate = data.getStringExtra("end_date");
            double minPrice = data.getDoubleExtra("min_price", 0);
            double maxPrice = data.getDoubleExtra("max_price", Double.MAX_VALUE);
            String language = data.getStringExtra("language");
            String city = data.getStringExtra("city");
            
            applyFilters(startDate, endDate, minPrice, maxPrice, language, city);
        }
    }

    private void applyFilters(String startDate, String endDate, double minPrice, double maxPrice, String language, String city) {
        filteredTours.clear();
        // Formato debe coincidir con el de cliente_tour_filtros.java (dd-MM-yyyy)
        SimpleDateFormat filterSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        
        // Parsear fechas de inicio y fin para el rango
        Date filterStartDate = null;
        Date filterEndDate = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            try {
                filterStartDate = filterSdf.parse(startDate);
                Log.d(TAG, "Fecha inicio parseada: " + startDate + " -> " + filterStartDate);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing start date: " + startDate + " - " + e.getMessage());
            }
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            try {
                filterEndDate = filterSdf.parse(endDate);
                // Ajustar al final del día
                if (filterEndDate != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(filterEndDate);
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    filterEndDate = cal.getTime();
                }
                Log.d(TAG, "Fecha fin parseada: " + endDate + " -> " + filterEndDate);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing end date: " + endDate + " - " + e.getMessage());
            }
        }
        
        for (Cliente_Tour tour : allTours) {
            boolean matchesPrice = tour.getPrice() >= minPrice && tour.getPrice() <= maxPrice;
            
            // Filtro por rango de fechas
            boolean matchesDate = true;
            if ((filterStartDate != null || filterEndDate != null) && tour.getFechaRealizacion() != null) {
                Date tourDate = tour.getFechaRealizacion().toDate();
                
                // Normalizar fecha del tour al inicio del día para comparación correcta
                Calendar tourCal = Calendar.getInstance();
                tourCal.setTime(tourDate);
                tourCal.set(Calendar.HOUR_OF_DAY, 0);
                tourCal.set(Calendar.MINUTE, 0);
                tourCal.set(Calendar.SECOND, 0);
                tourCal.set(Calendar.MILLISECOND, 0);
                Date normalizedTourDate = tourCal.getTime();
                
                // Si solo hay fecha de inicio, filtrar desde esa fecha en adelante
                if (filterStartDate != null && filterEndDate == null) {
                    matchesDate = !normalizedTourDate.before(filterStartDate);
                }
                // Si solo hay fecha de fin, filtrar hasta esa fecha
                else if (filterStartDate == null && filterEndDate != null) {
                    matchesDate = !normalizedTourDate.after(filterEndDate);
                }
                // Si hay ambas, filtrar en el rango
                else if (filterStartDate != null && filterEndDate != null) {
                    matchesDate = !normalizedTourDate.before(filterStartDate) && !normalizedTourDate.after(filterEndDate);
                }
            }
            
            // Filtro por ciudad
            boolean matchesCity = true;
            if (city != null && !city.isEmpty()) {
                String tourCity = tour.getCiudad();
                matchesCity = tourCity != null && tourCity.equalsIgnoreCase(city);
            }
            
            // Filtro por idioma
            boolean matchesLanguage = true;
            if (language != null && !language.isEmpty()) {
                List<String> tourLanguages = tour.getIdiomasRequeridos();
                matchesLanguage = tourLanguages != null && tourLanguages.contains(language);
            }
            
            // Aplicar todos los filtros
            if (matchesPrice && matchesDate && matchesCity && matchesLanguage) {
                filteredTours.add(tour);
            }
        }
        
        toursAdapter.notifyDataSetChanged();
    }
}
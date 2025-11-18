package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ReservasAdapter;
import com.example.connectifyproject.models.Cliente_Reserva;
import com.example.connectifyproject.models.Cliente_Tour;
import com.example.connectifyproject.models.Cliente_ServicioAdicional;
import com.example.connectifyproject.models.Cliente_PaymentMethod;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class cliente_reservas extends AppCompatActivity {

    private static final String TAG = "ClienteReservas";

    private ImageButton btnNotifications;
    private TabLayout tabLayoutReservas;
    private RecyclerView rvReservas;
    private Cliente_ReservasAdapter reservasAdapter;
    private List<Cliente_Reserva> allReservas;
    private List<Cliente_Reserva> filteredReservas;
    private boolean showingProximas = true;
    private BottomNavigationView bottomNavigation;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_reservas);

        initViews();
        setupBottomNavigation();
        setupClickListeners();
        setupRecyclerView();
        loadReservasData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Reservas" esté seleccionado cuando regresamos a esta actividad
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_reservas);
        }
    }

    private void initViews() {
        btnNotifications = findViewById(R.id.btn_notifications);
        tabLayoutReservas = findViewById(R.id.tab_layout_reservas);
        rvReservas = findViewById(R.id.rv_reservas);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
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
                // Ya estamos en reservas
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
        
        // Seleccionar "Reservas" por defecto
        bottomNavigation.setSelectedItemId(R.id.nav_reservas);
    }

    private void setupClickListeners() {
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_reservas");
            startActivity(intent);
        });
        
        setupTabs();
    }
    
    private void setupTabs() {
        // Agregar tabs
        tabLayoutReservas.addTab(tabLayoutReservas.newTab().setText("Próximas"));
        tabLayoutReservas.addTab(tabLayoutReservas.newTab().setText("Pasadas"));
        
        // Listener para cambios de tab
        tabLayoutReservas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingProximas = tab.getPosition() == 0;
                filterReservas();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }



    private void setupRecyclerView() {
    allReservas = new ArrayList<>();
    filteredReservas = new ArrayList<>();
    reservasAdapter = new Cliente_ReservasAdapter(this, filteredReservas);
        
        rvReservas.setLayoutManager(new LinearLayoutManager(this));
        rvReservas.setAdapter(reservasAdapter);
    }

    private void loadReservasData() {
        if (currentUserId == null) {
            Log.e(TAG, "Usuario no autenticado");
            return;
        }
        
        Log.d(TAG, "Cargando reservas para usuario: " + currentUserId);
        
        // Obtener todos los tours asignados (confirmados)
        db.collection("tours_asignados")
                .whereEqualTo("estado", "confirmado")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allReservas.clear();
                    Log.d(TAG, "Total tours confirmados encontrados: " + queryDocumentSnapshots.size());
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Obtener array de participantes
                        List<Map<String, Object>> participantes = (List<Map<String, Object>>) doc.get("participantes");
                        
                        if (participantes != null) {
                            // Buscar si el usuario actual está en los participantes
                            for (Map<String, Object> participante : participantes) {
                                String usuarioId = (String) participante.get("usuarioId");
                                
                                if (currentUserId.equals(usuarioId)) {
                                    // Este usuario tiene una reserva en este tour
                                    Log.d(TAG, "Reserva encontrada en tour: " + doc.getId());
                                    
                                    // Crear objeto Cliente_Reserva con datos reales
                                    Cliente_Reserva reserva = crearReservaDesdeFirebase(doc, participante);
                                    if (reserva != null) {
                                        allReservas.add(reserva);
                                    }
                                    break; // Solo necesitamos procesar este participante una vez
                                }
                            }
                        }
                    }
                    
                    Log.d(TAG, "Total reservas del usuario: " + allReservas.size());
                    filterReservas();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar reservas: " + e.getMessage(), e);
                });
    }    private Cliente_Reserva crearReservaDesdeFirebase(QueryDocumentSnapshot tourDoc, Map<String, Object> participanteData) {
        try {
            // Extraer datos del tour
            String tourId = tourDoc.getId();
            String titulo = tourDoc.getString("titulo");
            String empresaNombre = tourDoc.getString("nombreEmpresa"); // Campo correcto en Firebase
            String ubicacion = tourDoc.getString("ubicacion");
            String descripcion = tourDoc.getString("descripcion");
            
            // Precio puede venir como Double o Long (número en Firebase)
            Double precioPorPersona = 0.0;
            Object precioObj = tourDoc.get("precio");
            if (precioObj instanceof Double) {
                precioPorPersona = (Double) precioObj;
            } else if (precioObj instanceof Long) {
                precioPorPersona = ((Long) precioObj).doubleValue();
            }
            
            String duracion = tourDoc.getString("duracion");
            
            // Fecha de realización del tour
            Timestamp fechaRealizacion = tourDoc.getTimestamp("fechaRealizacion");
            String horaInicio = tourDoc.getString("horaInicio");
            String horaFin = tourDoc.getString("horaFin");
            
            // Datos del participante
            Integer numeroPersonas = participanteData.get("numeroPersonas") != null ? 
                    ((Long) participanteData.get("numeroPersonas")).intValue() : 1;
            
            // montoTotal viene como String "S/330.00", necesitamos parsearlo
            Double montoTotal = 0.0;
            Object montoObj = participanteData.get("montoTotal");
            if (montoObj instanceof String) {
                String montoStr = (String) montoObj;
                // Remover "S/" y espacios, luego parsear
                montoStr = montoStr.replace("S/", "").replace(" ", "").trim();
                try {
                    montoTotal = Double.parseDouble(montoStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parseando montoTotal: " + montoStr, e);
                    montoTotal = precioPorPersona * numeroPersonas; // Calcular como fallback
                }
            } else if (montoObj instanceof Double) {
                montoTotal = (Double) montoObj;
            } else if (montoObj instanceof Long) {
                montoTotal = ((Long) montoObj).doubleValue();
            }
            
            String metodoPagoId = (String) participanteData.get("metodoPagoId");
            
            // Formatear fecha
            String fechaFormateada = "";
            if (fechaRealizacion != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                fechaFormateada = sdf.format(fechaRealizacion.toDate());
            }
            
            // Determinar si es próxima o pasada
            boolean esFutura = false;
            if (fechaRealizacion != null) {
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                esFutura = fechaRealizacion.toDate().after(today.getTime());
            }
            String estado = esFutura ? "Próxima" : "Pasada";
            
            // Crear objeto Cliente_Tour
            Cliente_Tour tour = new Cliente_Tour(
                    tourId,
                    titulo != null ? titulo : "Tour sin título",
                    empresaNombre != null ? empresaNombre : "Empresa",
                    duracion != null ? duracion : "N/A",
                    fechaFormateada,
                    precioPorPersona != null ? precioPorPersona : 0.0,
                    ubicacion != null ? ubicacion : "",
                    descripcion != null ? descripcion : ""
            );
            
            // Crear objeto Cliente_Reserva
            Cliente_Reserva reserva = new Cliente_Reserva();
            reserva.setId(tourId);
            reserva.setTour(tour);
            reserva.setPersonas(numeroPersonas);
            reserva.setFecha(fechaFormateada);
            reserva.setHoraInicio(horaInicio != null ? horaInicio : "");
            reserva.setHoraFin(horaFin != null ? horaFin : "");
            reserva.setTotal(montoTotal);
            reserva.setEstado(estado);
            
            // Método de pago (se cargará más adelante si es necesario)
            // Por ahora dejamos null o creamos uno básico
            reserva.setMetodoPago(null);
            
            // Servicios adicionales (por ahora lista vacía)
            reserva.setServicios(new ArrayList<>());
            
            return reserva;
            
        } catch (Exception e) {
            Log.e(TAG, "Error al crear reserva desde Firebase: " + e.getMessage(), e);
            return null;
        }
    }
    
    private void filterReservas() {
        filteredReservas.clear();
        
        for (Cliente_Reserva reserva : allReservas) {
            boolean esFutura = "Próxima".equals(reserva.getEstado());
            
            if (showingProximas && esFutura) {
                filteredReservas.add(reserva);
            } else if (!showingProximas && !esFutura) {
                filteredReservas.add(reserva);
            }
        }
        
        Log.d(TAG, "Mostrando " + (showingProximas ? "próximas" : "pasadas") + ": " + filteredReservas.size() + " reservas");
        reservasAdapter.notifyDataSetChanged();
    }


}
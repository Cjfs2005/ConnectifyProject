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
import java.util.Arrays;
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
        
        // Obtener todos los tours donde el cliente esté inscrito
        // No filtrar por estado específico - mostrar todos (pendiente, check_in, en_curso, etc.)
        db.collection("tours_asignados")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allReservas.clear();
                    Log.d(TAG, "Total tours encontrados: " + queryDocumentSnapshots.size());
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Obtener array de participantes
                        List<Map<String, Object>> participantes = (List<Map<String, Object>>) doc.get("participantes");
                        
                        if (participantes != null && !participantes.isEmpty()) {
                            // Buscar si el usuario actual está en los participantes
                            for (Map<String, Object> participante : participantes) {
                                // ✅ CORRECCIÓN: Buscar por "clienteId" (no "usuarioId")
                                String clienteId = (String) participante.get("clienteId");
                                
                                if (currentUserId.equals(clienteId)) {
                                    // Este usuario tiene una reserva en este tour
                                    String estado = doc.getString("estado");
                                    Log.d(TAG, "Reserva encontrada en tour: " + doc.getId() + " (estado: " + estado + ")");
                                    
                                    // ✅ MOSTRAR TOURS EN ESTOS ESTADOS ACTIVOS:
                                    // - confirmado: Tour confirmado con guía asignado
                                    // - check_in: Check-in habilitado, listo para iniciar
                                    // - en_curso: Tour en progreso
                                    // - check_out: Check-out habilitado, tour terminando
                                    List<String> estadosValidos = Arrays.asList(
                                        "confirmado", "check_in", "en_curso", "en_progreso", 
                                        "check_out", "programado"
                                    );
                                    
                                    if (estado != null && estadosValidos.contains(estado.toLowerCase())) {
                                        // ✅ VALIDAR TOURS SIMULTÁNEOS - Solo un tour activo por día
                                        if (validarTourSimultaneo(doc, allReservas)) {
                                            // Crear objeto Cliente_Reserva con datos reales
                                            Cliente_Reserva reserva = crearReservaDesdeFirebase(doc, participante);
                                            if (reserva != null) {
                                                allReservas.add(reserva);
                                            }
                                        } else {
                                            Log.w(TAG, "Tour simultáneo detectado y omitido: " + doc.getId());
                                        }
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
            String empresaId = tourDoc.getString("empresaId"); // ID de la empresa para chat e info
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
            
            // Datos del participante (pueden no existir si solo se inscribió sin pagar)
            Integer numeroPersonas = 1; // Por defecto 1 persona
            if (participanteData.get("numeroPersonas") != null) {
                Object numPersonasObj = participanteData.get("numeroPersonas");
                if (numPersonasObj instanceof Long) {
                    numeroPersonas = ((Long) numPersonasObj).intValue();
                } else if (numPersonasObj instanceof Integer) {
                    numeroPersonas = (Integer) numPersonasObj;
                }
            }
            
            // montoTotal puede venir como String "S/330.00" o no existir
            Double montoTotal = precioPorPersona * numeroPersonas; // Valor por defecto
            Object montoObj = participanteData.get("montoTotal");
            if (montoObj instanceof String) {
                String montoStr = (String) montoObj;
                // Remover "S/" y espacios, luego parsear
                montoStr = montoStr.replace("S/", "").replace(" ", "").trim();
                try {
                    montoTotal = Double.parseDouble(montoStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parseando montoTotal: " + montoStr, e);
                }
            } else if (montoObj instanceof Double) {
                montoTotal = (Double) montoObj;
            } else if (montoObj instanceof Long) {
                montoTotal = ((Long) montoObj).doubleValue();
            }
            
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
            
            // Asignar empresaId para que esté disponible en detalle
            tour.setEmpresaId(empresaId);
            
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

    /**
     * ✅ VALIDAR TOURS SIMULTÁNEOS - Prevenir que el cliente tenga múltiples tours activos el mismo día
     */
    private boolean validarTourSimultaneo(QueryDocumentSnapshot nuevoTour, List<Cliente_Reserva> reservasExistentes) {
        try {
            // Obtener fecha del nuevo tour
            Timestamp fechaNuevoTour = nuevoTour.getTimestamp("fechaRealizacion");
            if (fechaNuevoTour == null) return true; // Si no hay fecha, permitir
            
            Calendar calNuevoTour = Calendar.getInstance();
            calNuevoTour.setTime(fechaNuevoTour.toDate());
            calNuevoTour.set(Calendar.HOUR_OF_DAY, 0);
            calNuevoTour.set(Calendar.MINUTE, 0);
            calNuevoTour.set(Calendar.SECOND, 0);
            calNuevoTour.set(Calendar.MILLISECOND, 0);
            
            // Verificar si ya existe una reserva activa para el mismo día
            for (Cliente_Reserva reservaExistente : reservasExistentes) {
                // Solo verificar tours con estados activos
                if (reservaExistente.getFecha() != null && !reservaExistente.getFecha().isEmpty()) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        Date fechaExistente = sdf.parse(reservaExistente.getFecha());
                        
                        Calendar calExistente = Calendar.getInstance();
                        calExistente.setTime(fechaExistente);
                        calExistente.set(Calendar.HOUR_OF_DAY, 0);
                        calExistente.set(Calendar.MINUTE, 0);
                        calExistente.set(Calendar.SECOND, 0);
                        calExistente.set(Calendar.MILLISECOND, 0);
                        
                        // Si las fechas coinciden, hay conflicto
                        if (calNuevoTour.getTimeInMillis() == calExistente.getTimeInMillis()) {
                            Log.w(TAG, "Tour simultáneo detectado: " + nuevoTour.getId() + " conflicta con reserva existente");
                            return false;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al parsear fecha de reserva existente: " + reservaExistente.getFecha(), e);
                    }
                }
            }
            
            return true; // No hay conflictos
            
        } catch (Exception e) {
            Log.e(TAG, "Error en validación de tours simultáneos", e);
            return true; // En caso de error, permitir el tour
        }
    }


}
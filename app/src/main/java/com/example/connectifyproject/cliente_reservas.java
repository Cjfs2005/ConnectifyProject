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
    private List<Map<String, Object>> allReservasCanceladas; // ✅ Nueva lista para canceladas
    private int currentTab = 0; // 0: Próximas, 1: Pasadas, 2: Canceladas
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
        
        // Recargar datos cuando volvemos (por si se canceló una reserva)
        recargarDatosActuales();
    }
    
    /**
     * Recargar los datos del tab actual
     */
    private void recargarDatosActuales() {
        if (currentTab == 2) {
            // Tab Canceladas
            mostrarReservasCanceladas();
        } else {
            // Tab Próximas o Pasadas
            loadReservasData();
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
        // ✅ Agregar tabs (ahora incluye Canceladas)
        tabLayoutReservas.addTab(tabLayoutReservas.newTab().setText("Próximas"));
        tabLayoutReservas.addTab(tabLayoutReservas.newTab().setText("Pasadas"));
        tabLayoutReservas.addTab(tabLayoutReservas.newTab().setText("Canceladas"));
        
        // Listener para cambios de tab
        tabLayoutReservas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                if (currentTab == 2) {
                    // Tab Canceladas
                    mostrarReservasCanceladas();
                } else {
                    // Tab Próximas o Pasadas
                    filterReservas();
                }
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
    allReservasCanceladas = new ArrayList<>(); // ✅ Inicializar lista de canceladas
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
                                    
                                    // ✅ MOSTRAR TOURS EN ESTOS ESTADOS:
                                    // - confirmado: Tour confirmado con guía asignado
                                    // - check_in: Check-in habilitado, listo para iniciar
                                    // - en_curso: Tour en progreso
                                    // - check_out: Check-out habilitado, tour terminando
                                    // - completado: Tour finalizado (aparecerá en "Pasadas")
                                    List<String> estadosValidos = Arrays.asList(
                                        "confirmado", "check_in", "en_curso", "en_progreso", 
                                        "check_out", "programado", "completado"
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
                    
                    Log.d(TAG, "Total reservas activas del usuario: " + allReservas.size());
                    
                    // ✅ AHORA CARGAR TOURS COMPLETADOS
                    cargarToursCompletados();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar reservas: " + e.getMessage(), e);
                });
    }
    
    /**
     * ✅ CARGAR TOURS COMPLETADOS PARA MOSTRAR EN "PASADAS"
     * Busca en tours_completados los tours donde participó el cliente
     */
    private void cargarToursCompletados() {
        if (currentUserId == null) {
            Log.e(TAG, "Usuario no autenticado");
            filterReservas();
            return;
        }
        
        Log.d(TAG, "Cargando tours completados para usuario: " + currentUserId);
        
        db.collection("tours_completados")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "Total tours completados encontrados: " + querySnapshot.size());
                
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    // Obtener el tourAsignadoId para buscar los participantes
                    String tourAsignadoId = doc.getString("tourAsignadoId");
                    
                    if (tourAsignadoId != null) {
                        // Consultar el tour asignado original para obtener los participantes
                        db.collection("tours_asignados")
                            .document(tourAsignadoId)
                            .get()
                            .addOnSuccessListener(tourAsignadoDoc -> {
                                if (tourAsignadoDoc.exists()) {
                                    List<Map<String, Object>> participantes = 
                                        (List<Map<String, Object>>) tourAsignadoDoc.get("participantes");
                                    
                                    if (participantes != null) {
                                        // Buscar si el cliente actual participó
                                        for (Map<String, Object> participante : participantes) {
                                            String clienteId = (String) participante.get("clienteId");
                                            
                                            if (currentUserId.equals(clienteId)) {
                                                // Este cliente participó en este tour completado
                                                Cliente_Reserva reserva = crearReservaCompletadaDesdeFirebase(doc, participante);
                                                if (reserva != null) {
                                                    allReservas.add(reserva);
                                                    Log.d(TAG, "Tour completado agregado: " + doc.getString("titulo"));
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                
                                // Aplicar filtro después de procesar cada documento
                                filterReservas();
                            });
                    }
                }
                
                // Aplicar filtro inicial (se volverá a aplicar cuando lleguen los tours completados)
                filterReservas();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar tours completados: " + e.getMessage(), e);
                filterReservas();
            });
    }
    
    /**
     * ✅ CREAR RESERVA DESDE TOUR COMPLETADO
     * Similar a crearReservaDesdeFirebase pero adaptado para tours_completados
     */
    private Cliente_Reserva crearReservaCompletadaDesdeFirebase(com.google.firebase.firestore.DocumentSnapshot tourCompletadoDoc, Map<String, Object> participanteData) {
        try {
            String tourId = tourCompletadoDoc.getId();
            String titulo = tourCompletadoDoc.getString("titulo");
            String empresaNombre = tourCompletadoDoc.getString("empresaNombre");
            String empresaId = tourCompletadoDoc.getString("empresaId");
            
            // Los tours completados no tienen todos los campos, usar valores por defecto
            String ubicacion = "";
            String ciudad = "";
            String descripcion = "";
            String imagenPrincipal = null;
            
            Double precioPorPersona = 0.0;
            Object precioObj = tourCompletadoDoc.get("precioTour");
            if (precioObj instanceof Double) {
                precioPorPersona = (Double) precioObj;
            } else if (precioObj instanceof Long) {
                precioPorPersona = ((Long) precioObj).doubleValue();
            }
            
            String duracion = tourCompletadoDoc.getString("duracionReal");
            if (duracion == null) duracion = "N/A";
            
            Timestamp fechaRealizacion = tourCompletadoDoc.getTimestamp("fechaRealizacion");
            Timestamp horaInicioReal = tourCompletadoDoc.getTimestamp("horaInicioReal");
            Timestamp horaFinReal = tourCompletadoDoc.getTimestamp("horaFinReal");
            
            // Formatear horas desde timestamps
            String horaInicio = "";
            String horaFin = "";
            if (horaInicioReal != null) {
                java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                horaInicio = timeFormat.format(horaInicioReal.toDate());
            }
            if (horaFinReal != null) {
                java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                horaFin = timeFormat.format(horaFinReal.toDate());
            }
            
            // Datos del participante
            Integer numeroPersonas = 1;
            if (participanteData.get("numeroPersonas") != null) {
                Object numPersonasObj = participanteData.get("numeroPersonas");
                if (numPersonasObj instanceof Long) {
                    numeroPersonas = ((Long) numPersonasObj).intValue();
                } else if (numPersonasObj instanceof Integer) {
                    numeroPersonas = (Integer) numPersonasObj;
                }
            }
            
            Double montoTotal = precioPorPersona * numeroPersonas;
            Object montoObj = participanteData.get("montoTotal");
            if (montoObj instanceof String) {
                String montoStr = (String) montoObj;
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
            
            // ✅ Tours completados SIEMPRE son "Pasada"
            String estado = "Pasada";
            
            // Crear objeto Cliente_Tour
            Cliente_Tour tour = new Cliente_Tour(
                    tourId,
                    titulo != null ? titulo : "Tour sin título",
                    empresaNombre != null ? empresaNombre : "Empresa",
                    duracion,
                    fechaFormateada,
                    precioPorPersona,
                    ubicacion,
                    descripcion
            );
            
            tour.setEmpresaId(empresaId);
            tour.setCiudad(ciudad);
            tour.setImageUrl(imagenPrincipal);
            
            // Crear objeto Cliente_Reserva
            Cliente_Reserva reserva = new Cliente_Reserva();
            reserva.setId(tourId);
            reserva.setTour(tour);
            reserva.setPersonas(numeroPersonas);
            reserva.setFecha(fechaFormateada);
            reserva.setHoraInicio(horaInicio);
            reserva.setHoraFin(horaFin);
            reserva.setTotal(montoTotal);
            reserva.setEstado(estado);
            reserva.setMetodoPago(null);
            reserva.setServicios(new ArrayList<>());
            
            return reserva;
            
        } catch (Exception e) {
            Log.e(TAG, "Error al crear reserva completada desde Firebase: " + e.getMessage(), e);
            return null;
        }
    }
    
    private Cliente_Reserva crearReservaDesdeFirebase(QueryDocumentSnapshot tourDoc, Map<String, Object> participanteData) {
        try {
            // Extraer datos del tour
            String tourId = tourDoc.getId();
            String titulo = tourDoc.getString("titulo");
            String empresaNombre = tourDoc.getString("nombreEmpresa"); // Campo correcto en Firebase
            String empresaId = tourDoc.getString("empresaId"); // ID de la empresa para chat e info
            String ubicacion = tourDoc.getString("ubicacion");
            String ciudad = tourDoc.getString("ciudad"); // Ciudad del tour
            String descripcion = tourDoc.getString("descripcion");
            String imagenPrincipal = tourDoc.getString("imagenPrincipal"); // Imagen del tour
            
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
            
            // ✅ Si el tour está completado, usar horas reales; si no, usar horas programadas
            String estadoTourTemp = tourDoc.getString("estado");
            String horaInicio, horaFin;
            
            if ("completado".equalsIgnoreCase(estadoTourTemp)) {
                // Usar horaInicioReal y horaFinReal (timestamps)
                Timestamp horaInicioReal = tourDoc.getTimestamp("horaInicioReal");
                Timestamp horaFinReal = tourDoc.getTimestamp("horaFinReal");
                
                java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                horaInicio = horaInicioReal != null ? timeFormat.format(horaInicioReal.toDate()) : "";
                horaFin = horaFinReal != null ? timeFormat.format(horaFinReal.toDate()) : "";
            } else {
                // Usar horas programadas (strings)
                horaInicio = tourDoc.getString("horaInicio");
                horaFin = tourDoc.getString("horaFin");
            }
            
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
            // ✅ Si el tour está completado, SIEMPRE es "Pasada"
            String estadoTour = tourDoc.getString("estado");
            String estado;
            
            if ("completado".equalsIgnoreCase(estadoTour)) {
                estado = "Pasada";
            } else {
                boolean esFutura = false;
                if (fechaRealizacion != null) {
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);
                    esFutura = fechaRealizacion.toDate().after(today.getTime());
                }
                estado = esFutura ? "Próxima" : "Pasada";
            }
            
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
            
            // Asignar empresaId, ciudad e imagen para que estén disponibles en detalle
            tour.setEmpresaId(empresaId);
            tour.setCiudad(ciudad);
            tour.setImageUrl(imagenPrincipal);
            
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
        
        boolean showingProximas = (currentTab == 0);
        
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
     * ✅ Cargar y mostrar reservas canceladas desde Firebase
     */
    private void mostrarReservasCanceladas() {
        if (currentUserId == null) {
            Log.e(TAG, "Usuario no autenticado");
            return;
        }
        
        Log.d(TAG, "Cargando reservas canceladas para usuario: " + currentUserId);
        
        db.collection("reservas_canceladas")
            .whereEqualTo("clienteId", currentUserId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                allReservasCanceladas.clear();
                filteredReservas.clear();
                
                Log.d(TAG, "Reservas canceladas encontradas: " + queryDocumentSnapshots.size());
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Map<String, Object> cancelada = doc.getData();
                    cancelada.put("documentId", doc.getId());
                    allReservasCanceladas.add(cancelada);
                    
                    // Crear Cliente_Reserva para mostrar en el adapter
                    Cliente_Reserva reserva = crearReservaCanceladaParaAdapter(cancelada);
                    if (reserva != null) {
                        filteredReservas.add(reserva);
                    }
                }
                
                Log.d(TAG, "Mostrando " + filteredReservas.size() + " reservas canceladas");
                reservasAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar reservas canceladas: " + e.getMessage(), e);
            });
    }
    
    /**
     * ✅ Crear objeto Cliente_Reserva desde datos de reserva cancelada
     */
    private Cliente_Reserva crearReservaCanceladaParaAdapter(Map<String, Object> canceladaData) {
        try {
            String tourId = (String) canceladaData.get("tourId");
            String titulo = (String) canceladaData.get("tourTitulo");
            String documentId = (String) canceladaData.get("documentId");
            
            // Obtener fecha
            String fecha = "";
            Object fechaObj = canceladaData.get("fechaRealizacion");
            if (fechaObj instanceof Timestamp) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                fecha = sdf.format(((Timestamp) fechaObj).toDate());
            }
            
            String horaInicio = (String) canceladaData.get("horaInicio");
            String horaFin = (String) canceladaData.get("horaFin");
            String hora = horaInicio + " - " + horaFin;
            
            // Obtener fecha de cancelación
            String fechaCancelacion = "";
            Object fechaCancelacionObj = canceladaData.get("fechaCancelacion");
            if (fechaCancelacionObj instanceof Timestamp) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                fechaCancelacion = sdf.format(((Timestamp) fechaCancelacionObj).toDate());
            }
            
            // Crear tour para la reserva
            Cliente_Tour tour = new Cliente_Tour();
            tour.setId(tourId != null ? tourId : documentId);
            tour.setTitle(titulo != null ? titulo : "Tour Cancelado");
            tour.setCompanyName("Cargando...");
            tour.setDuration(fechaCancelacion); // Usar duration para guardar fecha de cancelación
            tour.setDate(fecha);
            tour.setPrice(0.0);
            
            // Crear reserva con el tour
            Cliente_Reserva reserva = new Cliente_Reserva();
            reserva.setId(tourId != null ? tourId : documentId);
            reserva.setTour(tour);
            reserva.setFecha(fecha);
            reserva.setHoraInicio(horaInicio != null ? horaInicio : "");
            reserva.setHoraFin(horaFin != null ? horaFin : "");
            reserva.setEstado("Cancelada");
            
            // Guardar datos adicionales en la reserva para el diálogo
            reserva.setDocumentId(documentId);
            
            // Cargar datos del tour (nombre de empresa e imagen) de forma asíncrona
            if (tourId != null) {
                cargarDatosTourParaCancelada(tourId, reserva);
            }
            
            return reserva;
            
        } catch (Exception e) {
            Log.e(TAG, "Error al crear reserva cancelada para adapter: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * ✅ Cargar datos del tour (nombre de empresa e imagen) para reserva cancelada
     */
    private void cargarDatosTourParaCancelada(String tourId, Cliente_Reserva reserva) {
        db.collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    // Cargar nombre de empresa
                    String nombreEmpresa = doc.getString("nombreEmpresa");
                    if (nombreEmpresa != null) {
                        reserva.setEmpresaNombre(nombreEmpresa);
                    }
                    
                    // Cargar imagen del tour (campo correcto: imagenPrincipal)
                    String imagenUrl = doc.getString("imagenPrincipal");
                    if (imagenUrl != null && !imagenUrl.isEmpty()) {
                        Cliente_Tour tour = reserva.getTour();
                        if (tour != null) {
                            tour.setImageUrl(imagenUrl);
                        }
                    }
                    
                    reservasAdapter.notifyDataSetChanged();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error cargando datos del tour: " + e.getMessage());
                reserva.setEmpresaNombre("Empresa no disponible");
                reservasAdapter.notifyDataSetChanged();
            });
    }

    /**
     * ✅ VALIDAR TOURS SIMULTÁNEOS - Prevenir que el cliente tenga múltiples tours activos el mismo día
     */
    private boolean validarTourSimultaneo(QueryDocumentSnapshot nuevoTour, List<Cliente_Reserva> reservasExistentes) {
        try {
            // Obtener fecha y horas del nuevo tour
            Timestamp fechaNuevoTour = nuevoTour.getTimestamp("fechaRealizacion");
            String horaInicioNuevo = nuevoTour.getString("horaInicio");
            String horaFinNuevo = nuevoTour.getString("horaFin");
            
            if (fechaNuevoTour == null || horaInicioNuevo == null || horaFinNuevo == null) {
                return true; // Si no hay datos completos, permitir
            }
            
            Calendar calNuevoTour = Calendar.getInstance();
            calNuevoTour.setTime(fechaNuevoTour.toDate());
            calNuevoTour.set(Calendar.HOUR_OF_DAY, 0);
            calNuevoTour.set(Calendar.MINUTE, 0);
            calNuevoTour.set(Calendar.SECOND, 0);
            calNuevoTour.set(Calendar.MILLISECOND, 0);
            
            // Convertir horas a minutos para comparación fácil
            int inicioNuevoMinutos = convertirHoraAMinutos(horaInicioNuevo);
            int finNuevoMinutos = convertirHoraAMinutos(horaFinNuevo);
            
            // Verificar si hay solapamiento de horarios con reservas existentes
            for (Cliente_Reserva reservaExistente : reservasExistentes) {
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
                        
                        // Si las fechas coinciden, verificar horarios
                        if (calNuevoTour.getTimeInMillis() == calExistente.getTimeInMillis()) {
                            // Obtener horarios de la reserva existente
                            String horaInicioExistente = reservaExistente.getHoraInicio();
                            String horaFinExistente = reservaExistente.getHoraFin();
                            
                            if (horaInicioExistente != null && horaFinExistente != null) {
                                int inicioExistenteMinutos = convertirHoraAMinutos(horaInicioExistente);
                                int finExistenteMinutos = convertirHoraAMinutos(horaFinExistente);
                                
                                // Verificar si los horarios se solapan
                                boolean seSuperponen = !(finNuevoMinutos <= inicioExistenteMinutos || 
                                                         inicioNuevoMinutos >= finExistenteMinutos);
                                
                                if (seSuperponen) {
                                    Log.w(TAG, "Conflicto de horarios detectado: " + nuevoTour.getId() + 
                                        " (" + horaInicioNuevo + "-" + horaFinNuevo + ") se solapa con reserva existente " +
                                        "(" + horaInicioExistente + "-" + horaFinExistente + ")");
                                    return false;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al parsear fecha de reserva existente: " + reservaExistente.getFecha(), e);
                    }
                }
            }
            
            return true; // No hay conflictos de horarios
            
        } catch (Exception e) {
            Log.e(TAG, "Error en validación de tours simultáneos", e);
            return true; // En caso de error, permitir el tour
        }
    }
    
    /**
     * Convierte una hora en formato "HH:mm" a minutos desde medianoche
     */
    private int convertirHoraAMinutos(String hora) {
        try {
            String[] partes = hora.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = Integer.parseInt(partes[1]);
            return horas * 60 + minutos;
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir hora: " + hora, e);
            return 0;
        }
    }


}
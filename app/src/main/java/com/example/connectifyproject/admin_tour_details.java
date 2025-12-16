package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.adapters.AdminItinerarioAdapter;
import com.example.connectifyproject.adapters.AdminServiciosAdapter;
import com.example.connectifyproject.databinding.AdminTourDetailsViewBinding;
import com.example.connectifyproject.models.Cliente_ItinerarioItem;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class admin_tour_details extends AppCompatActivity implements OnMapReadyCallback, AdminItinerarioAdapter.OnItinerarioItemClickListener {
    private AdminTourDetailsViewBinding binding;
    private GoogleMap mGoogleMap;
    private String tourId;
    private String tourTipo;  // "borrador" o "publicado"
    private String tourTitulo;
    private String tourEstado;
    private boolean tourEsPublicado;
    private LatLng tourLocation;
    private String guiaAsignadoId;
    private String guiaSeleccionadoId;  // Para tours pendientes
    private Map<String, Object> guiaAsignadoData;  // Para tours confirmados
    private List<Map<String, Object>> participantesData;  // Para tours confirmados
    private Timestamp tourFechaRealizacion;  // Para validar tiempo de cancelación
    private String tourHoraInicio;  // Para validar tiempo de cancelación
    
    // Firebase
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;
    
    // Adapters para las listas
    private AdminItinerarioAdapter itinerarioAdapter;
    private com.example.connectifyproject.adapters.AdminServiciosAdapter serviciosAdapter;
    private com.example.connectifyproject.adapters.TourImageAdapter imageAdapter;
    private List<Cliente_ItinerarioItem> itinerarioItems;
    private List<Map<String, Object>> serviciosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminTourDetailsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        // Obtener datos del Intent
        tourId = getIntent().getStringExtra("tour_id");
        tourTitulo = getIntent().getStringExtra("tour_titulo");
        tourEstado = getIntent().getStringExtra("tour_estado");
        tourTipo = getIntent().getStringExtra("tour_tipo"); // "borrador" o "publicado"
        tourEsPublicado = getIntent().getBooleanExtra("tour_es_publicado", false);

        if (tourTitulo == null) {
            tourTitulo = "Tour de ejemplo";
        }
        
        if (tourId == null) {
            Toast.makeText(this, "Error: ID de tour no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar toolbar
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Cargar datos del tour desde Firebase (configura tabs después de cargar)
        loadTourData();

        // Configurar mapa
        initializeMap();

        // No mostrar bottom navigation en pantallas secundarias
        // setupBottomNavigation();
    }
    
    private void loadTourData() {
        // Determinar la colección según el tipo de tour
        String collection;
        if ("confirmado".equals(tourTipo) || "en_curso".equals(tourTipo) || 
            "check_in".equals(tourTipo) || "check_out".equals(tourTipo) ||
            "completado".equals(tourTipo)) {
            collection = "tours_asignados";
        } else if ("cancelado".equals(tourTipo)) {
            // ✅ Tours cancelados están en tours_asignados
            collection = "tours_asignados";
        } else if ("borrador".equals(tourTipo)) {
            collection = "tours_borradores";
        } else {
            collection = "tours_ofertas";
        }
        
        db.collection(collection).document(tourId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // ✅ Detectar tours sin asignar o pendientes
                    String tipoFirebase = documentSnapshot.getString("tipo");
                    String guiaId = documentSnapshot.getString("guiaAsignadoId");
                    
                    if (tipoFirebase != null) {
                        tourTipo = tipoFirebase;
                        setupTabs();
                        continueLoadingTourData(documentSnapshot);
                    } else if ("tours_ofertas".equals(collection) && guiaId == null) {
                        // Verificar si tiene ofertas pendientes en subcolección
                        db.collection("tours_ofertas")
                            .document(tourId)
                            .collection("guias_ofertados")
                            .whereEqualTo("estadoOferta", "pendiente")
                            .limit(1)
                            .get()
                            .addOnSuccessListener(ofertasSnapshot -> {
                                if (!ofertasSnapshot.isEmpty()) {
                                    // Tiene ofertas pendientes = pendiente
                                    tourTipo = "pendiente";
                                } else {
                                    // Sin ofertas = sin_asignar
                                    tourTipo = "sin_asignar";
                                }
                                setupTabs();
                                continueLoadingTourData(documentSnapshot);
                            })
                            .addOnFailureListener(e -> {
                                // En caso de error, asumir sin_asignar
                                tourTipo = "sin_asignar";
                                setupTabs();
                                continueLoadingTourData(documentSnapshot);
                            });
                        return; // Salir para esperar respuesta de subcolección
                    } else {
                        setupTabs();
                        continueLoadingTourData(documentSnapshot);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar tour: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void continueLoadingTourData(DocumentSnapshot documentSnapshot) {
        // Cargar datos básicos
        String titulo = documentSnapshot.getString("titulo");
                    String descripcion = documentSnapshot.getString("descripcion");
                    
                    // Manejar fechaRealizacion como String o Timestamp
                    String fechaRealizacion = null;
                    try {
                        Timestamp fechaTimestamp = documentSnapshot.getTimestamp("fechaRealizacion");
                        if (fechaTimestamp != null) {
                            fechaRealizacion = dateFormat.format(fechaTimestamp.toDate());
                        }
                    } catch (Exception e) {
                        fechaRealizacion = documentSnapshot.getString("fechaRealizacion");
                    }
                    
                    Double precio = documentSnapshot.getDouble("precio");
                    String horaInicio = documentSnapshot.getString("horaInicio");
                    String horaFin = documentSnapshot.getString("horaFin");
                    String duracion = documentSnapshot.getString("duracion");
                    guiaAsignadoId = documentSnapshot.getString("guiaAsignadoId");
                    
                    // ✅ Guardar fecha y hora para validación de cancelación (manejar diferentes tipos)
                    Object fechaObj = documentSnapshot.get("fechaRealizacion");
                    if (fechaObj instanceof com.google.firebase.Timestamp) {
                        tourFechaRealizacion = (com.google.firebase.Timestamp) fechaObj;
                    } else if (fechaObj instanceof String) {
                        // Si es String, intentar convertir a Timestamp
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                            java.util.Date date = sdf.parse((String) fechaObj);
                            tourFechaRealizacion = new com.google.firebase.Timestamp(date);
                        } catch (Exception e) {
                            tourFechaRealizacion = null;
                        }
                    }
                    tourHoraInicio = horaInicio;
                    
                    // Cargar imágenes en galería horizontal
                    List<String> imagenesUrls = (List<String>) documentSnapshot.get("imagenesUrls");
                    String estadoActual = documentSnapshot.getString("estado");
                    
                    // Cargar imágenes directamente si existen
                    if (imagenesUrls != null && !imagenesUrls.isEmpty()) {
                        imageAdapter = new com.example.connectifyproject.adapters.TourImageAdapter();
                        binding.recyclerViewImagenes.setLayoutManager(
                            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        );
                        binding.recyclerViewImagenes.setAdapter(imageAdapter);
                        imageAdapter.setImages(imagenesUrls);
                    } else if ((imagenesUrls == null || imagenesUrls.isEmpty()) && "confirmado".equals(tourTipo)) {
                        // Solo para confirmados, intentar cargar desde la oferta original
                        String ofertaTourId = documentSnapshot.getString("ofertaTourId");
                        if (ofertaTourId != null) {
                            cargarImagenesDesdeOferta(ofertaTourId);
                        }
                    }
                    
                    // Cargar itinerario
                    List<Map<String, Object>> itinerarioData = (List<Map<String, Object>>) documentSnapshot.get("itinerario");
                    if (itinerarioData != null && !itinerarioData.isEmpty()) {
                        itinerarioItems = new ArrayList<>();
                        for (Map<String, Object> punto : itinerarioData) {
                            String nombre = (String) punto.get("nombre");
                            String direccion = (String) punto.get("direccion");
                            Double latitud = (Double) punto.get("latitud");
                            Double longitud = (Double) punto.get("longitud");
                            List<String> actividades = (List<String>) punto.get("actividades");
                            
                            if (nombre != null && latitud != null && longitud != null) {
                                Cliente_ItinerarioItem item = new Cliente_ItinerarioItem(
                                    "",  // hora (vacío por ahora)
                                    nombre,
                                    direccion != null ? direccion : "",
                                    latitud,
                                    longitud
                                );
                                if (actividades != null && !actividades.isEmpty()) {
                                    item.setActividades(actividades);
                                }
                                itinerarioItems.add(item);
                            }
                        }
                    }
                    
                    // Cargar servicios adicionales
                    List<Map<String, Object>> serviciosData = (List<Map<String, Object>>) documentSnapshot.get("serviciosAdicionales");
                    if (serviciosData != null && !serviciosData.isEmpty()) {
                        serviciosList = new ArrayList<>(serviciosData);
                    }
                    
                    // Actualizar UI con los datos cargados
                    if (titulo != null) binding.tvTourNombre.setText(titulo);
                    if (descripcion != null) binding.tvTourDescripcion.setText(descripcion);
                    
                    // ✅ Mostrar motivo de cancelación y fecha si el tour está cancelado
                    String estadoTour = documentSnapshot.getString("estado");
                    if ("cancelado".equalsIgnoreCase(estadoTour)) {
                        String motivoCancelacion = documentSnapshot.getString("motivoCancelacion");
                        Timestamp fechaCancelacionTs = documentSnapshot.getTimestamp("fechaCancelacion");
                        String fechaCancelacionStr = "";
                        if (fechaCancelacionTs != null) {
                            fechaCancelacionStr = dateFormat.format(fechaCancelacionTs.toDate());
                        }
                        
                        if (motivoCancelacion != null && !motivoCancelacion.isEmpty()) {
                            String tipoCancelacion = motivoCancelacion.toLowerCase().contains("manual") ? "manual" : "automática";
                            String mensajeCancelacion = "\n\n❌ Cancelación " + tipoCancelacion + ": " + motivoCancelacion;
                            if (!fechaCancelacionStr.isEmpty()) {
                                mensajeCancelacion += "\n📅 Fecha de cancelación: " + fechaCancelacionStr;
                            }
                            binding.tvTourDescripcion.setText(descripcion + mensajeCancelacion);
                        }
                    }
                    
                    if (fechaRealizacion != null) {
                        binding.tvFecha.setText(fechaRealizacion);
                    }
                    String ciudad = documentSnapshot.getString("ciudad");
                    binding.tvCiudad.setText(ciudad != null && !ciudad.isEmpty() ? ciudad : "No especificado");
                    if (precio != null) binding.tvCostoPorPersona.setText("S/ " + precio.intValue());
                    if (horaInicio != null && horaFin != null) {
                        binding.tvHorario.setText(horaInicio + " - " + horaFin);
                    }
                    if (duracion != null) {
                        binding.tvDuracion.setText(duracion + " hrs");
                    }
                    
                    // Configurar adaptador de servicios en Info
                    if (serviciosList != null && !serviciosList.isEmpty()) {
                        serviciosAdapter = new AdminServiciosAdapter(serviciosList);
                        binding.recyclerViewServicios.setLayoutManager(new LinearLayoutManager(this));
                        binding.recyclerViewServicios.setAdapter(serviciosAdapter);
                    }
                    
                    // ✅ Configurar badge de estado (usar estado real de Firebase)
                    String estadoReal = documentSnapshot.getString("estado");
                    String estadoMostrar = estadoReal != null ? estadoReal.toUpperCase() : tourEstado;
                    binding.tvEstadoBadge.setText(estadoMostrar);
                    
                    // Color según estado
                    if ("cancelado".equalsIgnoreCase(estadoReal)) {
                        binding.tvEstadoBadge.setBackgroundColor(getColor(android.R.color.holo_red_dark));
                    } else if (tourEsPublicado) {
                        binding.tvEstadoBadge.setBackgroundColor(getColor(R.color.success_500));
                    } else {
                        binding.tvEstadoBadge.setBackgroundColor(getColor(R.color.text_secondary));
                    }
                    
                    // Guardar información del guía según el tipo de tour (se cargará en setupGuiaContent)
                    if ("confirmado".equals(tourTipo) || "en_curso".equals(tourTipo) || 
                        "check_in".equals(tourTipo) || "check_out".equals(tourTipo)) {
                        // Para tours confirmados y en curso, guardar guía del campo guiaAsignado
                        guiaAsignadoData = (Map<String, Object>) documentSnapshot.get("guiaAsignado");
                        // Guardar participantes del campo participantes
                        participantesData = (List<Map<String, Object>>) documentSnapshot.get("participantes");
                    } else if ("cancelado".equals(tourTipo)) {
                        // Para tours cancelados, guardar guía del campo guiaAsignado (aún está asignado)
                        guiaAsignadoData = (Map<String, Object>) documentSnapshot.get("guiaAsignado");
                        // No cargar participantes para cancelados (array vacío)
                    } else if ("pendiente".equals(tourTipo)) {
                        // Para tours pendientes, guardar ID del guía seleccionado
                        guiaSeleccionadoId = documentSnapshot.getString("guiaSeleccionadoActual");
                    }
                    
                    // Configurar botón "Seleccionar Guía"
                    setupButtons();
                    
                    // Si hay itinerario, actualizar el mapa
                    if (itinerarioItems != null && !itinerarioItems.isEmpty()) {
                        Cliente_ItinerarioItem primerPunto = itinerarioItems.get(0);
                        tourLocation = new LatLng(primerPunto.getLatitude(), primerPunto.getLongitude());
                        
                        // Actualizar el mapa si ya está listo
                        if (mGoogleMap != null) {
                            addItinerarioMarkersToMap();
                        }
                    }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragmentTour);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        
        // Configurar mapa
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        
        // Establecer ubicación según el tour
        setTourLocationOnMap();
    }

    private void setTourLocationOnMap() {
        // Usar el primer punto del itinerario si está disponible
        if (itinerarioItems != null && !itinerarioItems.isEmpty()) {
            // Agregar marcadores del itinerario
            addItinerarioMarkersToMap();
        } else {
            // Ubicación por defecto si no hay itinerario
            if (mGoogleMap != null && tourLocation != null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tourLocation, 14));
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(tourLocation)
                        .title(tourTitulo != null ? tourTitulo : "Ubicación del tour"));
            }
        }
    }
    
    private void addItinerarioMarkersToMap() {
        if (mGoogleMap != null && itinerarioItems != null && !itinerarioItems.isEmpty()) {
            // Limpiar marcadores anteriores
            mGoogleMap.clear();
            
            // Crear polyline para conectar los puntos
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(getResources().getColor(R.color.brand_purple_dark))
                    .width(8);
            
            // Builder para ajustar la cámara a todos los puntos
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            
            // Agregar marcadores para cada punto del itinerario
            for (int i = 0; i < itinerarioItems.size(); i++) {
                Cliente_ItinerarioItem item = itinerarioItems.get(i);
                LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
                
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title((i + 1) + ". " + item.getPlaceName())
                        .snippet(item.getDescription()));
                
                // Agregar punto a la polilínea
                polylineOptions.add(position);
                
                // Agregar punto al bounds
                boundsBuilder.include(position);
            }
            
            // Dibujar la línea conectando todos los puntos si hay más de uno
            if (itinerarioItems.size() > 1) {
                mGoogleMap.addPolyline(polylineOptions);
            }
            
            // Ajustar la cámara para mostrar todos los puntos
            try {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 150; // padding en píxeles
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                // Si hay un error, centrar en el primer punto
                LatLng firstPoint = new LatLng(itinerarioItems.get(0).getLatitude(), itinerarioItems.get(0).getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 12));
            }
        }
    }

    private void setupButtons() {
        // Mostrar botón "Seleccionar Guía" solo si el tour está publicado o sin asignar
        if ("publicado".equals(tourTipo) || "sin_asignar".equals(tourTipo)) {
            binding.btnAsignarGuia.setVisibility(View.VISIBLE);
            binding.btnAsignarGuia.setOnClickListener(v -> {
                // Navegar a la vista de selección de guías
                Intent intent = new Intent(this, admin_select_guide.class);
                intent.putExtra("ofertaId", tourId);
                intent.putExtra("tourTitulo", tourTitulo);
                startActivity(intent);
            });
        } else {
            // Ocultar botón para tours en borrador o ya asignados
            binding.btnAsignarGuia.setVisibility(View.GONE);
        }
        
        // Mostrar botón "Cancelar Tour" solo si el tour está confirmado/pendiente/programado
        // Y faltan MÁS de 2 horas para el inicio
        if (tourEstado != null && (tourEstado.equalsIgnoreCase("confirmado") || 
            tourEstado.equalsIgnoreCase("pendiente") || 
            tourEstado.equalsIgnoreCase("programado"))) {
            
            // Validar que falten más de 2 horas
            if (tourFechaRealizacion != null && tourHoraInicio != null) {
                double horasRestantes = com.example.connectifyproject.utils.TourTimeValidator
                    .calcularHorasHastaInicio(tourFechaRealizacion, tourHoraInicio);
                
                if (horasRestantes > 2.0) {
                    binding.btnCancelarTour.setVisibility(View.VISIBLE);
                    binding.btnCancelarTour.setOnClickListener(v -> {
                        // Revalidar tiempo al hacer click
                        double horasRestantesClick = com.example.connectifyproject.utils.TourTimeValidator
                            .calcularHorasHastaInicio(tourFechaRealizacion, tourHoraInicio);
                        
                        if (horasRestantesClick > 2.0) {
                            mostrarDialogoCancelacion();
                        } else {
                            Toast.makeText(this, 
                                "⚠️ No se puede cancelar. Solo se permite cancelación hasta 2 horas antes del inicio.", 
                                Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    binding.btnCancelarTour.setVisibility(View.GONE);
                }
            } else {
                binding.btnCancelarTour.setVisibility(View.GONE);
            }
        } else {
            binding.btnCancelarTour.setVisibility(View.GONE);
        }
    }
    
    /**
     * Mostrar diálogo de confirmación para cancelar tour
     */
    private void mostrarDialogoCancelacion() {
        // ✅ Primero verificar que el tour aún esté en estado válido
        db.collection("tours_asignados").document(tourId).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(this, "❌ El tour no existe", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String estadoActual = doc.getString("estado");
                if (estadoActual == null || (!estadoActual.equals("confirmado") && 
                    !estadoActual.equals("pendiente") && !estadoActual.equals("programado"))) {
                    Toast.makeText(this, 
                        "⚠️ Este tour ya no se puede cancelar (estado: " + estadoActual + ")", 
                        Toast.LENGTH_LONG).show();
                    // Redirigir a admin_tours
                    Intent intent = new Intent(this, admin_tours.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return;
                }
                
                // Estado válido, mostrar diálogo
                mostrarDialogoConfirmacionCancelacion();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al verificar estado del tour", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void mostrarDialogoConfirmacionCancelacion() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("⚠️ Cancelar Tour");
        builder.setMessage("¿Estás seguro de que deseas cancelar este tour?\n\n" +
                "• Se notificará a todos los participantes\n" +
                "• Se registrará un pago del 15% al guía como compensación\n" +
                "• Las reservas se moverán a canceladas");
        
        builder.setPositiveButton("Sí, cancelar", (dialog, which) -> {
            ejecutarCancelacion("Cancelación manual");
        });
        
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }
    
    /**
     * Ejecutar cancelación del tour usando TourFirebaseService
     */
    private void ejecutarCancelacion(String motivo) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Cancelando tour...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        com.example.connectifyproject.services.TourFirebaseService tourService = 
            new com.example.connectifyproject.services.TourFirebaseService();
        
        tourService.cancelarTourManual(tourId, motivo, new com.example.connectifyproject.services.TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                progressDialog.dismiss();
                Toast.makeText(admin_tour_details.this, 
                    "✅ " + message, Toast.LENGTH_LONG).show();
                
                // Redirigir a admin_tours para que se actualice la lista
                Intent intent = new Intent(admin_tour_details.this, admin_tours.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            
            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(admin_tour_details.this, 
                    "❌ " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupTabs() {
        // Limpiar tabs existentes antes de agregar nuevos
        binding.tabLayoutDetails.removeAllTabs();
        
        binding.tabLayoutDetails.addTab(binding.tabLayoutDetails.newTab().setText("Info"));
        binding.tabLayoutDetails.addTab(binding.tabLayoutDetails.newTab().setText("Itinerario"));
        
        // ✅ Agregar pestaña Guía para tours sin_asignar, pendiente, confirmado, en_curso, check_out y cancelado
        if ("sin_asignar".equals(tourTipo) || "pendiente".equals(tourTipo) || 
            "confirmado".equals(tourTipo) || "en_curso".equals(tourTipo) || 
            "check_in".equals(tourTipo) || "check_out".equals(tourTipo) || 
            "cancelado".equals(tourTipo)) {
            binding.tabLayoutDetails.addTab(binding.tabLayoutDetails.newTab().setText("Guía"));
        }
        
        // Agregar pestaña Participantes para tours confirmados, en_curso, check_in y check_out
        if ("confirmado".equals(tourTipo) || "en_curso".equals(tourTipo) || 
            "check_in".equals(tourTipo) || "check_out".equals(tourTipo)) {
            binding.tabLayoutDetails.addTab(binding.tabLayoutDetails.newTab().setText("Participantes"));
        }

        binding.tabLayoutDetails.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabText = tab.getText().toString();
                showTabContent(tabText);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        // Mostrar contenido inicial (Info)
        showTabContent("Info");
    }

    private void showTabContent(String tabName) {
        // Ocultar todas las secciones primero
        binding.layoutInfoSection.setVisibility(View.GONE);
        binding.layoutItinerarioSection.setVisibility(View.GONE);
        binding.layoutGuiaSection.setVisibility(View.GONE);
        binding.layoutParticipantesSection.setVisibility(View.GONE);
        binding.layoutServiciosSection.setVisibility(View.GONE);
        binding.layoutMapaSection.setVisibility(View.GONE);

        switch (tabName) {
            case "Info":
                binding.layoutInfoSection.setVisibility(View.VISIBLE);
                setupInfoContent();
                break;
            case "Itinerario":
                binding.layoutItinerarioSection.setVisibility(View.VISIBLE);
                binding.layoutMapaSection.setVisibility(View.VISIBLE);
                setupItinerarioContent();
                break;
            case "Guía":
                binding.layoutGuiaSection.setVisibility(View.VISIBLE);
                setupGuiaContent();
                break;
            case "Participantes":
                binding.layoutParticipantesSection.setVisibility(View.VISIBLE);
                setupParticipantesContent();
                break;
        }
    }

    private void setupInfoContent() {
        // Esta información ya está configurada en setupTourInfo()
        // Aquí podemos agregar más detalles específicos de la sección Info
    }

    private void setupItinerarioContent() {
        // Configurar RecyclerView para el itinerario
        if (itinerarioItems == null) {
            itinerarioItems = new ArrayList<>();
            
            // Datos de ejemplo del itinerario (basado en el código del guía)
            // Coordenadas para el tour de Lima
            LatLng PLAZA_MAYOR = new LatLng(-12.0464, -77.0428);
            LatLng CATEDRAL_LIMA = new LatLng(-12.0464, -77.0425);
            LatLng PALACIO_GOBIERNO = new LatLng(-12.0462, -77.0431);
            LatLng CASA_ALIAGA = new LatLng(-12.0465, -77.0426);
            LatLng MIRAFLORES = new LatLng(-12.1196, -77.0282);
            
            itinerarioItems.add(new Cliente_ItinerarioItem("09:00", "Plaza Mayor", 
                "Inicio del tour en el corazón de Lima colonial", 
                PLAZA_MAYOR.latitude, PLAZA_MAYOR.longitude));
            itinerarioItems.add(new Cliente_ItinerarioItem("09:30", "Catedral de Lima", 
                "Visita a la catedral metropolitana", 
                CATEDRAL_LIMA.latitude, CATEDRAL_LIMA.longitude));
            itinerarioItems.add(new Cliente_ItinerarioItem("10:30", "Palacio de Gobierno", 
                "Tour por la Casa de Pizarro", 
                PALACIO_GOBIERNO.latitude, PALACIO_GOBIERNO.longitude));
            itinerarioItems.add(new Cliente_ItinerarioItem("11:30", "Casa Aliaga", 
                "Mansión colonial más antigua de América", 
                CASA_ALIAGA.latitude, CASA_ALIAGA.longitude));
            itinerarioItems.add(new Cliente_ItinerarioItem("15:00", "Miraflores", 
                "Malecón y parques de Miraflores", 
                MIRAFLORES.latitude, MIRAFLORES.longitude));
        }
        
        if (itinerarioAdapter == null) {
            itinerarioAdapter = new AdminItinerarioAdapter(itinerarioItems, this);
            binding.recyclerViewItinerario.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerViewItinerario.setAdapter(itinerarioAdapter);
        }
    }

    private void setupGuiaContent() {
        // Ocultar todas las secciones primero
        binding.layoutGuiaAsignada.setVisibility(View.GONE);
        binding.tvGuiaNoAsignada.setVisibility(View.GONE);
        binding.layoutTimelineGuia.setVisibility(View.GONE);
        
        android.util.Log.d("AdminTourDetails", "setupGuiaContent - tourTipo: " + tourTipo);
        
        // Cargar información del guía según el tipo de tour
        if ("confirmado".equals(tourTipo) || "en_curso".equals(tourTipo) || 
            "check_in".equals(tourTipo) || "check_out".equals(tourTipo) || 
            "cancelado".equals(tourTipo)) {
            // Para tours confirmados, en curso y cancelados, obtener guía del campo guiaAsignado
            if (guiaAsignadoData != null) {
                android.util.Log.d("AdminTourDetails", "Mostrando info guía " + tourTipo);
                mostrarInfoGuiaConfirmado(guiaAsignadoData);
            } else {
                binding.tvGuiaNoAsignada.setVisibility(View.VISIBLE);
                binding.tvGuiaNoAsignada.setText("No hay información del guía");
            }
        } else if ("pendiente".equals(tourTipo)) {
            // Para tours pendientes, cargar guía de usuarios
            if (guiaSeleccionadoId != null && !guiaSeleccionadoId.isEmpty()) {
                android.util.Log.d("AdminTourDetails", "Cargando info de guía pendiente: " + guiaSeleccionadoId);
                cargarInfoGuiaPendiente(guiaSeleccionadoId);
            } else {
                binding.tvGuiaNoAsignada.setVisibility(View.VISIBLE);
                binding.tvGuiaNoAsignada.setText("No hay guía seleccionado");
            }
        } else if ("sin_asignar".equals(tourTipo)) {
            // Para tours sin asignar, mostrar mensaje
            binding.tvGuiaNoAsignada.setVisibility(View.VISIBLE);
            binding.tvGuiaNoAsignada.setText("Aún no se ha seleccionado un guía para este tour");
            android.util.Log.d("AdminTourDetails", "Tour sin asignar - mostrando mensaje");
        } else if (guiaAsignadoId != null && !guiaAsignadoId.isEmpty()) {
            // Para otros tipos con guía asignado
            android.util.Log.d("AdminTourDetails", "Cargando desde guiaAsignadoId: " + guiaAsignadoId);
            binding.layoutGuiaAsignada.setVisibility(View.VISIBLE);
            
            // Cargar datos reales del guía desde Firebase
            db.collection("usuarios").document(guiaAsignadoId).get()
                .addOnSuccessListener(docGuia -> {
                    if (docGuia.exists()) {
                        // Cargar nombre
                        String nombre = docGuia.getString("nombre");
                        String apellido = docGuia.getString("apellido");
                        if (nombre != null && apellido != null) {
                            binding.tvGuiaNombre.setText(nombre + " " + apellido);
                        }
                        
                        // Cargar correo
                        String email = docGuia.getString("email");
                        if (email != null && !email.isEmpty()) {
                            binding.tvGuiaCorreo.setText("📧 " + email);
                            binding.tvGuiaCorreo.setVisibility(View.VISIBLE);
                        }
                        
                        // Cargar teléfono
                        String telefono = docGuia.getString("telefono");
                        if (telefono != null && !telefono.isEmpty()) {
                            binding.tvGuiaTelefono.setText("📞 " + telefono);
                            binding.tvGuiaTelefono.setVisibility(View.VISIBLE);
                        }
                        
                        // Cargar foto del guía
                        String fotoUrl = docGuia.getString("fotoPerfil");
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(this)
                                .load(fotoUrl)
                                .placeholder(R.drawable.ic_person)
                                .circleCrop()
                                .into(binding.ivGuiaAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos del guía", Toast.LENGTH_SHORT).show();
                });
        }
    }

    @Override
    public void onItinerarioItemClick(Cliente_ItinerarioItem item) {
        // Cuando se hace clic en un item del itinerario, centrar el mapa en esa ubicación
        if (mGoogleMap != null) {
            LatLng location = new LatLng(item.getLatitude(), item.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
        }
        
        // Mostrar diálogo con actividades si existen
        List<String> actividades = item.getActividades();
        if (actividades != null && !actividades.isEmpty()) {
            mostrarDialogoActividades(item.getPlaceName(), actividades);
        } else {
            Toast.makeText(this, "No hay actividades registradas para este punto", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void mostrarDialogoActividades(String nombrePunto, List<String> actividades) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Actividades en ").append(nombrePunto).append(":\n\n");
        
        for (int i = 0; i < actividades.size(); i++) {
            mensaje.append(actividades.get(i));
            if (i < actividades.size() - 1) {
                mensaje.append("\n");
            }
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Actividades del Itinerario")
            .setMessage(mensaje.toString())
            .setPositiveButton("Cerrar", null)
            .show();
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("tours");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }
    
    /**
     * Cargar imágenes desde la oferta original (para tours confirmados sin imágenes)
     */
    private void cargarImagenesDesdeOferta(String ofertaTourId) {
        db.collection("tours_ofertas").document(ofertaTourId).get()
            .addOnSuccessListener(ofertaDoc -> {
                if (ofertaDoc.exists()) {
                    List<String> imagenesUrls = (List<String>) ofertaDoc.get("imagenesUrls");
                    if (imagenesUrls != null && !imagenesUrls.isEmpty()) {
                        imageAdapter = new com.example.connectifyproject.adapters.TourImageAdapter();
                        binding.recyclerViewImagenes.setLayoutManager(
                            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        );
                        binding.recyclerViewImagenes.setAdapter(imageAdapter);
                        imageAdapter.setImages(imagenesUrls);
                    }
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminTourDetails", "Error al cargar imágenes desde oferta", e);
            });
    }
    
    /**
     * Mostrar información del guía para tours confirmados
     * El guía ya está en el documento como objeto guiaAsignado
     */
    private void mostrarInfoGuiaConfirmado(Map<String, Object> guiaAsignado) {
        android.util.Log.d("AdminTourDetails", "mostrarInfoGuiaConfirmado llamado");
        
        // NO hacer visible aquí - el sistema de tabs controla la visibilidad
        // Solo preparar los datos
        binding.layoutGuiaAsignada.setVisibility(View.VISIBLE);
        binding.tvGuiaNoAsignada.setVisibility(View.GONE);
        
        // Obtener datos del guía
        String nombresCompletos = (String) guiaAsignado.get("nombresCompletos");
        String correoElectronico = (String) guiaAsignado.get("correoElectronico");
        String numeroTelefono = (String) guiaAsignado.get("numeroTelefono");
        String identificadorUsuario = (String) guiaAsignado.get("identificadorUsuario");
        
        android.util.Log.d("AdminTourDetails", "Nombre: " + nombresCompletos);
        android.util.Log.d("AdminTourDetails", "Correo: " + correoElectronico);
        android.util.Log.d("AdminTourDetails", "Teléfono: " + numeroTelefono);
        
        // Mostrar información
        if (nombresCompletos != null) {
            binding.tvGuiaNombre.setText(nombresCompletos);
            binding.tvGuiaNombre.setVisibility(View.VISIBLE);
        }
        
        if (correoElectronico != null) {
            binding.tvGuiaCorreo.setText("📧 " + correoElectronico);
            binding.tvGuiaCorreo.setVisibility(View.VISIBLE);
        }
        
        if (numeroTelefono != null) {
            binding.tvGuiaTelefono.setText("📞 " + numeroTelefono);
            binding.tvGuiaTelefono.setVisibility(View.VISIBLE);
        }
        
        // Cargar foto de perfil del guía desde usuarios
        if (identificadorUsuario != null && !identificadorUsuario.isEmpty()) {
            db.collection("usuarios").document(identificadorUsuario).get()
                .addOnSuccessListener(docGuia -> {
                    if (docGuia.exists()) {
                        String photoUrl = docGuia.getString("photoUrl");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                .load(photoUrl)
                                .placeholder(R.drawable.ic_person)
                                .circleCrop()
                                .into(binding.ivGuiaAvatar);
                            android.util.Log.d("AdminTourDetails", "Foto del guía confirmado cargada: " + photoUrl);
                        } else {
                            binding.ivGuiaAvatar.setImageResource(R.drawable.ic_person);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AdminTourDetails", "Error al cargar foto del guía: " + e.getMessage());
                    binding.ivGuiaAvatar.setImageResource(R.drawable.ic_person);
                });
        } else {
            binding.ivGuiaAvatar.setImageResource(R.drawable.ic_person);
        }
        
        // Mostrar badge de confirmado
        binding.tvGuiaEstadoBadge.setText("✓ Confirmado");
        binding.tvGuiaEstadoBadge.setVisibility(View.VISIBLE);
        binding.tvGuiaEstadoBadge.setTextColor(getColor(R.color.success_500));
        binding.tvGuiaEstadoBadge.setBackgroundTintList(getColorStateList(R.color.success_50));
    }
    
    /**
     * Cargar información del guía para tours pendientes
     * Busca el guía en la colección usuarios y muestra con badge de pendiente
     */
    private void cargarInfoGuiaPendiente(String guiaId) {
        android.util.Log.d("AdminTourDetails", "cargarInfoGuiaPendiente iniciado para: " + guiaId);
        db.collection("usuarios").document(guiaId).get()
            .addOnSuccessListener(docGuia -> {
                android.util.Log.d("AdminTourDetails", "Consulta usuarios exitosa. Existe: " + docGuia.exists());
                if (docGuia.exists()) {
                    // NO hacer visible aquí - el sistema de tabs controla la visibilidad
                    binding.layoutGuiaAsignada.setVisibility(View.VISIBLE);
                    binding.tvGuiaNoAsignada.setVisibility(View.GONE);
                    
                    // Obtener datos del guía - manejar diferentes estructuras
                    String nombreCompleto = null;
                    
                    // Intentar con nombresApellidos (estructura real)
                    String nombresApellidos = docGuia.getString("nombresApellidos");
                    if (nombresApellidos != null && !nombresApellidos.isEmpty()) {
                        nombreCompleto = nombresApellidos;
                    } else {
                        // Intentar con nombresCompletos (por compatibilidad)
                        String nombresCompletos = docGuia.getString("nombresCompletos");
                        if (nombresCompletos != null && !nombresCompletos.isEmpty()) {
                            nombreCompleto = nombresCompletos;
                        } else {
                            // Si no existe, intentar con nombre y apellido separados
                            String nombre = docGuia.getString("nombre");
                            String apellido = docGuia.getString("apellido");
                            if (nombre != null && apellido != null) {
                                nombreCompleto = nombre + " " + apellido;
                            } else if (nombre != null) {
                                nombreCompleto = nombre;
                            }
                        }
                    }
                    
                    String email = docGuia.getString("email");
                    String telefono = docGuia.getString("telefono");
                    
                    android.util.Log.d("AdminTourDetails", "Datos guía pendiente - Nombre completo: " + nombreCompleto);
                    android.util.Log.d("AdminTourDetails", "Email: " + email + ", Teléfono: " + telefono);
                    
                    // Mostrar información
                    if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                        binding.tvGuiaNombre.setText(nombreCompleto);
                        binding.tvGuiaNombre.setVisibility(View.VISIBLE);
                        android.util.Log.d("AdminTourDetails", "Nombre configurado: " + nombreCompleto);
                    }
                    
                    if (email != null) {
                        binding.tvGuiaCorreo.setText("📧 " + email);
                        binding.tvGuiaCorreo.setVisibility(View.VISIBLE);
                        android.util.Log.d("AdminTourDetails", "Email configurado: " + email);
                    }
                    
                    if (telefono != null) {
                        binding.tvGuiaTelefono.setText("📞 " + telefono);
                        binding.tvGuiaTelefono.setVisibility(View.VISIBLE);
                        android.util.Log.d("AdminTourDetails", "Teléfono configurado: " + telefono);
                    }
                    
                    // Cargar foto de perfil del guía
                    String photoUrl = docGuia.getString("photoUrl");
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .into(binding.ivGuiaAvatar);
                        android.util.Log.d("AdminTourDetails", "Foto del guía cargada: " + photoUrl);
                    } else {
                        binding.ivGuiaAvatar.setImageResource(R.drawable.ic_person);
                    }
                    
                    // Mostrar badge de pendiente
                    binding.tvGuiaEstadoBadge.setText("⏱ Pendiente de confirmación");
                    binding.tvGuiaEstadoBadge.setVisibility(View.VISIBLE);
                    binding.tvGuiaEstadoBadge.setTextColor(getColor(R.color.avatar_amber));
                    binding.tvGuiaEstadoBadge.setBackgroundTintList(getColorStateList(R.color.primary_50));
                    
                    android.util.Log.d("AdminTourDetails", "layoutGuiaAsignada visibility: " + binding.layoutGuiaAsignada.getVisibility());
                    android.util.Log.d("AdminTourDetails", "tvGuiaNombre visibility: " + binding.tvGuiaNombre.getVisibility());
                    android.util.Log.d("AdminTourDetails", "Información del guía pendiente configurada correctamente");
                } else {
                    android.util.Log.e("AdminTourDetails", "Documento de usuario no existe: " + guiaId);
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminTourDetails", "Error al cargar datos del guía: " + e.getMessage());
                Toast.makeText(this, "Error al cargar datos del guía", Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Cargar información del guía desde colección usuarios (para otros casos)
     */
    private void cargarInfoGuiaDesdeUsuarios(String guiaId) {
        db.collection("usuarios").document(guiaId).get()
            .addOnSuccessListener(docGuia -> {
                if (docGuia.exists()) {
                    // NO hacer visible aquí - el sistema de tabs controla la visibilidad
                    binding.layoutGuiaAsignada.setVisibility(View.VISIBLE);
                    binding.tvGuiaNoAsignada.setVisibility(View.GONE);
                    
                    // Obtener datos del guía - manejar diferentes estructuras
                    String nombreCompleto = null;
                    
                    // Intentar con nombresApellidos (estructura real)
                    String nombresApellidos = docGuia.getString("nombresApellidos");
                    if (nombresApellidos != null && !nombresApellidos.isEmpty()) {
                        nombreCompleto = nombresApellidos;
                    } else {
                        // Intentar con nombresCompletos (por compatibilidad)
                        String nombresCompletos = docGuia.getString("nombresCompletos");
                        if (nombresCompletos != null && !nombresCompletos.isEmpty()) {
                            nombreCompleto = nombresCompletos;
                        } else {
                            // Si no existe, intentar con nombre y apellido separados
                            String nombre = docGuia.getString("nombre");
                            String apellido = docGuia.getString("apellido");
                            if (nombre != null && apellido != null) {
                                nombreCompleto = nombre + " " + apellido;
                            } else if (nombre != null) {
                                nombreCompleto = nombre;
                            }
                        }
                    }
                    
                    String email = docGuia.getString("email");
                    String telefono = docGuia.getString("telefono");
                    
                    // Mostrar información
                    if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                        binding.tvGuiaNombre.setText(nombreCompleto);
                        binding.tvGuiaNombre.setVisibility(View.VISIBLE);
                    }
                    
                    if (email != null) {
                        binding.tvGuiaCorreo.setText("📧 " + email);
                        binding.tvGuiaCorreo.setVisibility(View.VISIBLE);
                    }
                    
                    if (telefono != null) {
                        binding.tvGuiaTelefono.setText("📞 " + telefono);
                        binding.tvGuiaTelefono.setVisibility(View.VISIBLE);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar datos del guía", Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Configurar contenido de la sección Participantes
     */
    private void setupParticipantesContent() {
        binding.layoutParticipantesContainer.removeAllViews();
        
        if (participantesData != null && !participantesData.isEmpty()) {
            binding.tvNoParticipantes.setVisibility(View.GONE);
            binding.layoutParticipantesContainer.setVisibility(View.VISIBLE);
            
            // ✅ Calcular total de personas sumando numeroPersonas
            int totalPersonas = 0;
            for (Map<String, Object> p : participantesData) {
                Object numPersonasObj = p.get("numeroPersonas");
                if (numPersonasObj instanceof Number) {
                    totalPersonas += ((Number) numPersonasObj).intValue();
                }
            }
            
            // ✅ Actualizar TextView con total de personas (verificar que existe)
            if (binding.tvTotalPersonas != null) {
                String textoTotal = totalPersonas == 1 ? "1 persona en total" : totalPersonas + " personas en total";
                binding.tvTotalPersonas.setText(textoTotal);
                binding.tvTotalPersonas.setVisibility(View.VISIBLE);
            }
            
            // Crear vista para cada participante
            for (int i = 0; i < participantesData.size(); i++) {
                Map<String, Object> participante = participantesData.get(i);
                
                // Obtener datos del participante
                String nombre = (String) participante.get("nombre");
                String tipoDoc = (String) participante.get("tipoDocumento");
                String numeroDoc = (String) participante.get("numeroDocumento");
                String email = (String) participante.get("email");
                String telefono = (String) participante.get("telefono");
                Object numPersonasObj = participante.get("numeroPersonas");
                
                // ✅ Obtener número de personas para este participante
                int numPersonas = 1; // Valor por defecto
                if (numPersonasObj instanceof Number) {
                    numPersonas = ((Number) numPersonasObj).intValue();
                }
                
                // Crear CardView para cada participante
                com.google.android.material.card.MaterialCardView cardView = 
                    new com.google.android.material.card.MaterialCardView(this);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, 16);
                cardView.setLayoutParams(cardParams);
                cardView.setRadius(8);
                cardView.setCardElevation(2);
                cardView.setContentPadding(16, 16, 16, 16);
                
                // Layout interno del card
                LinearLayout cardContent = new LinearLayout(this);
                cardContent.setOrientation(LinearLayout.VERTICAL);
                cardContent.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                
                // ✅ Mostrar número de personas reservadas en lugar de "Participante X"
                TextView tvNumero = new TextView(this);
                String textoPersonas = numPersonas == 1 ? "Reservó para 1 persona" : "Reservó para " + numPersonas + " personas";
                tvNumero.setText(textoPersonas);
                tvNumero.setTextColor(getColor(R.color.primary));
                tvNumero.setTextSize(12);
                tvNumero.setTypeface(null, android.graphics.Typeface.BOLD);
                cardContent.addView(tvNumero);
                
                // Nombre del participante
                TextView tvNombre = new TextView(this);
                tvNombre.setText("👤 " + (nombre != null ? nombre : "Sin nombre"));
                tvNombre.setTextColor(getColor(R.color.on_surface));
                tvNombre.setTextSize(16);
                tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams nombreParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                nombreParams.setMargins(0, 8, 0, 8);
                tvNombre.setLayoutParams(nombreParams);
                cardContent.addView(tvNombre);
                
                // Documento
                if (tipoDoc != null && numeroDoc != null) {
                    TextView tvDocumento = new TextView(this);
                    tvDocumento.setText("📄 " + tipoDoc + ": " + numeroDoc);
                    tvDocumento.setTextColor(getColor(R.color.text_secondary));
                    tvDocumento.setTextSize(14);
                    LinearLayout.LayoutParams docParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    docParams.setMargins(0, 0, 0, 4);
                    tvDocumento.setLayoutParams(docParams);
                    cardContent.addView(tvDocumento);
                }
                
                // Email
                if (email != null && !email.isEmpty()) {
                    TextView tvEmail = new TextView(this);
                    tvEmail.setText("📧 " + email);
                    tvEmail.setTextColor(getColor(R.color.text_secondary));
                    tvEmail.setTextSize(14);
                    LinearLayout.LayoutParams emailParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    emailParams.setMargins(0, 0, 0, 4);
                    tvEmail.setLayoutParams(emailParams);
                    cardContent.addView(tvEmail);
                }
                
                // Teléfono
                if (telefono != null && !telefono.isEmpty()) {
                    TextView tvTelefono = new TextView(this);
                    tvTelefono.setText("📞 " + telefono);
                    tvTelefono.setTextColor(getColor(R.color.text_secondary));
                    tvTelefono.setTextSize(14);
                    cardContent.addView(tvTelefono);
                }
                
                cardView.addView(cardContent);
                binding.layoutParticipantesContainer.addView(cardView);
            }
        } else {
            // No hay participantes
            binding.tvNoParticipantes.setVisibility(View.VISIBLE);
            binding.layoutParticipantesContainer.setVisibility(View.GONE);
            if (binding.tvTotalPersonas != null) {
                binding.tvTotalPersonas.setVisibility(View.GONE);
            }
        }
    }
}

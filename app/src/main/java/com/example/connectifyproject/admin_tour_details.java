package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.gms.maps.model.MarkerOptions;
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
    
    // Firebase
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;
    
    // Adapters para las listas
    private AdminItinerarioAdapter itinerarioAdapter;
    private AdminServiciosAdapter serviciosAdapter;
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

        // Cargar datos del tour desde Firebase
        loadTourData();

        // Configurar tabs
        setupTabs();

        // Configurar mapa
        initializeMap();

        // No mostrar bottom navigation en pantallas secundarias
        // setupBottomNavigation();
    }
    
    private void loadTourData() {
        // Determinar la colección según el tipo de tour
        String collection = "borrador".equals(tourTipo) ? "tours_borradores" : "tours_ofertas";
        
        db.collection(collection).document(tourId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Cargar datos básicos
                    String titulo = documentSnapshot.getString("titulo");
                    String descripcion = documentSnapshot.getString("descripcion");
                    String fechaRealizacion = documentSnapshot.getString("fechaRealizacion");
                    Double precio = documentSnapshot.getDouble("precio");
                    String horaInicio = documentSnapshot.getString("horaInicio");
                    String horaFin = documentSnapshot.getString("horaFin");
                    String duracion = documentSnapshot.getString("duracion");
                    
                    // Cargar imágenes en galería horizontal
                    List<String> imagenesUrls = (List<String>) documentSnapshot.get("imagenesUrls");
                    if (imagenesUrls != null && !imagenesUrls.isEmpty()) {
                        imageAdapter = new com.example.connectifyproject.adapters.TourImageAdapter();
                        binding.recyclerViewImagenes.setLayoutManager(
                            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        );
                        binding.recyclerViewImagenes.setAdapter(imageAdapter);
                        imageAdapter.setImages(imagenesUrls);
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
                            
                            if (nombre != null && latitud != null && longitud != null) {
                                itinerarioItems.add(new Cliente_ItinerarioItem(
                                    "",  // hora (vacío por ahora)
                                    nombre,
                                    direccion != null ? direccion : "",
                                    latitud,
                                    longitud
                                ));
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
                    if (fechaRealizacion != null) {
                        binding.tvFecha.setText(fechaRealizacion);
                    }
                    if (precio != null) binding.tvCostoPorPersona.setText("S/ " + precio.intValue());
                    if (horaInicio != null && horaFin != null) {
                        binding.tvHorario.setText(horaInicio + " - " + horaFin);
                    }
                    if (duracion != null) {
                        binding.tvDuracion.setText(duracion + " hrs");
                    }
                    
                    // Configurar adaptador de servicios
                    if (serviciosList != null && !serviciosList.isEmpty()) {
                        serviciosAdapter = new AdminServiciosAdapter(serviciosList);
                        binding.recyclerViewServicios.setLayoutManager(new LinearLayoutManager(this));
                        binding.recyclerViewServicios.setAdapter(serviciosAdapter);
                    }
                    
                    // Configurar badge de estado
                    binding.tvEstadoBadge.setText(tourEstado);
                    if (tourEsPublicado) {
                        binding.tvEstadoBadge.setBackgroundColor(getColor(R.color.success_500));
                    } else {
                        binding.tvEstadoBadge.setBackgroundColor(getColor(R.color.text_secondary));
                    }
                    
                    // Configurar botón "Seleccionar Guía"
                    setupButtons();
                    
                    // Si hay itinerario, establecer ubicación en el mapa
                    if (itinerarioItems != null && !itinerarioItems.isEmpty()) {
                        Cliente_ItinerarioItem primerPunto = itinerarioItems.get(0);
                        tourLocation = new LatLng(primerPunto.getLatitude(), primerPunto.getLongitude());
                        binding.tvTourLocation.setText(primerPunto.getPlaceName() + ", " + primerPunto.getDescription());
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar datos del tour: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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
            Cliente_ItinerarioItem primerPunto = itinerarioItems.get(0);
            tourLocation = new LatLng(primerPunto.getLatitude(), primerPunto.getLongitude());
            // La ubicación ya se estableció en loadTourData()
        } else {
            // Ubicación por defecto si no hay itinerario
            tourLocation = new LatLng(-12.046374, -77.042754); // Lima por defecto
            binding.tvTourLocation.setText("Lima, Perú");
        }
        
        if (mGoogleMap != null) {
            // Mover cámara a la ubicación del tour
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tourLocation, 14));
            
            // Agregar marcadores del itinerario si está disponible
            addItinerarioMarkersToMap();
        }
    }
    
    private void addItinerarioMarkersToMap() {
        if (mGoogleMap != null && itinerarioItems != null && !itinerarioItems.isEmpty()) {
            // Limpiar marcadores anteriores
            mGoogleMap.clear();
            
            // Agregar marcadores para cada punto del itinerario
            for (int i = 0; i < itinerarioItems.size(); i++) {
                Cliente_ItinerarioItem item = itinerarioItems.get(i);
                LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
                
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title((i + 1) + ". " + item.getPlaceName())
                        .snippet(item.getVisitTime() + " - " + item.getDescription()));
            }
        } else {
            // Si no hay itinerario, agregar marcador principal
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(tourLocation)
                    .title(tourTitulo)
                    .snippet("Ubicación del tour"));
        }
    }

    private void setupButtons() {
        // Mostrar botón "Seleccionar Guía" solo si el tour está publicado
        if ("publicado".equals(tourTipo)) {
            binding.btnAsignarGuia.setVisibility(View.VISIBLE);
            binding.btnAsignarGuia.setOnClickListener(v -> {
                // Navegar a la vista de selección de guías
                Intent intent = new Intent(this, admin_select_guide.class);
                intent.putExtra("ofertaId", tourId);
                intent.putExtra("tourTitulo", tourTitulo);
                startActivity(intent);
            });
        } else {
            // Ocultar botón para tours en borrador
            binding.btnAsignarGuia.setVisibility(View.GONE);
        }
    }

    private void setupTabs() {
        binding.tabLayoutDetails.addTab(binding.tabLayoutDetails.newTab().setText("Info"));
        binding.tabLayoutDetails.addTab(binding.tabLayoutDetails.newTab().setText("Itinerario"));
        binding.tabLayoutDetails.addTab(binding.tabLayoutDetails.newTab().setText("Guía"));
        binding.tabLayoutDetails.addTab(binding.tabLayoutDetails.newTab().setText("Servicios"));

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
            case "Servicios":
                binding.layoutServiciosSection.setVisibility(View.VISIBLE);
                setupServiciosContent();
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
        // Verificar el estado del tour y si tiene guía asignada
        // Posibles estados:
        // - "Guía no seleccionado": muestra botón de buscar guía
        // - "En espera de confirmación": muestra timeline de la propuesta
        // - "Hay datos sin completar": muestra botón de buscar guía
        // - Otros estados: muestra información del guía asignado
        
        boolean guiaNoSeleccionado = "Guía no seleccionado".equals(tourEstado) || 
                                   "Hay datos sin completar".equals(tourEstado);
        boolean enEsperaConfirmacion = "En espera de confirmación".equals(tourEstado);
        
        // Ocultar todas las secciones primero
        binding.layoutGuiaAsignada.setVisibility(View.GONE);
        binding.tvGuiaNoAsignada.setVisibility(View.GONE);
        binding.layoutTimelineGuia.setVisibility(View.GONE);
        
        if (enEsperaConfirmacion) {
            // Mostrar timeline de la propuesta
            binding.layoutTimelineGuia.setVisibility(View.VISIBLE);
            
            // Configurar horarios de la timeline (datos de ejemplo)
            binding.tvTimelinePropuestaEnviada.setText("10:00 AM");
            binding.tvTimelineGuiaVisualizo.setText("10:30 AM");
            
        } else if (guiaNoSeleccionado) {
            // Mostrar mensaje de no hay guía asignado
            binding.tvGuiaNoAsignada.setVisibility(View.VISIBLE);
            binding.tvGuiaNoAsignada.setText("No hay un guía asignado");
            
        } else {
            // Mostrar información del guía asignado
            binding.layoutGuiaAsignada.setVisibility(View.VISIBLE);
            
            // TODO: Cargar datos reales del guía desde Firebase usando guiaAsignadoId
            // Por ahora mostrar datos de ejemplo
            binding.tvGuiaIdiomas.setText("Español, Inglés");
            binding.tvGuiaTelefono.setText("📞 +51 987 654 321");
            // Cargar foto del guía con Glide
            // Glide.with(this).load(guiaFotoUrl).placeholder(R.drawable.ic_person).into(binding.ivGuiaFoto);
        }
    }

    private void setupServiciosContent() {
        // Configurar RecyclerView para servicios
        if (serviciosList == null) {
            serviciosList = new ArrayList<>();
            // Agregar servicios con sus costos (datos de ejemplo)
            Map<String, Object> servicio1 = new HashMap<>();
            servicio1.put("nombre", "Guía turístico profesional");
            servicio1.put("costo", 50.0);
            serviciosList.add(servicio1);
            
            Map<String, Object> servicio2 = new HashMap<>();
            servicio2.put("nombre", "Transporte incluido");
            servicio2.put("costo", 30.0);
            serviciosList.add(servicio2);
            
            Map<String, Object> servicio3 = new HashMap<>();
            servicio3.put("nombre", "Almuerzo tradicional");
            servicio3.put("costo", 25.0);
            serviciosList.add(servicio3);
            
            Map<String, Object> servicio4 = new HashMap<>();
            servicio4.put("nombre", "Entradas a museos y sitios");
            servicio4.put("costo", 40.0);
            serviciosList.add(servicio4);
            
            Map<String, Object> servicio5 = new HashMap<>();
            servicio5.put("nombre", "Seguro de viaje");
            servicio5.put("costo", 15.0);
            serviciosList.add(servicio5);
            
            Map<String, Object> servicio6 = new HashMap<>();
            servicio6.put("nombre", "Agua y refrigerios");
            servicio6.put("costo", 10.0);
            serviciosList.add(servicio6);
        }
        
        if (serviciosAdapter == null) {
            serviciosAdapter = new AdminServiciosAdapter(serviciosList);
            binding.recyclerViewServicios.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerViewServicios.setAdapter(serviciosAdapter);
        }
    }

    @Override
    public void onItinerarioItemClick(Cliente_ItinerarioItem item) {
        // Cuando se hace clic en un item del itinerario, centrar el mapa en esa ubicación
        if (mGoogleMap != null) {
            LatLng location = new LatLng(item.getLatitude(), item.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
        }
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("tours");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }
}

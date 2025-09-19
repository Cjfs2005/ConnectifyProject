package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.connectifyproject.databinding.AdminTourDetailsViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.tabs.TabLayout;

public class admin_tour_details extends AppCompatActivity implements OnMapReadyCallback {
    private AdminTourDetailsViewBinding binding;
    private GoogleMap mGoogleMap;
    private String tourTitulo;
    private String tourEstado;
    private boolean tourEsPublicado;
    private LatLng tourLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminTourDetailsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener datos del Intent
        tourTitulo = getIntent().getStringExtra("tour_titulo");
        tourEstado = getIntent().getStringExtra("tour_estado");
        tourEsPublicado = getIntent().getBooleanExtra("tour_es_publicado", false);

        if (tourTitulo == null) {
            tourTitulo = "Tour de ejemplo";
        }

        // Configurar toolbar
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Configurar información del tour
        setupTourInfo();

        // Configurar botones
        setupButtons();

        // Configurar tabs
        setupTabs();

        // Configurar mapa
        initializeMap();

        // Configurar bottom navigation
        setupBottomNavigation();
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
        // Ubicaciones predefinidas para diferentes tours
        switch (tourTitulo) {
            case "Tour Lima Centro":
                tourLocation = new LatLng(-12.046374, -77.042754); // Plaza Mayor Lima
                binding.tvTourLocation.setText("Plaza Mayor, Lima Centro, Perú");
                break;
            case "Tour Casonas Históricas":
                tourLocation = new LatLng(-12.045678, -77.041234); // Casonas históricas
                binding.tvTourLocation.setText("Casonas Históricas, Lima, Perú");
                break;
            case "Tour Huascarán":
                tourLocation = new LatLng(-9.123456, -77.654321); // Huascarán
                binding.tvTourLocation.setText("Parque Nacional Huascarán, Perú");
                break;
            default:
                tourLocation = new LatLng(-12.046374, -77.042754); // Lima por defecto
                binding.tvTourLocation.setText("Lima, Perú");
                break;
        }
        
        if (mGoogleMap != null) {
            // Mover cámara a la ubicación del tour
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tourLocation, 15));
            
            // Agregar marcador
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(tourLocation)
                    .title(tourTitulo)
                    .snippet("Ubicación del tour"));
        }
    }

    private void setupTourInfo() {
        // Configurar imagen del tour (por ahora usamos una imagen por defecto)
        binding.ivTourHero.setImageResource(R.drawable.tour_lima_centro);

        // Configurar badge de estado
        binding.tvEstadoBadge.setText(tourEstado);
        if (tourEsPublicado) {
            binding.tvEstadoBadge.setBackgroundColor(getColor(R.color.success_500));
        } else {
            binding.tvEstadoBadge.setBackgroundColor(getColor(R.color.text_secondary));
        }

        // Configurar información básica del tour
        binding.tvTourNombre.setText(tourTitulo);
        binding.tvTourDescripcion.setText("Se explora el bosque raro a las afueras de Lima en acompañamiento de un guía que ilustrará la flora y fauna de este enigmático lugar.");
        
        // Configurar precios
        binding.tvCostoPorPersona.setText("$499");
        binding.tvServicios.setText("Guía, equipamiento de exploración, comidas");
        
        // Configurar duración
        binding.tvFechaInicio.setText("15 de julio, 2024");
        binding.tvFechaFin.setText("20 de julio, 2024");
    }

    private void setupButtons() {
        // Mostrar botón "Asignar Guía" solo si el tour está en borrador
        if (!tourEsPublicado && "Borrador".equals(tourEstado)) {
            binding.btnAsignarGuia.setVisibility(View.VISIBLE);
            binding.btnAsignarGuia.setOnClickListener(v -> {
                // Navegar a la vista de selección de guías
                Intent intent = new Intent(this, admin_select_guide.class);
                intent.putExtra("tour_titulo", tourTitulo);
                intent.putExtra("tour_estado", tourEstado);
                startActivity(intent);
            });
        } else {
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
                Toast.makeText(admin_tour_details.this, "Sección: " + tabText, Toast.LENGTH_SHORT).show();
                // TODO: Implementar cambio de contenido según la tab seleccionada
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("tours");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }
}

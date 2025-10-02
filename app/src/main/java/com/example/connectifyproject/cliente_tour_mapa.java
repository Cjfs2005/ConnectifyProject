package com.example.connectifyproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connectifyproject.adapters.Cliente_ItinerarioAdapter;
import com.example.connectifyproject.models.Cliente_ItinerarioItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class cliente_tour_mapa extends AppCompatActivity implements Cliente_ItinerarioAdapter.OnItinerarioItemClickListener, OnMapReadyCallback {

    private MaterialToolbar toolbar;
    private RecyclerView rvItinerario;
    private Cliente_ItinerarioAdapter itinerarioAdapter;
    private List<Cliente_ItinerarioItem> itinerarioItems;
    private GoogleMap mMap;
    
    private String tourId, tourTitle;
    
    // Coordenadas para el tour de Lima
    private final LatLng PLAZA_MAYOR = new LatLng(-12.0464, -77.0428);
    private final LatLng CATEDRAL_LIMA = new LatLng(-12.0464, -77.0425);
    private final LatLng PALACIO_GOBIERNO = new LatLng(-12.0462, -77.0431);
    private final LatLng CASA_ALIAGA = new LatLng(-12.0465, -77.0429);
    private final LatLng MIRAFLORES = new LatLng(-12.1203, -77.0287);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_tour_mapa);

        getIntentData();
        initViews();
        setupToolbar();
        setupMapFragment();
        setupItinerario();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        tourId = intent.getStringExtra("tour_id");
        tourTitle = intent.getStringExtra("tour_title");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvItinerario = findViewById(R.id.rv_itinerario);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Itinerario");
        }
        // Asegurar flecha de regreso en blanco
        toolbar.setNavigationIconTint(Color.WHITE);
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Configurar el mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        
        // Agregar marcadores
        addMapMarkers();
        
        // Dibujar ruta
        drawRoute();
        
        // Centrar el mapa en Lima
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PLAZA_MAYOR, 13));
    }

    private void addMapMarkers() {
        // Marcador de inicio - Plaza Mayor
        mMap.addMarker(new MarkerOptions()
                .position(PLAZA_MAYOR)
                .title("Plaza Mayor")
                .snippet("Punto de inicio del tour")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Marcador - Catedral de Lima
        mMap.addMarker(new MarkerOptions()
                .position(CATEDRAL_LIMA)
                .title("Catedral de Lima")
                .snippet("Visita a la catedral histórica")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // Marcador - Palacio de Gobierno
        mMap.addMarker(new MarkerOptions()
                .position(PALACIO_GOBIERNO)
                .title("Palacio de Gobierno")
                .snippet("Sede del gobierno peruano")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        // Marcador - Casa Aliaga
        mMap.addMarker(new MarkerOptions()
                .position(CASA_ALIAGA)
                .title("Casa Aliaga")
                .snippet("Casa colonial histórica")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        // Marcador de fin - Miraflores
        mMap.addMarker(new MarkerOptions()
                .position(MIRAFLORES)
                .title("Miraflores")
                .snippet("Punto final del tour")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void drawRoute() {
        // Crear una polilínea que conecte todos los puntos
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(PLAZA_MAYOR)
                .add(CATEDRAL_LIMA)
                .add(PALACIO_GOBIERNO)
                .add(CASA_ALIAGA)
                .add(MIRAFLORES)
                .width(8)
                .color(Color.parseColor("#7C4DFF"))
                .geodesic(true);

        mMap.addPolyline(polylineOptions);
    }

    private void setupItinerario() {
        itinerarioItems = new ArrayList<>();
        
        // Datos hardcodeados del itinerario con coordenadas
        itinerarioItems.add(new Cliente_ItinerarioItem("09:00", "Plaza Mayor", "Inicio del tour en el corazón de Lima colonial", 
                PLAZA_MAYOR.latitude, PLAZA_MAYOR.longitude));
        itinerarioItems.add(new Cliente_ItinerarioItem("09:30", "Catedral de Lima", "Visita a la catedral metropolitana", 
                CATEDRAL_LIMA.latitude, CATEDRAL_LIMA.longitude));
        itinerarioItems.add(new Cliente_ItinerarioItem("10:30", "Palacio de Gobierno", "Tour por la Casa de Pizarro", 
                PALACIO_GOBIERNO.latitude, PALACIO_GOBIERNO.longitude));
        itinerarioItems.add(new Cliente_ItinerarioItem("11:30", "Casa Aliaga", "Mansión colonial más antigua de América", 
                CASA_ALIAGA.latitude, CASA_ALIAGA.longitude));
        itinerarioItems.add(new Cliente_ItinerarioItem("15:00", "Miraflores", "Malecón y parques de Miraflores", 
                MIRAFLORES.latitude, MIRAFLORES.longitude));

        itinerarioAdapter = new Cliente_ItinerarioAdapter(this, itinerarioItems);
        itinerarioAdapter.setOnItinerarioItemClickListener(this);
        rvItinerario.setLayoutManager(new LinearLayoutManager(this));
        rvItinerario.setAdapter(itinerarioAdapter);
    }

    @Override
    public void onItemClick(Cliente_ItinerarioItem item) {
        onItinerarioItemClick(item);
    }

    @Override
    public void onVerMasClick(Cliente_ItinerarioItem item) {
        onItinerarioItemClick(item);
    }

    public void onItinerarioItemClick(Cliente_ItinerarioItem item) {
        // Mostrar dialog con detalles del punto de interés
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getTime() + " - " + item.getTitle())
                .setMessage(item.getDescription())
                .setPositiveButton("Ver en mapa", (dialog, which) -> {
                    // Centrar el mapa en este punto
                    if (mMap != null) {
                        LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16));
                    }
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }
}
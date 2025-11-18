package com.example.connectifyproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class cliente_tour_mapa extends AppCompatActivity implements Cliente_ItinerarioAdapter.OnItinerarioItemClickListener, OnMapReadyCallback {

    private MaterialToolbar toolbar;
    private RecyclerView rvItinerario;
    private Cliente_ItinerarioAdapter itinerarioAdapter;
    private List<Cliente_ItinerarioItem> itinerarioItems;
    private GoogleMap mMap;
    private FirebaseFirestore db;
    
    private String tourId, tourTitle;
    private List<LatLng> routePoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_tour_mapa);

        db = FirebaseFirestore.getInstance();
        itinerarioItems = new ArrayList<>();
        routePoints = new ArrayList<>();
        
        getIntentData();
        initViews();
        setupToolbar();
        setupMapFragment();
        loadItinerarioFromFirebase();
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

    private void loadItinerarioFromFirebase() {
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Error: ID del tour no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        db.collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    List<Map<String, Object>> itinerario = 
                        (List<Map<String, Object>>) doc.get("itinerario");
                    
                    if (itinerario != null && !itinerario.isEmpty()) {
                        processItinerario(itinerario);
                    } else {
                        Toast.makeText(this, "No hay itinerario disponible", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "Tour no encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar itinerario: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    private void processItinerario(List<Map<String, Object>> itinerario) {
        itinerarioItems.clear();
        routePoints.clear();
        
        for (int i = 0; i < itinerario.size(); i++) {
            Map<String, Object> punto = itinerario.get(i);
            
            String nombre = (String) punto.get("nombre");
            String direccion = (String) punto.get("direccion");
            Double latitud = (Double) punto.get("latitud");
            Double longitud = (Double) punto.get("longitud");
            
            if (nombre != null && latitud != null && longitud != null) {
                // Agregar a la lista de puntos para el RecyclerView
                itinerarioItems.add(new Cliente_ItinerarioItem(
                    "", // hora vacía ya que no la tenemos en la estructura
                    nombre,
                    direccion != null ? direccion : "",
                    latitud,
                    longitud
                ));
                
                // Agregar coordenadas para la ruta
                routePoints.add(new LatLng(latitud, longitud));
            }
        }
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Si el mapa ya está listo, actualizar marcadores y ruta
        if (mMap != null) {
            updateMapWithItinerario();
        }
    }
    
    private void setupRecyclerView() {
        itinerarioAdapter = new Cliente_ItinerarioAdapter(this, itinerarioItems);
        itinerarioAdapter.setOnItinerarioItemClickListener(this);
        rvItinerario.setLayoutManager(new LinearLayoutManager(this));
        rvItinerario.setAdapter(itinerarioAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Configurar el mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        
        // Si ya tenemos datos del itinerario, actualizar el mapa
        if (!routePoints.isEmpty()) {
            updateMapWithItinerario();
        }
    }
    
    private void updateMapWithItinerario() {
        if (mMap == null || routePoints.isEmpty()) return;
        
        // Limpiar marcadores y polilíneas existentes
        mMap.clear();
        
        // Agregar marcadores
        for (int i = 0; i < itinerarioItems.size(); i++) {
            Cliente_ItinerarioItem item = itinerarioItems.get(i);
            LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
            
            float markerColor;
            if (i == 0) {
                markerColor = BitmapDescriptorFactory.HUE_GREEN; // Inicio
            } else if (i == itinerarioItems.size() - 1) {
                markerColor = BitmapDescriptorFactory.HUE_RED; // Fin
            } else {
                markerColor = BitmapDescriptorFactory.HUE_AZURE; // Puntos intermedios
            }
            
            mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(item.getTitle())
                .snippet(item.getDescription())
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        }
        
        // Dibujar ruta conectando todos los puntos
        if (routePoints.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(8)
                .color(Color.parseColor("#7C4DFF"))
                .geodesic(true);
            
            mMap.addPolyline(polylineOptions);
        }
        
        // Ajustar cámara para mostrar todos los puntos
        if (!routePoints.isEmpty()) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : routePoints) {
                boundsBuilder.include(point);
            }
            LatLngBounds bounds = boundsBuilder.build();
            
            int padding = 100; // píxeles
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
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
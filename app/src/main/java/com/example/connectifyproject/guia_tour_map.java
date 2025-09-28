package com.example.connectifyproject;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.connectifyproject.databinding.GuiaTourMapBinding;
import com.example.connectifyproject.service.GuiaLocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class guia_tour_map extends AppCompatActivity implements OnMapReadyCallback {

    private GuiaTourMapBinding binding;
    private GoogleMap mMap;
    private LatLng currentLocation;
    private Marker currentMarker;
    private List<LatLng> itineraryPoints = new ArrayList<>();
    private List<String> itineraryNames = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private int currentPointIndex = 0;
    private boolean isTourOngoing = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2;

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double lat = intent.getDoubleExtra("lat", 0);
            double lng = intent.getDoubleExtra("lng", 0);
            currentLocation = new LatLng(lat, lng);
            updateCurrentLocationMarker();
        }
    };

    private BroadcastReceiver arrivedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int index = intent.getIntExtra("index", -1);
            if (index != -1) {
                Toast.makeText(guia_tour_map.this, "Llegaste a " + itineraryNames.get(index), Toast.LENGTH_SHORT).show();
                currentPointIndex = index + 1;
                updateOngoingUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaTourMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tour - Mapa");
        }

        // Get data from intent
        String tourName = getIntent().getStringExtra("tour_name");
        String tourStatus = getIntent().getStringExtra("tour_status");
        ArrayList<String> itinerario = getIntent().getStringArrayListExtra("tour_itinerario");
        int clients = getIntent().getIntExtra("tour_clients", 0);

        isTourOngoing = "En Curso".equals(tourStatus);

        // Hardcoded coordinates for Lima historic center (example)
        itineraryPoints.add(new LatLng(-12.046374, -77.042793)); // Plaza de Armas
        itineraryPoints.add(new LatLng(-12.045581, -77.030476)); // Catedral de Lima
        itineraryPoints.add(new LatLng(-12.043333, -77.028333)); // Convento San Francisco
        itineraryPoints.add(new LatLng(-12.123611, -77.030278)); // Museo Larco

        // Extract names from itinerario
        if (itinerario != null) {
            for (String item : itinerario) {
                String[] parts = item.split(" – ");
                if (parts.length > 0) {
                    itineraryNames.add(parts[0].substring(3)); // Remove "1. "
                }
            }
        }

        binding.tourName.setText(tourName);

        // Setup UI based on status
        setupUIBasedOnStatus(clients);

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Button listeners
        binding.startTourButton.setOnClickListener(v -> startTour());
        binding.scanQrStart.setOnClickListener(v -> scanQrStart());
        binding.registerPosition.setOnClickListener(v -> registerPosition());
        binding.endTourButton.setOnClickListener(v -> endTour());
        binding.scanQrEnd.setOnClickListener(v -> scanQrEnd());

        /*
        // Nuevo Bottom Navigation con Toast
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_tours);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class));
                return true;
            } else if (id == R.id.nav_ofertas) {
                startActivity(new Intent(this, guia_tours_ofertas.class));
                return true;
            } else if (id == R.id.nav_tours) {
                return true;
            } else if (id == R.id.nav_perfil) {
                Toast.makeText(this, "Perfil seleccionado", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
         */
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(arrivedReceiver, new IntentFilter("ARRIVED_POINT"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(arrivedReceiver);
    }

    private void setupUIBasedOnStatus(int clients) {
        if (!isTourOngoing) {
            // Pre-tour
            binding.preTourLayout.setVisibility(View.VISIBLE);
            binding.ongoingTourLayout.setVisibility(View.GONE);
            binding.postTourLayout.setVisibility(View.GONE);
            binding.clientsCheckIn.setText("Clientes que realizaron el Check-in: 4/12"); // Simulated
            binding.importantNotePre.setText("¡Importante!\nAntes de iniciar el tour, es necesario escanear el código QR que todos los pasajeros te mostrarán en su dispositivo. Este paso confirma que este ha presentado al tour. Se valida nuevamente al cierre del viaje.");
        } else {
            // Ongoing
            binding.preTourLayout.setVisibility(View.GONE);
            binding.ongoingTourLayout.setVisibility(View.VISIBLE);
            binding.postTourLayout.setVisibility(View.GONE);
            updateOngoingUI();
            binding.clientsRegistered.setText("Clientes registrados: 8/12");
        }
    }

    private void updateOngoingUI() {
        if (currentPointIndex < itineraryNames.size()) {
            binding.currentEvent.setText("Evento Actual del Tour: " + itineraryNames.get(currentPointIndex));
            binding.nextStop.setText("Próxima parada: " + (currentPointIndex + 1 < itineraryNames.size() ? itineraryNames.get(currentPointIndex + 1) : "Fin del tour") + " - 09:30 am");
        } else {
            binding.currentEvent.setText("Tour completado");
            binding.nextStop.setText("");
            binding.ongoingTourLayout.setVisibility(View.GONE);
            binding.postTourLayout.setVisibility(View.VISIBLE);
            binding.clientsCheckOut.setText("Clientes que realizaron el Check-out: 4/12"); // Simulated
            binding.importantNotePost.setText("¡Importante!\nAntes de finalizar el tour, es necesario escanear el código QR-Fin que todos los pasajeros te mostrarán en su dispositivo. Este paso confirma que este ha realizado el tour.");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable built-in my location layer for user's position
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Add markers and polyline with improved styles
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(Color.BLUE)
                .width(10f)  // Increased width for better visibility as per Figma
                .pattern(null);  // Solid line

        for (int i = 0; i < itineraryPoints.size(); i++) {
            LatLng point = itineraryPoints.get(i);
            String name = i < itineraryNames.size() ? itineraryNames.get(i) : "Punto " + (i + 1);
            float hue = i == currentPointIndex ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_CYAN;
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(name)
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
            markers.add(marker);
            polylineOptions.add(point);

            // Add proximity circle (50m radius, semi-transparent green)
            mMap.addCircle(new CircleOptions()
                    .center(point)
                    .radius(50)
                    .strokeColor(Color.GREEN)
                    .fillColor(Color.argb(50, 0, 255, 0)));
        }
        mMap.addPolyline(polylineOptions);

        // Move camera to first point
        if (!itineraryPoints.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itineraryPoints.get(0), 15f));
        }

        // Custom current location marker (green)
        if (currentLocation != null) {
            updateCurrentLocationMarker();
        }
    }

    private void updateCurrentLocationMarker() {
        if (mMap == null || currentLocation == null) return;

        if (currentMarker == null) {
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Posición Actual")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        } else {
            currentMarker.setPosition(currentLocation);
        }

        // Optional: Add accuracy circle
        mMap.addCircle(new CircleOptions()
                .center(currentLocation)
                .radius(20)  // Approximate accuracy
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(30, 33, 150, 243)));
    }

    private void checkProximityToNextPoint() {
        if (currentPointIndex >= itineraryPoints.size() || currentLocation == null) return;

        LatLng next = itineraryPoints.get(currentPointIndex);
        float[] distance = new float[1];
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, next.latitude, next.longitude, distance);

        if (distance[0] < 50) {
            markPointAsArrived();
        } else {
            Toast.makeText(this, "Aún no estás cerca de la próxima parada (" + String.format("%.0fm", distance[0]) + ")", Toast.LENGTH_SHORT).show();
        }
    }

    private void markPointAsArrived() {
        if (currentPointIndex < markers.size()) {
            markers.get(currentPointIndex).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));  // Mark as visited
        }
        Toast.makeText(this, "¡Llegaste a " + itineraryNames.get(currentPointIndex) + "!", Toast.LENGTH_SHORT).show();
        currentPointIndex++;
        updateOngoingUI();
    }

    private void registerPosition() {
        if (currentLocation != null) {
            Toast.makeText(this, "Posición registrada: Lat " + String.format("%.6f", currentLocation.latitude) +
                    ", Lng " + String.format("%.6f", currentLocation.longitude), Toast.LENGTH_LONG).show();
            checkProximityToNextPoint();
        } else {
            Toast.makeText(this, "Obteniendo posición... Asegúrate de tener GPS activado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTour() {
        requestLocationPermissions();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            isTourOngoing = true;
            setupUIBasedOnStatus(0);

            Intent serviceIntent = new Intent(this, GuiaLocationService.class);
            serviceIntent.putParcelableArrayListExtra("itinerary_points", new ArrayList<>(itineraryPoints));
            ContextCompat.startForegroundService(this, serviceIntent);

            Toast.makeText(this, "¡Tour iniciado! Geolocalización activa.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permisos de ubicación requeridos para iniciar el tour.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE || requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                }
                Toast.makeText(this, "Permisos concedidos. Ubicación activada.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso denegado. No se puede rastrear la posición.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scanQrStart() {
        startActivity(new Intent(this, guia_check_in.class));
        Toast.makeText(this, "Escaneando QR-Inicio", Toast.LENGTH_SHORT).show();
    }

    private void endTour() {
        Intent serviceIntent = new Intent(this, GuiaLocationService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "Tour finalizado exitosamente.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void scanQrEnd() {
        startActivity(new Intent(this, guia_check_out.class));
        Toast.makeText(this, "Escaneando QR-Fin", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
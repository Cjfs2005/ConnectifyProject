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
    private static final float PROXIMITY_RADIUS_METERS = 50.0f; // Radio aceptable: 50 metros
    
    // Cliente para obtener ubicación en tiempo real
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    
    // 🚀 FIREBASE SERVICE PARA MANEJAR ESTADOS
    private com.example.connectifyproject.services.TourFirebaseService tourFirebaseService;
    private String tourId;
    private String tourName;

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

        // 🔧 INICIALIZAR FIREBASE SERVICE
        tourFirebaseService = new com.example.connectifyproject.services.TourFirebaseService();
        
        // 📍 INICIALIZAR CLIENTE DE UBICACIÓN
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this);
        
        // 📋 OBTENER DATOS DEL TOUR DESDE INTENT
        tourId = getIntent().getStringExtra("tour_id");
        tourName = getIntent().getStringExtra("tour_name");
        String tourStatus = getIntent().getStringExtra("tour_status");
        ArrayList<String> itinerario = getIntent().getStringArrayListExtra("tour_itinerario");
        int clients = getIntent().getIntExtra("tour_clients", 0);

        // 🔄 RECUPERAR ESTADO PERSISTENTE DESDE FIREBASE
        recuperarEstadoTour();

        binding.tourName.setText(tourName);
        
        // Button listeners
        binding.startTourButton.setOnClickListener(v -> startTour());
        binding.scanQrStart.setOnClickListener(v -> scanQrStart());
        binding.registerPosition.setOnClickListener(v -> registerPosition());
        binding.endTourButton.setOnClickListener(v -> endTour());
        binding.scanQrEnd.setOnClickListener(v -> scanQrEnd());
        
        // Cargar itinerario con coordenadas desde Firebase (esto configura UI y mapa)
        loadItinerarioFromFirebase(clients, tourStatus);

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

    /**
     * Cargar itinerario con coordenadas desde Firebase
     */
    private void loadItinerarioFromFirebase(int clients, String tourStatus) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        db.collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Obtener itinerario con coordenadas
                    java.util.List<java.util.Map<String, Object>> itinerarioData = 
                        (java.util.List<java.util.Map<String, Object>>) documentSnapshot.get("itinerario");
                    
                    if (itinerarioData != null && !itinerarioData.isEmpty()) {
                        itineraryPoints.clear();
                        itineraryNames.clear();
                        
                        for (java.util.Map<String, Object> punto : itinerarioData) {
                            // Extraer coordenadas
                            Object latObj = punto.get("latitud");
                            Object lngObj = punto.get("longitud");
                            String nombre = (String) punto.get("nombre");
                            
                            if (latObj != null && lngObj != null) {
                                double lat = latObj instanceof Double ? (Double) latObj : ((Number) latObj).doubleValue();
                                double lng = lngObj instanceof Double ? (Double) lngObj : ((Number) lngObj).doubleValue();
                                
                                itineraryPoints.add(new LatLng(lat, lng));
                                itineraryNames.add(nombre != null ? nombre : "Punto " + (itineraryPoints.size()));
                            }
                        }
                        
                        android.util.Log.d("GuiaTourMap", "Itinerario cargado: " + itineraryPoints.size() + " puntos");
                    } else {
                        Toast.makeText(this, "No hay puntos en el itinerario", Toast.LENGTH_SHORT).show();
                    }
                    
                    // Setup UI después de cargar datos
                    setupUIBasedOnStatus(clients);
                    
                    // Setup map
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(this);
                    }
                } else {
                    Toast.makeText(this, "Tour no encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar itinerario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("GuiaTourMap", "Error al cargar itinerario", e);
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(arrivedReceiver, new IntentFilter("ARRIVED_POINT"));
        
        // 🔄 RECUPERAR ESTADO AL VOLVER A LA PANTALLA
        recuperarEstadoTour();
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
            // CORREGIDO: Cargar contador real desde Firebase
            cargarContadorCheckIn();
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
            
            // Mostrar progreso de puntos visitados
            binding.clientsRegistered.setText(String.format("Puntos visitados: %d/%d", currentPointIndex, itineraryNames.size()));
        } else {
            // Todos los puntos completados
            binding.currentEvent.setText("✅ Todos los puntos del itinerario completados");
            binding.nextStop.setText("🏁 Ahora puedes finalizar el tour");
            binding.clientsRegistered.setText(String.format("Puntos visitados: %d/%d (✅ Completo)", itineraryNames.size(), itineraryNames.size()));
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

    /**
     * 📍 REGISTRAR POSICIÓN DEL GUÍA
     * Obtiene ubicación GPS actual, verifica permisos y valida proximidad al punto del itinerario
     */
    private void registerPosition() {
        // 1. Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "⚠️ Permisos de ubicación no concedidos. Solicitando...", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        
        // 2. Verificar que hay puntos en el itinerario
        if (itineraryPoints.isEmpty()) {
            Toast.makeText(this, "❌ No hay puntos en el itinerario para validar.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 3. Verificar que no hemos completado todos los puntos
        if (currentPointIndex >= itineraryPoints.size()) {
            Toast.makeText(this, "✅ Ya completaste todos los puntos del itinerario.", Toast.LENGTH_LONG).show();
            return;
        }
        
        Toast.makeText(this, "📍 Obteniendo ubicación GPS...", Toast.LENGTH_SHORT).show();
        
        // 4. Obtener ubicación actual en tiempo real
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    // Actualizar ubicación actual
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    updateCurrentLocationMarker();
                    
                    // Validar proximidad al siguiente punto
                    LatLng nextPoint = itineraryPoints.get(currentPointIndex);
                    String nextPointName = itineraryNames.get(currentPointIndex);
                    
                    float[] distance = new float[1];
                    Location.distanceBetween(
                        currentLocation.latitude, currentLocation.longitude,
                        nextPoint.latitude, nextPoint.longitude,
                        distance
                    );
                    
                    float distanceMeters = distance[0];
                    
                    if (distanceMeters <= PROXIMITY_RADIUS_METERS) {
                        // ✅ DENTRO DEL RADIO - Registrar posición exitosa
                        Toast.makeText(this, 
                            String.format("✅ Posición registrada en %s\n📏 Distancia: %.1f metros", 
                                nextPointName, distanceMeters), 
                            Toast.LENGTH_LONG).show();
                        
                        // Marcar punto como visitado y avanzar al siguiente
                        currentPointIndex++;
                        updateOngoingUI();
                    } else {
                        // ❌ FUERA DEL RADIO - Mostrar distancia
                        Toast.makeText(this, 
                            String.format("⚠️ Estás a %.1f metros de %s\n🎯 Debes estar a menos de %.0f metros", 
                                distanceMeters, nextPointName, PROXIMITY_RADIUS_METERS), 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    // No se pudo obtener ubicación
                    Toast.makeText(this, "❌ No se pudo obtener la ubicación.\n🔧 Verifica que el GPS esté activado.", Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "❌ Error obteniendo ubicación: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
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
        // CORREGIDO: Guía ESCANEA QR de cada cliente
        Intent intent = new Intent(this, guia_scan_qr_participants.class);
        intent.putExtra("tourId", tourId);
        intent.putExtra("tourTitulo", tourName);
        int clients = getIntent().getIntExtra("tour_clients", 0);
        intent.putExtra("numeroParticipantes", clients);
        intent.putExtra("scanMode", "check_in");
        startActivity(intent);
    }

    /**
     * 🏁 FINALIZAR TOUR
     * Solo permite finalizar si todos los puntos del itinerario fueron visitados
     */
    private void endTour() {
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "❌ Error: ID de tour no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // ✅ VALIDAR QUE TODOS LOS PUNTOS FUERON COMPLETADOS
        if (currentPointIndex < itineraryPoints.size()) {
            int puntosRestantes = itineraryPoints.size() - currentPointIndex;
            Toast.makeText(this, 
                String.format("⚠️ No puedes finalizar aún.\n📏 Faltan %d punto(s) por visitar.\n📍 Usa 'Registrar Posición' en cada punto.", puntosRestantes),
                Toast.LENGTH_LONG).show();
            return;
        }
        
        // Crear instancia del servicio Firebase si no existe
        if (tourFirebaseService == null) {
            tourFirebaseService = new com.example.connectifyproject.services.TourFirebaseService();
        }
        
        // Deshabilitar botón para evitar clicks múltiples
        binding.endTourButton.setEnabled(false);
        binding.endTourButton.setText("Habilitando Check-out...");
        
        // Habilitar check-out (en_curso → check_out)
        tourFirebaseService.habilitarCheckOut(tourId, new com.example.connectifyproject.services.TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_tour_map.this, 
                        "✅ Check-out habilitado.\n📱 Ahora escanea el QR de cada cliente.", Toast.LENGTH_LONG).show();
                    
                    // Detener servicio de ubicación
                    Intent serviceIntent = new Intent(guia_tour_map.this, com.example.connectifyproject.service.GuiaLocationService.class);
                    stopService(serviceIntent);
                    
                    // Mostrar layout de post-tour para escanear QR de check-out
                    isTourOngoing = false;
                    binding.ongoingTourLayout.setVisibility(View.GONE);
                    binding.postTourLayout.setVisibility(View.VISIBLE);
                    cargarContadorCheckOut();
                    binding.importantNotePost.setText("¡Importante!\nAhora debes escanear el código QR de Check-out de cada cliente. Presiona 'Escanear QR Fin' para comenzar.");
                    
                    // Regresar a tours asignados
                    Intent intent = new Intent(guia_tour_map.this, guia_assigned_tours.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_tour_map.this, 
                        "❌ Error al habilitar check-out: " + error, Toast.LENGTH_LONG).show();
                    
                    // Re-habilitar botón
                    binding.endTourButton.setEnabled(true);
                    binding.endTourButton.setText("Finalizar Tour");
                });
            }
        });
    }

    private void scanQrEnd() {
        // CORREGIDO: Guía ESCANEA QR de cada cliente al finalizar
        Intent intent = new Intent(this, guia_scan_qr_participants.class);
        intent.putExtra("tourId", tourId);
        intent.putExtra("tourTitulo", tourName);
        int clients = getIntent().getIntExtra("tour_clients", 0);
        intent.putExtra("numeroParticipantes", clients);
        intent.putExtra("scanMode", "check_out");
        startActivity(intent);
    }

    /**
     * 🔄 RECUPERAR ESTADO DEL TOUR DESDE FIREBASE
     * Para mantener consistencia al salir y volver a entrar
     */
    private void recuperarEstadoTour() {
        if (tourId == null || tourId.isEmpty()) return;
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Boolean tourStarted = documentSnapshot.getBoolean("tourStarted");
                    String estado = documentSnapshot.getString("estado");
                    
                    if (tourStarted != null && tourStarted) {
                        // El tour ya fue iniciado
                        isTourOngoing = true;
                        
                        // Recuperar progreso del itinerario
                        Long progressIndex = documentSnapshot.getLong("currentPointIndex");
                        if (progressIndex != null) {
                            currentPointIndex = progressIndex.intValue();
                        }
                    }
                    
                    // Actualizar UI según estado
                    if ("en_curso".equals(estado)) {
                        binding.preTourLayout.setVisibility(View.GONE);
                        binding.ongoingTourLayout.setVisibility(View.VISIBLE);
                        binding.postTourLayout.setVisibility(View.GONE);
                        updateOngoingUI();
                    }
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("GuiaTourMap", "Error al recuperar estado", e);
            });
    }
    
    /**
     * 📊 CARGAR CONTADOR REAL DE CHECK-IN DESDE FIREBASE
     */
    private void cargarContadorCheckIn() {
        if (tourId == null || tourId.isEmpty()) return;
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<java.util.Map<String, Object>> participantes = 
                        (List<java.util.Map<String, Object>>) documentSnapshot.get("participantes");
                    
                    if (participantes != null) {
                        int checkInsRealizados = 0;
                        for (java.util.Map<String, Object> participante : participantes) {
                            Boolean checkIn = (Boolean) participante.get("checkIn");
                            if (checkIn != null && checkIn) {
                                checkInsRealizados++;
                            }
                        }
                        
                        int total = participantes.size();
                        binding.clientsCheckIn.setText("Clientes que realizaron el Check-in: " + 
                            checkInsRealizados + "/" + total);
                    }
                }
            })
            .addOnFailureListener(e -> {
                binding.clientsCheckIn.setText("Clientes que realizaron el Check-in: 0/0");
            });
    }
    
    /**
     * 📊 CARGAR CONTADOR REAL DE CHECK-OUT DESDE FIREBASE
     */
    private void cargarContadorCheckOut() {
        if (tourId == null || tourId.isEmpty()) return;
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<java.util.Map<String, Object>> participantes = 
                        (List<java.util.Map<String, Object>>) documentSnapshot.get("participantes");
                    
                    if (participantes != null) {
                        int checkOutsRealizados = 0;
                        for (java.util.Map<String, Object> participante : participantes) {
                            Boolean checkOut = (Boolean) participante.get("checkOut");
                            if (checkOut != null && checkOut) {
                                checkOutsRealizados++;
                            }
                        }
                        
                        int total = participantes.size();
                        binding.clientsCheckOut.setText("Clientes que realizaron el Check-out: " + 
                            checkOutsRealizados + "/" + total);
                    }
                }
            })
            .addOnFailureListener(e -> {
                binding.clientsCheckOut.setText("Clientes que realizaron el Check-out: 0/0");
            });
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
package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.connectifyproject.databinding.AdminPerfilViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.example.connectifyproject.utils.GoogleMapsHelper;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class admin_perfil extends AppCompatActivity implements OnMapReadyCallback {
    private AdminPerfilViewBinding binding;
    private GoogleMapsHelper mapsHelper;
    private GoogleMap mGoogleMap;
    private boolean isUpdatingLocation = false; // Flag para evitar loop infinito
    private LatLng currentLocation = new LatLng(-12.046374, -77.042754); // Lima, Perú por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminPerfilViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar la barra superior
        setSupportActionBar(binding.topAppBar);

        // Configurar botón de notificaciones
        binding.btnNotifications.setOnClickListener(v -> {
            // TODO: Implementar navegación a notificaciones
        });

        // Inicializar helper de ubicación
        mapsHelper = new GoogleMapsHelper(this);

        // Inicializar mapa
        initializeMap();

        // Configurar componentes
        setupLocationSearch();
        setupButtons();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("perfil");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }

    private void setupLocationSearch() {
        // Click en el ícono de búsqueda - búsqueda manual únicamente
        binding.tilLocation.setEndIconOnClickListener(v -> {
            String query = binding.etLocation.getText().toString().trim();
            if (!query.isEmpty()) {
                searchLocation(query);
            } else {
                Toast.makeText(this, "Ingrese una ubicación para buscar", Toast.LENGTH_SHORT).show();
                binding.etLocation.requestFocus();
            }
        });
    }

    private void setupButtons() {
        // Botón cambiar logo
        binding.btnChangeLogo.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad de cambio de logo en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Sección de fotos promocionales
        binding.btnAddPhotos.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad de fotos promocionales en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Botón guardar
        binding.btnSave.setOnClickListener(v -> {
            saveProfileData();
        });

        // Botón cerrar sesión
        binding.btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        
        try {
            // Configurar mapa
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            
            // Ocultar el progress bar ya que el mapa está listo
            binding.progressBarMap.setVisibility(View.GONE);
            
            // Mover cámara a Lima por defecto
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            
            // Agregar marcador
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Ubicación de la empresa"));
            
            Toast.makeText(this, "Mapa cargado correctamente", Toast.LENGTH_SHORT).show();
            
            // Configurar click en el mapa
            mGoogleMap.setOnMapClickListener(latLng -> {
                currentLocation = latLng;
                mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Nueva ubicación"));
            
            // Obtener dirección de las coordenadas
            mapsHelper.getAddressFromCoordinates(latLng.latitude, latLng.longitude, 
                new GoogleMapsHelper.LocationSearchCallback() {
                    @Override
                    public void onLocationFound(String address, double latitude, double longitude) {
                        isUpdatingLocation = true;
                        binding.etLocation.setText(address);
                        // Restaurar el flag después de actualizar
                        isUpdatingLocation = false;
                    }

                    @Override
                    public void onLocationNotFound() {
                        Toast.makeText(admin_perfil.this, "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(admin_perfil.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
        });
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar el mapa: " + e.getMessage(), Toast.LENGTH_LONG).show();
            binding.progressBarMap.setVisibility(View.GONE);
        }
    }

    private void searchLocation(String query) {
        mapsHelper.searchLocation(query, new GoogleMapsHelper.LocationSearchCallback() {
            @Override
            public void onLocationFound(String address, double latitude, double longitude) {
                // Marcar que estamos actualizando para evitar el loop
                isUpdatingLocation = true;
                
                // Actualizar el campo de ubicación con la dirección encontrada
                binding.etLocation.setText(address);
                
                // Restaurar el flag
                isUpdatingLocation = false;
                
                // Actualizar mapa con las coordenadas
                updateMapLocation(latitude, longitude);
                    
                Toast.makeText(admin_perfil.this, "Ubicación encontrada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLocationNotFound() {
                Toast.makeText(admin_perfil.this, "No se encontró la ubicación", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(admin_perfil.this, "Error al buscar: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapLocation(double latitude, double longitude) {
        if (mGoogleMap != null) {
            currentLocation = new LatLng(latitude, longitude);
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Ubicación encontrada"));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        }
    }

    private void saveProfileData() {
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();

        if (email.isEmpty() || phone.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí se puede implementar el guardado real de los datos
        Toast.makeText(this, "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();

        // Navegar de regreso al dashboard
        Intent intent = new Intent(this, admin_dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void logout() {
        // Cerrar sesión de Firebase Auth
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    // Ir al SplashActivity que redirigirá al login
                    Intent intent = new Intent(this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos del helper de mapas
        if (mapsHelper != null) {
            mapsHelper.shutdown();
        }
    }
}

package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.connectifyproject.databinding.AdminCreateTourViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class admin_create_tour extends AppCompatActivity implements OnMapReadyCallback {
    private AdminCreateTourViewBinding binding;
    private GoogleMap mMap;
    private int currentStep = 1;
    private final int TOTAL_STEPS = 2;
    
    // Tour data
    private String tourTitle;
    private String tourDescription;
    private String tourPrice;
    private String tourDuration;
    private LatLng selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminCreateTourViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        setupButtons();
        setupBottomNavigation();
        initializeMap();
        showStep(currentStep);
    }

    private void setupUI() {
        binding.topAppBar.setNavigationOnClickListener(v -> finish());
        binding.topAppBar.setTitle("Crear Nuevo Tour");
        updateProgressIndicator();
    }

    private void setupButtons() {
        binding.btnNext.setOnClickListener(v -> nextStep());
        binding.btnPrevious.setOnClickListener(v -> previousStep());
        binding.btnSaveDraft.setOnClickListener(v -> saveDraft());
        binding.btnPublish.setOnClickListener(v -> publishTour());
    }

    private void showStep(int step) {
        // Ocultar todas las vistas primero
        binding.stepBasicInfo.setVisibility(View.GONE);
        binding.stepLocationMap.setVisibility(View.GONE);
        
        // Mostrar la vista correspondiente al paso actual
        switch (step) {
            case 1:
                binding.stepBasicInfo.setVisibility(View.VISIBLE);
                binding.btnPrevious.setVisibility(View.GONE);
                binding.btnNext.setVisibility(View.VISIBLE);
                binding.btnSaveDraft.setVisibility(View.VISIBLE);
                binding.btnPublish.setVisibility(View.GONE);
                break;
            case 2:
                binding.stepLocationMap.setVisibility(View.VISIBLE);
                binding.btnPrevious.setVisibility(View.VISIBLE);
                binding.btnNext.setVisibility(View.GONE);
                binding.btnSaveDraft.setVisibility(View.VISIBLE);
                binding.btnPublish.setVisibility(View.VISIBLE);
                break;
        }
        updateProgressIndicator();
    }

    private void updateProgressIndicator() {
        binding.tvStepIndicator.setText(String.format("Paso %d de %d", currentStep, TOTAL_STEPS));
        
        // Actualizar títulos según el paso
        switch (currentStep) {
            case 1:
                binding.tvStepTitle.setText("Información Básica");
                binding.tvStepSubtitle.setText("Ingresa los detalles principales del tour");
                break;
            case 2:
                binding.tvStepTitle.setText("Ubicación en el Mapa");
                binding.tvStepSubtitle.setText("Selecciona la ubicación donde se realizará el tour");
                break;
        }
    }

    private void nextStep() {
        if (validateCurrentStep()) {
            if (currentStep < TOTAL_STEPS) {
                saveCurrentStepData();
                currentStep++;
                showStep(currentStep);
            }
        }
    }

    private void previousStep() {
        if (currentStep > 1) {
            currentStep--;
            showStep(currentStep);
        }
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                return validateBasicInfo();
            case 2:
                return validateLocation();
            default:
                return false;
        }
    }

    private boolean validateBasicInfo() {
        String title = binding.etTourTitle.getText().toString().trim();
        String description = binding.etTourDescription.getText().toString().trim();
        String price = binding.etTourPrice.getText().toString().trim();
        String duration = binding.etTourDuration.getText().toString().trim();

        if (title.isEmpty()) {
            binding.etTourTitle.setError("Ingrese el título del tour");
            return false;
        }
        if (description.isEmpty()) {
            binding.etTourDescription.setError("Ingrese la descripción del tour");
            return false;
        }
        if (price.isEmpty()) {
            binding.etTourPrice.setError("Ingrese el precio del tour");
            return false;
        }
        if (duration.isEmpty()) {
            binding.etTourDuration.setError("Ingrese la duración del tour");
            return false;
        }
        return true;
    }

    private boolean validateLocation() {
        if (selectedLocation == null) {
            Toast.makeText(this, "Por favor selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveCurrentStepData() {
        switch (currentStep) {
            case 1:
                tourTitle = binding.etTourTitle.getText().toString().trim();
                tourDescription = binding.etTourDescription.getText().toString().trim();
                tourPrice = binding.etTourPrice.getText().toString().trim();
                tourDuration = binding.etTourDuration.getText().toString().trim();
                break;
        }
    }

    private void saveDraft() {
        saveCurrentStepData();
        Toast.makeText(this, "Tour guardado como borrador", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void publishTour() {
        if (validateCurrentStep()) {
            saveCurrentStepData();
            Toast.makeText(this, "Tour publicado exitosamente", Toast.LENGTH_SHORT).show();
            
            // Regresar a la lista de tours
            Intent intent = new Intent(this, admin_tours.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
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
        mMap = googleMap;
        
        // Configurar mapa centrado en Lima, Perú
        LatLng lima = new LatLng(-12.0464, -77.0428);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 11));
        
        // Permitir al usuario seleccionar ubicación haciendo clic
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            selectedLocation = latLng;
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Ubicación del Tour"));
        });
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("tours");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }
}

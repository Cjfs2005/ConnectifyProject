package com.example.connectifyproject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.connectifyproject.adapters.PlaceActivityAdapter;
import com.example.connectifyproject.adapters.TourPlaceAdapter;
import com.example.connectifyproject.adapters.TourServiceAdapter;
import com.example.connectifyproject.databinding.AdminCreateTourViewBinding;
import com.example.connectifyproject.models.TourPlace;
import com.example.connectifyproject.models.TourService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class admin_create_tour extends AppCompatActivity implements OnMapReadyCallback {
    
    private AdminCreateTourViewBinding binding;
    private GoogleMap mMap;
    private GoogleMap mMapPlaces;
    private int currentStep = 1;
    private final int TOTAL_STEPS = 4;
    
    private String tourTitle;
    private String tourDescription;
    private String tourPrice;
    private String tourDuration;
    private String tourDate;
    private List<TourPlace> selectedPlaces;
    private List<TourService> additionalServices;
    
    private LatLng selectedLocation;
    
    private TourPlaceAdapter placeAdapter;
    private PlaceActivityAdapter activityAdapter;
    private TourServiceAdapter serviceAdapter;
    
    private Calendar selectedCalendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminCreateTourViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        initializeData();
        setupUI();
        setupListeners();
        setupAdapters();
        initializeMaps();
        updateStepVisibility();
    }

    private void initializeData() {
        selectedPlaces = new ArrayList<>();
        additionalServices = new ArrayList<>();
        selectedCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.etTourDate.setText(dateFormat.format(selectedCalendar.getTime()));
    }

    private void setupUI() {
        setSupportActionBar(binding.topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupListeners() {
        binding.btnNext.setOnClickListener(v -> nextStep());
        binding.btnPrevious.setOnClickListener(v -> previousStep());
        binding.btnSaveDraft.setOnClickListener(v -> saveDraft());
        binding.btnCreateTour.setOnClickListener(v -> finishTour());
        
        binding.btnAddPlace.setOnClickListener(v -> addPlace());
        binding.btnAddService.setOnClickListener(v -> showAddServiceDialog());
        binding.etTourDate.setOnClickListener(v -> showDatePicker());
        
        binding.topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupAdapters() {
        placeAdapter = new TourPlaceAdapter(selectedPlaces, this::removePlace);
        binding.rvSelectedPlaces.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSelectedPlaces.setAdapter(placeAdapter);
        
        activityAdapter = new PlaceActivityAdapter(new ArrayList<>());
        binding.rvPlaceActivities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPlaceActivities.setAdapter(activityAdapter);
        
        serviceAdapter = new TourServiceAdapter(additionalServices, this::removeService);
        binding.rvServices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServices.setAdapter(serviceAdapter);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedCalendar.set(year, month, dayOfMonth);
                binding.etTourDate.setText(dateFormat.format(selectedCalendar.getTime()));
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void addPlace() {
        String searchText = binding.etSearchPlaces.getText().toString().trim();
        if (searchText.isEmpty()) {
            binding.etSearchPlaces.setError("Ingrese el nombre del lugar");
            return;
        }
        
        double lat, lng;
        if (selectedLocation != null) {
            lat = selectedLocation.latitude;
            lng = selectedLocation.longitude;
        } else {
            lat = -12.0464 + (Math.random() - 0.5) * 0.1;
            lng = -77.0428 + (Math.random() - 0.5) * 0.1;
        }
        
        TourPlace place = new TourPlace(searchText, "Lima, Peru", lat, lng);
        selectedPlaces.add(place);
        placeAdapter.notifyItemInserted(selectedPlaces.size() - 1);
        
        binding.etSearchPlaces.setText("");
        selectedLocation = null;
        Toast.makeText(this, "Lugar agregado al recorrido", Toast.LENGTH_SHORT).show();
        
        updateNextButtonVisibility();
    }

    private void removePlace(int position) {
        selectedPlaces.remove(position);
        placeAdapter.notifyItemRemoved(position);
        updateNextButtonVisibility();
    }

    private void showAddServiceDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_service, null);
        
        TextInputEditText etServiceName = dialogView.findViewById(R.id.et_service_name);
        TextInputEditText etServicePrice = dialogView.findViewById(R.id.et_service_price);
        TextInputEditText etServiceDescription = dialogView.findViewById(R.id.et_service_description);
        
        Dialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Agregar Servicio")
                .setView(dialogView)
                .setPositiveButton("Agregar", null)
                .setNegativeButton("Cancelar", null)
                .create();
        
        dialogView.findViewById(R.id.btn_add).setOnClickListener(v -> {
            String name = etServiceName.getText().toString().trim();
            String priceText = etServicePrice.getText().toString().trim();
            String description = etServiceDescription.getText().toString().trim();
            
            if (name.isEmpty()) {
                etServiceName.setError("Ingrese el nombre del servicio");
                return;
            }
            
            boolean isPaid = !priceText.isEmpty();
            double price = 0;
            
            if (isPaid) {
                try {
                    price = Double.parseDouble(priceText);
                } catch (NumberFormatException e) {
                    etServicePrice.setError("Ingrese un precio valido");
                    return;
                }
            }
            
            TourService service = new TourService(name, isPaid, price, description);
            additionalServices.add(service);
            serviceAdapter.notifyItemInserted(additionalServices.size() - 1);
            
            dialog.dismiss();
            Toast.makeText(this, "Servicio agregado", Toast.LENGTH_SHORT).show();
        });
        
        dialog.show();
    }

    private void removeService(int position) {
        additionalServices.remove(position);
        serviceAdapter.notifyItemRemoved(position);
    }

    private void nextStep() {
        if (validateCurrentStep()) {
            if (currentStep < TOTAL_STEPS) {
                currentStep++;
                updateStepVisibility();
                updateNavigationButtons();
            }
        }
    }

    private void previousStep() {
        if (currentStep > 1) {
            currentStep--;
            updateStepVisibility();
            updateNavigationButtons();
        }
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1: return validateStep1();
            case 2: return validateStep2();
            case 3: return validateStep3();
            case 4: return validateStep4();
            default: return true;
        }
    }

    private boolean validateStep1() {
        tourTitle = binding.etTourTitle.getText().toString().trim();
        tourDescription = binding.etTourDescription.getText().toString().trim();
        tourPrice = binding.etTourPrice.getText().toString().trim();
        tourDuration = binding.etTourDuration.getText().toString().trim();
        
        if (tourTitle.isEmpty()) {
            binding.etTourTitle.setError("Ingrese el titulo del tour");
            return false;
        }
        if (tourDescription.isEmpty()) {
            binding.etTourDescription.setError("Ingrese la descripcion");
            return false;
        }
        if (tourPrice.isEmpty()) {
            binding.etTourPrice.setError("Ingrese el precio");
            return false;
        }
        if (tourDuration.isEmpty()) {
            binding.etTourDuration.setError("Ingrese la duracion");
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (selectedPlaces.isEmpty()) {
            Toast.makeText(this, "Agregue al menos un lugar al recorrido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep3() {
        tourDate = binding.etTourDate.getText().toString();
        if (tourDate.isEmpty()) {
            Toast.makeText(this, "Seleccione la fecha de realizacion", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep4() {
        return true;
    }

    private void updateStepVisibility() {
        binding.stepBasicInfo.setVisibility(View.GONE);
        binding.stepPlaces.setVisibility(View.GONE);
        binding.stepActivities.setVisibility(View.GONE);
        binding.stepServices.setVisibility(View.GONE);
        
        switch (currentStep) {
            case 1:
                binding.stepBasicInfo.setVisibility(View.VISIBLE);
                binding.tvStepTitle.setText("Informacion Basica");
                binding.tvStepIndicator.setText("Paso 1 de 4");
                break;
            case 2:
                binding.stepPlaces.setVisibility(View.VISIBLE);
                binding.tvStepTitle.setText("Lugares del Recorrido");
                binding.tvStepIndicator.setText("Paso 2 de 4");
                break;
            case 3:
                binding.stepActivities.setVisibility(View.VISIBLE);
                binding.tvStepTitle.setText("Fecha y Actividades");
                binding.tvStepIndicator.setText("Paso 3 de 4");
                updateMapRoute();
                break;
            case 4:
                binding.stepServices.setVisibility(View.VISIBLE);
                binding.tvStepTitle.setText("Servicios Adicionales");
                binding.tvStepIndicator.setText("Paso 4 de 4");
                break;
        }
        
        updateNavigationButtons();
        updateNextButtonVisibility();
    }

    private void updateNavigationButtons() {
        binding.btnPrevious.setVisibility(currentStep > 1 ? View.VISIBLE : View.GONE);
        binding.btnNext.setVisibility(currentStep < TOTAL_STEPS ? View.VISIBLE : View.GONE);
        binding.btnCreateTour.setVisibility(currentStep == TOTAL_STEPS ? View.VISIBLE : View.GONE);
        binding.btnSaveDraft.setVisibility(currentStep < TOTAL_STEPS ? View.VISIBLE : View.GONE);
    }

    private void updateNextButtonVisibility() {
        if (currentStep == 2) {
            binding.btnNext.setVisibility(!selectedPlaces.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateMapRoute() {
        if (mMap != null && !selectedPlaces.isEmpty()) {
            mMap.clear();
            
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(getResources().getColor(R.color.brand_purple_dark))
                    .width(8);
            
            for (int i = 0; i < selectedPlaces.size(); i++) {
                TourPlace place = selectedPlaces.get(i);
                LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
                
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(place.getName()));
                
                polylineOptions.add(latLng);
            }
            
            if (selectedPlaces.size() > 1) {
                mMap.addPolyline(polylineOptions);
            }
            
            LatLng firstPlace = new LatLng(selectedPlaces.get(0).getLatitude(), selectedPlaces.get(0).getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPlace, 12));
        }
    }

    private void saveDraft() {
        Toast.makeText(this, "Borrador guardado", Toast.LENGTH_SHORT).show();
    }

    private void finishTour() {
        if (validateCurrentStep()) {
            Intent intent = new Intent(this, admin_tours.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void initializeMaps() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        SupportMapFragment mapFragmentPlaces = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment_places);
        if (mapFragmentPlaces != null) {
            mapFragmentPlaces.getMapAsync(googleMap -> {
                mMapPlaces = googleMap;
                setupPlacesMap();
            });
        }
    }

    private void setupPlacesMap() {
        if (mMapPlaces != null) {
            LatLng lima = new LatLng(-12.0464, -77.0428);
            mMapPlaces.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 11));
            
            mMapPlaces.setOnMapClickListener(latLng -> {
                mMapPlaces.clear();
                mMapPlaces.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Ubicacion seleccionada"));
                selectedLocation = latLng;
                Toast.makeText(admin_create_tour.this, 
                        "Ubicacion seleccionada. Ingrese el nombre y presione Agregar Lugar", 
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng lima = new LatLng(-12.0464, -77.0428);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 11));
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 1) {
            previousStep();
        } else {
            super.onBackPressed();
        }
    }
}
package com.example.connectifyproject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import com.example.connectifyproject.storage.AdminStorage;
import com.example.connectifyproject.storage.AdminStorage.TourDraft;
import com.example.connectifyproject.utils.NotificationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.IOException;
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
    private final AdminStorage adminStorage = new AdminStorage();

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
        // Notificaciones: crear canal y solicitar permiso si aplica
        NotificationHelper.createChannels(this);
        NotificationHelper.requestPostNotificationsIfNeeded(this);
        updateStepVisibility();
    }

    private void initializeData() {
        selectedPlaces = new ArrayList<>();
        additionalServices = new ArrayList<>();
        selectedCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.etTourDate.setText(dateFormat.format(selectedCalendar.getTime()));

        // Cargar borrador si existe
        try {
            if (adminStorage.hasDraft(this)) {
                TourDraft draft = adminStorage.loadDraft(this);
                if (draft != null) {
                    binding.etTourTitle.setText(draft.title);
                    binding.etTourDescription.setText(draft.description);
                    binding.etTourPrice.setText(draft.price);
                    binding.etTourDuration.setText(draft.duration);
                    if (draft.date != null && !draft.date.isEmpty()) {
                        binding.etTourDate.setText(draft.date);
                    }
                    if (draft.places != null) {
                        selectedPlaces.addAll(draft.places);
                    }
                    if (draft.services != null) {
                        additionalServices.addAll(draft.services);
                    }
                }
            }
        } catch (Exception ignored) { }
    }

    private void setupUI() {
        setSupportActionBar(binding.topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        // Configurar listener para el ícono de búsqueda (usa id del TextInputLayout)
        binding.tilSearchPlaces.setEndIconOnClickListener(v -> searchLocation());
    }
    

    private void setupListeners() {
        binding.btnNext.setOnClickListener(v -> nextStep());
        binding.btnPrevious.setOnClickListener(v -> previousStep());
        binding.btnSaveDraft.setOnClickListener(v -> saveDraft());
        binding.btnCreateTour.setOnClickListener(v -> finishTour());
        
        binding.btnAddPlace.setOnClickListener(v -> addPlace());
        binding.btnAddService.setOnClickListener(v -> showAddServiceDialog());
        binding.etTourDate.setOnClickListener(v -> showDatePicker());
        
        // Agregar listener para búsqueda de lugares con Enter
        binding.etSearchPlaces.setOnEditorActionListener((v, actionId, event) -> {
            searchLocation();
            return true;
        });
        
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

    private void searchLocation() {
        String searchText = binding.etSearchPlaces.getText().toString().trim();
        if (searchText.isEmpty()) {
            binding.etSearchPlaces.setError("Ingrese el nombre del lugar a buscar");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(searchText, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
                
                if (mMapPlaces != null) {
                    mMapPlaces.clear();
                    mMapPlaces.addMarker(new MarkerOptions()
                            .position(location)
                            .title(searchText));
                    mMapPlaces.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                    selectedLocation = location;
                    
                    Toast.makeText(this, "Ubicación encontrada. Verifique en el mapa y presione 'Agregar al Recorrido'", 
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Error: Mapa no disponible", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se encontró la ubicación. Intente con otro nombre.", 
                        Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error al buscar la ubicación. Verifique su conexión.", 
                    Toast.LENGTH_SHORT).show();
        }
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
        MaterialSwitch switchIsPaid = dialogView.findViewById(R.id.switch_is_paid);
        TextInputLayout tilServicePrice = dialogView.findViewById(R.id.til_service_price);
        
        // Configurar visibility del campo precio basado en el switch
        switchIsPaid.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilServicePrice.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                etServicePrice.setText("");
            }
        });
        
        Dialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();
        
        dialogView.findViewById(R.id.btn_add).setOnClickListener(v -> {
            String name = etServiceName.getText().toString().trim();
            String priceText = etServicePrice.getText().toString().trim();
            String description = etServiceDescription.getText().toString().trim();
            boolean isPaid = switchIsPaid.isChecked();
            
            if (name.isEmpty()) {
                etServiceName.setError("Ingrese el nombre del servicio");
                return;
            }
            
            double price = 0;
            if (isPaid) {
                if (priceText.isEmpty()) {
                    etServicePrice.setError("Ingrese el precio del servicio");
                    return;
                }
                try {
                    price = Double.parseDouble(priceText);
                    if (price <= 0) {
                        etServicePrice.setError("El precio debe ser mayor a 0");
                        return;
                    }
                } catch (NumberFormatException e) {
                    etServicePrice.setError("Ingrese un precio válido");
                    return;
                }
            }
            
            TourService service = new TourService(name, isPaid, price, description);
            additionalServices.add(service);
            serviceAdapter.notifyItemInserted(additionalServices.size() - 1);
            
            dialog.dismiss();
            Toast.makeText(this, "Servicio agregado", Toast.LENGTH_SHORT).show();
        });
        
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        
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
                updateActivitiesList();
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

    private void updateActivitiesList() {
        // Actualizar el adapter con los lugares seleccionados para que aparezcan en el paso 3
        activityAdapter = new PlaceActivityAdapter(selectedPlaces);
        binding.rvPlaceActivities.setAdapter(activityAdapter);
    }

    private void saveDraft() {
        try {
            TourDraft draft = new TourDraft();
            draft.title = binding.etTourTitle.getText().toString().trim();
            draft.description = binding.etTourDescription.getText().toString().trim();
            draft.price = binding.etTourPrice.getText().toString().trim();
            draft.duration = binding.etTourDuration.getText().toString().trim();
            draft.date = binding.etTourDate.getText().toString().trim();
            draft.places = new ArrayList<>(selectedPlaces);
            draft.services = new ArrayList<>(additionalServices);
            adminStorage.saveDraft(this, draft);
            Toast.makeText(this, "Borrador guardado", Toast.LENGTH_SHORT).show();
            // Volver automáticamente a la pantalla de gestión de tours
            Intent intent = new Intent(this, admin_tours.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo guardar el borrador", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishTour() {
        if (validateCurrentStep()) {
            // Enviar notificación de creado
            Intent openIntent = new Intent(this, admin_tours.class);
            NotificationHelper.notifyTourCreated(this, openIntent);
            // Limpiar borrador
            adminStorage.clearDraft(this);
            Intent intent = new Intent(this, admin_tours.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
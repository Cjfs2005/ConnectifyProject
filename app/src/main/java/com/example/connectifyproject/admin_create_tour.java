package com.example.connectifyproject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.connectifyproject.adapters.PlaceActivityAdapter;
import com.example.connectifyproject.adapters.TourPlaceAdapter;
import com.example.connectifyproject.adapters.TourServiceAdapter;
import com.example.connectifyproject.databinding.AdminCreateTourViewBinding;
import com.example.connectifyproject.models.TourBorrador;
import com.example.connectifyproject.models.TourPlace;
import com.example.connectifyproject.models.TourService;
import com.example.connectifyproject.services.AdminTourService;
import com.example.connectifyproject.utils.NotificacionLogUtils;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class admin_create_tour extends AppCompatActivity implements OnMapReadyCallback {
    
    // ⚠️ MODO PRUEBA: Cambiar a true para permitir crear tours en el mismo día (solo para testing)
    private static final boolean TEST_MODE = false;
    
    private AdminCreateTourViewBinding binding;
    private GoogleMap mMap;
    private GoogleMap mMapPlaces;
    private int currentStep = 1;
    private final int TOTAL_STEPS = 4;
    
    private String tourTitle;
    private String tourCiudad;
    private String tourDescription;
    private String tourPrice;
    private String tourDuration;
    private String tourDate;
    private String tourStartTime;
    private String tourEndTime;
    private String pagoGuia;
    private List<String> selectedIdiomas;
    private String consideraciones;
    private List<TourPlace> selectedPlaces;
    private List<TourService> additionalServices;
    
    private LatLng selectedLocation;
    
    private TourPlaceAdapter placeAdapter;
    private PlaceActivityAdapter activityAdapter;
    private TourServiceAdapter serviceAdapter;
    
    private Calendar selectedCalendar;
    private SimpleDateFormat dateFormat;
    
    // Firebase
    private AdminTourService adminTourService;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String empresaId;
    private String nombreEmpresa; // Nombre de la empresa del usuario
    private String correoEmpresa; // Correo de la empresa del usuario
    private String currentBorradorId; // ID del borrador actual
    
    // Gestión de imágenes
    private List<Uri> selectedImageUris; // URIs locales de imágenes seleccionadas
    private List<String> uploadedImageUrls; // URLs de imágenes ya subidas a Storage
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminCreateTourViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        initializeFirebase();
        initializeData();
        setupImagePicker();
        setupUI();
        setupCiudadSpinner();
        setupListeners();
        setupAdapters();
        initializeMaps();
        updateStepVisibility();
        
        // Verificar si se está editando un borrador existente
        Intent intent = getIntent();
        if (intent.hasExtra("borradorId")) {
            String borradorId = intent.getStringExtra("borradorId");
            if (borradorId != null && !borradorId.isEmpty()) {
                currentBorradorId = borradorId;
                
                // Cambiar título del toolbar
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Editar Tour");
                }
                binding.topAppBar.setTitle("Editar Tour");
                
                // Cargar datos del borrador
                loadBorradorData(borradorId);
            }
        }
    }
    
    private void initializeFirebase() {
        adminTourService = new AdminTourService();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Obtener empresaId del usuario actual
        // Para usuarios tipo Administrador, el empresaId es el mismo UID
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String rol = documentSnapshot.getString("rol");
                        
                        // Si es administrador, el empresaId es el mismo UID
                        if ("Administrador".equals(rol)) {
                            empresaId = userId;
                            
                            // Obtener datos de la empresa
                            nombreEmpresa = documentSnapshot.getString("nombreEmpresa");
                            correoEmpresa = documentSnapshot.getString("correoEmpresa");
                            
                            // Si no tiene correoEmpresa, usar el email del usuario
                            if (correoEmpresa == null || correoEmpresa.isEmpty()) {
                                correoEmpresa = documentSnapshot.getString("email");
                            }
                            
                            Log.d("AdminCreateTour", "EmpresaId: " + empresaId + 
                                  ", Nombre: " + nombreEmpresa + ", Correo: " + correoEmpresa);
                        } else {
                            // Si tiene empresaId como campo (casos legacy), usarlo
                            String empresaIdField = documentSnapshot.getString("empresaId");
                            if (empresaIdField != null && !empresaIdField.isEmpty()) {
                                empresaId = empresaIdField;
                            } else {
                                empresaId = userId; // Fallback al UID
                            }
                            nombreEmpresa = documentSnapshot.getString("nombreEmpresa");
                            correoEmpresa = documentSnapshot.getString("correoEmpresa");
                        }
                    } else {
                        Toast.makeText(this, "No se encontró información del usuario", Toast.LENGTH_SHORT).show();
                        Log.e("AdminCreateTour", "Documento de usuario no existe");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos de usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("AdminCreateTour", "Error al obtener datos de usuario", e);
                });
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            Log.e("AdminCreateTour", "Usuario no autenticado");
        }
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    if (selectedImageUris.size() + uris.size() > 3) {
                        Toast.makeText(this, "Máximo 3 imágenes permitidas", Toast.LENGTH_SHORT).show();
                        // Tomar solo las que caben
                        int remaining = 3 - selectedImageUris.size();
                        for (int i = 0; i < Math.min(remaining, uris.size()); i++) {
                            selectedImageUris.add(uris.get(i));
                        }
                    } else {
                        selectedImageUris.addAll(uris);
                    }
                    updateImagePreview();
                    Toast.makeText(this, selectedImageUris.size() + " imagen(es) seleccionada(s)", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void initializeData() {
        selectedPlaces = new ArrayList<>();
        additionalServices = new ArrayList<>();
        selectedImageUris = new ArrayList<>();
        uploadedImageUrls = new ArrayList<>();
        selectedIdiomas = new ArrayList<>();
        // Fecha por defecto: dentro de 1 día (24 horas)
        selectedCalendar = Calendar.getInstance();
        selectedCalendar.add(Calendar.DAY_OF_MONTH, 1);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.etTourDate.setText(dateFormat.format(selectedCalendar.getTime()));
    }

    private void setupUI() {
        setSupportActionBar(binding.topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        // Configurar listener para el ícono de búsqueda después del inflado
        setupSearchIconListener();
    }
    
    private void setupSearchIconListener() {
        Log.d("AdminCreateTour", ">>> setupSearchIconListener() INICIADO <<<");
        
        // Usar findViewById directamente con el ID del TextInputLayout
        TextInputLayout tilSearch = findViewById(R.id.til_search_places);
        
        if (tilSearch != null) {
            Log.d("AdminCreateTour", "✓ TextInputLayout encontrado con findViewById");
            
            tilSearch.setEndIconOnClickListener(v -> {
                Log.d("AdminCreateTour", "✓✓✓ ÍCONO DE LUPA PRESIONADO ✓✓✓");
                searchLocation();
            });
            
            Log.d("AdminCreateTour", "✓ Listener configurado exitosamente");
        } else {
            Log.e("AdminCreateTour", "✗ ERROR: TextInputLayout NO encontrado");
        }
    }
    
    private void setupCiudadSpinner() {
        String[] ciudades = {
            "Lima",
            "Cusco",
            "Arequipa",
            "Trujillo",
            "Chiclayo",
            "Piura",
            "Iquitos",
            "Huancayo",
            "Tacna",
            "Puno"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            ciudades
        );
        binding.spinnerCiudad.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnNext.setOnClickListener(v -> nextStep());
        binding.btnPrevious.setOnClickListener(v -> previousStep());
        binding.btnSaveDraft.setOnClickListener(v -> saveDraft());
        binding.btnCreateTour.setOnClickListener(v -> finishTour());
        
        binding.btnAddPlace.setOnClickListener(v -> addPlace());
        binding.btnAddService.setOnClickListener(v -> showAddServiceDialog());
        binding.etTourDate.setOnClickListener(v -> showDatePicker());
        
        // Listeners para los time pickers
        binding.etTourStartTime.setOnClickListener(v -> showTimePickerStart());
        binding.etTourEndTime.setOnClickListener(v -> showTimePickerEnd());
        
        // Listener para selector de idiomas
        binding.etIdiomasRequeridos.setOnClickListener(v -> showIdiomasDialog());
        
        // Botón para seleccionar imágenes (agregar en el layout si no existe)
        if (binding.btnSelectImages != null) {
            binding.btnSelectImages.setOnClickListener(v -> selectImages());
        }
        
        // Agregar listener para búsqueda de lugares con Enter
        binding.etSearchPlaces.setOnEditorActionListener((v, actionId, event) -> {
            searchLocation();
            return true;
        });
        
        binding.topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void selectImages() {
        if (selectedImageUris.size() >= 3) {
            Toast.makeText(this, "Ya ha seleccionado el máximo de 3 imágenes", Toast.LENGTH_SHORT).show();
            return;
        }
        imagePickerLauncher.launch("image/*");
    }
    
    private void updateImagePreview() {
        // Aquí podrías actualizar un RecyclerView o ImageView con las imágenes seleccionadas
        // Por ahora solo mostramos el contador
        if (binding.tvImageCount != null) {
            binding.tvImageCount.setText(selectedImageUris.size() + "/3 imágenes");
        }
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
        // Establecer fecha mínima: dentro de 1 día (24 horas)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 1);
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);
        minDate.set(Calendar.MILLISECOND, 0);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.show();
    }
    
    private void showTimePickerStart() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
            this,
            (view, hourOfDay, minuteOfDay) -> {
                tourStartTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfDay);
                binding.etTourStartTime.setText(tourStartTime);
                
                // Calcular duración automáticamente si ya hay hora de fin
                calcularDuracion();
            },
            hour,
            minute,
            true // Formato 24 horas
        );
        timePickerDialog.show();
    }
    
    private void showTimePickerEnd() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
            this,
            (view, hourOfDay, minuteOfDay) -> {
                tourEndTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfDay);
                binding.etTourEndTime.setText(tourEndTime);
                
                // Calcular duración automáticamente si ya hay hora de inicio
                calcularDuracion();
            },
            hour,
            minute,
            true // Formato 24 horas
        );
        timePickerDialog.show();
    }
    
    /**
     * Calcula automáticamente la duración del tour basándose en hora inicio y fin
     */
    private void calcularDuracion() {
        if (tourStartTime != null && !tourStartTime.isEmpty() && 
            tourEndTime != null && !tourEndTime.isEmpty()) {
            
            try {
                // Parse hora inicio
                String[] startParts = tourStartTime.split(":");
                int startHour = Integer.parseInt(startParts[0]);
                int startMinute = Integer.parseInt(startParts[1]);
                
                // Parse hora fin
                String[] endParts = tourEndTime.split(":");
                int endHour = Integer.parseInt(endParts[0]);
                int endMinute = Integer.parseInt(endParts[1]);
                
                // Calcular diferencia en minutos
                int startTotalMinutes = startHour * 60 + startMinute;
                int endTotalMinutes = endHour * 60 + endMinute;
                int differenceMinutes = endTotalMinutes - startTotalMinutes;
                
                // Si la hora de fin es menor que la de inicio, asumir que cruza medianoche
                if (differenceMinutes < 0) {
                    differenceMinutes += 24 * 60; // Agregar 24 horas
                }
                
                // Convertir a horas con decimales
                double durationHours = differenceMinutes / 60.0;
                
                // Formatear y mostrar duración
                String duracionFormateada;
                if (differenceMinutes % 60 == 0) {
                    // Duración exacta en horas (ej: 2 hrs, 3 hrs)
                    duracionFormateada = String.valueOf((int) durationHours);
                } else {
                    // Duración con decimales (ej: 2.5 hrs, 1.75 hrs)
                    duracionFormateada = String.format(Locale.getDefault(), "%.2f", durationHours);
                }
                
                binding.etTourDuration.setText(duracionFormateada);
                
                // Mostrar mensaje informativo
                Toast.makeText(this, 
                    "Duración calculada: " + duracionFormateada + " hrs", 
                    Toast.LENGTH_SHORT).show();
                
            } catch (Exception e) {
                Log.e("AdminCreateTour", "Error al calcular duración", e);
            }
        }
    }
    
    private void showIdiomasDialog() {
        String[] idiomasDisponibles = {"Español", "Inglés", "Francés", "Alemán", "Italiano", "Chino", "Japonés"};
        boolean[] idiomasSeleccionados = new boolean[idiomasDisponibles.length];
        
        // Marcar los idiomas ya seleccionados
        for (int i = 0; i < idiomasDisponibles.length; i++) {
            if (selectedIdiomas.contains(idiomasDisponibles[i])) {
                idiomasSeleccionados[i] = true;
            }
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar idiomas requeridos")
            .setMultiChoiceItems(idiomasDisponibles, idiomasSeleccionados, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!selectedIdiomas.contains(idiomasDisponibles[which])) {
                        selectedIdiomas.add(idiomasDisponibles[which]);
                    }
                } else {
                    selectedIdiomas.remove(idiomasDisponibles[which]);
                }
            })
            .setPositiveButton("Aceptar", (dialog, which) -> {
                if (selectedIdiomas.isEmpty()) {
                    binding.etIdiomasRequeridos.setText("");
                } else {
                    binding.etIdiomasRequeridos.setText(String.join(", ", selectedIdiomas));
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
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
        
        TourPlace place = new TourPlace(searchText, String.format("%.5f, %.5f", lat, lng), lat, lng);
        selectedPlaces.add(place);
        placeAdapter.notifyItemInserted(selectedPlaces.size() - 1);
        
        binding.etSearchPlaces.setText("");
        selectedLocation = null;
        Toast.makeText(this, "Lugar agregado al recorrido", Toast.LENGTH_SHORT).show();
        
        updateNextButtonVisibility();
    }

    private void searchLocation() {
        Log.d("AdminCreateTour", ">>> searchLocation() INICIADO <<<");
        
        String searchText = binding.etSearchPlaces.getText().toString().trim();
        Log.d("AdminCreateTour", "Texto de búsqueda: '" + searchText + "'");
        
        if (searchText.isEmpty()) {
            binding.etSearchPlaces.setError("Ingrese el nombre del lugar a buscar");
            Log.d("AdminCreateTour", "Búsqueda cancelada: texto vacío");
            return;
        }

        Log.d("AdminCreateTour", "Iniciando Geocoder...");
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
                    
                    // Animar cámara con zoom apropiado para ver detalles
                    // Zoom 16 = vista de calle/edificios
                    mMapPlaces.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(location, 16),
                        1000, // Duración de animación en ms
                        null
                    );
                    
                    selectedLocation = location;
                    
                    Toast.makeText(this, "✓ Ubicación encontrada. Verifique en el mapa y presione 'Agregar al Recorrido'", 
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
        tourCiudad = binding.spinnerCiudad.getText().toString().trim();
        tourDescription = binding.etTourDescription.getText().toString().trim();
        tourPrice = binding.etTourPrice.getText().toString().trim();
        tourDuration = binding.etTourDuration.getText().toString().trim();
        tourStartTime = binding.etTourStartTime.getText().toString().trim();
        tourEndTime = binding.etTourEndTime.getText().toString().trim();
        
        if (tourTitle.isEmpty()) {
            binding.etTourTitle.setError("Ingrese el titulo del tour");
            return false;
        }
        if (tourCiudad.isEmpty()) {
            binding.tilCiudad.setError("Seleccione la ciudad del tour");
            Toast.makeText(this, "Seleccione la ciudad del tour", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            binding.tilCiudad.setError(null);
        }
        if (tourDescription.isEmpty()) {
            binding.etTourDescription.setError("Ingrese la descripcion");
            return false;
        }
        if (tourPrice.isEmpty()) {
            binding.etTourPrice.setError("Ingrese el precio");
            return false;
        }
        if (tourStartTime.isEmpty()) {
            binding.etTourStartTime.setError("Seleccione la hora de inicio");
            Toast.makeText(this, "Seleccione la hora de inicio del tour", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tourEndTime.isEmpty()) {
            binding.etTourEndTime.setError("Seleccione la hora de fin");
            Toast.makeText(this, "Seleccione la hora de fin del tour", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validar que la fecha y hora sean al menos 24 horas en el futuro
        if (!validateFechaMinima()) {
            binding.etTourDate.setError("El tour debe iniciar en al menos 24 horas (1 día)");
            Toast.makeText(this, "El tour debe programarse para iniciar en al menos 24 horas (1 día) desde ahora", Toast.LENGTH_LONG).show();
            return false;
        }
        
        // Validar que la hora de fin sea al menos 1 hora después de la hora de inicio
        if (!validateTimeRange(tourStartTime, tourEndTime)) {
            Toast.makeText(this, "La hora de fin debe ser al menos 1 hora después de la hora de inicio", Toast.LENGTH_LONG).show();
            return false;
        }
        
        // Verificar que la duración se haya calculado (debería ser automática)
        if (tourDuration.isEmpty()) {
            binding.etTourDuration.setError("La duración no se calculó correctamente");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida que la fecha y hora seleccionadas sean al menos 24 horas en el futuro
     * ⚠️ En modo TEST_MODE=true, permite crear tours para el mismo día
     */
    private boolean validateFechaMinima() {
        if (selectedCalendar == null) {
            return false;
        }
        
        // 🧪 MODO PRUEBA: Si TEST_MODE está activo, no validar fecha mínima
        if (TEST_MODE) {
            Log.d("AdminCreateTour", "⚠️ TEST_MODE ACTIVO - Validación de fecha deshabilitada");
            return true;
        }
        
        // Validar que se haya seleccionado hora de inicio
        if (tourStartTime == null || tourStartTime.isEmpty()) {
            return false;
        }
        
        try {
            // Combinar fecha seleccionada con hora de inicio
            Calendar fechaHoraInicio = (Calendar) selectedCalendar.clone();
            String[] timeParts = tourStartTime.split(":");
            fechaHoraInicio.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            fechaHoraInicio.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            fechaHoraInicio.set(Calendar.SECOND, 0);
            fechaHoraInicio.set(Calendar.MILLISECOND, 0);
            
            // Calcular 24 horas desde ahora (1 día)
            Calendar limite24Horas = Calendar.getInstance();
            limite24Horas.add(Calendar.HOUR_OF_DAY, 24);
            
            // La fecha+hora de inicio debe ser al menos 24 horas en el futuro
            return !fechaHoraInicio.before(limite24Horas);
        } catch (Exception e) {
            Log.e("AdminCreateTour", "Error validando fecha mínima", e);
            return false;
        }
    }
    
    /**
     * Valida que la hora de fin sea al menos 1 hora después de la hora de inicio
     */
    private boolean validateTimeRange(String startTime, String endTime) {
        try {
            // Parse hora inicio
            String[] startParts = startTime.split(":");
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            
            // Parse hora fin
            String[] endParts = endTime.split(":");
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            
            // Calcular diferencia en minutos
            int startTotalMinutes = startHour * 60 + startMinute;
            int endTotalMinutes = endHour * 60 + endMinute;
            int differenceMinutes = endTotalMinutes - startTotalMinutes;
            
            // Si la hora de fin es menor, asumir que cruza medianoche
            if (differenceMinutes < 0) {
                differenceMinutes += 24 * 60;
            }
            
            // Validar que la diferencia sea al menos 60 minutos (1 hora)
            return differenceMinutes >= 60;
            
        } catch (Exception e) {
            Log.e("AdminCreateTour", "Error al validar rango de tiempo", e);
            return false;
        }
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
        
        // Validar que todos los lugares del itinerario tengan actividades
        for (int i = 0; i < selectedPlaces.size(); i++) {
            TourPlace place = selectedPlaces.get(i);
            if (place.getActivities() == null || place.getActivities().trim().isEmpty()) {
                Toast.makeText(this, "El lugar '" + place.getName() + "' debe tener al menos una actividad. Complete las actividades en la lista inferior.", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        
        return true;
    }

    private boolean validateStep4() {
        pagoGuia = binding.etPagoGuia.getText().toString().trim();
        consideraciones = binding.etConsideraciones.getText().toString().trim();
        
        // Validar pago al guía
        if (pagoGuia.isEmpty()) {
            binding.etPagoGuia.setError("Ingrese el pago al guía");
            Toast.makeText(this, "Debe especificar el pago al guía", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            double pago = Double.parseDouble(pagoGuia);
            if (pago <= 0) {
                binding.etPagoGuia.setError("El pago debe ser mayor a 0");
                Toast.makeText(this, "El pago al guía debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            binding.etPagoGuia.setError("Ingrese un monto válido");
            Toast.makeText(this, "Ingrese un monto válido para el pago al guía", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validar idiomas
        if (selectedIdiomas.isEmpty()) {
            binding.etIdiomasRequeridos.setError("Seleccione al menos un idioma");
            Toast.makeText(this, "Debe seleccionar al menos un idioma requerido", Toast.LENGTH_SHORT).show();
            return false;
        }
        
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
        if (!validateCurrentStep()) {
            return;
        }
        
        if (empresaId == null) {
            Toast.makeText(this, "Error: No se pudo obtener datos de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar que haya al menos 1 imagen
        if (selectedImageUris.isEmpty() && uploadedImageUrls.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar al menos 1 imagen del tour", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgressDialog("Guardando borrador...");
        
        // Si hay nuevas imágenes, subirlas primero
        if (!selectedImageUris.isEmpty()) {
            uploadImagesAndSaveDraft();
        } else {
            // Si no hay nuevas imágenes, guardar directamente
            saveBorradorToFirebase();
        }
    }
    
    private void uploadImagesAndSaveDraft() {
        // Crear o usar ID de borrador
        if (currentBorradorId == null) {
            currentBorradorId = db.collection("tours_borradores").document().getId();
        }
        
        List<com.google.android.gms.tasks.Task<String>> uploadTasks = new ArrayList<>();
        
        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            int imageIndex = uploadedImageUrls.size() + i;
            
            com.google.android.gms.tasks.Task<String> uploadTask = adminTourService.subirImagenBorrador(
                imageUri, empresaId, currentBorradorId, imageIndex
            );
            uploadTasks.add(uploadTask);
        }
        
        // Esperar a que todas las imágenes se suban
        com.google.android.gms.tasks.Tasks.whenAllSuccess(uploadTasks)
            .addOnSuccessListener(urls -> {
                // Agregar las nuevas URLs a la lista
                for (Object url : urls) {
                    uploadedImageUrls.add((String) url);
                }
                
                // Limpiar URIs locales ya que ya están subidas
                selectedImageUris.clear();
                updateImagePreview();
                
                // Ahora guardar el borrador
                saveBorradorToFirebase();
            })
            .addOnFailureListener(e -> {
                dismissProgressDialog();
                Toast.makeText(this, "Error al subir imágenes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void saveBorradorToFirebase() {
        // Validar que se hayan obtenido los datos de la empresa
        if (empresaId == null || empresaId.isEmpty()) {
            dismissProgressDialog();
            Toast.makeText(this, "Error: No se pudo obtener datos de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }
        
        TourBorrador borrador = createBorradorFromData();
        
        adminTourService.guardarBorrador(borrador)
            .addOnSuccessListener(borradorId -> {
                dismissProgressDialog();
                currentBorradorId = borradorId;
                Toast.makeText(this, "Borrador guardado exitosamente", Toast.LENGTH_SHORT).show();
                
                // Regresar a la vista de gestión de tours
                Intent intent = new Intent(this, admin_tours.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                dismissProgressDialog();
                Toast.makeText(this, "Error al guardar borrador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private TourBorrador createBorradorFromData() {
        TourBorrador borrador = new TourBorrador();
        
        if (currentBorradorId != null) {
            borrador.setId(currentBorradorId);
        }
        
        borrador.setTitulo(tourTitle != null ? tourTitle : binding.etTourTitle.getText().toString().trim());
        borrador.setCiudad(tourCiudad != null ? tourCiudad : binding.spinnerCiudad.getText().toString().trim());
        borrador.setDescripcion(tourDescription != null ? tourDescription : binding.etTourDescription.getText().toString().trim());
        
        try {
            double precio = Double.parseDouble(tourPrice != null ? tourPrice : binding.etTourPrice.getText().toString().trim());
            borrador.setPrecio(precio);
        } catch (NumberFormatException e) {
            borrador.setPrecio(0.0);
        }
        
        // Duracion como String (ej: "2 horas", "4.5 horas")
        String duracion = tourDuration != null ? tourDuration : binding.etTourDuration.getText().toString().trim();
        borrador.setDuracion(duracion);
        
        // Hora de inicio y fin
        borrador.setHoraInicio(tourStartTime != null ? tourStartTime : binding.etTourStartTime.getText().toString().trim());
        borrador.setHoraFin(tourEndTime != null ? tourEndTime : binding.etTourEndTime.getText().toString().trim());
        
        // Convertir fecha Calendar a String dd/MM/yyyy
        if (selectedCalendar != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaStr = dateFormat.format(selectedCalendar.getTime());
            borrador.setFechaRealizacion(fechaStr);
        }
        
        // Convertir itinerario (List<TourPlace> a List<Map>)
        List<Map<String, Object>> itinerario = new ArrayList<>();
        for (TourPlace place : selectedPlaces) {
            Map<String, Object> placeMap = new HashMap<>();
            placeMap.put("nombre", place.getName());
            placeMap.put("direccion", place.getAddress());
            placeMap.put("latitud", place.getLatitude());
            placeMap.put("longitud", place.getLongitude());
            
            // Guardar actividades del punto
            if (place.getActivities() != null && !place.getActivities().isEmpty()) {
                // Convertir actividades de String a List<String> separadas por salto de línea
                String[] actividadesArray = place.getActivities().split("\n");
                List<String> actividadesList = new ArrayList<>();
                for (String actividad : actividadesArray) {
                    String actividadTrim = actividad.trim();
                    if (!actividadTrim.isEmpty()) {
                        actividadesList.add(actividadTrim);
                    }
                }
                if (!actividadesList.isEmpty()) {
                    placeMap.put("actividades", actividadesList);
                }
            }
            
            itinerario.add(placeMap);
        }
        borrador.setItinerario(itinerario);
        
        // Convertir servicios adicionales
        List<Map<String, Object>> servicios = new ArrayList<>();
        for (TourService service : additionalServices) {
            Map<String, Object> serviceMap = new HashMap<>();
            serviceMap.put("nombre", service.getName());
            serviceMap.put("esPagado", service.isPaid());
            serviceMap.put("precio", service.getPrice());
            serviceMap.put("descripcion", service.getDescription());
            servicios.add(serviceMap);
        }
        borrador.setServiciosAdicionales(servicios);
        
        borrador.setImagenesUrls(new ArrayList<>(uploadedImageUrls));
        borrador.setImagenPrincipal(uploadedImageUrls.isEmpty() ? null : uploadedImageUrls.get(0));
        
        // Idiomas requeridos
        borrador.setIdiomasRequeridos(new ArrayList<>(selectedIdiomas));
        
        // Pago al guía
        try {
            double pagoGuiaValue = Double.parseDouble(pagoGuia != null ? pagoGuia : binding.etPagoGuia.getText().toString().trim());
            borrador.setPagoGuia(pagoGuiaValue);
        } catch (NumberFormatException e) {
            borrador.setPagoGuia(0.0);
        }
        
        // Consideraciones para el guía
        borrador.setConsideraciones(consideraciones != null ? consideraciones : binding.etConsideraciones.getText().toString().trim());
        
        // Datos de la empresa
        borrador.setEmpresaId(empresaId);
        borrador.setNombreEmpresa(nombreEmpresa);
        borrador.setCorreoEmpresa(correoEmpresa);
        borrador.setCreadoPor(auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "");
        
        return borrador;
    }
    
    /**
     * Carga los datos de un borrador existente desde Firebase
     */
    private void loadBorradorData(String borradorId) {
        showProgressDialog("Cargando datos del tour...");
        
        db.collection("tours_borradores").document(borradorId).get()
            .addOnSuccessListener(documentSnapshot -> {
                dismissProgressDialog();
                
                if (!documentSnapshot.exists()) {
                    Toast.makeText(this, "No se encontró el borrador", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                try {
                    TourBorrador borrador = documentSnapshot.toObject(TourBorrador.class);
                    if (borrador == null) {
                        Toast.makeText(this, "Error al cargar datos del borrador", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    
                    // Cargar campos básicos
                    if (borrador.getTitulo() != null) {
                        binding.etTourTitle.setText(borrador.getTitulo());
                        tourTitle = borrador.getTitulo();
                    }
                    
                    // Cargar ciudad
                    if (borrador.getCiudad() != null && !borrador.getCiudad().isEmpty()) {
                        String ciudad = borrador.getCiudad();
                        tourCiudad = ciudad;
                        binding.spinnerCiudad.setText(ciudad, false);
                    }
                    
                    if (borrador.getDescripcion() != null) {
                        binding.etTourDescription.setText(borrador.getDescripcion());
                        tourDescription = borrador.getDescripcion();
                    }
                    
                    if (borrador.getPrecio() > 0) {
                        binding.etTourPrice.setText(String.valueOf(borrador.getPrecio()));
                        tourPrice = String.valueOf(borrador.getPrecio());
                    }
                    
                    if (borrador.getDuracion() != null) {
                        binding.etTourDuration.setText(borrador.getDuracion());
                        tourDuration = borrador.getDuracion();
                    }
                    
                    // Cargar fecha
                    if (borrador.getFechaRealizacion() != null) {
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            Date fecha = dateFormat.parse(borrador.getFechaRealizacion());
                            if (fecha != null) {
                                selectedCalendar.setTime(fecha);
                                binding.etTourDate.setText(borrador.getFechaRealizacion());
                                tourDate = borrador.getFechaRealizacion();
                            }
                        } catch (Exception e) {
                            Log.e("AdminCreateTour", "Error parsing fecha", e);
                        }
                    }
                    
                    // Cargar horarios
                    if (borrador.getHoraInicio() != null) {
                        binding.etTourStartTime.setText(borrador.getHoraInicio());
                        tourStartTime = borrador.getHoraInicio();
                    }
                    
                    if (borrador.getHoraFin() != null) {
                        binding.etTourEndTime.setText(borrador.getHoraFin());
                        tourEndTime = borrador.getHoraFin();
                    }
                    
                    // Cargar imágenes URLs
                    if (borrador.getImagenesUrls() != null && !borrador.getImagenesUrls().isEmpty()) {
                        uploadedImageUrls.clear();
                        uploadedImageUrls.addAll(borrador.getImagenesUrls());
                        if (binding.tvImageCount != null) {
                            binding.tvImageCount.setText(uploadedImageUrls.size() + "/3 imágenes");
                        }
                    }
                    
                    // Cargar lugares (itinerario)
                    if (borrador.getItinerario() != null && !borrador.getItinerario().isEmpty()) {
                        selectedPlaces.clear();
                        for (Map<String, Object> placeMap : borrador.getItinerario()) {
                            String nombre = (String) placeMap.get("nombre");
                            String direccion = (String) placeMap.get("direccion");
                            double lat = placeMap.get("latitud") != null ? ((Number) placeMap.get("latitud")).doubleValue() : 0.0;
                            double lng = placeMap.get("longitud") != null ? ((Number) placeMap.get("longitud")).doubleValue() : 0.0;
                            
                            TourPlace place = new TourPlace(nombre, direccion, lat, lng);
                            
                            // Cargar actividades si existen
                            if (placeMap.containsKey("actividades")) {
                                @SuppressWarnings("unchecked")
                                List<String> actividades = (List<String>) placeMap.get("actividades");
                                if (actividades != null && !actividades.isEmpty()) {
                                    // setActivities espera un String, unimos con saltos de línea
                                    place.setActivities(String.join("\n", actividades));
                                }
                            }
                            
                            selectedPlaces.add(place);
                        }
                        placeAdapter.notifyDataSetChanged();
                    }
                    
                    // Cargar servicios adicionales
                    if (borrador.getServiciosAdicionales() != null && !borrador.getServiciosAdicionales().isEmpty()) {
                        additionalServices.clear();
                        for (Map<String, Object> serviceMap : borrador.getServiciosAdicionales()) {
                            String nombre = (String) serviceMap.get("nombre");
                            boolean esPagado = serviceMap.get("esPagado") != null && (Boolean) serviceMap.get("esPagado");
                            double precio = serviceMap.get("precio") != null ? ((Number) serviceMap.get("precio")).doubleValue() : 0.0;
                            String descripcion = (String) serviceMap.get("descripcion");
                            
                            TourService service = new TourService(nombre, esPagado, precio, descripcion);
                            additionalServices.add(service);
                        }
                        serviceAdapter.notifyDataSetChanged();
                    }
                    
                    // Cargar idiomas requeridos
                    if (borrador.getIdiomasRequeridos() != null && !borrador.getIdiomasRequeridos().isEmpty()) {
                        selectedIdiomas.clear();
                        selectedIdiomas.addAll(borrador.getIdiomasRequeridos());
                        binding.etIdiomasRequeridos.setText(String.join(", ", selectedIdiomas));
                    }
                    
                    // Cargar pago al guía
                    if (borrador.getPagoGuia() > 0) {
                        binding.etPagoGuia.setText(String.valueOf(borrador.getPagoGuia()));
                        pagoGuia = String.valueOf(borrador.getPagoGuia());
                    }
                    
                    // Cargar consideraciones
                    if (borrador.getConsideraciones() != null) {
                        binding.etConsideraciones.setText(borrador.getConsideraciones());
                        consideraciones = borrador.getConsideraciones();
                    }
                    
                    Toast.makeText(this, "Datos cargados correctamente", Toast.LENGTH_SHORT).show();
                    
                } catch (Exception e) {
                    Log.e("AdminCreateTour", "Error al parsear borrador", e);
                    Toast.makeText(this, "Error al procesar datos del borrador", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                dismissProgressDialog();
                Log.e("AdminCreateTour", "Error al cargar borrador", e);
                Toast.makeText(this, "Error al cargar borrador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void finishTour() {
        if (!validateCurrentStep()) {
            return;
        }
        
        if (empresaId == null) {
            Toast.makeText(this, "Error: No se pudo obtener datos de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar que haya al menos 1 imagen
        if (selectedImageUris.isEmpty() && uploadedImageUrls.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar al menos 1 imagen del tour", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgressDialog("Publicando tour...");
        
        // Si hay nuevas imágenes, subirlas primero
        if (!selectedImageUris.isEmpty()) {
            uploadImagesAndPublish();
        } else {
            // Si no hay nuevas imágenes, publicar directamente
            publishTourOffer();
        }
    }
    
    private void uploadImagesAndPublish() {
        // Crear o usar ID de borrador
        if (currentBorradorId == null) {
            currentBorradorId = db.collection("tours_borradores").document().getId();
        }
        
        List<com.google.android.gms.tasks.Task<String>> uploadTasks = new ArrayList<>();
        
        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            int imageIndex = uploadedImageUrls.size() + i;
            
            com.google.android.gms.tasks.Task<String> uploadTask = adminTourService.subirImagenBorrador(
                imageUri, empresaId, currentBorradorId, imageIndex
            );
            uploadTasks.add(uploadTask);
        }
        
        // Esperar a que todas las imágenes se suban
        com.google.android.gms.tasks.Tasks.whenAllSuccess(uploadTasks)
            .addOnSuccessListener(urls -> {
                // Agregar las nuevas URLs a la lista
                for (Object url : urls) {
                    uploadedImageUrls.add((String) url);
                }
                
                selectedImageUris.clear();
                
                // Ahora publicar
                publishTourOffer();
            })
            .addOnFailureListener(e -> {
                dismissProgressDialog();
                Toast.makeText(this, "Error al subir imágenes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void publishTourOffer() {
        // Validar que se hayan obtenido los datos de la empresa
        if (empresaId == null || empresaId.isEmpty()) {
            dismissProgressDialog();
            Toast.makeText(this, "Error: No se pudo obtener datos de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Primero guardar como borrador si no existe
        TourBorrador borrador = createBorradorFromData();
        
        adminTourService.guardarBorrador(borrador)
            .addOnSuccessListener(borradorId -> {
                currentBorradorId = borradorId;
                
                // Validar que el borrador sea válido
                if (!borrador.esValido()) {
                    dismissProgressDialog();
                    Toast.makeText(this, "Complete todos los campos requeridos (título, descripción, precio, duración, fecha, imágenes, idiomas)", 
                        Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Ahora publicar la oferta
                adminTourService.publicarOferta(borradorId)
                    .addOnSuccessListener(ofertaId -> {
                        dismissProgressDialog();
                        Toast.makeText(this, "Tour publicado exitosamente", Toast.LENGTH_SHORT).show();
                        // --- Notificación y log Firestore (Admin → Admin) ---
                        String adminId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "adminId";
                        String notiTitulo = "Nuevo tour creado";
                        String adminNombre = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Administrador";
                        String notiDesc = "El administrador " + adminNombre + " ha creado el tour '" + borrador.getTitulo() + "'.";
                        NotificacionLogUtils.crearNotificacion(notiTitulo, notiDesc, adminId);
                        NotificacionLogUtils.crearLog("Tour creado", notiDesc);
                        // Navegar a selección de guía
                        Intent intent = new Intent(this, admin_select_guide.class);
                        intent.putExtra("ofertaId", ofertaId);
                        intent.putExtra("tourTitulo", borrador.getTitulo());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        dismissProgressDialog();
                        Toast.makeText(this, "Error al publicar tour: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                dismissProgressDialog();
                Toast.makeText(this, "Error al guardar borrador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }
    
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
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
            // Centrar en Lima con zoom más cercano (zoom 13 = vista de ciudad)
            LatLng lima = new LatLng(-12.0464, -77.0428);
            mMapPlaces.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 13));
            
            mMapPlaces.setOnMapClickListener(latLng -> {
                mMapPlaces.clear();
                mMapPlaces.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Ubicacion seleccionada"));
                selectedLocation = latLng;
                
                // Hacer zoom al punto seleccionado
                mMapPlaces.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                
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
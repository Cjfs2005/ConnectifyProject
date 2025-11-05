package com.example.connectifyproject;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.connectifyproject.utils.StorageHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class guia_editar_perfil extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageHelper storageHelper;
    private Uri selectedImageUri;

    private ImageView ivProfilePhoto;
    private MaterialButton btnSubirImagen;
    private AutoCompleteTextView spinnerTipoDocumento;
    private TextInputEditText etNumeroDocumento;
    private TextInputEditText etTelefono;
    private TextInputEditText etFechaNacimiento;
    private TextInputEditText etDomicilio;
    private TextInputEditText etCci;
    private TextInputEditText etYape;
    private MaterialButton btnGuardar;

    private Calendar calendar = Calendar.getInstance();
    
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guia_editar_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageHelper = new StorageHelper();

        initViews();
        setupToolbar();
        setupDropdown();
        setupDatePicker();
        setupImagePicker();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        btnSubirImagen = findViewById(R.id.btn_subir_imagen);
        spinnerTipoDocumento = findViewById(R.id.spinner_tipo_documento);
        etNumeroDocumento = findViewById(R.id.et_numero_documento);
        etTelefono = findViewById(R.id.et_telefono);
        etFechaNacimiento = findViewById(R.id.et_fecha_nacimiento);
        etDomicilio = findViewById(R.id.et_domicilio);
        etCci = findViewById(R.id.et_cci);
        etYape = findViewById(R.id.et_yape);
        btnGuardar = findViewById(R.id.btn_guardar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDropdown() {
        String[] tiposDocumento = {"DNI", "Pasaporte", "Carnet de Extranjeria"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                tiposDocumento
        );
        spinnerTipoDocumento.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etFechaNacimiento.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    etFechaNacimiento.setText(formattedDate);
                },
                year, month, day
        );
        
        // Establecer fecha máxima (hoy)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        // Mostrar preview de la imagen seleccionada
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(ivProfilePhoto);
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImageSelector();
                    } else {
                        Toast.makeText(this, "Se necesita permiso para seleccionar imagenes", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupClickListeners() {
        btnSubirImagen.setOnClickListener(v -> checkPermissionAndOpenImagePicker());
        ivProfilePhoto.setOnClickListener(v -> checkPermissionAndOpenImagePicker());
        btnGuardar.setOnClickListener(v -> saveProfile());
    }

    private void checkPermissionAndOpenImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openImageSelector();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openImageSelector();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImageSelector() {
        imagePickerLauncher.launch("image/*");
    }



    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipoDocumento = spinnerTipoDocumento.getText().toString().trim();
        String numeroDocumento = etNumeroDocumento.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        String domicilio = etDomicilio.getText().toString().trim();
        String cci = etCci.getText().toString().trim();
        String yape = etYape.getText().toString().trim();

        // Validaciones básicas
        if (tipoDocumento.isEmpty() || numeroDocumento.isEmpty() || telefono.isEmpty() || fechaNacimiento.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación CCI (20 dígitos)
        if (!cci.isEmpty() && cci.length() != 20) {
            Toast.makeText(this, "El CCI debe tener exactamente 20 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación YAPE (9 dígitos)
        if (!yape.isEmpty() && yape.length() != 9) {
            Toast.makeText(this, "El número de YAPE debe tener exactamente 9 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar fecha de nacimiento
        if (!isValidDate(fechaNacimiento)) {
            Toast.makeText(this, "Formato de fecha inválido. Use DD/MM/YYYY", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear mapa con los datos a actualizar
        Map<String, Object> updates = new HashMap<>();
        updates.put("tipoDocumento", tipoDocumento);
        updates.put("numeroDocumento", numeroDocumento);
        updates.put("telefono", telefono);
        updates.put("fechaNacimiento", fechaNacimiento);
        
        if (!domicilio.isEmpty()) {
            updates.put("domicilio", domicilio);
        }
        if (!cci.isEmpty()) {
            updates.put("cci", cci);
        }
        if (!yape.isEmpty()) {
            updates.put("numeroYape", yape);
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        if (selectedImageUri != null) {
            uploadPhotoAndSaveData(updates);
        } else {
            saveDataToFirestore(updates, null);
        }
    }

    private void uploadPhotoAndSaveData(Map<String, Object> updates) {
        String userId = currentUser.getUid();

        storageHelper.uploadProfilePhoto(this, selectedImageUri, userId,
                new StorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        saveDataToFirestore(updates, downloadUrl);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar");
                        Toast.makeText(guia_editar_perfil.this,
                                "Error al subir imagen: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(double progress) {
                        // Opcional: mostrar progreso
                    }
                });
    }

    private void saveDataToFirestore(Map<String, Object> updates, String photoUrl) {
        if (photoUrl != null) {
            updates.put("photoUrl", photoUrl);
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                    Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date parsedDate = sdf.parse(date);
            return parsedDate != null && !parsedDate.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private void loadUserData() {
        if (currentUser == null) return;

        db.collection("usuarios")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Cargar datos en los campos
                        String tipoDocumento = document.getString("tipoDocumento");
                        String numeroDocumento = document.getString("numeroDocumento");
                        String telefono = document.getString("telefono");
                        String fechaNacimiento = document.getString("fechaNacimiento");
                        String domicilio = document.getString("domicilio");
                        String cci = document.getString("cci");
                        String numeroYape = document.getString("numeroYape");
                        String photoUrl = document.getString("photoUrl");

                        if (tipoDocumento != null) spinnerTipoDocumento.setText(tipoDocumento, false);
                        if (numeroDocumento != null) etNumeroDocumento.setText(numeroDocumento);
                        if (telefono != null) etTelefono.setText(telefono);
                        if (fechaNacimiento != null) etFechaNacimiento.setText(fechaNacimiento);
                        if (domicilio != null) etDomicilio.setText(domicilio);
                        if (cci != null) etCci.setText(cci);
                        if (numeroYape != null) etYape.setText(numeroYape);

                        // Cargar foto de perfil
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .circleCrop()
                                    .into(ivProfilePhoto);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show();
                });
    }
}
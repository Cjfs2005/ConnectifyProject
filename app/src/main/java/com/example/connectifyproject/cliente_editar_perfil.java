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

public class cliente_editar_perfil extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageHelper storageHelper;

    private ImageView ivProfilePhoto;
    private MaterialButton btnSubirImagen;
    private AutoCompleteTextView spinnerTipoDocumento;
    private TextInputEditText etNumeroDocumento;
    private TextInputEditText etTelefono;
    private TextInputEditText etFechaNacimiento;
    private MaterialButton btnGuardar;

    private Uri selectedImageUri;
    private Calendar calendar;
    private String fechaNacimiento;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_editar_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageHelper = new StorageHelper();

        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        calendar = Calendar.getInstance();

        initializeLaunchers();
        initViews();
        setupToolbar();
        setupDropdown();
        setupDatePicker();
        setupImageUpload();
        setupSaveButton();
        loadUserData();
    }

    private void initializeLaunchers() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(ivProfilePhoto);
                        Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show();
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

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        btnSubirImagen = findViewById(R.id.btn_subir_imagen);
        spinnerTipoDocumento = findViewById(R.id.spinner_tipo_documento);
        etNumeroDocumento = findViewById(R.id.et_numero_documento);
        etTelefono = findViewById(R.id.et_telefono);
        etFechaNacimiento = findViewById(R.id.et_fecha_nacimiento);
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
        findViewById(R.id.til_fecha_nacimiento).setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    fechaNacimiento = sdf.format(calendar.getTime());
                    etFechaNacimiento.setText(fechaNacimiento);
                },
                year, month, day
        );

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void setupImageUpload() {
        btnSubirImagen.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        });
    }

    private void openImageSelector() {
        pickImageLauncher.launch("image/*");
    }

    private void setupSaveButton() {
        btnGuardar.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        db.collection("usuarios")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String tipoDocumento = documentSnapshot.getString("tipoDocumento");
                        if (tipoDocumento != null && !tipoDocumento.isEmpty()) {
                            spinnerTipoDocumento.setText(tipoDocumento, false);
                        }

                        String numeroDocumento = documentSnapshot.getString("numeroDocumento");
                        if (numeroDocumento != null && !numeroDocumento.isEmpty()) {
                            etNumeroDocumento.setText(numeroDocumento);
                        }

                        String telefono = documentSnapshot.getString("telefono");
                        if (telefono != null && !telefono.isEmpty()) {
                            etTelefono.setText(telefono);
                        }

                        String fechaNac = documentSnapshot.getString("fechaNacimiento");
                        if (fechaNac != null && !fechaNac.isEmpty()) {
                            etFechaNacimiento.setText(fechaNac);
                            fechaNacimiento = fechaNac;
                            
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date date = sdf.parse(fechaNac);
                                if (date != null) {
                                    calendar.setTime(date);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        String photoUrl = documentSnapshot.getString("photoUrl");
                        loadProfilePhoto(photoUrl);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile() {
        String tipoDoc = spinnerTipoDocumento.getText().toString().trim();
        String numeroDoc = etNumeroDocumento.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String fechaNac = etFechaNacimiento.getText().toString().trim();

        if (tipoDoc.isEmpty()) {
            Toast.makeText(this, "Seleccione un tipo de documento", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numeroDoc.isEmpty()) {
            Toast.makeText(this, "Ingrese el numero de documento", Toast.LENGTH_SHORT).show();
            etNumeroDocumento.requestFocus();
            return;
        }

        if (telefono.isEmpty()) {
            Toast.makeText(this, "Ingrese el numero de telefono", Toast.LENGTH_SHORT).show();
            etTelefono.requestFocus();
            return;
        }

        if (fechaNac.isEmpty()) {
            Toast.makeText(this, "Seleccione la fecha de nacimiento", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        if (selectedImageUri != null) {
            uploadPhotoAndSaveData(tipoDoc, numeroDoc, telefono, fechaNac);
        } else {
            saveDataToFirestore(tipoDoc, numeroDoc, telefono, fechaNac, null);
        }
    }

    private void uploadPhotoAndSaveData(String tipoDoc, String numeroDoc, String telefono, String fechaNac) {
        String userId = currentUser.getUid();

        storageHelper.uploadProfilePhoto(this, selectedImageUri, userId,
                new StorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        saveDataToFirestore(tipoDoc, numeroDoc, telefono, fechaNac, downloadUrl);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar");
                        Toast.makeText(cliente_editar_perfil.this,
                                "Error al subir imagen: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(double progress) {
                        btnGuardar.setText("Subiendo... " + (int) progress + "%");
                    }
                });
    }

    private void saveDataToFirestore(String tipoDoc, String numeroDoc, String telefono,
                                     String fechaNac, String photoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("tipoDocumento", tipoDoc);
        updates.put("numeroDocumento", numeroDoc);
        updates.put("telefono", telefono);
        updates.put("fechaNacimiento", fechaNac);

        if (photoUrl != null) {
            updates.put("photoUrl", photoUrl);
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                });
    }

    private void loadProfilePhoto(String firestorePhotoUrl) {
        String photoUrlToLoad = null;
        
        // Prioridad 1: URL de Firestore
        if (firestorePhotoUrl != null && !firestorePhotoUrl.isEmpty() && !firestorePhotoUrl.equals("null")) {
            photoUrlToLoad = firestorePhotoUrl;
        }
        // Prioridad 2: URL de FirebaseAuth como fallback
        else if (currentUser.getPhotoUrl() != null) {
            photoUrlToLoad = currentUser.getPhotoUrl().toString();
        }
        
        if (photoUrlToLoad != null && !photoUrlToLoad.isEmpty()) {
            Glide.with(this)
                    .load(photoUrlToLoad)
                    .circleCrop()
                    .placeholder(R.drawable.ic_account_circle_24)
                    .error(R.drawable.ic_account_circle_24)
                    .into(ivProfilePhoto);
        } else {
            // Cargar placeholder
            ivProfilePhoto.setImageResource(R.drawable.ic_account_circle_24);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

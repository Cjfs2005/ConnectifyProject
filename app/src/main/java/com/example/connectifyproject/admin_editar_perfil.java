package com.example.connectifyproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.example.connectifyproject.utils.AuthConstants;
import com.example.connectifyproject.utils.StorageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class admin_editar_perfil extends AppCompatActivity {

    private static final String TAG = "AdminEditarPerfil";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageHelper storageHelper;

    // Views
    private ImageView ivProfilePhoto;
    private MaterialButton btnSubirImagen;
    private TextInputEditText etNombreCompleto;
    private AutoCompleteTextView spinnerTipoDocumento;
    private TextInputEditText etNumeroDocumento;
    private TextInputEditText etEmail;
    private TextInputEditText etCci;
    private MaterialButton btnGuardar;

    // Data
    private Uri selectedImageUri;
    private String currentPhotoUrl;

    // Launchers
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_editar_perfil_view);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageHelper = new StorageHelper();

        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupDropdown();
        initializeLaunchers();
        setupImageUpload();
        setupSaveButton();
        loadUserData();
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        btnSubirImagen = findViewById(R.id.btn_subir_imagen);
        etNombreCompleto = findViewById(R.id.et_nombre_completo);
        spinnerTipoDocumento = findViewById(R.id.spinner_tipo_documento);
        etNumeroDocumento = findViewById(R.id.et_numero_documento);
        etEmail = findViewById(R.id.et_email);
        etCci = findViewById(R.id.et_cci);
        btnGuardar = findViewById(R.id.btn_guardar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Perfil");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                AuthConstants.TIPOS_DOCUMENTO
        );
        spinnerTipoDocumento.setAdapter(adapter);
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
                        Toast.makeText(this, "Se necesita permiso para seleccionar imágenes", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupImageUpload() {
        btnSubirImagen.setOnClickListener(v -> checkPermissionAndOpenImagePicker());
        ivProfilePhoto.setOnClickListener(v -> checkPermissionAndOpenImagePicker());
    }

    private void checkPermissionAndOpenImagePicker() {
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
    }

    private void openImageSelector() {
        pickImageLauncher.launch("image/*");
    }

    private void setupSaveButton() {
        btnGuardar.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreCompleto = documentSnapshot.getString(AuthConstants.FIELD_NOMBRE_COMPLETO);
                        String tipoDocumento = documentSnapshot.getString(AuthConstants.FIELD_TIPO_DOCUMENTO);
                        String numeroDocumento = documentSnapshot.getString(AuthConstants.FIELD_NUMERO_DOCUMENTO);
                        String email = documentSnapshot.getString(AuthConstants.FIELD_EMAIL);
                        String cci = documentSnapshot.getString("cci");
                        currentPhotoUrl = documentSnapshot.getString(AuthConstants.FIELD_PHOTO_URL);

                        // Llenar campos
                        if (nombreCompleto != null) etNombreCompleto.setText(nombreCompleto);
                        if (tipoDocumento != null) spinnerTipoDocumento.setText(tipoDocumento, false);
                        if (numeroDocumento != null) etNumeroDocumento.setText(numeroDocumento);
                        if (email != null) etEmail.setText(email);
                        if (cci != null) etCci.setText(cci);

                        // Cargar foto de perfil
                        loadProfilePhoto(currentPhotoUrl);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos", e);
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProfilePhoto(String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_account_circle_24)
                    .error(R.drawable.ic_account_circle_24)
                    .into(ivProfilePhoto);
        }
    }

    private void saveProfile() {
        String nombreCompleto = etNombreCompleto.getText() != null ? etNombreCompleto.getText().toString().trim() : "";
        String tipoDocumento = spinnerTipoDocumento.getText().toString().trim();
        String numeroDocumento = etNumeroDocumento.getText() != null ? etNumeroDocumento.getText().toString().trim() : "";
        String cci = etCci.getText() != null ? etCci.getText().toString().trim() : "";

        // Validaciones
        if (nombreCompleto.isEmpty()) {
            Toast.makeText(this, "Ingresa tu nombre completo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipoDocumento.isEmpty()) {
            Toast.makeText(this, "Selecciona el tipo de documento", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numeroDocumento.isEmpty()) {
            Toast.makeText(this, "Ingresa el número de documento", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cci.isEmpty()) {
            Toast.makeText(this, "Ingresa el CCI", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cci.length() != 20) {
            Toast.makeText(this, "El CCI debe tener exactamente 20 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón durante el proceso
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        if (selectedImageUri != null) {
            // Subir nueva foto primero
            uploadPhotoAndSaveData(nombreCompleto, tipoDocumento, numeroDocumento, cci);
        } else {
            // Guardar sin cambiar la foto
            saveDataToFirestore(nombreCompleto, tipoDocumento, numeroDocumento, cci, currentPhotoUrl);
        }
    }

    private void uploadPhotoAndSaveData(String nombreCompleto, String tipoDocumento, String numeroDocumento, String cci) {
        storageHelper.uploadProfilePhoto(this, selectedImageUri, currentUser.getUid(), new StorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                saveDataToFirestore(nombreCompleto, tipoDocumento, numeroDocumento, cci, downloadUrl);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al subir foto", e);
                runOnUiThread(() -> {
                    Toast.makeText(admin_editar_perfil.this, "Error al subir la foto", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                });
            }

            @Override
            public void onProgress(double progress) {
                // Opcional: mostrar progreso
            }
        });
    }

    private void saveDataToFirestore(String nombreCompleto, String tipoDocumento, String numeroDocumento, String cci, String photoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(AuthConstants.FIELD_NOMBRE_COMPLETO, nombreCompleto);
        updates.put(AuthConstants.FIELD_TIPO_DOCUMENTO, tipoDocumento);
        updates.put(AuthConstants.FIELD_NUMERO_DOCUMENTO, numeroDocumento);
        updates.put("cci", cci);
        
        if (photoUrl != null && !photoUrl.isEmpty()) {
            updates.put(AuthConstants.FIELD_PHOTO_URL, photoUrl);
        }

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Perfil actualizado exitosamente");
                    Toast.makeText(this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar perfil", e);
                    Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                });
    }
}

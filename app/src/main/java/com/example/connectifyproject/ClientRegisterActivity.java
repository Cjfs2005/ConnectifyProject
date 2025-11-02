package com.example.connectifyproject;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.utils.AuthConstants;
import com.example.connectifyproject.utils.StorageHelper;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity de registro completo para Cliente
 * Incluye: foto de perfil, datos personales, documento, teléfono internacional
 */
public class ClientRegisterActivity extends AppCompatActivity {

    private static final String TAG = "ClientRegister";
    
    private ImageView imgProfilePreview;
    private Button btnSelectPhoto;
    private TextInputEditText etNombreCompleto, etNumeroDoc, etFechaNacimiento, etEmailCliente, etDomicilio;
    private EditText etTelefono;
    private Spinner spinnerTipoDoc;
    private CountryCodePicker ccpTelefono;
    private Button btnGuardar, btnLogout;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageHelper storageHelper;
    
    private Uri selectedImageUri;
    private String fechaNacimiento = "";
    private Calendar calendar;
    
    // Launcher para seleccionar imagen
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // Mostrar preview
                    Glide.with(this)
                            .load(uri)
                            .centerCrop()
                            .into(imgProfilePreview);
                    Toast.makeText(this, "Foto seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
    );
    
    // Launcher para permisos
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permiso denegado para acceder a las imágenes", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageHelper = new StorageHelper();
        currentUser = mAuth.getCurrentUser();
        calendar = Calendar.getInstance();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupSpinners();
        setupListeners();
        preloadData();
    }

    private void initViews() {
        imgProfilePreview = findViewById(R.id.imgProfilePreview);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        etNombreCompleto = findViewById(R.id.etNombreCompleto);
        spinnerTipoDoc = findViewById(R.id.spinnerTipoDoc);
        etNumeroDoc = findViewById(R.id.etNumeroDoc);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etEmailCliente = findViewById(R.id.etEmailCliente);
        ccpTelefono = findViewById(R.id.ccpTelefono);
        etTelefono = findViewById(R.id.etTelefono);
        etDomicilio = findViewById(R.id.etDomicilio);
        btnGuardar = findViewById(R.id.btnGuardarCliente);
        btnLogout = findViewById(R.id.btnLogoutClientRegister);
    }

    private void setupSpinners() {
        // Spinner de tipo de documento
        ArrayAdapter<String> adapterTipoDoc = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                AuthConstants.TIPOS_DOCUMENTO
        );
        adapterTipoDoc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDoc.setAdapter(adapterTipoDoc);
    }

    private void setupListeners() {
        btnSelectPhoto.setOnClickListener(v -> checkPermissionAndPickImage());
        etFechaNacimiento.setOnClickListener(v -> showDatePicker());
        btnGuardar.setOnClickListener(v -> saveClientData());
        btnLogout.setOnClickListener(v -> logout());
        
        // Vincular CountryCodePicker con EditText
        ccpTelefono.registerCarrierNumberEditText(etTelefono);
    }

    /**
     * Pre-cargar datos de Firebase Auth
     */
    private void preloadData() {
        String email = currentUser.getEmail();
        String nombre = currentUser.getDisplayName();
        
        etEmailCliente.setText(email);
        
        if (nombre != null && !nombre.isEmpty()) {
            etNombreCompleto.setText(nombre);
        }
        
        // Cargar foto de perfil si existe, sino mostrar default.png
        Uri photoUrl = currentUser.getPhotoUrl();
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .centerCrop()
                    .into(imgProfilePreview);
        } else {
            // Cargar imagen por defecto desde Firebase Storage
            loadDefaultImage();
        }
    }
    
    /**
     * Cargar imagen por defecto desde Firebase Storage
     */
    private void loadDefaultImage() {
        storageHelper.getDefaultPhotoUrl(new StorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                Glide.with(ClientRegisterActivity.this)
                        .load(downloadUrl)
                        .centerCrop()
                        .into(imgProfilePreview);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al cargar imagen por defecto", e);
                // Mantener el logo de la app como fallback
            }

            @Override
            public void onProgress(double progress) {
                // No necesario para lectura
            }
        });
    }

    /**
     * Verificar permisos y abrir selector de imagen
     */
    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ usa READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 y anteriores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }

    /**
     * Mostrar DatePicker para fecha de nacimiento
     */
    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR) - 18; // Por defecto 18 años atrás
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
        
        // Limitar fecha máxima a hoy
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Guardar datos del cliente en Firestore
     */
    private void saveClientData() {
        String nombreCompleto = etNombreCompleto.getText().toString().trim();
        String tipoDoc = spinnerTipoDoc.getSelectedItem().toString();
        String numeroDoc = etNumeroDoc.getText().toString().trim();
        String fecha = etFechaNacimiento.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String codigoPais = ccpTelefono.getSelectedCountryCodeWithPlus();
        String domicilio = etDomicilio.getText().toString().trim();

        // Validaciones
        if (nombreCompleto.isEmpty()) {
            etNombreCompleto.setError("Ingresa tu nombre completo");
            etNombreCompleto.requestFocus();
            return;
        }

        if (numeroDoc.isEmpty()) {
            etNumeroDoc.setError("Ingresa tu número de documento");
            etNumeroDoc.requestFocus();
            return;
        }

        if (fecha.isEmpty()) {
            etFechaNacimiento.setError("Selecciona tu fecha de nacimiento");
            etFechaNacimiento.requestFocus();
            return;
        }

        if (telefono.isEmpty()) {
            etTelefono.setError("Ingresa tu teléfono");
            etTelefono.requestFocus();
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        // Si hay foto seleccionada, subirla primero
        if (selectedImageUri != null) {
            uploadPhotoAndSaveData(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio);
        } else {
            // Usar foto por defecto
            saveDataToFirestore(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio, null);
        }
    }

    /**
     * Subir foto a Storage y luego guardar datos
     */
    private void uploadPhotoAndSaveData(String nombreCompleto, String tipoDoc, String numeroDoc, 
                                        String fecha, String telefono, String codigoPais, String domicilio) {
        storageHelper.uploadProfilePhoto(this, selectedImageUri, currentUser.getUid(), new StorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                Log.d(TAG, "Foto subida: " + downloadUrl);
                saveDataToFirestore(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio, downloadUrl);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al subir foto", e);
                Toast.makeText(ClientRegisterActivity.this, "Error al subir la foto", Toast.LENGTH_SHORT).show();
                btnGuardar.setEnabled(true);
                btnGuardar.setText("Completar Registro");
            }

            @Override
            public void onProgress(double progress) {
                btnGuardar.setText("Subiendo foto " + (int)progress + "%");
            }
        });
    }

    /**
     * Guardar todos los datos en Firestore
     */
    private void saveDataToFirestore(String nombreCompleto, String tipoDoc, String numeroDoc, 
                                     String fecha, String telefono, String codigoPais, String domicilio, String photoUrl) {
        Map<String, Object> clienteData = new HashMap<>();
        clienteData.put(AuthConstants.FIELD_EMAIL, currentUser.getEmail());
        clienteData.put(AuthConstants.FIELD_ROL, AuthConstants.ROLE_CLIENTE);
        clienteData.put(AuthConstants.FIELD_NOMBRE_COMPLETO, nombreCompleto);
        clienteData.put(AuthConstants.FIELD_TIPO_DOCUMENTO, tipoDoc);
        clienteData.put(AuthConstants.FIELD_NUMERO_DOCUMENTO, numeroDoc);
        clienteData.put(AuthConstants.FIELD_FECHA_NACIMIENTO, fecha);
        clienteData.put(AuthConstants.FIELD_TELEFONO, telefono);
        clienteData.put(AuthConstants.FIELD_CODIGO_PAIS, codigoPais);
        clienteData.put(AuthConstants.FIELD_DOMICILIO, domicilio.isEmpty() ? "" : domicilio);
        clienteData.put(AuthConstants.FIELD_UID, currentUser.getUid());
        clienteData.put(AuthConstants.FIELD_HABILITADO, true); // Cliente habilitado automáticamente
        
        // Usar foto subida o la de Google Auth
        if (photoUrl != null) {
            clienteData.put(AuthConstants.FIELD_PHOTO_URL, photoUrl);
        } else if (currentUser.getPhotoUrl() != null) {
            clienteData.put(AuthConstants.FIELD_PHOTO_URL, currentUser.getPhotoUrl().toString());
        } else {
            clienteData.put(AuthConstants.FIELD_PHOTO_URL, ""); // Firebase Storage añadirá default.png
        }

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(currentUser.getUid())
                .set(clienteData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cliente guardado exitosamente");
                    Toast.makeText(this, "¡Registro completado exitosamente!", Toast.LENGTH_SHORT).show();
                    redirectToClientDashboard();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar cliente", e);
                    Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Completar Registro");
                });
    }

    /**
     * Cerrar sesión
     */
    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> redirectToLogin());
    }

    /**
     * Redirigir al dashboard de cliente
     */
    private void redirectToClientDashboard() {
        Intent intent = new Intent(this, cliente_inicio.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Redirigir al login
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // No permitir volver atrás durante registro
        super.onBackPressed();
    }
}

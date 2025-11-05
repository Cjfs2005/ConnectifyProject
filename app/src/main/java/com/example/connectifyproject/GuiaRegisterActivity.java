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
import android.widget.CheckBox;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity de registro completo para Guía Turístico
 * Incluye: foto, datos personales, documento, teléfono, idiomas
 */
public class GuiaRegisterActivity extends AppCompatActivity {

    private static final String TAG = "GuiaRegister";
    
    private ImageView imgProfilePreview;
    private Button btnSelectPhoto;
    private TextInputEditText etNombreCompleto, etNumeroDoc, etFechaNacimiento, etEmailGuia, etDomicilio, etCci, etYape;
    private EditText etTelefono;
    private Spinner spinnerTipoDoc;
    private CountryCodePicker ccpTelefono;
    private CheckBox cbEspanol, cbIngles, cbFrances, cbAleman, cbItaliano, cbChino, cbJapones;
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
        setContentView(R.layout.activity_guia_register);

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
        imgProfilePreview = findViewById(R.id.imgProfilePreviewGuia);
        btnSelectPhoto = findViewById(R.id.btnSelectPhotoGuia);
        etNombreCompleto = findViewById(R.id.etNombreCompletoGuia);
        spinnerTipoDoc = findViewById(R.id.spinnerTipoDocGuia);
        etNumeroDoc = findViewById(R.id.etNumeroDocGuia);
        etFechaNacimiento = findViewById(R.id.etFechaNacimientoGuia);
        etEmailGuia = findViewById(R.id.etEmailGuia);
        ccpTelefono = findViewById(R.id.ccpTelefonoGuia);
        etTelefono = findViewById(R.id.etTelefonoGuia);
        etDomicilio = findViewById(R.id.etDomicilioGuia);
        etCci = findViewById(R.id.etCciGuia);
        etYape = findViewById(R.id.etYapeGuia);
        
        // Checkboxes de idiomas
        cbEspanol = findViewById(R.id.cbEspanol);
        cbIngles = findViewById(R.id.cbIngles);
        cbFrances = findViewById(R.id.cbFrances);
        cbAleman = findViewById(R.id.cbAleman);
        cbItaliano = findViewById(R.id.cbItaliano);
        cbChino = findViewById(R.id.cbChino);
        cbJapones = findViewById(R.id.cbJapones);
        
        btnGuardar = findViewById(R.id.btnGuardarGuia);
        btnLogout = findViewById(R.id.btnLogoutGuiaRegister);
    }

    private void setupSpinners() {
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
        btnGuardar.setOnClickListener(v -> saveGuiaData());
        btnLogout.setOnClickListener(v -> logout());
        ccpTelefono.registerCarrierNumberEditText(etTelefono);
    }

    private void preloadData() {
        String email = currentUser.getEmail();
        String nombre = currentUser.getDisplayName();
        
        etEmailGuia.setText(email);
        
        if (nombre != null && !nombre.isEmpty()) {
            etNombreCompleto.setText(nombre);
        }
        
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
                Glide.with(GuiaRegisterActivity.this)
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

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
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

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR) - 18;
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
        
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Obtener idiomas seleccionados
     */
    private List<String> getSelectedIdiomas() {
        List<String> idiomas = new ArrayList<>();
        if (cbEspanol.isChecked()) idiomas.add("Español");
        if (cbIngles.isChecked()) idiomas.add("Inglés");
        if (cbFrances.isChecked()) idiomas.add("Francés");
        if (cbAleman.isChecked()) idiomas.add("Alemán");
        if (cbItaliano.isChecked()) idiomas.add("Italiano");
        if (cbChino.isChecked()) idiomas.add("Chino");
        if (cbJapones.isChecked()) idiomas.add("Japonés");
        return idiomas;
    }

    private void saveGuiaData() {
        String nombreCompleto = etNombreCompleto.getText().toString().trim();
        String tipoDoc = spinnerTipoDoc.getSelectedItem().toString();
        String numeroDoc = etNumeroDoc.getText().toString().trim();
        String fecha = etFechaNacimiento.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String codigoPais = ccpTelefono.getSelectedCountryCodeWithPlus();
        String domicilio = etDomicilio.getText().toString().trim();
        String cci = etCci.getText().toString().trim();
        String yape = etYape.getText().toString().trim();
        List<String> idiomas = getSelectedIdiomas();

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

        if (cci.isEmpty()) {
            etCci.setError("Ingresa tu CCI (Código de Cuenta Interbancario)");
            etCci.requestFocus();
            return;
        }

        if (cci.length() != 20) {
            etCci.setError("El CCI debe tener exactamente 20 dígitos");
            etCci.requestFocus();
            return;
        }

        if (yape.isEmpty()) {
            etYape.setError("Ingresa tu número de YAPE");
            etYape.requestFocus();
            return;
        }

        if (yape.length() != 9) {
            etYape.setError("El número de YAPE debe tener exactamente 9 dígitos");
            etYape.requestFocus();
            return;
        }

        if (idiomas.size() < 2) {
            Toast.makeText(this, "Selecciona al menos 2 idiomas (Español + otro)", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        if (selectedImageUri != null) {
            uploadPhotoAndSaveData(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio, cci, yape, idiomas);
        } else {
            saveDataToFirestore(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio, cci, yape, idiomas, null);
        }
    }

    private void uploadPhotoAndSaveData(String nombreCompleto, String tipoDoc, String numeroDoc, 
                                        String fecha, String telefono, String codigoPais, String domicilio, 
                                        String cci, String yape, List<String> idiomas) {
        storageHelper.uploadProfilePhoto(this, selectedImageUri, currentUser.getUid(), new StorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                Log.d(TAG, "Foto subida: " + downloadUrl);
                saveDataToFirestore(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio, cci, yape, idiomas, downloadUrl);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al subir foto", e);
                Toast.makeText(GuiaRegisterActivity.this, "Error al subir la foto", Toast.LENGTH_SHORT).show();
                btnGuardar.setEnabled(true);
                btnGuardar.setText("Completar Registro");
            }

            @Override
            public void onProgress(double progress) {
                btnGuardar.setText("Subiendo foto " + (int)progress + "%");
            }
        });
    }

    private void saveDataToFirestore(String nombreCompleto, String tipoDoc, String numeroDoc, 
                                     String fecha, String telefono, String codigoPais, String domicilio, 
                                     String cci, String yape, List<String> idiomas, String photoUrl) {
        // Usar foto subida, de Google Auth o default
        if (photoUrl != null) {
            // Foto subida por el usuario
            saveFinalDataToFirestore(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio, cci, yape, idiomas, photoUrl);
        } else if (currentUser.getPhotoUrl() != null) {
            // Foto de Google Auth
            saveFinalDataToFirestore(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio, cci, yape, idiomas, currentUser.getPhotoUrl().toString());
        } else {
            // Obtener URL de foto por defecto desde Firebase Storage
            storageHelper.getDefaultPhotoUrl(new StorageHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    saveFinalDataToFirestore(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio, cci, yape, idiomas, downloadUrl);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error al obtener foto por defecto, usando gs:// URI", e);
                    // Fallback: usar el gs:// URI directamente
                    saveFinalDataToFirestore(nombreCompleto, tipoDoc, numeroDoc, fecha, telefono, codigoPais, domicilio, cci, yape, idiomas, AuthConstants.DEFAULT_PHOTO_URL);
                }

                @Override
                public void onProgress(double progress) {
                    // No aplica para obtener URL
                }
            });
        }
    }
    
    /**
     * Guarda los datos finales en Firestore con la URL de foto resuelta
     */
    private void saveFinalDataToFirestore(String nombreCompleto, String tipoDoc, String numeroDoc, 
                                          String fecha, String telefono, String codigoPais, String domicilio, 
                                          String cci, String yape, List<String> idiomas, String resolvedPhotoUrl) {
        Map<String, Object> guiaData = new HashMap<>();
        guiaData.put(AuthConstants.FIELD_EMAIL, currentUser.getEmail());
        guiaData.put(AuthConstants.FIELD_ROL, AuthConstants.ROLE_GUIA);
        guiaData.put(AuthConstants.FIELD_NOMBRE_COMPLETO, nombreCompleto);
        guiaData.put(AuthConstants.FIELD_TIPO_DOCUMENTO, tipoDoc);
        guiaData.put(AuthConstants.FIELD_NUMERO_DOCUMENTO, numeroDoc);
        guiaData.put(AuthConstants.FIELD_FECHA_NACIMIENTO, fecha);
        guiaData.put(AuthConstants.FIELD_TELEFONO, telefono);
        guiaData.put(AuthConstants.FIELD_CODIGO_PAIS, codigoPais);
        guiaData.put(AuthConstants.FIELD_DOMICILIO, domicilio.isEmpty() ? "" : domicilio);
        guiaData.put("cci", cci.isEmpty() ? "" : cci);
        guiaData.put("numeroYape", yape.isEmpty() ? "" : yape);
        guiaData.put(AuthConstants.FIELD_UID, currentUser.getUid());
        guiaData.put(AuthConstants.FIELD_HABILITADO, false); // Guía NO habilitado hasta aprobación del admin
        guiaData.put(AuthConstants.FIELD_IDIOMAS, idiomas);
        guiaData.put(AuthConstants.FIELD_FECHA_CREACION, com.google.firebase.Timestamp.now()); // Timestamp de creación
        guiaData.put(AuthConstants.FIELD_PERFIL_COMPLETO, true); // Perfil completado
        guiaData.put(AuthConstants.FIELD_PHOTO_URL, resolvedPhotoUrl); // Foto resuelta

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(currentUser.getUid())
                .set(guiaData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Guía guardado exitosamente con foto: " + resolvedPhotoUrl);
                    Toast.makeText(this, "Registro completado. Espera aprobación del administrador", Toast.LENGTH_LONG).show();
                    // NO redirigir al dashboard, mostrar mensaje de espera
                    showPendingApprovalMessage();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar guía", e);
                    Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Completar Registro");
                });
    }

    /**
     * Mostrar mensaje de cuenta pendiente de aprobación
     */
    private void showPendingApprovalMessage() {
        Toast.makeText(this, "Tu cuenta está pendiente de aprobación", Toast.LENGTH_LONG).show();
        // Cerrar sesión automáticamente ya que no está habilitado
        logout();
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> redirectToLogin());
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

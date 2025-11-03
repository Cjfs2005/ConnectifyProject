package com.example.connectifyproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.adapters.PromotionalPhotosAdapter;
import com.example.connectifyproject.utils.AuthConstants;
import com.example.connectifyproject.utils.StorageHelper;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminRegisterActivity extends AppCompatActivity implements PromotionalPhotosAdapter.OnPhotoRemovedListener {

    private static final String TAG = "AdminRegisterActivity";
    private static final int MIN_PROMOTIONAL_PHOTOS = 2;

    // Views
    private ImageView ivProfilePhoto;
    private TextInputEditText etNombreCompleto, etNumeroDoc, etEmail, etNombreEmpresa;
    private TextInputEditText etDescripcionEmpresa, etUbicacionEmpresa, etCorreoEmpresa, etTelefonoEmpresa;
    private Spinner spinnerTipoDoc;
    private MaterialButton btnSelectPhoto, btnSelectPromotionalPhotos, btnGuardar, btnLogout;
    private RecyclerView rvPromotionalPhotos;
    private TextView tvPhotoCount;

    // Data
    private Uri profilePhotoUri;
    private PromotionalPhotosAdapter promotionalPhotosAdapter;
    
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageHelper storageHelper;

    // Activity Result Launchers
    private ActivityResultLauncher<String> pickProfilePhoto;
    private ActivityResultLauncher<String> pickPromotionalPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        // Ocultar action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageHelper = new StorageHelper();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Inicializar vistas
        initViews();
        
        // Configurar pickers
        setupPhotoPickers();
        
        // Configurar adapter de fotos promocionales
        setupPromotionalPhotosAdapter();
        
        // Configurar tipo de documento dropdown
        setupDocTypeDropdown();
        
        // Cargar datos pre-registrados
        loadPreRegisteredData();
        
        // Configurar botón guardar
        btnGuardar.setOnClickListener(v -> validateAndSave());
        
        // Configurar botón logout
        btnLogout.setOnClickListener(v -> logout());
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        etNombreCompleto = findViewById(R.id.etNombreCompleto);
        spinnerTipoDoc = findViewById(R.id.spinnerTipoDoc);
        etNumeroDoc = findViewById(R.id.etNumeroDoc);
        etEmail = findViewById(R.id.etEmail);
        etNombreEmpresa = findViewById(R.id.etNombreEmpresa);
        etDescripcionEmpresa = findViewById(R.id.etDescripcionEmpresa);
        etUbicacionEmpresa = findViewById(R.id.etUbicacionEmpresa);
        etCorreoEmpresa = findViewById(R.id.etCorreoEmpresa);
        etTelefonoEmpresa = findViewById(R.id.etTelefonoEmpresa);
        btnSelectPromotionalPhotos = findViewById(R.id.btnSelectPromotionalPhotos);
        rvPromotionalPhotos = findViewById(R.id.rvPromotionalPhotos);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupPhotoPickers() {
        // Picker para foto de perfil
        pickProfilePhoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profilePhotoUri = uri;
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(ivProfilePhoto);
                    }
                }
        );

        btnSelectPhoto.setOnClickListener(v -> pickProfilePhoto.launch("image/*"));

        // Picker para fotos promocionales (múltiples)
        pickPromotionalPhotos = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            promotionalPhotosAdapter.addPhoto(uri);
                        }
                        updatePhotoCount();
                        rvPromotionalPhotos.setVisibility(View.VISIBLE);
                    }
                }
        );

        btnSelectPromotionalPhotos.setOnClickListener(v -> pickPromotionalPhotos.launch("image/*"));
    }

    private void setupPromotionalPhotosAdapter() {
        promotionalPhotosAdapter = new PromotionalPhotosAdapter(this);
        rvPromotionalPhotos.setLayoutManager(new GridLayoutManager(this, 3));
        rvPromotionalPhotos.setAdapter(promotionalPhotosAdapter);
    }

    private void setupDocTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                AuthConstants.TIPOS_DOCUMENTO
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDoc.setAdapter(adapter);
    }

    private void loadPreRegisteredData() {
        String uid = currentUser.getUid();
        
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String email = document.getString(AuthConstants.FIELD_EMAIL);
                        String nombreEmpresa = document.getString(AuthConstants.FIELD_NOMBRE_EMPRESA);
                        
                        if (email != null) {
                            etEmail.setText(email);
                        }
                        if (nombreEmpresa != null) {
                            etNombreEmpresa.setText(nombreEmpresa);
                        }

                        // Cargar foto de perfil si existe, sino mostrar default.png
                        Uri photoUrl = currentUser.getPhotoUrl();
                        if (photoUrl != null) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .circleCrop()
                                    .into(ivProfilePhoto);
                        } else {
                            // Cargar imagen por defecto desde Firebase Storage
                            loadDefaultImage();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos pre-registrados", e);
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Cargar imagen por defecto desde Firebase Storage
     */
    private void loadDefaultImage() {
        storageHelper.getDefaultPhotoUrl(new StorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                Glide.with(AdminRegisterActivity.this)
                        .load(downloadUrl)
                        .circleCrop()
                        .into(ivProfilePhoto);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al cargar imagen por defecto", e);
                // Mantener el logo de la app como fallback
            }

            @Override
            public void onProgress(double progress) {
                // No es necesario mostrar progreso para imagen por defecto
            }
        });
    }

    private void validateAndSave() {
        String nombreCompleto = etNombreCompleto.getText() != null ? etNombreCompleto.getText().toString().trim() : "";
        String tipoDoc = spinnerTipoDoc.getSelectedItem() != null ? spinnerTipoDoc.getSelectedItem().toString() : "";
        String numeroDoc = etNumeroDoc.getText() != null ? etNumeroDoc.getText().toString().trim() : "";
        String nombreEmpresa = etNombreEmpresa.getText() != null ? etNombreEmpresa.getText().toString().trim() : "";
        String descripcion = etDescripcionEmpresa.getText() != null ? etDescripcionEmpresa.getText().toString().trim() : "";
        String ubicacion = etUbicacionEmpresa.getText() != null ? etUbicacionEmpresa.getText().toString().trim() : "";
        String correoEmpresa = etCorreoEmpresa.getText() != null ? etCorreoEmpresa.getText().toString().trim() : "";
        String telefonoEmpresa = etTelefonoEmpresa.getText() != null ? etTelefonoEmpresa.getText().toString().trim() : "";

        // Validaciones
        if (TextUtils.isEmpty(nombreCompleto)) {
            Toast.makeText(this, "Ingresa tu nombre completo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(tipoDoc)) {
            Toast.makeText(this, "Selecciona el tipo de documento", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(numeroDoc)) {
            Toast.makeText(this, "Ingresa el número de documento", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nombreEmpresa)) {
            Toast.makeText(this, "Ingresa el nombre de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(descripcion)) {
            Toast.makeText(this, "Ingresa la descripción de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(ubicacion)) {
            Toast.makeText(this, "Ingresa la ubicación de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(correoEmpresa) || !Patterns.EMAIL_ADDRESS.matcher(correoEmpresa).matches()) {
            Toast.makeText(this, "Ingresa un correo válido de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(telefonoEmpresa)) {
            Toast.makeText(this, "Ingresa el teléfono de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (promotionalPhotosAdapter.getPhotoCount() < MIN_PROMOTIONAL_PHOTOS) {
            Toast.makeText(this, "Debes seleccionar al menos " + MIN_PROMOTIONAL_PHOTOS + " fotos promocionales", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón durante el proceso
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        // Subir fotos primero
        uploadPhotos(nombreCompleto, tipoDoc, numeroDoc, nombreEmpresa, descripcion, ubicacion, correoEmpresa, telefonoEmpresa);
    }

    private void uploadPhotos(String nombreCompleto, String tipoDoc, String numeroDoc, String nombreEmpresa,
                              String descripcion, String ubicacion, String correoEmpresa, String telefonoEmpresa) {
        String uid = currentUser.getUid();
        List<Uri> promotionalPhotos = promotionalPhotosAdapter.getPhotos();
        int totalPhotos = promotionalPhotos.size() + (profilePhotoUri != null ? 1 : 0);
        
        Log.d(TAG, "=== INICIO UPLOAD PHOTOS ===");
        Log.d(TAG, "Total fotos promocionales: " + promotionalPhotos.size());
        Log.d(TAG, "Tiene foto de perfil: " + (profilePhotoUri != null));
        Log.d(TAG, "Total fotos a subir: " + totalPhotos);
        
        // Usar array para mantener el orden de las fotos promocionales
        final String[] promotionalPhotoUrls = new String[promotionalPhotos.size()];
        AtomicInteger uploadCount = new AtomicInteger(0);
        final boolean[] saveToFirestoreCalled = new boolean[]{false}; // Flag para evitar llamadas múltiples
        
        // Variable para guardar la URL de la foto de perfil
        final String[] profilePhotoUrl = new String[1];

        // Callback para cuando todas las fotos estén subidas
        Runnable onAllPhotosUploaded = () -> {
            synchronized (promotionalPhotoUrls) {
                // Verificar que no se haya llamado ya
                if (saveToFirestoreCalled[0]) {
                    Log.d(TAG, "saveToFirestore ya fue llamado, ignorando callback adicional");
                    return;
                }
                
                int currentCount = uploadCount.get();
                Log.d(TAG, "onAllPhotosUploaded ejecutado - Count actual: " + currentCount + " / Total esperado: " + totalPhotos);
                
                if (currentCount == totalPhotos) {
                    // Verificar que todas las URLs promocionales estén presentes
                    boolean allUrlsPresent = true;
                    for (int idx = 0; idx < promotionalPhotoUrls.length; idx++) {
                        if (promotionalPhotoUrls[idx] == null || promotionalPhotoUrls[idx].isEmpty()) {
                            Log.w(TAG, "⚠️ URL de foto promocional [" + idx + "] aún no está disponible");
                            allUrlsPresent = false;
                            break;
                        }
                    }
                    
                    // Si la foto de perfil es requerida, verificar que también esté presente
                    if (allUrlsPresent && profilePhotoUri != null && profilePhotoUrl[0] == null) {
                        Log.w(TAG, "⚠️ URL de foto de perfil aún no está disponible");
                        allUrlsPresent = false;
                    }
                    
                    if (!allUrlsPresent) {
                        Log.d(TAG, "Esperando que todas las URLs estén disponibles...");
                        return; // Salir y esperar el siguiente callback
                    }
                    
                    // Marcar como llamado
                    saveToFirestoreCalled[0] = true;
                    
                    // Convertir array a lista, filtrando nulls por si acaso
                    List<String> photosList = new ArrayList<>();
                    for (int idx = 0; idx < promotionalPhotoUrls.length; idx++) {
                        String url = promotionalPhotoUrls[idx];
                        Log.d(TAG, "Foto promocional [" + idx + "]: " + (url != null ? url : "NULL"));
                        if (url != null && !url.isEmpty()) {
                            photosList.add(url);
                        }
                    }
                    Log.d(TAG, "Total URLs en photosList: " + photosList.size());
                    Log.d(TAG, "=== FIN UPLOAD - Llamando saveToFirestore ===");
                    
                    saveToFirestore(nombreCompleto, tipoDoc, numeroDoc, nombreEmpresa, descripcion, ubicacion,
                            correoEmpresa, telefonoEmpresa, photosList, profilePhotoUrl[0]);
                }
            }
        };

        // Subir fotos promocionales
        for (int i = 0; i < promotionalPhotos.size(); i++) {
            Uri photoUri = promotionalPhotos.get(i);
            final int photoIndex = i; // Importante: final para usar en lambda
            Log.d(TAG, "Iniciando subida de foto promocional [" + photoIndex + "]");
            
            storageHelper.uploadCompanyPhoto(this, photoUri, uid, photoIndex, new StorageHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    Log.d(TAG, "✓ Foto promocional [" + photoIndex + "] subida exitosamente: " + downloadUrl);
                    // Usar synchronized para garantizar atomicidad
                    synchronized (promotionalPhotoUrls) {
                        promotionalPhotoUrls[photoIndex] = downloadUrl;
                        int count = uploadCount.incrementAndGet();
                        Log.d(TAG, "Upload count incrementado a: " + count);
                    }
                    onAllPhotosUploaded.run();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "✗ Error al subir foto promocional [" + photoIndex + "]", e);
                    runOnUiThread(() -> {
                        Toast.makeText(AdminRegisterActivity.this, "Error al subir fotos", Toast.LENGTH_SHORT).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Completar Registro");
                    });
                }

                @Override
                public void onProgress(double progress) {
                    // Opcional: mostrar progreso
                }
            });
        }

        // Subir foto de perfil o usar default
        if (profilePhotoUri != null) {
            storageHelper.uploadProfilePhoto(this, profilePhotoUri, uid, new StorageHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    synchronized (promotionalPhotoUrls) {
                        profilePhotoUrl[0] = downloadUrl;
                        uploadCount.incrementAndGet();
                    }
                    onAllPhotosUploaded.run();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error al subir foto de perfil, usando default", e);
                    // Usar foto de Google Auth o default
                    synchronized (promotionalPhotoUrls) {
                        if (currentUser.getPhotoUrl() != null) {
                            profilePhotoUrl[0] = currentUser.getPhotoUrl().toString();
                        }
                        uploadCount.incrementAndGet();
                    }
                    onAllPhotosUploaded.run();
                }

                @Override
                public void onProgress(double progress) {
                    // Opcional
                }
            });
        } else {
            // Usar foto de Google Auth o default
            if (currentUser.getPhotoUrl() != null) {
                synchronized (promotionalPhotoUrls) {
                    profilePhotoUrl[0] = currentUser.getPhotoUrl().toString();
                    uploadCount.incrementAndGet();
                }
                onAllPhotosUploaded.run();
            } else {
                // Obtener URL de foto por defecto
                storageHelper.getDefaultPhotoUrl(new StorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        synchronized (promotionalPhotoUrls) {
                            profilePhotoUrl[0] = downloadUrl;
                            uploadCount.incrementAndGet();
                        }
                        onAllPhotosUploaded.run();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Error al obtener foto default", e);
                        synchronized (promotionalPhotoUrls) {
                            uploadCount.incrementAndGet();
                        }
                        onAllPhotosUploaded.run();
                    }

                    @Override
                    public void onProgress(double progress) {}
                });
            }
        }
    }

    private void saveToFirestore(String nombreCompleto, String tipoDoc, String numeroDoc, String nombreEmpresa,
                                 String descripcion, String ubicacion, String correoEmpresa,
                                 String telefonoEmpresa, List<String> fotosEmpresa, String photoUrl) {
        String uid = currentUser.getUid();

        Log.d(TAG, "=== GUARDANDO EN FIRESTORE ===");
        Log.d(TAG, "UID: " + uid);
        Log.d(TAG, "Nombre Empresa: " + nombreEmpresa);
        Log.d(TAG, "Fotos empresa recibidas: " + (fotosEmpresa != null ? fotosEmpresa.size() : "NULL"));
        if (fotosEmpresa != null) {
            for (int i = 0; i < fotosEmpresa.size(); i++) {
                Log.d(TAG, "  Foto [" + i + "]: " + fotosEmpresa.get(i));
            }
        }

        Map<String, Object> adminData = new HashMap<>();
        adminData.put(AuthConstants.FIELD_EMAIL, currentUser.getEmail());
        adminData.put(AuthConstants.FIELD_ROL, AuthConstants.ROLE_ADMIN);
        adminData.put(AuthConstants.FIELD_NOMBRE_COMPLETO, nombreCompleto);
        adminData.put(AuthConstants.FIELD_TIPO_DOCUMENTO, tipoDoc);
        adminData.put(AuthConstants.FIELD_NUMERO_DOCUMENTO, numeroDoc);
        adminData.put(AuthConstants.FIELD_UID, uid);
        adminData.put(AuthConstants.FIELD_HABILITADO, true); // Admin habilitado al completar registro
        adminData.put(AuthConstants.FIELD_PERFIL_COMPLETO, true);
        adminData.put(AuthConstants.FIELD_FECHA_CREACION, com.google.firebase.Timestamp.now());
        
        // Foto de perfil
        adminData.put(AuthConstants.FIELD_PHOTO_URL, photoUrl);
        
        // Campos de empresa
        adminData.put(AuthConstants.FIELD_NOMBRE_EMPRESA, nombreEmpresa);
        adminData.put(AuthConstants.FIELD_DESCRIPCION_EMPRESA, descripcion);
        adminData.put(AuthConstants.FIELD_UBICACION_EMPRESA, ubicacion);
        adminData.put(AuthConstants.FIELD_CORREO_EMPRESA, correoEmpresa);
        adminData.put(AuthConstants.FIELD_TELEFONO_EMPRESA, telefonoEmpresa);
        adminData.put(AuthConstants.FIELD_FOTOS_EMPRESA, fotosEmpresa);
        
        Log.d(TAG, "Fotos empresa en adminData: " + adminData.get(AuthConstants.FIELD_FOTOS_EMPRESA));
        
        // Campos de reseñas
        adminData.put(AuthConstants.FIELD_SUMA_RESENIAS, 0);
        adminData.put(AuthConstants.FIELD_NUMERO_RESENIAS, 0);

        // Guardar en Firestore
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(uid)
                .set(adminData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✓ Administrador guardado exitosamente en Firestore");
                    Log.d(TAG, "=== PROCESO COMPLETADO ===");
                    Toast.makeText(this, "¡Registro completado exitosamente!", Toast.LENGTH_SHORT).show();
                    // Redirigir al dashboard de admin (por ahora a cliente_inicio)
                    redirectToAdminDashboard();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗ Error al guardar administrador en Firestore", e);
                    Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Completar Registro");
                });
    }

    private void updatePhotoCount() {
        int count = promotionalPhotosAdapter.getPhotoCount();
        String text = count + " fotos seleccionadas (mínimo " + MIN_PROMOTIONAL_PHOTOS + ")";
        tvPhotoCount.setText(text);
    }

    @Override
    public void onPhotoRemoved(int position) {
        promotionalPhotosAdapter.removePhoto(position);
        updatePhotoCount();
        if (promotionalPhotosAdapter.getPhotoCount() == 0) {
            rvPromotionalPhotos.setVisibility(View.GONE);
        }
    }

    private void redirectToAdminDashboard() {
        Intent intent = new Intent(this, admin_dashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> redirectToLogin());
    }
}

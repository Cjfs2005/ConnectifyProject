package com.example.connectifyproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.PromotionalPhotosAdapter;
import com.example.connectifyproject.utils.AuthConstants;
import com.example.connectifyproject.utils.StorageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class admin_editar_empresa extends AppCompatActivity implements PromotionalPhotosAdapter.OnPhotoRemovedListener {

    private static final String TAG = "AdminEditarEmpresa";
    private static final int MIN_PROMOTIONAL_PHOTOS = 2;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageHelper storageHelper;

    // Views
    private TextInputEditText etNombreEmpresa;
    private TextInputEditText etDescripcionEmpresa;
    private TextInputEditText etUbicacionEmpresa;
    private TextInputEditText etCorreoEmpresa;
    private TextInputEditText etTelefonoEmpresa;
    private MaterialButton btnSelectPromotionalPhotos;
    private RecyclerView rvPromotionalPhotos;
    private TextView tvPhotoCount;
    private MaterialButton btnGuardar;

    // Data
    private PromotionalPhotosAdapter promotionalPhotosAdapter;
    private List<String> currentPhotoUrls = new ArrayList<>();
    private List<Uri> newPhotosToUpload = new ArrayList<>();
    private List<String> photosToDelete = new ArrayList<>();

    // Launchers
    private ActivityResultLauncher<String> pickPromotionalPhotos;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_editar_empresa_view);

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
        setupPromotionalPhotosAdapter();
        initializeLaunchers();
        setupClickListeners();
        loadCompanyData();
    }

    private void initViews() {
        etNombreEmpresa = findViewById(R.id.et_nombre_empresa);
        etDescripcionEmpresa = findViewById(R.id.et_descripcion_empresa);
        etUbicacionEmpresa = findViewById(R.id.et_ubicacion_empresa);
        etCorreoEmpresa = findViewById(R.id.et_correo_empresa);
        etTelefonoEmpresa = findViewById(R.id.et_telefono_empresa);
        btnSelectPromotionalPhotos = findViewById(R.id.btn_select_promotional_photos);
        rvPromotionalPhotos = findViewById(R.id.rv_promotional_photos);
        tvPhotoCount = findViewById(R.id.tv_photo_count);
        btnGuardar = findViewById(R.id.btn_guardar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Empresa");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupPromotionalPhotosAdapter() {
        promotionalPhotosAdapter = new PromotionalPhotosAdapter(this);
        rvPromotionalPhotos.setLayoutManager(new GridLayoutManager(this, 3));
        rvPromotionalPhotos.setAdapter(promotionalPhotosAdapter);
    }

    private void initializeLaunchers() {
        pickPromotionalPhotos = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            promotionalPhotosAdapter.addPhoto(uri);
                            newPhotosToUpload.add(uri);
                        }
                        updatePhotoCount();
                        rvPromotionalPhotos.setVisibility(View.VISIBLE);
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

    private void setupClickListeners() {
        btnSelectPromotionalPhotos.setOnClickListener(v -> checkPermissionAndOpenImagePicker());
        btnGuardar.setOnClickListener(v -> saveCompanyData());
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
        pickPromotionalPhotos.launch("image/*");
    }

    private void loadCompanyData() {
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreEmpresa = documentSnapshot.getString(AuthConstants.FIELD_NOMBRE_EMPRESA);
                        String descripcion = documentSnapshot.getString(AuthConstants.FIELD_DESCRIPCION_EMPRESA);
                        String ubicacion = documentSnapshot.getString(AuthConstants.FIELD_UBICACION_EMPRESA);
                        String correo = documentSnapshot.getString(AuthConstants.FIELD_CORREO_EMPRESA);
                        String telefono = documentSnapshot.getString(AuthConstants.FIELD_TELEFONO_EMPRESA);
                        List<String> fotosEmpresa = (List<String>) documentSnapshot.get(AuthConstants.FIELD_FOTOS_EMPRESA);

                        // Llenar campos
                        if (nombreEmpresa != null) etNombreEmpresa.setText(nombreEmpresa);
                        if (descripcion != null) etDescripcionEmpresa.setText(descripcion);
                        if (ubicacion != null) etUbicacionEmpresa.setText(ubicacion);
                        if (correo != null) etCorreoEmpresa.setText(correo);
                        if (telefono != null) etTelefonoEmpresa.setText(telefono);

                        // Cargar fotos existentes
                        if (fotosEmpresa != null && !fotosEmpresa.isEmpty()) {
                            currentPhotoUrls.addAll(fotosEmpresa);
                            for (String photoUrl : fotosEmpresa) {
                                promotionalPhotosAdapter.addPhotoUrl(photoUrl);
                            }
                            updatePhotoCount();
                            rvPromotionalPhotos.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos", e);
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveCompanyData() {
        String nombreEmpresa = etNombreEmpresa.getText() != null ? etNombreEmpresa.getText().toString().trim() : "";
        String descripcion = etDescripcionEmpresa.getText() != null ? etDescripcionEmpresa.getText().toString().trim() : "";
        String ubicacion = etUbicacionEmpresa.getText() != null ? etUbicacionEmpresa.getText().toString().trim() : "";
        String correo = etCorreoEmpresa.getText() != null ? etCorreoEmpresa.getText().toString().trim() : "";
        String telefono = etTelefonoEmpresa.getText() != null ? etTelefonoEmpresa.getText().toString().trim() : "";

        // Validaciones
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

        if (TextUtils.isEmpty(correo) || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingresa un correo válido de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(telefono)) {
            Toast.makeText(this, "Ingresa el teléfono de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalPhotos = promotionalPhotosAdapter.getPhotoCount();
        if (totalPhotos < MIN_PROMOTIONAL_PHOTOS) {
            Toast.makeText(this, "Debes tener al menos " + MIN_PROMOTIONAL_PHOTOS + " fotos promocionales", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón durante el proceso
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        // Subir nuevas fotos si hay
        if (!newPhotosToUpload.isEmpty()) {
            uploadNewPhotosAndSave(nombreEmpresa, descripcion, ubicacion, correo, telefono);
        } else {
            // Solo actualizar datos sin nuevas fotos
            saveToFirestore(nombreEmpresa, descripcion, ubicacion, correo, telefono, currentPhotoUrls);
        }
    }

    private void uploadNewPhotosAndSave(String nombreEmpresa, String descripcion, String ubicacion, 
                                        String correo, String telefono) {
        String uid = currentUser.getUid();
        int startIndex = currentPhotoUrls.size(); // Índice para las nuevas fotos
        
        List<String> newUploadedUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);

        for (int i = 0; i < newPhotosToUpload.size(); i++) {
            Uri photoUri = newPhotosToUpload.get(i);
            final int photoIndex = startIndex + i;

            storageHelper.uploadCompanyPhoto(this, photoUri, uid, photoIndex, new StorageHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    synchronized (newUploadedUrls) {
                        newUploadedUrls.add(downloadUrl);
                        int count = uploadCount.incrementAndGet();
                        
                        if (count == newPhotosToUpload.size()) {
                            // Todas las nuevas fotos subidas
                            List<String> allPhotos = new ArrayList<>(currentPhotoUrls);
                            allPhotos.addAll(newUploadedUrls);
                            saveToFirestore(nombreEmpresa, descripcion, ubicacion, correo, telefono, allPhotos);
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error al subir foto promocional", e);
                    runOnUiThread(() -> {
                        Toast.makeText(admin_editar_empresa.this, "Error al subir fotos", Toast.LENGTH_SHORT).show();
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
    }

    private void saveToFirestore(String nombreEmpresa, String descripcion, String ubicacion,
                                 String correo, String telefono, List<String> fotosEmpresa) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(AuthConstants.FIELD_NOMBRE_EMPRESA, nombreEmpresa);
        updates.put(AuthConstants.FIELD_DESCRIPCION_EMPRESA, descripcion);
        updates.put(AuthConstants.FIELD_UBICACION_EMPRESA, ubicacion);
        updates.put(AuthConstants.FIELD_CORREO_EMPRESA, correo);
        updates.put(AuthConstants.FIELD_TELEFONO_EMPRESA, telefono);
        updates.put(AuthConstants.FIELD_FOTOS_EMPRESA, fotosEmpresa);

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Datos de empresa actualizados exitosamente");
                    Toast.makeText(this, "Datos de empresa actualizados exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar datos de empresa", e);
                    Toast.makeText(this, "Error al actualizar datos", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                });
    }

    private void updatePhotoCount() {
        int count = promotionalPhotosAdapter.getPhotoCount();
        String text = count + " fotos seleccionadas (mínimo " + MIN_PROMOTIONAL_PHOTOS + ")";
        tvPhotoCount.setText(text);
    }

    @Override
    public void onPhotoRemoved(int position) {
        // Obtener la foto removida
        List<Uri> photos = promotionalPhotosAdapter.getPhotos();
        
        // Si es una foto nueva (URI), removerla de la lista de nuevas
        if (position < photos.size()) {
            Uri removedUri = photos.get(position);
            newPhotosToUpload.remove(removedUri);
        } else {
            // Es una foto existente (URL), agregarla a la lista de eliminación
            int urlIndex = position - photos.size();
            if (urlIndex >= 0 && urlIndex < currentPhotoUrls.size()) {
                String removedUrl = currentPhotoUrls.get(urlIndex);
                currentPhotoUrls.remove(urlIndex);
                photosToDelete.add(removedUrl);
            }
        }

        promotionalPhotosAdapter.removePhoto(position);
        updatePhotoCount();
        
        if (promotionalPhotosAdapter.getPhotoCount() == 0) {
            rvPromotionalPhotos.setVisibility(View.GONE);
        }
    }
}

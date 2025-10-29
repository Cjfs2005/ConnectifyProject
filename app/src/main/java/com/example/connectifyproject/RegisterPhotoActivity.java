package com.example.connectifyproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class RegisterPhotoActivity extends AppCompatActivity {

    private String userType, nombre, apellido, correo, telefono, codigoPais;
    private String tipoDocumento, numeroDocumento, fechaNacimiento, genero;
    private String password, idiomas, experiencia;
    
    private ImageView ivProfilePhoto;
    private FloatingActionButton fabCamera;
    private MaterialButton btnCamera, btnGallery, btnOmitir, btnFinalizar;
    private TextView tvError;
    private View ivBack;
    
    private Uri selectedImageUri;
    private boolean hasSelectedPhoto = false;

    // Constantes para permisos
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    // Launchers para actividades
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_photo);

        getDataFromIntent();
        initViews();
        setupActivityLaunchers();
        setupClickListeners();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        userType = intent.getStringExtra("user_type");
        nombre = intent.getStringExtra("nombre");
        apellido = intent.getStringExtra("apellido");
        correo = intent.getStringExtra("correo");
        telefono = intent.getStringExtra("telefono");
        codigoPais = intent.getStringExtra("codigo_pais");
        tipoDocumento = intent.getStringExtra("tipo_documento");
        numeroDocumento = intent.getStringExtra("numero_documento");
        fechaNacimiento = intent.getStringExtra("fecha_nacimiento");
        genero = intent.getStringExtra("genero");
        password = intent.getStringExtra("password");
        idiomas = intent.getStringExtra("idiomas");
        experiencia = intent.getStringExtra("experiencia");
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        fabCamera = findViewById(R.id.fabCamera);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnOmitir = findViewById(R.id.btnOmitir);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        tvError = findViewById(R.id.tvError);
        ivBack = findViewById(R.id.ivBack);
    }

    private void setupActivityLaunchers() {
        // Launcher para cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getExtras() != null) {
                                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                                ivProfilePhoto.setImageBitmap(imageBitmap);
                                hasSelectedPhoto = true;
                                tvError.setVisibility(View.GONE);
                            }
                        }
                    }
                });

        // Launcher para galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                selectedImageUri = data.getData();
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                            getContentResolver(), selectedImageUri);
                                    ivProfilePhoto.setImageBitmap(bitmap);
                                    hasSelectedPhoto = true;
                                    tvError.setVisibility(View.GONE);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    showError("Error al cargar la imagen");
                                }
                            }
                        }
                    }
                });

        // Launcher para permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        showError("Permiso denegado");
                    }
                });
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        fabCamera.setOnClickListener(v -> openCamera());
        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        
        btnOmitir.setOnClickListener(v -> finishRegistration());
        btnFinalizar.setOnClickListener(v -> finishRegistration());
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                showError("No se encontró una aplicación de cámara");
            }
        }
    }

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            if (intent.resolveActivity(getPackageManager()) != null) {
                galleryLauncher.launch(intent);
            } else {
                showError("No se encontró una aplicación de galería");
            }
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void finishRegistration() {
        // Aquí se enviarían los datos al servidor para crear la cuenta
        // Por ahora, mostraremos un mensaje de éxito y volveremos al login
        
        Toast.makeText(this, "¡Registro completado exitosamente!", Toast.LENGTH_LONG).show();
        
        // Volver al login y limpiar el stack de actividades
        Intent intent = new Intent(this, auth_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
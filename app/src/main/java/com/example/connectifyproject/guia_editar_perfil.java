package com.example.connectifyproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.connectifyproject.databinding.GuiaEditarPerfilBinding;

public class guia_editar_perfil extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private GuiaEditarPerfilBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaEditarPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupImageUpload();
        setupSaveButton();
        loadUserData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Editar perfil");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupImageUpload() {
        binding.btnSubirImagen.setOnClickListener(v -> openImageSelector());
        binding.ivProfilePhoto.setOnClickListener(v -> openImageSelector());
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void setupSaveButton() {
        binding.btnGuardar.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        // TODO: Replace with actual backend call
        binding.etCorreo.setText("jerez@gmail.com");
        binding.etTelefono.setText("987654321");
    }

    private void saveProfile() {
        String correo = binding.etCorreo.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();

        if (correo.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Completa los campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Send data to backend
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                binding.ivProfilePhoto.setImageURI(selectedImageUri);
                // TODO: Upload image to backend
            }
        }
    }
}
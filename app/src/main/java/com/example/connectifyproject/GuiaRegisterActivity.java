package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.utils.AuthConstants;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity de registro para Guía
 * Pre-llena email de Firebase Auth y guarda datos completos en Firestore
 */
public class GuiaRegisterActivity extends AppCompatActivity {

    private static final String TAG = "GuiaRegister";
    
    private EditText etNombre, etTelefono, etLicencia, etExperiencia;
    private Button btnGuardar, btnLogout;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guia_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupListeners();
        preloadData();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombreGuia);
        etTelefono = findViewById(R.id.etTelefonoGuia);
        etLicencia = findViewById(R.id.etLicenciaGuia);
        etExperiencia = findViewById(R.id.etExperienciaGuia);
        btnGuardar = findViewById(R.id.btnGuardarGuia);
        btnLogout = findViewById(R.id.btnLogoutGuiaRegister);
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> saveGuiaData());
        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Pre-cargar email de Firebase Auth
     */
    private void preloadData() {
        String nombre = currentUser.getDisplayName();
        
        if (nombre != null && !nombre.isEmpty()) {
            etNombre.setText(nombre);
        }
    }

    /**
     * Guardar datos del guía en Firestore
     */
    private void saveGuiaData() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String licencia = etLicencia.getText().toString().trim();
        String experiencia = etExperiencia.getText().toString().trim();

        // Validaciones
        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa tu nombre");
            etNombre.requestFocus();
            return;
        }

        if (telefono.isEmpty()) {
            etTelefono.setError("Ingresa tu teléfono");
            etTelefono.requestFocus();
            return;
        }

        if (licencia.isEmpty()) {
            etLicencia.setError("Ingresa tu número de licencia");
            etLicencia.requestFocus();
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        // Crear documento en Firestore
        Map<String, Object> guiaData = new HashMap<>();
        guiaData.put(AuthConstants.FIELD_EMAIL, currentUser.getEmail());
        guiaData.put(AuthConstants.FIELD_ROL, AuthConstants.ROLE_GUIA);
        guiaData.put(AuthConstants.FIELD_NOMBRE, nombre);
        guiaData.put("telefono", telefono);
        guiaData.put("licencia", licencia);
        guiaData.put("experiencia", experiencia);
        guiaData.put("uid", currentUser.getUid());
        guiaData.put("photoUrl", currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(currentUser.getUid())
                .set(guiaData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Guía guardado exitosamente");
                    Toast.makeText(this, "¡Registro completado exitosamente!", Toast.LENGTH_SHORT).show();
                    redirectToGuiaDashboard();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar guía", e);
                    Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
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
     * Redirigir al dashboard de guía
     */
    private void redirectToGuiaDashboard() {
        Intent intent = new Intent(this, guia_tours_ofertas.class);
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

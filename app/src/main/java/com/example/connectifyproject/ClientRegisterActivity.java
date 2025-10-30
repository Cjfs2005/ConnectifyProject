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
 * Activity de registro para Cliente
 * Pre-llena email de Firebase Auth y guarda datos completos en Firestore
 */
public class ClientRegisterActivity extends AppCompatActivity {

    private static final String TAG = "ClientRegister";
    
    private EditText etNombre, etTelefono, etDireccion;
    private Button btnGuardar, btnLogout;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_register);

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
        etNombre = findViewById(R.id.etNombreCliente);
        etTelefono = findViewById(R.id.etTelefonoCliente);
        etDireccion = findViewById(R.id.etDireccionCliente);
        btnGuardar = findViewById(R.id.btnGuardarCliente);
        btnLogout = findViewById(R.id.btnLogoutClientRegister);
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> saveClientData());
        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Pre-cargar email de Firebase Auth
     */
    private void preloadData() {
        // El email ya está en Firebase Auth, no es necesario pedirlo de nuevo
        String email = currentUser.getEmail();
        String nombre = currentUser.getDisplayName();
        
        if (nombre != null && !nombre.isEmpty()) {
            etNombre.setText(nombre);
        }
    }

    /**
     * Guardar datos del cliente en Firestore
     */
    private void saveClientData() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();

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

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        // Crear documento en Firestore
        Map<String, Object> clienteData = new HashMap<>();
        clienteData.put(AuthConstants.FIELD_EMAIL, currentUser.getEmail());
        clienteData.put(AuthConstants.FIELD_ROL, AuthConstants.ROLE_CLIENTE);
        clienteData.put(AuthConstants.FIELD_NOMBRE, nombre);
        clienteData.put("telefono", telefono);
        clienteData.put("direccion", direccion);
        clienteData.put("uid", currentUser.getUid());
        clienteData.put("emailVerificado", currentUser.isEmailVerified());
        clienteData.put("photoUrl", currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");

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

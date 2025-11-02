package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
 * Activity para seleccionar rol después de autenticación
 * Se muestra cuando el usuario está autenticado pero no tiene rol asignado
 */
public class RoleSelectionActivity extends AppCompatActivity {

    private static final String TAG = "RoleSelection";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private Button btnCliente;
    private Button btnGuia;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Ocultar action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Verificar que haya usuario autenticado
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Verificar si es SuperAdmin (no debería llegar aquí)
        if (AuthConstants.isSuperAdmin(currentUser.getEmail())) {
            redirectToSuperAdmin();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnCliente = findViewById(R.id.btnSoyCliente);
        btnGuia = findViewById(R.id.btnSoyGuia);
        btnLogout = findViewById(R.id.btnLogoutRoleSelection);
    }

    private void setupListeners() {
        btnCliente.setOnClickListener(v -> selectRole(AuthConstants.ROLE_CLIENTE));
        btnGuia.setOnClickListener(v -> selectRole(AuthConstants.ROLE_GUIA));
        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Seleccionar rol y crear documento en Firestore
     */
    private void selectRole(String rol) {
        if (currentUser == null) return;

        // Deshabilitar botones mientras se procesa
        setButtonsEnabled(false);

        // Crear documento básico en Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put(AuthConstants.FIELD_EMAIL, currentUser.getEmail());
        userData.put(AuthConstants.FIELD_ROL, rol);
        userData.put("uid", currentUser.getUid());

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(currentUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Rol guardado exitosamente: " + rol);
                    Toast.makeText(this, "Rol seleccionado: " + rol, Toast.LENGTH_SHORT).show();
                    redirectToRegistration(rol);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar rol", e);
                    Toast.makeText(this, "Error al guardar el rol", Toast.LENGTH_SHORT).show();
                    setButtonsEnabled(true);
                });
    }

    /**
     * Redirigir a pantalla de registro según rol
     */
    private void redirectToRegistration(String rol) {
        Intent intent;

        if (AuthConstants.ROLE_CLIENTE.equals(rol)) {
            intent = new Intent(this, ClientRegisterActivity.class);
        } else if (AuthConstants.ROLE_GUIA.equals(rol)) {
            intent = new Intent(this, GuiaRegisterActivity.class);
        } else {
            Toast.makeText(this, "Rol no reconocido", Toast.LENGTH_SHORT).show();
            return;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Cerrar sesión
     */
    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "Sesión cerrada");
                    redirectToLogin();
                });
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

    /**
     * Redirigir a SuperAdmin
     */
    private void redirectToSuperAdmin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Habilitar/deshabilitar botones
     */
    private void setButtonsEnabled(boolean enabled) {
        btnCliente.setEnabled(enabled);
        btnGuia.setEnabled(enabled);
        btnLogout.setEnabled(enabled);
    }

    @Override
    public void onBackPressed() {
        // No permitir volver atrás, solo logout
        super.onBackPressed();
    }
}

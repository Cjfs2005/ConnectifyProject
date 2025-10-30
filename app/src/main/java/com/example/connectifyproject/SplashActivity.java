package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.utils.AuthConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity inicial - Verifica autenticación de Firebase y redirige
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 1500; // 1.5 segundos
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ocultar la action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Mostrar splash por un momento y luego verificar autenticación
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthenticationState, SPLASH_DELAY);
    }

    /**
     * Verificar estado de autenticación de Firebase
     */
    private void checkAuthenticationState() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser != null) {
            // Usuario autenticado → Verificar rol
            Log.d(TAG, "Usuario autenticado: " + currentUser.getEmail());
            handleAuthenticatedUser(currentUser);
        } else {
            // No hay usuario autenticado → Ir a login
            Log.d(TAG, "No hay usuario autenticado");
            goToLogin();
        }
    }

    /**
     * Manejar usuario autenticado - verificar si es SuperAdmin o buscar en Firestore
     */
    private void handleAuthenticatedUser(FirebaseUser user) {
        String email = user.getEmail();
        
        // Verificar si es SuperAdmin PRIMERO
        if (AuthConstants.isSuperAdmin(email)) {
            Log.d(TAG, "SuperAdmin detectado: " + email);
            redirectToSuperAdmin();
            return;
        }

        // No es SuperAdmin → Buscar rol en Firestore
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(this::handleFirestoreDocument)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar Firestore", e);
                    // En caso de error, ir a login
                    goToLogin();
                });
    }

    /**
     * Manejar documento de Firestore
     */
    private void handleFirestoreDocument(DocumentSnapshot document) {
        if (document.exists()) {
            // Usuario tiene documento → Leer rol y redirigir
            String rol = document.getString(AuthConstants.FIELD_ROL);
            Log.d(TAG, "Rol encontrado: " + rol);
            redirectByRole(rol);
        } else {
            // Usuario autenticado pero sin documento → Ir a selección de rol
            Log.d(TAG, "Usuario sin rol asignado");
            redirectToRoleSelection();
        }
    }

    /**
     * Redirigir según rol de Firestore
     */
    private void redirectByRole(String rol) {
        Intent intent;
        
        if (AuthConstants.ROLE_CLIENTE.equals(rol)) {
            intent = new Intent(this, cliente_inicio.class);
        } else if (AuthConstants.ROLE_GUIA.equals(rol)) {
            intent = new Intent(this, guia_tours_ofertas.class);
        } else {
            // Rol desconocido → Ir a selección de rol
            redirectToRoleSelection();
            return;
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Redirigir a selección de rol
     */
    private void redirectToRoleSelection() {
        Intent intent = new Intent(this, RoleSelectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Redirigir a SuperAdmin dashboard
     */
    private void redirectToSuperAdmin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Ir a la pantalla de login con diseño personalizado
     */
    private void goToLogin() {
        Intent intent = new Intent(this, CustomLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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

        // Buscar por UID primero
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Documento encontrado por UID
                        handleFirestoreDocument(document);
                    } else {
                        // No existe por UID, buscar por email (para admins pre-registrados)
                        Log.d(TAG, "No se encontró por UID, buscando por email: " + email);
                        searchUserByEmail(user);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar Firestore", e);
                    goToLogin();
                });
    }

    /**
     * Buscar usuario por email (para admins pre-registrados)
     */
    private void searchUserByEmail(FirebaseUser user) {
        String email = user.getEmail();
        
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .whereEqualTo(AuthConstants.FIELD_EMAIL, email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String rol = doc.getString(AuthConstants.FIELD_ROL);
                        Boolean perfilCompleto = doc.getBoolean(AuthConstants.FIELD_PERFIL_COMPLETO);
                        
                        // Si es admin pre-registrado, migrar documento a UID
                        if (AuthConstants.ROLE_ADMIN.equals(rol) && (perfilCompleto == null || !perfilCompleto)) {
                            migratePreRegisteredAdmin(doc, user);
                        } else {
                            // Otro caso, redirigir normalmente
                            handleFirestoreDocument(doc);
                        }
                    } else {
                        // No existe en Firestore → Ir a selección de rol
                        redirectToRoleSelection();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar por email", e);
                    goToLogin();
                });
    }

    /**
     * Migrar documento de admin pre-registrado al UID correcto
     */
    private void migratePreRegisteredAdmin(DocumentSnapshot oldDoc, FirebaseUser user) {
        // Copiar datos al nuevo documento con UID
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .set(oldDoc.getData())
                .addOnSuccessListener(aVoid -> {
                    // Actualizar con UID
                    db.collection(AuthConstants.COLLECTION_USUARIOS)
                            .document(user.getUid())
                            .update(AuthConstants.FIELD_UID, user.getUid())
                            .addOnSuccessListener(aVoid2 -> {
                                // Eliminar documento antiguo
                                oldDoc.getReference().delete();
                                // Redirigir a completar perfil de admin
                                redirectToCompleteAdminProfile();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al migrar documento", e);
                    goToLogin();
                });
    }

    /**
     * Manejar documento de Firestore
     */
    private void handleFirestoreDocument(DocumentSnapshot document) {
        if (document.exists()) {
            // Usuario tiene documento → Leer rol y verificar si está habilitado
            String rol = document.getString(AuthConstants.FIELD_ROL);
            Boolean habilitado = document.getBoolean(AuthConstants.FIELD_HABILITADO);
            Boolean perfilCompleto = document.getBoolean(AuthConstants.FIELD_PERFIL_COMPLETO);
            
            Log.d(TAG, "Rol encontrado: " + rol + ", Habilitado: " + habilitado + ", Perfil completo: " + perfilCompleto);
            
            // Verificar si el perfil está completo
            if (perfilCompleto == null || !perfilCompleto) {
                Log.d(TAG, "Perfil incompleto, redirigiendo a completar registro");
                redirectToCompleteProfile(rol);
                return;
            }
            
            // Verificar si el usuario está habilitado
            if (habilitado != null && !habilitado) {
                // Usuario NO habilitado (solo aplica para Guías)
                Log.d(TAG, "Usuario no habilitado");
                showNotEnabledMessage();
                return;
            }
            
            redirectByRole(rol);
        } else {
            // Usuario autenticado pero sin documento → Ir a selección de rol
            Log.d(TAG, "Usuario sin rol asignado");
            redirectToRoleSelection();
        }
    }

    /**
     * Mostrar mensaje de cuenta no habilitada y cerrar sesión
     */
    private void showNotEnabledMessage() {
        Toast.makeText(this, 
            "Tu cuenta está pendiente de aprobación por el administrador", 
            Toast.LENGTH_LONG).show();
        
        // Cerrar sesión automáticamente
        FirebaseAuth.getInstance().signOut();
        
        // Esperar un momento y redirigir al login
        new Handler(Looper.getMainLooper()).postDelayed(() -> goToLogin(), 2000);
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
        } else if (AuthConstants.ROLE_ADMIN.equals(rol)) {
            // Por ahora redirigir a cliente_inicio, más adelante crear dashboard de admin
            intent = new Intent(this, cliente_inicio.class);
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
     * Redirigir a completar perfil según rol
     */
    private void redirectToCompleteProfile(String rol) {
        Intent intent;
        
        if (AuthConstants.ROLE_CLIENTE.equals(rol)) {
            intent = new Intent(this, ClientRegisterActivity.class);
        } else if (AuthConstants.ROLE_GUIA.equals(rol)) {
            intent = new Intent(this, GuiaRegisterActivity.class);
        } else if (AuthConstants.ROLE_ADMIN.equals(rol)) {
            intent = new Intent(this, AdminRegisterActivity.class);
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
     * Redirigir a completar perfil de admin
     */
    private void redirectToCompleteAdminProfile() {
        Intent intent = new Intent(this, AdminRegisterActivity.class);
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
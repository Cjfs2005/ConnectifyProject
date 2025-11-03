package com.example.connectifyproject;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.utils.AuthConstants;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Activity de login personalizada con diseño mejorado
 * Usa Firebase UI directamente
 */
public class CustomLoginActivity extends AppCompatActivity {

    private static final String TAG = "CustomLogin";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    private MaterialButton btnEmailLogin;
    private MaterialButton btnGoogleLogin;
    
    // Launcher para Firebase UI
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configurar idioma español antes de super.onCreate()
        setLocale("es");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_firebase_ui_custom);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    /**
     * Configurar el locale a español
     */
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void initViews() {
        btnEmailLogin = findViewById(R.id.email_button);
        btnGoogleLogin = findViewById(R.id.google_button);
    }

    private void setupListeners() {
        btnEmailLogin.setOnClickListener(v -> startFirebaseUIWithEmail());
        btnGoogleLogin.setOnClickListener(v -> startFirebaseUIWithGoogle());
    }

    /**
     * Iniciar Firebase UI solo con Email
     */
    private void startFirebaseUIWithEmail() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_ConnectifyProject)
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(signInIntent);
    }

    /**
     * Iniciar Firebase UI solo con Google
     */
    private void startFirebaseUIWithGoogle() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_ConnectifyProject)
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(signInIntent);
    }

    /**
     * Callback cuando Firebase UI termina
     */
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            // Login exitoso
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "Login exitoso: " + user.getEmail());
                handleSuccessfulLogin(user);
            }
        } else {
            // Login fallido o cancelado
            Log.e(TAG, "Login fallido o cancelado");
            Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
            // NO cerrar la actividad, permitir que el usuario lo intente de nuevo
        }
    }

    /**
     * Verificar usuario y redirigir
     */
    private void handleSuccessfulLogin(FirebaseUser user) {
        String email = user.getEmail();
        
        // Verificar si es SuperAdmin
        if (AuthConstants.isSuperAdmin(email)) {
            Log.d(TAG, "SuperAdmin detectado");
            redirectToSuperAdmin();
            return;
        }

        // Buscar documento en Firestore por UID primero
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        handleFirestoreDocument(document);
                    } else {
                        // No existe por UID, buscar por email (admins pre-registrados)
                        Log.d(TAG, "No se encontró por UID, buscando por email: " + email);
                        searchUserByEmail(user);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar Firestore", e);
                    Toast.makeText(this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
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
                            Log.d(TAG, "Admin pre-registrado encontrado, migrando a UID");
                            migratePreRegisteredAdmin(doc, user);
                        } else {
                            // Otro caso, redirigir normalmente
                            handleFirestoreDocument(doc);
                        }
                    } else {
                        // No existe en Firestore → Ir a selección de rol
                        Log.d(TAG, "Usuario sin rol asignado");
                        redirectToRoleSelection();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar por email", e);
                    Toast.makeText(this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Migrar documento de admin pre-registrado al UID correcto
     */
    private void migratePreRegisteredAdmin(DocumentSnapshot oldDoc, FirebaseUser user) {
        java.util.Map<String, Object> data = oldDoc.getData();
        if (data != null) {
            // Copiar datos al nuevo documento con UID
            data.put(AuthConstants.FIELD_UID, user.getUid());
            
            db.collection(AuthConstants.COLLECTION_USUARIOS)
                    .document(user.getUid())
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Documento migrado exitosamente");
                        // Eliminar documento antiguo
                        oldDoc.getReference().delete()
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "Documento antiguo eliminado");
                                    // Redirigir a completar perfil de admin
                                    redirectToCompleteAdminProfile();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al eliminar documento antiguo", e);
                                    // Aún así continuar
                                    redirectToCompleteAdminProfile();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al migrar documento", e);
                        Toast.makeText(this, "Error al procesar usuario", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "No hay datos en el documento antiguo");
            redirectToRoleSelection();
        }
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

    private void handleFirestoreDocument(DocumentSnapshot document) {
        if (document.exists()) {
            String rol = document.getString(AuthConstants.FIELD_ROL);
            String nombreCompleto = document.getString(AuthConstants.FIELD_NOMBRE_COMPLETO);
            Boolean habilitado = document.getBoolean(AuthConstants.FIELD_HABILITADO);
            
            Log.d(TAG, "Rol: " + rol + ", NombreCompleto: " + nombreCompleto + ", Habilitado: " + habilitado);
            
            // VALIDACIÓN 1: Verificar si el registro está completo
            if (nombreCompleto == null || nombreCompleto.isEmpty()) {
                Log.d(TAG, "Registro incompleto, redirigiendo a completar registro");
                redirectToCompleteRegistration(rol);
                return;
            }
            
            // VALIDACIÓN 2: Verificar si el usuario está habilitado (importante para Guías)
            if (habilitado != null && !habilitado) {
                Log.d(TAG, "Usuario NO habilitado");
                Toast.makeText(this, 
                    "Tu cuenta está pendiente de aprobación por el administrador", 
                    Toast.LENGTH_LONG).show();
                // Cerrar sesión
                mAuth.signOut();
                return;
            }
            
            // Usuario completo y habilitado → Redirigir al dashboard
            redirectByRole(rol);
        } else {
            Log.d(TAG, "Usuario nuevo sin rol asignado");
            redirectToRoleSelection();
        }
    }
    
    /**
     * Redirigir a completar registro según rol
     */
    private void redirectToCompleteRegistration(String rol) {
        Intent intent;
        
        if (AuthConstants.ROLE_CLIENTE.equals(rol)) {
            intent = new Intent(this, ClientRegisterActivity.class);
        } else if (AuthConstants.ROLE_GUIA.equals(rol)) {
            intent = new Intent(this, GuiaRegisterActivity.class);
        } else if (AuthConstants.ROLE_ADMIN.equals(rol)) {
            intent = new Intent(this, AdminRegisterActivity.class);
        } else {
            redirectToRoleSelection();
            return;
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectByRole(String rol) {
        Intent intent;
        
        if (AuthConstants.ROLE_CLIENTE.equals(rol)) {
            intent = new Intent(this, cliente_inicio.class);
        } else if (AuthConstants.ROLE_GUIA.equals(rol)) {
            intent = new Intent(this, guia_tours_ofertas.class);
        } else if (AuthConstants.ROLE_ADMIN.equals(rol)) {
            intent = new Intent(this, admin_dashboard.class);
        } else {
            redirectToRoleSelection();
            return;
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToRoleSelection() {
        Intent intent = new Intent(this, RoleSelectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToSuperAdmin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

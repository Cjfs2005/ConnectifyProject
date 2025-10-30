package com.example.connectifyproject;

import android.content.Intent;
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

        // Buscar documento en Firestore
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(this::handleFirestoreDocument)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar Firestore", e);
                    Toast.makeText(this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleFirestoreDocument(DocumentSnapshot document) {
        if (document.exists()) {
            String rol = document.getString(AuthConstants.FIELD_ROL);
            Log.d(TAG, "Rol encontrado: " + rol);
            redirectByRole(rol);
        } else {
            Log.d(TAG, "Usuario nuevo sin rol asignado");
            redirectToRoleSelection();
        }
    }

    private void redirectByRole(String rol) {
        Intent intent;
        
        if (AuthConstants.ROLE_CLIENTE.equals(rol)) {
            intent = new Intent(this, cliente_inicio.class);
        } else if (AuthConstants.ROLE_GUIA.equals(rol)) {
            intent = new Intent(this, guia_tours_ofertas.class);
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

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

/**
 * Activity para login con Firebase UI
 * Usa layout personalizado con logo de Tourly
 */
public class FirebaseLoginActivity extends AppCompatActivity {

    private static final String TAG = "FirebaseLogin";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    // Launcher para Firebase UI
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_login);

        // Ocultar action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Iniciar Firebase UI
        startFirebaseUI();
    }

    /**
     * Configurar e iniciar Firebase UI con layout personalizado
     */
    private void startFirebaseUI() {
        // Proveedores de autenticación
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Intent de Firebase UI con configuración básica
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_ConnectifyProject)
                .setLogo(R.mipmap.tourly_logo_img)
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
            finish(); // Volver al splash
        }
    }

    /**
     * Manejar login exitoso - verificar rol y redirigir
     */
    private void handleSuccessfulLogin(FirebaseUser user) {
        String email = user.getEmail();
        
        // Verificar si es SuperAdmin
        if (AuthConstants.isSuperAdmin(email)) {
            Log.d(TAG, "SuperAdmin detectado");
            redirectToSuperAdmin();
            return;
        }

        // Buscar documento en Firestore para usuarios normales
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(this::handleFirestoreDocument)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar Firestore", e);
                    Toast.makeText(this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
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
            // Usuario nuevo → Ir a selección de rol
            Log.d(TAG, "Usuario nuevo sin rol asignado");
            redirectToRoleSelection();
        }
    }

    /**
     * Redirigir según rol
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
     * Redirigir a pantalla de selección de rol
     */
    private void redirectToRoleSelection() {
        Intent intent = new Intent(this, RoleSelectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Redirigir a dashboard de SuperAdmin
     */
    private void redirectToSuperAdmin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

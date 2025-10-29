package com.example.connectifyproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.model.LoginResult;
import com.example.connectifyproject.utils.Cliente_PreferencesManager;

public class SplashActivity extends AppCompatActivity {

    private Cliente_PreferencesManager preferencesManager;
    private static final int SPLASH_DELAY = 1500; // 1.5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ocultar la action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        preferencesManager = new Cliente_PreferencesManager(this);

        // Mostrar splash por un momento y luego verificar autenticación
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthenticationState, SPLASH_DELAY);
    }

    /**
     * Verificar estado de autenticación y decidir a dónde ir
     */
    private void checkAuthenticationState() {
        if (preferencesManager.shouldRememberLogin()) {
            String email = preferencesManager.getSavedEmail();
            String password = preferencesManager.getSavedPassword();
            
            if (!email.isEmpty() && !password.isEmpty()) {
                // Validar credenciales automáticamente sin mostrar UI de login
                validateCredentialsAsync(email, password);
                return;
            }
        }
        
        // No hay credenciales guardadas → Ir a pantalla de login
        goToLogin();
    }

    /**
     * Validar credenciales guardadas en background
     */
    private void validateCredentialsAsync(String email, String password) {
        // Simular validación asíncrona (aquí irá Firebase/API en el futuro)
        new AsyncTask<Void, Void, LoginResult.UserType>() {
            @Override
            protected LoginResult.UserType doInBackground(Void... voids) {
                try {
                    // Simular tiempo de validación
                    Thread.sleep(500);
                    
                    // Simular lógica de validación (reemplazar con Firebase/API)
                    return validateCredentialsLogic(email, password);
                } catch (InterruptedException e) {
                    return null;
                }
            }
            
            @Override
            protected void onPostExecute(LoginResult.UserType userType) {
                if (userType != null) {
                    // Credenciales válidas → Ir directo al dashboard correspondiente
                    redirectToDashboard(userType);
                } else {
                    // Credenciales inválidas → Limpiar y mostrar login
                    preferencesManager.clearLoginCredentials();
                    goToLogin();
                }
            }
        }.execute();
    }

    /**
     * Lógica de validación de credenciales (hardcodeada por ahora)
     * TODO: Reemplazar con servicio de Firebase/API
     */
    private LoginResult.UserType validateCredentialsLogic(String email, String password) {
        // Por ahora simulamos que todas las credenciales guardadas son válidas
        // y determinamos el tipo por el email
        if (email.contains("admin")) {
            return LoginResult.UserType.ADMIN;
        } else if (email.contains("superadmin")) {
            return LoginResult.UserType.SUPERADMIN;
        } else if (email.contains("guia")) {
            return LoginResult.UserType.GUIA;
        } else {
            return LoginResult.UserType.CLIENTE;
        }
    }

    /**
     * Redirigir al dashboard correspondiente según el tipo de usuario
     */
    private void redirectToDashboard(LoginResult.UserType userType) {
        Intent intent;
        
        switch (userType) {
            case SUPERADMIN:
                intent = new Intent(this, MainActivity.class);
                break;
            case ADMIN:
                intent = new Intent(this, admin_dashboard.class);
                break;
            case CLIENTE:
                intent = new Intent(this, cliente_inicio.class);
                break;
            case GUIA:
                intent = new Intent(this, guia_tours_ofertas.class);
                break;
            default:
                // Fallback al login si hay algún problema
                goToLogin();
                return;
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Ir a la pantalla de login
     */
    private void goToLogin() {
        Intent intent = new Intent(this, auth_login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
package com.example.connectifyproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.connectifyproject.utils.Cliente_PreferencesManager;

public class cliente_permisos extends AppCompatActivity {

    private SwitchMaterial switchPosicion, switchNotificaciones;
    private Cliente_PreferencesManager preferencesManager;
    
    // Request codes para permisos
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_permisos);

        // Initialize PreferencesManager
        preferencesManager = new Cliente_PreferencesManager(this);

        // Setup toolbar
        setupToolbar();

        // Initialize switches
        initializeSwitches();

        // Load current permission states
        loadCurrentPermissionStates();

        // Setup switch listeners
        setupSwitchListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Permisos");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeSwitches() {
        switchPosicion = findViewById(R.id.switch_posicion);
        switchNotificaciones = findViewById(R.id.switch_notificaciones);
    }

    private void loadCurrentPermissionStates() {
        // Verificar estado actual de permisos del sistema
        boolean hasLocationPermission = checkLocationPermission();
        boolean hasNotificationPermission = checkNotificationPermission();

        // Actualizar switches basado en permisos reales del sistema
        switchPosicion.setChecked(hasLocationPermission);
        switchNotificaciones.setChecked(hasNotificationPermission);

        // Guardar estados en preferencias
        preferencesManager.savePermissionState("location", hasLocationPermission);
        preferencesManager.savePermissionState("notifications", hasNotificationPermission);
    }

    private void setupSwitchListeners() {
        switchPosicion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    requestLocationPermission();
                } else {
                    showPermissionDisableDialog("ubicación", () -> {
                        preferencesManager.savePermissionState("location", false);
                        openAppSettings();
                    });
                }
            }
        });

        switchNotificaciones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    requestNotificationPermission();
                } else {
                    showPermissionDisableDialog("notificaciones", () -> {
                        preferencesManager.savePermissionState("notifications", false);
                        openAppSettings();
                    });
                }
            }
        });
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    == PackageManager.PERMISSION_GRANTED;
        }
        // Para versiones anteriores a Android 13, las notificaciones están habilitadas por defecto
        return true;
    }

    private void requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionRationaleDialog(
                "Ubicación",
                "La aplicación necesita acceso a tu ubicación para mostrarte tours cercanos y mejorar tu experiencia.",
                () -> ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION)
            );
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationaleDialog(
                    "Notificaciones",
                    "La aplicación necesita enviar notificaciones para confirmaciones de reservas, recordatorios y actualizaciones importantes.",
                    () -> ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_NOTIFICATION_PERMISSION)
                );
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        } else {
            // Para versiones anteriores, marcar como concedido
            preferencesManager.savePermissionState("notifications", true);
            Toast.makeText(this, "Notificaciones habilitadas", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPermissionRationaleDialog(String permissionName, String message, Runnable onAccept) {
        new AlertDialog.Builder(this)
                .setTitle("Permiso de " + permissionName)
                .setMessage(message)
                .setPositiveButton("Permitir", (dialog, which) -> onAccept.run())
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                    loadCurrentPermissionStates(); // Restaurar estado del switch
                })
                .setCancelable(false)
                .show();
    }

    private void showPermissionDisableDialog(String permissionName, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle("Deshabilitar " + permissionName)
                .setMessage("¿Estás seguro de que quieres deshabilitar el permiso de " + permissionName + 
                          "? Esto puede afectar la funcionalidad de la aplicación. Deberás deshabilitarlo desde la configuración del sistema.")
                .setPositiveButton("Ir a Configuración", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                    loadCurrentPermissionStates(); // Restaurar estado del switch
                })
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                handleLocationPermissionResult(grantResults);
                break;
            case REQUEST_NOTIFICATION_PERMISSION:
                handleNotificationPermissionResult(grantResults);
                break;
        }
    }

    private void handleLocationPermissionResult(int[] grantResults) {
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        
        preferencesManager.savePermissionState("location", granted);
        switchPosicion.setChecked(granted);
        
        if (granted) {
            Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            showPermissionDeniedDialog("ubicación");
        }
    }

    private void handleNotificationPermissionResult(int[] grantResults) {
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        
        preferencesManager.savePermissionState("notifications", granted);
        switchNotificaciones.setChecked(granted);
        
        if (granted) {
            Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
            showPermissionDeniedDialog("notificaciones");
        }
    }

    private void showPermissionDeniedDialog(String permissionName) {
        new AlertDialog.Builder(this)
                .setTitle("Permiso Denegado")
                .setMessage("Sin el permiso de " + permissionName + " algunas funciones pueden no estar disponibles. " +
                          "Puedes habilitarlo más tarde desde la configuración de la aplicación.")
                .setPositiveButton("Ir a Configuración", (dialog, which) -> openAppSettings())
                .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar estados cuando regrese de configuración
        loadCurrentPermissionStates();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
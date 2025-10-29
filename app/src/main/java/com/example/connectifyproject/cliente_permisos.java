package com.example.connectifyproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class cliente_permisos extends AppCompatActivity {

    private SwitchMaterial switchPosicion, switchNotificaciones;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "PermisosPrefs";
    private static final String KEY_POSICION = "posicion_enabled";
    private static final String KEY_NOTIFICACIONES = "notificaciones_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_permisos);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Permisos");
        }

        // Initialize switches
        switchPosicion = findViewById(R.id.switch_posicion);
        switchNotificaciones = findViewById(R.id.switch_notificaciones);

        // Load saved preferences
        loadSavedPreferences();

        // Setup switch listeners
        switchPosicion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreference(KEY_POSICION, isChecked);
            }
        });

        switchNotificaciones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreference(KEY_NOTIFICACIONES, isChecked);
            }
        });

        // Handle back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadSavedPreferences() {
        // Load saved states, default to true
        boolean posicionEnabled = sharedPreferences.getBoolean(KEY_POSICION, true);
        boolean notificacionesEnabled = sharedPreferences.getBoolean(KEY_NOTIFICACIONES, true);

        switchPosicion.setChecked(posicionEnabled);
        switchNotificaciones.setChecked(notificacionesEnabled);
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
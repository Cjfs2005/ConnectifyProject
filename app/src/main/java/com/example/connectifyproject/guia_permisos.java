package com.example.connectifyproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.connectifyproject.databinding.GuiaPermisosBinding;

public class guia_permisos extends AppCompatActivity {

    private GuiaPermisosBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "PermisosPrefs";
    private static final String KEY_POSICION = "posicion_enabled";
    private static final String KEY_NOTIFICACIONES = "notificaciones_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaPermisosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        setupToolbar();
        loadSavedPreferences();
        setupSwitchListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Permisos");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSavedPreferences() {
        boolean posicionEnabled = sharedPreferences.getBoolean(KEY_POSICION, true);
        boolean notificacionesEnabled = sharedPreferences.getBoolean(KEY_NOTIFICACIONES, true);
        binding.switchPosicion.setChecked(posicionEnabled);
        binding.switchNotificaciones.setChecked(notificacionesEnabled);
    }

    private void setupSwitchListeners() {
        binding.switchPosicion.setOnCheckedChangeListener((buttonView, isChecked) -> savePreference(KEY_POSICION, isChecked));
        binding.switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> savePreference(KEY_NOTIFICACIONES, isChecked));
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
package com.example.connectifyproject;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.connectifyproject.databinding.AdminPlaceholderViewBinding;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Actividad principal para Guía
 * Pantalla donde el guía puede gestionar sus tours y ofertas
 */
public class guia_tours_ofertas extends AppCompatActivity {
    
    private AdminPlaceholderViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminPlaceholderViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

        binding.topAppBar.setTitle("Connectify - Guía");
        binding.tvPlaceholder.setText("Gestiona tus tours y ofertas");

        // Configurar navegación inferior específica para guía
        binding.bottomNav.setOnItemSelectedListener(navListener);
        binding.bottomNav.setSelectedItemId(R.id.nav_dashboard);
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            return true; // Ya estamos aquí
        }
        if (id == R.id.nav_tours) {
            // TODO: Navegar a tours del guía
            return true;
        }
        if (id == R.id.nav_chat) {
            // TODO: Navegar a chat del guía
            return true;
        }
        if (id == R.id.nav_pagos) {
            // TODO: Navegar a pagos del guía
            return true;
        }
        if (id == R.id.nav_perfil) {
            // TODO: Navegar a perfil del guía
            return true;
        }
        return false;
    };
}
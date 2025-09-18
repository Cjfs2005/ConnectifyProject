package com.example.connectifyproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.connectifyproject.databinding.AdminPlaceholderViewBinding;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Actividad principal para SuperAdmin
 * Contiene navegación entre gestión de usuarios, solicitudes y logs
 */
public class sa_users_view extends AppCompatActivity {
    
    private AdminPlaceholderViewBinding binding;
    
    @Override 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminPlaceholderViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

        binding.topAppBar.setTitle("Connectify - SuperAdmin");
        binding.tvPlaceholder.setText("Gestión de usuarios y administración del sistema");

        // Configurar navegación inferior específica para superadmin
        binding.bottomNav.setOnItemSelectedListener(navListener);
        binding.bottomNav.setSelectedItemId(R.id.nav_dashboard);
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            return true; // Ya estamos aquí
        }
        if (id == R.id.nav_tours) {
            // TODO: Gestión de tours del sistema
            return true;
        }
        if (id == R.id.nav_chat) {
            // TODO: Monitoreo de chats
            return true;
        }
        if (id == R.id.nav_pagos) {
            // TODO: Gestión de pagos del sistema
            return true;
        }
        if (id == R.id.nav_perfil) {
            // TODO: Perfil del superadmin
            return true;
        }
        return false;
    };
}
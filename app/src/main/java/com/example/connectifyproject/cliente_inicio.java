package com.example.connectifyproject;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.connectifyproject.databinding.AdminPlaceholderViewBinding;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Actividad principal para Cliente
 * Pantalla de inicio donde el cliente puede ver tours disponibles y gestionar sus reservas
 */
public class cliente_inicio extends AppCompatActivity {
    
    private AdminPlaceholderViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminPlaceholderViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

        binding.topAppBar.setTitle("Connectify - Cliente");
        binding.tvPlaceholder.setText("¡Bienvenido! Explora tours increíbles en Perú");

        // Configurar navegación inferior específica para cliente
        binding.bottomNav.setOnItemSelectedListener(navListener);
        binding.bottomNav.setSelectedItemId(R.id.nav_dashboard);
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) return true;
        if (id == R.id.nav_tours) {
            // TODO: Navegar a tours disponibles para el cliente
            return true;
        }
        if (id == R.id.nav_chat) {
            // TODO: Chat con guías/soporte
            return true;
        }
        if (id == R.id.nav_pagos) {
            // TODO: Historial de pagos y facturas
            return true;
        }
        if (id == R.id.nav_perfil) {
            // TODO: Perfil del cliente
            return true;
        }
        return false;
    };
}
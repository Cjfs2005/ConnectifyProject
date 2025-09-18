package com.example.connectifyproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Actividad principal para Cliente
 * Pantalla de inicio donde el cliente puede ver tours disponibles y gestionar sus reservas
 */
public class cliente_inicio extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_inicio);
        
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_dashboard);
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
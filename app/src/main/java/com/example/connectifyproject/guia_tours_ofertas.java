package com.example.connectifyproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Actividad principal para Guía
 * Pantalla donde el guía puede gestionar sus tours y ofertas
 */
public class guia_tours_ofertas extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guia_tours_ofertas);
        
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_dashboard);
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
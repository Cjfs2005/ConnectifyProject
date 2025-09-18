package com.example.connectifyproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Actividad principal para SuperAdmin
 * Contiene navegación entre gestión de usuarios, solicitudes y logs
 */
public class sa_users_view extends AppCompatActivity {
    
    @Override 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sa_users_view);
        
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
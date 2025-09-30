package com.example.connectifyproject;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_sa);
        if (host == null) {
            throw new IllegalStateException("Falta nav_host_sa en activity_main.xml");
        }

        NavController navController = host.getNavController();
        BottomNavigationView bottom = findViewById(R.id.bottom_nav_sa);
        if (bottom == null) {
            throw new IllegalStateException("Falta bottom_nav_sa en activity_main.xml");
        }

        // Enlaza BottomNav con el NavController
        NavigationUI.setupWithNavController(bottom, navController);

        // ✅ Fuerza los colores del BottomNavigationView usando tu selector:
        //    - Activo:   @color/brand_purple_dark (#420B58)
        //    - Inactivo: #8A8A8A
        bottom.setItemIconTintList(
                ContextCompat.getColorStateList(this, R.color.sa_bottom_nav_tint));
        bottom.setItemTextColor(
                ContextCompat.getColorStateList(this, R.color.sa_bottom_nav_tint));

        // (Opcional) Si no quieres indicador "pill" activo de M3, puedes desactivarlo:
        // bottom.setItemActiveIndicatorEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_sa);
        return (host != null && host.getNavController().navigateUp())
                || super.onSupportNavigateUp();
    }
}

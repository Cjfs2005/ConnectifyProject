package com.example.connectifyproject;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
        NavigationUI.setupWithNavController(bottom, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_sa);
        return host != null && host.getNavController().navigateUp() || super.onSupportNavigateUp();
    }
}

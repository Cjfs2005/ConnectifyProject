package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class cliente_tours extends AppCompatActivity implements cliente_fragment_menu.OnMenuItemSelectedListener {

    private ImageButton btnNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_tours);

        initViews();
        setupMenuFragment();
        setupClickListeners();
    }

    private void initViews() {
        btnNotifications = findViewById(R.id.btn_notifications);
    }

    private void setupMenuFragment() {
        cliente_fragment_menu menuFragment = new cliente_fragment_menu();
        menuFragment.setOnMenuItemSelectedListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_fragment_container, menuFragment)
                .commit();
                
        // Seleccionar "Tours" por defecto
        menuFragment.setSelectedItem(R.id.nav_tours);
    }

    private void setupClickListeners() {
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_tours");
            startActivity(intent);
        });
    }

    @Override
    public boolean onMenuItemSelected(int itemId) {
        if (itemId == R.id.nav_inicio) {
            Intent intent = new Intent(this, cliente_inicio.class);
            startActivity(intent);
            finish();
            return true;
        } else if (itemId == R.id.nav_reservas) {
            Intent intent = new Intent(this, cliente_reservas.class);
            startActivity(intent);
            finish();
            return true;
        } else if (itemId == R.id.nav_tours) {
            // Ya estamos en tours
            return true;
        } else if (itemId == R.id.nav_chat) {
            Intent intent = new Intent(this, cliente_chat_list.class);
            startActivity(intent);
            finish();
            return true;
        } else if (itemId == R.id.nav_perfil) {
            Intent intent = new Intent(this, cliente_perfil.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }
}
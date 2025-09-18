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

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Tours" esté seleccionado cuando regresamos a esta actividad
        cliente_fragment_menu menuFragment = (cliente_fragment_menu) getSupportFragmentManager()
                .findFragmentById(R.id.menu_fragment_container);
        if (menuFragment != null) {
            menuFragment.setSelectedItem(R.id.nav_tours);
        }
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
                .commitNow();
                
        // Seleccionar "Tours" por defecto - ahora el fragment está listo
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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_reservas) {
            Intent intent = new Intent(this, cliente_reservas.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_tours) {
            // Ya estamos en tours
            return true;
        } else if (itemId == R.id.nav_chat) {
            Intent intent = new Intent(this, cliente_chat_list.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_perfil) {
            Intent intent = new Intent(this, cliente_perfil.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
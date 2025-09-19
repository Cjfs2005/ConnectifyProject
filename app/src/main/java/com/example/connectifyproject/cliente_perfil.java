package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class cliente_perfil extends AppCompatActivity implements cliente_fragment_menu.OnMenuItemSelectedListener {

    private ImageButton btnNotifications;
    private TextView tvEditProfile;
    private TextView tvProfileInitials;
    private TextView tvUserName;
    private LinearLayout layoutPaymentMethods;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutPermissions;
    private LinearLayout layoutLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_perfil);

        initViews();
        setupMenuFragment();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Perfil" esté seleccionado cuando regresamos a esta actividad
        cliente_fragment_menu menuFragment = (cliente_fragment_menu) getSupportFragmentManager()
                .findFragmentById(R.id.menu_fragment_container);
        if (menuFragment != null) {
            menuFragment.setSelectedItem(R.id.nav_perfil);
        }
    }

    private void initViews() {
        btnNotifications = findViewById(R.id.btn_notifications);
        tvEditProfile = findViewById(R.id.tv_edit_profile);
        tvProfileInitials = findViewById(R.id.tv_profile_initials);
        tvUserName = findViewById(R.id.tv_user_name);
        layoutPaymentMethods = findViewById(R.id.layout_payment_methods);
        layoutChangePassword = findViewById(R.id.layout_change_password);
        layoutPermissions = findViewById(R.id.layout_permissions);
        layoutLogout = findViewById(R.id.layout_logout);
        
        // Configurar las iniciales del usuario
        setupUserInitials();
    }

    private void setupMenuFragment() {
        cliente_fragment_menu menuFragment = new cliente_fragment_menu();
        menuFragment.setOnMenuItemSelectedListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_fragment_container, menuFragment)
                .commitNow();
                
        // Seleccionar "Perfil" por defecto - ahora el fragment está listo
        menuFragment.setSelectedItem(R.id.nav_perfil);
    }

    private void setupClickListeners() {
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_perfil");
            startActivity(intent);
        });

        tvEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_editar_perfil.class);
            startActivity(intent);
        });

        layoutPaymentMethods.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_metodos_pago.class);
            startActivity(intent);
        });

        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_cambiar_contrasenia.class);
            startActivity(intent);
        });

        layoutPermissions.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_permisos.class);
            startActivity(intent);
        });

        layoutLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, auth_login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
            Intent intent = new Intent(this, cliente_tours.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_chat) {
            Intent intent = new Intent(this, cliente_chat_list.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_perfil) {
            // Ya estamos en perfil
            return true;
        }
        return false;
    }
    
    private void setupUserInitials() {
        // Obtener el nombre del usuario desde el TextView
        String userName = tvUserName.getText().toString();
        String initials = getInitials(userName);
        tvProfileInitials.setText(initials);
    }
    
    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "??";
        }
        
        String[] nameParts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        
        // Tomar máximo 2 iniciales
        int count = Math.min(nameParts.length, 2);
        for (int i = 0; i < count; i++) {
            if (nameParts[i].length() > 0) {
                initials.append(nameParts[i].charAt(0));
            }
        }
        
        return initials.toString().toUpperCase();
    }
}
package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.connectifyproject.models.Cliente_User;

public class cliente_perfil extends AppCompatActivity {

    private ImageButton btnNotifications;
    private MaterialButton btnEditarPerfil;
    private ImageView ivProfilePhoto;
    private TextView tvUserName;
    private LinearLayout layoutPaymentMethods;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutPermissions;
    private LinearLayout layoutLogout;
    private BottomNavigationView bottomNavigation;
    
    // Modelo de datos del usuario
    private Cliente_User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_perfil);

        initViews();
        loadUserData();
        setupBottomNavigation();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Perfil" esté seleccionado cuando regresamos a esta actividad
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_perfil);
        }
    }

    private void initViews() {
        btnNotifications = findViewById(R.id.btn_notifications);
        btnEditarPerfil = findViewById(R.id.tv_edit_profile);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        tvUserName = findViewById(R.id.tv_user_name);
        layoutPaymentMethods = findViewById(R.id.layout_payment_methods);
        layoutChangePassword = findViewById(R.id.layout_change_password);
        layoutPermissions = findViewById(R.id.layout_permissions);
        layoutLogout = findViewById(R.id.layout_logout);
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
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
        });
        
        // Seleccionar "Perfil" por defecto
        bottomNavigation.setSelectedItemId(R.id.nav_perfil);
    }

    private void setupClickListeners() {
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_perfil");
            startActivity(intent);
        });

        btnEditarPerfil.setOnClickListener(v -> {
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

    private void loadUserData() {
        // TODO: En producción, esto vendría de una API o base de datos
        // Por ahora usamos datos hardcodeados a través del método estático
        currentUser = Cliente_User.crearUsuarioEjemplo();
        
        // Actualizar la interfaz con los datos cargados
        updateUserInterface();
    }

    private void updateUserInterface() {
        if (currentUser != null) {
            // Actualizar nombre de usuario
            tvUserName.setText(currentUser.getNombreCompleto());
            
            // TODO: En el futuro se pueden actualizar otros campos del perfil
            // que actualmente están hardcodeados en el XML
            // Por ejemplo:
            // - Cargar imagen de perfil si existe: currentUser.getFotoPerfilUrl()
            // - Mostrar datos dinámicos en lugar de los hardcodeados en XML
        }
    }

    // Método para obtener el usuario actual (útil para otras activities)
    public Cliente_User getCurrentUser() {
        return currentUser;
    }


}
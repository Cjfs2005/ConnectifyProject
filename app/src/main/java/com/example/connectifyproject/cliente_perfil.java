package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.connectifyproject.models.Cliente_User;
import com.example.connectifyproject.utils.Cliente_PreferencesManager;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class cliente_perfil extends AppCompatActivity {

    private static final String TAG = "ClientePerfil";
    
    private ImageButton btnNotifications;
    private MaterialButton btnEditarPerfil;
    private ImageView ivProfilePhoto;
    private TextView tvUserName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvDocument;
    private TextView tvBirthDate;
    private TextView tvAddress;
    private LinearLayout layoutPaymentMethods;
    private LinearLayout layoutPermissions;
    private LinearLayout layoutLogout;
    private BottomNavigationView bottomNavigation;
    private Cliente_PreferencesManager preferencesManager;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Cliente_User currentUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        preferencesManager = new Cliente_PreferencesManager(this);
        
        if (currentUser == null) {
            redirectToLogin();
            return;
        }
        
        initViews();
        setupBottomNavigation();
        setupClickListeners();
        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Perfil" esté seleccionado cuando regresamos a esta actividad
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_perfil);
        }
        // Recargar datos por si se editaron
        loadUserData();
    }

    private void initViews() {
        btnNotifications = findViewById(R.id.btn_notifications);
        btnEditarPerfil = findViewById(R.id.tv_edit_profile);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        tvUserName = findViewById(R.id.tv_user_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvDocument = findViewById(R.id.tv_document);
        tvBirthDate = findViewById(R.id.tv_birth_date);
        tvAddress = findViewById(R.id.tv_address);
        layoutPaymentMethods = findViewById(R.id.layout_payment_methods);
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

        layoutPermissions.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_permisos.class);
            startActivity(intent);
        });

        layoutLogout.setOnClickListener(v -> {
            // Cerrar sesión de Firebase Auth
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> {
                        // Ir al SplashActivity que redirigirá al login
                        redirectToLogin();
                    });
        });
    }

    private void loadUserData() {
        if (currentUser == null) {
            redirectToLogin();
            return;
        }
        
        db.collection("usuarios")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(this::updateUserInterface)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos del usuario", e);
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInterface(DocumentSnapshot document) {
        if (!document.exists()) {
            Log.w(TAG, "Documento de usuario no existe");
            return;
        }
        
        try {
            // Cargar datos del documento
            String nombreCompleto = document.getString("nombreCompleto");
            String email = document.getString("email");
            String telefono = document.getString("telefono");
            String codigoPais = document.getString("codigoPais");
            String tipoDocumento = document.getString("tipoDocumento");
            String numeroDocumento = document.getString("numeroDocumento");
            String fechaNacimiento = document.getString("fechaNacimiento");
            String domicilio = document.getString("domicilio");
            String photoUrl = document.getString("photoUrl");
            
            // Actualizar UI con los datos
            if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                tvUserName.setText(nombreCompleto);
            }
            
            if (email != null && !email.isEmpty()) {
                tvEmail.setText(email);
            }
            
            if (telefono != null && !telefono.isEmpty()) {
                String telefonoCompleto = (codigoPais != null ? codigoPais + " " : "") + telefono;
                tvPhone.setText(telefonoCompleto);
            }
            
            if (tipoDocumento != null && numeroDocumento != null) {
                tvDocument.setText(tipoDocumento + ": " + numeroDocumento);
            }
            
            if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                tvBirthDate.setText(fechaNacimiento);
            }
            
            // Manejar domicilio opcional
            if (domicilio != null && !domicilio.isEmpty()) {
                tvAddress.setText(domicilio);
                tvAddress.setVisibility(View.VISIBLE);
            } else {
                tvAddress.setText("No especificado");
                tvAddress.setVisibility(View.VISIBLE);
            }
            
            // Cargar foto de perfil
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person_24)
                        .error(R.drawable.ic_person_24)
                        .into(ivProfilePhoto);
            } else {
                Glide.with(this)
                        .load(R.drawable.ic_person_24)
                        .circleCrop()
                        .into(ivProfilePhoto);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error al procesar datos del usuario", e);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
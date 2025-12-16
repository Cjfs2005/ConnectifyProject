package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.adapters.AdminCompanyPhotosAdapter;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.example.connectifyproject.utils.AuthConstants;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class admin_perfil extends AppCompatActivity {

    private static final String TAG = "AdminPerfil";
    
    // Views
    private ImageView ivProfilePhoto, btnNotifications;
    private TextView tvUserName, tvDocumentType, tvDocument, tvEmail, tvCci;
    private TextView tvCompanyName, tvCompanyDescription, tvCompanyLocation, tvCompanyPhone, tvCompanyEmail;
    private MaterialButton btnEditProfile, btnEditCompany;
    private RecyclerView rvCompanyPhotos;
    
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    
    // Adapters
    private AdminCompanyPhotosAdapter photosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_perfil_view);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupClickListeners();
        setupBottomNavigation();
        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando regrese de editar
        loadUserData();
    }

    private void initViews() {
        // Profile views
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        btnNotifications = findViewById(R.id.btn_notifications);
        tvUserName = findViewById(R.id.tv_user_name);
        tvDocumentType = findViewById(R.id.tv_document_type);
        tvDocument = findViewById(R.id.tv_document);
        tvEmail = findViewById(R.id.tv_email);
        tvCci = findViewById(R.id.tv_cci);
        
        // Company views
        tvCompanyName = findViewById(R.id.tv_company_name);
        tvCompanyDescription = findViewById(R.id.tv_company_description);
        tvCompanyLocation = findViewById(R.id.tv_company_location);
        tvCompanyPhone = findViewById(R.id.tv_company_phone);
        tvCompanyEmail = findViewById(R.id.tv_company_email);
        rvCompanyPhotos = findViewById(R.id.rv_company_photos);
        
        // Buttons
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnEditCompany = findViewById(R.id.btn_edit_company);
        
        // Setup RecyclerView
        setupCompanyPhotosRecyclerView();
    }

    private void setupCompanyPhotosRecyclerView() {
        rvCompanyPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photosAdapter = new AdminCompanyPhotosAdapter(this);
        rvCompanyPhotos.setAdapter(photosAdapter);
    }

    private void setupClickListeners() {
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, admin_notificaciones.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, admin_editar_perfil.class);
            startActivity(intent);
        });

        btnEditCompany.setOnClickListener(v -> {
            Intent intent = new Intent(this, admin_editar_empresa.class);
            startActivity(intent);
        });

        findViewById(R.id.layout_permissions).setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.layout_payment_methods).setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_metodos_pago.class);
            startActivity(intent);
        });

        findViewById(R.id.layout_logout).setOnClickListener(v -> logout());
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("perfil");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }

    private void loadUserData() {
        if (currentUser == null) return;

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUIWithUserData(documentSnapshot);
                    } else {
                        Log.e(TAG, "Documento de usuario no encontrado");
                        Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos de usuario", e);
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUIWithUserData(DocumentSnapshot document) {
        try {
            // Personal Information
            String nombreCompleto = document.getString(AuthConstants.FIELD_NOMBRE_COMPLETO);
            String tipoDocumento = document.getString(AuthConstants.FIELD_TIPO_DOCUMENTO);
            String numeroDocumento = document.getString(AuthConstants.FIELD_NUMERO_DOCUMENTO);
            String email = document.getString(AuthConstants.FIELD_EMAIL);
            String photoUrl = document.getString(AuthConstants.FIELD_PHOTO_URL);
            String cci = document.getString("cci");

            // Company Information
            String nombreEmpresa = document.getString(AuthConstants.FIELD_NOMBRE_EMPRESA);
            String descripcionEmpresa = document.getString(AuthConstants.FIELD_DESCRIPCION_EMPRESA);
            String ubicacionEmpresa = document.getString(AuthConstants.FIELD_UBICACION_EMPRESA);
            String telefonoEmpresa = document.getString(AuthConstants.FIELD_TELEFONO_EMPRESA);
            String correoEmpresa = document.getString(AuthConstants.FIELD_CORREO_EMPRESA);
            List<String> fotosEmpresa = (List<String>) document.get(AuthConstants.FIELD_FOTOS_EMPRESA);

            // Update UI
            if (nombreCompleto != null) tvUserName.setText(nombreCompleto);
            if (tipoDocumento != null) tvDocumentType.setText(tipoDocumento);
            if (numeroDocumento != null) tvDocument.setText(numeroDocumento);
            if (email != null) tvEmail.setText(email);
            if (cci != null && !cci.isEmpty()) tvCci.setText(cci);

            // Company data
            if (nombreEmpresa != null) tvCompanyName.setText(nombreEmpresa);
            if (descripcionEmpresa != null) tvCompanyDescription.setText(descripcionEmpresa);
            if (ubicacionEmpresa != null) tvCompanyLocation.setText(ubicacionEmpresa);
            if (telefonoEmpresa != null) tvCompanyPhone.setText(telefonoEmpresa);
            if (correoEmpresa != null) tvCompanyEmail.setText(correoEmpresa);

            // Load profile photo
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_account_circle_24)
                        .error(R.drawable.ic_account_circle_24)
                        .into(ivProfilePhoto);
            }

            // Load company photos
            if (fotosEmpresa != null && !fotosEmpresa.isEmpty()) {
                photosAdapter.updatePhotos(fotosEmpresa);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar UI con datos de usuario", e);
        }
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

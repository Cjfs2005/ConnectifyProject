package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class guia_perfil extends AppCompatActivity {

    private static final String TAG = "GuiaPerfil";
    
    private ImageButton btnNotifications;
    private MaterialButton btnEditarPerfil;
    private ImageView ivProfilePhoto;
    private TextView tvUserName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvDocumentType;
    private TextView tvDocument;
    private TextView tvBirthDate;
    private TextView tvAddress;
    private TextView tvCci;
    private TextView tvYape;
    private LinearLayout languagesContainer;
    private MaterialButton btnAddLanguage;
    private LinearLayout layoutPermissions;
    private LinearLayout layoutLogout;
    private BottomNavigationView bottomNavigation;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guia_activity_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        
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
        tvDocumentType = findViewById(R.id.tv_document_type);
        tvDocument = findViewById(R.id.tv_document);
        tvBirthDate = findViewById(R.id.tv_birth_date);
        tvAddress = findViewById(R.id.tv_address);
        tvCci = findViewById(R.id.tv_cci);
        tvYape = findViewById(R.id.tv_yape);
        languagesContainer = findViewById(R.id.languages_container);
        btnAddLanguage = findViewById(R.id.btn_add_language);
        layoutPermissions = findViewById(R.id.layout_permissions);
        layoutLogout = findViewById(R.id.layout_logout);
        bottomNavigation = findViewById(R.id.bottom_nav);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_historial) {
                Intent intent = new Intent(this, guia_historial.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_ofertas) {
                Intent intent = new Intent(this, guia_tours_ofertas.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_tours) {
                Intent intent = new Intent(this, guia_assigned_tours.class);
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
            Intent intent = new Intent(this, guia_notificaciones.class);
            intent.putExtra("origin_activity", "guia_perfil");
            startActivity(intent);
        });

        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_editar_perfil.class);
            startActivity(intent);
        });

        btnAddLanguage.setOnClickListener(v -> showAddLanguageDialog());

        layoutPermissions.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_permisos.class);
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
                    Log.e(TAG, "Error al cargar datos del guía", e);
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInterface(DocumentSnapshot document) {
        if (!document.exists()) {
            Log.w(TAG, "Documento de guía no existe");
            return;
        }
        
        try {
            // Cargar datos del documento
            String nombreCompleto = document.getString("nombresApellidos");
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
                tvDocumentType.setText(tipoDocumento);
                tvDocument.setText(numeroDocumento);
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

            // Métodos de pago
            String cci = document.getString("cci");
            String numeroYape = document.getString("numeroYape");
            
            if (cci != null && !cci.isEmpty()) {
                tvCci.setText(cci);
            } else {
                tvCci.setText("No especificado");
            }
            
            if (numeroYape != null && !numeroYape.isEmpty()) {
                tvYape.setText(numeroYape);
            } else {
                tvYape.setText("No especificado");
            }
            
            // Idiomas
            List<String> idiomas = (List<String>) document.get("idiomas");
            displayLanguages(idiomas);

            // Cargar foto de perfil
            loadProfilePhoto(photoUrl);

        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar interfaz", e);
        }
    }

    private void loadProfilePhoto(String firestorePhotoUrl) {
        // Cargar foto usando la URL de Firestore con fallback a Firebase Auth
        String photoUrlToLoad = firestorePhotoUrl;
        
        // Si no hay URL de Firestore válida, usar Firebase Auth como fallback
        if (photoUrlToLoad == null || photoUrlToLoad.isEmpty() || photoUrlToLoad.equals("null")) {
            photoUrlToLoad = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null;
        }
        
        if (photoUrlToLoad != null && !photoUrlToLoad.isEmpty()) {
            Glide.with(this)
                    .load(photoUrlToLoad)
                    .placeholder(R.drawable.ic_account_circle_24)
                    .error(R.drawable.ic_account_circle_24)
                    .into(ivProfilePhoto);
        } else {
            // Si no hay foto disponible, usar imagen por defecto
            ivProfilePhoto.setImageResource(R.drawable.ic_account_circle_24);
        }
    }

    private void displayLanguages(List<String> idiomas) {
        // Limpiar el container de idiomas
        languagesContainer.removeAllViews();
        
        if (idiomas != null && !idiomas.isEmpty()) {
            for (String idioma : idiomas) {
                addLanguageView(idioma);
            }
        } else {
            // Mostrar mensaje cuando no hay idiomas
            TextView noLanguagesText = new TextView(this);
            noLanguagesText.setText("No hay idiomas registrados");
            noLanguagesText.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            noLanguagesText.setPadding(16, 8, 16, 8);
            languagesContainer.addView(noLanguagesText);
        }
    }

    private void addLanguageView(String language) {
        // Crear una vista para cada idioma con botón de eliminar
        LinearLayout languageLayout = new LinearLayout(this);
        languageLayout.setOrientation(LinearLayout.HORIZONTAL);
        languageLayout.setPadding(16, 8, 16, 8);
        languageLayout.setGravity(Gravity.CENTER_VERTICAL);
        
        // Texto del idioma
        TextView languageText = new TextView(this);
        languageText.setText(language);
        languageText.setTextSize(16);
        languageText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        
        // Botón para eliminar
        MaterialButton removeButton = new MaterialButton(this);
        removeButton.setIcon(getResources().getDrawable(R.drawable.ic_delete, null));
        removeButton.setIconTint(getResources().getColorStateList(android.R.color.white, null));
        removeButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark, null));
        removeButton.setCornerRadius(12);
        removeButton.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        removeButton.setIconPadding(0);
        removeButton.setText("");
        removeButton.setGravity(Gravity.CENTER);
        removeButton.setInsetTop(0);
        removeButton.setInsetBottom(0);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(96, 96);
        buttonParams.setMargins(16, 0, 0, 0);
        removeButton.setLayoutParams(buttonParams);
        removeButton.setOnClickListener(v -> removeLanguage(language));
        
        languageLayout.addView(languageText);
        languageLayout.addView(removeButton);
        languagesContainer.addView(languageLayout);
    }

    private void removeLanguage(String language) {
        if (currentUser == null) return;
        
        // Obtener la lista actual de idiomas
        db.collection("usuarios")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    List<String> currentLanguages = (List<String>) document.get("idiomas");
                    if (currentLanguages != null) {
                        currentLanguages.remove(language);
                        
                        // Actualizar en Firebase
                        db.collection("usuarios")
                                .document(currentUser.getUid())
                                .update("idiomas", currentLanguages)
                                .addOnSuccessListener(aVoid -> {
                                    displayLanguages(currentLanguages);
                                    Toast.makeText(this, "Idioma eliminado", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al eliminar idioma", e);
                                    Toast.makeText(this, "Error al eliminar idioma", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener idiomas", e);
                    Toast.makeText(this, "Error al cargar idiomas", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddLanguageDialog() {
        // Lista de idiomas igual que en el registro de guía
        String[] languages = {
            "Español", "Inglés", "Francés", "Alemán", "Italiano", "Chino", "Japonés"
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Idioma");
        builder.setItems(languages, (dialog, which) -> {
            String selectedLanguage = languages[which];
            addLanguageToFirebase(selectedLanguage);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void addLanguageToFirebase(String newLanguage) {
        if (currentUser == null) return;
        
        // Obtener la lista actual de idiomas
        db.collection("usuarios")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    List<String> currentLanguages = (List<String>) document.get("idiomas");
                    if (currentLanguages == null) {
                        currentLanguages = new ArrayList<>();
                    }
                    
                    // Verificar si el idioma ya existe
                    if (!currentLanguages.contains(newLanguage)) {
                        currentLanguages.add(newLanguage);
                        final List<String> updatedLanguages = new ArrayList<>(currentLanguages);
                        
                        // Actualizar en Firebase
                        db.collection("usuarios")
                                .document(currentUser.getUid())
                                .update("idiomas", updatedLanguages)
                                .addOnSuccessListener(aVoid -> {
                                    displayLanguages(updatedLanguages);
                                    Toast.makeText(this, "Idioma agregado correctamente", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al agregar idioma", e);
                                    Toast.makeText(this, "Error al agregar idioma", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Este idioma ya está agregado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener idiomas", e);
                    Toast.makeText(this, "Error al cargar idiomas", Toast.LENGTH_SHORT).show();
                });
    }



    private void redirectToLogin() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
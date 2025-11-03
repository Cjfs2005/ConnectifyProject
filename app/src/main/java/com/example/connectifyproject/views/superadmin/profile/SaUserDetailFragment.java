package com.example.connectifyproject.views.superadmin.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.R;
import com.example.connectifyproject.model.Role;
import com.example.connectifyproject.model.User;
import com.example.connectifyproject.utils.AuthConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaUserDetailFragment extends Fragment {

    private static final String TAG = "SaUserDetailFragment";

    private ImageView ivProfilePhoto;
    private TextView tvTitle;
    private LinearLayout layoutAdminMessage, layoutUserFields;
    
    private TextInputEditText etRegDate, etRegTime, etFullName, etDocType, etDocNumber,
            etBirth, etEmail, etPhone, etAddress, etLanguages;
    
    private MaterialButton btnToggleStatus;
    private ImageButton btnBack;

    private User user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sa_user_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bind de vistas
        btnBack = root.findViewById(R.id.btnBack);
        tvTitle = root.findViewById(R.id.tvTitle);
        ivProfilePhoto = root.findViewById(R.id.ivProfilePhoto);
        
        layoutAdminMessage = root.findViewById(R.id.layoutAdminMessage);
        layoutUserFields = root.findViewById(R.id.layoutUserFields);
        
        etRegDate = root.findViewById(R.id.etRegDate);
        etRegTime = root.findViewById(R.id.etRegTime);
        etFullName = root.findViewById(R.id.etFullName);
        etDocType = root.findViewById(R.id.etDocType);
        etDocNumber = root.findViewById(R.id.etDocNumber);
        etBirth = root.findViewById(R.id.etBirth);
        etEmail = root.findViewById(R.id.etEmail);
        etPhone = root.findViewById(R.id.etPhone);
        etAddress = root.findViewById(R.id.etAddress);
        etLanguages = root.findViewById(R.id.etLanguages);
        
        btnToggleStatus = root.findViewById(R.id.btnToggleStatus);

        // Obtener usuario de los argumentos
        Bundle args = getArguments();
        if (args != null) {
            user = args.getParcelable("user");
        }

        if (user == null) {
            Snackbar.make(root, "Error: usuario no encontrado", Snackbar.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        // Botón atrás
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Configurar según el rol
        setupByRole();
        
        // Cargar datos adicionales desde Firebase
        loadAdditionalData();
        
        // Cargar foto de perfil
        loadProfilePhoto();
        
        // Botón para cambiar estado
        btnToggleStatus.setOnClickListener(v -> toggleUserStatus());
    }

    private void setupByRole() {
        if (user.getRole() == Role.ADMIN) {
            // Mostrar mensaje de "próximamente" para administradores
            tvTitle.setText("PERFIL DE ADMINISTRADOR");
            layoutAdminMessage.setVisibility(View.VISIBLE);
            layoutUserFields.setVisibility(View.GONE);
        } else {
            // Mostrar campos completos para guías y clientes
            layoutAdminMessage.setVisibility(View.GONE);
            layoutUserFields.setVisibility(View.VISIBLE);
            
            if (user.getRole() == Role.GUIDE) {
                tvTitle.setText("PERFIL DE GUÍA");
                // Mostrar campo de idiomas solo para guías
                findViewById(R.id.tilLanguages).setVisibility(View.VISIBLE);
            } else {
                tvTitle.setText("PERFIL DE CLIENTE");
                // Ocultar campo de idiomas para clientes
                findViewById(R.id.tilLanguages).setVisibility(View.GONE);
            }
            
            // Mostrar datos básicos del usuario
            renderBasicData();
        }
    }

    private View findViewById(int id) {
        return requireView().findViewById(id);
    }

    private void renderBasicData() {
        // Nombre completo
        String fullName = (user.getName() != null ? user.getName() : "") + 
                         (user.getLastName() != null ? " " + user.getLastName() : "");
        etFullName.setText(fullName.trim());
        
        // Tipo de documento
        etDocType.setText(user.getDocType() != null ? user.getDocType() : "");
        
        // Número de documento
        etDocNumber.setText(user.getDni() != null ? user.getDni() : "");
        
        // Fecha de nacimiento
        etBirth.setText(user.getBirth() != null ? user.getBirth() : "");
        
        // Email
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        
        // Teléfono
        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        
        // Dirección
        etAddress.setText(user.getAddress() != null ? user.getAddress() : "");
        
        // Estado del botón
        updateToggleButton();
    }

    private void loadAdditionalData() {
        if (user.getUid() == null || user.getRole() == Role.ADMIN) return;
        
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Fecha de creación
                        Timestamp fechaCreacion = doc.getTimestamp(AuthConstants.FIELD_FECHA_CREACION);
                        if (fechaCreacion != null) {
                            Date date = fechaCreacion.toDate();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            
                            etRegDate.setText(dateFormat.format(date));
                            etRegTime.setText(timeFormat.format(date));
                        }
                        
                        // Idiomas (solo para guías)
                        if (user.getRole() == Role.GUIDE) {
                            List<String> idiomas = (List<String>) doc.get(AuthConstants.FIELD_IDIOMAS);
                            if (idiomas != null && !idiomas.isEmpty()) {
                                etLanguages.setText(String.join(", ", idiomas));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error loading additional data", e)
                );
    }

    private void loadProfilePhoto() {
        // Cargar foto usando la URI del usuario (puede ser gs:// o http://)
        Glide.with(this)
                .load(user.getPhotoUri())
                .circleCrop()
                .placeholder(R.drawable.ic_account_circle_24)
                .error(R.drawable.ic_account_circle_24)
                .into(ivProfilePhoto);
    }

    private void toggleUserStatus() {
        if (user == null || user.getUid() == null) return;
        
        boolean newStatus = !user.isEnabled();
        
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .update(AuthConstants.FIELD_HABILITADO, newStatus)
                .addOnSuccessListener(unused -> {
                    user.setEnabled(newStatus);
                    updateToggleButton();
                    String message = newStatus ? "✅ Usuario habilitado" : "🛑 Usuario deshabilitado";
                    Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user status", e);
                    Snackbar.make(requireView(), "Error al cambiar el estado", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void updateToggleButton() {
        if (user.isEnabled()) {
            btnToggleStatus.setText("Deshabilitar usuario");
        } else {
            btnToggleStatus.setText("Habilitar usuario");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        ivProfilePhoto = null;
        tvTitle = null;
        layoutAdminMessage = null;
        layoutUserFields = null;
        etRegDate = null;
        etRegTime = null;
        etFullName = null;
        etDocType = null;
        etDocNumber = null;
        etBirth = null;
        etEmail = null;
        etPhone = null;
        etAddress = null;
        etLanguages = null;
        btnToggleStatus = null;
        btnBack = null;
        user = null;
        db = null;
        storage = null;
    }

    private static String nvl(String s) { 
        return s == null ? "" : s; 
    }
}

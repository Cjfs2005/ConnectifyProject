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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.R;
import com.example.connectifyproject.adapters.CompanyPhotosAdapter;
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
    private LinearLayout layoutAdminMessage, layoutUserFields, layoutAdminFields;
    
    private TextInputEditText etRegDate, etRegTime, etFullName, etDocType, etDocNumber,
            etBirth, etEmail, etPhone, etAddress, etLanguages;
    
    // Campos de administrador
    private TextInputEditText etCompanyName, etCompanyDescription, etCompanyLocation,
            etCompanyEmail, etCompanyPhone, etRating;
    private RecyclerView rvCompanyPhotos;
    private CompanyPhotosAdapter companyPhotosAdapter;
    
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
        layoutAdminFields = root.findViewById(R.id.layoutAdminFields);
        
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
        
        // Campos de administrador
        etCompanyName = root.findViewById(R.id.etCompanyName);
        etCompanyDescription = root.findViewById(R.id.etCompanyDescription);
        etCompanyLocation = root.findViewById(R.id.etCompanyLocation);
        etCompanyEmail = root.findViewById(R.id.etCompanyEmail);
        etCompanyPhone = root.findViewById(R.id.etCompanyPhone);
        etRating = root.findViewById(R.id.etRating);
        rvCompanyPhotos = root.findViewById(R.id.rvCompanyPhotos);
        
        // Setup adapter para fotos
        companyPhotosAdapter = new CompanyPhotosAdapter();
        rvCompanyPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvCompanyPhotos.setAdapter(companyPhotosAdapter);
        
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
            // Mostrar campos completos para administradores
            tvTitle.setText("PERFIL DE ADMINISTRADOR");
            layoutAdminMessage.setVisibility(View.GONE);
            layoutUserFields.setVisibility(View.VISIBLE);
            layoutAdminFields.setVisibility(View.VISIBLE);
            
            // Ocultar idiomas para admin
            findViewById(R.id.tilLanguages).setVisibility(View.GONE);
            
            // Mostrar datos básicos del administrador
            renderBasicData();
        } else {
            // Mostrar campos completos para guías y clientes
            layoutAdminMessage.setVisibility(View.GONE);
            layoutUserFields.setVisibility(View.VISIBLE);
            layoutAdminFields.setVisibility(View.GONE);
            
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
        if (user.getUid() == null) return;
        
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
                        
                        // Datos específicos según rol
                        if (user.getRole() == Role.GUIDE) {
                            // Idiomas (solo para guías)
                            List<String> idiomas = (List<String>) doc.get(AuthConstants.FIELD_IDIOMAS);
                            if (idiomas != null && !idiomas.isEmpty()) {
                                etLanguages.setText(String.join(", ", idiomas));
                            }
                        } else if (user.getRole() == Role.ADMIN) {
                            // Datos de empresa (solo para administradores)
                            loadAdminData(doc);
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error loading additional data", e)
                );
    }

    private void loadAdminData(DocumentSnapshot doc) {
        // Nombre de empresa
        String nombreEmpresa = doc.getString(AuthConstants.FIELD_NOMBRE_EMPRESA);
        if (nombreEmpresa != null) {
            etCompanyName.setText(nombreEmpresa);
        }
        
        // Descripción de empresa
        String descripcion = doc.getString(AuthConstants.FIELD_DESCRIPCION_EMPRESA);
        if (descripcion != null) {
            etCompanyDescription.setText(descripcion);
        }
        
        // Ubicación de empresa
        String ubicacion = doc.getString(AuthConstants.FIELD_UBICACION_EMPRESA);
        if (ubicacion != null) {
            etCompanyLocation.setText(ubicacion);
        }
        
        // Correo de empresa
        String correoEmpresa = doc.getString(AuthConstants.FIELD_CORREO_EMPRESA);
        if (correoEmpresa != null) {
            etCompanyEmail.setText(correoEmpresa);
        }
        
        // Teléfono de empresa
        String telefonoEmpresa = doc.getString(AuthConstants.FIELD_TELEFONO_EMPRESA);
        if (telefonoEmpresa != null) {
            etCompanyPhone.setText(telefonoEmpresa);
        }
        
        // Calcular y mostrar calificación promedio
        Long sumaResenias = doc.getLong(AuthConstants.FIELD_SUMA_RESENIAS);
        Long numeroResenias = doc.getLong(AuthConstants.FIELD_NUMERO_RESENIAS);
        
        if (numeroResenias != null && numeroResenias > 0 && sumaResenias != null) {
            double promedio = (double) sumaResenias / numeroResenias;
            String ratingText = String.format(Locale.getDefault(), "%.1f ⭐ (%d reseñas)", promedio, numeroResenias);
            etRating.setText(ratingText);
        } else {
            etRating.setText("Sin reseñas");
        }
        
        // Fotos de empresa
        List<String> fotosEmpresa = (List<String>) doc.get(AuthConstants.FIELD_FOTOS_EMPRESA);
        if (fotosEmpresa != null && !fotosEmpresa.isEmpty()) {
            companyPhotosAdapter.setPhotos(fotosEmpresa);
        }
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
        layoutAdminFields = null;
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
        etCompanyName = null;
        etCompanyDescription = null;
        etCompanyLocation = null;
        etCompanyEmail = null;
        etCompanyPhone = null;
        etRating = null;
        rvCompanyPhotos = null;
        companyPhotosAdapter = null;
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

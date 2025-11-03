package com.example.connectifyproject.views.superadmin.users;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.utils.AuthConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SaCreateAdminFragment extends Fragment {

    private static final String TAG = "SaCreateAdminFragment";

    private TextInputEditText etEmail, etNombreEmpresa;
    private MaterialButton btnGuardar;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sa_create_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Views
        etEmail = view.findViewById(R.id.etEmail);
        etNombreEmpresa = view.findViewById(R.id.etNombreEmpresa);
        btnGuardar = view.findViewById(R.id.btnGuardar);

        // Botón atrás
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Botón guardar
        btnGuardar.setOnClickListener(v -> createAdmin());
    }

    private void createAdmin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String nombreEmpresa = etNombreEmpresa.getText() != null ? etNombreEmpresa.getText().toString().trim() : "";

        // Validar email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar nombre empresa
        if (TextUtils.isEmpty(nombreEmpresa)) {
            Toast.makeText(requireContext(), "Ingresa el nombre de la empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Creando...");

        // Crear documento pre-registro en Firestore
        // Usamos el email como ID del documento (pero será reemplazado con UID cuando se autentique)
        // Por ahora guardamos en una subcolección o con un flag especial
        String preRegistroId = email.replace(".", "_").replace("@", "_at_");
        
        Map<String, Object> adminData = new HashMap<>();
        adminData.put(AuthConstants.FIELD_EMAIL, email);
        adminData.put(AuthConstants.FIELD_NOMBRE_EMPRESA, nombreEmpresa);
        adminData.put(AuthConstants.FIELD_ROL, AuthConstants.ROLE_ADMIN);
        adminData.put(AuthConstants.FIELD_PERFIL_COMPLETO, false);
        adminData.put(AuthConstants.FIELD_HABILITADO, false); // Se habilitará al completar registro
        adminData.put(AuthConstants.FIELD_FECHA_CREACION, com.google.firebase.Timestamp.now());
        adminData.put(AuthConstants.FIELD_SUMA_RESENIAS, 0);
        adminData.put(AuthConstants.FIELD_NUMERO_RESENIAS, 0);

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(preRegistroId)
                .set(adminData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Administrador pre-registrado. Debe iniciar sesión con: " + email, Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(this).popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear pre-registro", e);
                    Toast.makeText(requireContext(), "Error al crear administrador", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Crear Administrador");
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        etEmail = null;
        etNombreEmpresa = null;
        btnGuardar = null;
        db = null;
    }
}

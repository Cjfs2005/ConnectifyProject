package com.example.connectifyproject.views.superadmin.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.R;
import com.example.connectifyproject.SplashActivity;
import com.example.connectifyproject.databinding.FragmentSaProfileBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

public class SaProfileFragment extends Fragment {

    private FragmentSaProfileBinding binding;
    private FirebaseAuth mAuth;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSaProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        // Asegura morado oscuro (por si el tema no aplica)
        v.findViewById(R.id.toolbar).setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.brand_purple_dark)
        );

        // Cargar datos del usuario
        loadUserData();

        // Botón de notificaciones
        binding.btnNotifications.setOnClickListener(btn ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.saNotificationsFragment)
        );

        // Permisos
        binding.layoutPermissions.setOnClickListener(x -> {
            // TODO: Navegar a Activity de permisos cuando esté creado
            Intent intent = new Intent(requireContext(), SaPermissionsActivity.class);
            startActivity(intent);
        });

        // Cerrar sesión funcional
        binding.layoutLogout.setOnClickListener(x -> {
            AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener(task -> {
                        // Ir al SplashActivity que redirigirá al login
                        Intent intent = new Intent(requireContext(), SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    });
        });
    }

    private void loadUserData() {
        // Obtener usuario actual de Firebase
        if (mAuth.getCurrentUser() != null) {
            String displayName = mAuth.getCurrentUser().getDisplayName();
            
            // Si no hay nombre, usar "SuperAdmin"
            if (displayName == null || displayName.isEmpty()) {
                binding.tvUserName.setText("SuperAdmin");
            } else {
                binding.tvUserName.setText(displayName);
            }

            // Cargar foto de perfil
            if (mAuth.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(mAuth.getCurrentUser().getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_person_24)
                        .error(R.drawable.ic_person_24)
                        .into(binding.ivProfilePhoto);
            }
        } else {
            binding.tvUserName.setText("SuperAdmin");
        }
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}

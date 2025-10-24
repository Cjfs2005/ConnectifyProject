package com.example.connectifyproject.views.superadmin.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaProfileBinding;
import com.google.android.material.snackbar.Snackbar;

public class SaProfileFragment extends Fragment {

    private FragmentSaProfileBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSaProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        // Asegura morado oscuro (por si el tema no aplica)
        v.findViewById(R.id.toolbar).setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.brand_purple_dark)
        );

        binding.btnNotifications.setOnClickListener(btn ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.saNotificationsFragment)
        );

        binding.tvEditProfile.setOnClickListener(x ->
                NavHostFragment.findNavController(this).navigate(R.id.saEditProfileFragment)
        );

        // (Opcional) Clicks adicionales
        binding.layoutChangePassword.setOnClickListener(x ->
                Snackbar.make(binding.getRoot(), "Cambiar contraseña (demo)", Snackbar.LENGTH_SHORT).show()
        );
        binding.layoutPaymentMethods.setOnClickListener(x ->
                Snackbar.make(binding.getRoot(), "Métodos de pago (demo)", Snackbar.LENGTH_SHORT).show()
        );
        binding.layoutLogout.setOnClickListener(x ->
                Snackbar.make(binding.getRoot(), "Cerrando sesión… (demo)", Snackbar.LENGTH_SHORT).show()
        );
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}

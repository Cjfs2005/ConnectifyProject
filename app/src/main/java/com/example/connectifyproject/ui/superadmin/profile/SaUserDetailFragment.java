package com.example.connectifyproject.ui.superadmin.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaUserDetailBinding;

public class SaUserDetailFragment extends Fragment {

    private FragmentSaUserDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSaUserDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botón atrás: vuelve a la lista EXACTA que dejaste (con filtros y scroll)
        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Lee argumentos enviados desde la lista
        Bundle args = getArguments();
        if (args != null) {
            String name    = args.getString("name", "");
            String dni     = args.getString("dni", "");
            String company = args.getString("company", "");
            String role    = args.getString("role", "");

            // Intento simple de separar nombre y apellido
            String firstName = name;
            String lastName  = "";
            int idx = name.lastIndexOf(' ');
            if (idx > 0) {
                firstName = name.substring(0, idx).trim();
                lastName  = name.substring(idx + 1).trim();
            }

            // Asigna datos básicos
            binding.etFirstName.setText(firstName);
            binding.etLastName.setText(lastName);
            binding.etDocType.setText(dni.isEmpty() ? "" : "DNI");
            binding.etDocNumber.setText(dni);

            // Campos opcionales (si luego los mandas como args, se verán)
            binding.etBirth.setText(args.getString("birth", ""));      // ej: 08/17/1996
            binding.etEmail.setText(args.getString("email", ""));      // ej: correo
            binding.etPhone.setText(args.getString("phone", ""));      // ej: 999...
            binding.etAddress.setText(args.getString("address", ""));  // ej: calle...

            // Foto de perfil (si envías "photoUri" como string)
            String photoUri = args.getString("photoUri", "");
            if (!photoUri.isEmpty()) {
                binding.imgProfile.setImageURI(Uri.parse(photoUri));
            } else {
                binding.imgProfile.setImageResource(R.drawable.ic_account_circle_24);
            }
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

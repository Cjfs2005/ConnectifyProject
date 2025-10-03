package com.example.connectifyproject.views.superadmin.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaEditProfileBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaEditProfileFragment extends Fragment {

    private FragmentSaEditProfileBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSaEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        v.findViewById(R.id.toolbar).setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.brand_purple_dark)
        );

        binding.toolbar.setNavigationOnClickListener(x -> NavHostFragment.findNavController(this).navigateUp());

        // Dropdown documento
        String[] tipos = {"DNI", "CE", "Pasaporte"};
        binding.spinnerTipoDocumento.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, tipos));

        // DatePicker para fecha de nacimiento (usando MaterialDatePicker)
        binding.tilFechaNacimiento.setEndIconOnClickListener(x -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecciona fecha de nacimiento")
                    .build();
            picker.addOnPositiveButtonClickListener(time -> {
                String f = new SimpleDateFormat("dd/MM/yyyy", new Locale("es")).format(new Date(time));
                binding.etFechaNacimiento.setText(f);
            });
            picker.show(getParentFragmentManager(), "date");
        });

        // Guardar
        binding.btnGuardar.setOnClickListener(x -> {
            Snackbar.make(binding.getRoot(), "Perfil actualizado", Snackbar.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}

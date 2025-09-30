package com.example.connectifyproject.views.superadmin.users;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaCreateAdminBinding;

import java.util.Calendar;

public class SaCreateAdminFragment extends Fragment {

    private FragmentSaCreateAdminBinding binding;
    private ActivityResultLauncher<String> pickImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSaCreateAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botón atrás
        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Dropdown Tipo de documento
        String[] docTypes = getResources().getStringArray(R.array.sa_doc_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, docTypes);
        binding.actvDocType.setAdapter(adapter);
        binding.actvDocType.setOnItemClickListener((parent, v, position, id) -> {
            binding.tilDocNumber.setVisibility(View.VISIBLE);
        });

        // DatePicker
        View.OnClickListener openPicker = v -> showDatePicker();
        binding.etBirth.setOnClickListener(openPicker);
        binding.tilBirth.setEndIconOnClickListener(openPicker);

        // Picker de imagen (galería) + mostrar preview SOLO cuando se elige
        pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), (Uri uri) -> {
            if (uri != null) {
                binding.imgPreview.setImageURI(uri);
                binding.imgPreview.setVisibility(View.VISIBLE);
            }
        });
        binding.btnPickPhoto.setOnClickListener(v ->
                pickImage.launch("image/*")
        );

        // Guardar: aquí validarías/enviarías; por ahora solo feedback y VOLVER a la lista
        binding.btnSave.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Administrador guardado", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack(); // regresar a Gestión (lista)
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(requireContext(),
                (DatePicker datePicker, int y, int m, int d) -> {
                    String mm = String.format("%02d", m + 1);
                    String dd = String.format("%02d", d);
                    binding.etBirth.setText(mm + "/" + dd + "/" + y);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

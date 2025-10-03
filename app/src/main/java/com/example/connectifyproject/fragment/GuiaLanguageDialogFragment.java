package com.example.connectifyproject.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.connectifyproject.databinding.GuiaDialogLanguageEditBinding;
import com.example.connectifyproject.model.GuiaLanguage;

import java.util.List;

public class GuiaLanguageDialogFragment extends DialogFragment {
    private GuiaDialogLanguageEditBinding binding;
    private List<GuiaLanguage> languages;
    private int position = -1;
    private OnLanguageUpdatedListener listener;

    public interface OnLanguageUpdatedListener {
        void onLanguagesUpdated(List<GuiaLanguage> updatedLanguages);
    }

    public static GuiaLanguageDialogFragment newInstance(List<GuiaLanguage> languages, OnLanguageUpdatedListener listener) {
        GuiaLanguageDialogFragment fragment = new GuiaLanguageDialogFragment();
        fragment.languages = languages;
        fragment.listener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = GuiaDialogLanguageEditBinding.inflate(getLayoutInflater());

        String[] langArray = {"Español", "Inglés", "Italiano", "Francés", "Alemán", "Chino", "Japonés"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, langArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.languageSpinner.setAdapter(adapter);

        if (position >= 0) {
            GuiaLanguage lang = languages.get(position);
            for (int i = 0; i < langArray.length; i++) {
                if (langArray[i].equals(lang.getName())) {
                    binding.languageSpinner.setSelection(i);
                    break;
                }
            }
        }

        binding.saveBtn.setOnClickListener(v -> saveLanguage());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .setTitle(position >= 0 ? "Editar Idioma" : "Agregar Idioma")
                .setNegativeButton("Cancelar", null)
                .create();
    }

    private void saveLanguage() {
        String selected = binding.languageSpinner.getSelectedItem().toString();
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona un idioma", Toast.LENGTH_SHORT).show();
            return;
        }

        if (position < 0) {
            boolean exists = false;
            for (GuiaLanguage l : languages) {
                if (l.getName().equals(selected)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                Toast.makeText(requireContext(), "Idioma ya agregado", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        GuiaLanguage newLang = new GuiaLanguage(selected);
        if (position >= 0) {
            languages.set(position, newLang);
        } else {
            languages.add(newLang);
        }
        listener.onLanguagesUpdated(languages);
        dismiss();
    }

    public void setPosition(int pos) {
        this.position = pos;
    }
}
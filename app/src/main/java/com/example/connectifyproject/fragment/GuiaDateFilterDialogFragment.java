package com.example.connectifyproject.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.connectifyproject.databinding.GuiaDialogDateFilterBinding;

import java.util.Calendar;
import java.util.Locale;

public class GuiaDateFilterDialogFragment extends DialogFragment {
    private FilterListener listener;
    private GuiaDialogDateFilterBinding binding;

    public interface FilterListener {
        void onApplyFilters(String dateFrom, String dateTo, String amount, String duration, String languages);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FilterListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement FilterListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = GuiaDialogDateFilterBinding.inflate(LayoutInflater.from(getContext()));
        binding.dateFrom.setFocusable(false);
        binding.dateFrom.setClickable(true);
        binding.dateFrom.setOnClickListener(v -> showDatePicker(binding.dateFrom));
        binding.dateTo.setFocusable(false);
        binding.dateTo.setClickable(true);
        binding.dateTo.setOnClickListener(v -> showDatePicker(binding.dateTo));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(binding.getRoot())
                .setPositiveButton("Aplicar Filtros", (dialog, id) -> {
                    String dateFrom = binding.dateFrom.getText().toString();
                    String dateTo = binding.dateTo.getText().toString();
                    listener.onApplyFilters(dateFrom, dateTo, null, null, null);
                })
                .setNegativeButton("Reiniciar Filtros", (dialog, id) -> {
                    listener.onApplyFilters(null, null, null, null, null);
                });

        return builder.create();
    }

    private void showDatePicker(com.google.android.material.textfield.TextInputEditText dateEdit) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%02d-%02d-%d", day, month + 1, year);
            dateEdit.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
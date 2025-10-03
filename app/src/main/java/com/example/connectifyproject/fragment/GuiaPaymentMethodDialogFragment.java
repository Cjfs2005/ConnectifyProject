package com.example.connectifyproject.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.connectifyproject.databinding.GuiaDialogPaymentMethodEditBinding;
import com.example.connectifyproject.model.GuiaPaymentMethod;

import java.util.List;

public class GuiaPaymentMethodDialogFragment extends DialogFragment {
    private GuiaDialogPaymentMethodEditBinding binding;
    private List<GuiaPaymentMethod> paymentMethods;
    private int position = -1;
    private OnPaymentMethodUpdatedListener listener;

    public interface OnPaymentMethodUpdatedListener {
        void onPaymentMethodsUpdated(List<GuiaPaymentMethod> updatedPaymentMethods);
    }

    public static GuiaPaymentMethodDialogFragment newInstance(List<GuiaPaymentMethod> paymentMethods, int position, OnPaymentMethodUpdatedListener listener) {
        GuiaPaymentMethodDialogFragment fragment = new GuiaPaymentMethodDialogFragment();
        fragment.paymentMethods = paymentMethods;
        fragment.position = position;
        fragment.listener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = GuiaDialogPaymentMethodEditBinding.inflate(getLayoutInflater());

        RadioGroup radioGroup = binding.paymentTypeRadioGroup;
        RadioButton creditCard = binding.creditCardRadio;
        RadioButton debitCard = binding.debitCardRadio;
        RadioButton bankAccount = binding.bankAccountRadio;
        RadioButton yape = binding.yapeRadio;

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            binding.numberEdit.setVisibility(View.VISIBLE);
            binding.holderEdit.setVisibility(View.VISIBLE);
            binding.expiryEdit.setVisibility(checkedId == creditCard.getId() || checkedId == debitCard.getId() ? View.VISIBLE : View.GONE);
            binding.bankCodeEdit.setVisibility(checkedId == bankAccount.getId() ? View.VISIBLE : View.GONE);
        });

        if (position >= 0) {
            GuiaPaymentMethod method = paymentMethods.get(position);
            if ("Tarjeta Crédito".equals(method.getType())) creditCard.setChecked(true);
            else if ("Tarjeta Débito".equals(method.getType())) debitCard.setChecked(true);
            else if ("Cuenta Bancaria".equals(method.getType())) bankAccount.setChecked(true);
            else if ("Yape".equals(method.getType())) yape.setChecked(true);
            binding.numberEdit.setText(method.getNumber());
            binding.holderEdit.setText(method.getHolder());
            if (method.getExpiry() != null) binding.expiryEdit.setText(method.getExpiry());
            if (method.getBankCode() != null) binding.bankCodeEdit.setText(method.getBankCode());
        }

        binding.saveBtn.setOnClickListener(v -> savePaymentMethod());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .setTitle(position >= 0 ? "Editar Método de Pago" : "Agregar Método de Pago")
                .setNegativeButton("Cancelar", null)
                .create();
    }

    private void savePaymentMethod() {
        RadioGroup radioGroup = binding.paymentTypeRadioGroup;
        RadioButton selectedRadio = binding.getRoot().findViewById(radioGroup.getCheckedRadioButtonId());
        if (selectedRadio == null) {
            Toast.makeText(requireContext(), "Selecciona un tipo de pago", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = selectedRadio.getText().toString();
        String number = binding.numberEdit.getText().toString().trim();
        String holder = binding.holderEdit.getText().toString().trim();
        String expiry = binding.expiryEdit.getVisibility() == View.VISIBLE ? binding.expiryEdit.getText().toString().trim() : null;
        String bankCode = binding.bankCodeEdit.getVisibility() == View.VISIBLE ? binding.bankCodeEdit.getText().toString().trim() : null;

        if (number.isEmpty() || holder.isEmpty()) {
            Toast.makeText(requireContext(), "Completa los campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        GuiaPaymentMethod newMethod = new GuiaPaymentMethod(type, number, holder, expiry, bankCode);
        if (position >= 0) {
            paymentMethods.set(position, newMethod);
        } else {
            paymentMethods.add(newMethod);
        }
        listener.onPaymentMethodsUpdated(paymentMethods);
        dismiss();
    }
}
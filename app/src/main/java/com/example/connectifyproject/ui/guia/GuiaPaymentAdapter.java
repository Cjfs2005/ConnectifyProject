package com.example.connectifyproject.ui.guia;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.GuiaItemPaymentBinding;
import com.example.connectifyproject.model.GuiaPayment;

import java.util.List;
import java.util.Locale;

public class GuiaPaymentAdapter extends RecyclerView.Adapter<GuiaPaymentAdapter.ViewHolder> {

    private List<GuiaPayment> payments;
    private final Context context;

    public GuiaPaymentAdapter(Context context, List<GuiaPayment> payments) {
        this.context = context;
        this.payments = payments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GuiaItemPaymentBinding binding = GuiaItemPaymentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuiaPayment payment = payments.get(position);

        // Nombre del tour
        holder.binding.idText.setText("Tour: " + payment.getTourName());

        // Monto (ajusta el símbolo si quieres "S/")
        holder.binding.amountText.setText(
                String.format(Locale.getDefault(), "S/ %.2f", payment.getAmount())
        );

        // Fecha + hora
        holder.binding.dateText.setText(payment.getDateTime());

        // Estado (Pendiente / Realizado)
        String status = payment.getStatus();
        if (status == null) status = "";
        holder.binding.statusText.setText(status);

        if ("Pendiente".equalsIgnoreCase(status)) {
            holder.binding.statusText.setBackgroundColor(Color.parseColor("#FFF3E0")); // naranja suave
        } else if ("Realizado".equalsIgnoreCase(status)) {
            holder.binding.statusText.setBackgroundColor(Color.parseColor("#E8F5E9")); // verde suave
        } else {
            holder.binding.statusText.setBackgroundColor(Color.TRANSPARENT);
        }

        // Nombre de la empresa del admin
        String companyName = payment.getCompanyName();
        if (companyName != null && !companyName.isEmpty()) {
            holder.binding.methodText.setText("Empresa: " + companyName);
        } else {
            holder.binding.methodText.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return payments != null ? payments.size() : 0;
    }

    public void updateList(List<GuiaPayment> newList) {
        this.payments = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final GuiaItemPaymentBinding binding;

        ViewHolder(GuiaItemPaymentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

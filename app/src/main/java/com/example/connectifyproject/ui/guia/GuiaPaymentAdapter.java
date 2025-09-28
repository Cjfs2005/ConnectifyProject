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

public class GuiaPaymentAdapter extends RecyclerView.Adapter<GuiaPaymentAdapter.ViewHolder> {
    private List<GuiaPayment> payments;
    private Context context;

    public GuiaPaymentAdapter(Context context, List<GuiaPayment> payments) {
        this.context = context;
        this.payments = payments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GuiaItemPaymentBinding binding = GuiaItemPaymentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuiaPayment payment = payments.get(position);
        holder.binding.idText.setText("ID: " + payment.getId());
        holder.binding.amountText.setText("$" + payment.getAmount());
        holder.binding.dateText.setText(payment.getDate());
        holder.binding.statusText.setText(payment.getStatus());
        holder.binding.methodText.setText(payment.getMethod());

        // Color status
        if ("Pendiente".equals(payment.getStatus())) {
            holder.binding.statusText.setBackgroundColor(Color.parseColor("#FFF3E0"));  // Orange
        } else if ("Realizado".equals(payment.getStatus())) {
            holder.binding.statusText.setBackgroundColor(Color.parseColor("#E8F5E8"));  // Green
        }
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    public void updateList(List<GuiaPayment> newList) {
        payments = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        GuiaItemPaymentBinding binding;

        ViewHolder(GuiaItemPaymentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
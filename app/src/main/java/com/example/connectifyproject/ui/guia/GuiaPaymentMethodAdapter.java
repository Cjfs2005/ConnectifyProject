package com.example.connectifyproject.ui.guia;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.GuiaItemPaymentMethodBinding;
import com.example.connectifyproject.fragment.GuiaPaymentMethodDialogFragment;
import com.example.connectifyproject.model.GuiaPaymentMethod;

import java.util.List;

public class GuiaPaymentMethodAdapter extends RecyclerView.Adapter<GuiaPaymentMethodAdapter.ViewHolder> {
    private List<GuiaPaymentMethod> paymentMethods;
    private Activity activity;
    private OnPaymentMethodUpdatedListener listener;

    public interface OnPaymentMethodUpdatedListener {
        void onPaymentMethodsUpdated(List<GuiaPaymentMethod> updatedPaymentMethods);
    }

    public GuiaPaymentMethodAdapter(Activity activity, List<GuiaPaymentMethod> paymentMethods, OnPaymentMethodUpdatedListener listener) {
        this.activity = activity;
        this.paymentMethods = paymentMethods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GuiaItemPaymentMethodBinding binding = GuiaItemPaymentMethodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuiaPaymentMethod method = paymentMethods.get(position);
        holder.binding.methodText.setText(method.getType() + ": " + method.getNumber());
        holder.binding.holderText.setText("Titular: " + method.getHolder());
        if (method.getExpiry() != null) {
            holder.binding.expiryText.setText("Vencimiento: " + method.getExpiry());
            holder.binding.expiryText.setVisibility(View.VISIBLE);
        } else {
            holder.binding.expiryText.setVisibility(View.GONE);
        }
        if (method.getBankCode() != null) {
            holder.binding.bankCodeText.setText("Código: " + method.getBankCode());
            holder.binding.bankCodeText.setVisibility(View.VISIBLE);
        } else {
            holder.binding.bankCodeText.setVisibility(View.GONE);
        }

        holder.binding.editBtn.setOnClickListener(v -> {
            GuiaPaymentMethodDialogFragment dialog = GuiaPaymentMethodDialogFragment.newInstance(paymentMethods, position, updated -> {
                paymentMethods = updated;
                listener.onPaymentMethodsUpdated(paymentMethods);
                notifyDataSetChanged();
            });
            dialog.show(((androidx.appcompat.app.AppCompatActivity) activity).getSupportFragmentManager(), "edit_payment");
        });

        holder.binding.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar")
                    .setMessage("¿Eliminar " + method.getType() + "?")
                    .setPositiveButton("Sí", (d, w) -> {
                        paymentMethods.remove(position);
                        listener.onPaymentMethodsUpdated(paymentMethods);
                        notifyItemRemoved(position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public void updateList(List<GuiaPaymentMethod> newList) {
        paymentMethods = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        GuiaItemPaymentMethodBinding binding;

        ViewHolder(GuiaItemPaymentMethodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
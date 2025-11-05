package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connectifyproject.R;
import com.example.connectifyproject.models.Cliente_PaymentMethod;
import java.util.List;

public class Cliente_PaymentMethodAdapter extends RecyclerView.Adapter<Cliente_PaymentMethodAdapter.ViewHolder> {

    private List<Cliente_PaymentMethod> paymentMethods;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position, Cliente_PaymentMethod paymentMethod);
    }

    public Cliente_PaymentMethodAdapter(List<Cliente_PaymentMethod> paymentMethods, OnItemClickListener listener) {
        this.paymentMethods = paymentMethods;
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cliente_item_payment_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cliente_PaymentMethod paymentMethod = null;
        
        try {
            paymentMethod = paymentMethods.get(position);
            
            if (paymentMethod == null) {
                return;
            }
            
            // Mostrar nombre de la tarjeta o número enmascarado
            String displayName = paymentMethod.getDisplayName();
            holder.tvCardName.setText(displayName != null ? displayName : "Tarjeta");
            
            String expiryFormatted = paymentMethod.getExpiryFormatted();
            holder.tvExpiryDate.setText(expiryFormatted != null ? expiryFormatted : "");
        } catch (Exception e) {
            // En caso de error, mostrar valores por defecto
            holder.tvCardName.setText("Tarjeta");
            holder.tvExpiryDate.setText("");
        }
        
        // Capturar paymentMethod en variable final para los listeners
        final Cliente_PaymentMethod finalPaymentMethod = paymentMethod;
        
        // Click en eliminar
        holder.ivDelete.setOnClickListener(v -> {
            if (itemClickListener != null && finalPaymentMethod != null) {
                itemClickListener.onDeleteClick(position, finalPaymentMethod);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }
    
    public void updateData(List<Cliente_PaymentMethod> newPaymentMethods) {
        try {
            this.paymentMethods = newPaymentMethods != null ? newPaymentMethods : new java.util.ArrayList<>();
            notifyDataSetChanged();
        } catch (Exception e) {
            // En caso de error, mantener la lista actual o crear una vacía
            if (this.paymentMethods == null) {
                this.paymentMethods = new java.util.ArrayList<>();
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardName;
        TextView tvExpiryDate;
        ImageView ivCardLogo;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardName = itemView.findViewById(R.id.tv_card_name);
            tvExpiryDate = itemView.findViewById(R.id.tv_expiry_date);
            ivCardLogo = itemView.findViewById(R.id.iv_card_logo);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
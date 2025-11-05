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
        void onSetDefaultClick(int position, Cliente_PaymentMethod paymentMethod);
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
        Cliente_PaymentMethod paymentMethod = paymentMethods.get(position);
        
        // Mostrar nombre de la tarjeta o número enmascarado
        holder.tvCardName.setText(paymentMethod.getDisplayName());
        holder.tvExpiryDate.setText(paymentMethod.getExpiryFormatted());
        
        // Mostrar badge de default
        if (paymentMethod.isDefault()) {
            holder.tvDefaultBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvDefaultBadge.setVisibility(View.GONE);
        }
        
        // Click en eliminar
        holder.ivDelete.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onDeleteClick(position, paymentMethod);
            }
        });
        
        // Click en el item completo para marcar como default
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null && !paymentMethod.isDefault()) {
                itemClickListener.onSetDefaultClick(position, paymentMethod);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }
    
    public void updateData(List<Cliente_PaymentMethod> newPaymentMethods) {
        this.paymentMethods = newPaymentMethods;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardName;
        TextView tvExpiryDate;
        TextView tvDefaultBadge;
        ImageView ivCardLogo;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardName = itemView.findViewById(R.id.tv_card_name);
            tvExpiryDate = itemView.findViewById(R.id.tv_expiry_date);
            tvDefaultBadge = itemView.findViewById(R.id.tv_default_badge);
            ivCardLogo = itemView.findViewById(R.id.iv_card_logo);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
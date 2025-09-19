package com.example.connectifyproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {

    private List<cliente_metodos_pago.PaymentMethod> paymentMethods;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public PaymentMethodAdapter(List<cliente_metodos_pago.PaymentMethod> paymentMethods, OnDeleteClickListener listener) {
        this.paymentMethods = paymentMethods;
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        cliente_metodos_pago.PaymentMethod paymentMethod = paymentMethods.get(position);
        
        holder.tvCardNumber.setText(paymentMethod.getCardNumber());
        holder.tvExpiryDate.setText(paymentMethod.getExpiryDate());
        
        holder.ivDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardNumber;
        TextView tvExpiryDate;
        ImageView ivCardLogo;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardNumber = itemView.findViewById(R.id.tv_card_number);
            tvExpiryDate = itemView.findViewById(R.id.tv_expiry_date);
            ivCardLogo = itemView.findViewById(R.id.iv_card_logo);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
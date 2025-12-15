package com.example.connectifyproject.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connectifyproject.R;
import com.example.connectifyproject.models.Cliente_PaymentMethod;
import java.util.List;

public class AdminPaymentMethodSelectionAdapter extends RecyclerView.Adapter<AdminPaymentMethodSelectionAdapter.ViewHolder> {
    
    private List<Cliente_PaymentMethod> paymentMethods;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onSelectClick(int position, Cliente_PaymentMethod paymentMethod);
    }

    public AdminPaymentMethodSelectionAdapter(List<Cliente_PaymentMethod> paymentMethods, OnItemClickListener listener) {
        this.paymentMethods = paymentMethods;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_payment_method_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cliente_PaymentMethod method = paymentMethods.get(position);
        
        // Card number
        holder.tvCardNumber.setText(method.getDisplayName());
        
        // Card type and brand
        String cardInfo = method.getCardBrand() + " - " + method.getCardType();
        holder.tvCardType.setText(cardInfo);
        
        // Expiry date
        holder.tvExpiryDate.setText("Vence: " + method.getExpiryDate());
        
        // Default badge
        if (method.isDefault()) {
            holder.tvDefaultBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvDefaultBadge.setVisibility(View.GONE);
        }
        
        // Selection indicator
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
            holder.ivSelected.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.ivSelected.setVisibility(View.GONE);
        }
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSelectClick(position, method);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCardNumber;
        TextView tvCardType;
        TextView tvExpiryDate;
        TextView tvDefaultBadge;
        ImageView ivSelected;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvCardNumber = itemView.findViewById(R.id.tv_card_number);
            tvCardType = itemView.findViewById(R.id.tv_card_type);
            tvExpiryDate = itemView.findViewById(R.id.tv_expiry_date);
            tvDefaultBadge = itemView.findViewById(R.id.tv_default_badge);
            ivSelected = itemView.findViewById(R.id.iv_selected);
        }
    }
}

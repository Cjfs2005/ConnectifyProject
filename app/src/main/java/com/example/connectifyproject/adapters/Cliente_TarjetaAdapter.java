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
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.List;

public class Cliente_TarjetaAdapter extends RecyclerView.Adapter<Cliente_TarjetaAdapter.TarjetaViewHolder> {

    private List<Cliente_PaymentMethod> tarjetas;
    private int selectedPosition = 0; // Primera tarjeta seleccionada por defecto
    private OnTarjetaSelectedListener listener;

    public interface OnTarjetaSelectedListener {
        void onTarjetaSelected(Cliente_PaymentMethod tarjeta, int position);
    }

    public Cliente_TarjetaAdapter(List<Cliente_PaymentMethod> tarjetas, OnTarjetaSelectedListener listener) {
        this.tarjetas = tarjetas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TarjetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cliente_item_tarjeta, parent, false);
        return new TarjetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarjetaViewHolder holder, int position) {
        Cliente_PaymentMethod tarjeta = tarjetas.get(position);
        
        holder.tvCardNumber.setText(tarjeta.getMaskedCardNumber());
        holder.tvExpiryDate.setText(tarjeta.getExpiryDate());
        
        // Configurar ícono según tipo de tarjeta
        int iconResource = getCardTypeIcon(tarjeta.getCardType());
        holder.ivCardType.setImageResource(iconResource);
        
        holder.rbCardSelected.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onTarjetaSelected(tarjeta, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tarjetas.size();
    }

    public Cliente_PaymentMethod getSelectedTarjeta() {
        if (selectedPosition >= 0 && selectedPosition < tarjetas.size()) {
            return tarjetas.get(selectedPosition);
        }
        return null;
    }

    private int getCardTypeIcon(String cardType) {
        if (cardType == null) return R.drawable.cliente_visa;
        
        switch (cardType.toUpperCase()) {
            case "VISA":
                return R.drawable.cliente_visa;
            case "MASTERCARD":
                return R.drawable.cliente_visa; // Usar visa como fallback
            case "AMERICAN_EXPRESS":
            case "AMEX":
                return R.drawable.cliente_visa; // Usar visa como fallback
            default:
                return R.drawable.cliente_visa;
        }
    }

    static class TarjetaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCardType;
        TextView tvCardNumber;
        TextView tvExpiryDate;
        MaterialRadioButton rbCardSelected;

        public TarjetaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCardType = itemView.findViewById(R.id.iv_card_type);
            tvCardNumber = itemView.findViewById(R.id.tv_card_number);
            tvExpiryDate = itemView.findViewById(R.id.tv_expiry_date);
            rbCardSelected = itemView.findViewById(R.id.rb_card_selected);
        }
    }
}
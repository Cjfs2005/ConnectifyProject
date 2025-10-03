package com.example.connectifyproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.List;

public class TarjetaAdapter extends RecyclerView.Adapter<TarjetaAdapter.TarjetaViewHolder> {

    public static class Tarjeta {
        private String cardNumber;
        private String expiryDate;
        private String cardType;
        private int iconResource;

        public Tarjeta(String cardNumber, String expiryDate, String cardType, int iconResource) {
            this.cardNumber = cardNumber;
            this.expiryDate = expiryDate;
            this.cardType = cardType;
            this.iconResource = iconResource;
        }

        // Getters
        public String getCardNumber() { return cardNumber; }
        public String getExpiryDate() { return expiryDate; }
        public String getCardType() { return cardType; }
        public int getIconResource() { return iconResource; }
    }

    private List<Tarjeta> tarjetas;
    private int selectedPosition = 0; // Primera tarjeta seleccionada por defecto
    private OnTarjetaSelectedListener listener;

    public interface OnTarjetaSelectedListener {
        void onTarjetaSelected(Tarjeta tarjeta, int position);
    }

    public TarjetaAdapter(List<Tarjeta> tarjetas, OnTarjetaSelectedListener listener) {
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
        Tarjeta tarjeta = tarjetas.get(position);
        
        holder.tvCardNumber.setText(tarjeta.getCardNumber());
        holder.tvExpiryDate.setText(tarjeta.getExpiryDate());
        holder.ivCardType.setImageResource(tarjeta.getIconResource());
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

    public Tarjeta getSelectedTarjeta() {
        if (selectedPosition >= 0 && selectedPosition < tarjetas.size()) {
            return tarjetas.get(selectedPosition);
        }
        return null;
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
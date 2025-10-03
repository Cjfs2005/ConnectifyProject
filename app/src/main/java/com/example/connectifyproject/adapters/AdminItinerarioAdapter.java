package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.models.Cliente_ItinerarioItem;

import java.util.List;

public class AdminItinerarioAdapter extends RecyclerView.Adapter<AdminItinerarioAdapter.ItinerarioViewHolder> {

    private List<Cliente_ItinerarioItem> itinerarioItems;
    private OnItinerarioItemClickListener listener;

    public interface OnItinerarioItemClickListener {
        void onItinerarioItemClick(Cliente_ItinerarioItem item);
    }

    public AdminItinerarioAdapter(List<Cliente_ItinerarioItem> itinerarioItems, OnItinerarioItemClickListener listener) {
        this.itinerarioItems = itinerarioItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItinerarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_itinerario, parent, false);
        return new ItinerarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItinerarioViewHolder holder, int position) {
        Cliente_ItinerarioItem item = itinerarioItems.get(position);
        
        holder.tvTiempo.setText(item.getVisitTime());
        holder.tvNombreLugar.setText(item.getPlaceName());
        holder.tvDescripcion.setText(item.getDescription());
        
        // Mostrar duración si está disponible
        if (item.getDuration() != null && !item.getDuration().isEmpty()) {
            holder.tvDuracion.setText("Duración: " + item.getDuration());
            holder.tvDuracion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDuracion.setVisibility(View.GONE);
        }

        // Ocultar línea conectora en el último item
        if (position == itinerarioItems.size() - 1) {
            holder.lineaConectora.setVisibility(View.GONE);
        } else {
            holder.lineaConectora.setVisibility(View.VISIBLE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItinerarioItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itinerarioItems != null ? itinerarioItems.size() : 0;
    }

    public void updateList(List<Cliente_ItinerarioItem> newItems) {
        this.itinerarioItems = newItems;
        notifyDataSetChanged();
    }

    public static class ItinerarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvTiempo, tvNombreLugar, tvDescripcion, tvDuracion;
        View lineaConectora;
        ImageView ivLocationIcon;

        public ItinerarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTiempo = itemView.findViewById(R.id.tvTiempo);
            tvNombreLugar = itemView.findViewById(R.id.tvNombreLugar);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvDuracion = itemView.findViewById(R.id.tvDuracion);
            lineaConectora = itemView.findViewById(R.id.lineaConectora);
            ivLocationIcon = itemView.findViewById(R.id.ivLocationIcon);
        }
    }
}
package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;

import java.util.List;
import java.util.Map;

public class AdminServiciosAdapter extends RecyclerView.Adapter<AdminServiciosAdapter.ServicioViewHolder> {

    private List<Map<String, Object>> servicios;

    public AdminServiciosAdapter(List<Map<String, Object>> servicios) {
        this.servicios = servicios;
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_servicio, parent, false);
        return new ServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        Map<String, Object> servicio = servicios.get(position);
        String nombre = (String) servicio.get("nombre");
        Object costoObj = servicio.get("precio");
        
        holder.tvServiceName.setText(nombre != null ? nombre : "");
        
        if (costoObj != null) {
            double costo = costoObj instanceof Long ? 
                ((Long) costoObj).doubleValue() : (Double) costoObj;
            holder.tvServiceCost.setText(String.format("S/ %.0f", costo));
        } else {
            holder.tvServiceCost.setText("S/ 0");
        }
    }

    @Override
    public int getItemCount() {
        return servicios != null ? servicios.size() : 0;
    }

    public void updateList(List<Map<String, Object>> newServicios) {
        this.servicios = newServicios;
        notifyDataSetChanged();
    }

    public static class ServicioViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        TextView tvServiceCost;
        ImageView ivServiceIcon;

        public ServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceCost = itemView.findViewById(R.id.tvServiceCost);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
        }
    }
}
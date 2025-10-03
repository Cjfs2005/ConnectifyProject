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

public class AdminServiciosAdapter extends RecyclerView.Adapter<AdminServiciosAdapter.ServicioViewHolder> {

    private List<String> servicios;

    public AdminServiciosAdapter(List<String> servicios) {
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
        String servicio = servicios.get(position);
        holder.tvServiceName.setText(servicio);
    }

    @Override
    public int getItemCount() {
        return servicios != null ? servicios.size() : 0;
    }

    public void updateList(List<String> newServicios) {
        this.servicios = newServicios;
        notifyDataSetChanged();
    }

    public static class ServicioViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        ImageView ivServiceIcon;

        public ServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
        }
    }
}
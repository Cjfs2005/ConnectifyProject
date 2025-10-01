package com.example.connectifyproject.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.ItemServiceSaleBinding;
import com.example.connectifyproject.models.ServiceSale;

import java.util.ArrayList;
import java.util.List;

public class ServiceSalesAdapter extends RecyclerView.Adapter<ServiceSalesAdapter.VH> {

    private final List<ServiceSale> items = new ArrayList<>();
    private int maxAmount = 1;

    // Colores específicos para cada servicio basados en el diseño de Figma
    private static final int[] SERVICE_COLORS = new int[]{
            R.color.service_paquetes,    // Rosa para paquetes de comida
            R.color.service_traslado,    // Morado para traslado al hotel
            R.color.service_fotografia,  // Morado oscuro para servicio de fotografía
            R.color.service_souvenir,    // Morado más oscuro para souvenir exclusivo
            R.color.service_transporte,  // Azul morado para transporte ecológico
            R.color.service_guia,        // Azul morado oscuro para guía bilingüe
            R.color.service_entradas,    // Azul oscuro para entradas museos
            R.color.service_propinas     // Azul muy oscuro para propinas
    };

    public void submitList(List<ServiceSale> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
            maxAmount = 1;
            for (ServiceSale s : items) {
                if (s.getAmount() > maxAmount) maxAmount = s.getAmount();
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceSaleBinding binding = ItemServiceSaleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ServiceSale item = items.get(position);
        Context context = holder.binding.getRoot().getContext();
        
        holder.binding.tvServiceName.setText(item.getServiceName());
        holder.binding.tvServiceAmount.setText("$" + item.getAmount());

        // Porcentaje 0..100 para la barra de progreso
        int percent = Math.max(0, Math.min(100, Math.round((item.getAmount() * 100f) / maxAmount)));
        
        // Configurar la barra de progreso
        holder.binding.progressBar.setProgress(percent);

        // Color específico basado en la posición del servicio
        int colorResId = SERVICE_COLORS[position % SERVICE_COLORS.length];
        int color = ContextCompat.getColor(context, colorResId);
        
        // Aplicar color personalizado a la barra de progreso
        holder.binding.progressBar.getProgressDrawable().setTint(color);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemServiceSaleBinding binding;
        VH(ItemServiceSaleBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
    }
}
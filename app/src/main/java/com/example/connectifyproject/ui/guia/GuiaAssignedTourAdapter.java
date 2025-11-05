package com.example.connectifyproject.ui.guia;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.GuiaItemAssignedTourBinding;
import com.example.connectifyproject.databinding.GuiaItemHeaderBinding;
import com.example.connectifyproject.guia_assigned_tour_detail;
import com.example.connectifyproject.guia_check_in;
import com.example.connectifyproject.guia_check_out;
import com.example.connectifyproject.guia_tour_map;
import com.example.connectifyproject.model.GuiaAssignedItem;
import com.example.connectifyproject.model.GuiaAssignedTour;

import java.util.ArrayList;
import java.util.List;

public class GuiaAssignedTourAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<GuiaAssignedItem> items;
    private Context context;

    public GuiaAssignedTourAdapter(Context context, List<GuiaAssignedItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GuiaAssignedItem.TYPE_HEADER) {
            GuiaItemHeaderBinding binding = GuiaItemHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new HeaderViewHolder(binding);
        } else {
            GuiaItemAssignedTourBinding binding = GuiaItemAssignedTourBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new AssignedTourViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GuiaAssignedItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).binding.headerText.setText(item.getHeader());
        } else if (holder instanceof AssignedTourViewHolder) {
            GuiaAssignedTour tour = item.getAssignedTour();
            AssignedTourViewHolder tourHolder = (AssignedTourViewHolder) holder;
            
            // ✅ USAR LA MISMA ESTRUCTURA QUE OFERTAS
            tourHolder.binding.empresaText.setText(tour.getEmpresa());
            tourHolder.binding.tourName.setText(tour.getName());
            tourHolder.binding.tourDuration.setText(tour.getDuration());
            tourHolder.binding.tourClients.setText(tour.getClients() + " personas");
            
            // Estado con color dinámico
            String estado = formatearEstado(tour.getStatus());
            tourHolder.binding.tourStatus.setText(estado);
            
            // Fechas - separar fecha y hora como en ofertas
            String[] fechaHora = tour.getInitio().split(" - ");
            if (fechaHora.length == 2) {
                tourHolder.binding.dateText.setText(fechaHora[0]);
                tourHolder.binding.tourStartTime.setText(fechaHora[1]);
            } else {
                tourHolder.binding.dateText.setText(tour.getDate() != null ? tour.getDate() : "Fecha no disponible");
                tourHolder.binding.tourStartTime.setText("Hora no disponible");
            }
            
            // ✅ MOSTRAR PAGO AL GUÍA (igual que en ofertas)
            tourHolder.binding.pagoGuiaText.setText("S/. " + (int) tour.getPagoGuia());

            boolean isEnCurso = tour.getStatus().equals("En Curso");
            tourHolder.binding.actionsLayout.setVisibility(isEnCurso ? View.VISIBLE : View.GONE);

            // Entire card click for details
            tourHolder.itemView.setOnClickListener(v -> startDetailIntent(tour));

            if (isEnCurso) {
                tourHolder.binding.mapIcon.setOnClickListener(v -> {
                    // Simular notificación de ubicación antes de abrir el mapa
                    if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
                        ((com.example.connectifyproject.guia_assigned_tours) context).simulateLocationReminderNotification("Plaza de Armas");
                    }
                    Intent intent = new Intent(context, guia_tour_map.class);
                    intent.putExtra("tour_name", tour.getName());
                    intent.putExtra("tour_status", tour.getStatus());
                    intent.putStringArrayListExtra("tour_itinerario", new ArrayList<>(tour.getItinerario()));
                    intent.putExtra("tour_clients", tour.getClients());
                    context.startActivity(intent);
                });
                
                tourHolder.binding.checkInIcon.setOnClickListener(v -> {
                    // Simular notificación de check-in antes de abrir la pantalla
                    if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
                        ((com.example.connectifyproject.guia_assigned_tours) context).simulateCheckInNotification(tour.getName());
                    }
                    context.startActivity(new Intent(context, guia_check_in.class));
                });
                
                tourHolder.binding.checkOutIcon.setOnClickListener(v -> {
                    // Simular notificación de check-out antes de abrir la pantalla  
                    if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
                        ((com.example.connectifyproject.guia_assigned_tours) context).simulateCheckOutNotification(tour.getName());
                    }
                    context.startActivity(new Intent(context, guia_check_out.class));
                });
                tourHolder.binding.detailsIcon.setOnClickListener(v -> startDetailIntent(tour));
            }
        }
    }

    private void startDetailIntent(GuiaAssignedTour tour) {
        Intent intent = new Intent(context, guia_assigned_tour_detail.class);
        intent.putExtra("tour_name", tour.getName());
        intent.putExtra("tour_empresa", tour.getEmpresa());
        intent.putExtra("tour_initio", tour.getInitio());
        intent.putExtra("tour_duration", tour.getDuration());
        intent.putExtra("tour_clients", tour.getClients());
        intent.putExtra("tour_status", tour.getStatus());
        intent.putExtra("tour_languages", tour.getLanguages());
        intent.putExtra("tour_services", tour.getServices());
        intent.putStringArrayListExtra("tour_itinerario", new ArrayList<>(tour.getItinerario()));
        context.startActivity(intent);
    }

    /**
     * Formatear estado para mostrar en UI
     */
    private String formatearEstado(String estado) {
        if (estado == null) return "PENDIENTE";
        
        switch (estado.toLowerCase()) {
            case "en curso":
                return "EN CURSO";
            case "pendiente":
                return "PENDIENTE";
            case "finalizado":
                return "FINALIZADO";
            case "cancelado":
                return "CANCELADO";
            default:
                return estado.toUpperCase();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    public void updateItems(List<GuiaAssignedItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        GuiaItemHeaderBinding binding;

        public HeaderViewHolder(GuiaItemHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class AssignedTourViewHolder extends RecyclerView.ViewHolder {
        GuiaItemAssignedTourBinding binding;

        public AssignedTourViewHolder(GuiaItemAssignedTourBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
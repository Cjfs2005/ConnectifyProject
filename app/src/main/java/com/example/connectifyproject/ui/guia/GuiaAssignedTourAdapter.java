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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            // ✅ TEXTO INTELIGENTE PARA PARTICIPANTES
            int numParticipantes = tour.getClients();
            if (numParticipantes == 0) {
                tourHolder.binding.tourClients.setText("Sin registros aún");
            } else if (numParticipantes == 1) {
                tourHolder.binding.tourClients.setText("1 persona");
            } else {
                tourHolder.binding.tourClients.setText(numParticipantes + " personas");
            }
            
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

            // ✅ SOLO MOSTRAR BOTÓN DE DETALLES para tours normales
            // Los tours prioritarios usan el banner superior
            tourHolder.binding.actionsLayout.setVisibility(View.GONE); // Ocultar todos los botones
            
            // Solo mostrar botón de detalles
            tourHolder.binding.detailsIcon.setVisibility(View.VISIBLE);
            tourHolder.binding.mapIcon.setVisibility(View.GONE);
            tourHolder.binding.checkInIcon.setVisibility(View.GONE);
            tourHolder.binding.checkOutIcon.setVisibility(View.GONE);

            // Entire card click for details
            tourHolder.itemView.setOnClickListener(v -> startDetailIntent(tour));
            
            // Solo botón de detalles funcional
            tourHolder.binding.detailsIcon.setOnClickListener(v -> startDetailIntent(tour));
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
     * ✅ LÓGICA INTELIGENTE: Mostrar botones solo para tours inminentes o en curso
     * - Tours "en_curso" o "En Curso": Siempre mostrar
     * - Tours programados: Solo si es mañana o dentro de 1 día
     * - Tours pasados o muy futuros: No mostrar
     */
    private boolean shouldShowActionButtons(GuiaAssignedTour tour) {
        String status = tour.getStatus();
        
        // ✅ Si el tour ya está en curso, siempre mostrar botones
        if (status != null && (status.equalsIgnoreCase("en curso") || 
                              status.equalsIgnoreCase("en_curso") ||
                              status.equalsIgnoreCase("en_progreso"))) {
            return true;
        }
        
        // ✅ Para tours programados, verificar si es mañana o dentro de 1 día
        if (status != null && status.equalsIgnoreCase("programado")) {
            try {
                // Extraer fecha del tour
                String fechaTour = tour.getDate(); // Formato: "06/11/2025"
                if (fechaTour == null || fechaTour.trim().isEmpty()) {
                    return false; // Sin fecha válida, no mostrar botones
                }
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date tourDate = dateFormat.parse(fechaTour.trim());
                
                if (tourDate == null) {
                    return false; // Fecha no válida
                }
                
                // Obtener fecha actual
                Calendar today = Calendar.getInstance();
                Calendar tourCalendar = Calendar.getInstance();
                tourCalendar.setTime(tourDate);
                
                // Calcular diferencia en días
                long diffInMillis = tourCalendar.getTimeInMillis() - today.getTimeInMillis();
                long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
                
                // ✅ Mostrar botones si el tour es hoy o mañana (0 o 1 día de diferencia)
                return diffInDays >= 0 && diffInDays <= 1;
                
            } catch (ParseException e) {
                // Si hay error parsing fecha, no mostrar botones por seguridad
                return false;
            }
        }
        
        // ✅ Para cualquier otro estado, no mostrar botones
        return false;
    }

    /**
     * Formatear estado para mostrar en UI
     */
    private String formatearEstado(String estado) {
        if (estado == null) return "PENDIENTE";
        
        switch (estado.toLowerCase()) {
            case "en curso":
            case "en_curso":
            case "en_progreso":
                return "EN CURSO";
            case "programado":
                return "PROGRAMADO";
            case "pendiente":
                return "PENDIENTE";
            case "confirmado":
                return "CONFIRMADO";
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
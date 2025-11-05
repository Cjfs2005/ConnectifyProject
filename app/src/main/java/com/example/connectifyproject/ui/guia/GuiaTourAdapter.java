package com.example.connectifyproject.ui.guia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.guia_tour_detail;
import com.example.connectifyproject.databinding.GuiaItemHeaderBinding;
import com.example.connectifyproject.databinding.GuiaItemTourBinding;
import com.example.connectifyproject.model.GuiaItem;
import com.example.connectifyproject.model.GuiaTour;

import java.util.List;

public class GuiaTourAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<GuiaItem> items;
    private Context context;

    public GuiaTourAdapter(Context context, List<GuiaItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GuiaItem.TYPE_HEADER) {
            GuiaItemHeaderBinding binding = GuiaItemHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new HeaderViewHolder(binding);
        } else {
            GuiaItemTourBinding binding = GuiaItemTourBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new TourViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GuiaItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).binding.headerText.setText(item.getHeader());
        } else if (holder instanceof TourViewHolder) {
            GuiaTour tour = item.getTour();
            TourViewHolder tourHolder = (TourViewHolder) holder;
            
            // Empresa badge
            tourHolder.binding.empresaText.setText(tour.getEmpresa());
            
            // Título del tour
            tourHolder.binding.tourName.setText(tour.getName());
            
            // PAGO AL GUÍA (no precio del tour)
            tourHolder.binding.tourPrice.setText("S/. " + (int)tour.getPrice());
            
            // Duración
            tourHolder.binding.tourDuration.setText(tour.getDuration());
            
            // Idiomas
            tourHolder.binding.tourLanguages.setText(tour.getLanguages());
            
            // Fecha 
            tourHolder.binding.dateText.setText(tour.getDate());
            
            // Hora de inicio y fin
            tourHolder.binding.tourStartTime.setText(tour.getStartTime());
            
            // Pago al guía (mismo valor que el precio mostrado arriba)
            tourHolder.binding.pagoGuiaText.setText("S/. " + (int)tour.getPrice());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, guia_tour_detail.class);
                intent.putExtra("tour_name", tour.getName());
                intent.putExtra("tour_location", tour.getLocation());
                intent.putExtra("tour_price", tour.getPrice()); // Este es el pagoGuia
                intent.putExtra("tour_duration", tour.getDuration());
                intent.putExtra("tour_languages", tour.getLanguages());
                intent.putExtra("tour_start_time", tour.getStartTime());
                intent.putExtra("tour_date", tour.getDate());
                intent.putExtra("tour_description", tour.getDescription());
                intent.putExtra("tour_benefits", tour.getBenefits());
                intent.putExtra("tour_schedule", tour.getSchedule());
                intent.putExtra("tour_meeting_point", tour.getMeetingPoint());
                intent.putExtra("tour_empresa", tour.getEmpresa());
                intent.putExtra("tour_itinerario", tour.getItinerario());
                intent.putExtra("tour_consideraciones", tour.getExperienciaMinima()); // Consideraciones
                intent.putExtra("tour_pago_guia", tour.getPrice()); // Pago al guía
                intent.putExtra("tour_servicios", tour.getBenefits()); // Servicios
                intent.putExtra("tour_firebase_id", tour.getFirebaseId());
                
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, 1001);
                    ((Activity) context).overridePendingTransition(R.anim.guia_slide_in, R.anim.guia_slide_out);
                }
            });
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

    public void updateItems(List<GuiaItem> newItems) {
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

    static class TourViewHolder extends RecyclerView.ViewHolder {
        GuiaItemTourBinding binding;

        public TourViewHolder(GuiaItemTourBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
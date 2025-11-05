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
            tourHolder.binding.tourName.setText(tour.getName());
            tourHolder.binding.tourPrice.setText("Pago: S/ " + tour.getPrice());
            tourHolder.binding.tourDuration.setText("Duración: " + tour.getDuration());
            tourHolder.binding.tourLanguages.setText("Idiomas: " + tour.getLanguages());
            tourHolder.binding.tourStartTime.setText("Inicio: " + tour.getStartTime() + " " + tour.getDate());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, guia_tour_detail.class); // Renombrado asumido
                intent.putExtra("tour_name", tour.getName());
                intent.putExtra("tour_location", tour.getLocation());
                intent.putExtra("tour_price", tour.getPrice());
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
                intent.putExtra("tour_experiencia_minima", tour.getExperienciaMinima());
                intent.putExtra("tour_puntualidad", tour.getPuntualidad());
                intent.putExtra("tour_transporte_incluido", tour.isTransporteIncluido());
                intent.putExtra("tour_almuerzo_incluido", tour.isAlmuerzoIncluido());
                intent.putExtra("tour_firebase_id", tour.getFirebaseId()); // FIREBASE ID
                
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, 1001); // REQUEST_CODE_TOUR_DETAIL
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
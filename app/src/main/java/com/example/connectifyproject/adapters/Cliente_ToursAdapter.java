package com.example.connectifyproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.connectifyproject.R;
import com.example.connectifyproject.cliente_tour_detalle;
import com.example.connectifyproject.models.Cliente_Tour;

import java.util.List;

public class Cliente_ToursAdapter extends RecyclerView.Adapter<Cliente_ToursAdapter.TourViewHolder> {

    private Context context;
    private List<Cliente_Tour> tours;

    public Cliente_ToursAdapter(Context context, List<Cliente_Tour> tours) {
        this.context = context;
        this.tours = tours;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_tour, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Cliente_Tour tour = tours.get(position);
        
        holder.tvTourTitle.setText(tour.getTitulo());
        holder.tvTourCompany.setText(tour.getCompanyName());
        holder.tvTourDuration.setText("Duración: " + tour.getDuracion());
        holder.tvTourDate.setText("Fecha: " + tour.getDate());
        holder.tvTourPrice.setText(String.format("S/%.2f", tour.getPrecio()));
        
        // Cargar imagen con Glide
        if (tour.getImageUrl() != null && !tour.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(tour.getImageUrl())
                    .placeholder(R.drawable.cliente_tour_lima)
                    .error(R.drawable.cliente_tour_lima)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(holder.ivTourImage);
        } else {
            holder.ivTourImage.setImageResource(R.drawable.cliente_tour_lima);
        }
        
        // Click listener para abrir detalles del tour
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, cliente_tour_detalle.class);
            intent.putExtra("tour_object", tour);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tours.size();
    }

    public void updateTours(List<Cliente_Tour> newTours) {
        this.tours = newTours;
        notifyDataSetChanged();
    }

    static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTourImage;
        TextView tvTourTitle, tvTourCompany, tvTourDuration, tvTourDate, tvTourPrice;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTourImage = itemView.findViewById(R.id.iv_tour_image);
            tvTourTitle = itemView.findViewById(R.id.tv_tour_title);
            tvTourCompany = itemView.findViewById(R.id.tv_tour_company);
            tvTourDuration = itemView.findViewById(R.id.tv_tour_duration);
            tvTourDate = itemView.findViewById(R.id.tv_tour_date);
            tvTourPrice = itemView.findViewById(R.id.tv_tour_price);
        }
    }
}
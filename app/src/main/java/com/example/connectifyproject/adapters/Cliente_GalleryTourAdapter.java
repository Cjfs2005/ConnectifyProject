package com.example.connectifyproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connectifyproject.R;
import com.example.connectifyproject.cliente_tour_detalle;
import com.example.connectifyproject.models.Cliente_Tour;
import java.util.List;

/**
 * Adapter unificado para mostrar tours en galerías
 * Usado en cliente_inicio y cliente_empresa_info
 * Basado en el diseño de cliente_item_tour_gallery.xml
 */
public class Cliente_GalleryTourAdapter extends RecyclerView.Adapter<Cliente_GalleryTourAdapter.TourViewHolder> {
    
    private Context context;
    private List<Cliente_Tour> tours;
    private OnTourClickListener listener;
    
    public interface OnTourClickListener {
        void onTourClick(Cliente_Tour tour);
    }
    
    public Cliente_GalleryTourAdapter(Context context, List<Cliente_Tour> tours) {
        this.context = context;
        this.tours = tours;
    }
    
    public void setOnTourClickListener(OnTourClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_tour_gallery, parent, false);
        return new TourViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Cliente_Tour tour = tours.get(position);
        
        // Configurar datos del tour
        holder.tvTourTitle.setText(tour.getTitulo());
        holder.tvPrice.setText("S/" + String.format("%.2f", tour.getPrecio()));
        holder.ratingBar.setRating(tour.getCalificacion());
        holder.tvRating.setText(String.format("%.1f", tour.getCalificacion()));
        
        // Configurar ciudad
        String ciudad = tour.getCiudad();
        if (ciudad != null && !ciudad.isEmpty()) {
            holder.tvCiudad.setText(ciudad);
            holder.tvCiudad.setVisibility(View.VISIBLE);
        } else {
            holder.tvCiudad.setVisibility(View.GONE);
        }
        
        // Configurar fecha
        String fecha = tour.getDate();
        if (fecha != null && !fecha.isEmpty()) {
            holder.tvFecha.setText(fecha);
            holder.tvFecha.setVisibility(View.VISIBLE);
        } else {
            holder.tvFecha.setVisibility(View.GONE);
        }
        
        // Configurar imagen (por ahora usando imagen por defecto)
        // TODO: Implementar carga de imágenes con Picasso/Glide cuando se tengan URLs reales
        holder.ivTourImage.setImageResource(R.drawable.cliente_tour_lima);
        
        // Click listener para el card completo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Usar el listener personalizado si está disponible
                listener.onTourClick(tour);
            } else {
                // Navegación por defecto directa al detalle
                Intent intent = new Intent(context, cliente_tour_detalle.class);
                intent.putExtra("tour_object", tour);
                context.startActivity(intent);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return tours != null ? tours.size() : 0;
    }
    
    public void updateTours(List<Cliente_Tour> newTours) {
        this.tours = newTours;
        notifyDataSetChanged();
    }
    
    static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTourImage;
        TextView tvTourTitle;
        TextView tvCiudad;
        TextView tvFecha;
        RatingBar ratingBar;
        TextView tvRating;
        TextView tvPrice;
        
        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTourImage = itemView.findViewById(R.id.iv_tour_image);
            tvTourTitle = itemView.findViewById(R.id.tv_tour_title);
            tvCiudad = itemView.findViewById(R.id.tv_ciudad);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }
}
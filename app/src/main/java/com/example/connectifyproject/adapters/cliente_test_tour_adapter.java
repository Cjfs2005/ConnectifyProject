package com.example.connectifyproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connectifyproject.R;
import com.example.connectifyproject.models.cliente_test_tour;
import java.util.List;

/**
 * Adapter para mostrar tours en RecyclerView con diseño de cards
 * Basado en el diseño de cliente_item_tour_gallery.xml
 */
public class cliente_test_tour_adapter extends RecyclerView.Adapter<cliente_test_tour_adapter.TourViewHolder> {
    
    private Context context;
    private List<cliente_test_tour> tours;
    private OnTourClickListener listener;
    
    public interface OnTourClickListener {
        void onTourClick(cliente_test_tour tour);
    }
    
    public cliente_test_tour_adapter(Context context, List<cliente_test_tour> tours) {
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
        cliente_test_tour tour = tours.get(position);
        
        // Configurar datos del tour
        holder.tvTourTitle.setText(tour.getTitulo());
        holder.tvPrice.setText(tour.getPrecioFormateado());
        holder.ratingBar.setRating((float) tour.getCalificacion());
        holder.tvRating.setText(tour.getCalificacionFormateada());
        
        // Configurar imagen (por ahora usando imagen por defecto)
        // TODO: Implementar carga de imágenes con Picasso/Glide cuando se tengan URLs reales
        holder.ivTourImage.setImageResource(R.drawable.cliente_tour_lima);
        
        // Click listener para el card completo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTourClick(tour);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return tours != null ? tours.size() : 0;
    }
    
    public void updateTours(List<cliente_test_tour> newTours) {
        this.tours = newTours;
        notifyDataSetChanged();
    }
    
    static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTourImage;
        TextView tvTourTitle;
        RatingBar ratingBar;
        TextView tvRating;
        TextView tvPrice;
        
        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTourImage = itemView.findViewById(R.id.iv_tour_image);
            tvTourTitle = itemView.findViewById(R.id.tv_tour_title);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }
}
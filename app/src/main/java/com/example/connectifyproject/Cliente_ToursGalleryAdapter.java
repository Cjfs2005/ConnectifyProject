package com.example.connectifyproject;

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
import com.example.connectifyproject.models.Cliente_Tour;
import java.util.List;

public class Cliente_ToursGalleryAdapter extends RecyclerView.Adapter<Cliente_ToursGalleryAdapter.TourGalleryViewHolder> {

    private List<Cliente_Tour> toursList;
    private Context context;

    public Cliente_ToursGalleryAdapter(List<Cliente_Tour> toursList, Context context) {
        this.toursList = toursList;
        this.context = context;
    }

    @NonNull
    @Override
    public TourGalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cliente_item_tour_gallery, parent, false);
        return new TourGalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourGalleryViewHolder holder, int position) {
        Cliente_Tour tour = toursList.get(position);
        
        holder.tvTourTitle.setText(tour.getTitle());
        holder.tvPrice.setText("S/. " + tour.getPrice());
        holder.tvRating.setText("4.5"); // Rating hardcodeado por ahora
        
        // Configurar rating bar
        holder.ratingBar.setRating(4.5f);
        
        // Configurar click listener para navegar a detalles del tour
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, cliente_tour_detalle.class);
            intent.putExtra("tour_nombre", tour.getTitle());
            intent.putExtra("tour_descripcion", tour.getDescription());
            intent.putExtra("tour_precio", "S/. " + tour.getPrice());
            intent.putExtra("tour_rating", "4.5");
            intent.putExtra("tour_duracion", tour.getDuration());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return toursList.size();
    }

    static class TourGalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTourImage;
        TextView tvTourTitle;
        TextView tvPrice;
        TextView tvRating;
        RatingBar ratingBar;

        public TourGalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTourImage = itemView.findViewById(R.id.iv_tour_image);
            tvTourTitle = itemView.findViewById(R.id.tv_tour_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvRating = itemView.findViewById(R.id.tv_rating);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}
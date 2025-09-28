package com.example.connectifyproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.models.Cliente_Tour;

import java.util.List;

public class Cliente_ReservasAdapter extends RecyclerView.Adapter<Cliente_ReservasAdapter.ReservaViewHolder> {

    private Context context;
    private List<Cliente_Tour> reservas;

    public Cliente_ReservasAdapter(Context context, List<Cliente_Tour> reservas) {
        this.context = context;
        this.reservas = reservas;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_tour, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Cliente_Tour reserva = reservas.get(position);
        
        holder.tvTourTitle.setText(reserva.getTitle());
        holder.tvTourCompany.setText(reserva.getCompany());
        holder.tvTourDuration.setText("Duración: " + reserva.getDuration());
        holder.tvTourDate.setText("Fecha: " + reserva.getDate());
        holder.tvTourPrice.setText(String.format("S/%.2f", reserva.getPrice()));
        
        // Usar siempre la imagen de cliente_tour_lima como solicitado
        holder.ivTourImage.setImageResource(R.drawable.cliente_tour_lima);
        
        // NO agregar click listener - las reservas no deben navegar a ningún lado
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public void updateReservas(List<Cliente_Tour> newReservas) {
        this.reservas = newReservas;
        notifyDataSetChanged();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTourImage;
        TextView tvTourTitle, tvTourCompany, tvTourDuration, tvTourDate, tvTourPrice;

        public ReservaViewHolder(@NonNull View itemView) {
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
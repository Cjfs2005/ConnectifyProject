package com.example.connectifyproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.cliente_reserva_detalle;
import com.example.connectifyproject.models.Cliente_Reserva;
import com.example.connectifyproject.models.Cliente_Tour;

import java.util.List;

public class Cliente_ReservasAdapter extends RecyclerView.Adapter<Cliente_ReservasAdapter.ReservaViewHolder> {

    private Context context;
    private List<Cliente_Reserva> reservas;

    public Cliente_ReservasAdapter(Context context, List<Cliente_Reserva> reservas) {
        this.context = context;
        this.reservas = reservas;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Cliente_Reserva reserva = reservas.get(position);
        Cliente_Tour tour = reserva.getTour();
        
        if (tour != null) {
            holder.tvTourTitle.setText(tour.getTitulo());
            holder.tvTourCompany.setText(tour.getCompanyName());
            holder.tvTourDuration.setText("Duración: " + tour.getDuracion());
        } else {
            holder.tvTourTitle.setText("Reserva");
            holder.tvTourCompany.setText("");
            holder.tvTourDuration.setText("");
        }
        holder.tvTourDate.setText("Fecha: " + reserva.getFecha());
        
        // Usar la imagen por defecto para todas las reservas
        holder.ivTourImage.setImageResource(R.drawable.cliente_tour_lima);

        // Click abre detalle de reserva
        holder.itemView.setOnClickListener(v -> {
            if (reserva != null) {
                Intent intent = new Intent(context, cliente_reserva_detalle.class);
                intent.putExtra("reserva_object", reserva);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public void updateReservas(List<Cliente_Reserva> newReservas) {
        this.reservas = newReservas;
        notifyDataSetChanged();
    }
    
    private String getReservationStatus(String date) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            java.util.Date reservationDate = sdf.parse(date);
            java.util.Date currentDate = new java.util.Date();
            
            if (reservationDate.after(currentDate)) {
                return "Próxima";
            } else {
                return "Pasada";
            }
        } catch (Exception e) {
            return "Próxima"; // Valor por defecto
        }
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTourImage;
        TextView tvTourTitle, tvTourCompany, tvTourDuration, tvTourDate;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTourImage = itemView.findViewById(R.id.iv_tour_image);
            tvTourTitle = itemView.findViewById(R.id.tv_tour_title);
            tvTourCompany = itemView.findViewById(R.id.tv_tour_company);
            tvTourDuration = itemView.findViewById(R.id.tv_tour_duration);
            tvTourDate = itemView.findViewById(R.id.tv_tour_date);
        }
    }
}
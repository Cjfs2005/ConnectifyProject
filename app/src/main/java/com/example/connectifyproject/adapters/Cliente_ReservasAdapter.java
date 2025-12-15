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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.cliente_reserva_detalle;
import com.example.connectifyproject.models.Cliente_Reserva;
import com.example.connectifyproject.models.Cliente_Tour;
import com.example.connectifyproject.utils.Cliente_FileStorageManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class Cliente_ReservasAdapter extends RecyclerView.Adapter<Cliente_ReservasAdapter.ReservaViewHolder> {
    
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Context context;
    private List<Cliente_Reserva> reservas;
    private Cliente_FileStorageManager fileManager;

    public Cliente_ReservasAdapter(Context context, List<Cliente_Reserva> reservas) {
        this.context = context;
        this.reservas = reservas;
        this.fileManager = new Cliente_FileStorageManager(context);
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
            holder.tvTourTitle.setText(tour.getTitle());
            holder.tvTourCompany.setText(tour.getCompanyName());
            holder.tvTourDuration.setText("Duración: " + tour.getDuration());
        } else {
            holder.tvTourTitle.setText("Reserva");
            holder.tvTourCompany.setText("");
            holder.tvTourDuration.setText("");
        }
        holder.tvTourDate.setText("Fecha: " + reserva.getFecha());
        
        // Cargar imagen real del tour con Glide
        if (tour != null && tour.getImageUrl() != null && !tour.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(context)
                    .load(tour.getImageUrl())
                    .placeholder(R.drawable.cliente_tour_lima)
                    .error(R.drawable.cliente_tour_lima)
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(holder.ivTourImage);
        } else {
            holder.ivTourImage.setImageResource(R.drawable.cliente_tour_lima);
        }

        // ✅ Click: si es cancelada muestra diálogo, sino abre detalle
        holder.itemView.setOnClickListener(v -> {
            if (reserva != null) {
                if ("Cancelada".equals(reserva.getEstado())) {
                    // Mostrar diálogo para reservas canceladas
                    mostrarDialogoCancelada(reserva);
                } else {
                    // Abrir detalle normal para reservas activas
                    Intent intent = new Intent(context, cliente_reserva_detalle.class);
                    intent.putExtra("reserva_object", reserva);
                    context.startActivity(intent);
                }
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
    
    /**
     * ✅ Mostrar diálogo con información de reserva cancelada
     */
    private void mostrarDialogoCancelada(Cliente_Reserva reserva) {
        String documentId = reserva.getDocumentId();
        if (documentId == null) {
            Toast.makeText(context, "Error: No se pudo cargar información de la cancelación", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Cargar datos completos de la cancelación
        db.collection("reservas_canceladas")
            .document(documentId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Map<String, Object> data = doc.getData();
                    
                    String tourTitulo = doc.getString("tourTitulo");
                    String empresaNombre = reserva.getTour() != null ? reserva.getTour().getCompanyName() : "Empresa no disponible";
                    String fecha = reserva.getFecha();
                    String hora = reserva.getHoraInicio() + " - " + reserva.getHoraFin();
                    String motivo = doc.getString("motivoCancelacion");
                    String montoTotal = doc.getString("montoTotal");
                    
                    // Construir mensaje del diálogo
                    StringBuilder mensaje = new StringBuilder();
                    mensaje.append("Tour: ").append(tourTitulo != null ? tourTitulo : "N/A").append("\n\n");
                    mensaje.append("Empresa: ").append(empresaNombre).append("\n\n");
                    mensaje.append("Fecha: ").append(fecha).append("\n");
                    mensaje.append("Hora: ").append(hora).append("\n\n");
                    mensaje.append("Motivo: La empresa decidió cancelar el tour\n\n");
                    mensaje.append("Monto original: S/ ").append(montoTotal != null ? montoTotal : "0.00").append("\n\n");
                    mensaje.append("✓ Ya no se le cobrará este monto");
                    
                    new AlertDialog.Builder(context)
                        .setTitle("Reserva Cancelada")
                        .setMessage(mensaje.toString())
                        .setPositiveButton("Cerrar", null)
                        .show();
                } else {
                    Toast.makeText(context, "No se encontraron los detalles de la cancelación", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Error al cargar detalles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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
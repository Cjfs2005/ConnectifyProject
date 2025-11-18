package com.example.connectifyproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.models.Cliente_ItinerarioItem;

import java.util.List;

public class Cliente_ItinerarioAdapter extends RecyclerView.Adapter<Cliente_ItinerarioAdapter.ItinerarioViewHolder> {

    private Context context;
    private List<Cliente_ItinerarioItem> itinerarioItems;
    private OnItinerarioItemClickListener listener;

    public interface OnItinerarioItemClickListener {
        void onVerMasClick(Cliente_ItinerarioItem item);
        void onItemClick(Cliente_ItinerarioItem item);
    }

    public Cliente_ItinerarioAdapter(Context context, List<Cliente_ItinerarioItem> itinerarioItems) {
        this.context = context;
        this.itinerarioItems = itinerarioItems;
    }

    public void setOnItinerarioItemClickListener(OnItinerarioItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItinerarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_itinerario, parent, false);
        return new ItinerarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItinerarioViewHolder holder, int position) {
        Cliente_ItinerarioItem item = itinerarioItems.get(position);
        
        holder.tvPlaceName.setText(item.getPlaceName());
        holder.tvVisitTime.setText(item.getVisitTime());
        holder.tvPlaceDescription.setText(item.getDescription());
        
        // Hide timeline line for last item
        if (item.isLastItem() || position == itinerarioItems.size() - 1) {
            holder.timelineLine.setVisibility(View.GONE);
        } else {
            holder.timelineLine.setVisibility(View.VISIBLE);
        }
        
        // Ver más click listener
        holder.tvVerMasItinerario.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVerMasClick(item);
            } else {
                // Show popup with more details
                showItemDetailsDialog(item);
            }
        });
        
        // Item click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itinerarioItems.size();
    }

    private void showItemDetailsDialog(Cliente_ItinerarioItem item) {
        // Simple toast for now, can be enhanced with AlertDialog
        Toast.makeText(context, 
                "Detalles de " + item.getPlaceName() + ":\n" + item.getDescription(), 
                Toast.LENGTH_LONG).show();
    }

    static class ItinerarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaceName, tvVisitTime, tvPlaceDescription, tvVerMasItinerario;
        View timelineLine;

        public ItinerarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            tvVisitTime = itemView.findViewById(R.id.tv_visit_time);
            tvPlaceDescription = itemView.findViewById(R.id.tv_place_description);
            tvVerMasItinerario = itemView.findViewById(R.id.tv_ver_mas_itinerario);
            timelineLine = itemView.findViewById(R.id.timeline_line);
        }
    }
}
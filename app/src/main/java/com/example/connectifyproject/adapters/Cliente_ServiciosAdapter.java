package com.example.connectifyproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.cliente_servicio_detalle;
import com.example.connectifyproject.models.Cliente_ServicioAdicional;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

public class Cliente_ServiciosAdapter extends RecyclerView.Adapter<Cliente_ServiciosAdapter.ServicioViewHolder> {

    private Context context;
    private List<Cliente_ServicioAdicional> servicios;
    private OnServiceSelectedListener listener;

    public interface OnServiceSelectedListener {
        void onServiceSelected(Cliente_ServicioAdicional servicio, boolean isSelected);
    }

    public Cliente_ServiciosAdapter(Context context, List<Cliente_ServicioAdicional> servicios) {
        this.context = context;
        this.servicios = servicios;
    }

    public void setOnServiceSelectedListener(OnServiceSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_servicio, parent, false);
        return new ServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        Cliente_ServicioAdicional servicio = servicios.get(position);
        
        holder.tvServiceName.setText(servicio.getName() + " (S/" + String.format("%.2f", servicio.getPrice()) + " por persona)");
        holder.tvServiceDescription.setText(servicio.getDescription());
        holder.cbServiceSelected.setChecked(servicio.isSelected());
        
        // Checkbox listener
        holder.cbServiceSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            servicio.setSelected(isChecked);
            if (listener != null) {
                listener.onServiceSelected(servicio, isChecked);
            }
        });
        
        // Ver más click listener
        holder.tvVerMas.setOnClickListener(v -> {
            Intent intent = new Intent(context, cliente_servicio_detalle.class);
            intent.putExtra("servicio_nombre", servicio.getName());
            intent.putExtra("servicio_precio", servicio.getPrice());
            intent.putExtra("servicio_descripcion", servicio.getDescription());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return servicios.size();
    }

    static class ServicioViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvServiceDescription, tvVerMas;
        MaterialCheckBox cbServiceSelected;

        public ServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvServiceDescription = itemView.findViewById(R.id.tv_service_description);
            tvVerMas = itemView.findViewById(R.id.tv_ver_mas);
            cbServiceSelected = itemView.findViewById(R.id.cb_service_selected);
        }
    }
}
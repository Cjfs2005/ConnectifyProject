package com.example.connectifyproject.ui.guia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.GuiaItemClientBinding;
import com.example.connectifyproject.model.GuiaClient;

import java.util.List;

public class GuiaClientAdapter extends RecyclerView.Adapter<GuiaClientAdapter.ClientViewHolder> {
    private List<GuiaClient> clients;
    private Context context;

    public GuiaClientAdapter(Context context, List<GuiaClient> clients) {
        this.context = context;
        this.clients = clients;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GuiaItemClientBinding binding = GuiaItemClientBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ClientViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        GuiaClient client = clients.get(position);
        holder.binding.name.setText(client.getName());
        holder.binding.code.setText("Código de reserva: " + client.getCode());
        holder.binding.status.setText("Estado: " + client.getStatus());
        holder.binding.time.setText(client.getTime());
        holder.binding.phone.setText("Teléfono: " + client.getPhone());
        if (client.isNoAsistio()) {
            holder.binding.status.setText("Estado: No asistió");
        }
        // Show stars only if rating > 0 (post-checkout)
        if (client.getRating() > 0) {
            holder.binding.stars.setVisibility(View.VISIBLE);
            // Simple logic to show all stars for now (can be extended later)
        } else {
            holder.binding.stars.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        GuiaItemClientBinding binding;

        public ClientViewHolder(GuiaItemClientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
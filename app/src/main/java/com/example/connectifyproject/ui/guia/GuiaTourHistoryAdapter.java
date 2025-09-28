package com.example.connectifyproject.ui.guia;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.GuiaItemTourHistoryBinding;
import com.example.connectifyproject.model.GuiaTourHistory;

import java.util.List;

public class GuiaTourHistoryAdapter extends RecyclerView.Adapter<GuiaTourHistoryAdapter.ViewHolder> {
    private List<GuiaTourHistory> tours;
    private Context context;

    public GuiaTourHistoryAdapter(Context context, List<GuiaTourHistory> tours) {
        this.context = context;
        this.tours = tours;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GuiaItemTourHistoryBinding binding = GuiaItemTourHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuiaTourHistory tour = tours.get(position);
        holder.binding.idText.setText("ID: " + tour.getId());
        holder.binding.nameText.setText(tour.getName());
        holder.binding.dateText.setText(tour.getDate());
        holder.binding.statusText.setText(tour.getStatus());
        if (!tour.getRating().isEmpty()) {
            holder.binding.ratingText.setText(tour.getRating());
            holder.binding.ratingText.setVisibility(View.VISIBLE);
        } else {
            holder.binding.ratingText.setVisibility(View.GONE);
        }
        // Color status: orange pendiente, green realizado, red cancelado
        if ("Pendiente".equals(tour.getStatus())) {
            holder.binding.statusText.setBackgroundColor(Color.parseColor("#FFF3E0"));  // Orange light
        } else if ("Realizado".equals(tour.getStatus())) {
            holder.binding.statusText.setBackgroundColor(Color.parseColor("#E8F5E8"));  // Green light
        } else if ("Cancelado".equals(tour.getStatus())) {
            holder.binding.statusText.setBackgroundColor(Color.parseColor("#FFEBEE"));  // Red light
        }
    }

    @Override
    public int getItemCount() {
        return tours.size();
    }

    public void updateList(List<GuiaTourHistory> newList) {
        tours = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        GuiaItemTourHistoryBinding binding;

        ViewHolder(GuiaItemTourHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
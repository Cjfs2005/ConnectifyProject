package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.models.TourService;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TourServiceAdapter extends RecyclerView.Adapter<TourServiceAdapter.ViewHolder> {
    private List<TourService> services;
    private OnServiceRemoveListener listener;

    public interface OnServiceRemoveListener {
        void onRemove(int position);
    }

    public TourServiceAdapter(List<TourService> services, OnServiceRemoveListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tour_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourService service = services.get(position);
        holder.bind(service, position);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        TextView tvServicePrice;
        MaterialButton btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvServicePrice = itemView.findViewById(R.id.tv_service_price);
            btnRemove = itemView.findViewById(R.id.btn_remove_service);
        }

        void bind(TourService service, int position) {
            tvServiceName.setText(service.getName());
            
            if (service.isPaid()) {
                tvServicePrice.setText(String.format("S/ %.2f", service.getPrice()));
                tvServicePrice.setTextColor(itemView.getContext().getColor(R.color.brand_purple_dark));
            } else {
                tvServicePrice.setText("Gratis");
                tvServicePrice.setTextColor(itemView.getContext().getColor(R.color.success_500));
            }
            
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemove(position);
                }
            });
        }
    }
}
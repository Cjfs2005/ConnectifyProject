package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.models.TourPlace;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TourPlaceAdapter extends RecyclerView.Adapter<TourPlaceAdapter.ViewHolder> {
    private List<TourPlace> places;
    private OnPlaceRemoveListener listener;

    public interface OnPlaceRemoveListener {
        void onRemove(int position);
    }

    public TourPlaceAdapter(List<TourPlace> places, OnPlaceRemoveListener listener) {
        this.places = places;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tour_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourPlace place = places.get(position);
        holder.bind(place, position);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaceName;
        TextView tvPlaceAddress;
        MaterialButton btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            tvPlaceAddress = itemView.findViewById(R.id.tv_place_address);
            btnRemove = itemView.findViewById(R.id.btn_remove_place);
        }

        void bind(TourPlace place, int position) {
            tvPlaceName.setText(place.getName());
            tvPlaceAddress.setText(place.getAddress());
            
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemove(position);
                }
            });
        }
    }
}
package com.example.connectifyproject.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.models.TourPlace;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class PlaceActivityAdapter extends RecyclerView.Adapter<PlaceActivityAdapter.ViewHolder> {
    private List<TourPlace> places;

    public PlaceActivityAdapter(List<TourPlace> places) {
        this.places = places;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_activity, parent, false);
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
        TextInputEditText etActivities;

        ViewHolder(View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            etActivities = itemView.findViewById(R.id.et_activities);
        }

        void bind(TourPlace place, int position) {
            tvPlaceName.setText(place.getName());
            etActivities.setText(place.getActivities());
            
            etActivities.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    place.setActivities(s.toString());
                }
            });
        }
    }
}
package com.example.connectifyproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> {

    private final List<admin_select_guide.GuideItem> guides;
    private final OnGuideSelectedListener listener;

    public interface OnGuideSelectedListener {
        void onGuideSelected(admin_select_guide.GuideItem guide);
    }

    public GuideAdapter(List<admin_select_guide.GuideItem> guides, OnGuideSelectedListener listener) {
        this.guides = guides;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guide, parent, false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        admin_select_guide.GuideItem guide = guides.get(position);
        holder.bind(guide);
    }

    @Override
    public int getItemCount() {
        return guides.size();
    }

    class GuideViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProfile;
        private final TextView tvName;
        private final TextView tvRating;
        private final TextView tvLanguages;
        private final TextView tvLocation;
        private final TextView tvTours;
        private final Button btnSelect;

        public GuideViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.img_guide_profile);
            tvName = itemView.findViewById(R.id.tv_guide_name);
            tvRating = itemView.findViewById(R.id.tv_guide_rating);
            tvLanguages = itemView.findViewById(R.id.tv_guide_languages);
            tvLocation = itemView.findViewById(R.id.tv_guide_location);
            tvTours = itemView.findViewById(R.id.tv_guide_tours);
            btnSelect = itemView.findViewById(R.id.btn_select_guide);
        }

        public void bind(admin_select_guide.GuideItem guide) {
            tvName.setText(guide.name);
            tvRating.setText(String.format("★ %.1f", guide.rating));
            tvLanguages.setText(guide.languages);
            tvLocation.setText("Lima, Perú"); // Ubicación por defecto
            tvTours.setText(String.format("%d tours", guide.tourCount));

            // Usar la imagen del recurso drawable
            imgProfile.setImageResource(guide.profileImage);

            btnSelect.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGuideSelected(guide);
                }
            });
        }
    }
}

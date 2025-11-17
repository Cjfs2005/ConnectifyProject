package com.example.connectifyproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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
        private final TextView tvTours;
        private final Button btnSelect;

        public GuideViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.iv_guide_profile);
            tvName = itemView.findViewById(R.id.tv_guide_name);
            tvRating = itemView.findViewById(R.id.tv_guide_rating);
            tvLanguages = itemView.findViewById(R.id.tv_guide_languages);
            tvTours = itemView.findViewById(R.id.tv_guide_tour_count);
            btnSelect = itemView.findViewById(R.id.btn_select_guide);
        }

        public void bind(admin_select_guide.GuideItem guide) {
            tvName.setText(guide.name);
            tvRating.setText(String.format("★ %.1f", guide.rating));
            // Convertir lista de idiomas a String separado por comas
            tvLanguages.setText(guide.languages != null ? String.join(", ", guide.languages) : "");
            
            tvTours.setText(String.format("%d tours", guide.tourCount));

            // Cargar imagen con Glide desde URL (profileImageUrl) o placeholder
            if (guide.profileImageUrl != null && !guide.profileImageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(guide.profileImageUrl)
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .circleCrop()
                    .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.placeholder_profile);
            }

            btnSelect.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGuideSelected(guide);
                }
            });
        }
    }
}

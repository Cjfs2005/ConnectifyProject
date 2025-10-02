package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connectifyproject.R;
import com.example.connectifyproject.models.Cliente_Review;
import java.util.List;

public class Cliente_ReviewsAdapter extends RecyclerView.Adapter<Cliente_ReviewsAdapter.ReviewViewHolder> {

    private List<Cliente_Review> reviewsList;

    public Cliente_ReviewsAdapter(List<Cliente_Review> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cliente_item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Cliente_Review review = reviewsList.get(position);
        
        holder.tvUserName.setText(review.getUserName());
        holder.tvDate.setText(review.getDate());
        holder.tvReviewText.setText(review.getReviewText());
        holder.tvRatingNumber.setText(review.getRatingText());
        
        // Configurar rating bar
        try {
            float rating = Float.parseFloat(review.getRatingStars());
            holder.ratingBar.setRating(rating);
        } catch (NumberFormatException e) {
            holder.ratingBar.setRating(5.0f);
        }
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserPhoto;
        TextView tvUserName;
        TextView tvDate;
        TextView tvReviewText;
        TextView tvRatingNumber;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserPhoto = itemView.findViewById(R.id.iv_user_photo);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
            tvRatingNumber = itemView.findViewById(R.id.tv_rating_number);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}
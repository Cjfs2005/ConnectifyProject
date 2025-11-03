package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.R;

import java.util.ArrayList;
import java.util.List;

public class CompanyPhotosAdapter extends RecyclerView.Adapter<CompanyPhotosAdapter.PhotoViewHolder> {

    private List<String> photoUrls;

    public CompanyPhotosAdapter() {
        this.photoUrls = new ArrayList<>();
    }

    public void setPhotos(List<String> photoUrls) {
        this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_company_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String photoUrl = photoUrls.get(position);
        
        Glide.with(holder.itemView.getContext())
                .load(photoUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_image_24)
                .error(R.drawable.ic_image_24)
                .into(holder.ivPhoto);
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivCompanyPhoto);
        }
    }
}

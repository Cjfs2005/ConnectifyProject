package com.example.connectifyproject.adapters;

import android.content.Context;
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

public class AdminCompanyPhotosAdapter extends RecyclerView.Adapter<AdminCompanyPhotosAdapter.AdminPhotoViewHolder> {

    private Context context;
    private List<String> photoUrls;

    public AdminCompanyPhotosAdapter(Context context) {
        this.context = context;
        this.photoUrls = new ArrayList<>();
    }

    public AdminCompanyPhotosAdapter(Context context, List<String> photoUrls) {
        this.context = context;
        this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>();
    }

    public void updatePhotos(List<String> photoUrls) {
        this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setPhotos(List<String> photoUrls) {
        updatePhotos(photoUrls);
    }

    @NonNull
    @Override
    public AdminPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_company_photo, parent, false);
        return new AdminPhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminPhotoViewHolder holder, int position) {
        String photoUrl = photoUrls.get(position);
        
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(context)
                    .load(photoUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_24)
                    .error(R.drawable.ic_image_24)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_image_24);
        }
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    static class AdminPhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        public AdminPhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivAdminCompanyPhoto);
        }
    }
}
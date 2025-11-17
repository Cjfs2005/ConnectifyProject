package com.example.connectifyproject.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.R;

import java.util.ArrayList;
import java.util.List;

public class PromotionalPhotosAdapter extends RecyclerView.Adapter<PromotionalPhotosAdapter.PhotoViewHolder> {

    private List<Object> photos; // Can be Uri or String (URL)
    private OnPhotoRemovedListener listener;

    public interface OnPhotoRemovedListener {
        void onPhotoRemoved(int position);
    }

    public PromotionalPhotosAdapter(OnPhotoRemovedListener listener) {
        this.photos = new ArrayList<>();
        this.listener = listener;
    }

    public void setPhotos(List<Uri> photos) {
        this.photos = new ArrayList<>(photos);
        notifyDataSetChanged();
    }

    public void addPhoto(Uri photo) {
        this.photos.add(photo);
        notifyItemInserted(photos.size() - 1);
    }

    public void addPhotoUrl(String photoUrl) {
        this.photos.add(photoUrl);
        notifyItemInserted(photos.size() - 1);
    }

    public void removePhoto(int position) {
        if (position >= 0 && position < photos.size()) {
            photos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, photos.size());
        }
    }

    public List<Uri> getPhotos() {
        List<Uri> uriPhotos = new ArrayList<>();
        for (Object photo : photos) {
            if (photo instanceof Uri) {
                uriPhotos.add((Uri) photo);
            }
        }
        return uriPhotos;
    }

    public int getPhotoCount() {
        return photos.size();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotional_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Object photo = photos.get(position);
        
        if (photo instanceof Uri) {
            holder.ivPhoto.setImageURI((Uri) photo);
        } else if (photo instanceof String) {
            Glide.with(holder.itemView.getContext())
                    .load((String) photo)
                    .centerCrop()
                    .into(holder.ivPhoto);
        }
        
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton btnRemove;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

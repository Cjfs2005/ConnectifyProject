package com.example.connectifyproject.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;

import java.util.List;

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ImageVH> {
    private final List<String> imageUris; // String form of Uri

    public SelectedImageAdapter(List<String> imageUris) {
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public ImageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_image, parent, false);
        return new ImageVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageVH holder, int position) {
        String uriStr = imageUris.get(position);
        holder.bind(uriStr);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ImageVH extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        public ImageVH(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_preview);
        }
        public void bind(String uriStr) {
            try {
                imageView.setImageURI(Uri.parse(uriStr));
            } catch (Exception ignored) {}
        }
    }
}

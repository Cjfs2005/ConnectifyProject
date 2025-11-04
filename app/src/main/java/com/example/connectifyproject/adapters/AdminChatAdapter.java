package com.example.connectifyproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.R;
import com.example.connectifyproject.admin_chat_conversation;
import com.example.connectifyproject.models.AdminChatClient;

import java.util.ArrayList;
import java.util.List;

public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.ChatViewHolder> {

    private Context context;
    private List<AdminChatClient> clients;
    private List<AdminChatClient> filteredClients;

    public AdminChatAdapter(Context context, List<AdminChatClient> clients) {
        this.context = context;
        this.clients = clients;
        this.filteredClients = new ArrayList<>(clients);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        AdminChatClient client = filteredClients.get(position);
        
        holder.tvClientName.setText(client.getName());
        holder.tvLastMessage.setText(client.getLastMessage());
        holder.tvTime.setText(client.getTimeAgo());
        
        // Cargar foto de perfil real desde Firebase
        if (client.getClientPhotoUrl() != null && !client.getClientPhotoUrl().isEmpty()) {
            Glide.with(context)
                .load(client.getClientPhotoUrl())
                .placeholder(client.getPhotoResource())
                .error(client.getPhotoResource())
                .circleCrop()
                .into(holder.ivClientAvatar);
        } else {
            holder.ivClientAvatar.setImageResource(client.getPhotoResource());
        }
        
        // Mostrar contador de mensajes no leídos
        int unreadCount = client.getUnreadCount();
        if (unreadCount > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(unreadCount));
            holder.newMessageIndicator.setVisibility(View.GONE); // Ocultar el punto si hay contador
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
            holder.newMessageIndicator.setVisibility(View.GONE);
        }

        // Click listener para navegar a la conversación
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, admin_chat_conversation.class);
            intent.putExtra("client_id", client.getClientId());
            intent.putExtra("client_name", client.getName());
            intent.putExtra("client_photo_url", client.getClientPhotoUrl());
            intent.putExtra("client_avatar", client.getPhotoResource());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredClients.size();
    }

    public void updateData(List<AdminChatClient> newClients) {
        // Crear una copia temporal para evitar problemas de referencia
        List<AdminChatClient> tempList = new ArrayList<>(newClients);
        
        this.clients.clear();
        this.clients.addAll(tempList);
        this.filteredClients.clear();
        this.filteredClients.addAll(tempList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredClients.clear();
        if (query.isEmpty()) {
            filteredClients.addAll(clients);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (AdminChatClient client : clients) {
                if (client.getName().toLowerCase().contains(lowerCaseQuery) ||
                    client.getLastMessage().toLowerCase().contains(lowerCaseQuery)) {
                    filteredClients.add(client);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvLastMessage, tvTime, tvUnreadCount;
        ImageView ivClientAvatar;
        View newMessageIndicator;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            ivClientAvatar = itemView.findViewById(R.id.ivClientAvatar);
            newMessageIndicator = itemView.findViewById(R.id.vNewMessageIndicator);
        }
    }
}
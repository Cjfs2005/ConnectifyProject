package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.model.ChatMessage;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminMessageAdapter extends RecyclerView.Adapter<AdminMessageAdapter.MessageViewHolder> {
    
    private List<ChatMessage> messages;
    private String currentUserId;
    private SimpleDateFormat timeFormat;
    
    public AdminMessageAdapter(String currentUserId) {
        this.messages = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        // Verificar si el mensaje es del administrador actual
        boolean isAdminMessage = message.getSenderId().equals(currentUserId);
        
        if (isAdminMessage) {
            // Mostrar como mensaje del admin (derecha)
            holder.cardMessageAdmin.setVisibility(View.VISIBLE);
            holder.cardMessageClient.setVisibility(View.GONE);
            holder.tvMessageAdmin.setText(message.getMessageText());
            holder.tvTimeAdmin.setText(formatTime(message.getTimestamp()));
        } else {
            // Mostrar como mensaje del cliente (izquierda)
            holder.cardMessageAdmin.setVisibility(View.GONE);
            holder.cardMessageClient.setVisibility(View.VISIBLE);
            holder.tvMessageClient.setText(message.getMessageText());
            holder.tvTimeClient.setText(formatTime(message.getTimestamp()));
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }
    
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    private String formatTime(com.google.firebase.Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            return timeFormat.format(date);
        }
        return "";
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardMessageAdmin, cardMessageClient;
        TextView tvMessageAdmin, tvTimeAdmin;
        TextView tvMessageClient, tvTimeClient;
        
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMessageAdmin = itemView.findViewById(R.id.card_message_admin);
            cardMessageClient = itemView.findViewById(R.id.card_message_client);
            tvMessageAdmin = itemView.findViewById(R.id.tv_message_admin);
            tvTimeAdmin = itemView.findViewById(R.id.tv_time_admin);
            tvMessageClient = itemView.findViewById(R.id.tv_message_client);
            tvTimeClient = itemView.findViewById(R.id.tv_time_client);
        }
    }
}

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

public class ClienteMessageAdapter extends RecyclerView.Adapter<ClienteMessageAdapter.MessageViewHolder> {
    
    private List<ChatMessage> messages;
    private String currentUserId;
    private SimpleDateFormat timeFormat;
    
    public ClienteMessageAdapter(String currentUserId) {
        this.messages = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cliente_item_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        // Verificar si el mensaje es del usuario actual
        boolean isUserMessage = message.getSenderId().equals(currentUserId);
        
        if (isUserMessage) {
            // Mostrar como mensaje del usuario (derecha)
            holder.cardMessageUser.setVisibility(View.VISIBLE);
            holder.cardMessageCompany.setVisibility(View.GONE);
            holder.tvMessageUser.setText(message.getMessageText());
            holder.tvTimeUser.setText(formatTime(message.getTimestamp()));
        } else {
            // Mostrar como mensaje de la empresa (izquierda)
            holder.cardMessageUser.setVisibility(View.GONE);
            holder.cardMessageCompany.setVisibility(View.VISIBLE);
            holder.tvMessageCompany.setText(message.getMessageText());
            holder.tvTimeCompany.setText(formatTime(message.getTimestamp()));
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
        MaterialCardView cardMessageUser, cardMessageCompany;
        TextView tvMessageUser, tvTimeUser;
        TextView tvMessageCompany, tvTimeCompany;
        
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMessageUser = itemView.findViewById(R.id.card_message_user);
            cardMessageCompany = itemView.findViewById(R.id.card_message_company);
            tvMessageUser = itemView.findViewById(R.id.tv_message_user);
            tvTimeUser = itemView.findViewById(R.id.tv_time_user);
            tvMessageCompany = itemView.findViewById(R.id.tv_message_company);
            tvTimeCompany = itemView.findViewById(R.id.tv_time_company);
        }
    }
}

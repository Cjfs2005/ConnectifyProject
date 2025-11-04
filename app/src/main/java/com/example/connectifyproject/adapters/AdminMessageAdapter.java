package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.model.ChatItem;
import com.example.connectifyproject.model.ChatMessage;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_MESSAGE = 0;
    private static final int VIEW_TYPE_DATE_SEPARATOR = 1;
    
    private List<ChatItem> chatItems;
    private String currentUserId;
    private SimpleDateFormat timeFormat;
    
    public AdminMessageAdapter(String currentUserId) {
        this.chatItems = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @Override
    public int getItemViewType(int position) {
        return chatItems.get(position).getType();
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATE_SEPARATOR) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_date_separator, parent, false);
            return new DateSeparatorViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.admin_item_message, parent, false);
            return new MessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem chatItem = chatItems.get(position);
        
        if (holder instanceof DateSeparatorViewHolder) {
            ((DateSeparatorViewHolder) holder).bind(chatItem.getDateText());
        } else if (holder instanceof MessageViewHolder) {
            MessageViewHolder messageHolder = (MessageViewHolder) holder;
            ChatMessage message = chatItem.getMessage();
            
            // Verificar si el mensaje es del administrador actual
            boolean isAdminMessage = message.getSenderId().equals(currentUserId);
            
            if (isAdminMessage) {
                // Mostrar como mensaje del admin (derecha)
                messageHolder.cardMessageAdmin.setVisibility(View.VISIBLE);
                messageHolder.cardMessageClient.setVisibility(View.GONE);
                messageHolder.tvMessageAdmin.setText(message.getMessageText());
                messageHolder.tvTimeAdmin.setText(formatTime(message.getTimestamp()));
            } else {
                // Mostrar como mensaje del cliente (izquierda)
                messageHolder.cardMessageAdmin.setVisibility(View.GONE);
                messageHolder.cardMessageClient.setVisibility(View.VISIBLE);
                messageHolder.tvMessageClient.setText(message.getMessageText());
                messageHolder.tvTimeClient.setText(formatTime(message.getTimestamp()));
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return chatItems.size();
    }
    
    public void setChatItems(List<ChatItem> chatItems) {
        this.chatItems = chatItems;
        notifyDataSetChanged();
    }
    
    public void addMessage(ChatMessage message) {
        this.chatItems.add(new ChatItem(message));
        notifyItemInserted(chatItems.size() - 1);
    }
    
    private String formatTime(com.google.firebase.Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            return timeFormat.format(date);
        }
        return "";
    }
    
    static class DateSeparatorViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateSeparator;
        
        DateSeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateSeparator = itemView.findViewById(R.id.tv_date_separator);
        }
        
        void bind(String dateText) {
            tvDateSeparator.setText(dateText);
        }
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

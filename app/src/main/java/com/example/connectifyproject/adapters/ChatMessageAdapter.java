package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    // Clase interna para datos de mensaje
    public static class MessageData {
        public String message;
        public String time;
        public boolean isFromUser;

        public MessageData(String message, String time, boolean isFromUser) {
            this.message = message;
            this.time = time;
            this.isFromUser = isFromUser;
        }
    }

    private List<MessageData> messages;

    public ChatMessageAdapter(List<MessageData> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageData message = messages.get(position);
        
        if (message.isFromUser) {
            // Mostrar mensaje del usuario (lado derecho)
            holder.cardMessageUser.setVisibility(View.VISIBLE);
            holder.cardMessageCompany.setVisibility(View.GONE);
            holder.tvMessageUser.setText(message.message);
            holder.tvTimeUser.setText(message.time);
        } else {
            // Mostrar mensaje de la empresa (lado izquierdo)
            holder.cardMessageUser.setVisibility(View.GONE);
            holder.cardMessageCompany.setVisibility(View.VISIBLE);
            holder.tvMessageCompany.setText(message.message);
            holder.tvTimeCompany.setText(message.time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(MessageData message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardMessageUser, cardMessageCompany;
        TextView tvMessageUser, tvTimeUser, tvMessageCompany, tvTimeCompany;

        public MessageViewHolder(@NonNull View itemView) {
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
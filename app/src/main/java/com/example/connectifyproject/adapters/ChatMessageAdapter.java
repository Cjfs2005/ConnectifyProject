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

    private List<MessageData> messages;

    public ChatMessageAdapter(List<MessageData> messages) {
        this.messages = messages;
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
        MessageData message = messages.get(position);
        
        if (message.isFromAdmin()) {
            // Mostrar mensaje del admin (lado derecho) - usar card de user
            holder.cardMessageUser.setVisibility(View.VISIBLE);
            holder.cardMessageCompany.setVisibility(View.GONE);
            holder.tvMessageUser.setText(message.getMessage());
            holder.tvTimeUser.setText(message.getTime());
        } else {
            // Mostrar mensaje del cliente (lado izquierdo) - usar card de company
            holder.cardMessageUser.setVisibility(View.GONE);
            holder.cardMessageCompany.setVisibility(View.VISIBLE);
            holder.tvMessageCompany.setText(message.getMessage());
            holder.tvTimeCompany.setText(message.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public void addMessage(MessageData message) {
        if (messages != null) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardMessageUser;
        MaterialCardView cardMessageCompany;
        TextView tvMessageUser;
        TextView tvTimeUser;
        TextView tvMessageCompany;
        TextView tvTimeCompany;

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

    public static class MessageData {
        private String message;
        private String time;
        private boolean isFromAdmin;

        public MessageData(String message, String time, boolean isFromAdmin) {
            this.message = message;
            this.time = time;
            this.isFromAdmin = isFromAdmin;
        }

        public String getMessage() {
            return message;
        }

        public String getTime() {
            return time;
        }

        public boolean isFromAdmin() {
            return isFromAdmin;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public void setFromAdmin(boolean fromAdmin) {
            isFromAdmin = fromAdmin;
        }
    }
}
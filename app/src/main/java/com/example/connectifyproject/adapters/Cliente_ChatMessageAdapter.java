package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.models.Cliente_ChatMessage;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class Cliente_ChatMessageAdapter extends RecyclerView.Adapter<Cliente_ChatMessageAdapter.MessageViewHolder> {

    private List<Cliente_ChatMessage> messages;

    public Cliente_ChatMessageAdapter(List<Cliente_ChatMessage> messages) {
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
        Cliente_ChatMessage message = messages.get(position);
        
        if (message.isFromUser()) {
            // Mostrar mensaje del usuario (lado derecho)
            holder.cardMessageUser.setVisibility(View.VISIBLE);
            holder.cardMessageCompany.setVisibility(View.GONE);
            holder.tvMessageUser.setText(message.getMessage());
            holder.tvTimeUser.setText(message.getTime());
        } else {
            // Mostrar mensaje de la empresa (lado izquierdo)
            holder.cardMessageUser.setVisibility(View.GONE);
            holder.cardMessageCompany.setVisibility(View.VISIBLE);
            holder.tvMessageCompany.setText(message.getMessage());
            holder.tvTimeCompany.setText(message.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Cliente_ChatMessage message) {
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
package com.example.connectifyproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.admin_chat_conversation;

import java.util.ArrayList;
import java.util.List;

public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.ChatViewHolder> {

    private Context context;
    private List<ClientData> clients;
    private List<ClientData> filteredClients;

    // Clase interna para datos de cliente
    public static class ClientData {
        public String name;
        public String lastMessage;
        public String timeAgo;
        public int avatarResource;
        public boolean hasNewMessages;

        public ClientData(String name, String lastMessage, String timeAgo, int avatarResource, boolean hasNewMessages) {
            this.name = name;
            this.lastMessage = lastMessage;
            this.timeAgo = timeAgo;
            this.avatarResource = avatarResource;
            this.hasNewMessages = hasNewMessages;
        }
    }

    public AdminChatAdapter(Context context) {
        this.context = context;
        this.clients = new ArrayList<>();
        this.filteredClients = new ArrayList<>();
        loadClientsData();
    }

    private void loadClientsData() {
        // Datos de ejemplo basados en las conversaciones del cliente
        clients.add(new ClientData("Alex Rodríguez", "Perfecto, me interesa el de 3 días", "9:20", R.drawable.ic_avatar_male_1, true));
        clients.add(new ClientData("María García", "¿Tienen tours al Cañón del Colca?", "9:18", R.drawable.ic_avatar_female_1, true));
        clients.add(new ClientData("Carlos Mendoza", "Excelente elección. El tour de 3 días cuesta S/. 280 por persona", "9:22", R.drawable.ic_avatar_male_2, false));
        clients.add(new ClientData("Ana López", "Buenos días, ¿tienen tours al Colca?", "9:15", R.drawable.ic_avatar_female_2, true));
        clients.add(new ClientData("José Pérez", "Gracias por la información", "8:45", R.drawable.ic_avatar_male_3, false));
        clients.add(new ClientData("Carmen Silva", "¿Cuál es el precio del tour completo?", "8:30", R.drawable.ic_avatar_female_3, true));
        clients.add(new ClientData("Luis Torres", "Me gustaría reservar para mañana", "8:15", R.drawable.ic_avatar_male_1, false));
        clients.add(new ClientData("Rosa Vargas", "¿Incluye el almuerzo?", "7:50", R.drawable.ic_avatar_female_1, true));
        
        filteredClients.addAll(clients);
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
        ClientData client = filteredClients.get(position);
        
        holder.tvClientName.setText(client.name);
        holder.tvLastMessage.setText(client.lastMessage);
        holder.tvTime.setText(client.timeAgo);
        holder.ivClientAvatar.setImageResource(client.avatarResource);
        
        // Mostrar u ocultar indicador de mensajes nuevos
        holder.newMessageIndicator.setVisibility(client.hasNewMessages ? View.VISIBLE : View.GONE);

        // Click listener para navegar a la conversación
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, admin_chat_conversation.class);
            intent.putExtra("client_name", client.name);
            intent.putExtra("client_avatar", client.avatarResource);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredClients.size();
    }

    public void filter(String query) {
        filteredClients.clear();
        if (query.isEmpty()) {
            filteredClients.addAll(clients);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (ClientData client : clients) {
                if (client.name.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    client.lastMessage.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredClients.add(client);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvLastMessage, tvTime;
        ImageView ivClientAvatar;
        View newMessageIndicator;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivClientAvatar = itemView.findViewById(R.id.ivClientAvatar);
            newMessageIndicator = itemView.findViewById(R.id.vNewMessageIndicator);
        }
    }
}
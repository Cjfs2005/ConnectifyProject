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

import com.example.connectifyproject.R;
import com.example.connectifyproject.cliente_chat_conversation;

import java.util.ArrayList;
import java.util.List;

public class ChatCompanyAdapter extends RecyclerView.Adapter<ChatCompanyAdapter.ChatViewHolder> {

    private Context context;
    private List<CompanyData> companies;
    private List<CompanyData> filteredCompanies;

    // Clase interna para datos de empresa
    public static class CompanyData {
        public String name;
        public String lastMessage;
        public String timeAgo;
        public int logoResource;

        public CompanyData(String name, String lastMessage, String timeAgo, int logoResource) {
            this.name = name;
            this.lastMessage = lastMessage;
            this.timeAgo = timeAgo;
            this.logoResource = logoResource;
        }
    }

    public ChatCompanyAdapter(Context context) {
        this.context = context;
        this.companies = new ArrayList<>();
        this.filteredCompanies = new ArrayList<>();
        loadCompaniesData();
    }

    private void loadCompaniesData() {
        // Datos hardcodeados directamente en el adapter
        companies.add(new CompanyData("Lima Tours", "Quedo atento para cualquier consulta", "10 min", R.drawable.cliente_tour_lima));
        companies.add(new CompanyData("Arequipa Adventures", "El tour de mañana está confirmado", "25 min", R.drawable.cliente_tour_arequipa));
        companies.add(new CompanyData("Cusco Explorer", "Gracias por contactarnos", "1 h", R.drawable.cliente_tour_cusco));
        companies.add(new CompanyData("Trujillo Expeditions", "¿En qué horario prefiere el tour?", "2 h", R.drawable.cliente_tour_trujillo));
        companies.add(new CompanyData("Iquitos Nature", "Perfecto, nos vemos en el punto de encuentro", "1 día", R.drawable.cliente_tour_iquitos));
        companies.add(new CompanyData("Paracas Ocean", "El tour incluye almuerzo típico", "2 días", R.drawable.cliente_tour_paracas));
        companies.add(new CompanyData("Huacachina Desert", "¿Cuántas personas serán para el tour?", "3 días", R.drawable.cliente_tour_huacachina));
        
        filteredCompanies.addAll(companies);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_company, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        CompanyData company = filteredCompanies.get(position);
        
        holder.tvCompanyName.setText(company.name);
        holder.tvLastMessage.setText(company.lastMessage);
        holder.tvTime.setText(company.timeAgo);
        holder.ivCompanyLogo.setImageResource(company.logoResource);

        // Click listener para navegar a la conversación
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, cliente_chat_conversation.class);
            intent.putExtra("company_name", company.name);
            intent.putExtra("company_logo", company.logoResource);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredCompanies.size();
    }

    public void filter(String query) {
        filteredCompanies.clear();
        if (query.isEmpty()) {
            filteredCompanies.addAll(companies);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (CompanyData company : companies) {
                if (company.name.toLowerCase().contains(lowerCaseQuery) ||
                    company.lastMessage.toLowerCase().contains(lowerCaseQuery)) {
                    filteredCompanies.add(company);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvCompanyName, tvLastMessage, tvTime;
        ImageView ivCompanyLogo;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCompanyName = itemView.findViewById(R.id.tv_company_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivCompanyLogo = itemView.findViewById(R.id.iv_company_logo);
        }
    }
}
package com.example.connectifyproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.cliente_chat_conversation;
import com.example.connectifyproject.models.Cliente_ChatCompany;

import java.util.ArrayList;
import java.util.List;

public class Cliente_ChatCompanyAdapter extends RecyclerView.Adapter<Cliente_ChatCompanyAdapter.ChatViewHolder> {
    private static final String TAG = "ClienteChatAdapter";

    private Context context;
    private List<Cliente_ChatCompany> companies;
    private List<Cliente_ChatCompany> filteredCompanies;



    public Cliente_ChatCompanyAdapter(Context context, List<Cliente_ChatCompany> companies) {
        this.context = context;
        this.companies = companies;
        this.filteredCompanies = new ArrayList<>(companies);
        Log.d(TAG, "Cliente_ChatCompanyAdapter creado con " + companies.size() + " empresas");
    }



    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cliente_item_chat_company, parent, false);
        Log.d(TAG, "onCreateViewHolder llamado");
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Cliente_ChatCompany company = filteredCompanies.get(position);
        
        Log.d(TAG, "onBindViewHolder - Position: " + position + ", Empresa: " + company.getName());
        
        holder.tvCompanyName.setText(company.getName());
        holder.tvLastMessage.setText(company.getLastMessage());
        holder.tvTime.setText(company.getTimeAgo());
        holder.ivCompanyLogo.setImageResource(company.getLogoResource());

        // Click listener para navegar a la conversación
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, cliente_chat_conversation.class);
            intent.putExtra("admin_id", company.getAdminId());
            intent.putExtra("admin_name", company.getName());
            intent.putExtra("admin_photo_url", company.getAdminPhotoUrl());
            intent.putExtra("company_logo", company.getLogoResource());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        int count = filteredCompanies.size();
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    public void updateData(List<Cliente_ChatCompany> newCompanies) {
        Log.d(TAG, "updateData llamado con " + newCompanies.size() + " empresas");
        
        // Crear una copia temporal para evitar problemas de referencia
        List<Cliente_ChatCompany> tempList = new ArrayList<>(newCompanies);
        
        this.companies.clear();
        this.companies.addAll(tempList);
        this.filteredCompanies.clear();
        this.filteredCompanies.addAll(tempList);
        notifyDataSetChanged();
        Log.d(TAG, "Después de updateData - filteredCompanies size: " + filteredCompanies.size());
    }

    public void filter(String query) {
        filteredCompanies.clear();
        if (query.isEmpty()) {
            filteredCompanies.addAll(companies);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Cliente_ChatCompany company : companies) {
                if (company.getName().toLowerCase().contains(lowerCaseQuery) ||
                    company.getLastMessage().toLowerCase().contains(lowerCaseQuery)) {
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
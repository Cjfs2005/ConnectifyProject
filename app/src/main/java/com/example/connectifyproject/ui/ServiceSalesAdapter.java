package com.example.connectifyproject.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.ItemServiceSaleBinding;
import com.example.connectifyproject.models.ServiceSale;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class ServiceSalesAdapter extends RecyclerView.Adapter<ServiceSalesAdapter.VH> {

    private final List<ServiceSale> items = new ArrayList<>();
    private int maxAmount = 1;

    public void submitList(List<ServiceSale> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
            maxAmount = 1;
            for (ServiceSale s : items) {
                if (s.getAmount() > maxAmount) maxAmount = s.getAmount();
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceSaleBinding binding = ItemServiceSaleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ServiceSale item = items.get(position);
        holder.binding.tvServiceName.setText(item.getServiceName());
        holder.binding.tvServiceAmount.setText("$" + item.getAmount());

        LinearProgressIndicator pi = holder.binding.piBar;
        int percent = Math.round((item.getAmount() * 100f) / maxAmount);
        pi.setProgressCompat(percent, true);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemServiceSaleBinding binding;
        VH(ItemServiceSaleBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
    }
}
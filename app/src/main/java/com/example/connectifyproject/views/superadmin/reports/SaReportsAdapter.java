package com.example.connectifyproject.views.superadmin.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SaReportsAdapter extends RecyclerView.Adapter<SaReportsAdapter.VH> {

    public interface Listener {
        void onDownload(CompanyItem item);
    }

    public static class CompanyItem {
        public final String name;
        public final int count; // opcional, puedes usar 0

        public CompanyItem(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    private final List<CompanyItem> full = new ArrayList<>();
    private final List<CompanyItem> items = new ArrayList<>();
    private boolean asc = true;
    private String q = "";
    private final Listener listener;

    public SaReportsAdapter(List<CompanyItem> initial, Listener listener) {
        this.listener = listener;
        replaceAll(initial);
    }

    public void replaceAll(List<CompanyItem> data) {
        full.clear();
        if (data != null) full.addAll(data);
        apply();
    }

    public void setQuery(String query) {
        this.q = query == null ? "" : query.trim().toLowerCase();
        apply();
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
        apply();
    }

    private void apply() {
        items.clear();
        for (CompanyItem it : full) {
            if (!q.isEmpty() && !it.name.toLowerCase().contains(q)) continue;
            items.add(it);
        }
        Comparator<CompanyItem> cmp = (a, b) -> a.name.compareToIgnoreCase(b.name);
        if (!asc) cmp = (a, b) -> b.name.compareToIgnoreCase(a.name);
        Collections.sort(items, cmp);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_company, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvBadge;
        ImageButton btnDownload;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }

        void bind(CompanyItem item, Listener listener) {
            tvName.setText(item.name);
            tvBadge.setText(String.valueOf(item.count));
            btnDownload.setOnClickListener(v -> {
                if (listener != null) listener.onDownload(item);
            });
            // Tap en la fila también dispara descargar (opcional)
            itemView.setOnClickListener(v -> btnDownload.performClick());
        }
    }
}

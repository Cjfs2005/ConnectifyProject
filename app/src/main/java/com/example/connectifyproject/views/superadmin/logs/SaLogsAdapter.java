package com.example.connectifyproject.views.superadmin.logs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.model.Role;
import com.example.connectifyproject.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;

import com.example.connectifyproject.views.superadmin.logs.SaLogFirestoreItem;

public class SaLogsAdapter extends RecyclerView.Adapter<SaLogsAdapter.VH> {

    public enum SortOrder { RECENT, OLD }


    public interface Listener {
        void onClick(SaLogFirestoreItem item);
    }

    public static class LogItem {
        public final String action; // "Creó un tour", "Finalizó el tour", "Reservó un tour"
        public final Role role;     // ADMIN, GUIDE, CLIENT
        public final User user;     // quien hizo la acción
        public final long atMillis; // cuándo

        public LogItem(String action, Role role, User user, long atMillis) {
            this.action = action;
            this.role = role;
            this.user = user;
            this.atMillis = atMillis;
        }
    }


    private final List<SaLogFirestoreItem> full = new ArrayList<>();
    private final List<SaLogFirestoreItem> items = new ArrayList<>();
    private String searchText = "";
    private final Listener listener;
    private final Context context;

    private SortOrder sort = SortOrder.RECENT;
    private EnumSet<Role> roleFilter = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);


    public SaLogsAdapter(Context context, List<SaLogFirestoreItem> initial, Listener listener) {
        this.context = context;
        this.listener = listener;
        replaceAll(initial);
    }


    public void replaceAll(List<SaLogFirestoreItem> data) {
        full.clear();
        if (data != null) full.addAll(data);
        apply();
    }

    public void setSort(SortOrder order) {
        sort = (order == null) ? SortOrder.RECENT : order;
        apply();
    }

    public void setRoleFilter(EnumSet<Role> roles) {
        roleFilter = (roles == null || roles.isEmpty())
                ? EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT)
                : roles;
        apply();
    }


    private void apply() {
        items.clear();
        for (SaLogFirestoreItem it : full) {
            if (searchText.isEmpty() ||
                it.titulo.toLowerCase().contains(searchText) ||
                it.descripcion.toLowerCase().contains(searchText)) {
                items.add(it);
            }
        }
        Comparator<SaLogFirestoreItem> cmp = (a, b) -> Long.compare(b.timestamp, a.timestamp);
        if (sort == SortOrder.OLD) cmp = (a, b) -> Long.compare(a.timestamp, b.timestamp);
        Collections.sort(items, cmp);
        notifyDataSetChanged();
    }

    public void setSearchText(String text) {
        this.searchText = text == null ? "" : text.trim().toLowerCase();
        apply();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAction, tvUserSub, tvWhen;

        VH(@NonNull View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvUserSub = itemView.findViewById(R.id.tvUserSub);
            tvWhen = itemView.findViewById(R.id.tvWhen);
        }

        void bind(SaLogFirestoreItem it, Listener listener) {
            // Título
            tvAction.setText(it.titulo);
            // Descripción truncada a una línea con "..."
            tvUserSub.setText(it.descripcion);
            tvUserSub.setSingleLine(true);
            tvUserSub.setEllipsize(android.text.TextUtils.TruncateAt.END);
            // Fecha relativa
            tvWhen.setText(relative(it.timestamp));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(it);
            });
        }

        private static String relative(long t) {
            long now = System.currentTimeMillis();
            long msDay = 24L * 60 * 60 * 1000;
            if (isSameDay(t, now)) return "HOY";
            if (isSameDay(t, now - msDay)) return "AYER";
            SimpleDateFormat f = new SimpleDateFormat("dd MMM.", new Locale("es", "PE"));
            return f.format(new java.util.Date(t)).toUpperCase(new Locale("es", "PE"));
        }

        private static boolean isSameDay(long a, long b) {
            java.util.Calendar ca = java.util.Calendar.getInstance();
            ca.setTimeInMillis(a);
            java.util.Calendar cb = java.util.Calendar.getInstance();
            cb.setTimeInMillis(b);
            return ca.get(java.util.Calendar.YEAR) == cb.get(java.util.Calendar.YEAR)
                    && ca.get(java.util.Calendar.DAY_OF_YEAR) == cb.get(java.util.Calendar.DAY_OF_YEAR);
        }
    }
}

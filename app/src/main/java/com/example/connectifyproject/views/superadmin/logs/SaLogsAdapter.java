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

public class SaLogsAdapter extends RecyclerView.Adapter<SaLogsAdapter.VH> {

    public enum SortOrder { RECENT, OLD }

    public interface Listener {
        void onClick(LogItem item);
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

    private final List<LogItem> full = new ArrayList<>();
    private final List<LogItem> items = new ArrayList<>();
    private final Listener listener;

    private SortOrder sort = SortOrder.RECENT;
    private EnumSet<Role> roleFilter = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);

    public SaLogsAdapter(List<LogItem> initial, Listener listener) {
        this.listener = listener;
        replaceAll(initial);
    }

    public void replaceAll(List<LogItem> data) {
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
        for (LogItem it : full) {
            if (!roleFilter.contains(it.role)) continue;
            items.add(it);
        }
        Comparator<LogItem> cmp = (a, b) -> Long.compare(b.atMillis, a.atMillis);
        if (sort == SortOrder.OLD) cmp = (a, b) -> Long.compare(a.atMillis, b.atMillis);
        Collections.sort(items, cmp);
        notifyDataSetChanged();
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
        TextView tvAvatar, tvAction, tvUserSub, tvWhen;

        VH(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvUserSub = itemView.findViewById(R.id.tvUserSub);
            tvWhen = itemView.findViewById(R.id.tvWhen);
        }

        void bind(LogItem it, Listener listener) {
            // Avatar
            tvAvatar.setText(it.user.getInitial());

            // Acción
            tvAction.setText(it.action);

            // Subtítulo
            String doc = it.user.getDocType() != null ? it.user.getDocType() : "DNI";
            String roleText = roleToText(it.role);
            String sub = roleText + " • " + doc + " " + it.user.getDni();
            tvUserSub.setText(sub);

            // Fecha relativa
            tvWhen.setText(relative(it.atMillis));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(it);
            });
        }

        private static String roleToText(Role r) {
            switch (r) {
                case ADMIN: return "Admin";
                case GUIDE: return "Guía";
                case CLIENT: default: return "Cliente";
            }
        }

        private static String relative(long t) {
            long now = System.currentTimeMillis();
            long msDay = 24L * 60 * 60 * 1000;
            long diff = now - startOfDay(now) - (t - startOfDay(t));
            // Sencillo: Hoy/Ayer/fecha corta
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

        private static long startOfDay(long t) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTimeInMillis(t);
            c.set(java.util.Calendar.HOUR_OF_DAY, 0);
            c.set(java.util.Calendar.MINUTE, 0);
            c.set(java.util.Calendar.SECOND, 0);
            c.set(java.util.Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        }
    }
}

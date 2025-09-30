package com.example.connectifyproject.views.superadmin.users;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.model.Role;
import com.example.connectifyproject.model.User;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SaUsersAdapter extends RecyclerView.Adapter<SaUsersAdapter.VH> {

    public interface OnUserClickListener {
        void onView(User u);
    }

    private final List<User> full = new ArrayList<>();
    private final List<User> items = new ArrayList<>();
    private final OnUserClickListener listener;

    private EnumSet<Role> roleFilter = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
    private String query = "";

    public SaUsersAdapter(List<User> initial, OnUserClickListener listener) {
        this.listener = listener;
        replaceAll(initial);
    }

    public void replaceAll(List<User> data) {
        full.clear();
        if (data != null) full.addAll(data);
        applyFilters();
    }

    public void setQuery(String q) {
        this.query = q == null ? "" : q.trim().toLowerCase();
        applyFilters();
    }

    public void setRoleFilter(EnumSet<Role> roles) {
        this.roleFilter = (roles == null || roles.isEmpty())
                ? EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT)
                : roles;
        applyFilters();
    }

    private void applyFilters() {
        items.clear();
        for (User u : full) {
            if (!roleFilter.contains(u.getRole())) continue;

            if (!query.isEmpty()) {
                String haystack = (s(u.getName()) + " " + s(u.getLastName()) + " "
                        + s(u.getDni()) + " " + s(u.getCompany())).toLowerCase();
                if (!haystack.contains(query)) continue;
            }
            items.add(u);
        }
        notifyDataSetChanged();
    }

    static String s(String v) { return v == null ? "" : v; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sa_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvSub;
        ImageButton btnProfile;

        VH(@NonNull View itemView) {
            super(itemView);
            tvAvatar   = itemView.findViewById(R.id.tvAvatar);
            tvName     = itemView.findViewById(R.id.tvName);
            tvSub      = itemView.findViewById(R.id.tvSub);
            btnProfile = itemView.findViewById(R.id.btnProfile);
        }

        void bind(User u, OnUserClickListener listener) {
            // Avatar: inicial
            tvAvatar.setText(u.getInitial());

            // -> Color del avatar desde paleta fija
            int color = pickAvatarColor(itemView.getContext(), u);
            Drawable bg = tvAvatar.getBackground();
            if (bg != null) {
                bg = bg.mutate();
                DrawableCompat.setTint(bg, color);
                tvAvatar.setBackground(bg);
            } else {
                tvAvatar.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            // Nombre
            String fullName = (u.getName() == null ? "" : u.getName());
            if (u.getLastName() != null && !u.getLastName().isEmpty()) {
                fullName += " " + u.getLastName();
            }
            tvName.setText(fullName.trim());

            // Subtítulo: DOC + DNI • Empresa
            String doc = u.getDocType() != null ? u.getDocType() : "DNI";
            String sub = doc + " " + (u.getDni() == null ? "" : u.getDni());
            if (u.getCompany() != null && !u.getCompany().isEmpty()) {
                sub += " • " + u.getCompany();
            }
            tvSub.setText(sub);

            View.OnClickListener open = v -> { if (listener != null) listener.onView(u); };
            itemView.setOnClickListener(open);   // click en toda la tarjeta
            btnProfile.setOnClickListener(open); // click en la lupa
        }

        /** Paleta: #08807B, #F1A20B, #8D9C09, #D20D20 elegida por hash */
        private static int pickAvatarColor(android.content.Context ctx, User u) {
            int[] palette = new int[] {
                    ContextCompat.getColor(ctx, R.color.avatar_teal),   // #08807B
                    ContextCompat.getColor(ctx, R.color.avatar_amber),  // #F1A20B
                    ContextCompat.getColor(ctx, R.color.avatar_olive),  // #8D9C09
                    ContextCompat.getColor(ctx, R.color.avatar_red)     // #D20D20
            };
            String key = (s(u.getName()) + s(u.getLastName()) + s(u.getDni()));
            int idx = Math.abs(key.hashCode()) % palette.length;
            return palette[idx];
        }
    }
}

package com.example.connectifyproject.views.superadmin.users;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
                        + s(u.getDni()) + " " + s(u.getEmail()) + " " + roleToString(u.getRole())).toLowerCase();
                if (!haystack.contains(query)) continue;
            }
            items.add(u);
        }
        notifyDataSetChanged();
    }

    private static String roleToString(Role r) {
        if (r == null) return "";
        switch (r) {
            case GUIDE: return "Guía";
            case ADMIN: return "Administrador";
            case CLIENT: return "Cliente";
            default: return "";
        }
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
        ImageView ivAvatar;
        TextView tvName, tvSub;
        View vStatusIndicator;

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar          = itemView.findViewById(R.id.ivAvatar);
            tvName            = itemView.findViewById(R.id.tvName);
            tvSub             = itemView.findViewById(R.id.tvSub);
            vStatusIndicator  = itemView.findViewById(R.id.vStatusIndicator);
        }

        void bind(User u, OnUserClickListener listener) {
            // Determinar si el perfil está incompleto
            boolean isIncomplete = !u.isProfileComplete();
            
            // Cargar foto de perfil con Glide
            Glide.with(itemView.getContext())
                    .load(u.getPhotoUri())
                    .circleCrop()
                    .placeholder(R.drawable.ic_account_circle_24)
                    .error(R.drawable.ic_account_circle_24)
                    .into(ivAvatar);

            // Nombre completo
            String fullName = (u.getName() == null ? "" : u.getName());
            if (u.getLastName() != null && !u.getLastName().isEmpty()) {
                fullName += " " + u.getLastName();
            }
            tvName.setText(fullName.trim());

            // Subtítulo: Para admins incompletos mostrar correo, para otros mostrar DOC + DNI • Rol
            String sub;
            if (isIncomplete && u.getRole() == Role.ADMIN) {
                // Admin incompleto: mostrar correo
                sub = u.getEmail() != null ? u.getEmail() : "";
            } else {
                // Usuario normal: DOC + DNI • Rol
                String doc = u.getDocType() != null ? u.getDocType() : "DNI";
                sub = doc + " " + (u.getDni() == null ? "" : u.getDni());
            }
            String rol = roleToString(u.getRole());
            if (!rol.isEmpty()) {
                sub += " • " + rol;
            }
            tvSub.setText(sub);

            // Indicador de estado: verde si habilitado, rojo si no
            int statusColor;
            if (u.isEnabled()) {
                statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_enabled);
            } else {
                statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_disabled);
            }
            vStatusIndicator.setBackgroundTintList(ColorStateList.valueOf(statusColor));

            // Si el perfil está incompleto, aplicar sombreado y deshabilitar click
            if (isIncomplete) {
                itemView.setAlpha(0.5f); // Sombreado
                itemView.setOnClickListener(null); // No clickeable
                itemView.setClickable(false);
            } else {
                itemView.setAlpha(1.0f); // Sin sombreado
                View.OnClickListener open = v -> { if (listener != null) listener.onView(u); };
                itemView.setOnClickListener(open);
                itemView.setClickable(true);
            }
        }

        private static String roleToString(Role r) {
            if (r == null) return "";
            switch (r) {
                case GUIDE: return "Guía";
                case ADMIN: return "Administrador";
                case CLIENT: return "Cliente";
                default: return "";
            }
        }
    }
}

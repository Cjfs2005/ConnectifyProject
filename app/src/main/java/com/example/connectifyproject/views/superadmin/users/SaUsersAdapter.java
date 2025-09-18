package com.example.connectifyproject.views.superadmin.users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.model.Role;
import com.example.connectifyproject.model.User;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class SaUsersAdapter extends RecyclerView.Adapter<SaUsersAdapter.VH> {

    public interface OnUserClick { void onProfile(User u); }

    private final List<User> full = new ArrayList<>();
    private final List<User> shown = new ArrayList<>();
    @SuppressWarnings("unused")
    private final OnUserClick listener; // mantenido por compatibilidad

    private EnumSet<Role> roleFilter = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
    private String query = "";

    // Comparador alfabético (ES) que ignora tildes y mayúsculas
    private final Collator collator = Collator.getInstance(new Locale("es", "ES"));
    private final Comparator<User> byNameComparator = (a, b) -> {
        String na = a.getName() == null ? "" : a.getName();
        String nb = b.getName() == null ? "" : b.getName();
        return collator.compare(na, nb);
    };

    public SaUsersAdapter(List<User> data, OnUserClick listener) {
        collator.setStrength(Collator.PRIMARY);
        if (data != null) full.addAll(data);
        this.listener = listener;
        applyFilter();
    }

    public void setRoleFilter(EnumSet<Role> roles) {
        roleFilter = roles != null ? roles : EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
        applyFilter();
    }

    public void setQuery(String q) {
        query = (q == null) ? "" : q.trim();
        applyFilter();
    }

    private void applyFilter() {
        shown.clear();
        String q = query.toLowerCase(Locale.ROOT);

        for (User u : full) {
            boolean roleOk = roleFilter.contains(u.getRole());
            boolean textOk = q.isEmpty()
                    || contains(u.getName(), q)
                    || contains(u.getDni(), q)
                    || contains(u.getCompany(), q);

            if (roleOk && textOk) shown.add(u);
        }

        Collections.sort(shown, byNameComparator);
        notifyDataSetChanged();
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    // <-- NUEVO: calcula la inicial del nombre
    private String initialOf(String name) {
        if (name == null) return "?";
        String t = name.trim();
        if (t.isEmpty()) return "?";
        int cp = t.codePointAt(0);
        return new String(Character.toChars(Character.toUpperCase(cp)));
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sa_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        User u = shown.get(position);

        // Antes: h.tvAvatar.setText(u.getInitial());
        h.tvAvatar.setText(initialOf(u.getName()));
        h.tvName.setText(u.getName());

        // Subtítulo "DNI • Empresa"
        StringBuilder sub = new StringBuilder();
        if (u.getDni() != null && !u.getDni().isEmpty()) sub.append(u.getDni());
        if (u.getCompany() != null && !u.getCompany().isEmpty()) {
            if (sub.length() > 0) sub.append(" • ");
            sub.append(u.getCompany());
        }
        h.tvSub.setText(sub.toString());

        h.btnProfile.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("name",    u.getName());
            args.putString("dni",     u.getDni());
            args.putString("company", u.getCompany());
            args.putString("role",    u.getRole() != null ? u.getRole().name() : "");
            if (u.getBirth()    != null) args.putString("birth",    u.getBirth());
            if (u.getEmail()    != null) args.putString("email",    u.getEmail());
            if (u.getPhone()    != null) args.putString("phone",    u.getPhone());
            if (u.getAddress()  != null) args.putString("address",  u.getAddress());
            if (u.getPhotoUri() != null) args.putString("photoUri", u.getPhotoUri());
            Navigation.findNavController(v).navigate(R.id.saUserDetailFragment, args);
        });
    }

    @Override
    public int getItemCount() { return shown.size(); }

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
    }
}

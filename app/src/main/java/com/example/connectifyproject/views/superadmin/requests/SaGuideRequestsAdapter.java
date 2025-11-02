package com.example.connectifyproject.views.superadmin.requests;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.R;
import com.example.connectifyproject.model.User;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SaGuideRequestsAdapter extends RecyclerView.Adapter<SaGuideRequestsAdapter.VH> {

    public enum SortOrder { RECENT, OLD }

    public interface Listener {
        void onSelectionChanged(int count);
        void onOpen(GuideRequest req);
    }

    public static class GuideRequest {
        public final User user;
        public final long requestedAt;  // epoch millis
        public boolean selected;

        public GuideRequest(User user, long requestedAt) {
            this.user = user;
            this.requestedAt = requestedAt;
        }
    }

    private final List<GuideRequest> full = new ArrayList<>();
    private final List<GuideRequest> items = new ArrayList<>();
    private SortOrder sort = SortOrder.RECENT;
    private String q = "";
    private Listener listener;
    private boolean ready = false; // evita callbacks durante el constructor

    public SaGuideRequestsAdapter(List<GuideRequest> initial, Listener listener) {
        this.listener = null;
        replaceAll(initial); // no notifica aún
        this.listener = listener;
        this.ready = true;
    }

    public void replaceAll(List<GuideRequest> data) {
        full.clear();
        if (data != null) full.addAll(data);
        apply();
    }

    public void setQuery(String query) {
        this.q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        apply();
    }

    public void setSort(SortOrder order) {
        this.sort = order == null ? SortOrder.RECENT : order;
        apply();
    }

    public void selectAll(boolean select) {
        for (GuideRequest r : items) r.selected = select;
        notifyDataSetChanged();
        if (ready && listener != null) listener.onSelectionChanged(getSelectedCount());
    }

    public int getSelectedCount() {
        int n = 0;
        for (GuideRequest r : items) if (r.selected) n++;
        return n;
    }

    public List<GuideRequest> getSelected() {
        List<GuideRequest> out = new ArrayList<>();
        for (GuideRequest r : items) if (r.selected) out.add(r);
        return out;
    }

    private void apply() {
        items.clear();

        // filter
        for (GuideRequest r : full) {
            User u = r.user;
            String haystack = (s(u.getName()) + " " + s(u.getLastName()) + " " +
                    s(u.getDni())).toLowerCase(Locale.ROOT);
            if (!q.isEmpty() && !haystack.contains(q)) continue;
            items.add(r);
        }

        // sort
        Comparator<GuideRequest> cmp = (a, b) -> Long.compare(a.requestedAt, b.requestedAt);
        if (sort == SortOrder.RECENT) {
            cmp = (a, b) -> Long.compare(b.requestedAt, a.requestedAt);
        }
        Collections.sort(items, cmp);

        notifyDataSetChanged();

        if (ready && listener != null) listener.onSelectionChanged(getSelectedCount());
    }

    private static String s(String x) { return x == null ? "" : x; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guide_request, parent, false);
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
        MaterialCheckBox cb;

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName   = itemView.findViewById(R.id.tvName);
            tvSub    = itemView.findViewById(R.id.tvSub);
            cb       = itemView.findViewById(R.id.cbSelect);
        }

        void bind(GuideRequest r, Listener listener) {
            User u = r.user;

            // Cargar imagen de perfil con Glide
            Glide.with(itemView.getContext())
                    .load(u.getPhotoUri())
                    .circleCrop()
                    .placeholder(R.drawable.ic_account_circle_24)
                    .error(R.drawable.ic_account_circle_24)
                    .into(ivAvatar);

            String fullName = (u.getName() == null ? "" : u.getName());
            if (u.getLastName() != null && !u.getLastName().isEmpty()) {
                fullName += " " + u.getLastName();
            }
            tvName.setText(fullName.trim());

            String doc = u.getDocType() != null ? u.getDocType() : "DNI";
            String sub = doc + " " + (u.getDni() == null ? "" : u.getDni());
            tvSub.setText(sub);

            // checkbox selecciona
            cb.setOnCheckedChangeListener(null);
            cb.setChecked(r.selected);
            cb.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                r.selected = isChecked;
                if (listener != null) listener.onSelectionChanged(-1);
            });

            // tocar la tarjeta abre el detalle
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOpen(r);
            });
        }
    }
}

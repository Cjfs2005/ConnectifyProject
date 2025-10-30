package com.example.connectifyproject.views.superadmin.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SaNotificationsAdapter extends ListAdapter<NotificationItem, SaNotificationsAdapter.VH> {

    public interface Listener {
        void onOpen(NotificationItem item, int position);
        void onDelete(NotificationItem item, int position);
    }

    private final Listener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public SaNotificationsAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationItem it = getItem(position);
        h.tvTitle.setText(it.getTitle());
        h.tvMessage.setText(it.getMessage());
        h.tvDate.setText(sdf.format(it.getCreatedAtMillis()));
        h.dotUnread.setVisibility(it.isRead() ? View.INVISIBLE : View.VISIBLE);

        h.itemView.setOnClickListener(v -> {
            if (!it.isRead()) {
                it.setRead(true);
                notifyItemChanged(h.getAdapterPosition());
            }
            if (listener != null) listener.onOpen(it, h.getAdapterPosition());
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(it, h.getAdapterPosition());
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        View dotUnread; TextView tvTitle, tvMessage, tvDate; ImageButton btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            dotUnread = itemView.findViewById(R.id.dotUnread);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private static final DiffUtil.ItemCallback<NotificationItem> DIFF =
            new DiffUtil.ItemCallback<NotificationItem>() {
                @Override public boolean areItemsTheSame(@NonNull NotificationItem a, @NonNull NotificationItem b) { return a.getId().equals(b.getId()); }
                @Override public boolean areContentsTheSame(@NonNull NotificationItem a, @NonNull NotificationItem b) {
                    return a.isRead() == b.isRead()
                            && a.getUserFullName().equals(b.getUserFullName())
                            && a.getCreatedAtMillis() == b.getCreatedAtMillis();
                }
            };
}

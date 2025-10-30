package com.example.connectifyproject.adapters;

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
import com.example.connectifyproject.views.superadmin.notifications.NotificationItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Adapter compatible con:
 *  - item_notification.xml (IDs: dotUnread, tvTitle, tvMessage, tvDate, btnDelete)
 *  - Constructor que recibe List<NotificationAdapter.NotificationData> (como usa cliente_notificaciones.java)
 *  - Constructor que recibe Listener (para tu flujo de Superadmin)
 */
public class NotificationAdapter extends ListAdapter<NotificationItem, NotificationAdapter.VH> {

    /** Listener (opcional). Si no se pasa, se usa NO_OP_LISTENER para evitar NPE. */
    public interface Listener {
        void onOpen(NotificationItem item, int position);
        void onDelete(NotificationItem item, int position);
        void onMarkedRead(NotificationItem item, int position);
    }

    /** Clase de compatibilidad usada por cliente_notificaciones.java */
    public static class NotificationData {
        public final String title;     // "Recuerda dejar tu reseña"
        public final String message;   // "Tu tour ha finalizado..."
        public final String timeHHmm;  // "15:40"
        public final String dateMMdd;  // "02/09"

        public NotificationData(String title, String message, String timeHHmm, String dateMMdd) {
            this.title = title;
            this.message = message;
            this.timeHHmm = timeHHmm;
            this.dateMMdd = dateMMdd;
        }
    }

    // ---- Campos ----
    private Listener listener;
    private final SimpleDateFormat uiSdf   = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
    private final SimpleDateFormat parseSdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

    // ---- Constructor para tu flujo (Superadmin) ----
    public NotificationAdapter(@NonNull Listener listener) {
        super(DIFF);
        this.listener = (listener != null) ? listener : NO_OP_LISTENER;
    }

    // ---- Constructor COMPATIBLE con cliente_notificaciones.java ----
    public NotificationAdapter(@NonNull List<NotificationData> data) {
        super(DIFF);
        this.listener = NO_OP_LISTENER;
        submitData(data);
    }

    /** Permite setear/actualizar listener si usaste el ctor con lista */
    public void setListener(Listener l) { this.listener = (l != null) ? l : NO_OP_LISTENER; }

    /** Mapea List<NotificationData> → List<NotificationItem> y publica en el ListAdapter */
    public void submitData(List<NotificationData> data) {
        List<NotificationItem> mapped = new ArrayList<>();
        if (data != null) {
            for (NotificationData d : data) {
                long when = buildEpochFrom(d.dateMMdd, d.timeHHmm);
                String id = "c_" + Math.abs((d.title + when).hashCode()); // ID estable
                // 👉 Usa el constructor de 6 parámetros (modo CLIENTE) del NotificationItem
                mapped.add(new NotificationItem(id, d.title, d.message, when, false, true));
            }
        }
        submitList(mapped);
    }

    // ---------- ListAdapter ----------
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationItem it = getItem(position);

        h.tvTitle.setText(it.getTitle());
        h.tvMessage.setText(it.getMessage());
        h.tvDate.setText(uiSdf.format(it.getCreatedAtMillis()));
        h.dotUnread.setVisibility(it.isRead() ? View.INVISIBLE : View.VISIBLE);

        h.itemView.setOnClickListener(v -> {
            if (!it.isRead()) {
                it.setRead(true);
                notifyItemChanged(h.getAdapterPosition());
                listener.onMarkedRead(it, h.getAdapterPosition());
            }
            listener.onOpen(it, h.getAdapterPosition());
        });

        h.btnDelete.setOnClickListener(v -> listener.onDelete(it, h.getAdapterPosition()));
    }

    // ---------- ViewHolder ----------
    static class VH extends RecyclerView.ViewHolder {
        View dotUnread; TextView tvTitle, tvMessage, tvDate; ImageButton btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            dotUnread = itemView.findViewById(R.id.dotUnread);
            tvTitle   = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvDate    = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // ---------- Utilidades ----------
    private static final Listener NO_OP_LISTENER = new Listener() {
        @Override public void onOpen(NotificationItem item, int position) {}
        @Override public void onDelete(NotificationItem item, int position) {}
        @Override public void onMarkedRead(NotificationItem item, int position) {}
    };

    /** Construye epoch (millis) a partir de "MM/dd" + "HH:mm" usando el año actual */
    private long buildEpochFrom(String mmdd, String hhmm) {
        try {
            String composed = (mmdd != null ? mmdd : "01/01") + " " + (hhmm != null ? hhmm : "00:00");
            long parsed = parseSdf.parse(composed).getTime();
            Calendar now = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(parsed);
            cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
            return cal.getTimeInMillis();
        } catch (ParseException | NullPointerException e) {
            return System.currentTimeMillis();
        }
    }

    /** DiffUtil */
    private static final DiffUtil.ItemCallback<NotificationItem> DIFF =
            new DiffUtil.ItemCallback<NotificationItem>() {
                @Override public boolean areItemsTheSame(@NonNull NotificationItem a, @NonNull NotificationItem b) {
                    return a.getId().equals(b.getId());
                }
                @Override public boolean areContentsTheSame(@NonNull NotificationItem a, @NonNull NotificationItem b) {
                    boolean sameUser = (a.getUserFullName() == null && b.getUserFullName() == null)
                            || (a.getUserFullName() != null && a.getUserFullName().equals(b.getUserFullName()));
                    return a.isRead() == b.isRead()
                            && sameUser
                            && a.getCreatedAtMillis() == b.getCreatedAtMillis()
                            && a.getTitle().equals(b.getTitle())
                            && a.getMessage().equals(b.getMessage());
                }
            };
}
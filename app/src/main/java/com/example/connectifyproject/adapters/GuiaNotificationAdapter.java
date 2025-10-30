package com.example.connectifyproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;

import java.util.List;

public class GuiaNotificationAdapter extends RecyclerView.Adapter<GuiaNotificationAdapter.NotificationViewHolder> {

    public static class NotificationData {
        public String title;
        public String message;
        public String time;
        public String date;

        public NotificationData(String title, String message, String time, String date) {
            this.title = title;
            this.message = message;
            this.time = time;
            this.date = date;
        }
    }

    private List<NotificationData> notifications;

    public GuiaNotificationAdapter(List<NotificationData> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.guia_item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationData notification = notifications.get(position);

        holder.tvTitle.setText(notification.title);
        holder.tvMessage.setText(notification.message);
        holder.tvTime.setText(notification.time);
        holder.tvDate.setText(notification.date);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateList(List<NotificationData> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime, tvDate;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
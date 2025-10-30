package com.example.connectifyproject.repository.notifications;

import androidx.lifecycle.LiveData;

import com.example.connectifyproject.views.superadmin.notifications.NotificationItem;

import java.util.List;

public interface NotificationsRepository {
    LiveData<List<NotificationItem>> getNotifications();   // observar lista
    void markAllRead();                                     // marcar todas leídas
    void deleteAll();                                       // borrar todas
    void deleteById(String id);                             // borrar una
    void markRead(String id);                               // marcar una leída
    void refresh();                                         // recargar (REST/DB)
}

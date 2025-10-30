package com.example.connectifyproject.viewmodel.superadmin.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.connectifyproject.repository.notifications.FakeNotificationsRepository;
import com.example.connectifyproject.repository.notifications.NotificationsRepository;
import com.example.connectifyproject.views.superadmin.notifications.NotificationItem;

import java.util.List;

public class SaNotificationsViewModel extends ViewModel {

    // Cambia esta línea por tu implementación real cuando tengas BD/REST:
    private final NotificationsRepository repo = new FakeNotificationsRepository();

    public LiveData<List<NotificationItem>> getNotifications() {
        return repo.getNotifications();
    }

    public void markAllRead() { repo.markAllRead(); }
    public void deleteAll()   { repo.deleteAll(); }
    public void refresh()     { repo.refresh(); }
    public void deleteById(String id) { repo.deleteById(id); }
    public void markRead(String id)   { repo.markRead(id); }
}

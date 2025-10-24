package com.example.connectifyproject.repository.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.connectifyproject.views.superadmin.notifications.NotificationItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FakeNotificationsRepository implements NotificationsRepository {

    private final MutableLiveData<List<NotificationItem>> live = new MutableLiveData<>(new ArrayList<>());

    public FakeNotificationsRepository() { seed(); }

    private void seed() {
        long now = System.currentTimeMillis();
        List<NotificationItem> list = new ArrayList<>();
        list.add(new NotificationItem("n1", "Juan Pérez",      now -  45 * 60 * 1000, false));
        list.add(new NotificationItem("n2", "María Torres",    now -  2  * 60 * 60 * 1000, false));
        list.add(new NotificationItem("n3", "Ana Rodríguez",   now - 26  * 60 * 60 * 1000, true));
        live.postValue(list);
    }

    @Override public LiveData<List<NotificationItem>> getNotifications() { return live; }

    @Override public void markAllRead() {
        List<NotificationItem> cur = new ArrayList<>(safe());
        for (NotificationItem it : cur) it.setRead(true);
        live.postValue(cur);
    }

    @Override public void deleteAll() { live.postValue(new ArrayList<>()); }

    @Override public void deleteById(String id) {
        List<NotificationItem> cur = new ArrayList<>(safe());
        Iterator<NotificationItem> it = cur.iterator();
        while (it.hasNext()) if (it.next().getId().equals(id)) { it.remove(); break; }
        live.postValue(cur);
    }

    @Override public void markRead(String id) {
        List<NotificationItem> cur = new ArrayList<>(safe());
        for (NotificationItem it : cur) if (it.getId().equals(id)) { it.setRead(true); break; }
        live.postValue(cur);
    }

    @Override public void refresh() { seed(); }

    private List<NotificationItem> safe() { return live.getValue() == null ? new ArrayList<>() : live.getValue(); }
}

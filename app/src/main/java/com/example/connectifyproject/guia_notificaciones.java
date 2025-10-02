package com.example.connectifyproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.connectifyproject.adapters.GuiaNotificationAdapter;
import com.example.connectifyproject.databinding.GuiaNotificacionesBinding;
import java.util.ArrayList;
import java.util.List;

public class guia_notificaciones extends AppCompatActivity {

    private GuiaNotificacionesBinding binding;
    private GuiaNotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaNotificacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        loadNotifications();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuiaNotificationAdapter(new ArrayList<>());
        binding.recyclerViewNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        List<GuiaNotificationAdapter.NotificationData> backendNotifications = getBackendNotifications();
        adapter.updateList(backendNotifications);
    }

    private List<GuiaNotificationAdapter.NotificationData> getBackendNotifications() {
        List<GuiaNotificationAdapter.NotificationData> notifications = new ArrayList<>();
        notifications.add(new GuiaNotificationAdapter.NotificationData(
                "Ha llegado una nueva oferta de tour",
                "Nueva oferta disponible para 'Tour a Machu Picchu' el 05/10/2025.",
                "18:00",
                "02/10"
        ));
        notifications.add(new GuiaNotificationAdapter.NotificationData(
                "Tiene un tour asignado",
                "Tour asignado para mañana a las 12:40 en Centro de Lima.",
                "17:50",
                "02/10"
        ));
        return notifications;
    }
}
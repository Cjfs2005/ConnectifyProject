package com.example.connectifyproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.connectifyproject.adapters.GuiaNotificationAdapter;
import com.example.connectifyproject.databinding.GuiaNotificacionesBinding;
import java.util.ArrayList;
import java.util.List;

public class admin_notificaciones extends AppCompatActivity {

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
                "Pago recibido",
                "Se ha registrado el pago de S/ 250.00 por el tour 'Cusco Mágico'.",
                "09:30",
                "16/12"
        ));
        notifications.add(new GuiaNotificationAdapter.NotificationData(
                "Nuevo mensaje en el chat",
                "Tienes un nuevo mensaje de un cliente en el chat de atención.",
                "08:45",
                "16/12"
        ));
        notifications.add(new GuiaNotificationAdapter.NotificationData(
                "Tour modificado",
                "El tour 'Lima Colonial' ha sido actualizado.",
                "08:00",
                "15/12"
        ));
        return notifications;
    }
}

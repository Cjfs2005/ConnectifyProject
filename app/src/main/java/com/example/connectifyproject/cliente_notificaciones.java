package com.example.connectifyproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.NotificationAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class cliente_notificaciones extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_notificaciones);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView_notifications);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<NotificationAdapter.NotificationData> notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        // Datos hardcodeados de notificaciones - simula datos del backend
        List<NotificationAdapter.NotificationData> backendNotifications = getBackendNotifications();
        
        // Limpiar y agregar nuevas notificaciones
        adapter = new NotificationAdapter(backendNotifications);
        recyclerView.setAdapter(adapter);
    }

    private List<NotificationAdapter.NotificationData> getBackendNotifications() {
        // Simular respuesta del backend con notificaciones hardcodeadas
        List<NotificationAdapter.NotificationData> notifications = new ArrayList<>();
        
        // Año 2025 como aparece en la imagen
        notifications.add(new NotificationAdapter.NotificationData(
            "Recuerda dejar tu reseña",
            "Tu tour ha finalizado. Ayúdanos a mejorar compartiendo tu valoración y comentarios.",
            "15:40",
            "02/09",
            R.drawable.ic_bell
        ));
        
        notifications.add(new NotificationAdapter.NotificationData(
            "Alerta de chat",
            "Tienes un nuevo mensaje de la empresa 'Tours Perú' en el chat de atención al cliente.",
            "15:30",
            "02/09",
            R.drawable.ic_nav_chat
        ));
        
        notifications.add(new NotificationAdapter.NotificationData(
            "Cobro procesado",
            "Se ha realizado el cargo a tu tarjeta por el monto de S/ 150.00 correspondiente al tour 'Descubre Lima Por favor'.",
            "15:20",
            "02/09",
            R.drawable.ic_money
        ));
        
        notifications.add(new NotificationAdapter.NotificationData(
            "Has realizado el Check-out exitosamente",
            "Por favor, califica tu experiencia y deja observaciones sobre el tour.",
            "15:15",
            "02/09",
            R.drawable.ic_calendar
        ));
        
        notifications.add(new NotificationAdapter.NotificationData(
            "Has alcanzado un nuevo punto en el itinerario",
            "Estás ahora en Museo Larco. La siguiente parada es Plaza de Armas.",
            "14:00",
            "02/09",
            R.drawable.ic_bus
        ));
        
        notifications.add(new NotificationAdapter.NotificationData(
            "Has alcanzado un nuevo punto en el itinerario",
            "Estás ahora en Centro de Lima. La siguiente parada es Museo Larco.",
            "13:10",
            "02/09",
            R.drawable.ic_bus
        ));
        
        notifications.add(new NotificationAdapter.NotificationData(
            "¡Tu tour está por comenzar!",
            "El guía ha llegado al punto de inicio. Por favor, preséntate al QR físico para realizar el Check-in.",
            "12:50",
            "02/09",
            R.drawable.ic_calendar
        ));
        
        return notifications;
    }
}
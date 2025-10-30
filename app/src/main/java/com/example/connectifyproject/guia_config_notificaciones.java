package com.example.connectifyproject;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.databinding.GuiaConfigNotificacionesBinding;
import com.example.connectifyproject.service.GuiaNotificationService;
import com.example.connectifyproject.storage.GuiaPreferencesManager;

public class guia_config_notificaciones extends AppCompatActivity {

    private GuiaConfigNotificacionesBinding binding;
    private GuiaPreferencesManager preferencesManager;
    private GuiaNotificationService notificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaConfigNotificacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar servicios
        preferencesManager = new GuiaPreferencesManager(this);
        notificationService = new GuiaNotificationService(this);

        setupToolbar();
        loadCurrentSettings();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configuración de Notificaciones");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCurrentSettings() {
        // Cargar configuraciones actuales
        binding.switchNewOffers.setChecked(preferencesManager.isNotificationEnabled("new_offers"));
        binding.switchTourReminders.setChecked(preferencesManager.isNotificationEnabled("tour_reminders"));
        binding.switchLocationReminders.setChecked(preferencesManager.isNotificationEnabled("location_reminders"));
        binding.switchCheckinReminders.setChecked(preferencesManager.isNotificationEnabled("checkin_reminders"));
        binding.switchCheckoutReminders.setChecked(preferencesManager.isNotificationEnabled("checkout_reminders"));
        
        // Cargar días de anticipación
        binding.spinnerReminderDays.setSelection(preferencesManager.getReminderDaysAhead() - 1);
    }

    private void setupListeners() {
        // Switches de notificaciones
        binding.switchNewOffers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationEnabled("new_offers", isChecked);
            showSettingSavedMessage("Ofertas nuevas");
        });

        binding.switchTourReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationEnabled("tour_reminders", isChecked);
            showSettingSavedMessage("Recordatorios de tours");
        });

        binding.switchLocationReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationEnabled("location_reminders", isChecked);
            showSettingSavedMessage("Recordatorios de ubicación");
        });

        binding.switchCheckinReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationEnabled("checkin_reminders", isChecked);
            showSettingSavedMessage("Recordatorios de check-in");
        });

        binding.switchCheckoutReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationEnabled("checkout_reminders", isChecked);
            showSettingSavedMessage("Recordatorios de check-out");
        });

        // Botones de prueba
        binding.btnTestNewOffer.setOnClickListener(v -> {
            if (preferencesManager.isNotificationEnabled("new_offers")) {
                notificationService.sendNewTourOfferNotification(
                    "Tour de Prueba", "Empresa Test", 300.0
                );
                Toast.makeText(this, "🎯 Notificación de prueba enviada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Notificación desactivada", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnTestTourReminder.setOnClickListener(v -> {
            if (preferencesManager.isNotificationEnabled("tour_reminders")) {
                notificationService.sendTourReminderNotification(
                    "Tour de Prueba", "24/10/2025", "10:00 AM", 1
                );
                Toast.makeText(this, "📅 Recordatorio de prueba enviado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Recordatorio desactivado", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnTestLocationReminder.setOnClickListener(v -> {
            if (preferencesManager.isNotificationEnabled("location_reminders")) {
                notificationService.sendLocationReminderNotification("Plaza de Armas");
                Toast.makeText(this, "📍 Recordatorio de ubicación enviado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Recordatorio desactivado", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnTestCheckinReminder.setOnClickListener(v -> {
            if (preferencesManager.isNotificationEnabled("checkin_reminders")) {
                notificationService.sendCheckInReminderNotification("Tour de Prueba");
                Toast.makeText(this, "✅ Check-in de prueba enviado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Check-in desactivado", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnTestCheckoutReminder.setOnClickListener(v -> {
            if (preferencesManager.isNotificationEnabled("checkout_reminders")) {
                notificationService.sendCheckOutReminderNotification("Tour de Prueba");
                Toast.makeText(this, "🏁 Check-out de prueba enviado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Check-out desactivado", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para ver configuraciones en log
        binding.btnShowConfig.setOnClickListener(v -> {
            preferencesManager.exportPreferencesToLog();
            Toast.makeText(this, "📝 Configuraciones mostradas en el log", Toast.LENGTH_SHORT).show();
        });
    }

    private void showSettingSavedMessage(String setting) {
        Toast.makeText(this, "✅ " + setting + " configurado", Toast.LENGTH_SHORT).show();
    }
}
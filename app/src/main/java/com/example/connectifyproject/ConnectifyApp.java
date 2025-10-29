package com.example.connectifyproject;

import android.app.Application;

import com.example.connectifyproject.utils.NotificationHelper;

public class ConnectifyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Crear canales de notificación al iniciar la app
        NotificationHelper.createChannels(this);
    }
}
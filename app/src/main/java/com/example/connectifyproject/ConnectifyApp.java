package com.example.connectifyproject;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Build;

import com.example.connectifyproject.utils.NotificationHelper;

import java.util.Locale;

public class ConnectifyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Configurar idioma español por defecto
        setAppLocale("es");
        
        // Crear canales de notificación al iniciar la app
        NotificationHelper.createChannels(this);
    }
    
    /**
     * Configurar el idioma de la aplicación
     */
    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
package com.example.connectifyproject;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import com.example.connectifyproject.utils.NotificationHelper;
import com.example.connectifyproject.workers.TourCancelationWorker;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ConnectifyApp extends Application {
    private static final String TAG = "ConnectifyApp";
    private static final String TOUR_CANCELATION_WORK = "tour_cancelation_periodic";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Configurar idioma español por defecto
        setAppLocale("es");
        
        // Crear canales de notificación al iniciar la app
        NotificationHelper.createChannels(this);
        
        // ✅ Configurar WorkManager para cancelación automática de tours
        setupTourCancelationWorker();
    }
    
    /**
     * Configura el worker periódico para cancelar tours sin participantes
     * Se ejecuta cada 30 minutos
     */
    private void setupTourCancelationWorker() {
        try {
            // Restricciones: solo cuando hay conexión de red
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Crear trabajo periódico cada 30 minutos
            PeriodicWorkRequest cancelationWork = new PeriodicWorkRequest.Builder(
                    TourCancelationWorker.class,
                    30, // Intervalo
                    TimeUnit.MINUTES,
                    15, // Flex interval
                    TimeUnit.MINUTES
            )
                    .setConstraints(constraints)
                    .addTag("tour_cancelation")
                    .build();

            // Encolar el trabajo (no duplicar si ya existe)
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    TOUR_CANCELATION_WORK,
                    ExistingPeriodicWorkPolicy.KEEP, // Mantener el existente
                    cancelationWork
            );

            Log.d(TAG, "✅ Worker de cancelación automática configurado (cada 30 minutos)");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error configurando WorkManager: " + e.getMessage(), e);
        }
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
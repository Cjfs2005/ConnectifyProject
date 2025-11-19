package com.example.connectifyproject.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.connectifyproject.R;
import com.example.connectifyproject.guia_assigned_tours;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * 📍 SERVICIO DE TRACKING DE UBICACIÓN EN TIEMPO REAL
 * 
 * Este servicio se ejecuta en foreground mientras un tour está en curso.
 * Actualiza la ubicación del guía en Firebase cada 15 segundos para que
 * el administrador pueda verlo en tiempo real.
 */
public class LocationTrackingService extends Service {
    
    private static final String TAG = "LocationTracking";
    private static final String CHANNEL_ID = "LocationTrackingChannel";
    private static final int NOTIFICATION_ID = 1001;
    
    // Actualizar cada 15 segundos
    private static final long UPDATE_INTERVAL = 15000; // 15 segundos
    private static final long FASTEST_INTERVAL = 10000; // 10 segundos
    
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private FirebaseFirestore db;
    private String tourId;
    private boolean isTracking = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Servicio de tracking creado");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            tourId = intent.getStringExtra("tourId");
            String action = intent.getStringExtra("action");
            
            if ("START".equals(action) && tourId != null) {
                Log.d(TAG, "Iniciando tracking para tour: " + tourId);
                startForeground(NOTIFICATION_ID, getNotification());
                startLocationUpdates();
            } else if ("STOP".equals(action)) {
                Log.d(TAG, "Deteniendo tracking");
                stopLocationUpdates();
                stopSelf();
            }
        }
        return START_STICKY;
    }
    
    /**
     * Iniciar actualizaciones de ubicación
     */
    private void startLocationUpdates() {
        if (isTracking) {
            Log.d(TAG, "Ya está rastreando ubicación");
            return;
        }
        
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .build();
        
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    updateLocationInFirebase(location);
                }
            }
        };
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, 
                locationCallback, 
                Looper.getMainLooper()
            );
            isTracking = true;
            Log.d(TAG, "Actualizaciones de ubicación iniciadas");
        } catch (SecurityException e) {
            Log.e(TAG, "Error de permisos de ubicación", e);
        }
    }
    
    /**
     * Actualizar ubicación en Firebase
     */
    private void updateLocationInFirebase(Location location) {
        if (tourId == null || tourId.isEmpty()) {
            Log.w(TAG, "tourId es null, no se puede actualizar ubicación");
            return;
        }
        
        Map<String, Object> ubicacion = new HashMap<>();
        ubicacion.put("latitud", location.getLatitude());
        ubicacion.put("longitud", location.getLongitude());
        ubicacion.put("precision", (double) location.getAccuracy());
        ubicacion.put("timestamp", Timestamp.now());
        ubicacion.put("velocidad", location.hasSpeed() ? (double) location.getSpeed() : 0.0);
        ubicacion.put("altitud", location.hasAltitude() ? location.getAltitude() : 0.0);
        
        db.collection("tours_asignados")
            .document(tourId)
            .update("ubicacionGuia", ubicacion)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, String.format("Ubicación actualizada: (%.6f, %.6f) - Precisión: %.1fm", 
                    location.getLatitude(), location.getLongitude(), location.getAccuracy()));
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al actualizar ubicación en Firebase", e);
            });
    }
    
    /**
     * Detener actualizaciones de ubicación
     */
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isTracking = false;
            Log.d(TAG, "Actualizaciones de ubicación detenidas");
        }
    }
    
    /**
     * Crear notificación de foreground
     */
    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, guia_assigned_tours.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tour en curso")
            .setContentText("Compartiendo ubicación en tiempo real")
            .setSmallIcon(R.drawable.ic_location) // Asegúrate de tener este icono
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true);
        
        return builder.build();
    }
    
    /**
     * Crear canal de notificaciones (Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Tracking de Ubicación",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notificación mientras se comparte la ubicación del tour");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Canal de notificaciones creado");
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Servicio destruido");
        stopLocationUpdates();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

package com.example.connectifyproject.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class GuiaLocationService extends Service {

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private List<LatLng> itineraryPoints = new ArrayList<>();
    private int currentPointIndex = 0;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tour Location Tracking")
                .setContentText("Tracking your location for the tour")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Use a suitable icon
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

                    // Broadcast current location
                    Intent intent = new Intent("LOCATION_UPDATE");
                    intent.putExtra("lat", location.getLatitude());
                    intent.putExtra("lng", location.getLongitude());
                    LocalBroadcastManager.getInstance(GuiaLocationService.this).sendBroadcast(intent);

                    // Check proximity
                    checkProximity(current);
                }
            }
        };

        startLocationUpdates();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Location Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel for location tracking");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            itineraryPoints = intent.getParcelableArrayListExtra("itinerary_points");
            // currentPointIndex could be loaded from shared prefs if needed
        }
        return START_STICKY;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void checkProximity(LatLng current) {
        if (currentPointIndex >= itineraryPoints.size()) return;

        LatLng next = itineraryPoints.get(currentPointIndex);
        float[] distance = new float[1];
        Location.distanceBetween(current.latitude, current.longitude, next.latitude, next.longitude, distance);

        if (distance[0] < 50) {
            currentPointIndex++;
            // Broadcast arrived
            Intent arrivedIntent = new Intent("ARRIVED_POINT");
            arrivedIntent.putExtra("index", currentPointIndex - 1);
            LocalBroadcastManager.getInstance(this).sendBroadcast(arrivedIntent);
            // Save currentPointIndex to shared prefs if needed
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
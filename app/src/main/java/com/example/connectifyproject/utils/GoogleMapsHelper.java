package com.example.connectifyproject.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleMapsHelper {
    private static final String TAG = "GoogleMapsHelper";
    private final Context context;
    private final Geocoder geocoder;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface LocationSearchCallback {
        void onLocationFound(String address, double latitude, double longitude);
        void onLocationNotFound();
        void onError(String error);
    }

    public GoogleMapsHelper(Context context) {
        this.context = context;
        this.geocoder = new Geocoder(context, Locale.getDefault());
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void searchLocation(String query, LocationSearchCallback callback) {
        executor.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressLine = address.getAddressLine(0);
                    if (addressLine == null) {
                        addressLine = buildAddressString(address);
                    }
                    
                    final String finalAddress = addressLine;
                    final double lat = address.getLatitude();
                    final double lng = address.getLongitude();
                    
                    mainHandler.post(() -> callback.onLocationFound(finalAddress, lat, lng));
                } else {
                    mainHandler.post(callback::onLocationNotFound);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error en búsqueda de ubicación: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error de conexión: " + e.getMessage()));
            }
        });
    }

    public void getAddressFromCoordinates(double latitude, double longitude, LocationSearchCallback callback) {
        executor.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressLine = address.getAddressLine(0);
                    if (addressLine == null) {
                        addressLine = buildAddressString(address);
                    }
                    
                    final String finalAddress = addressLine;
                    mainHandler.post(() -> callback.onLocationFound(finalAddress, latitude, longitude));
                } else {
                    mainHandler.post(callback::onLocationNotFound);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error al obtener dirección: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error de conexión: " + e.getMessage()));
            }
        });
    }

    private String buildAddressString(Address address) {
        StringBuilder addressBuilder = new StringBuilder();
        
        if (address.getThoroughfare() != null) {
            addressBuilder.append(address.getThoroughfare());
            if (address.getSubThoroughfare() != null) {
                addressBuilder.append(" ").append(address.getSubThoroughfare());
            }
        }
        
        if (address.getLocality() != null) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(address.getLocality());
        }
        
        if (address.getCountryName() != null) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(address.getCountryName());
        }
        
        return addressBuilder.toString();
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}

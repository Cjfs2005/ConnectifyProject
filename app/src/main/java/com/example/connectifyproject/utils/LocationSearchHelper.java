package com.example.connectifyproject.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LocationSearchHelper {
    private static final String TAG = "LocationSearchHelper";
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search";
    private Context context;

    public interface LocationSearchCallback {
        void onLocationFound(String address, double latitude, double longitude);
        void onLocationNotFound();
        void onError(String error);
    }

    public LocationSearchHelper(Context context) {
        this.context = context;
    }

    public void searchLocation(String query, LocationSearchCallback callback) {
        new SearchLocationTask(callback).execute(query);
    }

    private static class SearchLocationTask extends AsyncTask<String, Void, LocationResult> {
        private LocationSearchCallback callback;

        public SearchLocationTask(LocationSearchCallback callback) {
            this.callback = callback;
        }

        @Override
        protected LocationResult doInBackground(String... params) {
            try {
                String query = params[0];
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlString = NOMINATIM_API + "?q=" + encodedQuery + "&format=json&limit=1&addressdetails=1";
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "TourlyApp/1.0");
                connection.connect();

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONArray results = new JSONArray(response.toString());
                    if (results.length() > 0) {
                        JSONObject location = results.getJSONObject(0);
                        double lat = location.getDouble("lat");
                        double lon = location.getDouble("lon");
                        String address = location.getString("display_name");
                        
                        return new LocationResult(true, address, lat, lon, null);
                    } else {
                        return new LocationResult(false, null, 0, 0, "No se encontraron resultados");
                    }
                } else {
                    return new LocationResult(false, null, 0, 0, "Error en la respuesta del servidor");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching location", e);
                return new LocationResult(false, null, 0, 0, e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(LocationResult result) {
            if (callback != null) {
                if (result.success) {
                    callback.onLocationFound(result.address, result.latitude, result.longitude);
                } else if (result.error != null) {
                    callback.onError(result.error);
                } else {
                    callback.onLocationNotFound();
                }
            }
        }
    }

    private static class LocationResult {
        boolean success;
        String address;
        double latitude;
        double longitude;
        String error;

        LocationResult(boolean success, String address, double latitude, double longitude, String error) {
            this.success = success;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.error = error;
        }
    }
}

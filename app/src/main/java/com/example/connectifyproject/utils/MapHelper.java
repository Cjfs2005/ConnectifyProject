package com.example.connectifyproject.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.ProgressBar;

public class MapHelper {
    
    private WebView webView;
    private ProgressBar progressBar;
    private double currentLatitude = -12.0464; // Lima por defecto
    private double currentLongitude = -77.0428;
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;
    
    public MapHelper(WebView webView, ProgressBar progressBar) {
        this.webView = webView;
        this.progressBar = progressBar;
        setupWebView();
        loadDefaultMap();
    }
    
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Simplemente ocultar el progress bar cuando termine
                progressBar.setVisibility(View.GONE);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // En caso de error, ocultar progress bar
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    
    private void loadDefaultMap() {
        // Intentar cargar mapa de Lima por defecto con timeout más corto
        timeoutRunnable = () -> {
            progressBar.setVisibility(View.GONE);
            loadStaticMap();
        };
        timeoutHandler.postDelayed(timeoutRunnable, 5000); // 5 segundos para mapa inicial
        
        updateMapLocation(currentLatitude, currentLongitude);
    }
    
    public void updateMapLocation(double latitude, double longitude) {
        currentLatitude = latitude;
        currentLongitude = longitude;
        
        // Limpiar timeout anterior si existe
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        
        // NO mostrar ProgressBar - cargar directamente
        progressBar.setVisibility(View.GONE);
        
        // Cargar mapa inmediatamente sin timeout
        String mapHtml = generateMapHtml(latitude, longitude);
        webView.loadDataWithBaseURL(null, mapHtml, "text/html", "UTF-8", null);
    }
    
    private String generateMapHtml(double latitude, double longitude) {
        // Usar una implementación completamente local sin dependencias externas
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='utf-8' />" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Mapa de Ubicación</title>" +
                "<style>" +
                "body { margin: 0; padding: 0; font-family: Arial, sans-serif; }" +
                "#map { height: 100vh; width: 100%; background: linear-gradient(45deg, #E8F5E8 25%, #F0F8F0 25%, #F0F8F0 50%, #E8F5E8 50%, #E8F5E8 75%, #F0F8F0 75%, #F0F8F0); background-size: 20px 20px; position: relative; display: flex; align-items: center; justify-content: center; }" +
                ".marker { width: 40px; height: 40px; background: #FF4444; border-radius: 50% 50% 50% 0; transform: rotate(-45deg); border: 3px solid white; box-shadow: 0 3px 6px rgba(0,0,0,0.3); position: relative; z-index: 2; }" +
                ".marker::after { content: ''; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(45deg); width: 12px; height: 12px; background: white; border-radius: 50%; }" +
                ".info-panel { position: absolute; bottom: 20px; left: 20px; right: 20px; background: rgba(255,255,255,0.95); padding: 15px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.2); text-align: center; z-index: 3; }" +
                ".coordinates { font-size: 12px; color: #666; margin-top: 5px; }" +
                ".status { color: #4CAF50; font-weight: bold; margin-bottom: 5px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div id='map'>" +
                "<div class='marker'></div>" +
                "</div>" +
                "<div class='info-panel'>" +
                "<div class='status'>📍 Ubicación encontrada</div>" +
                "<div>Ubicación precisa encontrada</div>" +
                "<div class='coordinates'>Lat: " + String.format("%.6f", latitude) + " | Lng: " + String.format("%.6f", longitude) + "</div>" +
                "</div>" +
                "<script>" +
                "// Marcar como cargado inmediatamente" +
                "document.addEventListener('DOMContentLoaded', function() {" +
                "  console.log('Mapa cargado correctamente');" +
                "});" +
                "</script>" +
                "</body>" +
                "</html>";
    }
    
    private void loadStaticMap() {
        // Cargar un mapa estático simple en caso de error
        progressBar.setVisibility(View.GONE);
        String staticMapHtml = generateStaticMapHtml(currentLatitude, currentLongitude);
        webView.loadDataWithBaseURL(null, staticMapHtml, "text/html", "UTF-8", null);
    }
    
    private String generateStaticMapHtml(double latitude, double longitude) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='utf-8' />" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Mapa Estático</title>" +
                "<style>" +
                "body { margin: 0; padding: 0; background: #E8F5E8; font-family: Arial, sans-serif; }" +
                ".map-container { height: 100vh; display: flex; align-items: center; justify-content: center; flex-direction: column; }" +
                ".location-info { text-align: center; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".pin { font-size: 48px; color: #FF4444; margin-bottom: 10px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='map-container'>" +
                "<div class='location-info'>" +
                "<div class='pin'>📍</div>" +
                "<h3>Ubicación encontrada</h3>" +
                "<p><strong>Latitud:</strong> " + String.format("%.6f", latitude) + "</p>" +
                "<p><strong>Longitud:</strong> " + String.format("%.6f", longitude) + "</p>" +
                "<p><small>Mapa en modo sin conexión</small></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    public double getCurrentLatitude() {
        return currentLatitude;
    }
    
    public double getCurrentLongitude() {
        return currentLongitude;
    }
}

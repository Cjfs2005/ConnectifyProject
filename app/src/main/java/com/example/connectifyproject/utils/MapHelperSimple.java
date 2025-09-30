package com.example.connectifyproject.utils;

import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.ProgressBar;

public class MapHelperSimple {
    
    private WebView webView;
    private ProgressBar progressBar;
    private double currentLatitude = -12.0464; // Lima por defecto
    private double currentLongitude = -77.0428;
    
    public MapHelperSimple(WebView webView, ProgressBar progressBar) {
        this.webView = webView;
        this.progressBar = progressBar;
        setupWebView();
    }
    
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });
        
        // Cargar mapa inicial inmediatamente
        loadMap(currentLatitude, currentLongitude);
    }
    
    public void updateMapLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        loadMap(latitude, longitude);
    }
    
    private void loadMap(double latitude, double longitude) {
        // No mostrar progress bar - cargar inmediatamente
        String mapHtml = generateMapHtml(latitude, longitude);
        webView.loadDataWithBaseURL(null, mapHtml, "text/html", "UTF-8", null);
    }
    
    private String generateMapHtml(double latitude, double longitude) {
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
                "</body>" +
                "</html>";
    }
}

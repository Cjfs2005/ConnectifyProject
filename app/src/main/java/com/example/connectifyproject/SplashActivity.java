package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ocultar la action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Después del delay, ir al login
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, auth_login.class);
            startActivity(intent);
            finish(); // Cerrar splash para que no vuelva al presionar back
        }, SPLASH_DELAY);
    }
}
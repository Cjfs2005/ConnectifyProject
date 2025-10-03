package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class RegisterTypeSelectionActivity extends AppCompatActivity {

    private MaterialCardView cardCliente, cardGuia;
    private View tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_type_selection);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        cardCliente = findViewById(R.id.cardCliente);
        cardGuia = findViewById(R.id.cardGuia);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupClickListeners() {
        cardCliente.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterBasicDataActivity.class);
            intent.putExtra("user_type", "CLIENTE");
            startActivity(intent);
        });

        cardGuia.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterBasicDataActivity.class);
            intent.putExtra("user_type", "GUIA");
            startActivity(intent);
        });

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, auth_login.class);
            startActivity(intent);
            finish();
        });
    }
}
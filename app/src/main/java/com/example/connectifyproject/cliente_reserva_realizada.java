package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class cliente_reserva_realizada extends AppCompatActivity {
    
    private MaterialButton btnAceptar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_reserva_realizada);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnAceptar = findViewById(R.id.btn_aceptar);
    }

    private void setupClickListeners() {
        btnAceptar.setOnClickListener(v -> {
            // Regresar a la lista de tours
            Intent intent = new Intent(this, cliente_tours.class);
            // Clear todas las actividades anteriores de la pila
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
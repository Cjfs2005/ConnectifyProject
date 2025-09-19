package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class cliente_metodo_pago extends AppCompatActivity {
    
    private RecyclerView rvPaymentMethods;
    private TextView tvTotalPrice;
    private MaterialButton btnReservar;
    private TarjetaAdapter tarjetaAdapter;
    private String totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_metodo_pago);

        initViews();
        setupToolbar();
        getTotalPriceFromIntent();
        setupPaymentMethods();
        setupClickListeners();
    }

    private void initViews() {
        rvPaymentMethods = findViewById(R.id.rv_payment_methods);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnReservar = findViewById(R.id.btn_reservar);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        toolbar.setNavigationOnClickListener(v -> finish());
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void getTotalPriceFromIntent() {
        totalPrice = getIntent().getStringExtra("total_price");
        if (totalPrice != null) {
            tvTotalPrice.setText(totalPrice);
        }
    }

    private void setupPaymentMethods() {
        List<TarjetaAdapter.Tarjeta> tarjetas = new ArrayList<>();
        
        // Agregar tarjetas de ejemplo (como en el mockup)
        tarjetas.add(new TarjetaAdapter.Tarjeta(
            "•••• •••• •••• 2934",
            "Fecha de vencimiento 12/28",
            "VISA",
            R.drawable.ic_visa
        ));
        
        tarjetas.add(new TarjetaAdapter.Tarjeta(
            "•••• •••• •••• 1340",
            "Fecha de vencimiento 11/27",
            "VISA",
            R.drawable.ic_visa
        ));

        tarjetaAdapter = new TarjetaAdapter(tarjetas, this::onTarjetaSelected);
        rvPaymentMethods.setLayoutManager(new LinearLayoutManager(this));
        rvPaymentMethods.setAdapter(tarjetaAdapter);
    }

    private void onTarjetaSelected(TarjetaAdapter.Tarjeta tarjeta, int position) {
        // La tarjeta seleccionada se maneja automáticamente en el adapter
    }

    private void setupClickListeners() {
        btnReservar.setOnClickListener(v -> {
            // Navegar a la pantalla de reserva realizada
            Intent intent = new Intent(this, cliente_reserva_realizada.class);
            intent.putExtra("total_price", totalPrice);
            startActivity(intent);
        });
    }
}
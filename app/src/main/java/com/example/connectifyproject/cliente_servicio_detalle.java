package com.example.connectifyproject;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class cliente_servicio_detalle extends AppCompatActivity {

    private TextView tvServiceTitle, tvServicePrice, tvServiceDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_servicio_detalle);

        initViews();
        setupToolbar();
        loadServiceData();
    }

    private void initViews() {
        tvServiceTitle = findViewById(R.id.tv_service_title);
        tvServicePrice = findViewById(R.id.tv_service_price);
        tvServiceDescription = findViewById(R.id.tv_service_description);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Servicio");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadServiceData() {
        // Obtener datos del intent o usar valores por defecto
        String serviceName = getIntent().getStringExtra("servicio_nombre");
        String servicePrice = getIntent().getStringExtra("servicio_precio");
        String serviceDescription = getIntent().getStringExtra("servicio_descripcion");

        // Si no hay datos del intent, usar datos hardcodeados
        if (serviceName == null) {
            serviceName = "Servicio de Transporte Premium";
        }
        if (servicePrice == null) {
            servicePrice = "S/. 50";
        }
        if (serviceDescription == null) {
            serviceDescription = "Nuestro servicio de transporte premium ofrece vehículos de lujo con aire acondicionado, conductores profesionales y servicios adicionales para garantizar tu comodidad durante todo el recorrido. Incluye agua embotellada, wifi gratuito y asientos de cuero premium.";
        }

        // Establecer datos en las vistas
        tvServiceTitle.setText(serviceName);
        tvServicePrice.setText(servicePrice);
        tvServiceDescription.setText(serviceDescription);
    }
}
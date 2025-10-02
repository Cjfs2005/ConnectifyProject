package com.example.connectifyproject;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.connectifyproject.models.Cliente_ServicioAdicional;

public class cliente_servicio_detalle extends AppCompatActivity {

    private String serviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_servicio_detalle);

        setupToolbar();
        loadServiceData();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadServiceData() {
        // Preferir objeto completo del intent
        Cliente_ServicioAdicional servicio = (Cliente_ServicioAdicional) getIntent().getSerializableExtra("servicio_object");
        String priceText;
        String descriptionText;

        if (servicio != null) {
            serviceName = servicio.getName();
            priceText = "S/" + String.format("%.2f", servicio.getPrice());
            descriptionText = servicio.getDescription();
        } else {
            // Compatibilidad con extras antiguos
            serviceName = getIntent().getStringExtra("servicio_nombre");
            Double priceValue = null;
            try {
                // si viene como double
                if (getIntent().hasExtra("servicio_precio")) {
                    Object p = getIntent().getExtras().get("servicio_precio");
                    if (p instanceof Double) priceValue = (Double) p;
                    else if (p instanceof String) priceValue = Double.parseDouble((String) p);
                }
            } catch (Exception ignored) {}
            priceText = priceValue != null ? ("S/" + String.format("%.2f", priceValue)) : "S/. 50";
            descriptionText = getIntent().getStringExtra("servicio_descripcion");

            if (serviceName == null) serviceName = "Servicio de Transporte Premium";
            if (descriptionText == null) descriptionText = "Nuestro servicio de transporte premium ofrece vehículos de lujo con aire acondicionado, conductores profesionales y servicios adicionales para garantizar tu comodidad durante todo el recorrido. Incluye agua embotellada, wifi gratuito y asientos de cuero premium.";
        }

        // Título en toolbar con el nombre del servicio
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(serviceName);
        } else {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(serviceName);
        }

        // Establecer datos en el resto de vistas
        TextView tvServiceTitle = findViewById(R.id.tv_service_title);
        TextView tvServicePrice = findViewById(R.id.tv_service_price);
        TextView tvServiceDescription = findViewById(R.id.tv_service_description);
        tvServiceTitle.setText(serviceName);
        tvServicePrice.setText(priceText);
        tvServiceDescription.setText(descriptionText);
    }
}
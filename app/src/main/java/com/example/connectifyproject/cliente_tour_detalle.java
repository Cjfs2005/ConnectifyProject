package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ServiciosAdapter;
import com.example.connectifyproject.models.Cliente_ServicioAdicional;
import com.example.connectifyproject.models.Cliente_Tour;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class cliente_tour_detalle extends AppCompatActivity implements Cliente_ServiciosAdapter.OnServiceSelectedListener {

    private MaterialToolbar toolbar;
    private TextView tvTourLocation, tvTourDuration, tvStartTime, tvEndTime, tvTourCompany;
    private TextView tvTourPriceBadge, tvPeopleCount, tvTotalPrice;
    private RecyclerView rvServiciosAdicionales;
    private ImageButton btnDecreasePeople, btnIncreasePeople;
    private MaterialButton btnContinuar;
    private MaterialCardView cardEmpresa;
    private View layoutItinerario;

    private Cliente_ServiciosAdapter serviciosAdapter;
    private List<Cliente_ServicioAdicional> serviciosAdicionales;
    
    private Cliente_Tour tour;
    private int peopleCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_tour_detalle);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadServiciosData();
        updateUI();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        tour = (Cliente_Tour) intent.getSerializableExtra("tour_object");
        
        if (tour == null) {
            // Fallback para compatibilidad con método anterior
            tour = new Cliente_Tour(
                intent.getStringExtra("tour_id"),
                intent.getStringExtra("tour_title"),
                intent.getStringExtra("tour_description"),
                intent.getStringExtra("tour_duration"),
                intent.getDoubleExtra("tour_price", 0.0),
                intent.getStringExtra("tour_location"),
                4.5f,
                intent.getStringExtra("tour_company")
            );
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTourLocation = findViewById(R.id.tv_tour_location);
        tvTourDuration = findViewById(R.id.tv_tour_duration);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        tvTourCompany = findViewById(R.id.tv_tour_company);
        tvTourPriceBadge = findViewById(R.id.tv_tour_price_badge);
        tvPeopleCount = findViewById(R.id.tv_people_count);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        rvServiciosAdicionales = findViewById(R.id.rv_servicios_adicionales);
        btnDecreasePeople = findViewById(R.id.btn_decrease_people);
        btnIncreasePeople = findViewById(R.id.btn_increase_people);
        btnContinuar = findViewById(R.id.btn_continuar);
        cardEmpresa = findViewById(R.id.card_empresa);
        layoutItinerario = findViewById(R.id.layout_itinerario);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(tour.getTitulo());
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        serviciosAdicionales = new ArrayList<>();
        serviciosAdapter = new Cliente_ServiciosAdapter(this, serviciosAdicionales);
        serviciosAdapter.setOnServiceSelectedListener(this);
        
        rvServiciosAdicionales.setLayoutManager(new LinearLayoutManager(this));
        rvServiciosAdicionales.setAdapter(serviciosAdapter);
    }

    private void setupClickListeners() {
        btnDecreasePeople.setOnClickListener(v -> {
            if (peopleCount > 1) {
                peopleCount--;
                updatePeopleCount();
            }
        });

        btnIncreasePeople.setOnClickListener(v -> {
            peopleCount++;
            updatePeopleCount();
        });

        btnContinuar.setOnClickListener(v -> {
            // Navegar a método de pago con el precio total calculado
            Intent intent = new Intent(this, cliente_metodo_pago.class);
            intent.putExtra("total_price", tvTotalPrice.getText().toString());
            intent.putExtra("tour_title", tour.getTitulo());
            intent.putExtra("people_count", peopleCount);
            startActivity(intent);
        });

        cardEmpresa.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_empresa_info.class);
            intent.putExtra("company_name", tour.getCompanyName());
            startActivity(intent);
        });

        layoutItinerario.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_tour_mapa.class);
            intent.putExtra("tour_id", tour.getId());
            intent.putExtra("tour_title", tour.getTitulo());
            startActivity(intent);
        });
    }

    private void loadServiciosData() {
        // Datos hardcodeados de servicios adicionales
        serviciosAdicionales.add(new Cliente_ServicioAdicional("1", "Almuerzo", 
                "Se incluye un almuerzo ubicado en el atrio.", 16.00));
        
        serviciosAdicionales.add(new Cliente_ServicioAdicional("2", "Folleto turístico (Gratis)", 
                "Se incluye un folleto turístico con información importante sobre...", 0.00));
        
        serviciosAdicionales.add(new Cliente_ServicioAdicional("3", "Cena", 
                "Se incluye una cena en un restaurante ubicado en las inmediaciones.", 25.00));

        serviciosAdapter.notifyDataSetChanged();
    }

    private void updateUI() {
        tvTourLocation.setText(tour.getUbicacion());
        tvTourDuration.setText(tour.getDuracion());
        tvTourCompany.setText(tour.getCompanyName());
        tvTourPriceBadge.setText("S/" + String.format("%.2f", tour.getPrecio()));
        
        // Datos hardcodeados para las fechas
        tvStartTime.setText("Hoy - 13:10");
        tvEndTime.setText("Hoy - 18:40");
        
        updatePeopleCount();
    }

    private void updatePeopleCount() {
        tvPeopleCount.setText(String.valueOf(peopleCount));
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double serviciosPrice = 0.0;
        for (Cliente_ServicioAdicional servicio : serviciosAdicionales) {
            if (servicio.isSelected()) {
                serviciosPrice += servicio.getPrice();
            }
        }
        
        double totalPerPerson = tour.getPrecio() + serviciosPrice;
        double totalPrice = totalPerPerson * peopleCount;
        
        tvTotalPrice.setText("S/" + String.format("%.2f", totalPrice));
    }

    @Override
    public void onServiceSelected(Cliente_ServicioAdicional servicio, boolean isSelected) {
        updateTotalPrice();
    }
}
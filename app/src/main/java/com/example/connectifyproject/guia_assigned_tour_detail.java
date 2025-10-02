package com.example.connectifyproject;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.databinding.GuiaAssignedTourDetailBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class guia_assigned_tour_detail extends AppCompatActivity {
    private GuiaAssignedTourDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaAssignedTourDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Tour");
        }

        Intent intent = getIntent();
        String tourName = intent.getStringExtra("tour_name");
        String tourEmpresa = intent.getStringExtra("tour_empresa");
        String tourInitio = intent.getStringExtra("tour_initio");
        String tourDuration = intent.getStringExtra("tour_duration");
        int tourClients = intent.getIntExtra("tour_clients", 0);
        String tourStatus = intent.getStringExtra("tour_status");
        String tourLanguages = intent.getStringExtra("tour_languages");
        String tourServices = intent.getStringExtra("tour_services");
        ArrayList<String> tourItinerario = intent.getStringArrayListExtra("tour_itinerario");

        binding.tourName.setText(tourName + " - " + tourStatus);
        binding.tourEmpresa.setText("Empresa: " + tourEmpresa);
        binding.tourInitio.setText("Fecha: " + tourInitio);
        binding.tourDuration.setText("Duración: " + tourDuration);
        binding.tourLanguages.setText("Idiomas: " + tourLanguages);
        binding.tourServices.setText("Servicios incluidos: " + tourServices);
        binding.tourClients.setText("Clientes asignados: " + tourClients);
        binding.tourStatus.setText("Estado del guía: " + (tourStatus.equals("En Curso") ? "Activo / En ruta" : "Inactivo"));

        StringBuilder itineraryText = new StringBuilder("Itinerario\n");
        if (tourItinerario != null) {
            for (String item : tourItinerario) {
                itineraryText.append(item).append("\n");
            }
        }
        binding.tourItinerario.setText(itineraryText.toString());

        if (tourStatus != null && tourStatus.equals("En Curso")) {
            binding.actionsLayout.setVisibility(View.VISIBLE);
        } else {
            binding.actionsLayout.setVisibility(View.GONE);
        }

        binding.checkInButton.setOnClickListener(v -> {
            startActivity(new Intent(this, guia_check_in.class));
            Toast.makeText(this, "Check-in iniciado (simulado)", Toast.LENGTH_SHORT).show();
        });

        binding.mapButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(this, guia_tour_map.class);
            mapIntent.putExtra("tour_name", tourName);
            mapIntent.putExtra("tour_status", tourStatus);
            mapIntent.putStringArrayListExtra("tour_itinerario", tourItinerario);
            mapIntent.putExtra("tour_clients", tourClients);
            startActivity(mapIntent);
        });

        binding.checkOutButton.setOnClickListener(v -> {
            startActivity(new Intent(this, guia_check_out.class));
            Toast.makeText(this, "Check-out iniciado (simulado)", Toast.LENGTH_SHORT).show();
        });

        // Elegant colors
        binding.mapButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3F51B5")));
        binding.checkInButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        binding.checkOutButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));

        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_tours);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class));
                return true;
            } else if (id == R.id.nav_ofertas) {
                startActivity(new Intent(this, guia_tours_ofertas.class));
                return true;
            } else if (id == R.id.nav_tours) {
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, guia_perfil.class));
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
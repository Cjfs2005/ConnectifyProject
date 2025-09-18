package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.databinding.GuiaTourDetailBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class guia_tour_detail extends AppCompatActivity implements OnMapReadyCallback {
    private GuiaTourDetailBinding binding;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaTourDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Tour");
        }

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            binding.tourName.setText(extras.getString("tour_name"));
            binding.tourLocation.setText(extras.getString("tour_location"));
            binding.tourDuration.setText(extras.getString("tour_duration"));
            binding.tourDescription.setText(extras.getString("tour_description"));
            binding.tourBenefits.setText(extras.getString("tour_benefits"));
            binding.tourSchedule.setText(extras.getString("tour_schedule"));
            binding.tourMeetingPoint.setText(extras.getString("tour_meeting_point"));
            binding.tourEmpresa.setText(extras.getString("tour_empresa"));
            binding.tourItinerario.setText(extras.getString("tour_itinerario"));
            binding.tourExperience.setText(extras.getString("tour_experiencia_minima"));
            binding.tourPunctuality.setText(extras.getString("tour_puntualidad"));
        }

        binding.acceptButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar aceptación")
                    .setMessage("¿Está seguro de aceptar esta oferta? Se rechazarán otras ofertas en el mismo horario")
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        Toast.makeText(this, "Oferta aceptada (simulado)", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        binding.rejectButton.setOnClickListener(v -> {
            Toast.makeText(this, "Oferta rechazada (simulado)", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Bottom Navigation original (comentado como solicitado)
        /*
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_ofertas);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class)); // Placeholder, renombrado
                return true;
            } else if (id == R.id.nav_ofertas) {
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, guia_assigned_tours.class)); // Renombrado
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, guia_perfil.class)); // Placeholder, renombrado
                return true;
            }
            return false;
        });
        */

        // Nuevo Bottom Navigation con Toast
        BottomNavigationView newBottomNav = binding.bottomNav;
        newBottomNav.setSelectedItemId(R.id.nav_ofertas);
        newBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class)); // Placeholder, renombrado
                return true;
            } else if (id == R.id.nav_ofertas) {
                return true;
            } else if (id == R.id.nav_tours) {
                Toast.makeText(this, "Tours asignados seleccionado", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_perfil) {
                Toast.makeText(this, "Perfil seleccionado", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng lima = new LatLng(-12.0464, -77.0428);
        mMap.addMarker(new MarkerOptions().position(lima).title("Centro Histórico de Lima"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 15));
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }
}
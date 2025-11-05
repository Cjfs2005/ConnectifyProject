package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.databinding.GuiaTourDetailBinding;
import com.example.connectifyproject.services.TourFirebaseService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class guia_tour_detail extends AppCompatActivity implements OnMapReadyCallback {
    private GuiaTourDetailBinding binding;
    private GoogleMap mMap;
    private TourFirebaseService tourService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaTourDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Inicializar servicio Firebase
        tourService = new TourFirebaseService();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Tour");
        }

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Header información
            binding.empresaBadge.setText(extras.getString("tour_empresa", "Empresa Tours"));
            String fecha = extras.getString("tour_date", "");
            String hora = extras.getString("tour_start_time", "");
            binding.tourDate.setText(fecha + " " + hora);
            
            // Información principal
            binding.tourName.setText(extras.getString("tour_name"));
            binding.tourLocation.setText("📍 " + extras.getString("tour_location"));
            binding.tourDuration.setText(extras.getString("tour_duration"));
            binding.tourLanguages.setText(extras.getString("tour_languages", "No especificado"));
            
            // Precio (asumiendo que viene como double)
            double precio = extras.getDouble("tour_price", 0.0);
            binding.tourPrice.setText("S/. " + (int)precio);
            
            // Itinerario y descripción
            binding.tourItinerario.setText(extras.getString("tour_itinerario"));
            binding.tourDescription.setText(extras.getString("tour_description"));
            
            // Requerimientos
            binding.tourExperience.setText("• Experiencia: " + extras.getString("tour_experiencia_minima", "No especificado"));
            binding.tourPunctuality.setText("• " + extras.getString("tour_puntualidad", "Puntualidad requerida"));
            
            // Beneficios
            binding.tourBenefits.setText(extras.getString("tour_benefits"));
            binding.tourSchedule.setText(extras.getString("tour_schedule"));
            
            // Punto de encuentro
            binding.tourMeetingPoint.setText(extras.getString("tour_meeting_point"));
        }

        binding.acceptButton.setOnClickListener(v -> {
            String firebaseId = getIntent().getStringExtra("tour_firebase_id");
            String tourName = getIntent().getStringExtra("tour_name");
            
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar aceptación")
                    .setMessage("¿Está seguro de aceptar la oferta '" + tourName + "'?\n\nSe rechazarán otras ofertas en el mismo horario.")
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        if (firebaseId != null && !firebaseId.isEmpty()) {
                            aceptarOfertaFirebase(firebaseId, tourName);
                        } else {
                            Toast.makeText(this, "Error: ID de oferta no válido", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        binding.rejectButton.setOnClickListener(v -> {
            String firebaseId = getIntent().getStringExtra("tour_firebase_id");
            String tourName = getIntent().getStringExtra("tour_name");
            
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar rechazo")
                    .setMessage("¿Está seguro de rechazar la oferta '" + tourName + "'?")
                    .setPositiveButton("Rechazar", (dialog, which) -> {
                        if (firebaseId != null && !firebaseId.isEmpty()) {
                            rechazarOfertaFirebase(firebaseId, tourName);
                        } else {
                            Toast.makeText(this, "Oferta rechazada", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Navbar eliminado - pantalla secundaria
        /*
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_ofertas);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class));
                return true;
            } else if (id == R.id.nav_ofertas) {
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, guia_assigned_tours.class));
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, guia_perfil.class));
                return true;
            }
            return false;
        });
        */
    }
    
    /**
     * Aceptar oferta usando Firebase
     */
    private void aceptarOfertaFirebase(String firebaseId, String tourName) {
        tourService.aceptarOferta(firebaseId, new TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_tour_detail.this, "¡Oferta '" + tourName + "' aceptada exitosamente!", Toast.LENGTH_LONG).show();
                    
                    // Volver a la pantalla anterior y actualizar la lista
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("oferta_aceptada", true);
                    resultIntent.putExtra("firebase_id", firebaseId);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_tour_detail.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * Rechazar oferta usando Firebase
     */
    private void rechazarOfertaFirebase(String firebaseId, String tourName) {
        tourService.rechazarOferta(firebaseId, new TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_tour_detail.this, "Oferta '" + tourName + "' rechazada", Toast.LENGTH_SHORT).show();
                    
                    // Volver a la pantalla anterior
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("oferta_rechazada", true);
                    resultIntent.putExtra("firebase_id", firebaseId);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_tour_detail.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
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
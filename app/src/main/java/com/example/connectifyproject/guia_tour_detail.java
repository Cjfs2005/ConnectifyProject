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
            
            // PAGO AL GUÍA (no precio del tour)
            double pagoGuia = extras.getDouble("tour_price", 0.0);
            binding.tourPrice.setText("S/. " + (int)pagoGuia);
            
            // Crear itinerario visual
            crearItinerarioVisual(extras.getString("tour_itinerario", ""));
            
            // Descripción
            binding.tourDescription.setText(extras.getString("tour_description"));
            
            // Consideraciones y requerimientos
            binding.tourConsideraciones.setText("• " + extras.getString("tour_consideraciones", "No especificadas"));
            binding.tourLanguagesRequired.setText("• Idiomas: " + extras.getString("tour_languages", "No especificado"));
            
            // Crear servicios dinámicos
            crearServiciosAdicionales(extras.getString("tour_servicios", ""));
            
            // Pago al guía destacado
            binding.pagoGuiaAmount.setText("S/. " + (int)pagoGuia);
            
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

    /**
     * Crear una vista visual del itinerario basada en estructura Firebase
     */
    private void crearItinerarioVisual(String itinerario) {
        if (itinerario == null || itinerario.isEmpty()) {
            // Itinerario de ejemplo basado en la estructura Firebase
            crearItinerarioEjemplo();
            return;
        }
        
        // Limpiar contenedor anterior
        binding.itinerarioContainer.removeAllViews();
        
        // Separar por paradas (formato: "hora lugar → hora lugar")
        String[] paradas = itinerario.split(" → ");
        
        for (int i = 0; i < paradas.length; i++) {
            String parada = paradas[i].trim();
            crearVistaParada(parada, i, paradas.length);
        }
    }
    
    /**
     * Crear itinerario de ejemplo basado en estructura Firebase
     */
    private void crearItinerarioEjemplo() {
        binding.itinerarioContainer.removeAllViews();
        
        String[][] paradasEjemplo = {
            {"15:00", "Puente de los Suspiros", "Inicio del tour en el icónico puente"},
            {"15:30", "Galería de Arte", "Visita a galería de arte local"},
            {"16:30", "Malecón de Barranco", "Caminata con vista al océano Pacífico"}
        };
        
        for (int i = 0; i < paradasEjemplo.length; i++) {
            String[] parada = paradasEjemplo[i];
            String paradaFormateada = parada[0] + " " + parada[1] + " - " + parada[2];
            crearVistaParada(paradaFormateada, i, paradasEjemplo.length);
        }
    }
    
    /**
     * Crear vista individual para cada parada
     */
    private void crearVistaParada(String parada, int indice, int total) {
        // Crear vista para cada parada
        android.widget.LinearLayout paradaLayout = new android.widget.LinearLayout(this);
        paradaLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        paradaLayout.setPadding(0, 12, 0, 12);
        
        // Icono de ubicación
        android.widget.TextView iconoView = new android.widget.TextView(this);
        if (indice == 0) {
            iconoView.setText("🚩"); // Inicio
        } else if (indice == total - 1) {
            iconoView.setText("🏁"); // Fin
        } else {
            iconoView.setText("📍"); // Punto intermedio
        }
        iconoView.setTextSize(18);
        iconoView.setPadding(0, 0, 16, 0);
        
        // Contenedor de texto
        android.widget.LinearLayout textoLayout = new android.widget.LinearLayout(this);
        textoLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        textoLayout.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        
        // Texto de la parada
        android.widget.TextView paradaText = new android.widget.TextView(this);
        paradaText.setText(parada);
        paradaText.setTextSize(14);
        paradaText.setTextColor(getResources().getColor(android.R.color.black));
        
        textoLayout.addView(paradaText);
        
        // Agregar elementos al layout principal
        paradaLayout.addView(iconoView);
        paradaLayout.addView(textoLayout);
        
        // Agregar al contenedor principal
        binding.itinerarioContainer.addView(paradaLayout);
        
        // Agregar línea conectora (excepto en el último elemento)
        if (indice < total - 1) {
            android.view.View linea = new android.view.View(this);
            android.widget.LinearLayout.LayoutParams lineaParams = 
                new android.widget.LinearLayout.LayoutParams(3, 40);
            lineaParams.setMargins(24, 0, 0, 0);
            linea.setLayoutParams(lineaParams);
            linea.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            binding.itinerarioContainer.addView(linea);
        }
    }
    
    /**
     * Crear vista de servicios adicionales
     */
    private void crearServiciosAdicionales(String servicios) {
        // Limpiar contenedor anterior
        binding.serviciosContainer.removeAllViews();
        
        // Servicios de ejemplo basados en la estructura Firebase
        String[] serviciosArray = {
            "Guía especializada en arte (Incluido)",
            "Café en terraza con vista (+S/. 15)",
            "Transporte desde hotel (Consultar)",
            "Material fotográfico (Incluido)"
        };
        
        for (String servicio : serviciosArray) {
            // Crear layout para cada servicio
            android.widget.LinearLayout servicioLayout = new android.widget.LinearLayout(this);
            servicioLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            servicioLayout.setPadding(0, 6, 0, 6);
            
            // Icono del servicio
            android.widget.TextView iconoView = new android.widget.TextView(this);
            if (servicio.contains("Incluido")) {
                iconoView.setText("✅");
                iconoView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (servicio.contains("+S/.")) {
                iconoView.setText("💰");
                iconoView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                iconoView.setText("ℹ️");
                iconoView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
            iconoView.setTextSize(14);
            iconoView.setPadding(0, 0, 12, 0);
            
            // Texto del servicio
            android.widget.TextView servicioText = new android.widget.TextView(this);
            servicioText.setText(servicio);
            servicioText.setTextSize(13);
            servicioText.setTextColor(getResources().getColor(android.R.color.black));
            
            // Agregar elementos al layout
            servicioLayout.addView(iconoView);
            servicioLayout.addView(servicioText);
            
            // Agregar al contenedor principal
            binding.serviciosContainer.addView(servicioLayout);
        }
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
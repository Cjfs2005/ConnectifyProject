package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.databinding.GuiaTourDetailBinding;
import com.example.connectifyproject.services.TourFirebaseService;
import com.example.connectifyproject.ui.guia.TourImageAdapter;
import androidx.viewpager2.widget.ViewPager2;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class guia_tour_detail extends AppCompatActivity implements OnMapReadyCallback {
    private GuiaTourDetailBinding binding;
    private GoogleMap mMap;
    private TourFirebaseService tourService;
    private String ofertaId;
    private List<Map<String, Object>> itinerarioData;
    private List<Map<String, Object>> serviciosData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaTourDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Inicializar servicio Firebase
        tourService = new TourFirebaseService();
        
        // Obtener ID de la oferta
        ofertaId = getIntent().getStringExtra("tour_firebase_id");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Tour");
        }

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);
        
        // Cargar datos desde Firebase
        if (ofertaId != null && !ofertaId.isEmpty()) {
            cargarDatosDesdeFirebase();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Header información
            binding.empresaBadge.setText(extras.getString("tour_empresa", "Empresa Tours"));
            String fecha = extras.getString("tour_date", "");
            String hora = extras.getString("tour_start_time", "");
            binding.tourDate.setText(fecha + " " + hora);
            
            // Información principal
            binding.tourName.setText(extras.getString("tour_name"));
            binding.tourDuration.setText(extras.getString("tour_duration"));
            binding.tourLanguages.setText(extras.getString("tour_languages", "No especificado"));
            
            // PAGO AL GUÍA (no precio del tour)
            double pagoGuia = extras.getDouble("tour_price", 0.0);
            binding.tourPrice.setText("S/. " + (int)pagoGuia);
            
            // Crear itinerario visual
            crearItinerarioVisual(extras.getString("tour_itinerario", ""));
            
            // Descripción
            //binding.tourDescription.setText(extras.getString("tour_description"));
            
            // Consideraciones y requerimientos
            String consideraciones = extras.getString("tour_consideraciones", "");
            if (consideraciones != null && !consideraciones.isEmpty() && !consideraciones.equals("No especificadas")) {
                binding.tourConsideraciones.setText("• " + consideraciones);
                binding.tourConsideraciones.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tourConsideraciones.setVisibility(android.view.View.GONE);
            }
            binding.tourLanguagesRequired.setText("• Idiomas: " + extras.getString("tour_languages", "No especificado"));
            
            // Crear servicios dinámicos
            crearServiciosAdicionales(extras.getString("tour_servicios", ""));
            
            // Pago al guía destacado
            binding.pagoGuiaAmount.setText("S/. " + (int)pagoGuia);
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
        // Obtener el adminId (empresaId) del documento de la oferta antes de crear la notificación
        FirebaseFirestore.getInstance()
            .collection("tours_ofertas")
            .document(firebaseId)
            .get()
            .addOnSuccessListener(doc -> {
                String adminId = doc.getString("empresaId");
                tourService.aceptarOferta(firebaseId, new TourFirebaseService.OperationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(guia_tour_detail.this, "¡Oferta '" + tourName + "' aceptada exitosamente!", Toast.LENGTH_LONG).show();
                            // --- Crear notificación y log para el admin real ---
                            String guiaNombre = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Guía";
                            String notiTitulo = "Oferta aceptada";
                            String notiDesc = "El guía " + guiaNombre + " aceptó la oferta del tour '" + tourName + "'.";
                            if (adminId != null && !adminId.isEmpty()) {
                                com.example.connectifyproject.utils.NotificacionLogUtils.crearNotificacion(notiTitulo, notiDesc, adminId);
                            }
                            com.example.connectifyproject.utils.NotificacionLogUtils.crearLog(notiTitulo, notiDesc);
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
            });
    }
    
    /**
     * Rechazar oferta usando Firebase
     */
    private void rechazarOfertaFirebase(String firebaseId, String tourName) {
        // Obtener el adminId (empresaId) del documento de la oferta antes de crear la notificación
        FirebaseFirestore.getInstance()
            .collection("tours_ofertas")
            .document(firebaseId)
            .get()
            .addOnSuccessListener(doc -> {
                String adminId = doc.getString("empresaId");
                tourService.rechazarOferta(firebaseId, new TourFirebaseService.OperationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(guia_tour_detail.this, "Oferta '" + tourName + "' rechazada", Toast.LENGTH_SHORT).show();
                            // --- Crear notificación y log para el admin real ---
                            String guiaNombre = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Guía";
                            String notiTitulo = "Oferta rechazada";
                            String notiDesc = "El guía " + guiaNombre + " rechazó la oferta del tour '" + tourName + "'.";
                            if (adminId != null && !adminId.isEmpty()) {
                                com.example.connectifyproject.utils.NotificacionLogUtils.crearNotificacion(notiTitulo, notiDesc, adminId);
                            }
                            com.example.connectifyproject.utils.NotificacionLogUtils.crearLog(notiTitulo, notiDesc);
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
            iconoView.setText(""); // Inicio
        } else if (indice == total - 1) {
            iconoView.setText(""); // Fin
        } else {
            iconoView.setText(""); // Punto intermedio
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
        /*if (indice < total - 1) {
            android.view.View linea = new android.view.View(this);
            android.widget.LinearLayout.LayoutParams lineaParams = 
                new android.widget.LinearLayout.LayoutParams(3, 40);
            lineaParams.setMargins(24, 0, 0, 0);
            linea.setLayoutParams(lineaParams);
            linea.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            binding.itinerarioContainer.addView(linea);
        }*/
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

    /**
     * Cargar itinerario y servicios desde Firebase
     */
    private void cargarDatosDesdeFirebase() {
        FirebaseFirestore.getInstance()
            .collection("tours_ofertas")
            .document(ofertaId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    // Validar disponibilidad de tiempo (12 horas)
                    Object fechaRealizacion = doc.get("fechaRealizacion");
                    String horaInicio = doc.getString("horaInicio");
                    
                    boolean puedeAceptar = com.example.connectifyproject.utils.TourTimeValidator
                        .puedeAceptarOferta(fechaRealizacion, horaInicio);
                    
                    if (!puedeAceptar) {
                        android.util.Log.w("GuiaTourDetail", "Oferta ya no disponible - No quedan 12 horas");
                        // Deshabilitar botones y mostrar mensaje
                        binding.acceptButton.setEnabled(false);
                        binding.rejectButton.setEnabled(false);
                        binding.acceptButton.setAlpha(0.5f);
                        binding.rejectButton.setAlpha(0.5f);
                        
                        Toast.makeText(this, 
                            "Esta oferta ya no está disponible. Deben quedar al menos 12 horas antes del tour.", 
                            Toast.LENGTH_LONG).show();
                    } else {
                        // Habilitar botones
                        binding.acceptButton.setEnabled(true);
                        binding.rejectButton.setEnabled(true);
                        binding.acceptButton.setAlpha(1.0f);
                        binding.rejectButton.setAlpha(1.0f);
                    }
                    
                    // Cargar galería de imágenes
                    List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                    if (imagenesUrls != null && !imagenesUrls.isEmpty()) {
                        setupImageGallery(imagenesUrls);
                    } else {
                        // Si no hay galería, intentar cargar imagen principal
                        String imagenPrincipal = doc.getString("imagenPrincipal");
                        if (imagenPrincipal != null && !imagenPrincipal.isEmpty()) {
                            setupImageGallery(java.util.Arrays.asList(imagenPrincipal));
                        } else {
                            // Mostrar imagen por defecto
                            setupImageGallery(java.util.Arrays.asList(""));
                        }
                    }
                    
                    // Cargar itinerario
                    itinerarioData = (List<Map<String, Object>>) doc.get("itinerario");
                    if (itinerarioData != null && !itinerarioData.isEmpty()) {
                        mostrarItinerarioReal();
                    }
                    
                    // Cargar servicios adicionales
                    serviciosData = (List<Map<String, Object>>) doc.get("serviciosAdicionales");
                    if (serviciosData != null && !serviciosData.isEmpty()) {
                        mostrarServiciosReales();
                    }
                    
                    // Actualizar mapa con puntos reales del itinerario
                    if (mMap != null && itinerarioData != null) {
                        mostrarRutaEnMapa();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar detalles del tour", Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Mostrar itinerario real desde Firebase
     */
    private void mostrarItinerarioReal() {
        binding.itinerarioContainer.removeAllViews();
        
        for (int i = 0; i < itinerarioData.size(); i++) {
            Map<String, Object> punto = itinerarioData.get(i);
            String nombre = (String) punto.get("nombre");
            String direccion = (String) punto.get("direccion");
            
            crearVistaParadaReal(nombre, direccion, i, itinerarioData.size());
        }
    }
    
    /**
     * Crear vista individual para cada parada del itinerario real
     */
    private void crearVistaParadaReal(String nombre, String direccion, int indice, int total) {
        android.widget.LinearLayout paradaLayout = new android.widget.LinearLayout(this);
        paradaLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        paradaLayout.setPadding(0, 12, 0, 12);
        
        // Icono de ubicación
        android.widget.TextView iconoView = new android.widget.TextView(this);
        if (indice == 0) {
            iconoView.setText(""); // Inicio
        } else if (indice == total - 1) {
            iconoView.setText(""); // Fin
        } else {
            iconoView.setText(""); // Punto intermedio
        }
        iconoView.setTextSize(18);
        iconoView.setPadding(0, 0, 16, 0);
        
        // Contenedor de texto
        android.widget.LinearLayout textoLayout = new android.widget.LinearLayout(this);
        textoLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        textoLayout.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        
        // Nombre del lugar
        android.widget.TextView nombreText = new android.widget.TextView(this);
        nombreText.setText((indice + 1) + ". " + (nombre != null ? nombre : "Punto del tour"));
        nombreText.setTextSize(14);
        nombreText.setTextColor(getResources().getColor(android.R.color.black));
        nombreText.setTypeface(null, android.graphics.Typeface.BOLD);
        
        textoLayout.addView(nombreText);
        
        // Obtener y mostrar actividades del punto
        if (itinerarioData != null && indice < itinerarioData.size()) {
            Map<String, Object> punto = itinerarioData.get(indice);
            List<String> actividades = (List<String>) punto.get("actividades");
            
            android.widget.TextView actividadesText = new android.widget.TextView(this);
            actividadesText.setTextSize(13);
            actividadesText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            actividadesText.setPadding(0, 4, 0, 0);
            
            if (actividades != null && !actividades.isEmpty()) {
                StringBuilder actividadesStr = new StringBuilder();
                for (int i = 0; i < actividades.size(); i++) {
                    actividadesStr.append(actividades.get(i));
                    if (i < actividades.size() - 1) {
                        actividadesStr.append("\n");
                    }
                }
                actividadesText.setText(actividadesStr.toString());
            } else {
                actividadesText.setText("No hay detalles de actividades");
                actividadesText.setTypeface(null, android.graphics.Typeface.ITALIC);
            }
            
            textoLayout.addView(actividadesText);
        }
        
        // Agregar elementos al layout principal
        paradaLayout.addView(iconoView);
        paradaLayout.addView(textoLayout);
        
        // Agregar al contenedor principal
        binding.itinerarioContainer.addView(paradaLayout);
        
        // Agregar línea conectora (excepto en el último elemento)
        /*if (indice < total - 1) {
            android.view.View linea = new android.view.View(this);
            android.widget.LinearLayout.LayoutParams lineaParams = 
                new android.widget.LinearLayout.LayoutParams(3, 40);
            lineaParams.setMargins(24, 0, 0, 0);
            linea.setLayoutParams(lineaParams);
            linea.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            binding.itinerarioContainer.addView(linea);
        }*/
    }
    
    /**
     * Mostrar servicios adicionales reales desde Firebase
     */
    private void mostrarServiciosReales() {
        binding.serviciosContainer.removeAllViews();
        
        for (Map<String, Object> servicio : serviciosData) {
            String nombre = (String) servicio.get("nombre");
            Boolean esPagado = (Boolean) servicio.get("esPagado");
            Object precioObj = servicio.get("precio");
            
            if (nombre != null) {
                android.widget.LinearLayout servicioLayout = new android.widget.LinearLayout(this);
                servicioLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                servicioLayout.setPadding(0, 8, 0, 8);
                
                android.widget.TextView iconoView = new android.widget.TextView(this);
                iconoView.setText("✓ ");
                iconoView.setTextSize(16);
                iconoView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                iconoView.setPadding(0, 0, 8, 0);
                
                android.widget.TextView nombreView = new android.widget.TextView(this);
                String textoServicio = nombre;
                
                if (esPagado != null && esPagado && precioObj != null) {
                    double precio = precioObj instanceof Long ? 
                        ((Long) precioObj).doubleValue() : (Double) precioObj;
                    textoServicio += " (S/ " + (int)precio + ")";
                } else {
                    textoServicio += " (Incluido)";
                }
                
                nombreView.setText(textoServicio);
                nombreView.setTextSize(14);
                nombreView.setTextColor(getResources().getColor(android.R.color.black));
                
                servicioLayout.addView(iconoView);
                servicioLayout.addView(nombreView);
                binding.serviciosContainer.addView(servicioLayout);
            }
        }
    }
    
    /**
     * Mostrar ruta en el mapa con todos los puntos del itinerario
     */
    private void mostrarRutaEnMapa() {
        if (mMap == null || itinerarioData == null || itinerarioData.isEmpty()) {
            return;
        }
        
        mMap.clear();
        
        // Crear polyline para conectar los puntos
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(getResources().getColor(R.color.brand_purple_dark))
                .width(8);
        
        // Builder para ajustar la cámara a todos los puntos
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        
        // Agregar marcadores para cada punto del itinerario
        for (int i = 0; i < itinerarioData.size(); i++) {
            Map<String, Object> punto = itinerarioData.get(i);
            Double latitud = (Double) punto.get("latitud");
            Double longitud = (Double) punto.get("longitud");
            String nombre = (String) punto.get("nombre");
            
            if (latitud != null && longitud != null) {
                LatLng position = new LatLng(latitud, longitud);
                
                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title((i + 1) + ". " + (nombre != null ? nombre : "Punto del tour")));
                
                // Agregar punto a la polilínea
                polylineOptions.add(position);
                
                // Agregar punto al bounds
                boundsBuilder.include(position);
            }
        }
        
        // Dibujar la línea conectando todos los puntos si hay más de uno
        if (itinerarioData.size() > 1) {
            mMap.addPolyline(polylineOptions);
        }
        
        // Ajustar la cámara para mostrar todos los puntos
        try {
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 150; // padding en píxeles
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            // Si hay un error, centrar en el primer punto
            if (!itinerarioData.isEmpty()) {
                Map<String, Object> primerPunto = itinerarioData.get(0);
                Double lat = (Double) primerPunto.get("latitud");
                Double lng = (Double) primerPunto.get("longitud");
                if (lat != null && lng != null) {
                    LatLng firstPoint = new LatLng(lat, lng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 12));
                }
            }
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
        
        // Si ya tenemos datos del itinerario, mostrarlos en el mapa
        if (itinerarioData != null && !itinerarioData.isEmpty()) {
            mostrarRutaEnMapa();
        } else {
            // Ubicación por defecto mientras se cargan los datos
            LatLng lima = new LatLng(-12.0464, -77.0428);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 12));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
        
        // Recargar datos para validar disponibilidad actualizada
        if (ofertaId != null && !ofertaId.isEmpty()) {
            android.util.Log.d("GuiaTourDetail", "onResume - Recargando datos para validar disponibilidad");
            cargarDatosDesdeFirebase();
        }
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
    
    /**
     * Configurar galería de imágenes con ViewPager2
     */
    private void setupImageGallery(List<String> imageUrls) {
        TourImageAdapter adapter = new TourImageAdapter(this, imageUrls);
        binding.imagesViewPager.setAdapter(adapter);
        
        // Configurar indicadores de página
        setupImageIndicators(imageUrls.size());
        
        // Actualizar indicador al cambiar de página
        binding.imagesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateImageIndicators(position);
            }
        });
    }
    
    /**
     * Crear indicadores de página para la galería
     */
    private void setupImageIndicators(int count) {
        binding.imagesIndicator.removeAllViews();
        
        if (count <= 1) {
            binding.imagesIndicator.setVisibility(android.view.View.GONE);
            return;
        }
        
        binding.imagesIndicator.setVisibility(android.view.View.VISIBLE);
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 0, 4, 0);
        
        for (int i = 0; i < count; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageResource(R.drawable.ic_circle);
            indicators[i].setLayoutParams(params);
            indicators[i].setColorFilter(Color.WHITE);
            indicators[i].setAlpha(0.5f);
            binding.imagesIndicator.addView(indicators[i]);
        }
        
        // Marcar el primero como seleccionado
        if (count > 0) {
            indicators[0].setAlpha(1.0f);
        }
    }
    
    /**
     * Actualizar indicadores al cambiar de imagen
     */
    private void updateImageIndicators(int position) {
        int childCount = binding.imagesIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView indicator = (ImageView) binding.imagesIndicator.getChildAt(i);
            indicator.setAlpha(i == position ? 1.0f : 0.5f);
        }
    }
}
package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.connectifyproject.adapters.Cliente_ServiciosAdapter;
import com.example.connectifyproject.adapters.ImageSliderAdapter;
import com.example.connectifyproject.models.Cliente_Reserva;
import com.example.connectifyproject.models.Cliente_ServicioAdicional;
import com.example.connectifyproject.models.Cliente_Tour;
import com.example.connectifyproject.utils.Cliente_FileStorageManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class cliente_reserva_detalle extends AppCompatActivity {

    private static final String TAG = "ClienteReservaDetalle";

    private MaterialToolbar toolbar;
    private RecyclerView rvServicios;
    private Cliente_ServiciosAdapter serviciosAdapter;

    private TextView tvTourLocation, tvTourPriceMain, tvStartTime, tvEndTime;
    private TextView tvMetodoPago, tvResumenTarjeta, tvMontoTour, tvTotal;
    private TextView tvServicioLinea1, tvMontoLinea1, tvServicioLinea2, tvMontoLinea2;
    private ViewPager2 vpTourImages;
    
    // Botones y tarjetas interactivas
    private View layoutItinerario, cardEmpresa, cardChat, cardDescargar, cardCancelar;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.cliente_reserva_detalle);
            initViews();
            Cliente_Reserva reserva = (Cliente_Reserva) getIntent().getSerializableExtra("reserva_object");
            if (reserva == null) {
                finish();
                return;
            }
            setupToolbar(reserva);
            bindData(reserva);
            setupClickListeners(reserva);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvServicios = findViewById(R.id.rv_servicios_adicionales);
        vpTourImages = findViewById(R.id.vp_tour_images);

        tvTourLocation = findViewById(R.id.tv_tour_location);
        tvTourPriceMain = findViewById(R.id.tv_tour_price_main);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);

        tvMetodoPago = findViewById(R.id.tv_metodo_pago);
        tvResumenTarjeta = findViewById(R.id.tv_resumen_tarjeta);
        tvMontoTour = findViewById(R.id.tv_monto_tour);
        tvTotal = findViewById(R.id.tv_total);

        tvServicioLinea1 = findViewById(R.id.tv_servicio_linea_1);
        tvMontoLinea1 = findViewById(R.id.tv_monto_linea_1);
        tvServicioLinea2 = findViewById(R.id.tv_servicio_linea_2);
        tvMontoLinea2 = findViewById(R.id.tv_monto_linea_2);
        
        // Botones y tarjetas interactivas
        layoutItinerario = findViewById(R.id.layout_itinerario);
        cardEmpresa = findViewById(R.id.card_empresa);
        cardChat = findViewById(R.id.card_chat);
        cardDescargar = findViewById(R.id.card_descargar);
        cardCancelar = findViewById(R.id.card_cancelar);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupToolbar(Cliente_Reserva reserva) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String tituloTour = (reserva != null && reserva.getTour() != null) ? 
                reserva.getTour().getTitulo() : "Detalle de reserva";
            getSupportActionBar().setTitle(tituloTour);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void bindData(@Nullable Cliente_Reserva reserva) {
        try {
            if (reserva == null) return;
            Cliente_Tour tour = reserva.getTour();
            if (tour != null) {
                if (tvTourLocation != null) tvTourLocation.setText(tour.getUbicacion());
                if (tvTourPriceMain != null) tvTourPriceMain.setText(String.format("S/%.2f", tour.getPrecio()));
                
                // Cargar imágenes del tour desde Firebase
                loadTourImages(tour.getId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en bindData - tour info", e);
        }

        try {
            if (tvStartTime != null) tvStartTime.setText(reserva.getFecha() + " - " + reserva.getHoraInicio());
            if (tvEndTime != null) tvEndTime.setText(reserva.getFecha() + " - " + reserva.getHoraFin());

            // Cargar método de pago desde Firebase
            loadPaymentMethodInfo(reserva);

            Cliente_Tour tour = reserva.getTour();
            if (tvMontoTour != null) tvMontoTour.setText(String.format("S/%.2f", (tour != null ? tour.getPrecio() * reserva.getPersonas() : 0.0)));
            if (tvTotal != null) tvTotal.setText(String.format("S/%.2f", reserva.getTotal()));
        } catch (Exception e) {
            Log.e(TAG, "Error en bindData - payment/times", e);
        }

        try {
            // Renderizar líneas de servicios (simplificado a 2 para ejemplo visual)
            List<Cliente_ServicioAdicional> seleccionados = new ArrayList<>();
            if (reserva.getServicios() != null) {
                for (Cliente_ServicioAdicional s : reserva.getServicios()) {
                    if (s != null && s.isSelected()) seleccionados.add(s);
                }
            }
            if (seleccionados.size() > 0) {
                Cliente_ServicioAdicional s1 = seleccionados.get(0);
                if (tvServicioLinea1 != null) tvServicioLinea1.setText("• " + s1.getName() + " (" + reserva.getPersonas() + " personas)");
                if (tvMontoLinea1 != null) tvMontoLinea1.setText(String.format("S/%.2f", s1.getPrice() * reserva.getPersonas()));
            } else {
                if (tvServicioLinea1 != null) tvServicioLinea1.setText("");
                if (tvMontoLinea1 != null) tvMontoLinea1.setText("");
            }
            if (seleccionados.size() > 1) {
                Cliente_ServicioAdicional s2 = seleccionados.get(1);
                if (tvServicioLinea2 != null) tvServicioLinea2.setText("• " + s2.getName() + " (" + reserva.getPersonas() + " personas)");
                if (tvMontoLinea2 != null) tvMontoLinea2.setText(String.format("S/%.2f", s2.getPrice() * reserva.getPersonas()));
            } else {
                if (tvServicioLinea2 != null) tvServicioLinea2.setText("");
                if (tvMontoLinea2 != null) tvMontoLinea2.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Lista de servicios bloqueada (checkbox disabled)
            if (rvServicios != null) {
                rvServicios.setLayoutManager(new LinearLayoutManager(this));
                List<Cliente_ServicioAdicional> servicios = reserva.getServicios();
                if (servicios == null) {
                    servicios = new ArrayList<>();
                }
                serviciosAdapter = new Cliente_ServiciosAdapter(this, servicios);
                serviciosAdapter.setReadOnly(true);
                rvServicios.setAdapter(serviciosAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadTourImages(String tourId) {
        if (tourId == null || tourId.isEmpty()) {
            setupTourImageSlider(new ArrayList<>());
            return;
        }
        
        db.collection("tours_asignados")
                .document(tourId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> imageUrls = (List<String>) doc.get("imagenesUrls");
                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            setupTourImageSlider(imageUrls);
                        } else {
                            setupTourImageSlider(new ArrayList<>());
                        }
                    } else {
                        setupTourImageSlider(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading tour images: " + e.getMessage());
                    setupTourImageSlider(new ArrayList<>());
                });
    }
    
    private void setupTourImageSlider(List<String> imageUrls) {
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageUrls, R.drawable.cliente_tour_lima);
        vpTourImages.setAdapter(adapter);
    }
    
    private void loadPaymentMethodInfo(Cliente_Reserva reserva) {
        if (reserva == null || mAuth.getCurrentUser() == null) {
            return;
        }
        
        String userId = mAuth.getCurrentUser().getUid();
        
        // Buscar el método de pago del usuario en el tour
        db.collection("tours_asignados")
                .document(reserva.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<java.util.Map<String, Object>> participantes = 
                                (List<java.util.Map<String, Object>>) doc.get("participantes");
                        
                        if (participantes != null) {
                            for (java.util.Map<String, Object> participante : participantes) {
                                String participanteUserId = (String) participante.get("usuarioId");
                                if (userId.equals(participanteUserId)) {
                                    String metodoPagoId = (String) participante.get("metodoPagoId");
                                    if (metodoPagoId != null && !metodoPagoId.isEmpty()) {
                                        loadPaymentMethodDetails(userId, metodoPagoId);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading payment method info: " + e.getMessage());
                });
    }
    
    private void loadPaymentMethodDetails(String userId, String metodoPagoId) {
        db.collection("usuarios")
                .document(userId)
                .collection("payment_methods")
                .document(metodoPagoId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String cardType = doc.getString("cardType");
                        String last4 = doc.getString("last4Digits");
                        
                        if (tvMetodoPago != null) tvMetodoPago.setText("Método de pago");
                        if (tvResumenTarjeta != null && cardType != null && last4 != null) {
                            tvResumenTarjeta.setText("Tarjeta " + cardType + " •••• " + last4);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading payment method details: " + e.getMessage());
                });
    }
    
    private void setupClickListeners(Cliente_Reserva reserva) {
        if (reserva == null || reserva.getTour() == null) return;
        
        Cliente_Tour tour = reserva.getTour();
        
        // Itinerario - abrir mapa del tour
        if (layoutItinerario != null) {
            layoutItinerario.setOnClickListener(v -> {
                Intent intent = new Intent(this, cliente_tour_mapa.class);
                intent.putExtra("tour_id", tour.getId());
                intent.putExtra("tour_title", tour.getTitulo());
                startActivity(intent);
            });
        }
        
        // Empresa de turismo - abrir info de la empresa
        if (cardEmpresa != null) {
            cardEmpresa.setOnClickListener(v -> {
                Intent intent = new Intent(this, cliente_empresa_info.class);
                intent.putExtra("company_name", tour.getCompanyName());
                startActivity(intent);
            });
        }
        
        // Chat con la empresa
        if (cardChat != null) {
            cardChat.setOnClickListener(v -> {
                Intent intent = new Intent(this, cliente_chat_conversation.class);
                // Agregar datos necesarios para el chat
                startActivity(intent);
            });
        }
        
        // Descargar recibo PDF
        if (cardDescargar != null) {
            cardDescargar.setOnClickListener(v -> {
                descargarReciboPDF(reserva);
            });
        }
        
        // Cancelar reserva - mostrar diálogo de confirmación
        if (cardCancelar != null) {
            cardCancelar.setOnClickListener(v -> {
                mostrarDialogoCancelarReserva();
            });
        }
    }
    
    private void mostrarDialogoCancelarReserva() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancelar reserva")
                .setMessage("¿Estás seguro de que deseas cancelar esta reserva?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    // Por ahora solo cerramos el dialog
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    
    private void descargarReciboPDF(Cliente_Reserva reserva) {
        if (reserva == null) {
            Toast.makeText(this, "Error: No hay datos de reserva disponibles", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Cliente_FileStorageManager fileManager = new Cliente_FileStorageManager(this);
            boolean success = fileManager.downloadReservationPDF(reserva);
            
            if (success) {
                Toast.makeText(this, "Recibo descargado exitosamente en Descargas", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error al generar el recibo PDF", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al descargar el recibo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

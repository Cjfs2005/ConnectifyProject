package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.connectifyproject.adapters.Cliente_TarjetaAdapter;
import com.example.connectifyproject.models.Cliente_PaymentMethod;

import java.util.ArrayList;
import java.util.List;

public class cliente_metodo_pago extends AppCompatActivity {
    
    private RecyclerView rvPaymentMethods;
    private TextView tvTotalPrice;
    private MaterialButton btnReservar;
    private FloatingActionButton fabAddMethod;
    private Cliente_TarjetaAdapter tarjetaAdapter;
    private String totalPrice;
    private String tourTitle;
    private String tourId;
    private int peopleCount;
    
    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ListenerRegistration paymentMethodsListener;
    
    // Data
    private List<Cliente_PaymentMethod> paymentMethods;
    private Cliente_PaymentMethod selectedPaymentMethod;
    
    // Empty state
    private LinearLayout layoutEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_metodo_pago);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        
        // Verificar usuario autenticado
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para continuar", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        paymentMethods = new ArrayList<>();
        
        initViews();
        setupToolbar();
        getTotalPriceFromIntent();
        setupRecyclerView();
        loadPaymentMethods();
        setupClickListeners();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paymentMethodsListener != null) {
            paymentMethodsListener.remove();
        }
    }

    private void initViews() {
        rvPaymentMethods = findViewById(R.id.rv_payment_methods);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnReservar = findViewById(R.id.btn_reservar);
        fabAddMethod = findViewById(R.id.fab_add_method);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
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
        tourTitle = getIntent().getStringExtra("tour_title");
        tourId = getIntent().getStringExtra("tour_id");
        peopleCount = getIntent().getIntExtra("people_count", 1);
        
        if (totalPrice != null) {
            tvTotalPrice.setText(totalPrice);
        }
    }

    private void setupRecyclerView() {
        tarjetaAdapter = new Cliente_TarjetaAdapter(paymentMethods, this::onTarjetaSelected);
        rvPaymentMethods.setLayoutManager(new LinearLayoutManager(this));
        rvPaymentMethods.setAdapter(tarjetaAdapter);
    }
    
    private void loadPaymentMethods() {
        // Listener en tiempo real para la subcolección payment_methods
        paymentMethodsListener = db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("payment_methods")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error al cargar métodos de pago: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        paymentMethods.clear();
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                Cliente_PaymentMethod paymentMethod = document.toObject(Cliente_PaymentMethod.class);
                                if (paymentMethod != null) {
                                    paymentMethod.setId(document.getId());
                                    paymentMethods.add(paymentMethod);
                                    
                                    // Si es el método por defecto, seleccionarlo automáticamente
                                    if (paymentMethod.isDefault()) {
                                        selectedPaymentMethod = paymentMethod;
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e("PaymentMethods", "Error parsing document: " + document.getId(), e);
                            }
                        }
                        
                        if (!paymentMethods.isEmpty()) {
                            hideEmptyState();
                            tarjetaAdapter.notifyDataSetChanged();
                        } else {
                            showEmptyState();
                        }
                    }
                });
    }

    private void onTarjetaSelected(Cliente_PaymentMethod tarjeta, int position) {
        selectedPaymentMethod = tarjeta;
    }
    
    private void showEmptyState() {
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.VISIBLE);
        }
        rvPaymentMethods.setVisibility(View.GONE);
        btnReservar.setEnabled(false);
    }
    
    private void hideEmptyState() {
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
        }
        rvPaymentMethods.setVisibility(View.VISIBLE);
        btnReservar.setEnabled(true);
    }

    private void setupClickListeners() {
        btnReservar.setOnClickListener(v -> {
            if (selectedPaymentMethod == null) {
                Toast.makeText(this, "Por favor selecciona un método de pago", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Guardar reserva en Firebase
            saveReservationToFirebase();
        });
        
        fabAddMethod.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_agregar_metodo_pago.class);
            startActivity(intent);
        });
    }
    
    private void saveReservationToFirebase() {
        if (tourId == null || currentUser == null) {
            Toast.makeText(this, "Error: Información incompleta", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnReservar.setEnabled(false);
        btnReservar.setText("Procesando...");
        
        // ✅ VALIDAR REGLA DE 2 HORAS antes de procesar pago
        db.collection("tours_asignados")
                .document(tourId)
                .get()
                .addOnSuccessListener(tourDoc -> {
                    if (!tourDoc.exists()) {
                        showError("Tour no encontrado");
                        return;
                    }
                    
                    com.google.firebase.Timestamp fechaRealizacion = tourDoc.getTimestamp("fechaRealizacion");
                    String horaInicio = tourDoc.getString("horaInicio");
                    
                    boolean disponible = com.example.connectifyproject.utils.TourTimeValidator
                        .tourDisponibleParaInscripcion(fechaRealizacion, horaInicio);
                    
                    if (!disponible) {
                        String mensaje = com.example.connectifyproject.utils.TourTimeValidator
                            .getMensajeTourNoDisponible(fechaRealizacion, horaInicio);
                        showError(mensaje);
                        
                        // Regresar a la lista de tours después de 2 segundos
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(this, cliente_tours.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }, 2000);
                        return;
                    }
                    
                    // Si está disponible, proceder con el pago
                    procesarPago();
                })
                .addOnFailureListener(e -> {
                    showError("Error al validar disponibilidad: " + e.getMessage());
                });
    }
    
    /**
     * Procesar el pago después de validar la disponibilidad del tour
     */
    private void procesarPago() {
        // Primero obtener datos del usuario
        db.collection("usuarios")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String nombreCompleto = userDoc.getString("nombresApellidos");
                        String email = userDoc.getString("email");
                        String telefono = userDoc.getString("telefono");
                        String tipoDocumento = userDoc.getString("tipoDocumento");
                        String numeroDocumento = userDoc.getString("numeroDocumento");
                        
                        // Crear objeto participante
                        java.util.Map<String, Object> participante = new java.util.HashMap<>();
                        participante.put("usuarioId", currentUser.getUid());
                        participante.put("nombre", nombreCompleto != null ? nombreCompleto : "Cliente");
                        participante.put("email", email != null ? email : "");
                        participante.put("telefono", telefono != null ? telefono : "");
                        participante.put("tipoDocumento", tipoDocumento != null ? tipoDocumento : "DNI");
                        participante.put("numeroDocumento", numeroDocumento != null ? numeroDocumento : "");
                        participante.put("numeroPersonas", peopleCount);
                        participante.put("fechaInscripcion", com.google.firebase.Timestamp.now());
                        participante.put("metodoPagoId", selectedPaymentMethod.getId());
                        participante.put("montoTotal", totalPrice);
                        
                        // Agregar participante al tour
                        addParticipantToTour(participante);
                    } else {
                        showError("No se encontró información del usuario");
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Error al obtener datos del usuario: " + e.getMessage());
                });
    }
    
    private void addParticipantToTour(java.util.Map<String, Object> participante) {
        db.collection("tours_asignados")
                .document(tourId)
                .update("participantes", com.google.firebase.firestore.FieldValue.arrayUnion(participante),
                       "numeroParticipantesTotal", com.google.firebase.firestore.FieldValue.increment(peopleCount))
                .addOnSuccessListener(aVoid -> {
                    // Reserva guardada exitosamente
                    navigateToConfirmation();
                })
                .addOnFailureListener(e -> {
                    showError("Error al guardar reserva: " + e.getMessage());
                });
    }
    
    private void navigateToConfirmation() {
        Intent intent = new Intent(this, cliente_reserva_realizada.class);
        intent.putExtra("total_price", totalPrice);
        intent.putExtra("tour_title", tourTitle);
        intent.putExtra("tour_id", tourId);
        intent.putExtra("people_count", peopleCount);
        intent.putExtra("payment_method_id", selectedPaymentMethod.getId());
        intent.putExtra("payment_method_last4", selectedPaymentMethod.getLast4Digits());
        intent.putExtra("payment_method_brand", selectedPaymentMethod.getCardBrand());
        startActivity(intent);
        finish();
    }
    
    private void showError(String message) {
        android.util.Log.e("MetodoPago", message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        btnReservar.setEnabled(true);
        btnReservar.setText("Reservar");
    }
}
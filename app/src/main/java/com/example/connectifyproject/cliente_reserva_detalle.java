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

    private TextView tvTourLocation, tvTourPriceMain, tvStartTime, tvEndTime;
    private TextView tvMetodoPago, tvResumenTarjeta, tvMontoTour, tvTotal;
    private TextView tvServicioLinea1, tvMontoLinea1, tvServicioLinea2, tvMontoLinea2;
    private ViewPager2 vpTourImages;
    
    // Información del guía
    private TextView tvGuiaNombre, tvGuiaTelefono;
    private ImageView ivGuiaFoto;
    private View layoutGuiaInfo;
    
    // Botones y tarjetas interactivas
    private View layoutItinerario, cardEmpresa, cardChat, cardDescargar, cardCancelar;
    private View cardQrCheckin, cardQrCheckout;
    
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
        
        // Información del guía
        tvGuiaNombre = findViewById(R.id.tv_guia_nombre);
        tvGuiaTelefono = findViewById(R.id.tv_guia_telefono);
        ivGuiaFoto = findViewById(R.id.iv_guia_foto);
        layoutGuiaInfo = findViewById(R.id.layout_guia_info);
        
        // Botones y tarjetas interactivas
        layoutItinerario = findViewById(R.id.layout_itinerario);
        cardEmpresa = findViewById(R.id.card_empresa);
        cardChat = findViewById(R.id.card_chat);
        cardDescargar = findViewById(R.id.card_descargar);
        cardCancelar = findViewById(R.id.card_cancelar);
        cardQrCheckin = findViewById(R.id.card_qr_checkin);
        cardQrCheckout = findViewById(R.id.card_qr_checkout);
        
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
                // Mostrar ciudad del tour en lugar de ubicación
                String ciudad = tour.getCiudad();
                if (tvTourLocation != null) tvTourLocation.setText(ciudad != null && !ciudad.isEmpty() ? ciudad : tour.getUbicacion());
                if (tvTourPriceMain != null) tvTourPriceMain.setText(String.format("S/%.2f", tour.getPrecio()));
                
                // Cargar imágenes del tour desde Firebase
                loadTourImages(tour.getId());
                
                // Cargar información del guía
                loadGuiaInfo(tour.getId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en bindData - tour info", e);
        }

        try {
            if (tvStartTime != null) tvStartTime.setText(reserva.getFecha() + " - " + reserva.getHoraInicio());
            if (tvEndTime != null) tvEndTime.setText(reserva.getFecha() + " - " + reserva.getHoraFin());

            // Cargar método de pago y servicios desde Firebase
            loadPaymentMethodInfo(reserva);
            loadServiciosAdicionales(reserva);

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
                                // Buscar por clienteId (no usuarioId)
                                String clienteId = (String) participante.get("clienteId");
                                if (userId.equals(clienteId)) {
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
    
    private void loadServiciosAdicionales(Cliente_Reserva reserva) {
        if (reserva == null || reserva.getTour() == null || mAuth.getCurrentUser() == null) {
            // Si no hay servicios, mostrar mensaje
            if (tvServicioLinea1 != null) tvServicioLinea1.setText("No se agregaron servicios adicionales");
            if (tvMontoLinea1 != null) tvMontoLinea1.setText("");
            if (tvServicioLinea2 != null) tvServicioLinea2.setVisibility(View.GONE);
            if (tvMontoLinea2 != null) tvMontoLinea2.setVisibility(View.GONE);
            return;
        }
        
        String userId = mAuth.getCurrentUser().getUid();
        String tourId = reserva.getTour().getId();
        
        // Obtener servicios desde el documento del tour
        db.collection("tours_asignados")
                .document(tourId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Buscar al participante actual
                        List<java.util.Map<String, Object>> participantes = 
                                (List<java.util.Map<String, Object>>) doc.get("participantes");
                        
                        if (participantes != null) {
                            for (java.util.Map<String, Object> participante : participantes) {
                                String clienteId = (String) participante.get("clienteId");
                                if (userId.equals(clienteId)) {
                                    // Obtener servicios adicionales del participante
                                    List<java.util.Map<String, Object>> serviciosArray = 
                                            (List<java.util.Map<String, Object>>) participante.get("serviciosAdicionales");
                                    
                                    int numPersonas = reserva.getPersonas();
                                    
                                    if (serviciosArray != null && !serviciosArray.isEmpty()) {
                                        // Mostrar servicios en las líneas
                                        for (int i = 0; i < serviciosArray.size() && i < 2; i++) {
                                            java.util.Map<String, Object> servicioMap = serviciosArray.get(i);
                                            String nombre = (String) servicioMap.get("nombre");
                                            Object precioObj = servicioMap.get("precio");
                                            double precio = 0.0;
                                            if (precioObj instanceof Number) {
                                                precio = ((Number) precioObj).doubleValue();
                                            }
                                            
                                            if (i == 0) {
                                                if (tvServicioLinea1 != null) {
                                                    tvServicioLinea1.setText("• " + nombre + " (" + numPersonas + " personas)");
                                                    tvServicioLinea1.setVisibility(View.VISIBLE);
                                                }
                                                if (tvMontoLinea1 != null) {
                                                    tvMontoLinea1.setText(String.format("S/%.2f", precio * numPersonas));
                                                    tvMontoLinea1.setVisibility(View.VISIBLE);
                                                }
                                            } else if (i == 1) {
                                                if (tvServicioLinea2 != null) {
                                                    tvServicioLinea2.setText("• " + nombre + " (" + numPersonas + " personas)");
                                                    tvServicioLinea2.setVisibility(View.VISIBLE);
                                                }
                                                if (tvMontoLinea2 != null) {
                                                    tvMontoLinea2.setText(String.format("S/%.2f", precio * numPersonas));
                                                    tvMontoLinea2.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }
                                    } else {
                                        // No hay servicios
                                        if (tvServicioLinea1 != null) tvServicioLinea1.setText("No se agregaron servicios adicionales");
                                        if (tvMontoLinea1 != null) tvMontoLinea1.setText("");
                                        if (tvServicioLinea2 != null) tvServicioLinea2.setVisibility(View.GONE);
                                        if (tvMontoLinea2 != null) tvMontoLinea2.setVisibility(View.GONE);
                                    }
                                    break;
                                }
                            }
                        } else {
                            // No hay participantes
                            if (tvServicioLinea1 != null) tvServicioLinea1.setText("No se agregaron servicios adicionales");
                            if (tvMontoLinea1 != null) tvMontoLinea1.setText("");
                            if (tvServicioLinea2 != null) tvServicioLinea2.setVisibility(View.GONE);
                            if (tvMontoLinea2 != null) tvMontoLinea2.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading servicios adicionales: " + e.getMessage());
                    if (tvServicioLinea1 != null) tvServicioLinea1.setText("No se agregaron servicios adicionales");
                    if (tvMontoLinea1 != null) tvMontoLinea1.setText("");
                    if (tvServicioLinea2 != null) tvServicioLinea2.setVisibility(View.GONE);
                    if (tvMontoLinea2 != null) tvMontoLinea2.setVisibility(View.GONE);
                });
    }
    
    private void setupClickListeners(Cliente_Reserva reserva) {
        if (reserva == null || reserva.getTour() == null) return;
        
        Cliente_Tour tour = reserva.getTour();
        
        // ✅ Si es una reserva pasada, ocultar botones de cancelar y QR
        boolean esPasada = "Pasada".equals(reserva.getEstado());
        if (esPasada) {
            if (cardCancelar != null) cardCancelar.setVisibility(View.GONE);
            if (cardQrCheckin != null) cardQrCheckin.setVisibility(View.GONE);
            if (cardQrCheckout != null) cardQrCheckout.setVisibility(View.GONE);
        }
        
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
                intent.putExtra("empresa_id", tour.getEmpresaId());
                intent.putExtra("company_name", tour.getCompanyName());
                startActivity(intent);
            });
        }
        
        // Chat con la empresa
        if (cardChat != null) {
            cardChat.setOnClickListener(v -> {
                String empresaId = tour.getEmpresaId();
                String empresaNombre = tour.getCompanyName();
                if (empresaId != null && empresaNombre != null) {
                    openOrCreateChat(empresaId, empresaNombre);
                } else {
                    Toast.makeText(this, "Error: No se puede iniciar el chat", Toast.LENGTH_SHORT).show();
                }
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
                mostrarDialogoCancelarReserva(reserva);
            });
        }
        
        // Mostrar QR Check-in
        if (cardQrCheckin != null) {
            cardQrCheckin.setOnClickListener(v -> {
                mostrarQRCliente("check_in", reserva);
            });
        }
        
        // Mostrar QR Check-out
        if (cardQrCheckout != null) {
            cardQrCheckout.setOnClickListener(v -> {
                mostrarQRCliente("check_out", reserva);
            });
        }
        
        // Gestionar visibilidad de botones QR según el estado del tour
        gestionarVisibilidadBotonesQR(reserva);
    }
    
    private void mostrarDialogoCancelarReserva(Cliente_Reserva reserva) {
        if (reserva == null || reserva.getTour() == null) {
            Toast.makeText(this, "Error: No se encontraron datos de la reserva", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Obtener datos del tour para validación
        String tourId = reserva.getTour().getId();
        String fecha = reserva.getFecha();
        String horaInicio = reserva.getHoraInicio();
        
        // Validar tiempo restante (debe haber más de 2 horas 10 minutos)
        db.collection("tours_asignados")
                .document(tourId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Error: Tour no encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    com.google.firebase.Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                    double horasRestantes = com.example.connectifyproject.utils.TourTimeValidator
                            .calcularHorasHastaInicio(fechaRealizacion, horaInicio);
                    
                    Log.d(TAG, "Horas restantes para cancelación: " + horasRestantes);
                    
                    // Validar que falten más de 2h 10min (2.166667 horas)
                    if (horasRestantes < 2.166667) {
                        String mensaje;
                        if (horasRestantes < 0) {
                            mensaje = "No puedes cancelar una reserva que ya ha comenzado.";
                        } else {
                            mensaje = "Solo puedes cancelar reservas con más de 2 horas de anticipación.\n\n" +
                                    "Tiempo restante: " + String.format("%.1f", horasRestantes) + " horas";
                        }
                        
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("No se puede cancelar")
                                .setMessage(mensaje)
                                .setPositiveButton("Entendido", null)
                                .show();
                        return;
                    }
                    
                    // Mostrar diálogo de confirmación
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Cancelar reserva")
                            .setMessage("¿Estás seguro de que deseas cancelar esta reserva?\n\n" +
                                    "Esta acción no se puede deshacer.")
                            .setPositiveButton("Confirmar", (dialog, which) -> {
                                cancelarReservaCliente(reserva, tourId, fechaRealizacion, horaInicio);
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al validar la reserva: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error validando tiempo de cancelación", e);
                });
    }
    
    /**
     * Cancelar reserva del cliente
     * 1. Remover del array participantes del tour
     * 2. Crear registro en reservas_canceladas
     */
    private void cancelarReservaCliente(Cliente_Reserva reserva, String tourId, 
                                       com.google.firebase.Timestamp fechaRealizacion, 
                                       String horaInicio) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        // --- Crear notificación y log para el admin (empresa) ---
        String clienteNombre = mAuth.getCurrentUser().getDisplayName() != null ? mAuth.getCurrentUser().getDisplayName() : "Cliente";
        String tourNombre = reserva != null && reserva.getTour() != null ? reserva.getTour().getTitulo() : "Tour";
        String notiTitulo = "Reserva cancelada";
        String notiDesc = "El cliente " + clienteNombre + " canceló la reserva del tour '" + tourNombre + "'.";

        // Obtener el id real del admin (empresaId) desde el tour antes de notificar
        String empresaId = reserva != null && reserva.getTour() != null ? reserva.getTour().getEmpresaId() : null;
        if (empresaId != null && !empresaId.isEmpty()) {
            com.example.connectifyproject.utils.NotificacionLogUtils.crearNotificacion(notiTitulo, notiDesc, empresaId);
        } else {
            // Fallback: notificar sin destinatario específico
            com.example.connectifyproject.utils.NotificacionLogUtils.crearNotificacion(notiTitulo, notiDesc, "");
        }
        com.example.connectifyproject.utils.NotificacionLogUtils.crearLog(notiTitulo, notiDesc);
        
        // Mostrar loader
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Cancelando reserva...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        String clienteId = mAuth.getCurrentUser().getUid();
        String horaFin = reserva.getHoraFin();
        
        // 1. Obtener el participante actual del array
        db.collection("tours_asignados")
                .document(tourId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error: Tour no encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    List<java.util.Map<String, Object>> participantes = 
                            (List<java.util.Map<String, Object>>) doc.get("participantes");
                    
                    if (participantes == null || participantes.isEmpty()) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error: No hay participantes en este tour", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Buscar el participante actual
                    java.util.Map<String, Object> participanteActual = null;
                    int indexToRemove = -1;
                    
                    for (int i = 0; i < participantes.size(); i++) {
                        java.util.Map<String, Object> p = participantes.get(i);
                        String pClienteId = (String) p.get("clienteId");
                        if (clienteId.equals(pClienteId)) {
                            participanteActual = p;
                            indexToRemove = i;
                            break;
                        }
                    }
                    
                    if (participanteActual == null) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error: No se encontró tu reserva en este tour", 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // 2. Crear registro en reservas_canceladas
                    java.util.Map<String, Object> reservaCancelada = new java.util.HashMap<>();
                    reservaCancelada.put("tourId", tourId);
                    reservaCancelada.put("tourTitulo", reserva.getTour().getTitulo());
                    reservaCancelada.put("fechaRealizacion", fechaRealizacion);
                    reservaCancelada.put("horaInicio", horaInicio);
                    reservaCancelada.put("horaFin", horaFin);
                    reservaCancelada.put("fechaCancelacion", com.google.firebase.Timestamp.now());
                    reservaCancelada.put("motivoCancelacion", "Cancelación por cliente");
                    
                    // Copiar datos del participante
                    reservaCancelada.put("clienteId", participanteActual.get("clienteId"));
                    reservaCancelada.put("nombre", participanteActual.get("nombre"));
                    reservaCancelada.put("correo", participanteActual.get("correo"));
                    reservaCancelada.put("numeroPersonas", participanteActual.get("numeroPersonas"));
                    reservaCancelada.put("montoTotal", participanteActual.get("montoTotal"));
                    
                    final int finalIndex = indexToRemove;
                    
                    db.collection("reservas_canceladas")
                            .add(reservaCancelada)
                            .addOnSuccessListener(docRef -> {
                                Log.d(TAG, "✅ Reserva guardada en reservas_canceladas");
                                
                                // 3. Remover del array participantes
                                participantes.remove(finalIndex);
                                
                                db.collection("tours_asignados")
                                        .document(tourId)
                                        .update("participantes", participantes)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "✅ Participante removido del tour");
                                            progressDialog.dismiss();
                                            Toast.makeText(this, 
                                                    "Reserva cancelada exitosamente", 
                                                    Toast.LENGTH_LONG).show();
                                            
                                            // Cerrar activity y volver a la lista con resultado OK
                                            setResult(RESULT_OK);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, 
                                                    "Error al actualizar el tour: " + e.getMessage(), 
                                                    Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Error removiendo participante", e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, 
                                        "Error al guardar la cancelación: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error guardando en reservas_canceladas", e);
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error al obtener datos del tour: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error obteniendo tour", e);
                });
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
    
    private void mostrarQRCliente(String tipoQR, Cliente_Reserva reserva) {
        if (reserva == null || reserva.getTour() == null) {
            Toast.makeText(this, "Error: No hay información del tour disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, cliente_show_qr.class);
        intent.putExtra("tourId", reserva.getTour().getId());
        intent.putExtra("reservaId", reserva.getId());
        intent.putExtra("tipoQR", tipoQR);
        intent.putExtra("tourTitulo", reserva.getTour().getTitulo());
        startActivity(intent);
    }
    
    private void gestionarVisibilidadBotonesQR(Cliente_Reserva reserva) {
        if (reserva == null) return;
        
        // Obtener estado del tour desde Firebase para determinar qué botones mostrar
        String tourId = reserva.getTour() != null ? reserva.getTour().getId() : null;
        if (tourId == null || tourId.isEmpty()) return;
        
        db.collection("tours_asignados").document(tourId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String estado = documentSnapshot.getString("estado");
                    if (estado != null) {
                        String estadoLower = estado.toLowerCase();
                        
                        // Mostrar botón check-in si el estado es "check_in" o "confirmado"
                        if (estadoLower.equals("check_in") || estadoLower.equals("confirmado") || 
                            estadoLower.equals("check-in disponible")) {
                            if (cardQrCheckin != null) {
                                cardQrCheckin.setVisibility(View.VISIBLE);
                            }
                        }
                        
                        // Mostrar botón check-out si el estado es "en_curso", "check_out" o "en progreso"
                        if (estadoLower.equals("en progreso") || estadoLower.equals("check_out") || 
                            estadoLower.equals("en_curso") || estadoLower.equals("check-out disponible")) {
                            if (cardQrCheckout != null) {
                                cardQrCheckout.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener estado del tour: " + e.getMessage());
            });
    }
    
    private void loadGuiaInfo(String tourId) {
        if (tourId == null || tourId.isEmpty()) {
            if (layoutGuiaInfo != null) layoutGuiaInfo.setVisibility(View.GONE);
            return;
        }
        
        db.collection("tours_asignados")
                .document(tourId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        java.util.Map<String, Object> guiaAsignado = 
                                (java.util.Map<String, Object>) doc.get("guiaAsignado");
                        
                        if (guiaAsignado != null) {
                            String nombresCompletos = (String) guiaAsignado.get("nombresCompletos");
                            String numeroTelefono = (String) guiaAsignado.get("numeroTelefono");
                            String identificadorUsuario = (String) guiaAsignado.get("identificadorUsuario");
                            
                            if (nombresCompletos != null && tvGuiaNombre != null) {
                                tvGuiaNombre.setText(nombresCompletos);
                            }
                            
                            if (numeroTelefono != null && tvGuiaTelefono != null) {
                                tvGuiaTelefono.setText("📞 " + numeroTelefono);
                            }
                            
                            // Cargar foto del guía
                            if (identificadorUsuario != null && !identificadorUsuario.isEmpty()) {
                                db.collection("usuarios")
                                        .document(identificadorUsuario)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                String photoUrl = userDoc.getString("photoUrl");
                                                if (photoUrl != null && !photoUrl.isEmpty() && ivGuiaFoto != null) {
                                                    com.bumptech.glide.Glide.with(this)
                                                            .load(photoUrl)
                                                            .placeholder(R.drawable.ic_person)
                                                            .circleCrop()
                                                            .into(ivGuiaFoto);
                                                }
                                            }
                                        });
                            }
                            
                            if (layoutGuiaInfo != null) layoutGuiaInfo.setVisibility(View.VISIBLE);
                        } else {
                            if (layoutGuiaInfo != null) layoutGuiaInfo.setVisibility(View.GONE);
                        }
                    } else {
                        if (layoutGuiaInfo != null) layoutGuiaInfo.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading guia info: " + e.getMessage());
                    if (layoutGuiaInfo != null) layoutGuiaInfo.setVisibility(View.GONE);
                });
    }
    
    private void openOrCreateChat(String empresaId, String empresaNombre) {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Toast.makeText(this, "Debes iniciar sesión para chatear", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // El chatId se forma concatenando clientId_adminId
        String chatId = currentUserId + "_" + empresaId;
        
        // Verificar si ya existe un chat
        db.collection("chats")
                .document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // El chat ya existe, abrirlo
                        openChatConversation(empresaId, empresaNombre);
                    } else {
                        // El chat no existe, crearlo
                        createAndOpenChat(chatId, currentUserId, empresaId, empresaNombre);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al verificar chat: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void createAndOpenChat(String chatId, String clientId, String empresaId, String empresaNombre) {
        // Obtener datos del cliente actual
        db.collection("usuarios")
                .document(clientId)
                .get()
                .addOnSuccessListener(clientDoc -> {
                    if (clientDoc.exists()) {
                        String clientName = clientDoc.getString("nombresApellidos");
                        String clientPhoto = clientDoc.getString("photoUrl");
                        
                        // Obtener datos de la empresa
                        db.collection("usuarios")
                                .document(empresaId)
                                .get()
                                .addOnSuccessListener(adminDoc -> {
                                    if (adminDoc.exists()) {
                                        String adminName = adminDoc.getString("nombreEmpresa");
                                        String adminPhoto = adminDoc.getString("photoUrl");
                                        
                                        // Crear documento del chat
                                        java.util.Map<String, Object> chatData = new java.util.HashMap<>();
                                        chatData.put("chatId", chatId);
                                        chatData.put("clientId", clientId);
                                        chatData.put("clientName", clientName != null ? clientName : "Cliente");
                                        chatData.put("clientPhotoUrl", clientPhoto != null ? clientPhoto : "");
                                        chatData.put("adminId", empresaId);
                                        chatData.put("adminName", adminName != null ? adminName : empresaNombre);
                                        chatData.put("adminPhotoUrl", adminPhoto != null ? adminPhoto : "");
                                        chatData.put("active", true);
                                        chatData.put("lastMessage", "");
                                        chatData.put("lastMessageTime", com.google.firebase.Timestamp.now());
                                        chatData.put("lastSenderId", "");
                                        chatData.put("unreadCountClient", 0);
                                        chatData.put("unreadCountAdmin", 0);
                                        
                                        db.collection("chats")
                                                .document(chatId)
                                                .set(chatData)
                                                .addOnSuccessListener(aVoid -> {
                                                    openChatConversation(empresaId, empresaNombre);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Error al crear chat: " + e.getMessage(), 
                                                            Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                });
                    }
                });
    }
    
    private void openChatConversation(String empresaId, String empresaNombre) {
        Intent intent = new Intent(this, cliente_chat_conversation.class);
        intent.putExtra("admin_id", empresaId);
        intent.putExtra("admin_name", empresaNombre);
        startActivity(intent);
    }
}

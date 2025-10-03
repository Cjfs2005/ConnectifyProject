package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ServiciosAdapter;
import com.example.connectifyproject.models.Cliente_Reserva;
import com.example.connectifyproject.models.Cliente_ServicioAdicional;
import com.example.connectifyproject.models.Cliente_Tour;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class cliente_reserva_detalle extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvServicios;
    private Cliente_ServiciosAdapter serviciosAdapter;

    private TextView tvTourLocation, tvTourPriceMain, tvStartTime, tvEndTime;
    private TextView tvMetodoPago, tvResumenTarjeta, tvMontoTour, tvTotal;
    private TextView tvServicioLinea1, tvMontoLinea1, tvServicioLinea2, tvMontoLinea2;
    private ImageView ivHero;
    
    // Botones y tarjetas interactivas
    private View layoutItinerario, cardEmpresa, cardChat, cardCancelar;

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
        ivHero = findViewById(R.id.iv_tour_hero);

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
        cardCancelar = findViewById(R.id.card_cancelar);
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
                // Hero: por ahora imagen por defecto
                if (ivHero != null) ivHero.setImageResource(R.drawable.cliente_tour_lima);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (tvStartTime != null) tvStartTime.setText(reserva.getFecha() + " - " + reserva.getHoraInicio());
            if (tvEndTime != null) tvEndTime.setText(reserva.getFecha() + " - " + reserva.getHoraFin());

            if (reserva.getMetodoPago() != null) {
                if (tvMetodoPago != null) tvMetodoPago.setText("Método de pago");
                if (tvResumenTarjeta != null) {
                    tvResumenTarjeta.setText(
                            (reserva.getMetodoPago().getCardType() != null ? "Tarjeta " + reserva.getMetodoPago().getCardType() + " " : "Tarjeta ")
                                    + reserva.getMetodoPago().getMaskedCardNumber());
                }
            }

            Cliente_Tour tour = reserva.getTour();
            if (tvMontoTour != null) tvMontoTour.setText(String.format("S/%.2f", (tour != null ? tour.getPrecio() : 0.0)));
            if (tvTotal != null) tvTotal.setText(String.format("S/%.2f", reserva.getTotal()));
        } catch (Exception e) {
            e.printStackTrace();
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
}

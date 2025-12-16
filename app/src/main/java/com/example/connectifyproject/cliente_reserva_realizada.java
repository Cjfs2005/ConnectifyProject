package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.models.Cliente_Reserva;
import com.example.connectifyproject.utils.Cliente_FileStorageManager;
import com.example.connectifyproject.utils.NotificationHelper;
import com.example.connectifyproject.utils.NotificacionLogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.button.MaterialButton;

public class cliente_reserva_realizada extends AppCompatActivity {
    
    private MaterialButton btnAceptar, btnDescargar;
    private Cliente_FileStorageManager fileManager;
    private Cliente_Reserva reserva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_reserva_realizada);

        fileManager = new Cliente_FileStorageManager(this);
        
        // Obtener datos de la reserva desde el Intent (enviados desde cliente_metodo_pago)
        String tourId = getIntent().getStringExtra("tour_id");
        String tourTitle = getIntent().getStringExtra("tour_title");
        String totalPrice = getIntent().getStringExtra("total_price");
        int peopleCount = getIntent().getIntExtra("people_count", 1);
        String paymentMethodId = getIntent().getStringExtra("payment_method_id");
        String paymentMethodLast4 = getIntent().getStringExtra("payment_method_last4");
        String paymentMethodBrand = getIntent().getStringExtra("payment_method_brand");

        // Construir un objeto reserva mínimo para mostrar y notificar
        reserva = new Cliente_Reserva();
        com.example.connectifyproject.models.Cliente_Tour tour = new com.example.connectifyproject.models.Cliente_Tour();
        tour.setId(tourId);
        tour.setTitle(tourTitle);
        reserva.setTour(tour);
        reserva.setId(tourId);
        reserva.setTotal(totalPrice != null ? Double.parseDouble(totalPrice.replace("S/","").replace(",",".").trim()) : 0.0);
        reserva.setPersonas(peopleCount);
        // Puedes setear más campos si lo necesitas
        
        initViews();
        setupClickListeners();
        
        // Mostrar notificación de reserva confirmada
        showReservationNotification();

        // --- Crear notificación y log para el admin real (empresaId) ---
        // Obtener el adminId (empresaId) desde el documento de la reserva en tours_asignados
        String tourAsignadoId = reserva != null && reserva.getTour() != null ? reserva.getTour().getId() : null;
        String clienteNombre = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Cliente";
        String tourNombre = reserva != null && reserva.getTour() != null ? reserva.getTour().getTitle() : "Tour";
        String notiTitulo = "Nueva reserva";
        String notiDesc = "El cliente " + clienteNombre + " reservó el tour '" + tourNombre + "'.";
        if (tourAsignadoId != null && !tourAsignadoId.isEmpty()) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("tours_asignados")
                .document(tourAsignadoId)
                .get()
                .addOnSuccessListener(doc -> {
                    String adminId = doc.getString("empresaId");
                    if (adminId != null && !adminId.isEmpty()) {
                        NotificacionLogUtils.crearNotificacion(notiTitulo, notiDesc, adminId);
                    }
                    NotificacionLogUtils.crearLog(notiTitulo, notiDesc);
                });
        } else {
            // Fallback: solo log si no se puede obtener el adminId
            NotificacionLogUtils.crearLog(notiTitulo, notiDesc);
        }
    }

    private void initViews() {
        btnAceptar = findViewById(R.id.btn_aceptar);
        btnDescargar = findViewById(R.id.btn_descargar);
        btnDescargar.setVisibility(View.GONE); // Ocultar botón de descargar
    }

    private void setupClickListeners() {
        btnAceptar.setOnClickListener(v -> {
            // Regresar a la lista de tours
            Intent intent = new Intent(this, cliente_tours.class);
            // Clear todas las actividades anteriores de la pila
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        btnDescargar.setOnClickListener(v -> downloadReservation());
    }

    /**
     * Mostrar notificación de reserva confirmada
     */
    private void showReservationNotification() {
        if (reserva != null) {
            NotificationHelper.showReservationConfirmedNotification(this, reserva.getTour().getTitle());
        }
    }

    /**
     * Descargar comprobante de reserva
     */
    private void downloadReservation() {
        if (reserva == null) {
            Toast.makeText(this, "Error: No se encontró información de la reserva", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fileManager.downloadReservationPDF(reserva)) {
            Toast.makeText(this, "Comprobante descargado en Descargas", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error al descargar el comprobante", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Obtener datos de reserva (simulados por ahora)
     */
    private Cliente_Reserva getReservaData() {
        // Por ahora simulamos una reserva
        // En el futuro esto vendrá de Intent o base de datos
        return Cliente_Reserva.getReservaExample();
    }
}
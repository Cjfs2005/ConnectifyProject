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
        
        // Obtener datos de la reserva (simulados)
        reserva = getReservaData();
        
        initViews();
        setupClickListeners();
        
        // Mostrar notificación de reserva confirmada
        showReservationNotification();

        // --- Crear notificación y log para el admin ---
        // Suponiendo que el admin tiene un ID fijo, por ejemplo "adminId". Si tienes varios admins, ajusta esto.
        String adminId = "adminId";
        String clienteNombre = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Cliente";
        String tourNombre = reserva != null && reserva.getTour() != null ? reserva.getTour().getTitle() : "Tour";
        String notiTitulo = "Nueva reserva";
        String notiDesc = "El cliente " + clienteNombre + " reservó el tour '" + tourNombre + "'.";
        NotificacionLogUtils.crearNotificacion(notiTitulo, notiDesc, adminId);
        NotificacionLogUtils.crearLog(notiTitulo, notiDesc);
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
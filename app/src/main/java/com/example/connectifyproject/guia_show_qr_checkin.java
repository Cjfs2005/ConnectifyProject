package com.example.connectifyproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 📱 PANTALLA DE CHECK-IN CON CÓDIGO QR
 * 
 * El guía muestra este código QR a los participantes para que confirmen su asistencia.
 * Actualiza en tiempo real cuántos participantes han hecho check-in.
 */
public class guia_show_qr_checkin extends AppCompatActivity {
    
    private static final String TAG = "GuiaQRCheckIn";
    
    private String tourId;
    private String tourTitulo;
    private int numeroParticipantes;
    
    private ImageView ivQRCode;
    private TextView tvTitulo;
    private TextView tvInstrucciones;
    private TextView tvContador;
    private ProgressBar progressBar;
    private Button btnEmpezarTour;
    private Button btnCancelar;
    
    private FirebaseFirestore db;
    private ListenerRegistration snapshotListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guia_show_qr_checkin_view);
        
        // Obtener datos del tour
        tourId = getIntent().getStringExtra("tourId");
        tourTitulo = getIntent().getStringExtra("tourTitulo");
        numeroParticipantes = getIntent().getIntExtra("numeroParticipantes", 0);
        
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tour no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        
        // Inicializar vistas
        initViews();
        
        // Configurar UI
        setupUI();
        
        // Generar código QR
        generarCodigoQRCheckIn();
        
        // Escuchar actualizaciones en tiempo real
        escucharActualizacionesCheckIn();
    }
    
    private void initViews() {
        ivQRCode = findViewById(R.id.iv_qr_code);
        tvTitulo = findViewById(R.id.tv_titulo);
        tvInstrucciones = findViewById(R.id.tv_instrucciones);
        tvContador = findViewById(R.id.tv_contador);
        progressBar = findViewById(R.id.progress_bar);
        btnEmpezarTour = findViewById(R.id.btn_empezar_tour);
        btnCancelar = findViewById(R.id.btn_cancelar);
    }
    
    private void setupUI() {
        tvTitulo.setText(tourTitulo != null ? tourTitulo : "Check-In de Participantes");
        tvInstrucciones.setText("Muestra este código QR a cada participante para que confirme su asistencia");
        tvContador.setText("0 de " + numeroParticipantes + " participantes registrados");
        
        if (numeroParticipantes > 0) {
            progressBar.setMax(numeroParticipantes);
            progressBar.setProgress(0);
        }
        
        btnEmpezarTour.setEnabled(false);
        btnEmpezarTour.setAlpha(0.5f);
        
        btnEmpezarTour.setOnClickListener(v -> empezarTour());
        btnCancelar.setOnClickListener(v -> cancelarCheckIn());
    }
    
    /**
     * Generar código QR para check-in
     */
    private void generarCodigoQRCheckIn() {
        try {
            // Crear JSON con datos del QR
            JSONObject qrData = new JSONObject();
            qrData.put("tourId", tourId);
            qrData.put("type", "check_in");
            qrData.put("timestamp", System.currentTimeMillis());
            
            String qrContent = qrData.toString();
            
            // Generar QR usando ZXing
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512);
            
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            // Mostrar QR en ImageView
            ivQRCode.setImageBitmap(bitmap);
            Log.d(TAG, "Código QR generado exitosamente");
            
        } catch (WriterException | JSONException e) {
            Log.e(TAG, "Error al generar código QR", e);
            Toast.makeText(this, "Error al generar código QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Escuchar actualizaciones de check-in en tiempo real
     */
    private void escucharActualizacionesCheckIn() {
        snapshotListener = db.collection("tours_asignados")
            .document(tourId)
            .addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error al escuchar actualizaciones", error);
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Obtener lista de participantes
                    List<Map<String, Object>> participantes = 
                        (List<Map<String, Object>>) documentSnapshot.get("participantes");
                    
                    if (participantes != null) {
                        // Contar cuántos han hecho check-in
                        int checkInsRealizados = 0;
                        for (Map<String, Object> participante : participantes) {
                            Boolean checkIn = (Boolean) participante.get("checkIn");
                            if (checkIn != null && checkIn) {
                                checkInsRealizados++;
                            }
                        }
                        
                        // Actualizar UI
                        actualizarContador(checkInsRealizados);
                        
                        Log.d(TAG, "Check-ins realizados: " + checkInsRealizados + " de " + numeroParticipantes);
                    }
                }
            });
    }
    
    /**
     * Actualizar contador y progress bar
     */
    private void actualizarContador(int checkInsRealizados) {
        tvContador.setText(checkInsRealizados + " de " + numeroParticipantes + " participantes registrados");
        progressBar.setProgress(checkInsRealizados);
        
        // Cambiar color según progreso
        if (checkInsRealizados == 0) {
            tvContador.setTextColor(getColor(R.color.text_secondary));
        } else if (checkInsRealizados < numeroParticipantes) {
            tvContador.setTextColor(getColor(R.color.avatar_amber));
        } else {
            tvContador.setTextColor(getColor(R.color.success_500));
        }
        
        // Habilitar botón si todos hicieron check-in (o si no hay participantes inscritos aún)
        if ((numeroParticipantes > 0 && checkInsRealizados == numeroParticipantes) || 
            (numeroParticipantes == 0 && checkInsRealizados > 0)) {
            btnEmpezarTour.setEnabled(true);
            btnEmpezarTour.setAlpha(1.0f);
            btnEmpezarTour.setText("Empezar Tour (" + checkInsRealizados + "/" + 
                (numeroParticipantes > 0 ? numeroParticipantes : checkInsRealizados) + ")");
        } else {
            btnEmpezarTour.setEnabled(false);
            btnEmpezarTour.setAlpha(0.5f);
            btnEmpezarTour.setText("Esperando check-in...");
        }
    }
    
    /**
     * Empezar tour (cambiar estado a "en_curso")
     */
    private void empezarTour() {
        // Cambiar estado del tour a "en_curso"
        db.collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(doc -> {
                List<Map<String, Object>> participantes = (List<Map<String, Object>>) doc.get("participantes");
                db.collection("tours_asignados")
                    .document(tourId)
                    .update(
                        "estado", "en_curso",
                        "fechaActualizacion", Timestamp.now()
                    )
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Tour iniciado exitosamente");
                        Toast.makeText(this, "¡Tour iniciado! Compartiendo ubicación...", Toast.LENGTH_SHORT).show();
                        // --- Notificación y log para todos los clientes y admin ---
                        String adminId = "adminId";
                        String guiaNombre = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Guía";
                        String notiTitulo = "Tour iniciado";
                        String notiDesc = "El guía " + guiaNombre + " ha iniciado el tour '" + tourTitulo + "'.";
                        if (participantes != null) {
                            for (Map<String, Object> participante : participantes) {
                                String clienteId = (String) participante.get("clienteId");
                                if (clienteId != null) {
                                    com.example.connectifyproject.utils.NotificacionLogUtils.crearNotificacion(notiTitulo, notiDesc, clienteId);
                                }
                            }
                        }
                        com.example.connectifyproject.utils.NotificacionLogUtils.crearNotificacion(notiTitulo, notiDesc, adminId);
                        com.example.connectifyproject.utils.NotificacionLogUtils.crearLog(notiTitulo, notiDesc);
                        // Iniciar servicio de tracking de ubicación
                        iniciarServicioTracking();
                        // Navegar a pantalla de progreso del tour
                        Intent intent = new Intent(guia_show_qr_checkin.this, guia_tour_progress.class);
                        intent.putExtra("tourId", tourId);
                        intent.putExtra("tourTitulo", tourTitulo);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al empezar tour", e);
                        Toast.makeText(this, "Error al empezar tour: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            });
    }
    
    /**
     * Iniciar servicio de tracking de ubicación
     */
    private void iniciarServicioTracking() {
        Intent serviceIntent = new Intent(this, com.example.connectifyproject.services.LocationTrackingService.class);
        serviceIntent.putExtra("tourId", tourId);
        serviceIntent.putExtra("action", "START");
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        Log.d(TAG, "Servicio de tracking iniciado");
    }
    
    /**
     * Cancelar proceso de check-in
     */
    private void cancelarCheckIn() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cancelar Check-In")
            .setMessage("¿Estás seguro de que deseas cancelar el proceso de check-in? El tour volverá al estado pendiente.")
            .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                // Volver estado a "pendiente"
                db.collection("tours_asignados")
                    .document(tourId)
                    .update("estado", "pendiente")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Check-in cancelado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cancelar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (snapshotListener != null) {
            snapshotListener.remove();
        }
    }
}

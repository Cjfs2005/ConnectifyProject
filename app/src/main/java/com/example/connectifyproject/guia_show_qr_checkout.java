package com.example.connectifyproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
 * 📱 PANTALLA DE CHECK-OUT CON CÓDIGO QR
 * 
 * El guía muestra este código QR a los participantes para que confirmen
 * su salida al finalizar el tour.
 */
public class guia_show_qr_checkout extends AppCompatActivity {
    
    private static final String TAG = "GuiaQRCheckOut";
    
    private String tourId;
    private String tourTitulo;
    private int numeroParticipantes;
    
    private ImageView ivQRCode;
    private TextView tvTitulo;
    private TextView tvInstrucciones;
    private TextView tvContador;
    private ProgressBar progressBar;
    private Button btnCompletarTour;
    
    private FirebaseFirestore db;
    private ListenerRegistration snapshotListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guia_show_qr_checkout_view);
        
        // Obtener datos del tour
        tourId = getIntent().getStringExtra("tourId");
        tourTitulo = getIntent().getStringExtra("tourTitulo");
        
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tour no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        
        // Obtener número de participantes
        obtenerNumeroParticipantes();
        
        // Inicializar vistas
        initViews();
        
        // Configurar UI
        setupUI();
        
        // Generar código QR
        generarCodigoQRCheckOut();
        
        // Escuchar actualizaciones en tiempo real
        escucharActualizacionesCheckOut();
    }
    
    private void initViews() {
        ivQRCode = findViewById(R.id.iv_qr_code);
        tvTitulo = findViewById(R.id.tv_titulo);
        tvInstrucciones = findViewById(R.id.tv_instrucciones);
        tvContador = findViewById(R.id.tv_contador);
        progressBar = findViewById(R.id.progress_bar);
        btnCompletarTour = findViewById(R.id.btn_completar_tour);
    }
    
    private void setupUI() {
        tvTitulo.setText(tourTitulo != null ? tourTitulo : "Check-Out de Participantes");
        tvInstrucciones.setText("Muestra este código QR a cada participante para que confirme su salida");
        
        btnCompletarTour.setEnabled(false);
        btnCompletarTour.setAlpha(0.5f);
        
        btnCompletarTour.setOnClickListener(v -> completarTour());
    }
    
    /**
     * Obtener número de participantes
     */
    private void obtenerNumeroParticipantes() {
        db.collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> participantes = 
                        (List<Map<String, Object>>) documentSnapshot.get("participantes");
                    
                    if (participantes != null) {
                        numeroParticipantes = participantes.size();
                        
                        tvContador.setText("0 de " + numeroParticipantes + " participantes confirmados");
                        progressBar.setMax(numeroParticipantes);
                        progressBar.setProgress(0);
                    }
                }
            });
    }
    
    /**
     * Generar código QR para check-out
     */
    private void generarCodigoQRCheckOut() {
        try {
            // Crear JSON con datos del QR
            JSONObject qrData = new JSONObject();
            qrData.put("tourId", tourId);
            qrData.put("type", "check_out");
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
            Log.d(TAG, "Código QR de check-out generado exitosamente");
            
        } catch (WriterException | JSONException e) {
            Log.e(TAG, "Error al generar código QR", e);
            Toast.makeText(this, "Error al generar código QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Escuchar actualizaciones de check-out en tiempo real
     */
    private void escucharActualizacionesCheckOut() {
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
                        // Contar cuántos han hecho check-out
                        int checkOutsRealizados = 0;
                        for (Map<String, Object> participante : participantes) {
                            Boolean checkOut = (Boolean) participante.get("checkOut");
                            if (checkOut != null && checkOut) {
                                checkOutsRealizados++;
                            }
                        }
                        
                        // Actualizar UI
                        actualizarContador(checkOutsRealizados);
                        
                        Log.d(TAG, "Check-outs realizados: " + checkOutsRealizados + " de " + numeroParticipantes);
                    }
                }
            });
    }
    
    /**
     * Actualizar contador y progress bar
     */
    private void actualizarContador(int checkOutsRealizados) {
        tvContador.setText(checkOutsRealizados + " de " + numeroParticipantes + " participantes confirmados");
        progressBar.setProgress(checkOutsRealizados);
        
        // Cambiar color según progreso
        if (checkOutsRealizados == 0) {
            tvContador.setTextColor(getColor(R.color.text_secondary));
        } else if (checkOutsRealizados < numeroParticipantes) {
            tvContador.setTextColor(getColor(R.color.avatar_amber));
        } else {
            tvContador.setTextColor(getColor(R.color.success_500));
        }
        
        // Habilitar botón si todos hicieron check-out
        if (checkOutsRealizados == numeroParticipantes && numeroParticipantes > 0) {
            btnCompletarTour.setEnabled(true);
            btnCompletarTour.setAlpha(1.0f);
            btnCompletarTour.setText("Completar Tour (" + checkOutsRealizados + "/" + numeroParticipantes + ")");
        } else {
            btnCompletarTour.setEnabled(false);
            btnCompletarTour.setAlpha(0.5f);
            btnCompletarTour.setText("Esperando check-out...");
        }
    }
    
    /**
     * Completar tour (cambiar estado a "completado")
     */
    private void completarTour() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Completar Tour")
            .setMessage("¿Confirmas que el tour ha finalizado? Todos los participantes ya hicieron check-out.")
            .setPositiveButton("Sí, completar", (dialog, which) -> {
                // Detener servicio de tracking
                detenerServicioTracking();
                // Obtener participantes antes de actualizar
                db.collection("tours_asignados")
                    .document(tourId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        List<Map<String, Object>> participantes = (List<Map<String, Object>>) doc.get("participantes");
                        // Cambiar estado del tour a "completado"
                        db.collection("tours_asignados")
                            .document(tourId)
                            .update(
                                "estado", "completado",
                                "checkOutRealizado", true,
                                "horaCheckOut", Timestamp.now(),
                                "fechaActualizacion", Timestamp.now()
                            )
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Tour completado exitosamente");
                                Toast.makeText(this, "¡Tour completado exitosamente!", Toast.LENGTH_LONG).show();
                                // --- Notificación y log para todos los clientes y admin ---
                                String adminId = "adminId";
                                String guiaNombre = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Guía";
                                String notiTitulo = "Tour finalizado";
                                String notiDesc = "El guía " + guiaNombre + " ha finalizado el tour '" + tourTitulo + "'.";
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
                                // Volver a la pantalla principal de tours asignados
                                Intent intent = new Intent(guia_show_qr_checkout.this, guia_assigned_tours.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al completar tour", e);
                                Toast.makeText(this, "Error al completar tour: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    /**
     * Detener servicio de tracking de ubicación
     */
    private void detenerServicioTracking() {
        Intent serviceIntent = new Intent(this, com.example.connectifyproject.services.LocationTrackingService.class);
        serviceIntent.putExtra("action", "STOP");
        startService(serviceIntent);
        
        Log.d(TAG, "Servicio de tracking detenido");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (snapshotListener != null) {
            snapshotListener.remove();
        }
    }
}

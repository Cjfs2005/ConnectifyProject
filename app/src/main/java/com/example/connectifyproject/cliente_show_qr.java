package com.example.connectifyproject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 📱 PANTALLA PARA QUE EL CLIENTE GENERE Y MUESTRE SU QR
 * 
 * El cliente muestra este QR al guía para confirmar:
 * - Check-in: Al inicio del tour
 * - Check-out: Al finalizar el tour
 */
public class cliente_show_qr extends AppCompatActivity {
    
    private static final String TAG = "ClienteShowQR";
    
    private String tourId;
    private String reservaId;
    private String tipoQR; // "check_in" o "check_out"
    private String tourTitulo;
    
    private ImageView ivQRCode;
    private TextView tvTitulo;
    private TextView tvInstrucciones;
    private Button btnCerrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_show_qr_view);
        
        // Obtener datos del intent
        tourId = getIntent().getStringExtra("tourId");
        reservaId = getIntent().getStringExtra("reservaId");
        tipoQR = getIntent().getStringExtra("tipoQR"); // "check_in" o "check_out"
        tourTitulo = getIntent().getStringExtra("tourTitulo");
        
        if (tourId == null || tipoQR == null) {
            Toast.makeText(this, "Error: Datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Inicializar vistas
        initViews();
        
        // Configurar UI
        setupUI();
        
        // Generar código QR
        generarCodigoQR();
    }
    
    private void initViews() {
        ivQRCode = findViewById(R.id.iv_qr_code);
        tvTitulo = findViewById(R.id.tv_titulo);
        tvInstrucciones = findViewById(R.id.tv_instrucciones);
        btnCerrar = findViewById(R.id.btn_cerrar);
        
        btnCerrar.setOnClickListener(v -> finish());
    }
    
    private void setupUI() {
        if ("check_in".equals(tipoQR)) {
            tvTitulo.setText("QR Check-in: " + (tourTitulo != null ? tourTitulo : "Tour"));
            tvInstrucciones.setText("Muestra este código QR al guía para confirmar tu llegada al punto de encuentro");
        } else if ("check_out".equals(tipoQR)) {
            tvTitulo.setText("QR Check-out: " + (tourTitulo != null ? tourTitulo : "Tour"));
            tvInstrucciones.setText("Muestra este código QR al guía para confirmar que has completado el tour");
        }
    }
    
    /**
     * Generar código QR con datos del cliente
     */
    private void generarCodigoQR() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Crear JSON con datos del QR
            JSONObject qrData = new JSONObject();
            qrData.put("tourId", tourId);
            qrData.put("clienteId", currentUser.getUid());
            qrData.put("clienteEmail", currentUser.getEmail());
            qrData.put("type", tipoQR);
            qrData.put("timestamp", System.currentTimeMillis());
            
            if (reservaId != null) {
                qrData.put("reservaId", reservaId);
            }
            
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
            Log.d(TAG, "Código QR generado exitosamente: " + tipoQR);
            
        } catch (WriterException | JSONException e) {
            Log.e(TAG, "Error al generar código QR", e);
            Toast.makeText(this, "Error al generar código QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

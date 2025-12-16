package com.example.connectifyproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 📷 GUÍA ESCANEA QR DE CADA CLIENTE
 * 
 * El guía usa la cámara para escanear los códigos QR individuales que cada cliente
 * muestra en su dispositivo (generados por cliente_show_qr.java).
 * 
 * Por cada QR escaneado:
 * - Valida que sea del tour correcto
 * - Marca participante.checkIn = true en Firebase
 * - Actualiza contador en tiempo real
 * - Permite iniciar tour cuando todos hayan hecho check-in
 */
public class guia_scan_qr_participants extends AppCompatActivity {
    
    private static final String TAG = "GuiaScanQRParticipants";
    private static final int CAMERA_PERMISSION_CODE = 1001;
    
    private String tourId;
    private String tourTitulo;
    private int numeroParticipantes;
    private String scanMode; // "check_in" o "check_out"
    
    private PreviewView previewView;
    private TextView tvInstrucciones;
    private TextView tvContador;
    private ProgressBar progressBar;
    private Button btnIniciarTour;
    private Button btnCancelar;
    
    private FirebaseFirestore db;
    private ListenerRegistration snapshotListener;
    private ProcessCameraProvider cameraProvider;
    private BarcodeScanner scanner;
    
    private List<String> clientesEscaneados = new ArrayList<>();
    private boolean isScanning = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guia_scan_qr_participants_view);
        
        // Obtener datos del tour
        tourId = getIntent().getStringExtra("tourId");
        tourTitulo = getIntent().getStringExtra("tourTitulo");
        numeroParticipantes = getIntent().getIntExtra("numeroParticipantes", 0);
        scanMode = getIntent().getStringExtra("scanMode"); // "check_in" o "check_out"
        
        if (scanMode == null) scanMode = "check_in";
        
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
        
        // Escuchar actualizaciones en tiempo real
        escucharActualizaciones();
        
        // Solicitar permisos de cámara
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        tvInstrucciones = findViewById(R.id.tv_instrucciones);
        tvContador = findViewById(R.id.tv_contador);
        progressBar = findViewById(R.id.progress_bar);
        btnIniciarTour = findViewById(R.id.btn_iniciar_tour);
        btnCancelar = findViewById(R.id.btn_cancelar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(scanMode.equals("check_in") ? "Check-In de Participantes" : "Check-Out de Participantes");
        }
    }
    
    private void setupUI() {
        String tituloModo = scanMode.equals("check_in") ? "Check-In" : "Check-Out";
        tvInstrucciones.setText("Escanea el código QR de " + tituloModo + " de cada participante");
        tvContador.setText("0 de " + numeroParticipantes + " participantes escaneados");
        
        if (numeroParticipantes > 0) {
            progressBar.setMax(numeroParticipantes);
            progressBar.setProgress(0);
        }
        
        btnIniciarTour.setEnabled(false);
        btnIniciarTour.setAlpha(0.5f);
        
        if (scanMode.equals("check_in")) {
            btnIniciarTour.setText("Iniciar Tour");
        } else {
            btnIniciarTour.setText("Finalizar Tour");
        }
        
        btnIniciarTour.setOnClickListener(v -> {
            if (scanMode.equals("check_in")) {
                // Redirigir al mapa para iniciar el tour
                redirigirAMapa();
            } else {
                finalizarTour();
            }
        });
        
        btnCancelar.setOnClickListener(v -> finish());
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.CAMERA}, 
            CAMERA_PERMISSION_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private void startCamera() {
        com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error al iniciar cámara", e);
                Toast.makeText(this, "Error al iniciar cámara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        
        CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build();
        
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build();
        
        // Configurar escáner de QR usando ML Kit
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build();
        scanner = BarcodeScanning.getClient(options);
        
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            processImageProxy(imageProxy);
        });
        
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }
    
    @androidx.camera.core.ExperimentalGetImage
    private void processImageProxy(ImageProxy imageProxy) {
        if (!isScanning) {
            imageProxy.close();
            return;
        }
        
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(
                mediaImage, 
                imageProxy.getImageInfo().getRotationDegrees()
            );
            
            scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String qrData = barcode.getRawValue();
                        if (qrData != null) {
                            processQRCode(qrData);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al escanear QR", e);
                })
                .addOnCompleteListener(task -> {
                    imageProxy.close();
                });
        } else {
            imageProxy.close();
        }
    }
    
    private void processQRCode(String qrData) {
        try {
            JSONObject qrJson = new JSONObject(qrData);
            
            String qrTourId = qrJson.getString("tourId");
            String qrClienteId = qrJson.getString("clienteId");
            String qrType = qrJson.getString("type");
            
            // Validar que sea del tour correcto
            if (!qrTourId.equals(tourId)) {
                runOnUiThread(() -> Toast.makeText(this, 
                    "⚠️ Este QR pertenece a otro tour", 
                    Toast.LENGTH_SHORT).show());
                return;
            }
            
            // Validar que sea del tipo correcto
            if (!qrType.equals(scanMode)) {
                runOnUiThread(() -> Toast.makeText(this, 
                    "⚠️ QR incorrecto. Esperando QR de " + scanMode, 
                    Toast.LENGTH_SHORT).show());
                return;
            }
            
            // Evitar escaneos duplicados
            if (clientesEscaneados.contains(qrClienteId)) {
                runOnUiThread(() -> Toast.makeText(this, 
                    "✅ Cliente ya escaneado", 
                    Toast.LENGTH_SHORT).show());
                return;
            }
            
            // Pausar escaneo mientras se procesa
            isScanning = false;
            
            // Registrar check-in/check-out en Firebase
            registrarEscaneo(qrClienteId);
            
        } catch (Exception e) {
            Log.e(TAG, "Error al procesar QR", e);
            runOnUiThread(() -> Toast.makeText(this, 
                "❌ QR inválido", 
                Toast.LENGTH_SHORT).show());
        }
    }
    
    private void registrarEscaneo(String clienteId) {
        DocumentReference tourRef = db.collection("tours_asignados").document(tourId);
        
        tourRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> participantes = 
                    (List<Map<String, Object>>) documentSnapshot.get("participantes");
                
                if (participantes != null) {
                    boolean encontrado = false;
                    
                    for (Map<String, Object> participante : participantes) {
                        String participanteId = (String) participante.get("clienteId");
                        
                        if (participanteId != null && participanteId.equals(clienteId)) {
                            // Actualizar check-in o check-out
                            String campo = scanMode.equals("check_in") ? "checkIn" : "checkOut";
                            participante.put(campo, true);
                            participante.put(campo + "Timestamp", System.currentTimeMillis());
                            
                            encontrado = true;
                            break;
                        }
                    }
                    
                    if (encontrado) {
                        // Actualizar Firebase
                        tourRef.update("participantes", participantes)
                            .addOnSuccessListener(aVoid -> {
                                clientesEscaneados.add(clienteId);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, 
                                        "✅ Cliente registrado exitosamente", 
                                        Toast.LENGTH_SHORT).show();
                                    
                                    // Reanudar escaneo después de 1 segundo
                                    previewView.postDelayed(() -> isScanning = true, 1000);
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al actualizar Firebase", e);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, 
                                        "❌ Error al registrar: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                                    isScanning = true;
                                });
                            });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, 
                                "⚠️ Cliente no inscrito en este tour", 
                                Toast.LENGTH_SHORT).show();
                            isScanning = true;
                        });
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error al consultar tour", e);
            runOnUiThread(() -> {
                Toast.makeText(this, 
                    "❌ Error de conexión", 
                    Toast.LENGTH_SHORT).show();
                isScanning = true;
            });
        });
    }
    
    private void escucharActualizaciones() {
        snapshotListener = db.collection("tours_asignados")
            .document(tourId)
            .addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error al escuchar actualizaciones", error);
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    List<Map<String, Object>> participantes = 
                        (List<Map<String, Object>>) documentSnapshot.get("participantes");
                    
                    if (participantes != null) {
                        int escaneadosCount = 0;
                        String campo = scanMode.equals("check_in") ? "checkIn" : "checkOut";
                        
                        for (Map<String, Object> participante : participantes) {
                            Boolean escaneado = (Boolean) participante.get(campo);
                            if (escaneado != null && escaneado) {
                                escaneadosCount++;
                            }
                        }
                        
                        actualizarContador(escaneadosCount);
                    }
                }
            });
    }
    
    private void actualizarContador(int escaneadosCount) {
        tvContador.setText(escaneadosCount + " de " + numeroParticipantes + " participantes escaneados");
        progressBar.setProgress(escaneadosCount);
        
        // Calcular porcentaje de asistencia
        double porcentajeAsistencia = numeroParticipantes > 0 ? 
            (double) escaneadosCount / numeroParticipantes : 0.0;
        int porcentajeInt = (int) (porcentajeAsistencia * 100);
        
        // ✅ CALCULAR MÍNIMO REQUERIDO (50% REDONDEADO ARRIBA)
        int minimoRequerido = (int) Math.ceil(numeroParticipantes * 0.5);
        
        // Cambiar color según progreso
        if (escaneadosCount == 0) {
            tvContador.setTextColor(getColor(R.color.text_secondary));
        } else if (escaneadosCount < minimoRequerido) {
            tvContador.setTextColor(getColor(R.color.avatar_red)); // Menos del 50%
        } else if (escaneadosCount < numeroParticipantes) {
            tvContador.setTextColor(getColor(R.color.avatar_amber)); // Entre 50% y 100%
        } else {
            tvContador.setTextColor(getColor(R.color.brand_green)); // 100%
        }
        
        // ✅ HABILITAR BOTÓN SI AL MENOS 50% ESCANEARON (REDONDEADO ARRIBA)
        if (numeroParticipantes > 0 && escaneadosCount >= minimoRequerido) {
            btnIniciarTour.setEnabled(true);
            btnIniciarTour.setAlpha(1.0f);
            if ("check_in".equals(scanMode)) {
                btnIniciarTour.setText("Iniciar Tour (" + porcentajeInt + "% asistencia)");
            } else {
                btnIniciarTour.setText("Finalizar Tour (" + porcentajeInt + "% check-out)");
            }
        } else {
            btnIniciarTour.setEnabled(false);
            btnIniciarTour.setAlpha(0.5f);
            if ("check_in".equals(scanMode)) {
                btnIniciarTour.setText("Requiere mínimo " + minimoRequerido + " participantes (" + escaneadosCount + "/" + numeroParticipantes + ")");
            } else {
                btnIniciarTour.setText("Requiere mínimo " + minimoRequerido + " con check-out (" + escaneadosCount + "/" + numeroParticipantes + ")");
            }
        }
    }
    
    /**
     * Iniciar tour - Valida y cambia estado a en_curso, luego va al mapa
     */
    private void redirigirAMapa() {
        // ✅ VALIDACIONES ANTES DE INICIAR TOUR
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(this, "Error: Tour no encontrado", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 1️⃣ VALIDAR HORA DE INICIO (debe haber pasado la hora)
                Object fechaRealizacion = doc.get("fechaRealizacion");
                String horaInicio = doc.getString("horaInicio");
                
                double horasRestantes = com.example.connectifyproject.utils.TourTimeValidator
                    .calcularHorasHastaInicio(fechaRealizacion, horaInicio);
                
                if (horasRestantes > 0) {
                    long minutosRestantes = (long) (horasRestantes * 60);
                    Toast.makeText(this, 
                        "⏰ El tour aún no puede iniciarse.\n" +
                        "Faltan " + minutosRestantes + " minutos para la hora de inicio.",
                        Toast.LENGTH_LONG).show();
                    return;
                }
                
                // 2️⃣ VALIDAR 50% DE PARTICIPANTES ESCANEADOS (check-in)
                java.util.List<java.util.Map<String, Object>> participantes = 
                    (java.util.List<java.util.Map<String, Object>>) doc.get("participantes");
                
                int totalParticipantes = participantes != null ? participantes.size() : 0;
                int participantesEscaneados = 0;
                
                if (participantes != null) {
                    for (java.util.Map<String, Object> participante : participantes) {
                        Boolean checkIn = (Boolean) participante.get("checkIn");
                        if (checkIn != null && checkIn) {
                            participantesEscaneados++;
                        }
                    }
                }
                
                // Redondeo hacia arriba: ceil(total * 0.5)
                int minimoRequerido = (int) Math.ceil(totalParticipantes * 0.5);
                
                if (participantesEscaneados < minimoRequerido) {
                    int porcentaje = totalParticipantes > 0 ? 
                        (int) ((participantesEscaneados * 100.0) / totalParticipantes) : 0;
                    
                    Toast.makeText(this, 
                        "👥 Se requiere al menos 50% de participantes con check-in para iniciar.\n\n" +
                        "Check-in realizados: " + participantesEscaneados + " / " + totalParticipantes + 
                        " (" + porcentaje + "%)\n" +
                        "Mínimo requerido: " + minimoRequerido,
                        Toast.LENGTH_LONG).show();
                    return;
                }
                
                // ✅ VALIDACIONES PASADAS - INICIAR TOUR (cambiar estado a en_curso)
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("tours_asignados")
                    .document(tourId)
                    .update(
                        "estado", "en_curso", 
                        "tourStarted", true,
                        "horaInicioReal", com.google.firebase.Timestamp.now(),
                        "puntoActualIndex", 0
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "🚀 Tour iniciado. Redirigiendo al mapa...", Toast.LENGTH_SHORT).show();
                        
                        // Redirigir al mapa
                        Intent intent = new Intent(this, guia_tour_map.class);
                        intent.putExtra("tour_id", tourId);
                        intent.putExtra("tour_name", tourTitulo);
                        intent.putExtra("tour_clients", numeroParticipantes);
                        intent.putExtra("tour_status", "en_curso");
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "❌ Error al iniciar tour: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al validar datos: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void finalizarTour() {
        // ✅ VALIDAR 50% DE CHECK-OUT ANTES DE FINALIZAR
        db.collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(this, "Error: Tour no encontrado", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // VALIDAR 50% DE CHECK-OUT (REDONDEADO ARRIBA)
                Long numParticipantes = doc.getLong("numeroParticipantesTotal");
                Long numCheckOut = doc.getLong("numeroParticipantesCheckOut");
                
                int totalParticipantes = numParticipantes != null ? numParticipantes.intValue() : 0;
                int participantesCheckOut = numCheckOut != null ? numCheckOut.intValue() : 0;
                
                // Redondeo hacia arriba: ceil(total * 0.5)
                int minimoRequerido = (int) Math.ceil(totalParticipantes * 0.5);
                
                if (participantesCheckOut < minimoRequerido) {
                    int porcentaje = totalParticipantes > 0 ? 
                        (int) ((participantesCheckOut * 100.0) / totalParticipantes) : 0;
                    
                    Toast.makeText(this, 
                        "👥 Se requiere al menos 50% de participantes con check-out para finalizar.\n\n" +
                        "Check-out realizados: " + participantesCheckOut + " / " + totalParticipantes + 
                        " (" + porcentaje + "%)\n" +
                        "Mínimo requerido: " + minimoRequerido,
                        Toast.LENGTH_LONG).show();
                    return;
                }
                
                // ✅ VALIDACIÓN PASADA - FINALIZAR TOUR
                com.google.firebase.Timestamp horaFinReal = com.google.firebase.Timestamp.now();
                
                db.collection("tours_asignados")
                    .document(tourId)
                    .update(
                        "estado", "completado",
                        "horaFinReal", horaFinReal
                    )
                    .addOnSuccessListener(aVoid -> {
                        // ✅ FASE 2: Crear documento en tours_completados y generar pagos
                        crearTourCompletadoYPagos(doc, horaFinReal);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "❌ Error al completar tour: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al validar datos: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * ✅ FASE 2: Crear documento en tours_completados y generar pagos automáticos
     */
    private void crearTourCompletadoYPagos(com.google.firebase.firestore.DocumentSnapshot tourDoc, com.google.firebase.Timestamp horaFinReal) {
        try {
            // 1️⃣ PREPARAR DATOS DEL TOUR COMPLETADO
            String titulo = tourDoc.getString("titulo");
            String guiaId = tourDoc.getString("guiaId");
            String guiaNombre = tourDoc.getString("guiaNombre");
            String empresaId = tourDoc.getString("empresaId");
            String empresaNombre = tourDoc.getString("empresaNombre");
            com.google.firebase.Timestamp fechaRealizacion = tourDoc.getTimestamp("fechaRealizacion");
            com.google.firebase.Timestamp horaInicio = tourDoc.getTimestamp("horaInicio");
            com.google.firebase.Timestamp horaInicioReal = tourDoc.getTimestamp("horaInicioReal");
            
            Long precioTourLong = tourDoc.getLong("precioTour");
            Long pagoGuiaLong = tourDoc.getLong("pagoGuia");
            Long numParticipantesLong = tourDoc.getLong("numeroParticipantesTotal");
            
            double precioTour = precioTourLong != null ? precioTourLong.doubleValue() : 0;
            double pagoGuia = pagoGuiaLong != null ? pagoGuiaLong.doubleValue() : 0;
            int numParticipantes = numParticipantesLong != null ? numParticipantesLong.intValue() : 0;
            
            String metodoPago = tourDoc.getString("metodoPago");
            List<Map<String, Object>> itinerario = (List<Map<String, Object>>) tourDoc.get("itinerario");
            
            // Calcular duración real
            String duracionReal = "N/A";
            if (horaInicioReal != null && horaFinReal != null) {
                long diffMillis = horaFinReal.toDate().getTime() - horaInicioReal.toDate().getTime();
                long hours = diffMillis / (1000 * 60 * 60);
                long minutes = (diffMillis % (1000 * 60 * 60)) / (1000 * 60);
                duracionReal = hours + "h " + minutes + "min";
            }
            
            // Contar puntos visitados
            int puntosVisitados = 0;
            if (itinerario != null) {
                for (Map<String, Object> punto : itinerario) {
                    Boolean completado = (Boolean) punto.get("completado");
                    if (completado != null && completado) {
                        puntosVisitados++;
                    }
                }
            }
            
            double pagoEmpresaTotal = precioTour * numParticipantes;
            
            // 2️⃣ CREAR DOCUMENTO EN tours_completados
            Map<String, Object> tourCompletado = new HashMap<>();
            tourCompletado.put("tourAsignadoId", tourId);
            tourCompletado.put("titulo", titulo);
            tourCompletado.put("guiaId", guiaId);
            tourCompletado.put("guiaNombre", guiaNombre);
            tourCompletado.put("empresaId", empresaId);
            tourCompletado.put("empresaNombre", empresaNombre);
            tourCompletado.put("fechaRealizacion", fechaRealizacion);
            tourCompletado.put("horaInicio", horaInicio);
            tourCompletado.put("horaInicioReal", horaInicioReal);
            tourCompletado.put("horaFinReal", horaFinReal);
            tourCompletado.put("duracionReal", duracionReal);
            tourCompletado.put("numeroParticipantes", numParticipantes);
            tourCompletado.put("precioTour", precioTour);
            tourCompletado.put("pagoGuia", pagoGuia);
            tourCompletado.put("pagoEmpresaTotal", pagoEmpresaTotal);
            tourCompletado.put("metodoPago", metodoPago);
            tourCompletado.put("estado", "completado");
            tourCompletado.put("puntosVisitados", puntosVisitados);
            tourCompletado.put("fechaRegistro", com.google.firebase.Timestamp.now());
            
            db.collection("tours_completados")
                .add(tourCompletado)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "✅ Tour completado registrado: " + docRef.getId());
                    
                    // 3️⃣ GENERAR PAGOS AUTOMÁTICOS
                    generarPagosAutomaticos(tourDoc, precioTour, pagoGuia, numParticipantes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al crear tour completado: " + e.getMessage());
                    Toast.makeText(this, "Error al registrar tour completado", Toast.LENGTH_SHORT).show();
                    finalizarYSalir();
                });
                
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al procesar tour completado: " + e.getMessage());
            Toast.makeText(this, "Error al finalizar tour", Toast.LENGTH_SHORT).show();
            finalizarYSalir();
        }
    }
    
    /**
     * ✅ FASE 2: Generar pagos automáticos (clientes→empresa, empresa→guía)
     */
    private void generarPagosAutomaticos(com.google.firebase.firestore.DocumentSnapshot tourDoc, 
                                          double precioTour, double pagoGuia, int numParticipantes) {
        try {
            String titulo = tourDoc.getString("titulo");
            String guiaId = tourDoc.getString("guiaId");
            String empresaId = tourDoc.getString("empresaId");
            
            // 4️⃣ OBTENER LISTA DE PARTICIPANTES
            db.collection("tours_asignados")
                .document(tourId)
                .collection("participantes")
                .get()
                .addOnSuccessListener(participantesSnapshot -> {
                    List<Map<String, Object>> pagosAGenerar = new ArrayList<>();
                    
                    // 5️⃣ CREAR PAGO POR CADA PARTICIPANTE (cliente → empresa)
                    for (com.google.firebase.firestore.QueryDocumentSnapshot participante : participantesSnapshot) {
                        String clienteId = participante.getId();
                        String clienteNombre = participante.getString("nombreCompleto");
                        
                        Map<String, Object> pagoCliente = new HashMap<>();
                        pagoCliente.put("fecha", com.google.firebase.Timestamp.now());
                        pagoCliente.put("monto", precioTour);
                        pagoCliente.put("nombreTour", titulo);
                        pagoCliente.put("tipoPago", "A Empresa");
                        pagoCliente.put("uidUsuarioPaga", clienteId);
                        pagoCliente.put("nombreUsuarioPaga", clienteNombre);
                        pagoCliente.put("uidUsuarioRecibe", empresaId);
                        pagoCliente.put("nombreUsuarioRecibe", "Empresa");
                        pagoCliente.put("tourId", tourId);
                        pagoCliente.put("estado", "completado");
                        
                        pagosAGenerar.add(pagoCliente);
                    }
                    
                    // 6️⃣ CREAR PAGO ÚNICO (empresa → guía)
                    Map<String, Object> pagoGuiaDoc = new HashMap<>();
                    pagoGuiaDoc.put("fecha", com.google.firebase.Timestamp.now());
                    pagoGuiaDoc.put("monto", pagoGuia);
                    pagoGuiaDoc.put("nombreTour", titulo);
                    pagoGuiaDoc.put("tipoPago", "A Guia");
                    pagoGuiaDoc.put("uidUsuarioPaga", empresaId);
                    pagoGuiaDoc.put("nombreUsuarioPaga", "Empresa");
                    pagoGuiaDoc.put("uidUsuarioRecibe", guiaId);
                    pagoGuiaDoc.put("nombreUsuarioRecibe", tourDoc.getString("guiaNombre"));
                    pagoGuiaDoc.put("tourId", tourId);
                    pagoGuiaDoc.put("estado", "completado");
                    
                    pagosAGenerar.add(pagoGuiaDoc);
                    
                    // 7️⃣ GUARDAR TODOS LOS PAGOS EN FIREBASE
                    int[] pagosCompletados = {0};
                    int totalPagos = pagosAGenerar.size();
                    
                    for (Map<String, Object> pago : pagosAGenerar) {
                        db.collection("pagos")
                            .add(pago)
                            .addOnSuccessListener(docRef -> {
                                pagosCompletados[0]++;
                                Log.d(TAG, "✅ Pago generado: " + docRef.getId());
                                
                                if (pagosCompletados[0] == totalPagos) {
                                    Log.d(TAG, "✅ Todos los pagos generados: " + totalPagos);
                                    finalizarYSalir();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Error al generar pago: " + e.getMessage());
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al obtener participantes: " + e.getMessage());
                    finalizarYSalir();
                });
                
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al generar pagos: " + e.getMessage());
            finalizarYSalir();
        }
    }
    
    /**
     * Finalizar actividad y regresar a lista de tours
     */
    private void finalizarYSalir() {
        Toast.makeText(this, "✅ Tour completado exitosamente", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(this, guia_assigned_tours.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (snapshotListener != null) {
            snapshotListener.remove();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (scanner != null) {
            scanner.close();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

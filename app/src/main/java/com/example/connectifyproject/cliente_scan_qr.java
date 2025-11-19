package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actividad para escanear códigos QR de check-in y check-out
 */
public class cliente_scan_qr extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        if (auth.getCurrentUser() != null) {
            usuarioId = auth.getCurrentUser().getUid();
            iniciarEscaneo();
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Inicia el escáner de QR usando ZXing
     */
    private void iniciarEscaneo() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el código QR del guía");
        integrator.setCameraId(0);  // Cámara trasera
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                procesarCodigoQR(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Procesa el contenido del código QR escaneado
     */
    private void procesarCodigoQR(String contenidoQR) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(contenidoQR, JsonObject.class);
            
            String tourId = jsonObject.get("tourId").getAsString();
            String tipo = jsonObject.get("type").getAsString();
            
            if (tipo.equals("check_in")) {
                realizarCheckIn(tourId);
            } else if (tipo.equals("check_out")) {
                realizarCheckOut(tourId);
            } else {
                Toast.makeText(this, "Código QR no válido", Toast.LENGTH_SHORT).show();
                finish();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error al leer código QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Realiza el check-in del usuario en el tour
     */
    private void realizarCheckIn(String tourId) {
        DocumentReference tourRef = db.collection("tours_asignados").document(tourId);
        
        tourRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> participantes = 
                    (List<Map<String, Object>>) documentSnapshot.get("participantes");
                
                if (participantes != null) {
                    boolean encontrado = false;
                    int indice = 0;
                    
                    // Buscar al usuario en la lista de participantes
                    for (Map<String, Object> participante : participantes) {
                        String participanteId = (String) participante.get("usuarioId");
                        if (participanteId != null && participanteId.equals(usuarioId)) {
                            encontrado = true;
                            break;
                        }
                        indice++;
                    }
                    
                    if (encontrado) {
                        // Actualizar el check-in del participante
                        String path = "participantes." + indice + ".checkIn";
                        String pathTimestamp = "participantes." + indice + ".horaCheckIn";
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(path, true);
                        updates.put(pathTimestamp, FieldValue.serverTimestamp());
                        
                        tourRef.update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "¡Check-in realizado exitosamente!", 
                                    Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al realizar check-in: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                                finish();
                            });
                    } else {
                        Toast.makeText(this, "No estás registrado en este tour", 
                            Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "No hay participantes en este tour", 
                        Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Tour no encontrado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al verificar tour: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Realiza el check-out del usuario del tour
     */
    private void realizarCheckOut(String tourId) {
        DocumentReference tourRef = db.collection("tours_asignados").document(tourId);
        
        tourRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> participantes = 
                    (List<Map<String, Object>>) documentSnapshot.get("participantes");
                
                if (participantes != null) {
                    boolean encontrado = false;
                    int indice = 0;
                    
                    // Buscar al usuario en la lista de participantes
                    for (Map<String, Object> participante : participantes) {
                        String participanteId = (String) participante.get("usuarioId");
                        if (participanteId != null && participanteId.equals(usuarioId)) {
                            encontrado = true;
                            break;
                        }
                        indice++;
                    }
                    
                    if (encontrado) {
                        // Actualizar el check-out del participante
                        String path = "participantes." + indice + ".checkOut";
                        String pathTimestamp = "participantes." + indice + ".horaCheckOut";
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(path, true);
                        updates.put(pathTimestamp, FieldValue.serverTimestamp());
                        
                        tourRef.update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "¡Check-out realizado exitosamente! Gracias por participar", 
                                    Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al realizar check-out: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                                finish();
                            });
                    } else {
                        Toast.makeText(this, "No estás registrado en este tour", 
                            Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "No hay participantes en este tour", 
                        Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Tour no encontrado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al verificar tour: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

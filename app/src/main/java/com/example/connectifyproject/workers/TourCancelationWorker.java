package com.example.connectifyproject.workers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.connectifyproject.services.TourFirebaseService;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Worker para ejecutar la cancelación automática de tours sin participantes
 * Se ejecuta cada 30 minutos en segundo plano
 */
public class TourCancelationWorker extends Worker {
    private static final String TAG = "TourCancelationWorker";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final TourFirebaseService tourService = new TourFirebaseService();

    public TourCancelationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "🔄 Iniciando verificación de tours para cancelación automática...");
        
        try {
            // Latch para esperar a que termine la operación async
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] success = {true};
            
            // Buscar todos los tours en estado confirmado, pendiente o programado
            db.collection("tours_asignados")
                .whereIn("estado", java.util.Arrays.asList("confirmado", "pendiente", "programado"))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int toursRevisados = 0;
                    int toursCancelados = 0;
                    
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        toursRevisados++;
                        
                        // Verificar número de participantes
                        List<Map<String, Object>> participantes = 
                            (List<Map<String, Object>>) doc.get("participantes");
                        
                        if (participantes == null || participantes.isEmpty()) {
                            // Verificar regla de 2 horas
                            Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                            String horaInicio = doc.getString("horaInicio");
                            
                            double horasRestantes = com.example.connectifyproject.utils.TourTimeValidator
                                .calcularHorasHastaInicio(fechaRealizacion, horaInicio);
                            
                            // Cancelar si faltan 2 horas o menos (pero no ha iniciado)
                            if (horasRestantes <= 2.0 && horasRestantes >= 0) {
                                Log.w(TAG, "⏰ Tour sin participantes a punto de cancelarse: " + 
                                    doc.getId() + " (faltan " + String.format("%.1f", horasRestantes) + "h)");
                                
                                // Llamar al método de cancelación
                                tourService.verificarYCancelarTourSinParticipantes(
                                    doc.getId(),
                                    new TourFirebaseService.OperationCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            Log.d(TAG, "✅ " + message);
                                        }
                                        
                                        @Override
                                        public void onError(String error) {
                                            Log.e(TAG, "❌ Error: " + error);
                                        }
                                    }
                                );
                                
                                toursCancelados++;
                            }
                        }
                    }
                    
                    Log.d(TAG, "✅ Verificación completada. Tours revisados: " + toursRevisados + 
                        ", Tours cancelados: " + toursCancelados);
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error en verificación: " + e.getMessage(), e);
                    success[0] = false;
                    latch.countDown();
                });
            
            // Esperar máximo 60 segundos a que termine
            boolean finished = latch.await(60, TimeUnit.SECONDS);
            
            if (!finished) {
                Log.e(TAG, "⏱️ Timeout esperando verificación de tours");
                return Result.failure();
            }
            
            return success[0] ? Result.success() : Result.retry();
            
        } catch (Exception e) {
            Log.e(TAG, "💥 Error fatal en worker: " + e.getMessage(), e);
            return Result.failure();
        }
    }
}

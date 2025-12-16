package com.example.connectifyproject.views.superadmin.reports;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.util.*;

/**
 * Repository para cargar datos reales de Firebase para el Dashboard de SuperAdmin.
 * 
 * Colecciones usadas:
 * - usuarios: filtrar por rol="Administrador" y habilitado=true para contar empresas activas
 * - pagos: filtrar por tipoPago="A Empresa" para calcular recaudación por empresa
 * - tours_asignados: filtrar por fechaRealizacion para contar tours completados/pendientes
 */
public class SaReportsRepository {

    private static final String TAG = "sa-reports-repo";
    
    // Colecciones
    private static final String COL_USUARIOS = "usuarios";
    private static final String COL_PAGOS = "pagos";
    private static final String COL_TOURS = "tours_asignados";
    
    // Campos usuarios
    private static final String FIELD_ROL = "rol";
    private static final String FIELD_HABILITADO = "habilitado";
    private static final String FIELD_NOMBRE_EMPRESA = "nombreEmpresa";
    
    // Campos pagos
    private static final String FIELD_TIPO_PAGO = "tipoPago";
    private static final String FIELD_UID_RECIBE = "uidUsuarioRecibe";
    private static final String FIELD_MONTO = "monto";
    private static final String FIELD_FECHA = "fecha";
    
    // Campos tours
    private static final String FIELD_FECHA_REALIZACION = "fechaRealizacion";
    private static final String FIELD_ESTADO = "estado";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface LoadCallback {
        void onLoaded(ReportsSummary summary, List<CompanyStat> allCompanies);
        void onError(Exception e);
    }

    /**
     * Carga los datos del mes seleccionado desde Firebase.
     * 
     * @param year Año (ej: 2025)
     * @param month Mes (1-12)
     * @param cb Callback con resultados
     */
    public void loadMonthData(int year, MonthFilter month, @NonNull LoadCallback cb) {
        Log.d(TAG, "Cargando datos para " + month.number + "/" + year);
        
        // Calcular rango de timestamps del mes
        Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"));
        calStart.set(Calendar.YEAR, year);
        calStart.set(Calendar.MONTH, month.number - 1);
        calStart.set(Calendar.DAY_OF_MONTH, 1);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        
        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.MONTH, 1);
        
        Timestamp tsStart = new Timestamp(calStart.getTime());
        Timestamp tsEnd = new Timestamp(calEnd.getTime());
        
        Log.d(TAG, "Rango: " + tsStart.toDate() + " - " + tsEnd.toDate());

        // 1) Cargar empresas activas (usuarios con rol=Administrador y habilitado=true)
        db.collection(COL_USUARIOS)
                .whereEqualTo(FIELD_ROL, "Administrador")
                .whereEqualTo(FIELD_HABILITADO, true)
                .get()
                .addOnCompleteListener(empresasTask -> {
                    if (!empresasTask.isSuccessful()) {
                        Log.e(TAG, "Error cargando empresas", empresasTask.getException());
                        cb.onError(empresasTask.getException());
                        return;
                    }
                    
                    List<DocumentSnapshot> empresaDocs = empresasTask.getResult().getDocuments();
                    int empresasActivas = empresaDocs.size();
                    
                    // Mapear UID -> nombre empresa
                    Map<String, String> uidToEmpresa = new HashMap<>();
                    for (DocumentSnapshot doc : empresaDocs) {
                        String uid = doc.getId();
                        String nombre = doc.getString(FIELD_NOMBRE_EMPRESA);
                        if (nombre == null || nombre.isEmpty()) {
                            nombre = "Empresa " + uid.substring(0, 6);
                        }
                        uidToEmpresa.put(uid, nombre);
                    }
                    
                    Log.d(TAG, "Empresas activas encontradas: " + empresasActivas);

                    // 2) Cargar pagos del mes (tipoPago = "A Empresa")
                    db.collection(COL_PAGOS)
                            .whereEqualTo(FIELD_TIPO_PAGO, "A Empresa")
                            .get()
                            .addOnCompleteListener(pagosTask -> {
                                if (!pagosTask.isSuccessful()) {
                                    Log.e(TAG, "Error cargando pagos", pagosTask.getException());
                                    cb.onError(pagosTask.getException());
                                    return;
                                }
                                
                                // Acumular recaudación por empresa (uidUsuarioRecibe)
                                Map<String, Double> recaudacionPorEmpresa = new HashMap<>();
                                final double[] totalRecaudadoMes = {0}; // Usar array para effectively final
                                
                                for (DocumentSnapshot pagoDoc : pagosTask.getResult().getDocuments()) {
                                    Timestamp fechaPago = pagoDoc.getTimestamp(FIELD_FECHA);
                                    if (fechaPago == null) continue;
                                    
                                    // Filtrar por mes
                                    if (fechaPago.compareTo(tsStart) >= 0 && fechaPago.compareTo(tsEnd) < 0) {
                                        String uidRecibe = pagoDoc.getString(FIELD_UID_RECIBE);
                                        Double monto = pagoDoc.getDouble(FIELD_MONTO);
                                        
                                        if (uidRecibe != null && monto != null) {
                                            recaudacionPorEmpresa.merge(uidRecibe, monto, Double::sum);
                                            totalRecaudadoMes[0] += monto;
                                        }
                                    }
                                }
                                
                                Log.d(TAG, "Total recaudado en el mes: S/." + totalRecaudadoMes[0]);

                                // 3) Cargar TODOS los tours y filtrar por fecha en cliente
                                // (evita problemas con tipos mixtos Timestamp/String)
                                db.collection(COL_TOURS)
                                        .get()
                                        .addOnCompleteListener(toursTask -> {
                                            if (!toursTask.isSuccessful()) {
                                                Log.e(TAG, "Error cargando tours", toursTask.getException());
                                                cb.onError(toursTask.getException());
                                                return;
                                            }
                                            
                                            int toursCompletados = 0;
                                            int toursPendientes = 0;
                                            
                                            Log.d(TAG, "Tours totales encontrados: " + toursTask.getResult().size());
                                            
                                            for (DocumentSnapshot tourDoc : toursTask.getResult().getDocuments()) {
                                                // Obtener fecha de realización (puede ser Timestamp o String)
                                                Timestamp fechaRealizacion = null;
                                                Object fechaObj = tourDoc.get(FIELD_FECHA_REALIZACION);
                                                
                                                if (fechaObj instanceof Timestamp) {
                                                    fechaRealizacion = (Timestamp) fechaObj;
                                                } else if (fechaObj instanceof String) {
                                                    fechaRealizacion = parseFechaString((String) fechaObj);
                                                }
                                                
                                                // Filtrar por mes
                                                if (fechaRealizacion == null) continue;
                                                if (fechaRealizacion.compareTo(tsStart) < 0 || fechaRealizacion.compareTo(tsEnd) >= 0) {
                                                    continue;
                                                }
                                                
                                                String estado = tourDoc.getString(FIELD_ESTADO);
                                                if (estado == null) continue;
                                                
                                                if ("completado".equalsIgnoreCase(estado)) {
                                                    toursCompletados++;
                                                } else if (!"cancelado".equalsIgnoreCase(estado)) {
                                                    toursPendientes++;
                                                }
                                            }
                                            
                                            Log.d(TAG, "Tours del mes - Completados: " + toursCompletados + ", Pendientes: " + toursPendientes);

                                            // Construir lista de CompanyStat con recaudación
                                            List<CompanyStat> companyStats = new ArrayList<>();
                                            for (Map.Entry<String, String> entry : uidToEmpresa.entrySet()) {
                                                String uid = entry.getKey();
                                                String nombreEmpresa = entry.getValue();
                                                double recaudacion = recaudacionPorEmpresa.getOrDefault(uid, 0.0);
                                                
                                                CompanyStat stat = new CompanyStat(uid, nombreEmpresa, true, (int) recaudacion);
                                                companyStats.add(stat);
                                            }
                                            
                                            // Ordenar por recaudación descendente
                                            companyStats.sort((a, b) -> Integer.compare(b.monthTotal, a.monthTotal));
                                            
                                            // Calcular promedio por día
                                            int daysInMonth = daysInMonth(year, month.number);
                                            double promedioDia = daysInMonth == 0 ? 0 : totalRecaudadoMes[0] / daysInMonth;
                                            
                                            // Crear resumen (total = recaudación total, empresasActivas, promedio/día)
                                            ReportsSummary summary = new ReportsSummary(
                                                    (int) totalRecaudadoMes[0],
                                                    empresasActivas,
                                                    Math.round(promedioDia * 100.0) / 100.0
                                            );
                                            
                                            // Agregar info de tours al summary
                                            summary.toursCompletados = toursCompletados;
                                            summary.toursPendientes = toursPendientes;
                                            
                                            Log.d(TAG, "Datos cargados exitosamente");
                                            cb.onLoaded(summary, companyStats);
                                        });
                            });
                });
    }

    private int daysInMonth(int year, int month1to12) {
        Calendar cc = Calendar.getInstance();
        cc.set(Calendar.YEAR, year);
        cc.set(Calendar.MONTH, month1to12 - 1);
        return cc.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * Parsea una fecha en formato String a Timestamp.
     * Soporta varios formatos comunes.
     */
    private Timestamp parseFechaString(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) return null;
        
        try {
            // Limpiar la cadena (quitar la parte de la hora si existe)
            String fechaLimpia = fechaStr;
            if (fechaStr.contains(" a las ")) {
                fechaLimpia = fechaStr.substring(0, fechaStr.indexOf(" a las "));
            }
            
            String[] formatos = {
                "dd 'de' MMMM 'de' yyyy",
                "dd/MM/yyyy",
                "dd-MM-yyyy"
            };
            
            for (String formato : formatos) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(formato, new java.util.Locale("es", "PE"));
                    sdf.setTimeZone(TimeZone.getTimeZone("America/Lima"));
                    java.util.Date date = sdf.parse(fechaLimpia);
                    if (date != null) {
                        return new Timestamp(date);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parseando fecha: " + fechaStr, e);
        }
        return null;
    }
}

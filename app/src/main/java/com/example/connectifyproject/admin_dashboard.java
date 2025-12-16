package com.example.connectifyproject;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.connectifyproject.databinding.AdminDashboardViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Dashboard del Administrador con datos REALES de Firebase.
 * 
 * Muestra:
 * - Total recaudado en el mes (pagos donde uidUsuarioRecibe = adminUid)
 * - Tours por estado (completado, pendiente, cancelado)
 * - Top 5 tours por recaudación (basado en nombreTour en colección pagos)
 * - Distribución por día de la semana
 */
public class admin_dashboard extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";
    
    private AdminDashboardViewBinding binding;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // empresa/administrador - se cargan dinámicamente desde Firebase
    private String companyName = "Cargando...";
    private String adminName   = "Cargando...";
    private String adminUid = null;

    private int selectedMonth;
    private int selectedYear;
    
    // Datos cargados
    private int totalRecaudadoMes = 0;
    private int toursCompletados = 0;
    private int toursPendientes = 0;
    private int toursCancelados = 0;
    private int[] recaudacionPorDia = new int[7]; // Lun-Dom
    private List<TourStat> top5Tours = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminDashboardViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        if (currentUser != null) {
            adminUid = currentUser.getUid();
        }

        // AppBar
        MaterialToolbar tb = binding.topAppBar;
        setSupportActionBar(tb);

        // Header - mostrar texto temporal mientras carga
        binding.tvCompanyHeader.setText(companyName);
        binding.tvAdminHeader.setText("Administrador: " + adminName);

        // Cargar datos del usuario desde Firebase
        loadUserData();

        // Menú de notificaciones
        binding.ivNotification.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_admin_notifications, popup.getMenu());
            popup.setOnMenuItemClickListener(this::onNotificationAction);
            popup.show();
        });

        // Bottom Nav
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("dashboard");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();

        // Mes y año actual por defecto
        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH);
        selectedYear = cal.get(Calendar.YEAR);

        // Dropdown de mes
        setupMonthDropdown(binding.adminAutoMonth);
        binding.adminAutoMonth.setText(monthLabel(selectedMonth), false);
        binding.adminAutoMonth.setOnItemClickListener((parent, view, position, id) -> {
            selectedMonth = position;
            loadDashboardData();
        });

        // Primera carga
        loadDashboardData();
    }

    private boolean onNotificationAction(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_mark_all_read) {
            return true;
        } else if (id == R.id.action_preferences) {
            return true;
        } else if (id == R.id.action_view_all) {
            return true;
        }
        return false;
    }

    /**
     * Carga los datos del dashboard desde Firebase.
     * 1. Pagos recibidos en el mes (tipoPago = "A Empresa", uidUsuarioRecibe = adminUid)
     * 2. Tours de la empresa por estado
     * 3. Top 5 tours por recaudación
     */
    private void loadDashboardData() {
        if (adminUid == null) {
            Log.e(TAG, "adminUid es null, no se pueden cargar datos");
            return;
        }
        
        Log.d(TAG, "Cargando datos para mes " + (selectedMonth + 1) + "/" + selectedYear);
        
        // Calcular rango de timestamps del mes
        Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"));
        calStart.set(Calendar.YEAR, selectedYear);
        calStart.set(Calendar.MONTH, selectedMonth);
        calStart.set(Calendar.DAY_OF_MONTH, 1);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        
        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.MONTH, 1);
        
        Timestamp tsStart = new Timestamp(calStart.getTime());
        Timestamp tsEnd = new Timestamp(calEnd.getTime());
        
        // 1) Cargar pagos recibidos del mes (para esta empresa)
        db.collection("pagos")
                .whereEqualTo("uidUsuarioRecibe", adminUid)
                .whereEqualTo("tipoPago", "A Empresa")
                .get()
                .addOnSuccessListener(pagosSnapshot -> {
                    // Reset datos
                    totalRecaudadoMes = 0;
                    recaudacionPorDia = new int[7];
                    Map<String, Integer> recaudacionPorTour = new HashMap<>();
                    
                    for (DocumentSnapshot doc : pagosSnapshot.getDocuments()) {
                        Timestamp fechaPago = doc.getTimestamp("fecha");
                        if (fechaPago == null) continue;
                        
                        // Filtrar por mes
                        if (fechaPago.compareTo(tsStart) >= 0 && fechaPago.compareTo(tsEnd) < 0) {
                            Double monto = doc.getDouble("monto");
                            String nombreTour = doc.getString("nombreTour");
                            
                            if (monto != null) {
                                int montoInt = monto.intValue();
                                totalRecaudadoMes += montoInt;
                                
                                // Distribución por día de la semana
                                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"));
                                cal.setTime(fechaPago.toDate());
                                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                                // Convertir: SUNDAY=1 -> índice 6, MONDAY=2 -> índice 0, etc.
                                int index = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
                                if (index >= 0 && index < 7) {
                                    recaudacionPorDia[index] += montoInt;
                                }
                                
                                // Acumular por nombre de tour
                                if (nombreTour != null && !nombreTour.isEmpty()) {
                                    recaudacionPorTour.merge(nombreTour, montoInt, Integer::sum);
                                }
                            }
                        }
                    }
                    
                    // Calcular Top 5 tours
                    top5Tours.clear();
                    for (Map.Entry<String, Integer> entry : recaudacionPorTour.entrySet()) {
                        top5Tours.add(new TourStat(entry.getKey(), entry.getValue()));
                    }
                    top5Tours.sort((a, b) -> Integer.compare(b.recaudacion, a.recaudacion));
                    if (top5Tours.size() > 5) {
                        top5Tours = new ArrayList<>(top5Tours.subList(0, 5));
                    }
                    
                    Log.d(TAG, "Total recaudado: S/." + totalRecaudadoMes + ", Tours: " + top5Tours.size());
                    
                    // 2) Cargar tours de esta empresa para contar estados
                    loadToursData(tsStart, tsEnd);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando pagos", e);
                    updateUI();
                });
    }
    
    private void loadToursData(Timestamp tsStart, Timestamp tsEnd) {
        Log.d(TAG, "Cargando tours para empresaId: " + adminUid);
        Log.d(TAG, "Rango de fechas: " + tsStart.toDate() + " - " + tsEnd.toDate());
        
        // Cargar todos los tours de la empresa y filtrar por fecha en cliente
        // (evita problemas de índices compuestos y tipos de datos mixtos)
        db.collection("tours_asignados")
                .whereEqualTo("empresaId", adminUid)
                .get()
                .addOnSuccessListener(toursSnapshot -> {
                    toursCompletados = 0;
                    toursPendientes = 0;
                    toursCancelados = 0;
                    
                    Log.d(TAG, "Tours encontrados para empresa: " + toursSnapshot.size());
                    
                    for (DocumentSnapshot doc : toursSnapshot.getDocuments()) {
                        // Obtener fecha de realización (puede ser Timestamp o String)
                        Timestamp fechaRealizacion = null;
                        Object fechaObj = doc.get("fechaRealizacion");
                        
                        if (fechaObj instanceof Timestamp) {
                            fechaRealizacion = (Timestamp) fechaObj;
                        } else if (fechaObj instanceof String) {
                            // Intentar parsear String a Timestamp
                            fechaRealizacion = parseFechaString((String) fechaObj);
                        }
                        
                        // Filtrar por mes
                        if (fechaRealizacion == null) {
                            Log.w(TAG, "Tour sin fechaRealizacion válida: " + doc.getId());
                            continue;
                        }
                        
                        if (fechaRealizacion.compareTo(tsStart) < 0 || fechaRealizacion.compareTo(tsEnd) >= 0) {
                            continue; // Fuera del rango del mes seleccionado
                        }
                        
                        String estado = doc.getString("estado");
                        Log.d(TAG, "Tour: " + doc.getId() + ", estado: " + estado + ", fecha: " + fechaRealizacion.toDate());
                        
                        if (estado == null) continue;
                        
                        switch (estado.toLowerCase()) {
                            case "completado":
                                toursCompletados++;
                                break;
                            case "cancelado":
                                toursCancelados++;
                                break;
                            default:
                                // pendiente, check_in, en_curso, check_out
                                toursPendientes++;
                                break;
                        }
                    }
                    
                    Log.d(TAG, "Tours del mes - Completados: " + toursCompletados + 
                              ", Pendientes: " + toursPendientes + 
                              ", Cancelados: " + toursCancelados);
                    
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando tours", e);
                    updateUI();
                });
    }
    
    /**
     * Parsea una fecha en formato String a Timestamp.
     * Formato esperado: "15 de diciembre de 2025 a las 4:02:29 a.m. UTC-5"
     */
    private Timestamp parseFechaString(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) return null;
        
        try {
            // Intentar varios formatos
            String[] formatos = {
                "dd 'de' MMMM 'de' yyyy",
                "dd/MM/yyyy",
                "dd-MM-yyyy"
            };
            
            // Limpiar la cadena (quitar la parte de la hora si existe)
            String fechaLimpia = fechaStr;
            if (fechaStr.contains(" a las ")) {
                fechaLimpia = fechaStr.substring(0, fechaStr.indexOf(" a las "));
            }
            
            for (String formato : formatos) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(formato, new Locale("es", "PE"));
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

    /** Actualiza la UI con los datos cargados de Firebase. */
    private void updateUI() {
        // Total del mes
        binding.adminTvTotalMonth.setText(formatCurrency(totalRecaudadoMes));

        // Barras por día de semana
        LinearProgressIndicator[] bars = new LinearProgressIndicator[]{
                binding.adminBarMon, binding.adminBarTue, binding.adminBarWed,
                binding.adminBarThu, binding.adminBarFri, binding.adminBarSat, binding.adminBarSun
        };
        TextView[] labels = new TextView[]{
                binding.adminTvMon, binding.adminTvTue, binding.adminTvWed,
                binding.adminTvThu, binding.adminTvFri, binding.adminTvSat, binding.adminTvSun
        };
        
        int maxDow = 0;
        for (int v : recaudacionPorDia) maxDow = Math.max(maxDow, v);
        
        for (int i = 0; i < 7; i++) {
            int val = recaudacionPorDia[i];
            labels[i].setText(formatCurrency(val));
            int pct = (maxDow == 0) ? 0 : Math.round(val * 100f / maxDow);
            bars[i].setProgress(pct);
        }

        // Pie de estados (orden: finished=completados, active=pendientes, cancelled=cancelados)
        binding.adminPie.setValues(toursCompletados, toursPendientes, toursCancelados);
        binding.adminPie.setColors(
                ContextCompat.getColor(this, R.color.legend_finished),   // Completados -> amarillo
                ContextCompat.getColor(this, R.color.legend_active),     // Pendientes  -> verde
                ContextCompat.getColor(this, R.color.legend_cancelled)   // Cancelados  -> rojo
        );
        binding.adminLegendFinished.setText(String.valueOf(toursCompletados));
        binding.adminLegendActive.setText(String.valueOf(toursPendientes));
        binding.adminLegendCancelled.setText(String.valueOf(toursCancelados));

        // Top 5 tours (mostrar solo los primeros 3 en la UI existente)
        String[] topNames = new String[]{"Sin datos", "Sin datos", "Sin datos"};
        int[] topVals = new int[]{0, 0, 0};
        
        for (int i = 0; i < Math.min(3, top5Tours.size()); i++) {
            topNames[i] = top5Tours.get(i).nombreTour;
            topVals[i] = top5Tours.get(i).recaudacion;
        }
        
        int maxTop = Math.max(topVals[0], Math.max(topVals[1], topVals[2]));

        binding.adminTop1Name.setText(topNames[0]);
        binding.adminTop2Name.setText(topNames[1]);
        binding.adminTop3Name.setText(topNames[2]);

        binding.adminTop1Val.setText(formatCurrency(topVals[0]));
        binding.adminTop2Val.setText(formatCurrency(topVals[1]));
        binding.adminTop3Val.setText(formatCurrency(topVals[2]));

        binding.adminBarTop1.setIndicatorColor(ContextCompat.getColor(this, R.color.sa_top1));
        binding.adminBarTop2.setIndicatorColor(ContextCompat.getColor(this, R.color.sa_top2));
        binding.adminBarTop3.setIndicatorColor(ContextCompat.getColor(this, R.color.sa_top3));

        binding.adminBarTop1.setProgress(maxTop == 0 ? 0 : Math.round(topVals[0] * 100f / maxTop));
        binding.adminBarTop2.setProgress(maxTop == 0 ? 0 : Math.round(topVals[1] * 100f / maxTop));
        binding.adminBarTop3.setProgress(maxTop == 0 ? 0 : Math.round(topVals[2] * 100f / maxTop));

        // Titulares
        styleTitle(binding.adminTvSectionDow);
        styleTitle(binding.adminTvSectionPie);
        styleTitle(binding.adminTvSectionTop);
    }
    
    private String formatCurrency(int amount) {
        if (amount >= 1000) {
            return "S/." + String.format(new Locale("es", "PE"), "%,d", amount);
        }
        return "S/." + amount;
    }

    private void styleTitle(TextView tv) {
        tv.setTextSize(22f);
        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
    }

    // --- dropdown mes ---
    private void setupMonthDropdown(AutoCompleteTextView view) {
        String[] months = new DateFormatSymbols(new Locale("es")).getMonths();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 12; i++) list.add(capitalize(months[i]));
        view.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list));
    }

    private String monthLabel(int monthIndex) {
        String[] months = new DateFormatSymbols(new Locale("es")).getMonths();
        return capitalize(months[monthIndex]);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(new Locale("es")) + s.substring(1);
    }

    /**
     * Carga los datos del usuario desde Firebase (nombre empresa y administrador)
     */
    private void loadUserData() {
        if (currentUser == null) {
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateHeaderWithUserData(documentSnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    // Si falla, mantener los valores por defecto
                    companyName = "Empresa";
                    adminName = "Administrador";
                    binding.tvCompanyHeader.setText(companyName);
                    binding.tvAdminHeader.setText("Administrador: " + adminName);
                });
    }

    /**
     * Actualiza el header con los datos cargados de Firebase
     */
    private void updateHeaderWithUserData(DocumentSnapshot document) {
        try {
            String nombreEmpresa = document.getString("nombreEmpresa");
            String nombreCompleto = document.getString("nombreCompleto");

            if (nombreEmpresa != null && !nombreEmpresa.isEmpty()) {
                companyName = nombreEmpresa;
            } else {
                companyName = "Mi Empresa";
            }

            if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                adminName = nombreCompleto;
            } else {
                adminName = "Administrador";
            }

            // Actualizar UI
            binding.tvCompanyHeader.setText(companyName);
            binding.tvAdminHeader.setText("Administrador: " + adminName);

        } catch (Exception e) {
            Log.e(TAG, "Error actualizando header", e);
        }
    }

    /**
     * Clase auxiliar para almacenar estadísticas de tours
     */
    private static class TourStat {
        final String nombreTour;
        final int recaudacion;
        
        TourStat(String nombreTour, int recaudacion) {
            this.nombreTour = nombreTour;
            this.recaudacion = recaudacion;
        }
    }
}

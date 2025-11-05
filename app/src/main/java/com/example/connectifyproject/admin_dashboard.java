package com.example.connectifyproject;

import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class admin_dashboard extends AppCompatActivity {

    private AdminDashboardViewBinding binding;

    // empresa/administrador que ya muestras en el header
    private String companyName = "Mundo Tours";
    private String adminName   = "Tony Flores";

    private int selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminDashboardViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // AppBar
        MaterialToolbar tb = binding.topAppBar;
        setSupportActionBar(tb);

        // Header
        binding.tvCompanyHeader.setText(companyName);
        binding.tvAdminHeader.setText("Administrador: " + adminName);

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

        // Mes actual por defecto
        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH);

        // Dropdown de mes
        setupMonthDropdown(binding.adminAutoMonth);
        binding.adminAutoMonth.setText(monthLabel(selectedMonth), false);
        binding.adminAutoMonth.setOnItemClickListener((parent, view, position, id) -> {
            selectedMonth = position;
            rebuild();
        });

        // Primera carga
        rebuild();
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

    /** Rellena todos los widgets del dashboard con datos (mock deterministas por empresa+mes). */
    private void rebuild() {
        MockData md = generateMockFor(companyName, selectedMonth);

        // Total del mes
        binding.adminTvTotalMonth.setText(String.valueOf(md.totalMonth));

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
        for (int v : md.byDow) maxDow = Math.max(maxDow, v);
        for (int i = 0; i < 7; i++) {
            int val = md.byDow[i];
            labels[i].setText(String.valueOf(val));
            int pct = (maxDow == 0) ? 0 : Math.round(val * 100f / maxDow);
            bars[i].setProgress(pct);
        }

        // Pie estados (orden: finished, active, cancelled)
        binding.adminPie.setValues(md.finished, md.active, md.cancelled);
        binding.adminPie.setColors(
                ContextCompat.getColor(this, R.color.legend_finished),   // Finalizadas -> #F1A20B
                ContextCompat.getColor(this, R.color.legend_active),     // Activas     -> #8D9C09
                ContextCompat.getColor(this, R.color.legend_cancelled)   // Canceladas  -> #D20D20
        );
        binding.adminLegendFinished.setText(String.valueOf(md.finished));
        binding.adminLegendActive.setText(String.valueOf(md.active));
        binding.adminLegendCancelled.setText(String.valueOf(md.cancelled));

        // Top-3 tours
        int[] topVals = new int[]{ md.top3Values[0], md.top3Values[1], md.top3Values[2] };
        String[] topNames = new String[]{ md.top3Names[0], md.top3Names[1], md.top3Names[2] };
        int maxTop = Math.max(topVals[0], Math.max(topVals[1], topVals[2]));

        binding.adminTop1Name.setText(topNames[0]);
        binding.adminTop2Name.setText(topNames[1]);
        binding.adminTop3Name.setText(topNames[2]);

        binding.adminTop1Val.setText(String.valueOf(topVals[0]));
        binding.adminTop2Val.setText(String.valueOf(topVals[1]));
        binding.adminTop3Val.setText(String.valueOf(topVals[2]));

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

    // --- datos mock iguales al fragment de SA ---
    private MockData generateMockFor(String company, int monthIdx) {
        long seed = (company == null ? 0 : company.hashCode()) * 31L + monthIdx;
        Random r = new Random(seed);

        MockData m = new MockData();
        m.totalMonth = 100 + r.nextInt(401);

        int left = m.totalMonth;
        for (int i = 0; i < 7; i++) {
            int v = (i == 6) ? Math.max(0, left)
                    : r.nextInt(Math.max(1, m.totalMonth / 3));
            m.byDow[i] = v;
            left -= v;
        }
        for (int i = 0; i < 7; i++) if (m.byDow[i] < 0) m.byDow[i] = 0;

        m.finished  = Math.max(0, (int) Math.round(m.totalMonth * (0.45 + r.nextDouble() * 0.25)));
        m.active    = Math.max(0, (int) Math.round(m.totalMonth * (0.20 + r.nextDouble() * 0.20)));
        m.cancelled = Math.max(0, m.totalMonth - m.finished - m.active);

        m.top3Names[0] = "City Tour";
        m.top3Names[1] = "Montaña 7 Colores";
        m.top3Names[2] = "Lago Titicaca";
        int base = Math.max(1, m.totalMonth / 5);
        m.top3Values[0] = base + r.nextInt(base);
        m.top3Values[1] = base - 5 + r.nextInt(base);
        m.top3Values[2] = base - 10 + r.nextInt(base);
        for (int i = 0; i < 3; i++) if (m.top3Values[i] < 0) m.top3Values[i] = r.nextInt(base);

        // ordenar desc
        for (int i = 0; i < 2; i++) {
            for (int j = i + 1; j < 3; j++) {
                if (m.top3Values[j] > m.top3Values[i]) {
                    int tv = m.top3Values[i]; m.top3Values[i] = m.top3Values[j]; m.top3Values[j] = tv;
                    String tn = m.top3Names[i]; m.top3Names[i] = m.top3Names[j]; m.top3Names[j] = tn;
                }
            }
        }
        return m;
    }

    private static class MockData {
        int totalMonth;
        int[] byDow = new int[7];
        int finished, active, cancelled;
        String[] top3Names = new String[3];
        int[] top3Values = new int[3];
    }
}

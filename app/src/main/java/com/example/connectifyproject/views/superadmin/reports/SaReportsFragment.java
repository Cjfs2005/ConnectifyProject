package com.example.connectifyproject.views.superadmin.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaReportsBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reportes de Reservas – SuperAdmin
 * - Filtros Mes/Empresa
 * - KPIs (Total mes, Activas, Promedio/día) SIEMPRE sobre TODAS las empresas
 * - Si Empresa="Todas": Top-5 (barras) + lista completa
 * - Si Empresa específica: oculta Top-5 y muestra solo esa empresa
 */
public class SaReportsFragment extends Fragment {

    private FragmentSaReportsBinding binding;

    // data: nombreEmpresa -> vector[12] con reservas por mes
    private final Map<String, int[]> data = new LinkedHashMap<>();

    private int selectedMonth;
    private String selectedCompany = "Todas";

    // Paleta para Top-5 (del mayor al menor)
    private static final int[] TOP5_COLORS = new int[]{
            Color.parseColor("#FF9B8F"),
            Color.parseColor("#EF7689"),
            Color.parseColor("#9E6A90"),
            Color.parseColor("#766788"),
            Color.parseColor("#71556B")
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSaReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        final NavController nav = NavHostFragment.findNavController(this);

        // 🔔 Campanita → Notificaciones
        View bell = v.findViewById(R.id.btnNotifications);
        if (bell != null) {
            bell.setOnClickListener(x -> {
                Bundle args = new Bundle();
                args.putInt("fromDestId",
                        nav.getCurrentDestination() != null ? nav.getCurrentDestination().getId() : 0);
                nav.navigate(R.id.saNotificationsFragment, args);
            });
        }

        // Datos demo
        seedMock();

        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH);

        setupMonthDropdown(binding.autoMonth);
        setupCompanyDropdown(binding.autoCompany);

        if (s != null) {
            selectedMonth = s.getInt("month", selectedMonth);
            selectedCompany = s.getString("company", "Todas");
        }

        binding.autoMonth.setText(monthLabel(selectedMonth), false);
        binding.autoCompany.setText(selectedCompany, false);

        binding.autoMonth.setOnItemClickListener((parent, view, position, id) -> {
            selectedMonth = position;
            rebuildDashboard();
        });

        binding.autoCompany.setOnItemClickListener((parent, view, position, id) -> {
            selectedCompany = (String) parent.getItemAtPosition(position);
            rebuildDashboard();
        });

        rebuildDashboard();
    }

    /** Reconstruye KPIs + Top-5 (si aplica) + lista completa. */
    private void rebuildDashboard() {
        // KPIs SIEMPRE con TODAS las empresas
        int totalMes = 0;
        int activas = 0;
        for (Map.Entry<String, int[]> e : data.entrySet()) {
            int v = e.getValue()[selectedMonth];
            totalMes += v;
            if (v > 0) activas++;
        }
        int days = daysInMonth(selectedMonth);
        int promedio = days == 0 ? 0 : (int) Math.round(totalMes / (double) days);

        binding.tvTotalMonth.setText(String.valueOf(totalMes));
        binding.tvActiveCompanies.setText(String.valueOf(activas));
        binding.tvAvgPerDay.setText(String.valueOf(promedio));

        // Reset contenedor
        binding.listContainer.removeAllViews();

        boolean hayDatos = totalMes > 0;
        binding.tvEmpty.setVisibility(hayDatos ? View.GONE : View.VISIBLE);
        if (!hayDatos) return;

        LayoutInflater inf = LayoutInflater.from(requireContext());

        if ("Todas".equals(selectedCompany)) {
            // ---------- Top-5 (orden desc) ----------
            List<ItemVal> vals = new ArrayList<>();
            for (Map.Entry<String, int[]> e : data.entrySet()) {
                vals.add(new ItemVal(e.getKey(), e.getValue()[selectedMonth]));
            }
            vals.sort((a, b) -> Integer.compare(b.value, a.value));
            List<ItemVal> top5 = vals.size() > 5 ? vals.subList(0, 5) : vals;

            int maxTop = 0;
            for (ItemVal it : top5) if (it.value > maxTop) maxTop = it.value;

            // Título grande
            binding.listContainer.addView(makeSectionTitle(inf,
                    "Top 5 empresas — " + monthLabel(selectedMonth), 25f));

            // Barra por empresa (usa sa_item_company_bar.xml)
            for (int i = 0; i < top5.size(); i++) {
                ItemVal it = top5.get(i);
                View row = inf.inflate(R.layout.sa_item_company_bar, binding.listContainer, false);

                TextView tvCompany = row.findViewById(R.id.tvCompany);
                Chip chip = row.findViewById(R.id.chipCount);
                LinearProgressIndicator prog = row.findViewById(R.id.progress);

                tvCompany.setText(it.name);
                chip.setText(String.valueOf(it.value));

                int percent = maxTop == 0 ? 0 : Math.round(it.value * 100f / maxTop);
                if (percent < 2 && it.value > 0) percent = 2;
                prog.setProgress(percent);

                // Color por ranking (0 = mayor → primer color)
                int color = TOP5_COLORS[Math.min(i, TOP5_COLORS.length - 1)];
                prog.setIndicatorColor(color);
                prog.setTrackColor(Color.parseColor("#E6E6E6"));

                // → tocar una barra también navega al detalle de esa empresa
                row.setOnClickListener(v -> navigateToCompanyReport(it.name));

                binding.listContainer.addView(row);
            }

            // ---------- Lista completa ----------
            binding.listContainer.addView(makeSectionTitle(inf, "Empresas (todas)", 25f));

            for (Map.Entry<String, int[]> e : data.entrySet()) {
                String company = e.getKey();
                int value = e.getValue()[selectedMonth];

                View row = inf.inflate(R.layout.sa_item_company_row, binding.listContainer, false);
                TextView tvName = row.findViewById(R.id.tvName);
                TextView tvBadge = row.findViewById(R.id.tvBadge);

                tvName.setText(company);
                tvBadge.setText(String.valueOf(value));

                // ✅ NUEVO: al tocar la fila, ir al detalle de esa empresa
                row.setOnClickListener(v -> navigateToCompanyReport(company));

                binding.listContainer.addView(row);
            }

        } else {
            // Empresa específica → solo esa
            int[] vector = data.get(selectedCompany);
            int valor = vector == null ? 0 : vector[selectedMonth];

            binding.listContainer.addView(makeSectionTitle(inf, selectedCompany, 25f));

            View row = inf.inflate(R.layout.sa_item_company_row, binding.listContainer, false);
            ((TextView) row.findViewById(R.id.tvName)).setText(selectedCompany);
            ((TextView) row.findViewById(R.id.tvBadge)).setText(String.valueOf(valor));

            // ✅ NUEVO: también navegamos desde la vista filtrada
            row.setOnClickListener(v -> navigateToCompanyReport(selectedCompany));

            binding.listContainer.addView(row);
        }
    }

    /** Navega al fragmento de reporte por empresa, pasando el nombre como argumento. */
    private void navigateToCompanyReport(String companyName) {
        NavController nav = NavHostFragment.findNavController(this);
        Bundle args = new Bundle();
        args.putString("companyName", companyName);
        nav.navigate(R.id.action_saReports_to_saCompanyReport, args);
    }

    // ---------- helpers UI ----------
    private TextView makeSectionTitle(LayoutInflater inf, String text, float sizeSp) {
        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int padTop = Math.round(12 * getResources().getDisplayMetrics().density);
        int padBottom = Math.round(8 * getResources().getDisplayMetrics().density);
        tv.setPadding(0, padTop, 0, padBottom);
        tv.setText(text);
        tv.setTextSize(sizeSp); // tamaño grande
        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        return tv;
    }

    private void setupMonthDropdown(AutoCompleteTextView view) {
        String[] months = new DateFormatSymbols(new Locale("es")).getMonths();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 12; i++) list.add(capitalize(months[i]));
        view.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, list));
    }

    private void setupCompanyDropdown(AutoCompleteTextView view) {
        List<String> items = new ArrayList<>();
        items.add("Todas");
        items.addAll(data.keySet());
        view.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, items));
    }

    private String monthLabel(int monthIndex) {
        String[] months = new DateFormatSymbols(new Locale("es")).getMonths();
        return capitalize(months[monthIndex]);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(new Locale("es")) + s.substring(1);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("month", selectedMonth);
        outState.putString("company", selectedCompany);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private int daysInMonth(int monthIndex) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, monthIndex);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    // ====== DEMO DATA ======
    private static class ItemVal {
        final String name; final int value;
        ItemVal(String n, int v) { name = n; value = v; }
    }

    private void seedMock() {
        data.put("PeruBus",            new int[]{120,150,160,140,180,210,230,220,190,170,160,200});
        data.put("Inka Express",       new int[]{ 90,110,130,120,140,160,170,165,150,145,140,155});
        data.put("Cusco Shuttle",      new int[]{ 60, 70, 80, 90,110,130,140,150,140,120,110,115});
        data.put("Andes Transit",      new int[]{ 45, 55, 65, 60, 75, 85, 95,100, 90, 80, 70, 75});
        data.put("Altiplano Coaches",  new int[]{ 30, 40, 50, 55, 60, 70, 80, 85, 78, 72, 66, 70});
    }
}

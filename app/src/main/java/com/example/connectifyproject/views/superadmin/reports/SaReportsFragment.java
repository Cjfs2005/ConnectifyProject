package com.example.connectifyproject.views.superadmin.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.IdRes;
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

public class SaReportsFragment extends Fragment {

    private FragmentSaReportsBinding binding;

    // data: nombreEmpresa -> vector[12] con reservas por mes
    private final Map<String, int[]> data = new LinkedHashMap<>();

    private int selectedMonth;
    private String selectedCompany = "Todas";

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

        // 🔔 campanita → notificaciones
        View bell = v.findViewById(R.id.btnNotifications);
        if (bell != null) {
            bell.setOnClickListener(x -> {
                Bundle args = new Bundle();
                args.putInt("fromDestId",
                        nav.getCurrentDestination() != null ? nav.getCurrentDestination().getId() : 0);
                nav.navigate(R.id.saNotificationsFragment, args);
            });
        }

        seedMock(); // demo

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
        int totalMesTodas = 0;
        int activasMes = 0;
        for (Map.Entry<String, int[]> e : data.entrySet()) {
            int v = e.getValue()[selectedMonth];
            totalMesTodas += v;
            if (v > 0) activasMes++;
        }
        int days = daysInMonth(selectedMonth);
        int promedio = days == 0 ? 0 : (int) Math.round(totalMesTodas / (double) days);

        binding.tvTotalMonth.setText(String.valueOf(totalMesTodas));
        binding.tvActiveCompanies.setText(String.valueOf(activasMes));
        binding.tvAvgPerDay.setText(String.valueOf(promedio));

        // Contenedor visual
        binding.listContainer.removeAllViews();

        boolean hayDatos = totalMesTodas > 0;
        binding.tvEmpty.setVisibility(hayDatos ? View.GONE : View.VISIBLE);
        if (!hayDatos) return;

        LayoutInflater inf = LayoutInflater.from(requireContext());

        if ("Todas".equals(selectedCompany)) {
            // ---------- Top-5 ----------
            List<ItemVal> vals = new ArrayList<>();
            for (Map.Entry<String, int[]> e : data.entrySet()) {
                vals.add(new ItemVal(e.getKey(), e.getValue()[selectedMonth]));
            }
            vals.sort((a, b) -> Integer.compare(b.value, a.value));
            List<ItemVal> top5 = vals.size() > 5 ? vals.subList(0, 5) : vals;

            int maxTop = 0;
            for (ItemVal it : top5) if (it.value > maxTop) maxTop = it.value;

            // título Top-5 (programático)
            binding.listContainer.addView(makeSectionTitle(inf,
                    "Top 5 empresas — " + monthLabel(selectedMonth)));

            // fila barra
            for (ItemVal it : top5) {
                View row = inf.inflate(R.layout.item_report_company_bar, binding.listContainer, false);

                TextView tvCompany = findText(row, new String[]{"tvCompany", "tvName", "textEmpresa", "title"});
                View chipOrText = findAny(row, new String[]{"chipCount", "tvBadge", "tvValue", "value"});
                LinearProgressIndicator prog = findProgress(row, new String[]{"progress", "progressBar"});

                if (tvCompany != null) tvCompany.setText(it.name);

                if (chipOrText instanceof Chip) {
                    ((Chip) chipOrText).setText(String.valueOf(it.value));
                } else if (chipOrText instanceof TextView) {
                    ((TextView) chipOrText).setText(String.valueOf(it.value));
                }

                if (prog != null) {
                    int percent = maxTop == 0 ? 0 : Math.round(it.value * 100f / maxTop);
                    if (percent < 2 && it.value > 0) percent = 2;
                    prog.setProgress(percent);
                } else {
                    // Si no existe el LinearProgressIndicator, intenta un ProgressBar clásico
                    ProgressBar pb = row.findViewById(android.R.id.progress);
                    if (pb != null) {
                        pb.setIndeterminate(false);
                        pb.setMax(100);
                        int percent = maxTop == 0 ? 0 : Math.round(it.value * 100f / maxTop);
                        pb.setProgress(percent);
                    }
                }

                binding.listContainer.addView(row);
            }

            // ---------- Lista completa ----------
            binding.listContainer.addView(makeSectionTitle(inf, "Empresas (todas)"));

            for (Map.Entry<String, int[]> e : data.entrySet()) {
                String company = e.getKey();
                int value = e.getValue()[selectedMonth];

                View row = inf.inflate(R.layout.item_report_company, binding.listContainer, false);

                TextView tvName = findText(row, new String[]{"tvName", "tvCompany", "textEmpresa", "title"});
                TextView tvBadge = findText(row, new String[]{"tvBadge", "textTotalMes", "tvValue", "value", "tv_count"});
                View btnDownload = findView(row, new String[]{"btnDownload", "buttonDownload", "btn_descargar", "btnAction"});

                if (tvName != null) tvName.setText(company);
                if (tvBadge != null) tvBadge.setText(String.valueOf(value));
                if (btnDownload instanceof ImageButton) {
                    btnDownload.setOnClickListener(v -> v.performHapticFeedback(
                            android.view.HapticFeedbackConstants.VIRTUAL_KEY));
                } else {
                    // si no hay botón, permite click en la fila
                    row.setOnClickListener(v -> v.performHapticFeedback(
                            android.view.HapticFeedbackConstants.VIRTUAL_KEY));
                }

                binding.listContainer.addView(row);
            }

        } else {
            // Empresa específica → solo esa en la lista
            int[] vector = data.get(selectedCompany);
            int valor = vector == null ? 0 : vector[selectedMonth];

            binding.listContainer.addView(makeSectionTitle(inf, selectedCompany));

            View row = inf.inflate(R.layout.item_report_company, binding.listContainer, false);

            TextView tvName = findText(row, new String[]{"tvName", "tvCompany", "textEmpresa", "title"});
            TextView tvBadge = findText(row, new String[]{"tvBadge", "textTotalMes", "tvValue", "value", "tv_count"});
            View btnDownload = findView(row, new String[]{"btnDownload", "buttonDownload", "btn_descargar", "btnAction"});

            if (tvName != null) tvName.setText(selectedCompany);
            if (tvBadge != null) tvBadge.setText(String.valueOf(valor));
            if (btnDownload instanceof ImageButton) {
                btnDownload.setOnClickListener(v -> v.performHapticFeedback(
                        android.view.HapticFeedbackConstants.VIRTUAL_KEY));
            } else {
                row.setOnClickListener(v -> v.performHapticFeedback(
                        android.view.HapticFeedbackConstants.VIRTUAL_KEY));
            }

            binding.listContainer.addView(row);
        }
    }

    // ---------- helpers tolerant a IDs ----------
    private TextView makeSectionTitle(LayoutInflater inf, String text) {
        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setPadding(0, dp(12), dp(0), dp(8));
        tv.setText(text);
        tv.setTextSize(16);
        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        return tv;
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }

    private static TextView findText(View root, String[] ids) {
        for (String name : ids) {
            @IdRes int resId = id(root, name);
            if (resId != 0) {
                View v = root.findViewById(resId);
                if (v instanceof TextView) return (TextView) v;
            }
        }
        return null;
    }

    private static View findView(View root, String[] ids) {
        for (String name : ids) {
            @IdRes int resId = id(root, name);
            if (resId != 0) {
                return root.findViewById(resId);
            }
        }
        return null;
    }

    private static LinearProgressIndicator findProgress(View root, String[] ids) {
        for (String name : ids) {
            @IdRes int resId = id(root, name);
            if (resId != 0) {
                View v = root.findViewById(resId);
                if (v instanceof LinearProgressIndicator) return (LinearProgressIndicator) v;
            }
        }
        return null;
    }

    @IdRes
    private static int id(View root, String name) {
        return root.getResources().getIdentifier(name, "id", root.getContext().getPackageName());
        // 0 si no existe
    }

    // ---------- dropdowns & util ----------
    private void setupMonthDropdown(AutoCompleteTextView view) {
        String[] months = new DateFormatSymbols(new Locale("es")).getMonths();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 12; i++) list.add(capitalize(months[i]));
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, list);
        view.setAdapter(ad);
    }

    private void setupCompanyDropdown(AutoCompleteTextView view) {
        List<String> items = new ArrayList<>();
        items.add("Todas");
        items.addAll(data.keySet());
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, items);
        view.setAdapter(ad);
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

    // Alias para mantener compatibilidad con el código anterior
    private static View findAny(View root, String[] ids) {
        return findView(root, ids);
    }

}

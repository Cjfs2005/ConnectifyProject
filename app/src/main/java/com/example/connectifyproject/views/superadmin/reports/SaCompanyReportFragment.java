package com.example.connectifyproject.views.superadmin.reports;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaCompanyReportBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SaCompanyReportFragment extends Fragment {

    private FragmentSaCompanyReportBinding binding;
    private String companyName = "";
    private int selectedMonth;

    // paleta (coincide con la de Top-5)
    @ColorInt private int c1, c2, c3, c4, c5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSaCompanyReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        // colores top (para Top-3)
        c1 = getColor(R.color.sa_top1); // #3B234A
        c2 = getColor(R.color.sa_top2); // #523961
        c3 = getColor(R.color.sa_top3); // #BAAFC4
        c4 = getColor(R.color.sa_top4); // #C3BBC9
        c5 = getColor(R.color.sa_top5); // #D4C7BF

        final NavController nav = NavHostFragment.findNavController(this);
        companyName = getArguments() != null ? getArguments().getString("companyName", "") : "";

        // Toolbar con up-navigation, título y flecha blanca
        MaterialToolbar tb = binding.toolbar;
        tb.setTitle(companyName.isEmpty() ? "Reporte de empresa" : companyName);
        tb.setNavigationIcon(R.drawable.ic_arrow_back_24);
        tb.setNavigationIconTint(getColor(android.R.color.white));
        tb.setNavigationOnClickListener(v1 -> nav.navigateUp());

        // mes actual por defecto
        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH);

        setupMonthDropdown(binding.autoMonth);
        binding.autoMonth.setText(monthLabel(selectedMonth), false);
        binding.autoMonth.setOnItemClickListener((parent, view, position, id) -> {
            selectedMonth = position;
            rebuild();
        });

        // primera carga
        rebuild();
    }

    private void rebuild() {
        // 1) Datos simulados deterministas por (empresa, mes)
        MockData md = generateMockFor(companyName, selectedMonth);

        // 2) Total (mes)
        binding.tvTotalMonth.setText(String.valueOf(md.totalMonth));

        // 3) Barras por día de semana (Lun..Dom)
        LinearProgressIndicator[] bars = new LinearProgressIndicator[]{
                binding.barMon, binding.barTue, binding.barWed,
                binding.barThu, binding.barFri, binding.barSat, binding.barSun
        };
        TextView[] labels = new TextView[]{
                binding.tvMon, binding.tvTue, binding.tvWed,
                binding.tvThu, binding.tvFri, binding.tvSat, binding.tvSun
        };
        int maxDow = 0;
        for (int v : md.byDow) maxDow = Math.max(maxDow, v);
        for (int i = 0; i < 7; i++) {
            int val = md.byDow[i];
            labels[i].setText(String.valueOf(val));
            int pct = (maxDow == 0) ? 0 : Math.round(val * 100f / maxDow);
            bars[i].setProgress(pct);
        }

        // 4) Pie de estados (orden: finalizadas, activas, canceladas)
        binding.pie.setValues(md.finished, md.active, md.cancelled);
        binding.pie.setColors(
                getColor(R.color.legend_finished),   // Finalizadas -> #F1A20B
                getColor(R.color.legend_active),     // Activas     -> #8D9C09
                getColor(R.color.legend_cancelled)   // Canceladas  -> #D20D20
        );
        binding.legendFinished.setText(String.valueOf(md.finished));
        binding.legendActive.setText(String.valueOf(md.active));
        binding.legendCancelled.setText(String.valueOf(md.cancelled));

        // 5) Top-3 tours (barras horizontales con palette)
        int[] topVals = new int[]{ md.top3Values[0], md.top3Values[1], md.top3Values[2] };
        String[] topNames = new String[]{ md.top3Names[0], md.top3Names[1], md.top3Names[2] };
        int maxTop = Math.max(topVals[0], Math.max(topVals[1], topVals[2]));

        // nombres
        binding.tvTop1Name.setText(topNames[0]);
        binding.tvTop2Name.setText(topNames[1]);
        binding.tvTop3Name.setText(topNames[2]);

        // valores
        binding.tvTop1Val.setText(String.valueOf(topVals[0]));
        binding.tvTop2Val.setText(String.valueOf(topVals[1]));
        binding.tvTop3Val.setText(String.valueOf(topVals[2]));

        // progress + colores
        binding.barTop1.setIndicatorColor(c1);
        binding.barTop2.setIndicatorColor(c2);
        binding.barTop3.setIndicatorColor(c3);

        binding.barTop1.setProgress(maxTop == 0 ? 0 : Math.round(topVals[0] * 100f / maxTop));
        binding.barTop2.setProgress(maxTop == 0 ? 0 : Math.round(topVals[1] * 100f / maxTop));
        binding.barTop3.setProgress(maxTop == 0 ? 0 : Math.round(topVals[2] * 100f / maxTop));

        // títulos grandes
        styleSectionTitle(binding.tvSectionDow);
        styleSectionTitle(binding.tvSectionPie);
        styleSectionTitle(binding.tvSectionTop);
    }

    private void styleSectionTitle(TextView tv){
        tv.setTextSize(22f);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
    }

    // ---------- Dropdown Mes ----------
    private void setupMonthDropdown(AutoCompleteTextView view) {
        String[] months = new DateFormatSymbols(new Locale("es")).getMonths();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 12; i++) list.add(capitalize(months[i]));
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, list);
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

    // ---------- Datos mock (deterministas) ----------
    private MockData generateMockFor(String company, int monthIdx) {
        // seed estable por empresa + mes
        long seed = (company == null ? 0 : company.hashCode()) * 31L + monthIdx;
        Random r = new Random(seed);

        MockData m = new MockData();
        // total mes (100–500)
        m.totalMonth = 100 + r.nextInt(401);

        // distribución por día (suma aprox al total, no estricta)
        int left = m.totalMonth;
        for (int i = 0; i < 7; i++) {
            int v = (i == 6) ? Math.max(0, left) : r.nextInt(Math.max(1, m.totalMonth / 3));
            m.byDow[i] = v;
            left -= v;
        }
        // corrige mínimos a cero
        for (int i = 0; i < 7; i++) if (m.byDow[i] < 0) m.byDow[i] = 0;

        // estados (partición simple)
        m.finished  = Math.max(0, (int) Math.round(m.totalMonth * (0.45 + r.nextDouble() * 0.25)));
        m.active    = Math.max(0, (int) Math.round(m.totalMonth * (0.20 + r.nextDouble() * 0.20)));
        m.cancelled = Math.max(0, m.totalMonth - m.finished - m.active);

        // top-3 tours
        m.top3Names[0] = "City Tour";
        m.top3Names[1] = "Montaña 7 Colores";
        m.top3Names[2] = "Lago Titicaca";
        int base = Math.max(1, m.totalMonth / 5);
        m.top3Values[0] = base + r.nextInt(base);         // mayor
        m.top3Values[1] = base - 5 + r.nextInt(base);     // medio
        m.top3Values[2] = base - 10 + r.nextInt(base);    // menor
        // normaliza no negativos
        for (int i = 0; i < 3; i++) if (m.top3Values[i] < 0) m.top3Values[i] = r.nextInt(base);

        // ordena top-3 desc
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

    private int getColor(int resId) {
        return requireContext().getResources().getColor(resId, requireContext().getTheme());
    }

    private static class MockData {
        int totalMonth;
        int[] byDow = new int[7]; // Lun..Dom
        int finished, active, cancelled;
        String[] top3Names = new String[3];
        int[] top3Values = new int[3];
    }
}

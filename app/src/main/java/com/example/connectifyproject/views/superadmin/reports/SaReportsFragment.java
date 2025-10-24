package com.example.connectifyproject.views.superadmin.reports;

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

public class SaReportsFragment extends Fragment {

    private FragmentSaReportsBinding binding;
    private final Map<String, int[]> data = new LinkedHashMap<>();
    private int selectedMonth;
    private String selectedCompany = "Todas";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSaReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        final NavController nav = NavHostFragment.findNavController(this);

        // 🔔 Campanita → Notificaciones (enviamos fromDestId)
        View bell = v.findViewById(R.id.btnNotifications);
        if (bell != null) {
            bell.setOnClickListener(x -> {
                Bundle args = new Bundle();
                args.putInt("fromDestId", nav.getCurrentDestination() != null ? nav.getCurrentDestination().getId() : 0);
                nav.navigate(R.id.saNotificationsFragment, args);
            });
        }

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

    private void rebuildDashboard() {
        List<String> companies = new ArrayList<>(data.keySet());
        if (!"Todas".equals(selectedCompany)) {
            companies.clear();
            companies.add(selectedCompany);
        }

        int total = 0, active = 0, max = 0;
        for (String c : companies) {
            int v = data.get(c)[selectedMonth];
            total += v;
            if (v > 0) active++;
            if (v > max) max = v;
        }

        binding.tvTotalMonth.setText(String.valueOf(total));
        binding.tvActiveCompanies.setText(String.valueOf(active));
        int days = daysInMonth(selectedMonth);
        binding.tvAvgPerDay.setText(days == 0 ? "0" : String.valueOf(Math.round(total / (double) days)));

        binding.listContainer.removeAllViews();
        if (companies.isEmpty() || max == 0) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            return;
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
        }

        LayoutInflater inf = LayoutInflater.from(requireContext());
        for (String c : companies) {
            int value = data.get(c)[selectedMonth];

            View row = inf.inflate(R.layout.item_report_company_bar, binding.listContainer, false);
            TextView tvCompany = row.findViewById(R.id.tvCompany);
            Chip chip = row.findViewById(R.id.chipCount);
            LinearProgressIndicator prog = row.findViewById(R.id.progress);

            tvCompany.setText(c);
            chip.setText(String.valueOf(value));

            int percent = max == 0 ? 0 : Math.round(value * 100f / max);
            if (percent < 2 && value > 0) percent = 2;
            prog.setProgress(percent);

            binding.listContainer.addView(row);
        }
    }

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
        return s.substring(0,1).toUpperCase(new Locale("es")) + s.substring(1);
    }

    private int daysInMonth(int monthIndex) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, monthIndex);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void seedMock() {
        data.put("PeruBus",            new int[]{120,150,160,140,180,210,230,220,190,170,160,200});
        data.put("Inka Express",       new int[]{ 90,110,130,120,140,160,170,165,150,145,140,155});
        data.put("Cusco Shuttle",      new int[]{ 60, 70, 80, 90,110,130,140,150,140,120,110,115});
        data.put("Andes Transit",      new int[]{ 45, 55, 65, 60, 75, 85, 95,100, 90, 80, 70, 75});
        data.put("Altiplano Coaches",  new int[]{ 30, 40, 50, 55, 60, 70, 80, 85, 78, 72, 66, 70});
    }
}

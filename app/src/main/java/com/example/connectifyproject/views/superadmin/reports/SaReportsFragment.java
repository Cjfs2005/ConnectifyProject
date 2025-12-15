package com.example.connectifyproject.views.superadmin.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaReportsBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard de Reportes – SuperAdmin
 * 
 * Muestra datos REALES de Firebase:
 * - Total recaudado en el mes (colección "pagos" con tipoPago="A Empresa")
 * - Empresas activas (colección "usuarios" con rol="Administrador" y habilitado=true)
 * - Promedio recaudación por día
 * - Top 5 empresas por recaudación
 * - Tours completados vs pendientes
 */
public class SaReportsFragment extends Fragment {

    private static final String TAG = "SaReportsFragment";
    
    private FragmentSaReportsBinding binding;
    private SaReportsViewModel viewModel;

    private int selectedMonth;
    private int selectedYear;
    private String selectedCompanyId = "ALL";

    // Paleta para Top-5 (del mayor al menor)
    private static final int[] TOP5_COLORS = new int[]{
            Color.parseColor("#FF9B8F"),
            Color.parseColor("#EF7689"),
            Color.parseColor("#9E6A90"),
            Color.parseColor("#766788"),
            Color.parseColor("#71556B")
    };
    
    // Formateador de moneda
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

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

        // Usar requireActivity() como scope para mantener el ViewModel entre navegaciones
        viewModel = new ViewModelProvider(requireActivity()).get(SaReportsViewModel.class);

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

        // Mes y año actuales por defecto
        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH);
        selectedYear = cal.get(Calendar.YEAR);

        // Restaurar estado si existe
        if (s != null) {
            selectedMonth = s.getInt("month", selectedMonth);
            selectedYear = s.getInt("year", selectedYear);
            selectedCompanyId = s.getString("companyId", "ALL");
        }

        setupMonthDropdown(binding.autoMonth);
        setupCompanyDropdown(binding.autoCompany);

        binding.autoMonth.setText(monthLabel(selectedMonth), false);
        binding.autoCompany.setText("Todas", false);

        binding.autoMonth.setOnItemClickListener((parent, view, position, id) -> {
            selectedMonth = position;
            loadData();
        });

        binding.autoCompany.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedCompanyId = "ALL";
            } else {
                List<CompanyStat> companies = viewModel.companies.getValue();
                if (companies != null && position - 1 < companies.size()) {
                    selectedCompanyId = companies.get(position - 1).companyId;
                }
            }
            viewModel.applyCompanyFilter(selectedCompanyId);
            rebuildDashboard();
        });

        // Observar cambios del ViewModel
        observeViewModel();

        // Cargar datos: verificar si ya hay datos del mismo mes/año, si no, cargar
        checkAndLoadData();
    }
    
    /**
     * Verifica si los datos actuales del ViewModel corresponden al mes/año seleccionado.
     * Si ya existen datos válidos, solo reconstruye la UI; si no, carga desde Firebase.
     */
    private void checkAndLoadData() {
        MonthFilter currentVmMonth = viewModel.selectedMonth;
        int currentVmYear = viewModel.selectedYear;
        MonthFilter targetMonth = MonthFilter.fromNumber(selectedMonth + 1);
        
        Boolean dataReady = viewModel.dataReady.getValue();
        
        // Si ya tenemos datos para el mismo mes/año, solo reconstruir UI
        if (Boolean.TRUE.equals(dataReady) 
                && currentVmMonth == targetMonth 
                && currentVmYear == selectedYear) {
            Log.d(TAG, "Datos ya disponibles para " + targetMonth + "/" + selectedYear + ", reconstruyendo UI");
            updateCompanyDropdown(viewModel.companies.getValue());
            rebuildDashboard();
        } else {
            // Cargar datos nuevos
            Log.d(TAG, "Cargando datos nuevos para " + targetMonth + "/" + selectedYear);
            loadData();
        }
    }
    
    private void observeViewModel() {
        // Observer principal: solo reconstruir UI cuando TODOS los datos estén listos
        viewModel.dataReady.observe(getViewLifecycleOwner(), ready -> {
            Log.d(TAG, "dataReady changed: " + ready);
            if (Boolean.TRUE.equals(ready)) {
                updateCompanyDropdown(viewModel.companies.getValue());
                rebuildDashboard();
            }
        });
        
        // Observer de loading para mostrar/ocultar indicador de carga
        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            Log.d(TAG, "isLoading changed: " + loading);
            if (Boolean.TRUE.equals(loading)) {
                // Mostrar estado de carga
                binding.tvEmpty.setText("Cargando datos...");
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.listContainer.removeAllViews();
            }
        });
    }
    
    private void loadData() {
        MonthFilter month = MonthFilter.fromNumber(selectedMonth + 1);
        if (month == null) month = MonthFilter.DEC;
        
        Log.d(TAG, "Cargando datos para " + month + "/" + selectedYear);
        viewModel.load(selectedYear, month);
    }
    
    private void updateCompanyDropdown(List<CompanyStat> companies) {
        if (companies == null) return;
        
        List<String> items = new ArrayList<>();
        items.add("Todas");
        for (CompanyStat stat : companies) {
            items.add(stat.name);
        }
        binding.autoCompany.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, items));
    }

    /** Reconstruye KPIs + Top-5 (si aplica) + lista completa con datos reales de Firebase. */
    private void rebuildDashboard() {
        Log.d(TAG, "rebuildDashboard() llamado");
        
        ReportsSummary summary = viewModel.summary.getValue();
        List<CompanyStat> companies = viewModel.companies.getValue();
        List<CompanyStat> top5 = viewModel.top5.getValue();
        
        Log.d(TAG, "Summary: " + (summary != null) + ", Companies: " + (companies != null ? companies.size() : 0) + ", Top5: " + (top5 != null ? top5.size() : 0));
        
        if (summary == null) {
            binding.tvEmpty.setText("Sin datos disponibles");
            binding.tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        
        // KPIs
        binding.tvTotalMonth.setText(formatCurrency(summary.totalMes));
        binding.tvActiveCompanies.setText(String.valueOf(summary.empresasActivas));
        binding.tvAvgPerDay.setText(formatCurrency((int) summary.promedioDia));

        // Reset contenedor
        binding.listContainer.removeAllViews();

        boolean hayDatos = summary.totalMes > 0 || summary.empresasActivas > 0;
        binding.tvEmpty.setVisibility(hayDatos ? View.GONE : View.VISIBLE);
        if (!hayDatos) {
            binding.tvEmpty.setText("No hay datos para este mes");
        }
        
        LayoutInflater inf = LayoutInflater.from(requireContext());

        // Mostrar info de tours
        binding.listContainer.addView(makeSectionTitle(inf, 
                "Tours del mes — " + monthLabel(selectedMonth), 20f));
        
        TextView tvTours = new TextView(requireContext());
        tvTours.setText("✅ Completados: " + summary.toursCompletados + "  |  ⏳ Pendientes: " + summary.toursPendientes);
        tvTours.setPadding(0, 8, 0, 16);
        binding.listContainer.addView(tvTours);

        if ("ALL".equals(selectedCompanyId) && top5 != null && !top5.isEmpty()) {
            // ---------- Top-5 empresas por recaudación ----------
            binding.listContainer.addView(makeSectionTitle(inf,
                    "Top 5 empresas por recaudación — " + monthLabel(selectedMonth), 25f));

            int maxTop = 0;
            for (CompanyStat stat : top5) {
                if (stat.monthTotal > maxTop) maxTop = stat.monthTotal;
            }

            for (int i = 0; i < top5.size(); i++) {
                CompanyStat stat = top5.get(i);
                View row = inf.inflate(R.layout.sa_item_company_bar, binding.listContainer, false);

                TextView tvCompany = row.findViewById(R.id.tvCompany);
                Chip chip = row.findViewById(R.id.chipCount);
                LinearProgressIndicator prog = row.findViewById(R.id.progress);

                tvCompany.setText(stat.name);
                chip.setText(formatCurrency(stat.monthTotal));

                int percent = maxTop == 0 ? 0 : Math.round(stat.monthTotal * 100f / maxTop);
                if (percent < 2 && stat.monthTotal > 0) percent = 2;
                prog.setProgress(percent);

                int color = TOP5_COLORS[Math.min(i, TOP5_COLORS.length - 1)];
                prog.setIndicatorColor(color);
                prog.setTrackColor(Color.parseColor("#E6E6E6"));

                final String companyName = stat.name;
                row.setOnClickListener(v -> navigateToCompanyReport(companyName));

                binding.listContainer.addView(row);
            }

            // ---------- Lista completa de empresas ----------
            if (companies != null && !companies.isEmpty()) {
                binding.listContainer.addView(makeSectionTitle(inf, "Todas las empresas", 25f));

                for (CompanyStat stat : companies) {
                    View row = inf.inflate(R.layout.sa_item_company_row, binding.listContainer, false);
                    TextView tvName = row.findViewById(R.id.tvName);
                    TextView tvBadge = row.findViewById(R.id.tvBadge);

                    tvName.setText(stat.name);
                    tvBadge.setText(formatCurrency(stat.monthTotal));

                    final String companyName = stat.name;
                    row.setOnClickListener(v -> navigateToCompanyReport(companyName));

                    binding.listContainer.addView(row);
                }
            }

        } else if (companies != null) {
            // Empresa específica seleccionada
            CompanyStat selectedStat = null;
            for (CompanyStat stat : companies) {
                if (stat.companyId.equals(selectedCompanyId)) {
                    selectedStat = stat;
                    break;
                }
            }
            
            if (selectedStat != null) {
                binding.listContainer.addView(makeSectionTitle(inf, selectedStat.name, 25f));

                View row = inf.inflate(R.layout.sa_item_company_row, binding.listContainer, false);
                ((TextView) row.findViewById(R.id.tvName)).setText(selectedStat.name);
                ((TextView) row.findViewById(R.id.tvBadge)).setText(formatCurrency(selectedStat.monthTotal));

                final String companyName = selectedStat.name;
                row.setOnClickListener(v -> navigateToCompanyReport(companyName));

                binding.listContainer.addView(row);
            }
        }
    }
    
    private String formatCurrency(int amount) {
        return "S/." + String.format(new Locale("es", "PE"), "%,d", amount);
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
        outState.putInt("year", selectedYear);
        outState.putString("companyId", selectedCompanyId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.connectifyproject.views.superadmin.reports;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaReportsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class SaReportsFragment extends Fragment {

    private FragmentSaReportsBinding binding;
    private SaReportsAdapter adapter;
    private boolean sortAsc = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSaReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recycler
        binding.rvCompanies.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SaReportsAdapter(mockCompanies(), item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Descargar reporte")
                    .setMessage("¿Descargar reporte de reservas de \"" + item.name + "\"?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Descargar", (d, w) ->
                            Snackbar.make(binding.getRoot(),
                                    "Descarga iniciada: " + item.name,
                                    Snackbar.LENGTH_LONG).show())
                    .show();
        });
        binding.rvCompanies.setAdapter(adapter);

        // Restaurar
        if (savedInstanceState != null) {
            sortAsc = savedInstanceState.getBoolean("sortAsc", true);
            adapter.setAsc(sortAsc);
            String q = savedInstanceState.getString("q", "");
            binding.etSearch.setText(q);
            adapter.setQuery(q);
        }

        // Buscar
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                adapter.setQuery(s == null ? "" : s.toString());
            }
        });

        // Orden
        binding.btnSort.setOnClickListener(this::showSortPopup);
    }

    private void showSortPopup(View anchor) {
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenuInflater().inflate(R.menu.menu_reports_sort, pm.getMenu());
        // Estado actual
        pm.getMenu().findItem(R.id.sort_asc).setChecked(sortAsc);
        pm.getMenu().findItem(R.id.sort_desc).setChecked(!sortAsc);
        pm.setOnMenuItemClickListener(this::onSortItem);
        pm.show();
    }

    private boolean onSortItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_asc) {
            sortAsc = true;
        } else if (id == R.id.sort_desc) {
            sortAsc = false;
        } else {
            return false;
        }
        adapter.setAsc(sortAsc);
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("sortAsc", sortAsc);
        out.putString("q", binding.etSearch.getText() == null ? "" : binding.etSearch.getText().toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --------- Mock ----------
    private List<SaReportsAdapter.CompanyItem> mockCompanies() {
        String[] names = new String[] {
                "Andes Adventure Perú",
                "Aventura Inca Tours",
                "Camino del Sol Travel",
                "Cusco Mágico Expeditions",
                "Descubre Perú Tours",
                "Explora Andina",
                "Inka Dreams Travel",
                "Tierra de los Incas Tours",
                "Viajes Pachamama"
        };
        List<SaReportsAdapter.CompanyItem> list = new ArrayList<>();
        for (String n : names) list.add(new SaReportsAdapter.CompanyItem(n, 0));
        return list;
    }
}

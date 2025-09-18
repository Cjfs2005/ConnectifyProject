package com.example.connectifyproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.GuiaFragmentToursBinding;
import com.example.connectifyproject.model.GuiaTourHistory;
import com.example.connectifyproject.ui.guia.GuiaTourHistoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class GuiaToursFragment extends Fragment {
    private GuiaFragmentToursBinding binding;
    private GuiaTourHistoryAdapter adapter;
    private List<GuiaTourHistory> tours = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = GuiaFragmentToursBinding.inflate(inflater, container, false);
        loadData();  // Hardcoded; cambia a loadFromDB() para NoSQL

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GuiaTourHistoryAdapter(requireContext(), tours);
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    private void loadData() {
        // Hardcoded, fecha actual 17 Sep 2025
        tours.clear();
        tours.add(new GuiaTourHistory("6909454317", "Tour pendiente", "17 Sep 2025 11:21 AM", "Pendiente", ""));
        tours.add(new GuiaTourHistory("5649257490", "Guía turística por Lima", "17 Sep 2025 10:34 AM", "Realizado", "★★★★★ Confirmado"));
        tours.add(new GuiaTourHistory("9796542786", "Tour cancelado", "15 Sep 2025 10:11 AM", "Cancelado", ""));
        if (adapter != null) adapter.updateList(tours);
    }

    // Placeholder para NoSQL
    private void loadFromDB() {
        // TODO: Similar a GuiaPagosFragment, query "tourHistory" collection
        // adapter.updateList(tours);  // Map to GuiaTourHistory
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuiaToursFragment extends Fragment {
    private GuiaFragmentToursBinding binding;
    private GuiaTourHistoryAdapter adapter;
    private List<GuiaTourHistory> tours = new ArrayList<>();
    private FirebaseFirestore db;
    private String guiaId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = GuiaFragmentToursBinding.inflate(inflater, container, false);
        
        db = FirebaseFirestore.getInstance();
        guiaId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                 FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        loadFromDB();  // ✅ Cambiar a dinámico desde Firebase

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GuiaTourHistoryAdapter(requireContext(), tours);
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    private void loadData() {
        // Método legacy - mantener por compatibilidad
        tours.clear();
        tours.add(new GuiaTourHistory("6909454317", "Tour pendiente", "17 Sep 2025 11:21 AM", "Pendiente", ""));
        tours.add(new GuiaTourHistory("5649257490", "Guía turística por Lima", "17 Sep 2025 10:34 AM", "Realizado", "★★★★★ Confirmado"));
        tours.add(new GuiaTourHistory("9796542786", "Tour cancelado", "15 Sep 2025 10:11 AM", "Cancelado", ""));
        if (adapter != null) adapter.updateList(tours);
    }

    // ✅ Cargar desde tours_completados
    private void loadFromDB() {
        if (guiaId == null) {
            loadData(); // Fallback a datos hardcodeados
            return;
        }
        
        db.collection("tours_completados")
            .whereEqualTo("guiaId", guiaId)
            .orderBy("fechaRegistro", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    android.util.Log.e("GuiaToursFragment", "Error cargando historial: " + error.getMessage());
                    return;
                }
                
                if (querySnapshot != null) {
                    tours.clear();
                    
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String titulo = doc.getString("titulo");
                        Timestamp fechaRegistro = doc.getTimestamp("fechaRegistro");
                        String duracionReal = doc.getString("duracionReal");
                        Long numParticipantes = doc.getLong("numeroParticipantes");
                        Double pagoGuia = doc.getDouble("pagoGuia");
                        String empresaNombre = doc.getString("empresaNombre");
                        
                        String fecha = fechaRegistro != null ? 
                            dateFormat.format(fechaRegistro.toDate()) : "Sin fecha";
                        
                        String detalles = "";
                        if (numParticipantes != null) {
                            detalles += numParticipantes + " participantes";
                        }
                        if (duracionReal != null) {
                            detalles += (detalles.isEmpty() ? "" : " • ") + duracionReal;
                        }
                        if (pagoGuia != null) {
                            detalles += (detalles.isEmpty() ? "" : " • ") + String.format("S/ %.2f", pagoGuia);
                        }
                        
                        tours.add(new GuiaTourHistory(
                            doc.getId(),
                            titulo != null ? titulo : "Tour sin título",
                            fecha,
                            empresaNombre != null ? empresaNombre : "Empresa",
                            detalles
                        ));
                    }
                    
                    if (adapter != null) {
                        adapter.updateList(tours);
                    }
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
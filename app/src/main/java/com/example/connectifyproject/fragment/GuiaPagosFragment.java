package com.example.connectifyproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.GuiaFragmentPagosBinding;
import com.example.connectifyproject.model.GuiaPayment;
import com.example.connectifyproject.ui.guia.GuiaPaymentAdapter;

import java.util.ArrayList;
import java.util.List;

public class GuiaPagosFragment extends Fragment {
    private GuiaFragmentPagosBinding binding;
    private GuiaPaymentAdapter adapter;
    private List<GuiaPayment> payments = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = GuiaFragmentPagosBinding.inflate(inflater, container, false);
        loadData();  // Hardcoded; cambia a loadFromDB() para NoSQL

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GuiaPaymentAdapter(requireContext(), payments);
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    private void loadData() {
        // Hardcoded del mockup, fecha actual 17 Sep 2025
        payments.clear();
        payments.add(new GuiaPayment("6909454317", 350.00, "17 Sep 2025 11:21 AM", "Pendiente", "Transferencia"));
        payments.add(new GuiaPayment("5649257490", 100.00, "17 Sep 2025 10:34 AM", "Realizado", "ABC Bank ATM"));
        payments.add(new GuiaPayment("6909454317", 350.00, "17 Sep 2025 11:21 AM", "Pendiente", "Transferencia"));
        if (adapter != null) adapter.updateList(payments);
    }

    // Placeholder para NoSQL (ej. Firestore)
    private void loadFromDB() {
        // TODO: Ej. FirebaseFirestore db = FirebaseFirestore.getInstance();
        // db.collection("payments").whereEqualTo("userId", "currentUser").get().addOnCompleteListener(task -> {
        //     if (task.isSuccessful()) {
        //         payments.clear();
        //         for (QueryDocumentSnapshot doc : task.getResult()) {
        //             payments.add(new GuiaPayment(doc.getString("id"), doc.getDouble("amount"), doc.getString("date"), doc.getString("status"), doc.getString("method")));
        //         }
        //         adapter.updateList(payments);
        //     }
        // });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.AdminHistorialPagosViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;

import java.util.ArrayList;
import java.util.List;

public class admin_historial_pagos extends AppCompatActivity {
    private AdminHistorialPagosViewBinding binding;
    private HistorialAdapter historialAdapter;
    private List<TransaccionItem> transaccionesList;
    private String clienteNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminHistorialPagosViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener nombre del cliente desde Intent
        clienteNombre = getIntent().getStringExtra("cliente_nombre");
        if (clienteNombre == null) {
            clienteNombre = "Cliente";
        }

        // Configurar toolbar
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar datos de prueba
        setupTestData();

        // Configurar bottom navigation
        setupBottomNavigation();

        // Configurar botón exportar
        binding.btnExportar.setOnClickListener(v -> {
            Toast.makeText(this, "Exportando historial de " + clienteNombre + "...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        transaccionesList = new ArrayList<>();
        historialAdapter = new HistorialAdapter(transaccionesList);
        binding.recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistorial.setAdapter(historialAdapter);
    }

    private void setupTestData() {
        // Datos de prueba estáticos para el historial
        transaccionesList.add(new TransaccionItem("Exitoso", "1234567890", "$100.00", true));
        transaccionesList.add(new TransaccionItem("Fallido", "9876543210", "$50.00", false));
        transaccionesList.add(new TransaccionItem("Exitoso", "1122334455", "$75.00", true));
        transaccionesList.add(new TransaccionItem("Exitoso", "5544332211", "$120.00", true));
        transaccionesList.add(new TransaccionItem("Fallido", "6677889900", "$80.00", false));
        
        historialAdapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("pagos");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }

    // Clase interna para los datos de transacción
    public static class TransaccionItem {
        private String estado;
        private String id;
        private String monto;
        private boolean exitoso;

        public TransaccionItem(String estado, String id, String monto, boolean exitoso) {
            this.estado = estado;
            this.id = id;
            this.monto = monto;
            this.exitoso = exitoso;
        }

        // Getters
        public String getEstado() { return estado; }
        public String getId() { return id; }
        public String getMonto() { return monto; }
        public boolean isExitoso() { return exitoso; }
    }

    // Adapter para RecyclerView del historial
    private class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {
        private List<TransaccionItem> transacciones;

        public HistorialAdapter(List<TransaccionItem> transacciones) {
            this.transacciones = transacciones;
        }

        @NonNull
        @Override
        public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaccion, parent, false);
            return new HistorialViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
            TransaccionItem transaccion = transacciones.get(position);
            holder.bind(transaccion);
        }

        @Override
        public int getItemCount() {
            return transacciones.size();
        }

        class HistorialViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivEstado;
            private TextView tvEstado;
            private TextView tvId;
            private TextView tvMonto;

            public HistorialViewHolder(@NonNull View itemView) {
                super(itemView);
                ivEstado = itemView.findViewById(R.id.iv_estado);
                tvEstado = itemView.findViewById(R.id.tv_estado);
                tvId = itemView.findViewById(R.id.tv_id);
                tvMonto = itemView.findViewById(R.id.tv_monto);
            }

            public void bind(TransaccionItem transaccion) {
                tvEstado.setText(transaccion.getEstado());
                tvId.setText("ID: " + transaccion.getId());
                tvMonto.setText(transaccion.getMonto());

                // Configurar icono y colores según el estado
                if (transaccion.isExitoso()) {
                    ivEstado.setImageResource(R.drawable.ic_check_circle);
                    ivEstado.setColorFilter(getColor(R.color.success_500));
                    tvEstado.setTextColor(getColor(R.color.success_500));
                } else {
                    ivEstado.setImageResource(R.drawable.ic_cancel);
                    ivEstado.setColorFilter(getColor(R.color.error_500));
                    tvEstado.setTextColor(getColor(R.color.error_500));
                }
            }
        }
    }
}

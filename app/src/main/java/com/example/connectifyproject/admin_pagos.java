package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.AdminPagosViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;

import java.util.ArrayList;
import java.util.List;

public class admin_pagos extends AppCompatActivity {
    private AdminPagosViewBinding binding;
    private PagosAdapter pagosAdapter;
    private List<PagoItem> pagosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminPagosViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar datos de prueba
        setupTestData();

        // Configurar bottom navigation
        setupBottomNavigation();
    }

    private void setupRecyclerView() {
        pagosList = new ArrayList<>();
        pagosAdapter = new PagosAdapter(pagosList);
        binding.recyclerViewPagos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewPagos.setAdapter(pagosAdapter);
    }

    private void setupTestData() {
        // Datos de prueba estáticos
        pagosList.add(new PagoItem("Alex", "10:30 AM", R.drawable.ic_avatar_male_1, true));
        pagosList.add(new PagoItem("Sara", "11:45 AM", R.drawable.ic_avatar_female_1, true));
        pagosList.add(new PagoItem("David", "12:15 PM", R.drawable.ic_avatar_male_2, true));
        pagosList.add(new PagoItem("Emily", "1:30 PM", R.drawable.ic_avatar_female_2, true));
        pagosList.add(new PagoItem("Maxwell", "2:45 PM", R.drawable.ic_avatar_male_3, true));
        pagosList.add(new PagoItem("Jessica", "3:15 PM", R.drawable.ic_avatar_female_3, true));
        
        pagosAdapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("pagos");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }

    // Clase interna para los datos de pago
    public static class PagoItem {
        private String nombre;
        private String hora;
        private int avatarResource;
        private boolean conectado;

        public PagoItem(String nombre, String hora, int avatarResource, boolean conectado) {
            this.nombre = nombre;
            this.hora = hora;
            this.avatarResource = avatarResource;
            this.conectado = conectado;
        }

        // Getters
        public String getNombre() { return nombre; }
        public String getHora() { return hora; }
        public int getAvatarResource() { return avatarResource; }
        public boolean isConectado() { return conectado; }
    }

    // Adapter para RecyclerView
    private class PagosAdapter extends RecyclerView.Adapter<PagosAdapter.PagoViewHolder> {
        private List<PagoItem> pagos;

        public PagosAdapter(List<PagoItem> pagos) {
            this.pagos = pagos;
        }

        @NonNull
        @Override
        public PagoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pago_cliente, parent, false);
            return new PagoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PagoViewHolder holder, int position) {
            PagoItem pago = pagos.get(position);
            holder.bind(pago);
        }

        @Override
        public int getItemCount() {
            return pagos.size();
        }

        class PagoViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivAvatar;
            private TextView tvNombre;
            private TextView tvHora;
            private TextView tvEstado;

            public PagoViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.ivAvatar);
                tvNombre = itemView.findViewById(R.id.tvClientName);
                tvHora = itemView.findViewById(R.id.tvPaymentTime);
                tvEstado = itemView.findViewById(R.id.tvPaymentStatus);
            }

            public void bind(PagoItem pago) {
                ivAvatar.setImageResource(pago.getAvatarResource());
                tvNombre.setText("Cliente: " + pago.getNombre());
                tvHora.setText(pago.getHora());
                tvEstado.setText("Pago completado");

                // Click listener para ir al historial de pagos
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(admin_pagos.this, admin_historial_pagos.class);
                    intent.putExtra("cliente_nombre", pago.getNombre());
                    startActivity(intent);
                });
            }
        }
    }
}
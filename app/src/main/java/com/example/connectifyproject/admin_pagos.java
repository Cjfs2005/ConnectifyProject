package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.example.connectifyproject.databinding.AdminPagosViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;

import java.util.ArrayList;
import java.util.List;

public class admin_pagos extends AppCompatActivity {
    private AdminPagosViewBinding binding;
    private PagosAdapter pagosAdapter;
    private List<PagoItem> pagosList;
    private List<PagoItem> filteredPagosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminPagosViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Configurar botón de notificaciones
        setupNotifications();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar búsqueda
        setupSearch();

        // Configurar datos de prueba
        setupTestData();

        // Configurar bottom navigation
        setupBottomNavigation();
    }

    private void setupNotifications() {
        binding.btnNotifications.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_admin_notifications, popup.getMenu());
            popup.setOnMenuItemClickListener(this::onNotificationAction);
            popup.show();
        });
    }

    private boolean onNotificationAction(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_mark_all_read) {
            Toast.makeText(this, "Todas las notificaciones marcadas como leídas", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_preferences) {
            Toast.makeText(this, "Configuración de notificaciones", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_view_all) {
            Toast.makeText(this, "Ver todas las notificaciones", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void setupRecyclerView() {
        pagosList = new ArrayList<>();
        filteredPagosList = new ArrayList<>();
        pagosAdapter = new PagosAdapter(filteredPagosList);
        binding.recyclerViewPagos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewPagos.setAdapter(pagosAdapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPagos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterPagos(String query) {
        filteredPagosList.clear();
        if (query.isEmpty()) {
            filteredPagosList.addAll(pagosList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (PagoItem pago : pagosList) {
                if (pago.getNombre().toLowerCase().contains(lowerCaseQuery) ||
                    pago.getHora().toLowerCase().contains(lowerCaseQuery)) {
                    filteredPagosList.add(pago);
                }
            }
        }
        pagosAdapter.notifyDataSetChanged();
    }

    private void setupTestData() {
        // Datos de prueba estáticos
        pagosList.add(new PagoItem("Alex", "10:30 AM", R.drawable.ic_avatar_male_1, true));
        pagosList.add(new PagoItem("Sara", "11:45 AM", R.drawable.ic_avatar_female_1, true));
        pagosList.add(new PagoItem("David", "12:15 PM", R.drawable.ic_avatar_male_2, true));
        pagosList.add(new PagoItem("Emily", "1:30 PM", R.drawable.ic_avatar_female_2, true));
        pagosList.add(new PagoItem("Maxwell", "2:45 PM", R.drawable.ic_avatar_male_3, true));
        pagosList.add(new PagoItem("Jessica", "3:15 PM", R.drawable.ic_avatar_female_3, true));
        
        // Inicializar lista filtrada
        filteredPagosList.addAll(pagosList);
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
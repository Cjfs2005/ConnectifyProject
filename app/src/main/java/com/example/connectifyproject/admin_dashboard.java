package com.example.connectifyproject;

import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.AdminDashboardViewBinding;
import com.example.connectifyproject.models.DashboardSummary;
import com.example.connectifyproject.ui.ServiceSalesAdapter;
import com.example.connectifyproject.viewmodel.AdminDashboardViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;

public class admin_dashboard extends AppCompatActivity {

    private AdminDashboardViewBinding binding;
    private AdminDashboardViewModel viewModel;
    private final ServiceSalesAdapter adapter = new ServiceSalesAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminDashboardViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        View anchor = binding.cardHeader.findViewById(R.id.ivNotifications);
        anchor.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_admin_notifications, popup.getMenu());
            popup.setOnMenuItemClickListener(this::onNotificationAction);
            popup.show();
        });

        binding.rvServiceSales.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServiceSales.setAdapter(adapter);

        binding.bottomNav.setOnItemSelectedListener(navListener);
        binding.bottomNav.setSelectedItemId(R.id.nav_dashboard);

        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);
        viewModel.getSummary().observe(this, this::bindSummary);
        viewModel.getServiceSales().observe(this, adapter::submitList);

        viewModel.loadData();
    }

    private boolean onNotificationAction(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_mark_all_read) {
            binding.tvBadge.setVisibility(View.GONE);
            return true;
        } else if (id == R.id.action_preferences) {
            return true;
        } else if (id == R.id.action_view_all) {
            return true;
        }
        return false;
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) return true;
        if (id == R.id.nav_tours) {
            startActivity(new android.content.Intent(this, admin_tours.class));
            return true;
        }
        if (id == R.id.nav_chat) {
            startActivity(new android.content.Intent(this, admin_chat.class));
            return true;
        }
        if (id == R.id.nav_pagos) {
            startActivity(new android.content.Intent(this, admin_pagos.class));
            return true;
        }
        if (id == R.id.nav_perfil) {
            startActivity(new android.content.Intent(this, admin_perfil.class));
            return true;
        }
        return false;
    };

    private void bindSummary(DashboardSummary s) {
        if (s == null) return;

        binding.tvWelcomeName.setText(getString(R.string.welcome_name, s.getNombre()));
        binding.tvWelcomeCompany.setText(getString(R.string.welcome_company));
        binding.tvBadge.setText(String.valueOf(s.getNotificaciones()));
        binding.tvBadge.setVisibility(s.getNotificaciones() > 0 ? View.VISIBLE : View.GONE);

        setKpi(binding.kpiToursEnCurso, R.id.ivKpiIcon1, R.id.tvKpiValue1, R.id.tvKpiLabel1,
                R.drawable.ic_bus, String.valueOf(s.getToursEnCurso()), getString(R.string.kpi_tours_en_curso));

        setKpi(binding.kpiProximosTours, R.id.ivKpiIcon2, R.id.tvKpiValue2, R.id.tvKpiLabel2,
                R.drawable.ic_calendar, String.valueOf(s.getProximosTours()), getString(R.string.kpi_proximos_tours));

        setKpi(binding.kpiVentasTotales, R.id.ivKpiIcon3, R.id.tvKpiValue3, R.id.tvKpiLabel3,
                R.drawable.ic_money, "$" + s.getVentasTotales(), getString(R.string.kpi_ventas_totales));

        setKpi(binding.kpiVentasTours, R.id.ivKpiIcon4, R.id.tvKpiValue4, R.id.tvKpiLabel4,
                R.drawable.ic_money, "$" + s.getVentasTours(), getString(R.string.kpi_ventas_tours));
    }

    private void setKpi(MaterialCardView card, int iconId, int valueId, int labelId,
                        int iconRes, String value, String label) {
        if (card == null) return;
        android.widget.ImageView icon = card.findViewById(iconId);
        android.widget.TextView tvValue = card.findViewById(valueId);
        android.widget.TextView tvLabel = card.findViewById(labelId);
        icon.setImageResource(iconRes);
        tvValue.setText(value);
        tvLabel.setText(label);
    }
}
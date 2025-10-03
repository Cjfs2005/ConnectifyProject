package com.example.connectifyproject;

import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.AdminDashboardViewBinding;
import com.example.connectifyproject.models.DashboardSummary;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.example.connectifyproject.views.ServiceSalesAdapter;
import com.example.connectifyproject.viewmodel.AdminDashboardViewModel;
import com.google.android.material.card.MaterialCardView;

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

        View anchor = binding.ivNotification;
        anchor.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_admin_notifications, popup.getMenu());
            popup.setOnMenuItemClickListener(this::onNotificationAction);
            popup.show();
        });

        binding.recyclerServiceSales.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerServiceSales.setAdapter(adapter);

        // Agregar el Fragment de navegación inferior
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("dashboard");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();

        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);
        viewModel.getSummary().observe(this, this::bindSummary);
        viewModel.getServiceSales().observe(this, adapter::submitList);

        viewModel.loadData();
    }

    private boolean onNotificationAction(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_mark_all_read) {
            // Ya no hay badge visible, solo mostramos un mensaje
            return true;
        } else if (id == R.id.action_preferences) {
            return true;
        } else if (id == R.id.action_view_all) {
            return true;
        }
        return false;
    }

    private void bindSummary(DashboardSummary s) {
        if (s == null) return;
        // El layout ahora es estático con valores fijos
        // Los datos reales se pueden mostrar en el RecyclerView de ventas por servicio
    }

}
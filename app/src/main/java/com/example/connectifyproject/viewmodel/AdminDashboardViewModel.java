package com.example.connectifyproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.connectifyproject.models.DashboardSummary;
import com.example.connectifyproject.models.ServiceSale;
import com.example.connectifyproject.repository.DashboardRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminDashboardViewModel extends ViewModel {
    private final DashboardRepository repo = new DashboardRepository();

    private final MutableLiveData<DashboardSummary> summary = new MutableLiveData<>();
    private final MutableLiveData<List<ServiceSale>> serviceSales = new MutableLiveData<>();

    public LiveData<DashboardSummary> getSummary() { return summary; }
    public LiveData<List<ServiceSale>> getServiceSales() { return serviceSales; }

    public void loadData() {
        // Cargar datos de forma sincrónica para evitar problemas
        try {
            DashboardSummary summaryData = new DashboardSummary(
                    3,   // tours en curso
                    5,   // próximos tours
                    4500, // ventas totales
                    3200, // ventas tours
                    12,   // notificaciones
                    "Admin"    // nombre
            );
            summary.setValue(summaryData);
            
            List<ServiceSale> salesData = repo.fetchServiceSalesSync();
            serviceSales.setValue(salesData);
        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error, establecer valores por defecto
            summary.setValue(null);
            serviceSales.setValue(new java.util.ArrayList<>());
        }
    }
}
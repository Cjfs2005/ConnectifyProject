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
        CompletableFuture<DashboardSummary> s = repo.fetchSummaryAsync();
        CompletableFuture<List<ServiceSale>> l = repo.fetchServiceSalesAsync();

        s.thenAccept(summary::postValue);
        l.thenAccept(serviceSales::postValue);
    }
}
package com.example.connectifyproject.repository;

import com.example.connectifyproject.models.DashboardSummary;
import com.example.connectifyproject.models.ServiceSale;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DashboardRepository {

    public CompletableFuture<DashboardSummary> fetchSummaryAsync() {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(300);
            return new DashboardSummary(
                    3,   // tours en curso
                    5,   // próximos tours
                    4500,
                    2500,
                    3,
                    "Tony"
            );
        }, Executors.newCachedThreadPool());
    }

    public CompletableFuture<List<ServiceSale>> fetchServiceSalesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(400);
            List<ServiceSale> list = new ArrayList<>();
            list.add(new ServiceSale("Paquetes de comida", 400));
            list.add(new ServiceSale("Traslado al hotel", 380));
            list.add(new ServiceSale("Servicio de fotografía", 380));
            list.add(new ServiceSale("Souvenir exclusivo", 350));
            list.add(new ServiceSale("Transporte ecológico", 260));
            list.add(new ServiceSale("Guía bilingüe", 200));
            list.add(new ServiceSale("Entradas a atracciones", 100));
            list.add(new ServiceSale("Seguro de viaje", 90));
            return list;
        }, Executors.newCachedThreadPool());
    }

    private void simulateDelay(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
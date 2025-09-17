package com.example.connectifyproject.models;

public class ServiceSale {
    private final String serviceName;
    private final int amount;

    public ServiceSale(String serviceName, int amount) {
        this.serviceName = serviceName;
        this.amount = amount;
    }

    public String getServiceName() { return serviceName; }
    public int getAmount() { return amount; }
}
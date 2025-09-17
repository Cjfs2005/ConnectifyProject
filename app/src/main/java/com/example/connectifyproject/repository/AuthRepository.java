package com.example.connectifyproject.repository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthRepository {
    public CompletableFuture<Boolean> loginAsync(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(600); // Simula I/O en background
            return email != null && email.contains("@") && password != null && password.length() >= 4;
        }, Executors.newCachedThreadPool());
    }

    private void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
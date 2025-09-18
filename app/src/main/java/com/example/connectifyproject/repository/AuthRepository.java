package com.example.connectifyproject.repository;

import com.example.connectifyproject.model.LoginResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthRepository {
    
    // Credenciales hardcodeadas para cada tipo de usuario
    private static final String SUPERADMIN_EMAIL = "superadmin@connectify.com";
    private static final String SUPERADMIN_PASSWORD = "super123";
    
    private static final String ADMIN_EMAIL = "admin@connectify.com";
    private static final String ADMIN_PASSWORD = "admin123";
    
    private static final String CLIENTE_EMAIL = "cliente@connectify.com";
    private static final String CLIENTE_PASSWORD = "cliente123";
    
    private static final String GUIA_EMAIL = "guia@connectify.com";
    private static final String GUIA_PASSWORD = "guia123";

    public CompletableFuture<LoginResult> loginAsync(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(600); // Simula I/O en background
            
            if (email == null || password == null) {
                return new LoginResult(false, null);
            }
            
            // Validar credenciales específicas por rol
            if (SUPERADMIN_EMAIL.equals(email) && SUPERADMIN_PASSWORD.equals(password)) {
                return new LoginResult(true, LoginResult.UserType.SUPERADMIN);
            } else if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
                return new LoginResult(true, LoginResult.UserType.ADMIN);
            } else if (CLIENTE_EMAIL.equals(email) && CLIENTE_PASSWORD.equals(password)) {
                return new LoginResult(true, LoginResult.UserType.CLIENTE);
            } else if (GUIA_EMAIL.equals(email) && GUIA_PASSWORD.equals(password)) {
                return new LoginResult(true, LoginResult.UserType.GUIA);
            }
            
            return new LoginResult(false, null);
        }, Executors.newCachedThreadPool());
    }

    private void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
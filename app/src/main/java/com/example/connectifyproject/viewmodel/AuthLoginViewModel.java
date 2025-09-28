package com.example.connectifyproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.connectifyproject.model.LoginResult;
import com.example.connectifyproject.repository.AuthRepository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class AuthLoginViewModel extends ViewModel {

    private final AuthRepository repo = new AuthRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>(null);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    /** La UI observa esto para navegar. */
    public enum Destination { SUPERADMIN, ADMIN, GUIDE, CLIENT }
    private final MutableLiveData<Destination> goTo = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<LoginResult> getLoginResult() { return loginResult; }
    public LiveData<String> getError() { return error; }
    public LiveData<Destination> getGoTo() { return goTo; }

    public void login(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            error.setValue("EMPTY");
            return;
        }
        loading.setValue(true);
        error.setValue(null);

        CompletableFuture<LoginResult> fut = repo.loginAsync(email, password);
        fut.thenAccept(result -> {
            loading.postValue(false);
            loginResult.postValue(result);

            if (result == null) { error.postValue("INVALID"); return; }
            boolean success = isSuccess(result);
            if (!success) { error.postValue("INVALID"); return; }

            String roleStr = extractRoleString(result);
            Destination dest = mapRole(roleStr);
            goTo.postValue(dest);

        }).exceptionally(ex -> {
            loading.postValue(false);
            error.postValue(ex.getMessage());
            return null;
        });
    }

    /** Llama esto tras navegar para evitar re-disparos. */
    public void clearNavigation() { goTo.setValue(null); }

    // -------- helpers ----------
    private boolean isSuccess(LoginResult r) {
        try {
            Method m = r.getClass().getMethod("isSuccess");
            Object v = m.invoke(r);
            if (v instanceof Boolean) return (Boolean) v;
        } catch (Throwable ignored) {}
        try {
            Method m = r.getClass().getMethod("getSuccess");
            Object v = m.invoke(r);
            if (v instanceof Boolean) return (Boolean) v;
        } catch (Throwable ignored) {}
        try {
            Field f = r.getClass().getDeclaredField("success");
            f.setAccessible(true);
            Object v = f.get(r);
            if (v instanceof Boolean) return (Boolean) v;
        } catch (Throwable ignored) {}
        // si no encontramos flag, asumimos true para no bloquear (ajústalo si quieres)
        return true;
    }

    private String extractRoleString(Object obj) {
        if (obj == null) return null;

        // 1) Métodos directos en LoginResult
        String s = tryStringMethod(obj, "getRole", "role", "getRoleName", "getType");
        if (s != null) return s;

        // 2) Campo directo
        s = tryStringField(obj, "role", "roleName", "type");
        if (s != null) return s;

        // 3) Buscar objeto user y leer su rol
        Object user = tryObjectMethod(obj, "getUser", "user");
        if (user == null) user = tryObjectField(obj, "user");
        if (user != null) {
            s = tryStringMethod(user, "getRole", "role", "getRoleName", "getType");
            if (s != null) return s;
            s = tryStringField(user, "role", "roleName", "type");
            if (s != null) return s;
        }

        return null;
    }

    private Destination mapRole(String roleString) {
        if (roleString == null) return Destination.SUPERADMIN;
        String r = roleString.trim().toUpperCase(Locale.ROOT);
        if (r.contains("SUPER")) return Destination.SUPERADMIN;
        if (r.equals("ADMIN") || r.contains("ADMIN")) return Destination.SUPERADMIN; // tu panel
        if (r.contains("GUIDE") || r.contains("GUÍA") || r.contains("GUIA")) return Destination.GUIDE;
        if (r.contains("CLIENT")) return Destination.CLIENT;
        return Destination.SUPERADMIN; // fallback
        // si en tu app hay un "ADMIN" distinto de SUPERADMIN, cambia aquí
    }

    // reflection utils
    private String tryStringMethod(Object obj, String... names) {
        for (String n : names) {
            try {
                Method m = obj.getClass().getMethod(n);
                Object v = m.invoke(obj);
                if (v != null) return String.valueOf(v);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private Object tryObjectMethod(Object obj, String... names) {
        for (String n : names) {
            try {
                Method m = obj.getClass().getMethod(n);
                Object v = m.invoke(obj);
                if (v != null) return v;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private String tryStringField(Object obj, String... names) {
        for (String n : names) {
            try {
                Field f = obj.getClass().getDeclaredField(n);
                f.setAccessible(true);
                Object v = f.get(obj);
                if (v != null) return String.valueOf(v);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private Object tryObjectField(Object obj, String... names) {
        for (String n : names) {
            try {
                Field f = obj.getClass().getDeclaredField(n);
                f.setAccessible(true);
                Object v = f.get(obj);
                if (v != null) return v;
            } catch (Throwable ignored) {}
        }
        return null;
    }
}

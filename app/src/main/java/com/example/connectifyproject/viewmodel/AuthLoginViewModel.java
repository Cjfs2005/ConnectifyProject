package com.example.connectifyproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.connectifyproject.model.LoginResult;
import com.example.connectifyproject.repository.AuthRepository;

import java.util.concurrent.CompletableFuture;

public class AuthLoginViewModel extends ViewModel {
    private final AuthRepository repo = new AuthRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>(null);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<LoginResult> getLoginResult() { return loginResult; }
    public LiveData<String> getError() { return error; }

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
            if (!result.isSuccess()) error.postValue("INVALID");
        }).exceptionally(ex -> {
            loading.postValue(false);
            error.postValue(ex.getMessage());
            return null;
        });
    }
}
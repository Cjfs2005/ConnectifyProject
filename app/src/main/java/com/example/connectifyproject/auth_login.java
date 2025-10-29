package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.connectifyproject.databinding.MainLoginViewBinding;
import com.example.connectifyproject.model.LoginResult;
import com.example.connectifyproject.utils.Cliente_PreferencesManager;
import com.example.connectifyproject.viewmodel.AuthLoginViewModel;
import com.example.connectifyproject.views.superadmin.users.SaUsersFragment;

public class auth_login extends AppCompatActivity {

    private MainLoginViewBinding binding;
    private AuthLoginViewModel viewModel;
    private Cliente_PreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainLoginViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefsManager = new Cliente_PreferencesManager(this);
        viewModel = new ViewModelProvider(this).get(AuthLoginViewModel.class);

        // Cargar credenciales guardadas si existen (para mostrar en UI)
        loadSavedCredentials();

        viewModel.getLoading().observe(this, isLoading -> {
            boolean show = isLoading != null && isLoading;
            binding.progress.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!show);
        });

        viewModel.getError().observe(this, err -> {
            if (err == null) {
                binding.tvError.setVisibility(View.GONE);
            } else {
                binding.tvError.setVisibility(View.VISIBLE);
                if ("EMPTY".equals(err)) {
                    binding.tvError.setText(getString(R.string.login_error_empty));
                } else if ("INVALID".equals(err)) {
                    binding.tvError.setText(getString(R.string.login_error_invalid));
                } else {
                    binding.tvError.setText(err);
                }
            }
        });

        viewModel.getLoginResult().observe(this, result -> {
            if (result != null && result.isSuccess()) {
                // Guardar credenciales si el checkbox está marcado
                saveCredentialsIfNeeded();
                
                Intent intent = getIntentForUserType(result.getUserType());
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });

        binding.btnLogin.setOnClickListener(v ->
                viewModel.login(
                        String.valueOf(binding.etEmail.getText()),
                        String.valueOf(binding.etPassword.getText())
                )
        );

        binding.btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterTypeSelectionActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Cargar credenciales guardadas si el usuario había marcado "Recordarme"
     */
    private void loadSavedCredentials() {
        if (prefsManager.shouldRememberLogin()) {
            String savedEmail = prefsManager.getSavedEmail();
            String savedPassword = prefsManager.getSavedPassword();
            
            if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
                binding.etEmail.setText(savedEmail);
                binding.etPassword.setText(savedPassword);
                binding.cbKeepSession.setChecked(true);
            }
        }
    }

    /**
     * Guardar credenciales si el checkbox está marcado
     */
    private void saveCredentialsIfNeeded() {
        String email = String.valueOf(binding.etEmail.getText());
        String password = String.valueOf(binding.etPassword.getText());
        boolean rememberMe = binding.cbKeepSession.isChecked();
        
        prefsManager.saveLoginCredentials(email, password, rememberMe);
    }

    private Intent getIntentForUserType(LoginResult.UserType userType) {
        switch (userType) {
            case SUPERADMIN:
                return new Intent(this, MainActivity.class);
            case ADMIN:
                return new Intent(this, admin_dashboard.class);
            case CLIENTE:
                return new Intent(this, cliente_inicio.class);
            case GUIA:
                return new Intent(this, guia_tours_ofertas.class);
            default:
                // Fallback al dashboard de admin si hay algún problema
                return new Intent(this, admin_dashboard.class);
        }
    }

}
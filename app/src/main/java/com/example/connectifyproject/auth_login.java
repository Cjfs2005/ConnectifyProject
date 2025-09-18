package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.connectifyproject.databinding.MainLoginViewBinding;
import com.example.connectifyproject.viewmodel.AuthLoginViewModel;

public class auth_login extends AppCompatActivity {

    private MainLoginViewBinding binding;
    private AuthLoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainLoginViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthLoginViewModel.class);

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

        viewModel.getSuccess().observe(this, ok -> {
            if (ok != null && ok) {
                Intent i = new Intent(this, admin_dashboard.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });

        binding.btnLogin.setOnClickListener(v ->
                viewModel.login(
                        String.valueOf(binding.etEmail.getText()),
                        String.valueOf(binding.etPassword.getText())
                )
        );
    }
}
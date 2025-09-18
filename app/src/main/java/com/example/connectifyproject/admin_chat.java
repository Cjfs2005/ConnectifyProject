package com.example.connectifyproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.databinding.AdminPlaceholderViewBinding;
import com.google.android.material.navigation.NavigationBarView;

public class admin_chat extends AppCompatActivity {
    private AdminPlaceholderViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminPlaceholderViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

        binding.topAppBar.setTitle("Chat");
        binding.tvPlaceholder.setText(getString(R.string.placeholder_message, "Chat"));

        binding.bottomNav.setOnItemSelectedListener(navListener);
        binding.bottomNav.setSelectedItemId(R.id.nav_chat);
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, admin_dashboard.class));
            return true;
        }
        if (id == R.id.nav_tours) {
            startActivity(new android.content.Intent(this, admin_tours.class));
            return true;
        }
        if (id == R.id.nav_chat) return true;
        if (id == R.id.nav_pagos) {
            startActivity(new android.content.Intent(this, admin_pagos.class));
            return true;
        }
        if (id == R.id.nav_perfil) {
            startActivity(new android.content.Intent(this, admin_perfil.class));
            return true;
        }
        return false;
    };
}
package com.example.connectifyproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.connectifyproject.databinding.AdminPlaceholderViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;

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

        // Agregar el Fragment de navegación inferior
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("chat");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }
}
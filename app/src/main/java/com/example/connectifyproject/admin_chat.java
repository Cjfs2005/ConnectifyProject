package com.example.connectifyproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.adapters.AdminChatAdapter;
import com.example.connectifyproject.databinding.AdminChatViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;

public class admin_chat extends AppCompatActivity {
    private AdminChatViewBinding binding;
    private AdminChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminChatViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        // Configurar RecyclerView con datos de ejemplo
        setupRecyclerView();
        
        // Configurar búsqueda
        setupSearch();

        // Agregar el Fragment de navegación inferior
        setupBottomNavigation();
    }

    private void setupRecyclerView() {
        binding.recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new AdminChatAdapter(this);
        binding.recyclerViewChats.setAdapter(chatAdapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (chatAdapter != null) {
                    chatAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("chat");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }
}
package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.ChatCompanyAdapter;

public class cliente_chat_list extends AppCompatActivity implements cliente_fragment_menu.OnMenuItemSelectedListener {

    private RecyclerView recyclerView;
    private ChatCompanyAdapter adapter;
    private EditText searchEditText;
    private ImageButton btnNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_chat_list);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupMenuFragment();
        setupClickListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView_chats);
        searchEditText = findViewById(R.id.editText_search);
        btnNotifications = findViewById(R.id.btn_notifications);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatCompanyAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_notificaciones.class);
            intent.putExtra("origin_activity", "cliente_chat_list");
            startActivity(intent);
        });
    }

    private void setupMenuFragment() {
        cliente_fragment_menu menuFragment = new cliente_fragment_menu();
        menuFragment.setOnMenuItemSelectedListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_fragment_container, menuFragment)
                .commit();
                
        // Seleccionar "Chat" por defecto
        menuFragment.setSelectedItem(R.id.nav_chat);
    }

    @Override
    public boolean onMenuItemSelected(int itemId) {
        if (itemId == R.id.nav_inicio) {
            Intent intent = new Intent(this, cliente_inicio.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_reservas) {
            Intent intent = new Intent(this, cliente_reservas.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_tours) {
            Intent intent = new Intent(this, cliente_tours.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_chat) {
            // Ya estamos en chat
            return true;
        } else if (itemId == R.id.nav_perfil) {
            Intent intent = new Intent(this, cliente_perfil.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
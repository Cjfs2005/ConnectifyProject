package com.example.connectifyproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.ChatCompanyAdapter;

public class cliente_chat_list extends AppCompatActivity implements cliente_fragment_menu.OnMenuItemSelectedListener {

    private RecyclerView recyclerView;
    private ChatCompanyAdapter adapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_chat_list);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupMenuFragment();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView_chats);
        searchEditText = findViewById(R.id.editText_search);
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
        // Navegación será implementada según sea necesario
        if (itemId == R.id.nav_inicio) {
            // Navegar a inicio
            return true;
        } else if (itemId == R.id.nav_reservas) {
            // Navegar a reservas
            return true;
        } else if (itemId == R.id.nav_tours) {
            // Navegar a tours
            return true;
        } else if (itemId == R.id.nav_chat) {
            // Ya estamos en chat
            return true;
        } else if (itemId == R.id.nav_perfil) {
            // Navegar a perfil
            return true;
        }
        return false;
    }
}
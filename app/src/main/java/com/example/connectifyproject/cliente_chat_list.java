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

import com.example.connectifyproject.adapters.Cliente_ChatCompanyAdapter;
import com.example.connectifyproject.models.Cliente_ChatCompany;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class cliente_chat_list extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Cliente_ChatCompanyAdapter adapter;
    private EditText searchEditText;
    private ImageButton btnNotifications;
    private BottomNavigationView bottomNavigation;
    private List<Cliente_ChatCompany> companies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_chat_list);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupBottomNavigation();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Chat" esté seleccionado cuando regresamos a esta actividad
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_chat);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView_chats);
        searchEditText = findViewById(R.id.editText_search);
        btnNotifications = findViewById(R.id.btn_notifications);
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadCompaniesData();
        adapter = new Cliente_ChatCompanyAdapter(this, companies);
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

    private void loadCompaniesData() {
        companies = new ArrayList<>();
        // Todos con la imagen de Lima Tours como solicitado
        companies.add(new Cliente_ChatCompany("Lima Tours", "Quedo atento para cualquier consulta", "10 min", R.drawable.cliente_tour_lima));
        companies.add(new Cliente_ChatCompany("Arequipa Adventures", "El tour de mañana está confirmado", "25 min", R.drawable.cliente_tour_lima));
        companies.add(new Cliente_ChatCompany("Cusco Explorer", "Gracias por contactarnos", "1 h", R.drawable.cliente_tour_lima));
        companies.add(new Cliente_ChatCompany("Trujillo Expeditions", "¿En qué horario prefiere el tour?", "2 h", R.drawable.cliente_tour_lima));
        companies.add(new Cliente_ChatCompany("Iquitos Nature", "Perfecto, nos vemos en el punto de encuentro", "1 día", R.drawable.cliente_tour_lima));
        companies.add(new Cliente_ChatCompany("Paracas Ocean", "El tour incluye almuerzo típico", "2 días", R.drawable.cliente_tour_lima));
        companies.add(new Cliente_ChatCompany("Huacachina Desert", "¿Cuántas personas serán para el tour?", "3 días", R.drawable.cliente_tour_lima));
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_inicio) {
                Intent intent = new Intent(this, cliente_inicio.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                Intent intent = new Intent(this, cliente_reservas.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_tours) {
                Intent intent = new Intent(this, cliente_tours.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Ya estamos en chat
                return true;
            } else if (itemId == R.id.nav_perfil) {
                Intent intent = new Intent(this, cliente_perfil.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });
        
        // Seleccionar "Chat" por defecto
        bottomNavigation.setSelectedItemId(R.id.nav_chat);
    }


}
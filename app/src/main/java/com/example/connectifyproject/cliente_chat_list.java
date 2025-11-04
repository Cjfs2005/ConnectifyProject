package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ChatCompanyAdapter;
import com.example.connectifyproject.models.Cliente_ChatCompany;
import com.example.connectifyproject.services.ChatService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class cliente_chat_list extends AppCompatActivity {

    private static final String TAG = "ClienteChatList";
    
    private RecyclerView recyclerView;
    private Cliente_ChatCompanyAdapter adapter;
    private EditText searchEditText;
    private ImageButton btnNotifications;
    private BottomNavigationView bottomNavigation;
    private List<Cliente_ChatCompany> companies;
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_chat_list);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupSearch();
        setupBottomNavigation();
        setupClickListeners();
        
        // Cargar empresas/admins desde Firebase
        loadAdminsFromFirebase();
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
        companies = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    private void loadAdminsFromFirebase() {
        if (ChatService.TEST_MODE) {
            // MODO TEST: Primero escuchar chats existentes, luego completar con admins sin chat
            db.collection("chats")
                    .whereEqualTo("clientId", currentUser.getUid())
                    .addSnapshotListener((chatSnapshots, chatError) -> {
                        if (chatError != null) {
                            Toast.makeText(this, "Error al cargar chats: " + chatError.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Mapa de adminId -> Cliente_ChatCompany para chats existentes
                        final java.util.Map<String, Cliente_ChatCompany> chatCompaniesMap = new java.util.HashMap<>();
                        
                        if (chatSnapshots != null) {
                            for (QueryDocumentSnapshot chatDoc : chatSnapshots) {
                                String adminId = chatDoc.getString("adminId");
                                String adminName = chatDoc.getString("adminName");
                                String adminPhotoUrl = chatDoc.getString("adminPhotoUrl");
                                String lastMessage = chatDoc.getString("lastMessage");
                                String lastSenderId = chatDoc.getString("lastSenderId");
                                Timestamp lastMessageTime = chatDoc.getTimestamp("lastMessageTime");
                                
                                // Formatear mensaje con "Tú:" si fue enviado por el cliente
                                String displayMessage = lastMessage != null ? lastMessage : "Toca para iniciar conversación";
                                if (lastSenderId != null && lastSenderId.equals(currentUser.getUid())) {
                                    displayMessage = "Tú: " + displayMessage;
                                }
                                
                                Cliente_ChatCompany company = new Cliente_ChatCompany(
                                        adminName != null ? adminName : "Empresa",
                                        displayMessage,
                                        formatTimestamp(lastMessageTime),
                                        R.drawable.cliente_tour_lima
                                );
                                company.setAdminId(adminId);
                                company.setAdminPhotoUrl(adminPhotoUrl);
                                company.setLastMessageTime(lastMessageTime);
                                
                                chatCompaniesMap.put(adminId, company);
                            }
                        }
                        
                        // Ahora cargar TODOS los admins y combinar
                        db.collection("usuarios")
                                .whereEqualTo("rol", "Administrador")
                                .get()
                                .addOnSuccessListener(userSnapshots -> {
                                    companies.clear();
                                    
                                    for (QueryDocumentSnapshot userDoc : userSnapshots) {
                                        String adminId = userDoc.getId();
                                        
                                        if (chatCompaniesMap.containsKey(adminId)) {
                                            // Ya existe chat, usar datos del chat
                                            companies.add(chatCompaniesMap.get(adminId));
                                        } else {
                                            // No hay chat, crear entrada sin mensajes
                                            String adminName = userDoc.getString("nombresApellidos");
                                            String adminPhotoUrl = userDoc.getString("photoUrl");
                                            String empresaName = userDoc.getString("nombreEmpresa");
                                            
                                            // Usar nombre de empresa si existe, sino usar nombres y apellidos
                                            String displayName = (empresaName != null && !empresaName.isEmpty()) 
                                                ? empresaName 
                                                : (adminName != null ? adminName : "Empresa");
                                            
                                            Cliente_ChatCompany company = new Cliente_ChatCompany(
                                                    displayName,
                                                    "Toca para iniciar conversación",
                                                    "",
                                                    R.drawable.cliente_tour_lima
                                            );
                                            company.setAdminId(adminId);
                                            company.setAdminPhotoUrl(adminPhotoUrl);
                                            company.setLastMessageTime(null);
                                            
                                            companies.add(company);
                                        }
                                    }
                                    
                                    // Ordenar por último mensaje (más reciente primero)
                                    java.util.Collections.sort(companies, (c1, c2) -> {
                                        Timestamp t1 = c1.getLastMessageTime();
                                        Timestamp t2 = c2.getLastMessageTime();
                                        
                                        if (t1 == null && t2 == null) return 0;
                                        if (t1 == null) return 1;
                                        if (t2 == null) return -1;
                                        
                                        return t2.compareTo(t1); // Descendente (más reciente primero)
                                    });
                                    
                                    adapter.updateData(companies);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al cargar empresas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
        } else {
            // MODO PRODUCCIÓN: Cargar solo chats existentes del usuario con listener en tiempo real
            db.collection("chats")
                .whereEqualTo("clientId", currentUser.getUid())
                .whereEqualTo("active", true)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error al cargar conversaciones: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (queryDocumentSnapshots == null) return;
                    
                    companies.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String adminName = document.getString("adminName");
                        String lastMessage = document.getString("lastMessage");
                        String lastSenderId = document.getString("lastSenderId");
                        String adminPhotoUrl = document.getString("adminPhotoUrl");
                        com.google.firebase.Timestamp lastMessageTime = document.getTimestamp("lastMessageTime");
                        
                        // Formatear mensaje con "Tú:" si fue enviado por el cliente
                        String displayMessage = lastMessage != null ? lastMessage : "";
                        if (lastSenderId != null && lastSenderId.equals(currentUser.getUid())) {
                            displayMessage = "Tú: " + displayMessage;
                        }
                        
                        Cliente_ChatCompany company = new Cliente_ChatCompany(
                            adminName, 
                            displayMessage, 
                            formatTimestamp(lastMessageTime), 
                            R.drawable.cliente_tour_lima
                        );
                        company.setAdminId(document.getString("adminId"));
                        company.setAdminPhotoUrl(adminPhotoUrl);
                        company.setLastMessageTime(lastMessageTime);
                        companies.add(company);
                    }
                    
                    // Ordenar manualmente por último mensaje (más reciente primero)
                    java.util.Collections.sort(companies, (c1, c2) -> {
                        Timestamp t1 = c1.getLastMessageTime();
                        Timestamp t2 = c2.getLastMessageTime();
                        
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        
                        return t2.compareTo(t1); // Descendente (más reciente primero)
                    });
                    
                    adapter.updateData(companies);
                });
        }
    }
    
    private String formatTimestamp(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return "";
        
        Date messageDate = timestamp.toDate();
        Date now = new Date();
        
        // Obtener calendarios para comparar fechas
        java.util.Calendar messageCal = java.util.Calendar.getInstance();
        messageCal.setTime(messageDate);
        
        java.util.Calendar nowCal = java.util.Calendar.getInstance();
        nowCal.setTime(now);
        
        // Mismo día - mostrar hora
        if (messageCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
            messageCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            return timeFormat.format(messageDate);
        }
        
        // Ayer
        nowCal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        if (messageCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
            messageCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            return "Ayer";
        }
        
        // Fecha completa dd/MM/yy
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault());
        return dateFormat.format(messageDate);
    }
    
    private String getTimeAgo(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return "";
        
        long diff = System.currentTimeMillis() - timestamp.toDate().getTime();
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (minutes < 60) return minutes + " min";
        if (hours < 24) return hours + " h";
        return days + " días";
    }

    private void loadCompaniesData() {
        // Método obsoleto - ahora se usa loadAdminsFromFirebase()
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
package com.example.connectifyproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.adapters.AdminChatAdapter;
import com.example.connectifyproject.databinding.AdminChatViewBinding;
import com.example.connectifyproject.models.AdminChatClient;
import com.example.connectifyproject.services.ChatService;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class admin_chat extends AppCompatActivity {
    private static final String TAG = "AdminChat";
    private AdminChatViewBinding binding;
    private AdminChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private List<AdminChatClient> clients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminChatViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar RecyclerView
        setupRecyclerView();
        
        // Configurar búsqueda
        setupSearch();

        // Agregar el Fragment de navegación inferior
        setupBottomNavigation();

        // Cargar clientes desde Firebase
        loadClientsFromFirebase();
    }

    private void setupRecyclerView() {
        clients = new ArrayList<>();
        binding.recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new AdminChatAdapter(this, clients);
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

    private void loadClientsFromFirebase() {
        if (ChatService.TEST_MODE) {
            // Modo de prueba: cargar todos los clientes
            db.collection("usuarios")
                    .whereEqualTo("rol", "Cliente")
                    .addSnapshotListener((queryDocumentSnapshots, error) -> {
                        if (error != null) {
                            Toast.makeText(this, "Error al cargar clientes: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(this, "No se encontraron clientes registrados", Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        clients.clear();
                        int totalClients = queryDocumentSnapshots.size();
                        final int[] processedClients = {0};
                        final java.util.List<AdminChatClient> tempClients = new java.util.ArrayList<>();
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String clientId = document.getId();
                            String clientName = document.getString("nombresApellidos");
                            String clientPhotoUrl = document.getString("photoUrl");

                            if (clientName == null) clientName = "Cliente";
                            
                            final String finalClientName = clientName;
                            
                            // Buscar si existe un chat con este cliente
                            db.collection("chats")
                                .whereEqualTo("adminId", currentUser.getUid())
                                .whereEqualTo("clientId", clientId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(chatSnapshots -> {
                                    AdminChatClient client;
                                    
                                    if (!chatSnapshots.isEmpty()) {
                                        // Hay chat existente, obtener el último mensaje
                                        String chatId = chatSnapshots.getDocuments().get(0).getId();
                                        String lastMessage = chatSnapshots.getDocuments().get(0).getString("lastMessage");
                                        String lastSenderId = chatSnapshots.getDocuments().get(0).getString("lastSenderId");
                                        Timestamp lastMessageTime = chatSnapshots.getDocuments().get(0).getTimestamp("lastMessageTime");
                                        
                                        // Formatear mensaje con "Tú:" si fue enviado por el admin
                                        String displayMessage = lastMessage != null ? lastMessage : "Toca para iniciar conversación";
                                        if (lastSenderId != null && lastSenderId.equals(currentUser.getUid())) {
                                            displayMessage = "Tú: " + displayMessage;
                                        }
                                        
                                        client = new AdminChatClient(
                                                finalClientName,
                                                displayMessage,
                                                formatTimestamp(lastMessageTime),
                                                R.drawable.ic_avatar_male_1
                                        );
                                        client.setClientId(clientId);
                                        client.setClientPhotoUrl(clientPhotoUrl);
                                        client.setLastMessageTime(lastMessageTime);
                                    } else {
                                        // No hay chat, mostrar mensaje por defecto
                                        client = new AdminChatClient(
                                                finalClientName,
                                                "Toca para iniciar conversación",
                                                "",
                                                R.drawable.ic_avatar_male_1
                                        );
                                        client.setClientId(clientId);
                                        client.setClientPhotoUrl(clientPhotoUrl);
                                        client.setLastMessageTime(null);
                                    }
                                    
                                    tempClients.add(client);
                                    processedClients[0]++;
                                    
                                    if (processedClients[0] == totalClients) {
                                        // Ordenar por último mensaje (más reciente primero)
                                        java.util.Collections.sort(tempClients, (c1, c2) -> {
                                            Timestamp t1 = c1.getLastMessageTime();
                                            Timestamp t2 = c2.getLastMessageTime();
                                            
                                            if (t1 == null && t2 == null) return 0;
                                            if (t1 == null) return 1;
                                            if (t2 == null) return -1;
                                            
                                            return t2.compareTo(t1); // Descendente (más reciente primero)
                                        });
                                        
                                        clients.addAll(tempClients);
                                        chatAdapter.updateData(clients);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // En caso de error, agregar sin mensaje
                                    AdminChatClient client = new AdminChatClient(
                                            finalClientName,
                                            "Toca para iniciar conversación",
                                            "",
                                            R.drawable.ic_avatar_male_1
                                    );
                                    client.setClientId(clientId);
                                    client.setClientPhotoUrl(clientPhotoUrl);
                                    client.setLastMessageTime(null);
                                    
                                    tempClients.add(client);
                                    processedClients[0]++;
                                    
                                    if (processedClients[0] == totalClients) {
                                        // Ordenar por último mensaje (más reciente primero)
                                        java.util.Collections.sort(tempClients, (c1, c2) -> {
                                            Timestamp t1 = c1.getLastMessageTime();
                                            Timestamp t2 = c2.getLastMessageTime();
                                            
                                            if (t1 == null && t2 == null) return 0;
                                            if (t1 == null) return 1;
                                            if (t2 == null) return -1;
                                            
                                            return t2.compareTo(t1); // Descendente
                                        });
                                        
                                        clients.addAll(tempClients);
                                        chatAdapter.updateData(clients);
                                    }
                                });
                        }
                    });
        } else {
            // Modo producción: cargar solo chats activos con listener en tiempo real
            db.collection("chats")
                    .whereEqualTo("adminId", currentUser.getUid())
                    .whereEqualTo("active", true)
                    .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                    .addSnapshotListener((queryDocumentSnapshots, error) -> {
                        if (error != null) {
                            Toast.makeText(this, "Error al cargar chats: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        if (queryDocumentSnapshots == null) return;
                        
                        clients.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String clientId = document.getString("clientId");
                            String clientName = document.getString("clientName");
                            String clientPhotoUrl = document.getString("clientPhotoUrl");
                            String lastMessage = document.getString("lastMessage");
                            String lastSenderId = document.getString("lastSenderId");
                            Timestamp lastMessageTime = document.getTimestamp("lastMessageTime");

                            // Formatear mensaje con "Tú:" si fue enviado por el admin
                            String displayMessage = lastMessage != null ? lastMessage : "";
                            if (lastSenderId != null && lastSenderId.equals(currentUser.getUid())) {
                                displayMessage = "Tú: " + displayMessage;
                            }

                            AdminChatClient client = new AdminChatClient(
                                    clientName,
                                    displayMessage,
                                    formatTimestamp(lastMessageTime),
                                    R.drawable.ic_avatar_male_1
                            );
                            client.setClientId(clientId);
                            client.setClientPhotoUrl(clientPhotoUrl);
                            client.setLastMessageTime(lastMessageTime);
                            clients.add(client);
                        }
                        chatAdapter.updateData(clients);
                    });
        }
    }

    private String formatTimestamp(Timestamp timestamp) {
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

    private String getTimeAgo(Timestamp timestamp) {
        if (timestamp == null) return "";
        
        Date date = timestamp.toDate();
        long diff = new Date().getTime() - date.getTime();
        long minutes = diff / (60 * 1000);
        
        if (minutes < 60) {
            return minutes + " min";
        } else if (minutes < 1440) {
            return (minutes / 60) + " h";
        } else {
            return (minutes / 1440) + " días";
        }
    }
}
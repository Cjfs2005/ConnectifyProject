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
import com.example.connectifyproject.services.ChatNotificationService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class admin_chat extends AppCompatActivity {
    private static final String TAG = "AdminChat";
    private AdminChatViewBinding binding;
    private AdminChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private List<AdminChatClient> clients;
    private ChatNotificationService notificationService;
    private ChatService chatService;
    
    // Map para trackear el último mensaje de cada chat y detectar cambios
    private Map<String, String> previousLastMessages = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminChatViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        notificationService = new ChatNotificationService(this);
        chatService = new ChatService();

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
            // Modo de prueba: Primero escuchar chats existentes, luego completar con clientes sin chat
            db.collection("chats")
                    .whereEqualTo("adminId", currentUser.getUid())
                    .addSnapshotListener((chatSnapshots, chatError) -> {
                        if (chatError != null) {
                            Toast.makeText(this, "Error al cargar chats: " + chatError.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Mapa de clientId -> AdminChatClient para chats existentes
                        final java.util.Map<String, AdminChatClient> chatClientsMap = new java.util.HashMap<>();
                        
                        if (chatSnapshots != null) {
                            for (QueryDocumentSnapshot chatDoc : chatSnapshots) {
                                String clientId = chatDoc.getString("clientId");
                                String clientName = chatDoc.getString("clientName");
                                String clientPhotoUrl = chatDoc.getString("clientPhotoUrl");
                                String lastMessage = chatDoc.getString("lastMessage");
                                String lastSenderId = chatDoc.getString("lastSenderId");
                                Timestamp lastMessageTime = chatDoc.getTimestamp("lastMessageTime");
                                
                                // Formatear mensaje con "Tú:" si fue enviado por el admin
                                String displayMessage = lastMessage != null ? lastMessage : "Toca para iniciar conversación";
                                if (lastSenderId != null && lastSenderId.equals(currentUser.getUid())) {
                                    displayMessage = "Tú: " + displayMessage;
                                }
                                
                                AdminChatClient client = new AdminChatClient(
                                        clientName != null ? clientName : "Cliente",
                                        displayMessage,
                                        formatTimestamp(lastMessageTime),
                                        R.drawable.ic_avatar_male_1
                                );
                                client.setClientId(clientId);
                                client.setClientPhotoUrl(clientPhotoUrl);
                                client.setLastMessageTime(lastMessageTime);
                                
                                chatClientsMap.put(clientId, client);
                            }
                        }
                        
                        // Ahora cargar TODOS los clientes y combinar
                        db.collection("usuarios")
                                .whereEqualTo("rol", "Cliente")
                                .get()
                                .addOnSuccessListener(userSnapshots -> {
                                    clients.clear();
                                    
                                    for (QueryDocumentSnapshot userDoc : userSnapshots) {
                                        String clientId = userDoc.getId();
                                        
                                        if (chatClientsMap.containsKey(clientId)) {
                                            // Ya existe chat, usar datos del chat
                                            clients.add(chatClientsMap.get(clientId));
                                        } else {
                                            // No hay chat, crear entrada sin mensajes
                                            String clientName = userDoc.getString("nombresApellidos");
                                            String clientPhotoUrl = userDoc.getString("photoUrl");
                                            
                                            AdminChatClient client = new AdminChatClient(
                                                    clientName != null ? clientName : "Cliente",
                                                    "Toca para iniciar conversación",
                                                    "",
                                                    R.drawable.ic_avatar_male_1
                                            );
                                            client.setClientId(clientId);
                                            client.setClientPhotoUrl(clientPhotoUrl);
                                            client.setLastMessageTime(null);
                                            
                                            clients.add(client);
                                        }
                                    }
                                    
                                    // Ordenar por último mensaje (más reciente primero)
                                    java.util.Collections.sort(clients, (c1, c2) -> {
                                        Timestamp t1 = c1.getLastMessageTime();
                                        Timestamp t2 = c2.getLastMessageTime();
                                        
                                        if (t1 == null && t2 == null) return 0;
                                        if (t1 == null) return 1;
                                        if (t2 == null) return -1;
                                        
                                        return t2.compareTo(t1); // Descendente (más reciente primero)
                                    });
                                    
                                    chatAdapter.updateData(clients);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al cargar clientes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
        } else {
            // Modo producción: cargar solo chats activos con listener en tiempo real
            db.collection("chats")
                    .whereEqualTo("adminId", currentUser.getUid())
                    .whereEqualTo("active", true)
                    .addSnapshotListener((queryDocumentSnapshots, error) -> {
                        if (error != null) {
                            Toast.makeText(this, "Error al cargar chats: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        if (queryDocumentSnapshots == null) return;
                        
                        clients.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String chatId = document.getId();
                            String clientId = document.getString("clientId");
                            String clientName = document.getString("clientName");
                            String clientPhotoUrl = document.getString("clientPhotoUrl");
                            String lastMessage = document.getString("lastMessage");
                            String lastSenderId = document.getString("lastSenderId");
                            Timestamp lastMessageTime = document.getTimestamp("lastMessageTime");
                            Integer unreadCountAdmin = document.getLong("unreadCountAdmin") != null ? 
                                document.getLong("unreadCountAdmin").intValue() : 0;
                            
                            // Detectar nuevo mensaje y enviar notificación
                            if (lastMessage != null && lastSenderId != null) {
                                String previousMessage = previousLastMessages.get(chatId);
                                
                                // Si es un mensaje nuevo, no fue enviado por mí, y no estoy viendo ese chat
                                if (previousMessage != null && 
                                    !lastMessage.equals(previousMessage) && 
                                    !lastSenderId.equals(currentUser.getUid()) &&
                                    !chatId.equals(admin_chat_conversation.currentOpenChatId)) {
                                    
                                    // Enviar notificación
                                    notificationService.sendMessageNotification(
                                        clientName,
                                        lastMessage,
                                        chatId,
                                        "CLIENT",
                                        currentUser.getUid(),
                                        "ADMIN"
                                    );
                                    Log.d(TAG, "Notificación enviada para chat: " + chatId);
                                }
                                
                                // Si el chat está abierto y hay mensajes no leídos, resetear el contador en Firebase
                                if (chatId.equals(admin_chat_conversation.currentOpenChatId) && unreadCountAdmin > 0) {
                                    chatService.markMessagesAsRead(chatId, "ADMIN");
                                    Log.d(TAG, "Contador reseteado automáticamente para chat abierto: " + chatId);
                                }
                                
                                // Actualizar el último mensaje conocido
                                previousLastMessages.put(chatId, lastMessage);
                            }

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
                            
                            // Si el chat está abierto, mostrar contador como 0
                            if (chatId.equals(admin_chat_conversation.currentOpenChatId)) {
                                client.setUnreadCount(0);
                            } else {
                                client.setUnreadCount(unreadCountAdmin);
                            }
                            
                            clients.add(client);
                        }
                        
                        // Ordenar manualmente por último mensaje (más reciente primero)
                        java.util.Collections.sort(clients, (c1, c2) -> {
                            Timestamp t1 = c1.getLastMessageTime();
                            Timestamp t2 = c2.getLastMessageTime();
                            
                            if (t1 == null && t2 == null) return 0;
                            if (t1 == null) return 1;
                            if (t2 == null) return -1;
                            
                            return t2.compareTo(t1); // Descendente (más reciente primero)
                        });
                        
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
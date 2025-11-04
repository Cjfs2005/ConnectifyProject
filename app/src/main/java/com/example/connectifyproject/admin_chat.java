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
        Log.d(TAG, "loadClientsFromFirebase - TEST_MODE: " + ChatService.TEST_MODE);
        
        if (ChatService.TEST_MODE) {
            // Modo de prueba: cargar todos los clientes
            Log.d(TAG, "Consultando usuarios con rol='Cliente'");
            db.collection("usuarios")
                    .whereEqualTo("rol", "Cliente")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d(TAG, "Query exitosa. Documentos encontrados: " + queryDocumentSnapshots.size());
                        clients.clear();
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String clientId = document.getId();
                            String clientName = document.getString("nombresApellidos");
                            String clientPhotoUrl = document.getString("photoUrl");

                            Log.d(TAG, "Cliente encontrado - ID: " + clientId + 
                                       ", Nombre: " + clientName);

                            if (clientName == null) clientName = "Cliente";

                            AdminChatClient client = new AdminChatClient(
                                    clientName,
                                    "Toca para iniciar conversación",
                                    "",
                                    R.drawable.ic_avatar_male_1
                            );
                            client.setClientId(clientId);
                            client.setClientPhotoUrl(clientPhotoUrl);
                            clients.add(client);
                        }
                        
                        Log.d(TAG, "Total cargados en lista: " + clients.size() + " clientes");
                        chatAdapter.updateData(clients);
                        
                        if (clients.size() == 0) {
                            Toast.makeText(this, "No se encontraron clientes registrados", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al cargar clientes: " + e.getMessage(), e);
                        Toast.makeText(this, "Error al cargar clientes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            // Modo producción: cargar solo chats activos
            db.collection("chats")
                    .whereEqualTo("adminId", currentUser.getUid())
                    .whereEqualTo("active", true)
                    .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        clients.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String clientId = document.getString("clientId");
                            String clientName = document.getString("clientName");
                            String clientPhotoUrl = document.getString("clientPhotoUrl");
                            String lastMessage = document.getString("lastMessage");
                            Timestamp lastMessageTime = document.getTimestamp("lastMessageTime");

                            AdminChatClient client = new AdminChatClient(
                                    clientName,
                                    lastMessage,
                                    getTimeAgo(lastMessageTime),
                                    R.drawable.ic_avatar_male_1
                            );
                            client.setClientId(clientId);
                            client.setClientPhotoUrl(clientPhotoUrl);
                            clients.add(client);
                        }
                        chatAdapter.updateData(clients);
                        Log.d(TAG, "Chats activos cargados: " + clients.size());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al cargar chats", e);
                        Toast.makeText(this, "Error al cargar chats", Toast.LENGTH_SHORT).show();
                    });
        }
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
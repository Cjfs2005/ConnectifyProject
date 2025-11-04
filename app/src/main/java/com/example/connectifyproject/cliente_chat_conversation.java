package com.example.connectifyproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.adapters.ClienteMessageAdapter;
import com.example.connectifyproject.model.Chat;
import com.example.connectifyproject.model.ChatMessage;
import com.example.connectifyproject.services.ChatNotificationService;
import com.example.connectifyproject.services.ChatService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class cliente_chat_conversation extends AppCompatActivity {

    private static final String TAG = "ClienteChatConv";
    
    private RecyclerView recyclerViewMessages;
    private ClienteMessageAdapter messageAdapter;
    private EditText editTextMessage;
    private FloatingActionButton fabSend;
    private MaterialToolbar toolbar;
    private ImageView ivCompanyLogoToolbar;
    private TextView tvCompanyNameToolbar;
    private TextView tvStatus;

    private ChatService chatService;
    private ChatNotificationService notificationService;
    private FirebaseUser currentUser;
    
    private String chatId;
    private String adminId;
    private String adminName;
    private String adminPhotoUrl;
    private String clientId;
    private String clientName;
    private String clientPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_chat_conversation);

        // Inicializar servicios
        chatService = new ChatService();
        notificationService = new ChatNotificationService(this);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSendButton();
        initializeChat();
    }

    private void getIntentData() {
        // Obtener datos de la empresa/admin del intent
        adminId = getIntent().getStringExtra("admin_id");
        adminName = getIntent().getStringExtra("admin_name");
        adminPhotoUrl = getIntent().getStringExtra("admin_photo_url");
        
        // Obtener datos del cliente actual
        clientId = currentUser.getUid();
        clientName = getIntent().getStringExtra("client_name");
        if (clientName == null) {
            clientName = currentUser.getDisplayName() != null ? 
                        currentUser.getDisplayName() : "Usuario";
        }
        clientPhotoUrl = currentUser.getPhotoUrl() != null ? 
                        currentUser.getPhotoUrl().toString() : "";
        
        if (adminId == null || adminName == null) {
            Toast.makeText(this, "Error: Datos de la empresa no disponibles", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        recyclerViewMessages = findViewById(R.id.recyclerView_messages);
        editTextMessage = findViewById(R.id.editText_message);
        fabSend = findViewById(R.id.fab_send);
        toolbar = findViewById(R.id.toolbar);
        ivCompanyLogoToolbar = findViewById(R.id.iv_company_logo_toolbar);
        tvCompanyNameToolbar = findViewById(R.id.tv_company_name_toolbar);
        tvStatus = findViewById(R.id.tv_status);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Mostrar datos de la empresa/admin
        tvCompanyNameToolbar.setText(adminName);
        tvStatus.setText("En línea");
        
        // Cargar imagen con Glide
        if (adminPhotoUrl != null && !adminPhotoUrl.isEmpty()) {
            Glide.with(this)
                .load(adminPhotoUrl)
                .placeholder(R.drawable.cliente_tour_lima)
                .into(ivCompanyLogoToolbar);
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new ClienteMessageAdapter(clientId);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupSendButton() {
        fabSend.setOnClickListener(v -> sendMessage());
    }
    
    private void initializeChat() {
        // Crear o obtener el chat existente
        chatService.getOrCreateChat(
            clientId, clientName, clientPhotoUrl,
            adminId, adminName, adminPhotoUrl,
            new ChatService.OnChatReadyListener() {
                @Override
                public void onChatReady(Chat chat) {
                    chatId = chat.getChatId();
                    Log.d(TAG, "Chat inicializado: " + chatId);
                    
                    // Marcar mensajes como leídos
                    chatService.markMessagesAsRead(chatId, "CLIENT");
                    
                    // Escuchar mensajes en tiempo real
                    listenToMessages();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error al inicializar chat", e);
                    Toast.makeText(cliente_chat_conversation.this, 
                        "Error al cargar el chat", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void listenToMessages() {
        chatService.listenToMessages(chatId, new ChatService.OnMessagesLoadedListener() {
            @Override
            public void onMessagesLoaded(List<ChatMessage> messagesList) {
                messageAdapter.setMessages(messagesList);
                scrollToBottom();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error al escuchar mensajes", e);
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        
        if (!TextUtils.isEmpty(messageText)) {
            ChatMessage message = new ChatMessage(
                chatId,
                clientId,
                clientName,
                "CLIENT",
                messageText
            );
            
            chatService.sendMessage(message, new ChatService.OnMessageSentListener() {
                @Override
                public void onMessageSent(ChatMessage sentMessage) {
                    editTextMessage.setText("");
                    scrollToBottom();
                    
                    // Enviar notificación al admin
                    notificationService.sendMessageNotification(
                        clientName,
                        messageText,
                        chatId,
                        "CLIENT",
                        adminId,
                        "ADMIN"
                    );
                    
                    Log.d(TAG, "Mensaje enviado exitosamente");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error al enviar mensaje", e);
                    Toast.makeText(cliente_chat_conversation.this, 
                        "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            recyclerViewMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Marcar mensajes como leídos cuando se abre la conversación
        if (chatId != null) {
            chatService.markMessagesAsRead(chatId, "CLIENT");
        }
    }
}
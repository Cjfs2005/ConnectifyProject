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
import com.example.connectifyproject.adapters.AdminMessageAdapter;
import com.example.connectifyproject.model.Chat;
import com.example.connectifyproject.model.ChatMessage;
import com.example.connectifyproject.services.ChatNotificationService;
import com.example.connectifyproject.services.ChatService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class admin_chat_conversation extends AppCompatActivity {

    private static final String TAG = "AdminChatConv";
    
    private RecyclerView recyclerViewMessages;
    private AdminMessageAdapter messageAdapter;
    private EditText editTextMessage;
    private FloatingActionButton fabSend;
    private MaterialToolbar toolbar;
    private ImageView ivClientAvatarToolbar;
    private TextView tvClientNameToolbar;
    private TextView tvStatus;

    private ChatService chatService;
    private ChatNotificationService notificationService;
    private FirebaseUser currentUser;
    
    private String chatId;
    private String clientId;
    private String clientName;
    private String clientPhotoUrl;
    private String adminId;
    private String adminName;
    private String adminPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_chat_conversation_view);

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
        clientId = getIntent().getStringExtra("client_id");
        clientName = getIntent().getStringExtra("client_name");
        clientPhotoUrl = getIntent().getStringExtra("client_photo_url");
        
        adminId = currentUser.getUid();
        adminName = getIntent().getStringExtra("admin_name");
        if (adminName == null) {
            adminName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Empresa";
        }
        adminPhotoUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "";
        
        if (clientId == null || clientName == null) {
            Toast.makeText(this, "Error: Datos del cliente no disponibles", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        recyclerViewMessages = findViewById(R.id.recyclerView_messages);
        editTextMessage = findViewById(R.id.editText_message);
        fabSend = findViewById(R.id.fab_send);
        toolbar = findViewById(R.id.topAppBar);
        ivClientAvatarToolbar = findViewById(R.id.iv_client_avatar_toolbar);
        tvClientNameToolbar = findViewById(R.id.tv_client_name_toolbar);
        tvStatus = findViewById(R.id.tv_status);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        tvClientNameToolbar.setText(clientName);
        tvStatus.setText("En línea");
        
        if (clientPhotoUrl != null && !clientPhotoUrl.isEmpty()) {
            Glide.with(this).load(clientPhotoUrl).placeholder(R.drawable.ic_avatar_male_1).into(ivClientAvatarToolbar);
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new AdminMessageAdapter(adminId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupSendButton() {
        fabSend.setOnClickListener(v -> sendMessage());
    }
    
    private void initializeChat() {
        chatService.getOrCreateChat(clientId, clientName, clientPhotoUrl, adminId, adminName, adminPhotoUrl,
            new ChatService.OnChatReadyListener() {
                @Override
                public void onChatReady(Chat chat) {
                    chatId = chat.getChatId();
                    Log.d(TAG, "Chat inicializado: " + chatId);
                    chatService.markMessagesAsRead(chatId, "ADMIN");
                    listenToMessages();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error al inicializar chat", e);
                    Toast.makeText(admin_chat_conversation.this, "Error al cargar el chat", Toast.LENGTH_SHORT).show();
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
            ChatMessage message = new ChatMessage(chatId, adminId, adminName, "ADMIN", messageText);
            
            chatService.sendMessage(message, new ChatService.OnMessageSentListener() {
                @Override
                public void onMessageSent(ChatMessage sentMessage) {
                    editTextMessage.setText("");
                    scrollToBottom();
                    notificationService.sendMessageNotification(adminName, messageText, chatId, "ADMIN", clientId, "CLIENT");
                    Log.d(TAG, "Mensaje enviado exitosamente");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error al enviar mensaje", e);
                    Toast.makeText(admin_chat_conversation.this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
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
        if (chatId != null) {
            chatService.markMessagesAsRead(chatId, "ADMIN");
        }
    }
}

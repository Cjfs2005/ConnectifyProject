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
import com.example.connectifyproject.model.ChatItem;
import com.example.connectifyproject.model.ChatMessage;
import com.example.connectifyproject.services.ChatNotificationService;
import com.example.connectifyproject.services.ChatService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class admin_chat_conversation extends AppCompatActivity {

    private static final String TAG = "AdminChatConv";
    
    // Variable estática para trackear qué chat está abierto
    public static String currentOpenChatId = null;
    
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
        
        // Cargar datos desde Firebase para asegurar consistencia
        loadAdminPhotoUrl();
        loadAdminCompanyName();
        
        if (clientId == null || clientName == null) {
            Toast.makeText(this, "Error: Datos del cliente no disponibles", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void loadAdminPhotoUrl() {
        chatService.getUserPhotoUrl(adminId, new ChatService.OnPhotoUrlLoadedListener() {
            @Override
            public void onSuccess(String photoUrl) {
                adminPhotoUrl = photoUrl != null ? photoUrl : "";
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al cargar photoUrl del admin: " + e.getMessage());
                adminPhotoUrl = "";
            }
        });
    }
    
    private void loadAdminCompanyName() {
        chatService.getCompanyName(adminId, new ChatService.OnCompanyNameLoadedListener() {
            @Override
            public void onSuccess(String companyName) {
                // Si hay nombreEmpresa, usarlo; sino mantener el nombre actual
                if (companyName != null && !companyName.isEmpty()) {
                    adminName = companyName;
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al cargar nombreEmpresa: " + e.getMessage());
                // Mantener el adminName que vino del intent o del displayName
            }
        });
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
        
        // Ocultar estado "En línea"
        if (tvStatus != null) {
            tvStatus.setVisibility(android.view.View.GONE);
        }
        
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
                    currentOpenChatId = chatId; // Marcar este chat como abierto
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
    
    @Override
    protected void onResume() {
        super.onResume();
        if (chatId != null) {
            currentOpenChatId = chatId;
            chatService.markMessagesAsRead(chatId, "ADMIN");
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        currentOpenChatId = null; // Limpiar al salir
    }
    
    private void listenToMessages() {
        chatService.listenToMessages(chatId, new ChatService.OnMessagesLoadedListener() {
            @Override
            public void onMessagesLoaded(List<ChatMessage> messagesList) {
                List<ChatItem> chatItems = processMessagesWithDateSeparators(messagesList);
                messageAdapter.setChatItems(chatItems);
                scrollToBottom();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error al escuchar mensajes", e);
            }
        });
    }
    
    private List<ChatItem> processMessagesWithDateSeparators(List<ChatMessage> messages) {
        List<ChatItem> chatItems = new ArrayList<>();
        
        if (messages.isEmpty()) {
            return chatItems;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        String lastDateString = null;
        
        for (ChatMessage message : messages) {
            if (message.getTimestamp() != null) {
                Date messageDate = message.getTimestamp().toDate();
                String currentDateString = getDateSeparatorText(messageDate, dateFormat);
                
                // Si la fecha cambió, agregar separador
                if (!currentDateString.equals(lastDateString)) {
                    chatItems.add(new ChatItem(currentDateString));
                    lastDateString = currentDateString;
                }
            }
            
            // Agregar el mensaje
            chatItems.add(new ChatItem(message));
        }
        
        return chatItems;
    }
    
    private String getDateSeparatorText(Date messageDate, SimpleDateFormat dateFormat) {
        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(messageDate);
        
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        
        // Comparar solo fecha (sin hora)
        boolean isToday = isSameDay(messageCalendar, today);
        boolean isYesterday = isSameDay(messageCalendar, yesterday);
        
        if (isToday) {
            return "Hoy";
        } else if (isYesterday) {
            return "Ayer";
        } else {
            return dateFormat.format(messageDate);
        }
    }
    
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        
        if (!TextUtils.isEmpty(messageText)) {
            ChatMessage message = new ChatMessage(chatId, adminId, adminName, "ADMIN", messageText);
            
            chatService.sendMessage(message, clientName, clientPhotoUrl, adminName, adminPhotoUrl,
                new ChatService.OnMessageSentListener() {
                    @Override
                    public void onMessageSent(ChatMessage sentMessage) {
                        editTextMessage.setText("");
                        scrollToBottom();
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
}

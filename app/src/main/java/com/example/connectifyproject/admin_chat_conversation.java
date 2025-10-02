package com.example.connectifyproject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.ChatMessageAdapter;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class admin_chat_conversation extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private ChatMessageAdapter messageAdapter;
    private EditText editTextMessage;
    private FloatingActionButton fabSend;
    private MaterialToolbar toolbar;
    private ImageView ivClientAvatarToolbar;
    private TextView tvClientNameToolbar;
    private TextView tvStatus;

    private List<ChatMessageAdapter.MessageData> messages;
    private String clientName;
    private int clientAvatar;
    private Handler mainHandler;
    private boolean isActivityDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.admin_chat_conversation_view);

            // Inicializar Handler principal
            mainHandler = new Handler(Looper.getMainLooper());
            isActivityDestroyed = false;

            getIntentData();
            initViews();
            setupToolbar();
            setupRecyclerView();
            setupSendButton();
            loadMessagesForClient();
            setupBottomNavigation();
            
        } catch (Exception e) {
            e.printStackTrace();
            // Si hay cualquier error en la inicialización, cierra la activity de manera segura
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;
        // Limpiar callbacks pendientes
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    private void getIntentData() {
        try {
            clientName = getIntent().getStringExtra("client_name");
            clientAvatar = getIntent().getIntExtra("client_avatar", R.drawable.ic_avatar_male_1);
            
            if (clientName == null || clientName.isEmpty()) {
                clientName = "Cliente";
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Datos por defecto si hay error obteniendo del intent
            clientName = "Cliente";
            clientAvatar = R.drawable.ic_avatar_male_1;
        }
    }

    private void initViews() {
        try {
            recyclerViewMessages = findViewById(R.id.recyclerView_messages);
            editTextMessage = findViewById(R.id.editText_message);
            fabSend = findViewById(R.id.fab_send);
            toolbar = findViewById(R.id.topAppBar);
            ivClientAvatarToolbar = findViewById(R.id.iv_client_avatar_toolbar);
            tvClientNameToolbar = findViewById(R.id.tv_client_name_toolbar);
            tvStatus = findViewById(R.id.tv_status);
        } catch (Exception e) {
            e.printStackTrace();
            // Si hay error al encontrar las vistas, marcar como destruida para evitar crashes posteriores
            isActivityDestroyed = true;
        }
    }

    private void setupToolbar() {
        if (toolbar == null || isActivityDestroyed) return;
        
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
            
            // Datos dinámicos según el cliente seleccionado
            if (tvClientNameToolbar != null) {
                tvClientNameToolbar.setText(clientName != null ? clientName : "Cliente");
            }
            if (ivClientAvatarToolbar != null) {
                ivClientAvatarToolbar.setImageResource(clientAvatar);
            }
            if (tvStatus != null) {
                tvStatus.setText("En línea");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        if (recyclerViewMessages == null) return;
        
        try {
            messages = new ArrayList<>();
            messageAdapter = new ChatMessageAdapter(messages);
            
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(true);
            recyclerViewMessages.setLayoutManager(layoutManager);
            recyclerViewMessages.setAdapter(messageAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSendButton() {
        if (fabSend != null) {
            fabSend.setOnClickListener(v -> sendMessage());
        }
    }

    private void sendMessage() {
        if (!isActivityInValidState()) return;
        
        String messageText = editTextMessage != null ? editTextMessage.getText().toString().trim() : "";
        
        if (!TextUtils.isEmpty(messageText) && messages != null && messageAdapter != null) {
            String currentTime = getCurrentTime();
            
            // Agregar mensaje del administrador de forma segura
            try {
                messages.add(new ChatMessageAdapter.MessageData(messageText, currentTime, true));
                messageAdapter.notifyItemInserted(messages.size() - 1);
                editTextMessage.setText("");
                
                // Scroll to the bottom de forma segura
                if (recyclerViewMessages != null) {
                    recyclerViewMessages.scrollToPosition(messages.size() - 1);
                }
                
                // Simular respuesta del cliente después de envío del admin
                simulateClientResponse(messageText);
            } catch (Exception e) {
                // Log el error pero no crash la app
                e.printStackTrace();
            }
        }
    }

    private void simulateClientResponse(String adminMessage) {
        if (!isActivityInValidState() || mainHandler == null) return;
        
        // Usar Handler en lugar de postDelayed del RecyclerView para evitar memory leaks
        mainHandler.postDelayed(() -> {
            // Verificar nuevamente si la Activity sigue viva
            if (!isActivityInValidState() || messages == null || messageAdapter == null) {
                return;
            }
            
            try {
                String clientResponse = getClientResponseForMessage(adminMessage, clientName);
                String currentTime = getCurrentTime();
                
                messages.add(new ChatMessageAdapter.MessageData(clientResponse, currentTime, false));
                messageAdapter.notifyItemInserted(messages.size() - 1);
                
                // Scroll to the bottom de forma segura
                if (recyclerViewMessages != null) {
                    recyclerViewMessages.scrollToPosition(messages.size() - 1);
                }
            } catch (Exception e) {
                // Log el error pero no crash la app
                e.printStackTrace();
            }
        }, 2000);
    }

    private String getClientResponseForMessage(String adminMessage, String client) {
        // Simular lógica del backend que determina la respuesta del cliente según el mensaje del admin
        
        if (adminMessage == null || client == null) {
            return "Gracias por contactarnos.";
        }
        
        String lowerMessage = adminMessage.toLowerCase();
        
        // Respuestas específicas por cliente y contenido del mensaje
        switch (client) {
            case "Alex Rodriguez":
                if (lowerMessage.contains("precio") || lowerMessage.contains("costo")) {
                    return "Perfecto, ese precio me parece bien. ¿Cómo procedo con la reserva?";
                } else if (lowerMessage.contains("disponibilidad") || lowerMessage.contains("fecha")) {
                    return "Excelente, esa fecha me conviene perfectamente.";
                } else if (lowerMessage.contains("incluye") || lowerMessage.contains("servicios")) {
                    return "Genial, todos esos servicios son justo lo que necesito.";
                }
                break;
                
            case "Maria Garcia":
                if (lowerMessage.contains("precio") || lowerMessage.contains("costo")) {
                    return "¿Hay algún descuento por grupo? Somos 4 personas.";
                } else if (lowerMessage.contains("horario") || lowerMessage.contains("hora")) {
                    return "Perfecto, esa hora nos queda muy bien.";
                }
                break;
                
            case "Carlos Mendoza":
                if (lowerMessage.contains("precio") || lowerMessage.contains("costo")) {
                    return "¿Aceptan tarjeta de crédito para el pago?";
                } else if (lowerMessage.contains("incluye")) {
                    return "¿También incluye el seguro de viaje?";
                }
                break;
        }
        
        // Respuestas genéricas cuando no hay coincidencia específica
        String[] genericResponses = {
            "Muchas gracias por la información.",
            "Perfecto, eso es exactamente lo que buscaba.",
            "Excelente servicio, gracias por la respuesta rápida.",
            "Entendido, me parece muy bien.",
            "Gracias por aclarar esas dudas."
        };
        
        int randomIndex = (int) (Math.random() * genericResponses.length);
        return genericResponses[randomIndex];
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void loadMessagesForClient() {
        if (isActivityDestroyed || messages == null || messageAdapter == null) return;
        
        try {
            // Simular datos del backend - En una app real, estos vendrían de una API
            // que retornaría un JSON con los mensajes de la conversación específica
            List<String[]> backendMessages = getBackendMessagesForClient(clientName != null ? clientName : "");
            
            // Procesar mensajes del "backend" y agregarlos a la lista
            for (String[] messageData : backendMessages) {
                if (messageData != null && messageData.length == 3) {
                    String messageText = messageData[0];
                    String time = messageData[1];
                    boolean isFromClient = Boolean.parseBoolean(messageData[2]);
                    
                    if (messageText != null && time != null) {
                        // isFromClient true = mensaje del cliente (fondo gris, lado izquierdo)
                        // isFromClient false = mensaje del administrador (fondo morado, lado derecho)
                        // Pero el adaptador usa isFromUser, donde true = usuario/admin (morado, derecha)
                        // Por lo tanto, invertimos: !isFromClient
                        messages.add(new ChatMessageAdapter.MessageData(messageText, time, !isFromClient));
                    }
                }
            }
            
            messageAdapter.notifyDataSetChanged();
            
            // Scroll to the bottom de forma segura
            if (!messages.isEmpty() && recyclerViewMessages != null) {
                recyclerViewMessages.scrollToPosition(messages.size() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String[]> getBackendMessagesForClient(String clientName) {
        // Simular respuesta del backend - En una app real, esto sería una llamada HTTP
        // que retornaría algo como: GET /api/admin/chats/{clientId}/messages
        List<String[]> backendData = new ArrayList<>();
        
        if (clientName == null) {
            // Datos por defecto para evitar null pointer
            backendData.add(new String[]{"Hola, necesito información sobre tours", "10:00", "true"});
            backendData.add(new String[]{"¡Hola! Con gusto te ayudo con la información", "10:02", "false"});
            return backendData;
        }
        
        // Cada array contiene: [mensaje, hora, esDelCliente]
        switch (clientName) {
            case "Alex Rodriguez":
                backendData.add(new String[]{"Hola, me interesa el tour a Machu Picchu", "14:30", "true"});
                backendData.add(new String[]{"¡Perfecto! Tenemos disponibilidad para este fin de semana", "14:32", "false"});
                backendData.add(new String[]{"¿Qué incluye el paquete?", "14:35", "true"});
                backendData.add(new String[]{"Incluye: transporte, guía, entradas y almuerzo típico", "14:37", "false"});
                backendData.add(new String[]{"Perfecto, me interesa el de 3 días", "14:40", "true"});
                break;
                
            case "Maria Garcia":
                backendData.add(new String[]{"Buenos días, ¿tienen tours familiares?", "09:15", "true"});
                backendData.add(new String[]{"¡Buenos días! Sí, tenemos paquetes especiales para familias", "09:18", "false"});
                backendData.add(new String[]{"Somos 4 personas, 2 adultos y 2 niños", "09:20", "true"});
                backendData.add(new String[]{"Perfecto, tenemos descuentos especiales para niños", "09:22", "false"});
                break;
                
            case "Carlos Mendoza":
                backendData.add(new String[]{"Hola, quiero información sobre tours corporativos", "16:45", "true"});
                backendData.add(new String[]{"¡Hola! Claro, manejamos tours para empresas", "16:47", "false"});
                backendData.add(new String[]{"¿Qué opciones tienen disponibles?", "16:50", "true"});
                backendData.add(new String[]{"Tenemos paquetes de team building y tours culturales", "16:52", "false"});
                break;
                
            case "Laura Torres":
                backendData.add(new String[]{"¿Tienen tours de aventura?", "11:30", "true"});
                backendData.add(new String[]{"Sí, tenemos tours de trekking y deportes extremos", "11:32", "false"});
                backendData.add(new String[]{"¿Cuál recomiendan para principiantes?", "11:35", "true"});
                backendData.add(new String[]{"Te recomiendo el tour de caminata suave por el valle", "11:37", "false"});
                break;
                
            default:
                // Mensajes genéricos para clientes sin conversación específica
                backendData.add(new String[]{"Hola, necesito información sobre tours", "10:00", "true"});
                backendData.add(new String[]{"¡Hola! Con gusto te ayudo con la información", "10:02", "false"});
                break;
        }
        
        return backendData;
    }

    private void setupBottomNavigation() {
        try {
            AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("chat");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            // Si hay error con la navegación, continúa sin ella
        }
    }

    // Método adicional para validar que la actividad está en estado válido
    private boolean isActivityInValidState() {
        return !isActivityDestroyed && !isFinishing();
    }
}
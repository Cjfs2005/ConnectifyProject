package com.example.connectifyproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.ChatMessageAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class cliente_chat_conversation extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private ChatMessageAdapter messageAdapter;
    private EditText editTextMessage;
    private FloatingActionButton fabSend;
    private MaterialToolbar toolbar;
    private ImageView ivCompanyLogoToolbar;
    private TextView tvCompanyNameToolbar;
    private TextView tvStatus;

    private List<ChatMessageAdapter.MessageData> messages;
    private String companyName;
    private int companyLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_chat_conversation);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSendButton();
        loadMessagesForCompany();
    }

    private void getIntentData() {
        companyName = getIntent().getStringExtra("company_name");
        companyLogo = getIntent().getIntExtra("company_logo", R.drawable.cliente_tour_lima);
        
        if (companyName == null) {
            companyName = "Lima Tours";
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
        
        // Datos dinámicos según la empresa seleccionada
        tvCompanyNameToolbar.setText(companyName);
        ivCompanyLogoToolbar.setImageResource(companyLogo);
        tvStatus.setText("En línea");
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(messages);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupSendButton() {
        fabSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        
        if (!TextUtils.isEmpty(messageText)) {
            String currentTime = getCurrentTime();
            
            // Agregar mensaje del usuario
            messages.add(new ChatMessageAdapter.MessageData(messageText, currentTime, true));
            messageAdapter.notifyItemInserted(messages.size() - 1);
            editTextMessage.setText("");
            
            // Scroll to the bottom
            recyclerViewMessages.scrollToPosition(messages.size() - 1);
            
            // Simular que el backend procesa el mensaje y devuelve una respuesta
            // En un backend real, aquí harías una llamada HTTP y recibirías la respuesta
            simulateBackendResponse(messageText);
        }
    }

    private void simulateBackendResponse(String userMessage) {
        // Simular respuesta del backend después de 2 segundos
        recyclerViewMessages.postDelayed(() -> {
            // En un backend real, esto vendría de la API
            String backendResponse = getBackendResponseForMessage(userMessage, companyName);
            String currentTime = getCurrentTime();
            
            messages.add(new ChatMessageAdapter.MessageData(backendResponse, currentTime, false));
            messageAdapter.notifyItemInserted(messages.size() - 1);
            
            // Scroll to the bottom
            recyclerViewMessages.scrollToPosition(messages.size() - 1);
        }, 2000);
    }

    private String getBackendResponseForMessage(String userMessage, String company) {
        // Simular lógica del backend que determina la respuesta según el mensaje y empresa
        // En un backend real, esto sería una llamada a la API que procesa el mensaje
        
        String lowerMessage = userMessage.toLowerCase();
        
        // Respuestas específicas por empresa y contenido del mensaje
        if (company.equals("Lima Tours")) {
            if (lowerMessage.contains("precio") || lowerMessage.contains("costo")) {
                return "Para Machu Picchu el precio es S/. 450 por persona. Incluye todo lo mencionado.";
            } else if (lowerMessage.contains("horario") || lowerMessage.contains("hora")) {
                return "Salimos a las 5:00 AM desde el hotel. El retorno es aproximadamente a las 8:00 PM.";
            } else if (lowerMessage.contains("incluye") || lowerMessage.contains("include")) {
                return "Incluye: transporte, guía, entradas, almuerzo típico y seguro de viaje.";
            }
        } else if (company.equals("Arequipa Adventures")) {
            if (lowerMessage.contains("precio") || lowerMessage.contains("costo")) {
                return "El tour al Colca de 3 días cuesta S/. 280 por persona.";
            } else if (lowerMessage.contains("incluye")) {
                return "Incluye: transporte, hospedaje 2 noches, todas las comidas y guía especializado.";
            }
        }
        
        // Respuestas genéricas cuando no hay coincidencia específica
        String[] genericResponses = {
            "¡Perfecto! Te ayudo con esa información.",
            "Claro, déjame verificar la disponibilidad para ti.",
            "Excelente pregunta, permíteme darte los detalles.",
            "Por supuesto, estoy aquí para ayudarte con tu consulta.",
            "¡Gracias por tu interés! Te proporciono la información."
        };
        
        int randomIndex = (int) (Math.random() * genericResponses.length);
        return genericResponses[randomIndex];
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void loadMessagesForCompany() {
        // Simular datos del backend - En una app real, estos vendrían de una API
        // que retornaría un JSON con los mensajes de la conversación específica
        List<String[]> backendMessages = getBackendMessagesForCompany(companyName);
        
        // Procesar mensajes del "backend" y agregarlos a la lista
        for (String[] messageData : backendMessages) {
            String messageText = messageData[0];
            String time = messageData[1];
            boolean isFromUser = Boolean.parseBoolean(messageData[2]);
            
            messages.add(new ChatMessageAdapter.MessageData(messageText, time, isFromUser));
        }
        
        messageAdapter.notifyDataSetChanged();
        
        // Scroll to the bottom
        if (messages.size() > 0) {
            recyclerViewMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private List<String[]> getBackendMessagesForCompany(String companyName) {
        // Simular respuesta del backend - En una app real, esto sería una llamada HTTP
        // que retornaría algo como: GET /api/chats/{companyId}/messages
        List<String[]> backendData = new ArrayList<>();
        
        // Cada array contiene: [mensaje, hora, esDelUsuario]
        if (companyName.equals("Lima Tours")) {
            backendData.add(new String[]{"Hola, me interesa el tour a Machu Picchu", "14:30", "true"});
            backendData.add(new String[]{"¡Hola! Por supuesto, tenemos disponibilidad para este fin de semana", "14:32", "false"});
            backendData.add(new String[]{"¿Qué incluye el paquete?", "14:35", "true"});
            backendData.add(new String[]{"El paquete incluye transporte, guía especializado, entradas y almuerzo típico", "14:37", "false"});
            backendData.add(new String[]{"Perfecto, ¿cuál es el precio?", "14:40", "true"});
            backendData.add(new String[]{"Para 2 personas el costo es de S/. 450 por persona. ¿Te parece bien?", "14:42", "false"});
            
        } else if (companyName.equals("Arequipa Adventures")) {
            backendData.add(new String[]{"Buenos días, ¿tienen tours al Colca?", "09:15", "true"});
            backendData.add(new String[]{"¡Buenos días! Sí, tenemos tours de 2 y 3 días al Cañón del Colca", "09:18", "false"});
            backendData.add(new String[]{"Perfecto, me interesa el de 3 días", "09:20", "true"});
            backendData.add(new String[]{"Excelente elección. El tour de 3 días cuesta S/. 280 por persona", "09:22", "false"});
            
        } else if (companyName.equals("Cusco Explorer")) {
            backendData.add(new String[]{"Hola, quería consultar sobre el tour al Valle Sagrado", "16:45", "true"});
            backendData.add(new String[]{"¡Hola! Claro, tenemos salidas diarias al Valle Sagrado", "16:47", "false"});
            backendData.add(new String[]{"¿Qué lugares visitamos?", "16:50", "true"});
            backendData.add(new String[]{"Visitamos Pisaq, Ollantaytambo y Chinchero. Incluye almuerzo buffet", "16:52", "false"});
            
        } else if (companyName.equals("Trujillo Expeditions")) {
            backendData.add(new String[]{"¿Tienen tours a Huacas del Sol y de la Luna?", "11:30", "true"});
            backendData.add(new String[]{"Sí, tenemos tours arqueológicos que incluyen ambas huacas", "11:32", "false"});
            backendData.add(new String[]{"¿Cuánto dura el recorrido?", "11:35", "true"});
            backendData.add(new String[]{"El tour completo dura aproximadamente 4 horas", "11:37", "false"});
            
        } else {
            // Mensajes genéricos para empresas sin conversación específica
            backendData.add(new String[]{"Hola, ¿en qué puedo ayudarte?", "10:00", "false"});
        }
        
        return backendData;
    }
}
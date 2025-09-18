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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
            ChatMessageAdapter.MessageData newMessage = new ChatMessageAdapter.MessageData(messageText, currentTime, true);
            
            messageAdapter.addMessage(newMessage);
            editTextMessage.setText("");
            
            // Scroll to the bottom
            recyclerViewMessages.scrollToPosition(messages.size() - 1);
            
            // Simulate company response after 2 seconds
            recyclerViewMessages.postDelayed(this::simulateCompanyResponse, 2000);
        }
    }

    private void simulateCompanyResponse() {
        String[] responses = {
            "¡Perfecto! Te ayudo con esa información.",
            "Claro, déjame verificar la disponibilidad.",
            "Excelente pregunta, te respondo enseguida.",
            "Por supuesto, estoy aquí para ayudarte.",
            "¡Gracias por contactarnos!"
        };
        
        int randomIndex = (int) (Math.random() * responses.length);
        String response = responses[randomIndex];
        String currentTime = getCurrentTime();
        
        ChatMessageAdapter.MessageData companyMessage = new ChatMessageAdapter.MessageData(response, currentTime, false);
        messageAdapter.addMessage(companyMessage);
        
        // Scroll to the bottom
        recyclerViewMessages.scrollToPosition(messages.size() - 1);
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void loadMessagesForCompany() {
        // Mensajes hardcodeados específicos por empresa
        Map<String, List<ChatMessageAdapter.MessageData>> companyMessages = getCompanyMessages();
        
        List<ChatMessageAdapter.MessageData> companySpecificMessages = companyMessages.get(companyName);
        if (companySpecificMessages != null) {
            messages.addAll(companySpecificMessages);
        } else {
            // Mensajes por defecto si no hay específicos
            messages.add(new ChatMessageAdapter.MessageData("Hola, ¿en qué puedo ayudarte?", "10:00", false));
        }
        
        messageAdapter.notifyDataSetChanged();
        
        // Scroll to the bottom
        if (messages.size() > 0) {
            recyclerViewMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private Map<String, List<ChatMessageAdapter.MessageData>> getCompanyMessages() {
        Map<String, List<ChatMessageAdapter.MessageData>> companyMessages = new HashMap<>();
        
        // Mensajes para Lima Tours
        List<ChatMessageAdapter.MessageData> limaMessages = new ArrayList<>();
        limaMessages.add(new ChatMessageAdapter.MessageData("Hola, me interesa el tour a Machu Picchu", "14:30", true));
        limaMessages.add(new ChatMessageAdapter.MessageData("¡Hola! Por supuesto, tenemos disponibilidad para este fin de semana", "14:32", false));
        limaMessages.add(new ChatMessageAdapter.MessageData("¿Qué incluye el paquete?", "14:35", true));
        limaMessages.add(new ChatMessageAdapter.MessageData("El paquete incluye transporte, guía especializado, entradas y almuerzo típico", "14:37", false));
        companyMessages.put("Lima Tours", limaMessages);
        
        // Mensajes para Arequipa Adventures
        List<ChatMessageAdapter.MessageData> arequipaMessages = new ArrayList<>();
        arequipaMessages.add(new ChatMessageAdapter.MessageData("Buenos días, ¿tienen tours al Colca?", "09:15", true));
        arequipaMessages.add(new ChatMessageAdapter.MessageData("¡Buenos días! Sí, tenemos tours de 2 y 3 días al Cañón del Colca", "09:18", false));
        arequipaMessages.add(new ChatMessageAdapter.MessageData("Perfecto, me interesa el de 3 días", "09:20", true));
        companyMessages.put("Arequipa Adventures", arequipaMessages);
        
        // Mensajes para Cusco Explorer
        List<ChatMessageAdapter.MessageData> cuscoMessages = new ArrayList<>();
        cuscoMessages.add(new ChatMessageAdapter.MessageData("Hola, quería consultar sobre el tour al Valle Sagrado", "16:45", true));
        cuscoMessages.add(new ChatMessageAdapter.MessageData("¡Hola! Claro, tenemos salidas diarias al Valle Sagrado", "16:47", false));
        companyMessages.put("Cusco Explorer", cuscoMessages);
        
        // Mensajes para Trujillo Expeditions
        List<ChatMessageAdapter.MessageData> trujilloMessages = new ArrayList<>();
        trujilloMessages.add(new ChatMessageAdapter.MessageData("¿Tienen tours a Huacas del Sol y de la Luna?", "11:30", true));
        trujilloMessages.add(new ChatMessageAdapter.MessageData("Sí, tenemos tours arqueológicos que incluyen ambas huacas", "11:32", false));
        companyMessages.put("Trujillo Expeditions", trujilloMessages);
        
        return companyMessages;
    }
}
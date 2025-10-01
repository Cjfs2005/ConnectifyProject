package com.example.connectifyproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class admin_chat_conversation extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Esta actividad será implementada más tarde para conversaciones individuales
        // Por ahora solo maneja el intent para evitar crashes
        String clientName = getIntent().getStringExtra("client_name");
        int clientAvatar = getIntent().getIntExtra("client_avatar", 0);
        
        // TODO: Implementar layout de conversación individual
        finish(); // Por ahora regresa inmediatamente
    }
}
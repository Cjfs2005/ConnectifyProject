package com.example.connectifyproject.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.connectifyproject.R;
import com.example.connectifyproject.admin_chat_conversation;
import com.example.connectifyproject.cliente_chat_conversation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatNotificationService {
    private static final String TAG = "ChatNotificationService";
    private static final String CHANNEL_ID = "chat_messages_channel";
    private static final String CHANNEL_NAME = "Mensajes de Chat";
    private static final String CHANNEL_DESC = "Notificaciones de nuevos mensajes de chat";
    
    private final Context context;
    private final NotificationManager notificationManager;
    private final FirebaseFirestore db;
    
    public ChatNotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.db = FirebaseFirestore.getInstance();
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Canal de notificaciones de chat creado");
        }
    }
    
    /**
     * Envía una notificación de nuevo mensaje
     * @param senderName Nombre del remitente
     * @param messageText Texto del mensaje
     * @param chatId ID del chat
     * @param senderRole Rol del remitente ("CLIENT" o "ADMIN")
     * @param receiverId ID del receptor
     * @param receiverRole Rol del receptor
     */
    public void sendMessageNotification(String senderName, String messageText, 
                                       String chatId, String senderRole,
                                       String receiverId, String receiverRole) {
        // No enviar notificación si el usuario está en la conversación
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                             FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (currentUserId != null && currentUserId.equals(receiverId)) {
            Log.d(TAG, "El receptor es el usuario actual, verificando si está en la conversación");
            // Aquí podrías verificar si el usuario está viendo la conversación actualmente
            // Por ahora, siempre enviamos la notificación
        }
        
        // Crear intent para abrir la conversación
        Intent intent;
        if ("CLIENT".equals(receiverRole)) {
            intent = new Intent(context, cliente_chat_conversation.class);
            intent.putExtra("chatId", chatId);
            intent.putExtra("companyName", senderName);
            intent.putExtra("companyId", receiverId);
        } else {
            intent = new Intent(context, admin_chat_conversation.class);
            intent.putExtra("chatId", chatId);
            intent.putExtra("clientName", senderName);
            intent.putExtra("clientId", receiverId);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chat_24)
            .setContentTitle(senderName)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(messageText));
        
        // Mostrar la notificación
        notificationManager.notify(chatId.hashCode(), builder.build());
        Log.d(TAG, "Notificación de chat enviada: " + senderName + " -> " + receiverId);
    }
    
    /**
     * Cancela una notificación específica de chat
     */
    public void cancelNotification(String chatId) {
        notificationManager.cancel(chatId.hashCode());
    }
    
    /**
     * Cancela todas las notificaciones de chat
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}

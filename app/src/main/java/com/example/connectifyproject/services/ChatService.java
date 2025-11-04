package com.example.connectifyproject.services;

import android.util.Log;

import com.example.connectifyproject.model.Chat;
import com.example.connectifyproject.model.ChatMessage;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class ChatService {
    private static final String TAG = "ChatService";
    private static final String COLLECTION_CHATS = "chats";
    private static final String COLLECTION_MESSAGES = "messages";
    
    // CONFIGURACIÓN: Cambiar a false para implementación real
    public static final boolean TEST_MODE = true;
    
    private final FirebaseFirestore db;
    
    public ChatService() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Crea o obtiene un chat entre un cliente y una empresa
     */
    public void getOrCreateChat(String clientId, String clientName, String clientPhotoUrl,
                               String adminId, String adminName, String adminPhotoUrl,
                               OnChatReadyListener listener) {
        String chatId = generateChatId(clientId, adminId);
        
        db.collection(COLLECTION_CHATS)
            .document(chatId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Chat chat = documentSnapshot.toObject(Chat.class);
                    listener.onChatReady(chat);
                } else {
                    // Crear nuevo chat
                    Chat newChat = new Chat(clientId, clientName, clientPhotoUrl,
                                          adminId, adminName, adminPhotoUrl);
                    
                    db.collection(COLLECTION_CHATS)
                        .document(chatId)
                        .set(newChat)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Chat creado exitosamente: " + chatId);
                            listener.onChatReady(newChat);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al crear chat", e);
                            listener.onError(e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener chat", e);
                listener.onError(e);
            });
    }
    
    /**
     * Envía un mensaje en un chat
     */
    public void sendMessage(ChatMessage message, OnMessageSentListener listener) {
        // Generar ID único para el mensaje
        String messageId = db.collection(COLLECTION_CHATS)
            .document(message.getChatId())
            .collection(COLLECTION_MESSAGES)
            .document()
            .getId();
        
        message.setMessageId(messageId);
        
        // Guardar mensaje
        db.collection(COLLECTION_CHATS)
            .document(message.getChatId())
            .collection(COLLECTION_MESSAGES)
            .document(messageId)
            .set(message)
            .addOnSuccessListener(aVoid -> {
                // Actualizar el chat con el último mensaje
                updateChatLastMessage(message);
                Log.d(TAG, "Mensaje enviado: " + messageId);
                listener.onMessageSent(message);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al enviar mensaje", e);
                listener.onError(e);
            });
    }
    
    /**
     * Actualiza el último mensaje y contador de no leídos en el chat
     */
    private void updateChatLastMessage(ChatMessage message) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", message.getMessageText());
        updates.put("lastMessageTime", message.getTimestamp());
        
        // Incrementar contador de no leídos del receptor
        String unreadField = "CLIENT".equals(message.getSenderRole()) ? 
                           "unreadCountAdmin" : "unreadCountClient";
        
        db.collection(COLLECTION_CHATS)
            .document(message.getChatId())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long currentUnread = documentSnapshot.getLong(unreadField);
                    int newUnread = (currentUnread != null ? currentUnread.intValue() : 0) + 1;
                    updates.put(unreadField, newUnread);
                    
                    db.collection(COLLECTION_CHATS)
                        .document(message.getChatId())
                        .update(updates)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat actualizado"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar chat", e));
                }
            });
    }
    
    /**
     * Marca los mensajes como leídos
     */
    public void markMessagesAsRead(String chatId, String userRole) {
        String unreadField = "CLIENT".equals(userRole) ? 
                           "unreadCountClient" : "unreadCountAdmin";
        
        Map<String, Object> updates = new HashMap<>();
        updates.put(unreadField, 0);
        
        db.collection(COLLECTION_CHATS)
            .document(chatId)
            .update(updates)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Mensajes marcados como leídos"))
            .addOnFailureListener(e -> Log.e(TAG, "Error al marcar mensajes como leídos", e));
    }
    
    /**
     * Obtiene todos los chats de un usuario
     * En TEST_MODE: muestra todos los chats
     * En modo real: solo muestra chats activos
     */
    public void getUserChats(String userId, String userRole, OnChatsLoadedListener listener) {
        String fieldName = "CLIENT".equals(userRole) ? "clientId" : "adminId";
        
        Query query = db.collection(COLLECTION_CHATS)
            .whereEqualTo(fieldName, userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING);
        
        if (!TEST_MODE) {
            query = query.whereEqualTo("active", true);
        }
        
        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error al escuchar chats", error);
                listener.onError(error);
                return;
            }
            
            if (value != null) {
                listener.onChatsLoaded(value.toObjects(Chat.class));
            }
        });
    }
    
    /**
     * Escucha los mensajes de un chat en tiempo real
     */
    public void listenToMessages(String chatId, OnMessagesLoadedListener listener) {
        db.collection(COLLECTION_CHATS)
            .document(chatId)
            .collection(COLLECTION_MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error al escuchar mensajes", error);
                    listener.onError(error);
                    return;
                }
                
                if (value != null) {
                    listener.onMessagesLoaded(value.toObjects(ChatMessage.class));
                }
            });
    }
    
    /**
     * Genera un ID único para el chat
     */
    private String generateChatId(String clientId, String adminId) {
        if (clientId.compareTo(adminId) < 0) {
            return clientId + "_" + adminId;
        } else {
            return adminId + "_" + clientId;
        }
    }
    
    // Interfaces de callbacks
    public interface OnChatReadyListener {
        void onChatReady(Chat chat);
        void onError(Exception e);
    }
    
    public interface OnMessageSentListener {
        void onMessageSent(ChatMessage message);
        void onError(Exception e);
    }
    
    public interface OnChatsLoadedListener {
        void onChatsLoaded(java.util.List<Chat> chats);
        void onError(Exception e);
    }
    
    public interface OnMessagesLoadedListener {
        void onMessagesLoaded(java.util.List<ChatMessage> messages);
        void onError(Exception e);
    }
}

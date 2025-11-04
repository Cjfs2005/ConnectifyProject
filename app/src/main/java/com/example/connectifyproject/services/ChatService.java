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
     * Obtiene un chat existente (sin crear uno nuevo)
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
                    // Retornar un chat temporal (no guardado en BD hasta que se envíe el primer mensaje)
                    Chat tempChat = new Chat(clientId, clientName, clientPhotoUrl,
                                          adminId, adminName, adminPhotoUrl);
                    listener.onChatReady(tempChat);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener chat", e);
                listener.onError(e);
            });
    }
    
    /**
     * Envía un mensaje en un chat (crea el chat si es el primer mensaje)
     */
    public void sendMessage(ChatMessage message, OnMessageSentListener listener) {
        sendMessage(message, null, null, null, null, listener);
    }
    
    /**
     * Envía un mensaje en un chat con información completa para crear el chat si es necesario
     */
    public void sendMessage(ChatMessage message, String clientName, String clientPhotoUrl, 
                          String adminName, String adminPhotoUrl, OnMessageSentListener listener) {
        String chatId = message.getChatId();
        
        // Primero verificar si el chat existe, si no, crearlo
        db.collection(COLLECTION_CHATS)
            .document(chatId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    // Crear el chat primero (es el primer mensaje)
                    createChatFromMessage(message, clientName, clientPhotoUrl, 
                                        adminName, adminPhotoUrl, listener);
                } else {
                    // El chat ya existe, solo enviar el mensaje
                    saveMessage(message, listener);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al verificar chat", e);
                listener.onError(e);
            });
    }
    
    /**
     * Crea el chat en la base de datos a partir del primer mensaje
     */
    private void createChatFromMessage(ChatMessage message, String clientName, String clientPhotoUrl,
                                      String adminName, String adminPhotoUrl, 
                                      OnMessageSentListener listener) {
        // Extraer IDs del chatId (formato: clientId_adminId o adminId_clientId)
        String[] ids = message.getChatId().split("_");
        String clientId = ids[0].compareTo(ids[1]) < 0 ? ids[0] : ids[1];
        String adminId = ids[0].compareTo(ids[1]) < 0 ? ids[1] : ids[0];
        
        // Crear el chat con información completa
        Chat newChat = new Chat();
        newChat.setChatId(message.getChatId());
        newChat.setClientId(clientId);
        newChat.setAdminId(adminId);
        newChat.setClientName(clientName != null ? clientName : "Cliente");
        newChat.setClientPhotoUrl(clientPhotoUrl);
        newChat.setAdminName(adminName != null ? adminName : "Empresa");
        newChat.setAdminPhotoUrl(adminPhotoUrl);
        newChat.setLastMessage(message.getMessageText());
        newChat.setLastMessageTime(message.getTimestamp());
        newChat.setActive(true);
        
        db.collection(COLLECTION_CHATS)
            .document(message.getChatId())
            .set(newChat)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Chat creado con primer mensaje: " + message.getChatId());
                saveMessage(message, listener);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al crear chat", e);
                listener.onError(e);
            });
    }
    
    /**
     * Guarda el mensaje en la colección de mensajes
     */
    private void saveMessage(ChatMessage message, OnMessageSentListener listener) {
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
        updates.put("lastSenderId", message.getSenderId()); // Guardar quién envió el último mensaje
        
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
    
    /**
     * Obtiene la URL de la foto de perfil de un usuario desde Firebase
     */
    public void getUserPhotoUrl(String userId, OnPhotoUrlLoadedListener listener) {
        db.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String photoUrl = documentSnapshot.getString("photoUrl");
                    listener.onSuccess(photoUrl);
                } else {
                    listener.onSuccess(null);
                }
            })
            .addOnFailureListener(listener::onFailure);
    }
    
    /**
     * Obtiene el nombre de empresa de un administrador desde Firebase
     */
    public void getCompanyName(String adminId, OnCompanyNameLoadedListener listener) {
        db.collection("usuarios")
            .document(adminId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nombreEmpresa = documentSnapshot.getString("nombreEmpresa");
                    // Si no hay nombreEmpresa, usar nombresApellidos como fallback
                    if (nombreEmpresa == null || nombreEmpresa.isEmpty()) {
                        nombreEmpresa = documentSnapshot.getString("nombresApellidos");
                    }
                    listener.onSuccess(nombreEmpresa);
                } else {
                    listener.onSuccess(null);
                }
            })
            .addOnFailureListener(listener::onFailure);
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
    
    public interface OnPhotoUrlLoadedListener {
        void onSuccess(String photoUrl);
        void onFailure(Exception e);
    }
    
    public interface OnCompanyNameLoadedListener {
        void onSuccess(String companyName);
        void onFailure(Exception e);
    }
}

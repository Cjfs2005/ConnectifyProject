package com.example.connectifyproject.model;

import com.google.firebase.Timestamp;

public class Chat {
    private String chatId;
    private String clientId;
    private String clientName;
    private String clientPhotoUrl;
    private String adminId;
    private String adminName; // Nombre de la empresa
    private String adminPhotoUrl;
    private String lastMessage;
    private String lastSenderId; // ID del usuario que envió el último mensaje
    private Timestamp lastMessageTime;
    private int unreadCountClient; // Mensajes no leídos por el cliente
    private int unreadCountAdmin; // Mensajes no leídos por el admin
    private boolean active; // Para implementación real: solo mostrar si está activo

    public Chat() {
        // Constructor vacío requerido por Firebase
    }

    public Chat(String clientId, String clientName, String clientPhotoUrl,
                String adminId, String adminName, String adminPhotoUrl) {
        this.chatId = generateChatId(clientId, adminId);
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientPhotoUrl = clientPhotoUrl;
        this.adminId = adminId;
        this.adminName = adminName;
        this.adminPhotoUrl = adminPhotoUrl;
        this.lastMessage = "";
        this.lastSenderId = "";
        this.lastMessageTime = Timestamp.now();
        this.unreadCountClient = 0;
        this.unreadCountAdmin = 0;
        this.active = true; // En modo prueba siempre true
    }

    // Genera un ID único para el chat basado en clientId y adminId
    private String generateChatId(String clientId, String adminId) {
        // Ordenar alfabéticamente para que siempre sea el mismo ID
        if (clientId.compareTo(adminId) < 0) {
            return clientId + "_" + adminId;
        } else {
            return adminId + "_" + clientId;
        }
    }

    // Getters y Setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhotoUrl() {
        return clientPhotoUrl;
    }

    public void setClientPhotoUrl(String clientPhotoUrl) {
        this.clientPhotoUrl = clientPhotoUrl;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminPhotoUrl() {
        return adminPhotoUrl;
    }

    public void setAdminPhotoUrl(String adminPhotoUrl) {
        this.adminPhotoUrl = adminPhotoUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastSenderId() {
        return lastSenderId;
    }

    public void setLastSenderId(String lastSenderId) {
        this.lastSenderId = lastSenderId;
    }

    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Timestamp lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCountClient() {
        return unreadCountClient;
    }

    public void setUnreadCountClient(int unreadCountClient) {
        this.unreadCountClient = unreadCountClient;
    }

    public int getUnreadCountAdmin() {
        return unreadCountAdmin;
    }

    public void setUnreadCountAdmin(int unreadCountAdmin) {
        this.unreadCountAdmin = unreadCountAdmin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

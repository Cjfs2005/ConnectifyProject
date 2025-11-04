package com.example.connectifyproject.model;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String messageId;
    private String chatId;
    private String senderId;
    private String senderName;
    private String senderRole; // "CLIENT" o "ADMIN"
    private String messageText;
    private Timestamp timestamp;
    private boolean read;
    private String messageType; // "text", "image", etc. (para futuro)

    public ChatMessage() {
        // Constructor vacío requerido por Firebase
    }

    public ChatMessage(String chatId, String senderId, String senderName, String senderRole, String messageText) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.messageText = messageText;
        this.timestamp = Timestamp.now();
        this.read = false;
        this.messageType = "text";
    }

    // Getters y Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}

package com.example.connectifyproject.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class AdminChatClient implements Serializable {
    private String id;
    private String name;
    private String lastMessage;
    private String timeAgo;
    private int photoResource;
    private boolean isOnline;
    private String clientId;
    private String clientPhotoUrl;
    private Timestamp lastMessageTime; // Para ordenar

    public AdminChatClient(String name, String lastMessage, String timeAgo, int photoResource) {
        this.id = "";
        this.name = name;
        this.lastMessage = lastMessage;
        this.timeAgo = timeAgo;
        this.photoResource = photoResource;
        this.isOnline = false;
    }

    public AdminChatClient(String id, String name, String lastMessage, String timeAgo, int photoResource, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timeAgo = timeAgo;
        this.photoResource = photoResource;
        this.isOnline = isOnline;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTimeAgo() { return timeAgo; }
    public int getPhotoResource() { return photoResource; }
    public boolean isOnline() { return isOnline; }
    public String getClientId() { return clientId; }
    public String getClientPhotoUrl() { return clientPhotoUrl; }
    public Timestamp getLastMessageTime() { return lastMessageTime; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    public void setPhotoResource(int photoResource) { this.photoResource = photoResource; }
    public void setOnline(boolean online) { isOnline = online; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setClientPhotoUrl(String clientPhotoUrl) { this.clientPhotoUrl = clientPhotoUrl; }
    public void setLastMessageTime(Timestamp lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}

package com.example.connectifyproject.models;

import java.io.Serializable;

public class Cliente_ChatCompany implements Serializable {
    private String id;
    private String name;
    private String lastMessage;
    private String timeAgo;
    private int logoResource;
    private boolean isOnline;

    public Cliente_ChatCompany(String name, String lastMessage, String timeAgo, int logoResource) {
        this.id = "";
        this.name = name;
        this.lastMessage = lastMessage;
        this.timeAgo = timeAgo;
        this.logoResource = logoResource;
        this.isOnline = false;
    }

    public Cliente_ChatCompany(String id, String name, String lastMessage, String timeAgo, int logoResource, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timeAgo = timeAgo;
        this.logoResource = logoResource;
        this.isOnline = isOnline;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTimeAgo() { return timeAgo; }
    public int getLogoResource() { return logoResource; }
    public boolean isOnline() { return isOnline; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    public void setLogoResource(int logoResource) { this.logoResource = logoResource; }
    public void setOnline(boolean online) { isOnline = online; }
}
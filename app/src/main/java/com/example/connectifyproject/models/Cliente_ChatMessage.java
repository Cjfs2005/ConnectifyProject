package com.example.connectifyproject.models;

import java.io.Serializable;

public class Cliente_ChatMessage implements Serializable {
    private String id;
    private String message;
    private String time;
    private boolean isFromUser;
    private String companyId;
    private String userId;

    public Cliente_ChatMessage(String message, String time, boolean isFromUser) {
        this.id = "";
        this.message = message;
        this.time = time;
        this.isFromUser = isFromUser;
        this.companyId = "";
        this.userId = "";
    }

    public Cliente_ChatMessage(String id, String message, String time, boolean isFromUser, String companyId, String userId) {
        this.id = id;
        this.message = message;
        this.time = time;
        this.isFromUser = isFromUser;
        this.companyId = companyId;
        this.userId = userId;
    }

    // Getters
    public String getId() { return id; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public boolean isFromUser() { return isFromUser; }
    public String getCompanyId() { return companyId; }
    public String getUserId() { return userId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setMessage(String message) { this.message = message; }
    public void setTime(String time) { this.time = time; }
    public void setFromUser(boolean fromUser) { isFromUser = fromUser; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public void setUserId(String userId) { this.userId = userId; }
}
package com.example.connectifyproject.models;

import java.io.Serializable;

public class Cliente_Notification implements Serializable {
    private String title;
    private String message;
    private String time;
    private String date;
    private String id;
    private boolean isRead;

    public Cliente_Notification(String title, String message, String time, String date) {
        this.title = title;
        this.message = message;
        this.time = time;
        this.date = date;
        this.id = "";
        this.isRead = false;
    }

    public Cliente_Notification(String id, String title, String message, String time, String date, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.date = date;
        this.isRead = isRead;
    }

    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getId() { return id; }
    public boolean isRead() { return isRead; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setTime(String time) { this.time = time; }
    public void setDate(String date) { this.date = date; }
    public void setId(String id) { this.id = id; }
    public void setRead(boolean read) { isRead = read; }
}
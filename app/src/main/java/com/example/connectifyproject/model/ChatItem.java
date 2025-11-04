package com.example.connectifyproject.model;

public class ChatItem {
    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_DATE_SEPARATOR = 1;
    
    private int type;
    private ChatMessage message;
    private String dateText;
    
    // Constructor para mensajes
    public ChatItem(ChatMessage message) {
        this.type = TYPE_MESSAGE;
        this.message = message;
    }
    
    // Constructor para separadores de fecha
    public ChatItem(String dateText) {
        this.type = TYPE_DATE_SEPARATOR;
        this.dateText = dateText;
    }
    
    public int getType() {
        return type;
    }
    
    public ChatMessage getMessage() {
        return message;
    }
    
    public String getDateText() {
        return dateText;
    }
}

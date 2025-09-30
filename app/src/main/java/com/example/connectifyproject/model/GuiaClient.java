package com.example.connectifyproject.model;

public class GuiaClient {
    private String name;
    private String code;
    private String status;
    private String time;
    private String phone;
    private boolean done;
    private boolean noAsistio;
    private int rating; // 0 if not rated, >0 after checkout with rating

    public GuiaClient(String name, String code, String status, String time, String phone, boolean done, boolean noAsistio, int rating) {
        this.name = name;
        this.code = code;
        this.status = status;
        this.time = time;
        this.phone = phone;
        this.done = done;
        this.noAsistio = noAsistio;
        this.rating = rating;
    }

    // Getters
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getStatus() { return status; }
    public String getTime() { return time; }
    public String getPhone() { return phone; }
    public boolean isDone() { return done; }
    public boolean isNoAsistio() { return noAsistio; }
    public int getRating() { return rating; }
}
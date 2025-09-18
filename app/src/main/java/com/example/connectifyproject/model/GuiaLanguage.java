package com.example.connectifyproject.model;

public class GuiaLanguage {
    private String name;  // "Español", "Inglés", etc.

    public GuiaLanguage(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public String toString() {
        return name;
    }
}
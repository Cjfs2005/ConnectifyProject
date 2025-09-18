package com.example.connectifyproject.model;

public class User {
    private final String name;
    private final String dni;
    private final String company;
    private final Role role;

    public User(String name, String dni, String company, Role role) {
        this.name = name;
        this.dni = dni;
        this.company = company;
        this.role = role;
    }

    public String getName() { return name; }
    public String getDni() { return dni; }
    public String getCompany() { return company; }
    public Role getRole() { return role; }

    public String getInitial() {
        return (name != null && !name.isEmpty()) ? name.substring(0,1).toUpperCase() : "?";
    }
}

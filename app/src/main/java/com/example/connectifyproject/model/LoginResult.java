package com.example.connectifyproject.model;

public class LoginResult {
    private final boolean success;
    private final UserType userType;

    public LoginResult(boolean success, UserType userType) {
        this.success = success;
        this.userType = userType;
    }

    public boolean isSuccess() {
        return success;
    }

    public UserType getUserType() {
        return userType;
    }

    public enum UserType {
        SUPERADMIN,
        ADMIN,
        CLIENTE,
        GUIA
    }
}
package com.example.connectifyproject.models;

import java.io.Serializable;

/**
 * Modelo para representar un método de pago del usuario cliente
 * Utilizado en la gestión de métodos de pago y funciones relacionadas
 */
public class Cliente_PaymentMethod implements Serializable {
    
    private String id;
    private String cardNumber;
    private String expiryDate;
    private String cardHolderName;
    private String cardType; // "VISA", "MASTERCARD", "AMERICAN_EXPRESS", etc.
    private boolean isDefault;
    private String lastFourDigits;
    private String cvv; // Generalmente no se almacena, solo para procesamiento temporal

    // Constructor completo
    public Cliente_PaymentMethod(String id, String cardNumber, String expiryDate, 
                               String cardHolderName, String cardType, boolean isDefault) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cardHolderName = cardHolderName;
        this.cardType = cardType;
        this.isDefault = isDefault;
        this.lastFourDigits = extractLastFourDigits(cardNumber);
    }

    // Constructor básico (para compatibilidad con código existente)
    public Cliente_PaymentMethod(String cardNumber, String expiryDate) {
        this("", cardNumber, expiryDate, "", "VISA", false);
    }

    // Constructor vacío
    public Cliente_PaymentMethod() {
        this("", "", "", "", "VISA", false);
    }

    // Getters
    public String getId() { return id; }
    public String getCardNumber() { return cardNumber; }
    public String getExpiryDate() { return expiryDate; }
    public String getCardHolderName() { return cardHolderName; }
    public String getCardType() { return cardType; }
    public boolean isDefault() { return isDefault; }
    public String getLastFourDigits() { return lastFourDigits; }
    public String getCvv() { return cvv; }

    // Método para obtener número de tarjeta enmascarado
    public String getMaskedCardNumber() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            return "**** **** **** " + lastFour;
        }
        return cardNumber;
    }

    // Método para extraer últimos 4 dígitos
    private String extractLastFourDigits(String cardNumber) {
        if (cardNumber != null && cardNumber.replaceAll("\\s", "").length() >= 4) {
            String cleanNumber = cardNumber.replaceAll("\\s", "");
            return cleanNumber.substring(cleanNumber.length() - 4);
        }
        return "";
    }

    // Método para obtener texto de vencimiento formateado
    public String getFormattedExpiryText() {
        return "Fecha de vencimiento " + expiryDate;
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setCardNumber(String cardNumber) { 
        this.cardNumber = cardNumber;
        this.lastFourDigits = extractLastFourDigits(cardNumber);
    }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    // Método estático para crear métodos de pago de ejemplo (hardcodeados)
    public static Cliente_PaymentMethod crearEjemploVisa() {
        return new Cliente_PaymentMethod("pm_001", "**** **** **** 2934", "12/28", 
                                       "Jorge Romero", "VISA", true);
    }

    public static Cliente_PaymentMethod crearEjemploMastercard() {
        return new Cliente_PaymentMethod("pm_002", "**** **** **** 1340", "11/27", 
                                       "Jorge Romero", "MASTERCARD", false);
    }

    @Override
    public String toString() {
        return "Cliente_PaymentMethod{" +
                "cardType='" + cardType + '\'' +
                ", lastFourDigits='" + lastFourDigits + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
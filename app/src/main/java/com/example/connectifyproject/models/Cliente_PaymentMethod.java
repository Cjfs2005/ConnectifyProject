package com.example.connectifyproject.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**
 * Modelo para métodos de pago simulados de Cliente
 * Representa una tarjeta guardada en el perfil
 */
public class Cliente_PaymentMethod implements Serializable {
    
    private String id;
    private String cardNumber;        // Solo en simulación - en producción NO guardar
    private String cardBrand;         // Visa, Mastercard, Amex
    private String last4Digits;       // Para mostrar en UI (•••• 4242)
    private String cardholderName;    // Nombre del titular
    private String expiryMonth;       // MM (01-12)
    private String expiryYear;        // YYYY (2025)
    private String cardType;          // credit, debit
    private boolean isDefault;        // Tarjeta predeterminada
    private boolean isSimulated;      // Flag de simulación
    private String nickname;          // Apodo opcional ("Mi Visa", etc)
    private Timestamp createdAt;      // Fecha de creación
    private Timestamp lastUsedAt;     // Última vez usada
    
    // Campo antiguo para retrocompatibilidad
    private String expiryDate;        // Formato antiguo
    
    // Constructor vacío requerido por Firestore
    public Cliente_PaymentMethod() {
    }
    
    // Constructor completo nuevo
    public Cliente_PaymentMethod(String id, String cardNumber, String cardBrand, 
                                String last4Digits, String cardholderName,
                                String expiryMonth, String expiryYear, 
                                String cardType, boolean isDefault, 
                                boolean isSimulated, String nickname,
                                Timestamp createdAt, Timestamp lastUsedAt) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.cardBrand = cardBrand;
        this.last4Digits = last4Digits;
        this.cardholderName = cardholderName;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cardType = cardType;
        this.isDefault = isDefault;
        this.isSimulated = isSimulated;
        this.nickname = nickname;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        
        // Campo de retrocompatibilidad
        this.expiryDate = expiryMonth + "/" + expiryYear;
    }
    
    // Constructor antiguo para retrocompatibilidad
    public Cliente_PaymentMethod(String id, String cardNumber, String expiryDate, 
                               String cardHolderName, String cardType, boolean isDefault) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cardholderName = cardHolderName;
        this.cardType = cardType;
        this.isDefault = isDefault;
        this.last4Digits = extractLastFourDigits(cardNumber);
        this.isSimulated = true;
        
        // Parsear expiryDate (MM/YYYY)
        if (expiryDate != null && expiryDate.contains("/")) {
            String[] parts = expiryDate.split("/");
            this.expiryMonth = parts[0];
            this.expiryYear = parts[1];
        }
        
        // Detectar marca si no está especificada
        if (cardNumber != null && !cardNumber.startsWith("*")) {
            this.cardBrand = com.example.connectifyproject.utils.Cliente_CardValidator.detectCardBrand(cardNumber);
        } else {
            this.cardBrand = cardType;
        }
    }
    
    // Getters nuevos
    public String getId() { return id; }
    public String getCardNumber() { return cardNumber; }
    public String getCardBrand() { return cardBrand; }
    public String getLast4Digits() { return last4Digits; }
    public String getCardholderName() { return cardholderName; }
    public String getExpiryMonth() { return expiryMonth; }
    public String getExpiryYear() { return expiryYear; }
    public String getCardType() { return cardType; }
    public boolean isDefault() { return isDefault; }
    public boolean isSimulated() { return isSimulated; }
    public String getNickname() { return nickname; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getLastUsedAt() { return lastUsedAt; }
    
    // Getters antiguos para retrocompatibilidad
    public String getExpiryDate() { return expiryMonth + "/" + expiryYear; }

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
        if (expiryMonth != null && expiryYear != null) {
            return "Vence " + expiryMonth + "/" + expiryYear;
        }
        return "Fecha de vencimiento " + expiryDate;
    }

    // Setters
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    public void setLast4Digits(String last4Digits) { this.last4Digits = last4Digits; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }
    public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }
    public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }
    public void setSimulated(boolean isSimulated) { this.isSimulated = isSimulated; }
    public void setIsSimulated(boolean isSimulated) { this.isSimulated = isSimulated; }
    public void setIsDefault(boolean isDefault) { this.isDefault = isDefault; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setLastUsedAt(Timestamp lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    
    public void setId(String id) { this.id = id; }
    public void setCardNumber(String cardNumber) { 
        this.cardNumber = cardNumber;
        this.last4Digits = extractLastFourDigits(cardNumber);
    }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    
    /**
     * Obtiene el nombre para mostrar en UI
     * Ej: "Visa •••• 4242" o "Mi Visa •••• 4242"
     */
    public String getDisplayName() {
        String lastDigits = (last4Digits != null) ? last4Digits : "****";
        
        if (nickname != null && !nickname.isEmpty()) {
            return nickname + " •••• " + lastDigits;
        }
        if (cardBrand != null && !cardBrand.isEmpty()) {
            return cardBrand + " •••• " + lastDigits;
        }
        if (cardType != null && !cardType.isEmpty()) {
            return cardType + " •••• " + lastDigits;
        }
        return "Tarjeta •••• " + lastDigits;
    }
    
    /**
     * Obtiene la fecha de vencimiento formateada
     * Ej: "12/2027"
     */
    public String getExpiryFormatted() {
        if (expiryMonth != null && expiryYear != null) {
            return expiryMonth + "/" + expiryYear;
        }
        if (expiryDate != null && !expiryDate.isEmpty()) {
            return expiryDate;
        }
        return "**/**";
    }
    
    /**
     * Verifica si la tarjeta está vencida
     */
    public boolean isExpired() {
        try {
            String month = expiryMonth;
            String year = expiryYear;
            
            if (month == null && expiryDate != null && expiryDate.contains("/")) {
                String[] parts = expiryDate.split("/");
                month = parts[0];
                year = parts[1];
            }
            
            if (month == null || year == null) return false;
            
            int expMonth = Integer.parseInt(month);
            int expYear = Integer.parseInt(year);
            
            java.util.Calendar now = java.util.Calendar.getInstance();
            int currentMonth = now.get(java.util.Calendar.MONTH) + 1;
            int currentYear = now.get(java.util.Calendar.YEAR);
            
            if (expYear < currentYear) return true;
            if (expYear == currentYear && expMonth < currentMonth) return true;
            
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Método estático para crear métodos de pago de ejemplo (hardcodeados)
    public static Cliente_PaymentMethod crearEjemploVisa() {
        return new Cliente_PaymentMethod(
            "pm_001",
            "4242424242424242",
            "Visa",
            "4242",
            "JUAN PEREZ",
            "12",
            "2027",
            "credit",
            true,
            true,
            "Mi Visa",
            Timestamp.now(),
            null
        );
    }

    public static Cliente_PaymentMethod crearEjemploMastercard() {
        return new Cliente_PaymentMethod(
            "pm_002",
            "5555555555554444",
            "Mastercard",
            "4444",
            "JUAN PEREZ",
            "08",
            "2026",
            "debit",
            false,
            true,
            "Tarjeta débito",
            Timestamp.now(),
            null
        );
    }

    /**
     * Convierte el objeto a Map para guardar en Firestore
     */
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("cardNumber", cardNumber);
        map.put("cardBrand", cardBrand);
        map.put("last4Digits", last4Digits);
        map.put("cardholderName", cardholderName);
        map.put("expiryMonth", expiryMonth);
        map.put("expiryYear", expiryYear);
        map.put("cardType", cardType);
        map.put("isDefault", isDefault);
        map.put("isSimulated", isSimulated);
        map.put("nickname", nickname);
        map.put("createdAt", createdAt);
        map.put("lastUsedAt", lastUsedAt);
        return map;
    }

    @Override
    public String toString() {
        return "Cliente_PaymentMethod{" +
                "cardBrand='" + cardBrand + '\'' +
                ", last4Digits='" + last4Digits + '\'' +
                ", expiryDate='" + getExpiryFormatted() + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
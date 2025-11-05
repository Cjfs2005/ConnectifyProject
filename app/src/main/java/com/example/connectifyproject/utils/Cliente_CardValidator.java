package com.example.connectifyproject.utils;

import java.util.Calendar;

/**
 * Validador de tarjetas de crédito/débito para Cliente
 * Incluye algoritmo de Luhn y validaciones de formato
 */
public class Cliente_CardValidator {
    
    // Tarjetas de prueba estándar (como Stripe)
    public static final String TEST_VISA = "4242424242424242";
    public static final String TEST_VISA_DEBIT = "4000056655665556";
    public static final String TEST_MASTERCARD = "5555555555554444";
    public static final String TEST_MASTERCARD_DEBIT = "5200828282828210";
    public static final String TEST_AMEX = "378282246310005";
    
    /**
     * Valida el número de tarjeta usando el algoritmo de Luhn
     * https://en.wikipedia.org/wiki/Luhn_algorithm
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }
        
        // Remover espacios y guiones
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        // Verificar que solo contenga dígitos
        if (!cardNumber.matches("\\d+")) {
            return false;
        }
        
        // Verificar longitud (13-19 dígitos)
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        // Algoritmo de Luhn
        return luhnCheck(cardNumber);
    }
    
    /**
     * Implementación del algoritmo de Luhn
     */
    private static boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        // Iterar desde el final hacia el inicio
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
    
    /**
     * Detecta la marca de la tarjeta según el número
     */
    public static String detectCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "Unknown";
        }
        
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        // Visa: comienza con 4
        if (cardNumber.startsWith("4")) {
            return "Visa";
        }
        
        // Mastercard: comienza con 51-55 o 2221-2720
        if (cardNumber.matches("^5[1-5].*") || 
            (cardNumber.length() >= 4 && 
             Integer.parseInt(cardNumber.substring(0, 4)) >= 2221 && 
             Integer.parseInt(cardNumber.substring(0, 4)) <= 2720)) {
            return "Mastercard";
        }
        
        // American Express: comienza con 34 o 37
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
            return "American Express";
        }
        
        // Discover: comienza con 6011, 622126-622925, 644-649, 65
        if (cardNumber.startsWith("6011") || 
            cardNumber.startsWith("65") ||
            cardNumber.matches("^64[4-9].*") ||
            (cardNumber.length() >= 6 && 
             Integer.parseInt(cardNumber.substring(0, 6)) >= 622126 && 
             Integer.parseInt(cardNumber.substring(0, 6)) <= 622925)) {
            return "Discover";
        }
        
        // Diners Club: comienza con 36 o 38 o 300-305
        if (cardNumber.startsWith("36") || cardNumber.startsWith("38") ||
            (cardNumber.length() >= 3 && 
             Integer.parseInt(cardNumber.substring(0, 3)) >= 300 && 
             Integer.parseInt(cardNumber.substring(0, 3)) <= 305)) {
            return "Diners Club";
        }
        
        // JCB: comienza con 3528-3589
        if (cardNumber.length() >= 4 && 
            Integer.parseInt(cardNumber.substring(0, 4)) >= 3528 && 
            Integer.parseInt(cardNumber.substring(0, 4)) <= 3589) {
            return "JCB";
        }
        
        return "Unknown";
    }
    
    /**
     * Obtiene los últimos 4 dígitos de la tarjeta
     */
    public static String getLast4Digits(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "";
        }
        
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        if (cardNumber.length() < 4) {
            return cardNumber;
        }
        
        return cardNumber.substring(cardNumber.length() - 4);
    }
    
    /**
     * Valida la fecha de vencimiento
     */
    public static boolean isValidExpiry(String month, String year) {
        try {
            int expiryMonth = Integer.parseInt(month);
            int expiryYear = Integer.parseInt(year);
            
            // Validar mes (01-12)
            if (expiryMonth < 1 || expiryMonth > 12) {
                return false;
            }
            
            // Obtener fecha actual
            Calendar now = Calendar.getInstance();
            int currentMonth = now.get(Calendar.MONTH) + 1; // 0-indexed
            int currentYear = now.get(Calendar.YEAR);
            
            // La tarjeta no puede estar vencida
            if (expiryYear < currentYear) {
                return false;
            }
            
            if (expiryYear == currentYear && expiryMonth < currentMonth) {
                return false;
            }
            
            return true;
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Valida el CVV según la marca de tarjeta
     */
    public static boolean isValidCVV(String cvv, String cardBrand) {
        if (cvv == null || cvv.isEmpty()) {
            return false;
        }
        
        // American Express usa 4 dígitos, otros usan 3
        if (cardBrand != null && cardBrand.equalsIgnoreCase("American Express")) {
            return cvv.matches("\\d{4}");
        }
        
        return cvv.matches("\\d{3}");
    }
    
    /**
     * Valida el nombre del titular
     */
    public static boolean isValidCardholderName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Mínimo 3 caracteres, solo letras y espacios
        String trimmed = name.trim();
        return trimmed.length() >= 3 && trimmed.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }
    
    /**
     * Formatea el número de tarjeta para mostrar
     * Ej: "4242424242424242" -> "4242 4242 4242 4242"
     */
    public static String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "";
        }
        
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        StringBuilder formatted = new StringBuilder();
        String brand = detectCardBrand(cardNumber);
        
        // American Express usa formato diferente: XXXX XXXXXX XXXXX
        if (brand.equals("American Express")) {
            for (int i = 0; i < cardNumber.length(); i++) {
                if (i == 4 || i == 10) {
                    formatted.append(" ");
                }
                formatted.append(cardNumber.charAt(i));
            }
        } else {
            // Otros: XXXX XXXX XXXX XXXX
            for (int i = 0; i < cardNumber.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ");
                }
                formatted.append(cardNumber.charAt(i));
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * Enmascara el número de tarjeta
     * Ej: "4242424242424242" -> "•••• •••• •••• 4242"
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "";
        }
        
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        if (cardNumber.length() < 4) {
            return cardNumber;
        }
        
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "•••• •••• •••• " + last4;
    }
    
    /**
     * Verifica si es una tarjeta de prueba válida
     */
    public static boolean isTestCard(String cardNumber) {
        if (cardNumber == null) return false;
        
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        return cardNumber.equals(TEST_VISA) ||
               cardNumber.equals(TEST_VISA_DEBIT) ||
               cardNumber.equals(TEST_MASTERCARD) ||
               cardNumber.equals(TEST_MASTERCARD_DEBIT) ||
               cardNumber.equals(TEST_AMEX);
    }
}

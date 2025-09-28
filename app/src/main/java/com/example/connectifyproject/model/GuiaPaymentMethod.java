package com.example.connectifyproject.model;

public class GuiaPaymentMethod {
    private String type; // "Tarjeta Crédito", "Tarjeta Débito", "Cuenta Bancaria", "Yape"
    private String number; // Últimos 4 dígitos, número de cuenta, o número de Yape
    private String holder; // Titular
    private String expiry; // Fecha de vencimiento (solo para tarjetas)
    private String bankCode; // Código bancario (solo para cuenta bancaria)

    public GuiaPaymentMethod(String type, String number, String holder, String expiry, String bankCode) {
        this.type = type;
        this.number = number;
        this.holder = holder;
        this.expiry = expiry;
        this.bankCode = bankCode;
    }

    public String getType() { return type; }
    public String getNumber() { return number; }
    public String getHolder() { return holder; }
    public String getExpiry() { return expiry; }
    public String getBankCode() { return bankCode; }

    @Override
    public String toString() {
        return type + " (" + number + ")";
    }
}
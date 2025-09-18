package com.example.connectifyproject.model;

import androidx.annotation.Nullable;

public class User {

    // Básicos
    private final String name;           // Nombre(s)
    @Nullable private final String lastName;       // Apellido(s)
    private final String dni;            // Número de documento
    private final String company;        // Empresa (si aplica)
    private final Role role;             // GUIDE / ADMIN / CLIENT

    // Extras de perfil
    @Nullable private final String docType;   // "DNI", "CE", "PASAPORTE"...
    @Nullable private final String birth;     // "MM/DD/YYYY" (o el formato que uses)
    @Nullable private final String email;
    @Nullable private final String phone;
    @Nullable private final String address;
    @Nullable private final String photoUri;  // content:// o file:// (opcional)

    // Constructor completo
    public User(String name,
                @Nullable String lastName,
                String dni,
                String company,
                Role role,
                @Nullable String docType,
                @Nullable String birth,
                @Nullable String email,
                @Nullable String phone,
                @Nullable String address,
                @Nullable String photoUri) {

        this.name = name;
        this.lastName = lastName;
        this.dni = dni;
        this.company = company;
        this.role = role;
        this.docType = docType;
        this.birth = birth;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.photoUri = photoUri;
    }

    // Constructor antiguo (compatibilidad): asume docType = "DNI" y sin extras
    public User(String name, String dni, String company, Role role) {
        this(name, null, dni, company, role, "DNI", null, null, null, null, null);
    }

    // --- Getters ---
    public String getName() { return name; }
    @Nullable public String getLastName() { return lastName; }
    public String getDni() { return dni; }
    public String getCompany() { return company; }
    public Role getRole() { return role; }

    @Nullable public String getDocType() { return docType; }
    @Nullable public String getBirth()   { return birth; }
    @Nullable public String getEmail()   { return email; }
    @Nullable public String getPhone()   { return phone; }
    @Nullable public String getAddress() { return address; }
    @Nullable public String getPhotoUri(){ return photoUri; }

    // Útil para el “circulito” inicial del avatar
    public String getInitial() {
        String base = (name != null && !name.trim().isEmpty()) ? name.trim() :
                (lastName != null ? lastName.trim() : "?");
        int cp = base.codePointAt(0);
        return new String(Character.toChars(Character.toUpperCase(cp)));
    }
}

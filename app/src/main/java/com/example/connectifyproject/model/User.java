package com.example.connectifyproject.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class User implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;

    // Básicos
    private final String name;                 // Nombre(s)
    @Nullable private final String lastName;   // Apellido(s)
    private final String dni;                  // Número de documento
    private final String company;              // Empresa (si aplica)
    private final Role role;                   // GUIDE / ADMIN / CLIENT

    // Extras de perfil
    @Nullable private final String docType;    // "DNI", "CE", "PASAPORTE"...
    @Nullable private final String birth;      // "MM/DD/YYYY" (o el formato que uses)
    @Nullable private final String email;
    @Nullable private final String phone;
    @Nullable private final String address;
    @Nullable private final String photoUri;   // content:// o file:// (opcional)
    
    // UID de Firebase (mutable)
    @Nullable private String uid;
    
    // Estado de habilitación (mutable)
    private boolean enabled;

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
        this.enabled = true; // Por defecto habilitado
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
    @Nullable public String getUid()     { return uid; }
    public boolean isEnabled()           { return enabled; }
    
    // Setter para uid (necesario para Firebase)
    public void setUid(@Nullable String uid) { this.uid = uid; }
    
    // Setter para enabled (necesario para Firebase)
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // Útil para el "circulito" inicial del avatar
    public String getInitial() {
        String base = (name != null && !name.trim().isEmpty()) ? name.trim() :
                (lastName != null ? lastName.trim() : "?");
        int cp = base.codePointAt(0);
        return new String(Character.toChars(Character.toUpperCase(cp)));
    }

    // =====================
    // Parcelable
    // =====================

    protected User(Parcel in) {
        this.name = in.readString();
        this.lastName = in.readString();
        this.dni = in.readString();
        this.company = in.readString();
        String roleName = in.readString();     // no-null en nuestro modelo
        this.role = Role.valueOf(roleName);
        this.docType = in.readString();
        this.birth = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
        this.address = in.readString();
        this.photoUri = in.readString();
        this.uid = in.readString();
        this.enabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.lastName);
        dest.writeString(this.dni);
        dest.writeString(this.company);
        dest.writeString(this.role.name());
        dest.writeString(this.docType);
        dest.writeString(this.birth);
        dest.writeString(this.email);
        dest.writeString(this.phone);
        dest.writeString(this.address);
        dest.writeString(this.photoUri);
        dest.writeString(this.uid);
        dest.writeByte((byte) (this.enabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}

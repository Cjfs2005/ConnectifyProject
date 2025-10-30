package com.example.connectifyproject.views.superadmin.notifications;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Modelo unificado para notificaciones.
 * - Modo Superadmin: usa (id, userFullName, createdAtMillis, read) y el mensaje es "X ha solicitado ser guía".
 * - Modo Cliente: usa (id, customTitle, customMessage, createdAtMillis, read, true) y muestra esos textos.
 */
public class NotificationItem implements Parcelable {

    private final String id;
    private final long createdAtMillis;
    private boolean read;

    // Superadmin (puede ser null en modo cliente)
    private final String userFullName;

    // Cliente (pueden ser null en modo superadmin)
    private final String customTitle;
    private final String customMessage;

    /** Constructor MODO SUPERADMIN */
    public NotificationItem(String id, String userFullName, long createdAtMillis, boolean read) {
        this.id = id;
        this.userFullName = userFullName;
        this.createdAtMillis = createdAtMillis;
        this.read = read;
        this.customTitle = null;
        this.customMessage = null;
    }

    /** Constructor MODO CLIENTE (custom). El último boolean solo sirve para diferenciar la firma. */
    public NotificationItem(String id, String customTitle, String customMessage,
                            long createdAtMillis, boolean read, boolean customMode) {
        this.id = id;
        this.customTitle = customTitle;
        this.customMessage = customMessage;
        this.createdAtMillis = createdAtMillis;
        this.read = read;
        this.userFullName = null;
    }

    protected NotificationItem(Parcel in) {
        id = in.readString();
        createdAtMillis = in.readLong();
        read = in.readByte() != 0;
        userFullName = in.readString();
        customTitle = in.readString();
        customMessage = in.readString();
    }

    public static final Creator<NotificationItem> CREATOR = new Creator<NotificationItem>() {
        @Override public NotificationItem createFromParcel(Parcel in) { return new NotificationItem(in); }
        @Override public NotificationItem[] newArray(int size) { return new NotificationItem[size]; }
    };

    public String getId() { return id; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    /** Título a mostrar en UI */
    public String getTitle() {
        if (customTitle != null) return customTitle;      // Cliente
        return "Solicitud de guía";                       // Superadmin
    }

    /** Mensaje a mostrar en UI */
    public String getMessage() {
        if (customMessage != null) return customMessage;  // Cliente
        return (userFullName == null ? "" : userFullName + " ") + "ha solicitado ser guía"; // Superadmin
    }

    /** Solo útil en superadmin; puede ser null en modo cliente */
    public String getUserFullName() { return userFullName; }

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(createdAtMillis);
        dest.writeByte((byte) (read ? 1 : 0));
        dest.writeString(userFullName);
        dest.writeString(customTitle);
        dest.writeString(customMessage);
    }
}

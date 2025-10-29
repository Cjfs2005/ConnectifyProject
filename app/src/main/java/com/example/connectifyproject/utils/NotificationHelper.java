package com.example.connectifyproject.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.connectifyproject.R;
import com.example.connectifyproject.cliente_tours;

/**
 * Helper compartido para gestión de notificaciones
 * Compatible con admin y cliente para evitar merge conflicts
 */
public class NotificationHelper {

    // Canales compartidos
    public static final String ADMIN_CHANNEL = "admin_notifications";
    public static final String RESERVATIONS_CHANNEL = "reservations";
    public static final String DOWNLOADS_CHANNEL = "downloads";

    /**
     * Crear todos los canales de notificación al iniciar la app
     * Método principal que se llama desde ConnectifyApp
     */
    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            
            // Canal para admin (para compatibilidad)
            NotificationChannel adminChannel = new NotificationChannel(
                    ADMIN_CHANNEL,
                    "Notificaciones Administrativas",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            adminChannel.setDescription("Notificaciones para administradores");
            notificationManager.createNotificationChannel(adminChannel);

            // Canal para reservas de cliente
            NotificationChannel reservationsChannel = new NotificationChannel(
                    RESERVATIONS_CHANNEL,
                    "Reservas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            reservationsChannel.setDescription("Confirmaciones y actualizaciones de reservas");
            notificationManager.createNotificationChannel(reservationsChannel);

            // Canal para descargas
            NotificationChannel downloadsChannel = new NotificationChannel(
                    DOWNLOADS_CHANNEL,
                    "Descargas",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            downloadsChannel.setDescription("Notificaciones de archivos descargados");
            notificationManager.createNotificationChannel(downloadsChannel);
        }
    }

    /**
     * Mostrar notificación de reserva confirmada
     */
    public static void showReservationConfirmedNotification(Context context, String tourName) {
        if (!hasNotificationPermission(context)) {
            return;
        }

        Intent intent = new Intent(context, cliente_tours.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, RESERVATIONS_CHANNEL)
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentTitle("¡Reserva confirmada!")
                .setContentText("Tu reserva para " + tourName + " ha sido confirmada")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1001, builder.build());
    }

    /**
     * Mostrar notificación de descarga completada
     */
    public static void showDownloadCompletedNotification(Context context, String fileName) {
        if (!hasNotificationPermission(context)) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DOWNLOADS_CHANNEL)
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentTitle("Descarga completada")
                .setContentText("Se ha descargado: " + fileName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1002, builder.build());
    }

    /**
     * Verificar si la app tiene permisos de notificación
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, 
                    android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // En versiones anteriores no se necesita permiso
    }

    /**
     * Verificar si las notificaciones están habilitadas
     */
    public static boolean areNotificationsEnabled(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        return notificationManager.areNotificationsEnabled();
    }
}
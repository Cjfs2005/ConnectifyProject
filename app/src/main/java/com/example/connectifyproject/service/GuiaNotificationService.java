package com.example.connectifyproject.service;

import android.Manifest;
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
import com.example.connectifyproject.guia_tours_ofertas;
import com.example.connectifyproject.guia_assigned_tours;

public class GuiaNotificationService {
    private Context context;
    
    // Canales de notificación
    private static final String CHANNEL_TOURS = "channel_tours";
    private static final String CHANNEL_REMINDERS = "channel_reminders";
    private static final String CHANNEL_CHECKIN = "channel_checkin";
    
    // IDs de notificación
    private static final int NOTIFICATION_NEW_TOUR = 1001;
    private static final int NOTIFICATION_TOUR_REMINDER = 1002;
    private static final int NOTIFICATION_LOCATION_REMINDER = 1003;
    private static final int NOTIFICATION_CHECKIN_REMINDER = 1004;
    private static final int NOTIFICATION_CHECKOUT_REMINDER = 1005;

    public GuiaNotificationService(Context context) {
        this.context = context;
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para ofertas de tours
            NotificationChannel tourChannel = new NotificationChannel(
                    CHANNEL_TOURS,
                    "Ofertas de Tours",
                    NotificationManager.IMPORTANCE_HIGH
            );
            tourChannel.setDescription("Notificaciones de nuevas ofertas de tours");
            tourChannel.enableVibration(true);

            // Canal para recordatorios
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Recordatorios de Tours",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            reminderChannel.setDescription("Recordatorios de tours próximos");

            // Canal para check-in/check-out
            NotificationChannel checkinChannel = new NotificationChannel(
                    CHANNEL_CHECKIN,
                    "Check-in y Check-out",
                    NotificationManager.IMPORTANCE_HIGH
            );
            checkinChannel.setDescription("Recordatorios para check-in y check-out");
            checkinChannel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(tourChannel);
            notificationManager.createNotificationChannel(reminderChannel);
            notificationManager.createNotificationChannel(checkinChannel);
        }
    }

    // 1. Nueva oferta de tour
    public void sendNewTourOfferNotification(String tourName, String empresa, double payment) {
        Intent intent = new Intent(context, guia_tours_ofertas.class);
        intent.putExtra("notification_type", "new_offer");
        intent.putExtra("tour_name", tourName);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TOURS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🎯 Nueva Oferta de Tour")
                .setContentText("Tour: " + tourName + " - S/ " + payment)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("¡Nueva oportunidad disponible!\n" +
                                "📍 Tour: " + tourName + "\n" +
                                "🏢 Empresa: " + empresa + "\n" +
                                "💰 Pago: S/ " + payment + "\n" +
                                "Toca para ver detalles y postular"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 1000});

        sendNotification(NOTIFICATION_NEW_TOUR, builder);
    }

    // 2. Recordatorio de tours próximos (1-3 días)
    public void sendTourReminderNotification(String tourName, String date, String time, int daysAhead) {
        Intent intent = new Intent(context, guia_assigned_tours.class);
        intent.putExtra("notification_type", "tour_reminder");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String reminderText;
        if (daysAhead == 0) {
            reminderText = "¡HOY tienes un tour!";
        } else if (daysAhead == 1) {
            reminderText = "¡MAÑANA tienes un tour!";
        } else {
            reminderText = "En " + daysAhead + " días tienes un tour";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(android.R.drawable.ic_menu_today)
                .setContentTitle("📅 " + reminderText)
                .setContentText(tourName + " - " + date + " a las " + time)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("📅 " + reminderText + "\n" +
                                "🎯 Tour: " + tourName + "\n" +
                                "📅 Fecha: " + date + "\n" +
                                "⏰ Hora: " + time + "\n" +
                                "¡Prepárate para brindar la mejor experiencia!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        sendNotification(NOTIFICATION_TOUR_REMINDER + daysAhead, builder);
    }

    // 3. Recordatorio de registrar ubicación durante tour activo
    public void sendLocationReminderNotification(String currentPoint) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("📍 Registrar Ubicación")
                .setContentText("Recuerda registrar tu posición en: " + currentPoint)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("📍 Tour en curso\n" +
                                "Es momento de registrar tu ubicación en:\n" +
                                "📌 " + currentPoint + "\n" +
                                "Esto ayuda a mantener informados a los clientes"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        sendNotification(NOTIFICATION_LOCATION_REMINDER, builder);
    }

    // 4. Recordatorio de Check-in
    public void sendCheckInReminderNotification(String tourName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CHECKIN)
                .setSmallIcon(android.R.drawable.ic_menu_save)
                .setContentTitle("✅ Recordatorio: Realizar Check-in")
                .setContentText("Es hora de escanear los códigos QR de los clientes")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("✅ Check-in necesario\n" +
                                "🎯 Tour: " + tourName + "\n" +
                                "📱 Escanea los códigos QR que te mostrarán los clientes\n" +
                                "¡Importante para confirmar su asistencia!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 600});

        sendNotification(NOTIFICATION_CHECKIN_REMINDER, builder);
    }

    // 5. Recordatorio de Check-out
    public void sendCheckOutReminderNotification(String tourName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CHECKIN)
                .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setContentTitle("🏁 Recordatorio: Realizar Check-out")
                .setContentText("Es hora de finalizar el tour con los clientes")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("🏁 Check-out necesario\n" +
                                "🎯 Tour: " + tourName + "\n" +
                                "📱 Escanea los códigos QR-Fin de los clientes\n" +
                                "¡Último paso para completar el tour!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 600});

        sendNotification(NOTIFICATION_CHECKOUT_REMINDER, builder);
    }

    private void sendNotification(int notificationId, NotificationCompat.Builder builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                    == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notificationId, builder.build());
            }
        } else {
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
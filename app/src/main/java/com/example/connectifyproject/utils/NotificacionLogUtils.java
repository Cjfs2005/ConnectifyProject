package com.example.connectifyproject.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class NotificacionLogUtils {
    public static void crearNotificacion(String titulo, String descripcion, String usuarioDestinoId) {
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("titulo", titulo);
        notificacion.put("descripcion", descripcion);
        notificacion.put("usuarioDestinoId", usuarioDestinoId);
        notificacion.put("timestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("notificaciones").add(notificacion);
    }

    public static void crearLog(String titulo, String descripcion) {
        Map<String, Object> log = new HashMap<>();
        log.put("titulo", titulo);
        log.put("descripcion", descripcion);
        log.put("timestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("logs").add(log);
    }
}

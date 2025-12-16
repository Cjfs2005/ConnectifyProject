package com.example.connectifyproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.adapters.GuiaNotificationAdapter;
import com.example.connectifyproject.databinding.GuiaNotificacionesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class guia_notificaciones extends AppCompatActivity {

    private static final String TAG = "GuiaNotificaciones";

    private GuiaNotificacionesBinding binding;
    private GuiaNotificationAdapter adapter;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaNotificacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupToolbar();
        setupRecyclerView();
        loadNotificationsFromFirebase();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuiaNotificationAdapter(new ArrayList<>());
        binding.recyclerViewNotifications.setAdapter(adapter);
    }

    private void loadNotificationsFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Usuario no autenticado");
            showEmptyState("Debes iniciar sesión para ver tus notificaciones");
            return;
        }

        String userId = currentUser.getUid();
        showLoading(true);

        db.collection("notificaciones")
            .whereEqualTo("usuarioDestinoId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                showLoading(false);
                List<GuiaNotificationAdapter.NotificationData> notifications = new ArrayList<>();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        String titulo = document.getString("titulo");
                        String descripcion = document.getString("descripcion");
                        Long timestamp = document.getLong("timestamp");
                        
                        // Convertir timestamp a hora y fecha
                        String time = "";
                        String date = "";
                        long ts = timestamp != null ? timestamp : 0L;
                        if (timestamp != null) {
                            Date dateObj = new Date(timestamp);
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                            time = timeFormat.format(dateObj);
                            date = dateFormat.format(dateObj);
                        }
                        
                        GuiaNotificationAdapter.NotificationData notification = 
                            new GuiaNotificationAdapter.NotificationData(
                                titulo != null ? titulo : "Sin título",
                                descripcion != null ? descripcion : "",
                                time,
                                date,
                                ts
                            );
                        
                        notifications.add(notification);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parseando notificación: " + document.getId(), e);
                    }
                }

                // Ordenar por timestamp descendente (más recientes primero)
                notifications.sort((n1, n2) -> Long.compare(n2.timestamp, n1.timestamp));

                if (notifications.isEmpty()) {
                    showEmptyState("No tienes notificaciones");
                } else {
                    hideEmptyState();
                    adapter.updateList(notifications);
                }

                Log.d(TAG, "Notificaciones cargadas: " + notifications.size());
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error cargando notificaciones", e);
                showEmptyState("Error al cargar notificaciones");
            });
    }

    private void showLoading(boolean show) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        binding.recyclerViewNotifications.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(String message) {
        if (binding.tvEmpty != null) {
            binding.tvEmpty.setText(message);
            binding.tvEmpty.setVisibility(View.VISIBLE);
        }
        binding.recyclerViewNotifications.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        if (binding.tvEmpty != null) {
            binding.tvEmpty.setVisibility(View.GONE);
        }
        binding.recyclerViewNotifications.setVisibility(View.VISIBLE);
    }
}
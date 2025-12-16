package com.example.connectifyproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_NotificationAdapter;
import com.example.connectifyproject.models.Cliente_Notification;
import com.google.android.material.appbar.MaterialToolbar;
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

public class cliente_notificaciones extends AppCompatActivity {

    private static final String TAG = "ClienteNotificaciones";
    
    private RecyclerView recyclerView;
    private Cliente_NotificationAdapter adapter;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_notificaciones);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadNotificationsFromFirebase();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView_notifications);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Cliente_NotificationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
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
                List<Cliente_Notification> notifications = new ArrayList<>();
                List<Long> timestamps = new ArrayList<>();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        String id = document.getId();
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

                        Boolean isRead = document.getBoolean("leido");
                        
                        Cliente_Notification notification = new Cliente_Notification(
                            id,
                            titulo != null ? titulo : "Sin título",
                            descripcion != null ? descripcion : "",
                            time,
                            date,
                            isRead != null && isRead
                        );
                        
                        notifications.add(notification);
                        timestamps.add(ts);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parseando notificación: " + document.getId(), e);
                    }
                }

                // Ordenar por timestamp descendente (más recientes primero)
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < notifications.size(); i++) indices.add(i);
                indices.sort((i1, i2) -> Long.compare(timestamps.get(i2), timestamps.get(i1)));
                
                List<Cliente_Notification> sorted = new ArrayList<>();
                for (int idx : indices) sorted.add(notifications.get(idx));
                notifications = sorted;

                if (notifications.isEmpty()) {
                    showEmptyState("No tienes notificaciones");
                } else {
                    hideEmptyState();
                    adapter = new Cliente_NotificationAdapter(notifications);
                    recyclerView.setAdapter(adapter);
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
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(String message) {
        if (tvEmpty != null) {
            tvEmpty.setText(message);
            tvEmpty.setVisibility(View.VISIBLE);
        }
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }
        recyclerView.setVisibility(View.VISIBLE);
    }
}
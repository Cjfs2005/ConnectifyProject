package com.example.connectifyproject;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;

public class cliente_editar_perfil extends AppCompatActivity {

    private TextView profileInitials;
    private TextInputEditText editTextName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_editar_perfil);

        // Setup views
        initViews();
        setupToolbar();
        setupInitials();
    }

    private void initViews() {
        profileInitials = findViewById(R.id.profileInitials);
        editTextName = findViewById(R.id.editTextName);
    }

    private void setupToolbar() {
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Editar Perfil");
        }

        // Handle back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupInitials() {
        // Configurar el nombre por defecto y las iniciales
        String defaultName = "Jorge Romero Paredes";
        editTextName.setText(defaultName);
        
        String initials = getInitials(defaultName);
        profileInitials.setText(initials);
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "??";
        }
        
        String[] nameParts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        
        // Tomar máximo 2 iniciales
        int count = Math.min(nameParts.length, 2);
        for (int i = 0; i < count; i++) {
            if (nameParts[i].length() > 0) {
                initials.append(nameParts[i].charAt(0));
            }
        }
        
        return initials.toString().toUpperCase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
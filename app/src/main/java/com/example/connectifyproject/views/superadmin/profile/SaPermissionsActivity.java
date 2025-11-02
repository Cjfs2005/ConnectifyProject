package com.example.connectifyproject.views.superadmin.profile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.R;
import com.google.android.material.appbar.MaterialToolbar;

public class SaPermissionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sa_permissions);

        // Configurar toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}

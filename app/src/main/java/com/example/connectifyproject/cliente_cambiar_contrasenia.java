package com.example.connectifyproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

public class cliente_cambiar_contrasenia extends AppCompatActivity {

    private TextInputEditText etNuevaContrasenia, etRepetirContrasenia;
    private ImageView ivCheckLetra, ivCheckMayuscula, ivCheckNumero, ivCheckLongitud;
    private MaterialButton btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_cambiar_contrasenia);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Cambiar contraseña");
        }

        // Initialize views
        etNuevaContrasenia = findViewById(R.id.et_nueva_contrasenia);
        etRepetirContrasenia = findViewById(R.id.et_repetir_contrasenia);
        ivCheckLetra = findViewById(R.id.iv_check_letra);
        ivCheckMayuscula = findViewById(R.id.iv_check_mayuscula);
        ivCheckNumero = findViewById(R.id.iv_check_numero);
        ivCheckLongitud = findViewById(R.id.iv_check_longitud);
        btnGuardar = findViewById(R.id.btn_guardar);

        // Initialize validation icons as unchecked
        resetValidationIcons();
        
        // Setup back pressed callback
        setupBackPressedCallback();

        // Setup text watchers for password validation
        etNuevaContrasenia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Handle save button (no functionality yet)
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement save functionality
            }
        });
    }

    private void validatePassword(String password) {
        // Check if contains at least one letter
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        updateValidationIcon(ivCheckLetra, hasLetter);

        // Check if contains at least one uppercase letter
        boolean hasUppercase = password.matches(".*[A-Z].*");
        updateValidationIcon(ivCheckMayuscula, hasUppercase);

        // Check if contains at least one number
        boolean hasNumber = password.matches(".*\\d.*");
        updateValidationIcon(ivCheckNumero, hasNumber);

        // Check if has at least 8 characters
        boolean hasMinLength = password.length() >= 8;
        updateValidationIcon(ivCheckLongitud, hasMinLength);
    }

    private void updateValidationIcon(ImageView imageView, boolean isValid) {
        if (isValid) {
            imageView.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_light));
        } else {
            imageView.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
    }

    private void resetValidationIcons() {
        updateValidationIcon(ivCheckLetra, false);
        updateValidationIcon(ivCheckMayuscula, false);
        updateValidationIcon(ivCheckNumero, false);
        updateValidationIcon(ivCheckLongitud, false);
    }

    private void setupBackPressedCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
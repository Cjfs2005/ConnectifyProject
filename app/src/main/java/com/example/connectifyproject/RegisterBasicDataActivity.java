package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class RegisterBasicDataActivity extends AppCompatActivity {

    private String userType;
    private TextInputEditText etNombre, etApellido, etCorreo, etTelefono;
    private TextInputLayout tilNombre, tilApellido, tilCorreo, tilTelefono, tilCodigoPais;
    private AutoCompleteTextView actvCodigoPais;
    private MaterialButton btnSiguiente;
    private View tvError, ivBack;

    // Códigos de país
    private final Map<String, String> paisCodigos = new HashMap<String, String>() {{
        put("🇵🇪 Perú (+51)", "+51");
        put("🇲🇽 México (+52)", "+52");
        put("🇨🇴 Colombia (+57)", "+57");
        put("🇦🇷 Argentina (+54)", "+54");
        put("🇨🇱 Chile (+56)", "+56");
        put("🇺🇸 Estados Unidos (+1)", "+1");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_basic_data);

        userType = getIntent().getStringExtra("user_type");
        if (userType == null) userType = "CLIENTE";

        initViews();
        setupCountrySelector();
        setupClickListeners();
        setupValidations();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        
        tilNombre = findViewById(R.id.tilNombre);
        tilApellido = findViewById(R.id.tilApellido);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilTelefono = findViewById(R.id.tilTelefono);
        tilCodigoPais = findViewById(R.id.tilCodigoPais);
        
        actvCodigoPais = findViewById(R.id.actvCodigoPais);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        tvError = findViewById(R.id.tvError);
        ivBack = findViewById(R.id.ivBack);
    }

    private void setupCountrySelector() {
        String[] paises = paisCodigos.keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, paises);
        actvCodigoPais.setAdapter(adapter);
        actvCodigoPais.setText("🇵🇪 Perú (+51)", false); // Por defecto Perú
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSiguiente.setOnClickListener(v -> {
            if (validateFields()) {
                navigateToNextStep();
            }
        });
    }

    private void setupValidations() {
        // Aquí se pueden agregar validaciones en tiempo real si se desea
    }

    private boolean validateFields() {
        boolean isValid = true;
        
        // Limpiar errores previos
        tilNombre.setError(null);
        tilApellido.setError(null);
        tilCorreo.setError(null);
        tilTelefono.setError(null);
        tvError.setVisibility(View.GONE);

        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("El nombre es requerido");
            isValid = false;
        }

        if (TextUtils.isEmpty(apellido)) {
            tilApellido.setError("El apellido es requerido");
            isValid = false;
        }

        if (TextUtils.isEmpty(correo)) {
            tilCorreo.setError("El correo es requerido");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Ingresa un correo válido");
            isValid = false;
        }

        if (TextUtils.isEmpty(telefono)) {
            tilTelefono.setError("El teléfono es requerido");
            isValid = false;
        } else if (telefono.length() < 8) {
            tilTelefono.setError("El teléfono debe tener al menos 8 dígitos");
            isValid = false;
        }

        return isValid;
    }

    private void navigateToNextStep() {
        Intent intent = new Intent(this, RegisterDocumentDataActivity.class);
        intent.putExtra("user_type", userType);
        intent.putExtra("nombre", etNombre.getText().toString().trim());
        intent.putExtra("apellido", etApellido.getText().toString().trim());
        intent.putExtra("correo", etCorreo.getText().toString().trim());
        intent.putExtra("telefono", etTelefono.getText().toString().trim());
        intent.putExtra("codigo_pais", paisCodigos.get(actvCodigoPais.getText().toString()));
        startActivity(intent);
    }
}
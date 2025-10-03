package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class RegisterPasswordActivity extends AppCompatActivity {

    private String userType, nombre, apellido, correo, telefono, codigoPais;
    private String tipoDocumento, numeroDocumento, fechaNacimiento, genero;
    
    private TextInputEditText etPassword, etConfirmPassword;
    private TextInputLayout tilPassword, tilConfirmPassword, tilIdiomas, tilExperiencia;
    private AutoCompleteTextView actvIdiomas, actvExperiencia;
    private LinearLayout llGuideSpecific;
    private MaterialButton btnSiguiente;
    private View tvError, ivBack;
    
    // Validación visual
    private ImageView ivMinCharacters, ivUppercase, ivNumber;
    private boolean hasMinCharacters = false;
    private boolean hasUppercase = false;
    private boolean hasNumber = false;

    // Opciones para guías
    private final String[] idiomas = {
            "Español",
            "Inglés",
            "Francés",
            "Portugués",
            "Italiano",
            "Alemán",
            "Japonés",
            "Chino Mandarín",
            "Español + Inglés",
            "Español + Inglés + Francés"
    };

    private final String[] experiencia = {
            "Menos de 1 año",
            "1 - 2 años",
            "3 - 5 años",
            "6 - 10 años",
            "Más de 10 años"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_password);

        getDataFromIntent();
        initViews();
        setupGuideSpecificFields();
        setupPasswordValidation();
        setupClickListeners();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        userType = intent.getStringExtra("user_type");
        nombre = intent.getStringExtra("nombre");
        apellido = intent.getStringExtra("apellido");
        correo = intent.getStringExtra("correo");
        telefono = intent.getStringExtra("telefono");
        codigoPais = intent.getStringExtra("codigo_pais");
        tipoDocumento = intent.getStringExtra("tipo_documento");
        numeroDocumento = intent.getStringExtra("numero_documento");
        fechaNacimiento = intent.getStringExtra("fecha_nacimiento");
        genero = intent.getStringExtra("genero");
    }

    private void initViews() {
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilIdiomas = findViewById(R.id.tilIdiomas);
        tilExperiencia = findViewById(R.id.tilExperiencia);
        
        actvIdiomas = findViewById(R.id.actvIdiomas);
        actvExperiencia = findViewById(R.id.actvExperiencia);
        
        llGuideSpecific = findViewById(R.id.llGuideSpecific);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        tvError = findViewById(R.id.tvError);
        ivBack = findViewById(R.id.ivBack);
        
        // Iconos de validación
        ivMinCharacters = findViewById(R.id.ivMinCharacters);
        ivUppercase = findViewById(R.id.ivUppercase);
        ivNumber = findViewById(R.id.ivNumber);
    }

    private void setupGuideSpecificFields() {
        if ("GUIA".equals(userType)) {
            llGuideSpecific.setVisibility(View.VISIBLE);
            
            // Configurar dropdown de idiomas
            ArrayAdapter<String> idiomasAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_dropdown_item_1line, idiomas);
            actvIdiomas.setAdapter(idiomasAdapter);
            
            // Configurar dropdown de experiencia
            ArrayAdapter<String> experienciaAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_dropdown_item_1line, experiencia);
            actvExperiencia.setAdapter(experienciaAdapter);
        } else {
            llGuideSpecific.setVisibility(View.GONE);
        }
    }

    private void setupPasswordValidation() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordRequirements(s.toString());
            }
        });
    }

    private void validatePasswordRequirements(String password) {
        // Validar longitud mínima
        hasMinCharacters = password.length() >= 8;
        updateValidationIcon(ivMinCharacters, hasMinCharacters);
        
        // Validar mayúscula
        hasUppercase = Pattern.compile("[A-Z]").matcher(password).find();
        updateValidationIcon(ivUppercase, hasUppercase);
        
        // Validar número
        hasNumber = Pattern.compile("[0-9]").matcher(password).find();
        updateValidationIcon(ivNumber, hasNumber);
    }

    private void updateValidationIcon(ImageView icon, boolean isValid) {
        if (isValid) {
            icon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            icon.setColorFilter(ContextCompat.getColor(this, R.color.gray_400));
        }
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSiguiente.setOnClickListener(v -> {
            if (validateFields()) {
                navigateToNextStep();
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;
        
        // Limpiar errores previos
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        if (tilIdiomas != null) tilIdiomas.setError(null);
        if (tilExperiencia != null) tilExperiencia.setError(null);
        tvError.setVisibility(View.GONE);

        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("La contraseña es requerida");
            isValid = false;
        } else if (!hasMinCharacters || !hasUppercase || !hasNumber) {
            tilPassword.setError("La contraseña no cumple los requisitos");
            isValid = false;
        }

        // Validar confirmación de contraseña
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Confirma tu contraseña");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Las contraseñas no coinciden");
            isValid = false;
        }

        // Validar campos específicos de guía
        if ("GUIA".equals(userType)) {
            String idiomas = actvIdiomas.getText().toString().trim();
            String experiencia = actvExperiencia.getText().toString().trim();
            
            if (TextUtils.isEmpty(idiomas)) {
                tilIdiomas.setError("Selecciona los idiomas que hablas");
                isValid = false;
            }
            
            if (TextUtils.isEmpty(experiencia)) {
                tilExperiencia.setError("Selecciona tu nivel de experiencia");
                isValid = false;
            }
        }

        return isValid;
    }

    private void navigateToNextStep() {
        Intent intent = new Intent(this, RegisterPhotoActivity.class);
        intent.putExtra("user_type", userType);
        intent.putExtra("nombre", nombre);
        intent.putExtra("apellido", apellido);
        intent.putExtra("correo", correo);
        intent.putExtra("telefono", telefono);
        intent.putExtra("codigo_pais", codigoPais);
        intent.putExtra("tipo_documento", tipoDocumento);
        intent.putExtra("numero_documento", numeroDocumento);
        intent.putExtra("fecha_nacimiento", fechaNacimiento);
        intent.putExtra("genero", genero);
        intent.putExtra("password", etPassword.getText().toString().trim());
        
        // Datos específicos de guía
        if ("GUIA".equals(userType)) {
            intent.putExtra("idiomas", actvIdiomas.getText().toString());
            intent.putExtra("experiencia", actvExperiencia.getText().toString());
        }
        
        startActivity(intent);
    }
}
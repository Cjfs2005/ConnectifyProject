package com.example.connectifyproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterDocumentDataActivity extends AppCompatActivity {

    private String userType, nombre, apellido, correo, telefono, codigoPais;
    private TextInputEditText etNumeroDocumento, etFechaNacimiento;
    private TextInputLayout tilTipoDocumento, tilNumeroDocumento, tilFechaNacimiento, tilGenero;
    private AutoCompleteTextView actvTipoDocumento, actvGenero;
    private MaterialButton btnSiguiente;
    private View tvError, ivBack;
    private Calendar selectedDate;

    // Opciones de tipo de documento
    private final String[] tiposDocumento = {
            "DNI - Documento Nacional de Identidad",
            "Pasaporte",
            "Carnet de Extranjería"
    };

    // Opciones de género
    private final String[] generos = {
            "Masculino",
            "Femenino",
            "Prefiero no decirlo"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_document_data);

        getDataFromIntent();
        initViews();
        setupDropdowns();
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
    }

    private void initViews() {
        etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        
        tilTipoDocumento = findViewById(R.id.tilTipoDocumento);
        tilNumeroDocumento = findViewById(R.id.tilNumeroDocumento);
        tilFechaNacimiento = findViewById(R.id.tilFechaNacimiento);
        tilGenero = findViewById(R.id.tilGenero);
        
        actvTipoDocumento = findViewById(R.id.actvTipoDocumento);
        actvGenero = findViewById(R.id.actvGenero);
        
        btnSiguiente = findViewById(R.id.btnSiguiente);
        tvError = findViewById(R.id.tvError);
        ivBack = findViewById(R.id.ivBack);

        selectedDate = Calendar.getInstance();
    }

    private void setupDropdowns() {
        // Configurar dropdown de tipo de documento
        ArrayAdapter<String> tipoDocAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, tiposDocumento);
        actvTipoDocumento.setAdapter(tipoDocAdapter);

        // Configurar dropdown de género
        ArrayAdapter<String> generoAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, generos);
        actvGenero.setAdapter(generoAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSiguiente.setOnClickListener(v -> {
            if (validateFields()) {
                navigateToNextStep();
            }
        });

        // Click listener para el selector de fecha
        etFechaNacimiento.setOnClickListener(v -> showDatePicker());
        tilFechaNacimiento.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 18; // Por defecto hace 18 años
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etFechaNacimiento.setText(dateFormat.format(selectedDate.getTime()));
                }, year, month, day);

        // Establecer fecha máxima (18 años atrás)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        // Establecer fecha mínima (100 años atrás)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private boolean validateFields() {
        boolean isValid = true;
        
        // Limpiar errores previos
        tilTipoDocumento.setError(null);
        tilNumeroDocumento.setError(null);
        tilFechaNacimiento.setError(null);
        tilGenero.setError(null);
        tvError.setVisibility(View.GONE);

        String tipoDocumento = actvTipoDocumento.getText().toString().trim();
        String numeroDocumento = etNumeroDocumento.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        String genero = actvGenero.getText().toString().trim();

        if (TextUtils.isEmpty(tipoDocumento)) {
            tilTipoDocumento.setError("Selecciona el tipo de documento");
            isValid = false;
        }

        if (TextUtils.isEmpty(numeroDocumento)) {
            tilNumeroDocumento.setError("El número de documento es requerido");
            isValid = false;
        } else {
            // Validar según tipo de documento
            if (tipoDocumento.contains("DNI") && numeroDocumento.length() != 8) {
                tilNumeroDocumento.setError("El DNI debe tener 8 dígitos");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(fechaNacimiento)) {
            tilFechaNacimiento.setError("La fecha de nacimiento es requerida");
            isValid = false;
        }

        if (TextUtils.isEmpty(genero)) {
            tilGenero.setError("Selecciona tu género");
            isValid = false;
        }

        return isValid;
    }

    private void navigateToNextStep() {
        Intent intent = new Intent(this, RegisterPasswordActivity.class);
        intent.putExtra("user_type", userType);
        intent.putExtra("nombre", nombre);
        intent.putExtra("apellido", apellido);
        intent.putExtra("correo", correo);
        intent.putExtra("telefono", telefono);
        intent.putExtra("codigo_pais", codigoPais);
        intent.putExtra("tipo_documento", actvTipoDocumento.getText().toString());
        intent.putExtra("numero_documento", etNumeroDocumento.getText().toString().trim());
        intent.putExtra("fecha_nacimiento", etFechaNacimiento.getText().toString().trim());
        intent.putExtra("genero", actvGenero.getText().toString());
        startActivity(intent);
    }
}
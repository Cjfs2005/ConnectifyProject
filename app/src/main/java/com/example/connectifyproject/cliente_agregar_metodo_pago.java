package com.example.connectifyproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.example.connectifyproject.models.Cliente_PaymentMethod;
import com.example.connectifyproject.utils.Cliente_CardValidator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class cliente_agregar_metodo_pago extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Preview views
    private TextView tvCardBrand;
    private TextView tvCardNumberPreview;
    private TextView tvCardHolderPreview;
    private TextView tvExpiryPreview;

    // Input views
    private TextInputLayout tilCardNumber;
    private TextInputLayout tilCardholderName;
    private TextInputLayout tilExpiryMonth;
    private TextInputLayout tilExpiryYear;
    private TextInputLayout tilCvv;
    private TextInputLayout tilNickname;

    private TextInputEditText etCardNumber;
    private TextInputEditText etCardholderName;
    private AutoCompleteTextView spinnerExpiryMonth;
    private AutoCompleteTextView spinnerExpiryYear;
    private TextInputEditText etCvv;
    private TextInputEditText etNickname;

    private MaterialCheckBox cbSetDefault;
    private MaterialButton btnSave;

    // Data
    private String detectedBrand = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_agregar_metodo_pago);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupDropdowns();
        setupTextWatchers();
        setupSaveButton();
    }

    private void initViews() {
        // Preview
        tvCardBrand = findViewById(R.id.tv_card_brand);
        tvCardNumberPreview = findViewById(R.id.tv_card_number_preview);
        tvCardHolderPreview = findViewById(R.id.tv_card_holder_preview);
        tvExpiryPreview = findViewById(R.id.tv_expiry_preview);

        // Inputs
        tilCardNumber = findViewById(R.id.til_card_number);
        tilCardholderName = findViewById(R.id.til_cardholder_name);
        tilExpiryMonth = findViewById(R.id.til_expiry_month);
        tilExpiryYear = findViewById(R.id.til_expiry_year);
        tilCvv = findViewById(R.id.til_cvv);
        tilNickname = findViewById(R.id.til_nickname);

        etCardNumber = findViewById(R.id.et_card_number);
        etCardholderName = findViewById(R.id.et_cardholder_name);
        spinnerExpiryMonth = findViewById(R.id.spinner_expiry_month);
        spinnerExpiryYear = findViewById(R.id.spinner_expiry_year);
        etCvv = findViewById(R.id.et_cvv);
        etNickname = findViewById(R.id.et_nickname);

        cbSetDefault = findViewById(R.id.cb_set_default);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDropdowns() {
        // Months
        String[] months = new String[12];
        for (int i = 1; i <= 12; i++) {
            months[i - 1] = String.format("%02d", i);
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                months
        );
        spinnerExpiryMonth.setAdapter(monthAdapter);

        // Years (next 15 years)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            years.add(String.valueOf(currentYear + i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                years
        );
        spinnerExpiryYear.setAdapter(yearAdapter);
    }

    private void setupTextWatchers() {
        // Card Number - Auto format and detect brand
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;
                String input = s.toString().replaceAll("\\s", "");

                // Detect brand
                detectedBrand = Cliente_CardValidator.detectCardBrand(input);
                tvCardBrand.setText(detectedBrand.isEmpty() ? "TARJETA" : detectedBrand.toUpperCase());

                // Format for preview
                String formatted = Cliente_CardValidator.formatCardNumber(input);
                tvCardNumberPreview.setText(formatted.isEmpty() ? "•••• •••• •••• ••••" : formatted);

                // Clear error
                tilCardNumber.setError(null);

                isFormatting = false;
            }
        });

        // Cardholder Name
        etCardholderName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString().trim();
                tvCardHolderPreview.setText(name.isEmpty() ? "NOMBRE APELLIDO" : name.toUpperCase());
                tilCardholderName.setError(null);
            }
        });

        // Expiry Month
        spinnerExpiryMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateExpiryPreview();
            }
        });

        // Expiry Year
        spinnerExpiryYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateExpiryPreview();
            }
        });

        // CVV
        etCvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                tilCvv.setError(null);
            }
        });
    }

    private void updateExpiryPreview() {
        String month = spinnerExpiryMonth.getText().toString();
        String year = spinnerExpiryYear.getText().toString();

        if (!month.isEmpty() && !year.isEmpty()) {
            String shortYear = year.substring(2);
            tvExpiryPreview.setText(month + "/" + shortYear);
        } else {
            tvExpiryPreview.setText("MM/AA");
        }
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> saveCard());
    }

    private void saveCard() {
        // Get values
        String cardNumber = etCardNumber.getText().toString().replaceAll("\\s", "");
        String cardholderName = etCardholderName.getText().toString().trim().toUpperCase();
        String expiryMonth = spinnerExpiryMonth.getText().toString();
        String expiryYear = spinnerExpiryYear.getText().toString();
        String cvv = etCvv.getText().toString();
        String nickname = etNickname.getText().toString().trim();
        // Eliminamos la lógica de predeterminada

        // Validate
        boolean isValid = true;

        // Validate card number
        if (cardNumber.isEmpty()) {
            tilCardNumber.setError("Ingrese el número de tarjeta");
            isValid = false;
        } else if (!Cliente_CardValidator.isValidCardNumber(cardNumber)) {
            tilCardNumber.setError("Número de tarjeta inválido");
            isValid = false;
        }

        // Validate cardholder name
        if (cardholderName.isEmpty()) {
            tilCardholderName.setError("Ingrese el nombre del titular");
            isValid = false;
        } else if (!Cliente_CardValidator.isValidCardholderName(cardholderName)) {
            tilCardholderName.setError("Nombre inválido (solo letras y espacios)");
            isValid = false;
        }

        // Validate expiry
        if (expiryMonth.isEmpty() || expiryYear.isEmpty()) {
            Toast.makeText(this, "Seleccione la fecha de vencimiento", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            if (!Cliente_CardValidator.isValidExpiry(String.valueOf(month), String.valueOf(year))) {
                Toast.makeText(this, "Fecha de vencimiento inválida o vencida", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
        }

        // Validate CVV
        if (cvv.isEmpty()) {
            tilCvv.setError("Ingrese el CVV");
            isValid = false;
        } else if (!Cliente_CardValidator.isValidCVV(cvv, detectedBrand)) {
            tilCvv.setError("CVV inválido");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Disable button
        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");

        // Create payment method object
        Cliente_PaymentMethod paymentMethod = new Cliente_PaymentMethod();
        paymentMethod.setCardNumber(cardNumber); // Solo para simulación
        paymentMethod.setCardBrand(detectedBrand);
        paymentMethod.setLast4Digits(Cliente_CardValidator.getLast4Digits(cardNumber));
        paymentMethod.setCardholderName(cardholderName);
        paymentMethod.setExpiryMonth(expiryMonth);
        paymentMethod.setExpiryYear(expiryYear);
        paymentMethod.setCardType("credit"); // Default
        paymentMethod.setSimulated(true);
        paymentMethod.setNickname(nickname.isEmpty() ? null : nickname);
        paymentMethod.setCreatedAt(Timestamp.now());
        // NO guardamos el CVV

        // Save to Firestore
        String userId = mAuth.getCurrentUser().getUid();
        savePaymentMethod(userId, paymentMethod);
    }

    private void savePaymentMethod(String userId, Cliente_PaymentMethod paymentMethod) {
        // Sin lógica de predeterminada, simplemente guardar
        db.collection("usuarios")
                .document(userId)
                .collection("payment_methods")
                .add(paymentMethod)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tarjeta guardada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Guardar tarjeta");
                });
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

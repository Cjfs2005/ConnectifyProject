package com.example.connectifyproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class cliente_tour_filtros extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilStartDate, tilEndDate, tilLanguage, tilCity;
    private TextInputEditText etStartDate, etEndDate, etMinPrice, etMaxPrice;
    private AutoCompleteTextView actLanguage, actCity;
    private MaterialButton btnClearDates, btnClearPrice, btnClearLanguage, btnClearCity;
    private MaterialButton btnClearFilters, btnApplyFilters;

    private Calendar startDateCalendar, endDateCalendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_tour_filtros);

        initViews();
        setupToolbar();
        setupLanguageDropdown();
        setupCityDropdown();
        setupClickListeners();
        initDateFormatters();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilStartDate = findViewById(R.id.til_start_date);
        tilEndDate = findViewById(R.id.til_end_date);
        tilLanguage = findViewById(R.id.til_language);
        tilCity = findViewById(R.id.til_city);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        etMinPrice = findViewById(R.id.et_min_price);
        etMaxPrice = findViewById(R.id.et_max_price);
        actLanguage = findViewById(R.id.act_language);
        actCity = findViewById(R.id.act_city);
        btnClearDates = findViewById(R.id.btn_clear_dates);
        btnClearPrice = findViewById(R.id.btn_clear_price);
        btnClearLanguage = findViewById(R.id.btn_clear_language);
        btnClearCity = findViewById(R.id.btn_clear_city);
        btnClearFilters = findViewById(R.id.btn_clear_filters);
        btnApplyFilters = findViewById(R.id.btn_apply_filters);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Filtros de tours");
        }
        
        // Configurar el color blanco para el botón de retroceder
        toolbar.setNavigationIconTint(getResources().getColor(android.R.color.white));
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupLanguageDropdown() {
        String[] languages = {"Español", "Inglés", "Francés", "Alemán", "Italiano", "Chino", "Japonés"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, languages);
        actLanguage.setAdapter(adapter);
    }

    private void setupCityDropdown() {
        String[] cities = {"Lima", "Cusco", "Arequipa", "Trujillo", "Chiclayo", 
                          "Piura", "Iquitos", "Huancayo", "Tacna", "Puno"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, cities);
        actCity.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Date pickers
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        tilStartDate.setEndIconOnClickListener(v -> showDatePicker(true));
        
        etEndDate.setOnClickListener(v -> showDatePicker(false));
        tilEndDate.setEndIconOnClickListener(v -> showDatePicker(false));

        // Clear buttons
        btnClearDates.setOnClickListener(v -> clearDates());
        btnClearPrice.setOnClickListener(v -> clearPrices());
        btnClearLanguage.setOnClickListener(v -> clearLanguage());
        btnClearCity.setOnClickListener(v -> clearCity());
        btnClearFilters.setOnClickListener(v -> clearAllFilters());

        // Apply filters
        btnApplyFilters.setOnClickListener(v -> applyFilters());
    }

    private void initDateFormatters() {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCalendar : endDateCalendar;
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    String selectedDate = dateFormat.format(calendar.getTime());
                    if (isStartDate) {
                        etStartDate.setText(selectedDate);
                    } else {
                        etEndDate.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void clearDates() {
        etStartDate.setText("");
        etEndDate.setText("");
    }

    private void clearPrices() {
        etMinPrice.setText("");
        etMaxPrice.setText("");
    }

    private void clearLanguage() {
        actLanguage.setText("");
    }

    private void clearCity() {
        actCity.setText("");
    }

    private void clearAllFilters() {
        clearDates();
        clearPrices();
        clearLanguage();
        clearCity();
    }

    private void applyFilters() {
        Intent resultIntent = new Intent();
        
        // Get filter values
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String language = actLanguage.getText().toString().trim();
        String city = actCity.getText().toString().trim();
        
        double minPrice = 0;
        double maxPrice = Double.MAX_VALUE;
        
        try {
            if (!TextUtils.isEmpty(etMinPrice.getText().toString())) {
                minPrice = Double.parseDouble(etMinPrice.getText().toString());
            }
        } catch (NumberFormatException e) {
            minPrice = 0;
        }
        
        try {
            if (!TextUtils.isEmpty(etMaxPrice.getText().toString())) {
                maxPrice = Double.parseDouble(etMaxPrice.getText().toString());
            }
        } catch (NumberFormatException e) {
            maxPrice = Double.MAX_VALUE;
        }
        
        // Pass filter values back to tours activity
        resultIntent.putExtra("start_date", startDate);
        resultIntent.putExtra("end_date", endDate);
        resultIntent.putExtra("min_price", minPrice);
        resultIntent.putExtra("max_price", maxPrice);
        resultIntent.putExtra("language", language);
        resultIntent.putExtra("city", city);
        
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
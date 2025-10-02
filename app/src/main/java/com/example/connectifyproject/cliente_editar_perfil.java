package com.example.connectifyproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.connectifyproject.models.Cliente_User;
import java.util.Calendar;

public class cliente_editar_perfil extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    
    private ImageView ivProfilePhoto;
    private MaterialButton btnSubirImagen, btnGuardar;
    private TextInputEditText etNombre, etApellido, etNumeroDocumento, etTelefono, etFechaNacimiento, etDomicilio;
    private AutoCompleteTextView spinnerTipoDocumento;
    private TextInputLayout tilFechaNacimiento;
    
    private String[] tiposDocumento = {"DNI", "Pasaporte", "Carnet de extranjería"};
    
    // Modelo de datos del usuario
    private Cliente_User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_editar_perfil);

        // Initialize views
        initViews();
        setupToolbar();
        setupDropdown();
        setupDatePicker();
        setupImageUpload();
        setupSaveButton();
        
        // Load user data from backend (simulate with hardcoded data)
        loadUserData();
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        btnSubirImagen = findViewById(R.id.btn_subir_imagen);
        btnGuardar = findViewById(R.id.btn_guardar);
        etNombre = findViewById(R.id.et_nombre);
        etApellido = findViewById(R.id.et_apellido);
        etNumeroDocumento = findViewById(R.id.et_numero_documento);
        etTelefono = findViewById(R.id.et_telefono);
        etFechaNacimiento = findViewById(R.id.et_fecha_nacimiento);
        etDomicilio = findViewById(R.id.et_domicilio);
        spinnerTipoDocumento = findViewById(R.id.spinner_tipo_documento);
        tilFechaNacimiento = findViewById(R.id.til_fecha_nacimiento);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Editar perfil");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, tiposDocumento);
        spinnerTipoDocumento.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        
        tilFechaNacimiento.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        etFechaNacimiento.setText(selectedDate);
                    }
                }, year, month, day);
        
        // Set max date to today (user can't select future dates)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupImageUpload() {
        btnSubirImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });
        
        ivProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void setupSaveButton() {
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private void loadUserData() {
        // TODO: En producción, esto vendría de Intent extras o API
        // Por ahora usamos datos hardcodeados
        currentUser = Cliente_User.crearUsuarioEjemplo();
        
        // Cargar datos en los campos del formulario
        if (currentUser != null) {
            etNombre.setText(currentUser.getNombre());
            etApellido.setText(currentUser.getApellidos());
            spinnerTipoDocumento.setText(currentUser.getTipoDocumento(), false);
            etNumeroDocumento.setText(currentUser.getNumeroDocumento());
            etTelefono.setText(currentUser.getTelefono());
            etFechaNacimiento.setText(currentUser.getFechaNacimiento());
            etDomicilio.setText(currentUser.getDomicilio());
        }
    }

    private void saveProfile() {
        // TODO: Implement save functionality
        // Get all field values
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String tipoDocumento = spinnerTipoDocumento.getText().toString().trim();
        String numeroDocumento = etNumeroDocumento.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        String domicilio = etDomicilio.getText().toString().trim();

        // Validate fields
        if (nombre.isEmpty() || apellido.isEmpty() || numeroDocumento.isEmpty()) {
            // Show error message
            return;
        }

        // Actualizar el modelo con los nuevos datos
        if (currentUser != null) {
            currentUser.setNombre(nombre);
            currentUser.setApellidos(apellido);
            currentUser.setTipoDocumento(tipoDocumento);
            currentUser.setNumeroDocumento(numeroDocumento);
            currentUser.setTelefono(telefono);
            currentUser.setFechaNacimiento(fechaNacimiento);
            currentUser.setDomicilio(domicilio);
        }
        
        // TODO: Send data to backend/API
        // Por ahora solo mostramos un mensaje de éxito
        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
        
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    // Configurar la imagen para que cubra todo el círculo
                    ivProfilePhoto.setImageURI(selectedImageUri);
                    
                    // Configuración para que la imagen cubra completamente el círculo
                    ivProfilePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    
                    // Remover cualquier tint o background que pueda interferir
                    ivProfilePhoto.setBackgroundTintList(null);
                    ivProfilePhoto.setImageTintList(null);
                    
                    // Remover padding para que la imagen cubra todo el espacio circular
                    ivProfilePhoto.setPadding(0, 0, 0, 0);
                    
                    // Remover el background para mostrar solo la imagen
                    ivProfilePhoto.setBackground(null);
                    
                    // Asegurar que mantenga la forma circular
                    ivProfilePhoto.setClipToOutline(true);
                    
                    Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                    
                    // En caso de error, mantener la imagen por defecto
                    ivProfilePhoto.setImageResource(R.drawable.ic_person);
                    ivProfilePhoto.setScaleType(ImageView.ScaleType.CENTER);
                }
                
                // TODO: Upload image to backend
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
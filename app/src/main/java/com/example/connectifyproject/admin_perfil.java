package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.connectifyproject.databinding.AdminPerfilViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.example.connectifyproject.utils.LocationSearchHelper;

public class admin_perfil extends AppCompatActivity {
    private AdminPerfilViewBinding binding;
    private LocationSearchHelper locationHelper;
    private boolean isUpdatingLocation = false; // Flag para evitar loop infinito
    private Handler searchHandler = new Handler(); // Para el debounce de búsqueda
    private Runnable searchRunnable; // Runnable para la búsqueda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminPerfilViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar la barra superior
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Inicializar helper de ubicación
        locationHelper = new LocationSearchHelper(this);

        // Configurar componentes
        setupLocationSearch();
        setupButtons();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("perfil");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }

    private void setupLocationSearch() {
        // TextWatcher para búsqueda automática con debounce
        binding.etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Solo buscar si no estamos actualizando programáticamente
                if (!isUpdatingLocation && s.length() > 3) {
                    // Cancelar búsqueda anterior si existe
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    
                    // Crear nueva búsqueda con delay para evitar demasiadas llamadas
                    searchRunnable = () -> searchLocation(s.toString());
                    searchHandler.postDelayed(searchRunnable, 500); // 500ms delay
                }
            }
        });

        // Click en el ícono de búsqueda
        binding.tilLocation.setEndIconOnClickListener(v -> {
            String query = binding.etLocation.getText().toString().trim();
            if (!query.isEmpty()) {
                searchLocation(query);
            }
        });

        // Click en la barra de búsqueda del mapa
        binding.tvSearchHint.setOnClickListener(v -> {
            String query = binding.etLocation.getText().toString().trim();
            if (!query.isEmpty()) {
                searchLocation(query);
            } else {
                binding.etLocation.requestFocus();
            }
        });
    }

    private void setupButtons() {
        // Botón cambiar logo
        binding.btnChangeLogo.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad de cambio de logo en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Sección de fotos promocionales
        binding.btnAddPhotos.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad de fotos promocionales en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Botón guardar
        binding.btnSave.setOnClickListener(v -> {
            saveProfileData();
        });
    }

    private void searchLocation(String query) {
        locationHelper.searchLocation(query, new LocationSearchHelper.LocationSearchCallback() {
            @Override
            public void onLocationFound(String address, double latitude, double longitude) {
                runOnUiThread(() -> {
                    // Marcar que estamos actualizando para evitar el loop
                    isUpdatingLocation = true;
                    
                    // Actualizar el campo de ubicación con la dirección encontrada
                    binding.etLocation.setText(address);
                    
                    // Restaurar el flag después de un pequeño delay
                    searchHandler.postDelayed(() -> {
                        isUpdatingLocation = false;
                    }, 200);
                    
                    // Aquí podrías actualizar el mapa con las coordenadas
                    updateMapLocation(latitude, longitude);
                    
                    Toast.makeText(admin_perfil.this, "Ubicación encontrada", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onLocationNotFound() {
                runOnUiThread(() -> {
                    Toast.makeText(admin_perfil.this, "No se encontró la ubicación", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(admin_perfil.this, "Error al buscar: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateMapLocation(double latitude, double longitude) {
        // Aquí se puede implementar la actualización del mapa
        // Por ahora solo mostramos las coordenadas en el log
        System.out.println("Ubicación actualizada: " + latitude + ", " + longitude);
    }

    private void saveProfileData() {
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();

        if (email.isEmpty() || phone.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí se puede implementar el guardado real de los datos
        Toast.makeText(this, "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();

        // Navegar de regreso al dashboard
        Intent intent = new Intent(this, admin_dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar callbacks pendientes
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}

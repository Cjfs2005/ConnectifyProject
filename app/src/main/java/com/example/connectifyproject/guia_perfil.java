package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.GuiaActivityPerfilBinding;
import com.example.connectifyproject.fragment.GuiaLanguageDialogFragment;
import com.example.connectifyproject.fragment.GuiaPaymentMethodDialogFragment;
import com.example.connectifyproject.model.GuiaLanguage;
import com.example.connectifyproject.model.GuiaPaymentMethod;
import com.example.connectifyproject.ui.guia.GuiaLanguageAdapter;
import com.example.connectifyproject.ui.guia.GuiaPaymentMethodAdapter;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class guia_perfil extends AppCompatActivity {
    private GuiaActivityPerfilBinding binding;
    private List<GuiaLanguage> languages = new ArrayList<>();
    private GuiaLanguageAdapter languageAdapter;
    private List<GuiaPaymentMethod> paymentMethods = new ArrayList<>();
    private GuiaPaymentMethodAdapter paymentMethodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.profileImage.setImageResource(android.R.drawable.ic_menu_gallery);
        binding.nameText.setText("Gianmarco Martinez Soler");
        binding.roleText.setText("Guía Turístico");
        binding.dniText.setText("DNI: 78965432");
        binding.emailText.setText("jerez@gmail.com");
        binding.phoneText.setText("Teléfono: 987654321");
        binding.toursCountText.setText("Tours guiados: 14");
        binding.ratingText.setText("Calificación promedio: 4.0 ★★★★☆");

        binding.languagesRecycler.setLayoutManager(new LinearLayoutManager(this));
        languageAdapter = new GuiaLanguageAdapter(this, languages);
        binding.languagesRecycler.setAdapter(languageAdapter);
        loadLanguages();

        binding.billingRecycler.setLayoutManager(new LinearLayoutManager(this));
        paymentMethodAdapter = new GuiaPaymentMethodAdapter(this, paymentMethods, updated -> {
            paymentMethods = updated;
            paymentMethodAdapter.updateList(paymentMethods);
        });
        binding.billingRecycler.setAdapter(paymentMethodAdapter);
        loadPaymentMethods();

        binding.editLanguagesBtn.setOnClickListener(v -> {
            GuiaLanguageDialogFragment dialog = GuiaLanguageDialogFragment.newInstance(languages, updated -> {
                languages = updated;
                languageAdapter.updateList(languages);
                Toast.makeText(this, "Idioma agregado", Toast.LENGTH_SHORT).show();
            });
            dialog.show(getSupportFragmentManager(), "add_lang");
        });
        binding.addBillingBtn.setOnClickListener(v -> {
            GuiaPaymentMethodDialogFragment dialog = GuiaPaymentMethodDialogFragment.newInstance(paymentMethods, -1, updated -> {
                paymentMethods = updated;
                paymentMethodAdapter.updateList(paymentMethods);
                Toast.makeText(this, "Método de pago agregado", Toast.LENGTH_SHORT).show();
            });
            dialog.show(getSupportFragmentManager(), "add_payment");
        });
        binding.logoutBtn.setOnClickListener(v -> {
            // Cerrar sesión de Firebase Auth
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(this, SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
        });

        binding.editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_editar_perfil.class);
            startActivity(intent);
        });

        binding.layoutNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_config_notificaciones.class);
            startActivity(intent);
        });

        binding.layoutPermissions.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_permisos.class);
            startActivity(intent);
        });

        binding.btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_notificaciones.class);
            intent.putExtra("origin_activity", getClass().getSimpleName()); // O usa el nombre específico, e.g., "guia_historial"
            startActivity(intent);
        });

        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_perfil);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class));
                return true;
            } else if (id == R.id.nav_ofertas) {
                startActivity(new Intent(this, guia_tours_ofertas.class));
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, guia_assigned_tours.class));
                return true;
            } else if (id == R.id.nav_perfil) {
                return true;
            }
            return false;
        });
    }

    private void loadLanguages() {
        languages.clear();
        languages.add(new GuiaLanguage("Español"));
        languages.add(new GuiaLanguage("Inglés"));
        languages.add(new GuiaLanguage("Italiano"));
        languages.add(new GuiaLanguage("Francés"));
        languageAdapter.updateList(languages);
    }

    private void loadPaymentMethods() {
        paymentMethods.clear();
        paymentMethods.add(new GuiaPaymentMethod("Tarjeta Crédito", "1234", "Juan Perez", "MM/AA", null));
        paymentMethods.add(new GuiaPaymentMethod("Cuenta Bancaria", "1382938994832923", "Juan Perez", null, null));
        paymentMethods.add(new GuiaPaymentMethod("Yape", "969128802", "Juan Perez", null, null));
        paymentMethodAdapter.updateList(paymentMethods);
    }
}
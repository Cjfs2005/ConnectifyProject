package com.example.connectifyproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.connectifyproject.databinding.GuiaHistorialBinding;
import com.example.connectifyproject.fragment.GuiaPagosFragment;
import com.example.connectifyproject.fragment.GuiaToursFragment;
import com.example.connectifyproject.service.GuiaNotificationService;
import com.example.connectifyproject.storage.GuiaPreferencesManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class guia_historial extends AppCompatActivity {
    private GuiaHistorialBinding binding;
    private GuiaNotificationService notificationService;
    private GuiaPreferencesManager preferencesManager;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaHistorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar servicios
        notificationService = new GuiaNotificationService(this);
        preferencesManager = new GuiaPreferencesManager(this);

        // Verificar permisos de notificaciones
        checkNotificationPermissions();

        setSupportActionBar(binding.toolbar);

        // Verificar si viene de una notificación
        handleNotificationIntent();

        // Notificaciones (redirige a guia_notificaciones)
        binding.btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_notificaciones.class);
            intent.putExtra("origin_activity", getClass().getSimpleName());
            startActivity(intent);
        });

        // ViewPager con tabs (Pagos en pos 0, Tours en pos 1)
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return 2;
            }

            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new GuiaPagosFragment();
                } else {
                    return new GuiaToursFragment();
                }
            }
        });

        // Tabs labels
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Pagos Recibidos" : "Tours");
        }).attach();

        // Bottom Navigation
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_historial);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                return true;
            } else if (id == R.id.nav_ofertas) {
                Intent intent = new Intent(this, guia_tours_ofertas.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  // ✅ Añadir
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_tours) {
                Intent intent = new Intent(this, guia_assigned_tours.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  // ✅ Añadir
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_perfil) {
                Intent intent = new Intent(this, guia_perfil.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  // ✅ Añadir
                startActivity(intent);
                return true;
            }
            return false;
        });

        // BOTONES DE PRUEBA PARA NOTIFICACIONES
        addTestNotificationButtons();
        
        // Inicializar configuraciones por defecto
        initializeDefaultPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Historial" esté seleccionado cuando regresamos a esta actividad
        if (binding.bottomNav != null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_historial);
        }

        // Forzar recarga de los fragments de historial
        if (binding.viewPager != null) {
            FragmentStateAdapter adapter = (FragmentStateAdapter) binding.viewPager.getAdapter();
            if (adapter != null) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + i);
                    if (fragment instanceof com.example.connectifyproject.fragment.GuiaToursFragment) {
                        ((com.example.connectifyproject.fragment.GuiaToursFragment) fragment).loadFromDB();
                    } else if (fragment instanceof com.example.connectifyproject.fragment.GuiaPagosFragment) {
                        ((com.example.connectifyproject.fragment.GuiaPagosFragment) fragment).loadPaymentsFromFirestore();
                    }
                }
            }
        }
    }

    private void checkNotificationPermissions() {
        // Permisos deshabilitados - no solicitar automáticamente en esta pantalla
        // Los permisos se deben solicitar en guia_permisos.java
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Ya no se solicitan permisos en esta pantalla
    }

    private void handleNotificationIntent() {
        String notificationType = getIntent().getStringExtra("notification_type");
        if (notificationType != null) {
            switch (notificationType) {
                case "tour_reminder":
                    binding.viewPager.setCurrentItem(1); // Tab de tours
                    Toast.makeText(this, "📅 Recordatorio de tour activado", Toast.LENGTH_LONG).show();
                    break;
                case "new_offer":
                    Toast.makeText(this, "🎯 Nueva oferta disponible", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private void initializeDefaultPreferences() {
        // Configurar preferencias por defecto si es la primera vez
        if (preferencesManager.getGuideLanguages().isEmpty()) {
            java.util.Set<String> defaultLanguages = new java.util.HashSet<>();
            defaultLanguages.add("Español");
            defaultLanguages.add("Inglés");
            preferencesManager.saveGuideLanguages(defaultLanguages);
        }
        
        // Exportar configuraciones actuales al log para debugging
        preferencesManager.exportPreferencesToLog();
    }

    // MÉTODOS DE PRUEBA PARA NOTIFICACIONES
    private void addTestNotificationButtons() {
        // NOTA: Estos son eventos de prueba - en producción serían llamados por eventos reales
        
        // Simular nueva oferta (presionar 3 segundos el toolbar)
        binding.toolbar.setOnLongClickListener(v -> {
            testNewOfferNotification();
            return true;
        });
        
        // Las pruebas de recordatorios se han movido a lugares más fáciles de activar:
        // - Recordatorios de Tours: Toolbar de guia_assigned_tours
        // - Recordatorio de Ubicación: Toolbar de guia_tours_ofertas
    }

    // MÉTODOS DE PRUEBA ESPECÍFICOS
    
    public void testNewOfferNotification() {
        if (preferencesManager.isNotificationEnabled("new_offers")) {
            notificationService.sendNewTourOfferNotification(
                "Tour Machu Picchu Premium", 
                "Inca Adventures SAC", 
                450.0
            );
            Toast.makeText(this, "🎯 Notificación de nueva oferta enviada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "⚠️ Notificaciones de ofertas desactivadas", Toast.LENGTH_SHORT).show();
        }
    }

    public void testTourReminders() {
        if (preferencesManager.isNotificationEnabled("tour_reminders")) {
            // Simular 3 recordatorios: hoy, mañana, en 2 días
            notificationService.sendTourReminderNotification(
                "City Tour Lima Histórica", "23/10/2025", "9:00 AM", 0
            );
            notificationService.sendTourReminderNotification(
                "Tour Barranco y Miraflores", "24/10/2025", "2:00 PM", 1
            );
            notificationService.sendTourReminderNotification(
                "Tour Gastronómico", "25/10/2025", "11:00 AM", 2
            );
            Toast.makeText(this, "📅 Recordatorios de tours enviados (hoy, mañana, 2 días)", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "⚠️ Recordatorios de tours desactivados", Toast.LENGTH_SHORT).show();
        }
    }

    public void testLocationReminder() {
        if (preferencesManager.isNotificationEnabled("location_reminders")) {
            notificationService.sendLocationReminderNotification("Plaza de Armas");
            Toast.makeText(this, "📍 Recordatorio de ubicación enviado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "⚠️ Recordatorios de ubicación desactivados", Toast.LENGTH_SHORT).show();
        }
    }

    public void testCheckInReminder() {
        if (preferencesManager.isNotificationEnabled("checkin_reminders")) {
            notificationService.sendCheckInReminderNotification("City Tour Lima Histórica");
            Toast.makeText(this, "✅ Recordatorio de check-in enviado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "⚠️ Recordatorios de check-in desactivados", Toast.LENGTH_SHORT).show();
        }
    }

    public void testCheckOutReminder() {
        if (preferencesManager.isNotificationEnabled("checkout_reminders")) {
            notificationService.sendCheckOutReminderNotification("City Tour Lima Histórica");
            Toast.makeText(this, "🏁 Recordatorio de check-out enviado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "⚠️ Recordatorios de check-out desactivados", Toast.LENGTH_SHORT).show();
        }
    }

    // Métodos públicos para llamar desde otras actividades
    public void simulateCheckInPhase(String tourName) {
        testCheckInReminder();
    }

    public void simulateCheckOutPhase(String tourName) {
        testCheckOutReminder();
    }
}
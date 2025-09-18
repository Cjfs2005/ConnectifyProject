package com.example.connectifyproject;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class ProfileActivity extends AppCompatActivity implements BottomNavFragment.OnNavItemClickListener {

    // Datos del perfil hardcodeados
    private String userName = "Jorge Romero Paredes";
    private String userInitials = "JR";
    
    // Información personal
    private String[] personalInfoTitles = {
        "DNI",
        "Correo", 
        "Teléfono",
        "Fecha de nacimiento",
        "Domicilio"
    };
    
    private String[] personalInfoValues = {
        "70910370",
        "jromero@gmail.com",
        "924425834",
        "16/04/2001",
        "Calle Avenida 263, Lima, Perú"
    };
    
    private int[] personalInfoIcons = {
        R.drawable.ic_id_card,
        R.drawable.ic_email,
        R.drawable.ic_phone,
        R.drawable.ic_calendar,
        R.drawable.ic_location
    };
    
    // Métodos de pago
    private String paymentMethod = "Tarjeta VISA xxxx xxxx xxxx 2234";
    
    // Opciones de configuración
    private String[] configTitles = {
        "Cambiar contraseña",
        "Permisos"
    };
    
    private String[] configSubtitles = {
        "¿Olvidaste tu contraseña?",
        "GPS Activo, Cámara Activa"
    };
    
    private int[] configIcons = {
        R.drawable.ic_lock,
        R.drawable.ic_settings
    };
    
    // Elementos de UI
    private TextView tvUserName;
    private TextView tvUserInitials;
    private LinearLayout personalInfoContainer;
    private LinearLayout configContainer;
    private TextView tvPaymentMethod;
    private BottomNavFragment bottomNavFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initViews();
        setupBottomNavigation();
        populateUserInfo();
        populatePersonalInfo();
        populatePaymentInfo();
        populateConfigOptions();
        setupClickListeners();
    }
    
    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserInitials = findViewById(R.id.tv_user_initials);
        personalInfoContainer = findViewById(R.id.personal_info_container);
        configContainer = findViewById(R.id.config_container);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
    }
    
    private void setupBottomNavigation() {
        bottomNavFragment = new BottomNavFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottom_nav_container, bottomNavFragment)
                .commit();
        
        // Esperar a que el fragment se cargue y luego seleccionar "Perfil"
        getSupportFragmentManager().executePendingTransactions();
        
        // Usar un post para asegurar que el fragment esté completamente inicializado
        findViewById(R.id.bottom_nav_container).post(() -> {
            if (bottomNavFragment != null) {
                bottomNavFragment.setSelectedItem(R.id.nav_perfil);
            }
        });
    }
    
    private void populateUserInfo() {
        tvUserName.setText(userName);
        tvUserInitials.setText(userInitials);
    }
    
    private void populatePersonalInfo() {
        for (int i = 0; i < personalInfoTitles.length; i++) {
            View itemView = createInfoItem(
                personalInfoIcons[i],
                personalInfoTitles[i], 
                personalInfoValues[i],
                false
            );
            personalInfoContainer.addView(itemView);
        }
    }
    
    private void populatePaymentInfo() {
        tvPaymentMethod.setText(paymentMethod);
    }
    
    private void populateConfigOptions() {
        for (int i = 0; i < configTitles.length; i++) {
            View itemView = createConfigItem(
                configIcons[i],
                configTitles[i],
                configSubtitles[i]
            );
            configContainer.addView(itemView);
        }
    }
    
    private void setupClickListeners() {
        // Botón editar perfil
        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            // TODO: Implementar edición de perfil
        });
        
        // Logout
        findViewById(R.id.logout_container).setOnClickListener(v -> {
            // TODO: Implementar logout
        });
        
        // Métodos de pago
        findViewById(R.id.tv_payment_method).getRootView().setOnClickListener(v -> {
            // TODO: Implementar gestión de métodos de pago
        });
    }
    
    private View createInfoItem(int iconRes, String title, String value, boolean showArrow) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_profile_info, null);
        
        ImageView icon = itemView.findViewById(R.id.iv_icon);
        TextView tvTitle = itemView.findViewById(R.id.tv_title);
        TextView tvValue = itemView.findViewById(R.id.tv_value);
        ImageView arrow = itemView.findViewById(R.id.iv_arrow);
        
        icon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvValue.setText(value);
        arrow.setVisibility(showArrow ? View.VISIBLE : View.GONE);
        
        return itemView;
    }
    
    private View createConfigItem(int iconRes, String title, String subtitle) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_profile_config, null);
        
        ImageView icon = itemView.findViewById(R.id.iv_icon);
        TextView tvTitle = itemView.findViewById(R.id.tv_title);
        TextView tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
        
        icon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);
        
        // Agregar click listener para futuras navegaciones
        itemView.setOnClickListener(v -> {
            // TODO: Implementar navegación a otras activities
        });
        
        return itemView;
    }
    
    @Override
    public void onNavItemClick(int itemId) {
        if (itemId == R.id.nav_perfil) {
            // Ya estamos en perfil, marcar como seleccionado
            if (bottomNavFragment != null) {
                bottomNavFragment.setSelectedItem(R.id.nav_perfil);
            }
            return;
        }
        
        // TODO: Implementar navegación a otras activities basado en itemId
        // Por ahora solo logueamos la navegación
        String destination = "";
        if (itemId == R.id.nav_home) {
            destination = "Inicio";
        } else if (itemId == R.id.nav_reservas) {
            destination = "Reservas";
        } else if (itemId == R.id.nav_tours) {
            destination = "Tours";
        } else if (itemId == R.id.nav_chat) {
            destination = "Chat";
        }
        
        // Aquí puedes agregar la lógica de navegación cuando tengas las otras activities
        // Intent intent = new Intent(this, OtherActivity.class);
        // startActivity(intent);
    }
}

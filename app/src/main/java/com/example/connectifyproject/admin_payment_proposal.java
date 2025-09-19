package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.connectifyproject.databinding.AdminPaymentProposalViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;

public class admin_payment_proposal extends AppCompatActivity {
    private AdminPaymentProposalViewBinding binding;
    private String tourTitulo;
    private String guideName;
    private double guideRating;
    private String guideLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminPaymentProposalViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener datos del Intent
        tourTitulo = getIntent().getStringExtra("tour_titulo");
        guideName = getIntent().getStringExtra("guide_name");
        guideRating = getIntent().getDoubleExtra("guide_rating", 0.0);
        guideLanguages = getIntent().getStringExtra("guide_languages");

        setupUI();
        setupButtons();
        setupBottomNavigation();
    }

    private void setupUI() {
        // Configurar toolbar
        binding.topAppBar.setNavigationOnClickListener(v -> finish());
        binding.topAppBar.setTitle("Propuesta de Pago");

        // Mostrar información del tour y guía
        binding.tvTourTitle.setText(tourTitulo);
        binding.tvGuideName.setText(guideName);
        binding.tvGuideRating.setText(String.format("★ %.1f", guideRating));
        binding.tvGuideLanguages.setText(guideLanguages);

        // Configurar información de pago por defecto
        binding.etTourPrice.setText("150.00");
        binding.etGuideCommission.setText("20.00");
        binding.etPlatformFee.setText("15.00");
        binding.tvTotalAmount.setText("S/ 185.00");

        // Calcular automáticamente cuando cambien los valores
        setupPriceCalculation();
    }

    private void setupPriceCalculation() {
        View.OnFocusChangeListener recalculateListener = (v, hasFocus) -> {
            if (!hasFocus) {
                calculateTotal();
            }
        };

        binding.etTourPrice.setOnFocusChangeListener(recalculateListener);
        binding.etGuideCommission.setOnFocusChangeListener(recalculateListener);
        binding.etPlatformFee.setOnFocusChangeListener(recalculateListener);
    }

    private void calculateTotal() {
        try {
            double tourPrice = Double.parseDouble(binding.etTourPrice.getText().toString());
            double guideCommission = Double.parseDouble(binding.etGuideCommission.getText().toString());
            double platformFee = Double.parseDouble(binding.etPlatformFee.getText().toString());
            
            double total = tourPrice + guideCommission + platformFee;
            binding.tvTotalAmount.setText(String.format("S/ %.2f", total));
        } catch (NumberFormatException e) {
            binding.tvTotalAmount.setText("S/ 0.00");
        }
    }

    private void setupButtons() {
        binding.btnSendProposal.setOnClickListener(v -> {
            sendPaymentProposal();
        });

        binding.btnCancel.setOnClickListener(v -> {
            finish();
        });
    }

    private void sendPaymentProposal() {
        // Validar campos
        String tourPrice = binding.etTourPrice.getText().toString().trim();
        String paymentTerms = binding.etPaymentTerms.getText().toString().trim();
        String additionalNotes = binding.etAdditionalNotes.getText().toString().trim();

        if (tourPrice.isEmpty()) {
            binding.etTourPrice.setError("Ingrese el precio del tour");
            return;
        }

        if (paymentTerms.isEmpty()) {
            binding.etPaymentTerms.setError("Ingrese los términos de pago");
            return;
        }

        // Simular envío de propuesta
        Toast.makeText(this, "Propuesta enviada al guía " + guideName, Toast.LENGTH_LONG).show();
        
        // Regresar a la lista de tours
        Intent intent = new Intent(this, admin_tours.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("tours");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }
}

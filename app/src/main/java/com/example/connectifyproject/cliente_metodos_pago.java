package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class cliente_metodos_pago extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PaymentMethodAdapter adapter;
    private List<PaymentMethod> paymentMethods;
    private FloatingActionButton fabAddPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_metodos_pago);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerPaymentMethods);
        fabAddPayment = findViewById(R.id.fab_add_payment_method);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Métodos de pago");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupRecyclerView() {
        // Datos hardcodeados
        paymentMethods = new ArrayList<>();
        paymentMethods.add(new PaymentMethod("**** **** **** 2934", "Fecha de vencimiento 12/28"));
        paymentMethods.add(new PaymentMethod("**** **** **** 1340", "Fecha de vencimiento 11/27"));

        adapter = new PaymentMethodAdapter(paymentMethods, new PaymentMethodAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                showDeleteConfirmationDialog(position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cliente_metodos_pago.this, cliente_nuevo_metodo_pago.class);
                startActivity(intent);
            }
        });
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar método de pago")
                .setMessage("¿Está seguro de que desea eliminar este método de pago?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Por ahora solo cerramos el dialog
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // Clase para representar un método de pago
    public static class PaymentMethod {
        private String cardNumber;
        private String expiryDate;

        public PaymentMethod(String cardNumber, String expiryDate) {
            this.cardNumber = cardNumber;
            this.expiryDate = expiryDate;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public String getExpiryDate() {
            return expiryDate;
        }
    }
}
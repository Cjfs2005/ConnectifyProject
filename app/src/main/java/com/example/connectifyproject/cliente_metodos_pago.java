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
import com.example.connectifyproject.models.Cliente_PaymentMethod;
import com.example.connectifyproject.adapters.Cliente_PaymentMethodAdapter;
import java.util.ArrayList;
import java.util.List;

public class cliente_metodos_pago extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Cliente_PaymentMethodAdapter adapter;
    private List<Cliente_PaymentMethod> paymentMethods;
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
        // Datos hardcodeados usando el nuevo modelo
        paymentMethods = new ArrayList<>();
        paymentMethods.add(Cliente_PaymentMethod.crearEjemploVisa());
        paymentMethods.add(Cliente_PaymentMethod.crearEjemploMastercard());

        adapter = new Cliente_PaymentMethodAdapter(paymentMethods, new Cliente_PaymentMethodAdapter.OnDeleteClickListener() {
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

}
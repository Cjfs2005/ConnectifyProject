package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.connectifyproject.models.Cliente_PaymentMethod;
import com.example.connectifyproject.adapters.AdminPaymentMethodSelectionAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity para que el admin seleccione un método de pago antes de asignar un guía.
 * Esta es solo una visualización - no guarda nada en Firebase.
 */
public class AdminSelectPaymentMethodActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminPaymentMethodSelectionAdapter adapter;
    private List<Cliente_PaymentMethod> paymentMethods;
    private FloatingActionButton fabAddPayment;
    private LinearLayout emptyStateLayout;
    private MaterialButton btnContinue;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration paymentMethodsListener;
    
    private String selectedPaymentMethodId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_select_payment_method);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadPaymentMethods();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerPaymentMethods);
        fabAddPayment = findViewById(R.id.fab_add_payment_method);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        btnContinue = findViewById(R.id.btn_continue);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Seleccionar método de pago");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        paymentMethods = new ArrayList<>();
        
        adapter = new AdminPaymentMethodSelectionAdapter(paymentMethods, 
            new AdminPaymentMethodSelectionAdapter.OnItemClickListener() {
                @Override
                public void onSelectClick(int position, Cliente_PaymentMethod paymentMethod) {
                    selectedPaymentMethodId = paymentMethod.getId();
                    adapter.setSelectedPosition(position);
                    adapter.notifyDataSetChanged();
                    btnContinue.setEnabled(true);
                }
            });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAddPayment.setOnClickListener(v -> {
            Intent intent = new Intent(AdminSelectPaymentMethodActivity.this, cliente_agregar_metodo_pago.class);
            startActivity(intent);
        });
        
        btnContinue.setOnClickListener(v -> {
            if (selectedPaymentMethodId == null) {
                Toast.makeText(this, "Por favor selecciona un método de pago", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Retornar a la actividad anterior con el método seleccionado
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedPaymentMethodId", selectedPaymentMethodId);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void loadPaymentMethods() {
        String userId = currentUser.getUid();
        
        paymentMethodsListener = db.collection("usuarios")
                .document(userId)
                .collection("payment_methods")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error al cargar métodos de pago", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        paymentMethods.clear();
                        
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Cliente_PaymentMethod method = doc.toObject(Cliente_PaymentMethod.class);
                            method.setId(doc.getId());
                            paymentMethods.add(method);
                        }

                        // Actualizar UI
                        if (paymentMethods.isEmpty()) {
                            emptyStateLayout.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            btnContinue.setEnabled(false);
                        } else {
                            emptyStateLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paymentMethodsListener != null) {
            paymentMethodsListener.remove();
        }
    }
}

package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.example.connectifyproject.models.Cliente_PaymentMethod;
import com.example.connectifyproject.adapters.Cliente_PaymentMethodAdapter;
import java.util.ArrayList;
import java.util.List;

public class cliente_metodos_pago extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Cliente_PaymentMethodAdapter adapter;
    private List<Cliente_PaymentMethod> paymentMethods;
    private FloatingActionButton fabAddPayment;
    private LinearLayout emptyStateLayout;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration paymentMethodsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_metodos_pago);

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
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Métodos de pago");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        paymentMethods = new ArrayList<>();
        
        adapter = new Cliente_PaymentMethodAdapter(paymentMethods, 
            new Cliente_PaymentMethodAdapter.OnItemClickListener() {
                @Override
                public void onDeleteClick(int position, Cliente_PaymentMethod paymentMethod) {
                    showDeleteConfirmationDialog(paymentMethod);
                }

                @Override
                public void onSetDefaultClick(int position, Cliente_PaymentMethod paymentMethod) {
                    setAsDefault(paymentMethod);
                }
            });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAddPayment.setOnClickListener(v -> {
            Intent intent = new Intent(cliente_metodos_pago.this, cliente_agregar_metodo_pago.class);
            startActivity(intent);
        });
    }

    private void loadPaymentMethods() {
        android.util.Log.d("PaymentMethods", "Iniciando carga de métodos de pago para usuario: " + currentUser.getUid());
        
        // Listener en tiempo real para la subcolección payment_methods
        paymentMethodsListener = db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("payment_methods")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        android.util.Log.e("PaymentMethods", "Error en listener: " + error.getMessage(), error);
                        Toast.makeText(this, "Error al cargar métodos de pago: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        android.util.Log.d("PaymentMethods", "Documentos encontrados: " + queryDocumentSnapshots.size());
                        paymentMethods.clear();
                        
                        // Mostrar todas las tarjetas
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            android.util.Log.d("PaymentMethods", "Procesando documento: " + document.getId());
                            android.util.Log.d("PaymentMethods", "Datos del documento: " + document.getData());
                            
                            try {
                                Cliente_PaymentMethod paymentMethod = document.toObject(Cliente_PaymentMethod.class);
                                if (paymentMethod != null) {
                                    paymentMethod.setId(document.getId());
                                    paymentMethods.add(paymentMethod);
                                    android.util.Log.d("PaymentMethods", "Método agregado - Card Brand: " + paymentMethod.getCardBrand() + 
                                                      ", Last 4: " + paymentMethod.getLast4Digits() + ", Default: " + paymentMethod.isDefault());
                                } else {
                                    android.util.Log.w("PaymentMethods", "El documento no se pudo convertir a PaymentMethod");
                                }
                            } catch (Exception e) {
                                // Si hay error parseando un documento, continuar con los siguientes
                                android.util.Log.e("PaymentMethods", "Error parsing document: " + document.getId(), e);
                                continue;
                            }
                        }
                        
                        try {
                            if (!paymentMethods.isEmpty()) {
                                android.util.Log.d("PaymentMethods", "Mostrando " + paymentMethods.size() + " tarjetas");
                                hideEmptyState();
                            } else {
                                android.util.Log.w("PaymentMethods", "No hay tarjetas para mostrar");
                                showEmptyState();
                            }
                            
                            if (adapter != null) {
                                android.util.Log.d("PaymentMethods", "Actualizando adapter con " + paymentMethods.size() + " elementos");
                                adapter.updateData(paymentMethods);
                            } else {
                                android.util.Log.e("PaymentMethods", "Adapter es null");
                            }
                        } catch (Exception e) {
                            android.util.Log.e("PaymentMethods", "Error updating UI", e);
                            showEmptyState();
                        }
                    } else {
                        android.util.Log.d("PaymentMethods", "queryDocumentSnapshots es null");
                    }
                });
    }

    private void showDeleteConfirmationDialog(Cliente_PaymentMethod paymentMethod) {
        // No permitir eliminar si es el único método de pago y es default
        if (paymentMethod.isDefault() && paymentMethods.size() == 1) {
            Toast.makeText(this, "No puede eliminar el único método de pago", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Eliminar método de pago")
                .setMessage("¿Está seguro de que desea eliminar " + paymentMethod.getDisplayName() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deletePaymentMethod(paymentMethod);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void deletePaymentMethod(Cliente_PaymentMethod paymentMethod) {
        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("payment_methods")
                .document(paymentMethod.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Método de pago eliminado", Toast.LENGTH_SHORT).show();
                    
                    // Si era default y quedan más tarjetas, marcar la primera como default
                    if (paymentMethod.isDefault() && paymentMethods.size() > 1) {
                        // El listener se encargará de actualizar la lista
                        setFirstAsDefault();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setAsDefault(Cliente_PaymentMethod paymentMethod) {
        // Usar batch write para actualizar todas las tarjetas de una vez
        WriteBatch batch = db.batch();
        
        // Quitar default de todas las tarjetas
        for (Cliente_PaymentMethod pm : paymentMethods) {
            db.collection("usuarios")
                    .document(currentUser.getUid())
                    .collection("payment_methods")
                    .document(pm.getId())
                    .update("default", false);
        }
        
        // Marcar la seleccionada como default
        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("payment_methods")
                .document(paymentMethod.getId())
                .update("default", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, paymentMethod.getDisplayName() + " marcado como predeterminado",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setFirstAsDefault() {
        if (!paymentMethods.isEmpty()) {
            Cliente_PaymentMethod firstMethod = paymentMethods.get(0);
            db.collection("usuarios")
                    .document(currentUser.getUid())
                    .collection("payment_methods")
                    .document(firstMethod.getId())
                    .update("default", true);
        }
    }

    private void showEmptyState() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remover listener cuando se destruye la actividad
        if (paymentMethodsListener != null) {
            paymentMethodsListener.remove();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
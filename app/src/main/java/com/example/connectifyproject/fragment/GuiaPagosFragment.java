package com.example.connectifyproject.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.GuiaFragmentPagosBinding;
import com.example.connectifyproject.model.GuiaPayment;
import com.example.connectifyproject.ui.guia.GuiaPaymentAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GuiaPagosFragment extends Fragment {

    private static final String TAG = "GuiaPagosFragment";

    private GuiaFragmentPagosBinding binding;
    private GuiaPaymentAdapter adapter;
    private final List<GuiaPayment> payments = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = GuiaFragmentPagosBinding.inflate(inflater, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GuiaPaymentAdapter(requireContext(), payments);
        binding.recyclerView.setAdapter(adapter);

        // Cargar pagos desde Firestore
        loadPaymentsFromFirestore();

        return binding.getRoot();
    }

    /**
     * Carga desde Firestore solo los pagos recibidos por el guía logueado.
     */
    private void loadPaymentsFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "No se encontró sesión de guía",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String guideUid = user.getUid();
        Log.d(TAG, "Cargando pagos para guia uidUsuarioRecibe = " + guideUid);

        db.collection("pagos")
                .whereEqualTo("uidUsuarioRecibe", guideUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    payments.clear();

                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                    Log.d(TAG, "Pagos encontrados: " + docs.size());

                    if (docs.isEmpty()) {
                        adapter.updateList(new ArrayList<>());
                        return;
                    }

                    // Ordenar por fecha DESC (más nuevos primero)
                    Collections.sort(docs, (d1, d2) -> {
                        Timestamp t1 = d1.getTimestamp("fecha");
                        Timestamp t2 = d2.getTimestamp("fecha");

                        long l1 = (t1 != null) ? t1.toDate().getTime() : 0L;
                        long l2 = (t2 != null) ? t2.toDate().getTime() : 0L;

                        return Long.compare(l2, l1); // descendente
                    });

                    for (DocumentSnapshot doc : docs) {

                        String id = doc.getId();
                        Double amount = doc.getDouble("monto");
                        if (amount == null) amount = 0.0;

                        Timestamp ts = doc.getTimestamp("fecha");
                        String formattedDate = formatDateTime(ts);

                        String status = doc.getString("estado");
                        String tourName = doc.getString("nombreTour");
                        String uidUsuarioPaga = doc.getString("uidUsuarioPaga");

                        GuiaPayment payment = new GuiaPayment(
                                id,
                                amount,
                                formattedDate,
                                status,
                                tourName,
                                null,             // companyName, se llenará luego
                                uidUsuarioPaga
                        );

                        payments.add(payment);

                        // Traer nombre de la empresa del usuario que paga
                        fetchCompanyNameForPayer(payment);
                    }

                    adapter.updateList(new ArrayList<>(payments));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener pagos del guia", e);
                    Toast.makeText(requireContext(),
                            "Error al cargar pagos",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Busca al usuario que paga:
     *  1) Primero intentando con documentId == uidUsuarioPaga
     *  2) Si no existe, buscando por campo "uid" == uidUsuarioPaga
     * Si es Administrador y tiene nombreEmpresa, se muestra; si no, se usa nombresApellidos.
     */
    private void fetchCompanyNameForPayer(GuiaPayment payment) {
        String rawUid = payment.getUidUsuarioPaga();
        if (rawUid == null) {
            return;
        }

        final String uidUsuarioPaga = rawUid.trim();
        if (uidUsuarioPaga.isEmpty()) {
            return;
        }

        Log.d(TAG, "Buscando pagador con uidUsuarioPaga=" + uidUsuarioPaga);

        // 1) Intentar por ID de documento
        db.collection("usuarios")
                .document(uidUsuarioPaga)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d(TAG, "Encontrado usuario por documentId: " + uidUsuarioPaga);
                        applyCompanyOrName(payment, doc);
                        adapter.notifyDataSetChanged();
                    } else {
                        // 2) Si no existe documento con ese ID, buscar por campo "uid"
                        searchByUidField(payment, uidUsuarioPaga);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error buscando usuario por documentId", e);
                    // Igual intentamos por campo uid
                    searchByUidField(payment, uidUsuarioPaga);
                });
    }

    /**
     * Búsqueda alternativa: whereEqualTo("uid", uidUsuarioPaga)
     */
    private void searchByUidField(GuiaPayment payment, String uidUsuarioPaga) {
        db.collection("usuarios")
                .whereEqualTo("uid", uidUsuarioPaga)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        Log.d(TAG, "Encontrado usuario por campo uid=" + uidUsuarioPaga);
                        applyCompanyOrName(payment, doc);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "No se encontró usuario con uid=" + uidUsuarioPaga
                                + " (ni como documentId ni como campo uid).");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener usuario por campo uid", e)
                );
    }

    /**
     * Decide qué nombre mostrar en la tarjeta:
     *   - Si rol == "Administrador" y tiene nombreEmpresa -> usa nombreEmpresa
     *   - En otro caso -> usa nombresApellidos
     */
    private void applyCompanyOrName(GuiaPayment payment, DocumentSnapshot doc) {
        String rol = doc.getString("rol");
        String companyName = doc.getString("nombreEmpresa");
        String fullName = doc.getString("nombresApellidos");

        String label;
        if ("Administrador".equalsIgnoreCase(rol) && companyName != null && !companyName.isEmpty()) {
            label = companyName;
        } else if (fullName != null && !fullName.isEmpty()) {
            label = fullName;
        } else {
            label = ""; // por si acaso
        }

        payment.setCompanyName(label);
        Log.d(TAG, "Nombre asociado al pago: " + label + " (rol=" + rol + ")");
    }

    private String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf =
                new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.connectifyproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.example.connectifyproject.utils.AuthConstants;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class admin_pagos extends AppCompatActivity {

    // ---------- Firebase ----------
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // ---------- UI ----------
    private EditText editTextBuscar;          // etSearch (oculta)
    private AutoCompleteTextView spinnerMeses; // dropdown de meses
    private RecyclerView recyclerViewPagos;

    private PagosAdapter adapter;

    // ---------- Datos ----------
    private final List<PagoItem> listaCompletaPagos = new ArrayList<>();
    private final List<PagoItem> listaFiltradaPagos = new ArrayList<>();

    // Estado de filtros
    private int mesSeleccionado = 0; // 0 = todos, 1..12 = ene..dic

    // Formato de fecha/hora
    private static final Locale LOCALE_PE = new Locale("es", "PE");
    private static final TimeZone TIMEZONE_LIMA = TimeZone.getTimeZone("America/Lima");
    private static final SimpleDateFormat FORMATO_FECHA_HORA =
            new SimpleDateFormat("dd/MM/yyyy h:mm a", LOCALE_PE);

    static {
        FORMATO_FECHA_HORA.setTimeZone(TIMEZONE_LIMA);
    }

    private static final String TAG = "AdminPagos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_pagos_view);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Toolbar
        View topAppBar = findViewById(R.id.topAppBar);
        if (topAppBar != null) {
            topAppBar.setOnClickListener(v -> onBackPressed());
        }

        // ---------- Referencias UI ----------
        editTextBuscar = findViewById(R.id.etSearch);
        spinnerMeses = findViewById(R.id.spinnerMonthFilter);
        recyclerViewPagos = findViewById(R.id.recyclerViewPagos);

        // Ocultar COMPLETAMENTE la barra de búsqueda (lupa + caja)
        TextInputLayout tilSearch = findViewById(R.id.tilSearch);
        if (tilSearch != null) {
            tilSearch.setVisibility(View.GONE);
        }
        if (editTextBuscar != null) {
            editTextBuscar.setVisibility(View.GONE);
        }

        // RecyclerView
        recyclerViewPagos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PagosAdapter(listaFiltradaPagos);
        recyclerViewPagos.setAdapter(adapter);

        // Bottom nav admin
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("pagos");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavContainer, bottomNavFragment)
                .commit();

        configurarDropdownMeses();
        cargarPagosParaAdministradorActual();
    }

    // =========================================================
    // DROPDOWN DE MESES (Material exposed dropdown)
    // =========================================================

    private void configurarDropdownMeses() {
        if (spinnerMeses == null) return;

        List<String> meses = new ArrayList<>();
        meses.add("Todos los meses");
        meses.add("Enero");
        meses.add("Febrero");
        meses.add("Marzo");
        meses.add("Abril");
        meses.add("Mayo");
        meses.add("Junio");
        meses.add("Julio");
        meses.add("Agosto");
        meses.add("Septiembre");
        meses.add("Octubre");
        meses.add("Noviembre");
        meses.add("Diciembre");

        ArrayAdapter<String> adapterMeses = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                meses
        );
        spinnerMeses.setAdapter(adapterMeses);

        // Valor por defecto
        spinnerMeses.setText(meses.get(0), false);

        spinnerMeses.setOnItemClickListener((parent, view, position, id) -> {
            mesSeleccionado = position; // 0 = todos, 1..12 = ene..dic
            aplicarFiltros();
        });
    }

    // =========================================================
    // CARGA DE PAGOS DESDE FIREBASE
    // =========================================================

    private void cargarPagosParaAdministradorActual() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listaCompletaPagos.clear();
            listaFiltradaPagos.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        String adminUid = currentUser.getUid();

        db.collection("pagos")
                .whereEqualTo("uidUsuarioRecibe", adminUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaCompletaPagos.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        String tipoPago = doc.getString("tipoPago");
                        // Solo pagos "A Empresa"
                        if (tipoPago == null || !tipoPago.equals("A Empresa")) {
                            continue;
                        }

                        Object fechaObj = doc.get("fecha");
                        if (fechaObj == null) continue;

                        String fechaFormateada;
                        int mesNumero;

                        if (fechaObj instanceof Timestamp) {
                            Timestamp ts = (Timestamp) fechaObj;
                            Date date = ts.toDate();

                            fechaFormateada = FORMATO_FECHA_HORA.format(date);

                            Calendar cal = Calendar.getInstance(TIMEZONE_LIMA, LOCALE_PE);
                            cal.setTime(date);
                            mesNumero = cal.get(Calendar.MONTH) + 1; // 1..12
                        } else if (fechaObj instanceof String) {
                            // Compatibilidad con pagos antiguos en String
                            String fechaRaw = (String) fechaObj;
                            fechaFormateada = formatearFechaHoraCadena(fechaRaw);
                            mesNumero = extraerMesDesdeFechaCadena(fechaRaw);
                        } else {
                            continue;
                        }

                        Double monto = doc.getDouble("monto");
                        String nombreTour = doc.getString("nombreTour");
                        String uidPaga = doc.getString("uidUsuarioPaga");
                        String uidRecibe = doc.getString("uidUsuarioRecibe");

                        if (uidPaga == null) continue;

                        // Crear item con valores básicos
                        PagoItem item = new PagoItem();
                        item.idDocumento = doc.getId();
                        item.fechaFormateada = fechaFormateada;
                        item.mesNumero = mesNumero;
                        item.monto = (monto != null) ? monto : 0.0;
                        item.nombreTour = (nombreTour != null) ? nombreTour : "";
                        item.uidCliente = uidPaga;
                        item.uidAdmin = uidRecibe;
                        item.tipoPago = tipoPago;
                        item.nombreCliente = "Cliente"; // provisional

                        listaCompletaPagos.add(item);

                        // --- Cargar nombre real del cliente ---
                        // MISMA lógica que en SplashActivity: documento con ID = uid
                        db.collection(AuthConstants.COLLECTION_USUARIOS)
                                .document(uidPaga)
                                .get()
                                .addOnSuccessListener(clienteDoc -> {
                                    if (clienteDoc.exists()) {
                                        String nombresApellidos =
                                                clienteDoc.getString("nombresApellidos");
                                        Log.d(TAG, "Nombre encontrado para " + uidPaga + ": " + nombresApellidos);
                                        if (nombresApellidos != null &&
                                                !nombresApellidos.trim().isEmpty()) {
                                            item.nombreCliente = nombresApellidos;
                                        } else {
                                            item.nombreCliente = "Cliente";
                                        }
                                    } else {
                                        Log.w(TAG, "No existe documento de usuario para uid " + uidPaga);
                                        item.nombreCliente = "Cliente";
                                    }
                                    aplicarFiltros(); // refrescar lista
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error obteniendo usuario " + uidPaga, e);
                                    item.nombreCliente = "Cliente";
                                    aplicarFiltros();
                                });
                    }

                    // Primera carga (antes de que terminen de llegar todos los nombres)
                    aplicarFiltros();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo pagos", e);
                    listaCompletaPagos.clear();
                    listaFiltradaPagos.clear();
                    adapter.notifyDataSetChanged();
                });
    }

    // =========================================================
    // FILTRADO SOLO POR MES
    // =========================================================

    private void aplicarFiltros() {
        listaFiltradaPagos.clear();

        for (PagoItem item : listaCompletaPagos) {
            // Filtro de mes
            if (mesSeleccionado != 0 && item.mesNumero != mesSeleccionado) {
                continue;
            }
            listaFiltradaPagos.add(item);
        }

        adapter.notifyDataSetChanged();
    }

    // =========================================================
    // HELPERS DE FECHA / HORA PARA CADENA ANTIGUA
    // =========================================================

    @NonNull
    private String formatearFechaHoraCadena(@NonNull String fechaRaw) {
        try {
            String[] partes = fechaRaw.split(",");
            if (partes.length < 2) {
                return fechaRaw;
            }

            String parteFecha = partes[0].trim();      // "12 de noviembre de 2025"
            String parteHoraZona = partes[1].trim();   // "1:26:38 p.m. UTC-5"

            // ---- Fecha ----
            String[] tokensFecha = parteFecha.split(" ");
            if (tokensFecha.length < 5) return fechaRaw;

            String dia = pad2(tokensFecha[0]);
            String mesNombre = tokensFecha[2].toLowerCase(Locale.ROOT);
            String anio = tokensFecha[4];

            int mesNumero = mesDesdeNombre(mesNombre);
            String mes = pad2(String.valueOf(mesNumero));

            String fechaCorta = dia + "/" + mes + "/" + anio;

            // ---- Hora ----
            String horaCorta = extraerHoraCorta(parteHoraZona);

            return fechaCorta + " " + horaCorta;
        } catch (Exception e) {
            return fechaRaw;
        }
    }

    @NonNull
    private String extraerHoraCorta(@NonNull String parteHoraZona) {
        int idxUtc = parteHoraZona.indexOf("UTC");
        String core = (idxUtc > 0) ? parteHoraZona.substring(0, idxUtc).trim() : parteHoraZona.trim();

        String[] pieces = core.split(" ");
        if (pieces.length < 2) return core;

        String tiempo = pieces[0]; // "1:26:38"
        String ampm = pieces[1];   // "p.m." / "a.m."

        String[] tParts = tiempo.split(":");
        if (tParts.length < 2) return core;

        return tParts[0] + ":" + tParts[1] + " " + ampm;
    }

    private int extraerMesDesdeFechaCadena(@NonNull String fechaRaw) {
        try {
            String[] partes = fechaRaw.split(",");
            if (partes.length == 0) return 0;

            String parteFecha = partes[0].trim();
            String[] tokensFecha = parteFecha.split(" ");
            if (tokensFecha.length < 3) return 0;

            String mesNombre = tokensFecha[2].toLowerCase(Locale.ROOT);
            return mesDesdeNombre(mesNombre);
        } catch (Exception e) {
            return 0;
        }
    }

    private int mesDesdeNombre(String mesNombre) {
        switch (mesNombre) {
            case "enero": return 1;
            case "febrero": return 2;
            case "marzo": return 3;
            case "abril": return 4;
            case "mayo": return 5;
            case "junio": return 6;
            case "julio": return 7;
            case "agosto": return 8;
            case "septiembre":
            case "setiembre": return 9;
            case "octubre": return 10;
            case "noviembre": return 11;
            case "diciembre": return 12;
            default: return 0;
        }
    }

    @NonNull
    private String pad2(@NonNull String s) {
        if (s.length() >= 2) return s;
        return "0" + s;
    }

    // =========================================================
    // MODELO
    // =========================================================

    private static class PagoItem {
        String idDocumento;
        String fechaFormateada;
        int mesNumero;

        double monto;
        String nombreTour;

        String uidCliente;
        String uidAdmin;
        String nombreCliente;
        String tipoPago;
    }

    // =========================================================
    // ADAPTER
    // =========================================================

    private static class PagosAdapter extends RecyclerView.Adapter<PagosAdapter.PagoViewHolder> {

        private final List<PagoItem> items;

        PagosAdapter(List<PagoItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public PagoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pago_cliente, parent, false);
            return new PagoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PagoViewHolder holder, int position) {
            PagoItem item = items.get(position);

            String nombreCliente = (item.nombreCliente != null && !item.nombreCliente.isEmpty())
                    ? item.nombreCliente
                    : "Cliente";

            holder.textClientName.setText(nombreCliente);

            String detalle = "S/ " + String.format(Locale.US, "%.2f", item.monto)
                    + " · " + item.nombreTour;
            holder.textStatus.setText(detalle);

            holder.textTime.setText(item.fechaFormateada != null ? item.fechaFormateada : "");
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class PagoViewHolder extends RecyclerView.ViewHolder {
            TextView textClientName;  // tvClientName
            TextView textStatus;      // tvPaymentStatus
            TextView textTime;        // tvPaymentTime

            PagoViewHolder(@NonNull View itemView) {
                super(itemView);
                textClientName = itemView.findViewById(R.id.tvClientName);
                textStatus = itemView.findViewById(R.id.tvPaymentStatus);
                textTime = itemView.findViewById(R.id.tvPaymentTime);
            }
        }
    }
}

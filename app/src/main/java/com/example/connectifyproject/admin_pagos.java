package com.example.connectifyproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.example.connectifyproject.utils.AuthConstants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
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

    private static final String TAG = "AdminPagos";

    // Secciones
    private static final int SECTION_RECIBIDOS = 0;
    private static final int SECTION_REALIZADOS = 1;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // UI
    private AutoCompleteTextView spinnerMeses;
    private RecyclerView recyclerViewPagos;
    private MaterialButtonToggleGroup togglePayments;
    private MaterialButton btnPagosRecibidos;
    private MaterialButton btnPagosRealizados;

    // Datos
    private final List<PagoItem> listaRecibidos = new ArrayList<>();
    private final List<PagoItem> listaRealizados = new ArrayList<>();
    private final List<PagoItem> listaMostrada = new ArrayList<>();

    private int currentSection = SECTION_RECIBIDOS;
    private int mesSeleccionado = 0; // 0 = todos, 1..12 = ene..dic

    // Formato de fecha/hora
    private static final Locale LOCALE_PE = new Locale("es", "PE");
    private static final TimeZone TIMEZONE_LIMA = TimeZone.getTimeZone("America/Lima");
    private static final SimpleDateFormat FORMATO_FECHA_HORA =
            new SimpleDateFormat("dd/MM/yyyy h:mm a", LOCALE_PE);

    static {
        FORMATO_FECHA_HORA.setTimeZone(TIMEZONE_LIMA);
    }

    private PagosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_pagos_view);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        if (topAppBar != null) {
            topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        }

        spinnerMeses = findViewById(R.id.spinnerMonthFilter);
        recyclerViewPagos = findViewById(R.id.recyclerViewPagos);
        togglePayments = findViewById(R.id.togglePayments);
        btnPagosRecibidos = findViewById(R.id.btnPagosRecibidos);
        btnPagosRealizados = findViewById(R.id.btnPagosRealizados);

        // RecyclerView
        recyclerViewPagos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PagosAdapter(listaMostrada);
        recyclerViewPagos.setAdapter(adapter);

        // Bottom nav admin
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("pagos");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavContainer, bottomNavFragment)
                .commit();

        configurarDropdownMeses();
        configurarToggleSecciones();
        cargarPagos();
    }

    // =========================================================
    // DROPDOWN DE MESES
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
        spinnerMeses.setText(meses.get(0), false);

        spinnerMeses.setOnItemClickListener((parent, view, position, id) -> {
            mesSeleccionado = position; // 0 = todos
            rebuildListaMostrada();
        });
    }

    // =========================================================
    // TOGGLE DE SECCIONES
    // =========================================================

    private void configurarToggleSecciones() {
        if (togglePayments == null) return;

        togglePayments.setSingleSelection(true);
        // Por defecto: Pagos recibidos
        btnPagosRecibidos.setChecked(true);
        currentSection = SECTION_RECIBIDOS;

        togglePayments.addOnButtonCheckedListener(
                (group, checkedId, isChecked) -> {
                    if (!isChecked) return;

                    if (checkedId == R.id.btnPagosRecibidos) {
                        currentSection = SECTION_RECIBIDOS;
                    } else if (checkedId == R.id.btnPagosRealizados) {
                        currentSection = SECTION_REALIZADOS;
                    }
                    rebuildListaMostrada();
                });
    }

    // =========================================================
    // CARGA DE PAGOS DESDE FIREBASE
    // =========================================================

    private void cargarPagos() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listaRecibidos.clear();
            listaRealizados.clear();
            listaMostrada.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        String adminUid = currentUser.getUid();

        // 1) Pagos RECIBIDOS: Cliente -> Administrador (A Empresa)
        db.collection("pagos")
                .whereEqualTo("uidUsuarioRecibe", adminUid)
                .whereEqualTo("tipoPago", "A Empresa")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaRecibidos.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        PagoItem item = crearPagoDesdeDocumento(doc, true);
                        if (item != null) {
                            listaRecibidos.add(item);
                            cargarNombreUsuario(item); // cliente
                        }
                    }
                    rebuildListaMostrada();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error cargando pagos recibidos", e));

        // 2) Pagos REALIZADOS: Administrador -> Guía (A Guia)
        db.collection("pagos")
                .whereEqualTo("uidUsuarioPaga", adminUid)
                .whereEqualTo("tipoPago", "A Guia")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaRealizados.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        PagoItem item = crearPagoDesdeDocumento(doc, false);
                        if (item != null) {
                            listaRealizados.add(item);
                            cargarNombreUsuario(item); // guía
                        }
                    }
                    rebuildListaMostrada();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error cargando pagos realizados", e));
    }

    /** true = recibido (cliente->admin), false = realizado (admin->guia) */
    private PagoItem crearPagoDesdeDocumento(DocumentSnapshot doc, boolean esRecibido) {
        Object fechaObj = doc.get("fecha");
        if (fechaObj == null) return null;

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
            String fechaRaw = (String) fechaObj;
            fechaFormateada = formatearFechaHoraCadena(fechaRaw);
            mesNumero = extraerMesDesdeFechaCadena(fechaRaw);
        } else {
            return null;
        }

        Double monto = doc.getDouble("monto");
        String nombreTour = doc.getString("nombreTour");
        String uidPaga = doc.getString("uidUsuarioPaga");
        String uidRecibe = doc.getString("uidUsuarioRecibe");

        if (uidPaga == null || uidRecibe == null) return null;

        PagoItem item = new PagoItem();
        item.idDocumento = doc.getId();
        item.fechaFormateada = fechaFormateada;
        item.mesNumero = mesNumero;
        item.monto = (monto != null) ? monto : 0.0;
        item.nombreTour = (nombreTour != null) ? nombreTour : "";

        if (esRecibido) {
            item.tipo = PagoItem.TIPO_RECIBIDO;
            item.uidOtroUsuario = uidPaga;          // cliente
            item.nombreOtroUsuario = "Cliente";
        } else {
            item.tipo = PagoItem.TIPO_REALIZADO;
            item.uidOtroUsuario = uidRecibe;        // guía
            item.nombreOtroUsuario = "Guía";
        }

        return item;
    }

    /**
     * Cargar nombre (nombresApellidos) desde la colección de usuarios.
     * Usa AuthConstants.COLLECTION_USUARIOS para asegurar que es la misma
     * colección que el resto de la app.
     */
    private void cargarNombreUsuario(PagoItem item) {
        if (item.uidOtroUsuario == null) return;

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(item.uidOtroUsuario)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String nombre = doc.getString("nombresApellidos");
                        Log.d(TAG, "Usuario " + item.uidOtroUsuario +
                                " encontrado. nombresApellidos=" + nombre +
                                " tipo=" + (item.tipo == PagoItem.TIPO_RECIBIDO ? "RECIBIDO" : "REALIZADO"));

                        if (nombre != null && !nombre.trim().isEmpty()) {
                            item.nombreOtroUsuario = nombre;
                        }
                    } else {
                        Log.w(TAG, "Documento de usuario NO existe para uid=" + item.uidOtroUsuario);
                    }
                    rebuildListaMostrada();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando usuario " + item.uidOtroUsuario, e);
                });
    }

    // =========================================================
    // ARMAR LISTA MOSTRADA APLICANDO FILTRO DE MES + SECCION
    // =========================================================

    private void rebuildListaMostrada() {
        listaMostrada.clear();

        List<PagoItem> fuente = (currentSection == SECTION_RECIBIDOS)
                ? listaRecibidos
                : listaRealizados;

        for (PagoItem p : fuente) {
            if (mesSeleccionado != 0 && p.mesNumero != mesSeleccionado) continue;
            listaMostrada.add(p);
        }

        adapter.notifyDataSetChanged();
    }

    // =========================================================
    // HELPERS DE FECHA PARA REGISTROS ANTIGUOS EN STRING
    // =========================================================

    @NonNull
    private String formatearFechaHoraCadena(@NonNull String fechaRaw) {
        try {
            String[] partes = fechaRaw.split(",");
            if (partes.length < 2) {
                return fechaRaw;
            }

            String parteFecha = partes[0].trim();
            String parteHoraZona = partes[1].trim();

            String[] tokensFecha = parteFecha.split(" ");
            if (tokensFecha.length < 5) return fechaRaw;

            String dia = pad2(tokensFecha[0]);
            String mesNombre = tokensFecha[2].toLowerCase(Locale.ROOT);
            String anio = tokensFecha[4];

            int mesNumero = mesDesdeNombre(mesNombre);
            String mes = pad2(String.valueOf(mesNumero));

            String fechaCorta = dia + "/" + mes + "/" + anio;
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
    // MODELO DE PAGO
    // =========================================================

    private static class PagoItem {
        static final int TIPO_RECIBIDO = 1;
        static final int TIPO_REALIZADO = 2;

        String idDocumento;
        String fechaFormateada;
        int mesNumero;

        double monto;
        String nombreTour;

        int tipo;                 // RECIBIDO / REALIZADO
        String uidOtroUsuario;    // cliente o guía
        String nombreOtroUsuario; // se muestra en el card
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

            String defaultName = (item.tipo == PagoItem.TIPO_RECIBIDO) ? "Cliente" : "Guía";
            String nombre = (item.nombreOtroUsuario != null && !item.nombreOtroUsuario.isEmpty())
                    ? item.nombreOtroUsuario
                    : defaultName;

            holder.textClientName.setText(nombre);

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

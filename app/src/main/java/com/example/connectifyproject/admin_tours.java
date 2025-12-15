package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.databinding.AdminToursViewBinding;
import com.example.connectifyproject.models.TourBorrador;
import com.example.connectifyproject.models.OfertaTour;
import com.example.connectifyproject.services.AdminTourService;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class admin_tours extends AppCompatActivity {
    private AdminToursViewBinding binding;
    private ToursAdapter toursAdapter;
    private List<TourItem> toursList;
    private List<TourItem> toursListFiltered; // Lista filtrada para búsqueda
    private String currentTab = "borradores";
    
    // Firebase
    private AdminTourService adminTourService;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String empresaId;
    private SimpleDateFormat dateFormat;
    
    // Request codes
    private static final int REQUEST_SELECT_PAYMENT = 1001;
    
    // Temporary data for guide selection flow
    private String tempOfertaId;
    private String tempTourTitulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminToursViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase
        adminTourService = new AdminTourService();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        // Obtener empresaId
        loadEmpresaId();

        // Configurar toolbar
        setSupportActionBar(binding.topAppBar);

        // Configurar botón de notificaciones
        binding.btnNotifications.setOnClickListener(v -> {
            // TODO: Implementar navegación a notificaciones
        });

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar TabLayout
        setupTabs();

        // Configurar buscador
        setupSearchBar();

        // NO cargar datos iniciales aquí, esperar a que se cargue empresaId
        // La carga se hará automáticamente en loadEmpresaId()

        // Configurar bottom navigation
        setupBottomNavigation();

        // Configurar botón crear tour
        binding.fabCreateTour.setOnClickListener(v -> {
            Intent intent = new Intent(this, admin_create_tour.class);
            startActivity(intent);
        });
    }
    
    private void loadEmpresaId() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String rol = documentSnapshot.getString("rol");
                        
                        // Si es Administrador, empresaId = UID
                        if ("Administrador".equals(rol)) {
                            empresaId = userId;
                        } else {
                            // Si es otro rol, buscar campo empresaId
                            empresaId = documentSnapshot.getString("empresaId");
                        }
                        
                        // Recargar tours después de obtener empresaId
                        if (empresaId != null) {
                            // ✅ Ejecutar cancelación automática cuando admin abre la app
                            ejecutarCancelacionAutomatica();
                            loadTours(currentTab);
                        } else {
                            Toast.makeText(this, "No se pudo obtener ID de empresa", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos de usuario", Toast.LENGTH_SHORT).show();
                });
        }
    }
    
    /**
     * ✅ Ejecutar cancelación automática de tours sin participantes
     * Se ejecuta cuando el admin abre la app
     */
    private void ejecutarCancelacionAutomatica() {
        com.example.connectifyproject.services.TourFirebaseService tourService = 
            new com.example.connectifyproject.services.TourFirebaseService();
        
        // Buscar tours sin participantes que deban cancelarse
        db.collection("tours_asignados")
            .whereEqualTo("empresaId", empresaId)
            .whereIn("estado", java.util.Arrays.asList("confirmado", "pendiente", "programado"))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    List<Map<String, Object>> participantes = 
                        (List<Map<String, Object>>) doc.get("participantes");
                    
                    if (participantes == null || participantes.isEmpty()) {
                        // Verificar regla de 2 horas
                        com.google.firebase.Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                        String horaInicio = doc.getString("horaInicio");
                        
                        double horasRestantes = com.example.connectifyproject.utils.TourTimeValidator
                            .calcularHorasHastaInicio(fechaRealizacion, horaInicio);
                        
                        if (horasRestantes <= 2.0 && horasRestantes >= 0) {
                            tourService.verificarYCancelarTourSinParticipantes(
                                doc.getId(),
                                new com.example.connectifyproject.services.TourFirebaseService.OperationCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        android.util.Log.d("AdminTours", "✅ Tour auto-cancelado: " + doc.getId());
                                    }
                                    
                                    @Override
                                    public void onError(String error) {
                                        android.util.Log.e("AdminTours", "❌ Error cancelando tour: " + error);
                                    }
                                }
                            );
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminTours", "Error verificando tours para cancelación", e);
            });
    }

    private void setupRecyclerView() {
        toursList = new ArrayList<>();
        toursListFiltered = new ArrayList<>();
        toursAdapter = new ToursAdapter(toursListFiltered);
        binding.recyclerViewTours.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTours.setAdapter(toursAdapter);
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                String tabState;
                
                switch (position) {
                    case 0:
                        tabState = "borradores";
                        break;
                    case 1:
                        tabState = "publicados";
                        break;
                    case 2:
                        tabState = "pendiente";
                        break;
                    case 3:
                        tabState = "confirmados";
                        break;
                    case 4:
                        tabState = "cancelados";
                        break;
                    default:
                        tabState = "borradores";
                }
                
                currentTab = tabState;
                // Limpiar búsqueda al cambiar de tab
                binding.etSearch.setText("");
                loadTours(tabState);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    /**
     * Configurar barra de búsqueda con filtrado en tiempo real
     */
    private void setupSearchBar() {
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTours(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    
    /**
     * Filtrar tours por título (case-insensitive)
     */
    private void filterTours(String query) {
        toursListFiltered.clear();
        
        if (query == null || query.trim().isEmpty()) {
            // Si no hay búsqueda, mostrar todos los tours
            toursListFiltered.addAll(toursList);
        } else {
            // Filtrar por título (case-insensitive)
            String queryLower = query.toLowerCase().trim();
            for (TourItem tour : toursList) {
                if (tour.getTitulo().toLowerCase().contains(queryLower)) {
                    toursListFiltered.add(tour);
                }
            }
        }
        
        toursAdapter.notifyDataSetChanged();
    }

    private void loadTours(String estado) {
        if (empresaId == null) {
            return; // Esperar a que se cargue empresaId
        }
        
        toursList.clear();
        toursAdapter.notifyDataSetChanged();
        
        switch (estado) {
            case "borradores":
                loadBorradores();
                break;
                
            case "publicados":
                loadPublicados();
                break;
                
            case "pendiente":
                loadPendienteConfirmacion();
                break;
                
            case "confirmados":
                loadConfirmados();
                break;
                
            case "cancelados":
                loadCancelados();
                break;
        }
    }
    
    /**
     * Recargar tours de la pestaña actual
     */
    private void loadToursForCurrentTab() {
        loadTours(currentTab);
    }
    
    /**
     * MÉTODO DE MIGRACIÓN - SOLO NECESARIO UNA VEZ
     * 
     * Este método migra borradores antiguos que fueron creados antes de agregar
     * los campos 'estado' y 'habilitado' al modelo TourBorrador.
     * 
     * PUEDE SER ELIMINADO después de que todos los usuarios hayan actualizado
     * sus borradores existentes, ya que todos los nuevos borradores ya incluyen
     * estos campos automáticamente desde el constructor de TourBorrador.
     * 
     * Para usarlo temporalmente, descomenta la línea en loadBorradores():
     * migrarBorradoresAntiguos();
     */
    /*
    private void migrarBorradoresAntiguos() {
        // Buscar borradores sin los campos estado/habilitado y actualizarlos
        db.collection("tours_borradores")
            .whereEqualTo("empresaId", empresaId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int migrados = 0;
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    // Verificar si le faltan los campos
                    String estado = doc.getString("estado");
                    Boolean habilitado = doc.getBoolean("habilitado");
                    
                    if (estado == null || habilitado == null) {
                        // Actualizar el documento con los campos faltantes
                        db.collection("tours_borradores")
                            .document(doc.getId())
                            .update(
                                "estado", "borrador",
                                "habilitado", true
                            )
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("AdminTours", "Borrador migrado: " + doc.getId());
                            });
                        migrados++;
                    }
                }
                if (migrados > 0) {
                    android.util.Log.d("AdminTours", "Se migraron " + migrados + " borradores antiguos");
                }
            });
    }
    */
    
    private void loadBorradores() {
        android.util.Log.d("AdminTours", "=== CARGANDO BORRADORES ===");
        android.util.Log.d("AdminTours", "EmpresaId: " + empresaId);
        
        // NOTA: Si tienes borradores antiguos sin los campos estado/habilitado,
        // descomenta temporalmente la siguiente línea para migrarlos:
        // migrarBorradoresAntiguos();
        
        // Consultar directamente la colección tours_borradores (igual que tours_ofertas para publicados)
        db.collection("tours_borradores")
            .whereEqualTo("empresaId", empresaId)
            .whereEqualTo("estado", "borrador")
            .whereEqualTo("habilitado", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                android.util.Log.d("AdminTours", "Tours encontrados en tours_borradores: " + querySnapshot.size());
                
                toursList.clear();
                int toursAgregados = 0;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        android.util.Log.d("AdminTours", "Procesando borrador: " + doc.getId());
                        
                        String titulo = doc.getString("titulo");
                        android.util.Log.d("AdminTours", "  - titulo: " + titulo);
                        
                        // Manejar fecha como String o Timestamp (igual que en loadPublicados)
                        String fecha = "Sin fecha";
                        try {
                            // Intentar primero como Timestamp
                            Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                            if (fechaRealizacion != null) {
                                fecha = dateFormat.format(fechaRealizacion.toDate());
                            }
                        } catch (Exception e) {
                            // Si falla, intentar como String
                            String fechaString = doc.getString("fechaRealizacion");
                            if (fechaString != null && !fechaString.isEmpty()) {
                                fecha = fechaString;
                            }
                            android.util.Log.d("AdminTours", "  - fechaRealizacion es String: " + fechaString);
                        }
                        android.util.Log.d("AdminTours", "  - fecha procesada: " + fecha);
                        
                        List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                        String imageUrl = (imagenesUrls != null && !imagenesUrls.isEmpty()) 
                            ? imagenesUrls.get(0) 
                            : null;
                        android.util.Log.d("AdminTours", "  - imageUrl: " + (imageUrl != null ? "presente" : "null"));
                        
                        toursList.add(new TourItem(
                            doc.getId(),
                            titulo != null ? titulo : "Sin título",
                            fecha,
                            "Borrador",
                            imageUrl,
                            false,
                            "borrador"
                        ));
                        toursAgregados++;
                        android.util.Log.d("AdminTours", "  ✓ Borrador agregado a la lista");
                    } catch (Exception e) {
                        android.util.Log.e("AdminTours", "Error procesando borrador " + doc.getId(), e);
                    }
                }
                
                android.util.Log.d("AdminTours", "Total borradores agregados: " + toursAgregados);
                
                // Aplicar filtro actual si existe
                String currentQuery = binding.etSearch.getText().toString();
                filterTours(currentQuery);
                
                android.util.Log.d("AdminTours", "Adapter notificado");
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminTours", "Error al cargar borradores", e);
                Toast.makeText(this, "Error al cargar borradores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadPublicados() {
        android.util.Log.d("AdminTours", "=== CARGANDO PUBLICADOS ===");
        android.util.Log.d("AdminTours", "EmpresaId: " + empresaId);
        
        // Cargar tours publicados SIN guía seleccionado
        db.collection("tours_ofertas")
            .whereEqualTo("empresaId", empresaId)
            .whereEqualTo("estado", "publicado")
            .whereEqualTo("habilitado", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                android.util.Log.d("AdminTours", "Tours encontrados en tours_ofertas: " + querySnapshot.size());
                
                toursList.clear();
                int toursAgregados = 0;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        android.util.Log.d("AdminTours", "Procesando tour: " + doc.getId());
                        
                        String guiaSeleccionado = doc.getString("guiaSeleccionadoActual");
                        android.util.Log.d("AdminTours", "  - guiaSeleccionadoActual: " + guiaSeleccionado);
                        
                        // Solo mostrar tours SIN guía seleccionado
                        if (guiaSeleccionado == null || guiaSeleccionado.isEmpty()) {
                            String titulo = doc.getString("titulo");
                            String horaInicio = doc.getString("horaInicio");
                            android.util.Log.d("AdminTours", "  - titulo: " + titulo);
                            android.util.Log.d("AdminTours", "  - horaInicio: " + horaInicio);
                            
                            // Manejar fecha como String o Timestamp
                            String fecha = "Sin fecha";
                            Object fechaRealizacion = null;
                            try {
                                // Intentar primero como Timestamp
                                fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                                if (fechaRealizacion != null) {
                                    fecha = dateFormat.format(((Timestamp)fechaRealizacion).toDate());
                                }
                            } catch (Exception e) {
                                // Si falla, intentar como String
                                String fechaString = doc.getString("fechaRealizacion");
                                if (fechaString != null && !fechaString.isEmpty()) {
                                    fecha = fechaString;
                                    fechaRealizacion = fechaString;
                                }
                                android.util.Log.d("AdminTours", "  - fechaRealizacion es String: " + fechaString);
                            }
                            android.util.Log.d("AdminTours", "  - fecha procesada: " + fecha);
                            
                            // Calcular estado de bloqueo basado en tiempo
                            String estadoBloqueo = com.example.connectifyproject.utils.TourTimeValidator
                                .getEstadoTourSinAsignar(fechaRealizacion, horaInicio);
                            
                            // Solo agregar si no está oculto
                            if (!"oculto".equals(estadoBloqueo)) {
                                List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                                String imageUrl = (imagenesUrls != null && !imagenesUrls.isEmpty()) 
                                    ? imagenesUrls.get(0) 
                                    : null;
                                android.util.Log.d("AdminTours", "  - imageUrl: " + (imageUrl != null ? "presente" : "null"));
                                android.util.Log.d("AdminTours", "  - estadoBloqueo: " + estadoBloqueo);
                                
                                TourItem tourItem = new TourItem(
                                    doc.getId(),
                                    titulo != null ? titulo : "Sin título",
                                    fecha,
                                    "Sin asignar",
                                    imageUrl,
                                    true,
                                    "publicado"
                                );
                                tourItem.setFechaRealizacion(fechaRealizacion);
                                tourItem.setHoraInicio(horaInicio);
                                tourItem.setEstadoBloqueo(estadoBloqueo);
                                
                                toursList.add(tourItem);
                                toursAgregados++;
                                android.util.Log.d("AdminTours", "  ✓ Tour agregado a la lista (estado: " + estadoBloqueo + ")");
                            } else {
                                android.util.Log.d("AdminTours", "  - Tour omitido (ya pasó su hora de inicio)");
                            }
                        } else {
                            android.util.Log.d("AdminTours", "  - Tour omitido (tiene guía asignado)");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AdminTours", "Error procesando tour " + doc.getId(), e);
                    }
                }
                
                android.util.Log.d("AdminTours", "Total tours agregados: " + toursAgregados);
                
                // Aplicar filtro actual si existe
                String currentQuery = binding.etSearch.getText().toString();
                filterTours(currentQuery);
                
                android.util.Log.d("AdminTours", "Adapter notificado");
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminTours", "Error al cargar tours publicados", e);
                Toast.makeText(this, "Error al cargar tours publicados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadPendienteConfirmacion() {
        db.collection("tours_ofertas")
            .whereEqualTo("empresaId", empresaId)
            .whereEqualTo("estado", "publicado")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                toursList.clear();
                querySnapshot.getDocuments().forEach(doc -> {
                    String guiaSeleccionado = doc.getString("guiaSeleccionadoActual");
                    
                    // Solo mostrar tours con guía seleccionado (pendiente de confirmación)
                    if (guiaSeleccionado != null && !guiaSeleccionado.isEmpty()) {
                        String titulo = doc.getString("titulo");
                        String horaInicio = doc.getString("horaInicio");
                        
                        // Manejar fecha como String o Timestamp
                        String fecha = "Sin fecha";
                        Object fechaRealizacion = null;
                        try {
                            fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                            if (fechaRealizacion != null) {
                                fecha = dateFormat.format(((Timestamp)fechaRealizacion).toDate());
                            }
                        } catch (Exception e) {
                            String fechaString = doc.getString("fechaRealizacion");
                            if (fechaString != null && !fechaString.isEmpty()) {
                                fecha = fechaString;
                                fechaRealizacion = fechaString;
                            }
                        }
                        
                        // Calcular estado de bloqueo basado en tiempo (12 horas para pendientes)
                        String estadoBloqueo = com.example.connectifyproject.utils.TourTimeValidator
                            .getEstadoTourPendiente(fechaRealizacion, horaInicio);
                        
                        // Solo agregar si no está oculto
                        if (!"oculto".equals(estadoBloqueo)) {
                            List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                            String imageUrl = (imagenesUrls != null && !imagenesUrls.isEmpty()) 
                                ? imagenesUrls.get(0) 
                                : null;
                            
                            TourItem tourItem = new TourItem(
                                doc.getId(),
                                titulo,
                                fecha,
                                "Pendiente confirmación",
                                imageUrl,
                                false,
                                "pendiente"
                            );
                            
                            tourItem.setGuiaSeleccionadoId(guiaSeleccionado);
                            tourItem.setFechaRealizacion(fechaRealizacion);
                            tourItem.setHoraInicio(horaInicio);
                            tourItem.setEstadoBloqueo(estadoBloqueo);
                            
                            // Verificar si hay rechazo no visto
                            checkForRejection(doc.getId(), guiaSeleccionado, tourItem);
                            
                            toursList.add(tourItem);
                        }
                    }
                });
                
                // Aplicar filtro actual si existe
                String currentQuery = binding.etSearch.getText().toString();
                filterTours(currentQuery);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar tours pendientes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void checkForRejection(String ofertaId, String guiaId, TourItem tourItem) {
        db.collection("tours_ofertas")
            .document(ofertaId)
            .collection("guias_ofertados")
            .document(guiaId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String estadoOferta = doc.getString("estadoOferta");
                    Boolean vistoAdmin = doc.getBoolean("vistoAdmin");
                    String motivoRechazo = doc.getString("motivoRechazo");
                    
                    if ("rechazado".equals(estadoOferta) && (vistoAdmin == null || !vistoAdmin)) {
                        tourItem.setTieneRechazo(true);
                        tourItem.setMotivoRechazo(motivoRechazo);
                        // Ya no necesitamos notificar aquí, el filtro se aplicará después
                    }
                }
            });
    }
    
    private void loadConfirmados() {
        db.collection("tours_asignados")
            .whereEqualTo("empresaId", empresaId)
            .whereEqualTo("habilitado", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                toursList.clear();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String estadoDoc = doc.getString("estado");
                    
                    // Solo mostrar tours confirmados, en curso o completados
                    if (estadoDoc != null && 
                        (estadoDoc.equals("confirmado") || 
                         estadoDoc.equals("en_curso") || 
                         estadoDoc.equals("completado"))) {
                        
                        String titulo = doc.getString("titulo");
                        
                        // Manejar fecha como String o Timestamp
                        String fecha = "Sin fecha";
                        try {
                            Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                            if (fechaRealizacion != null) {
                                fecha = dateFormat.format(fechaRealizacion.toDate());
                            }
                        } catch (Exception e) {
                            String fechaString = doc.getString("fechaRealizacion");
                            if (fechaString != null && !fechaString.isEmpty()) {
                                fecha = fechaString;
                            }
                        }
                        
                        List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                        String imageUrl = (imagenesUrls != null && !imagenesUrls.isEmpty()) 
                            ? imagenesUrls.get(0) 
                            : null;
                        
                        String estadoDisplay = capitalizeFirst(estadoDoc.replace("_", " "));
                        
                        toursList.add(new TourItem(
                            doc.getId(),
                            titulo,
                            fecha,
                            estadoDisplay,
                            imageUrl,
                            true,
                            "confirmado"
                        ));
                    }
                }
                
                // Aplicar filtro actual si existe
                String currentQuery = binding.etSearch.getText().toString();
                filterTours(currentQuery);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar tours confirmados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadCancelados() {
        // Buscar en tours_ofertas cancelados
        db.collection("tours_ofertas")
            .whereEqualTo("empresaId", empresaId)
            .whereEqualTo("estado", "cancelado")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                toursList.clear();
                querySnapshot.getDocuments().forEach(doc -> {
                    String titulo = doc.getString("titulo");
                    
                    // Manejar fecha como String o Timestamp
                    String fecha = "Sin fecha";
                    try {
                        Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                        if (fechaRealizacion != null) {
                            fecha = dateFormat.format(fechaRealizacion.toDate());
                        }
                    } catch (Exception e) {
                        String fechaString = doc.getString("fechaRealizacion");
                        if (fechaString != null && !fechaString.isEmpty()) {
                            fecha = fechaString;
                        }
                    }
                    
                    List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                    String imageUrl = (imagenesUrls != null && !imagenesUrls.isEmpty()) 
                        ? imagenesUrls.get(0) 
                        : null;
                    
                    toursList.add(new TourItem(
                        doc.getId(),
                        titulo,
                        fecha,
                        "Cancelado",
                        imageUrl,
                        false,
                        "cancelado"
                    ));
                });
                
                // También buscar en tours_asignados cancelados
                db.collection("tours_asignados")
                    .whereEqualTo("empresaId", empresaId)
                    .whereEqualTo("estado", "cancelado")
                    .get()
                    .addOnSuccessListener(querySnapshot2 -> {
                        querySnapshot2.getDocuments().forEach(doc -> {
                            String titulo = doc.getString("titulo");
                            
                            // Manejar fecha como String o Timestamp
                            String fecha = "Sin fecha";
                            try {
                                Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                                if (fechaRealizacion != null) {
                                    fecha = dateFormat.format(fechaRealizacion.toDate());
                                }
                            } catch (Exception e) {
                                String fechaString = doc.getString("fechaRealizacion");
                                if (fechaString != null && !fechaString.isEmpty()) {
                                    fecha = fechaString;
                                }
                            }
                            
                            List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                            String imageUrl = (imagenesUrls != null && !imagenesUrls.isEmpty()) 
                                ? imagenesUrls.get(0) 
                                : null;
                            
                            toursList.add(new TourItem(
                                doc.getId(),
                                titulo,
                                fecha,
                                "Cancelado",
                                imageUrl,
                                false,
                                "cancelado"
                            ));
                        });
                        
                        // Aplicar filtro actual si existe
                        String currentQuery = binding.etSearch.getText().toString();
                        filterTours(currentQuery);
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar tours cancelados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("tours");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }

    // Clase interna para los datos del tour
    public static class TourItem {
        private String id;
        private String titulo;
        private String fecha;
        private String estado;
        private String imagenUrl;
        private boolean esPublicado;
        private String tipo; // borrador, pendiente, sin_guia, confirmado, cancelado
        private boolean tieneRechazo; // Indica si hay un rechazo no visto
        private String guiaSeleccionadoId; // ID del guía actualmente seleccionado
        private String motivoRechazo; // Motivo del rechazo si existe
        
        // Nuevos campos para gestión de tiempo
        private Object fechaRealizacion; // Timestamp o String de la fecha
        private String horaInicio; // Hora de inicio del tour (HH:mm)
        private String estadoBloqueo; // "visible", "bloqueado", "oculto"

        public TourItem(String id, String titulo, String fecha, String estado, String imagenUrl, 
                       boolean esPublicado, String tipo) {
            this.id = id;
            this.titulo = titulo;
            this.fecha = fecha;
            this.estado = estado;
            this.imagenUrl = imagenUrl;
            this.esPublicado = esPublicado;
            this.tipo = tipo;
            this.tieneRechazo = false;
            this.guiaSeleccionadoId = null;
            this.motivoRechazo = null;
            this.fechaRealizacion = null;
            this.horaInicio = null;
            this.estadoBloqueo = "visible";
        }

        // Getters
        public String getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getFecha() { return fecha; }
        public String getEstado() { return estado; }
        public String getImagenUrl() { return imagenUrl; }
        public boolean isEsPublicado() { return esPublicado; }
        public String getTipo() { return tipo; }
        public boolean isTieneRechazo() { return tieneRechazo; }
        public String getGuiaSeleccionadoId() { return guiaSeleccionadoId; }
        public String getMotivoRechazo() { return motivoRechazo; }
        public Object getFechaRealizacion() { return fechaRealizacion; }
        public String getHoraInicio() { return horaInicio; }
        public String getEstadoBloqueo() { return estadoBloqueo; }
        
        // Setters
        public void setTieneRechazo(boolean tieneRechazo) { this.tieneRechazo = tieneRechazo; }
        public void setGuiaSeleccionadoId(String guiaSeleccionadoId) { this.guiaSeleccionadoId = guiaSeleccionadoId; }
        public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
        public void setFechaRealizacion(Object fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }
        public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
        public void setEstadoBloqueo(String estadoBloqueo) { this.estadoBloqueo = estadoBloqueo; }
    }

    // Adapter para RecyclerView
    private class ToursAdapter extends RecyclerView.Adapter<ToursAdapter.TourViewHolder> {
        private List<TourItem> tours;

        public ToursAdapter(List<TourItem> tours) {
            this.tours = tours;
        }

        @NonNull
        @Override
        public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tour, parent, false);
            return new TourViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
            TourItem tour = tours.get(position);
            holder.bind(tour);
        }

        @Override
        public int getItemCount() {
            return tours.size();
        }

        class TourViewHolder extends RecyclerView.ViewHolder {
            private final ImageView ivTourImage;
            private final TextView tvTitulo;
            private final TextView tvFecha;
            private final TextView tvEstado;
            private final Button btnAction;
            private final View badgeRechazo; // Badge para indicar rechazo

            public TourViewHolder(@NonNull View itemView) {
                super(itemView);
                ivTourImage = itemView.findViewById(R.id.iv_tour_image);
                tvTitulo = itemView.findViewById(R.id.tv_titulo);
                tvFecha = itemView.findViewById(R.id.tv_fecha);
                tvEstado = itemView.findViewById(R.id.tv_estado);
                btnAction = itemView.findViewById(R.id.btn_action);
                badgeRechazo = itemView.findViewById(R.id.badge_rechazo);
            }

            public void bind(TourItem tour) {
                // Cargar imagen con Glide
                if (tour.getImagenUrl() != null && !tour.getImagenUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                        .load(tour.getImagenUrl())
                        .placeholder(R.drawable.placeholder_tour)
                        .error(R.drawable.placeholder_tour)
                        .centerCrop()
                        .into(ivTourImage);
                } else {
                    ivTourImage.setImageResource(R.drawable.placeholder_tour);
                }
                
                tvTitulo.setText(tour.getTitulo());
                tvFecha.setText(tour.getFecha());
                tvEstado.setText(tour.getEstado());

                // Configurar colores según el estado
                if (tour.isEsPublicado()) {
                    tvEstado.setTextColor(getColor(R.color.success_500));
                } else {
                    tvEstado.setTextColor(getColor(R.color.text_secondary));
                }
                
                // Mostrar badge de rechazo si aplica
                if (badgeRechazo != null) {
                    badgeRechazo.setVisibility(tour.isTieneRechazo() ? View.VISIBLE : View.GONE);
                }
                
                // Configurar botón de acción según el tipo
                setupActionButton(tour);

                // Aplicar estilo visual según estado de bloqueo (pero mantener clickeable para mostrar mensaje)
                if ("bloqueado".equals(tour.getEstadoBloqueo())) {
                    itemView.setAlpha(0.5f);
                    // NO deshabilitar itemView para permitir click y mostrar mensaje
                } else {
                    itemView.setAlpha(1.0f);
                }
                
                // Click listener - Para borradores, ir directo a editar
                itemView.setOnClickListener(v -> {
                    // Validar estado de bloqueo antes de permitir acción
                    if ("bloqueado".equals(tour.getEstadoBloqueo())) {
                        // Recalcular estado por si acaso (el usuario pudo tener la pantalla abierta mucho tiempo)
                        String estadoActual = tour.isEsPublicado() 
                            ? com.example.connectifyproject.utils.TourTimeValidator.getEstadoTourSinAsignar(
                                tour.getFechaRealizacion(), tour.getHoraInicio())
                            : com.example.connectifyproject.utils.TourTimeValidator.getEstadoTourPendiente(
                                tour.getFechaRealizacion(), tour.getHoraInicio());
                        
                        if ("oculto".equals(estadoActual) || "bloqueado".equals(estadoActual)) {
                            // Mostrar mensaje explicativo
                            String mensaje = tour.isEsPublicado()
                                ? com.example.connectifyproject.utils.TourTimeValidator.getMensajeTourSinAsignarBloqueado(
                                    tour.getFechaRealizacion(), tour.getHoraInicio())
                                : com.example.connectifyproject.utils.TourTimeValidator.getMensajeTourPendienteBloqueado(
                                    tour.getFechaRealizacion(), tour.getHoraInicio());
                            
                            new androidx.appcompat.app.AlertDialog.Builder(admin_tours.this)
                                .setTitle("Tour no disponible")
                                .setMessage(mensaje)
                                .setPositiveButton("Entendido", (dialog, which) -> {
                                    // Recargar la lista para actualizar el estado
                                    loadToursForCurrentTab();
                                })
                                .show();
                            return;
                        }
                    }
                    
                    if ("borrador".equals(tour.getTipo())) {
                        // Para borradores, ir directo a editar
                        Intent intent = new Intent(admin_tours.this, admin_create_tour.class);
                        intent.putExtra("borradorId", tour.getId());
                        startActivity(intent);
                    } else {
                        // Para otros tipos, ir a detalles
                        Intent intent = new Intent(admin_tours.this, admin_tour_details.class);
                        intent.putExtra("tour_id", tour.getId());
                        intent.putExtra("tour_titulo", tour.getTitulo());
                        intent.putExtra("tour_estado", tour.getEstado());
                        intent.putExtra("tour_tipo", tour.getTipo());
                        startActivity(intent);
                    }
                });
            }
            
            private void setupActionButton(TourItem tour) {
                // Si el tour está bloqueado, deshabilitar el botón
                boolean esBloqueado = "bloqueado".equals(tour.getEstadoBloqueo());
                
                switch (tour.getTipo()) {
                    case "borrador":
                        // Para borradores, ocultar el botón (se hace clic en el ítem completo)
                        btnAction.setVisibility(View.GONE);
                        break;
                        
                    case "sin_guia":
                        btnAction.setText("Seleccionar guía");
                        btnAction.setVisibility(View.VISIBLE);
                        btnAction.setEnabled(!esBloqueado);
                        btnAction.setAlpha(esBloqueado ? 0.5f : 1.0f);
                        btnAction.setOnClickListener(v -> {
                            if (!esBloqueado) {
                                // Guardar datos temporalmente
                                tempOfertaId = tour.getId();
                                tempTourTitulo = tour.getTitulo();
                                
                                // Primero ir a selección de método de pago
                                Intent intent = new Intent(admin_tours.this, AdminSelectPaymentMethodActivity.class);
                                startActivityForResult(intent, REQUEST_SELECT_PAYMENT);
                            }
                        });
                        break;
                        
                    case "pendiente":
                        if (tour.isTieneRechazo()) {
                            // Mostrar opciones para rechazos
                            btnAction.setText("Gestionar rechazo");
                            btnAction.setVisibility(View.VISIBLE);
                            btnAction.setOnClickListener(v -> {
                                showRejectionOptions(tour);
                            });
                        } else {
                            // Ocultar botón - el usuario puede hacer clic en el item completo
                            btnAction.setVisibility(View.GONE);
                        }
                        break;
                    
                    case "confirmado":
                        // Ocultar botón - el usuario puede hacer clic en el item completo
                        btnAction.setVisibility(View.GONE);
                        break;
                        
                    default:
                        btnAction.setVisibility(View.GONE);
                        break;
                }
            }
            
            private void showRejectionOptions(TourItem tour) {
                String mensaje = "El guía ha rechazado la oferta del tour.";
                if (tour.getMotivoRechazo() != null && !tour.getMotivoRechazo().isEmpty()) {
                    mensaje += "\n\nMotivo: " + tour.getMotivoRechazo();
                }
                
                new androidx.appcompat.app.AlertDialog.Builder(admin_tours.this)
                    .setTitle("Tour Rechazado")
                    .setMessage(mensaje)
                    .setPositiveButton("Seleccionar otro guía", (dialog, which) -> {
                        // Marcar como visto
                        marcarRechazoVisto(tour.getId(), tour.getGuiaSeleccionadoId());
                        
                        // Guardar datos temporalmente
                        tempOfertaId = tour.getId();
                        tempTourTitulo = tour.getTitulo();
                        
                        // Primero ir a selección de método de pago
                        Intent intent = new Intent(admin_tours.this, AdminSelectPaymentMethodActivity.class);
                        startActivityForResult(intent, REQUEST_SELECT_PAYMENT);
                    })
                    .setNegativeButton("Cancelar ofrecimiento", (dialog, which) -> {
                        // Cancelar el ofrecimiento actual
                        cancelarOfrecimiento(tour.getId(), tour.getGuiaSeleccionadoId());
                    })
                    .setNeutralButton("Cerrar", (dialog, which) -> {
                        // Solo marcar como visto
                        marcarRechazoVisto(tour.getId(), tour.getGuiaSeleccionadoId());
                    })
                    .show();
            }
        }
    }
    
    private void marcarRechazoVisto(String ofertaId, String guiaId) {
        adminTourService.marcarRechazoVisto(ofertaId, guiaId)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Rechazo marcado como visto", Toast.LENGTH_SHORT).show();
                loadTours(currentTab); // Recargar lista
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al marcar rechazo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void cancelarOfrecimiento(String ofertaId, String guiaId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar cancelación")
            .setMessage("¿Está seguro de cancelar el ofrecimiento al guía actual?")
            .setPositiveButton("Confirmar", (dialog, which) -> {
                adminTourService.cancelarOfrecimiento(ofertaId, guiaId)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Ofrecimiento cancelado exitosamente", Toast.LENGTH_SHORT).show();
                        loadTours(currentTab); // Recargar lista
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cancelar ofrecimiento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SELECT_PAYMENT && resultCode == RESULT_OK) {
            // El admin seleccionó un método de pago (o lo saltó)
            // Ahora navegar a selección de guía
            if (tempOfertaId != null && tempTourTitulo != null) {
                Intent intent = new Intent(admin_tours.this, admin_select_guide.class);
                intent.putExtra("ofertaId", tempOfertaId);
                intent.putExtra("tourTitulo", tempTourTitulo);
                
                // Opcional: pasar el método de pago seleccionado (solo informativo)
                if (data != null && data.hasExtra("selectedPaymentMethodId")) {
                    String paymentMethodId = data.getStringExtra("selectedPaymentMethodId");
                    intent.putExtra("paymentMethodId", paymentMethodId);
                }
                
                startActivity(intent);
                
                // Limpiar datos temporales
                tempOfertaId = null;
                tempTourTitulo = null;
            }
        }
    }
}
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class admin_tours extends AppCompatActivity {
    private AdminToursViewBinding binding;
    private ToursAdapter toursAdapter;
    private List<TourItem> toursList;
    private String currentTab = "borradores";
    
    // Firebase
    private AdminTourService adminTourService;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String empresaId;
    private SimpleDateFormat dateFormat;

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

        // Configurar datos iniciales
        loadTours("borradores");

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
                        empresaId = documentSnapshot.getString("empresaId");
                        // Recargar tours después de obtener empresaId
                        loadTours(currentTab);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos de usuario", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void setupRecyclerView() {
        toursList = new ArrayList<>();
        toursAdapter = new ToursAdapter(toursList);
        binding.recyclerViewTours.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTours.setAdapter(toursAdapter);
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabText = tab.getText().toString().toLowerCase();
                currentTab = tabText;
                loadTours(tabText);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
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
                
            case "pendiente":
                loadPendienteConfirmacion();
                break;
                
            case "sin_guia":
                loadSinGuiaAsignado();
                break;
                
            case "confirmados":
                loadConfirmados();
                break;
                
            case "cancelados":
                loadCancelados();
                break;
        }
    }
    
    private void loadBorradores() {
        adminTourService.listarBorradores(empresaId)
            .addOnSuccessListener(borradores -> {
                toursList.clear();
                for (TourBorrador borrador : borradores) {
                    // fechaRealizacion ya es String en formato dd/MM/yyyy
                    String fecha = borrador.getFechaRealizacion() != null 
                        ? borrador.getFechaRealizacion()
                        : "Sin fecha";
                    
                    String imageUrl = borrador.getImagenPrincipal();
                    
                    toursList.add(new TourItem(
                        borrador.getId(),
                        borrador.getTitulo(),
                        fecha,
                        "Borrador",
                        imageUrl,
                        false,
                        "borrador"
                    ));
                }
                toursAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar borradores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                        String fecha = fechaRealizacion != null 
                            ? dateFormat.format(fechaRealizacion.toDate())
                            : "Sin fecha";
                        
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
                        
                        // Verificar si hay rechazo no visto
                        checkForRejection(doc.getId(), guiaSeleccionado, tourItem);
                        
                        toursList.add(tourItem);
                    }
                });
                toursAdapter.notifyDataSetChanged();
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
                        toursAdapter.notifyDataSetChanged();
                    }
                }
            });
    }
    
    private void loadSinGuiaAsignado() {
        db.collection("tours_ofertas")
            .whereEqualTo("empresaId", empresaId)
            .whereEqualTo("estado", "publicado")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                toursList.clear();
                querySnapshot.getDocuments().forEach(doc -> {
                    String guiaSeleccionado = doc.getString("guiaSeleccionadoActual");
                    
                    // Solo mostrar tours SIN guía seleccionado
                    if (guiaSeleccionado == null || guiaSeleccionado.isEmpty()) {
                        String titulo = doc.getString("titulo");
                        Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                        String fecha = fechaRealizacion != null 
                            ? dateFormat.format(fechaRealizacion.toDate())
                            : "Sin fecha";
                        
                        List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                        String imageUrl = (imagenesUrls != null && !imagenesUrls.isEmpty()) 
                            ? imagenesUrls.get(0) 
                            : null;
                        
                        toursList.add(new TourItem(
                            doc.getId(),
                            titulo,
                            fecha,
                            "Sin guía asignado",
                            imageUrl,
                            false,
                            "sin_guia"
                        ));
                    }
                });
                toursAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar tours sin guía: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadConfirmados() {
        db.collection("tours_asignados")
            .whereEqualTo("empresaId", empresaId)
            .whereIn("estado", java.util.Arrays.asList("confirmado", "en_curso", "completado"))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                toursList.clear();
                querySnapshot.getDocuments().forEach(doc -> {
                    String titulo = doc.getString("titulo");
                    Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                    String fecha = fechaRealizacion != null 
                        ? dateFormat.format(fechaRealizacion.toDate())
                        : "Sin fecha";
                    
                    List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                    String imageUrl = (imagenesUrls != null && !imagenesUrls.isEmpty()) 
                        ? imagenesUrls.get(0) 
                        : null;
                    
                    String estadoDoc = doc.getString("estado");
                    String estadoDisplay = estadoDoc != null ? capitalizeFirst(estadoDoc) : "Confirmado";
                    
                    toursList.add(new TourItem(
                        doc.getId(),
                        titulo,
                        fecha,
                        estadoDisplay,
                        imageUrl,
                        true,
                        "confirmado"
                    ));
                });
                toursAdapter.notifyDataSetChanged();
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
                    Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                    String fecha = fechaRealizacion != null 
                        ? dateFormat.format(fechaRealizacion.toDate())
                        : "Sin fecha";
                    
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
                            Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
                            String fecha = fechaRealizacion != null 
                                ? dateFormat.format(fechaRealizacion.toDate())
                                : "Sin fecha";
                            
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
                        toursAdapter.notifyDataSetChanged();
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
        
        // Setters
        public void setTieneRechazo(boolean tieneRechazo) { this.tieneRechazo = tieneRechazo; }
        public void setGuiaSeleccionadoId(String guiaSeleccionadoId) { this.guiaSeleccionadoId = guiaSeleccionadoId; }
        public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
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

                // Click listener para ir a detalles del tour
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(admin_tours.this, admin_tour_details.class);
                    intent.putExtra("tour_id", tour.getId());
                    intent.putExtra("tour_titulo", tour.getTitulo());
                    intent.putExtra("tour_estado", tour.getEstado());
                    intent.putExtra("tour_tipo", tour.getTipo());
                    startActivity(intent);
                });
            }
            
            private void setupActionButton(TourItem tour) {
                switch (tour.getTipo()) {
                    case "borrador":
                        btnAction.setText("Editar");
                        btnAction.setVisibility(View.VISIBLE);
                        btnAction.setOnClickListener(v -> {
                            Intent intent = new Intent(admin_tours.this, admin_create_tour.class);
                            intent.putExtra("borradorId", tour.getId());
                            startActivity(intent);
                        });
                        break;
                        
                    case "sin_guia":
                        btnAction.setText("Seleccionar guía");
                        btnAction.setVisibility(View.VISIBLE);
                        btnAction.setOnClickListener(v -> {
                            Intent intent = new Intent(admin_tours.this, admin_select_guide.class);
                            intent.putExtra("ofertaId", tour.getId());
                            intent.putExtra("tourTitulo", tour.getTitulo());
                            startActivity(intent);
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
                            btnAction.setText("Ver detalle");
                            btnAction.setVisibility(View.VISIBLE);
                            btnAction.setOnClickListener(v -> {
                                Intent intent = new Intent(admin_tours.this, admin_tour_details.class);
                                intent.putExtra("tour_id", tour.getId());
                                intent.putExtra("tour_tipo", tour.getTipo());
                                startActivity(intent);
                            });
                        }
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
                        // Marcar como visto y navegar a selección de guía
                        marcarRechazoVisto(tour.getId(), tour.getGuiaSeleccionadoId());
                        
                        Intent intent = new Intent(admin_tours.this, admin_select_guide.class);
                        intent.putExtra("ofertaId", tour.getId());
                        intent.putExtra("tourTitulo", tour.getTitulo());
                        startActivity(intent);
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
}
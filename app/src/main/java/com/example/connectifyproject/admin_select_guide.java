package com.example.connectifyproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.databinding.AdminSelectGuideViewBinding;
import com.example.connectifyproject.services.AdminTourService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class admin_select_guide extends AppCompatActivity {
    private AdminSelectGuideViewBinding binding;
    private GuideAdapter guideAdapter;
    private List<GuideItem> allGuides;
    private List<GuideItem> filteredGuides;
    
    // Datos del tour
    private String ofertaId;
    private String tourTitulo;
    private List<String> idiomasRequeridos;
    
    // Firebase
    private AdminTourService adminTourService;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    
    private static final String CHANNEL_ID = "tour_notifications";

    // Clase para representar un guía
    public static class GuideItem {
        public String id; // UID del usuario guía
        public String name;
        public String email;
        public double rating;
        public int tourCount;
        public List<String> languages;
        public String profileImageUrl;
        public boolean disponible;

        public GuideItem(String id, String name, String email, double rating, int tourCount, 
                        List<String> languages, String profileImageUrl, boolean disponible) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.rating = rating;
            this.tourCount = tourCount;
            this.languages = languages;
            this.profileImageUrl = profileImageUrl;
            this.disponible = disponible;
        }
        
        public String getLanguagesText() {
            if (languages == null || languages.isEmpty()) {
                return "Sin idiomas registrados";
            }
            return String.join(", ", languages);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminSelectGuideViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase
        adminTourService = new AdminTourService();
        db = FirebaseFirestore.getInstance();
        
        // Crear canal de notificaciones
        createNotificationChannel();

        // Obtener datos del Intent
        ofertaId = getIntent().getStringExtra("ofertaId");
        tourTitulo = getIntent().getStringExtra("tourTitulo");
        
        if (ofertaId == null || tourTitulo == null) {
            Toast.makeText(this, "Error: Datos del tour no disponibles", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Cargar idiomas requeridos del tour
        loadTourRequirements();

        // Configurar toolbar
        binding.topAppBar.setTitle("Seleccionar Guía - " + tourTitulo);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Inicializar listas
        allGuides = new ArrayList<>();
        filteredGuides = new ArrayList<>();
        
        setupRecyclerView();
        setupSearch();
        setupLanguageFilters();
        
        // Cargar guías desde Firebase
        loadGuidesFromFirebase();
    }
    
    private void loadTourRequirements() {
        android.util.Log.d("AdminSelectGuide", "Cargando requisitos del tour: " + ofertaId);
        
        db.collection("tours_ofertas")
            .document(ofertaId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    android.util.Log.d("AdminSelectGuide", "Tour encontrado en tours_ofertas");
                    idiomasRequeridos = (List<String>) doc.get("idiomasRequeridos");
                    android.util.Log.d("AdminSelectGuide", "Idiomas requeridos del tour: " + idiomasRequeridos);
                    
                    if (idiomasRequeridos != null && !idiomasRequeridos.isEmpty()) {
                        // Preseleccionar chips de idiomas requeridos
                        preselectLanguageChips();
                    } else {
                        android.util.Log.w("AdminSelectGuide", "⚠ El tour no tiene idiomas requeridos configurados");
                    }
                } else {
                    android.util.Log.e("AdminSelectGuide", "✗ Tour no encontrado en tours_ofertas");
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminSelectGuide", "Error al cargar requisitos del tour", e);
                Toast.makeText(this, "Error al cargar requisitos del tour", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void preselectLanguageChips() {
        if (idiomasRequeridos == null) return;
        
        for (String idioma : idiomasRequeridos) {
            switch (idioma.toLowerCase()) {
                case "español":
                    binding.chipEspanol.setChecked(true);
                    break;
                case "inglés":
                case "ingles":
                    binding.chipIngles.setChecked(true);
                    break;
                case "francés":
                case "frances":
                    binding.chipFrances.setChecked(true);
                    break;
                case "alemán":
                case "aleman":
                    binding.chipAleman.setChecked(true);
                    break;
                case "italiano":
                    binding.chipItaliano.setChecked(true);
                    break;
                case "chino":
                    binding.chipChino.setChecked(true);
                    break;
                case "japonés":
                case "japones":
                    binding.chipJapones.setChecked(true);
                    break;
            }
        }
    }
    
    private void loadGuidesFromFirebase() {
        showProgressDialog("Cargando guías...");
        
        android.util.Log.d("AdminSelectGuide", "=== INICIANDO CARGA DE GUÍAS ===");
        android.util.Log.d("AdminSelectGuide", "OfertaId: " + ofertaId);
        android.util.Log.d("AdminSelectGuide", "Tour título: " + tourTitulo);
        android.util.Log.d("AdminSelectGuide", "Idiomas requeridos: " + idiomasRequeridos);
        
        db.collection("usuarios")
            .whereEqualTo("rol", "Guia")  // ✓ Corregido: "Guia" con mayúscula
            .whereEqualTo("habilitado", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                android.util.Log.d("AdminSelectGuide", "Guías encontrados en DB: " + querySnapshot.size());
                
                allGuides.clear();
                int guiasDescartados = 0;
                int guiasAgregados = 0;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String id = doc.getId();
                    
                    // Usar nombresApellidos como campo único
                    String nombresApellidos = doc.getString("nombresApellidos");
                    String email = doc.getString("email");
                    String name = nombresApellidos != null ? nombresApellidos : "Sin nombre";
                    
                    android.util.Log.d("AdminSelectGuide", "--- Procesando guía: " + name + " (ID: " + id + ")");
                    
                    // Calificación (calculada a partir de sumaResenias / numeroResenias)
                    Long numeroResenias = doc.getLong("numeroResenias");
                    Long sumaResenias = doc.getLong("sumaResenias");
                    double rating = 0.0;
                    if (numeroResenias != null && numeroResenias > 0 && sumaResenias != null) {
                        rating = (double) sumaResenias / numeroResenias;
                    }
                    android.util.Log.d("AdminSelectGuide", "  - Calificación: " + rating + " (reseñas: " + numeroResenias + ")");
                    
                    // Tours completados (si existe el campo)
                    Long toursCompletados = doc.getLong("toursCompletados");
                    int tourCount = toursCompletados != null ? toursCompletados.intValue() : 0;
                    android.util.Log.d("AdminSelectGuide", "  - Tours completados: " + tourCount);
                    
                    List<String> idiomas = (List<String>) doc.get("idiomas");
                    if (idiomas == null) {
                        idiomas = new ArrayList<>();
                    }
                    android.util.Log.d("AdminSelectGuide", "  - Idiomas del guía: " + idiomas);
                    
                    // Usar photoUrl (el campo correcto en tu BD)
                    String profileImageUrl = doc.getString("photoUrl");
                    android.util.Log.d("AdminSelectGuide", "  - Foto perfil: " + (profileImageUrl != null ? "presente" : "null"));
                    
                    // Por ahora todos disponibles, se validará al seleccionar
                    boolean disponible = true;
                    
                    // Filtrar solo guías que cumplan con TODOS los idiomas requeridos
                    if (idiomasRequeridos != null && !idiomasRequeridos.isEmpty()) {
                        android.util.Log.d("AdminSelectGuide", "  - Verificando idiomas requeridos...");
                        boolean cumpleTodosLosIdiomas = true;
                        
                        // El guía debe tener TODOS los idiomas requeridos
                        for (String idiomaRequerido : idiomasRequeridos) {
                            boolean tieneEsteIdioma = false;
                            for (String idiomaGuia : idiomas) {
                                if (idiomaGuia.equalsIgnoreCase(idiomaRequerido)) {
                                    android.util.Log.d("AdminSelectGuide", "    ✓ Coincidencia: " + idiomaRequerido + " = " + idiomaGuia);
                                    tieneEsteIdioma = true;
                                    break;
                                }
                            }
                            if (!tieneEsteIdioma) {
                                android.util.Log.d("AdminSelectGuide", "    ✗ Falta idioma: " + idiomaRequerido);
                                cumpleTodosLosIdiomas = false;
                                break;
                            }
                        }
                        
                        if (!cumpleTodosLosIdiomas) {
                            android.util.Log.d("AdminSelectGuide", "  ✗ Guía descartado por idiomas");
                            guiasDescartados++;
                            continue; // Saltar este guía si no cumple TODOS los idiomas
                        }
                    }
                    
                    android.util.Log.d("AdminSelectGuide", "  ✓ Guía agregado a la lista");
                    GuideItem guide = new GuideItem(id, name, email, rating, tourCount, 
                                                   idiomas, profileImageUrl, disponible);
                    allGuides.add(guide);
                    guiasAgregados++;
                }
                
                android.util.Log.d("AdminSelectGuide", "=== RESUMEN ===");
                android.util.Log.d("AdminSelectGuide", "Total guías procesados: " + querySnapshot.size());
                android.util.Log.d("AdminSelectGuide", "Guías agregados: " + guiasAgregados);
                android.util.Log.d("AdminSelectGuide", "Guías descartados: " + guiasDescartados);
                
                filteredGuides.clear();
                filteredGuides.addAll(allGuides);
                
                dismissProgressDialog();
                guideAdapter.notifyDataSetChanged();
                
                if (allGuides.isEmpty()) {
                    android.util.Log.w("AdminSelectGuide", "⚠ No se encontraron guías que cumplan los requisitos");
                    Toast.makeText(this, "No se encontraron guías que cumplan los requisitos", 
                        Toast.LENGTH_LONG).show();
                } else {
                    android.util.Log.d("AdminSelectGuide", "✓ " + allGuides.size() + " guías cargados exitosamente");
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminSelectGuide", "Error al cargar guías", e);
                dismissProgressDialog();
                Toast.makeText(this, "Error al cargar guías: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void setupRecyclerView() {
        guideAdapter = new GuideAdapter(filteredGuides, this::onGuideSelected);
        binding.recyclerViewGuides.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewGuides.setAdapter(guideAdapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterGuides(s.toString().trim());
            }
        });
    }

    private void filterGuides(String query) {
        filteredGuides.clear();
        if (query.isEmpty()) {
            filteredGuides.addAll(allGuides);
        } else {
            for (GuideItem guide : allGuides) {
                if (guide.name.toLowerCase().contains(query.toLowerCase()) ||
                    guide.email.toLowerCase().contains(query.toLowerCase()) ||
                    guide.getLanguagesText().toLowerCase().contains(query.toLowerCase())) {
                    filteredGuides.add(guide);
                }
            }
        }
        guideAdapter.notifyDataSetChanged();
    }

    private void setupLanguageFilters() {
        binding.chipGroupLanguages.setOnCheckedStateChangeListener((group, checkedIds) -> {
            applyLanguageFilter();
        });
    }

    private void applyLanguageFilter() {
        List<String> selectedLanguages = new ArrayList<>();
        
        if (binding.chipEspanol.isChecked()) selectedLanguages.add("Español");
        if (binding.chipIngles.isChecked()) selectedLanguages.add("Inglés");
        if (binding.chipFrances.isChecked()) selectedLanguages.add("Francés");
        if (binding.chipAleman.isChecked()) selectedLanguages.add("Alemán");
        if (binding.chipItaliano.isChecked()) selectedLanguages.add("Italiano");
        if (binding.chipChino.isChecked()) selectedLanguages.add("Chino");
        if (binding.chipJapones.isChecked()) selectedLanguages.add("Japonés");

        filteredGuides.clear();
        
        if (selectedLanguages.isEmpty()) {
            // Si no hay filtros seleccionados, mostrar todos los guías
            filteredGuides.addAll(allGuides);
        } else {
            // Filtrar guías que hablen al menos uno de los idiomas seleccionados
            for (GuideItem guide : allGuides) {
                for (String language : selectedLanguages) {
                    if (containsLanguage(guide.languages, language)) {
                        filteredGuides.add(guide);
                        break; // Evitar duplicados
                    }
                }
            }
        }
        
        // También aplicar filtro de búsqueda si hay texto
        String searchQuery = binding.etSearch.getText().toString().trim();
        if (!searchQuery.isEmpty()) {
            filterGuides(searchQuery);
        } else {
            guideAdapter.notifyDataSetChanged();
        }
    }
    
    private boolean containsLanguage(List<String> languages, String targetLanguage) {
        if (languages == null) return false;
        for (String lang : languages) {
            if (lang.toLowerCase().contains(targetLanguage.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void onGuideSelected(GuideItem guide) {
        android.util.Log.d("AdminSelectGuide", "onGuideSelected llamado para: " + guide.name);
        
        // Validar que el tour inicia en al menos 18 horas
        validarTiempoYSeleccionarGuia(guide);
    }
    
    private void validarTiempoYSeleccionarGuia(GuideItem guide) {
        // Cargar datos del tour para validar tiempo
        db.collection("tours_ofertas")
            .document(ofertaId)
            .get()
            .addOnSuccessListener(tourDoc -> {
                if (!tourDoc.exists()) {
                    Toast.makeText(this, "Error: Tour no encontrado", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Object fechaRealizacion = tourDoc.get("fechaRealizacion");
                String horaInicio = tourDoc.getString("horaInicio");
                
                // Validar que se puede asignar guía (>=18h)
                if (!com.example.connectifyproject.utils.TourTimeValidator.puedeAsignarGuia(fechaRealizacion, horaInicio)) {
                    String mensaje = com.example.connectifyproject.utils.TourTimeValidator.getMensajeTourSinAsignarBloqueado(fechaRealizacion, horaInicio);
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("No se puede asignar guía")
                        .setMessage(mensaje)
                        .setPositiveButton("Entendido", null)
                        .show();
                    return;
                }
                
                // Si pasa la validación, mostrar confirmación
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Confirmar selección")
                    .setMessage("¿Desea ofrecer el tour \"" + tourTitulo + "\" a " + guide.name + "?")
                    .setPositiveButton("Confirmar", (dialog, which) -> {
                        android.util.Log.d("AdminSelectGuide", "Usuario confirmó la selección");
                        selectGuide(guide);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        android.util.Log.d("AdminSelectGuide", "Usuario canceló la selección");
                    })
                    .show();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminSelectGuide", "Error al cargar tour", e);
                Toast.makeText(this, "Error al validar tiempo del tour", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void selectGuide(GuideItem guide) {
        android.util.Log.d("AdminSelectGuide", "=== SELECCIONANDO GUÍA ===");
        android.util.Log.d("AdminSelectGuide", "Guía seleccionado: " + guide.name + " (ID: " + guide.id + ")");
        android.util.Log.d("AdminSelectGuide", "Oferta ID: " + ofertaId);
        android.util.Log.d("AdminSelectGuide", "Tour: " + tourTitulo);
        
        showProgressDialog("Ofreciendo tour al guía...");
        
        adminTourService.seleccionarGuia(ofertaId, guide.id, null)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("AdminSelectGuide", "✓ Tour ofrecido exitosamente");
                dismissProgressDialog();
                
                // Enviar notificación local al guía
                sendNotificationToGuide(guide);
                
                // Mostrar confirmación
                Toast.makeText(this, "Tour ofrecido exitosamente a " + guide.name, 
                    Toast.LENGTH_LONG).show();
                
                // Regresar a admin_tours
                Intent intent = new Intent(this, admin_tours.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminSelectGuide", "✗ Error al ofrecer tour", e);
                dismissProgressDialog();
                Toast.makeText(this, "Error al ofrecer tour: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void sendNotificationToGuide(GuideItem guide) {
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        // Intent para abrir la app cuando se toque la notificación
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Nueva oferta de tour")
            .setContentText("Se te ha ofrecido el tour: " + tourTitulo)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Se te ha ofrecido el tour \"" + tourTitulo + 
                        "\". Revisa los detalles en la aplicación."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Tour Notifications";
            String description = "Notificaciones de ofertas de tours";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }
    
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    // Adapter para RecyclerView de guías
    private static class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> {
        private final List<GuideItem> guides;
        private final OnGuideClickListener listener;
        
        interface OnGuideClickListener {
            void onGuideClick(GuideItem guide);
        }
        
        public GuideAdapter(List<GuideItem> guides, OnGuideClickListener listener) {
            this.guides = guides;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guide, parent, false);
            return new GuideViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
            holder.bind(guides.get(position), listener);
        }
        
        @Override
        public int getItemCount() {
            return guides.size();
        }
        
        static class GuideViewHolder extends RecyclerView.ViewHolder {
            private final ImageView ivProfileImage;
            private final TextView tvName;
            private final TextView tvRating;
            private final TextView tvTourCount;
            private final TextView tvLanguages;
            private final com.google.android.material.button.MaterialButton btnSelectGuide;
            
            public GuideViewHolder(@NonNull View itemView) {
                super(itemView);
                ivProfileImage = itemView.findViewById(R.id.iv_guide_profile);
                tvName = itemView.findViewById(R.id.tv_guide_name);
                tvRating = itemView.findViewById(R.id.tv_guide_rating);
                tvTourCount = itemView.findViewById(R.id.tv_guide_tour_count);
                tvLanguages = itemView.findViewById(R.id.tv_guide_languages);
                btnSelectGuide = itemView.findViewById(R.id.btn_select_guide);
            }
            
            public void bind(GuideItem guide, OnGuideClickListener listener) {
                tvName.setText(guide.name);
                tvRating.setText(String.format(Locale.getDefault(), "%.1f", guide.rating));
                tvTourCount.setText("("+guide.tourCount + " reseñas)");
                tvLanguages.setText(guide.getLanguagesText());
                
                // Cargar imagen de perfil con Glide
                if (guide.profileImageUrl != null && !guide.profileImageUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                        .load(guide.profileImageUrl)
                        .placeholder(R.drawable.placeholder_profile)
                        .error(R.drawable.placeholder_profile)
                        .circleCrop()
                        .into(ivProfileImage);
                } else {
                    ivProfileImage.setImageResource(R.drawable.placeholder_profile);
                }
                
                // Click listener en el botón "Elegir"
                btnSelectGuide.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onGuideClick(guide);
                    }
                });
            }
        }
    }
}
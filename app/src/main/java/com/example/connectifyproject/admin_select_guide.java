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
        db.collection("tours_ofertas")
            .document(ofertaId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    idiomasRequeridos = (List<String>) doc.get("idiomasRequeridos");
                    if (idiomasRequeridos != null && !idiomasRequeridos.isEmpty()) {
                        // Preseleccionar chips de idiomas requeridos
                        preselectLanguageChips();
                    }
                }
            })
            .addOnFailureListener(e -> {
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
                case "italiano":
                    binding.chipItaliano.setChecked(true);
                    break;
                case "mandarín":
                case "mandarin":
                    binding.chipMandarin.setChecked(true);
                    break;
                case "portugués":
                case "portugues":
                    binding.chipPortugues.setChecked(true);
                    break;
            }
        }
    }
    
    private void loadGuidesFromFirebase() {
        showProgressDialog("Cargando guías...");
        
        db.collection("usuarios")
            .whereEqualTo("rol", "guia")
            .whereEqualTo("habilitado", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                allGuides.clear();
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String id = doc.getId();
                    String nombre = doc.getString("nombre");
                    String apellido = doc.getString("apellido");
                    String email = doc.getString("email");
                    String name = (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
                    
                    // Calificación y tours (por ahora valores por defecto)
                    Double calificacion = doc.getDouble("calificacion");
                    double rating = calificacion != null ? calificacion : 0.0;
                    
                    Long toursCompletados = doc.getLong("toursCompletados");
                    int tourCount = toursCompletados != null ? toursCompletados.intValue() : 0;
                    
                    List<String> idiomas = (List<String>) doc.get("idiomas");
                    if (idiomas == null) {
                        idiomas = new ArrayList<>();
                    }
                    
                    String profileImageUrl = doc.getString("urlFotoPerfil");
                    
                    // Por ahora todos disponibles, se validará al seleccionar
                    boolean disponible = true;
                    
                    // Filtrar solo guías que cumplan con idiomas requeridos
                    if (idiomasRequeridos != null && !idiomasRequeridos.isEmpty()) {
                        boolean cumpleRequisito = false;
                        for (String idiomaRequerido : idiomasRequeridos) {
                            for (String idiomaGuia : idiomas) {
                                if (idiomaGuia.equalsIgnoreCase(idiomaRequerido)) {
                                    cumpleRequisito = true;
                                    break;
                                }
                            }
                            if (cumpleRequisito) break;
                        }
                        
                        if (!cumpleRequisito) {
                            continue; // Saltar este guía si no cumple idiomas
                        }
                    }
                    
                    GuideItem guide = new GuideItem(id, name, email, rating, tourCount, 
                                                   idiomas, profileImageUrl, disponible);
                    allGuides.add(guide);
                }
                
                filteredGuides.clear();
                filteredGuides.addAll(allGuides);
                
                dismissProgressDialog();
                guideAdapter.notifyDataSetChanged();
                
                if (allGuides.isEmpty()) {
                    Toast.makeText(this, "No se encontraron guías que cumplan los requisitos", 
                        Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> {
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
        
        if (binding.chipEspanol.isChecked()) selectedLanguages.add("español");
        if (binding.chipIngles.isChecked()) selectedLanguages.add("inglés");
        if (binding.chipFrances.isChecked()) selectedLanguages.add("francés");
        if (binding.chipItaliano.isChecked()) selectedLanguages.add("italiano");
        if (binding.chipMandarin.isChecked()) selectedLanguages.add("mandarín");
        if (binding.chipPortugues.isChecked()) selectedLanguages.add("portugués");

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
        // Confirmar selección
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar selección")
            .setMessage("¿Desea ofrecer el tour \"" + tourTitulo + "\" a " + guide.name + "?")
            .setPositiveButton("Confirmar", (dialog, which) -> {
                selectGuide(guide);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void selectGuide(GuideItem guide) {
        showProgressDialog("Ofreciendo tour al guía...");
        
        adminTourService.seleccionarGuia(ofertaId, guide.id, null)
            .addOnSuccessListener(aVoid -> {
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
            
            public GuideViewHolder(@NonNull View itemView) {
                super(itemView);
                ivProfileImage = itemView.findViewById(R.id.iv_guide_profile);
                tvName = itemView.findViewById(R.id.tv_guide_name);
                tvRating = itemView.findViewById(R.id.tv_guide_rating);
                tvTourCount = itemView.findViewById(R.id.tv_guide_tour_count);
                tvLanguages = itemView.findViewById(R.id.tv_guide_languages);
            }
            
            public void bind(GuideItem guide, OnGuideClickListener listener) {
                tvName.setText(guide.name);
                tvRating.setText(String.format(Locale.getDefault(), "%.1f", guide.rating));
                tvTourCount.setText(guide.tourCount + " tours");
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
                
                // Click listener
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onGuideClick(guide);
                    }
                });
            }
        }
    }
}

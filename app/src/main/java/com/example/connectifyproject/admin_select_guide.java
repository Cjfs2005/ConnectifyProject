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
    private String tourCiudad;
    private List<String> idiomasRequeridos;
    
    // Firebase
    private AdminTourService adminTourService;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    
    private static final String CHANNEL_ID = "tour_notifications";
    private static final int REQUEST_SELECT_PAYMENT = 2001;
    
    // Guía seleccionado temporalmente (esperando método de pago)
    private GuideItem selectedGuide;

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
        public String ciudad;
        public int reviewCount;  // Número de reseñas

        public GuideItem(String id, String name, String email, double rating, int tourCount, 
                        List<String> languages, String profileImageUrl, boolean disponible, String ciudad, int reviewCount) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.rating = rating;
            this.tourCount = tourCount;
            this.languages = languages;
            this.profileImageUrl = profileImageUrl;
            this.disponible = disponible;
            this.ciudad = ciudad;
            this.reviewCount = reviewCount;
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
        setupCityFilter();
        
        // Cargar guías desde Firebase
        loadGuidesFromFirebase();
    }
    
    // Variables para validación de horarios
    private Object tourFechaRealizacion;
    private String tourHoraInicio;
    private String tourHoraFin;
    
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
                    
                    // Guardar datos de horario para validación de conflictos
                    tourFechaRealizacion = doc.get("fechaRealizacion");
                    tourHoraInicio = doc.getString("horaInicio");
                    tourHoraFin = doc.getString("horaFin");
                    tourCiudad = doc.getString("ciudad");
                    
                    android.util.Log.d("AdminSelectGuide", "Ciudad del tour: " + tourCiudad);
                    
                    android.util.Log.d("AdminSelectGuide", "Horario tour - Fecha: " + tourFechaRealizacion + 
                                      ", Inicio: " + tourHoraInicio + ", Fin: " + tourHoraFin);
                    
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
                    
                    // Obtener ciudad del guía (almacenada en campo domicilio)
                    String ciudadGuia = doc.getString("domicilio");
                    android.util.Log.d("AdminSelectGuide", "  - Ciudad del guía: " + ciudadGuia);
                    
                    // Por ahora todos disponibles, se validará al seleccionar
                    boolean disponible = true;
                    
                    android.util.Log.d("AdminSelectGuide", "  ✓ Guía aceptado, verificando horario...");
                    
                    // Crear el GuideItem temporalmente con número de reseñas
                    int reviewCount = numeroResenias != null ? numeroResenias.intValue() : 0;
                    GuideItem guide = new GuideItem(id, name, email, rating, tourCount, 
                                                   idiomas, profileImageUrl, disponible, ciudadGuia, reviewCount);
                    
                    // Verificar conflicto de horario antes de agregar
                    verificarConflictoHorario(id, tieneConflicto -> {
                        if (!tieneConflicto) {
                            android.util.Log.d("AdminSelectGuide", "  ✓ Sin conflicto - Guía agregado: " + name);
                            allGuides.add(guide);
                            filteredGuides.clear();
                            filteredGuides.addAll(allGuides);
                            runOnUiThread(() -> guideAdapter.notifyDataSetChanged());
                        } else {
                            android.util.Log.d("AdminSelectGuide", "  ✗ Conflicto de horario - Guía descartado: " + name);
                        }
                    });
                }
                
                // Dar tiempo para que terminen las verificaciones asíncronas
                new android.os.Handler().postDelayed(() -> {
                    android.util.Log.d("AdminSelectGuide", "=== RESUMEN ===");
                    android.util.Log.d("AdminSelectGuide", "Total guías procesados: " + querySnapshot.size());
                    android.util.Log.d("AdminSelectGuide", "Guías agregados: " + allGuides.size());
                    android.util.Log.d("AdminSelectGuide", "Guías descartados: " + (querySnapshot.size() - allGuides.size()));
                    
                    dismissProgressDialog();
                    
                    if (allGuides.isEmpty()) {
                        android.util.Log.w("AdminSelectGuide", "⚠ No se encontraron guías disponibles");
                        Toast.makeText(this, "No se encontraron guías disponibles para este horario", 
                            Toast.LENGTH_LONG).show();
                    } else {
                        android.util.Log.d("AdminSelectGuide", "✓ " + allGuides.size() + " guías disponibles");
                        // Aplicar filtro inicial si hay chips preseleccionados
                        applyLanguageFilter();
                    }
                }, 1500); // Esperar 1.5 segundos para las verificaciones asíncronas
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminSelectGuide", "Error al cargar guías", e);
                dismissProgressDialog();
                Toast.makeText(this, "Error al cargar guías: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Verifica si un guía tiene conflicto de horario con el tour a asignar
     * Consulta tours_asignados del guía en la misma fecha
     */
    private void verificarConflictoHorario(String guiaId, ConflictoCallback callback) {
        if (tourFechaRealizacion == null || tourHoraInicio == null || tourHoraFin == null) {
            callback.onResult(false); // Sin datos, no se puede validar
            return;
        }
        
        // ✅ Convertir fecha del tour a Timestamp para comparación precisa
        com.google.firebase.Timestamp fechaTourTimestamp = null;
        if (tourFechaRealizacion instanceof com.google.firebase.Timestamp) {
            fechaTourTimestamp = (com.google.firebase.Timestamp) tourFechaRealizacion;
        } else if (tourFechaRealizacion instanceof String) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                java.util.Date date = sdf.parse((String) tourFechaRealizacion);
                fechaTourTimestamp = new com.google.firebase.Timestamp(date);
            } catch (Exception e) {
                android.util.Log.e("AdminSelectGuide", "Error parseando fecha: " + tourFechaRealizacion, e);
                callback.onResult(false);
                return;
            }
        }
        
        if (fechaTourTimestamp == null) {
            callback.onResult(false);
            return;
        }
        
        // Obtener solo la fecha (sin hora) para comparación
        java.util.Calendar calTour = java.util.Calendar.getInstance();
        calTour.setTime(fechaTourTimestamp.toDate());
        calTour.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calTour.set(java.util.Calendar.MINUTE, 0);
        calTour.set(java.util.Calendar.SECOND, 0);
        calTour.set(java.util.Calendar.MILLISECOND, 0);
        
        com.google.firebase.Timestamp finalFechaTour = fechaTourTimestamp;
        
        db.collection("tours_asignados")
            .whereEqualTo("guiaAsignado.identificadorUsuario", guiaId)
            .whereEqualTo("habilitado", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                android.util.Log.d("AdminSelectGuide", "Verificando " + querySnapshot.size() + " tours asignados del guía " + guiaId);
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Object fechaDoc = doc.get("fechaRealizacion");
                    
                    if (fechaDoc instanceof com.google.firebase.Timestamp) {
                        com.google.firebase.Timestamp fechaExistenteTimestamp = (com.google.firebase.Timestamp) fechaDoc;
                        
                        // Comparar solo fechas (sin hora)
                        java.util.Calendar calExistente = java.util.Calendar.getInstance();
                        calExistente.setTime(fechaExistenteTimestamp.toDate());
                        calExistente.set(java.util.Calendar.HOUR_OF_DAY, 0);
                        calExistente.set(java.util.Calendar.MINUTE, 0);
                        calExistente.set(java.util.Calendar.SECOND, 0);
                        calExistente.set(java.util.Calendar.MILLISECOND, 0);
                        
                        // Si es el mismo día, verificar solapamiento de horarios
                        if (calTour.getTimeInMillis() == calExistente.getTimeInMillis()) {
                            String horaInicioExistente = doc.getString("horaInicio");
                            String horaFinExistente = doc.getString("horaFin");
                            
                            android.util.Log.d("AdminSelectGuide", "Mismo día detectado - Tour existente: " + 
                                              horaInicioExistente + " - " + horaFinExistente);
                            android.util.Log.d("AdminSelectGuide", "Tour a asignar: " + 
                                              tourHoraInicio + " - " + tourHoraFin);
                            
                            if (hayConflictoHorario(tourHoraInicio, tourHoraFin, horaInicioExistente, horaFinExistente)) {
                                android.util.Log.d("AdminSelectGuide", "✗ Conflicto detectado - Guía: " + guiaId + 
                                                  " tiene tour de " + horaInicioExistente + " a " + horaFinExistente);
                                callback.onResult(true); // Hay conflicto
                                return;
                            }
                        }
                    }
                }
                android.util.Log.d("AdminSelectGuide", "✓ Sin conflictos para guía: " + guiaId);
                callback.onResult(false); // No hay conflicto
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminSelectGuide", "Error verificando conflictos", e);
                callback.onResult(false); // En caso de error, permitir selección
            });
    }
    
    /**
     * Verifica si dos rangos de horarios se solapan
     */
    private boolean hayConflictoHorario(String inicio1, String fin1, String inicio2, String fin2) {
        if (inicio1 == null || fin1 == null || inicio2 == null || fin2 == null) {
            return false;
        }
        
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            java.util.Date i1 = sdf.parse(inicio1);
            java.util.Date f1 = sdf.parse(fin1);
            java.util.Date i2 = sdf.parse(inicio2);
            java.util.Date f2 = sdf.parse(fin2);
            
            // Solapamiento: (inicio1 < fin2) AND (fin1 > inicio2)
            return i1.before(f2) && f1.after(i2);
        } catch (Exception e) {
            android.util.Log.e("AdminSelectGuide", "Error comparando horarios", e);
            return false;
        }
    }
    
    interface ConflictoCallback {
        void onResult(boolean tieneConflicto);
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
    
    private void setupCityFilter() {
        // Switch activado por defecto
        binding.switchCityFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyLanguageFilter(); // Reaplicar todos los filtros
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
            // ✅ Filtrar guías que hablen TODOS los idiomas seleccionados
            for (GuideItem guide : allGuides) {
                boolean tieneTodosLosIdiomas = true;
                
                // Verificar que el guía tenga CADA UNO de los idiomas seleccionados
                for (String language : selectedLanguages) {
                    if (!containsLanguage(guide.languages, language)) {
                        tieneTodosLosIdiomas = false;
                        break;
                    }
                }
                
                if (tieneTodosLosIdiomas) {
                    filteredGuides.add(guide);
                }
            }
        }
        
        // Aplicar filtro de ciudad si está activado
        if (binding.switchCityFilter.isChecked() && tourCiudad != null && !tourCiudad.isEmpty()) {
            List<GuideItem> ciudadFiltered = new ArrayList<>();
            for (GuideItem guide : filteredGuides) {
                if (guide.ciudad != null && guide.ciudad.equalsIgnoreCase(tourCiudad)) {
                    ciudadFiltered.add(guide);
                }
            }
            filteredGuides.clear();
            filteredGuides.addAll(ciudadFiltered);
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
        
        // Guardar guía seleccionado temporalmente
        selectedGuide = guide;
        
        // Ir a selección de método de pago
        Intent intent = new Intent(this, AdminSelectPaymentMethodActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_PAYMENT);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SELECT_PAYMENT && resultCode == RESULT_OK && data != null) {
            String paymentMethodId = data.getStringExtra("selectedPaymentMethodId");
            android.util.Log.d("AdminSelectGuide", "Método de pago seleccionado: " + paymentMethodId);
            
            // Ahora validar tiempo y asignar guía
            if (selectedGuide != null) {
                validarTiempoYSeleccionarGuia(selectedGuide, paymentMethodId);
            }
        } else {
            android.util.Log.d("AdminSelectGuide", "Selección de método de pago cancelada");
            selectedGuide = null;
        }
    }
    
    private void validarTiempoYSeleccionarGuia(GuideItem guide, String paymentMethodId) {
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
                        selectGuide(guide, paymentMethodId);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        android.util.Log.d("AdminSelectGuide", "Usuario canceló la selección");
                        selectedGuide = null;
                    })
                    .show();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AdminSelectGuide", "Error al cargar tour", e);
                Toast.makeText(this, "Error al validar tiempo del tour", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void selectGuide(GuideItem guide, String paymentMethodId) {
        android.util.Log.d("AdminSelectGuide", "=== SELECCIONANDO GUÍA ===");
        android.util.Log.d("AdminSelectGuide", "Guía seleccionado: " + guide.name + " (ID: " + guide.id + ")");
        android.util.Log.d("AdminSelectGuide", "Oferta ID: " + ofertaId);
        android.util.Log.d("AdminSelectGuide", "Tour: " + tourTitulo);
        android.util.Log.d("AdminSelectGuide", "Método de pago: " + paymentMethodId);
        
        showProgressDialog("Ofreciendo tour al guía...");
        
        adminTourService.seleccionarGuia(ofertaId, guide.id, paymentMethodId)
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
                // Mostrar rating y número de reseñas correctamente
                tvRating.setText(String.format(Locale.getDefault(), "★ %.1f (%d reseñas)", guide.rating, guide.reviewCount));
                // Si quieres mostrar tours completados, usa esta línea, si no, déjala vacía
                tvTourCount.setText(guide.tourCount > 0 ? String.format(Locale.getDefault(), "%d tours", guide.tourCount) : "");
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
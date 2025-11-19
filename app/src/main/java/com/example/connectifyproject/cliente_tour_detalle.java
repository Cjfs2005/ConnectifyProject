package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.adapters.Cliente_ServiciosAdapter;
import com.example.connectifyproject.models.Cliente_ServicioAdicional;
import com.example.connectifyproject.models.Cliente_Tour;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class cliente_tour_detalle extends AppCompatActivity implements Cliente_ServiciosAdapter.OnServiceSelectedListener {

    private MaterialToolbar toolbar;
    private androidx.viewpager2.widget.ViewPager2 vpTourImages;
    private TextView tvTourLocation, tvTourPriceMain, tvStartTime, tvEndTime, tvTourCompany;
    private TextView tvPeopleCount, tvTotalPrice, tvIdiomas;
    private TextView tvGuiaNombre, tvGuiaTelefono;
    private ImageView ivGuiaFoto;
    private LinearLayout layoutGuiaInfo;
    private RecyclerView rvServiciosAdicionales;
    private TextView tvNoServicios;
    private ImageButton btnDecreasePeople, btnIncreasePeople;
    private MaterialButton btnContinuar;
    private MaterialCardView cardEmpresa;
    private View layoutItinerario;

    private Cliente_ServiciosAdapter serviciosAdapter;
    private List<Cliente_ServicioAdicional> serviciosAdicionales;
    
    private Cliente_Tour tour;
    private int peopleCount = 1;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_tour_detalle);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadServiciosData();
        updateUI();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        
        // Crear tour desde los extras del intent
        tour = new Cliente_Tour();
        tour.setId(intent.getStringExtra("tour_id"));
        tour.setTitle(intent.getStringExtra("tour_title"));
        tour.setDescription(intent.getStringExtra("tour_description"));
        tour.setCompanyName(intent.getStringExtra("tour_company"));
        tour.setLocation(intent.getStringExtra("tour_location"));
        tour.setPrice(intent.getDoubleExtra("tour_price", 0.0));
        tour.setDuration(intent.getStringExtra("tour_duration"));
        tour.setDate(intent.getStringExtra("tour_date"));
        tour.setStartTime(intent.getStringExtra("tour_start_time"));
        tour.setEndTime(intent.getStringExtra("tour_end_time"));
        tour.setImageUrl(intent.getStringExtra("tour_image_url"));
        tour.setOfertaTourId(intent.getStringExtra("oferta_tour_id"));
        tour.setEmpresaId(intent.getStringExtra("empresa_id"));
        
        ArrayList<String> idiomas = intent.getStringArrayListExtra("idiomas");
        if (idiomas != null) {
            tour.setIdiomasRequeridos(idiomas);
        }
        
        // Recargar servicios e itinerario desde Firebase
        loadTourDetailsFromFirebase();
    }
    
    private void loadTourDetailsFromFirebase() {
        if (tour.getId() == null) return;
        
        FirebaseFirestore.getInstance()
                .collection("tours_asignados")
                .document(tour.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Cargar servicios adicionales
                        List<Map<String, Object>> servicios = 
                            (List<Map<String, Object>>) doc.get("serviciosAdicionales");
                        tour.setServiciosAdicionales(servicios);
                        
                        // Cargar itinerario
                        List<Map<String, Object>> itinerario = 
                            (List<Map<String, Object>>) doc.get("itinerario");
                        tour.setItinerario(itinerario);
                        
                        // Extraer nombre del primer punto del itinerario para la ubicación
                        if (itinerario != null && !itinerario.isEmpty()) {
                            Map<String, Object> primerPunto = itinerario.get(0);
                            String nombrePunto = (String) primerPunto.get("nombre");
                            if (nombrePunto != null && !nombrePunto.isEmpty()) {
                                tour.setLocation(nombrePunto);
                                tvTourLocation.setText(nombrePunto);
                            }
                        }
                        
                        // Cargar información del guía
                        Map<String, Object> guiaAsignado = 
                            (Map<String, Object>) doc.get("guiaAsignado");
                        if (guiaAsignado != null) {
                            loadGuiaInfo(guiaAsignado);
                        } else {
                            layoutGuiaInfo.setVisibility(View.GONE);
                        }
                        
                        // Actualizar servicios
                        loadServiciosData();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando detalles del tour", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadGuiaInfo(Map<String, Object> guiaAsignado) {
        String nombresCompletos = (String) guiaAsignado.get("nombresCompletos");
        String numeroTelefono = (String) guiaAsignado.get("numeroTelefono");
        String identificadorUsuario = (String) guiaAsignado.get("identificadorUsuario");
        
        if (nombresCompletos != null) {
            tvGuiaNombre.setText(nombresCompletos);
        }
        
        if (numeroTelefono != null) {
            tvGuiaTelefono.setText("📞 " + numeroTelefono);
        }
        
        // Cargar foto del guía desde usuarios
        if (identificadorUsuario != null && !identificadorUsuario.isEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(identificadorUsuario)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String photoUrl = doc.getString("photoUrl");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                .load(photoUrl)
                                .placeholder(R.drawable.ic_person)
                                .circleCrop()
                                .into(ivGuiaFoto);
                        }
                    }
                });
        }
        
        layoutGuiaInfo.setVisibility(View.VISIBLE);
    }

    private void initViews() {
        db = FirebaseFirestore.getInstance();
        toolbar = findViewById(R.id.toolbar);
        vpTourImages = findViewById(R.id.vp_tour_images);
        tvTourLocation = findViewById(R.id.tv_tour_location);
        tvTourPriceMain = findViewById(R.id.tv_tour_price_main);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        tvTourCompany = findViewById(R.id.tv_tour_company);
        tvPeopleCount = findViewById(R.id.tv_people_count);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvIdiomas = findViewById(R.id.tv_idiomas);
        tvGuiaNombre = findViewById(R.id.tv_guia_nombre);
        tvGuiaTelefono = findViewById(R.id.tv_guia_telefono);
        ivGuiaFoto = findViewById(R.id.iv_guia_foto);
        layoutGuiaInfo = findViewById(R.id.layout_guia_info);
        rvServiciosAdicionales = findViewById(R.id.rv_servicios_adicionales);
        tvNoServicios = findViewById(R.id.tv_no_servicios);
        btnDecreasePeople = findViewById(R.id.btn_decrease_people);
        btnIncreasePeople = findViewById(R.id.btn_increase_people);
        btnContinuar = findViewById(R.id.btn_continuar);
        cardEmpresa = findViewById(R.id.card_empresa);
        layoutItinerario = findViewById(R.id.layout_itinerario);
        
        // Cargar imágenes del tour desde Firebase
        loadTourImages();
    }
    
    private void loadTourImages() {
        if (tour == null || tour.getId() == null) return;
        
        db.collection("tours_asignados")
            .document(tour.getId())
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    List<String> imagenesUrls = (List<String>) doc.get("imagenesUrls");
                    if (imagenesUrls != null && !imagenesUrls.isEmpty()) {
                        setupTourImageSlider(imagenesUrls);
                    } else {
                        // Usar placeholder si no hay imágenes
                        setupTourImageSlider(java.util.Arrays.asList("placeholder"));
                    }
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("TourDetalle", "Error loading images", e);
                setupTourImageSlider(java.util.Arrays.asList("placeholder"));
            });
    }
    
    private void setupTourImageSlider(List<String> imageUrls) {
        com.example.connectifyproject.adapters.ImageSliderAdapter adapter = 
            new com.example.connectifyproject.adapters.ImageSliderAdapter(this, imageUrls, 
                R.drawable.cliente_tour_lima);
        vpTourImages.setAdapter(adapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(tour.getTitulo());
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        serviciosAdicionales = new ArrayList<>();
        serviciosAdapter = new Cliente_ServiciosAdapter(this, serviciosAdicionales);
        serviciosAdapter.setOnServiceSelectedListener(this);
        
        rvServiciosAdicionales.setLayoutManager(new LinearLayoutManager(this));
        rvServiciosAdicionales.setAdapter(serviciosAdapter);
    }

    private void setupClickListeners() {
        btnDecreasePeople.setOnClickListener(v -> {
            if (peopleCount > 1) {
                peopleCount--;
                updatePeopleCount();
            }
        });

        btnIncreasePeople.setOnClickListener(v -> {
            peopleCount++;
            updatePeopleCount();
        });

        btnContinuar.setOnClickListener(v -> {
            // Inscribir al cliente en el tour (sin pago)
            inscribirseEnTour();
        });

        cardEmpresa.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_empresa_info.class);
            intent.putExtra("empresa_id", tour.getEmpresaId());
            intent.putExtra("company_name", tour.getCompanyName());
            startActivity(intent);
        });

        layoutItinerario.setOnClickListener(v -> {
            Intent intent = new Intent(this, cliente_tour_mapa.class);
            intent.putExtra("tour_id", tour.getId());
            intent.putExtra("tour_title", tour.getTitulo());
            startActivity(intent);
        });
    }

    private void loadServiciosData() {
        serviciosAdicionales.clear();
        
        List<Map<String, Object>> servicios = tour.getServiciosAdicionales();
        if (servicios != null && !servicios.isEmpty()) {
            rvServiciosAdicionales.setVisibility(View.VISIBLE);
            tvNoServicios.setVisibility(View.GONE);
            
            for (int i = 0; i < servicios.size(); i++) {
                Map<String, Object> servicio = servicios.get(i);
                
                String nombre = (String) servicio.get("nombre");
                String descripcion = (String) servicio.get("descripcion");
                Number precioNum = (Number) servicio.get("precio");
                double precio = precioNum != null ? precioNum.doubleValue() : 0.0;
                
                String id = String.valueOf(i);
                serviciosAdicionales.add(new Cliente_ServicioAdicional(id, nombre, descripcion, precio));
            }
        } else {
            rvServiciosAdicionales.setVisibility(View.GONE);
            tvNoServicios.setVisibility(View.VISIBLE);
        }
        
        serviciosAdapter.notifyDataSetChanged();
    }

    private void updateUI() {
        // Imagen hero se carga con el slider (ya no se usa)
        
        // Ubicación
        tvTourLocation.setText(tour.getUbicacion());
        
        // Precio
        tvTourPriceMain.setText("S/" + String.format("%.2f", tour.getPrecio()));
        
        // Empresa
        tvTourCompany.setText(tour.getCompanyName());
        
        // Fechas y horas
        tvStartTime.setText(tour.getDate() + " - " + tour.getStartTime());
        tvEndTime.setText(tour.getDate() + " - " + tour.getEndTime());
        
        // Idiomas
        List<String> idiomas = tour.getIdiomasRequeridos();
        if (idiomas != null && !idiomas.isEmpty()) {
            StringBuilder idiomasText = new StringBuilder();
            for (String idioma : idiomas) {
                idiomasText.append("• ").append(idioma).append("\n");
            }
            tvIdiomas.setText(idiomasText.toString().trim());
        } else {
            tvIdiomas.setText("No especificado");
        }
        
        updatePeopleCount();
    }

    private void updatePeopleCount() {
        tvPeopleCount.setText(String.valueOf(peopleCount));
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double serviciosPrice = 0.0;
        for (Cliente_ServicioAdicional servicio : serviciosAdicionales) {
            if (servicio.isSelected()) {
                serviciosPrice += servicio.getPrice();
            }
        }
        
        double totalPerPerson = tour.getPrecio() + serviciosPrice;
        double totalPrice = totalPerPerson * peopleCount;
        
        tvTotalPrice.setText("S/" + String.format("%.2f", totalPrice));
    }

    @Override
    public void onServiceSelected(Cliente_ServicioAdicional servicio, boolean isSelected) {
        updateTotalPrice();
    }
    
    /**
     * 📝 INSCRIBIR CLIENTE EN EL TOUR (SIN PAGO)
     * Agrega al cliente al array participantes[] del tour en Firebase
     */
    private void inscribirseEnTour() {
        // Verificar autenticación
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "❌ Debes iniciar sesión para inscribirte", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (tour.getId() == null || tour.getId().isEmpty()) {
            Toast.makeText(this, "❌ Error: ID de tour no válido", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnContinuar.setEnabled(false);
        btnContinuar.setText("Inscribiendo...");
        
        String clienteId = currentUser.getUid();
        String clienteEmail = currentUser.getEmail();
        
        // Crear objeto participante
        // NOTA: No se puede usar FieldValue.serverTimestamp() dentro de arrayUnion()
        // Se usa com.google.firebase.Timestamp.now() en su lugar
        Map<String, Object> participante = new HashMap<>();
        participante.put("clienteId", clienteId);
        participante.put("clienteEmail", clienteEmail != null ? clienteEmail : "");
        participante.put("checkIn", false);
        participante.put("checkOut", false);
        participante.put("fechaInscripcion", com.google.firebase.Timestamp.now());
        
        // Verificar si ya está inscrito
        FirebaseFirestore.getInstance()
                .collection("tours_asignados")
                .document(tour.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<Map<String, Object>> participantes = 
                            (List<Map<String, Object>>) doc.get("participantes");
                        
                        // Verificar si ya está inscrito
                        if (participantes != null) {
                            for (Map<String, Object> p : participantes) {
                                if (clienteId.equals(p.get("clienteId"))) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "ℹ️ Ya estás inscrito en este tour", Toast.LENGTH_LONG).show();
                                        btnContinuar.setEnabled(true);
                                        btnContinuar.setText("Ya inscrito");
                                    });
                                    return;
                                }
                            }
                        }
                        
                        // Agregar participante al array
                        FirebaseFirestore.getInstance()
                                .collection("tours_asignados")
                                .document(tour.getId())
                                .update("participantes", FieldValue.arrayUnion(participante))
                                .addOnSuccessListener(aVoid -> {
                                    // También actualizar el contador
                                    FirebaseFirestore.getInstance()
                                            .collection("tours_asignados")
                                            .document(tour.getId())
                                            .update("numeroParticipantesTotal", FieldValue.increment(1))
                                            .addOnSuccessListener(aVoid2 -> {
                                                runOnUiThread(() -> {
                                                    Toast.makeText(this, "✅ ¡Inscripción exitosa! Ahora puedes hacer check-in cuando comience el tour", Toast.LENGTH_LONG).show();
                                                    btnContinuar.setText("✅ Inscrito");
                                                    
                                                    // Regresar a la pantalla anterior después de 1.5 segundos
                                                    new android.os.Handler().postDelayed(() -> {
                                                        finish();
                                                    }, 1500);
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                runOnUiThread(() -> {
                                                    Toast.makeText(this, "⚠️ Inscrito pero error al actualizar contador", Toast.LENGTH_SHORT).show();
                                                    btnContinuar.setEnabled(true);
                                                    btnContinuar.setText("Inscribirse");
                                                });
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "❌ Error al inscribirse: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        btnContinuar.setEnabled(true);
                                        btnContinuar.setText("Inscribirse");
                                    });
                                });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "❌ Tour no encontrado", Toast.LENGTH_SHORT).show();
                            btnContinuar.setEnabled(true);
                            btnContinuar.setText("Inscribirse");
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "❌ Error al verificar inscripción: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnContinuar.setEnabled(true);
                        btnContinuar.setText("Inscribirse");
                    });
                });
    }
}
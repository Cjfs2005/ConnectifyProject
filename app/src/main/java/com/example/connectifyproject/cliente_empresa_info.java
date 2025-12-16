package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.models.Cliente_Tour;
import com.example.connectifyproject.adapters.Cliente_GalleryTourAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.example.connectifyproject.adapters.Cliente_ReviewsAdapter;
import com.example.connectifyproject.models.Cliente_Review;

public class cliente_empresa_info extends AppCompatActivity {

    private RecyclerView rvReviews, rvToursGallery;
    private Cliente_ReviewsAdapter reviewsAdapter;
    private Cliente_GalleryTourAdapter toursGalleryAdapter;
    private List<Cliente_Review> reviewsList;
    private List<Cliente_Tour> toursGalleryList;
    private TextView tvVerMasReviews, tvReviewsTitle, tvNoReviews;
    private TextView tvCompanyAddress, tvCompanyDescription, tvCompanyEmail, tvCompanyPhone;
    private ViewPager2 viewPagerEmpresaFotos;
    private Button btnChatEmpresa;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String empresaId;
    private String empresaNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_empresa_info);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        empresaId = getIntent().getStringExtra("empresa_id");
        empresaNombre = getIntent().getStringExtra("company_name");
        
        initViews();
        setupToolbar();
        
        if (empresaId != null && !empresaId.isEmpty()) {
            loadCompanyInfoFromFirebase();
        } else {
            Toast.makeText(this, "Error: ID de empresa no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        setupReviews();
        setupToursGallery();
        setupClickListeners();
    }

    private void initViews() {
        rvReviews = findViewById(R.id.rv_reviews);
        rvToursGallery = findViewById(R.id.rv_tours_disponibles);
        tvVerMasReviews = findViewById(R.id.tv_ver_mas_reviews);
        tvReviewsTitle = findViewById(R.id.tv_reviews_title);
        tvNoReviews = findViewById(R.id.tv_no_reviews);
        tvCompanyAddress = findViewById(R.id.tv_company_address);
        tvCompanyDescription = findViewById(R.id.tv_company_description);
        tvCompanyEmail = findViewById(R.id.tv_company_email);
        tvCompanyPhone = findViewById(R.id.tv_company_phone);
        
        // ViewPager para fotos
        viewPagerEmpresaFotos = findViewById(R.id.vp_empresa_fotos);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (empresaNombre != null) {
                getSupportActionBar().setTitle(empresaNombre);
            }
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadCompanyInfoFromFirebase() {
        db.collection("usuarios")
            .document(empresaId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    processEmpresaData(doc);
                } else {
                    Toast.makeText(this, "Empresa no encontrada", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar información: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    private void processEmpresaData(DocumentSnapshot doc) {
        // Nombre de empresa
        String nombreEmpresa = doc.getString("nombreEmpresa");
        if (nombreEmpresa != null) {
            empresaNombre = nombreEmpresa;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(nombreEmpresa);
            }
        }
        
        // Descripción
        String descripcion = doc.getString("descripcionEmpresa");
        tvCompanyDescription.setText(descripcion != null ? descripcion : "Sin descripción disponible");
        
        // Ubicación
        String ubicacion = doc.getString("ubicacionEmpresa");
        tvCompanyAddress.setText(ubicacion != null ? ubicacion : "Ubicación no disponible");
        
        // Email
        String correoEmpresa = doc.getString("correoEmpresa");
        tvCompanyEmail.setText(correoEmpresa != null ? correoEmpresa : "No disponible");
        
        // Teléfono
        String telefonoEmpresa = doc.getString("telefonoEmpresa");
        tvCompanyPhone.setText(telefonoEmpresa != null ? telefonoEmpresa : "No disponible");
        
        // Cargar galería de fotos
        List<String> fotosEmpresa = (List<String>) doc.get("fotosEmpresa");
        if (fotosEmpresa != null && !fotosEmpresa.isEmpty() && viewPagerEmpresaFotos != null) {
            setupEmpresaPhotosGallery(fotosEmpresa);
        } else {
            // Si no hay fotos, mostrar placeholder
            setupEmpresaPhotosGallery(java.util.Arrays.asList("placeholder"));
        }
    }
    
    private void setupEmpresaPhotosGallery(List<String> fotosUrls) {
        if (viewPagerEmpresaFotos == null) return;
        
        com.example.connectifyproject.adapters.ImageSliderAdapter adapter = 
            new com.example.connectifyproject.adapters.ImageSliderAdapter(this, fotosUrls, 
                R.drawable.cliente_tour_lima);
        viewPagerEmpresaFotos.setAdapter(adapter);
    }

    private void setupReviews() {
        reviewsList = new ArrayList<>();
        reviewsAdapter = new Cliente_ReviewsAdapter(reviewsList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewsAdapter);
        
        // Cargar reseñas desde Firebase
        loadReviewsFromFirebase();
    }
    
    private void loadReviewsFromFirebase() {
        if (empresaId == null) return;
        
        // Obtener reseñas de la colección principal 'resenias' filtrando por empresaId
        // Usar solo whereEqualTo para evitar requerir índice compuesto, ordenar en memoria
        db.collection("resenias")
                .whereEqualTo("empresaId", empresaId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalReviews = querySnapshot.size();
                    
                    // Actualizar título con contador
                    if (tvReviewsTitle != null) {
                        tvReviewsTitle.setText("Reseñas (" + totalReviews + ")");
                    }
                    
                    if (totalReviews == 0) {
                        // Mostrar empty state
                        showReviewsEmptyState();
                    } else {
                        // Calcular promedio y cargar reseñas
                        reviewsList.clear();
                        double sumaRatings = 0;
                        int contador = 0;
                        
                        // Convertir a lista y ordenar manualmente por fecha
                        java.util.List<com.google.firebase.firestore.DocumentSnapshot> docs = 
                            new java.util.ArrayList<>(querySnapshot.getDocuments());
                        docs.sort((d1, d2) -> {
                            com.google.firebase.Timestamp f1 = d1.getTimestamp("fecha");
                            com.google.firebase.Timestamp f2 = d2.getTimestamp("fecha");
                            if (f1 == null) return 1;
                            if (f2 == null) return -1;
                            return f2.compareTo(f1); // DESC
                        });
                        
                        for (com.google.firebase.firestore.DocumentSnapshot doc : docs) {
                            String clienteId = doc.getString("clienteId");
                            String clienteNombre = doc.getString("clienteNombre");
                            String comentario = doc.getString("comentario");
                            Number puntuacionNum = (Number) doc.get("puntuacion");
                            com.google.firebase.Timestamp fecha = doc.getTimestamp("fecha");
                            
                            float puntuacion = puntuacionNum != null ? puntuacionNum.floatValue() : 0f;
                            sumaRatings += puntuacion;
                            contador++;
                            
                            // Convertir a modelo Cliente_Review con constructor completo
                            Cliente_Review review = new Cliente_Review(
                                doc.getId(),  // id
                                clienteId,    // usuarioId
                                clienteNombre != null ? clienteNombre : "Usuario",  // nombreUsuario
                                null,         // fotoUsuario (no necesaria)
                                puntuacion,   // calificacion
                                comentario != null && !comentario.isEmpty() ? comentario : "",  // comentario
                                fecha         // fecha Timestamp
                            );
                            reviewsList.add(review);
                        }
                        
                        // Calcular y mostrar promedio
                        if (contador > 0) {
                            float promedioRating = (float) (sumaRatings / contador);
                            // Mostrar promedio en el título o en un TextView separado
                            if (tvReviewsTitle != null) {
                                tvReviewsTitle.setText(String.format("Reseñas (" + totalReviews + ") - %.1f ★", promedioRating));
                            }
                        }
                        
                        // Actualizar adapter
                        if (reviewsAdapter != null) {
                            reviewsAdapter.notifyDataSetChanged();
                        }
                        
                        // Mostrar/ocultar empty state
                        if (rvReviews != null) rvReviews.setVisibility(View.VISIBLE);
                        if (tvNoReviews != null) tvNoReviews.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("EmpresaInfo", "Error counting reviews", e);
                    if (tvReviewsTitle != null) {
                        tvReviewsTitle.setText("Reseñas");
                    }
                });
    }
    
    private void showReviewsEmptyState() {
        if (rvReviews != null) rvReviews.setVisibility(View.GONE);
        if (tvNoReviews != null) tvNoReviews.setVisibility(View.VISIBLE);
        if (tvVerMasReviews != null) tvVerMasReviews.setVisibility(View.GONE);
    }
    
    private void hideReviewsEmptyState() {
        if (rvReviews != null) rvReviews.setVisibility(View.VISIBLE);
        if (tvNoReviews != null) tvNoReviews.setVisibility(View.GONE);
    }

    private void setupToursGallery() {
        toursGalleryList = new ArrayList<>();
        toursGalleryAdapter = new Cliente_GalleryTourAdapter(this, toursGalleryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvToursGallery.setLayoutManager(layoutManager);
        rvToursGallery.setAdapter(toursGalleryAdapter);
        
        // Cargar tours reales desde Firebase
        loadToursFromFirebase();
    }
    
    private void loadToursFromFirebase() {
        if (empresaId == null) {
            android.util.Log.w("EmpresaInfo", "empresaId is null, cannot load tours");
            return;
        }
        
        android.util.Log.d("EmpresaInfo", "Loading ALL tours for empresaId: " + empresaId);
        
        // Calcular fecha de mañana para filtrar localmente
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Date tomorrow = calendar.getTime();
        
        android.util.Log.d("EmpresaInfo", "Filtering tours from: " + tomorrow);
        
        // Obtener TODOS los tours de la empresa sin filtro de fecha en Firebase
        db.collection("tours_asignados")
                .whereEqualTo("empresaId", empresaId)
                .whereEqualTo("estado", "confirmado")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("EmpresaInfo", "Tours query successful, found: " + queryDocumentSnapshots.size());
                    toursGalleryList.clear();
                    
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Filtrar por fecha localmente
                            com.google.firebase.Timestamp fechaRealizacion = document.getTimestamp("fechaRealizacion");
                            if (fechaRealizacion != null && fechaRealizacion.toDate().after(tomorrow)) {
                                // Extraer datos del tour
                                String tourId = document.getId();
                                String titulo = document.getString("titulo");
                                String descripcion = document.getString("descripcion");
                                String duracion = document.getString("duracion");
                                Number precioNum = (Number) document.get("precio");
                                double precio = precioNum != null ? precioNum.doubleValue() : 0.0;
                                
                                // Obtener ubicación del primer punto del itinerario
                                String ubicacion = "No especificado";
                                List<Map<String, Object>> itinerario = (List<Map<String, Object>>) document.get("itinerario");
                                if (itinerario != null && !itinerario.isEmpty()) {
                                    Map<String, Object> primerPunto = itinerario.get(0);
                                    String nombrePunto = (String) primerPunto.get("nombre");
                                    if (nombrePunto != null && !nombrePunto.isEmpty()) {
                                        ubicacion = nombrePunto;
                                    }
                                }
                                
                                // Obtener calificación de la empresa (no del tour individual)
                                float calificacion = 0f;
                                
                                String nombreEmpresa = empresaNombre != null ? empresaNombre : "Empresa";
                                
                                Cliente_Tour tour = new Cliente_Tour(tourId, titulo, descripcion, 
                                        duracion, precio, ubicacion, calificacion, nombreEmpresa);
                                
                                // Obtener URL de la primera imagen
                                List<String> imagenesUrls = (List<String>) document.get("imagenesUrls");
                                if (imagenesUrls != null && !imagenesUrls.isEmpty()) {
                                    tour.setImageUrl(imagenesUrls.get(0));
                                }
                                
                                toursGalleryList.add(tour);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("EmpresaInfo", "Error parsing tour: " + document.getId(), e);
                        }
                    }
                    
                    android.util.Log.d("EmpresaInfo", "Tours filtered and added: " + toursGalleryList.size());
                    toursGalleryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("EmpresaInfo", "Error loading tours: " + e.getMessage(), e);
                    // No mostrar Toast para evitar confusión, solo log
                });
    }

    private void setupClickListeners() {
        tvVerMasReviews.setOnClickListener(v -> {
            // Aquí se podría navegar a una pantalla completa de reseñas
        });

        findViewById(R.id.layout_chat).setOnClickListener(v -> {
            if (empresaId != null && empresaNombre != null) {
                openOrCreateChat();
            } else {
                Toast.makeText(this, "Error: No se puede iniciar el chat", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void openOrCreateChat() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Toast.makeText(this, "Debes iniciar sesión para chatear", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // El chatId se forma concatenando clientId_adminId
        String chatId = currentUserId + "_" + empresaId;
        
        // Verificar si ya existe un chat
        db.collection("chats")
            .document(chatId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    // El chat ya existe, abrirlo
                    openChatConversation(chatId);
                } else {
                    // El chat no existe, crearlo
                    createAndOpenChat(chatId, currentUserId);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al verificar chat: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void createAndOpenChat(String chatId, String clientId) {
        // Obtener datos del cliente actual
        db.collection("usuarios")
            .document(clientId)
            .get()
            .addOnSuccessListener(clientDoc -> {
                if (clientDoc.exists()) {
                    String clientName = clientDoc.getString("nombresApellidos");
                    String clientPhoto = clientDoc.getString("photoUrl");
                    
                    // Obtener datos de la empresa (ya los tenemos cargados)
                    db.collection("usuarios")
                        .document(empresaId)
                        .get()
                        .addOnSuccessListener(adminDoc -> {
                            if (adminDoc.exists()) {
                                String adminName = adminDoc.getString("nombreEmpresa");
                                String adminPhoto = adminDoc.getString("photoUrl");
                                
                                // Crear documento del chat
                                java.util.Map<String, Object> chatData = new java.util.HashMap<>();
                                chatData.put("chatId", chatId);
                                chatData.put("clientId", clientId);
                                chatData.put("clientName", clientName != null ? clientName : "Cliente");
                                chatData.put("clientPhotoUrl", clientPhoto != null ? clientPhoto : "");
                                chatData.put("adminId", empresaId);
                                chatData.put("adminName", adminName != null ? adminName : empresaNombre);
                                chatData.put("adminPhotoUrl", adminPhoto != null ? adminPhoto : "");
                                chatData.put("active", true);
                                chatData.put("lastMessage", "");
                                chatData.put("lastMessageTime", com.google.firebase.Timestamp.now());
                                chatData.put("lastSenderId", "");
                                chatData.put("unreadCountClient", 0);
                                chatData.put("unreadCountAdmin", 0);
                                
                                db.collection("chats")
                                    .document(chatId)
                                    .set(chatData)
                                    .addOnSuccessListener(aVoid -> {
                                        openChatConversation(chatId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al crear chat: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    });
                            }
                        });
                }
            });
    }
    
    private void openChatConversation(String chatId) {
        Intent intent = new Intent(this, cliente_chat_conversation.class);
        intent.putExtra("admin_id", empresaId);
        intent.putExtra("admin_name", empresaNombre);
        // admin_photo_url se carga desde Firebase en cliente_chat_conversation
        startActivity(intent);
    }
}


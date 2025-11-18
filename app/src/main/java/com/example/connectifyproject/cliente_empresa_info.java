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
import com.example.connectifyproject.adapters.Cliente_ReviewsAdapter;
import com.example.connectifyproject.models.Cliente_Review;

public class cliente_empresa_info extends AppCompatActivity {

    private RecyclerView rvReviews, rvToursGallery;
    private Cliente_ReviewsAdapter reviewsAdapter;
    private Cliente_GalleryTourAdapter toursGalleryAdapter;
    private List<Cliente_Review> reviewsList;
    private List<Cliente_Tour> toursGalleryList;
    private TextView tvVerMasReviews;
    private TextView tvCompanyAddress, tvCompanyDescription, tvCompanyEmail, tvCompanyPhone;
    private ViewPager2 viewPagerEmpresaFotos;
    private TabLayout tabLayoutIndicators;
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
        tvCompanyAddress = findViewById(R.id.tv_company_address);
        tvCompanyDescription = findViewById(R.id.tv_company_description);
        tvCompanyEmail = findViewById(R.id.tv_company_email);
        tvCompanyPhone = findViewById(R.id.tv_company_phone);
        
        // Buscar ViewPager y TabLayout para fotos (si existen en el layout)
        // TODO: Descomentar cuando se agreguen al layout
        // viewPagerEmpresaFotos = findViewById(R.id.view_pager_empresa_fotos);
        // tabLayoutIndicators = findViewById(R.id.tab_layout_indicators);
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
        }
    }
    
    private void setupEmpresaPhotosGallery(List<String> fotosUrls) {
        // TODO: Implementar adaptador de ViewPager2 para fotos
        // Por ahora dejamos esto pendiente hasta implementar el slider
    }

    private void setupReviews() {
        reviewsList = new ArrayList<>();
        reviewsList.add(new Cliente_Review("María González", "5.0", "5", "15 Sep 2024",
                "Excelente experiencia! El tour fue increíble, el guía muy conocedor y los lugares visitados superaron mis expectativas. Definitivamente lo recomiendo."));
        reviewsList.add(new Cliente_Review("Carlos Mendoza", "4.8", "4.5", "12 Sep 2024",
                "Muy buen servicio, puntuales y organizados. Los lugares que visitamos fueron hermosos y aprendimos mucho sobre la historia de Lima."));
        reviewsList.add(new Cliente_Review("Ana Rodríguez", "5.0", "5", "10 Sep 2024",
                "Perfecto! Todo salió como estaba planeado. El transporte cómodo, el guía excelente y la comida deliciosa. Volveré a contratar sus servicios."));

        reviewsAdapter = new Cliente_ReviewsAdapter(reviewsList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewsAdapter);
    }

    private void setupToursGallery() {
        toursGalleryList = new ArrayList<>();
        toursGalleryList.add(new Cliente_Tour("1", "City Tour Lima",
            "Descubre la historia de Lima visitando sus lugares más emblemáticos",
            "Todo el día", 80.0, "Lima Histórica", 4.5f, "Lima Tours"));
        toursGalleryList.add(new Cliente_Tour("2", "Tour Gastronómico",
            "Experiencia culinaria única por los mejores restaurantes de la ciudad",
            "4 horas", 120.0, "Miraflores", 4.8f, "Lima Tours"));
        toursGalleryList.add(new Cliente_Tour("3", "Circuito Mágico",
            "Espectáculo de fuentes danzantes con luces y música",
            "3 horas", 60.0, "Parque de las Aguas", 4.3f, "Lima Tours"));
        toursGalleryList.add(new Cliente_Tour("4", "Barranco Bohemio",
            "Recorre el distrito más artístico y cultural de Lima",
            "5 horas", 90.0, "Barranco", 4.6f, "Lima Tours"));

        toursGalleryAdapter = new Cliente_GalleryTourAdapter(this, toursGalleryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvToursGallery.setLayoutManager(layoutManager);
        rvToursGallery.setAdapter(toursGalleryAdapter);
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
        intent.putExtra("chat_id", chatId);
        intent.putExtra("empresa_id", empresaId);
        intent.putExtra("empresa_nombre", empresaNombre);
        startActivity(intent);
    }
}


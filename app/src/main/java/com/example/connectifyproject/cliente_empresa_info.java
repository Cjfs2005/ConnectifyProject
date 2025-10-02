package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connectifyproject.models.Cliente_Tour;
import com.example.connectifyproject.adapters.Cliente_GalleryTourAdapter;
import com.google.android.material.appbar.MaterialToolbar;
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
    private TextView tvCompanyAddress, tvCompanyDescription, tvCompanyEmail, tvCompanyPhone; // removed tvCompanyName
    private Button btnChatEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_empresa_info);

        initViews();
        setupToolbar();
        loadCompanyInfo();
        setupReviews();
        setupToursGallery();
        setupClickListeners();
    }

    private void initViews() {
        rvReviews = findViewById(R.id.rv_reviews);
        rvToursGallery = findViewById(R.id.rv_tours_disponibles);
        tvVerMasReviews = findViewById(R.id.tv_ver_mas_reviews);
        // tv_company_name removed from layout; title goes in toolbar now
        tvCompanyAddress = findViewById(R.id.tv_company_address);
        tvCompanyDescription = findViewById(R.id.tv_company_description);
        tvCompanyEmail = findViewById(R.id.tv_company_email);
        tvCompanyPhone = findViewById(R.id.tv_company_phone);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadCompanyInfo() {
        String companyName = getIntent().getStringExtra("company_name");
        if (companyName == null) {
            companyName = "Lima Tours & Adventures";
        }
        // Prefer ActionBar title to avoid showing app name fallback
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(companyName);
        } else {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(companyName);
        }
        // Title text color is handled via XML theme/tint on the toolbar

        // Establecer información de la empresa
        tvCompanyAddress.setText("Av. José Larco 1232, Miraflores, Lima");
        tvCompanyDescription.setText("Somos una empresa especializada en tours culturales y gastronómicos por Lima. Con más de 10 años de experiencia, ofrecemos experiencias únicas que combinan historia, cultura y gastronomía peruana. Nuestros guías certificados te llevarán a descubrir los secretos mejor guardados de la capital del Perú.");
        tvCompanyEmail.setText("contacto@limatours.com");
        tvCompanyPhone.setText("+51 1 234-5678");
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
            Intent intent = new Intent(cliente_empresa_info.this, cliente_chat_conversation.class);
            intent.putExtra("empresa_nombre", "Lima Tours & Adventures");
            intent.putExtra("empresa_tipo", "empresa");
            startActivity(intent);
        });
    }
}


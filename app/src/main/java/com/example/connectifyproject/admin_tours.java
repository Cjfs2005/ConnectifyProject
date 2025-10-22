package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.AdminToursViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;
import com.google.android.material.tabs.TabLayout;
import com.example.connectifyproject.storage.AdminStorage;
import com.example.connectifyproject.storage.AdminStorage.TourDraft;

import java.util.ArrayList;
import java.util.List;

public class admin_tours extends AppCompatActivity {
    private AdminToursViewBinding binding;
    private ToursAdapter toursAdapter;
    private List<TourItem> toursList;
    private String currentTab = "publicados";
    private final AdminStorage adminStorage = new AdminStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminToursViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar TabLayout
        setupTabs();

        // Configurar datos iniciales
        loadTours("publicados");

        // Configurar bottom navigation
        setupBottomNavigation();

        // Configurar botón crear tour
        binding.fabCreateTour.setOnClickListener(v -> {
            Intent intent = new Intent(this, admin_create_tour.class);
            startActivity(intent);
        });
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
        toursList.clear();
        
        switch (estado) {
            case "publicados":
                // Tours publicados con toda la información completa
                toursList.add(new TourItem(
                    "Exploración por el centro de Lima",
                    "Jul 15",
                    "Publicado",
                    R.drawable.tour_lima_centro,
                    true,
                    null
                ));
                toursList.add(new TourItem(
                    "Casonas antiguas",
                    "Ago 5",
                    "Publicado",
                    R.drawable.tour_casonas,
                    true,
                    null
                ));
                toursList.add(new TourItem(
                    "Montaña Huascarán",
                    "Sep 1 - Sep 5",
                    "Publicado",
                    R.drawable.tour_huascaran,
                    true,
                    null
                ));
                break;
                
            case "borrador":
                // Cargar borradores reales desde almacenamiento
                List<TourDraft> drafts = adminStorage.listDrafts(this);
                if (drafts.isEmpty()) {
                    toursList.add(new TourItem(
                        "Sin borradores",
                        "",
                        "No hay tours en borrador",
                        R.drawable.tour_lima_centro,
                        false,
                        null
                    ));
                } else {
                    for (TourDraft d : drafts) {
                        String title = (d.title != null && !d.title.isEmpty()) ? d.title : "Borrador sin título";
                        String fecha = (d.date != null && !d.date.isEmpty()) ? d.date : "Fecha no definida";
                        toursList.add(new TourItem(
                            title,
                            fecha,
                            "Borrador",
                            R.drawable.tour_lima_centro,
                            false,
                            d.id
                        ));
                    }
                }
                break;
                
            case "cancelados":
                // Tours cancelados
                toursList.add(new TourItem(
                    "Tour cancelado ejemplo",
                    "Oct 1",
                    "Cancelado",
                    R.drawable.tour_lima_centro,
                    false,
                    null
                ));
                break;
        }
        
        toursAdapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("tours");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }

    // Clase interna para los datos del tour
    public static class TourItem {
        private String titulo;
        private String fecha;
        private String estado;
        private int imagenResource;
        private boolean esPublicado;
        private String draftId; // null si no es borrador

        public TourItem(String titulo, String fecha, String estado, int imagenResource, boolean esPublicado, String draftId) {
            this.titulo = titulo;
            this.fecha = fecha;
            this.estado = estado;
            this.imagenResource = imagenResource;
            this.esPublicado = esPublicado;
            this.draftId = draftId;
        }

        // Getters
        public String getTitulo() { return titulo; }
        public String getFecha() { return fecha; }
        public String getEstado() { return estado; }
        public int getImagenResource() { return imagenResource; }
        public boolean isEsPublicado() { return esPublicado; }
        public String getDraftId() { return draftId; }
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
            private ImageView ivTourImage;
            private TextView tvTitulo;
            private TextView tvFecha;
            private TextView tvEstado;

            public TourViewHolder(@NonNull View itemView) {
                super(itemView);
                ivTourImage = itemView.findViewById(R.id.iv_tour_image);
                tvTitulo = itemView.findViewById(R.id.tv_titulo);
                tvFecha = itemView.findViewById(R.id.tv_fecha);
                tvEstado = itemView.findViewById(R.id.tv_estado);
            }

            public void bind(TourItem tour) {
                ivTourImage.setImageResource(tour.getImagenResource());
                tvTitulo.setText(tour.getTitulo());
                tvFecha.setText(tour.getFecha());
                tvEstado.setText(tour.getEstado());

                // Configurar colores según el estado
                if (tour.isEsPublicado()) {
                    tvEstado.setTextColor(getColor(R.color.success_500));
                } else {
                    tvEstado.setTextColor(getColor(R.color.text_secondary));
                }

                // Click listener para ir a detalles del tour
                itemView.setOnClickListener(v -> {
                    if (tour.getDraftId() != null) {
                        Intent intent = new Intent(admin_tours.this, admin_create_tour.class);
                        intent.putExtra("draft_id", tour.getDraftId());
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(admin_tours.this, admin_tour_details.class);
                        intent.putExtra("tour_titulo", tour.getTitulo());
                        intent.putExtra("tour_estado", tour.getEstado());
                        intent.putExtra("tour_es_publicado", tour.isEsPublicado());
                        startActivity(intent);
                    }
                });
            }
        }
    }
}
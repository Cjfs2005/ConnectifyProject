package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.AdminSelectGuideViewBinding;
import com.example.connectifyproject.ui.admin.AdminBottomNavFragment;

import java.util.ArrayList;
import java.util.List;

public class admin_select_guide extends AppCompatActivity {
    private AdminSelectGuideViewBinding binding;
    private GuideAdapter guideAdapter;
    private List<GuideItem> allGuides;
    private List<GuideItem> filteredGuides;
    private String tourTitulo;
    private String tourEstado;

    // Clase para representar un guía
    public static class GuideItem {
        public String name;
        public double rating;
        public int tourCount;
        public String languages;
        public int profileImage;

        public GuideItem(String name, double rating, int tourCount, String languages, int profileImage) {
            this.name = name;
            this.rating = rating;
            this.tourCount = tourCount;
            this.languages = languages;
            this.profileImage = profileImage;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminSelectGuideViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener datos del Intent
        tourTitulo = getIntent().getStringExtra("tour_titulo");
        tourEstado = getIntent().getStringExtra("tour_estado");

        // Configurar toolbar
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Inicializar datos
        initializeGuides();
        setupRecyclerView();
        setupSearch();
        setupLanguageFilters();
        setupBottomNavigation();
    }

    private void initializeGuides() {
        allGuides = new ArrayList<>();
        allGuides.add(new GuideItem("Elena Rodriguez", 4.8, 120, "Inglés, Español", R.drawable.tour_lima_centro));
        allGuides.add(new GuideItem("Marcus Johnson", 4.7, 150, "Francés, Español", R.drawable.tour_casonas));
        allGuides.add(new GuideItem("Sophia Chen", 4.9, 110, "Inglés, Mandarín", R.drawable.tour_huascaran));
        allGuides.add(new GuideItem("Carlos Martinez", 4.6, 95, "Español, Portugués", R.drawable.tour_lima_centro));
        allGuides.add(new GuideItem("Ana Torres", 4.8, 130, "Inglés, Español, Italiano", R.drawable.tour_casonas));

        filteredGuides = new ArrayList<>(allGuides);
    }

    private void setupRecyclerView() {
        guideAdapter = new GuideAdapter(convertToAdapterGuides(filteredGuides), this::onGuideSelected);
        binding.recyclerViewGuides.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewGuides.setAdapter(guideAdapter);
    }

    private List<admin_select_guide.GuideItem> convertToAdapterGuides(List<GuideItem> guides) {
        List<admin_select_guide.GuideItem> adapterGuides = new ArrayList<>();
        for (GuideItem guide : guides) {
            adapterGuides.add(new admin_select_guide.GuideItem(
                guide.name, guide.rating, guide.tourCount, guide.languages, guide.profileImage
            ));
        }
        return adapterGuides;
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
                    guide.languages.toLowerCase().contains(query.toLowerCase())) {
                    filteredGuides.add(guide);
                }
            }
        }
        // Actualizar el adaptador con la nueva lista filtrada
        guideAdapter = new GuideAdapter(convertToAdapterGuides(filteredGuides), this::onGuideSelected);
        binding.recyclerViewGuides.setAdapter(guideAdapter);
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
        if (binding.chipItaliano.isChecked()) selectedLanguages.add("Italiano");
        if (binding.chipMandarin.isChecked()) selectedLanguages.add("Mandarín");
        if (binding.chipPortugues.isChecked()) selectedLanguages.add("Portugués");

        filteredGuides.clear();
        
        if (selectedLanguages.isEmpty()) {
            // Si no hay filtros seleccionados, mostrar todos los guías
            filteredGuides.addAll(allGuides);
        } else {
            // Filtrar guías que hablen al menos uno de los idiomas seleccionados
            for (GuideItem guide : allGuides) {
                for (String language : selectedLanguages) {
                    if (guide.languages.contains(language)) {
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
            // Actualizar el adaptador con la nueva lista filtrada
            guideAdapter = new GuideAdapter(convertToAdapterGuides(filteredGuides), this::onGuideSelected);
            binding.recyclerViewGuides.setAdapter(guideAdapter);
        }
    }

    private void onGuideSelected(GuideItem guide) {
        // Navegar a la propuesta de pago
        Intent intent = new Intent(this, admin_payment_proposal.class);
        intent.putExtra("tour_titulo", tourTitulo);
        intent.putExtra("guide_name", guide.name);
        intent.putExtra("guide_rating", guide.rating);
        intent.putExtra("guide_languages", guide.languages);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        AdminBottomNavFragment bottomNavFragment = AdminBottomNavFragment.newInstance("tours");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomNavContainer, bottomNavFragment);
        transaction.commit();
    }
}

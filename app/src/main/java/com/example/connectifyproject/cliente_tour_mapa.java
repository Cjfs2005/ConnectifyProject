package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.adapters.Cliente_ItinerarioAdapter;
import com.example.connectifyproject.models.Cliente_ItinerarioItem;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class cliente_tour_mapa extends AppCompatActivity implements Cliente_ItinerarioAdapter.OnItinerarioItemClickListener {

    private MaterialToolbar toolbar;
    private ImageView ivMapPlaceholder;
    private RecyclerView rvItinerario;
    private Cliente_ItinerarioAdapter itinerarioAdapter;
    private List<Cliente_ItinerarioItem> itinerarioItems;
    
    private String tourId, tourTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_tour_mapa);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadItinerarioData();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        tourId = intent.getStringExtra("tour_id");
        tourTitle = intent.getStringExtra("tour_title");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivMapPlaceholder = findViewById(R.id.iv_map_placeholder);
        rvItinerario = findViewById(R.id.rv_itinerario);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (tourTitle != null) {
                getSupportActionBar().setTitle("Ruta: " + tourTitle);
            }
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        itinerarioItems = new ArrayList<>();
        itinerarioAdapter = new Cliente_ItinerarioAdapter(this, itinerarioItems);
        itinerarioAdapter.setOnItinerarioItemClickListener(this);
        
        rvItinerario.setLayoutManager(new LinearLayoutManager(this));
        rvItinerario.setAdapter(itinerarioAdapter);
    }

    private void loadItinerarioData() {
        // Datos hardcodeados del itinerario basados en el mockup
        itinerarioItems.add(new Cliente_ItinerarioItem("1", "Plaza de Armas", "1hrs 30 min", "09:00",
                "Centro de este importante centro histórico alrededor de diversos edificios representativos...", 
                -12.0464, -77.0428));
        
        itinerarioItems.add(new Cliente_ItinerarioItem("2", "Catedral", "1hrs", "10:45",
                "Imponente catedral centro histórico alrededor de diversos edificios representativos...", 
                -12.0458, -77.0428));
        
        itinerarioItems.add(new Cliente_ItinerarioItem("3", "Convento San Francisco", "1 hrs", "12:00",
                "Histórico convento centro histórico alrededor de diversos edificios representativos...", 
                -12.0469, -77.0282));

        // Mark the last item
        if (!itinerarioItems.isEmpty()) {
            itinerarioItems.get(itinerarioItems.size() - 1).setLastItem(true);
        }

        itinerarioAdapter.notifyDataSetChanged();
    }

    @Override
    public void onVerMasClick(Cliente_ItinerarioItem item) {
        // Show detailed dialog or navigate to details
        showItinerarioItemDetails(item);
    }

    @Override
    public void onItemClick(Cliente_ItinerarioItem item) {
        // Could focus on map location if Google Maps is integrated
        // For now, show a toast
        showItinerarioItemDetails(item);
    }

    private void showItinerarioItemDetails(Cliente_ItinerarioItem item) {
        // Create and show a detailed dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(item.getPlaceName())
                .setMessage("Horario: " + item.getVisitTime() + "\n" +
                           "Duración: " + item.getDuration() + "\n\n" +
                           "Descripción:\n" + item.getDescription() + "\n\n" +
                           "Este es un lugar importante dentro del recorrido turístico que ofrece una experiencia única para los visitantes.")
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
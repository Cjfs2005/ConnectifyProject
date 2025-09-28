package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.databinding.GuiaToursOfertasBinding;
import com.example.connectifyproject.fragment.GuiaFilterDialogFragment;
import com.example.connectifyproject.model.GuiaItem;
import com.example.connectifyproject.model.GuiaTour;
import com.example.connectifyproject.ui.guia.GuiaTourAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class guia_tours_ofertas extends AppCompatActivity implements GuiaFilterDialogFragment.FilterListener {
    private GuiaToursOfertasBinding binding;
    private GuiaTourAdapter adapter;
    private List<GuiaTour> allTours;
    private List<GuiaItem> displayedItems = new ArrayList<>(); // Inicializar aquí para evitar null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaToursOfertasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hardcoded data (updated dates near current: September 2025+)
        allTours = new ArrayList<>();
        allTours.add(new GuiaTour("Tour por Centro Histórico de Lima", "Lima, Lima", 250, "9 horas", "Español,Inglés", "12:00", "23/10/2025",
                "Visita Plaza de Armas, Catedral, etc.", "Pago neto, Almuerzo incluido, Certificado", "9:00 a.m. - 5:00 p.m.", "Avenida del Sol 1457, Playa Serena, Lima",
                "LimaTours SAC", "Plaza de Armas (1hrs30min), Catedral(1hrs), Convento San Francisco(1hrs), Museo Larco(2hrs)", "1 año como guía turístico", "Puntualidad en puntos de encuentro", true, true));
        allTours.add(new GuiaTour("Tour por Centro Histórico de Lima", "Lima, Lima", 280, "8 horas", "Español,Francés", "11:00", "23/10/2025",
                "Explora monumentos coloniales.", "Pago neto, Transporte incluido", "8:00 a.m. - 4:00 p.m.", "Plaza Mayor, Lima",
                "PeruGuides Inc", "Catedral (2hrs), Museo (1hr30min)", "2 años experiencia", "Alta puntualidad requerida", true, false));
        allTours.add(new GuiaTour("Tour por Centro Histórico de Lima", "Lima, Lima", 250, "9 horas", "Español,Inglés", "12:00", "23/10/2025",
                "Visita Plaza de Armas, Catedral, etc.", "Pago neto, Almuerzo incluido, Certificado", "9:00 a.m. - 5:00 p.m.", "Avenida del Sol 1457, Playa Serena, Lima",
                "LimaTours SAC", "Plaza de Armas (1hrs30min), Catedral(1hrs), Convento San Francisco(1hrs), Museo Larco(2hrs)", "1 año como guía turístico", "Puntualidad en puntos de encuentro", true, true));
        allTours.add(new GuiaTour("Tour por Centro Histórico de Lima", "Lima, Lima", 280, "8 horas", "Español,Francés", "11:00", "23/10/2025",
                "Explora monumentos coloniales.", "Pago neto, Transporte incluido", "8:00 a.m. - 4:00 p.m.", "Plaza Mayor, Lima",
                "PeruGuides Inc", "Catedral (2hrs), Museo (1hr30min)", "2 años experiencia", "Alta puntualidad requerida", true, false));

        // Setup RecyclerView PRIMERO
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuiaTourAdapter(this, displayedItems); // Pasar lista vacía inicialmente
        binding.recyclerView.setAdapter(adapter);

        // Ahora llamar al filtro inicial
        onApplyFilters(null, null, null, null, null);

        // Filter button
        binding.filterButton.setOnClickListener(v -> {
            GuiaFilterDialogFragment dialog = new GuiaFilterDialogFragment();
            dialog.show(getSupportFragmentManager(), "guia_filter_dialog");
        });

        // Bottom Navigation original (comentado como solicitado)
        /*
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_ofertas);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class)); // Placeholder, renombrado
                return true;
            } else if (id == R.id.nav_ofertas) {
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, guia_assigned_tours.class)); // Renombrado
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, guia_perfil.class)); // Placeholder, renombrado
                return true;
            }
            return false;
        });
        */

        // Nuevo Bottom Navigation con Toast
        BottomNavigationView newBottomNav = binding.bottomNav;
        newBottomNav.setSelectedItemId(R.id.nav_ofertas);
        newBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class)); // Placeholder, renombrado
                return true;
            } else if (id == R.id.nav_ofertas) {
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, guia_assigned_tours.class)); // Renombrado
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, guia_perfil.class)); // Placeholder, renombrado
                return true;
            }
            return false;
        });
    }

    @Override
    public void onApplyFilters(String dateFrom, String dateTo, String amount, String duration, String languages) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat storedFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        List<GuiaTour> filteredTours = new ArrayList<>();
        for (GuiaTour tour : allTours) {
            boolean matches = true;
            try {
                Date tourDate = storedFormat.parse(tour.getDate());
                if (dateFrom != null && !dateFrom.isEmpty()) {
                    Date fromDate = inputFormat.parse(dateFrom);
                    if (tourDate.before(fromDate)) matches = false;
                }
                if (dateTo != null && !dateTo.isEmpty()) {
                    Date toDate = inputFormat.parse(dateTo);
                    if (tourDate.after(toDate)) matches = false;
                }
            } catch (ParseException e) {
                matches = false; // Invalid date skips
            }
            if (amount != null && !amount.isEmpty()) {
                try {
                    double maxAmount = Double.parseDouble(amount.replaceAll("[^0-9.]", "")); // Clean S/.
                    if (tour.getPrice() > maxAmount) matches = false;
                } catch (NumberFormatException e) {
                    // Ignore invalid
                }
            }
            if (duration != null && !duration.isEmpty() && !tour.getDuration().toLowerCase().contains(duration.toLowerCase())) matches = false;
            if (languages != null && !languages.isEmpty() && !tour.getLanguages().toLowerCase().contains(languages.toLowerCase())) matches = false;
            if (matches) filteredTours.add(tour);
        }

        // Group by date with headers
        displayedItems.clear(); // Limpiar la lista existente
        String currentDate = null;
        for (GuiaTour tour : filteredTours) {
            if (!tour.getDate().equals(currentDate)) {
                currentDate = tour.getDate();
                String header = (currentDate.equals("23/10/2025") ? "Hoy, 23 de Octubre" : "Mañana, 24 de Octubre"); // Dynamic based on date
                displayedItems.add(new GuiaItem(header));
            }
            displayedItems.add(new GuiaItem(tour));
        }

        // Show no results if empty
        if (displayedItems.isEmpty()) {
            binding.noResultsView.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.noResultsView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.updateItems(displayedItems); // Ahora adapter no es null
    }
}
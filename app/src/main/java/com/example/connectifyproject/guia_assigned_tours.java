package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.GuiaAssignedToursBinding;
import com.example.connectifyproject.fragment.GuiaDateFilterDialogFragment;
import com.example.connectifyproject.model.GuiaAssignedItem;
import com.example.connectifyproject.model.GuiaAssignedTour;
import com.example.connectifyproject.ui.guia.GuiaAssignedTourAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class guia_assigned_tours extends AppCompatActivity implements GuiaDateFilterDialogFragment.FilterListener {
    private GuiaAssignedToursBinding binding;
    private GuiaAssignedTourAdapter adapter;
    private List<GuiaAssignedTour> allAssignedTours = new ArrayList<>();
    private List<GuiaAssignedItem> displayedItems = new ArrayList<>();
    private List<GuiaAssignedTour> originalTours = new ArrayList<>();
    private boolean isLoading = false;
    private String currentDateFrom, currentDateTo, currentAmount, currentDuration, currentLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaAssignedToursBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hardcoded original data, duplicated for verification, limited to generate up to 20 items
        List<String> itinerario1 = new ArrayList<>();
        itinerario1.add("1. Plaza de Armas – 09:00 am");
        itinerario1.add("2. Catedral de Lima – 09:30 am");
        itinerario1.add("3. Convento San Francisco – 11:00 am");
        itinerario1.add("4. Museo Larco – 01:00 pm");
        originalTours.add(new GuiaAssignedTour("City Tour Histórico Lima", "LimaTours SAC", "02/10/2025 - 09:00 am", "6 h", 12, "En Curso", "02/10/2025", "Español, Inglés", "Desayuno, Almuerzo", itinerario1));
        List<String> itinerario2 = new ArrayList<>();
        itinerario2.add("1. Plaza de Armas – 09:00 am");
        itinerario2.add("2. Catedral de Lima – 09:30 am");
        itinerario2.add("3. Convento San Francisco – 11:00 am");
        itinerario2.add("4. Museo Larco – 01:00 pm");
        originalTours.add(new GuiaAssignedTour("Tour por Centro Histórico de Lima", "LimaTours SAC", "03/10/2025 - 09:00 am", "6 h", 12, "Pendiente", "03/10/2025", "Español, Inglés", "Desayuno, Almuerzo", itinerario2));
        List<String> itinerario3 = new ArrayList<>();
        itinerario3.add("1. Plaza de Armas – 09:00 am");
        itinerario3.add("2. Catedral de Lima – 09:30 am");
        itinerario3.add("3. Convento San Francisco – 11:00 am");
        itinerario3.add("4. Museo Larco – 01:00 pm");
        originalTours.add(new GuiaAssignedTour("Tour por Centro Histórico de Lima", "LimaTours SAC", "04/10/2025 - 09:00 am", "6 h", 12, "Pendiente", "04/10/2025", "Español, Francés", "Almuerzo", itinerario3));
        originalTours.add(new GuiaAssignedTour("City Tour Histórico Lima", "LimaTours SAC", "02/10/2025 - 09:00 am", "6 h", 12, "En Curso", "02/10/2025", "Español, Inglés", "Desayuno, Almuerzo", itinerario1));
        originalTours.add(new GuiaAssignedTour("Tour por Centro Histórico de Lima", "LimaTours SAC", "03/10/2025 - 09:00 am", "6 h", 12, "Pendiente", "03/10/2025", "Español, Inglés", "Desayuno, Almuerzo", itinerario2));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuiaAssignedTourAdapter(this, displayedItems);
        binding.recyclerView.setAdapter(adapter);

        // Add scroll listener for loading more as scroll
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && lastVisibleItem >= totalItemCount - 1 && dy > 0 && allAssignedTours.size() < 20) {
                    loadMore();
                }
            }
        });

        // Load initial page after adapter is set
        loadMore();

        binding.filterButton.setOnClickListener(v -> {
            GuiaDateFilterDialogFragment dialog = new GuiaDateFilterDialogFragment();
            dialog.show(getSupportFragmentManager(), "guia_date_filter_dialog");
        });

        binding.btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_notificaciones.class);
            intent.putExtra("origin_activity", "guia_assigned_tours");
            startActivity(intent);
        });

        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_tours);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class));
                return true;
            } else if (id == R.id.nav_ofertas) {
                startActivity(new Intent(this, guia_tours_ofertas.class));
                return true;
            } else if (id == R.id.nav_tours) {
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, guia_perfil.class));
                return true;
            }
            return false;
        });
    }

    private void loadMore() {
        isLoading = true;
        int offsetDays = allAssignedTours.size() / originalTours.size(); // Correct offset calculation
        List<GuiaAssignedTour> moreTours = new ArrayList<>();
        for (GuiaAssignedTour t : originalTours) {
            if (allAssignedTours.size() + moreTours.size() >= 20) break;
            List<String> newItinerario = new ArrayList<>(t.getItinerario());
            String newDate = addDaysToDate(t.getDate(), offsetDays);
            String newInitio = newDate + " - " + t.getInitio().split(" - ")[1];
            GuiaAssignedTour copy = new GuiaAssignedTour(
                    t.getName(),
                    t.getEmpresa(),
                    newInitio,
                    t.getDuration(),
                    t.getClients(),
                    t.getStatus(),
                    newDate,
                    t.getLanguages(),
                    t.getServices(),
                    newItinerario
            );
            moreTours.add(copy);
        }
        allAssignedTours.addAll(moreTours);
        onApplyFilters(currentDateFrom, currentDateTo, currentAmount, currentDuration, currentLanguages);
        isLoading = false;
    }

    private String addDaysToDate(String dateStr, int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_YEAR, days);
            return sdf.format(cal.getTime());
        } catch (ParseException e) {
            return dateStr;
        }
    }

    @Override
    public void onApplyFilters(String dateFrom, String dateTo, String amount, String duration, String languages) {
        this.currentDateFrom = dateFrom;
        this.currentDateTo = dateTo;
        this.currentAmount = amount;
        this.currentDuration = duration;
        this.currentLanguages = languages;

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat storedFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        List<GuiaAssignedTour> filteredTours = new ArrayList<>();
        for (GuiaAssignedTour tour : allAssignedTours) {
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
                matches = false;
            }
            if (duration != null && !duration.isEmpty() && !tour.getDuration().toLowerCase().contains(duration.toLowerCase())) matches = false;
            if (languages != null && !languages.isEmpty() && !tour.getLanguages().toLowerCase().contains(languages.toLowerCase())) matches = false;
            if (matches) filteredTours.add(tour);
        }

        displayedItems.clear();
        String currentDate = null;
        for (GuiaAssignedTour tour : filteredTours) {
            if (!tour.getDate().equals(currentDate)) {
                currentDate = tour.getDate();
                String header = getFormattedHeader(currentDate);
                displayedItems.add(new GuiaAssignedItem(header));
            }
            displayedItems.add(new GuiaAssignedItem(tour));
        }

        if (displayedItems.isEmpty()) {
            binding.noResultsView.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.noResultsView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.updateItems(displayedItems);
    }

    private String getFormattedHeader(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date tourDate = sdf.parse(date);
            Date today = sdf.parse("02/10/2025"); // Current date
            if (sdf.format(today).equals(date)) {
                return "Hoy, " + date.replace("/", " de ");
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(today);
                cal.add(Calendar.DAY_OF_YEAR, 1);
                if (sdf.format(cal.getTime()).equals(date)) {
                    return "Mañana, " + date.replace("/", " de ");
                }
                return date.replace("/", " de ");
            }
        } catch (ParseException e) {
            return date;
        }
    }
}
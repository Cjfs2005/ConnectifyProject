package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.GuiaToursOfertasBinding;
import com.example.connectifyproject.fragment.GuiaFilterDialogFragment;
import com.example.connectifyproject.model.GuiaItem;
import com.example.connectifyproject.model.GuiaTour;
import com.example.connectifyproject.ui.guia.GuiaTourAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class guia_tours_ofertas extends AppCompatActivity implements GuiaFilterDialogFragment.FilterListener {
    private GuiaToursOfertasBinding binding;
    private GuiaTourAdapter adapter;
    private List<GuiaTour> allTours = new ArrayList<>();
    private List<GuiaItem> displayedItems = new ArrayList<>();
    private List<GuiaTour> originalTours = new ArrayList<>();
    private boolean isLoading = false;
    private String currentDateFrom, currentDateTo, currentAmount, currentDuration, currentLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaToursOfertasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hardcoded original data, duplicated for verification, limited to generate up to 20 items
        originalTours.add(new GuiaTour("Tour por Centro Histórico de Lima", "Lima, Lima", 250, "9 horas", "Español,Inglés", "12:00", "02/10/2025",
                "Visita Plaza de Armas, Catedral, etc.", "Pago neto, Almuerzo incluido, Certificado", "9:00 a.m. - 5:00 p.m.", "Avenida del Sol 1457, Playa Serena, Lima",
                "LimaTours SAC", "Plaza de Armas (1hrs30min), Catedral(1hrs), Convento San Francisco(1hrs), Museo Larco(2hrs)", "1 año como guía turístico", "Puntualidad en puntos de encuentro", true, true));
        originalTours.add(new GuiaTour("Tour por Centro Histórico de Lima", "Lima, Lima", 280, "8 horas", "Español,Francés", "11:00", "02/10/2025",
                "Explora monumentos coloniales.", "Pago neto, Transporte incluido", "8:00 a.m. - 4:00 p.m.", "Plaza Mayor, Lima",
                "PeruGuides Inc", "Catedral (2hrs), Museo (1hr30min)", "2 años experiencia", "Alta puntualidad requerida", true, false));
        originalTours.add(new GuiaTour("Tour por Centro Histórico de Lima", "Lima, Lima", 250, "9 horas", "Español,Inglés", "12:00", "02/10/2025",
                "Visita Plaza de Armas, Catedral, etc.", "Pago neto, Almuerzo incluido, Certificado", "9:00 a.m. - 5:00 p.m.", "Avenida del Sol 1457, Playa Serena, Lima",
                "LimaTours SAC", "Plaza de Armas (1hrs30min), Catedral(1hrs), Convento San Francisco(1hrs), Museo Larco(2hrs)", "1 año como guía turístico", "Puntualidad en puntos de encuentro", true, true));
        originalTours.add(new GuiaTour("Tour por Centro Histórico de Lima", "Lima, Lima", 280, "8 horas", "Español,Francés", "11:00", "02/10/2025",
                "Explora monumentos coloniales.", "Pago neto, Transporte incluido", "8:00 a.m. - 4:00 p.m.", "Plaza Mayor, Lima",
                "PeruGuides Inc", "Catedral (2hrs), Museo (1hr30min)", "2 años experiencia", "Alta puntualidad requerida", true, false));
        originalTours.add(new GuiaTour("Tour por Centro Histórico de Lima", "Lima, Lima", 250, "9 horas", "Español,Inglés", "12:00", "02/10/2025",
                "Visita Plaza de Armas, Catedral, etc.", "Pago neto, Almuerzo incluido, Certificado", "9:00 a.m. - 5:00 p.m.", "Avenida del Sol 1457, Playa Serena, Lima",
                "LimaTours SAC", "Plaza de Armas (1hrs30min), Catedral(1hrs), Convento San Francisco(1hrs), Museo Larco(2hrs)", "1 año como guía turístico", "Puntualidad en puntos de encuentro", true, true));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuiaTourAdapter(this, displayedItems);
        binding.recyclerView.setAdapter(adapter);

        // Add scroll listener for loading more as scroll
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && lastVisibleItem >= totalItemCount - 1 && dy > 0 && allTours.size() < 20) {
                    loadMore();
                }
            }
        });

        // Load initial page after adapter is set
        loadMore();

        binding.filterButton.setOnClickListener(v -> {
            GuiaFilterDialogFragment dialog = new GuiaFilterDialogFragment();
            dialog.show(getSupportFragmentManager(), "guia_filter_dialog");
        });

        binding.btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_notificaciones.class);
            intent.putExtra("origin_activity", "guia_tours_ofertas");
            startActivity(intent);
        });

        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_ofertas);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, guia_historial.class));
                return true;
            } else if (id == R.id.nav_ofertas) {
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, guia_assigned_tours.class));
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
        int offsetDays = allTours.size() / originalTours.size(); // Correct offset calculation
        List<GuiaTour> moreTours = new ArrayList<>();
        for (GuiaTour t : originalTours) {
            if (allTours.size() + moreTours.size() >= 20) break;
            String newDate = addDaysToDate(t.getDate(), offsetDays);
            GuiaTour copy = new GuiaTour(
                    t.getName(),
                    t.getLocation(),
                    t.getPrice(),
                    t.getDuration(),
                    t.getLanguages(),
                    t.getStartTime(),
                    newDate,
                    t.getDescription(),
                    t.getBenefits(),
                    t.getSchedule(),
                    t.getMeetingPoint(),
                    t.getEmpresa(),
                    t.getItinerario(),
                    t.getExperienciaMinima(),
                    t.getPuntualidad(),
                    t.isTransporteIncluido(),
                    t.isAlmuerzoIncluido()
            );
            moreTours.add(copy);
        }
        allTours.addAll(moreTours);
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
                matches = false;
            }
            if (amount != null && !amount.isEmpty()) {
                try {
                    double maxAmount = Double.parseDouble(amount.replaceAll("[^0-9.]", ""));
                    if (tour.getPrice() > maxAmount) matches = false;
                } catch (NumberFormatException e) {
                }
            }
            if (duration != null && !duration.isEmpty() && !tour.getDuration().toLowerCase().contains(duration.toLowerCase())) matches = false;
            if (languages != null && !languages.isEmpty() && !tour.getLanguages().toLowerCase().contains(languages.toLowerCase())) matches = false;
            if (matches) filteredTours.add(tour);
        }

        displayedItems.clear();
        String currentDate = null;
        for (GuiaTour tour : filteredTours) {
            if (!tour.getDate().equals(currentDate)) {
                currentDate = tour.getDate();
                String header = getFormattedHeader(currentDate);
                displayedItems.add(new GuiaItem(header));
            }
            displayedItems.add(new GuiaItem(tour));
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
package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.GuiaToursOfertasBinding;
import com.example.connectifyproject.fragment.GuiaFilterDialogFragment;
import com.example.connectifyproject.model.GuiaItem;
import com.example.connectifyproject.model.GuiaTour;
import com.example.connectifyproject.models.OfertaTour;
import com.example.connectifyproject.service.GuiaNotificationService;
import com.example.connectifyproject.services.TourFirebaseService;
import com.example.connectifyproject.storage.GuiaPreferencesManager;
import com.example.connectifyproject.ui.guia.GuiaTourAdapter;
import com.example.connectifyproject.utils.TourDataSeeder;
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
    
    // Firebase Services
    private TourFirebaseService tourService;
    private List<OfertaTour> ofertasFirebase = new ArrayList<>();
    
    // Servicios para notificaciones
    private GuiaNotificationService notificationService;
    private GuiaPreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaToursOfertasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar servicios Firebase
        tourService = new TourFirebaseService();
        
        // Inicializar servicios para notificaciones
        notificationService = new GuiaNotificationService(this);
        preferencesManager = new GuiaPreferencesManager(this);

        // Configurar toolbar sin botón de retroceso (pantalla principal)
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ofertas de Tours");
        }
        
        binding.toolbar.setOnLongClickListener(v -> {
            testLocationReminder();
            return true;
        });

        // Configurar RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuiaTourAdapter(this, displayedItems);
        binding.recyclerView.setAdapter(adapter);

        // SOLO EJECUTAR UNA VEZ para crear datos de prueba
        //TourDataSeeder.crearOfertasDePrueba(); // Descomenta para crear ofertas de prueba
        
        // Cargar ofertas desde Firebase
        cargarOfertasDesdeFirebase();

        // Add scroll listener for loading more as scroll
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Deshabilitado el scroll infinito por ahora
                // Se carga todo desde Firebase de una vez
            }
        });

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

    /**
     * Cargar ofertas desde Firebase
     */
    private void cargarOfertasDesdeFirebase() {
        binding.noResultsView.setVisibility(View.GONE);
        
        tourService.getOfertasDisponibles(new TourFirebaseService.TourCallback() {
            @Override
            public void onSuccess(List<OfertaTour> ofertas) {
                runOnUiThread(() -> {
                    ofertasFirebase = ofertas;
                    // Convertir ofertas de Firebase a modelo GuiaTour existente
                    allTours.clear();
                    for (OfertaTour oferta : ofertas) {
                        GuiaTour tour = convertirOfertaToGuiaTour(oferta);
                        allTours.add(tour);
                    }
                    
                    // Aplicar filtros actuales
                    onApplyFilters(currentDateFrom, currentDateTo, currentAmount, currentDuration, currentLanguages);
                    
                    if (allTours.isEmpty()) {
                        mostrarMensajeNoOfertas();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_tours_ofertas.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    mostrarMensajeNoOfertas();
                });
            }
        });
    }
    
    /**
     * Convertir OfertaTour de Firebase a GuiaTour para compatibilidad
     */
    private GuiaTour convertirOfertaToGuiaTour(OfertaTour oferta) {
        // Extraer ubicación del primer punto del itinerario
        String location = "Lima, Lima"; // Default
        if (oferta.getItinerario() != null && !oferta.getItinerario().isEmpty()) {
            Object lugarObj = oferta.getItinerario().get(0).get("lugar");
            if (lugarObj != null) {
                location = lugarObj.toString() + ", Lima";
            }
        }
        
        // Convertir PAGO AL GUÍA (no precio del tour)
        int pagoGuia = (int) oferta.getPagoGuia();
        
        // Convertir idiomas de List<String> a String separado por comas
        String idiomas = oferta.getIdiomasTexto();
        
        // Horario mejorado (solo hora de inicio)
        String horario = "Inicio: " + oferta.getHoraInicio();
        
        // Punto de encuentro (primer lugar del itinerario)
        String puntoEncuentro = "Por definir";
        if (oferta.getItinerario() != null && !oferta.getItinerario().isEmpty()) {
            Object lugarObj = oferta.getItinerario().get(0).get("lugar");
            if (lugarObj != null) {
                puntoEncuentro = lugarObj.toString();
            }
        }
        
        // Beneficios (información clara para el guía)
        String beneficios = "Pago garantizado: S/. " + (int)oferta.getPagoGuia();
        if (oferta.getServiciosAdicionales() != null) {
            StringBuilder sb = new StringBuilder(beneficios);
            for (Object servicioObj : oferta.getServiciosAdicionales()) {
                if (servicioObj instanceof java.util.Map) {
                    java.util.Map<String, Object> servicio = (java.util.Map<String, Object>) servicioObj;
                    Boolean esPagado = (Boolean) servicio.get("esPagado");
                    if (esPagado != null && !esPagado) {
                        String nombre = (String) servicio.get("nombre");
                        if (nombre != null) {
                            sb.append(", ").append(nombre);
                        }
                    }
                }
            }
            beneficios = sb.toString();
        }
        
        // Crear GuiaTour compatible
        GuiaTour tour = new GuiaTour(
            oferta.getTitulo(),                    // name
            location,                              // location  
            pagoGuia,                              // price (usando PAGO AL GUÍA)
            oferta.getDuracion(),                  // duration
            idiomas,                               // languages
            oferta.getHoraInicio(),               // startTime
            oferta.getFechaRealizacion(),         // date
            oferta.getDescripcion(),              // description
            beneficios,                           // benefits
            horario,                              // schedule
            puntoEncuentro,                       // meetingPoint
            oferta.getNombreEmpresa(),            // empresa
            oferta.getResumenItinerario(),        // itinerario
            oferta.getConsideraciones(),          // experienciaMinima
            "Puntualidad requerida",              // puntualidad
            true,                                 // transporteIncluido
            false                                 // almuerzoIncluido
        );
        
        // Guardar referencia al ID de Firebase
        tour.setFirebaseId(oferta.getId());
        
        return tour;
    }
    
    /**
     * Mostrar mensaje cuando no hay ofertas
     */
    private void mostrarMensajeNoOfertas() {
        binding.noResultsView.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
    }
    
    /**
     * Aceptar oferta de tour
     */
    public void aceptarOferta(GuiaTour tour) {
        if (tour.getFirebaseId() == null) {
            Toast.makeText(this, "Error: ID de oferta no válido", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Aceptar Tour")
                .setMessage("¿Estás seguro de que quieres aceptar el tour '" + tour.getName() + "'?\n\nPago: S/. " + tour.getPrice())
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    procesarAceptacionTour(tour);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    /**
     * Procesar la aceptación del tour
     */
    private void procesarAceptacionTour(GuiaTour tour) {
        tourService.aceptarOferta(tour.getFirebaseId(), new TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_tours_ofertas.this, message, Toast.LENGTH_LONG).show();
                    // Recargar ofertas para quitar la aceptada
                    cargarOfertasDesdeFirebase();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_tours_ofertas.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
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
            Date today = sdf.parse("04/11/2025"); // Current date
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

    // MÉTODO DE PRUEBA: Recordatorio de Ubicación (desde toolbar)
    public void testLocationReminder() {
        if (preferencesManager.isNotificationEnabled("location_reminders")) {
            notificationService.sendLocationReminderNotification("Plaza de Armas");
            Toast.makeText(this, "📍 Recordatorio de ubicación enviado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "⚠️ Recordatorios de ubicación desactivados", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) { // REQUEST_CODE_TOUR_DETAIL
            boolean ofertaAceptada = data.getBooleanExtra("oferta_aceptada", false);
            boolean ofertaRechazada = data.getBooleanExtra("oferta_rechazada", false);
            String firebaseId = data.getStringExtra("firebase_id");
            
            if (ofertaAceptada || ofertaRechazada) {
                // Recargar las ofertas desde Firebase para reflejar los cambios
                cargarOfertasDesdeFirebase();
                
                if (ofertaAceptada) {
                    Toast.makeText(this, "¡Oferta aceptada exitosamente!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
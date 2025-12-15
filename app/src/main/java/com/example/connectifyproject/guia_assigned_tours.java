package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.data.TourAsignadoDataSeeder;
import com.example.connectifyproject.utils.TestMomentoTourData;
import com.example.connectifyproject.utils.FirebaseCleanupUtil;
import com.example.connectifyproject.utils.TourHoySeeder;
import com.example.connectifyproject.databinding.GuiaAssignedToursBinding;
import com.example.connectifyproject.fragment.GuiaDateFilterDialogFragment;
import com.example.connectifyproject.model.GuiaAssignedItem;
import com.example.connectifyproject.model.GuiaAssignedTour;
import com.example.connectifyproject.models.TourAsignado;
import com.example.connectifyproject.service.GuiaNotificationService;
import com.example.connectifyproject.services.TourFirebaseService;
import com.example.connectifyproject.storage.GuiaPreferencesManager;
import com.example.connectifyproject.ui.guia.GuiaAssignedTourAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class guia_assigned_tours extends AppCompatActivity implements GuiaDateFilterDialogFragment.FilterListener {
    private static final String TAG = "GuiaAssignedTours";
    
    private GuiaAssignedToursBinding binding;
    private GuiaAssignedTourAdapter adapter;
    private List<GuiaAssignedTour> allAssignedTours = new ArrayList<>();
    private List<GuiaAssignedItem> displayedItems = new ArrayList<>();
    private boolean isLoading = false;
    
    // 🎯 TOUR PRIORITARIO - Variables importantes
    private TourAsignado tourPrioritario = null;
    private String currentDateFrom, currentDateTo, currentCiudad;
    private com.google.firebase.firestore.ListenerRegistration priorityListener;
    private com.google.firebase.firestore.ListenerRegistration realtimeListener;
    private boolean isUpdatingUI = false; // ✅ Flag para prevenir bucle infinito
    
    // Servicios
    private GuiaNotificationService notificationService;
    private GuiaPreferencesManager preferencesManager;
    private TourFirebaseService tourFirebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaAssignedToursBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar servicios
        notificationService = new GuiaNotificationService(this);
        preferencesManager = new GuiaPreferencesManager(this);
        tourFirebaseService = new TourFirebaseService();

        // ========================================================================
        // 🔧 CONFIGURACIÓN INICIAL - EJECUTAR SEGÚN NECESIDAD
        // ========================================================================
        
        // 🧹 PASO 1: LIMPIAR DATOS PROBLEMÁTICOS (Solo si hay problemas)
        // Ejecutar UNA SOLA VEZ para eliminar tours con errores de formato String/Timestamp
        // FirebaseCleanupUtil.eliminarToursProblematicos();
        
        // 📝 PASO 2: CREAR TOURS ASIGNADOS DE PRUEBA
        // Descomenta las siguientes líneas SOLO para crear la colección inicial
        // ⚠️ IMPORTANTE: Vuelve a comentar después de la primera ejecución
        //TourAsignadoDataSeeder seeder = new TourAsignadoDataSeeder();
        //seeder.crearToursAsignadosDePrueba();
        
        // 🧪 PASO 3: TESTING ADICIONAL (Opcional)
        // Solo usar si necesitas tours adicionales para testing específico
        //TourHoySeeder.crearTourPendienteHoy(); // Tour individual para hoy
        // TestMomentoTourData.crearToursParaTestingMomentoTour(); // OBSOLETO - No usar
        
        Log.d(TAG, "Configuración de seeders completada");

        // Configurar RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuiaAssignedTourAdapter(this, displayedItems);
        binding.recyclerView.setAdapter(adapter);

        // Mostrar loading mientras carga
        binding.recyclerView.setVisibility(View.GONE);
        binding.noResultsView.setVisibility(View.GONE);
        
        // Cargar tours asignados desde Firebase
        loadToursAsignados();
        
        // 🎯 CARGAR TOUR PRIORITARIO
        loadTourPrioritario();

        binding.filterButton.setOnClickListener(v -> {
            GuiaDateFilterDialogFragment dialog = new GuiaDateFilterDialogFragment();
            dialog.show(getSupportFragmentManager(), "guia_date_filter_dialog");
        });

        binding.btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, guia_notificaciones.class);
            intent.putExtra("origin_activity", "guia_assigned_tours");
            startActivity(intent);
        });

        // Configurar toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tours Asignados");
        }
        
        binding.toolbar.setOnLongClickListener(v -> {
            testTourReminders();
            return true;
        });

        // Configurar bottom navigation
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_tours);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial) {
                Intent intent = new Intent(this, guia_historial.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_ofertas) {
                Intent intent = new Intent(this, guia_tours_ofertas.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_tours) {
                return true;
            } else if (id == R.id.nav_perfil) {
                Intent intent = new Intent(this, guia_perfil.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que "Tours" esté seleccionado cuando regresamos a esta actividad
        if (binding.bottomNav != null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_tours);
        }
        
        // 🔄 ACTUALIZAR DATOS EN TIEMPO REAL AL REGRESAR
        loadToursAsignados();
        loadTourPrioritario();
    }

    /**
     * Cargar tours asignados desde Firebase
     */
    private void loadToursAsignados() {
        Log.d(TAG, "Cargando tours asignados desde Firebase...");
        
        tourFirebaseService.getToursAsignados(new TourFirebaseService.TourAsignadoCallback() {
            @Override
            public void onSuccess(List<TourAsignado> tours) {
                Log.d(TAG, "Tours asignados cargados: " + tours.size());
                
                // ✅ FILTRAR TOURS COMPLETADOS - No mostrarlos en la lista
                allAssignedTours.clear();
                for (TourAsignado tourAsignado : tours) {
                    // ✅ AUTO-CANCELAR TOURS SIN INSCRIPCIONES QUE YA PASARON
                    autoCancelarTourSinInscripcionesVencido(tourAsignado);
                    
                    // No mostrar tours completados/finalizados/cancelados en la lista principal
                    if (tourAsignado.getEstado() == null || 
                        (!tourAsignado.getEstado().equalsIgnoreCase("completado") && 
                         !tourAsignado.getEstado().equalsIgnoreCase("finalizado") &&
                         !tourAsignado.getEstado().equalsIgnoreCase("cancelado"))) {
                        
                        GuiaAssignedTour guiaAssignedTour = convertToGuiaAssignedTour(tourAsignado);
                        allAssignedTours.add(guiaAssignedTour);
                        Log.d(TAG, "Tour agregado: " + tourAsignado.getTitulo() + " - Estado: " + tourAsignado.getEstado());
                    } else {
                        Log.d(TAG, "Tour completado/cancelado omitido: " + tourAsignado.getTitulo() + " - Estado: " + tourAsignado.getEstado());
                    }
                }
                
                // Aplicar filtros y actualizar UI
                runOnUiThread(() -> {
                    onApplyFilters(currentDateFrom, currentDateTo, currentCiudad);
                });
                
                // 🔄 CONFIGURAR LISTENER EN TIEMPO REAL PARA CAMBIOS DE ESTADO
                setupRealtimeUpdates();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al cargar tours asignados: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(guia_assigned_tours.this, "Error al cargar tours: " + error, Toast.LENGTH_LONG).show();
                    // Mostrar vista vacía en caso de error
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.noResultsView.setVisibility(View.VISIBLE);
                });
            }
        });
    }
    
    /**
     * 🔄 CONFIGURAR LISTENER EN TIEMPO REAL PARA ACTUALIZACIONES DE ESTADO
     */
    private void setupRealtimeUpdates() {
        if (tourFirebaseService == null) return;
        
        // ✅ VALIDAR USUARIO AUTENTICADO
        com.google.firebase.auth.FirebaseUser currentUser = 
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Usuario no autenticado - no se pueden configurar listeners en tiempo real");
            return;
        }
        
        // ✅ EVITAR MÚLTIPLES LISTENERS
        if (realtimeListener != null) {
            realtimeListener.remove();
        }
        
        // Escuchar cambios en tours asignados para actualizar estados en tiempo real
        realtimeListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("tours_asignados")
            .whereEqualTo("guiaAsignado.identificadorUsuario", currentUser.getUid())
            .whereEqualTo("habilitado", true)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                
                if (value != null && !value.isEmpty() && !isUpdatingUI) {
                    Log.d(TAG, "Cambios detectados en tours asignados - actualizando UI");
                    // ✅ PREVENIR BUCLE INFINITO
                    isUpdatingUI = true;
                    runOnUiThread(() -> {
                        loadToursAsignados();
                        if (tourPrioritario != null) {
                            loadTourPrioritario();
                        }
                        // Reset flag después de un delay
                        binding.getRoot().postDelayed(() -> isUpdatingUI = false, 2000);
                    });
                }
            });
    }

    /**
     * Convertir TourAsignado de Firebase a GuiaAssignedTour para UI - COMPATIBLE CON OFERTAS
     */
    private GuiaAssignedTour convertToGuiaAssignedTour(TourAsignado tourAsignado) {
        // Formatear fecha para UI
        String fechaFormateada = formatDateForUI(tourAsignado.getFechaRealizacion());
        String inicioFormateado = fechaFormateada + " - " + tourAsignado.getHoraInicio();
        
        // Convertir itinerario con estructura COMPATIBLE (lugar, actividad)
        List<String> itinerarioFormateado = new ArrayList<>();
        if (tourAsignado.getItinerario() != null) {
            for (int i = 0; i < tourAsignado.getItinerario().size(); i++) {
                Map<String, Object> punto = (Map<String, Object>) tourAsignado.getItinerario().get(i);
                String orden = String.valueOf(i + 1);
                
                // ✅ USAR "lugar" en lugar de "titulo" (compatible con ofertas)
                String lugar = (String) punto.get("lugar");
                if (lugar == null) {
                    lugar = (String) punto.get("titulo"); // Fallback para datos existentes
                }
                
                String hora = (String) punto.get("horaEstimada");
                
                // Manejar casos donde lugar o hora pueden ser null
                if (lugar == null) lugar = "Sin título";
                if (hora == null) hora = "Sin hora";
                
                itinerarioFormateado.add(orden + ". " + lugar + " - " + hora);
            }
        }
        
        // Formatear idiomas con manejo de null
        String idiomas = "";
        if (tourAsignado.getIdiomasRequeridos() != null && !tourAsignado.getIdiomasRequeridos().isEmpty()) {
            idiomas = String.join(", ", tourAsignado.getIdiomasRequeridos());
        }
        
        // Formatear servicios adicionales con manejo de null
        String servicios = "";
        if (tourAsignado.getServiciosAdicionales() != null && !tourAsignado.getServiciosAdicionales().isEmpty()) {
            List<String> nombreServicios = new ArrayList<>();
            for (Object servicio : tourAsignado.getServiciosAdicionales()) {
                Map<String, Object> servicioMap = (Map<String, Object>) servicio;
                String nombreServicio = (String) servicioMap.get("nombre");
                if (nombreServicio != null) {
                    nombreServicios.add(nombreServicio);
                }
            }
            servicios = String.join(", ", nombreServicios);
        }
        
        // Determinar estado para UI
        String estadoUI = mapearEstadoParaUI(tourAsignado.getEstado());
        
        // Número de participantes (manejar caso null de Firebase)
        int numeroParticipantes = tourAsignado.getNumeroParticipantesTotal() != null ? 
            tourAsignado.getNumeroParticipantesTotal() : 0;

        // ✅ INCLUIR PAGO AL GUÍA (compatible con ofertas)
        double pagoGuia = tourAsignado.getPagoGuia() > 0 ? tourAsignado.getPagoGuia() : 85.0; // Valor por defecto

        return new GuiaAssignedTour(
            tourAsignado.getTitulo(),
            tourAsignado.getNombreEmpresa(),
            inicioFormateado,
            tourAsignado.getDuracion(),
            numeroParticipantes,
            estadoUI,
            fechaFormateada,
            idiomas,
            servicios,
            itinerarioFormateado,
            pagoGuia, // ✅ Añadir pagoGuia al constructor
            tourAsignado.getId(), // ✅ Pasar ID para operaciones Firebase
            tourAsignado.getCiudad() // Ciudad del tour
        );
    }

    /**
     * Formatear fecha de Firebase Timestamp a formato UI
     */
    private String formatDateForUI(Object fechaRealizacion) {
        if (fechaRealizacion == null) return "";
        
        try {
            // Si es Timestamp de Firebase
            if (fechaRealizacion instanceof com.google.firebase.Timestamp) {
                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) fechaRealizacion;
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(date);
            }
            // Si es String, intentar parsearlo
            else if (fechaRealizacion instanceof String) {
                return (String) fechaRealizacion;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al formatear fecha: ", e);
        }
        
        return "";
    }

    /**
     * Mapear estado de Firebase a estado de UI
     */
    private String mapearEstadoParaUI(String estadoFirebase) {
        if (estadoFirebase == null) return "Pendiente";
        
        switch (estadoFirebase.toLowerCase()) {
            case "pendiente":
                return "Pendiente";
            case "check_in":
                return "Check-in Disponible";
            case "en_curso":
                return "En Curso";
            case "check_out":
                return "Check-out Disponible";
            case "completado":
                return "Completado";
            case "cancelado":
                return "Cancelado";
            // Compatibilidad con estados antiguos
            case "programado":
                return "Programado";
            case "confirmado":
                return "Programado";
            case "en_progreso":
                return "En Curso";
            case "finalizado":
                return "Completado";
            default:
                return "Pendiente";
        }
    }

    @Override
    public void onApplyFilters(String dateFrom, String dateTo, String ciudad) {
        this.currentDateFrom = dateFrom;
        this.currentDateTo = dateTo;
        this.currentCiudad = ciudad;

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat storedFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        List<GuiaAssignedTour> filteredTours = new ArrayList<>();
        
        for (GuiaAssignedTour tour : allAssignedTours) {
            boolean matches = true;
            
            // Filtro por fecha
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
            
            // Filtro por ciudad
            if (ciudad != null && !ciudad.isEmpty()) {
                String tourCiudad = tour.getCiudad();
                if (tourCiudad == null || !tourCiudad.equalsIgnoreCase(ciudad)) {
                    matches = false;
                }
            }
            
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
        
        // ✅ MANTENER INFORMACIÓN DE TOUR PRIORITARIO DESPUÉS DE FILTROS
        if (tourPrioritario != null && adapter != null) {
            adapter.setTourPrioritario(tourPrioritario.getId()); // Usar ID real
        }
    }

    private String getFormattedHeader(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date tourDate = sdf.parse(date);
            Date today = new Date(); // Fecha actual real
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            
            if (todayFormat.format(today).equals(date)) {
                return "Hoy, " + date.replace("/", " de ");
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(today);
                cal.add(Calendar.DAY_OF_YEAR, 1);
                if (todayFormat.format(cal.getTime()).equals(date)) {
                    return "Mañana, " + date.replace("/", " de ");
                }
                return date.replace("/", " de ");
            }
        } catch (ParseException e) {
            return date;
        }
    }

    // === MÉTODOS PARA SIMULAR NOTIFICACIONES DE CHECK-IN/CHECK-OUT ===
    
    public void simulateCheckInNotification(String tourName) {
        if (preferencesManager.isNotificationEnabled("checkin_reminders")) {
            notificationService.sendCheckInReminderNotification(tourName);
            Toast.makeText(this, "✅ Notificación de check-in enviada para: " + tourName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "⚠️ Notificaciones de check-in desactivadas", Toast.LENGTH_SHORT).show();
        }
    }

    public void simulateCheckOutNotification(String tourName) {
        if (preferencesManager.isNotificationEnabled("checkout_reminders")) {
            notificationService.sendCheckOutReminderNotification(tourName);
            Toast.makeText(this, "🏁 Notificación de check-out enviada para: " + tourName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "⚠️ Notificaciones de check-out desactivadas", Toast.LENGTH_SHORT).show();
        }
    }

    public void simulateLocationReminderNotification(String location) {
        if (preferencesManager.isNotificationEnabled("location_reminders")) {
            notificationService.sendLocationReminderNotification(location);
            Toast.makeText(this, "📍 Recordatorio de ubicación enviado para: " + location, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "⚠️ Recordatorios de ubicación desactivados", Toast.LENGTH_SHORT).show();
        }
    }
    
    // MÉTODO DE PRUEBA: Recordatorios de Tours (desde toolbar)
    public void testTourReminders() {
        if (preferencesManager.isNotificationEnabled("tour_reminders")) {
            // Simular 3 recordatorios: hoy, mañana, en 2 días
            notificationService.sendTourReminderNotification(
                "City Tour Lima Histórica", "05/11/2025", "9:00 AM", 0
            );
            notificationService.sendTourReminderNotification(
                "Tour Barranco y Miraflores", "06/11/2025", "2:00 PM", 1
            );
            notificationService.sendTourReminderNotification(
                "Tour Gastronómico", "07/11/2025", "11:00 AM", 2
            );
            Toast.makeText(this, "📅 Recordatorios de tours enviados (hoy, mañana, 2 días)", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "⚠️ Recordatorios de tours desactivados", Toast.LENGTH_SHORT).show();
        }
    }

    // Método público para acceso desde adaptadores
    public void testNotificationsForTour(String tourName, String status) {
        // Simular diferentes notificaciones según el estado del tour
        switch (status) {
            case "En Curso":
                // Simular check-in y recordatorio de ubicación
                simulateCheckInNotification(tourName);
                // Esperar 3 segundos y enviar recordatorio de ubicación
                new android.os.Handler().postDelayed(() -> {
                    simulateLocationReminderNotification("Plaza de Armas");
                }, 3000);
                break;
            case "Pendiente":
                // Simular recordatorio de tour próximo
                notificationService.sendTourReminderNotification(tourName, "Mañana", "9:00 AM", 1);
                Toast.makeText(this, "📅 Recordatorio de tour próximo enviado", Toast.LENGTH_SHORT).show();
                break;
            case "Finalizado":
                // Simular check-out
                simulateCheckOutNotification(tourName);
                break;
        }
    }
    
    // ========================================================================
    // 🎯 MÉTODOS DE TOUR PRIORITARIO
    // ========================================================================
    
    /**
     * 🎯 CARGAR TOUR PRIORITARIO - Método principal
     */
    private void loadTourPrioritario() {
        tourFirebaseService.getTourPrioritario(new TourFirebaseService.TourPrioritarioCallback() {
            @Override
            public void onSuccess(TourAsignado tour) {
                tourPrioritario = tour;
                if (tour != null) {
                    Log.d(TAG, "✅ Tour prioritario encontrado: " + tour.getTitulo() + " - Estado: " + tour.getEstado());
                    mostrarBannerTourPrioritario(tour);
                    
                    // ✅ INFORMAR AL ADAPTADOR CUÁL ES EL TOUR PRIORITARIO (usando ID real)
                    if (adapter != null) {
                        adapter.setTourPrioritario(tour.getId()); // Usar ID real de Firebase
                    }
                } else {
                    Log.d(TAG, "❌ No hay tour prioritario disponible");
                    ocultarBannerTourPrioritario();
                    
                    // ✅ LIMPIAR TOUR PRIORITARIO EN ADAPTADOR
                    if (adapter != null) {
                        adapter.setTourPrioritario(null);
                    }
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando tour prioritario: " + error);
                ocultarBannerTourPrioritario();
                
                // ✅ LIMPIAR TOUR PRIORITARIO EN ADAPTADOR EN CASO DE ERROR
                if (adapter != null) {
                    adapter.setTourPrioritario(null);
                }
            }
        });
    }
    
    /**
     * 🎨 MOSTRAR BANNER CON TOUR PRIORITARIO
     */
    private void mostrarBannerTourPrioritario(TourAsignado tour) {
        runOnUiThread(() -> {
            // Mostrar el banner
            binding.tourPrioritarioCard.setVisibility(View.VISIBLE);
            
            // Configurar información del tour
            binding.tourPrioritarioTitulo.setText(tour.getTitulo());
            binding.tourPrioritarioInfo.setText(String.format(
                "🕘 Inicio: %s | 👥 %d participantes", 
                tour.getHoraInicio(), 
                tour.getNumeroParticipantesTotal()
            ));
            
            // Configurar estado y color del banner
            configurarEstadoBanner(tour);
            
            // Configurar botones según estado
            configurarBotonesPrioritario(tour);
        });
    }
    
    /**
     * 🎨 CONFIGURAR ESTADO Y COLOR DEL BANNER
     */
    private void configurarEstadoBanner(TourAsignado tour) {
        String estado = tour.getEstado();
        String estadoTexto = mapearEstadoParaUI(estado);
        int colorBanner = getColorForEstado(estado);
        
        binding.tourPrioritarioEstado.setText(estadoTexto);
        
        // ✅ APLICAR COLOR DE FONDO TRANSLÚCIDO SEGÚN ESTADO
        int colorFondo = getColorFondoBanner(estado);
        binding.tourPrioritarioCard.setCardBackgroundColor(colorFondo);
    }
    
    /**
     * 🎨 OBTENER COLOR DE FONDO PARA BANNER (MÁS SUAVE)
     */
    private int getColorFondoBanner(String estado) {
        switch (estado.toLowerCase()) {
            case "en_curso": return 0xFFE8F5E8; // Verde claro para EN CURSO
            case "programado": return 0xFFE3F2FD; // Azul claro para PROGRAMADO
            case "completado": return 0xFFF3E5F5; // Púrpura claro para COMPLETADO
            case "cancelado": return 0xFFFFEBEE; // Rojo claro para CANCELADO
            default: return 0xFFF5F5F5; // Gris claro para otros estados
        }
    }
    
    /**
     * 🔘 CONFIGURAR BOTONES SEGÚN ESTADO UNIFICADO DEL TOUR
     * Estados: pendiente, check_in, en_curso, check_out, completado
     */
    private void configurarBotonesPrioritario(TourAsignado tour) {
        String estado = tour.getEstado();
        
        // BOTÓN DETALLES - Siempre disponible
        binding.btnDetallesRapido.setVisibility(View.VISIBLE);
        binding.btnDetallesRapido.setOnClickListener(v -> abrirDetallesTour(tour));
        
        // Configurar botones según estado del tour
        switch (estado != null ? estado.toLowerCase() : "pendiente") {
            case "pendiente":
                // � PENDIENTE: Solo Detalles + Botón "Habilitar Check-in"
                binding.btnMapaRapido.setVisibility(View.GONE);
                binding.btnCheckInRapido.setVisibility(View.VISIBLE);
                binding.btnCheckOutRapido.setVisibility(View.GONE);
                binding.btnCheckInRapido.setText("Habilitar Check-in");
                binding.btnCheckInRapido.setOnClickListener(v -> habilitarCheckInParaTour(tour.getId(), tour.getTitulo()));
                break;
                
            case "check_in":
                // ✅ CHECK-IN DISPONIBLE: Mapa + Check-in + Detalles
                binding.btnMapaRapido.setVisibility(View.VISIBLE);
                binding.btnCheckInRapido.setVisibility(View.VISIBLE);
                binding.btnCheckOutRapido.setVisibility(View.GONE);
                binding.btnCheckInRapido.setText("Check-in");
                binding.btnMapaRapido.setOnClickListener(v -> abrirMapaTour(tour));
                binding.btnCheckInRapido.setOnClickListener(v -> abrirCheckInTour(tour));
                break;
                
            case "en_curso":
                // ▶️ EN CURSO: Mapa + Check-out + Detalles
                binding.btnMapaRapido.setVisibility(View.VISIBLE);
                binding.btnCheckInRapido.setVisibility(View.GONE);
                binding.btnCheckOutRapido.setVisibility(View.VISIBLE);
                binding.btnCheckOutRapido.setText("Terminar Tour");
                binding.btnMapaRapido.setOnClickListener(v -> abrirMapaTour(tour));
                binding.btnCheckOutRapido.setOnClickListener(v -> habilitarCheckOutParaTour(tour.getId(), tour.getTitulo()));
                break;
                
            case "check_out":
                // 🏁 CHECK-OUT DISPONIBLE: Check-out + Detalles
                binding.btnMapaRapido.setVisibility(View.VISIBLE);
                binding.btnCheckInRapido.setVisibility(View.GONE);
                binding.btnCheckOutRapido.setVisibility(View.VISIBLE);
                binding.btnCheckOutRapido.setText("Check-out");
                binding.btnMapaRapido.setOnClickListener(v -> abrirMapaTour(tour));
                binding.btnCheckOutRapido.setOnClickListener(v -> abrirCheckOutTour(tour));
                break;
                
            case "completado":
            case "terminado":
            default:
                // 🔴 TERMINADO: Solo detalles
                binding.btnMapaRapido.setVisibility(View.GONE);
                binding.btnCheckInRapido.setVisibility(View.GONE);
                binding.btnCheckOutRapido.setVisibility(View.GONE);
                break;
        }
    }
    
    /**
     * 🙈 OCULTAR BANNER CUANDO NO HAY TOUR PRIORITARIO
     */
    private void ocultarBannerTourPrioritario() {
        runOnUiThread(() -> {
            binding.tourPrioritarioCard.setVisibility(View.GONE);
        });
    }
    
    /**
     * 📱 ABRIR MAPA DEL TOUR PRIORITARIO
     */
    private void abrirMapaTour(TourAsignado tour) {
        Intent intent = new Intent(this, guia_tour_map.class);
        intent.putExtra("tour_id", tour.getId()); // ✅ ID para operaciones Firebase
        intent.putExtra("tour_name", tour.getTitulo());
        intent.putExtra("tour_status", tour.getEstado());
        intent.putExtra("tour_clients", tour.getNumeroParticipantesTotal());
        
        // Convertir itinerario a ArrayList<String>
        ArrayList<String> itinerarioList = new ArrayList<>();
        if (tour.getItinerario() != null) {
            for (Map<String, Object> punto : tour.getItinerario()) {
                String lugar = (String) punto.get("lugar");
                String hora = (String) punto.get("horaEstimada");
                if (lugar != null && hora != null) {
                    itinerarioList.add(hora + " " + lugar);
                }
            }
        }
        intent.putStringArrayListExtra("tour_itinerario", itinerarioList);
        
        // Simular notificación de ubicación
        simulateLocationReminderNotification("Ubicación de inicio");
        startActivity(intent);
    }
    
    /**
     * ✅ ABRIR CHECK-IN DEL TOUR PRIORITARIO
     * El guía ESCANEA los QR individuales de cada cliente
     */
    private void abrirCheckInTour(TourAsignado tour) {
        // Simular notificación de check-in
        simulateCheckInNotification(tour.getTitulo());
        
        // CORREGIDO: Abrir escáner de QR en vez de mostrar QR del guía
        Intent intent = new Intent(this, guia_scan_qr_participants.class);
        intent.putExtra("tourId", tour.getId());
        intent.putExtra("tourTitulo", tour.getTitulo());
        intent.putExtra("numeroParticipantes", tour.getNumeroParticipantesTotal());
        intent.putExtra("scanMode", "check_in");
        startActivity(intent);
    }
    
    /**
     * 🏁 ESCANEAR QR DE CHECK-OUT
     * Guía ESCANEA el QR de cada cliente al finalizar el tour
     */
    private void abrirCheckOutTour(TourAsignado tour) {
        // Simular notificación de check-out
        simulateCheckOutNotification(tour.getTitulo());
        
        Intent intent = new Intent(this, guia_scan_qr_participants.class);
        intent.putExtra("tourId", tour.getId());
        intent.putExtra("tourTitulo", tour.getTitulo());
        intent.putExtra("numeroParticipantes", tour.getNumeroParticipantesTotal());
        intent.putExtra("scanMode", "check_out"); // ✅ Modo check-out
        startActivity(intent);
    }
    
    /**
     * 📋 ABRIR DETALLES DEL TOUR PRIORITARIO
     */
    private void abrirDetallesTour(TourAsignado tour) {
        GuiaAssignedTour guiaAssignedTour = convertToGuiaAssignedTour(tour);
        
        Intent intent = new Intent(this, guia_assigned_tour_detail.class);
        intent.putExtra("tour_id", tour.getId()); // ✅ CRÍTICO: Pasar ID del tour
        intent.putExtra("tour_name", guiaAssignedTour.getName());
        intent.putExtra("tour_empresa", guiaAssignedTour.getEmpresa());
        intent.putExtra("tour_initio", guiaAssignedTour.getInitio());
        intent.putExtra("tour_duration", guiaAssignedTour.getDuration());
        intent.putExtra("tour_clients", guiaAssignedTour.getClients());
        intent.putExtra("tour_status", guiaAssignedTour.getStatus());
        intent.putExtra("tour_languages", guiaAssignedTour.getLanguages());
        intent.putExtra("tour_services", guiaAssignedTour.getServices());
        intent.putStringArrayListExtra("tour_itinerario", new ArrayList<>(guiaAssignedTour.getItinerario()));
        startActivity(intent);
    }
    
    /**
     * 🔧 MÉTODOS HELPER
     */
    private boolean esTourDeHoy(TourAsignado tour) {
        if (tour.getFechaRealizacion() == null) return false;
        
        Date fechaTour = tour.getFechaRealizacion().toDate();
        Date hoy = new Date();
        
        // Comparar solo la fecha (sin hora)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(hoy).equals(sdf.format(fechaTour));
    }
    
    private int getColorForEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "en_curso": return 0xFF4CAF50; // Verde intenso para EN CURSO
            case "programado": return 0xFF2196F3; // Azul para PROGRAMADO
            case "completado": return 0xFF9C27B0; // Púrpura para COMPLETADO
            case "cancelado": return 0xFFF44336; // Rojo para CANCELADO
            default: return 0xFF9E9E9E; // Gris para otros estados
        }
    }
    
    /**
     * 🔄 HABILITAR CHECK-IN PARA TOUR
     */
    public void habilitarCheckInParaTour(String tourId, String tourName) {
        tourFirebaseService.habilitarCheckIn(tourId, new TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_assigned_tours.this, 
                        "✅ Check-in habilitado para: " + tourName, Toast.LENGTH_LONG).show();
                    
                    // Recargar datos
                    loadToursAsignados();
                    loadTourPrioritario();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_assigned_tours.this, 
                        "❌ Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * 🔚 HABILITAR CHECK-OUT PARA TOUR
     */
    public void habilitarCheckOutParaTour(String tourId, String tourName) {
        tourFirebaseService.habilitarCheckOut(tourId, new TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_assigned_tours.this, 
                        "🏁 Check-out habilitado para: " + tourName, Toast.LENGTH_LONG).show();
                    
                    // Recargar datos
                    loadToursAsignados();
                    loadTourPrioritario();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_assigned_tours.this, 
                        "❌ Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * 🚫 AUTO-CANCELAR TOURS SIN INSCRIPCIONES QUE YA PASARON
     * Verifica si un tour tiene 0 participantes y ya pasó su fecha/hora de finalización
     * Si ambas condiciones se cumplen, cambia automáticamente el estado a "cancelado"
     */
    private void autoCancelarTourSinInscripcionesVencido(TourAsignado tour) {
        try {
            // Verificar si ya está cancelado
            if ("cancelado".equalsIgnoreCase(tour.getEstado())) {
                return;
            }
            
            // Verificar número de participantes
            int numParticipantes = 0;
            if (tour.getParticipantes() != null) {
                numParticipantes = tour.getParticipantes().size();
            }
            if (tour.getNumeroParticipantesTotal() != null && tour.getNumeroParticipantesTotal() > 0) {
                numParticipantes = tour.getNumeroParticipantesTotal();
            }
            
            // Si tiene participantes, no cancelar
            if (numParticipantes > 0) {
                return;
            }
            
            // ✅ NUEVA REGLA: Validar que falten 2 horas o menos para el inicio
            if (tour.getFechaRealizacion() == null || tour.getHoraInicio() == null) {
                return;
            }
            
            double horasRestantes = com.example.connectifyproject.utils.TourTimeValidator
                .calcularHorasHastaInicio(tour.getFechaRealizacion(), tour.getHoraInicio());
            
            // Solo cancelar si faltan 2 horas o menos (pero aún no ha iniciado)
            if (horasRestantes <= 2.0 && horasRestantes >= 0) {
                // El tour está a 2 horas o menos y no tiene inscripciones -> CANCELAR
                Log.w(TAG, "🚫 Auto-cancelando tour sin inscripciones (faltan " + 
                    String.format("%.1f", horasRestantes) + " horas): " + tour.getTitulo());
                
                // Llamar al método actualizado que crea pagos
                tourFirebaseService.verificarYCancelarTourSinParticipantes(
                    tour.getId(), 
                    new TourFirebaseService.OperationCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Log.d(TAG, "✅ Tour cancelado automáticamente: " + tour.getTitulo());
                            tour.setEstado("cancelado");
                        }
                        
                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Error al cancelar tour automáticamente: " + error);
                        }
                    }
                );
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar auto-cancelación: " + e.getMessage(), e);
        }
    }
    
    /**
     * ▶️ CAMBIAR ESTADO: PENDIENTE → CHECK_IN
     * Cambio directo de estado para tour prioritario pendiente
     */
    public void cambiarEstadoPendienteACheckIn(String tourId, String tourName) {
        tourFirebaseService.cambiarPendienteACheckIn(tourId, new TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_assigned_tours.this, 
                        "▶️ Tour " + tourName + " listo para check-in", Toast.LENGTH_LONG).show();
                    
                    // Recargar datos y tour prioritario
                    loadToursAsignados();
                    loadTourPrioritario();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_assigned_tours.this, 
                        "❌ Error cambiando estado: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * 🛑 CAMBIAR ESTADO: EN_CURSO → CHECK_OUT
     * Cambio directo de estado para tour en curso
     */
    public void cambiarEstadoEnCursoACheckOut(String tourId, String tourName) {
        tourFirebaseService.cambiarEnCursoACheckOut(tourId, new TourFirebaseService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_assigned_tours.this, 
                        "🛑 Tour " + tourName + " listo para check-out", Toast.LENGTH_LONG).show();
                    
                    // Recargar datos y tour prioritario
                    loadToursAsignados();
                    loadTourPrioritario();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(guia_assigned_tours.this, 
                        "❌ Error cambiando estado: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ Limpiar listeners para evitar memory leaks
        if (priorityListener != null) {
            priorityListener.remove();
        }
        if (realtimeListener != null) {
            realtimeListener.remove();
        }
    }
}
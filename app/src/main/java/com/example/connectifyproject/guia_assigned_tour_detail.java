package com.example.connectifyproject;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.databinding.GuiaAssignedTourDetailBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class guia_assigned_tour_detail extends AppCompatActivity {
    private GuiaAssignedTourDetailBinding binding;
    private FirebaseFirestore db;
    private String tourId;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    
    // Variables para almacenar datos del tour
    private String tourName;
    private String tourStatus;
    private int tourClients;
    private ArrayList<String> tourItinerario;
    private List<Map<String, Object>> tourItinerarioCompleto; // Con coordenadas
    private Timestamp fechaRealizacion;
    private String horaInicio;
    private String duracionHoras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GuiaAssignedTourDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Tour");
        }

        // Inicializar Firebase y formatos de fecha
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Obtener tour ID del intent
        Intent intent = getIntent();
        tourId = intent.getStringExtra("tour_id");

        android.util.Log.d("GuiaAssignedTour", "onCreate - tourId: " + tourId);

        if (tourId != null && !tourId.isEmpty()) {
            loadTourDataFromFirebase();
        } else {
            android.util.Log.e("GuiaAssignedTour", "Error: ID del tour no encontrado o vacío");
            Toast.makeText(this, "Error: ID del tour no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Cargar datos del tour desde Firebase (colección tours_asignados)
     */
    private void loadTourDataFromFirebase() {
        android.util.Log.d("GuiaAssignedTour", "loadTourDataFromFirebase - Consultando tourId: " + tourId);
        
        db.collection("tours_asignados")
            .document(tourId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                android.util.Log.d("GuiaAssignedTour", "loadTourDataFromFirebase - Success. Existe: " + documentSnapshot.exists());
                
                if (documentSnapshot.exists()) {
                    android.util.Log.d("GuiaAssignedTour", "Tour encontrado, llamando setupTourFromFirebase");
                    setupTourFromFirebase(documentSnapshot);
                } else {
                    android.util.Log.e("GuiaAssignedTour", "Tour no existe en Firebase");
                    Toast.makeText(this, "Tour no encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("GuiaAssignedTour", "Error al cargar tour: " + e.getMessage(), e);
                Toast.makeText(this, "Error al cargar tour: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    /**
     * Configurar UI con datos de Firebase
     */
    /**
 * Configurar UI con datos de Firebase
 */
private void setupTourFromFirebase(DocumentSnapshot doc) {
    android.util.Log.d("GuiaAssignedTour", "setupTourFromFirebase - Iniciando configuración UI");
    
    // Datos básicos
    String titulo = doc.getString("titulo");
    String nombreEmpresa = doc.getString("nombreEmpresa");
    String descripcion = doc.getString("descripcion");
    String duracion = doc.getString("duracion");
    String horaInicio = doc.getString("horaInicio");
    String horaFin = doc.getString("horaFin");
    String estado = doc.getString("estado");
    Double pagoGuia = doc.getDouble("pagoGuia");
    
    android.util.Log.d("GuiaAssignedTour", "Datos cargados - Titulo: " + titulo + ", Estado: " + estado);
    
    // Fecha de realización
    Timestamp fechaRealizacion = doc.getTimestamp("fechaRealizacion");
    String fechaFormateada = fechaRealizacion != null ? 
        dateFormat.format(fechaRealizacion.toDate()) : "Fecha no disponible";
    
    // ✅ IDIOMAS - Ya es List<String>, está correcto
    List<String> idiomasLista = (List<String>) doc.get("idiomasRequeridos");
    String idiomas = idiomasLista != null && !idiomasLista.isEmpty() ? 
        String.join(", ", idiomasLista) : "No especificado";
    
    // ✅ SERVICIOS ADICIONALES - Corregir cast y extracción
    List<Map<String, Object>> serviciosData = (List<Map<String, Object>>) doc.get("serviciosAdicionales");
    List<String> nombresServicios = new ArrayList<>();
    if (serviciosData != null) {
        for (Map<String, Object> servicio : serviciosData) {
            String nombre = (String) servicio.get("nombre");
            if (nombre != null && !nombre.isEmpty()) {
                nombresServicios.add(nombre);
            }
        }
    }
    String servicios = !nombresServicios.isEmpty() ? 
        String.join(", ", nombresServicios) : "Sin servicios adicionales";
    
    // ✅ ITINERARIO - Ya está correcto
    List<Map<String, Object>> itinerarioData = (List<Map<String, Object>>) doc.get("itinerario");
    ArrayList<String> itinerarioTexto = new ArrayList<>();
    if (itinerarioData != null) {
        for (Map<String, Object> punto : itinerarioData) {
            String nombrePunto = (String) punto.get("nombre");
            String direccion = (String) punto.get("direccion");
            if (nombrePunto != null) {
                itinerarioTexto.add(nombrePunto + (direccion != null ? " - " + direccion : ""));
            }
        }
    }
    
    // Participantes
    List<Map<String, Object>> participantesData = (List<Map<String, Object>>) doc.get("participantes");
    int numParticipantes = participantesData != null ? participantesData.size() : 0;
    
    // Cargar imagen principal si existe
    String imagenPrincipal = doc.getString("imagenPrincipal");
    if (imagenPrincipal != null && !imagenPrincipal.isEmpty()) {
        // Si tienes un ImageView para la imagen principal en el layout
        // Glide.with(this).load(imagenPrincipal).into(binding.tourImage);
    }
    
    // Configurar UI
    // Guardar datos en variables de clase
    this.tourName = titulo;
    this.tourStatus = estado;
    this.tourClients = numParticipantes;
    this.tourItinerario = itinerarioTexto;
    this.tourItinerarioCompleto = itinerarioData;
    this.fechaRealizacion = fechaRealizacion;
    this.horaInicio = horaInicio;
    this.duracionHoras = duracion;
    
    setupTourHeader(titulo, nombreEmpresa, fechaFormateada + " " + horaInicio, 
                   duracion + " horas", numParticipantes, estado, pagoGuia);
    setupParticipantes(participantesData);
    setupItinerario(itinerarioTexto);
    setupTourInfo(idiomas, servicios, descripcion);
    setupActionButtons(estado);
    
    // Lógica de acciones
    boolean shouldShowActions = shouldShowActionButtons(estado, fechaFormateada);
    binding.actionsCard.setVisibility(shouldShowActions ? View.VISIBLE : View.GONE);
    
    setupButtonClickListeners(titulo, estado, itinerarioTexto, numParticipantes);
}

    /**
     * ✅ HEADER: Configurar información principal del tour
     */
    private void setupTourHeader(String tourName, String tourEmpresa, String tourInitio, 
                                String tourDuration, int tourClients, String tourStatus, Double pagoGuia) {
        binding.tourName.setText(tourName != null ? tourName : "Tour sin título");
        binding.empresaBadge.setText(tourEmpresa != null ? tourEmpresa : "Empresa");
        binding.tourInitio.setText(tourInitio != null ? tourInitio : "Fecha no disponible");
        binding.tourDuration.setText(tourDuration != null ? tourDuration : "Duración");
        binding.tourClients.setText(tourClients + " personas");
        
        // Pago al guía desde Firebase
        if (pagoGuia != null) {
            binding.pagoGuiaAmount.setText("S/. " + String.format(Locale.getDefault(), "%.0f", pagoGuia));
        } else {
            binding.pagoGuiaAmount.setText("S/. 0");
        }
        
        // Estado del tour con color
        binding.tourStatusBadge.setText(formatearEstado(tourStatus));
        binding.tourStatusBadge.setBackgroundColor(getStatusColor(tourStatus));
    }

    /**
     * ✅ PARTICIPANTES: Mostrar lista de participantes desde Firebase
     */
    private void setupParticipantes(List<Map<String, Object>> participantesData) {
        LinearLayout container = binding.participantesContainer;
        container.removeAllViews();
        
        if (participantesData != null && !participantesData.isEmpty()) {
            for (Map<String, Object> participante : participantesData) {
                String nombre = (String) participante.get("nombre");
                String tipoDoc = (String) participante.get("tipoDocumento");
                String numeroDoc = (String) participante.get("numeroDocumento");
                
                TextView participanteView = new TextView(this);
                String textoCompleto = "👤 " + (nombre != null ? nombre : "Participante");
                if (tipoDoc != null && numeroDoc != null) {
                    textoCompleto += " - " + tipoDoc + ": " + numeroDoc;
                }
                participanteView.setText(textoCompleto);
                participanteView.setTextSize(14);
                participanteView.setTextColor(Color.parseColor("#212121"));
                participanteView.setPadding(0, 8, 0, 8);
                container.addView(participanteView);
            }
        } else {
            TextView emptyView = new TextView(this);
            emptyView.setText("👤 No hay participantes registrados aún");
            emptyView.setTextSize(14);
            emptyView.setTextColor(Color.parseColor("#757575"));
            container.addView(emptyView);
        }
    }

    /**
     * ✅ ITINERARIO: Mostrar puntos del tour dinámicamente
     */
    private void setupItinerario(ArrayList<String> tourItinerario) {
        LinearLayout container = binding.itinerarioContainer;
        container.removeAllViews();
        
        if (tourItinerario != null && !tourItinerario.isEmpty()) {
            for (int i = 0; i < tourItinerario.size(); i++) {
                TextView itinerarioView = new TextView(this);
                itinerarioView.setText("📍 " + (i + 1) + ". " + tourItinerario.get(i));
                itinerarioView.setTextSize(14);
                itinerarioView.setTextColor(Color.parseColor("#212121"));
                itinerarioView.setPadding(0, 8, 0, 8);
                container.addView(itinerarioView);
            }
        } else {
            TextView emptyView = new TextView(this);
            emptyView.setText("📍 Itinerario no disponible");
            emptyView.setTextSize(14);
            emptyView.setTextColor(Color.parseColor("#757575"));
            container.addView(emptyView);
        }
    }

    /**
     * ✅ INFO: Configurar información adicional del tour
     */
    private void setupTourInfo(String tourLanguages, String tourServices, String descripcion) {
        binding.tourLanguages.setText("🌐 Idiomas: " + (tourLanguages != null ? tourLanguages : "No especificado"));
        binding.tourServices.setText("🎁 Servicios: " + (tourServices != null && !tourServices.isEmpty() ? tourServices : "No especificado"));
        
        // Agregar descripción si existe el campo en el layout
        // (Asumiendo que hay un TextView para descripción en el layout)
    }

    /**
     * ✅ BOTONES: Configurar listeners para acciones según estado del tour
     */
    private void setupButtonClickListeners(String tourName, String tourStatus, 
                                         ArrayList<String> tourItinerario, int tourClients) {
        
        String estadoLower = tourStatus != null ? tourStatus.toLowerCase() : "";
        
        // BOTÓN CHECK-IN: Solo para habilitar check-in desde estado pendiente
        binding.checkInButton.setOnClickListener(v -> {
            if (estadoLower.equals("pendiente") || estadoLower.equals("programado") || estadoLower.equals("confirmado")) {
                // Habilitar check-in (cambiar estado de pendiente a check_in)
                habilitarCheckIn();
            }
            // Ya no se muestra QR desde aquí, el guía debe ir al mapa para escanear
        });

        // BOTÓN MAPA: Siempre navega al mapa
        binding.mapButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(this, guia_tour_map.class);
            mapIntent.putExtra("tour_id", tourId);
            mapIntent.putExtra("tour_name", this.tourName);
            mapIntent.putExtra("tour_status", this.tourStatus);
            mapIntent.putStringArrayListExtra("tour_itinerario", this.tourItinerario);
            mapIntent.putExtra("tour_clients", this.tourClients);
            startActivity(mapIntent);
        });

        // BOTÓN CHECK-OUT: Solo para habilitar check-out desde en_curso
        binding.checkOutButton.setOnClickListener(v -> {
            if (estadoLower.equals("en_curso") || estadoLower.equals("en curso") || estadoLower.equals("en_progreso")) {
                // Habilitar check-out (cambiar estado de en_curso a check_out)
                habilitarCheckOut();
            }
            // Ya no se muestra QR desde aquí, el guía debe ir al mapa para escanear
        });
    }
    
    /**
     * Habilitar check-in: Cambiar estado del tour de "pendiente" a "check_in"
     */
    private void habilitarCheckIn() {
        db.collection("tours_asignados")
            .document(tourId)
            .update("estado", "check_in")
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "✅ Check-in habilitado. Ahora puedes mostrar el QR.", Toast.LENGTH_LONG).show();
                // Recargar datos para actualizar UI
                loadTourDataFromFirebase();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "❌ Error al habilitar check-in: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Mostrar QR de check-in
     * Si el estado ya es check_in, mostrar directamente sin validar
     */
    private void mostrarQRCheckIn() {
        // Si el estado es check_in, mostrar QR directamente (ya fue habilitado)
        String estadoLower = tourStatus != null ? tourStatus.toLowerCase() : "";
        boolean checkInYaHabilitado = estadoLower.equals("check_in") || estadoLower.equals("check-in disponible");
        
        // Si no está habilitado, validar ventana temporal
        if (!checkInYaHabilitado && !esVentanaValidaParaCheckIn()) {
            long minutosParaInicio = calcularMinutosParaInicio();
            
            if (minutosParaInicio > 10) {
                Toast.makeText(this, 
                    "⏰ El check-in estará disponible 10 minutos antes del inicio del tour (faltan " + minutosParaInicio + " minutos)", 
                    Toast.LENGTH_LONG).show();
                return;
            } else if (minutosParaInicio < 0 && yaPasoHoraFin()) {
                Toast.makeText(this, 
                    "⏰ El check-in ya no está disponible. El tour ha finalizado.", 
                    Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        // 📱 CORRECTO: Guía ESCANEA QR del cliente
        Intent intent = new Intent(this, guia_scan_qr_participants.class);
        intent.putExtra("tourId", tourId);
        intent.putExtra("tourTitulo", tourName);
        intent.putExtra("numeroParticipantes", tourClients);
        intent.putExtra("scanMode", "check_in"); // ✅ Modo check-in
        startActivity(intent);
    }
    
    /**
     * ⏰ VALIDAR VENTANA TEMPORAL PARA CHECK-IN
     * Check-in solo disponible: 10 minutos antes del inicio hasta hora_inicio + duración
     */
    private boolean esVentanaValidaParaCheckIn() {
        long minutosParaInicio = calcularMinutosParaInicio();
        
        // Check-in disponible desde 10 minutos antes hasta el final del tour
        return minutosParaInicio >= -1000 && minutosParaInicio <= 10 && !yaPasoHoraFin();
    }
    
    /**
     * ⏰ CALCULAR MINUTOS QUE FALTAN PARA EL INICIO
     * @return minutos (positivo = falta tiempo, negativo = ya pasó)
     */
    private long calcularMinutosParaInicio() {
        try {
            if (fechaRealizacion == null || horaInicio == null) {
                return Long.MAX_VALUE;
            }
            
            // Combinar fecha con hora de inicio
            Date fechaTour = fechaRealizacion.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            String fechaStr = dateOnlyFormat.format(fechaTour);
            Date fechaHoraInicio = sdf.parse(fechaStr + " " + horaInicio);
            
            if (fechaHoraInicio == null) {
                return Long.MAX_VALUE;
            }
            
            Date ahora = new Date();
            long diffMs = fechaHoraInicio.getTime() - ahora.getTime();
            return diffMs / (60 * 1000); // Convertir a minutos
            
        } catch (Exception e) {
            android.util.Log.e("GuiaAssignedTour", "Error calculando minutos para inicio", e);
            return Long.MAX_VALUE;
        }
    }
    
    /**
     * ⏰ VERIFICAR SI YA PASÓ LA HORA DE FIN DEL TOUR
     * hora_fin = hora_inicio + duración
     */
    private boolean yaPasoHoraFin() {
        try {
            if (fechaRealizacion == null || horaInicio == null || duracionHoras == null) {
                return false;
            }
            
            // Combinar fecha con hora de inicio
            Date fechaTour = fechaRealizacion.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            String fechaStr = dateOnlyFormat.format(fechaTour);
            Date fechaHoraInicio = sdf.parse(fechaStr + " " + horaInicio);
            
            if (fechaHoraInicio == null) {
                return false;
            }
            
            // Agregar duración del tour
            int duracionMinutos = (int)(Double.parseDouble(duracionHoras) * 60);
            Date fechaHoraFin = new Date(fechaHoraInicio.getTime() + (duracionMinutos * 60 * 1000));
            
            Date ahora = new Date();
            return ahora.after(fechaHoraFin);
            
        } catch (Exception e) {
            android.util.Log.e("GuiaAssignedTour", "Error verificando hora fin", e);
            return false;
        }
    }
    
    /**
     * Habilitar check-out: Cambiar estado del tour de "en_curso" a "check_out"
     */
    private void habilitarCheckOut() {
        db.collection("tours_asignados")
            .document(tourId)
            .update("estado", "check_out")
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "✅ Check-out habilitado. Ahora puedes mostrar el QR.", Toast.LENGTH_LONG).show();
                // Recargar datos para actualizar UI
                loadTourDataFromFirebase();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "❌ Error al habilitar check-out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * 📱 ESCANEAR QR DE CHECK-OUT
     * Guía ESCANEA el QR de cada cliente al finalizar
     */
    private void mostrarQRCheckOut() {
        Intent intent = new Intent(this, guia_scan_qr_participants.class);
        intent.putExtra("tourId", tourId);
        intent.putExtra("tourTitulo", tourName);
        intent.putExtra("numeroParticipantes", tourClients);
        intent.putExtra("scanMode", "check_out"); // ✅ Modo check-out
        startActivity(intent);
    }

    /**
     * ✅ ESTADOS: Formatear estado para mostrar en UI
     */
    private String formatearEstado(String estado) {
        if (estado == null) return "PENDIENTE";
        
        switch (estado.toLowerCase()) {
            case "pendiente":
                return "PENDIENTE";
            case "check_in":
            case "check-in disponible":
                return "CHECK-IN DISPONIBLE";
            case "en_curso":
            case "en curso":
            case "en_progreso":
                return "EN CURSO";
            case "check_out":
            case "check-out disponible":
                return "CHECK-OUT DISPONIBLE";
            case "completado":
            case "finalizado":
                return "COMPLETADO";
            case "cancelado":
                return "CANCELADO";
            // Compatibilidad con estados antiguos
            case "programado":
                return "PROGRAMADO";
            case "confirmado":
                return "CONFIRMADO";
            default:
                return estado.toUpperCase();
        }
    }

    /**
     * ✅ COLORES: Obtener color según estado del tour
     */
    private int getStatusColor(String estado) {
        if (estado == null) return Color.parseColor("#757575");
        
        switch (estado.toLowerCase()) {
            case "pendiente":
                return Color.parseColor("#FF9800"); // Naranja para pendiente
            case "check_in":
            case "check-in disponible":
                return Color.parseColor("#03DAC6"); // Verde agua para check-in
            case "en_curso":
            case "en curso":
            case "en_progreso":
                return Color.parseColor("#4CAF50"); // Verde intenso para en curso
            case "check_out":
            case "check-out disponible":
                return Color.parseColor("#FF5722"); // Naranja rojizo para check-out
            case "completado":
            case "finalizado":
                return Color.parseColor("#9C27B0"); // Púrpura para completado
            case "cancelado":
                return Color.parseColor("#F44336"); // Rojo para cancelado
            // Compatibilidad con estados antiguos
            case "programado":
                return Color.parseColor("#2196F3"); // Azul para programado
            case "confirmado":
                return Color.parseColor("#2196F3"); // Azul para confirmado
            default:
                return Color.parseColor("#757575"); // Gris para otros estados
        }
    }

    /**
     * ✅ LÓGICA: Determinar si mostrar botones de acción según estado del tour
     */
    private boolean shouldShowActionButtons(String status, String fechaHora) {
        if (status == null) return false;
        
        String estadoLower = status.toLowerCase();
        
        // Mostrar botones para estos estados:
        // - pendiente: Botón "Habilitar Check-in"
        // - check_in: Botón "Ver Mapa y Escanear Check-in"
        // - en_curso: Botones "Ver Mapa" + "Finalizar Tour"
        // - check_out: Botón "Ver Mapa y Escanear Check-out"
        
        return estadoLower.equals("pendiente") ||
               estadoLower.equals("check_in") ||
               estadoLower.equals("check-in disponible") ||
               estadoLower.equals("en_curso") ||
               estadoLower.equals("en curso") ||
               estadoLower.equals("en_progreso") ||
               estadoLower.equals("check_out") ||
               estadoLower.equals("check-out disponible") ||
               estadoLower.equals("programado") ||
               estadoLower.equals("confirmado");
    }

    /**
     * ✅ CONFIGURAR BOTONES DE ACCIÓN SEGÚN ESTADO DEL TOUR
     */
    private void setupActionButtons(String tourStatus) {
        if (tourStatus == null) {
            binding.actionsCard.setVisibility(View.GONE);
            return;
        }
        
        String estadoLower = tourStatus.toLowerCase();
        
        // Ocultar todos los botones primero
        binding.checkInButton.setVisibility(View.GONE);
        binding.mapButton.setVisibility(View.GONE);
        binding.checkOutButton.setVisibility(View.GONE);
        
        // Configurar botones según estado
        switch (estadoLower) {
            case "pendiente":
            case "programado":
            case "confirmado":
                // 📌 PENDIENTE/PROGRAMADO: Solo botón para habilitar check-in
                binding.checkInButton.setVisibility(View.VISIBLE);
                binding.checkInButton.setText("Habilitar Check-in");
                binding.checkInButton.setIconResource(R.drawable.ic_check_circle);
                break;
                
            case "check_in":
            case "check-in disponible":
                // ✅ CHECK-IN DISPONIBLE: Solo mostrar botón de mapa
                // El guía debe ir al mapa para escanear QR de clientes
                binding.mapButton.setVisibility(View.VISIBLE);
                binding.mapButton.setText("Ver Mapa y Escanear Check-in");
                break;
                
            case "en_curso":
            case "en curso":
            case "en_progreso":
                // 🚀 EN CURSO: Mapa + Progreso + Check-out
                binding.mapButton.setVisibility(View.VISIBLE);
                binding.mapButton.setText("Ver Mapa y Progreso");
                
                binding.checkOutButton.setVisibility(View.VISIBLE);
                binding.checkOutButton.setText("Finalizar Tour");
                binding.checkOutButton.setIconResource(R.drawable.ic_check_circle);
                break;
                
            case "check_out":
            case "check-out disponible":
                // 🏁 CHECK-OUT DISPONIBLE: Mostrar mapa para escanear check-out
                binding.mapButton.setVisibility(View.VISIBLE);
                binding.mapButton.setText("Ver Mapa y Escanear Check-out");
                break;
                
            default:
                // Estados completado, cancelado, etc: No mostrar botones
                binding.actionsCard.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
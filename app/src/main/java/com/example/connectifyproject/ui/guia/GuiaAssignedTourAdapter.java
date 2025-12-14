package com.example.connectifyproject.ui.guia;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.GuiaItemAssignedTourBinding;
import com.example.connectifyproject.databinding.GuiaItemHeaderBinding;
import com.example.connectifyproject.guia_assigned_tour_detail;
import com.example.connectifyproject.guia_check_in;
import com.example.connectifyproject.guia_check_out;
import com.example.connectifyproject.guia_tour_map;
import com.example.connectifyproject.model.GuiaAssignedItem;
import com.example.connectifyproject.model.GuiaAssignedTour;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GuiaAssignedTourAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<GuiaAssignedItem> items;
    private Context context;
    
    // ✅ TOUR PRIORITARIO - Para saber cuál debe tener botones activos
    private String tourPrioritarioId = null;

    public GuiaAssignedTourAdapter(Context context, List<GuiaAssignedItem> items) {
        this.context = context;
        this.items = items;
    }
    
    /**
     * ✅ CONFIGURAR TOUR PRIORITARIO - Para habilitar botones
     */
    public void setTourPrioritario(String tourId) {
        this.tourPrioritarioId = tourId;
        notifyDataSetChanged(); // Refrescar vista para aplicar cambios
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GuiaAssignedItem.TYPE_HEADER) {
            GuiaItemHeaderBinding binding = GuiaItemHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new HeaderViewHolder(binding);
        } else {
            GuiaItemAssignedTourBinding binding = GuiaItemAssignedTourBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new AssignedTourViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GuiaAssignedItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).binding.headerText.setText(item.getHeader());
        } else if (holder instanceof AssignedTourViewHolder) {
            GuiaAssignedTour tour = item.getAssignedTour();
            AssignedTourViewHolder tourHolder = (AssignedTourViewHolder) holder;
            
            // ✅ USAR LA MISMA ESTRUCTURA QUE OFERTAS
            tourHolder.binding.empresaText.setText(tour.getEmpresa());
            tourHolder.binding.tourName.setText(tour.getName());
            tourHolder.binding.tourDuration.setText(tour.getDuration());
            // ✅ TEXTO INTELIGENTE PARA PARTICIPANTES
            int numParticipantes = tour.getClients();
            if (numParticipantes == 0) {
                tourHolder.binding.tourClients.setText("Sin registros aún");
            } else if (numParticipantes == 1) {
                tourHolder.binding.tourClients.setText("1 persona");
            } else {
                tourHolder.binding.tourClients.setText(numParticipantes + " personas");
            }
            
            // Estado con color dinámico
            String estado = formatearEstado(tour.getStatus());
            tourHolder.binding.tourStatus.setText(estado);
            
            // ✅ APLICAR COLOR AL ESTADO SEGÚN TIPO
            int colorEstado = getColorForEstado(tour.getStatus());
            tourHolder.binding.tourStatus.setTextColor(colorEstado);
            
            // Fechas - separar fecha y hora como en ofertas
            String[] fechaHora = tour.getInitio().split(" - ");
            if (fechaHora.length == 2) {
                tourHolder.binding.dateText.setText(fechaHora[0]);
                tourHolder.binding.tourStartTime.setText(fechaHora[1]);
            } else {
                tourHolder.binding.dateText.setText(tour.getDate() != null ? tour.getDate() : "Fecha no disponible");
                tourHolder.binding.tourStartTime.setText("Hora no disponible");
            }
            
            // ✅ MOSTRAR PAGO AL GUÍA (igual que en ofertas)
            tourHolder.binding.pagoGuiaText.setText("S/. " + (int) tour.getPagoGuia());

            // ✅ MOSTRAR ESTADO DEL TOUR VISUALMENTE
            configurarEstadoTour(tourHolder, tour);
            
            // ✅ TODOS LOS TOURS TIENEN BOTONES SEGÚN SU ESTADO
            configurarBotonesPorEstado(tourHolder, tour);

            // ✅ DESTACAR TOUR PRIORITARIO VISUALMENTE
            boolean esTourPrioritario = tourPrioritarioId != null && 
                                       tour.getName() != null && 
                                       tour.getName().hashCode() == tourPrioritarioId.hashCode();
            
            if (esTourPrioritario) {
                // Resaltar visualmente el tour prioritario
                tourHolder.itemView.setBackgroundResource(R.color.tour_prioritario_background);
            } else {
                tourHolder.itemView.setBackgroundResource(R.color.white);
            }

            // Entire card click for details
            tourHolder.itemView.setOnClickListener(v -> startDetailIntent(tour));
        }
    }

    /**
     * ✅ CONFIGURAR ESTADO VISUAL DEL TOUR
     */
    private void configurarEstadoTour(AssignedTourViewHolder holder, GuiaAssignedTour tour) {
        String estado = tour.getStatus();
        
        // Configurar empresa sin el estado (ya está en tour_status)
        holder.binding.empresaText.setText(tour.getEmpresa());
        
        // Configurar el campo de estado dedicado
        if (estado != null) {
            holder.binding.tourStatus.setText(estado.toUpperCase());
            
            // Configurar colores según el estado
            int colorFondo;
            int colorTexto;
            
            switch (estado.toLowerCase()) {
                case "en curso":
                    colorFondo = 0xFFE8F5E8; // Verde claro
                    colorTexto = 0xFF4CAF50; // Verde
                    break;
                case "pendiente":
                    colorFondo = 0xFFE3F2FD; // Azul claro
                    colorTexto = 0xFF2196F3; // Azul
                    break;
                case "programado":
                    colorFondo = 0xFFE8F5E8; // Verde claro
                    colorTexto = 0xFF4CAF50; // Verde
                    break;
                case "completado":
                case "finalizado":
                    colorFondo = 0xFFF3E5F5; // Púrpura claro
                    colorTexto = 0xFF9C27B0; // Púrpura
                    break;
                default:
                    colorFondo = 0xFFF5F5F5; // Gris claro
                    colorTexto = 0xFF757575; // Gris
                    break;
            }
            
            holder.binding.tourStatus.setBackgroundColor(colorFondo);
            holder.binding.tourStatus.setTextColor(colorTexto);
        } else {
            holder.binding.tourStatus.setText("PENDIENTE");
            holder.binding.tourStatus.setBackgroundColor(0xFFE3F2FD);
            holder.binding.tourStatus.setTextColor(0xFF2196F3);
        }
    }

    /**
     * ✅ CONFIGURACIÓN DE BOTONES SEGÚN ESTADO DEL TOUR
     * Todos los tours muestran botones según su estado específico
     */
    private void configurarBotonesPorEstado(AssignedTourViewHolder holder, GuiaAssignedTour tour) {
        String estado = tour.getStatus();
        boolean esTourPrioritario = tourPrioritarioId != null && 
                                   tour.getTourId().equals(tourPrioritarioId);
        
        // Mostrar layout de acciones
        holder.binding.actionsLayout.setVisibility(View.VISIBLE);
        
        // BOTÓN DETALLES - Oculto porque el card completo ya es clickeable
        holder.binding.detailsIcon.setVisibility(View.GONE);
        
        // 🎯 CONFIGURAR BOTONES SEGÚN ESTADO (PARA TODOS LOS TOURS)
        configurarBotonesPorEstadoTour(holder, tour, estado);
        
        // Destacar visualmente el tour prioritario
        if (esTourPrioritario) {
            holder.itemView.setBackgroundResource(R.color.brand_green_light);
        } else {
            holder.itemView.setBackgroundResource(R.color.white);
        }
    }

    /**
     * 🎯 CONFIGURAR BOTONES ESPECÍFICOS SEGÚN ESTADO DEL TOUR
     * Todos los tours muestran botones apropiados según su estado
     */
    private void configurarBotonesPorEstadoTour(AssignedTourViewHolder holder, GuiaAssignedTour tour, String estado) {
        switch (estado != null ? estado.toLowerCase() : "pendiente") {
            case "pendiente":
                // 📅 PENDIENTE: Solo Detalles (no hay acciones disponibles)
                holder.binding.mapIcon.setVisibility(View.GONE);
                holder.binding.checkInIcon.setVisibility(View.GONE);
                holder.binding.checkOutIcon.setVisibility(View.GONE);
                break;
                
            case "check_in":
                // ✅ CHECK_IN: Detalles + Check-in
                holder.binding.mapIcon.setVisibility(View.GONE);
                holder.binding.checkInIcon.setVisibility(View.VISIBLE);
                holder.binding.checkOutIcon.setVisibility(View.GONE);
                holder.binding.checkInIcon.setOnClickListener(v -> abrirCheckInTour(tour));
                break;
                
            case "en_curso":
            case "en curso":
            case "en_progreso":
                // ▶️ EN CURSO: Detalles + Mapa
                holder.binding.mapIcon.setVisibility(View.VISIBLE);
                holder.binding.checkInIcon.setVisibility(View.GONE);
                holder.binding.checkOutIcon.setVisibility(View.GONE);
                holder.binding.mapIcon.setOnClickListener(v -> startMapIntent(tour));
                break;
                
            case "check_out":
                // 🏁 CHECK_OUT: Detalles + Check-out
                holder.binding.mapIcon.setVisibility(View.GONE);
                holder.binding.checkInIcon.setVisibility(View.GONE);
                holder.binding.checkOutIcon.setVisibility(View.VISIBLE);
                holder.binding.checkOutIcon.setOnClickListener(v -> abrirCheckOutTour(tour));
                break;
                
            case "completado":
            case "finalizado":
            case "cancelado":
                // 🏁 ESTADOS FINALES: Solo Detalles
                holder.binding.mapIcon.setVisibility(View.GONE);
                holder.binding.checkInIcon.setVisibility(View.GONE);
                holder.binding.checkOutIcon.setVisibility(View.GONE);
                break;
                
            default:
                // ✅ OTROS ESTADOS: Solo detalles
                holder.binding.mapIcon.setVisibility(View.GONE);
                holder.binding.checkInIcon.setVisibility(View.GONE);
                holder.binding.checkOutIcon.setVisibility(View.GONE);
                break;
        }
    }

    // ✅ MÉTODOS PARA MANEJAR ACCIONES DE BOTONES - Ya están definidos más arriba
    // abrirCheckInTour(), abrirCheckOutTour(), abrirMapaTour(), startDetailIntent()
    
    /**
     * ▶️ CAMBIAR DE PENDIENTE A CHECK_IN
     * Cambio directo de estado para tour prioritario pendiente
     */
    private void cambiarPendienteACheckIn(GuiaAssignedTour tour) {
        if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
            ((com.example.connectifyproject.guia_assigned_tours) context)
                .cambiarEstadoPendienteACheckIn(tour.getTourId(), tour.getName());
        }
    }
    
    /**
     * 🛑 CAMBIAR DE EN_CURSO A CHECK_OUT  
     * Cambio directo de estado para tour en curso
     */
    private void cambiarEnCursoACheckOut(GuiaAssignedTour tour) {
        if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
            ((com.example.connectifyproject.guia_assigned_tours) context)
                .cambiarEstadoEnCursoACheckOut(tour.getTourId(), tour.getName());
        }
    }

    /**
     * 🔄 HABILITAR CHECK-IN (pendiente → check_in)
     */
    private void habilitarCheckInTour(GuiaAssignedTour tour) {
        if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
            ((com.example.connectifyproject.guia_assigned_tours) context)
                .habilitarCheckInParaTour(tour.getTourId(), tour.getName());
        }
    }
    
    /**
     * 🛑 HABILITAR CHECK-OUT (en_curso → check_out)
     */
    private void habilitarCheckOutTour(GuiaAssignedTour tour) {
        if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
            ((com.example.connectifyproject.guia_assigned_tours) context)
                .habilitarCheckOutParaTour(tour.getTourId(), tour.getName());
        }
    }
    
    /**
     * �️ ABRIR MAPA DEL TOUR
     */
    private void startMapIntent(GuiaAssignedTour tour) {
        abrirMapaTour(tour);
    }
    
    /**
     * ✅ MÉTODOS HELPER PARA ABRIR PANTALLAS
     */
    private void abrirMapaTour(GuiaAssignedTour tour) {
        // Simular notificación de ubicación
        if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
            ((com.example.connectifyproject.guia_assigned_tours) context).simulateLocationReminderNotification("Plaza de Armas");
        }
        Intent intent = new Intent(context, guia_tour_map.class);
        intent.putExtra("tour_id", tour.getTourId()); // ✅ ID para operaciones Firebase
        intent.putExtra("tour_name", tour.getName());
        intent.putExtra("tour_status", tour.getStatus());
        intent.putStringArrayListExtra("tour_itinerario", new ArrayList<>(tour.getItinerario()));
        intent.putExtra("tour_clients", tour.getClients());
        context.startActivity(intent);
    }
    
    private void abrirCheckInTour(GuiaAssignedTour tour) {
        // Simular notificación de check-in
        if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
            ((com.example.connectifyproject.guia_assigned_tours) context).simulateCheckInNotification(tour.getName());
        }
        Intent intent = new Intent(context, guia_check_in.class);
        intent.putExtra("tour_id", tour.getTourId()); // ✅ ID para operaciones Firebase
        intent.putExtra("tour_name", tour.getName());
        intent.putExtra("participants_count", tour.getClients());
        context.startActivity(intent);
    }
    
    private void abrirCheckOutTour(GuiaAssignedTour tour) {
        // Simular notificación de check-out
        if (context instanceof com.example.connectifyproject.guia_assigned_tours) {
            ((com.example.connectifyproject.guia_assigned_tours) context).simulateCheckOutNotification(tour.getName());
        }
        Intent intent = new Intent(context, guia_check_out.class);
        intent.putExtra("tour_id", tour.getTourId()); // ✅ ID para operaciones Firebase
        intent.putExtra("tour_name", tour.getName());
        intent.putExtra("participants_count", tour.getClients());
        context.startActivity(intent);
    }

    private void startDetailIntent(GuiaAssignedTour tour) {
        Intent intent = new Intent(context, guia_assigned_tour_detail.class);
        intent.putExtra("tour_id", tour.getTourId()); // ✅ ID del tour para cargar desde Firebase
        context.startActivity(intent);
    }

    /**
     * Formatear estado para mostrar en UI
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
     * ✅ OBTENER COLOR PARA ESTADO (MISMO QUE BANNER PRIORITARIO)
     */
    private int getColorForEstado(String estado) {
        if (estado == null) return Color.GRAY;
        
        switch (estado.toLowerCase()) {
            case "pendiente":
                return Color.parseColor("#FF9800"); // Naranja para pendiente
            case "check_in":
            case "check-in disponible":
                return Color.parseColor("#03DAC6"); // Verde agua para check-in
            case "en_curso":
            case "en curso":
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
            default:
                return Color.parseColor("#9E9E9E"); // Gris para otros estados
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    public void updateItems(List<GuiaAssignedItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        GuiaItemHeaderBinding binding;

        public HeaderViewHolder(GuiaItemHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class AssignedTourViewHolder extends RecyclerView.ViewHolder {
        GuiaItemAssignedTourBinding binding;

        public AssignedTourViewHolder(GuiaItemAssignedTourBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
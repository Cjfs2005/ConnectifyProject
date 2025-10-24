package com.example.connectifyproject.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.example.connectifyproject.models.Cliente_Reserva;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Gestor de almacenamiento externo específico para cliente
 * Maneja descarga de PDFs de reservas y otros archivos
 */
public class Cliente_FileStorageManager {

    private Context context;

    public Cliente_FileStorageManager(Context context) {
        this.context = context;
    }

    /**
     * Verificar si el almacenamiento externo está disponible para escritura
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Crear y guardar PDF de reserva en almacenamiento externo
     */
    public boolean downloadReservationPDF(Cliente_Reserva reserva) {
        if (!isExternalStorageWritable()) {
            return false;
        }

        try {
            // Crear nombre del archivo PDF
            String fileName = "Reserva_" + reserva.getTour().getTitle().replaceAll("[^a-zA-Z0-9]", "_") + 
                            "_" + getCurrentTimestamp() + ".pdf";
            
            // Guardar en directorio de Descargas
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File reservationFile = new File(downloadsDir, fileName);
            
            // Generar PDF real
            generatePDF(reserva, reservationFile);
            
            // Mostrar notificación de descarga completada
            NotificationHelper.showDownloadCompletedNotification(context, fileName);
            
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crear y guardar PDF de reserva en almacenamiento interno (específico de la app)
     */
    public boolean saveReservationInternalStorage(Cliente_Reserva reserva) {
        try {
            // Generar contenido
            String content = generateReservationContent(reserva);
            
            // Crear nombre del archivo
            String fileName = "reserva_" + reserva.getTour().getId() + "_" + getCurrentTimestamp() + ".txt";
            
            // Crear subdirectorio para reservas
            File reservasDir = new File(context.getFilesDir(), "reservas");
            if (!reservasDir.exists()) {
                reservasDir.mkdir();
            }
            
            File reservationFile = new File(reservasDir, fileName);
            
            // Escribir contenido
            try (FileOutputStream outputStream = new FileOutputStream(reservationFile);
                 FileWriter writer = new FileWriter(outputStream.getFD())) {
                writer.write(content);
            }
            
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generar contenido detallado de la reserva (simulación de PDF)
     */
    private String generateReservationContent(Cliente_Reserva reserva) {
        StringBuilder content = new StringBuilder();
        
        content.append("═══════════════════════════════════════\n");
        content.append("           CONFIRMACIÓN DE RESERVA\n");
        content.append("═══════════════════════════════════════\n\n");
        
        content.append("INFORMACIÓN DEL TOUR:\n");
        content.append("─────────────────────\n");
        content.append("Tour: ").append(reserva.getTour().getTitle()).append("\n");
        content.append("Ubicación: ").append(reserva.getTour().getLocation()).append("\n");
        content.append("Duración: ").append(reserva.getTour().getDuration()).append("\n");
        content.append("Fecha: ").append(reserva.getFecha()).append("\n\n");
        
        content.append("DETALLES DE LA RESERVA:\n");
        content.append("─────────────────────────\n");
        content.append("Número de personas: ").append(reserva.getPersonas()).append("\n");
        content.append("Estado: ").append(reserva.getEstado()).append("\n\n");
        
        if (reserva.getServiciosAdicionales() != null && !reserva.getServiciosAdicionales().isEmpty()) {
            content.append("SERVICIOS ADICIONALES:\n");
            content.append("────────────────────────\n");
            for (int i = 0; i < reserva.getServiciosAdicionales().size(); i++) {
                content.append("• ").append(reserva.getServiciosAdicionales().get(i).getName())
                       .append(" - S/").append(reserva.getServiciosAdicionales().get(i).getPrice()).append("\n");
            }
            content.append("\n");
        }
        
        content.append("INFORMACIÓN DE PAGO:\n");
        content.append("───────────────────────\n");
        content.append("Método de pago: ").append(reserva.getMetodoPago().getCardType()).append("\n");
        content.append("Subtotal: S/").append(reserva.getSubtotal()).append("\n");
        content.append("IGV (18%): S/").append(reserva.getIgv()).append("\n");
        content.append("TOTAL: S/").append(reserva.getTotal()).append("\n\n");
        
        content.append("═══════════════════════════════════════\n");
        content.append("Descargado el: ").append(getCurrentDateTime()).append("\n");
        content.append("Tourly - Tu compañero de viajes\n");
        content.append("═══════════════════════════════════════\n");
        
        return content.toString();
    }

    /**
     * Obtener timestamp actual para nombres de archivo
     */
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Obtener fecha y hora formateada para el contenido
     */
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Compartir archivo de reserva
     */
    public void shareReservationFile(String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, fileName);
        
        if (file.exists()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mi Reserva - Tourly");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Adjunto mi confirmación de reserva");
            context.startActivity(Intent.createChooser(shareIntent, "Compartir reserva"));
        }
    }

    /**
     * Listar archivos de reserva guardados
     */
    public String[] listSavedReservations() {
        File reservasDir = new File(context.getFilesDir(), "reservas");
        if (reservasDir.exists()) {
            return reservasDir.list();
        }
        return new String[0];
    }

    /**
     * Eliminar archivo de reserva
     */
    public boolean deleteReservationFile(String fileName) {
        File reservasDir = new File(context.getFilesDir(), "reservas");
        File file = new File(reservasDir, fileName);
        return file.exists() && file.delete();
    }

    /**
     * Generar PDF con el contenido de la reserva
     */
    private void generatePDF(Cliente_Reserva reserva, File outputFile) throws IOException {
        // Crear documento PDF
        PdfDocument pdfDocument = new PdfDocument();
        
        // Crear página
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        
        Canvas canvas = page.getCanvas();
        
        // Configurar fuentes
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(24);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        
        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.BLACK);
        headerPaint.setTextSize(16);
        headerPaint.setTypeface(Typeface.DEFAULT_BOLD);
        
        Paint bodyPaint = new Paint();
        bodyPaint.setColor(Color.BLACK);
        bodyPaint.setTextSize(12);
        bodyPaint.setTypeface(Typeface.DEFAULT);
        
        // Coordenadas iniciales
        int x = 50;
        int y = 80;
        int lineHeight = 25;
        
        // Título principal
        canvas.drawText("COMPROBANTE DE RESERVA", x, y, titlePaint);
        y += lineHeight * 2;
        
        // Información de la reserva
        canvas.drawText("INFORMACIÓN DE LA RESERVA", x, y, headerPaint);
        y += lineHeight;
        canvas.drawText("Código de reserva: " + reserva.getId(), x, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Fecha de reserva: " + reserva.getFecha(), x, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Número de personas: " + reserva.getPersonas(), x, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Estado: " + reserva.getEstado(), x, y, bodyPaint);
        y += lineHeight * 2;
        
        // Información del tour
        canvas.drawText("INFORMACIÓN DEL TOUR", x, y, headerPaint);
        y += lineHeight;
        canvas.drawText("Tour: " + reserva.getTour().getTitle(), x, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Ubicación: " + reserva.getTour().getLocation(), x, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Duración: " + reserva.getTour().getDuration(), x, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Fecha del tour: " + reserva.getFecha(), x, y, bodyPaint);
        y += lineHeight * 2;
        
        // Servicios adicionales
        if (reserva.getServiciosAdicionales() != null && !reserva.getServiciosAdicionales().isEmpty()) {
            canvas.drawText("SERVICIOS ADICIONALES", x, y, headerPaint);
            y += lineHeight;
            for (int i = 0; i < reserva.getServiciosAdicionales().size(); i++) {
                String servicio = "• " + reserva.getServiciosAdicionales().get(i).getName() + 
                                " - S/" + reserva.getServiciosAdicionales().get(i).getPrice();
                canvas.drawText(servicio, x, y, bodyPaint);
                y += lineHeight;
            }
            y += lineHeight;
        }
        
        // Información de pago
        canvas.drawText("INFORMACIÓN DE PAGO", x, y, headerPaint);
        y += lineHeight;
        canvas.drawText("Método de pago: " + reserva.getMetodoPago().getCardType(), x, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Tarjeta: " + reserva.getMetodoPago().getMaskedCardNumber(), x, y, bodyPaint);
        y += lineHeight * 2;
        
        // Totales
        canvas.drawText("RESUMEN DE PAGO", x, y, headerPaint);
        y += lineHeight;
        canvas.drawText("Subtotal: S/" + String.format("%.2f", reserva.getSubtotal()), x, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("TOTAL: S/" + String.format("%.2f", reserva.getTotal()), x, y, titlePaint);
        y += lineHeight * 2;
        
        // Pie de página
        canvas.drawText("Gracias por elegir nuestros servicios", x, y + 50, bodyPaint);
        canvas.drawText("Documento generado el: " + getCurrentTimestamp(), x, y + 70, bodyPaint);
        
        // Finalizar página
        pdfDocument.finishPage(page);
        
        // Escribir archivo
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            pdfDocument.writeTo(outputStream);
        }
        
        // Cerrar documento
        pdfDocument.close();
    }
}
# 📱 Guía de Implementación - Notificaciones y Storage Local
## Para el Rol Guía - Android 12 (API 31)

### 🚀 IMPLEMENTACIÓN COMPLETADA

Se ha implementado exitosamente el sistema de **Notificaciones** y **Storage Local** para el rol Guía con las siguientes características:

---

## 📋 FUNCIONALIDADES IMPLEMENTADAS

### 🔔 **NOTIFICACIONES**
1. **Nueva oferta de tour disponible** (Prioridad ALTA)
2. **Tours próximos (1-3 días)** (Recordatorios programables)
3. **Recordatorio de registrar ubicación** (Durante tour activo)
4. **Recordatorio de Check-in** (Fase inicial del tour)
5. **Recordatorio de Check-out** (Fase final del tour)

### 💾 **STORAGE LOCAL**
1. **Configuraciones de notificaciones** (Activar/desactivar por tipo)
2. **Días de anticipación** para recordatorios (1-3 días)
3. **Intervalo de recordatorios de ubicación** (10-30 minutos)
4. **Idiomas del guía** (Persistente entre sesiones)
5. **Nivel de experiencia** y **pago mínimo aceptado**
6. **Configuraciones de la app** (modo oscuro, tipo de mapa, etc.)

---

## 🛠️ ARCHIVOS CREADOS/MODIFICADOS

### ✅ **Archivos Nuevos Creados:**
```
📁 service/
   └── GuiaNotificationService.java        [Gestor de notificaciones]

📁 storage/
   └── GuiaPreferencesManager.java         [Gestor de preferencias locales]

📁 activities/
   └── guia_config_notificaciones.java    [Pantalla de configuración]

📁 res/layout/
   └── guia_config_notificaciones.xml     [Layout de configuración]
```

### ✏️ **Archivos Modificados:**
```
📁 manifests/
   └── AndroidManifest.xml                 [Permisos agregados]

📁 activities/
   └── guia_historial.java                 [Integración completa]
   └── guia_assigned_tours.java            [Métodos de prueba]

📁 res/values/
   └── arrays.xml                          [Opciones de configuración]
```

---

## 🔧 CONFIGURACIÓN REQUERIDA

### 1. **Permisos Agregados** (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### 2. **Compatibilidad Asegurada**
- ✅ Android 12 (API 31)
- ✅ Canales de notificación (API 26+)
- ✅ Permisos runtime (API 33+, compatible con 31)

---

## 🧪 CÓMO PROBAR LAS IMPLEMENTACIONES

### **MÉTODO 1: Pruebas Rápidas desde guia_historial**

1. **Abrir la actividad `guia_historial`**
2. **Realizar las siguientes acciones:**

   **🎯 Nueva Oferta de Tour:**
   - Mantener presionado el **toolbar** por 3 segundos
   - Aparecerá notificación de nueva oferta

   **📅 Recordatorios de Tours (1-3 días):**
   - Mantener presionado el **área de contenido** (ViewPager) por 3 segundos
   - Se enviarán 3 notificaciones: hoy, mañana, en 2 días

   **📍 Recordatorio de Ubicación:**
   - Mantener presionado las **pestañas** (TabLayout) por 3 segundos
   - Aparecerá recordatorio de registrar ubicación

### **MÉTODO 2: Configuración Avanzada** (Opcional)

1. **Crear actividad de configuración:**
   - Implementar `guia_config_notificaciones.java`
   - Permite activar/desactivar notificaciones individualmente
   - Botones de prueba para cada tipo

### **MÉTODO 3: Pruebas desde Tours Asignados**

1. **Abrir `guia_assigned_tours`**
2. **En el código del adaptador, llamar:**
   ```java
   // Para tours en curso
   activity.testNotificationsForTour(tourName, "En Curso");
   
   // Para tours pendientes
   activity.testNotificationsForTour(tourName, "Pendiente");
   
   // Para tours finalizados
   activity.testNotificationsForTour(tourName, "Finalizado");
   ```

---

## 📊 VERIFICACIÓN DEL STORAGE LOCAL

### **Ver Configuraciones Guardadas:**

1. **Abrir Android Studio Logcat**
2. **Filtrar por: `Preferences`**
3. **Al iniciar `guia_historial` verás:**
   ```
   D/Preferences: === CONFIGURACIONES ACTUALES ===
   D/Preferences: Nuevas ofertas: true
   D/Preferences: Recordatorios de tours: true
   D/Preferences: Recordatorios de ubicación: true
   D/Preferences: Recordatorios check-in: true
   D/Preferences: Recordatorios check-out: true
   D/Preferences: Días de anticipación: 2
   D/Preferences: Intervalo ubicación: 15 min
   D/Preferences: Idiomas: [Español, Inglés]
   D/Preferences: Experiencia: Principiante
   D/Preferences: Pago mínimo: S/ 0.0
   D/Preferences: Modo oscuro: false
   D/Preferences: Tipo de mapa: normal
   D/Preferences: ================================
   ```

### **Probar Persistencia:**

1. **Cambiar configuraciones en la app**
2. **Cerrar completamente la app**
3. **Volver a abrir**
4. **Verificar que las configuraciones se mantienen**

---

## 🎯 CASOS DE USO REALES

### **Cuándo se Activarían en Producción:**

1. **Nueva Oferta:** Cuando una empresa publique un tour que coincida con el perfil del guía
2. **Recordatorios de Tours:** Sistema automático basado en tours asignados próximos
3. **Ubicación:** Timer durante tours activos para recordar registrar posición
4. **Check-in:** Al llegar a la hora de inicio del tour
5. **Check-out:** Al finalizar el tiempo estimado del tour

---

## ⚙️ PASOS FINALES PARA PROBAR

### **1. Compilar el Proyecto**
```bash
./gradlew assembleDebug
```

### **2. Ejecutar en Dispositivo/Emulador**
- Android 12+ recomendado
- Permitir notificaciones cuando la app lo solicite

### **3. Verificar Funcionalidad**
- Seguir los métodos de prueba descritos arriba
- Revisar el panel de notificaciones
- Verificar que las configuraciones se guardan

### **4. Debugging**
- Usar **Logcat** para ver mensajes de debug
- Filtros recomendados: `Preferences`, `Notification`, `GuiaNotification`

---

## 🎉 RESULTADO ESPERADO

Al completar las pruebas, deberías ver:

✅ Notificaciones funcionando con diferentes estilos y prioridades  
✅ Navegación correcta al tocar las notificaciones  
✅ Configuraciones persistentes entre sesiones de la app  
✅ Mensajes de confirmación (Toast) al enviar notificaciones  
✅ Log detallado de todas las configuraciones guardadas  

---

## 🔧 SOLUCIÓN DE PROBLEMAS

### **Si no aparecen notificaciones:**
1. Verificar permisos concedidos en Configuración > Apps > Tu App > Notificaciones
2. Revisar que los canales de notificación estén activos
3. Verificar que las configuraciones estén habilitadas

### **Si no se guardan las preferencias:**
1. Revisar Logcat para errores de SharedPreferences
2. Verificar que la app tenga permisos de escritura

### **Para más debugging:**
1. Usar `preferencesManager.exportPreferencesToLog()` para ver el estado actual
2. Activar verbose logging en las configuraciones

---

¡La implementación está completa y lista para pruebas! 🚀
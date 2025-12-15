# 📋 ANÁLISIS DEL SISTEMA ACTUAL DE CANCELACIÓN Y ESTADOS

## 🚨 PROBLEMA PRINCIPAL: LA CANCELACIÓN **NO ES AUTOMÁTICA**

### ❌ Lo que NO está sucediendo:
La cancelación **NO se ejecuta automáticamente** en segundo plano. Los métodos existen pero **NUNCA SE LLAMAN** sin intervención manual.

### ✅ Lo que SÍ está sucediendo:

#### 1. **Cancelación "Semi-automática" en `guia_assigned_tours.java`**
```java
// Línea 183: Se llama al cargar la lista de tours
autoCancelarTourSinInscripcionesVencido(tourAsignado);
```

**Cuándo se ejecuta:**
- ✅ Cuando el guía abre la app y va a la pantalla de Tours Asignados
- ✅ Solo cuando se carga la lista de tours (onResume)
- ❌ **NO** se ejecuta en segundo plano
- ❌ **NO** se ejecuta si nadie abre la app
- ❌ **NO** se ejecuta automáticamente a la hora exacta del tour

**Qué hace:**
1. Verifica si el tour tiene 0 participantes
2. Verifica si ya pasó la **hora de finalización** (horaFin, no horaInicio)
3. Si ambas condiciones se cumplen, cambia el estado a "cancelado"

**Archivo:** `guia_assigned_tours.java` líneas 896-956

---

#### 2. **Método de cancelación en `TourFirebaseService.java`**
```java
// Línea 1428
public void verificarYCancelarTourSinParticipantes(String tourId, OperationCallback callback)
```

**Cuándo se ejecuta:**
- ❌ **NUNCA** se llama automáticamente
- ✅ Solo si alguien lo invoca manualmente desde código
- ✅ Está diseñado para ser llamado por un scheduler, pero **NO HAY SCHEDULER IMPLEMENTADO**

**Qué hace:**
1. Verifica si el tour está en estado `pendiente`, `confirmado` o `programado`
2. Verifica si tiene 0 participantes
3. Si ambas condiciones se cumplen:
   - Cambia estado a "cancelado"
   - **Reduce el pago del guía al 15%** (pagoGuia * 0.15)
   - Agrega `motivoCancelacion`: "Sin participantes inscritos a la hora de inicio"
   - Agrega `fechaCancelacion`: timestamp actual

**Archivo:** `TourFirebaseService.java` líneas 1428-1479

---

#### 3. **Método "Verificar Tours Para Auto-Cancelación" (NUNCA SE USA)**
```java
// Línea 1487
public void verificarToursParaAutoCancelacion(OperationCallback callback)
```

**Cuándo se ejecuta:**
- ❌ **NUNCA** - Este método existe pero no se llama desde ningún lugar
- ✅ Está diseñado para ser llamado por un CRON/Scheduler cada X minutos

**Qué haría (si se implementara):**
1. Busca todos los tours en estados `pendiente`, `confirmado`, `programado`
2. Filtra los que su hora de inicio ya pasó (hace menos de 5 minutos)
3. Para cada uno, llama a `verificarYCancelarTourSinParticipantes()`

**Archivo:** `TourFirebaseService.java` líneas 1487-1549

---

## 📱 ¿QUÉ ES UN SCHEDULER?

Un **scheduler** es un mecanismo que ejecuta código automáticamente en intervalos de tiempo, **sin necesidad de que el usuario abra la app**.

### Opciones para implementar un scheduler en Android:

#### **Opción 1: WorkManager** (Recomendado para Android)
```kotlin
// Ejecuta la verificación cada hora
val workRequest = PeriodicWorkRequestBuilder<TourCancelationWorker>(1, TimeUnit.HOURS)
    .build()
WorkManager.getInstance(context).enqueue(workRequest)
```

**Ventajas:**
- ✅ Funciona incluso si la app está cerrada
- ✅ Respeta las optimizaciones de batería de Android
- ✅ Persiste entre reinicios del dispositivo
- ✅ Garantizado por el sistema operativo

**Desventajas:**
- ⚠️ No es preciso al minuto (puede haber delays de hasta 15 minutos)
- ⚠️ Puede ser pausado por el sistema si hay poca batería

#### **Opción 2: Cloud Functions (Firebase)** (Recomendado para precisión)
```javascript
// Se ejecuta cada 5 minutos en la nube
exports.verificarCancelaciones = functions.pubsub
  .schedule('*/5 * * * *')
  .onRun(async (context) => {
    // Buscar tours sin participantes y cancelarlos
  });
```

**Ventajas:**
- ✅ Se ejecuta en la nube (no depende del dispositivo)
- ✅ Muy preciso (cada X minutos exactos)
- ✅ No consume batería del dispositivo

**Desventajas:**
- ⚠️ Requiere plan Blaze de Firebase (pago)
- ⚠️ Más complejo de configurar

#### **Opción 3: AlarmManager** (No recomendado)
- ⚠️ Puede ser cancelado por el sistema
- ⚠️ No garantizado en Android 6+
- ❌ No recomendado para tareas periódicas

---

## 🔄 ESTADOS DE UN TOUR ASIGNADO

### Estados actuales implementados:

| Estado | Descripción | Cuándo cambia | Archivo responsable |
|--------|-------------|---------------|---------------------|
| `pendiente` | Tour asignado, esperando fecha | Cuando guía acepta oferta | `TourFirebaseService.java` - `crearTourAsignado()` |
| `check_in` | Check-in habilitado, guía puede escanear QR | Cuando guía presiona "Habilitar Check-in" | `guia_assigned_tour_detail.java` - `habilitarCheckIn()` |
| `en_curso` | Tour en progreso | Cuando guía presiona "Empezar Tour" después del check-in | `guia_check_in.java` - `empezarTour()` |
| `check_out` | Check-out habilitado, guía puede escanear QR de salida | Cuando guía presiona "Habilitar Check-out" | `guia_assigned_tour_detail.java` - `habilitarCheckOut()` |
| `completado` | Tour finalizado con éxito | Cuando guía presiona "Terminar Tour" | `guia_check_out.java` - `terminarTour()` |
| `cancelado` | Tour cancelado | Ver sección de cancelación | Varios archivos |

---

## 🚫 MECANISMOS DE CANCELACIÓN ACTUALES

### ❌ **NO HAY CANCELACIÓN MANUAL IMPLEMENTADA**

Actualmente **NO EXISTE** un botón o funcionalidad para que el admin o el guía cancelen un tour manualmente.

### ✅ Cancelación "semi-automática" existente:

#### **Cancelación Tipo 1: Al cargar lista de tours** (implementada)
- **Archivo:** `guia_assigned_tours.java` línea 896
- **Se ejecuta:** Cuando el guía abre la pantalla de Tours Asignados
- **Condiciones:**
  1. Tour tiene 0 participantes
  2. Ya pasó la hora de finalización (horaFin)
- **Acción:** Cambia estado a "cancelado"

#### **Cancelación Tipo 2: Método preparado pero nunca se llama** (existe pero no funciona)
- **Archivo:** `TourFirebaseService.java` línea 1428
- **Se ejecuta:** NUNCA (solo si alguien lo llama manualmente)
- **Condiciones:**
  1. Tour en estado `pendiente`, `confirmado` o `programado`
  2. Tour tiene 0 participantes
- **Acción:**
  - Cambia estado a "cancelado"
  - Reduce pago del guía al 15%
  - Agrega motivo y fecha de cancelación

---

## ⚠️ PROBLEMA DEL CHECK-IN (Tu pregunta)

### 🔍 **¿Por qué puedes presionar "Habilitar Check-in" 2 horas antes del tour?**

**Respuesta:** Porque el botón **NO VALIDA** el tiempo antes de cambiar el estado.

#### Archivo: `guia_assigned_tour_detail.java` línea 393
```java
private void habilitarCheckIn() {
    db.collection("tours_asignados")
        .document(tourId)
        .update("estado", "check_in")  // ❌ Cambia directo sin validar tiempo
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "✅ Check-in habilitado. Ahora puedes mostrar el QR.", Toast.LENGTH_LONG).show();
            loadTourDataFromFirebase();
        })
        // ...
}
```

**Problema identificado:**
- ❌ No valida si faltan más de 10 minutos para el inicio
- ❌ Permite habilitar check-in en cualquier momento
- ✅ Solo muestra el QR si faltan menos de 10 minutos (línea 415)

**Validación actual (solo para mostrar QR, NO para habilitar):**
```java
// Línea 415: Se valida al mostrar QR, NO al habilitar
private void mostrarQRCheckIn() {
    if (!checkInYaHabilitado && !esVentanaValidaParaCheckIn()) {
        long minutosParaInicio = calcularMinutosParaInicio();
        if (minutosParaInicio > 10) {
            Toast.makeText(this, "⏰ El check-in estará disponible 10 minutos antes...");
            return;
        }
    }
    // ... mostrar QR
}
```

---

## 🎯 RESUMEN DE PROBLEMAS ENCONTRADOS

### 1. ❌ Cancelación NO automática
- Los métodos existen pero **nadie los llama automáticamente**
- Se requiere implementar un **scheduler** (WorkManager o Cloud Functions)

### 2. ❌ Check-in sin validación de tiempo
- El botón "Habilitar Check-in" **no valida** si faltan más de 10 minutos
- Permite cambiar a estado `check_in` en cualquier momento
- Solo valida tiempo al **mostrar el QR** (no al cambiar estado)

### 3. ❌ No hay cancelación manual
- Admin no puede cancelar tours
- Guía no puede cancelar tours
- No existe interfaz para cancelaciones manuales

### 4. ❌ Lógica inconsistente
- `autoCancelarTourSinInscripcionesVencido()` verifica **horaFin**
- `verificarYCancelarTourSinParticipantes()` verifica **horaInicio**
- Deberían verificar el mismo punto temporal

---

## ✅ SOLUCIONES RECOMENDADAS

### Prioridad ALTA:
1. **Agregar validación de tiempo al habilitar check-in**
   - Archivo: `guia_assigned_tour_detail.java` línea 393
   - Validar que falten ≤10 minutos antes de cambiar estado

2. **Implementar scheduler con WorkManager**
   - Crear worker que ejecute `verificarToursParaAutoCancelacion()` cada hora
   - Asegura cancelaciones automáticas sin abrir la app

### Prioridad MEDIA:
3. **Unificar lógica de cancelación**
   - Decidir si cancelar a la `horaInicio` o `horaFin`
   - Usar la misma lógica en ambos métodos

4. **Agregar cancelación manual**
   - Botón "Cancelar Tour" en interfaz de admin
   - Calcular compensación según tiempo restante

### Prioridad BAJA:
5. **Migrar a Cloud Functions** (si se requiere precisión al minuto)
   - Ejecuta verificaciones cada 5 minutos en la nube
   - No depende de dispositivos

---

## 📌 CONCLUSIÓN

**El sistema actual:**
- ✅ Tiene los métodos de cancelación preparados
- ❌ **NO** los ejecuta automáticamente
- ⚠️ Solo cancela cuando el guía abre la app
- ❌ Check-in se puede habilitar en cualquier momento (sin validar 10 minutos)

**Lo que se necesita:**
1. Implementar scheduler (WorkManager o Cloud Functions)
2. Agregar validación de tiempo al habilitar check-in
3. (Opcional) Agregar cancelación manual desde UI

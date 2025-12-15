# 🎯 FLUJO "DURANTE EL TOUR" - EXPLICACIÓN COMPLETA

## 📱 SECCIÓN TOUR PRIORITARIO (GUÍA)

### ¿Cuándo aparece el tour prioritario?

El banner de **Tour Prioritario** aparece en la pantalla `guia_assigned_tours` siguiendo estas prioridades:

#### 🔥 **PRIORIDAD 1: Tour en curso** (Máxima prioridad)
- **Estado:** `en_curso`
- **Cuándo:** El guía ya inició el tour (escaneó QRs y presionó "Empezar Tour")
- **Aparece:** Inmediatamente después de iniciar

#### 🛑 **PRIORIDAD 2: Tour en check-out** (Alta prioridad)
- **Estado:** `check_out`
- **Cuándo:** El tour finalizó y el guía habilitó el check-out
- **Aparece:** Cuando el guía presiona "Habilitar Check-out"

#### ✅ **PRIORIDAD 3: Tour con check-in habilitado**
- **Estado:** `check_in`
- **Cuándo:** El guía habilitó el check-in antes del tour
- **Aparece:** Cuando el guía presiona "Habilitar Check-in"

#### ⏰ **PRIORIDAD 4: Tour confirmado próximo a iniciar**
- **Estados:** `confirmado`, `pendiente`, `programado`
- **Cuándo:** **Faltan 10 minutos o menos para la hora de inicio**
- **Aparece:** Automáticamente 10 minutos antes del tour

#### 📋 **Código de referencia:**
```java
// TourFirebaseService.java línea 673-750
public void getTourPrioritario(TourPrioritarioCallback callback) {
    // Busca tours del guía ordenados por fecha
    // Aplica lógica de prioridades
    // Retorna el tour más urgente
}
```

### ⚠️ **IMPORTANTE: Por qué nunca has visto el tour prioritario**

Si nunca has visto la sección de tour prioritario, puede ser por:

1. **No has creado tours de prueba cercanos a la hora actual**
   - Los seeders crean tours en fechas futuras lejanas
   - Necesitas crear un tour para **HOY** y dentro de **10 minutos**

2. **No has habilitado el check-in manualmente**
   - Aunque el tour esté próximo, si no habilitas check-in no aparecerá como prioritario

3. **El tour no está en los estados correctos**
   - Debe estar en `confirmado`, `check_in`, `en_curso`, o `check_out`

---

## 🎮 ACCIONES DEL GUÍA DURANTE EL TOUR

### 1️⃣ **ANTES DEL TOUR (10 minutos antes)**

#### **Acción: Habilitar Check-in**
- **Archivo:** `guia_assigned_tour_detail.java`
- **Método:** `habilitarCheckIn()` línea 393
- **Condición temporal:** ❌ **NO HAY VALIDACIÓN** (problema identificado)
  - Actualmente se puede habilitar en cualquier momento
  - **DEBE validar:** Solo permitir si faltan ≤10 minutos para inicio

**Cambio de estado:**
```
pendiente/confirmado → check_in
```

**Lo que sucede:**
1. Se actualiza `estado` a `check_in` en Firebase
2. El tour aparece como prioritario
3. Se habilita el botón "Mostrar QR Check-in"

---

### 2️⃣ **DURANTE CHECK-IN (10 min antes hasta hora inicio)**

#### **Acción: Mostrar QR para Check-in**
- **Archivo:** `guia_assigned_tour_detail.java`
- **Método:** `mostrarQRCheckIn()` línea 415
- **Condición temporal:** ✅ **Sí valida** - Faltan ≤10 minutos para inicio

**Lo que sucede:**
1. Muestra QR único del tour
2. Clientes escanean el QR para confirmar asistencia
3. Se abre `guia_scan_qr_participants.java` para escanear QRs de clientes

#### **Acción: Escanear QR de participantes**
- **Archivo:** `guia_scan_qr_participants.java`
- **Método:** `procesarQRCode()` línea 476
- **Condición:** Cliente debe tener reserva activa (no cancelada)

**Lo que sucede:**
1. Escanea código QR del cliente
2. Busca reserva en `tours_asignados/{tourId}/participantes/{reservaId}`
3. Actualiza `asistencia` a `true`
4. Incrementa contador `numeroParticipantesConfirmados`

---

### 3️⃣ **AL INICIAR EL TOUR (a la hora de inicio)**

#### **Acción: Empezar Tour**
- **Archivo:** `guia_scan_qr_participants.java`
- **Método:** `iniciarTour()` línea 463
- **Botón:** "Iniciar Tour" (se habilita cuando hay al menos 1 participante confirmado)
- **Condición temporal:** ❌ **NO HAY VALIDACIÓN** (puede iniciar en cualquier momento)

**Cambio de estado:**
```
check_in → en_curso
```

**Lo que sucede:**
1. Actualiza `estado` a `en_curso`
2. Guarda `horaInicioReal` (timestamp actual)
3. El tour se marca como "En Progreso"
4. Se puede acceder a `guia_tour_progress.java`

---

### 4️⃣ **DURANTE EL TOUR**

#### **Pantalla: Progreso del Tour**
- **Archivo:** `guia_tour_progress.java`
- **Acciones disponibles:**
  - Ver mapa con puntos de interés
  - Ver lista de participantes confirmados
  - Ver detalles del tour
  - **Finalizar tour (disponible en cualquier momento)**

**No hay validaciones temporales en esta fase**

---

### 5️⃣ **AL FINALIZAR EL TOUR (después de hora fin)**

#### **Acción: Habilitar Check-out**
- **Archivo:** `guia_assigned_tour_detail.java`
- **Método:** `habilitarCheckOut()` línea 428
- **Condición temporal:** ❌ **NO HAY VALIDACIÓN** (problema identificado)
  - Actualmente se puede habilitar en cualquier momento
  - **DEBE validar:** Solo permitir después de `horaFin` o después de `horaInicioReal + duracion`

**Cambio de estado:**
```
en_curso → check_out
```

**Lo que sucede:**
1. Se actualiza `estado` a `check_out`
2. El tour aparece como prioritario (alta prioridad)
3. Se habilita escaneo de QR de salida

#### **Acción: Escanear QR de salida**
- **Archivo:** `guia_scan_qr_participants.java` (reutiliza la misma pantalla)
- **Método:** Similar a check-in pero actualiza campo de salida

#### **Acción: Terminar Tour**
- **Archivo:** `guia_tour_progress.java`
- **Método:** `finalizarTour()` línea 327
- **Condición:** ✅ Valida que check-out esté habilitado

**Cambio de estado:**
```
check_out → completado
```

**Lo que sucede:**
1. Actualiza `estado` a `completado`
2. Guarda `horaFinReal` (timestamp actual)
3. Calcula duración real del tour
4. ⚠️ **IMPORTANTE:** En este punto debería procesarse el pago al guía

---

## 📊 ESTADOS DEL TOUR Y TRANSICIONES

### Diagrama de Estados

```
[sin_guia] ← Tour creado por admin, sin guía asignado
    ↓ (Admin asigna guía)
[pendiente] ← Guía aceptó, esperando fecha del tour
    ↓ (10 min antes: Guía habilita check-in)
[check_in] ← Check-in habilitado, escaneando QRs
    ↓ (Guía presiona "Empezar Tour")
[en_curso] ← Tour en progreso
    ↓ (Guía habilita check-out)
[check_out] ← Tour terminó, escaneando QRs de salida
    ↓ (Guía presiona "Terminar Tour")
[completado] ← Tour finalizado con éxito
    
[cancelado] ← Puede ocurrir en cualquier momento antes de completado
```

### Estados en Detalle

| Estado | Color UI | Significado | Acciones disponibles |
|--------|----------|-------------|---------------------|
| `sin_guia` | Gris | Tour sin asignar | Admin: Asignar guía |
| `pendiente` | Amarillo | Esperando fecha | Guía: Ver detalles, Cancelar* |
| `check_in` | Azul | Check-in activo | Guía: Escanear QR, Empezar tour |
| `en_curso` | Verde | Tour en progreso | Guía: Ver progreso, Habilitar check-out |
| `check_out` | Naranja | Check-out activo | Guía: Escanear QR salida, Terminar tour |
| `completado` | Verde oscuro | Finalizado | Solo lectura |
| `cancelado` | Rojo | Cancelado | Solo lectura |

*Cancelación manual no está implementada actualmente

---

## 🔍 ¿EL GUÍA VE LOS CAMBIOS DE ESTADO?

### ✅ SÍ - En múltiples lugares:

#### 1. **En la lista de tours asignados** (`guia_assigned_tours.java`)
- Cada tarjeta de tour muestra el estado actual
- El estado se actualiza en tiempo real con listeners de Firebase
- Colores y badges indican el estado visualmente

#### 2. **En el tour prioritario** (banner superior)
- Muestra el estado del tour más urgente
- Cambia de color según el estado
- Se actualiza automáticamente

#### 3. **En el detalle del tour** (`guia_assigned_tour_detail.java`)
- Muestra el estado actual prominentemente
- Los botones cambian según el estado:
  - `pendiente` → Botón "Habilitar Check-in"
  - `check_in` → Botón "Mostrar QR Check-in"
  - `en_curso` → Botón "Ver Progreso del Tour"
  - `check_out` → Botón "Escanear QR Salida"
  - `completado` → Sin botones de acción

#### 4. **En historial** (si existe implementación)
- Tours completados aparecen en historial
- Se pueden filtrar por estado

---

## 👤 EXPERIENCIA DEL CLIENTE

### Estados que ve el cliente:

| Estado del Tour | Lo que ve el cliente | Archivo/Activity |
|-----------------|----------------------|------------------|
| `sin_guia` | "Procesando tu reserva" | `cliente_inicio.java` |
| `pendiente` | "Tour confirmado - Guía asignado" | `cliente_detalle_tour.java` |
| `check_in` | "Check-in disponible" + Botón QR | `cliente_detalle_tour.java` |
| `en_curso` | "Tour en progreso" | `cliente_detalle_tour.java` |
| `check_out` | "Tour finalizando" | `cliente_detalle_tour.java` |
| `completado` | "Tour completado" + Opción de reseña | `cliente_historial.java` |
| `cancelado` | "Tour cancelado" + Motivo | `cliente_historial.java` |

### Acciones del cliente según estado:

#### **Estado: `pendiente` o `check_in`**
- ✅ **Puede cancelar** (si faltan >2h 10min)
- ✅ Ver detalles del tour
- ✅ Ver información del guía
- ✅ Ver punto de encuentro

#### **Estado: `check_in` (10 min antes)**
- ✅ **Mostrar su QR personal** para que guía lo escanee
- ✅ Confirmar asistencia presencial
- **Archivo:** `cliente_detalle_tour.java`
- **Método:** `generarQRParticipante()`

#### **Estado: `en_curso` o `check_out`**
- ❌ **NO puede cancelar** (tour ya comenzó)
- ✅ Ver estado en tiempo real
- ✅ Recibir notificaciones de progreso

#### **Estado: `completado`**
- ✅ **Dejar reseña y calificación**
- ✅ Ver fotos del tour (si se implementa)
- ✅ Descargar recibo de pago
- **Archivo:** `cliente_historial.java` o `cliente_detalle_tour_completado.java`

---

## 🚨 PROBLEMAS IDENTIFICADOS

### 1. **Validaciones temporales faltantes:**
- ❌ `habilitarCheckIn()` no valida que falten ≤10 min
- ❌ `habilitarCheckOut()` no valida que tour haya terminado
- ❌ `iniciarTour()` no valida hora de inicio

### 2. **Cancelación manual no implementada:**
- ❌ No hay botón para que admin cancele tour
- ❌ No hay botón para que guía cancele tour
- ❌ No hay flujo de reembolso al cancelar

### 3. **Cancelación automática limitada:**
- ❌ Solo se ejecuta cuando guía abre la app
- ❌ No hay scheduler para cancelación automática
- ❌ No se cancelan tours sin participantes a tiempo

### 4. **Estados intermedios faltantes:**
- ❌ No hay estado `confirmado` (tours van directo de `pendiente` a `check_in`)
- ❌ No hay estado `rechazado` (cuando guía rechaza oferta)

---

## ✅ RECOMENDACIONES

### Validaciones temporales que debes implementar:

```java
// 1. Habilitar Check-in (10 min antes)
private void habilitarCheckIn() {
    long minutosParaInicio = calcularMinutosParaInicio();
    if (minutosParaInicio > 10) {
        Toast.makeText(this, 
            "El check-in estará disponible 10 minutos antes del tour", 
            Toast.LENGTH_LONG).show();
        return;
    }
    // Continuar con habilitación...
}

// 2. Iniciar Tour (a la hora de inicio o después)
private void iniciarTour() {
    long minutosParaInicio = calcularMinutosParaInicio();
    if (minutosParaInicio > 5) {
        Toast.makeText(this, 
            "Solo puedes iniciar el tour 5 minutos antes de la hora programada", 
            Toast.LENGTH_LONG).show();
        return;
    }
    // Continuar con inicio...
}

// 3. Habilitar Check-out (después de hora fin estimada)
private void habilitarCheckOut() {
    long minutosDesdeFin = calcularMinutosDesdeFin();
    if (minutosDesdeFin < -15) { // Si faltan más de 15 min para hora fin
        Toast.makeText(this, 
            "El check-out estará disponible al finalizar el tour", 
            Toast.LENGTH_LONG).show();
        return;
    }
    // Continuar con habilitación...
}
```

### Implementar cancelación automática:

**Opción A: Cloud Function (Recomendado)**
```javascript
// Firebase Cloud Function que se ejecuta cada 5 minutos
exports.cancelarToursSinParticipantes = functions.pubsub
    .schedule('*/5 * * * *')
    .onRun(async (context) => {
        // Buscar tours que ya pasaron y tienen 0 participantes
        // Cancelarlos automáticamente
    });
```

**Opción B: WorkManager en Android**
```java
// Verificar cada hora desde la app
PeriodicWorkRequest checkToursWork = 
    new PeriodicWorkRequestBuilder<>(
        TourCancelationWorker.class, 
        1, 
        TimeUnit.HOURS
    ).build();
```

---

## 📝 RESUMEN DE TUS PREGUNTAS

### ❓ ¿Cuándo aparece el tour prioritario?
**R:** Aparece 10 minutos antes del tour o cuando el guía habilita check-in/inicia el tour.

### ❓ ¿Cuántas horas antes aparece?
**R:** 10 **MINUTOS** antes (no horas). Es la ventana de check-in.

### ❓ ¿Por qué nunca lo has visto?
**R:** Probablemente no has creado tours para "hoy" dentro de 10 minutos. Los seeders crean tours en fechas futuras.

### ❓ ¿Qué acciones puede realizar el guía?
**R:** 
1. Habilitar check-in
2. Escanear QR de participantes
3. Iniciar tour
4. Ver progreso
5. Habilitar check-out
6. Terminar tour

### ❓ ¿El guía maneja los cambios de estado?
**R:** Sí, el guía es responsable de todos los cambios de estado después de `pendiente`.

### ❓ ¿Cuáles son las condiciones temporales?
**R:** Actualmente **NO HAY** validaciones (problema). Deberían ser:
- Check-in: ≤10 min antes
- Iniciar: ≤5 min antes
- Check-out: Después de hora fin

### ❓ ¿El guía ve los cambios de estado?
**R:** Sí, en la lista de tours, banner prioritario, y detalle del tour.

### ❓ ¿El tour pasa a "finalizado"?
**R:** Sí, pasa a estado `completado` cuando el guía presiona "Terminar Tour".

### ❓ ¿Qué variaciones ve el cliente?
**R:** El cliente ve diferentes estados y acciones:
- Puede mostrar su QR durante check-in
- Puede cancelar antes de 2h 10min
- Ve "Tour en progreso" durante el tour
- Puede dejar reseña al completarse

---

## 🎬 PRÓXIMOS PASOS SUGERIDOS

1. **Implementar validaciones temporales** en los métodos de cambio de estado
2. **Crear tour de prueba para HOY** para ver el sistema en acción
3. **Implementar cancelación automática** con Cloud Functions o WorkManager
4. **Agregar estado `confirmado`** para tours que tienen participantes
5. **Implementar flujo de reseñas** después de `completado`
6. **Procesar pago del guía** automáticamente al completar tour

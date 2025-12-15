# 📋 PLAN DE IMPLEMENTACIÓN - FLUJO COMPLETO TOUR

## ✅ LO QUE ENTENDÍ

### 🎯 FLUJO DEL GUÍA

#### 1. **HABILITAR CHECK-IN (10 minutos antes)**
- ✅ **Validación temporal:** Solo mostrar botón si faltan ≤10 minutos para inicio
- ✅ **Dónde:** `guia_assigned_tour_detail.java` + Banner prioritario
- ✅ **Estado:** `pendiente` → `check_in`

#### 2. **ESCANEAR QR CHECK-IN**
- ✅ Cliente muestra su QR personal
- ✅ Guía escanea QR de cada participante
- ✅ Se incrementa contador `numeroParticipantesConfirmados`

#### 3. **INICIAR TOUR (a la hora de inicio)**
- ✅ **Botón siempre visible** pero valida al presionar:
  - ⏰ Hora actual ≥ hora de inicio del tour
  - 👥 Al menos **50% de participantes** hicieron check-in
- ✅ **Toast explicativo** si no cumple condiciones
- ✅ **Estado:** `check_in` → `en_curso`
- ✅ **Guarda:** `horaInicioReal` (timestamp)

#### 4. **DURANTE EL TOUR - Marcar puntos del recorrido**
- ✅ **Pantalla:** `guia_tour_progress.java` (ya existe)
- ✅ **Funcionalidad actual:**
  - Muestra lista de puntos del itinerario
  - Guía marca checkboxes conforme visita puntos
  - Progreso se guarda en `tours_asignados/{tourId}/itinerario[i].completado`
  - Botón "Finalizar" se habilita cuando **todos los puntos están marcados**

**❓ PREGUNTA 1:** ¿Hay validación geográfica actualmente? Vi código de `RADIUS_VALIDATION_METERS = 100` pero ¿está activo?

**❓ PREGUNTA 2:** Sobre tracking de ubicación en tiempo real:
- ¿Quieres que el guía vaya guardando su ubicación GPS cada X minutos?
- ¿En qué campo del tour se guardaría? ¿`ubicacionActual: {lat, lng, timestamp}`?
- ¿Con qué frecuencia? ¿Cada 2 minutos? ¿5 minutos?

#### 5. **HABILITAR CHECK-OUT (después de cubrir todos los puntos)**
- ✅ **Condición:** Todos los puntos del itinerario marcados
- ⚠️ **DUDA:** ¿Solo eso o también que haya pasado cierto tiempo desde inicio?
- ✅ **Estado:** `en_curso` → `check_out`

#### 6. **ESCANEAR QR CHECK-OUT**
- ✅ Cliente muestra su QR de salida
- ✅ Guía escanea QR de cada participante
- ✅ Se marca `participantes[i].checkOut = true`

#### 7. **FINALIZAR TOUR**
- ✅ **Validación:** Al menos **50% de participantes** hicieron check-out
- ✅ **Estado:** `check_out` → `completado`
- ✅ **Guarda:** `horaFinReal` (timestamp)
- ✅ **Acciones automáticas:**
  1. Crear documento en nueva colección `tours_completados`
  2. Crear pagos en colección `pagos`:
     - Pagos de clientes → empresa (uno por cada participante)
     - Pago de empresa → guía (uno solo con el total)
  3. Dejar de mostrar en `guia_assigned_tours` (filtrar por estado)

**❓ PREGUNTA 3:** Sobre el registro en `tours_completados`:
- ¿Qué campos específicos quieres que tenga?
- ¿Debería incluir: titulo, fecha, duracion, pagoGuia, numeroParticipantes, calificacionPromedio?

**❓ PREGUNTA 4:** Sobre los pagos:
- Pagos clientes → empresa: ¿El `monto` es el precio que pagó cada cliente individualmente?
- Pago empresa → guía: ¿Es el `pagoGuia` que ya está calculado en el tour?
- ¿El `nombreTour` debe ser el título del tour o el ID?

---

### 👤 FLUJO DEL CLIENTE

#### 1. **QR DE CHECK-IN**
- ✅ **Cuándo mostrar:** Estado del tour = `check_in`
- ✅ **Dónde:**
  - En `cliente_detalle_tour.java` (detalle de reserva)
  - **TAMBIÉN** en `cliente_inicio.java` (pantalla principal)
- ✅ **QR contiene:** `{tourId}_{reservaId}_{clienteId}` o similar

#### 2. **QR DE CHECK-OUT**
- ✅ **Cuándo mostrar:** Estado del tour = `check_out`
- ✅ **Dónde:**
  - En `cliente_detalle_tour.java`
  - **TAMBIÉN** en `cliente_inicio.java`
- ✅ **Restricción:** No mostrar después de `horaFin` del tour
  - Para evitar conflictos con siguientes tours

#### 3. **VER UBICACIÓN EN TIEMPO REAL**
- ✅ **Cuándo:** Estado del tour = `en_curso`
- ✅ **Dónde:** `cliente_inicio.java` (pantalla principal)
- ✅ **Qué mostrar:** Mapa con ubicación actual del guía
- ✅ **Actualización:** En tiempo real con listener de Firebase

**❓ PREGUNTA 5:** ¿El mapa debe mostrar también el itinerario completo (todos los puntos)?

#### 4. **PANTALLA INICIO CUANDO NO HAY TOUR ACTIVO**
- ✅ Actualmente: Solo muestra tours recientes disponibles
- ⚠️ **Tu preocupación:** Puede quedar vacía si no hay tours pronto
- **❓ PROPUESTAS - ¿Cuál prefieres?**
  1. **Banner motivacional:** "¡Explora nuevos destinos!" con CTA a tours
  2. **Tours populares:** Mostrar tours más reservados
  3. **Tours por ciudad:** Carrusel de tours agrupados por ciudad
  4. **Historial reciente:** Tus últimos 3 tours completados
  5. **Recomendaciones:** Tours similares a los que ya tomaste

---

### 🏢 FLUJO DEL ADMIN

#### 1. **NUEVA PESTAÑA: "EN CURSO"**
- ✅ **Posición:** Antes de "Borrador", "Sin asignar", "Pendiente"
- ✅ **Filtro:** Tours con estado `check_in`, `en_curso`, o `check_out`
- ✅ **Mostrar:**
  - Estado actual del tour (badge de color)
  - Si está `en_curso`: **Punto actual del itinerario** donde está el guía
  - Hora de inicio real
  - Número de participantes confirmados
  - Nombre del guía asignado
- ✅ **Actualización:** Tiempo real con listeners de Firebase

**❓ PREGUNTA 6:** ¿El admin puede realizar alguna acción sobre estos tours en curso?
- ¿Ver ubicación del guía en mapa?
- ¿Contactar al guía por chat?
- ¿O solo es visualización?

#### 2. **NUEVA PESTAÑA: "FINALIZADOS"**
- ✅ **Posición:** Entre "Confirmados" y "Cancelados"
- ✅ **Filtro:** Tours con estado `completado`
- ✅ **Mostrar:**
  - Fecha de realización
  - Guía que lo completó
  - Número de participantes
  - Monto total generado
  - Calificación promedio (si ya hay reseñas)
- ✅ **Acciones:** Solo lectura (no se pueden editar)

**❓ PREGUNTA 7:** ¿Los tours finalizados deben tener alguna acción disponible?
- ¿Ver reporte detallado?
- ¿Descargar comprobante?
- ¿O completamente bloqueados?

---

## 🔍 ANÁLISIS DE CÓDIGO EXISTENTE

### ✅ **LO QUE YA EXISTE Y FUNCIONA:**

#### 1. **Tracking de puntos del itinerario** (`guia_tour_progress.java`)
```java
// Ya implementado:
- Lista de puntos con checkboxes
- Guardar estado en Firebase: itinerario[i].completado = true
- Barra de progreso visual
- Botón "Finalizar" se habilita cuando todos están marcados
```

#### 2. **Validación geográfica opcional**
```java
RADIUS_VALIDATION_METERS = 100.0; // 100 metros
// Existe el código pero ¿está activo?
```

#### 3. **Generación de QR para cliente**
```java
// En cliente_detalle_tour.java existe:
generarQRParticipante() // Ya implementado
```

#### 4. **Sistema de estados ya funcional**
```java
Estados: pendiente → check_in → en_curso → check_out → completado
```

---

## ⚠️ **LO QUE FALTA IMPLEMENTAR:**

### 🎯 **GUÍA:**
1. ✅ Validación temporal botón "Habilitar Check-in" (≤10 min)
2. ✅ Validación temporal + 50% al "Iniciar Tour"
3. ✅ Tracking ubicación GPS en tiempo real durante tour
4. ✅ Validación 50% check-out para "Finalizar Tour"
5. ✅ Crear documento en `tours_completados`
6. ✅ Crear pagos automáticos al finalizar
7. ✅ Filtrar tours `completado` de la lista

### 👤 **CLIENTE:**
1. ✅ Mostrar QR check-in en `cliente_inicio.java` (no solo en detalle)
2. ✅ Mostrar QR check-out en `cliente_inicio.java`
3. ✅ Validación temporal: no mostrar QR checkout después de horaFin
4. ✅ Mostrar mapa con ubicación en tiempo real del guía
5. ✅ Auto-actualizar pantalla inicio cada X segundos
6. ✅ Contenido para cuando no hay tour activo
7. ✅ Eliminar imagen QR por defecto que mencionaste

### 🏢 **ADMIN:**
1. ✅ Nueva pestaña "En Curso" con filtros y UI
2. ✅ Mostrar punto actual del itinerario en tiempo real
3. ✅ Nueva pestaña "Finalizados"
4. ✅ Diseño de cards para ambas pestañas

---

## 🚨 PREGUNTAS CRÍTICAS ANTES DE IMPLEMENTAR

### **SOBRE TRACKING DE UBICACIÓN:**
**❓ Q1:** ¿Cada cuánto tiempo el guía debe guardar su ubicación GPS?
- Opciones: Cada 1 min / 2 min / 5 min

**❓ Q2:** ¿En qué estructura se guarda en Firebase?
```javascript
// Opción A: Array de ubicaciones
tours_asignados/{tourId}/ubicaciones: [
  {lat: -12.0, lng: -77.0, timestamp: ...},
  {lat: -12.1, lng: -77.1, timestamp: ...}
]

// Opción B: Solo última ubicación
tours_asignados/{tourId}/ubicacionActual: {
  lat: -12.0, 
  lng: -77.0, 
  timestamp: ...
}
```

### **SOBRE COLECCIÓN tours_completados:**
**❓ Q3:** ¿Qué campos debe tener este documento?
```javascript
{
  tourId: "...",
  titulo: "...",
  guiaId: "...",
  guiaNombre: "...",
  empresaId: "...",
  empresaNombre: "...",
  fechaRealizacion: timestamp,
  horaInicioReal: timestamp,
  horaFinReal: timestamp,
  duracionReal: "2h 30min",
  numeroParticipantes: 15,
  pagoGuia: 300,
  pagoEmpresa: 4500,
  // ¿Qué más?
}
```

### **SOBRE PAGOS:**
**❓ Q4:** Confirmar estructura de pagos:
```javascript
// Pago cliente → empresa (uno por participante)
{
  fecha: timestamp,
  monto: 300, // precio que pagó el cliente
  nombreTour: "Full Day Paracas",
  tipoPago: "A Empresa",
  uidUsuarioPaga: "cliente123", // UID del cliente
  uidUsuarioRecibe: "empresa456" // UID del admin/empresa
}

// Pago empresa → guía (uno total)
{
  fecha: timestamp,
  monto: 450, // pagoGuia del tour
  nombreTour: "Full Day Paracas",
  tipoPago: "A Guia",
  uidUsuarioPaga: "empresa456", // UID del admin/empresa
  uidUsuarioRecibe: "guia789" // UID del guía
}
```

**❓ Q5:** ¿Los pagos se crean aunque sean hipotéticos? (no hay integración de pago real)

### **SOBRE VALIDACIÓN 50%:**
**❓ Q6:** Confirmar cálculo del 50%:
```java
int totalParticipantes = tour.getNumeroParticipantesTotal();
int confirmados = tour.getNumeroParticipantesConfirmados();
boolean cumple50 = confirmados >= (totalParticipantes * 0.5);
```

**❓ Q7:** ¿Redondeo hacia arriba o abajo?
- Ejemplo: 7 participantes → ¿50% = 3 o 4?

### **SOBRE PANTALLA INICIO CLIENTE:**
**❓ Q8:** ¿Qué prefieres mostrar cuando no hay tour activo?
- Dame tu preferencia de las 5 opciones que mencioné arriba

**❓ Q9:** Sobre actualización automática:
- ¿Usar Firebase listeners (tiempo real, consume recursos)?
- ¿O polling cada X segundos (menos preciso, menos consumo)?

### **SOBRE PESTAÑAS ADMIN:**
**❓ Q10:** En "En Curso", ¿debe haber acciones disponibles o solo visualización?

**❓ Q11:** En "Finalizados", ¿alguna acción necesaria o completamente read-only?

### **SOBRE VALIDACIONES TEMPORALES:**
**❓ Q12:** Ventanas de tiempo - confirmar:
- Check-in: ≤10 minutos antes de horaInicio
- Iniciar tour: A partir de horaInicio (¿o 5 min antes?)
- Check-out: Después de marcar todos los puntos (¿o también validar tiempo mínimo?)

---

## 📝 ORDEN DE IMPLEMENTACIÓN SUGERIDO

### **FASE 1: Validaciones Temporales (Lo más crítico)**
1. Validar botón "Habilitar Check-in" (≤10 min)
2. Validar botón "Iniciar Tour" (hora + 50%)
3. Validar botón "Finalizar Tour" (50% check-out)

### **FASE 2: Tracking de Ubicación**
1. Implementar servicio de GPS en background
2. Guardar ubicación cada X minutos durante tour
3. Mostrar mapa en `cliente_inicio.java`

### **FASE 3: Finalización y Pagos**
1. Crear colección `tours_completados`
2. Generar pagos automáticamente
3. Filtrar tours completados de listas

### **FASE 4: UI Admin**
1. Pestaña "En Curso"
2. Pestaña "Finalizados"
3. Actualización en tiempo real

### **FASE 5: UX Cliente**
1. QR check-in/out en pantalla inicio
2. Auto-actualización pantalla inicio
3. Contenido cuando no hay tour activo
4. Eliminar QR por defecto

---

## ✅ CONFIRMACIÓN FINAL

**¿Entendí correctamente tu visión del flujo completo?**
**¿Hay algo que malinterpreté o falta agregar?**
**Responde las preguntas numeradas para afinar detalles.**

Una vez confirmes, empezaré la implementación fase por fase. 🚀

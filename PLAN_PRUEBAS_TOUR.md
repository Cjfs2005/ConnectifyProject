# 📋 PLAN DE PRUEBAS - SISTEMA DE TOURS

## 🎯 Objetivo
Validar el flujo completo desde la creación de un tour hasta su finalización, incluyendo las nuevas funcionalidades implementadas:
- Tour prioritario basado en proximidad temporal (≤10 minutos)
- Validación de ventana temporal para QR Check-in
- Auto-cancelación de tours sin participantes

---

## 🔄 FLUJO PRINCIPAL DE PRUEBAS

### 1️⃣ FASE 1: CREACIÓN DE TOUR (Rol: Administrador)

#### Test Case 1.1: Crear Tour Completo
**Objetivo**: Verificar que el administrador puede crear un tour con todos los datos requeridos

**Precondiciones**:
- Usuario autenticado como administrador
- App instalada y funcionando

**Pasos**:
1. Navegar a "Crear Tour"
2. Completar formulario:
   - Título: "Tour Centro Histórico de Lima"
   - Descripción: "Recorrido por los principales monumentos coloniales"
   - Fecha: HOY + 1 hora
   - Hora inicio: Hora actual + 1 hora (ej: si son las 14:00, poner 15:00)
   - Duración: 1 hora
   - Idiomas: Español, Inglés
   - Precio por persona: S/. 50
   - Pago al guía: S/. 100
   - Máximo participantes: 10
3. Agregar itinerario (mínimo 3 puntos):
   - **Plaza de Armas**: -12.046374, -77.042793
   - **Catedral de Lima**: -12.045581, -77.030476
   - **Convento San Francisco**: -12.043333, -77.028333
4. Agregar servicios adicionales: "Agua embotellada", "Entrada a museos"
5. Publicar tour
6. Asignar guías disponibles (seleccionar al menos 2 guías)

**Resultado Esperado**:
- ✅ Tour creado con estado "publicado"
- ✅ Tour aparece en Firebase colección "tours_ofertas"
- ✅ Guías asignados tienen el tour en su subcolección "guias_ofertados"
- ✅ Confirmación visual en pantalla

**Datos a Registrar**:
- ID del tour: `_________________`
- Hora de inicio configurada: `_________________`

---

### 2️⃣ FASE 2: ACEPTACIÓN DE TOUR (Rol: Guía)

#### Test Case 2.1: Visualizar Ofertas Disponibles
**Objetivo**: Verificar que el guía ve el tour en su lista de ofertas

**Precondiciones**:
- Tour creado en Fase 1
- Usuario autenticado como guía asignado

**Pasos**:
1. Abrir app como guía
2. Navegar a "Ofertas"
3. Buscar el tour creado

**Resultado Esperado**:
- ✅ Tour aparece en lista de ofertas
- ✅ Muestra todos los detalles correctamente
- ✅ Botón "Aceptar" disponible

#### Test Case 2.2: Aceptar Oferta de Tour
**Objetivo**: Verificar que el guía puede aceptar un tour

**Pasos**:
1. Click en el tour
2. Revisar detalles del tour
3. Click en "Aceptar Tour"
4. Confirmar aceptación

**Resultado Esperado**:
- ✅ Tour desaparece de "Ofertas"
- ✅ Tour aparece en "Tours Asignados" con estado "confirmado"
- ✅ Documento creado en Firebase colección "tours_asignados"
- ✅ Estado del guía en "guias_ofertados" cambia a "aceptado"

**Datos a Registrar**:
- ID del tour asignado: `_________________`

---

### 3️⃣ FASE 3: INSCRIPCIÓN DE CLIENTE

#### Test Case 3.1: Cliente se Inscribe al Tour
**Objetivo**: Verificar que un cliente puede inscribirse al tour

**Precondiciones**:
- Tour aceptado por guía (Fase 2)
- Usuario autenticado como cliente

**Pasos**:
1. Abrir app como cliente
2. Buscar tour "Tour Centro Histórico de Lima"
3. Ver detalles del tour
4. Click en "Inscribirse"
5. Completar datos de participantes:
   - Nombre: "Juan Pérez"
   - DNI: 12345678
   - Email: juan@ejemplo.com
6. Confirmar inscripción

**Resultado Esperado**:
- ✅ Cliente agregado a array "participantes" en Firebase
- ✅ Cliente tiene propiedad `checkIn: false`
- ✅ Confirmación de inscripción
- ✅ Cliente puede ver el tour en "Mis Tours"

**Datos a Registrar**:
- Número de participantes inscritos: `_________________`

---

### 4️⃣ FASE 4: TOUR PRIORITARIO Y VALIDACIONES TEMPORALES

#### Test Case 4.1: Tour NO Aparece Como Prioritario (>10 minutos)
**Objetivo**: Verificar que el tour NO aparece como prioritario si faltan más de 10 minutos

**Precondiciones**:
- Faltan más de 10 minutos para la hora de inicio del tour
- Tour en estado "confirmado"

**Pasos**:
1. Abrir app como guía
2. Navegar a "Tours Asignados"
3. Observar banner de tour prioritario

**Resultado Esperado**:
- ✅ Banner de tour prioritario NO muestra el tour creado
- ✅ Tour aparece en lista normal con estado "CONFIRMADO"

#### Test Case 4.2: Validación QR Check-in Bloqueado (>10 minutos)
**Objetivo**: Verificar que el QR check-in no está disponible si faltan más de 10 minutos

**Pasos**:
1. Click en el tour
2. Intentar click en "Habilitar Check-in"

**Resultado Esperado**:
- ✅ Mensaje: "⏰ El check-in estará disponible 10 minutos antes del inicio del tour (faltan X minutos)"
- ✅ No se abre pantalla de QR

---

### 5️⃣ FASE 5: INICIO DEL TOUR (≤10 minutos antes)

#### Test Case 5.1: Tour Aparece Como Prioritario
**Objetivo**: Verificar que el tour aparece como prioritario cuando faltan ≤10 minutos

**Precondiciones**:
- **AJUSTAR HORA DEL SISTEMA** o **ESPERAR** hasta 10 minutos antes de la hora configurada
- Tour en estado "confirmado"
- Hay al menos 1 participante inscrito

**Pasos**:
1. Refrescar app (cerrar y abrir)
2. Observar banner de tour prioritario

**Resultado Esperado**:
- ✅ Banner muestra "🎯 Tour Prioritario"
- ✅ Muestra nombre del tour
- ✅ Muestra hora de inicio
- ✅ Botón "Ver Detalles" disponible

#### Test Case 5.2: Habilitar Check-in
**Objetivo**: Verificar que el guía puede habilitar el check-in

**Pasos**:
1. Click en "Ver Detalles" del tour prioritario
2. Click en "Habilitar Check-in"
3. Confirmar acción

**Resultado Esperado**:
- ✅ Estado del tour cambia de "confirmado" → "check_in"
- ✅ Mensaje: "✅ Check-in habilitado. Ahora puedes mostrar el QR."
- ✅ Botón cambia a "Mostrar QR Check-in"

#### Test Case 5.3: Mostrar QR Check-in
**Objetivo**: Verificar que se muestra el código QR correctamente

**Pasos**:
1. Click en "Mostrar QR Check-in"

**Resultado Esperado**:
- ✅ Se abre pantalla con código QR grande
- ✅ Muestra título del tour
- ✅ Muestra contador: "0 de X participantes registrados"
- ✅ Botón "Empezar Tour" deshabilitado (gris)

---

### 6️⃣ FASE 6: CHECK-IN DE PARTICIPANTES

#### Test Case 6.1: Cliente Escanea QR Check-in
**Objetivo**: Verificar que el cliente puede hacer check-in

**Precondiciones**:
- QR Check-in visible en dispositivo del guía
- Cliente con app abierta

**Pasos**:
1. Como cliente, abrir "Mis Tours"
2. Abrir tour activo
3. Click en "Escanear QR Check-in"
4. Escanear QR mostrado por el guía

**Resultado Esperado**:
- ✅ Participante marcado con `checkIn: true` en Firebase
- ✅ Contador en app del guía actualiza automáticamente
- ✅ Mensaje de confirmación al cliente
- ✅ Cuando todos estén registrados, botón "Empezar Tour" se habilita (verde)

---

### 7️⃣ FASE 7: TOUR EN CURSO

#### Test Case 7.1: Iniciar Tour
**Objetivo**: Verificar que el guía puede iniciar el tour

**Precondiciones**:
- Al menos 1 participante con check-in realizado

**Pasos**:
1. Click en "Empezar Tour"
2. Confirmar inicio

**Resultado Esperado**:
- ✅ Estado cambia de "check_in" → "en_curso"
- ✅ Tour prioritario sigue mostrándose
- ✅ Opciones disponibles: "Ver Mapa y Progreso", "Finalizar Tour"

#### Test Case 7.2: Ver Mapa con Itinerario Real
**Objetivo**: Verificar que el mapa muestra el recorrido configurado

**Pasos**:
1. Click en "Ver Mapa"

**Resultado Esperado**:
- ✅ Mapa de Google Maps se abre
- ✅ Se muestran los 3 marcadores configurados:
  - Plaza de Armas
  - Catedral de Lima
  - Convento San Francisco
- ✅ Línea azul conectando los puntos
- ✅ Círculos verdes de proximidad (50m) alrededor de cada punto
- ✅ **NO** se muestran coordenadas hardcodeadas antiguas

---

### 8️⃣ FASE 8: FINALIZACIÓN DEL TOUR

#### Test Case 8.1: Habilitar Check-out
**Objetivo**: Verificar que el guía puede habilitar check-out

**Pasos**:
1. Cuando el tour termine, click en "Finalizar Tour"
2. Confirmar acción

**Resultado Esperado**:
- ✅ Estado cambia de "en_curso" → "check_out"
- ✅ Botón cambia a "Mostrar QR Check-out"

#### Test Case 8.2: Check-out de Participantes
**Objetivo**: Verificar que los clientes pueden hacer check-out

**Pasos**:
1. Click en "Mostrar QR Check-out"
2. Cliente escanea QR con su app

**Resultado Esperado**:
- ✅ Participante marcado con `checkOut: true`
- ✅ Contador actualiza en tiempo real
- ✅ Cuando todos completen, tour pasa a "completado"

---

### 9️⃣ FASE 9: VALIDACIÓN DE AUTO-CANCELACIÓN

#### Test Case 9.1: Tour Sin Participantes a Hora de Inicio
**Objetivo**: Verificar auto-cancelación cuando no hay participantes

**Precondiciones**:
- Crear NUEVO tour con hora de inicio en 5 minutos
- **NO** inscribir ningún cliente
- Esperar a que pase la hora de inicio

**Pasos**:
1. Crear tour con fecha/hora actual + 5 minutos
2. Guía acepta el tour
3. NO inscribir clientes
4. Esperar 5 minutos (o ajustar hora del sistema)
5. Ejecutar verificación automática (o manualmente llamar al método)

**Resultado Esperado**:
- ✅ Estado cambia automáticamente a "cancelado"
- ✅ `pagoGuia` se reduce al 15% del valor original
- ✅ Campo `motivoCancelacion`: "Sin participantes inscritos a la hora de inicio"
- ✅ Tour desaparece de tours prioritarios

**Cálculo del Pago**:
- Pago original: S/. 100
- Pago reducido (15%): S/. 15
- Verificar en Firebase: `_________________`

---

### 🔟 FASE 10: VALIDACIONES DE BLOQUEO TEMPORAL

#### Test Case 10.1: QR Check-in Bloqueado Después de Finalizar
**Objetivo**: Verificar que el QR check-in no está disponible después de hora_inicio + duración

**Precondiciones**:
- Tour con duración de 1 hora
- Hora de fin ya pasada

**Pasos**:
1. Esperar a que pase: hora_inicio + 1 hora
2. Intentar acceder a "Mostrar QR Check-in"

**Resultado Esperado**:
- ✅ Mensaje: "⏰ El check-in ya no está disponible. El tour ha finalizado."
- ✅ No se muestra QR

---

## 📊 RESUMEN DE VALIDACIONES

### Checklist General
- [ ] Tours se crean correctamente con itinerario real
- [ ] Guías ven ofertas y pueden aceptar
- [ ] Clientes pueden inscribirse
- [ ] Tour prioritario aparece SOLO cuando faltan ≤10 minutos
- [ ] QR Check-in bloqueado fuera de ventana temporal (10 min antes hasta hora_fin)
- [ ] Mapa muestra recorrido real (NO coordenadas hardcodeadas)
- [ ] Auto-cancelación funciona cuando no hay participantes
- [ ] Pago del guía se reduce al 15% en cancelación automática
- [ ] Check-in/Check-out en tiempo real
- [ ] Estados cambian correctamente en Firebase

---

## 🐛 REGISTRO DE BUGS/PROBLEMAS

| ID | Descripción | Severidad | Estado |
|----|-------------|-----------|--------|
| 1  |             |           |        |
| 2  |             |           |        |
| 3  |             |           |        |

---

## 📝 NOTAS IMPORTANTES

### Configuración de Tiempo para Pruebas
Para facilitar las pruebas sin esperar tiempos reales:

**Opción 1: Ajustar Hora del Sistema**
- Android: Configuración > Sistema > Fecha y hora > Desactivar "Usar hora de red"
- Ajustar manualmente para simular diferentes momentos

**Opción 2: Crear Tours con Horarios Inmediatos**
- Crear tour con hora de inicio = hora actual + 12 minutos
- Esperar 2 minutos para que entre en ventana de ≤10 minutos

### Datos de Prueba Recomendados
```
Tour 1 (Prueba Normal):
- Hora: Actual + 12 minutos
- Duración: 1 hora
- Participantes: 2-3 inscritos

Tour 2 (Auto-cancelación):
- Hora: Actual + 5 minutos
- Duración: 1 hora
- Participantes: 0 (NO inscribir nadie)

Tour 3 (Validación Temporal):
- Hora: Actual - 2 horas (pasado)
- Duración: 1 hora
- Verificar bloqueos de QR
```

---

## ✅ CRITERIOS DE ÉXITO

El sistema pasa la validación si:
1. ✅ 100% de los Test Cases principales ejecutados sin errores críticos
2. ✅ Tour prioritario funciona con lógica temporal correcta
3. ✅ Auto-cancelación ejecuta correctamente
4. ✅ Mapa muestra itinerario real (no hardcodeado)
5. ✅ Validaciones temporales de QR funcionan
6. ✅ Estados de Firebase se actualizan correctamente
7. ✅ No hay errores de compilación o crashes

---

**Fecha de Última Actualización**: 19 de noviembre de 2025
**Versión del Plan**: 1.0

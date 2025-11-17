# 🎯 Estados de Tours - Definición Clara

## 📋 **Estados Posibles para Tours Asignados**

### **1. 🔸 "programado"**
- **Descripción**: Tour confirmado pero aún no iniciado
- **Cuándo se aplica**:
  - Cuando el guía acepta una oferta
  - Tour está planificado para una fecha futura
  - Antes de hacer check-in

### **2. 🟢 "en_curso"** 
- **Descripción**: Tour actualmente en ejecución
- **Cuándo se aplica**:
  - Cuando el guía presiona "Empezar Tour" en check-in
  - Automáticamente si ya es la hora de inicio y hay check-in
  - Tour está siendo realizado en tiempo real

### **3. 🔵 "completado"**
- **Descripción**: Tour terminado exitosamente
- **Cuándo se aplica**:
  - Cuando el guía completa check-out
  - Todas las actividades del tour han terminado

### **4. 🔴 "cancelado"**
- **Descripción**: Tour cancelado por cualquier motivo
- **Cuándo se aplica**:
  - Por decisión del guía o empresa
  - Por falta de participantes
  - Por condiciones externas

---

## 🎯 **Lógica de Tour Prioritario**

### **PRIORIDAD 1: Tour "en_curso"**
- ✅ **Criterio**: Estado = "en_curso"
- ✅ **Funciones disponibles**: Mapa, Check-out, Detalles
- ✅ **Banner**: Verde intenso

### **PRIORIDAD 2: Tour "programado" que es HOY y ya es hora**
- ✅ **Criterio**: Estado = "programado" + fecha = hoy + hora >= hora_inicio
- ✅ **Funciones disponibles**: Check-in (con botón "Empezar Tour"), Mapa, Detalles

### **PRIORIDAD 3: Tour "programado" que es HOY**
- ✅ **Criterio**: Estado = "programado" + fecha = hoy
- ✅ **Funciones disponibles**: Check-in, Detalles
- ✅ **Banner**: Naranja

### **PRIORIDAD 4: Tour "programado" más próximo**
- ✅ **Criterio**: Estado = "programado" + fecha futura más cercana
- ✅ **Funciones disponibles**: Solo Detalles
- ✅ **Banner**: Azul claro

---

## 🔄 **Flujo de Estados**

```
[OFERTA ACEPTADA] 
        ↓
   📅 "programado"
        ↓ (Guía presiona "Empezar Tour")
   🚀 "en_curso" 
        ↓ (Guía completa check-out)
   ✅ "completado"
```

---

## 🎮 **Funcionalidades por Estado**

| Funcionalidad | programado | en_curso | completado | cancelado |
|---------------|------------|----------|------------|-----------|
| **Detalles**  | ✅         | ✅       | ✅         | ✅        |
| **Mapa**      | ⚠️ (solo hoy) | ✅    | ❌         | ❌        |
| **Check-in**  | ✅         | ❌       | ❌         | ❌        |
| **Check-out** | ❌         | ✅       | ❌         | ❌        |

**Leyenda:**
- ✅ = Disponible
- ❌ = No disponible  
- ⚠️ = Disponible con condiciones

---

## 🎨 **Colores de Banner por Estado**

- 🟢 **"en_curso"**: `#E8F5E8` (Verde claro)
- 🟡 **"programado" (hoy)**: `#FFF3E0` (Naranja claro)  
- 🔵 **"programado" (futuro)**: `#E3F2FD` (Azul claro)
- 🔴 **"cancelado"**: `#FFEBEE` (Rojo claro)
- ⚪ **Default**: `#F5F5F5` (Gris claro)
# 📋 Guía de Uso - Seeders Firebase

## 🎯 **Propósito**
Esta guía explica cómo usar correctamente los seeders para poblar Firebase con datos de prueba sin generar errores o duplicados.

---

## 🔧 **Seeders Disponibles**

### **1. 🏛️ TourAsignadoDataSeeder**
- **Propósito**: Crear tours asignados completos para testing del ciclo de vida
- **Contenido**: 5 tours (2 para hoy, 3 futuros) con estados realistas
- **Estados incluidos**: `pendiente`, `en_curso`
- **Ubicación**: `app/src/main/java/.../data/TourAsignadoDataSeeder.java`

### **2. 🧪 TourHoySeeder** 
- **Propósito**: Crear un tour específico para la fecha actual
- **Uso**: Solo cuando necesites UN tour adicional para hoy
- **Estado**: `pendiente`
- **Ubicación**: `app/src/main/java/.../utils/TourHoySeeder.java`

### **3. 🧹 FirebaseCleanupUtil**
- **Propósito**: Limpiar tours problemáticos con errores de formato
- **Uso**: Solo cuando hay tours corruptos en Firebase
- **Ubicación**: `app/src/main/java/.../utils/FirebaseCleanupUtil.java`

### **4. ⚠️ TestMomentoTourData** (OBSOLETO)
- **Estado**: NO USAR - usa sistema `momentoTour` obsoleto
- **Reemplazo**: Usar `TourAsignadoDataSeeder`

---

## 🚀 **Instrucciones de Uso**

### **Paso 1: Limpiar Firebase (Si es necesario)**
```bash
1. Eliminar colección tours_asignados desde Firebase Console
   O usar: FirebaseCleanupUtil.eliminarToursProblematicos();
```

### **Paso 2: Crear Tours de Prueba**
```java
// En guia_assigned_tours.java - DESCOMENTA SOLO ESTAS 2 LÍNEAS:
TourAsignadoDataSeeder seeder = new TourAsignadoDataSeeder();
seeder.crearToursAsignadosDePrueba();
```

### **Paso 3: Ejecutar App**
1. **Ejecuta** la aplicación UNA VEZ
2. **Verifica** que se crearon 5 tours en Firebase
3. **INMEDIATAMENTE** vuelve a comentar las líneas del paso 2

### **Paso 4: Testing**
- ✅ **Tour pendiente**: Lima (hoy 6/11) - Probar check-in
- ✅ **Tour en curso**: Huacachina (hoy 6/11) - Probar check-out  
- ✅ **Tours futuros**: Cusco, Arequipa, Ica - Verificar estados inactivos

---

## 🎯 **Datos Creados**

### **Tours para HOY (6 noviembre 2025)**
```javascript
// Tour 1: PENDIENTE (Botones Check-in activos)
{
  "titulo": "Lima Histórica - Centro Colonial",
  "estado": "pendiente",
  "fechaRealizacion": "06/11/2025 Timestamp"
}

// Tour 2: EN CURSO (Botones Check-out activos)  
{
  "titulo": "Huacachina Aventura - Dunas y Oasis",
  "estado": "en_curso", 
  "fechaRealizacion": "06/11/2025 Timestamp"
}
```

### **Tours FUTUROS (Estados inactivos)**
- 📅 **20/11/2025**: Machu Picchu (Cusco)
- 📅 **25/11/2025**: Ciudad Blanca (Arequipa)  
- 📅 **30/11/2025**: Oasis Huacachina (Ica)

---

## ✅ **Verificaciones de Calidad**

### **Campos Correctos (SIN errores)**
- ✅ `estado` único (pendiente/en_curso/completado)
- ✅ NO usa `momentoTour` (eliminado)
- ✅ `fechaRealizacion` como `Timestamp`
- ✅ `numeroParticipantesTotal` dinámico
- ✅ Estructura compatible con modelo `TourAsignado`

### **Estados de Testing**
- ✅ `pendiente`: Permite check-in → iniciarTour()
- ✅ `en_curso`: Permite check-out → terminarTour()
- ✅ `completado`: Solo visualización (no implementado en seeder)

---

## ⚠️ **Errores Comunes y Soluciones**

### **Error: Tours duplicados**
**Causa**: Ejecutar seeder múltiples veces
**Solución**: Siempre comentar seeders después de primera ejecución

### **Error: Campo `momentoTour` no encontrado** 
**Causa**: Usar `TestMomentoTourData` obsoleto
**Solución**: Usar solo `TourAsignadoDataSeeder`

### **Error: Deserialización Timestamp**
**Causa**: Tours antiguos con fecha String  
**Solución**: `FirebaseCleanupUtil.eliminarToursProblematicos()`

### **Error: Estado no reconocido**
**Causa**: Estados incorrectos en Firebase
**Solución**: Verificar que solo use: pendiente, check_in, en_curso, check_out, completado

---

## 🎉 **Testing del Ciclo Completo**

### **Flujo de Testing Recomendado**
1. **Ejecutar seeder** (crear tours)
2. **Tour Pendiente** → Botón Check-in → Estado `check_in`
3. **Tour Check-in** → Botón "Empezar Tour" → Estado `en_curso` 
4. **Tour En Curso** → Botón Mapa → Botón "Finalizar" → Estado `check_out`
5. **Tour Check-out** → Botón "Terminar Tour" → Estado `completado`

### **Verificar en Firebase**
- Estados se actualizan en tiempo real
- Sin campos `momentoTour` residuales
- Timestamps correctos para fechas

---

## 🏆 **Estado Final**

Con esta configuración tienes:
- ✅ **Sistema unificado** de estados  
- ✅ **Datos de testing** realistas
- ✅ **Compatibilidad total** con modelo actualizado
- ✅ **Ciclo de vida completo** funcional
- ✅ **Proceso de aceptación** de ofertas compatible

¡Disfruta del sistema completamente funcional! 🚀
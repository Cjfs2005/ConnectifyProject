# 🚀 **Instrucciones de Configuración - Firebase Tours**

## ✅ **Sí, el TourDataSeeder crea automáticamente los documentos en Firebase**

El `TourDataSeeder` que creé inserta automáticamente **3 ofertas de prueba** en la colección `tours_ofertas` con la estructura exacta que definimos. 

### **Estructura de las ofertas creadas:**

```javascript
{
  "titulo": "City Tour Lima Centro Histórico",
  "descripcion": "Explora el corazón colonial de Lima visitando lugares emblemáticos",
  "precio": 65.0,
  "duracion": "4 horas",
  "fechaRealizacion": "15/11/2025",
  "horaInicio": "09:00",
  "horaFin": "13:00",
  "itinerario": [
    {
      "orden": 1,
      "lugar": "Plaza Mayor",
      "horaEstimada": "09:00",
      "actividad": "Inicio del tour en el corazón de Lima colonial"
    },
    // ... más puntos
  ],
  "serviciosAdicionales": [
    {
      "nombre": "Almuerzo en restaurante típico",
      "descripcion": "Comida tradicional peruana en restaurante del centro",
      "esPagado": true,
      "precio": 25.0
    },
    // ... más servicios
  ],
  "empresaId": "YkFFwgnA5Mg5apyDZxPRLDF3OZF3", // Trujillo Tours
  "nombreEmpresa": "Trujillo Tours",
  "correoEmpresa": "trujillotours@gmail.com",
  "pagoGuia": 450.0,
  "idiomasRequeridos": ["Espanol", "Ingles"],
  "consideraciones": "Minimo 1 ano como guia turistico. Conocimiento en historia colonial de Lima.",
  "estado": "publicado",
  "guiaAsignadoId": null,
  "fechaAsignacion": null,
  "fechaCreacion": "timestamp",
  "fechaActualizacion": "timestamp",
  "habilitado": true,
  "perfilCompleto": true
}
```

## 🔧 **Pasos para Configurar y Probar:**

### **1. Crear las Ofertas de Prueba (UNA SOLA VEZ):**

1. **Abre** `guia_tours_ofertas.java`
2. **Busca** la línea comentada:
   ```java
   // TourDataSeeder.crearOfertasDePrueba(); // Descomenta para crear ofertas de prueba
   ```
3. **Descomenta** esa línea:
   ```java
   TourDataSeeder.crearOfertasDePrueba(); // Descomenta para crear ofertas de prueba
   ```
4. **Ejecuta** la aplicación y ve a la sección de ofertas del guía
5. **Inmediatamente después** de que se ejecute, **vuelve a comentar** la línea:
   ```java
   // TourDataSeeder.crearOfertasDePrueba(); // Descomenta para crear ofertas de prueba
   ```

⚠️ **IMPORTANTE**: Solo ejecuta esto UNA VEZ, o se crearán ofertas duplicadas.

### **2. Verificar en Firebase Console:**

1. Ve a **Firebase Console** → Tu proyecto → **Firestore Database**
2. Verifica que se haya creado la colección `tours_ofertas`
3. Confirma que hay **3 documentos** con la estructura correcta:
   - City Tour Lima Centro Histórico (Trujillo Tours)
   - Tour Barranco Bohemio (Lima Tours)
   - Circuito Gastronómico Lima (Santa Anita Tours)

### **3. Probar la Funcionalidad:**

#### **✅ Cargar ofertas desde Firebase:**
1. **Ejecuta** la app como guía
2. **Ve** a la sección "Ofertas de Tours"
3. **Verifica** que se carguen las 3 ofertas desde Firebase
4. **Revisa** los logs en Android Studio para confirmar la conexión

#### **✅ Aceptar una oferta:**
1. **Selecciona** cualquier oferta de la lista
2. **Presiona** el botón "Aceptar" (en el adapter o detail)
3. **Confirma** en el diálogo de aceptación
4. **Verifica** que aparezca el mensaje "¡Tour aceptado exitosamente!"
5. **Comprueba** que la oferta desaparezca de la lista

#### **✅ Verificar en Firebase que se actualizó:**
1. **Ve** a Firebase Console → Firestore
2. **Busca** el documento de la oferta aceptada en `tours_ofertas`
3. **Confirma** que:
   - `estado` cambió de `"publicado"` a `"asignado"`
   - `guiaAsignadoId` tiene el UID del guía actual
   - `fechaAsignacion` tiene un timestamp reciente

### **4. Logs Importantes a Revisar:**

```
TourFirebaseService: Obteniendo ofertas disponibles...
TourFirebaseService: Oferta cargada: City Tour Lima Centro Histórico
TourFirebaseService: Total ofertas cargadas: 3
TourFirebaseService: Guía YbmULw4iJXT41CdCLXV1ktCrfek1 intentando aceptar oferta: [ID]
TourFirebaseService: Oferta actualizada exitosamente
```

## 🛠️ **Solución de Problemas:**

### **❌ No aparecen ofertas:**
- Verifica que Firebase esté conectado correctamente
- Revisa que las ofertas existan en la colección `tours_ofertas`
- Confirma que `estado = "publicado"` y `habilitado = true`

### **❌ Error al aceptar oferta:**
- Verifica que el usuario esté autenticado con Firebase Auth
- Confirma que el UID del guía exista en la colección `usuarios`
- Revisa los permisos de Firestore

### **❌ Compilación con errores:**
- Verifica que todos los archivos se hayan creado correctamente:
  - `models/OfertaTour.java`
  - `services/TourFirebaseService.java`
  - `utils/TourDataSeeder.java`
- Sincroniza el proyecto (Sync Now)

## 📋 **Lo que NO Modificamos:**

- ✅ **Colección `usuarios`**: No se toca, solo se lee
- ✅ **Estructuras existentes**: Solo agregamos nuevas funcionalidades
- ✅ **UI existente**: Mantiene la misma interfaz, pero con datos de Firebase

## 📝 **Próximos Pasos:**

Una vez que verifiques que las ofertas funcionan correctamente, podemos proceder con:
1. Colección `tours_asignados` 
2. Integración completa del mapa con Firebase
3. Funcionalidades adicionales

**¿Está todo funcionando correctamente? ¡Cuéntame cómo te va con las pruebas!** 🚀
# 🌊 Sistema de Gestión — Acuario Marino
## Proyecto Java — POO + Patrones de Diseño (Adapter & Bridge)

---

## 📐 Arquitectura del Proyecto

```
AquariumSystem/
└── src/com/aquarium/
    ├── model/           ← Entidades del dominio
    │   ├── WaterParameters.java
    │   ├── MarineAnimal.java
    │   └── FeedingRecord.java
    │
    ├── adapter/         ← PATRÓN ADAPTER
    │   ├── SensorReader.java          (Target Interface)
    │   ├── LegacyProAquaSensor.java   (Adaptee – sistema legado)
    │   ├── LegacyProAquaAdapter.java  (Adapter – traduce °F→°C, SG→ppt)
    │   ├── AquaSmartIotAdapter.java   (Adapter – sensor moderno IoT)
    │   └── SensorReadException.java
    │
    ├── bridge/          ← PATRÓN BRIDGE
    │   ├── FeedingMethod.java         (Implementor Interface)
    │   ├── ManualFeedingMethod.java   (Concrete Implementor)
    │   ├── AutomaticDispenserMethod.java
    │   ├── DripFeedingMethod.java
    │   └── AnimalFeeder.java          (Abstraction)
    │
    ├── repository/      ← Acceso a datos en memoria
    │   ├── AnimalRepository.java
    │   └── FeedingRecordRepository.java
    │
    ├── service/         ← Lógica de negocio central
    │   └── AquariumService.java
    │
    ├── ui/              ← Interfaz gráfica Swing
    │   ├── AquariumUI.java
    │   └── AnimalTableModel.java
    │
    └── util/
        └── DataSeeder.java
```

---

## 🔌 Patrón ADAPTER — Sensores de Agua

**Problema:** El sistema necesita leer datos de dos sensores incompatibles:
- `LegacyProAquaSensor` (antiguo): devuelve un `String` delimitado por `;`,
  con temperatura en **Fahrenheit** y salinidad como **gravedad específica**.
- `AquaSmartIot` (moderno): API diferente, unidades métricas directas.

**Solución:** La interfaz `SensorReader` define el contrato unificado.
Cada `Adapter` envuelve su sensor y realiza las conversiones necesarias:

```
SensorReader (Target Interface)
    ├── LegacyProAquaAdapter  →  wraps  LegacyProAquaSensor
    │       convierte: °F→°C,  SG→ppt (partes por mil)
    └── AquaSmartIotAdapter   →  wraps  AquaSmartIot API
```

El resto de la aplicación NUNCA conoce el tipo de sensor real.

---

## 🌉 Patrón BRIDGE — Sistema de Alimentación

**Problema:** Existen múltiples **tipos de animales** (con distintas porciones
ideales, comportamientos bajo estrés, etc.) y múltiples **métodos de entrega**
de alimento (manual, dispensador automático, bomba de goteo).
Sin Bridge, necesitaríamos `N × M` subclases.

**Solución:** Separar la abstracción de la implementación:

```
AnimalFeeder (Abstraction)           FeedingMethod (Implementor)
    │ usa  ──────────────────────────────► ManualFeedingMethod
    │                                      AutomaticDispenserMethod
    │                                      DripFeedingMethod
    │
    └── feedAnimal(animal, portion)
           → calcula porción real
           → delega entrega al FeedingMethod
```

Se puede cambiar el método de entrega **en tiempo de ejecución** sin
tocar la lógica de alimentación.

---

## 🧠 Lógica de Negocio Implementada

| Clase | Lógica |
|---|---|
| `WaterParameters.computeHealthScore()` | Puntúa 0–100 ponderando 7 parámetros físico-químicos |
| `MarineAnimal.feed()` | Calcula si la porción es correcta, baja, o excesiva; ajusta hambre y estrés |
| `MarineAnimal.evaluateHealthFromWater()` | Determina estado de salud según diferencia de temperatura ideal, pH y toxinas |
| `MarineAnimal.passTime()` | Incrementa hambre y estrés al simular horas transcurridas |
| `LegacyProAquaAdapter.readParameters()` | Parsea String raw, convierte unidades, lanza excepción si falla |
| `AutomaticDispenserMethod.computeEffectiveAmount()` | Degrada eficiencia según % humedad ambiental |
| `AnimalFeeder.feedAnimal()` | Ajusta porción por hambre (+20%) y enfermedad (−50%) |
| `AquariumService.evaluateWaterAlerts()` | Genera alertas por umbrales de cada parámetro |

---

## ▶️ Cómo Ejecutar

### Linux / macOS
```bash
chmod +x build.sh
./build.sh
```

### Windows
```cmd
build.bat
```

### Requisitos
- Java JDK **17 o superior**
- No requiere dependencias externas

---

## 🖥️ Funciones de la Interfaz

| Función | Descripción |
|---|---|
| **Tomar Medición** | Lee el sensor activo y actualiza las barras de calidad del agua |
| **Alimentar** | Alimenta el animal seleccionado con porción inteligente |
| **Alimentar Todos** | Alimenta todos los animales con hambre ≥ 50% |
| **+3 Horas** | Simula el paso del tiempo: incrementa hambre y estrés |
| **Agregar Animal** | Formulario para registrar un nuevo habitante |
| **Cambiar Sensor** | Demuestra el patrón **Adapter** en tiempo real |
| **Cambiar Método Alim.** | Demuestra el patrón **Bridge** en tiempo real |
| **Calibrar Sensor** | Ejecuta rutina de calibración del sensor activo |
| **Ver Reporte** | Muestra resumen completo del estado del acuario |

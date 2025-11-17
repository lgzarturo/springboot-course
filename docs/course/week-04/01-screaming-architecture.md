# Screaming Architecture

Como hemos desarrollado la aplicación actualmente es basado en una arquitectura hexagonal, podemos observar que la estructura del proyecto y los nombres de los paquetes reflejan claramente las responsabilidades y roles de cada componente. Esta es una manifestación del principio de "Screaming Architecture", que sugiere que la arquitectura de un sistema debe ser evidente a simple vista.

---

## 📋 Tabla de Contenidos

1. [Resumen](#1-resumen)
2. [Análisis de la Estructura Actual](#2-análisis-de-la-estructura-actual)
3. [Principios de Screaming Architecture](#3-principios-de-screaming-architecture)
4. [Comparación: Actual vs. Propuesta](#4-comparación-actual-vs-propuesta)
5. [Estructura Propuesta Detallada](#5-estructura-propuesta-detallada)
6. [Plan de Refactorización](#6-plan-de-refactorización)
7. [ADRs - Architecture Decision Records](#7-adrs---architecture-decision-records)
8. [Guía de Migración con TDD](#8-guía-de-migración-con-tdd)
9. [Ejemplos de Código](#9-ejemplos-de-código)
10. [Checklist de Implementación](#10-checklist-de-implementación)

---

## 1. Resumen

### 🎯 Objetivo

Refactorizar el proyecto Spring Boot Course desde una **arquitectura hexagonal organizada por capas técnicas** hacia una **arquitectura hexagonal con screaming architecture organizada por features de negocio**.

### 🎁 Beneficios Esperados

| Beneficio             | Descripción                                         |
|-----------------------|-----------------------------------------------------|
| **Claridad**          | El propósito de negocio "grita" desde la estructura |
| **Encapsulación**     | Features autocontenidas (domain + adapters juntos)  |
| **Colaboración**      | Equipos pueden trabajar en features independientes  |
| **Escalabilidad**     | Preparado para microservicios                       |
| **Bajo Acoplamiento** | Reduce dependencias entre features                  |
| **Mantenibilidad**    | Fácil eliminar features obsoletas                   |

### 📊 Impacto

- **Código**: 🟡 MEDIO - Refactorización estructural, no lógica
- **Tests**: 🟢 BAJO - Solo cambios en imports
- **Riesgo**: 🟢 BAJO - Validación continua con tests

---

## 2. Análisis de la Estructura Actual

### Estructura Actual (Por Capas Técnicas)

```
src/main/kotlin/com/lgzarturo/springbootcourse/
│
├── config/                          # ⚙️ Configuraciones
│   ├── WebConfig.kt
│   └── OpenApiConfig.kt
│
├── domain/                          # 🎯 Dominio (TODOS mezclados)
│   ├── model/
│   │   ├── Ping.kt                  # Feature: Ping
│   │   ├── Booking.kt               # Feature: Bookings
│   │   └── Room.kt                  # Feature: Rooms
│   │
│   ├── service/
│   │   ├── PingService.kt           # Feature: Ping
│   │   ├── BookingService.kt        # Feature: Bookings
│   │   └── RoomService.kt           # Feature: Rooms
│   │
│   └── port/
│       ├── input/
│       │   ├── PingUseCase.kt       # Feature: Ping
│       │   └── BookingUseCase.kt    # Feature: Bookings
│       └── output/
│           └── BookingRepository.kt # Feature: Bookings
│
├── infrastructure/                  # 🔌 Infraestructura (TODOS mezclados)
│   ├── rest/
│   │   ├── controller/
│   │   │   ├── PingController.kt    # Feature: Ping
│   │   │   └── BookingController.kt # Feature: Bookings
│   │   ├── dto/
│   │   └── mapper/
│   │
│   ├── persistence/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── mapper/
│   │
│   └── exception/
│       └── GlobalExceptionHandler.kt
│
└── shared/                          # 🔧 Compartido
    ├── constant/
    ├── util/
    └── extension/
```

### ❌ Problemas Identificados

#### 1. **No es evidente qué hace la aplicación**

- Al mirar la estructura, solo vemos capas técnicas (`domain`, `infrastructure`)
- No se ve el propósito de negocio (¿gestión hotelera?, ¿e-commerce?, ¿qué?)
- Un desarrollador nuevo no entiende qué hace la app sin leer código

#### 2. **Features dispersas en múltiples paquetes**

Para trabajar en la feature "Bookings", debo editar todos los siguientes archivos:

```
domain/model/Booking.kt
domain/service/BookingService.kt
domain/port/input/BookingUseCase.kt
domain/port/output/BookingRepository.kt
infrastructure/rest/controller/BookingController.kt
infrastructure/rest/dto/BookingRequest.kt
infrastructure/persistence/entity/BookingEntity.kt
infrastructure/persistence/repository/JpaBookingRepository.kt
```

**Problema**: 8 archivos en 7 carpetas diferentes, lo que se vuelve muy complicado de mantener.

#### 3. **Difícil trabajar en paralelo**

- Varios desarrolladores editan los mismos paquetes (`domain/model/`, `infrastructure/rest/`)
- Conflictos frecuentes en Git
- Difícil hacer code reviews por feature

#### 4. **Acoplamiento oculto entre features**

- Fácil que `BookingService` dependa directamente de `RoomService`
- No hay barreras claras entre features
- Difícil detectar violaciones de arquitectura
- Escalabilidad limitada: no se puede extraer una feature a un microservicio

#### 5. **Escalabilidad limitada**

- Difícil extraer una feature a un microservicio
- No hay separación clara para modularización
- Migración a microservicios requiere reescritura completa
- Esto es un problema, que si no se identifica a tiempo, puede ser costoso en el futuro

---

## 3. Principios de Screaming Architecture

### Definición (Uncle Bob)

> *"The architecture should scream the intent of the system, not the frameworks it uses."*  
> — Robert C. Martin (Clean Architecture, 2012)

### 🎯 Principios Fundamentales

#### 1. **Organización por Features/Use Cases**

La estructura debe reflejar **CAPACIDADES DE NEGOCIO**, no capas técnicas.

**❌ Mal** (capas técnicas):
```
controllers/
services/
repositories/
```

**✅ Bien** (features de negocio):
```
bookings/          # "Gestión de Reservas"
rooms/             # "Gestión de Habitaciones"
guests/            # "Gestión de Huéspedes"
payments/          # "Procesamiento de Pagos"
```

#### 2. **Independencia de Frameworks**

El dominio NO debe depender de:
- Spring Framework
- JPA/Hibernate
- REST/HTTP
- Base de datos específica

**Ya lo cumplimos** con arquitectura hexagonal actual, por ese lado, no hay problema y podemos seguir así, lo importante sería **organizar por features**.

#### 3. **Autoexplicativa (Self-Explanatory)**

Un desarrollador nuevo debe entender qué hace la app **en 30 segundos** mirando la estructura:

```
src/main/kotlin/com/lgzarturo/hotelbooking/
├── bookings/       # "Ah, se pueden hacer reservas"
├── rooms/          # "Ah, se gestionan habitaciones"
├── guests/         # "Ah, se registran huéspedes"
└── payments/       # "Ah, se procesan pagos"
```

#### 4. **Encapsulación por Feature**

Todo lo relacionado a una feature vive **junto**:

- Domain models
- Use cases
- Adapters (REST, DB)
- Tests
- Documentación

> De momento con la arquitectura hexagonal, cada archivo está distribuido en su respectiva capa técnica, pero en la propuesta de screaming architecture, cada feature se encuentra en su propio directorio y ahi se encuentran todos los archivos relacionados con ella.

#### 5. **Bajo Acoplamiento entre Features**

Features se comunican por:
- **Interfaces públicas** (puertos)
- **Eventos de dominio** (pub/sub)
- **APIs internas** (DTOs)

**NO** por dependencias directas entre clases, esto es un acierto fundamental y ya lo estamos aplicando con la arquitectura hexagonal.

---

## 4. Comparación: Actual vs. Propuesta

### 🔴 Antes: Organización por Capas Técnicas

```
springbootcourse/
├── config/                    # ¿Qué configura?
├── domain/                    # ¿Dominio de qué?
│   ├── model/                 # ¿Qué modelos?
│   ├── service/               # ¿Qué servicios?
│   └── port/                  # ¿Puertos de qué?
├── infrastructure/            # ¿Infraestructura de qué?
│   ├── rest/                  # ¿Qué endpoints?
│   └── persistence/           # ¿Qué entidades?
└── shared/                    # ¿Compartido entre qué?
```

**Pregunta al mirar esta estructura**: *"¿Qué hace esta aplicación?"*  
**Respuesta**: *"No lo sé, debo leer el código"* ❌

### 🟢 Después: Organización por Features

```
springbootcourse/
├── ping/                      # Feature: Sistema de Ping/Health Check
│   ├── domain/                # Dominio del ping
│   ├── application/           # Casos de uso del ping
│   └── adapters/              # Adaptadores del ping
│       ├── rest/              # API REST del ping
│       └── persistence/       # Persistencia del ping (si aplica)
│
├── bookings/                  # Feature: Gestión de Reservas
│   ├── domain/                # Dominio de reservas
│   │   ├── Booking.kt
│   │   ├── BookingStatus.kt
│   │   └── BookingRules.kt
│   ├── application/           # Casos de uso de reservas
│   │   ├── CreateBookingUseCase.kt
│   │   ├── CancelBookingUseCase.kt
│   │   └── ports/
│   └── adapters/              # Adaptadores de reservas
│       ├── rest/
│       │   ├── BookingController.kt
│       │   └── dto/
│       └── persistence/
│           ├── JpaBookingRepository.kt
│           └── entity/
│
├── rooms/                     # Feature: Gestión de Habitaciones
│   ├── domain/
│   ├── application/
│   └── adapters/
│
├── guests/                    # Feature: Gestión de Huéspedes
│   ├── domain/
│   ├── application/
│   └── adapters/
│
└── shared/                    # SOLO código REALMENTE compartido
    ├── security/              # Seguridad (cross-cutting)
    ├── exception/             # Excepciones globales
    ├── config/                # Configuración de Spring
    └── util/                  # Utilidades genéricas
```

**Pregunta al mirar esta estructura**: *"¿Qué hace esta aplicación?"*  
**Respuesta**: *"Gestiona reservas, habitaciones y huéspedes de un hotel"* ✅

### 📊 Comparativa de Ventajas

Con esta propuesta podemos identificar los siguientes beneficios claros:

| Aspecto                       | Antes (Capas)    | Después (Features)   |
|-------------------------------|------------------|----------------------|
| **Claridad de propósito**     | ❌ No evidente    | ✅ Grita el negocio   |
| **Trabajar en una feature**   | ❌ 7+ carpetas    | ✅ 1 carpeta          |
| **Paralelización de equipos** | ❌ Conflictos     | ✅ Independientes     |
| **Code review**               | ❌ Difícil        | ✅ Por feature        |
| **Testing**                   | ❌ Disperso       | ✅ Junto al código    |
| **Eliminar feature**          | ❌ Buscar en todo | ✅ Eliminar 1 carpeta |
| **Extraer microservicio**     | ❌ Reescritura    | ✅ Copiar feature     |
| **Onboarding nuevo dev**      | ❌ Confuso        | ✅ Intuitivo          |

---

## 5. Estructura Propuesta Detallada

### 📁 Estructura Completa por Features

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/lgzarturo/springbootcourse/
│   │       │
│   │       ├── SpringbootCourseApplication.kt   # Aplicación principal
│   │       │
│   │       ├── ping/                            # ✅ Feature: Ping/Health
│   │       │   ├── domain/
│   │       │   │   ├── Ping.kt
│   │       │   │   └── exceptions/
│   │       │   │       └── PingException.kt
│   │       │   │
│   │       │   ├── application/                 # Casos de uso
│   │       │   │   ├── GetPingUseCase.kt
│   │       │   │   ├── GetPingWithMessageUseCase.kt
│   │       │   │   ├── HealthCheckUseCase.kt
│   │       │   │   └── ports/
│   │       │   │       ├── input/
│   │       │   │       │   └── PingUseCasePort.kt
│   │       │   │       └── output/
│   │       │   │           └── PingRepositoryPort.kt (si aplica)
│   │       │   │
│   │       │   └── adapters/                    # Adaptadores
│   │       │       ├── rest/                    # Entrada: REST
│   │       │       │   ├── PingController.kt
│   │       │       │   ├── dto/
│   │       │       │   │   ├── PingRequest.kt
│   │       │       │   │   └── PingResponse.kt
│   │       │       │   └── mapper/
│   │       │       │       └── PingMapper.kt
│   │       │       │
│   │       │       └── persistence/             # Salida: DB (si aplica)
│   │       │           ├── JpaPingRepository.kt
│   │       │           ├── entity/
│   │       │           │   └── PingEntity.kt
│   │       │           └── mapper/
│   │       │               └── PingEntityMapper.kt
│   │       │
│   │       ├── bookings/                        # ✅ Feature: Reservas
│   │       │   ├── domain/
│   │       │   │   ├── Booking.kt               # Entidad de dominio
│   │       │   │   ├── BookingStatus.kt         # Value Object
│   │       │   │   ├── BookingRules.kt          # Reglas de negocio
│   │       │   │   └── exceptions/
│   │       │   │       ├── BookingNotFoundException.kt
│   │       │   │       ├── InvalidBookingDateException.kt
│   │       │   │       └── RoomNotAvailableException.kt
│   │       │   │
│   │       │   ├── application/
│   │       │   │   ├── CreateBookingUseCase.kt
│   │       │   │   ├── CancelBookingUseCase.kt
│   │       │   │   ├── GetBookingUseCase.kt
│   │       │   │   ├── UpdateBookingUseCase.kt
│   │       │   │   └── ports/
│   │       │   │       ├── input/
│   │       │   │       │   ├── CreateBookingUseCasePort.kt
│   │       │   │       │   └── CancelBookingUseCasePort.kt
│   │       │   │       └── output/
│   │       │   │           ├── BookingRepositoryPort.kt
│   │       │   │           └── RoomAvailabilityPort.kt  # Puerto a otra feature
│   │       │   │
│   │       │   └── adapters/
│   │       │       ├── rest/
│   │       │       │   ├── BookingController.kt
│   │       │       │   ├── dto/
│   │       │       │   │   ├── CreateBookingRequest.kt
│   │       │       │   │   ├── BookingResponse.kt
│   │       │       │   │   └── BookingListResponse.kt
│   │       │       │   └── mapper/
│   │       │       │       └── BookingDtoMapper.kt
│   │       │       │
│   │       │       ├── persistence/
│   │       │       │   ├── JpaBookingRepository.kt
│   │       │       │   ├── entity/
│   │       │       │   │   └── BookingEntity.kt
│   │       │       │   └── mapper/
│   │       │       │       └── BookingEntityMapper.kt
│   │       │       │
│   │       │       └── events/                  # Eventos de dominio
│   │       │           ├── BookingCreatedEvent.kt
│   │       │           └── BookingCancelledEvent.kt
│   │       │
│   │       ├── rooms/                           # ✅ Feature: Habitaciones
│   │       │   ├── domain/
│   │       │   │   ├── Room.kt
│   │       │   │   ├── RoomType.kt
│   │       │   │   ├── RoomAvailability.kt
│   │       │   │   └── exceptions/
│   │       │   │
│   │       │   ├── application/
│   │       │   │   ├── CreateRoomUseCase.kt
│   │       │   │   ├── CheckAvailabilityUseCase.kt
│   │       │   │   └── ports/
│   │       │   │
│   │       │   └── adapters/
│   │       │       ├── rest/
│   │       │       ├── persistence/
│   │       │       └── availability/            # Adaptador para bookings
│   │       │           └── RoomAvailabilityAdapter.kt  # Implementa RoomAvailabilityPort
│   │       │
│   │       ├── guests/                          # ✅ Feature: Huéspedes
│   │       │   ├── domain/
│   │       │   ├── application/
│   │       │   └── adapters/
│   │       │
│   │       └── shared/                          # ⚙️ Solo CROSS-CUTTING
│   │           │
│   │           ├── config/                      # Configuración de Spring
│   │           │   ├── WebConfig.kt
│   │           │   ├── OpenApiConfig.kt
│   │           │   ├── SecurityConfig.kt
│   │           │   └── DatabaseConfig.kt
│   │           │
│   │           ├── security/                    # Seguridad transversal
│   │           │   ├── JwtAuthenticationFilter.kt
│   │           │   └── SecurityUtils.kt
│   │           │
│   │           ├── exception/                   # Manejo global de errores
│   │           │   ├── GlobalExceptionHandler.kt
│   │           │   ├── ErrorResponse.kt
│   │           │   └── ErrorCodes.kt
│   │           │
│   │           ├── events/                      # Infraestructura de eventos
│   │           │   ├── DomainEventPublisher.kt
│   │           │   └── DomainEvent.kt
│   │           │
│   │           ├── util/                        # Utilidades genéricas
│   │           │   ├── DateTimeUtils.kt
│   │           │   ├── StringUtils.kt
│   │           │   └── ValidationUtils.kt
│   │           │
│   │           └── extension/                   # Extension functions
│   │               ├── DateTimeExtensions.kt
│   │               └── CollectionExtensions.kt
│   │
│   └── resources/
│       ├── application.yaml
│       ├── application-dev.yaml
│       └── application-prod.yaml
│
└── test/
    └── kotlin/
        └── com/lgzarturo/springbootcourse/
            │
            ├── ping/                            # Tests de Ping
            │   ├── domain/
            │   │   └── PingTest.kt
            │   ├── application/
            │   │   └── GetPingUseCaseTest.kt
            │   └── adapters/
            │       └── rest/
            │           └── PingControllerTest.kt
            │
            ├── bookings/                        # Tests de Bookings
            │   ├── domain/
            │   │   ├── BookingTest.kt
            │   │   └── BookingRulesTest.kt
            │   ├── application/
            │   │   ├── CreateBookingUseCaseTest.kt
            │   │   └── CancelBookingUseCaseTest.kt
            │   └── adapters/
            │       ├── rest/
            │       │   └── BookingControllerTest.kt
            │       └── persistence/
            │           └── JpaBookingRepositoryTest.kt
            │
            └── shared/
                └── util/
                    └── DateTimeUtilsTest.kt
```

### 🎯 Principios de la Nueva Estructura

#### 1. **Feature como unidad de organización**

Cada feature es **autocontenida**:

- Puede ser entendida independientemente
- Puede ser desarrollada por un equipo diferente
- Puede ser extraída a un microservicio
- Es facil de mover a otro proyecto
- Es trivial de integrar con otras features

#### 2. **Hexagonal dentro de cada feature**

Aquí viene lo importante: **Hexagonal dentro de cada feature**. Para mantener las buenas prácticas que ya usamos en el proyecto, cada feature debe seguir la siguiente estructura:

```
feature/
├── domain/          # Corazón del hexágono (reglas de negocio)
├── application/     # Casos de uso (orquestación)
└── adapters/        # Conectores al mundo exterior
    ├── rest/        # Entrada: HTTP
    ├── persistence/ # Salida: DB
    ├── messaging/   # Salida: Eventos
    └── external/    # Salida: APIs externas
```

#### 3. **Comunicación entre features**

**✅ CORRECTO** - Por interfaces (puertos):

```kotlin
// En bookings/application/ports/output/RoomAvailabilityPort.kt
interface RoomAvailabilityPort {
    fun isRoomAvailable(roomId: String, dates: DateRange): Boolean
}

// En rooms/adapters/availability/RoomAvailabilityAdapter.kt
class RoomAvailabilityAdapter : RoomAvailabilityPort {
    override fun isRoomAvailable(roomId: String, dates: DateRange): Boolean {
        // Implementación
    }
}
```

**❌ INCORRECTO** - Dependencia directa:

```kotlin
// ❌ NO HACER ESTO
class BookingService(
    private val roomService: RoomService  // Dependencia directa a otra feature
) {
    fun createBooking(...) {
        roomService.checkAvailability(...)  // Acoplamiento fuerte
    }
}
```

#### 4. **Shared solo para cross-cutting**

`shared/` debe contener SOLO:

- Configuración de frameworks (Spring, etc.)
- Seguridad transversal
- Manejo global de errores
- Utilidades realmente genéricas
- Infraestructura de eventos

**NO** debe contener:
- ❌ Modelos de dominio
- ❌ Lógica de negocio
- ❌ Servicios específicos

---

## 6. Plan de Refactorización

### 🗓️ Fases del Plan

#### **Fase 1: Análisis y Preparación** (Duración: 1 día)

**Objetivo**: Entender el código actual y preparar el terreno.

**Tareas**:
1. ✅ Identificar todas las features actuales
    - ✅ Ping
    - ⏳ Bookings (futuro del milestone 2)
    - ⏳ Rooms (futuro del milestone 2)
    - ⏳ Guests (futuro del milestone 2)

2. ✅ Documentar dependencias entre features
    - Crear diagrama de dependencias
    - Identificar acoplamiento actual

3. ✅ Crear ADRs (Architecture Decision Records)
    - ADR-001: Adopción de Screaming Architecture
    - ADR-002: Organización por Features
    - ADR-003: Comunicación entre Features

4. ✅ Definir estructura objetivo detallada
    - Convenciones de nombres
    - Estructura de carpetas estándar por feature

5. ✅ Backup del código actual
   ```bash
   git checkout -b backup-before-refactoring
   git push origin backup-before-refactoring
   ```
   
    Este paso es importante, debido a que si algo sale mal, podemos volver a este punto.

---

#### **Fase 2: Crear Nueva Estructura Base** (Duración: 2 horas)

**Objetivo**: Preparar las carpetas y configuración base.

**Tareas**:

1. **Crear estructura de carpetas**
   ```bash
   mkdir -p src/main/kotlin/com/lgzarturo/springbootcourse/ping/{domain,application,adapters}
   mkdir -p src/main/kotlin/com/lgzarturo/springbootcourse/bookings/{domain,application,adapters}
   mkdir -p src/main/kotlin/com/lgzarturo/springbootcourse/shared/{config,security,exception,util}
   ```

2. **Crear `package-info.kt` para cada feature**
   ```kotlin
   // src/main/kotlin/com/lgzarturo/springbootcourse/ping/package-info.kt
   /**
    * Feature: Ping/Health Check
    * 
    * Esta feature proporciona endpoints para verificar el estado de la aplicación.
    * 
    * Casos de uso:
    * - Ping simple
    * - Ping con mensaje personalizado
    * - Health check
    * 
    */
   package com.lgzarturo.springbootcourse.ping
   ```

3. **Actualizar `build.gradle.kts`** (si es necesario)
   ```kotlin
   // Verificar que Gradle reconozca las nuevas rutas
   sourceSets {
       main {
           java {
               srcDirs("src/main/kotlin")
           }
       }
   }
   ```

4. **Configurar Spring para escanear features**
   ```kotlin
   // SpringbootCourseApplication.kt
   @SpringBootApplication(
       scanBasePackages = [
           "com.lgzarturo.springbootcourse.ping",
           "com.lgzarturo.springbootcourse.bookings",
           "com.lgzarturo.springbootcourse.rooms",
           "com.lgzarturo.springbootcourse.shared"
       ]
   )
   class SpringbootCourseApplication
   ```

---

#### **Fase 3: Migración Feature "Ping"** (Duración: 4 horas)

**Objetivo**: Migrar la primera feature como piloto.

##### **Paso 1: Ejecutar tests actuales (baseline)**
```bash
./gradlew test
# Todos los tests deben PASAR ✅
```

##### **Paso 2: Crear estructura de la feature Ping**
```
ping/
├── domain/
│   ├── Ping.kt                      # Modelo de dominio
│   └── exceptions/
│       └── PingException.kt
│
├── application/
│   ├── GetPingUseCase.kt           # Implementación
│   ├── GetPingWithMessageUseCase.kt
│   ├── HealthCheckUseCase.kt
│   └── ports/
│       └── input/
│           └── PingUseCasePort.kt  # Interfaz
│
└── adapters/
    └── rest/
        ├── PingController.kt
        ├── dto/
        │   ├── PingRequest.kt
        │   └── PingResponse.kt
        └── mapper/
            └── PingMapper.kt
```

##### **Paso 3: Mover archivos (con TDD)**

**3.1. Mover domain**
```bash
# Antes:
domain/model/Ping.kt
# Después:
ping/domain/Ping.kt
```

```kotlin
// ping/domain/Ping.kt
package com.lgzarturo.springbootcourse.ping.domain

import java.time.LocalDateTime

/**
 * Entidad de dominio: Ping
 * Representa una respuesta de ping del sistema.
 */
data class Ping(
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val version: String = "1.0.0"
) {
    init {
        require(message.isNotBlank()) { "El mensaje no puede estar vacío" }
    }
}
```

**Test asociado**:
```kotlin
// test/kotlin/com/lgzarturo/springbootcourse/ping/domain/PingTest.kt
package com.lgzarturo.springbootcourse.ping.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class PingTest {
    
    @Test
    fun `should create ping with valid message`() {
        // Given
        val message = "pong"
        
        // When
        val ping = Ping(message = message)
        
        // Then
        assertEquals(message, ping.message)
        assertEquals("1.0.0", ping.version)
    }
    
    @Test
    fun `should fail when message is blank`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            Ping(message = "")
        }
    }
}
```

**Ejecutar test**:
```bash
./gradlew test --tests "PingTest"
# Debe PASAR ✅
```

**3.2. Mover application (casos de uso)**
```kotlin
// ping/application/ports/input/PingUseCasePort.kt
package com.lgzarturo.springbootcourse.ping.application.ports.input

import com.lgzarturo.springbootcourse.ping.domain.Ping

/**
 * Puerto de entrada: Casos de uso de Ping
 */
interface PingUseCasePort {
    fun getPing(): Ping
    fun getPingWithMessage(message: String): Ping
    fun healthCheck(): Map<String, Any>
}
```

```kotlin
// ping/application/GetPingUseCase.kt
package com.lgzarturo.springbootcourse.ping.application

import com.lgzarturo.springbootcourse.ping.application.ports.input.PingUseCasePort
import com.lgzarturo.springbootcourse.ping.domain.Ping
import org.springframework.stereotype.Service

/**
 * Caso de uso: Obtener Ping
 */
@Service
class GetPingUseCase : PingUseCasePort {
    
    override fun getPing(): Ping {
        return Ping(message = "pong")
    }
    
    override fun getPingWithMessage(message: String): Ping {
        return Ping(message = message)
    }
    
    override fun healthCheck(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "service" to "springboot-course",
            "version" to "1.0.0"
        )
    }
}
```

**Test asociado**:
```kotlin
// test/kotlin/com/lgzarturo/springbootcourse/ping/application/GetPingUseCaseTest.kt
package com.lgzarturo.springbootcourse.ping.application

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetPingUseCaseTest {
    
    private val useCase = GetPingUseCase()
    
    @Test
    fun `should return pong`() {
        // When
        val result = useCase.getPing()
        
        // Then
        assertEquals("pong", result.message)
        assertEquals("1.0.0", result.version)
    }
    
    @Test
    fun `should return custom message`() {
        // Given
        val customMessage = "hello"
        
        // When
        val result = useCase.getPingWithMessage(customMessage)
        
        // Then
        assertEquals(customMessage, result.message)
    }
    
    @Test
    fun `should return health check`() {
        // When
        val result = useCase.healthCheck()
        
        // Then
        assertEquals("UP", result["status"])
        assertEquals("springboot-course", result["service"])
    }
}
```

**Ejecutar test**:
```bash
./gradlew test --tests "GetPingUseCaseTest"
# Debe PASAR ✅
```

**3.3. Mover adapters (REST)**
```kotlin
// ping/adapters/rest/dto/PingResponse.kt
package com.lgzarturo.springbootcourse.ping.adapters.rest.dto

import java.time.LocalDateTime

/**
 * DTO: Respuesta de Ping
 */
data class PingResponse(
    val message: String,
    val timestamp: LocalDateTime,
    val version: String
)
```

```kotlin
// ping/adapters/rest/mapper/PingMapper.kt
package com.lgzarturo.springbootcourse.ping.adapters.rest.mapper

import com.lgzarturo.springbootcourse.ping.adapters.rest.dto.PingResponse
import com.lgzarturo.springbootcourse.ping.domain.Ping
import org.springframework.stereotype.Component

/**
 * Mapper: Ping Domain <-> PingResponse DTO
 */
@Component
class PingMapper {
    
    fun toResponse(ping: Ping): PingResponse {
        return PingResponse(
            message = ping.message,
            timestamp = ping.timestamp,
            version = ping.version
        )
    }
}
```

```kotlin
// ping/adapters/rest/PingController.kt
package com.lgzarturo.springbootcourse.ping.adapters.rest

import com.lgzarturo.springbootcourse.ping.adapters.rest.dto.PingResponse
import com.lgzarturo.springbootcourse.ping.adapters.rest.mapper.PingMapper
import com.lgzarturo.springbootcourse.ping.application.ports.input.PingUseCasePort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST: Ping API
 * Adaptador de entrada HTTP para los casos de uso de Ping
 */
@RestController
@RequestMapping("/api/v1/ping")
@Tag(name = "Ping", description = "Endpoints de prueba y health check")
class PingController(
    private val pingUseCase: PingUseCasePort,
    private val pingMapper: PingMapper
) {
    
    @GetMapping
    @Operation(summary = "Ping simple", description = "Retorna 'pong'")
    fun ping(): ResponseEntity<PingResponse> {
        val ping = pingUseCase.getPing()
        return ResponseEntity.ok(pingMapper.toResponse(ping))
    }
    
    @GetMapping("/{message}")
    @Operation(summary = "Ping con mensaje", description = "Retorna el mensaje personalizado")
    fun pingWithMessage(@PathVariable message: String): ResponseEntity<PingResponse> {
        val ping = pingUseCase.getPingWithMessage(message)
        return ResponseEntity.ok(pingMapper.toResponse(ping))
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica el estado del servicio")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        val health = pingUseCase.healthCheck()
        return ResponseEntity.ok(health)
    }
}
```

**Test de integración**:
```kotlin
// test/kotlin/com/lgzarturo/springbootcourse/ping/adapters/rest/PingControllerTest.kt
package com.lgzarturo.springbootcourse.ping.adapters.rest

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class PingControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Test
    fun `GET ping should return pong`() {
        mockMvc.perform(get("/api/v1/ping"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("pong"))
            .andExpect(jsonPath("$.version").value("1.0.0"))
    }
    
    @Test
    fun `GET ping with message should return custom message`() {
        mockMvc.perform(get("/api/v1/ping/hello"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("hello"))
    }
    
    @Test
    fun `GET health should return status UP`() {
        mockMvc.perform(get("/api/v1/ping/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("springboot-course"))
    }
}
```

**Ejecutar test**:
```bash
./gradlew test --tests "PingControllerTest"
# Debe PASAR ✅
```

##### **Paso 4: Eliminar archivos antiguos**
```bash
# Después de verificar que TODOS los tests pasan
rm -rf src/main/kotlin/com/lgzarturo/springbootcourse/domain/model/Ping.kt
rm -rf src/main/kotlin/com/lgzarturo/springbootcourse/domain/service/PingService.kt
rm -rf src/main/kotlin/com/lgzarturo/springbootcourse/infrastructure/rest/controller/PingController.kt
# etc.
```

##### **Paso 5: Ejecutar TODOS los tests**
```bash
./gradlew clean test
# Todos los tests deben PASAR ✅
```

---

#### **Fase 4: Feature "Bookings"** (Duración: 1-2 días)

**Objetivo**: La segunda feature ya se desarrollará con la estructura bien definida.

**Pasos** (igual que Fase 3):
1. Ejecutar tests actuales
2. Crear estructura de la feature
3. Mover domain (con TDD)
4. Mover application (con TDD)
5. Mover adapters (con TDD)
6. Actualizar imports
7. Ejecutar tests (deben pasar)
8. Commit

**Estructura propuesta**:
```
bookings/
├── domain/
│   ├── Booking.kt
│   ├── BookingStatus.kt
│   ├── BookingRules.kt
│   └── exceptions/
│       ├── BookingNotFoundException.kt
│       ├── InvalidBookingDateException.kt
│       └── RoomNotAvailableException.kt
│
├── application/
│   ├── CreateBookingUseCase.kt
│   ├── CancelBookingUseCase.kt
│   ├── GetBookingUseCase.kt
│   └── ports/
│       ├── input/
│       │   ├── CreateBookingUseCasePort.kt
│       │   └── CancelBookingUseCasePort.kt
│       └── output/
│           ├── BookingRepositoryPort.kt
│           └── RoomAvailabilityPort.kt  # Comunicación con feature Rooms
│
└── adapters/
    ├── rest/
    │   ├── BookingController.kt
    │   ├── dto/
    │   └── mapper/
    │
    └── persistence/
        ├── JpaBookingRepository.kt
        ├── entity/
        └── mapper/
```

---

#### **Fase 5: Migración de Config a Shared** (Duración: 2 horas)

**Objetivo**: Mover configuraciones a `shared/config/`. De esta forma, podremos compartirlas entre features.

```bash
# Mover configuraciones
mv config/WebConfig.kt shared/config/
mv config/OpenApiConfig.kt shared/config/
```

**Actualizar imports**:
```kotlin
// Antes:
import com.lgzarturo.springbootcourse.config.WebConfig

// Después:
import com.lgzarturo.springbootcourse.shared.config.WebConfig
```

**Estructura final de shared**:
```
shared/
├── config/
│   ├── WebConfig.kt
│   ├── OpenApiConfig.kt
│   ├── SecurityConfig.kt  (futuro)
│   └── DatabaseConfig.kt  (futuro)
│
├── exception/
│   ├── GlobalExceptionHandler.kt
│   ├── ErrorResponse.kt
│   └── ErrorCodes.kt
│
├── util/
│   └── DateTimeUtils.kt
│
└── extension/
    └── DateTimeExtensions.kt
```

---

#### **Fase 6: Actualizar Documentación** (Duración: 4 horas)

**Tareas**:
1. Actualizar `README.md`
2. Actualizar `ARCHITECTURE.md`
3. Crear `SCREAMING_ARCHITECTURE.md`
4. Actualizar `DEVELOPMENT_GUIDE.md`
5. Crear ejemplos de nuevas features

---

#### **Fase 7: Validación Final** (Duración: 2 horas)

**Checklist de validación**:
- [ ] Todos los tests pasan
- [ ] Aplicación arranca correctamente
- [ ] Todos los endpoints funcionan
- [ ] Swagger UI funciona
- [ ] Actuator funciona
- [ ] Documentación actualizada
- [ ] ADRs creados
- [ ] Code review completado

---

### 📅 Timeline Completo

| Fase                    | Duración | Acumulado    |
|-------------------------|----------|--------------|
| Fase 1: Análisis        | 1 día    | 1 día        |
| Fase 2: Estructura base | 2 horas  | 1.25 días    |
| Fase 3: Migrar Ping     | 4 horas  | 1.75 días    |
| Fase 4: Bookings        | 1-2 días | 3-4 días     |
| Fase 5: Migrar Config   | 2 horas  | 3-4 días     |
| Fase 6: Documentación   | 4 horas  | 3.5-4.5 días |
| Fase 7: Validación      | 2 horas  | 4-5 días     |

**Total estimado**: **4-5 días de trabajo**

---

## 7. ADRs - Architecture Decision Records

### ADR-001: Adopción de Screaming Architecture

**Status**: ✅ Accepted  
**Date**: 2025-11-15  
**Deciders**: Arturo López

#### Context

El proyecto actual usa arquitectura hexagonal organizada por **capas técnicas** (domain, infrastructure, config, shared). Si bien esto mantiene el dominio separado de la infraestructura, **no refleja el propósito de negocio** en la estructura de carpetas.

**Problemas identificados**:
1. Al mirar la estructura, no es evidente qué hace la aplicación
2. Una feature está dispersa en múltiples paquetes
3. Difícil trabajar en paralelo (conflictos en Git)
4. Acoplamiento oculto entre features
5. Difícil extraer features a microservicios

#### Decision

Adoptamos **Screaming Architecture** organizando el código por **features de negocio** en lugar de capas técnicas.

**Estructura propuesta**:
```
springbootcourse/
├── ping/          # Feature: Ping/Health Check
├── bookings/      # Feature: Gestión de Reservas
├── rooms/         # Feature: Gestión de Habitaciones
├── guests/        # Feature: Gestión de Huéspedes
└── shared/        # Solo cross-cutting concerns
```

Cada feature mantiene **arquitectura hexagonal internamente**:
```
feature/
├── domain/        # Dominio puro
├── application/   # Casos de uso
└── adapters/      # Adaptadores (REST, DB, etc.)
```

#### Consequences

**Positivas** ✅:
- Propósito de negocio "grita" desde la estructura
- Features autocontenidas y fáciles de entender
- Equipos pueden trabajar en paralelo sin conflictos
- Fácil extraer features a microservicios
- Mejor onboarding para nuevos desarrolladores

**Negativas** ⚠️:
- Requiere refactorización de código existente
- Cambio de mentalidad del equipo
- Archivos compartidos deben ser cuidadosamente evaluados

**Neutrales** ℹ️:
- No afecta la lógica de negocio (solo estructura)
- Tests se mantienen, solo cambian imports

#### Compliance

Para cumplir con esta decisión:
1. Cada nueva feature DEBE seguir la estructura propuesta
2. Features NO DEBEN depender directamente de otras features
3. Comunicación entre features DEBE ser por interfaces (puertos)
4. `shared/` SOLO para cross-cutting concerns

---

### ADR-002: Organización por Features (Business Capabilities)

**Status**: ✅ Accepted  
**Date**: 2025-11-16  
**Deciders**: Arturo López

#### Context

Necesitamos definir **cómo organizar el código** una vez adoptada Screaming Architecture.

**Opciones consideradas**:
1. Por capas técnicas (actual) ❌
2. Por features de negocio ✅
3. Por módulos funcionales
4. Híbrido (mezcla)

#### Decision

Organizamos el código por **features de negocio** (business capabilities).

**Definición de feature**:
> Una feature es una **capacidad de negocio completa** que puede ser entendida y desarrollada de forma independiente.

**Ejemplos de features**:
- `bookings/` - Gestión de reservas (crear, modificar, cancelar)
- `rooms/` - Gestión de habitaciones (disponibilidad, tipos, precios)
- `guests/` - Gestión de huéspedes (registro, perfil)
- `payments/` - Procesamiento de pagos

**NO son features**:
- ❌ `services/` (es una capa técnica)
- ❌ `controllers/` (es una capa técnica)
- ❌ `utils/` (no es negocio)

#### Consequences

**Positivas** ✅:
- Código organizado por lo que **hace**, no por cómo lo hace
- Fácil entender el alcance de la aplicación
- Features pueden evolucionar independientemente

**Negativas** ⚠️:
- Duplicación de código entre features (a veces necesaria)
- Dificultad para identificar qué es "compartido"

#### Compliance

**Criterios para crear una nueva feature**:
1. ✅ Representa una capacidad de negocio completa
2. ✅ Puede ser explicada a un product owner
3. ✅ Tiene su propio bounded context
4. ✅ Puede ser desarrollada independientemente
5. ✅ Tiene valor de negocio por sí sola

**Ejemplo de validación**:
```
Feature propuesta: "bookings"
¿Es una capacidad de negocio? ✅ Sí ("gestión de reservas")
¿Puede explicarse a un PO? ✅ Sí ("permitir a usuarios reservar habitaciones")
¿Tiene bounded context? ✅ Sí (reserva, fechas, habitación, estado)
¿Puede desarrollarse sola? ✅ Sí (con mocks de rooms)
¿Tiene valor de negocio? ✅ Sí (core del sistema)
Conclusión: ✅ ES UNA FEATURE VÁLIDA
```

---

### ADR-003: Comunicación entre Features

**Status**: ✅ Accepted  
**Date**: 2025-11-17
**Deciders**: Arturo López

#### Context

Features deben ser independientes pero a veces necesitan **comunicarse entre sí**.

**Ejemplo**:
```
Booking (reserva) necesita verificar si Room (habitación) está disponible
```

**Opciones consideradas**:
1. Dependencia directa (inyectar RoomService en BookingService) ❌
2. Por interfaces/puertos (Dependency Inversion) ✅
3. Por eventos de dominio (pub/sub)
4. Por API interna (HTTP)

#### Decision

Features se comunican por **interfaces/puertos** (Dependency Inversion Principle).

**Patrón**:
```kotlin
// En bookings/application/ports/output/RoomAvailabilityPort.kt
package com.lgzarturo.springbootcourse.bookings.application.ports.output

interface RoomAvailabilityPort {
    fun isAvailable(roomId: String, dates: DateRange): Boolean
}

// En bookings/application/CreateBookingUseCase.kt
class CreateBookingUseCase(
    private val roomAvailability: RoomAvailabilityPort  // Dependencia por interfaz
) {
    fun execute(request: CreateBookingRequest): Booking {
        if (!roomAvailability.isAvailable(request.roomId, request.dates)) {
            throw RoomNotAvailableException()
        }
        // ...
    }
}

// En rooms/adapters/availability/RoomAvailabilityAdapter.kt
package com.lgzarturo.springbootcourse.rooms.adapters.availability

@Component
class RoomAvailabilityAdapter(
    private val roomRepository: RoomRepositoryPort
) : RoomAvailabilityPort {  // Implementa la interfaz de bookings
    
    override fun isAvailable(roomId: String, dates: DateRange): Boolean {
        val room = roomRepository.findById(roomId)
        return room.isAvailableFor(dates)
    }
}
```

**Para eventos** (futuro):
```kotlin
// Para comunicación asíncrona
@Component
class BookingEventPublisher(
    private val eventPublisher: ApplicationEventPublisher
) {
    fun publishBookingCreated(booking: Booking) {
        eventPublisher.publishEvent(BookingCreatedEvent(booking))
    }
}

// Listener en otra feature
@Component
class RoomAvailabilityEventListener {
    
    @EventListener
    fun on(event: BookingCreatedEvent) {
        // Actualizar disponibilidad de habitación
    }
}
```

#### Consequences

**Positivas** ✅:
- Features desacopladas (bajo acoplamiento)
- Fácil testear (mockear interfaces)
- Fácil cambiar implementación
- Preparado para microservicios (cambiar interfaz por HTTP)

**Negativas** ⚠️:
- Más interfaces (más código)
- Curva de aprendizaje para desarrolladores junior

#### Compliance

**Reglas**:
1. ✅ Features DEBEN comunicarse por interfaces (puertos)
2. ❌ Features NO DEBEN tener dependencias directas entre sí
3. ✅ Interfaces de comunicación DEBEN estar en el paquete que las necesita
4. ✅ Implementaciones DEBEN estar en el paquete que las provee

**Validación**:
```kotlin
// ❌ INCORRECTO
class BookingService(
    private val roomService: RoomService  // Dependencia directa
)

// ✅ CORRECTO
class BookingService(
    private val roomAvailability: RoomAvailabilityPort  // Dependencia por interfaz
)
```

---

### ADR-004: Qué va en `shared/`

**Status**: ✅ Accepted  
**Date**: 2025-11-17  
**Deciders**: Arturo López

#### Context

Necesitamos definir **qué código pertenece a `shared/`** vs. dentro de features.

**Riesgo**: `shared/` puede convertirse en un "cajón de sastre" donde va todo.

#### Decision

`shared/` contiene **SOLO código cross-cutting** (transversal) que:
1. Es usado por **múltiples features**
2. NO tiene lógica de negocio específica
3. Es infraestructura o utilidades genéricas

**Estructura permitida**:
```
shared/
├── config/              # Configuración de frameworks
│   ├── WebConfig.kt     # CORS, MVC
│   ├── OpenApiConfig.kt # Swagger
│   └── SecurityConfig.kt # Seguridad global
│
├── security/            # Seguridad transversal
│   ├── JwtAuthenticationFilter.kt
│   └── SecurityUtils.kt
│
├── exception/           # Manejo global de errores
│   ├── GlobalExceptionHandler.kt
│   └── ErrorResponse.kt
│
├── events/              # Infraestructura de eventos
│   ├── DomainEventPublisher.kt
│   └── DomainEvent.kt
│
├── util/                # Utilidades genéricas
│   ├── DateTimeUtils.kt
│   └── ValidationUtils.kt
│
└── extension/           # Extension functions genéricas
    └── StringExtensions.kt
```

**NO permitido en `shared/`**:
- ❌ Modelos de dominio específicos
- ❌ Lógica de negocio
- ❌ Servicios de negocio
- ❌ Repositorios
- ❌ DTOs específicos

#### Consequences

**Positivas** ✅:
- `shared/` pequeño y manejable
- Lógica de negocio siempre en features
- Fácil identificar dependencias transversales

**Negativas** ⚠️:
- Puede haber duplicación entre features (a veces es correcto)

#### Compliance

**Test para validar si algo va en `shared/`**:

```
¿Este código es usado por 3+ features? → ✅ Puede ir en shared/
¿Tiene lógica de negocio específica? → ❌ NO va en shared/
¿Es configuración de framework? → ✅ Va en shared/config/
¿Es una utilidad genérica (tipo StringUtils)? → ✅ Va en shared/util/
¿Es un modelo de dominio? → ❌ Va en la feature correspondiente
```

**Ejemplo de validación**:
```kotlin
// Código propuesto: DateRange.kt
data class DateRange(val start: LocalDate, val end: LocalDate)
// ¿Es usado por múltiples features? → ✅ Sí (bookings, rooms, availability)
// ¿Tiene lógica de negocio? → ⚠️ Podría (validaciones de fechas)
// Decisión: ❌ NO va en shared/
// Razón: Es un concepto de dominio (bounded context)
// Solución: Cada feature define su propio DateRange si es necesario,
//  o se crea un módulo "commons" para conceptos compartidos
```

---

## 8. Guía de Migración con TDD

### 🧪 Principios TDD para la Migración

#### 1. **Red-Green-Refactor NO aplica literalmente**
En una refactorización de estructura:
- ✅ **Green**: Tests actuales pasan (baseline)
- 🔄 **Refactor**: Cambiar estructura
- ✅ **Green**: Tests siguen pasando (validación)

#### 2. **Tests como Red de Seguridad**
Los tests existentes son nuestra garantía de que la refactorización no rompe funcionalidad.

**Proceso**:
```bash
# 1. Baseline: Todos los tests DEBEN pasar
./gradlew test
✅ 77 tests passed

# 2. Hacer cambio estructural (mover archivos)
mv domain/model/Ping.kt ping/domain/Ping.kt

# 3. Actualizar imports
# (IntelliJ puede hacer esto automáticamente)

# 4. Ejecutar tests nuevamente
./gradlew test
✅ 77 tests passed (si falla, revertir)

# 5. Commit solo si tests pasan
git commit -m "refactor: move Ping to feature package"
```

#### 3. **Commits Pequeños y Frecuentes**
Cada cambio estructural = 1 commit

**Mal** ❌:
```bash
git commit -m "refactor: restructure entire project"
```

**Bien** ✅:
```bash
git commit -m "refactor(ping): move Ping domain model to ping/domain/"
git commit -m "refactor(ping): move PingUseCase to ping/application/"
git commit -m "refactor(ping): move PingController to ping/adapters/rest/"
```

### 📋 Checklist TDD por Feature

#### **Antes de empezar**
- [ ] Todos los tests actuales pasan
- [ ] Código commiteado (working directory limpio)
- [ ] Branch creada para la migración

#### **Durante la migración**
- [ ] Mover 1 archivo a la vez
- [ ] Actualizar imports
- [ ] Ejecutar tests
- [ ] Si pasan → commit
- [ ] Si fallan → revertir y revisar

#### **Después de la migración**
- [ ] Todos los tests pasan
- [ ] Aplicación arranca
- [ ] Endpoints funcionan (prueba manual)
- [ ] Swagger UI funciona
- [ ] Code review
- [ ] Merge a main

#### **Gradle Tasks útiles**

```bash
# Ejecutar solo tests de una feature
./gradlew test --tests "com.lgzarturo.springbootcourse.ping.*"

# Ejecutar tests con info detallada
./gradlew test --info

# Clean + rebuild
./gradlew clean build

# Ejecutar aplicación
./gradlew bootRun
```

#### **Git Aliases útiles**

```bash
# Agregar a ~/.gitconfig
[alias]
    # Ver archivos modificados
    st = status -sb
    
    # Commit con mensaje
    cm = commit -m
    
    # Ver últimos commits
    lg = log --oneline --graph --decorate --all -10
    
    # Revertir último commit (mantener cambios)
    undo = reset --soft HEAD^
```

### 🔍 Validación de Migración

#### **Checklist de validación por feature**

```bash
# 1. Tests unitarios
./gradlew test --tests "*ping.domain*"
./gradlew test --tests "*ping.application*"

# 2. Tests de integración
./gradlew test --tests "*ping.adapters*"

# 3. Arrancar aplicación
./gradlew bootRun

# 4. Probar endpoints manualmente
# Revisar los archivos dentro de la carpeta http para probar con el cliente de intellij o Postman
```

---

## 9. Ejemplos de Código

### 🏗️ Estructura Completa de una Feature

```
bookings/
├── domain/
│   ├── Booking.kt
│   ├── BookingStatus.kt
│   ├── BookingRules.kt
│   └── exceptions/
│       ├── BookingNotFoundException.kt
│       ├── InvalidBookingDateException.kt
│       └── RoomNotAvailableException.kt
│
├── application/
│   ├── CreateBookingUseCase.kt
│   ├── CancelBookingUseCase.kt
│   ├── GetBookingUseCase.kt
│   └── ports/
│       ├── input/
│       │   ├── CreateBookingUseCasePort.kt
│       │   └── CancelBookingUseCasePort.kt
│       └── output/
│           ├── BookingRepositoryPort.kt
│           └── RoomAvailabilityPort.kt
│
└── adapters/
    ├── rest/
    │   ├── BookingController.kt
    │   ├── dto/
    │   │   ├── CreateBookingRequest.kt
    │   │   ├── BookingResponse.kt
    │   │   └── BookingListResponse.kt
    │   └── mapper/
    │       └── BookingDtoMapper.kt
    │
    └── persistence/
        ├── JpaBookingRepository.kt
        ├── entity/
        │   └── BookingEntity.kt
        └── mapper/
            └── BookingEntityMapper.kt
```

## 10. Checklist de Implementación

### ✅ Fase 1: Análisis y Preparación

- [x] Identificar features actuales
- [x] Documentar dependencias
- [x] Crear ADRs
- [x] Definir estructura objetivo
- [x] Backup del código

### ⏳ Fase 2: Crear Estructura Base

- [ ] Crear carpetas por features
- [ ] Crear `package-info.kt` para cada feature
- [ ] Configurar Gradle
- [ ] Actualizar Spring Boot scanner

### ⏳ Fase 3: Migrar Feature Ping

- [ ] Ejecutar tests (baseline)
- [ ] Crear estructura `/ping/`
- [ ] Mover `domain/` con tests
- [ ] Mover `application/` con tests
- [ ] Mover `adapters/rest/` con tests
- [ ] Actualizar imports
- [ ] Ejecutar tests (validación)
- [ ] Eliminar archivos antiguos
- [ ] Commit

### ⏳ Fase 4: Migrar Feature Bookings

- [ ] Ejecutar tests (baseline)
- [ ] Crear estructura `/bookings/`
- [ ] Mover `domain/` con tests
- [ ] Mover `application/` con tests
- [ ] Mover `adapters/` con tests
- [ ] Actualizar imports
- [ ] Ejecutar tests (validación)
- [ ] Commit

### ⏳ Fase 5: Migrar Config a Shared

- [ ] Mover `config/` a `shared/config/`
- [ ] Mover `exception/` a `shared/exception/`
- [ ] Actualizar imports
- [ ] Ejecutar tests

### ⏳ Fase 6: Actualizar Documentación

- [ ] Actualizar `README.md`
- [ ] Actualizar `ARCHITECTURE.md`
- [ ] Crear `SCREAMING_ARCHITECTURE.md`
- [ ] Actualizar `DEVELOPMENT_GUIDE.md`
- [ ] Actualizar diagramas

### ⏳ Fase 7: Validación Final

- [ ] Todos los tests pasan
- [ ] Aplicación arranca
- [ ] Endpoints funcionan
- [ ] Swagger UI funciona
- [ ] Actuator funciona
- [ ] Code review
- [ ] Merge a main

---

## 📚 Referencias

### Screaming Architecture
- [The Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Screaming Architecture - Uncle Bob](https://blog.cleancoder.com/uncle-bob/2011/09/30/Screaming-Architecture.html)

### Hexagonal Architecture
- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [DDD, Hexagonal, Onion, Clean, CQRS, … How I put it all together](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/)

### Package by Feature
- [Package by Feature - Package by Component](https://phauer.com/2020/package-by-feature/)
- [Organizing Code - Simon Brown](https://www.codingthearchitecture.com/2015/03/08/package_by_component_and_architecturally_aligned_testing.html)

### Spring Boot Best Practices
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Boot Project Structure Best Practices](https://medium.com/the-resonant-web/spring-boot-2-0-project-structure-and-best-practices-part-2-7137bdcba7d3)

---

## 🎯 Conclusiones

### Ventajas de Screaming Architecture

1. **Claridad inmediata**: El propósito de negocio es evidente
2. **Encapsulación completa**: Todo lo de una feature vive junto
3. **Equipos paralelos**: Sin conflictos en Git
4. **Microservicios ready**: Fácil extraer features
5. **Mantenibilidad**: Eliminar feature = eliminar carpeta

### Desafíos

1. **Curva de aprendizaje**: Cambio de mentalidad
2. **Duplicación**: A veces es necesaria y correcta
3. **Comunicación entre features**: Requiere disciplina

### Recomendaciones Finales

1. ✅ Migrar incrementalmente (feature por feature)
2. ✅ Mantener tests pasando en todo momento
3. ✅ Commits pequeños y frecuentes
4. ✅ Documentar decisiones (ADRs)
5. ✅ Code review cuidadoso

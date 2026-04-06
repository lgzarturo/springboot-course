# Plan de Migración: Hexagonal → MVC por Features

## Contexto

El proyecto actualmente usa arquitectura hexagonal con organización por capas.
Esto genera:

- 5-6 niveles de anidación por feature (`adapters/rest/dto/`,
  `application/ports/output/`, etc.)
- Interfaces "port" que solo tienen una implementación (ceremonia sin valor)
- Clases "adapter" que Spring ya resuelve con `@RestController` y `@Repository`
- Stubs vacíos (`cart/`, `gamification/`, `payments/`) al mismo nivel que
  `shared/`
- Un programador nuevo necesita entender la arquitectura antes de entender el
  código

## Principios de la Nueva Estructura

1. **Un paquete = una feature**. Todo lo relacionado con una feature vive junto.
2. **Máximo 2 niveles de profundidad** dentro de una feature. `hotels/dto/` es
   el máximo.
3. **Sin ports artificiales**. El service es la capa de negocio. Si necesitas
   abstracción del repositorio, usa una interfaz simple.
4. **Sin adapters**. Spring ya separa REST de persistence con sus propias
   anotaciones.
5. **Sin config por feature**. Usa `@Service`, `@Repository`, `@Component`
   directamente.
6. **Eliminar stubs vacíos**. No crees paquetes para features que no existen
   aún.

---

## Estructura Objetivo

```
com.lgzarturo.springbootcourse/
│
├── SpringbootCourseApplication.kt          ← Entry point
│
├── config/                                 ← Infraestructura transversal
│   ├── OpenApiConfig.kt                    ← Swagger/OpenAPI
│   └── WebConfig.kt                        ← CORS, interceptores globales
│
├── common/                                 ← Componentes reutilizables
│   ├── exception/
│   │   ├── ErrorResponse.kt                ← DTO de error estandarizado
│   │   └── GlobalExceptionHandler.kt       ← @RestControllerAdvice global
│   ├── pagination/
│   │   ├── PageRequest.kt                  ← Wrapper de Spring Pageable
│   │   ├── PageResult.kt                   ← Resultado paginado genérico
│   │   └── SortOrder.kt                    ← Dirección de ordenamiento
│   ├── constants/
│   │   └── AppConstants.kt                 ← Constantes de la app
│   └── extensions/
│       └── DateTimeExtensions.kt           ← Extensiones de Kotlin
│
└── features/                               ← Features de negocio
    │
    ├── hotels/                             ← Feature completa y autocontenida
    │   ├── HotelController.kt
    │   ├── HotelService.kt
    │   ├── HotelRepository.kt              ← Implementación Spring Data JPA
    │   ├── HotelEntity.kt                  ← Entidad JPA
    │   ├── Hotel.kt                        ← Modelo de dominio
    │   ├── HotelSearchCriteria.kt          ← Criterios de búsqueda
    │   └── dto/
    │       ├── CreateHotelRequest.kt
    │       ├── UpdateHotelRequest.kt
    │       ├── HotelResponse.kt
    │       └── PageResponse.kt
    │
    ├── examples/                           ← Feature de referencia (renombrado)
    │   ├── ExampleController.kt
    │   ├── ExampleService.kt
    │   ├── ExampleRepository.kt
    │   ├── ExampleEntity.kt
    │   ├── Example.kt
    │   └── dto/
    │       ├── ExampleRequest.kt
    │       ├── ExamplePatchUpdate.kt
    │       └── ExampleResponse.kt
    │
    ├── ping/                               ← Feature simple de health check
    │   ├── PingController.kt
    │   ├── PingService.kt
    │   └── dto/
    │       └── PingResponse.kt
    │
    ├── users/                              ← Feature con value objects
    │   ├── UserController.kt
    │   ├── UserService.kt
    │   ├── UserRepository.kt
    │   ├── User.kt
    │   ├── UserRole.kt
    │   ├── CreateUserCommand.kt
    │   ├── exceptions/
    │   │   └── DuplicateEmailException.kt
    │   ├── valueobjects/
    │   │   ├── Email.kt
    │   │   ├── Password.kt
    │   │   ├── PhoneNumber.kt
    │   │   └── UserId.kt
    │   └── dto/
    │       ├── UpdateUserRequest.kt
    │       └── UserResponse.kt
    │
    ├── rooms/                              ← Feature parcial (solo persistence)
    │   ├── RoomEntity.kt
    │   ├── RoomJpaRepository.kt
    │   └── Room.kt
    │
    └── sentry/                             ← Feature de monitoreo
        └── SentryController.kt
```

---

## Reglas de Organización

### Paquetes de Infraestructura (`config/`, `common/`)

| Paquete              | Qué contiene                                                  | Qué NO contiene                                   |
| -------------------- | ------------------------------------------------------------- | ------------------------------------------------- |
| `config/`            | Configuración global de Spring (CORS, OpenAPI, Jackson, etc.) | Configuración específica de una feature           |
| `common/exception/`  | Manejo global de errores, DTOs de error                       | Excepciones de dominio específicas de una feature |
| `common/pagination/` | Utilidades de paginación reutilizables                        | Lógica de paginación específica de una feature    |
| `common/constants/`  | Constantes usadas por múltiples features                      | Constantes internas de una feature                |
| `common/extensions/` | Extensiones de Kotlin de uso general                          | Extensiones específicas de una feature            |

### Paquetes de Features (`features/`)

| Archivo          | Responsabilidad                                    | Annotations típicas                                  |
| ---------------- | -------------------------------------------------- | ---------------------------------------------------- |
| `*Controller.kt` | Endpoints REST, valida input, delega al service    | `@RestController`, `@RequestMapping`                 |
| `*Service.kt`    | Lógica de negocio, orquesta repositorios           | `@Service`                                           |
| `*Repository.kt` | Acceso a datos, Spring Data JPA                    | `@Repository` (interface que extiende JpaRepository) |
| `*Entity.kt`     | Entidad JPA, mapeo a tabla                         | `@Entity`, `@Table`                                  |
| `*.kt` (modelo)  | Modelo de dominio puro, sin dependencias de Spring | Data class simple                                    |
| `dto/*`          | DTOs de request/response                           | Data classes con validaciones                        |
| `valueobjects/`  | Value objects del dominio                          | `@JvmInline value class`                             |
| `exceptions/`    | Excepciones específicas de la feature              | Exception subclasses                                 |

### Qué se ELIMINA

| Concepto Hexagonal                            | Por qué se elimina                                    | Reemplazo                               |
| --------------------------------------------- | ----------------------------------------------------- | --------------------------------------- |
| `adapters/rest/`                              | Spring ya sabe que un `@RestController` es un adapter | Archivo directo en la feature           |
| `adapters/persistence/`                       | Spring ya sabe que un `@Repository` es un adapter     | Archivo directo en la feature           |
| `application/ports/input/`                    | Interfaces con una sola implementación                | El service ES la interfaz               |
| `application/ports/output/`                   | Interfaces con una sola implementación                | El repository ES la interfaz            |
| `config/*Config.kt` por feature               | Overkill para wiring simple                           | `@Service`, `@Repository`, `@Component` |
| `*Mapper.kt` como componente                  | Overkill para mapeos simples                          | Companion object `fromDomain()`         |
| Stubs vacíos (`cart/`, `gamification/`, etc.) | YAGNI - You Aren't Gonna Need It                      | Se crean cuando se necesiten            |

---

## Plan de Ejecución por Fases

### Fase 1: Crear estructura base y mover infraestructura

**Acciones:**

1. Crear directorio `features/`
2. Mover `shared/config/OpenApiConfig.kt` → `config/OpenApiConfig.kt`
3. Mover `shared/config/WebConfig.kt` → `config/WebConfig.kt`
4. Mover `shared/exception/` → `common/exception/`
5. Mover `shared/domain/` → `common/pagination/`
6. Mover `shared/constant/` → `common/constants/`
7. Mover `shared/extension/` → `common/extensions/`
8. Eliminar directorio `shared/`
9. Eliminar stubs vacíos: `cart/`, `gamification/`, `payments/`, `pokemon/`,
   `reservations/`, `reviews/`, `services/`

**Actualizar imports:** Todos los archivos que referencien `shared.*` →
`common.*` o `config.*`

### Fase 2: Migrar `hotels/` → `features/hotels/`

**Estructura actual (13 archivos, 5 niveles):**

```
hotels/
  adapters/rest/HotelController.kt
  adapters/rest/dto/request/CreateHotelRequest.kt
  adapters/rest/dto/request/UpdateHotelRequest.kt
  adapters/rest/dto/response/HotelResponse.kt
  adapters/rest/dto/response/PageResponse.kt
  adapters/persistence/HotelJpaRepository.kt
  adapters/persistence/HotelRoomJpaRepository.kt
  adapters/persistence/entity/HotelEntity.kt
  application/ports/output/HotelRepositoryPort.kt
  domain/Hotel.kt
  domain/HotelSearchCriteria.kt
  service/HotelService.kt
  config/HotelServiceConfig.kt
```

**Estructura objetivo (10 archivos, 2 niveles):**

```
features/hotels/
  HotelController.kt
  HotelService.kt
  HotelRepository.kt              ← HotelRoomJpaRepository renombrado
  HotelJpaRepository.kt
  HotelEntity.kt
  Hotel.kt
  HotelSearchCriteria.kt
  dto/CreateHotelRequest.kt
  dto/UpdateHotelRequest.kt
  dto/HotelResponse.kt
  dto/PageResponse.kt
```

**Acciones:**

1. Mover `HotelController.kt` → `features/hotels/HotelController.kt`
2. Mover `dto/*` → `features/hotels/dto/*`
3. Mover `HotelJpaRepository.kt` → `features/hotels/HotelJpaRepository.kt`
4. Renombrar `HotelRoomJpaRepository.kt` → `HotelRepository.kt` y mover a
   `features/hotels/`
5. Mover `HotelEntity.kt` → `features/hotels/HotelEntity.kt`
6. Mover `Hotel.kt` → `features/hotels/Hotel.kt`
7. Mover `HotelSearchCriteria.kt` → `features/hotels/HotelSearchCriteria.kt`
8. Mover `HotelService.kt` → `features/hotels/HotelService.kt`
9. **Eliminar** `HotelRepositoryPort.kt` (redundante con `HotelRepository`)
10. **Eliminar** `HotelServiceConfig.kt` (agregar `@Service` a `HotelService`)
11. Eliminar directorios vacíos: `adapters/`, `application/`, `config/`,
    `domain/`, `service/`

**Actualizar imports en:**

- `HotelController.kt` (dto, service, domain)
- `HotelService.kt` (repository, domain)
- `HotelRepository.kt` (entity, JPA repository, domain)
- Tests de hotels

### Fase 3: Migrar `example/` → `features/examples/`

**Estructura actual (12 archivos, 5 niveles):**

```
example/
  adapters/rest/ExampleController.kt
  adapters/rest/dto/request/ExampleRequest.kt
  adapters/rest/dto/request/ExamplePatchUpdate.kt
  adapters/rest/dto/response/ExampleResponse.kt
  adapters/persistence/ExampleJpaRepository.kt
  adapters/persistence/ExampleRepositoryAdapter.kt
  adapters/persistence/entity/ExampleEntity.kt
  application/ports/input/ExampleUseCasePort.kt
  application/ports/output/ExampleRepositoryPort.kt
  domain/Example.kt
  service/ExampleServicePort.kt
  config/ExampleServiceConfig.kt
```

**Estructura objetivo (9 archivos, 2 niveles):**

```
features/examples/
  ExampleController.kt
  ExampleService.kt                 ← ExampleServicePort renombrado
  ExampleRepository.kt              ← ExampleRepositoryAdapter renombrado
  ExampleJpaRepository.kt
  ExampleEntity.kt
  Example.kt
  dto/ExampleRequest.kt
  dto/ExamplePatchUpdate.kt
  dto/ExampleResponse.kt
```

**Acciones:**

1. Mover y renombrar archivos al nivel `features/examples/`
2. **Eliminar** `ExampleUseCasePort.kt` (redundante con `ExampleService`)
3. **Eliminar** `ExampleRepositoryPort.kt` (redundante con `ExampleRepository`)
4. **Eliminar** `ExampleServiceConfig.kt` (agregar `@Service` a
   `ExampleService`)
5. Actualizar `ExampleController` para depender de `ExampleService` directamente
6. Actualizar imports en tests

### Fase 4: Migrar `ping/` → `features/ping/`

**Estructura actual (7 archivos, 4 niveles):**

```
ping/
  adapters/rest/PingController.kt
  adapters/rest/dto/PingResponse.kt
  adapters/rest/mapper/PingMapper.kt
  application/ports/input/PingUseCasePort.kt
  domain/Ping.kt
  service/PingService.kt
  config/PingConfig.kt
```

**Estructura objetivo (4 archivos, 2 niveles):**

```
features/ping/
  PingController.kt
  PingService.kt
  Ping.kt
  dto/PingResponse.kt
```

**Acciones:**

1. Mover `PingController.kt` → `features/ping/PingController.kt`
2. Mover `PingService.kt` → `features/ping/PingService.kt`
3. Mover `Ping.kt` → `features/ping/Ping.kt`
4. Mover `dto/PingResponse.kt` → `features/ping/dto/PingResponse.kt`
5. **Eliminar** `PingMapper.kt` (mover lógica a companion object en
   `PingResponse`)
6. **Eliminar** `PingUseCasePort.kt` (redundante con `PingService`)
7. **Eliminar** `PingConfig.kt` (agregar `@Service` a `PingService`)
8. Actualizar imports en tests

### Fase 5: Migrar `users/` → `features/users/`

**Estructura actual (13 archivos, 4 niveles):**

```
users/
  adapters/rest/UserController.kt
  adapters/rest/dto/request/UpdateUserRequest.kt
  adapters/rest/dto/response/UserResponse.kt
  adapters/rest/mapper/UserMapper.kt
  adapters/persistence/UserRepository.kt
  application/ports/input/GetUserUseCase.kt
  application/ports/input/UpdateUserUseCase.kt
  domain/User.kt
  domain/UserRole.kt
  domain/CreateUserCommand.kt
  domain/exceptions/DuplicateEmailException.kt
  domain/valueobjects/*.kt (4 archivos)
  service/CreateUserService.kt
  service/PasswordEncoder.kt
```

**Estructura objetivo (12 archivos, 3 niveles):**

```
features/users/
  UserController.kt
  UserService.kt                    ← CreateUserService renombrado
  UserRepository.kt
  User.kt
  UserRole.kt
  CreateUserCommand.kt
  PasswordEncoder.kt
  exceptions/
    DuplicateEmailException.kt
  valueobjects/
    Email.kt
    Password.kt
    PhoneNumber.kt
    UserId.kt
  dto/
    UpdateUserRequest.kt
    UserResponse.kt
```

**Acciones:**

1. Mover archivos al nivel `features/users/`
2. **Eliminar** `GetUserUseCase.kt` y `UpdateUserUseCase.kt` (redundantes con
   `UserService`)
3. **Eliminar** `UserMapper.kt` (mover lógica a companion object en
   `UserResponse`)
4. Actualizar imports en tests

### Fase 6: Migrar `rooms/` → `features/rooms/`

**Estructura actual (3 archivos, 3 niveles):**

```
rooms/
  adapters/persistence/RoomJpaRepository.kt
  adapters/persistence/entity/RoomEntity.kt
  domain/Room.kt
```

**Estructura objetivo (3 archivos, 1 nivel):**

```
features/rooms/
  RoomJpaRepository.kt
  RoomEntity.kt
  Room.kt
```

**Acciones:**

1. Mover todos los archivos al nivel `features/rooms/`
2. Eliminar directorios vacíos

### Fase 7: Migrar `sentry/` → `features/sentry/`

**Estructura actual (2 archivos, 2 niveles):**

```
sentry/
  adapters/rest/SentryController.kt
  PackageInfo.kt
```

**Estructura objetivo (1 archivo, 1 nivel):**

```
features/sentry/
  SentryController.kt
```

**Acciones:**

1. Mover `SentryController.kt` → `features/sentry/SentryController.kt`
2. Eliminar directorios vacíos

### Fase 8: Reorganizar tests

**Estructura objetivo:**

```
src/test/kotlin/com/lgzarturo/springbootcourse/
│
├── config/
│   └── TestcontainersConfiguration.kt
│
├── common/
│   └── MockkTestConfig.kt
│
├── features/
│   ├── hotels/
│   │   ├── HotelControllerTest.kt
│   │   ├── HotelServiceTest.kt
│   │   ├── HotelRepositoryTest.kt
│   │   ├── HotelRepositoryIntegrationTest.kt
│   │   ├── HotelIntegrationTest.kt
│   │   └── HotelE2ETest.kt
│   │
│   ├── examples/
│   │   ├── ExampleControllerTest.kt
│   │   └── ExampleServiceTest.kt
│   │
│   ├── ping/
│   │   ├── PingControllerTest.kt
│   │   └── PingServiceTest.kt
│   │
│   └── users/
│       ├── UserControllerTest.kt
│       ├── UserTest.kt
│       └── CreateUserServiceTest.kt
│
├── BaseIntegrationTest.kt
├── DatabaseIntegrationTest.kt
└── SpringbootCourseApplicationTests.kt
```

**Acciones:**

1. Mover tests de `hotels/adapters/rest/` → `features/hotels/`
2. Mover tests de `hotels/service/` → `features/hotels/`
3. Mover tests de `hotels/adapters/persistence/` → `features/hotels/`
4. Mover tests de `hotels/integration/` → `features/hotels/`
5. Mover tests de `example/adapters/rest/` → `features/examples/`
6. Mover tests de `example/service/` → `features/examples/`
7. Mover tests de `ping/adapters/rest/` → `features/ping/`
8. Mover tests de `ping/service/` → `features/ping/`
9. Mover tests de `users/` → `features/users/`
10. Actualizar package declarations e imports en todos los tests
11. Mover `MockkTestConfig.kt` → `common/MockkTestConfig.kt`
12. Mover `TestcontainersConfiguration.kt` →
    `config/TestcontainersConfiguration.kt`

### Fase 9: Verificación final

1. Ejecutar `.\gradlew clean test` y verificar que los 127 tests pasan
2. Ejecutar `.\gradlew compileKotlin` y verificar que no hay errores
3. Ejecutar `.\gradlew ktlintFormat` para formatear código
4. Revisar manualmente que no queden directorios vacíos
5. Verificar que no haya imports rotos

---

## Comparación Antes/Después

### Métricas de Complejidad

| Métrica                         | Antes (Hexagonal)                | Después (MVC Features)                      | Mejora |
| ------------------------------- | -------------------------------- | ------------------------------------------- | ------ |
| **Niveles máximos en hotels/**  | 5 (`adapters/rest/dto/request/`) | 2 (`dto/`)                                  | -60%   |
| **Archivos en hotels/**         | 13                               | 11                                          | -15%   |
| **Paquetes raíz**               | 14 (incluyendo 7 stubs)          | 5 (config, common, features, rooms, sentry) | -64%   |
| **Interfaces "port"**           | 6 (una por feature)              | 0                                           | -100%  |
| **Clases "config" por feature** | 4 (una por feature)              | 0                                           | -100%  |
| **Clases "adapter"**            | 4                                | 0                                           | -100%  |
| **Clases "mapper"**             | 2                                | 0                                           | -100%  |

### Ejemplo: Antes vs Después para `hotels`

**Antes (Hexagonal):**

```
hotels/
  adapters/
    rest/
      HotelController.kt          ← Depende de HotelService (concreto, no port)
      dto/
        request/
          CreateHotelRequest.kt
          UpdateHotelRequest.kt
        response/
          HotelResponse.kt
          PageResponse.kt
    persistence/
      HotelJpaRepository.kt       ← Spring Data JPA
      HotelRoomJpaRepository.kt   ← Adapter que implementa HotelRepositoryPort
      entity/
        HotelEntity.kt
  application/
    ports/
      output/
        HotelRepositoryPort.kt    ← Interface con UNA implementación
  config/
    HotelServiceConfig.kt         ← @Bean hotelService(repo)
  domain/
    Hotel.kt
    HotelSearchCriteria.kt
  service/
    HotelService.kt               ← Implementa lógica, delega a HotelRepositoryPort
```

**Después (MVC Features):**

```
features/hotels/
  HotelController.kt              ← @RestController, depende de HotelService
  HotelService.kt                 ← @Service, lógica de negocio
  HotelRepository.kt              ← @Repository, Spring Data JPA
  HotelJpaRepository.kt           ← Interface Spring Data (usada por HotelRepository)
  HotelEntity.kt                  ← @Entity JPA
  Hotel.kt                        ← Modelo de dominio
  HotelSearchCriteria.kt          ← Criterios de búsqueda
  dto/
    CreateHotelRequest.kt
    UpdateHotelRequest.kt
    HotelResponse.kt
    PageResponse.kt
```

**Diferencia clave:** De 5 niveles de anidación a 2. De 13 archivos a 11. De 6
tipos de concepto (adapters, application, config, domain, service, dto) a 3
(controller, service, repository, dto).

---

## Notas Importantes

### Sobre `@Service` vs `@Component`

- `@Service` en clases de servicio (HotelService, ExampleService, etc.)
- `@Repository` en interfaces de Spring Data JPA
- `@RestController` en controllers
- `@Component` solo si no encaja en las categorías anteriores

### Sobre Companion Objects como Mappers

En lugar de clases `*Mapper.kt` separadas:

```kotlin
// Antes
class HotelMapper {
    fun toResponse(hotel: Hotel) = HotelResponse(...)
}

// Después
data class HotelResponse(...) {
    companion object {
        fun fromDomain(hotel: Hotel) = HotelResponse(...)
    }
}
```

### Sobre Ports Eliminados

Cada "port" tenía exactamente UNA implementación. Eso no es un port, es una
implementación directa:

```kotlin
// Antes: port + implementación
interface HotelRepositoryPort { fun save(hotel: Hotel): Hotel }
class HotelRoomJpaRepository(...) : HotelRepositoryPort { ... }

// Después: implementación directa
@Repository
class HotelRepository(...) { fun save(hotel: Hotel): Hotel { ... } }
```

### Sobre Stubs Eliminados

Los stubs (`cart/`, `gamification/`, `payments/`, `pokemon/`, `reservations/`,
`reviews/`, `services/`) se eliminan porque:

- No tienen código funcional
- Ocupan espacio visual en el explorador de archivos
- Crean la ilusión de progreso sin entregar valor
- **YAGNI**: Se crean cuando realmente se necesitan, no antes

Si en el futuro se necesita una feature `cart/`, se crea `features/cart/` con su
estructura MVC desde el inicio.

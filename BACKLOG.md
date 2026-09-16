# 📋 BACKLOG — Curso Spring Boot + Kotlin

> **Proyecto:** Gran Hotel Pokémon — API REST de Gestión Hotelera
> **Stack:** Spring Boot 4.1.1 · Kotlin 2.2.x · Gradle Kotlin DSL · JDK 25
> **Arquitectura:** Screaming Architecture por Features + Hexagonal interna
> **Metodología:** TDD (Red → Green → Refactor) · Conventional Commits

---

## 📊 Estado Actual del Proyecto

> Última actualización: Septiembre 2026
> Versión: `0.0.3`
> Rama activa: `feature/milestone-04-docs`

### ✅ Completado

| Semana | Capítulo narrativo                                 | Insignia                        | Estado     |
| ------ | -------------------------------------------------- | ------------------------------- | ---------- |
| 0      | La Aldea Inicial — Bienvenida y entorno            | 🏅 _Semilla de Kotlin_           | ✅ Completo |
| 1      | Kit del Entrenador — Proyecto base                 | 🏅 _Semilla de Kotlin_           | ✅ Completo |
| 2      | El Bosque de los Controladores — Conceptos básicos | 🏅 _Ruta de las 1000 Peticiones_ | ✅ Completo |
| 3      | Las Minas de Datos — Persistencia con JPA          | 🏅 _Reliquia de las Entidades_   | ✅ Completo |
| 4      | El Dojo de la Validación — Screaming Architecture  | 🏅 _Escudo de Validación_        | ✅ Completo |

### 🔧 Implementado hasta ahora

#### Features existentes

| Feature      |      Modelo dominio      |    Entidad JPA    |        Repository         |                               Service                               |         Controller         |              Tests               |
| ------------ | :----------------------: | :---------------: | :-----------------------: | :-----------------------------------------------------------------: | :------------------------: | :------------------------------: |
| **ping**     |         ✅ `Ping`         |         —         |             —             |                           ✅ `PingService`                           |     ✅ `PingController`     |       ✅ Unit + Controller        |
| **examples** |       ✅ `Example`        | ✅ `ExampleEntity` | ✅ `ExampleJpaRepository`  |                       ✅ `ExampleServicePort`                        |   ✅ `ExampleController`    |  ✅ Unit + Controller + Adapter   |
| **hotels**   |        ✅ `Hotel`         |  ✅ `HotelEntity`  |  ✅ `HotelJpaRepository`   |                          ✅ `HotelService`                           |    ✅ `HotelController`     |    ✅ Unit + Integration + E2E    |
| **rooms**    |         ✅ `Room`         |  ✅ `RoomEntity`   |   ✅ `RoomJpaRepository`   |                                 ❌ —                                 |            ❌ —             |               ❌ —                |
| **users**    | ✅ `User` + Value Objects |        ❌ —        | ✅ `UserRepository` (port) | ⚠️ Stub (`CreateUserService`, `GetUserUseCase`, `UpdateUserUseCase`) | ❌ Vacío (`UserController`) | ✅ Unit (dominio y value objects) |
| **sentry**   |            —             |         —         |             —             |                                  —                                  |    ✅ `SentryController`    |              ✅ Unit              |

#### Infraestructura transversal

- ✅ `GlobalExceptionHandler` — Manejo global de errores (validation, type mismatch, 404, 500)
- ✅ `ErrorResponse` — DTO estandarizado de errores
- ✅ `OpenApiConfig` + Swagger UI operativo (`/swagger-ui.html`)
- ✅ `WebConfig` — CORS configurado
- ✅ Paginación neutra: `PageRequest`, `PageResult`, `SortOrder` (clean core)
- ✅ Extensiones de fecha/hora: `DateTimeExtensions`
- ✅ Perfiles configurados: `dev`, `prod`, `test`, `generate-ddl`, `migration`
- ✅ Flyway con migraciones V1 y V2
- ✅ HikariCP configurado
- ✅ Actuator con health, metrics, prometheus
- ✅ Sentry integrado para error tracking
- ✅ Detekt + KtLint + JaCoCo configurados
- ✅ Testcontainers configurado

#### Qué falta por implementar

- ❌ **Users**: `UserController` vacío, servicios son stubs, no hay entidad JPA
- ❌ **Rooms**: Sin service ni controller propios (solo entidad y repo)
- ❌ **Reservation**: No existe
- ❌ **Payment**: No existe
- ❌ **Review**: No existe
- ❌ **Pokemon**: No existe
- ❌ **Service** (servicios adicionales): No existe
- ❌ **Spring Security + JWT**: No implementado
- ❌ **Documentación OpenAPI completa**: Solo configuración base
- ❌ **Despliegue y CI/CD**: No configurado
- ❌ **Eventos asíncronos y caché**: No implementado

---

## 🗺️ Backlog por Semanas

> Cada semana corresponde a un Capítulo/Templo del "Viaje del Héroe" de Kai en
> el Hotel Pokémon. Los ejercicios siguen el ciclo TDD 🔴→🟢→🔵.

---

### Semana 5 — La Fortaleza de la Seguridad 🏰

**Capítulo narrativo:** _La Fortaleza de Ciudad Azafrán_
**Insignia:** 🏅 _Llave Maestra JWT_
**Enemigo:** Team Rocket (ataques de fuerza bruta)
**Rama sugerida:** `feature/milestone-05-security`

#### Ejercicio 5.1 — Completar el CRUD de Users con TDD

> **Como** entrenador Pokémon,
> **quiero** registrarme, ver mi perfil y actualizarlo,
> **para** poder reservar habitaciones en el Hotel Pokémon.

**Tareas:**

- [ ] Crear `UserEntity` JPA mapeando el modelo de dominio `User` existente
- [ ] Implementar `UserJpaRepository` con Spring Data
- [ ] Crear `UserRepositoryAdapter` que implemente el puerto `UserRepository`
- [ ] Completar `CreateUserService.execute()` con lógica real (verificar duplicados, hashear password)
- [ ] Completar `GetUserUseCase.execute()` consultando el repositorio
- [ ] Completar `UpdateUserUseCase.execute()` con actualización parcial
- [ ] Implementar `UserController` con endpoints REST:
  - `POST /api/v1/users` — Registro
  - `GET /api/v1/users/{id}` — Consultar perfil
  - `PUT /api/v1/users/{id}` — Actualizar perfil
  - `DELETE /api/v1/users/{id}` — Desactivar cuenta
  - `GET /api/v1/users` — Listar (admin)
- [ ] Migración Flyway `V3__Add_Users_Table.sql`

**Criterios de aceptación:**

1. `POST /api/v1/users` con datos válidos retorna `201 Created` y el usuario creado (sin exponer password)
2. `POST /api/v1/users` con email duplicado retorna `409 Conflict`
3. Las contraseñas se almacenan hasheadas con BCrypt, nunca en texto plano
4. `GET /api/v1/users/{id}` con ID inexistente retorna `404 Not Found`
5. Los Value Objects (`Email`, `Password`, `PhoneNumber`) validan sus invariantes
6. Tests unitarios del servicio, tests del controlador con `@WebMvcTest`, tests de integración con `@DataJpaTest`
7. Cobertura ≥ 80% para el feature `users`

---

#### Ejercicio 5.2 — Spring Security con JWT stateless

> **Como** desarrollador del Hotel Pokémon,
> **quiero** proteger los endpoints con autenticación JWT,
> **para** garantizar que solo usuarios autorizados accedan a recursos protegidos.

**Tareas:**

- [ ] Agregar dependencias: `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- [ ] Crear feature `auth/` con estructura Screaming Architecture
- [ ] Implementar `JwtService` — generación y validación de tokens
- [ ] Implementar `JwtAuthenticationFilter` — filtro de autenticación
- [ ] Implementar `CustomUserDetailsService` cargando usuarios desde `UserRepository`
- [ ] Configurar `SecurityFilterChain`:
  - CSRF deshabilitado (API stateless)
  - Sesión STATELESS
  - Endpoints públicos: `/api/auth/**`, `/swagger-ui/**`, `/api-docs/**`, `/actuator/health`
  - Endpoints protegidos por rol
- [ ] Crear `AuthController`:
  - `POST /api/auth/login` — Autenticación (retorna JWT)
  - `POST /api/auth/register` — Registro público
- [ ] Configurar `PasswordEncoder` como bean (`BCryptPasswordEncoder`)

**Criterios de aceptación:**

1. `POST /api/auth/login` con credenciales válidas retorna `200` con token JWT
2. `POST /api/auth/login` con credenciales inválidas retorna `401 Unauthorized`
3. Acceso a `GET /api/v1/users` sin token retorna `401 Unauthorized`
4. Acceso a `GET /api/v1/users` con token válido de rol `ADMIN` retorna `200`
5. Acceso a `GET /api/v1/users` con token válido de rol `GUEST` retorna `403 Forbidden`
6. Tokens expirados retornan `401 Unauthorized`
7. El token incluye `username`, `roles` y timestamp de expiración en sus claims
8. Tests de integración con `MockMvc` validan cada escenario de acceso

---

#### Ejercicio 5.3 — Roles y autoridades granulares

> **Como** administrador del Hotel Pokémon,
> **quiero** definir roles con permisos granulares,
> **para** controlar el acceso a funcionalidades específicas del sistema.

**Tareas:**

- [ ] Crear entidades `Role` y `Authority` con relación `@ManyToMany`
- [ ] Relación `User ↔ Role` (`@ManyToMany`)
- [ ] Definir roles base: `ROLE_GUEST`, `ROLE_STAFF`, `ROLE_ADMIN`
- [ ] Definir authorities: `USER_READ`, `USER_WRITE`, `HOTEL_MANAGE`, `RESERVATION_CREATE`, etc.
- [ ] Migración Flyway `V4__Add_Roles_Authorities.sql` con datos seed
- [ ] Aplicar `@PreAuthorize` en controladores existentes

**Criterios de aceptación:**

1. Un usuario `GUEST` puede consultar hoteles pero no crearlos
2. Un usuario `STAFF` puede gestionar reservas
3. Un usuario `ADMIN` tiene acceso total
4. Los roles se cargan al contexto de seguridad desde la base de datos
5. Tests de autorización validan cada combinación de rol y endpoint

---

### Semana 6 — La Torre Pokémon de las Pruebas 🗼

**Capítulo narrativo:** _El Oráculo de las Pruebas_
**Insignia:** 🏅 _Cinturón de Pruebas_
**Enemigo:** Mewtwo (flaky tests)
**Rama sugerida:** `feature/milestone-06-testing`

#### Ejercicio 6.1 — Estrategia de testing por niveles

> **Como** desarrollador,
> **quiero** una suite de pruebas estructurada en niveles,
> **para** garantizar calidad en cada capa de la aplicación.

**Tareas:**

- [ ] Implementar los 6 niveles de prueba para el feature `hotels` completo:
  1. Unitarias de servicio de dominio (MockK, sin Spring)
  2. De controlador con `@WebMvcTest` (servicio mockeado)
  3. Unitarias del adapter de persistencia (JPA repo mockeado)
  4. De integración con `@SpringBootTest` + H2
  5. De datos JPA con `@DataJpaTest`
  6. E2E con `@SpringBootTest` + `TestRestTemplate` + Testcontainers PostgreSQL
- [ ] Replicar la estrategia para el feature `users`
- [ ] Configurar `@Nested` para organizar tests por escenario (Given/When/Then)
- [ ] Agregar `@DisplayName` descriptivos en español

**Criterios de aceptación:**

1. Cada feature tiene al menos pruebas en los niveles 1, 2 y 4
2. Las pruebas unitarias NO levantan contexto de Spring (< 100ms cada una)
3. Las pruebas E2E usan Testcontainers con PostgreSQL real
4. No hay tests flaky — todos son reproducibles y aislados
5. La nomenclatura sigue el patrón `debe_resultado_cuando_condición`
6. JaCoCo reporta cobertura ≥ 80% global

---

#### Ejercicio 6.2 — Pruebas de seguridad y endpoints protegidos

> **Como** desarrollador,
> **quiero** validar que la seguridad funciona correctamente en tests,
> **para** prevenir regresiones en la protección de endpoints.

**Tareas:**

- [ ] Crear `@WithMockUser` personalizado con roles del dominio
- [ ] Tests de endpoints protegidos con/sin token JWT
- [ ] Tests de endpoints con token expirado
- [ ] Tests de acceso con roles insuficientes
- [ ] Configurar `SecurityTestConfig` para tests que no necesitan seguridad

**Criterios de aceptación:**

1. Tests verifican `401` para requests sin autenticación
2. Tests verifican `403` para roles insuficientes
3. Tests verifican acceso exitoso con credenciales válidas
4. `SecurityTestConfig` permite ejecutar tests de features aislados sin levantar la cadena de seguridad completa

---

#### Ejercicio 6.3 — Cobertura y quality gates

> **Como** equipo de desarrollo,
> **quiero** configurar quality gates automáticos,
> **para** mantener estándares de calidad mínimos en cada PR.

**Tareas:**

- [ ] Configurar JaCoCo con umbral mínimo de cobertura al 80%
- [ ] Configurar Detekt con reglas estrictas (complejidad ciclomática ≤ 10)
- [ ] Configurar KtLint para estilo consistente
- [ ] Crear tarea Gradle `check` que ejecute: `ktlintCheck` → `detekt` → `test` → `jacocoTestCoverageVerification`
- [ ] Documentar en README cómo ejecutar la verificación completa

**Criterios de aceptación:**

1. `./gradlew check` falla si la cobertura es < 80%
2. `./gradlew check` falla si hay violaciones de Detekt
3. `./gradlew check` falla si hay problemas de formato KtLint
4. El reporte HTML de JaCoCo se genera en `build/reports/jacoco/`
5. Todos los tests actuales pasan con el pipeline completo

---

### Semana 7 — El Mapa del Merodeador: Documentación 📜

**Capítulo narrativo:** _El Templo de los Avisos_
**Insignia:** 🏅 _Libro de las API_
**Enemigo:** Ditto (documentación desactualizada)
**Rama sugerida:** `feature/milestone-07-docs`

#### Ejercicio 7.1 — Documentación OpenAPI completa

> **Como** desarrollador externo,
> **quiero** una documentación interactiva de la API,
> **para** integrarme con el sistema del Hotel Pokémon sin depender de documentación manual.

**Tareas:**

- [ ] Anotar todos los controladores con `@Tag(name, description)`
- [ ] Anotar todos los endpoints con `@Operation(summary, description)`
- [ ] Documentar request/response con `@Schema` en los DTOs
- [ ] Documentar códigos de respuesta con `@ApiResponse` (200, 201, 400, 401, 403, 404, 409, 500)
- [ ] Agregar ejemplos en las anotaciones `@Schema(example = "...")`
- [ ] Configurar seguridad JWT en OpenAPI (botón "Authorize" en Swagger UI)
- [ ] Agrupar endpoints por feature en tags

**Criterios de aceptación:**

1. Swagger UI (`/swagger-ui.html`) muestra todos los endpoints organizados por feature
2. Cada endpoint tiene summary, description y ejemplos de request/response
3. Los modelos de DTO muestran validaciones y tipos de datos
4. El botón "Authorize" permite ingresar un token JWT para probar endpoints protegidos
5. Los códigos de error están documentados con ejemplos del `ErrorResponse`
6. `/api-docs` genera un JSON OpenAPI 3.0 válido

---

#### Ejercicio 7.2 — CRUD completo de Rooms

> **Como** administrador del Hotel Pokémon,
> **quiero** gestionar las habitaciones temáticas,
> **para** ofrecer experiencias personalizadas a los entrenadores.

**Tareas:**

- [ ] Crear `RoomService` con lógica de negocio para habitaciones
- [ ] Crear `RoomController` con endpoints:
  - `POST /api/v1/hotels/{hotelId}/rooms` — Crear habitación
  - `GET /api/v1/hotels/{hotelId}/rooms` — Listar habitaciones del hotel
  - `GET /api/v1/hotels/{hotelId}/rooms/{roomId}` — Detalle de habitación
  - `PUT /api/v1/hotels/{hotelId}/rooms/{roomId}` — Actualizar
  - `DELETE /api/v1/hotels/{hotelId}/rooms/{roomId}` — Eliminar
- [ ] Enriquecer `RoomEntity` con campos temáticos: `pokemonTheme`, `capacity`, `status`, `description`
- [ ] Crear DTOs: `CreateRoomRequest`, `UpdateRoomRequest`, `RoomResponse`
- [ ] Crear enums: `RoomType`, `RoomStatus`, `PokemonTheme`
- [ ] Migración Flyway `V5__Enrich_Rooms_Table.sql`
- [ ] Documentar con anotaciones OpenAPI completas

**Criterios de aceptación:**

1. CRUD completo funcional para habitaciones anidadas bajo un hotel
2. Validación con Jakarta Validation en los DTOs (`@get:NotBlank`, `@get:Positive`, etc.)
3. Un room sin hotel asociado retorna `400 Bad Request`
4. `GET /api/v1/hotels/{hotelId}/rooms` soporta paginación y filtrado por `status` y `pokemonTheme`
5. Tests unitarios y de integración para el feature completo
6. Documentación OpenAPI completa para todos los endpoints

---

### Semana 8 — El Portal a la Nube: Despliegue y Monitoreo ☁️

**Capítulo narrativo:** _La Puerta de Heroku — Despliegue y Monitoreo_
**Insignia:** 🏅 _Puerta de los Sistemas en Vivo_
**Enemigo:** Kyogre (outage de producción)
**Rama sugerida:** `feature/milestone-08-deploy`

#### Ejercicio 8.1 — Empaquetado y Docker

> **Como** equipo de operaciones,
> **quiero** empaquetar la aplicación en un contenedor Docker optimizado,
> **para** desplegar de forma reproducible en cualquier entorno.

**Tareas:**

- [ ] Crear `Dockerfile` multi-stage con Layered JARs:
  - Stage 1: Build con Gradle
  - Stage 2: Runtime con imagen base Distroless o Eclipse Temurin slim
- [ ] Crear `docker-compose.yml` para desarrollo local (app + PostgreSQL + Redis)
- [ ] Configurar `.dockerignore`
- [ ] Optimizar la imagen (< 200MB)
- [ ] Configurar `JAVA_TOOL_OPTIONS` para contenedores (memory limits)

**Criterios de aceptación:**

1. `docker build -t hotel-pokemon .` genera una imagen funcional
2. `docker compose up` levanta la aplicación con PostgreSQL
3. La aplicación inicia correctamente dentro del contenedor
4. La imagen pesa menos de 200MB
5. Los secretos se pasan como variables de entorno, nunca hardcodeados en la imagen

---

#### Ejercicio 8.2 — Observabilidad con Actuator y Prometheus

> **Como** operador del sistema,
> **quiero** monitorear la salud y el rendimiento de la API,
> **para** detectar y resolver problemas antes de que afecten a los usuarios.

**Tareas:**

- [ ] Configurar Actuator con endpoints seguros (solo `ADMIN` accede a `/actuator/**`)
- [ ] Exponer métricas para Prometheus en `/actuator/prometheus`
- [ ] Crear Health Indicators personalizados:
  - `DatabaseHealthIndicator` — Verifica la conexión a PostgreSQL
  - `ExternalServiceHealthIndicator` — Placeholder para servicios externos
- [ ] Configurar health groups: `liveness` y `readiness` para Kubernetes
- [ ] Agregar métricas de negocio con Micrometer:
  - Contador de reservas creadas
  - Timer de tiempo de respuesta por endpoint
  - Gauge de habitaciones disponibles
- [ ] Crear `docker-compose-monitoring.yml` con Prometheus + Grafana (opcional)

**Criterios de aceptación:**

1. `GET /actuator/health` retorna `{"status": "UP"}` con detalles de componentes
2. `GET /actuator/prometheus` expone métricas en formato Prometheus
3. Los endpoints de Actuator (excepto `/health`) requieren autenticación `ADMIN`
4. Las health probes de Kubernetes (`/actuator/health/liveness`, `/actuator/health/readiness`) funcionan
5. Las métricas de negocio personalizadas aparecen en el endpoint de Prometheus
6. Sentry captura errores de producción correctamente

---

#### Ejercicio 8.3 — CI/CD con GitHub Actions

> **Como** equipo de desarrollo,
> **quiero** automatizar el pipeline de integración continua,
> **para** detectar errores tempranamente y mantener la calidad del código.

**Tareas:**

- [ ] Crear `.github/workflows/ci.yml` con:
  - Trigger en push a `main` y PRs
  - Matrix: JDK 25
  - Steps: Checkout → Setup JDK → Cache Gradle → KtLint → Detekt → Test → JaCoCo → Build
- [ ] Crear `.github/workflows/docker.yml` para construir y publicar imagen
- [ ] Configurar cache de Gradle para acelerar builds
- [ ] Agregar badge de CI al README

**Criterios de aceptación:**

1. El pipeline CI ejecuta en < 5 minutos
2. El pipeline falla si los tests no pasan
3. El pipeline falla si la cobertura es < 80%
4. El pipeline falla si hay violaciones de Detekt o KtLint
5. Los reportes de cobertura se publican como artifacts del workflow

---

### Semana 9 — La Liga de los Maestros: Temas Avanzados ⚡

**Capítulo narrativo:** _El Laberinto de la Asincronía_
**Insignia:** 🏅 _Energía de la Caché_
**Enemigo:** Rayquaza (congestión de mensajería)
**Rama sugerida:** `feature/milestone-09-advanced`

#### Ejercicio 9.1 — Sistema de Reservaciones

> **Como** entrenador Pokémon,
> **quiero** reservar una habitación temática en el hotel,
> **para** disfrutar de una estadía personalizada con mi equipo Pokémon.

**Tareas:**

- [ ] Crear feature `reservations/` con estructura completa:
  - Modelo de dominio: `Reservation` con máquina de estados (`PENDING` → `CONFIRMED` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED`)
  - Entidad JPA: `ReservationEntity` con relaciones a `User` y `Room`
  - Repository, Service, Controller
- [ ] Implementar validaciones de negocio:
  - No permitir reservas en fechas ya ocupadas
  - Validar que `checkOutDate > checkInDate`
  - Calcular `totalPrice` basado en noches × precio por noche
- [ ] Endpoints:
  - `POST /api/v1/reservations` — Crear reserva
  - `GET /api/v1/reservations/{id}` — Detalle
  - `GET /api/v1/reservations` — Listar (filtros por usuario, estado, fechas)
  - `PATCH /api/v1/reservations/{id}/confirm` — Confirmar
  - `PATCH /api/v1/reservations/{id}/checkin` — Check-in
  - `PATCH /api/v1/reservations/{id}/checkout` — Check-out
  - `PATCH /api/v1/reservations/{id}/cancel` — Cancelar
- [ ] Migración Flyway `V6__Add_Reservations_Table.sql`

**Criterios de aceptación:**

1. Solo transiciones de estado válidas son permitidas (no se puede hacer checkout sin checkin)
2. No se pueden crear reservas superpuestas para la misma habitación
3. El precio total se calcula automáticamente
4. Al confirmar una reserva, el estado de la habitación cambia a `RESERVED`
5. Al cancelar, la habitación vuelve a estar `AVAILABLE`
6. Tests unitarios cubren todas las transiciones de estado y reglas de negocio
7. Tests de integración validan el flujo completo de reservación

---

#### Ejercicio 9.2 — Eventos de dominio con Spring Events

> **Como** sistema del Hotel Pokémon,
> **quiero** emitir eventos cuando ocurren acciones importantes,
> **para** desacoplar la lógica de notificaciones y auditoría.

**Tareas:**

- [ ] Crear eventos de dominio:
  - `ReservationCreatedEvent`
  - `ReservationConfirmedEvent`
  - `CheckInCompletedEvent`
  - `CheckOutCompletedEvent`
  - `ReservationCancelledEvent`
- [ ] Publicar eventos con `ApplicationEventPublisher`
- [ ] Crear listeners con `@TransactionalEventListener`:
  - `NotificationListener` — Simular envío de emails/notificaciones
  - `AuditListener` — Registrar cambios en log de auditoría
- [ ] Configurar `@Async` para procesamiento asíncrono de eventos
- [ ] Configurar `TaskExecutor` personalizado (no usar `SimpleAsyncTaskExecutor`)

**Criterios de aceptación:**

1. Los eventos se publican después de que la transacción se completa (`AFTER_COMMIT`)
2. Los listeners procesan eventos de forma asíncrona sin bloquear el flujo principal
3. Si un listener falla, no afecta la operación principal
4. Los eventos son testables — se pueden verificar en tests de integración
5. El log de auditoría registra quién hizo qué y cuándo

---

#### Ejercicio 9.3 — Caché con Caffeine / Redis

> **Como** sistema del Hotel Pokémon,
> **quiero** cachear consultas frecuentes,
> **para** mejorar el rendimiento y reducir la carga en la base de datos.

**Tareas:**

- [ ] Agregar dependencia de `spring-boot-starter-cache`
- [ ] Configurar Caffeine como provider de cache (local, sin infraestructura externa)
- [ ] Aplicar `@Cacheable` en:
  - `HotelService.getHotelById()` — Cache por ID
  - `RoomService.getAvailableRooms()` — Cache de disponibilidad
- [ ] Aplicar `@CacheEvict` cuando se modifican datos
- [ ] Aplicar `@CachePut` para actualizaciones
- [ ] Exponer métricas de cache hit/miss en Actuator

**Criterios de aceptación:**

1. La segunda consulta al mismo hotel se sirve desde caché (verificar con logs de SQL)
2. Al actualizar un hotel, el caché se invalida
3. El caché tiene TTL configurado (ej. 5 minutos)
4. Las métricas de `cache.gets`, `cache.puts`, `cache.evictions` están disponibles en Actuator
5. Los tests verifican el comportamiento del caché (hit y miss)

---

### Semana 10 — El Hotel Pokémon de Lujo: Proyecto Final 🏆

**Capítulo narrativo:** _El Palacio del Hotel Pokémon — El Legado Cumplido_
**Insignia:** 🏅 _Corona del Arquitecto_
**Enemigo:** Arceus (sistema a escala mundial)
**Rama sugerida:** `feature/milestone-10-capstone`

#### Ejercicio 10.1 — Sistema de Pagos

> **Como** entrenador Pokémon,
> **quiero** pagar mi reservación,
> **para** confirmar mi estadía en el hotel.

**Tareas:**

- [ ] Crear feature `payments/` con estructura completa:
  - Modelo: `Payment` con estados (`PENDING` → `COMPLETED` / `FAILED` / `REFUNDED`)
  - Entidad JPA: `PaymentEntity` con relación `@OneToOne` a `Reservation`
  - Enums: `PaymentMethod`, `PaymentStatus`
- [ ] Implementar `PaymentService` con:
  - Procesamiento de pago (simulado — interfaz para futuro gateway)
  - Reembolso
  - Consulta de estado
- [ ] Endpoints:
  - `POST /api/v1/reservations/{id}/payments` — Procesar pago
  - `GET /api/v1/payments/{id}` — Estado del pago
  - `POST /api/v1/payments/{id}/refund` — Reembolso
- [ ] Migración Flyway `V7__Add_Payments_Table.sql`

**Criterios de aceptación:**

1. Solo se puede pagar una reserva en estado `CONFIRMED`
2. Al completar el pago, la reserva cambia a estado `IN_PROGRESS` (lista para check-in)
3. Solo se puede reembolsar un pago `COMPLETED`
4. El monto del pago coincide con el `totalPrice` de la reserva
5. Tests cubren todos los flujos de pago y reembolso

---

#### Ejercicio 10.2 — Reviews y entidades opcionales

> **Como** entrenador Pokémon,
> **quiero** dejar una reseña de mi estadía,
> **para** ayudar a otros entrenadores a elegir su habitación.

**Tareas:**

- [ ] Crear feature `reviews/` con:
  - Modelo: `Review` con rating (1-5) y comentario
  - Entidad JPA: `ReviewEntity` con relación `@OneToOne` a `Reservation`
- [ ] Endpoints:
  - `POST /api/v1/reservations/{id}/reviews` — Crear reseña
  - `GET /api/v1/hotels/{id}/reviews` — Listar reseñas del hotel
  - `GET /api/v1/reviews/{id}` — Detalle
- [ ] Solo se puede crear una reseña para una reserva `COMPLETED`
- [ ] Calcular rating promedio del hotel
- [ ] Migración Flyway `V8__Add_Reviews_Table.sql`

**Criterios de aceptación:**

1. Solo el usuario de la reserva puede crear la reseña
2. Una reserva solo puede tener una reseña
3. El rating debe estar entre 1 y 5
4. `GET /api/v1/hotels/{id}/reviews` incluye paginación y el rating promedio
5. Tests unitarios y de integración completos

---

#### Ejercicio 10.3 — Integración completa y release final

> **Como** equipo de desarrollo del Hotel Pokémon,
> **quiero** integrar todas las funcionalidades y preparar el release,
> **para** entregar un sistema de gestión hotelera completo y funcional.

**Tareas:**

- [ ] Verificar que todos los features funcionan integrados:
  - Flujo completo: Registro → Login → Buscar hotel → Ver habitaciones → Reservar → Pagar → Check-in → Check-out → Dejar reseña
- [ ] Crear test E2E del flujo completo con Testcontainers
- [ ] Ejecutar el pipeline completo: `ktlintCheck` → `detekt` → `test` → `jacocoTestCoverageVerification` → `build`
- [ ] Revisar y completar documentación OpenAPI de todos los endpoints
- [ ] Actualizar README.md con:
  - Instrucciones de setup local
  - Endpoints disponibles
  - Arquitectura del proyecto
  - Cómo ejecutar tests
- [ ] Actualizar HISTORIAL.md con los logros de todas las semanas
- [ ] Crear tag `v1.0.0` y release notes
- [ ] (Opcional) Crear feature `pokemon/` con registro de Pokémon acompañantes

**Criterios de aceptación:**

1. El flujo completo funciona de extremo a extremo en un test automatizado
2. Todos los tests pasan (`./gradlew check`)
3. Cobertura global ≥ 80%
4. Swagger UI muestra todos los endpoints documentados y organizados
5. La aplicación arranca sin errores con `./gradlew bootRun`
6. Docker Compose levanta el entorno completo
7. El README explica cómo poner el proyecto en marcha en < 5 minutos
8. El proyecto tiene al menos 8 entidades del dominio implementadas

---

## 📈 Resumen de Progreso

| Semana | Temas principales                             | Insignia                         | Estado      |
| ------ | --------------------------------------------- | -------------------------------- | ----------- |
| 0      | Bienvenida, entorno, narrativa                | 🏅 Semilla de Kotlin              | ✅ Completo  |
| 1      | Proyecto base, dependencias                   | 🏅 Semilla de Kotlin              | ✅ Completo  |
| 2      | HTTP, Spring Boot, perfiles, Flyway           | 🏅 Ruta de las 1000 Peticiones    | ✅ Completo  |
| 3      | JPA, TDD, CRUD Example, entidades             | 🏅 Reliquia de las Entidades      | ✅ Completo  |
| 4      | Screaming Architecture, validación, Swagger   | 🏅 Escudo de Validación           | ✅ Completo  |
| 5      | Users CRUD, Spring Security, JWT, RBAC        | 🏅 Llave Maestra JWT              | 🔲 Pendiente |
| 6      | Testing por niveles, cobertura, quality gates | 🏅 Cinturón de Pruebas            | 🔲 Pendiente |
| 7      | OpenAPI completa, CRUD Rooms                  | 🏅 Libro de las API               | 🔲 Pendiente |
| 8      | Docker, Actuator, Prometheus, CI/CD           | 🏅 Puerta de los Sistemas en Vivo | 🔲 Pendiente |
| 9      | Reservaciones, eventos, caché                 | 🏅 Energía de la Caché            | 🔲 Pendiente |
| 10     | Pagos, reviews, integración, release v1.0     | 🏅 Corona del Arquitecto          | 🔲 Pendiente |

---

## 🎯 Entidades del Dominio — Estado

| Entidad        | Dominio |  JPA  | Repo  | Service | Controller | Tests | Semana |
| -------------- | :-----: | :---: | :---: | :-----: | :--------: | :---: | :----: |
| Example        |    ✅    |   ✅   |   ✅   |    ✅    |     ✅      |   ✅   |   3    |
| Hotel          |    ✅    |   ✅   |   ✅   |    ✅    |     ✅      |   ✅   |  4-5   |
| Room           |    ✅    |   ✅   |   ✅   |    🔲    |     🔲      |   🔲   |   7    |
| User           |    ✅    |   🔲   |   ⚠️   |    ⚠️    |     🔲      |   ⚠️   |   5    |
| Role/Authority |    🔲    |   🔲   |   🔲   |    🔲    |     —      |   🔲   |   5    |
| Reservation    |    🔲    |   🔲   |   🔲   |    🔲    |     🔲      |   🔲   |   9    |
| Payment        |    🔲    |   🔲   |   🔲   |    🔲    |     🔲      |   🔲   |   10   |
| Review         |    🔲    |   🔲   |   🔲   |    🔲    |     🔲      |   🔲   |   10   |

> ✅ = Completo · ⚠️ = Parcial/Stub · 🔲 = Pendiente

---

## 📚 Referencias

- [Narrativa del Curso](docs/course/week-00/01-historia-del-viaje.md) — El Legado del Desarrollador
- [Método de Aprendizaje](docs/course/week-00/02-metodo-de-aprendizaje.md) — El Viaje del Héroe
- [Calendario de Seguimiento](docs/course/README.md) — Plan semanal tentativo
- [Entidades del Sistema](docs/course/week-03/02-entidades.md) — Definición de entidades
- [Screaming Architecture](docs/course/week-04/01-screaming-architecture.md) — Estructura por features
- [Spring Security + JWT](docs/course/week-05/01-spring-security.md) — Diseño de seguridad
- [TDD para Hotels](docs/course/week-05/02-tdd-red-hotels.md) — Estrategia de testing por niveles
- [Retos 2026](docs/course/week-05/03-retos.md) — 52 retos avanzados semanales
- [HISTORIAL](HISTORIAL.md) — Logros e insignias conseguidas

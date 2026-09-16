# 🚀 IDEAS POR IMPLEMENTAR — Liga de Campeones

> **Documento:** Ejercicios avanzados de práctica deliberada post-curso
> **Prerrequisito:** Haber completado las 10 semanas del [BACKLOG.md](BACKLOG.md)
> **Proyecto:** Gran Hotel Pokémon — API REST de Gestión Hotelera
> **Stack:** Spring Boot 4.1.1 · Kotlin 2.2.x · JDK 25 · Gradle Kotlin DSL

---

## 🎯 Propósito

Kai ha completado los 10 Templos del Código y obtenido la _Corona del
Arquitecto_. Pero la Liga Pokémon de Desarrolladores tiene desafíos que van más
allá del curso base. Este documento define **ejercicios de nivel intermedio,
avanzado y experto** organizados en fases temáticas para seguir practicando
después del proyecto final.

Cada ejercicio tiene:

- 📖 Historia con contexto del Hotel Pokémon
- 📋 Tareas concretas con checkboxes
- ✅ Criterios de aceptación verificables
- 📊 Métricas de éxito medibles
- 🔗 Dependencias entre ejercicios
- 💡 Hints para cuando te atasques
- 📚 Recursos externos recomendados

---

## Mapa de Fases

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    🏆 LIGA DE CAMPEONES POKÉMON                          │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  FASE 1 ─── Resiliencia y Tolerancia a Fallos ──── 🛡️ Escudo de Onix     │
│  FASE 2 ─── Consultas Flexibles con GraphQL ────── 🔍 Lente de Alakazam  │
│  FASE 3 ─── Concurrencia y Rendimiento ─────────── ⚡ Velocidad de Jolteon│
│  FASE 4 ─── Inteligencia Artificial ────────────── 🧠 Psíquico de Mewtwo │
│  FASE 5 ─── Comunicación entre Servicios ───────── 🌐 Red de Porygon      │
│  FASE 6 ─── Compilación Nativa con GraalVM ─────── 💎 Cristal de Dialga  │
│                                                                          │
│  Total: 22 ejercicios · Tiempo estimado: 8-12 semanas                    │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Fase 1 — Resiliencia y Tolerancia a Fallos 🛡️

**Insignia:** _Escudo de Onix_ — Protección inquebrantable contra fallos
**Narrativa:** El Hotel Pokémon depende de servicios externos (pasarela de
pagos, servicio de clima para tours, API de Pokédex). Cuando estos servicios
fallan, el hotel no puede detenerse. Kai debe construir defensas como las de
Onix: duras, confiables y automáticas.

---

### 1.1 — Circuit Breaker con Resilience4j

> **Como** sistema del Hotel Pokémon,
> **quiero** proteger las llamadas a servicios externos con Circuit Breaker,
> **para** que un servicio caído no derrumbe toda la aplicación.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 4-6 horas
**Dependencias:** Semana 10 del BACKLOG (sistema de pagos implementado)

**Tareas:**

- [ ] Agregar dependencia `resilience4j-spring-boot3`
- [ ] Crear interfaz `PaymentGatewayClient` simulando un servicio externo de pagos
- [ ] Implementar `@CircuitBreaker` en el servicio de pagos:
  - Estado `CLOSED` → Procesamiento normal
  - Estado `OPEN` → Fallo rápido sin intentar la llamada
  - Estado `HALF_OPEN` → Prueba con llamadas limitadas
- [ ] Implementar `fallbackMethod` que registre el pago como `PENDING_RETRY`
- [ ] Configurar umbrales en `application.yaml`:

  ```yaml
  resilience4j.circuitbreaker.instances.paymentGateway:
    failureRateThreshold: 50
    waitDurationInOpenState: 10s
    permittedNumberOfCallsInHalfOpenState: 3
    slidingWindowSize: 10
  ```

- [ ] Exponer métricas del circuit breaker en Actuator (`/actuator/circuitbreakers`)

**Criterios de aceptación:**

1. Cuando el servicio externo falla 5 de 10 veces, el circuit breaker se abre
2. En estado abierto, las llamadas retornan el fallback en < 5ms (sin timeout)
3. Después de 10 segundos, el circuit breaker pasa a half-open y prueba 3 llamadas
4. El fallback registra el pago como `PENDING_RETRY` para procesamiento batch posterior
5. Las métricas del circuit breaker son visibles en `/actuator/circuitbreakers`

**Métricas de éxito:**

- Tiempo de respuesta del fallback: < 5ms
- Tasa de errores propagados al usuario: 0% (siempre hay fallback)
- Transición CLOSED→OPEN detectable en métricas de Actuator

**💡 Hints:**

- Usa `@CircuitBreaker(name = "paymentGateway", fallbackMethod = "paymentFallback")`
- El fallback debe tener la misma firma que el método original + `Exception` como último parámetro
- Para simular fallos, crea un `FakePaymentGateway` que falle aleatoriamente con `Random.nextBoolean()`

**📚 Recursos:**

- [Resilience4j Spring Boot 3 docs](https://resilience4j.readme.io/docs/getting-started-3)
- [Resilience4j Circuit Breaker](https://resilience4j.readme.io/docs/circuitbreaker)

---

### 1.2 — Retry con backoff exponencial

> **Como** sistema del Hotel Pokémon,
> **quiero** reintentar automáticamente operaciones fallidas,
> **para** recuperarme de errores transitorios sin intervención manual.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 2-3 horas
**Dependencias:** Ejercicio 1.1 (Circuit Breaker)

**Tareas:**

- [ ] Configurar `@Retry` en operaciones de pago:

  ```yaml
  resilience4j.retry.instances.paymentGateway:
    maxAttempts: 3
    waitDuration: 500ms
    enableExponentialBackoff: true
    exponentialBackoffMultiplier: 2
    retryExceptions:
      - java.net.ConnectException
      - java.net.SocketTimeoutException
  ```

- [ ] Implementar `@Retry` combinado con `@CircuitBreaker` (orden de decoradores)
- [ ] Crear tests que verifican el número exacto de reintentos
- [ ] Agregar logs estructurados para cada reintento (`attempt 1/3`, `attempt 2/3`)

**Criterios de aceptación:**

1. Un error transitorio se reintenta hasta 3 veces con backoff: 500ms → 1s → 2s
2. Si los 3 reintentos fallan, se activa el circuit breaker
3. Errores no retryables (`IllegalArgumentException`) NO se reintentan
4. Cada reintento se registra en los logs con el número de intento

**Métricas de éxito:**

- Tasa de recuperación por retry: > 60% de los errores transitorios se resuelven en el primer reintento

**💡 Hints:**

- El orden importa: `Retry(CircuitBreaker(método))` — el retry envuelve al circuit breaker
- Usa `@Order` o configura `resilience4j.circuitbreaker.circuitBreakerAspectOrder`

**📚 Recursos:**

- [Resilience4j Retry](https://resilience4j.readme.io/docs/retry)

---

### 1.3 — Rate Limiting para protección de API

> **Como** administrador del Hotel Pokémon,
> **quiero** limitar las peticiones por usuario,
> **para** proteger la API de abuso y ataques de fuerza bruta.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 3-4 horas
**Dependencias:** Semana 5 del BACKLOG (autenticación JWT)

**Tareas:**

- [ ] Configurar `@RateLimiter` de Resilience4j en endpoints sensibles:
  - Login: máximo 5 intentos por minuto por IP
  - Creación de reservas: máximo 10 por minuto por usuario
  - Endpoints públicos: máximo 100 por minuto por IP
- [ ] Implementar `KeyResolver` personalizado que identifique al usuario por JWT o por IP
- [ ] Retornar `429 Too Many Requests` con header `Retry-After`
- [ ] Agregar Bulkhead para limitar llamadas concurrentes a servicios externos

**Criterios de aceptación:**

1. El intento #6 de login en 1 minuto retorna `429 Too Many Requests`
2. La respuesta incluye el header `Retry-After: <segundos>`
3. El rate limit se aplica por usuario autenticado (no por IP global)
4. El bulkhead limita a máximo 10 llamadas concurrentes al servicio de pagos
5. Las métricas de rate limiting son visibles en Actuator

**Métricas de éxito:**

- 100% de las peticiones que exceden el límite reciben `429`
- 0 peticiones legítimas bloqueadas durante carga normal

**💡 Hints:**

- Usa `@RateLimiter(name = "loginRateLimiter")` y configura `limitForPeriod` y `limitRefreshPeriod`
- Para el Bulkhead, usa `@Bulkhead(name = "paymentBulkhead", type = Bulkhead.Type.SEMAPHORE)`

**📚 Recursos:**

- [Resilience4j Rate Limiter](https://resilience4j.readme.io/docs/ratelimiter)
- [Resilience4j Bulkhead](https://resilience4j.readme.io/docs/bulkhead)

---

### 1.4 — Time Limiter y fallback en cascada

> **Como** huésped del Hotel Pokémon,
> **quiero** que la API responda rápido aunque un servicio externo esté lento,
> **para** no esperar indefinidamente al buscar habitaciones disponibles.

**Nivel:** 🟠 Avanzado
**Tiempo estimado:** 3-4 horas
**Dependencias:** Ejercicios 1.1, 1.2, 1.3

**Tareas:**

- [ ] Configurar `@TimeLimiter` con timeout de 2 segundos para llamadas externas
- [ ] Implementar fallback en cascada:
  1. Intentar servicio externo (con timeout 2s)
  2. Si falla → consultar caché local (Caffeine)
  3. Si caché vacía → retornar datos por defecto ("Servicio temporalmente no disponible")
- [ ] Combinar los 4 patrones en un solo servicio: `TimeLimiter → CircuitBreaker → Retry → Bulkhead`
- [ ] Crear test de integración que simule cada nivel del fallback

**Criterios de aceptación:**

1. Si el servicio externo responde en < 2s, se usa la respuesta real
2. Si el servicio tarda > 2s, se cancela y se consulta el caché
3. Si el caché está vacío, se retorna un response degradado pero válido
4. El usuario siempre recibe una respuesta (nunca un timeout genérico)
5. El orden de los decoradores es correcto: `TimeLimiter(CircuitBreaker(Retry(Bulkhead(fn))))`

**Métricas de éxito:**

- P99 de tiempo de respuesta: < 2.5s (incluyendo fallback)
- Disponibilidad percibida por el usuario: 99.9%

**💡 Hints:**

- `@TimeLimiter` requiere que el método retorne `CompletableFuture<T>` o `Mono<T>`
- Para Kotlin, usa `suspend fun` con coroutines y adapta con `mono {}` o `future {}`

**📚 Recursos:**

- [Resilience4j Getting Started with Spring Boot 3](https://resilience4j.readme.io/docs/getting-started-3)
- [Resilience4j Configuration](https://resilience4j.readme.io/docs/circuitbreaker#create-and-configure-a-circuitbreaker)

---

## Fase 2 — Consultas Flexibles con GraphQL y gRPC 🔍

**Insignia:** _Lente de Alakazam_ — Visión flexible y precisa de los datos
**Narrativa:** Los entrenadores quieren buscar habitaciones de formas muy
variadas: por tema Pokémon, por rango de precios, con detalles de reviews o sin
ellos. La API REST tradicional requiere múltiples endpoints. Kai descubre que
GraphQL permite a cada entrenador pedir exactamente lo que necesita, como
Alakazam que lee la mente de su entrenador.

---

### 2.1 — GraphQL con Spring for GraphQL

> **Como** entrenador Pokémon,
> **quiero** consultar hoteles con exactamente los campos que necesito,
> **para** no recibir datos innecesarios y optimizar mi experiencia móvil.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 6-8 horas
**Dependencias:** Semana 7 del BACKLOG (CRUD Rooms), Semana 10 (Reviews)

**Tareas:**

- [ ] Agregar dependencia `spring-boot-starter-graphql`
- [ ] Definir schema GraphQL (`src/main/resources/graphql/schema.graphqls`):

  ```graphql
  type Query {
    hotel(id: ID!): Hotel
    hotels(filter: HotelFilter, page: Int, size: Int): HotelPage!
    availableRooms(hotelId: ID!, checkIn: String!, checkOut: String!): [Room!]!
  }

  type Mutation {
    createReservation(input: CreateReservationInput!): Reservation!
    cancelReservation(id: ID!): Reservation!
  }

  type Hotel {
    id: ID!
    name: String!
    address: String!
    rooms: [Room!]!
    reviews: [Review!]!
    averageRating: Float
  }

  type Room {
    id: ID!
    number: String!
    type: RoomType!
    pokemonTheme: PokemonTheme!
    pricePerNight: Float!
    status: RoomStatus!
  }
  ```

- [ ] Implementar `@QueryMapping` y `@MutationMapping` en controladores GraphQL
- [ ] Implementar `@SchemaMapping` para resolver relaciones (Hotel → Rooms, Hotel → Reviews)
- [ ] Resolver el problema N+1 con `@BatchMapping` o DataLoader
- [ ] Habilitar GraphiQL para exploración interactiva (`/graphiql`)

**Criterios de aceptación:**

1. La query `{ hotel(id: "1") { name rooms { number pokemonTheme } } }` retorna solo los campos pedidos
2. Las relaciones se resuelven de forma lazy — si no se piden `rooms`, no se consultan
3. El problema N+1 está resuelto: listar 10 hoteles con sus rooms genera ≤ 2 queries SQL
4. GraphiQL está accesible en `/graphiql` y permite explorar el schema
5. Las mutaciones validan input y retornan errores GraphQL estándar

**Métricas de éxito:**

- Reducción de payload: ≥ 40% menos datos transferidos vs REST equivalente
- N+1 eliminado: máximo 2 queries SQL para `hotels { rooms }` con 10 hoteles

**💡 Hints:**

- Usa `@BatchMapping` en lugar de `@SchemaMapping` para resolver colecciones: recibe `List<Hotel>` y retorna `Map<Hotel, List<Room>>`
- Para el N+1, piensa en `JOIN FETCH` en el repositorio o usa DataLoader de GraphQL-Java
- En Kotlin, los data classes mapean directamente a tipos GraphQL

**📚 Recursos:**

- [Spring for GraphQL docs](https://docs.spring.io/spring-graphql/reference/)
- [GraphQL Java DataLoader](https://www.graphql-java.com/documentation/batching)

---

### 2.2 — Subscriptions GraphQL en tiempo real

> **Como** entrenador Pokémon,
> **quiero** recibir notificaciones en tiempo real cuando mi habitación esté lista,
> **para** no tener que consultar repetidamente el estado.

**Nivel:** 🟠 Avanzado
**Tiempo estimado:** 4-6 horas
**Dependencias:** Ejercicio 2.1 (GraphQL base), Semana 9 del BACKLOG (eventos)

**Tareas:**

- [ ] Implementar `@SubscriptionMapping` para eventos de reservación:

  ```graphql
  type Subscription {
    reservationStatusChanged(reservationId: ID!): ReservationUpdate!
    roomAvailabilityChanged(hotelId: ID!): Room!
  }
  ```

- [ ] Integrar con Spring Events: cuando una reserva cambia de estado, emitir a la subscription
- [ ] Usar WebSocket como transporte para subscriptions
- [ ] Crear test de integración que verifica la recepción de eventos

**Criterios de aceptación:**

1. Un cliente suscrito a `reservationStatusChanged` recibe actualizaciones en < 500ms
2. Las subscriptions usan WebSocket (`ws://`) como transporte
3. La desconexión del cliente libera los recursos de la subscription
4. Los eventos de dominio (`ReservationConfirmedEvent`) se propagan a las subscriptions

**Métricas de éxito:**

- Latencia de notificación: < 500ms desde el evento hasta el cliente

**💡 Hints:**

- Usa `Flux<T>` o `Flow<T>` de Kotlin para la subscription
- `Sinks.many().multicast().onBackpressureBuffer()` es útil para crear el publisher reactivo

**📚 Recursos:**

- [Spring GraphQL Subscriptions](https://docs.spring.io/spring-graphql/reference/transports.html#server.websocket)

---

### 2.3 — gRPC para comunicación interna de servicios

> **Como** arquitecto del Hotel Pokémon,
> **quiero** usar gRPC para comunicación entre módulos internos,
> **para** obtener comunicación tipada, eficiente y con contratos estrictos.

**Nivel:** 🟠 Avanzado
**Tiempo estimado:** 6-8 horas
**Dependencias:** Ejercicio 2.1 (para comparar con GraphQL)

**Tareas:**

- [ ] Agregar dependencias: `grpc-spring-boot-starter`, `protobuf-gradle-plugin`
- [ ] Definir contratos en Protobuf (`src/main/proto/hotel_service.proto`):

  ```protobuf
  service HotelService {
    rpc GetHotel (GetHotelRequest) returns (HotelResponse);
    rpc ListRooms (ListRoomsRequest) returns (stream RoomResponse);
    rpc CheckAvailability (AvailabilityRequest) returns (AvailabilityResponse);
  }
  ```

- [ ] Implementar servidor gRPC con `@GrpcService`
- [ ] Implementar cliente gRPC para consumir el servicio
- [ ] Crear test comparativo: REST vs GraphQL vs gRPC (latencia y tamaño de payload)
- [ ] Documentar cuándo usar cada protocolo

**Criterios de aceptación:**

1. El servidor gRPC arranca en un puerto separado (ej. 9090)
2. El contrato `.proto` genera código Kotlin/Java automáticamente con Gradle
3. La comunicación gRPC usa HTTP/2 y serialización binaria (Protobuf)
4. El test comparativo muestra la diferencia de latencia entre REST, GraphQL y gRPC
5. El documento explica cuándo usar cada uno (API pública → REST/GraphQL, interna → gRPC)

**Métricas de éxito:**

- Latencia gRPC vs REST: gRPC ≥ 30% más rápido en llamadas internas
- Tamaño de payload: Protobuf ≥ 50% más pequeño que JSON equivalente

**💡 Hints:**

- Usa `net.devh:grpc-spring-boot-starter` (la más mantenida para Spring Boot 3)
- Para streaming, usa `StreamObserver` o `Flow<T>` de Kotlin con `grpc-kotlin`
- No expongas gRPC al público — es para comunicación interna entre servicios

**📚 Recursos:**

- [gRPC-Kotlin](https://github.com/grpc/grpc-kotlin)
- [gRPC Spring Boot Starter](https://yidongnan.github.io/grpc-spring-boot-starter/)

---

## Fase 3 — Concurrencia y Rendimiento ⚡

**Insignia:** _Velocidad de Jolteon_ — Procesar miles de peticiones sin sudar
**Narrativa:** Es temporada alta en el Hotel Pokémon: el Torneo Regional atrae
a miles de entrenadores que quieren reservar al mismo tiempo. El sistema
necesita manejar concurrencia masiva sin bloqueos ni race conditions. Kai debe
dominar los tres caminos de la velocidad: Virtual Threads, Coroutines y
programación reactiva.

---

### 3.1 — Virtual Threads con Project Loom (JDK 25)

> **Como** sistema del Hotel Pokémon,
> **quiero** usar Virtual Threads para manejar miles de conexiones concurrentes,
> **para** escalar sin aumentar los recursos de hardware.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 3-4 horas
**Dependencias:** Semana 10 del BACKLOG completada

**Tareas:**

- [ ] Habilitar Virtual Threads en Spring Boot:

  ```yaml
  spring.threads.virtual.enabled: true
  ```

- [ ] Verificar que Tomcat usa Virtual Threads por defecto
- [ ] Crear benchmark comparativo:
  - Escenario: 500 requests concurrentes a `GET /api/v1/hotels` con query a PostgreSQL (100ms simulado)
  - Medir con Platform Threads vs Virtual Threads
- [ ] Configurar `@Async` con Virtual Thread executor:

  ```kotlin
  @Bean
  fun virtualThreadExecutor(): AsyncTaskExecutor =
      SimpleAsyncTaskExecutor().apply { setVirtualThreads(true) }
  ```

- [ ] Documentar las diferencias de rendimiento medidas

**Criterios de aceptación:**

1. Con Virtual Threads, 500 requests concurrentes se procesan sin `RejectedExecutionException`
2. El throughput mejora ≥ 2x comparado con Platform Threads (pool de 200)
3. El uso de memoria no crece linealmente con el número de threads virtuales
4. Los `@Async` tasks usan Virtual Threads en lugar del `SimpleAsyncTaskExecutor` por defecto

**Métricas de éxito:**

- Throughput: ≥ 2000 req/s con Virtual Threads vs ≤ 800 req/s con Platform Threads
- Memoria: < 500MB para 500 Virtual Threads concurrentes
- Latencia P99: < 200ms bajo carga

**💡 Hints:**

- Spring Boot 4.x con `spring.threads.virtual.enabled=true` configura Tomcat automáticamente
- Los Virtual Threads NO son más rápidos que los platform threads — son más **escalables** (usan menos memoria)
- Cuidado con `synchronized` blocks — bloquean el carrier thread. Usa `ReentrantLock` en su lugar

**📚 Recursos:**

- [Spring Boot Virtual Threads](https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html)
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)

---

### 3.2 — Kotlin Coroutines para lógica de negocio asíncrona

> **Como** desarrollador del Hotel Pokémon,
> **quiero** usar Coroutines de Kotlin para operaciones asíncronas complejas,
> **para** escribir código concurrente legible sin callbacks ni reactive streams.

**Nivel:** 🟠 Avanzado
**Tiempo estimado:** 6-8 horas
**Dependencias:** Ejercicio 3.1 (Virtual Threads)

**Tareas:**

- [ ] Agregar dependencias: `kotlinx-coroutines-core`, `kotlinx-coroutines-reactor`
- [ ] Refactorizar `ReservationService` para usar `suspend fun`:

  ```kotlin
  suspend fun createReservation(command: CreateReservationCommand): Reservation {
      val room = async { roomService.findById(command.roomId) }
      val user = async { userService.findById(command.userId) }
      // Ambas consultas en paralelo
      return reservationFactory.create(room.await(), user.await(), command)
  }
  ```

- [ ] Implementar timeout con `withTimeout()`:

  ```kotlin
  withTimeout(2000) {
      paymentGateway.processPayment(payment)
  }
  ```

- [ ] Crear `CoroutineScope` personalizado con `SupervisorJob` para manejo de errores
- [ ] Implementar `Flow<T>` para streaming de disponibilidad de habitaciones
- [ ] Comparar rendimiento: coroutines vs virtual threads vs blocking

**Criterios de aceptación:**

1. Las operaciones paralelas (buscar room + user) se ejecutan concurrentemente con `async/await`
2. El timeout de 2s cancela la operación limpiamente sin dejar recursos colgados
3. Un error en una coroutine hija no cancela las demás (usando `SupervisorJob`)
4. El `Flow` de disponibilidad emite actualizaciones en tiempo real
5. Los tests de coroutines usan `runTest {}` con `TestDispatcher`

**Métricas de éxito:**

- Latencia de `createReservation`: reducida ≥ 40% al paralelizar consultas
- Cancelación limpia: 100% de recursos liberados tras timeout

**💡 Hints:**

- Usa `coroutineScope { }` para structured concurrency — si una falla, todas se cancelan
- Para Spring MVC (no WebFlux), Spring Boot 4.x soporta `suspend fun` en controllers directamente
- No mezcles `runBlocking` con coroutines dentro de un request — causa deadlocks

**📚 Recursos:**

- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Spring + Kotlin Coroutines](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html)

---

### 3.3 — WebFlux reactivo (ejercicio comparativo)

> **Como** desarrollador,
> **quiero** implementar un endpoint con WebFlux reactivo,
> **para** comparar con Virtual Threads y Coroutines y elegir el mejor enfoque.

**Nivel:** 🟠 Avanzado
**Tiempo estimado:** 6-8 horas
**Dependencias:** Ejercicios 3.1 y 3.2

**Tareas:**

- [ ] Crear un módulo o controller separado con endpoints reactivos:

  ```kotlin
  @RestController
  @RequestMapping("/reactive/hotels")
  class ReactiveHotelController(private val hotelRepository: ReactiveHotelRepository) {
      @GetMapping("/{id}")
      fun getById(@PathVariable id: String): Mono<HotelResponse> =
          hotelRepository.findById(id).map { HotelResponse.fromDomain(it) }

      @GetMapping
      fun getAll(): Flux<HotelResponse> =
          hotelRepository.findAll().map { HotelResponse.fromDomain(it) }
  }
  ```

- [ ] Usar R2DBC (reactive database driver) para PostgreSQL
- [ ] Crear benchmark con Gatling o k6 comparando los 3 enfoques:
  - `/api/v1/hotels` → Blocking + Virtual Threads
  - `/coroutines/hotels` → Kotlin Coroutines
  - `/reactive/hotels` → WebFlux + R2DBC
- [ ] Documentar resultados con tablas comparativas
- [ ] Escribir ADR (Architecture Decision Record) justificando la elección final

**Criterios de aceptación:**

1. Los 3 endpoints retornan los mismos datos
2. El benchmark ejecuta 1000 req/s durante 60 segundos con cada enfoque
3. La documentación incluye tablas comparativas de: throughput, latencia P50/P95/P99, uso de memoria
4. El ADR justifica cuándo usar cada enfoque en el contexto del Hotel Pokémon

**Métricas de éxito:**

- Benchmark documentado con métricas reales de cada enfoque
- ADR con decisión fundamentada

**💡 Hints:**

- WebFlux y MVC no pueden coexistir fácilmente en el mismo classpath — usa perfiles o módulos separados
- R2DBC no soporta relaciones lazy como JPA — planifica queries manualmente
- Para la comparativa, usa `k6` (`brew install k6`) — es más ligero que Gatling

**📚 Recursos:**

- [Spring WebFlux docs](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [R2DBC PostgreSQL](https://github.com/pgjdbc/r2dbc-postgresql)
- [k6 load testing](https://k6.io/docs/)

---

### 3.4 — Optimistic Locking y manejo de concurrencia

> **Como** sistema de reservas,
> **quiero** evitar que dos entrenadores reserven la misma habitación al mismo tiempo,
> **para** prevenir overselling.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 3-4 horas
**Dependencias:** Semana 9 del BACKLOG (sistema de reservaciones)

**Tareas:**

- [ ] Agregar `@Version` a `RoomEntity` y `ReservationEntity`
- [ ] Capturar `OptimisticLockingFailureException` en el servicio
- [ ] Implementar retry automático con `@Retryable`:

  ```kotlin
  @Retryable(
      value = [OptimisticLockingFailureException::class],
      maxAttempts = 3,
      backoff = Backoff(delay = 100, multiplier = 2.0)
  )
  @Transactional
  fun reserveRoom(roomId: String, userId: String): Reservation { ... }
  ```

- [ ] Crear test de concurrencia con `CountDownLatch` y múltiples threads

**Criterios de aceptación:**

1. Dos reservas simultáneas para la misma habitación: solo una tiene éxito
2. La reserva fallida recibe `409 Conflict` con mensaje claro
3. El retry resuelve conflictos transitorios en ≤ 3 intentos
4. El test de concurrencia con 10 threads simultáneos no produce inconsistencias

**Métricas de éxito:**

- 0 reservas duplicadas bajo carga concurrente
- Tasa de resolución por retry: > 90%

**💡 Hints:**

- `@Version` hace que Hibernate agregue `WHERE version = ?` al UPDATE
- No uses `synchronized` — no funciona en un cluster multi-instancia
- El `CountDownLatch` en tests: todas las coroutines esperan la señal y ejecutan simultáneamente

**📚 Recursos:**

- [JPA Optimistic Locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html)
- [Spring Retry](https://docs.spring.io/spring-retry/reference/)

---

## Fase 4 — Inteligencia Artificial 🧠

**Insignia:** _Psíquico de Mewtwo_ — El poder de la IA al servicio del hotel
**Narrativa:** El Profesor Oak tiene una idea revolucionaria: usar la
inteligencia artificial para mejorar la experiencia de los huéspedes. Un
asistente virtual que recomiende habitaciones, analice reseñas y genere
contenido personalizado. Kai debe integrar modelos de lenguaje con el
ecosistema Spring Boot, creando un Pokémon digital que entiende las necesidades
de cada entrenador.

---

### 4.1 — Chatbot de recomendaciones con Spring AI

> **Como** entrenador Pokémon,
> **quiero** chatear con un asistente virtual del hotel,
> **para** recibir recomendaciones personalizadas de habitaciones y tours.

**Nivel:** 🟠 Avanzado
**Tiempo estimado:** 6-8 horas
**Dependencias:** Semana 10 del BACKLOG completada

**Tareas:**

- [ ] Agregar dependencia `spring-ai-openai-spring-boot-starter` (o `spring-ai-ollama` para local)
- [ ] Configurar el modelo en `application.yaml`:

  ```yaml
  spring.ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat.options:
        model: gpt-4o-mini
        temperature: 0.7
    # Alternativa local con Ollama:
    # ollama:
    #   base-url: http://localhost:11434
    #   chat.options.model: llama3
  ```

- [ ] Crear feature `assistant/` con:
  - `AssistantController` — `POST /api/v1/assistant/chat`
  - `HotelAssistantService` — Lógica de conversación con contexto del hotel
- [ ] Implementar system prompt con contexto del Hotel Pokémon:

  ```
  Eres Rotom, el asistente virtual del Gran Hotel Pokémon. Ayudas a los
  entrenadores a encontrar la habitación perfecta según sus preferencias
  de tipo Pokémon, presupuesto y necesidades especiales.
  ```

- [ ] Implementar Function Calling para que la IA consulte datos reales:
  - `searchAvailableRooms(theme, checkIn, checkOut, maxPrice)` → Consulta a RoomService
  - `getHotelInfo(hotelId)` → Consulta a HotelService
  - `createReservation(roomId, userId, checkIn, checkOut)` → Crear reserva
- [ ] Implementar memoria de conversación con `ChatMemory`

**Criterios de aceptación:**

1. El chatbot responde preguntas sobre el hotel con contexto real
2. "¿Qué habitaciones temáticas de tipo fuego tienen disponibles?" → Consulta real a la BD y responde con datos reales
3. El function calling ejecuta operaciones reales (buscar habitaciones, crear reservas)
4. La memoria de conversación mantiene contexto entre mensajes del mismo usuario
5. El chatbot funciona tanto con OpenAI como con Ollama (configuración por perfil)

**Métricas de éxito:**

- Tiempo de respuesta del chatbot: < 5s (incluyendo llamada a LLM)
- Precisión de function calling: 100% de las funciones se invocan correctamente

**💡 Hints:**

- Para Function Calling, registra funciones con `@Bean` y `@Description` en Spring AI
- Usa `MessageChatMemoryAdvisor` con un `InMemoryChatMemory` para conversaciones stateful
- Para desarrollo local, Ollama es gratuito: `ollama pull llama3` y listo

**📚 Recursos:**

- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)
- [Spring AI Function Calling](https://docs.spring.io/spring-ai/reference/api/functions.html)
- [Ollama](https://ollama.ai/)

---

### 4.2 — RAG: Respuestas basadas en datos del hotel

> **Como** asistente virtual del Hotel Pokémon,
> **quiero** responder preguntas basándome en la documentación real del hotel,
> **para** dar información precisa y actualizada sin alucinaciones.

**Nivel:** 🔴 Experto
**Tiempo estimado:** 8-10 horas
**Dependencias:** Ejercicio 4.1 (Chatbot base)

**Tareas:**

- [ ] Agregar vector store: `spring-ai-pgvector-store` (usa PostgreSQL con pgvector)
- [ ] Crear pipeline de ingesta de documentos:
  - Indexar descripciones de habitaciones, tours y servicios del hotel
  - Indexar reseñas de huéspedes
  - Chunking inteligente por párrafos
- [ ] Implementar `QuestionAnswerAdvisor` con RAG:

  ```kotlin
  @Bean
  fun chatClient(builder: ChatClient.Builder, vectorStore: VectorStore) =
      builder
          .defaultAdvisors(QuestionAnswerAdvisor(vectorStore))
          .defaultSystem("Eres Rotom, el asistente del Gran Hotel Pokémon...")
          .build()
  ```

- [ ] Crear endpoint para ingerir nuevos documentos: `POST /api/v1/assistant/ingest`
- [ ] Crear endpoint para consulta RAG: `POST /api/v1/assistant/ask`

**Criterios de aceptación:**

1. "¿La Suite Charizard tiene chimenea?" → Responde basándose en los datos indexados del hotel
2. Las respuestas incluyen la fuente de información (de qué documento se obtuvo)
3. Si no hay información relevante, el asistente dice "No tengo esa información" en lugar de inventar
4. Nuevas habitaciones/servicios se indexan automáticamente al crearlas
5. La búsqueda vectorial retorna los 5 fragmentos más relevantes

**Métricas de éxito:**

- Precisión de las respuestas RAG: ≥ 90% (basado en datos verificables)
- Tasa de alucinaciones: < 5%
- Tiempo de respuesta RAG: < 3s

**💡 Hints:**

- Habilita pgvector en PostgreSQL: `CREATE EXTENSION vector;`
- El `DocumentReader` de Spring AI soporta Markdown, PDF y texto plano
- Calibra el `similarity threshold` para balancear precisión y recall

**📚 Recursos:**

- [Spring AI RAG](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)
- [PGVector Spring AI](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)

---

### 4.3 — Análisis de sentimiento de reseñas

> **Como** gerente del Hotel Pokémon,
> **quiero** analizar automáticamente el sentimiento de las reseñas,
> **para** detectar problemas de servicio antes de que escalen.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 3-4 horas
**Dependencias:** Semana 10 del BACKLOG (reviews), Ejercicio 4.1

**Tareas:**

- [ ] Crear `SentimentAnalysisService` que use Spring AI para clasificar reseñas:

  ```kotlin
  fun analyzeSentiment(reviewText: String): SentimentResult {
      val response = chatClient.prompt()
          .user("Clasifica el sentimiento de esta reseña como POSITIVE, NEUTRAL o NEGATIVE. " +
                "Incluye un score de 0 a 1 y los temas principales mencionados. " +
                "Reseña: $reviewText")
          .call()
          .entity(SentimentResult::class.java)
      return response
  }
  ```

- [ ] Procesar reseñas asíncronamente con `@TransactionalEventListener` al crear una review
- [ ] Almacenar el resultado del análisis en la entidad `Review` (sentiment, score, topics)
- [ ] Crear endpoint de dashboard: `GET /api/v1/analytics/sentiment`
  - Sentimiento promedio por hotel
  - Temas más mencionados (positivos y negativos)
  - Tendencia temporal

**Criterios de aceptación:**

1. Al crear una reseña, el sentimiento se analiza automáticamente en segundo plano
2. El dashboard muestra sentimiento agregado por hotel con porcentajes
3. Los temas se extraen correctamente (ej. "limpieza", "atención", "comida")
4. El análisis no bloquea la creación de la reseña

**Métricas de éxito:**

- Precisión de clasificación: ≥ 85% (verificado manualmente con 20 reseñas de prueba)
- Latencia del análisis: < 3s por reseña

**💡 Hints:**

- Usa structured output de Spring AI para obtener `SentimentResult` tipado directamente
- El `@TransactionalEventListener(phase = AFTER_COMMIT)` asegura que la review ya está persistida
- Para el dashboard, usa queries de agregación en el repositorio JPA

**📚 Recursos:**

- [Spring AI Structured Output](https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html)

---

### 4.4 — Generación de contenido con IA

> **Como** equipo de marketing del Hotel Pokémon,
> **quiero** generar automáticamente descripciones de habitaciones y sugerencias de tours,
> **para** enriquecer el catálogo sin esfuerzo manual.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 3-4 horas
**Dependencias:** Ejercicio 4.1

**Tareas:**

- [ ] Crear `ContentGenerationService`:
  - `generateRoomDescription(room: Room): String` — Genera descripción temática
  - `generateTourSuggestion(pokemonTheme: PokemonTheme): String` — Sugiere tours
  - `generateWelcomeMessage(user: User): String` — Mensaje de bienvenida personalizado
- [ ] Usar templates de prompt con variables:

  ```kotlin
  val prompt = PromptTemplate("""
      Genera una descripción atractiva para una habitación de hotel temática Pokémon.
      Tipo: {roomType}
      Tema Pokémon: {pokemonTheme}
      Capacidad: {capacity} personas
      La descripción debe ser en español, máximo 200 palabras, e incluir
      referencias al tipo Pokémon de la habitación.
  """)
  ```

- [ ] Endpoint: `POST /api/v1/content/generate/room-description`
- [ ] Caché de generaciones para no repetir llamadas al LLM

**Criterios de aceptación:**

1. Las descripciones generadas son coherentes con el tema Pokémon de la habitación
2. El contenido es en español y ≤ 200 palabras
3. Generaciones idénticas se sirven desde caché (mismos parámetros = misma descripción)
4. El administrador puede regenerar el contenido si no le satisface

**Métricas de éxito:**

- Calidad subjetiva: las descripciones son usables sin edición en ≥ 70% de los casos
- Caché hit rate: > 80% después de generar contenido para todas las habitaciones

**💡 Hints:**

- Usa `PromptTemplate` de Spring AI para templates reutilizables con variables
- Para caché, usa `@Cacheable` con una key compuesta por los parámetros del prompt

**📚 Recursos:**

- [Spring AI Prompt Templates](https://docs.spring.io/spring-ai/reference/api/prompt.html)

---

## Fase 5 — Comunicación entre Servicios y Caché Distribuido 🌐

**Insignia:** _Red de Porygon_ — Conexión eficiente entre sistemas
**Narrativa:** El Hotel Pokémon ha crecido: ahora tiene sucursales en distintas
ciudades. Los sistemas necesitan comunicarse entre sí de forma eficiente, y los
datos más consultados deben estar en una caché distribuida para que todas las
sucursales tengan acceso rápido. Kai despliega una red de Porygon que conecta
todo el ecosistema.

---

### 5.1 — Redis como caché distribuido

> **Como** sistema del Hotel Pokémon con múltiples instancias,
> **quiero** usar Redis como caché compartido,
> **para** que todas las instancias sirvan los mismos datos cacheados.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 4-6 horas
**Dependencias:** Semana 9 del BACKLOG (caché con Caffeine)

**Tareas:**

- [ ] Agregar dependencia `spring-boot-starter-data-redis`
- [ ] Configurar Redis en `application.yaml`:

  ```yaml
  spring.data.redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
  spring.cache:
    type: redis
    redis:
      time-to-live: 300s  # 5 minutos
      cache-null-values: false
  ```

- [ ] Agregar Redis al `docker-compose.yml`
- [ ] Migrar `@Cacheable` de Caffeine a Redis para datos compartidos:
  - Catálogo de hoteles → Redis (compartido entre instancias)
  - Detalles de habitación → Redis
  - Datos de sesión → Caffeine local (no compartible)
- [ ] Implementar caché multinivel: Caffeine (L1 local) + Redis (L2 distribuido)
- [ ] Serializar con Jackson (no Java serialization) para que el caché sea versionable

**Criterios de aceptación:**

1. Dos instancias de la app leen el mismo caché de Redis
2. Al invalidar el caché en una instancia, la otra también lo ve invalidado
3. El TTL de 5 minutos funciona correctamente
4. Los objetos cacheados usan serialización JSON (no binaria de Java)
5. Métricas de Redis (hits/misses/evictions) visibles en Actuator

**Métricas de éxito:**

- Cache hit rate: > 70% en peticiones de lectura
- Latencia de cache hit: < 5ms (Redis local)
- Reducción de queries a PostgreSQL: > 50%

**💡 Hints:**

- Usa `RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(Jackson2JsonRedisSerializer())`
- Para caché multinivel, crea un `CacheManager` compuesto con `CompositeCacheManager`
- Cuidado con la serialización de objetos con relaciones JPA — cachea DTOs, no entidades

**📚 Recursos:**

- [Spring Data Redis](https://docs.spring.io/spring-data/redis/reference/)
- [Spring Cache with Redis](https://docs.spring.io/spring-boot/reference/io/caching.html)

---

### 5.2 — Sesiones distribuidas con Redis

> **Como** sistema escalable,
> **quiero** compartir sesiones entre instancias,
> **para** que un usuario no pierda su sesión al ser balanceado a otra instancia.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 2-3 horas
**Dependencias:** Ejercicio 5.1 (Redis configurado)

**Tareas:**

- [ ] Agregar dependencia `spring-session-data-redis`
- [ ] Configurar Spring Session:

  ```yaml
  spring.session:
    store-type: redis
    redis.flush-mode: on_save
    timeout: 30m
  ```

- [ ] Crear endpoint que demuestre persistencia de sesión: `GET /api/v1/session/info`
- [ ] Test: levantar 2 instancias en puertos distintos y verificar que la sesión se comparte

**Criterios de aceptación:**

1. La sesión persiste después de reiniciar la aplicación
2. Dos instancias comparten la misma sesión para el mismo usuario
3. La sesión expira después de 30 minutos de inactividad

**💡 Hints:**

- En una API stateless con JWT, las sesiones pueden parecer innecesarias — pero son útiles para datos temporales como carritos de compras

**📚 Recursos:**

- [Spring Session Redis](https://docs.spring.io/spring-session/reference/guides/boot-redis.html)

---

### 5.3 — Problem Details RFC 9457

> **Como** consumidor de la API,
> **quiero** recibir errores en formato estándar RFC 9457,
> **para** parsear errores de forma consistente sin importar el endpoint.

**Nivel:** 🟡 Intermedio
**Tiempo estimado:** 3-4 horas
**Dependencias:** Semana 4 del BACKLOG (GlobalExceptionHandler)

**Tareas:**

- [ ] Migrar `ErrorResponse` personalizado a `ProblemDetail` de Spring:

  ```kotlin
  @ExceptionHandler(RoomNotAvailableException::class)
  fun handleRoomNotAvailable(ex: RoomNotAvailableException): ProblemDetail {
      val problem = ProblemDetail.forStatusAndDetail(
          HttpStatus.CONFLICT,
          ex.message ?: "La habitación no está disponible"
      )
      problem.title = "Habitación No Disponible"
      problem.setProperty("roomId", ex.roomId)
      problem.setProperty("requestedDates", ex.dates)
      return problem
  }
  ```

- [ ] Configurar Spring Boot para usar Problem Details por defecto:

  ```yaml
  spring.mvc.problemdetails.enabled: true
  ```

- [ ] Migrar todas las excepciones del `GlobalExceptionHandler` al formato `ProblemDetail`
- [ ] Actualizar la documentación OpenAPI para reflejar el formato RFC 9457

**Criterios de aceptación:**

1. Todos los errores retornan `Content-Type: application/problem+json`
2. El body incluye: `type`, `title`, `status`, `detail` y `instance`
3. Las excepciones de negocio incluyen propiedades extendidas relevantes
4. La documentación Swagger muestra el schema de ProblemDetail

**📚 Recursos:**

- [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457)
- [Spring Boot Problem Details](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.error-handling)

---

## Fase 6 — Compilación Nativa con GraalVM 💎

**Insignia:** _Cristal de Dialga_ — Dominio del tiempo (arranque instantáneo)
**Narrativa:** El Hotel Pokémon necesita funciones serverless para manejar
picos de demanda: funciones que arranquen en milisegundos para procesar
reservas masivas durante el Torneo Regional. Kai descubre el poder de GraalVM
Native Image: compilar el hotel a código máquina nativo que arranca 100 veces
más rápido. Como Dialga, que controla el tiempo, Kai comprime el arranque
de minutos a milisegundos.

---

### 6.1 — Compilación nativa AOT con GraalVM

> **Como** equipo de DevOps,
> **quiero** compilar la aplicación a un binario nativo,
> **para** reducir el tiempo de arranque y el consumo de memoria en producción.

**Nivel:** 🔴 Experto
**Tiempo estimado:** 8-12 horas
**Dependencias:** Todas las fases anteriores completadas (o al menos BACKLOG completo)

**Tareas:**

- [ ] Instalar GraalVM y configurar `GRAALVM_HOME`
- [ ] Agregar plugin de GraalVM Native Build Tools en `build.gradle.kts`:

  ```kotlin
  plugins {
      id("org.graalvm.buildtools.native") version "0.10.6"
  }
  ```

- [ ] Ejecutar compilación nativa: `./gradlew nativeCompile`
- [ ] Resolver errores de reflexión:
  - Configurar `reflect-config.json` para clases JPA/Hibernate
  - Configurar `resource-config.json` para archivos de recursos
  - Usar `@RegisterReflectionForBinding` en DTOs
- [ ] Crear archivo `META-INF/native-image/` con configuraciones de runtime hints
- [ ] Medir y documentar:
  - Tiempo de arranque (JVM vs Native)
  - Uso de memoria (JVM vs Native)
  - Tamaño de la imagen
  - Tiempo de compilación
- [ ] Crear Dockerfile para imagen nativa:

  ```dockerfile
  FROM ghcr.io/graalvm/native-image:ol9-java25 AS builder
  # Build nativo
  FROM gcr.io/distroless/base-debian12
  COPY --from=builder /app/build/native/nativeCompile/hotel-pokemon .
  ENTRYPOINT ["./hotel-pokemon"]
  ```

**Criterios de aceptación:**

1. La aplicación compila a binario nativo sin errores
2. Todos los endpoints funcionan correctamente en modo nativo
3. Las migraciones Flyway se ejecutan al arrancar
4. JPA/Hibernate funciona con las entidades del proyecto

**Métricas de éxito:**

- Tiempo de arranque JVM: ~3-5s → Native: < 200ms (**≥ 15x más rápido**)
- Memoria en reposo JVM: ~300MB → Native: < 80MB (**≥ 3x menos**)
- Tamaño de imagen Docker JVM: ~200MB → Native: < 100MB
- Tiempo de compilación nativa: documentado (esperado 3-10 minutos)

**💡 Hints:**

- Spring Boot 4.x tiene excelente soporte AOT — `./gradlew processAot` genera los hints automáticamente
- Ejecuta `./gradlew nativeTest` para correr tests en modo nativo y detectar problemas de reflexión
- Los problemas más comunes son: serialización Jackson, proxies JPA, y `@ConfigurationProperties`
- Si una clase falla en nativo, añade `@Reflective` o regístrala en `RuntimeHintsRegistrar`

**📚 Recursos:**

- [Spring Boot GraalVM Native Image](https://docs.spring.io/spring-boot/reference/packaging/native-image/index.html)
- [GraalVM Native Image Reference](https://www.graalvm.org/latest/reference-manual/native-image/)
- [Spring Boot AOT Processing](https://docs.spring.io/spring-boot/reference/packaging/native-image/advanced-topics.html)

---

### 6.2 — Benchmarking JVM vs Native

> **Como** arquitecto,
> **quiero** un benchmark exhaustivo comparando JVM y Native Image,
> **para** tomar decisiones informadas sobre cuándo usar cada modo.

**Nivel:** 🟠 Avanzado
**Tiempo estimado:** 4-6 horas
**Dependencias:** Ejercicio 6.1

**Tareas:**

- [ ] Crear script de benchmark automatizado:

  ```bash
  # benchmark.sh
  # 1. Arrancar en modo JVM, medir startup y memory
  # 2. Ejecutar k6 con 100 req/s durante 60s
  # 3. Arrancar en modo native, repetir
  # 4. Comparar y generar reporte
  ```

- [ ] Medir las siguientes métricas:

  | Métrica                 | JVM | Native |
  | ----------------------- | --- | ------ |
  | Startup time            | ?   | ?      |
  | RSS memory (idle)       | ?   | ?      |
  | RSS memory (under load) | ?   | ?      |
  | Throughput (req/s)      | ?   | ?      |
  | Latency P50             | ?   | ?      |
  | Latency P99             | ?   | ?      |
  | Docker image size       | ?   | ?      |
  | Build time              | ?   | ?      |

- [ ] Documentar limitaciones encontradas:
  - ¿Qué funcionalidades no compilan en nativo?
  - ¿Qué workarounds fueron necesarios?
  - ¿Cuánto tiempo tomó resolver los problemas de reflexión?
- [ ] Escribir ADR con recomendación: ¿Cuándo usar JVM vs Native en producción?

**Criterios de aceptación:**

1. El benchmark ejecuta con ambos modos bajo las mismas condiciones
2. El reporte incluye todas las métricas de la tabla
3. Las limitaciones están documentadas con las soluciones aplicadas
4. El ADR tiene una recomendación clara con justificación

**💡 Hints:**

- Usa `time ./hotel-pokemon` para medir startup y `ps aux | grep hotel` para RSS
- El throughput nativo puede ser _menor_ que JVM bajo carga sostenida (JIT vs AOT)
- El caso de uso ideal para nativo es serverless (cold start importa) y microservicios con baja memoria

**📚 Recursos:**

- [Benchmarking GraalVM](https://www.graalvm.org/latest/reference-manual/native-image/guides/optimize-native-executable-with-pgo/)

---

## 📈 Resumen de Ejercicios

| #   | Ejercicio                       | Fase         | Nivel        | Tiempo | Dependencias     |
| --- | ------------------------------- | ------------ | ------------ | ------ | ---------------- |
| 1.1 | Circuit Breaker                 | Resiliencia  | 🟡 Intermedio | 4-6h   | BACKLOG S10      |
| 1.2 | Retry con backoff               | Resiliencia  | 🟡 Intermedio | 2-3h   | 1.1              |
| 1.3 | Rate Limiting                   | Resiliencia  | 🟡 Intermedio | 3-4h   | BACKLOG S5       |
| 1.4 | Time Limiter + fallback cascada | Resiliencia  | 🟠 Avanzado   | 3-4h   | 1.1, 1.2, 1.3    |
| 2.1 | GraphQL con Spring              | GraphQL/gRPC | 🟡 Intermedio | 6-8h   | BACKLOG S7, S10  |
| 2.2 | GraphQL Subscriptions           | GraphQL/gRPC | 🟠 Avanzado   | 4-6h   | 2.1, BACKLOG S9  |
| 2.3 | gRPC para servicios internos    | GraphQL/gRPC | 🟠 Avanzado   | 6-8h   | 2.1              |
| 3.1 | Virtual Threads (Loom)          | Concurrencia | 🟡 Intermedio | 3-4h   | BACKLOG S10      |
| 3.2 | Kotlin Coroutines               | Concurrencia | 🟠 Avanzado   | 6-8h   | 3.1              |
| 3.3 | WebFlux comparativo             | Concurrencia | 🟠 Avanzado   | 6-8h   | 3.1, 3.2         |
| 3.4 | Optimistic Locking              | Concurrencia | 🟡 Intermedio | 3-4h   | BACKLOG S9       |
| 4.1 | Chatbot con Spring AI           | IA           | 🟠 Avanzado   | 6-8h   | BACKLOG S10      |
| 4.2 | RAG con pgvector                | IA           | 🔴 Experto    | 8-10h  | 4.1              |
| 4.3 | Análisis de sentimiento         | IA           | 🟡 Intermedio | 3-4h   | 4.1, BACKLOG S10 |
| 4.4 | Generación de contenido         | IA           | 🟡 Intermedio | 3-4h   | 4.1              |
| 5.1 | Redis caché distribuido         | Servicios    | 🟡 Intermedio | 4-6h   | BACKLOG S9       |
| 5.2 | Sesiones distribuidas           | Servicios    | 🟡 Intermedio | 2-3h   | 5.1              |
| 5.3 | Problem Details RFC 9457        | Servicios    | 🟡 Intermedio | 3-4h   | BACKLOG S4       |
| 6.1 | GraalVM Native Image            | GraalVM      | 🔴 Experto    | 8-12h  | BACKLOG completo |
| 6.2 | Benchmark JVM vs Native         | GraalVM      | 🟠 Avanzado   | 4-6h   | 6.1              |

**Totales:**

- 🟡 Intermedio: 11 ejercicios
- 🟠 Avanzado: 7 ejercicios
- 🔴 Experto: 2 ejercicios
- **Tiempo total estimado: 95-130 horas (~8-12 semanas a ritmo parcial)**

---

## 🔗 Grafo de Dependencias

```
BACKLOG S5 (Security) ──────────────────┐
BACKLOG S7 (Rooms) ─────────┐           │
BACKLOG S9 (Reservations) ──┤           │
BACKLOG S10 (Payments) ─────┤           │
                            │           │
                            ▼           ▼
                     ┌─── 2.1 GraphQL  1.3 Rate Limiting
                     │      │
                     │      ▼
                     │   2.2 Subscriptions
                     │   2.3 gRPC
                     │
                     ├─── 1.1 Circuit Breaker
                     │      │
                     │      ▼
                     │   1.2 Retry
                     │      │
                     │      ▼
                     │   1.4 Fallback cascada ◄── 1.3
                     │
                     ├─── 3.1 Virtual Threads
                     │      │
                     │      ▼
                     │   3.2 Coroutines
                     │      │
                     │      ▼
                     │   3.3 WebFlux
                     │
                     ├─── 3.4 Optimistic Locking
                     │
                     ├─── 4.1 Chatbot Spring AI
                     │      │
                     │      ├──▶ 4.2 RAG
                     │      ├──▶ 4.3 Sentimiento
                     │      └──▶ 4.4 Generación
                     │
                     ├─── 5.1 Redis Caché
                     │      │
                     │      └──▶ 5.2 Sesiones
                     │
                     ├─── 5.3 Problem Details
                     │
                     └─── 6.1 GraalVM Native ◄── (todo lo anterior)
                            │
                            └──▶ 6.2 Benchmark
```

---

## 📚 Recursos Generales

- [Spring Boot 4.x Reference](https://docs.spring.io/spring-boot/reference/)
- [Kotlin Reference](https://kotlinlang.org/docs/reference/)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring for GraphQL](https://docs.spring.io/spring-graphql/reference/)
- [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/)
- [Retos 2026 del Curso](docs/course/week-05/03-retos.md) — Los 52 retos semanales originales

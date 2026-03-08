# Retos 2026: Gran Hotel Pokémon API

> Los ejemplos de código están en español para facilitar la comprensión del flujo de trabajo. Sin embargo, lo recomendable es que el código esté en inglés, la documentación y los comentarios en español si es necesario (en caso de ser necesario distribuir el código ente multiples equipos, el idioma recomendable sería inglés).

## **Fase 1: Fundamentos y Configuración (Semanas 1-4)**

### **Semana 1: Inicialización del Proyecto**

- **Caso de uso**: Configurar un proyecto SpringBoot + Kotlin con estructura modular siguiendo Clean Architecture. 
- **Crítica**: No estructures por capas técnicas (controllers, services, repositories). Eso es legacy. Estructura por Feature o Componente.
- **Implementación recomendada**: Configura el proyecto como un Modular Monolith (usando paquetes o módulos de Gradle).

```text
# Estructura de directorios básica

com.app.hotelpokemon
├── shared (Kernel compartido, Value Objects genéricos)
├── catalog (Módulo: Habitaciones y Tours)
│   ├── domain (Entidades puras, puertos)
│   ├── application (Casos de uso)
│   ├── infrastructure (Controladores REST, Repositorios JPA)
└── trainers (Módulo: Entrenadores y Decks)
```

**Stack recomendado**:

```yaml
- SpringBoot 3.x + Kotlin 1.9+
- Gradle Kotlin DSL
- Estructura de paquetes por capas
- Configuración de propiedades multi-entorno
- Dockerfile básico
```

### **Semana 2: Configuración de Base de Datos**

- **Caso de uso**: Implementar conexión a PostgreSQL con Flyway para migraciones.
- **Implementación recomendada**: Usa Testcontainers desde el día 1 para pruebas de integración reales, no H2.

```sql
-- Ejemplo de migración V1
CREATE TABLE entrenadores (
    id UUID PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nivel_entrenador INT DEFAULT 1,
    -- Normaliza insignias a otra tabla o usa JSONB si son datos planos
    metadata_insignias JSONB DEFAULT '{}',
    fecha_registro TIMESTAMP
);
```

### **Semana 3: Entidades vs Modelos de Dominio (Anti-Patrón Alert)**
 
- **Caso de uso**: Modelar las entidades principales usando Data Classes y JPA.
- **Crítica**: NUNCA uses data class de Kotlin para @Entity de JPA.
  - Rompe el Lazy Loading (Hibernate necesita proxies). 
  - toString() circular puede causar StackOverflow. 
  - equals/hashCode generados por data class incluyen campos mutables.
- **Implementación recomendada**: Usa clases normales o el plugin jpa (all-open) y define la igualdad solo por ID.

```kotlin
@Entity
@Table(name = "entrenadores")
data class Entrenador(
    @Id val id: UUID = UUID.randomUUID(),
    val nombre: String,
    val email: String,
    @Enumerated(EnumType.STRING)
    var rango: RangoEntrenador,
    val pokeballsDisponibles: Int
) {
    // Igualdad referencial estricta para JPA
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EntrenadorEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
```

### **Semana 4: Repositorios y Puertos**

- **Caso de uso**: Implementar repositorios JPA con métodos personalizados.
- **Crítica**: JpaRepository es infraestructura. Si lo usas directamente en tu lógica de negocio, te acoplas al framework.
- **Implementación recomendada**: Aplica Inversión de Dependencia.
  1. Dominio: Define `interface EntrenadorRepository { fun guardar(e: Entrenador) }` 
  2. Infraestructura: Implementa esa interfaz usando JpaRepository.

```kotlin
@Repository
interface EntrenadorRepository : JpaRepository<Entrenador, UUID> {
    fun findByRango(rango: RangoEntrenador): List<Entrenador>
    fun countByFechaRegistroAfter(fecha: LocalDate): Long
}
```

## **Fase 2: CRUD Básico (Semanas 5-8)**

### **Semana 5: Habitaciones del Hotel**

- **Caso de uso**: CRUD para tipos de habitaciones con características especiales.
- **Caso de uso**: Las habitaciones no son solo datos, tienen comportamiento (bonificaciones).
- **Implementación recomendada**: Usa Strategy Pattern implícito en Enums o Sealed Classes para evitar if/else masivos.

```kotlin
// Cada tipo tiene bonificaciones distintas para encontrar Pokémon
enum class TipoHabitacion(val multiplicadorSpawn: Double) {
    ESTANDAR(1.0),
    PREMIUM(1.5),
    SUITE_LEGENDARIA(3.0); // La lógica vive cerca del dato

    fun calcularProbabilidad(base: Double): Double = base * multiplicadorSpawn
}
```

### **Semana 6: Servicios de Tours**

- **Caso de uso**: Sistema de tours con diferentes rutas y rarezas de Pokémon.
- **Caso de uso**: Un Tour garantiza consistencia. No puedes tener un Tour sin ubicaciones.
- **Implementación recomendada**: El constructor o factory method debe validar invariantes.

```kotlin
data class Tour(
    val id: UUID,
    val nombre: String,
    val dificultad: Dificultad,
    val ubicaciones: List<Ubicacion>,
    val pokemonesDisponibles: List<PokemonRareza>
)
```

### **Semana 7: Servicios de Dominio (Use Cases)**

- **Caso de uso**: Reservar habitaciones con validación de fechas y disponibilidad.
- **Crítica**: reservarHabitacion no es una función suelta, es un Caso de Uso transaccional.
- **Implementación recomendada**: Maneja errores con Result o Either (Arrow Library) en lugar de Exceptions para flujo de control.

```kotlin
fun reservarHabitacion(
    entrenadorId: UUID,
    habitacionId: UUID,
    fechaInicio: LocalDate,
    fechaFin: LocalDate
): Reserva

@Service
class ReservarHabitacionUseCase(
    private val repo: ReservaRepository,
    private val disponibilidad: DisponibilidadService
) {
    @Transactional
    fun ejecutar(comando: CrearReservaCommand): Result<ReservaId> {
        if (!disponibilidad.existe(comando.habitacionId, comando.fechas)) {
            return Result.failure(HabitacionOcupadaException())
        }
        // Lógica de creación...
    }
}
```

### **Semana 8: Máquinas de Estado Check-in/Check-out**

- **Caso de uso**: Proceso completo de hospedaje con validación de estado. El ciclo de vida de una reserva (Pendiente -> CheckIn -> CheckOut -> Cancelada) es delicado.
- **Implementación recomendada**: No cambies flags booleanos (isCheckedIn = true). Usa un patrón de estado explícito para evitar transiciones ilegales (ej: hacer checkout sin checkin).

```kotlin
interface CheckinService {
    fun realizarCheckin(reservaId: UUID): Checkin
    fun generarFactura(checkinId: UUID): Factura
}
```

## **Fase 3: Lógica de Negocio (Semanas 9-12)**

### **Semana 9: Encontrar Pokémon en Tours (Strategy Pattern)**

- **Caso de uso**: Algoritmo probabilístico para encontrar Pokémon basado en:
  - Rango del entrenador
  - Tipo de tour
  - Bonificación de habitación
  - Items especiales
- **Crítica**: No hagas un "Dios Método" que calcule todo.
- **Implementación recomendada**: Crea un motor de reglas inyectable.

```kotlin
interface CalculadoraProbabilidad {
    fun calcular(contexto: ContextoEncuentro): Double
}

@Component
class ProbabilidadClimaStrategy : CalculadoraProbabilidad { ... }

@Component
class ProbabilidadRangoStrategy : CalculadoraProbabilidad { ... }

// El servicio orquestador suma o promedia las estrategias
```

### **Semana 10: Sistema de Deck de Cartas (Mappers y Factory)**

- **Caso de uso**: Convertir Pokémon encontrados en cartas coleccionables. Convertir un "Pokemon Salvaje" (Entidad efímera) en una "Carta" (Entidad persistente propiedad del usuario).
- **Implementación recomendada**: Esto es un cambio de contexto (Bounded Context). Usa un Factory explícito.

```kotlin
data class CartaPokemon(
    val id: UUID,
    val pokemon: Pokemon,
    val nivel: Int,
    val ataques: List<Ataque>,
    val rareza: RarezaCarta,
    val entrenadorId: UUID
)

object CartaFactory {
    fun crearDesdeEncuentro(pokemon: Pokemon, entrenador: Entrenador): CartaPokemon {
        val rarezaCalculada = calcularRareza(pokemon.stats)
        return CartaPokemon(
            id = UUID.randomUUID(),
            pokemonId = pokemon.id,
            duenoId = entrenador.id,
            rareza = rarezaCalculada,
            fechaObtencion = Instant.now()
        )
    }
}
```

### **Semana 11: Batallas en el Coliseo (State Pattern & Immutability)**

- **Caso de uso**: Sistema de batallas por turnos entre decks. 
- **Crítica**: El diseño original puede mutar el estado directamente (void o Unit). En un sistema de turnos, se necesita trazabilidad. Si hay un error en el turno 5, debes poder reproducirlo. Implementa un diseño basado en Inmutabilidad y Eventos. Una batalla es una máquina de estados.
- **Implementación recomendada**: Usa Sealed Classes para modelar el resultado de un turno de forma exhaustiva.

```kotlin
// Dominio: El resultado no es solo "daño", es un evento complejo
sealed class ResultadoTurno {
    data class Exito(val danio: Int, val estadoAlterado: EstadoPokemon?, val esCritico: Boolean) : ResultadoTurno()
    data class Fallo(val razon: String) : ResultadoTurno()
    object FinBatalla : ResultadoTurno()
}

// Servicio: Transiciones de estado puras
@Service
class BatallaService(private val repo: BatallaRepository) {

    @Transactional
    fun ejecutarTurno(batallaId: UUID, comando: EjecutarMovimientoCmd): ResultadoTurno {
        val batalla = repo.findById(batallaId)

        // La lógica de negocio vive en el Agregado (Batalla), no en el servicio
        val resultado = batalla.calcularTurno(comando.movimientoId)

        repo.save(batalla) // Guarda el nuevo estado
        return resultado
    }
}
```

### **Semana 12: Sistema de Logros (Event-Driven Architecture)**

- **Caso de uso**: Logros desbloqueables por actividades en el hotel. 
- **Crítica**: NUNCA llames al LogroService desde el BatallaService. Eso resulta en acoplamiento fuerte. Si mañana añades logros por "Caminar 10km", ¿vas a modificar el servicio de caminatas? 
- **Implementación recomendada**: Usar Spring Events. El servicio de batallas lanza un evento "BatallaGanada", y el sistema de logros escucha sin que la batalla sepa que existen los logros.

```kotlin
// 1. El Evento (Agnóstico de quién lo escucha)
data class BatallaTerminadaEvent(val ganadorId: UUID, val perdedorId: UUID)

// 2. El Publicador (En BatallaService)
applicationEventPublisher.publishEvent(BatallaTerminadaEvent(...))

// 3. El Listener (En el módulo de Logros)
@Component
class LogroListener(private val logroService: LogroService) {

    @EventListener
    @Async // Hazlo asíncrono para no bloquear la respuesta al usuario
    fun alTerminarBatalla(event: BatallaTerminadaEvent) {
        logroService.verificarRachaVictorias(event.ganadorId)
    }
}
```

## **Fase 4: APIs REST Avanzadas (Semanas 13-16)**

### **Semana 13: DTOs y Mappers (Kotlin Idiomatic vs MapStruct)**

- **Caso de uso**: Implementar Extension Functions para conversión entre entidades y DTOs.
- **Crítica**: MapStruct es excelente en Java, pero en Kotlin es verboso y requiere configuración extra (kapt/ksp). 
- **Implementación recomendada**: En Kotlin, las Extension Functions son más limpias, legibles y fáciles de testear para mapeos 1:1. Es recomendable usarlas a menos que el mapeo sea extremadamente complejo.

```kotlin
// Archivo: EntrenadorMappers.kt
fun Entrenador.toResponse() = EntrenadorResponse(
    id = this.id,
    nombre = this.nombre,
    // Lógica de presentación simple aquí
    titulo = "${this.rango} ${this.nombre}"
)

fun CrearEntrenadorRequest.toDomain() = Entrenador(
    nombre = this.nombre,
    email = this.email
)

// Uso en Controller
@GetMapping("/{id}")
fun get(@PathVariable id: UUID) = service.buscar(id).toResponse()
```

### **Semana 14: Paginación y Filtros**

- **Caso de uso**: API's con paginación, sorting y filtros dinámicos. 
- **Crítica**: No abuses de múltiples parámetros en el endpoint (page, size, sort, filtro1, filtro2...). Para endpoints sencillos usa `Pageable`, para mayor control usa los parámetros opcionales.

```kotlin
@GetMapping("/entrenadores")
fun listarEntrenadores(
    @RequestParam page: Int = 0,
    @RequestParam size: Int = 20,
    @RequestParam sort: String = "nombre",
    @RequestParam rango: RangoEntrenador? = null
): Page<EntrenadorDTO>
```

### **Semana 15: Búsqueda Avanzada**

- **Caso de uso**: Search API con múltiples criterios usando Specifications.
- **Crítica**: Los parámetros opcionales masivos en el controlador (nombre?, fecha?, tipo?) crean un "telescoping constructor" feo. 
- **Mejora**: Usa un objeto Criteria y especificaciones de JPA (o mejor aún, Blaze-Persistence si quieres tipos seguros).

```kotlin
// 1. DTO de Criterios (Captura todo lo que viene del query param)
data class BusquedaHabitacionCriteria(
    val tipo: TipoHabitacion?,
    val precioRango: ClosedRange<BigDecimal>?
)

// 2. Repository con Specification (Spring Data JPA)
interface HabitacionRepository : JpaRepository<Habitacion, UUID>, JpaSpecificationExecutor<Habitacion>

// 3. Specification Dinámica
fun buildSpec(criteria: BusquedaHabitacionCriteria): Specification<Habitacion> {
    return Specification { root, _, cb ->
        val predicates = mutableListOf<Predicate>()
        criteria.tipo?.let { predicates.add(cb.equal(root.get<TipoHabitacion>("tipo"), it)) }
        // ... más reglas
        cb.and(*predicates.toTypedArray())
    }
}
```

### **Semana 16: HATEOAS (Realidad vs Teoría)**

- **Caso de uso**: Implementar HATEOAS en todos los endpoints.
- **Crítica**: HATEOAS agrega complejidad y latencia. La mayoría de frontends modernos (React/Next.js) lo ignoran porque ya conocen las rutas. 
- **Implementación recomendada**: Úsalo solo si realmente tienes clientes desconocidos. Si lo haces, no ensucies el controlador. Usa RepresentationModelAssembler.

```kotlin
@GetMapping("/reservas/{id}")
fun getReserva(@PathVariable id: UUID): EntityModel<ReservaDTO> {
    val reserva = reservaService.findById(id)
    return EntityModel.of(reserva,
        linkTo(methodOn(ReservaController::class.java).getReserva(id)).withSelfRel(),
        linkTo(methodOn(ReservaController::class.java).cancelarReserva(id)).withRel("cancelar")
    )
}
```

## **Fase 5: Validación y Manejo de Errores (Semanas 17-20)**

### **Semana 17: Validación con Bean Validation (Input vs Domain)**

- **Caso de uso**: Validar todos los DTOs de entrada.
- **Crítica**: @NotNull en el DTO está bien para sintaxis. Pero la validación semántica ("La fecha fin no puede ser antes de fecha inicio") pertenece al Dominio, no al Controller. 
- **Implementación recomendada**: Divide y vencerás.
  1. JSR-380 (Annotations) para formato (Email, NotNull). 
  2. Init Blocks / Domain Services para reglas de negocio.

```kotlin
data class ReservaRequest(
    @field:NotNull
    val entrenadorId: UUID,
    
    @field:Future
    @field:NotNull
    val fechaInicio: LocalDate,
    
    @field:Min(1)
    @field:Max(30)
    val noches: Int
)

// Dominio: Validación de Lógica
class Reserva(...) {
    init {
        require(fechaFin.isAfter(fechaInicio)) { "La fecha final debe ser posterior a la inicial" }
    }
}
```

### **Semana 18: Manejo Global de Excepciones (RFC 7807)**

- **Caso de uso**: @ControllerAdvice con excepciones personalizadas.
- **Crítica**: No inventes tu propio JSON de error (`{ "cod": 500, "msg": "error" }`). Es un antipatrón. 
- **Implementación recomendada**: Spring Boot soporta nativamente Problem Details. Úsalo para estandarizar errores API.

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(HabitacionOcupadaException::class)
    fun handleConflict(ex: HabitacionOcupadaException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "Conflicto")
        problem.title = "Recurso no disponible"
        problem.setProperty("habitacion_id", ex.habitacionId) // Metadata extra útil
        return problem
    }
}
```

### **Semana 19: Transacciones y Concurrencia (Optimistic Locking)**

- **Caso de uso**: Manejar reservas simultáneas con @Transactional y optimistic locking.
- **Crítica**: `@Transactional` está bien, pero no es mágico. Si dos usuarios reservan la misma habitación al milisegundo exacto, tendrás overselling. 
- **Implementación recomendada**: El bloqueo optimista requiere manejo de reintentos (Retries) o fallo rápido.
  1. Agrega @Version en tu entidad. 
  2. Captura OptimisticLockingFailureException.

Kotlin

```kotlin
@Entity
class Habitacion(...) {
    @Version
    var version: Long = 0
}

// En el servicio, o usas @Retryable o devuelves error al usuario para que intente de nuevo
@Retryable(value = [OptimisticLockingFailureException::class], maxAttempts = 2)
@Transactional
fun reservar(...) { ... }
```

### **Semana 20: Circuit Breaker y Retry con Resilience4j (No Hystrix)**

- **Caso de uso**: Implementar resiliencia en llamadas externas (ej: servicio de pagos).
- **Crítica**: Netflix Hystrix está deprecado. No lo uses en nuevos proyectos.
- **Implementación recomendada**: Usa Resilience4j con Spring Boot AOP. Configura timeouts agresivos. Es mejor fallar rápido que colgar el hilo.

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      pagosExternos:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
```

```kotlin
@CircuitBreaker(name = "pagosExternos", fallbackMethod = "pagoFallback")
fun procesarPago(reservaId: UUID): PagoStatus {
    return pasarelaPagoClient.cobrar(...)
}

// El fallback debe tener la misma firma + la excepción
fun pagoFallback(reservaId: UUID, ex: Exception): PagoStatus {
    log.error("Pasarela caída. Guardando pago como PENDIENTE para reintento batch.")
    return PagoStatus.PENDIENTE_REINTENTO
}
```

## **Fase 6: Seguridad (Semanas 21-24)**

### **Semana 21: Autenticación JWT (Stateless JWT)**

- **Caso de uso**: Sistema de login para entrenadores.
- **Crítica**: Es importante entender que devolver el token en el cuerpo del JSON expone al cliente a ataques XSS si lo guardan en localStorage. 
- **Implementación recomendada**: Implementa Spring Security como un "cross-cutting concern". Considera usar HttpOnly Cookies para el Refresh Token si la seguridad es crítica. Además, configura un SecurityFilterChain declarativo.

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtFilter: JwtAuthFilter) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() } // Stateless no necesita CSRF (generalmente)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**", "/public/**").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
```

### **Semana 22: Autorización con Roles (Meta-Annotations)**

- **Caso de uso**: Control de acceso basado en roles (ENTRENADOR, GIMNASIO_LIDER, ADMIN).
- **Crítica**: Escribir `@PreAuthorize("hasRole('ADMIN') ...")` por todo el código es sucio y difícil de mantener (Magic Strings). Sin embargo, en la mayoría de los casos es suficiente. 
- **Implementación recomendada**: Crea tus propias anotaciones de seguridad que describan la intención del negocio, no la implementación técnica.

```kotlin
// Básico
@PreAuthorize("hasRole('ADMIN') or #entrenadorId == authentication.principal.id")
@PutMapping("/entrenadores/{entrenadorId}/deck")
fun actualizarDeck(@PathVariable entrenadorId: UUID, @RequestBody deck: DeckDTO)

// Más robusto y reutilizable
// Definición de la anotación
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('GIMNASIO_LIDER') or @securityService.esDuenoDelDeck(#entrenadorId)")
annotation class RequiereLiderODueno

// Uso limpio en el Controller
@RequiereLiderODueno
@PutMapping("/entrenadores/{entrenadorId}/deck")
fun actualizarDeck(@PathVariable entrenadorId: UUID, ...)
```

### **Semana 23: OAuth2 con Proveedores Pokémon (Mapeo de Identidad)**

- **Caso de uso**: Login con Pokémon Trainer Club y Google. Ojo: el reto no es configurar el YAML, es vincular al usuario de Google/Pokémon con tu entidad Entrenador interna
- **Implementación recomendada**: Implementa un OAuth2UserService personalizado.

```properties
security:
  oauth2:
    client:
      registration:
        pokemon-trainer-club:
          client-id: ${PTC_CLIENT_ID}
          client-secret: ${PTC_CLIENT_SECRET}
```

```kotlin
@Service
class CustomOAuth2UserService(private val repo: EntrenadorRepository) : DefaultOAuth2UserService() {
    
    @Transactional
    override fun loadUser(request: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(request)
        val email = oAuth2User.attributes["email"] as String
        
        // Busca o crea el entrenador al vuelo (JIT Provisioning)
        val entrenador = repo.findByEmail(email) 
            ?: repo.save(Entrenador.crearDesdeOAuth(email, oAuth2User.attributes))
            
        return PrincipalEntrenador(entrenador, oAuth2User.attributes)
    }
}
```

### **Semana 24: Auditoría y Logs Sensibles**

- **Caso de uso**: Trackear todas las acciones importantes.
- **Crítica**: @CreatedBy val creadoPor: String en la mayoría de los casos puede ser suficiente, pero hay que reconocer que es débil. ¿Quién es "Juan"? ¿Qué ID tiene? Si cambia de nombre, pierdes el rastro. 
- **Implementación recomendada**: Audita el ID del usuario (UUID) y usa AuditorAware.

```kotlin
// Básico
@EntityListeners(AuditingEntityListener::class)
data class Reserva(
    // ...
    @CreatedBy
    val creadoPor: String,
    
    @LastModifiedDate
    val ultimaModificacion: Instant
)

// Avanzado
@Component
class SpringSecurityAuditorAware : AuditorAware<UUID> {
    override fun getCurrentAuditor(): Optional<UUID> {
        val auth = SecurityContextHolder.getContext().authentication
        // Asume que tu Principal tiene el ID
        return Optional.ofNullable((auth?.principal as? PrincipalEntrenador)?.id)
    }
}

@EntityListeners(AuditingEntityListener::class)
class Reserva {
    @CreatedBy var creadorId: UUID? = null // Referencia inmutable
    @LastModifiedDate var fechaModificacion: Instant? = null
}
```

## **Fase 7: Testing (Semanas 25-28)**

### **Semana 25: Unit Tests con JUnit 5**

- **Caso de uso**: Testear servicios de negocio con MockK.
- **Crítica**: No mockees todo. Si mockeas los Value Objects o las Entidades, estás testeando fantasía. 
- **Implementación recomendada**: Usa "Sociable Unit Tests". Testea el Servicio pero usa las Entidades reales. Solo mockea los puertos externos (Repositorios, APIs externas).

```kotlin
@Test
fun `deberia encontrar pokemon en tour`() {
    // Given
    every { tourService.obtenerTour(any()) } returns tour
    every { probabilidadService.calcular(any(), any()) } returns 0.8
    
    // When
    val resultado = exploracionService.explorar(tourId, entrenadorId)
    
    // Then
    assertThat(resultado.pokemonesEncontrados).isNotEmpty
}
```

### **Semana 26: Integration Tests**

- **Caso de uso**: Testear repositorios con Testcontainers.
- **Crítica**: Configurar `PostgreSQLContainer` manualmente en cada test es mucho trabajo y complica las pruebas. 
- **Implementación recomendada**: Usa `@ServiceConnection` (Spring Boot 3+) para inyección automática de credenciales.

```kotlin
@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class BaseIntegrationTest {

    companion object {
        @Container
        @ServiceConnection // ¡Magia! Configura spring.datasource.url automáticamente
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }
}
```

### **Semana 27: Controller Tests (Testing de Contrato)**

- **Caso de uso**: Testear endpoints REST con MockMvc.
- **Implementación recomendada**: MockMvc es un mock del Servlet. WebTestClient invoca el endpoint real HTTP. Es más fidedigno.

```kotlin
// Básico usando MockMvc
@WebMvcTest(ReservaController::class)
class ReservaControllerTest {
    @Test
    fun `POST reservas deberia retornar 201`() {
        mockMvc.perform(post("/reservas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(reservaRequestJson))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
    }
}

// Avanzado usando WebTestClient
@Test
fun `crear reserva retorna 201`() {
    webTestClient.post().uri("/reservas")
        .bodyValue(reservaDto)
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("$.id").isNotEmpty
}
```

### **Semana 28: Performance Tests (Gatling en Kotlin)**

- **Caso de uso**: Test de carga para reservas simultáneas con Gatling.
- **Implementación recomendada**: Gatling ahora soporta DSL en Kotlin oficial. Úsalo para mantener un solo lenguaje en el stack.

```kotlin
// Simulation.kt
class ReservaSimulation : Simulation() {
    val httpProtocol = http.baseUrl("http://localhost:8080")
    
    val scn = scenario("Reserva Masiva")
        .exec(http("Post Reserva")
            .post("/reservas")
            .body(StringBody("""{ "entrenadorId": "..." }"""))
            .check(status().`is`(201))
        )

    init {
        setUp(scn.injectOpen(rampUsers(500).during(30.seconds))).protocols(httpProtocol)
    }
}
```

## **Fase 8: Patrones Avanzados (Semanas 29-32)**

### **Semana 29: CQRS para Reportes (Command Query Responsibility Segregation)**

- **Caso de uso**: Separar escritura (reservas) de lectura (reportes).
- **Implementación recomendada**: No es solo separar clases. Es separar Modelos. Las lecturas (Reportes de Dashboard) suelen ser agregaciones complejas que matan a la base de datos transaccional (3NF).
  1. Write Model (Command): Tu entidad JPA normalizada, optimizada para integridad (ACID). 
  2. Read Model (Query): Una Vista Materializada en Postgres o un índice en Elasticsearch/Mongo, optimizado para lectura rápida (desnormalizado).

```kotlin
// Command: Crea la reserva y emite evento
fun handle(cmd: CrearReservaCmd) {
    val reserva = repo.save(cmd.toEntity())
    eventBus.publish(ReservaCreadaEvent(reserva))
}

// Query Handler (Escucha evento y actualiza la vista de reporte)
@EventListener
fun on(event: ReservaCreadaEvent) {
    val reporte = ReporteOcupacion(fecha = event.fecha, +1 ocupacion)
    reporteRepo.upsert(reporte) // Tabla plana para queries rapidísimos
}
```

### **Semana 30: Event Sourcing (Nuclear event-driven architecture)**

- **Caso de uso**: Trackear todos los eventos de una reserva.
- **Crítica**: Implementar Event Sourcing "a mano" es el error más caro que puedes cometer. Implica snapshotting, versionado de eventos, replay, projections... 
- **Implementación recomendada**: Si realmente necesitas auditar cada cambio de estado para reconstruir el pasado (ej: partidas legales de torneos), usa un framework como Axon Framework o EventStoreDB. No lo hagas "vanilla" en Spring Boot a menos que seas experto.
- **Alternativa Pragmática (Event-Driven Audit)**: En lugar de Sourcing (donde el evento es la fuente de verdad), usa Event Logging. Guardas el estado actual en SQL (como siempre) y guardas un log de eventos en una tabla domain_events solo para auditoría/histórico. Es 10 veces más barato de mantener.

```kotlin
// En lugar de reconstruir el estado desde eventos (Sourcing),
// simplemente guardamos qué pasó.
@Service
class HistorialService {
    @TransactionalEventListener
    fun registrarEvento(event: DomainEvent) {
        historialRepo.save(
            HistorialEntry(
                tipo = event.javaClass.simpleName,
                payload = jsonMapper.writeValueAsString(event),
                timestamp = Instant.now()
            )
        )
    }
}
```

### **Semana 31: Saga Pattern**

- **Caso de uso**: Orquestar proceso de reserva (validación → pago → confirmación).

```kotlin
interface ReservaSaga {
    fun iniciar(reservaRequest: ReservaRequest): SagaInstance
    fun handlePagoExitoso(pagoId: UUID)
    fun handlePagoFallido(pagoId: UUID, razon: String)
}
```

### **Semana 32: Cache Estratégica**

- **Caso de uso**: Cachear datos frecuentes (catálogo de habitaciones, tours).

```kotlin
@Cacheable(value = ["habitaciones"], key = "#tipo")
fun findHabitacionesDisponibles(tipo: TipoHabitacion): List<HabitacionDTO> {
    // Lógica costosa
}
```

### **Semana 31-32: Observabilidad (Metrics & Tracing)**

No puedes gestionar lo que no mides. Para orquestar procesos complejos, necesitas un sistema de observabilidad, incluso en el manejo de la concurrencia, el estado de las transacciones y el rendimiento. Es por eso que en este punto es necesario empezar a pensar en uso de estas herramientas.

- Micrometer + Prometheus: Mide latencia de reservas, tasa de éxito de capturas, memoria JVM. 
- OpenTelemetry + Jaeger: Traza la petición desde que entra al Controller hasta que impacta la DB.
- Sentry: Para monitorear errores en producción con contexto completo.

## **Fase 9: Microservicios (Semanas 33-36)**

> Advertencia: Antes de romper el monolito, pregúntate: "¿Tengo un problema de escala organizacional o técnica?". Si la respuesta es "no", quédate en el monolito modular. Si es "sí" (para fines educativos), hazlo bien para evitar la falacia de la computación distribuida.

### **Semana 33: Servicio de Notificaciones (Transactional Outbox Pattern)**

- **Caso de uso**: MS separado para enviar emails/notificaciones.
- **Crítica**: Si guardas la reserva en DB y falla el envío a Kafka (o viceversa), tienes inconsistencia de datos. 
- **Implementación recomendada**: Implementa el patrón Outbox. Guarda el evento en la misma transacción de base de datos en una tabla outbox, y usa un proceso (Debezium o Polling) para enviarlo a Kafka.

```kotlin
@Service
class ReservaService(
    private val reservaRepo: ReservaRepository,
    private val outboxRepo: OutboxRepository, // Tabla intermedia
    private val eventMapper: EventMapper
) {
    @Transactional
    fun crearReserva(cmd: CrearReservaCmd) {
        // 1. Guardar estado de negocio
        val reserva = reservaRepo.save(cmd.toEntity())

        // 2. Guardar intención de notificar (misma transacción ACID)
        val evento = eventMapper.toEvent(reserva)
        outboxRepo.save(OutboxMessage(
            topic = "reservas.creadas",
            payload = toJson(evento),
            status = Status.PENDING
        ))
    }
    // Un proceso background leerá la tabla outbox y enviará a Kafka
}
```

### **Semana 34: API Gateway (Seguridad y Rate Limiting)**

- **Caso de uso**: Spring Cloud Gateway para rutear requests.
- **Crítica**: Un Gateway sin Rate Limiting es una puerta abierta a ataques DDoS. Además, el Gateway debe encargarse de la traducción de tokens (JWT Relay). 
- **Implementación recomendada**: Configura Resilience4j o Redis RateLimiter en el Gateway.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: reservas-service
          uri: lb://reservas-service
          predicates:
            - Path=/api/reservas/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10 # 10 req/seg
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}" # Limitar por usuario, no por IP global
```

### **Semana 35: Service Discovery (Kubernetes vs Eureka)**

- **Caso de uso**: Eureka para descubrimiento de servicios.
- **Crítica**: En entornos modernos (Kubernetes), el Service Discovery es nativo (DNS K8s). 
- **Implementación recomendada**: Si despliegas en K8s, elimina Eureka. Si estás aprendiendo Spring Cloud "vanilla", úsalo, pero entiende que es redundante en la nube moderna.

```kotlin
@SpringBootApplication
@EnableEurekaClient
class HabitacionesServiceApplication
```

- **Enfoque Práctico (K8s Native)**: Simplemente usa el nombre del servicio en el application.yaml del cliente.

```yaml
# En K8s, el DNS resuelve "reservas-service" a la IP interna
feign:
  client:
    config:
      default:
        url: http://reservas-service:8080
```

### **Semana 36: Comunicación Asíncrona (Schema Registry)**

- **Caso de uso**: Eventos de dominio con Kafka.
- **Crítica**: Enviar JSON crudo por Kafka es peligroso. Si el consumidor espera un campo que el productor borró, el sistema explota. Usa Avro y un Schema Registry. Esto garantiza contratos estrictos entre microservicios (evolución de esquemas compatible).
- **Implementación recomendada**: Define el evento en un archivo `.avsc` (Avro) y genera el código Kotlin automáticamente.

```kotlin
@Component
class ReservaEventHandler {
    @KafkaListener(topics = ["reservas-creadas"])
    fun handleReservaCreada(event: ReservaCreadaEvent) {
        // Actualizar caché
        // Notificar a otros servicios
        val evento = ReservaCreadaEvent.newBuilder()
            .setReservaId(id.toString())
            .setFecha(Instant.now().toEpochMilli())
            .build()

        kafkaTemplate.send("reservas-topic", evento)
    }
}
```

## **Fase 10: Documentación (Semanas 37-40)**

### **Semana 37: OpenAPI 3 + Swagger (Documentación Viva)**

- **Caso de uso**: Documentación interactiva de APIs.
- **Crítica**: No es necesario que configures Swagger manualmente si puedes inferirlo. Pero ojo: no expongas tus Entidades JPA en la documentación. 
- **Implementación recomendada**: Usa springdoc-openapi y anota tus DTOs para que los ejemplos en la UI sean útiles.

```kotlin
@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(Info().title("Gran Hotel Pokémon API"))
            .addSecurityItem(SecurityRequirement().addList("JWT"))
    }
}

data class ReservaRequest(
    @Schema(description = "ID del entrenador", example = "a0eebc99-9c0b...", required = true)
    val entrenadorId: UUID,

    @Schema(description = "Fecha formato ISO", example = "2025-12-25")
    val fechaInicio: LocalDate
)

@Operation(summary = "Crear nueva reserva", description = "Valida disponibilidad y bloquea recursos")
@ApiResponses(value = [
    ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
    ApiResponse(responseCode = "409", description = "Habitación no disponible en esas fechas")
])
@PostMapping
fun crear(...) { ... }
```

### **Semana 38: AsyncAPI para Eventos (Springwolf)**

- **Caso de uso**: Documentar eventos Kafka.
- **Crítica**: Mantener un YAML de AsyncAPI a mano es imposible; siempre estará desactualizado. Para esos casos Usa Springwolf. Es como Swagger, pero para Kafka/RabbitMQ. Genera la documentación escaneando tus `@KafkaListener`.
- **Implementación recomendada**: Agrega la dependencia springwolf-kafka y anota tus listeners.

```yaml
asyncapi: 2.0.0
info:
  title: Gran Hotel Pokémon Events
channels:
  reservas-creadas:
    subscribe:
      message:
        $ref: '#/components/messages/ReservaCreada'
```

- Springwolf

```kotlin
@AsyncListener(
    operation = AsyncOperation(
        channelName = "reservas-creadas",
        description = "Escucha eventos de nuevas reservas para actualizar dashboard"
    )
)
@KafkaListener(topics = ["reservas-creadas"])
fun listen(event: ReservaCreadaPayload) { ... }
```

### **Semana 39: API Versioning (Pragmatismo)**

- **Caso de uso**: Versionar APIs con diferentes estrategias.
- **Crítica**: Elige URL Versioning (/v1/, /v2/) para APIs públicas (es explícito y cacheable). Usa Header Versioning solo si tienes control total de los clientes (apps móviles internas).
- **Implementación recomendada**: URL Path Strategy - Estructura tus paquetes por versión si los cambios son drásticos.

```kotlin
// Header Versioning
@GetMapping(value = ["/entrenadores"], headers = ["API-Version=2"])
fun listarEntrenadoresV2(): List<EntrenadorDTOV2>

// URL Versioning
// Package: com.hotel.v1.controller
@RequestMapping("/api/v1/tours")
class TourControllerV1 { ... }

// Package: com.hotel.v2.controller
@RequestMapping("/api/v2/tours")
class TourControllerV2 { ... }
```

### **Semana 40: Postman Collection (Newman)**

- **Caso de uso**: Crear y automatizar colección completa de requests.
- **Crítica**: Integra Newman (CLI de Postman) en tu pipeline de CI/CD (GitHub Actions / Jenkins).
- **Implementación recomendada**: Script de CI (.github/workflows/api-test.yml):

```json
{
  "info": {
    "name": "Gran Hotel Pokémon API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Reservas",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/reservas"
      }
    }
  ]
}
```

Newman 

```yaml
steps:
  - name: Run API Tests
    run: |
      # Levanta la app
      ./gradlew bootRun & 
      # Espera a que inicie...
      # Ejecuta la colección contra la app corriendo
      newman run postman/collection.json \
        -e postman/local-env.json \
        --reporters cli,junit
```

## **Fase 11: DevOps (Semanas 41-44)**

### **Semana 41: CI/CD Pipeline**

- **Caso de uso**: GitHub Actions para build, test y deploy.
- **Crítica**: Un pipeline real debe fallar rápido. Si hay errores de estilo (Linting) o vulnerabilidades, no compiles.
- **Implementación recomendada**: Implementa Caching (para no descargar medio internet en cada commit) y análisis estático antes de los tests.

```yaml
name: Production Pipeline
on: [push]
jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '21'
          cache: 'gradle' # Cacheo automático de dependencias

      - name: Static Analysis (Detekt & Ktlint)
        run: ./gradlew detekt ktlintCheck

      - name: Security Scan (Trivy)
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'

  build-and-test:
    needs: validate
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: ./gradlew test bootJar
```

### **Semana 42: Docker Multi-stage (Layered Jars)**

- **Caso de uso**: Optimizar imagen Docker.
- **Crítica**: Si cambias una línea de código, Docker invalida toda la capa del JAR (incluyendo dependencias de 50MB). 
- **Implementación recomendada**: Usa Spring Boot Layered Jars. Separa dependencias, loader y código fuente en capas distintas de Docker.

```dockerfile
# Builder Stage
FROM eclipse-temurin:21-jre-alpine as builder
WORKDIR /application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
# Extrae las capas
RUN java -Djarmode=layertools -jar application.jar extract

# Runner Stage (Distroless para seguridad máxima - sin shell)
FROM gcr.io/distroless/java21-debian12
WORKDIR /application

# Copia capas por separado: Las dependencias (que cambian poco) se cachean mejor
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

### **Semana 43: Kubernetes Deployment (Probes & Resources)**

- **Caso de uso**: Desplegar en cluster K8s.
- **Crítica**: Un deployment sin resources y probes es una bomba de tiempo. Sin límites, un pod puede comerse toda la RAM del nodo. Sin probes, K8s enviará tráfico a un pod muerto. 
- **Implementación recomendada**: Define Quality of Service (QoS) garantizado.

```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
        - name: reservas
          image: gran-hotel/reservas:v1
          resources:
            requests: # Lo mínimo garantizado
              memory: "512Mi"
              cpu: "250m"
            limits: # El techo (si pasa, OOMKilled)
              memory: "1Gi"
              cpu: "500m"
          livenessProbe: # ¿Estoy vivo? (Si falla, reinicia)
            httpGet:
              path: /actuator/health/liveness
              port: 8080
          readinessProbe: # ¿Puedo recibir tráfico? (Si falla, saca del LoadBalancer)
            httpGet:
              path: /actuator/health/readiness
              port: 8080
```

### **Semana 44: Helm Charts (DRY - Don't Repeat Yourself)**

- **Caso de uso**: Package manager para K8s.
- **Implementación recomendada**: Crea un Umbrella Chart o usa templates para inyectar valores variables.

```yaml
# values.yaml
replicaCount: 2
image:
  repository: gran-hotel/reservas
  tag: "latest"

config:
  dbUrl: "jdbc:postgresql://postgres-prod:5432/db"

# En el template deployment.yaml:
# value: {{ .Values.config.dbUrl }}
```

## **Fase 12: Monitoreo y Observabilidad (Semanas 45-48)**

### **Semana 45: Métricas de Negocio (Micrometer)**

- **Caso de uso**: Exponer métricas para Prometheus.
- **Crítica**: No inyectes el Registry manualmente. Usa AOP. 
- **Implementación recomendada**: Mide lo que importa al negocio ($$$).

```kotlin
@Service
class ReservaService(private val meterRegistry: MeterRegistry) {

    // Timer para latencia y throughput
    // Taggea dinámicamente: éxito/fallo, tipoHabitacion
    fun crearReserva(tipo: String) {
        val sample = Timer.start(meterRegistry)
        try {
            // logica...
            sample.stop(Timer.builder("negocio.reservas.tiempo")
                .tag("tipo", tipo)
                .tag("resultado", "exito")
                .register(meterRegistry))
        } catch (e: Exception) {
            sample.stop(Timer.builder("negocio.reservas.tiempo")
                .tag("resultado", "error")
                .register(meterRegistry))
            throw e
        }
    }
}
```

### **Semana 46: Distributed Tracing (OpenTelemetry)**

- **Caso de uso**: Trazar requests entre microservicios con Sleuth/Zipkin.
- **Crítica**: Spring Cloud Sleuth está bien para pruebas y aprender. 
- **Implementación recomendada**: Micrometer Tracing + OpenTelemetry. Configura OTLP para enviar trazas a Grafana Tempo o Jaeger.

En build.gradle.kts:

```kotlin
implementation("io.micrometer:micrometer-tracing-bridge-otel")
implementation("io.opentelemetry:opentelemetry-exporter-otlp")
// Esto inyecta automáticamente el traceId y spanId en tus logs (Logback) y envía la traza al colector. No toques código, es configuración.
```

Configuración de Sleuth:

```yaml
spring:
  sleuth:
    sampler:
      probability: 1.0
  zipkin:
    base-url: http://zipkin:9411
```

### **Semana 47: Health Checks (Actuator)**

- **Caso de uso**: Endpoints de salud personalizados.
- **Crítica**: El health check por defecto solo dice "estoy encendido". 
- **Implementación recomendada**: Implementa HealthGroups. Separa chequeos vitales (DB) de chequeos no vitales (API externa de clima).

```yaml
management:
  endpoint:
    health:
      group:
        readiness:
          include: db, redis # Si estos fallan, no mandes tráfico
        liveness:
          include: ping # Solo comprueba que el proceso JVM corre
```

```kotlin
@Component
class PokemonApiHealthIndicator : AbstractHealthIndicator() {
    override fun doHealthCheck(builder: Health.Builder) {
        val pokemonApiStatus = checkPokemonApi()
        if (pokemonApiStatus.isUp) {
            builder.up()
        } else {
            builder.down()
                .withDetail("error", pokemonApiStatus.error)
        }
    }
}
```

### **Semana 48: Logs Estructurados (Loki/ELK)**

- **Caso de uso**: Logs en JSON para ELK Stack.
- **Crítica**: Logs planos (log.info("Hola")) son basura en microservicios. 
- **Implementación recomendada**: Logs en formato JSON (Logstash Logback Encoder) que incluyan automáticamente traceId para que puedas saltar de Grafana (Métricas) a Tempo (Trazas) y a Loki (Logs) con un clic.

```kotlin
@Slf4j
@Service
class ReservaService {
    fun crearReserva(request: ReservaRequest) {
        log.info("Creando reserva", StructuredArguments.kv("entrenadorId", request.entrenadorId))
        // Lógica
    }
}
```

## **Fase 13: Optimización y Escalabilidad (Semanas 49-52)**

### **Semana 49: Partitioning (No Sharding Manual)**

- **Caso de uso**: Sharding por región de entrenadores.
- **Crítica**: Implementar Sharding en la capa de aplicación (if id starts with A -> DB1) es una pesadilla de mantenimiento y transacciones distribuidas. 
- **Implementación recomendada**: Usa PostgreSQL Partitioning (Declarative Partitioning). Es transparente para tu código Java/Kotlin.

```sql
-- La app ve una sola tabla "batallas_log", Postgres gestiona la división física
CREATE TABLE batallas_log (
    id UUID,
    fecha_batalla DATE NOT NULL,
    ...
) PARTITION BY RANGE (fecha_batalla);

CREATE TABLE batallas_2024_01 PARTITION OF batallas_log
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
```

```kotlin
// Estrategia de sharding por primera letra del nombre
fun determineShard(entrenadorId: UUID): String {
    val region = calcularRegion(entrenadorId)
    return "shard_${region}"
}
```

### **Semana 50: API Rate Limiting (Bucket4j + Redis)**

- **Caso de uso**: Limitar requests por tipo de entrenador.
- **Crítica**: Un Rate Limiter en memoria (Guava/Caffeine) no funciona si más réplicas del servicio, pero puede ser suficiente para la mayoría de los casos. Sin embargo, es importante entender que el usuario golpeará la réplica A, luego la B, evadiendo el límite. 
- **Implementación recomendada**: Usa Bucket4j con backend en Redis.

```kotlin
// Básico
@RateLimiter(name = "reservas", fallbackMethod = "rateLimiterFallback")
@PostMapping("/reservas")
fun crearReserva(@RequestBody request: ReservaRequest): ResponseEntity<ReservaDTO> {
    // Lógica
}

// Avanzado
@Service
class RateLimitService(private val redissonClient: RedissonClient) {
    fun tryConsume(apiKey: String): Boolean {
        // El bucket vive en Redis, compartido por todos los pods
        val bucket = Bucket4j.extension(Redisson::class.java)
            .builder()
            .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
            .build(redissonClient, "limite:$apiKey")

        return bucket.tryConsume(1)
    }
}
```

### **Semana 51: GraphQL API**

- **Caso de uso**: Endpoint GraphQL para consultas complejas.
- **Crítica**: Hay que tener presente que si no se configura bien GraphQL hará 1 query para entrenadores + N queries para decks. 
- **Implementación recomendada**: Usa el patrón DataLoader (Batching).

```graphql
type Query {
  entrenador(id: ID!): Entrenador
  reservas(filters: ReservaFilters): [Reserva]
}

type Entrenador {
  id: ID!
  nombre: String!
  deck: [CartaPokemon]
  reservas: [Reserva]
}
```

```kotlin
@SchemaMapping(typeName = "Entrenador", field = "deck")
fun deck(entrenador: Entrenador, loader: DataLoader<UUID, List<CartaPokemon>>): CompletableFuture<List<CartaPokemon>> {
    // DataLoader acumula IDs y hace UNA sola query: SELECT * FROM cartas WHERE entrenador_id IN (...)
    return loader.load(entrenador.id)
}
```

### **Semana 52: Serverless Functions**

- **Caso de uso**: Funciones Lambda para procesos específicos.
- **Crítica**: JVM en AWS Lambda tiene "Cold Start" (tarda 3-5 segundos en despertar). Inaceptable para APIs de usuario. 
- **Implementación recomendada**: Compila tu función Kotlin a binario nativo con GraalVM o usa AWS SnapStart.

Implementación Práctica: Configura el plugin de GraalVM en Gradle.

```kotlin
// Función para procesar checkouts automáticos
class CheckoutFunction : RequestHandler<CheckoutEvent, Unit> {
    override fun handleRequest(event: CheckoutEvent, context: Context) {
        // Lógica de checkout
        // Actualizar estado
        // Generar factura
    }
}
```

```bash
./gradlew nativeCompile
# Genera un ejecutable binario que arranca en 50ms, no en 3000ms.
```

## Cierre del Proyecto

Si implementaste todos los retos has completado el ciclo. Lo que empezaste como "configurar Spring Boot" es ahora una arquitectura que soporta:

- Alta Disponibilidad: K8s Probes y ReplicaSets.
- Observabilidad: Trazabilidad distribuida con OpenTelemetry.
- Rendimiento: Caching de Docker, Partitioning de BD y Native Images.

> **Nota**: Los retos son acumulativos y cada semana construye sobre las anteriores, permitiendo crear un sistema completo y profesional mientras domina SpringBoot con Kotlin.

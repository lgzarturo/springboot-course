# Cuestionario: Spring Boot + Kotlin

## **Nivel Básico: Fundamentos y Lenguaje**

### **1. ¿Qué es Spring Boot realmente y qué significa "Convention over Configuration"?**

**Respuesta Técnica:** Spring Boot no es un framework nuevo, es una herramienta
de **opinión** sobre la plataforma Spring. Su propuesta de valor es eliminar la
"parálisis por análisis" en la configuración inicial.

- **Convention over Configuration:** Spring Boot asume una configuración
  predeterminada sensata (ej. si ve `H2` en el classpath, autoconfigura una DB
  en memoria). Solo configuras lo que se desvía de la norma.
- **Gestión de Dependencias:** Los `Starters` curan conflictos de versiones
  (Dependency Hell), garantizando que las librerías funcionen juntas.

```kotlin
/* * Mal enfoque: Configurar DataSource manualmente en un prototipo.
 * Buen enfoque: Dejar que Boot detecte el driver y configure el pool (HikariCP) automáticamente.
 */
@SpringBootApplication // Activa: @Configuration, @EnableAutoConfiguration, @ComponentScan
class HotelPokemonApplication

fun main(args: Array<String>) {
    runApplication<HotelPokemonApplication>(*args)
}
```

### **2. ¿Por qué Kotlin es arquitectónicamente superior a Java para Spring Boot hoy?**

**Respuesta Técnica:** Más allá de ser conciso, Kotlin ataca errores en tiempo
de compilación que en Java son errores en tiempo de ejecución.

- **Null Safety:** El sistema de tipos obliga a gestionar la nulidad. Se acabó
  el `Optional.ofNullable()` defensivo de Java.
- **Inmutabilidad por defecto:** `val` fomenta diseños thread-safe sin esfuerzo.
- **Interoperabilidad:** Puedes tener servicios Legacy en Java y nuevos en
  Kotlin en el mismo proyecto sin fricción.

```kotlin
// Java: Verbosidad y riesgo de NPE
public class Entrenador {
    private String nombre; // ¿Puede ser null? Quién sabe.
    // ...getters, setters, equals, hashcode
}

// Kotlin: Expresividad y Seguridad
// 'data class' nos da equals/hashCode/toString/copy gratis.
// 'val' asegura inmutabilidad.
// 'String?' vs 'String' define explícitamente el contrato de nulidad.
data class EntrenadorDTO(
    val id: UUID,
    val nombre: String,
    val region: String? = null // Default value reduce sobrecarga de constructores
)
```

---

## **Nivel Intermedio: Arquitectura y Patrones**

### **3. Inyección de Dependencias: ¿Por qué deberías prohibir `@Autowired` en campos?**

**Respuesta Técnica:** La inyección por campo (`@Autowired` private var) es un
antipatrón porque:

1. **Oculta dependencias:** No ves la complejidad de la clase hasta que revisas
   el código interno.
2. **Impide la inmutabilidad:** Obliga a que las variables sean mutables (`var`
   o `lateinit`).
3. **Dificulta el Testing:** Necesitas levantar el contexto de Spring o usar
   Reflection para testear la clase.

**Mejor Práctica (Constructor Injection):**

```kotlin
@Service
class ReservaService(
    // Claridad absoluta: Para instanciar esto, NECESITO un repositorio.
    private val habitacionRepository: HabitacionRepository,
    private val notificacionService: NotificacionService
) {
    // Lógica de negocio
}
// En Test Unitario:
// val service = ReservaService(mockRepo, mockNotif) -> No hace falta Spring Context.
```

### **4. Transacciones Distribuidas: Saga Pattern vs. Two-Phase Commit (2PC)**

**Respuesta Técnica:** En microservicios, ACID no escala entre servicios. 2PC
bloquea recursos y aumenta la latencia. La solución es la **Consistencia
Eventual** mediante el patrón SAGA.

- **Saga (Orquestación):** Un coordinador central dice qué hacer.
- **Compensación:** Si un paso falla, no hay "rollback" de base de datos global;
  se ejecuta una transacción inversa lógica (ej. "Reembolsar Dinero").

```kotlin
@Service
class ReservaOrchestrator(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    // Lógica de compensación explícita
    fun procesarFalloReserva(evento: ReservaFallidaEvent) {
        // No borramos el registro, aplicamos una contra-operación.
        pagoService.reembolsar(evento.pagoId)
        inventarioService.liberarHabitacion(evento.habitacionId)
        log.info("Compensación completada para reserva ${evento.reservaId}")
    }
}
```

### **5. ¿Cómo resuelves el problema N+1 en Spring Data JPA con Kotlin?**

**Respuesta Técnica:** El problema N+1 ocurre cuando el ORM hace 1 query para
traer una lista y N queries adicionales para traer las relaciones de cada
elemento. Mata el rendimiento silenciosamente.

- **Solución:** Usar `EntityGraph` o `JOIN FETCH` en JPQL para cargar todo en
  una sola query.

```kotlin
@Repository
interface EntrenadorRepository : JpaRepository<Entrenador, UUID> {

    // MAL: Lazy loading disparará N queries al acceder a la lista de pokemons
    // fun findAll(): List<Entrenador>

    // BIEN: EntityGraph fuerza un JOIN en la query inicial.
    @EntityGraph(attributePaths = ["pokemonsDeSuEquipo"])
    override fun findAll(): List<Entrenador>

    // Alternativa JPQL
    @Query("SELECT e FROM Entrenador e LEFT JOIN FETCH e.pokemonsDeSuEquipo")
    fun findAllConPokemons(): List<Entrenador>
}
```

---

## **Nivel Avanzado: Resiliencia, Escalabilidad y CI/CD**

### **6. Migraciones de Base de Datos Zero-Downtime (Expand & Contract)**

**Respuesta Técnica:** En producción, no puedes hacer `ALTER TABLE` bloqueantes
o borrar columnas que el código actual usa. Se usa el patrón
**Expand-Contract**:

1. **Expand:** Añades la nueva columna/tabla. El código escribe en AMBAS (vieja
   y nueva), pero lee de la vieja.
2. **Migrate:** Un script en segundo plano copia los datos históricos a la nueva
   estructura.
3. **Contract:** Despliegas código que lee/escribe solo en la nueva. Borras la
   columna vieja en un deploy futuro.

```kotlin
// Flyway: El control de versiones de tu DB
// V1__init.sql -> Estado inicial
// V2__add_column_nivel.sql -> ALTER TABLE entrenadores ADD nivel INT; (No romper, nullable o default)

// En Código (Feature Flagging implícito):
fun obtenerNivel(entrenador: Entrenador): Int {
    // Lógica de transición
    return entrenador.nivelNuevo ?: convertirNivelViejo(entrenador.categoria)
}
```

### **7. Corrutinas vs WebFlux: ¿Por qué preferimos Corrutinas en Kotlin?**

**Respuesta Técnica:** Spring WebFlux (Project Reactor) usa un estilo funcional
complejo (`Mono`, `Flux`, `.flatMap`). Kotlin Corrutinas permite escribir código
asíncrono no bloqueante con estilo **imperativo y secuencial**.

- **Under the hood:** Utilizan continuaciones (`Continuation Passing Style`)
  para suspender la ejecución sin bloquear el hilo del sistema operativo. Esto
  permite manejar miles de requests concurrentes con pocos hilos.

```kotlin
@RestController
class PokedexController(private val pokeApi: WebClient) {

    // Código imperativo, lectura lineal, pero 100% no bloqueante
    @GetMapping("/pokemon/{id}")
    suspend fun getPokemonInfo(@PathVariable id: String): PokemonInfo {
        // El hilo se libera mientras esperamos la respuesta externa
        val basicInfo = pokeApi.get().uri("/pokemon/$id").awaitBody<BasicInfo>()
        val speciesInfo = pokeApi.get().uri(basicInfo.speciesUrl).awaitBody<SpeciesInfo>()

        return combinar(basicInfo, speciesInfo)
    }
}
```

### **8. Patrones de Resiliencia: Circuit Breaker**

**Respuesta Técnica:** Evita el "efecto cascada". Si un microservicio externo
(ej. Pasarela de Pagos) está caído, tu servicio no debe quedarse esperando
(`timeout`) y consumiendo hilos hasta morir. Debe "abrir el circuito" y fallar
rápido o dar una respuesta por defecto.

```kotlin
@Service
class PagoService {

    @CircuitBreaker(name = "pasarelaPago", fallbackMethod = "pagoFallback")
    fun procesarPago(orden: Orden) {
        // Llamada riesgosa a servicio externo
        restTemplate.postForObject(...)
    }

    // Se ejecuta si el circuito está abierto o hay excepción
    fun pagoFallback(orden: Orden, ex: Exception) {
        log.warn("Pasarela caída. Guardando pago para reintento asíncrono.")
        queueService.encolarPagoPendiente(orden)
    }
}
```

### **9. Optimización de Memoria en la JVM para Contenedores (Docker/K8s)**

**Respuesta Técnica:** Java en contenedores solía ignorar los límites de memoria
del cgroup, causando OOMKilled.

- **JVM Awareness:** La JVM detecta que está en un contenedor.
- **Heap vs Non-Heap:** No asignes el 100% de la RAM al Heap (`-Xmx`). Deja
  espacio para Metaspace, Threads y Overhead del GC.
- **Best Practice:** Usar `XX:MaxRAMPercentage` en lugar de valores fijos
  (`-Xmx4g`), para que se adapte dinámicamente al límite del Pod de Kubernetes.

```dockerfile
# Dockerfile optimizado
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### **10. Gestión de Errores Global: `@ControllerAdvice**`

**Respuesta Técnica:** No pongas `try-catch` en cada controlador. Centraliza el
manejo de excepciones para garantizar respuestas JSON consistentes (RFC 7807
Problem Details).

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException::class)
    fun handleNotFound(ex: RecursoNoEncontradoException): ResponseEntity<ErrorDTO> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorDTO("NOT_FOUND", ex.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorDTO> {
        // Transforma errores de validación de Spring en un formato limpio para el frontend
        return ResponseEntity.badRequest().body(...)
    }
}
```

## **Testing & Calidad (La red de seguridad)**

### **11. ¿Por qué deberías preferir `TestContainers` sobre H2 para tests de integración?**

**Respuesta Técnica:** H2 es una base de datos en memoria que "emula" SQL, pero
no es PostgreSQL ni MySQL. Tiene dialectos y comportamientos diferentes (ej.
JSONB, funciones de ventana, constraints). Usar H2 te da falsos positivos.
`TestContainers` levanta un contenedor Docker real de tu base de datos para el
test, garantizando paridad exacta con producción.

```kotlin
@Testcontainers
@SpringBootTest
class ReservaIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
        }
    }
    // El test corre contra un Postgres real. Si pasa aquí, pasa en prod.
}
```

### **12. ¿Cuál es el costo oculto de usar `@MockBean` excesivamente?**

**Respuesta Técnica:** Cada vez que usas `@MockBean` en un test, Spring
**ensucia el contexto de la aplicación**. Esto obliga al framework a recargar el
contexto completo para el siguiente test, lo que aumenta drásticamente el tiempo
de ejecución de la suite de pruebas. **Mejor práctica:** Usar Mockito puro
(`Mockito.mock()`) con inyección por constructor para tests unitarios (rápidos)
y reservar `@MockBean` solo cuando sea inevitable en tests de integración.

### **13. ¿Cómo testeas lógica asíncrona (Corrutinas) sin flaky tests?**

**Respuesta Técnica:** No uses `Thread.sleep()`. En Kotlin, usa `runTest` (de
`kotlinx-coroutines-test`). Esto permite controlar el tiempo virtualmente,
ejecutando delays instantáneamente pero manteniendo el orden lógico.

```kotlin
@Test
fun `debe calcular experiencia asincrona`() = runTest {
    val service = EntrenadorService(dispatcher = StandardTestDispatcher(testScheduler))
    val resultado = service.calcularNivelAsync()
    advanceUntilIdle() // Avanza el tiempo virtual
    assertEquals(50, resultado)
}
```

---

## **Seguridad y Diseño de API**

### **14. Stateful vs Stateless Auth: ¿Por qué JWT es el estándar en Microservicios?**

**Respuesta Técnica:** En una arquitectura monolítica clásica, la sesión
(`JSESSIONID`) se guarda en la memoria del servidor. Si escalas horizontalmente
(5 instancias), necesitas "Sticky Sessions" o replicación de sesiones (Redis).
**JWT (Stateless):** El token contiene la identidad y los permisos (claims)
firmados. El servidor no guarda estado. Cualquier instancia puede validar el
token matemáticamente sin consultar una base de datos centralizada en cada
request.

### **15. ¿Cómo implementas seguridad a nivel de método (ACL) limpiamente?**

**Respuesta Técnica:** Usar `@PreAuthorize` con SpEL (Spring Expression
Language). Esto desacopla la lógica de seguridad del flujo de negocio.

```kotlin
@Service
class GimnasioService {
    // Solo el líder del gimnasio o un admin puede cerrar el gimnasio
    // #id es el argumento del método
    @PreAuthorize("hasRole('ADMIN') or @gimnasioSecurity.esLider(#id, authentication.name)")
    fun cerrarGimnasio(id: UUID) { ... }
}
```

### **16. ¿Cómo manejas secretos (API Keys, DB Passwords) en producción?**

**Respuesta Técnica:** **Nunca** hardcodeados en `application.yml` ni
commiteados en Git.

1. **Variables de Entorno:** Inyección vía Docker/K8s (`${DB_PASSWORD}`).
2. **Secret Managers:** Integración con AWS Secrets Manager o HashiCorp Vault.
   Spring Cloud Vault puede inyectarlos como properties al arrancar la
   aplicación de forma transparente.

---

## **Persistencia Avanzada (JPA/Hibernate)**

### **17. Optimistic Locking: ¿Cómo evitas que dos entrenadores reserven la misma habitación al mismo milisegundo?**

**Respuesta Técnica:** No uses bloqueos de base de datos pesados
(`SELECT FOR UPDATE`) a menos que sea crítico. Usa una columna `@Version`. JPA
verificará si la versión en la DB coincide con la que tienes en memoria antes de
hacer el update.

```kotlin
@Entity
class Habitacion {
    @Id val id: UUID = UUID.randomUUID()

    @Version
    val version: Long = 0 // Hibernate maneja esto automáticamente
}
// Si dos hilos intentan guardar, uno lanzará OptimisticLockException.
// Capturas la excepción y pides al usuario que reintente.
```

### **18. Projections: ¿Por qué no debes devolver Entidades JPA directamente en el Controller?**

**Respuesta Técnica:**

1. **Seguridad:** Expones datos internos (password hashes, auditoría).
2. **Rendimiento:** Serializar una entidad puede disparar lazy loading de
   relaciones, trayendo media base de datos a memoria.
3. **Acoplamiento:** Atas tu contrato de API a tu esquema de DB. **Solución:**
   Usa DTOs (Data classes en Kotlin) o Interfaces de Proyección de Spring Data
   para traer solo los campos necesarios.

### **19. ¿Qué ventaja ofrecen los "Reified Type Parameters" de Kotlin en `RestTemplate` o `WebClient`?**

**Respuesta Técnica:** En Java, debido al _Type Erasure_, no puedes hacer
`getForObject(url, List<Pokemon>.class)`. Necesitas `ParameterizedTypeReference`
horribles. En Kotlin, con funciones `inline` y `reified`, el tipo se preserva.

```kotlin
// Java way (feo)
List<Pokemon> lista = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Pokemon>>(){}).getBody();

// Kotlin way (elegante, gracias a extension functions)
val lista: List<Pokemon> = restTemplate.getForObject(url) // Infiere el tipo mágicamente
```

### **20. Auditoría Automática: ¿Cómo registras quién creó un registro sin ensuciar la lógica de negocio?**

**Respuesta Técnica:** Usando JPA Auditing (`@EnableJpaAuditing`).

```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class Auditable {
    @CreatedDate
    var fechaCreacion: Instant? = null

    @CreatedBy
    var usuarioCreacion: String? = null // Spring Security llena esto automáticamente
}
```

---

## **Arquitectura y Patrones de Diseño**

### **21. Hexagonal Architecture (Ports & Adapters): ¿Cómo organizas los paquetes?**

**Respuesta Técnica:** El objetivo es proteger el **Dominio** de la
infraestructura (Frameworks, DB, Web).

- `domain`: Entidades puras, lógica de negocio. Cero dependencias de Spring.
- `application`: Casos de uso (Servicios), Interfaces (Puertos) de
  entrada/salida.
- `infrastructure`:
- `web`: Controllers (Adaptador de entrada).
- `persistence`: Repositorios JPA implementando interfaces del dominio
  (Adaptador de salida).
- `config`: Beans de Spring.

### **22. Validaciones: Bean Validation vs. Domain Validation**

**Respuesta Técnica:**

- **Bean Validation (`@NotNull`, `@Size`):** Para validación superficial de
  formato en la capa de entrada (DTOs). "Fail fast" antes de procesar.
- **Domain Validation:** Reglas de negocio complejas ("Un entrenador no puede
  tener más de 6 pokémons"). Esto va **dentro** de la entidad o servicio de
  dominio, escrito en Kotlin puro, asegurando que la entidad nunca pueda estar
  en un estado inválido.

### **23. ¿Para qué sirve el patrón "Outbox" en microservicios?**

**Respuesta Técnica:** Resuelve el problema de dualidad de escritura: "Guardar
en DB y publicar en Kafka". Si guardas y Kafka falla, tienes inconsistencia.
**Outbox Pattern:** Guardas el evento en una tabla `outbox` en la **misma
transacción** de base de datos que tu entidad. Luego, un proceso asíncrono (o
Debezium) lee la tabla `outbox` y lo publica en Kafka. Atomicidad garantizada.

### **24. Application Events: Desacoplamiento intra-modular**

**Respuesta Técnica:** En lugar de que `ReservaService` llame directamente a
`EmailService`, `LogisticaService` y `FacturacionService`, publica un evento.

```kotlin
// ReservaService.kt
eventPublisher.publishEvent(ReservaCreadaEvent(reserva))

// EmailService.kt
@EventListener
fun alCrearReserva(event: ReservaCreadaEvent) { ... }
// Si mañana agregas un servicio de Analytics, solo escuchas el evento. No tocas ReservaService.
```

---

## **Observabilidad y Operaciones (DevOps friendly)**

### **25. Liveness vs Readiness Probes en Kubernetes: ¿Cuál es la diferencia crítica?**

**Respuesta Técnica:** Spring Boot Actuator expone ambos.

- **Liveness:** "¿Estoy vivo o colgado?" Si falla, K8s **reinicia** el pod. (Ej.
  Deadlock).
- **Readiness:** "¿Puedo recibir tráfico?" Si falla, K8s **deja de enviarle
  tráfico** pero no lo mata. (Ej. La DB está lenta o estoy calentando caché).
- _Error común:_ Poner la verificación de la DB en Liveness. Si la DB cae, K8s
  reiniciará todos tus pods en bucle, empeorando el problema.

### **26. Logging Estructurado: ¿Por qué `log.info("Usuario " + id)` es un crimen?**

**Respuesta Técnica:** En sistemas modernos (ELK, Datadog), los logs se indexan.
Un string plano es difícil de consultar. Usa **Structured Logging** (JSON).
Kotlin permite esto elegantemente con librerías como `kotlin-logging`. El log
debe verse así: `{"message": "Login", "userId": "123", "ip": "10.0.0.1"}` para
poder filtrar por `userId` en Kibana.

### **27. Distributed Tracing: ¿Qué es el `TraceId` y `SpanId`?**

**Respuesta Técnica:** En microservicios, un request pasa por 5 servicios. Si
hay un error en el 4º, ¿cómo encuentras el log inicial en el 1º? Micrometer
Tracing (antes Sleuth) inyecta un `TraceId` único en los headers HTTP. Ese ID
viaja por todos los servicios y se imprime en cada log. Te permite reconstruir
la "historia" completa de una transacción distribuida.

### **28. Graceful Shutdown: ¿Qué pasa con los requests en vuelo cuando haces deploy?**

**Respuesta Técnica:** Si matas el proceso (`SIGTERM`), cortas conexiones
activas. Spring Boot soporta **Graceful Shutdown**.

1. Deja de aceptar tráfico nuevo.
2. Espera un tiempo configurable (`server.shutdown=graceful`) para que terminen
   los requests activos.
3. Cierra conexiones a DB y pools.
4. Se apaga.

---

## **Kotlin Specifics & Performance**

### **29. Scope Functions (`let`, `apply`, `also`): ¿Cuándo usar cuál en Spring?**

**Respuesta Técnica:** No es solo estilo, es legibilidad de intención.

- `apply`: Para configurar un objeto (ej. Builder de un DTO o Entidad).
  `entidad.apply { nombre = "X" }`.
- `also`: Para efectos secundarios (logging, validación) sin modificar el flujo.
  `repo.save(x).also { log.info("Guardado") }`.
- `let`: Para transformar objetos nulos. `nullableVar?.let { ... }`.

### **30. `@Async` vs Corrutinas: ¿Por qué `@Async` es peligroso por defecto?**

**Respuesta Técnica:** `@Async` usa un `SimpleAsyncTaskExecutor` por defecto,
que crea **un hilo nuevo por cada tarea**. Si te atacan, revientas la memoria
del servidor (OutOfMemoryError) por creación de hilos. Siempre debes configurar
un `ThreadPoolTaskExecutor` con límites definidos. O mejor aún: usa Corrutinas,
que multiplexan miles de tareas en pocos hilos.

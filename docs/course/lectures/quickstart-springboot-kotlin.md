# MVC con Spring Boot y Kotlin

En Spring Boot con Kotlin, la forma más limpia de organizar el acceso a datos es
seguir el patrón de 3 capas: el Controlador recibe las peticiones HTTP, el
Servicio contiene la lógica de negocio, y el Repositorio habla con la base de
datos. Nunca deberías inyectar un Repositorio directamente en un Controlador.
Parece un atajo, pero créeme a la larga te genera acoplamiento y hace el código
más difícil de probar.

## Configuración del proyecto

### Dependencias

Antes de entrar en el código de la aplicación, hay que revisar que Gradle tiene
las dependencias necesarias y los plugins que necesitamos para que Kotlin y JPA
funcionen correctamente.

- El plugin `kotlin("plugin.jpa")` es requerido porque JPA requiere
  constructores sin argumentos, y en Kotlin eso no existe por defecto en las
  clases normales. Este plugin genera automáticamente constructores sin
  argumentos para las entidades.
- El plugin `kotlin("plugin.spring")` es requerido para que Spring pueda
  trabajar con Kotlin. Este plugin proporciona soporte para Spring Boot y Spring
  MVC en Kotlin. Además, hace las clases `open` para que Spring pueda
  inyectarlas en los controladores y otros componentes.

Dependencia del archivo `build.gradle.kts`:

```kotlin
plugins {
    // ...
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Database
    runtimeOnly("org.postgresql:postgresql") // o H2 si quieres algo en memoria

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
```

### Propiedades

```yaml
spring:
  application:
    name: hotel-plokemon-api
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
        jdbc:
          batch_size: 20
          fetch_size: 50
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver

  h2:
    console:
      enabled: true

server:
  port: 8080
```

## Arquitectura en capas

Entity → Repository → Service → Controller

```
┌───────────────────────────────────────┐
│           Controller Layer            │  ← REST API, validación de entrada
│   (@RestController, @RequestMapping)  │
├───────────────────────────────────────┤
│           Service Layer               │  ← Lógica de negocio, transacciones
│     (@Service, @Transactional)        │
├───────────────────────────────────────┤
│         Repository Layer              │  ← Acceso a datos, consultas
│    (@Repository, JpaRepository)       │
├───────────────────────────────────────┤
│         Entity/Domain Layer           │  ← Modelos JPA, relaciones
│          (@Entity, @Table)            │
└───────────────────────────────────────┘
```

La forma recomendada de estructurar esto en Spring Boot sigue el patrón de
capas. Cada capa tiene una responsabilidad clara y no se mezcla con las demás.
La idea es simple: el controlador no sabe nada de la base de datos, y la entidad
no sabe nada del HTTP. Lo que los conecta es el servicio, y lo que viaja entre
capas son los DTOs (objetos de transferencia de datos).

Para este ejemplo, vamos a usar un dominio concreto para los ejemplos: Pokemon,
KindPokemon, Trainer y TrainerProfile.

## La entidad base es la Trainer.

```kotlin
@Entity
@Table(name = "trainers")
class Trainer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(nullable = false, length = 100)
    var name: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, updatable = true)
    var updatedAt: Instant = Instant.now()
) {
    override fun toString(): String {
        return "Entrenador(id=$id, nombre='$name')"
    }
}
```

> Pon atención sobre el uso de `var` solo en los atributos que pueden cambiar
> (name, email, updatedAt) y el uso de `val` en los atributos que no pueden
> cambiar (id, createdAt, updatedAt) porque deben ser valores inmutables. Esto
> es una buena práctica en Kotlin porque el compilador te protege de
> modificaciones accidentales.

## Ahora vamos a definir el repositorio.

```kotlin
@Repository
interface TrainerRepository : JpaRepository<Trainer, Long> {
    // Spring Data genera la query usando el nombre del método
    fun findByEmail(email: String): Trainer?

    // Puedes usar @Query para definir consultas más complejas
    @Query("SELECT e FROM Trainer e WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByName(@Param("name") name: String): List<Trainer>

    // Puedes usar @Query para definir consultas con relaciones
    @Query("SELECT e FROM Trainer e JOIN FETCH e.pokemons WHERE e.id = :id")
    fun findByIdWithPokemons(@Param("id") id: Long): Trainer?
}
```

El método `findByEmail` es una consulta simple, si no existe el entrenador con
el email proporcionado, devuelve `null`. En lugar de lanzar una excepción. Esto
es mucho más idiomático que el `Optional<Trainer>` de Java.

Hasta este punto, el repositorio y la entidad son la capa de acceso a datos. Es
importante tener en cuenta que la comunicación del servicio al repositorio se
hace con la entidad, pero del servicio al controlador se hace con el modelo de
dominio (los famosos DTOs).

## Ahora vamos a definir unos DTOs

```kotlin
// Esta clase representa la información necesaria para crear un entrenador
data class CreateTrainerRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    val name: String,
    @field:Email(message = "El email no es valido")
    @field:NotBlank(message = "El email es obligatorio")
    val email: String
)

// Esta clase representa la información necesaria para actualizar un entrenador
data class PatchTrainerRequest(
    val name: String? = null,
    val email: String? = null
)

// Esta clase representa lo que va a responder el controlador
data class TrainerResponse(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        // Este método se usa para convertir una entidad Trainer en un DTO
        fun from(trainer: Trainer) = TrainerResponse(
            id = trainer.id,
            name = trainer.name,
            email = trainer.email,
            createdAt = trainer.createdAt,
            updatedAt = trainer.updatedAt
        )
    }
}

// Esta clase representa cuando un entrenador añade un Pokémon a su lista
data class CreatePokemonRequest(
    val name: String,
    val kind: KindPokemon,
)

// Esta clase representa un Pokémon que se devuelve en la respuesta del controlador
data class PokemonResponse(
    val id: Long,
    val name: String,
    val kind: KindPokemon
)
```

## Ahora vamos a definir el servicio.

En esta caso, el servicio va a ser una clase que va a contener los métodos que
van a interactuar con el repositorio y que van a ser usados por el controlador.
El objetivo es que el controlador no tenga que saber nada sobre la persistencia.

```kotlin
@Service
@Transactional
class TrainerService(
    // Spring inyecta el repositorio, la inyección por constructor es la forma recomendada
    // No se requiere @Autowired si solo existe un constructor
    private val trainerRepository: TrainerRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): List<TrainerResponse> {
        return trainerRepository.findAll().map { TrainerResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): TrainerResponse = TrainerResponse.from(trainerRepository.findById(id).orElseThrow {
        EntityNotFoundException("Entrenador con id $id no encontrado")
    })

    fun create(request: CreateTrainerRequest): TrainerResponse {
        if (trainerRepository.findByEmail(request.email) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "El email ${request.email} ya está en uso")
        }
        val trainer = Trainer(name = request.name, email = request.email)
        return TrainerResponse.from(trainerRepository.save(trainer))
    }

    fun patch(id: Long, request: PatchTrainerRequest): TrainerResponse {
        val trainer = trainerRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Entrenador no encontrado") }

        // El operador `?.` (safe call) + let solo ejecuta si el valor no es null
        request.name?.let { trainer.name = it }
        request.email?.let { trainer.email = it }
        return TrainerResponse.from(trainerRepository.save(trainer))
    }

    fun delete(id: Long) {
        if (!trainerRepository.existsById(id)) throw EntityNotFoundException(
            "Entrenador con id $id no encontrado"
        )
        trainerRepository.deleteById(id)
    }

    fun addPokemonToTrainer(authorId: Long, request: CreatePokemonRequest): TrainerResponse {
        val trainer = trainerRepository.findById(authorId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Entrenador no encontrado") }

        val pokemon = Pokemon(
            name = request.name,
            kind = request.kind
        )

        // Usamos el helper para mantener la consistencia bidireccional
        trainer.addPokemon(pokemon)

        // Como Pokemon tiene cascade desde Trainer, solo necesitamos guardar Trainer
        trainerRepository.save(trainer)

        return TrainerResponse.from(trainer)
    }
}
```

## Ahora vamos a definir el controlador.

```kotlin
@RestController
@RequestMapping("/api/v1/trainers", produces = ["application/json"])
class TrainerController(private val trainerService: TrainerService) {
    // Es un método GET que devuelve una lista de entrenadores
    // Si todo va bien, retorna 200 OK con la lista de entrenadores
    // Si no hay entrenadores, retorna 200 OK con una lista vacía
    // Si hay un error, retorna 500 Internal Server Error, a menos que se especifique lo contrario
    @GetMapping
    fun findAll(): ResponseEntity<List<TrainerResponse>> {
        return ResponseEntity.ok(trainerService.findAll())
    }

    // Es un método GET que devuelve un entrenador por su id
    // Si todo va bien, retorna 200 OK con el entrenador
    // Si no se encuentra el entrenador, retorna 404 Not Found
    // Si hay un error, retorna 500 Internal Server Error, a menos que se especifique lo contrario
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<TrainerResponse> {
        return ResponseEntity.ok(trainerService.findById(id))
    }

    // Es un método POST que crea un nuevo entrenador
    // Si todo va bien, retorna 201 Created con el entrenador creado
    // Si hay un error, retorna 500 Internal Server Error, a menos que se especifique lo contrario
    @PostMapping
    fun create(@RequestBody @Valid request: CreateTrainerRequest): ResponseEntity<TrainerResponse> {
        val createdTrainer = trainerService.create(request)
        // Devolvemos 201 Created con la URI del recurso recién creado en el header Location
        val uri = URI.create("/api/v1/trainers/${createdTrainer.id}")
        val headers = HttpHeaders().apply { location = uri }
        return ResponseEntity.created(uri).headers(headers).body(createdTrainer)
    }

    // @PatchMapping sirve para actualizaciones PARCIALES de un recurso
    // La diferencia con @PutMapping es conceptual pero importante:
    //   PUT reemplaza el recurso COMPLETO (tienes que enviar todos los campos)
    //   PATCH modifica solo los campos que envías, el resto queda como estaba
    //
    // Ejemplo:
    //   PUT /api/v1/trainers/1 → { "name": "Nuevo nombre", "email": "user@domain.com" } (todos obligatorios)
    //   PATCH /api/v1/trainers/1 → { "name": "Solo cambio el nombre" } (el resto no se toca)

    // Es un método PATCH que actualiza los datos de un entrenador
    // Si todo va bien, retorna 200 OK con el entrenador actualizado
    // Si no se encuentra el entrenador, retorna 404 Not Found
    // Si hay un error, retorna 500 Internal Server Error, a menos que se especifique lo contrario
    @PatchMapping("/{id}")
    fun patch(@PathVariable id: Long, @RequestBody request: PatchTrainerRequest): ResponseEntity<TrainerResponse> {
        return ResponseEntity.ok(trainerService.patch(id, request))
    }

    // Es un método POST que agrega un Pokémon a un entrenador
    // Si todo va bien, retorna 201 Created con el Pokémon creado
    // Si no se encuentra el entrenador, retorna 404 Not Found
    // Si hay un error, retorna 500 Internal Server Error, a menos que se especifique lo contrario
    @PostMapping("/{id}/pokemons")
    fun addPokemonToTrainer(
        @PathVariable id: Long,
        @RequestBody @Valid request: CreatePokemonRequest
    ): ResponseEntity<PokemonResponse> {
        val pokemon = trainerService.addPokemonToTrainer(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(pokemon)
    }

    // Endpoint para acceder al lado inverso de la relación OneToMany
    // Es un método GET que devuelve todos los Pokémon de un entrenador
    // Si todo va bien, retorna 200 OK con una lista de PokémonResponse
    // Si no se encuentra el entrenador, retorna 404 Not Found
    // Si hay un error, retorna 500 Internal Server Error, a menos que se especifique lo contrario
    @GetMapping("/{id}/pokemons")
    fun getPokemons(@PathVariable id: Long): ResponseEntity<List<PokemonResponse>> {
        val trainer = trainerService.findById(id)
        return ResponseEntity.ok(trainer.pokemons.map { PokemonResponse.from(it) })
    }

    // Es un método DELETE que borra un entrenador por su id
    // Si todo va bien, retorna 204 No Content
    // Si no se encuentra el entrenador, retorna 404 Not Found
    // Si hay un error, retorna 500 Internal Server Error, a menos que se especifique lo contrario
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        trainerService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
```

## Una pequeña nota sobre el método `PATCH`.

La diferencia entre `PATCH` y `PUT` es meramente conceptual, pero es importante
tener en cuenta, ya que en la especificación HTTP se menciona lo siguiente:

`PUT` remplaza el recurso completo, Si un entrenador tiene nombre y correo, una
llamada PUT debería enviar ambos campos. Si omites uno, se entiende que quieres
borrarlo o dejarlo en blanco.

`PATCH` actualiza solo los campos que enviaste. Si solo quieres cambiar el
correo de un entrenador, mandas solo el campo de correo y el resto se queda
igual.

Es por esa razón que en `PatchTrainerRequest` todos los campos son `nullables`
con valor por defecto `null`. La lógica de negocio es que si el campo es null,
lo dejo como está; pero si el campo es no null, lo actualizo.

```kotlin
// El cliente envía solo lo que quiere cambiar:
// PATCH /api/v1/trainers/1
// { "email": "nuevo@correo.com" }

// Esto se deserializa como:
// PatchTrainerRequest(name = null, email = "nuevo@correo.com")

// Y en el service:
request.name?.let { trainier.name = it }    // null, no se toca
request.email?.let { trainier.email = it }  // tiene valor, se actualiza
```

> Esto es mucho más eficiente que forzar al cliente a enviar todo el objeto cada
> vez que quiere cambiar un solo campo.

## Relaciones y buenas prácticas

Justo en este punto, es donde la mayoría de los programadores junior tienen
problemas. En este punto, vamos a profundizar en las relaciones entre entidades
y a explicarlas en detalle.

### El entrenador tiene muchos Pokemones.

Esta es una de las relaciones más comunes.

Un entrenador puede tener muchos Pokémon, pero cada Pokémon pertenece a un solo
entrenador. Esta es una relación clásica de uno a muchos.

Aunque varios Pokémon puedan ser del mismo tipo o especie, no son copias entre
sí. Cada uno es un individuo único, con su propio nombre, nivel, estadísticas y
características. Dos entrenadores pueden tener, por ejemplo, un Pikachu, pero en
realidad se trata de criaturas distintas.

Piensa en el tipo de Pokémon como una especie. Define rasgos generales, pero no
la identidad del individuo. La identidad aparece cuando ese Pokémon forma un
vínculo con su entrenador y adquiere atributos propios que lo diferencian de los
demás.

Por eso, aunque externamente parezcan iguales, cada Pokémon debe tratarse como
una entidad única dentro del sistema.

```kotlin
@Entity
@Table(name = "pokemons")
class Pokemon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(nullable = false, length = 100)
    var name: String,
    // El uso de @Enumerated es muy importante para evitar problemas de integridad referencial.
    // En este caso, se usa `EnumType.STRING` para que el tipo de dato sea una cadena en la base de datos.
    // Se usa columnDefinition para especificar el tipo de dato en la base de datos y evitar que los genere como Checker en PostgreSQL.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    var kind: KindPokemon = KindPokemon.NORMAL,
    // El dueño de la relación es el entrenador. Este es el que tiene la FK en la BD
    // LAZY es crítico para evitar carga excesiva de datos en consultas, especialmente en relaciones muchos a uno
    // de lo contrario, cada vez que se consulta un entrenador, también se cargan todos sus Pokémon, lo que puede ser ineficiente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    val trainer: Trainer
)


enum class KindPokemon {
    NORMAL,
    FIRE,
    WATER,
    ELECTRIC,
    GRASS,
    ICE,
    FIGHTING,
    POISON,
    GROUND,
    FLYING,
    PSYCHIC,
}
```

Ahora vamos a modificar el lado del entrenador para que pueda tener muchos
Pokémon.

```kotlin
@Entity
@Table(name = "trainer_profiles")
class Trainer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(nullable = false, length = 100)
    var name: String,
    @Column(nullable = false, unique = true)
    var email: String,
    // mappedBy indica que Pokemon.trainer es el atributo que contiene la FK en la entidad Pokemon
    // CASCADE ALL indica que se van a propagar las operaciones (save, delete) en los pokemones
    // orphanRemoval = true elimina los pokemones que se devinculen de su entrenador
    @OneToMany(
        mappedBy = "trainer",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val pokemons: Set<Pokemon> = HashSet(),
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false, updatable = true)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "Entrenador(id=$id, nombre='$name')"
    }

    // Métodos helper para mantener ambos lados de la relación sincronizados
    // Esto es una buena práctica para mantener la integridad referencial y evitar inconsistencias
    // en la base de datos. Muchos desarrolladores no lo hacen porque es muy complicado y luego se
    // preguntan por qué JPA se comporta raro.

    fun addPokemon(pokemon: Pokemon) {
        pokemons.add(pokemon)
        pokemon.trainer = this
    }

    fun removePokemon(pokemon: Pokemon) {
        pokemons.remove(pokemon)
        pokemon.trainer = null
    }
}
```

## DTO para pokemones

```kotlin
data class PokemonResponse(
    val id: Long,
    val name: String,
    val kind: KindPokemon,
    val trainerId: Long?,
    val trainerName: String?
) {
    companion object {
        fun from(pokemon: Pokemon) = PokemonResponse(
            id = pokemon.id,
            name = pokemon.name,
            kind = pokemon.kind,
            trainerId = pokemon.trainer?.id,
            trainerName = pokemon.trainer?.name
        )
    }
}
```

> El atributo `trainerName` es opcional porque el Pokémon no tiene que
> pertenecer a un entrenador. Es cuando está en un estado salvaje. Además, no se
> requiere el objeto `TrainerResponse`. Esta es una forma de evitar referencias
> circulares en la serialización JSON.

## El entrenador tiene un perfil

Ahora la relación `@OneToOne`, esta se usa cuando dos entidades comparten una
relación exclusiva. Aquí cada entrenador tiene exactamente un perfil con su
biografía y foto.

```kotlin
@Entity
@Table(name = "trainers")
class TrainerProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    @Column(columnDefinition = "TEXT")
    var biography: String? = null,

    @Column(name = "photo_url")
    var photoUrl: String? = null,

    @Column(name = "website_url")
    var websiteUrl: String? = null,

    // El lado dueño de la relación (tiene la FK)
    // JoinColumn indica que Trainer es quien "posee" la relación
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false, unique = true)
    val trainer: Trainer
)
```

Y ahora modificamos el entrenador para que tenga un perfil.

```kotlin
@Entity
@Table(name = "trainers")
class Trainer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(nullable = false, length = 100)
    var name: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @OneToMany(
        mappedBy = "trainer",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val pokemons: Set<Pokemon> = HashSet(),
    // @OneToOne: Trainer posee la relación porque tiene el @JoinColumn aquí
    // cascade = ALL significa que si guardas Author, también se guarda el perfil
    // orphanRemoval = true: si desvinculás el perfil, se borra de la DB
    @OneToOne(
        mappedBy = "trainer",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    var profile: TrainerProfile? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false, updatable = true)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "Entrenador(id=$id, nombre='$name')"
    }

    fun addPokemon(pokemon: Pokemon) {
        pokemons.add(pokemon)
        pokemon.trainer = this
    }

    fun removePokemon(pokemon: Pokemon) {
        pokemons.remove(pokemon)
        pokemon.trainer = null
    }
}
```

> Un pequeño detalle: `Hibernate` tiene una limitación conocida con el lado
> inverso de `@OneToOne`. En la práctica, él `LAZY` no funciona en el lado
> inverso si la relación es nullable, esto hay que tenerlo en cuenta. Ya que
> `Hibernate` no sabe si el entrenador tiene un perfil o no, entonces necesita
> hacer una `query` para saber si el perfil existe o es `null`, y ya que va a la
> base de datos, trae todo el objeto. Si esto llega a generar problemas de
> rendimiento, algunas alternativas son usar `@MapsId` para compartir la misma
> `PK` o modelar como un `@OneToMany` con máximo logico de uno.

## Resumen visual de las relaciones

Para que quede claro cómo se mapea todo esto en la base de datos:

```
trainer_profiles          trainers                   pokemons
─────────────────         ──────────────────────     ──────────────────────
id (PK)          ←──FK──  id (PK)                    id (PK)
biography                 name                       name
photo_url                 email                      kind
website_url               profile_id (FK) ──────┘    trainer_id (FK) ──→ trainers.id
                          created_at
                          updated_at
```

La columna `profile_id` está en `trainers` porque Trainer es el lado dueño del
`@OneToOne`. La columna `trainer_id` está en `pokemons` porque `@ManyToOne`
siempre pone la FK en el lado "muchos".

## Manejando errores

Para cerrar el círculo del controlador, es recomendable manejar los errores. Una
solución robusta es usar un handler que convierta las excepciones en respuestas
HTTP limpias.

```kotlin
// GlobalExceptionHandler.kt
@RestControllerAdvice
class GlobalExceptionHandler {

    data class ErrorResponse(
        val status: Int,
        val message: String,
        val timestamp: LocalDateTime = LocalDateTime.now()
    )

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = 404,
            message = ex.message ?: "Recurso no encontrado"
        )
        return ResponseEntity.status(404).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        // Recoge todos los errores de validación en un solo mensaje
        val messages = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }

        val error = ErrorResponse(
            status = 400,
            message = messages
        )
        return ResponseEntity.badRequest().body(error)
    }
}
```

> Esta clase complementa el controlador de entrenadores, ya que cada que se
> produce un error en el servicio, se devuelve un error HTTP.

## Preguntas frecuentes

1. **¿Por qué `LAZY` siempre en colecciones?** Imagina este escenario: tienes un
   entrenador con 500 pokemones. Si la relación se define como `EAGER`, cada vez
   que cargas al entrenador, Hibernate ejecuta una query adicional para traer
   los 500 pokemones. Aunque solo quieras saber el nombre del entrenador. En
   cambio, con `LAZY`, los pokemones solo serán cargados cuando accedes de forma
   explícita a `trainer.pokemones`.
2. **¿Por qué los métodos helper?** Cuando la relación es bidireccional, JPA
   necesita que ambos lados de la relación estén sincronizados en la memoria. Si
   haces una llamada a `trainer.pokemons.add(pokemon)` y luego
   `pokemon.trainer = trainer`, puedes tener un estado inconsistente en la base
   de datos dentro de la sesión de Hibernate. Los helpers garantizan que siempre
   estén en sintonía.
3. **¿Por qué usar DTOs y no exponer la entidad directamente?**: Si expones la
   entidad en el controlador, cualquier cambio en la base de datos rompe tu API
   pública. Además, puedes tener campos sensibles (contraseñas, datos internos)
   que nunca deberían salir en el JSON.
4. **¿Qué problemas suceden con las relaciones Bidireccionales?** Cuando
   trabajas con relaciones bidireccionales en Hibernate, tienes un desafío:
   ambos lados de la relación apuntan el uno al otro, y si no lo manejas bien,
   puedes terminar con problemas de lazy loading, ciclos infinitos en la
   serialización a JSON, o actualizaciones que no funcionan como esperabas. La
   clave está en entender que una relación siempre tiene un "dueño" (el que
   tiene la clave foránea en la base de datos) y el otro lado es simplemente una
   referencia inversa. El dueño es quien dice "yo soy responsable de guardar
   esta relación", y el otro lado dice "yo solo consulto, la relación la maneja
   el otro".

## Factos interesantes

- Todas las relaciones deben ser LAZY por defecto, especialmente si son
  colecciones.
- Usa siempre DTOs en lugar de exponer entidades.
- El lado @ManyToOne es el que tiene la FK en la base de datos. (por lo tanto
  define al dueño de la relación)
- Para relaciones bidireccionales, siempre usa los helpers.
- `@PatchMapping` es para actualizaciones parciales: en Kotlin esto es elegante
  porque puedes usar campos `nullable` en el DTO y el operador `?.let` para
  aplicar solo lo que llegó.
- `@PatchMapping` va sin `@Valid` porque los campos son opcionales.
- `PATCH` es mejor que `PUT` para APIs modernas. Es más eficiente y la
  experiencia es mejor para los clientes.
- `@Transactional(readOnly = true)` en los métodos de lectura es un detalle
  pequeño que puede mejorar mucho la performance en aplicaciones con carga alta.
- Las relaciones bidireccionales son poderosas pero requieren disciplina. Muchos
  prefieren las relaciones unidireccionales, porque es fácil olvidarse de
  mantenerlas sincronizadas. Pero las unidireccionales tienden a dejar datos
  inconsistentes en la base de datos.

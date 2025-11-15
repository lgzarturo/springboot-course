# Roadmap para aprender Java y Spring Boot desde cero hasta nivel avanzado con Kotlin y Gradle

Si estás empezando en backend y quieres un camino serio que te lleve a un nivel profesional con Java y Spring Boot, este roadmap te va a servir. Java sigue siendo un pilar en el mundo backend porque es estable, maduro y tiene una comunidad enorme. No es un lenguaje sencillo y su curva es más empinada que otras opciones, pero si sigues un proceso ordenado se vuelve mucho más manejable.

Para este camino vamos a trabajar Java y Spring Boot, pero con una curva moderna: Kotlin y Gradle. Hoy Kotlin es un lenguaje más limpio y expresivo, con compatibilidad total con el ecosistema Java. En cuanto a Gradle, es el estándar en proyectos nuevos que buscan builds más rápidos y configuraciones flexibles.

Antes de entrar al detalle, algo básico: si tu lógica no está bien afianzada, todo lo demás se te va a complicar. Lógica, algoritmos y pensamiento sistemático son el cimiento real del backend.

---

## Paso 0: Lógica y algoritmos

Antes de escribir una sola línea en Java o Kotlin, necesitas entender cómo resolver problemas con pasos claros. Ese es el objetivo de esta etapa.

Puntos a cubrir:

* Variables y flujo lógico
* Condicionales y ciclos
* Resolución de problemas
* Algoritmos básicos (búsqueda, ordenamiento)
* Pensamiento computacional

Con esto te será más fácil asimilar lo que hace especial a Java y su ecosistema.

---

## Paso 1: Elige tu IDE

Vas a pasar horas aquí, así que usa uno en el que estés cómodo.

Recomendados:

* IntelliJ IDEA (el estándar para Java y Kotlin)
* Eclipse
* NetBeans
* VS Code (con plugins)

Elige uno y aprende sus atajos, cómo correr pruebas, configurar plugins y navegar código. Como backend dev, tu velocidad depende de esto más de lo que crees.

---

## Paso 2: Fundamentos de Java SE

Aquí trabajas los bloques esenciales del lenguaje. Incluso si luego te mueves fuerte hacia Kotlin, Java te da la base conceptual que necesitas para dominar el ecosistema Spring.

Temas clave:

* Tipos de datos, control de flujo y ciclos
* Arrays, listas y colecciones
* Manejo de errores y excepciones
* Clases, métodos y scopes
* Introducción al modelo de capas

La idea es que puedas leer y entender código Java sin trabarte.

---

## Paso 3: Bases de datos relacionales

Todo backend serio toca bases de datos. Aprender SQL te va a acompañar durante toda tu carrera.

Temas clave:

* DER y normalización básica
* DDL y DML
* Consultas SQL
* Joins, subconsultas y agregaciones
* Introducción a transacciones

Mi recomendación: empieza con MySQL o PostgreSQL. Ambos son estándar en el mercado.

---

## Paso 4: Programación Orientada a Objetos

La base del ecosistema Java. Aquí afinas la manera en la que estructuras tus aplicaciones.

Temas clave:

* Clases, objetos y métodos
* Abstracción, encapsulamiento
* Herencia y polimorfismo
* Interfaces
* Excepciones
* Colecciones modernas
* ORM y JPA desde cero (CRUD, entidades, relaciones, cascadas)

Este paso es vital. Si no dominas POO y JPA, Spring Boot te va a parecer un caos.

---

## Paso 5: Java Web (Java EE)

No tienes que ser experto en Java EE para trabajar con Spring, pero entender los fundamentos web en Java es importante. Esto te da contexto para lo que Spring resuelve por ti.

Temas clave:

* HTTP y request-response
* Cliente servidor
* Servlets y JSP
* Qué es una API
* Servidores web

Con esto entiendes qué abstrae Spring Boot y por qué.

---

## Paso 6: Spring Boot (con Kotlin y Gradle)

Aquí empieza lo bueno. Spring Boot es el corazón del backend moderno en Java. En este curso la base será Kotlin + Spring Boot + Gradle.

Temas clave:

* Arquitectura de Spring
* Controladores y API REST
* Manejo de rutas y endpoints
* Beans, IoC y DI
* Spring Data JPA
* Validación con Jakarta Validation
* Manejo global de errores
* DTOs y mapeadores
* Arquitectura en capas
* Configuración con Gradle
* Profiles, environments y properties

En esta etapa ya deberías construir un monolito funcional: usuarios, roles, autenticación básica, operaciones CRUD y manejo robusto de errores.

---

## Paso 7: Microservicios

Una vez que dominas el monolito, das el salto a sistemas distribuidos. Aquí aprendes a separar servicios y a manejar problemas que aparecen solo en esa arquitectura.

Temas clave:

* Monolito vs microservicios
* Spring Cloud
* Discovery con Eureka
* Config Server
* Feign Clients
* Balanceo de carga
* Circuit breaker (Resilience4j)
* Comunicación asíncrona (RabbitMQ o Kafka)
* Gateways y seguridad distribuida

Aquí empiezas a trabajar con patrones como API Gateway, saga, event sourcing y mensajería.

---

## Paso 8: Complementos esenciales para backend

Esto te da herramientas para trabajar con código limpio y mantenible.

Temas clave:

* Lambdas y Streams
* Conceptos funcionales en Kotlin
* Coroutines en Kotlin (importantísimo)
* Hilos, concurrencia y paralelismo
* Generics
* Patrones de diseño útiles en backend

    * Factory
    * Singleton controlado
    * Strategy
    * Observer
    * Builder
* Clean Architecture y principios SOLID

Con esto tu código pasa de funcionar a ser profesional.

---

## Paso 9: Seguridad y Testing

Sin seguridad y pruebas tu servicio no está listo para producción. Este paso es obligatorio.

Temas clave:

* Spring Security moderno
* JWT
* OAuth2
* Keycloak como IAM
* Autorización basada en roles y permisos
* Seguridad en APIs públicas y privadas
* Test unitarios con JUnit 5
* Tests de integración con Spring Boot Test
* Mockito y test doubles
* TestContainers (indispensable para backend actual)

Si no pruebas tu API con bases de datos reales usando TestContainers, no estás probando de verdad.

---

## Paso 10: Herramientas que todo backend debe manejar

Aquí afinas tu flujo de trabajo y te preparas para equipos reales.

* Git y GitHub
* Docker y contenedores
* Docker Compose para entornos locales
* Swagger o SpringDoc
* CI y CD (GitHub Actions, GitLab CI)
* UML
* Terminal Linux
* SonarLint y herramientas de calidad
* SCRUM y manejo de sprints

---

## Extra: Lo que debe dominar un backend fuerte con Spring Boot, Kotlin y Gradle

Para moverte sin miedo en proyectos reales, deberías ser capaz de:

* Construir un monolito completo, listo para producción
* Desplegarlo en contenedores
* Conectar múltiples servicios mediante mensajería o REST
* Diseñar entidades y relaciones sólidas con JPA
* Implementar seguridad sólida y escalable
* Escribir pruebas que cubran tu lógica crítica
* Manejar concurrencia y procesos asíncronos con coroutines
* Documentar APIs con claridad
* Usar Gradle para manejar builds, perfiles y dependencias con confianza
* Entender cómo funciona Spring por dentro, no solo “qué botones apretar”

> A continuación puedes encontrar dos roadmaps uno enfocado en [tecnologías Java](04-roadmap-java.md) y otro en [Spring Boot](05-roadmap-springboot.md).

---

## Proyecto con Spring Boot + Kotlin + Gradle

### Entidades del Sistema de Gestión Hotelera

Es necesario crear las entidades necesarias para el proyecto. A continuación se define el listado de entidades que se deben crear, así como sus atributos y relaciones.

Siguiendo el camino del proyecto, podemos confirmar que ya tenemos la estructura implementada con la arquitectura Hexagonal, y se estableció el flujo del desarrollo TDD, se documentaron los principios SOLID y se establecieron las pruebas unitarias. En este punto, ya podemos empezar a desarrollar las funcionalidades, tenemos una base sólida de código, así como el manejo de errores y pruebas que nos permiten avanzar con confianza.

Estamos en la semana 3, y ahora toca avanzar con las entidades, sus relaciones y sus atributos. Terminando la persistencia de los datos se creará un release con la [versión 0.0.2 del proyecto](https://github.com/lgzarturo/springboot-course/releases/tag/v0.0.2).

Acorde a la narrativa del curso, se deben crear las entidades necesarias para el proyecto, que en este caso son:

#### Entidades Principales

1. **User (Entrenador/Usuario)**
   - **Propósito**: Representar a los entrenadores Pokémon que se hospedan en el hotel.
   - **Atributos principales**:
     - id: Long
     - username: String
     - email: String
     - password: String (hasheado)
     - firstName: String
     - lastName: String
     - phoneNumber: String?
     - role: UserRole (ENUM: GUEST, STAFF, ADMIN)
     - createdAt: LocalDateTime
     - updatedAt: LocalDateTime

2. **Room (Habitación)**
   - **Propósito**: Habitaciones temáticas del hotel.
   - **Atributos principales**:
     - id: Long
     - roomNumber: String
     - roomType: RoomType (ENUM)
     - pokemonTheme: PokemonTheme (ENUM: FIRE, WATER, GRASS, ELECTRIC, etc.)
     - capacity: Int
     - pricePerNight: BigDecimal
     - description: String?
     - amenities: String (JSON o lista)
     - status: RoomStatus (ENUM: AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED)
     - createdAt: LocalDateTime
     - updatedAt: LocalDateTime

3. **Reservation (Reserva)**
   - **Propósito**: Gestionar las reservas de habitaciones.
   - **Atributos principales**:
     - id: Long
     - userId: Long
     - roomId: Long
     - checkInDate: LocalDate
     - checkOutDate: LocalDate
     - numberOfGuests: Int
     - totalPrice: BigDecimal
     - status: ReservationStatus (ENUM: PENDING, CONFIRMED, CANCELLED, COMPLETED, IN_PROGRESS)
     - specialRequests: String?
     - createdAt: LocalDateTime
     - updatedAt: LocalDateTime
     - cancelledAt: LocalDateTime?

4. **Payment (Pago)**
   - **Propósito**: Gestionar los pagos de las reservas.
   - **Atributos principales**:
     - id: Long
     - reservationId: Long
     - amount: BigDecimal
     - paymentMethod: PaymentMethod (ENUM: CREDIT_CARD, DEBIT_CARD, CASH, TRANSFER)
     - paymentStatus: PaymentStatus (ENUM: PENDING, COMPLETED, FAILED, REFUNDED)
     - transactionId: String?
     - paymentDate: LocalDateTime?
     - createdAt: LocalDateTime
     - updatedAt: LocalDateTime

5. **Pokemon (Pokémon Acompañante)** *(Opcional/Avanzado)*
   - **Propósito**: Registrar los Pokémon que acompañan a los entrenadores.
   - **Atributos principales**:
     - id: Long
     - userId: Long
     - name: String
     - species: String
     - type: PokemonType (ENUM)
     - level: Int?
     - specialNeeds: String?
     - createdAt: LocalDateTime

6. **Review (Reseña)** *(Opcional/Avanzado)*
   - **Propósito**: Permitir que los huéspedes dejen reseñas.
   - **Atributos principales**:
     - id: Long
     - userId: Long
     - reservationId: Long
     - rating: Int (1-5)
     - comment: String?
     - createdAt: LocalDateTime
     - updatedAt: LocalDateTime

7. **Service (Servicio Adicional)** *(Opcional/Avanzado)*
   - **Propósito**: Servicios adicionales del hotel (spa, restaurante, etc.).
   - **Atributos principales**:
     - id: Long
     - name: String
     - description: String?
     - price: BigDecimal
     - serviceType: ServiceType (ENUM: SPA, RESTAURANT, LAUNDRY, TOUR, etc.)
     - available: Boolean

8. **ReservationService (Relación M2M)** *(Opcional/Avanzado)*
   - **Propósito**: Vincular servicios adicionales con reservas.
   - **Atributos principales**:
     - id: Long
     - reservationId: Long
     - serviceId: Long
     - quantity: Int
     - totalPrice: BigDecimal
     - scheduledAt: LocalDateTime?

#### Relaciones entre Entidades

1. **User → Reservation** (`@OneToMany`)
   - Un usuario puede tener múltiples reservas.
   - Una reserva pertenece a un usuario.

2. **Room → Reservation** (`@OneToMany`)
   - Una habitación puede tener múltiples reservas (en diferentes fechas).
   - Una reserva es para una habitación específica.

3. **Reservation → Payment** (`@OneToOne`)
   - Una reserva tiene un pago asociado.
   - Un pago pertenece a una reserva.

4. **User → Pokemon** (`@OneToMany`) *(Opcional)*
   - Un usuario puede registrar múltiples Pokémon.
   - Un Pokémon pertenece a un usuario.

5. **Reservation → Review** (`@OneToOne`) *(Opcional)*
   - Una reserva puede tener una reseña.
   - Una reseña pertenece a una reserva.

6. **Reservation ↔ Service** (`@ManyToMany`) *(Opcional)*
   - Una reserva puede incluir múltiples servicios.
   - Un servicio puede estar en múltiples reservas.
   - Tabla intermedia: `ReservationService`

#### Enumeraciones (ENUMs)

```kotlin
enum class UserRole { GUEST, STAFF, ADMIN }
enum class RoomStatus { AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED }
enum class PokemonTheme {
    FIRE, WATER, GRASS, ELECTRIC, PSYCHIC,
    DRAGON, FAIRY, GHOST, NORMAL
}
enum class RoomType {
    STANDARD, DELUXE, SUITE, PRESIDENTIAL
}
enum class ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED, IN_PROGRESS
}
enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
}
enum class PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, CASH, TRANSFER, POKEDOLLARS
}
enum class PokemonType {
    FIRE, WATER, GRASS, ELECTRIC, ICE,
    FIGHTING, POISON, GROUND, FLYING, PSYCHIC,
    BUG, ROCK, GHOST, DRAGON, DARK,
    STEEL, FAIRY, NORMAL
}
enum class ServiceType {
    SPA, RESTAURANT, LAUNDRY, TOUR,
    POKEMON_CARE, BATTLE_ARENA
}
```

#### Diagrama UML de Entidades

![Diagrama UML de Entidades](../../resources/images/21-diagrama-entidades-uml.png)

#### Consideraciones de Arquitectura

- Seguir **Arquitectura Hexagonal** para todas las entidades.
- Implementar **TDD** en cada caso (Red → Green → Refactor).
- Usar **DTOs** separados para Request/Response.
- Implementar **validaciones con Jakarta Validation**.
- Manejar errores con **GlobalExceptionHandler**.
- Documentar con **OpenAPI/Swagger**.
- Proteger endpoints con **Spring Security + JWT**.
- Configurar perfiles para **dev, prod, test**.
- Usar **Docker** para despliegue.
- Integrar herramientas de calidad como **Detekt, KTLint, JaCoCo**.
- Usar **Gitflow** para el flujo de trabajo.
- Documentar el proceso en **docs/course/**.

Con este roadmap detallado y el esquema de entidades para el sistema de gestión hotelera, estás bien encaminado para convertirte en un desarrollador backend competente con Java Spring Boot. Recuerda que la práctica constante y la construcción de proyectos reales son clave para consolidar tus habilidades. ¡Mucho éxito en tu viaje de aprendizaje!

---

## Especialización Backend Java

A continuación te presento el roadmap ampliado y accionable que complementa las secciones anteriores. Está orientado a que avances de forma progresiva hasta desempeñarse como desarrolladores backend en Java con Spring Boot.

Para cada paso encontrarás objetivos, competencias clave y entregables sugeridos que podrás usar como guía práctica.

### Paso 0: Lógica y algoritmos (Fundamentos)

- Objetivo: Desarrollar pensamiento lógico y capacidad de resolver problemas.
- Competencias:
  - Estructuras de control (if/else, bucles).
  - Estructuras de datos (listas, pilas/colas, mapas).
  - Complejidad temporal/espacial básica.
- Entregables:
  - Katas de programación (FizzBuzz, Roman numerals, bowling, etc.).
  - Ejercicios en plataformas como HackerRank/CodeWars.

### Paso 1: IDE y productividad

- Objetivo: Dominar un IDE profesional.
- Competencias:
  - Atajos de teclado, refactors, depuración (breakpoints, watches).
  - Integración con Git, ejecución de pruebas, plugins útiles (LSP, linters).
- Entregables:
  - Proyecto simple con depuración paso a paso, configuración de perfiles de ejecución.

### Paso 2: Java SE (Core Java)

- Objetivo: Comprender el lenguaje y su ecosistema estándar.
- Competencias:
  - Tipos, colecciones, excepciones, genéricos, lambdas/streams, records (Java 16+), módulos (JPMS, opcional).
  - I/O, NIO, fechas (`java.time`), concurrencia básica (`CompletableFuture`).
- Entregables:
  - Librería utilitaria con tests y ejemplos de colecciones, streams y excepciones.

### Paso 3: Bases de Datos Relacionales (SQL)

- Objetivo: Modelar datos y consultar eficientemente.
- Competencias:
  - Normalización, claves primarias/foráneas, índices.
  - DDL/DML, joins, subconsultas, vistas, transacciones.
- Entregables:
  - DER + script SQL (migraciones con Flyway/Liquibase).
  - Consultas parametrizadas y reporte de ejecución.

### Paso 4: Programación Orientada a Objetos (POO) y Persistencia (JPA)

- Objetivo: Aplicar POO con principios SOLID y persistencia con ORM.
- Competencias:
  - Encapsulamiento, herencia, composición, polimorfismo.
  - Mapeo ORM con JPA/Hibernate, ciclos de vida de entidades.
- Entregables:
  - Módulo de dominio con modelos puros + repositorios (puertos) + adaptadores JPA.

### Paso 5: Java Web (HTTP, APIs y componentes EE)

- Objetivo: Entender el modelo cliente/servidor y contratos HTTP.
- Competencias:
  - Métodos y códigos de estado, cabeceras, content negotiation.
  - Servlets/JSP (histórico), filtros, interceptores.
- Entregables:
  - Mini API con endpoints CRUD simples y documentación de contrato.

### Paso 6: Spring Boot (API REST y arquitectura)

- Objetivo: Construir APIs robustas y mantenibles.
- Competencias:
  - Inversión de control, inyección de dependencias, perfiles.
  - Spring MVC, validación (Jakarta Validation), manejo global de excepciones.
  - Arquitectura hexagonal (Ports & Adapters), DTOs y mappers.
- Entregables:
  - API con controladores, servicios y adaptadores de persistencia.
  - Documentación OpenAPI/Swagger y Actuator habilitado.

### Paso 7: Microservicios (cuando aplique)

- Objetivo: Diseñar sistemas distribuidos escalables.
- Competencias:
  - Comunicación síncrona/asíncrona, descubrimiento de servicios, resiliencia (circuit breaker, retries, timeouts).
  - Spring Cloud, configuración centralizada, gateway, tracing.
- Entregables:
  - Dos servicios colaborando (por ejemplo, reservas y pagos) con Feign y resiliencia básica.

### Paso 8: Complementos Java

- Objetivo: Profundizar en herramientas de productividad y diseño.
- Competencias:
  - Lambdas/Streams avanzados, concurrencia, patrones de diseño.
  - Clean Code, refactorización dirigida por pruebas.
- Entregables:
  - Refactor de un módulo aplicando patrones (Strategy, Factory, Template Method, etc.).

### Paso 9: Seguridad y Testing (ampliado)

- Objetivo: Incorporar seguridad y una estrategia de pruebas sólida.
- Competencias:
  - Spring Security (autenticación/autorización), JWT, OAuth2/OIDC.
  - Pirámide de pruebas: unitarias, integración, end‑to‑end.
- Entregables:
  - Endpoints protegidos + batería de pruebas (unitarias e integración) con cobertura reportada.

### Paso 10: Herramientas y prácticas profesionales

- Objetivo: Trabajar como en un equipo real.
- Competencias:
  - Git/GitHub (branching, PR, code review), CI/CD, Docker, documentación viva.
  - Scrum/Kanban, calidad de código (KTLint/Checkstyle, Detekt/Sonar), observabilidad.
- Entregables:
  - Pipeline CI que ejecute tests, linters y genere reportes (JaCoCo, changelog y release semántico).

> Sugerencia de práctica guiada: Sigue la narrativa y ejemplos del proyecto Hotel Pokémon en este repositorio. En la semana 3 definiste entidades y persistencia (ver [02-entidades.md](02-entidades.md)) y aplicaste TDD para un CRUD de ejemplo (ver [01-crud-con-tdd.md](01-crud-con-tdd.md)). El siguiente hito es cerrar la versión v0.0.2 con persistencia y pruebas básicas funcionando.

---

## Guía de Pruebas en Spring Boot (Unitarias, Integración y E2E)

### Introducción

En el desarrollo de software, las pruebas unitarias y de integración son esenciales para garantizar la calidad, la fiabilidad y el mantenimiento del código. En proyectos de Spring Boot, seguir buenas prácticas y estándares específicos es crucial para asegurar que las pruebas sean efectivas y eficientes.

### Ventajas de Realizar Pruebas de Código

1. Aseguramiento de la Calidad
   - Confianza en el Código: Las pruebas de código aseguran que el software funciona como se espera, lo que genera confianza tanto en los desarrolladores como en los clientes.
   - Detección Temprana de Errores: Permiten identificar y corregir errores en etapas tempranas del desarrollo, lo que reduce costos y tiempo de corrección.
   - Recuerda: agregar pruebas, ya sean unitarias o de integración, es una responsabilidad y una obligación profesional. Como dijo Kent Beck, padre del TDD: “El código limpio que funciona crea confianza en su desarrollo”. Escribir pruebas no solo asegura la calidad de nuestro trabajo, también facilita el mantenimiento y futuras actualizaciones.
2. Facilitación del Mantenimiento y Actualización
   - Mantenimiento Simplificado: Con pruebas automatizadas, es más fácil realizar cambios sin temor a romper funcionalidades existentes.
   - Actualización Segura: Facilitan la actualización de dependencias y versiones (Java/Spring) al asegurar que los cambios no introducen errores.
3. Documentación del Código
   - Comprensión del Proyecto: Las pruebas actúan como documentación viva del comportamiento esperado.
   - Colaboración: Proveen contexto claro para que otros desarrolladores se integren más rápido.
4. Fomento de Buenas Prácticas
   - Mejor Diseño: Obliga a pensar en la funcionalidad y el diseño antes de implementar, resultando en código más robusto y eficiente.

### Principios F.I.R.S.T. de las Pruebas Automatizadas

1. Fast (rápidas): Ejecución veloz para fomentar el feedback continuo.
2. Isolated (aisladas): Independientes entre sí y del orden de ejecución.
3. Repeatable (repetibles): Mismo resultado sin importar el entorno.
4. Self‑validating (auto‑validadas): Pasan o fallan sin verificación manual.
5. Timely (oportunas): Escritas a tiempo, idealmente antes o junto con el código (TDD).

---

### Pruebas Unitarias en Spring Boot

¿Qué son? Verifican el comportamiento de una unidad (clase/método) en aislamiento.

Buenas prácticas
1) Aislamiento
   - Mocking: Usa bibliotecas para simular dependencias externas. En Java es común Mockito; en Kotlin, MockK. También existe `mockito-kotlin` si programas en Kotlin pero prefieres Mockito.
   - Inyección de Dependencias: Aprovecha la DI de Spring en capas superiores; en unit tests del dominio no cargues Spring.
2) Simplicidad y Claridad
   - Un solo propósito por prueba y nombres descriptivos.
3) Cobertura de Código
   - Cubre casos felices y de error, incluidos límites y excepciones.
   - Mide con JaCoCo y prioriza áreas críticas (reglas de negocio).
4) Rapidez
   - Evita levantar el contexto de Spring en unit tests. Ejecuta en paralelo cuando sea posible.

Ejemplo (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
  @Mock PaymentRepository repo;
  @InjectMocks PaymentService service;

  @Test
  void shouldCreatePayment() {
    var payment = new Payment(null, new BigDecimal("10.00"));
    when(repo.save(any())).thenReturn(payment.withId(1L));

    var result = service.create(payment);

    assertThat(result.getId()).isEqualTo(1L);
    verify(repo).save(any());
  }
}
```

Ejemplo (Kotlin + MockK)

```kotlin
class PaymentServiceTest {
    private val repo = mockk<PaymentRepository>()
    private val service = PaymentService(repo)

    @Test
    fun `should create payment`() {
        val payment = Payment(id = null, amount = BigDecimal("10.00"))
        every { repo.save(any()) } returns payment.copy(id = 1)

        val result = service.create(payment)

        assertEquals(1, result.id)
        verify(exactly = 1) { repo.save(any()) }
    }
}
```

Dependencias de prueba (Gradle)

```kotlin
dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    // Java
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    // Kotlin
    testImplementation("io.mockk:mockk")
    // Cobertura e integración
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
```

---

### Pruebas de Integración en Spring Boot

¿Qué son? Verifican la interacción entre componentes (controladores, servicios, repositorios) y con recursos reales como bases de datos.

Buenas prácticas
1) Entorno de Pruebas Realista
   - Usa H2 en memoria o Testcontainers para bases reales (PostgreSQL, MySQL).
   - Perfiles de prueba: `@ActiveProfiles("test")` para aislar configuración.
2) Datos de Prueba
   - Inicializa datos controlados (scripts `data.sql` o `@Sql`) o builders de objetos.
3) Funcionalidad Completa
   - Prueba endpoints con MockMvc o RestAssured.
   - Valida transacciones y comportamiento en escenarios reales.
4) Automatización y CI
   - Ejecuta en el pipeline CI/CD y monitorea fallos con reportes.

Ejemplo con `@WebMvcTest` + MockMvc

```kotlin
@WebMvcTest(ExampleController::class)
class ExampleControllerIT(@Autowired val mockMvc: MockMvc) {
    @MockBean lateinit var service: ExampleUseCase

    @Test
    fun `should return 201 when creating`() {
        whenever(service.create(any())).thenReturn(Example(1, "Test", "Desc"))

        mockMvc.perform(
            post("/api/v1/examples")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""{"name":"Test","description":"Desc"}""")
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.id").value(1))
    }
}
```

Ejemplo con Testcontainers (PostgreSQL)

```kotlin
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class DatabaseIntegrationTest {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("app")
            .withUsername("postgres")
            .withPassword("postgres")
    }

    @Test
    fun `context loads and connects to DB`() {
        // Ejecuta casos que interactúan con la base de datos real en contenedor
    }
}
```

RestAssured para probar APIs REST

```java
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Test
void shouldCreateReservation() {
  given()
    .contentType("application/json")
    .body("{\"roomId\":1,\"userId\":1,\"checkInDate\":\"2025-11-20\"}")
  .when()
    .post("/api/v1/reservations")
  .then()
    .statusCode(201)
    .body("id", notNullValue());
}
```

> Nota: En este repositorio ya encontrarás ejemplos reales en [01-crud-con-tdd.md](01-crud-con-tdd.md) y tests de controladores/servicios en `src/test/`.

---

### Tipos de Pruebas dentro de Integración
- Pruebas de componentes: enfocadas a un controlador/servicio aislado del resto usando mocks/`@WebMvcTest`.
- Pruebas de acceso a datos: `@DataJpaTest` para validar consultas personalizadas y mapping de entidades.
- Pruebas end‑to‑end (E2E): cubren todo el flujo, incluyendo sistemas externos, típicamente con Selenium (UI), Cucumber (BDD) o RestAssured (APIs).

---

## Bibliotecas Recomendadas

### Pruebas Unitarias

1. JUnit
   - Anotaciones: `@Test`, `@BeforeEach`, `@AfterEach`, parametrizadas.
   - Integración con Maven/Gradle y la mayoría de IDEs.
2. Mockito (Java) / MockK (Kotlin)
   - Creación de mocks y stubs.
   - Verificación de interacciones.
3. AssertJ / Kotest Assertions
   - Aserciones fluidas y legibles.
4. Spring Test
   - Soporte para contextos de Spring cuando realmente sea necesario.

### Pruebas de Integración

1. Spring Boot Test
   - `@SpringBootTest` para cargar el contexto completo.
   - `@MockBean` / `@SpyBean` para inyectar mocks en el contexto.
2. Testcontainers
   - Arranque de servicios externos en Docker (bases, colas, etc.).

### Pruebas End‑to‑End

1. Selenium
   - Automatización de UI web (multi‑navegador).
2. Cucumber (BDD)
   - Casos de prueba en Gherkin, ejecutables.
3. RestAssured
   - Testing fluido de APIs REST.

---

## Estrategia de Testing para este Proyecto

- Unitarias (rápidas): servicios de dominio y funciones puras sin cargar Spring.
- Integración (selectivas): controladores con MockMvc, adaptadores de persistencia, mapeos y excepciones.
- E2E (futuro): flujos de negocio clave.
- Cobertura: utiliza JaCoCo; apunta a cobertura significativa en lógica de negocio (>70% de líneas es un buen inicio), priorizando ramas críticas. Revisa la sección “¿Qué probar y qué no?” en [01-crud-con-tdd.md](01-crud-con-tdd.md).
- Automatización: integra tests y linters en GitHub Actions (ver [WORKFLOW.md](../../../WORKFLOW.md)).

## Checklist Rápido para tus PR

- [ ] Tests unitarios para la lógica nueva o modificada.
- [ ] Tests de integración para endpoints y repositorios con lógica no trivial.
- [ ] Reporte JaCoCo sin caídas drásticas de cobertura.
- [ ] Mensajes de commit convencionales y documentación actualizada.

Con esta guía, tu roadmap no solo te dice qué aprender, sino también cómo validar que lo aprendido funciona y es mantenible en el tiempo. ¡Sigue iterando con TDD y buenas prácticas!

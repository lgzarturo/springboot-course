# Guía Narrativa: De Novato a Maestro en Spring Boot

## La Historia de Kai y el Hotel Pokémon

---

## ¿Por qué este curso existe?

Internet está lleno de tutoriales que te enseñan a hacer un "Hello World" con
Spring Boot. Este curso no es uno de ellos.

La mayoría de los tutoriales te dejan con una aplicación que funciona en tu
máquina, pero que nunca sobreviviría a producción real: sin seguridad, sin tests,
sin observabilidad, sin criterio arquitectónico. Aprendes a copiar anotaciones,
no a razonar sobre un sistema.

**Este curso parte de un principio diferente:** la mejor forma de aprender es
construir algo real, con restricciones reales, tomando decisiones reales.

Por eso construimos el **Hotel Pokémon** — un sistema de gestión hotelera
temático donde los entrenadores reservan habitaciones, registran a sus Pokémon
compañeros y contratan servicios. Es un dominio lo suficientemente familiar para
no perderse en la lógica de negocio, y lo suficientemente complejo para
justificar todas las herramientas que aprenderemos.

---

## Kai, el Entrenador

Kai es un desarrollador junior con ganas de convertirse en un ingeniero de
software completo. Sabe programar lo básico — ha visto videos, hecho algún
curso, quizás construido algún CRUD. Pero cuando intenta entrar a un equipo
profesional, se topa con términos que no entienden del todo: "¿por qué usamos
Hexagonal si solo tenemos un repositorio?", "¿cómo funciona un JWT realmente?",
"¿qué es un test de integración vs. un unit test?".

Kai no necesita más teoría. Necesita **contexto**: problemas reales donde esas
herramientas cobran sentido.

La historia de Kai es la historia de cualquier desarrollador que ha sentido que
hay un salto enorme entre "saber usar Spring Boot" y "ser un buen ingeniero con
Spring Boot". Este curso cierra ese salto.

---

## ¿Por qué un Hotel? ¿Por qué Pokémon?

### El dominio importa

Un sistema hotelero tiene exactamente la complejidad correcta para aprender
desarrollo backend serio:

- **Entidades con relaciones reales**: Usuarios, Habitaciones, Reservas,
  Servicios, Pokemon's, etc. Hay relaciones Many-to-One, One-to-Many, con
  atributos propios.
- **Lógica de negocio no trivial**: No se puede reservar una habitación ocupada.
  Las fechas no pueden solaparse. El precio total se calcula con reglas.
- **Múltiples roles de usuario**: Un TRAINER (huésped) no puede ver las reservas
  de otro. Un GYM_LEADER (staff) puede cambiar el estado de las habitaciones. El
  PROFESOR_OAK (admin) tiene acceso total.
- **Necesidad de observabilidad**: Cuando algo falla en la reserva del cliente
  #1847, el equipo de soporte necesita saber exactamente qué pasó y cuándo.

### El tema lúdico tiene un propósito pedagógico

La narrativa Pokémon no es decoración. Tiene tres funciones concretas:

1. **Hace memorable lo abstracto.** "El Gimnasio de Vermilion te enseña
   seguridad" se recuerda mejor que "Fase 2: Autenticación y Autorización". El
   cerebro retiene información anclada a historias.

2. **Reduce la fricción de aprender.** Cuando el ejercicio dice "Team Rocket
   intentó entrar al sistema robando contraseñas, Lt. Surge exige JWT", el
   problema técnico tiene contexto emocional. No es un requerimiento abstracto,
   es una amenaza concreta con nombre.

3. **Crea hitos de progreso tangibles.** Cada Gimnasio es una insignia ganada.
   Eso importa psicológicamente cuando el camino es largo.

Los roles del sistema (`TRAINER`, `GYM_LEADER`, `PROFESOR_OAK`) no son
nombres caprichosos: reflejan niveles de acceso reales que tendrías en cualquier
aplicación multirol. El tema es Pokémon, la arquitectura es la misma que usarías
en cualquier otro proyecto.

---

## La Ruta de los Seis Gimnasios

El curso está organizado en seis fases, cada una dominada por un Líder de
Gimnasio. Cada fase corresponde a un dominio técnico que todo ingeniero backend
de nivel medio-senior debe dominar.

```
Kai (novato)
    │
    ▼
💎 PEWTER     → Persistencia con JPA y Flyway         
    │
    ▼
⚡ VERMILION  → Seguridad: JWT, OAuth2, RBAC          
    │
    ▼
🌊 CERULEAN   → Validaciones y manejo de errores       
    │
    ▼
🌱 CELADON    → Testing: Unit, Integration, E2E        
    │
    ▼
🌋 CINNABAR   → Observabilidad: Logs, Métricas, Trazas 
    │
    ▼
🌍 VIRIDIAN   → CI/CD, Docker, Análisis Estático        
    │
    ▼
🏆 LIGA POKÉMON → Proyecto integrador completo          
```

**Total estimado: ~11 semanas full-time**

---

## Los Seis Gimnasios en Detalle

### 💎 Gimnasio 1 — Pewter: Los Cimientos de la Persistencia

**Líder:** Brock | **Insignia:** Boulder Badge

> _"Un sistema sin cimientos sólidos colapsa bajo su propio peso. Los datos son
> la roca sobre la que todo se construye."_

Brock no es el líder más glamoroso, pero es el más fundamental. Antes de
construir cualquier otra cosa, el Hotel Pokémon necesita guardar datos
correctamente.

**Qué aprende Kai:**

- Mapeo de entidades JPA con Kotlin: `@Entity`, `@Column`, `@GeneratedValue`
- Relaciones reales: `@OneToMany`, `@ManyToOne`, `@ManyToMany` con tabla
  intermedia
- Por qué `FetchType.LAZY` es tu amigo y `FetchType.EAGER` es una trampa
  (el "N+1 Query of Doom" de Team Rocket)
- Migraciones con Flyway: versionado de esquemas, reproducibilidad, seed data
- Spring Data JPA: queries derivadas, JPQL, queries nativas para casos complejos
- Patrón Port-Adapter para persistencia en arquitectura hexagonal
- Optimistic locking con `@Version` para concurrencia en reservas simultáneas

**Por qué importa el orden:**
Sin persistencia no hay nada. Todas las fases siguientes necesitan datos.
Empezar aquí también obliga a pensar el modelo de dominio antes de escribir
controllers — el orden correcto de razonamiento.

**El primer desafío real:** detectar solapamiento de fechas en reservas usando
una query SQL que pregunta si dos intervalos se intersectan. Es el tipo de
problema donde `SELECT * FROM reservations WHERE room_id = ?` no alcanza.

---

### ⚡ Gimnasio 2 — Vermilion: El Escudo Eléctrico

**Líder:** Lt. Surge | **Insignia:** Thunder Badge

> _"La seguridad no es una capa adicional que se añade al final. Es un aspecto
> integral del sistema desde el primer commit."_

Team Rocket ha intentado entrar al sistema. Un entrenador mal intencionado está
viendo las reservas de otros huéspedes. Es hora de blindar el Hotel Pokémon.

**Qué aprende Kai:**

- Configuración de Spring Security con Kotlin DSL
- Autenticación stateless con JWT: cómo funciona el token, qué va en el
  payload, qué nunca debe ir ahí
- Access tokens (24h) + Refresh tokens (7 días con rotación) almacenados en BD
- Autorización basada en roles con `@PreAuthorize` y Spring Expression Language
- Seguridad a nivel de método: `@reservationSecurity.isOwner(#id, principal.id)`
- Protección contra ataques comunes: SQL injection, CSRF en APIs stateless, CORS
- OAuth2 para login con Google/GitHub (sprint avanzado)

**Por qué este orden:**
La seguridad viene antes de las validaciones y los tests porque define el
modelo de autorización. Saber quién puede hacer qué es una restricción que afecta
a todos los componentes posteriores.

**La lección más importante:** JWT no cifra los datos, solo los firma. Nunca
metas contraseñas, números de tarjeta ni datos sensibles en el payload. Kai
aprende esto haciendo exactamente ese error y viendo qué pasa en jwt.io.

---

### 🌊 Gimnasio 3 — Cerulean: La Disciplina de la Precisión

**Líder:** Misty | **Insignia:** Cascade Badge

> _"Un sistema que acepta datos inválidos es un sistema que miente sobre su
> propio estado."_

Misty llega al hotel y quiere reservar la Charizard Chamber. Pero intenta hacer
una reserva con fecha de checkout anterior al check-in. El sistema acepta la
petición sin quejarse. Ese bug ya existe en producción.

**Qué aprende Kai:**

- Bean Validation con anotaciones: `@NotBlank`, `@Email`, `@Min`, `@Max`,
  `@Future`, constraints personalizados con `@Constraint`
- Validación a nivel de clase con `@Valid` en controllers y cascada en objetos
  anidados
- Manejo estructurado de errores: `@RestControllerAdvice`,
  `@ExceptionHandler`, formato RFC 7807 (Problem Details)
- Excepciones de dominio con semántica clara: `RoomNotAvailableException`,
  `DuplicateEmailException`, `ReservationConflictException`
- Códigos HTTP correctos: la diferencia entre 400, 404, 409 y 422 no es
  arbitraria
- Transacciones con `@Transactional`: cuándo son necesarias y por qué
  `readOnly = true` importa para performance

**Por qué este orden:**
Las validaciones dependen de haber definido los DTOs y las excepciones de
dominio. Después de esta fase, Kai puede construir endpoints que no solo
funcionan, sino que comunican errores con precisión suficiente para que el
frontend los maneje correctamente.

---

### 🌱 Gimnasio 4 — Celadon: El Arte del Testing

**Líder:** Erika | **Insignia:** Rainbow Badge

> _"El código sin tests no es código terminado, es deuda técnica disfrazada de
> progreso."_

Erika tiene un jardín perfecto porque cada planta fue cuidada desde el
principio. El Hotel Pokémon tiene tests, pero son frágiles: fallan por razones
aleatorias, no cubren los casos importantes, y el equipo los ignora cuando no
pasan en CI.

**Qué aprende Kai:**

- **Unit tests:** dominio puro con MockK, sin Spring context, en <1ms por test
- **Tests de controlador:** `@WebMvcTest` + `MockMvc`, solo carga la capa web
- **Tests de repositorio:** `@DataJpaTest` con H2, verifica queries
  personalizadas
- **Tests de integración:** `BaseIntegrationTest` + Testcontainers con
  PostgreSQL real (sin el "funciona en H2, pero falla en Postgres" problem)
- **Tests E2E:** `@SpringBootTest` con `TestRestTemplate`, el sistema completo
- Cobertura con JaCoCo: qué significa el 85% mínimo y por qué no es un número
  sagrado, sino una señal de alerta
- TDD en práctica: primero el test, luego la implementación (el ejemplo
  canónico con `PingService`)
- Estrategia de pirámide de tests: por qué la mayoría deben ser unit tests

**Por qué este orden:**
Kai ya tiene un sistema con persistencia, seguridad y validaciones. Ahora
puede escribir tests significativos contra código real, no contra stubs vacíos.
Esta es también la fase donde el valor de la arquitectura hexagonal se vuelve
tangible: las capas separadas permiten tests rápidos y aislados.

**El insight clave:** Testcontainers resuelve el problema clásico de "los tests
pasan en mi máquina". Si el test usa PostgreSQL real, detecta el bug de
migración que H2 no detectaría.

---

### 🌋 Gimnasio 5 — Cinnabar: Ver para Operar

**Líder:** Blaine | **Insignia:** Volcano Badge

> _"Un sistema que no puede ser observado no puede ser operado. Solo puedes
> arreglar lo que puedes ver."_

El Hotel Pokémon está en producción. Un cliente llama diciendo que su reserva
"no funcionó", pero no hay ningún error en el sistema. ¿Qué pasó? ¿Cuándo?
¿Con qué datos? Sin observabilidad, la respuesta es: no lo sé.

**Qué aprende Kai:**

- **Logging estructurado:** logs como JSON con campos buscables (`userId`,
  `reservationId`, `traceId`) en lugar de strings planos
- **Niveles de log y cuándo usarlos:** DEBUG en desarrollo, INFO para eventos de
  negocio, WARN para situaciones recuperables, ERROR solo cuando algo realmente
  falla
- **Métricas con Micrometer + Prometheus:** contadores, gauges, histogramas.
  Ejemplos: `reservations.created.total`, `hotel.availability.rooms`,
  `api.response.time`
- **Spring Actuator:** endpoints de health, info, metrics para monitoreo
- **Distributed Tracing con OpenTelemetry:** trace IDs que permiten seguir una
  petición a través de múltiples servicios
- **Sentry para error tracking:** captura de excepciones no manejadas con
  contexto completo (stack trace, usuario, request, variables de entorno)

**Por qué este orden:**
Observabilidad es lo último en el "camino feliz" porque depende de haber
construido el sistema primero. Pero conceptualmente viene antes de CI/CD: para
saber si un deploy fue exitoso, necesitas métricas.

---

### 🌍 Gimnasio 6 — Viridian: La Automatización Total

**Líder:** Giovanni | **Insignia:** Earth Badge

> _"El código que no está en producción no entrega valor. La automatización es
> lo que hace posible deployar con confianza."_

Giovanni no es el tipo de líder que celebra héroes individuales. Celebra
sistemas que funcionan solos: pipelines que detectan bugs antes de que lleguen
a producción, análisis estático que mantiene la calidad del código sin revisión
manual exhaustiva.

**Qué aprende Kai:**

- **Docker:** contenedorizar la aplicación, Docker Compose para desarrollo local
  con PostgreSQL + app
- **GitHub Actions:** pipeline CI/CD completo
  - Build y tests en cada PR
  - Análisis estático con Detekt y KTLint
  - Reporte de cobertura con JaCoCo
  - Deploy automatizado con semantic-release
- **Análisis estático con Detekt:** reglas de calidad más allá del formateo,
  detección de code smells
- **KTLint:** formateo consistente del código Kotlin
- **Conventional Commits:** cómo un mensaje de commit genera automáticamente la
  versión del release (`feat:` → minor, `fix:` → patch)
- **Estrategia de branching:** feature branches, protección del branch main,
  pull requests como gate de calidad

**Por qué este orden:**
Esta es la fase final porque integra todo lo anterior. Un pipeline de CI/CD
que ejecuta tests de integración necesita que esos tests existan y sean
confiables. Los análisis estáticos detectan violaciones de las convenciones
establecidas en fases anteriores.

---

### 🏆 Liga Pokémon: El Proyecto Integrador

Después de ganar las seis insignias, Kai enfrenta la Liga Pokémon: construir
desde cero un módulo completo del Hotel Pokémon con todas las capas integradas.

El módulo propuesto es el **sistema de reviews**: los entrenadores que hicieron
checkout pueden dejar una reseña de 1-5 estrellas con comentario. Las reseñas
son públicas, pero solo los usuarios autenticados que tuvieron una estancia
confirmada pueden crear una.

Este módulo es pequeño, pero cubre todo el stack:

- Entidad JPA con relación a User y Reservation
- Validación de negocio (solo post-checkout)
- Endpoints REST con autorización por rol
- Unit tests + integration tests + E2E
- Logs y métricas relevantes
- Pipeline CI que lo valida automáticamente

---

## Por Dónde Empezar

### Si eres nuevo en Spring Boot

1. Clona el repositorio y levanta la aplicación siguiendo la
   [guía de configuración del entorno](../../README.md#configuración-del-entorno)
2. Explora la estructura de paquetes en `src/main/kotlin/` sin leer todo el
   código — solo observa cómo está organizado
3. Lee el
   [Plan de Migración MVC](../architecture/mvc-migration-plan.md) para entender
   qué arquitectura usamos y por qué la estructura del código es así
4. Comienza el
   [Roadmap de Ejercicios](exercises-roadmap.md) desde la Fase 1 (Gimnasio
   Pewter)
5. Usa el
   [Itinerario de Desarrollo](development-itinerary.md) como referencia de
   código cuando estés bloqueado en un ejercicio

### Si ya tienes experiencia con Spring

1. Lee el
   [Plan de Migración MVC](../architecture/mvc-migration-plan.md) para entender
   las decisiones arquitectónicas del proyecto
2. Ve directo al
   [Itinerario de Desarrollo](development-itinerary.md) para ver los ejemplos
   de código con Kotlin
3. Usa el
   [Roadmap de Ejercicios](exercises-roadmap.md) como checklist de criterios de
   aceptación para cada fase

### Si quieres repasar una fase específica

Cada gimnasio en el [Roadmap de Ejercicios](exercises-roadmap.md) es
autocontenido. Puedes saltar directamente a la fase que necesites repasar.

---

## Cómo Está Organizado el Código

El repositorio usa **MVC organizado por features** (Screaming Architecture).
La estructura grita "hotels", "users", "ping" — no "adapters", "ports" ni
"config".

```
src/main/kotlin/com/lgzarturo/springbootcourse/
├── SpringbootCourseApplication.kt
├── config/               # Infraestructura transversal (CORS, OpenAPI)
├── common/               # Componentes reutilizables (errores, paginación)
└── features/             # Cada feature es autocontenida
    ├── hotels/
    │   ├── HotelController.kt      # @RestController
    │   ├── HotelService.kt         # @Service, lógica de negocio
    │   ├── HotelRepository.kt      # @Repository, acceso a datos
    │   ├── HotelEntity.kt          # @Entity JPA
    │   ├── Hotel.kt                # Modelo de dominio puro
    │   └── dto/
    │       ├── CreateHotelRequest.kt
    │       └── HotelResponse.kt
    ├── users/
    ├── ping/
    ├── rooms/
    └── sentry/
```

**Regla de oro:** máximo 2 niveles de anidación dentro de una feature.
`hotels/dto/` es el límite. Si sientes que necesitas más niveles, el problema
es que la feature es demasiado grande.

---

## Lo Que Este Curso No Es

- **No es un tutorial de "cómo hacer un CRUD en 20 minutos".** Los CRUDs son un
  medio, no el fin.
- **No es una introducción a Kotlin.** Se asume conocimiento básico del lenguaje.
  Si vienes de Java, los primeros ejercicios te enseñarán las diferencias que
  importan en la práctica.
- **No es teoría de arquitectura pura.** Cada decisión arquitectónica se toma en
  el contexto de un problema concreto del Hotel Pokémon.

---

## El Compromiso de Kai

Al final del curso, Kai no solo sabe usar Spring Boot. Sabe:

- Por qué una decisión de arquitectura hace el código más mantenible
- Cuándo una abstracción ayuda y cuándo es ceremonia sin valor
- Cómo un test de integración detecta bugs que un unit test no puede ver
- Por qué un deploy fallido a medianoche es un problema de observabilidad,
  no solo de código
- Qué hace exactamente un JWT cuando llega a su filtro de seguridad

Eso es lo que diferencia a un desarrollador que usa herramientas de uno que las
entiende.

---

## Recursos del Proyecto

| Recurso                                                        | Contenido                                                       |
|----------------------------------------------------------------|-----------------------------------------------------------------|
| [Itinerario de Desarrollo](development-itinerary.md)           | Código de referencia por fase con ejemplos Kotlin completos     |
| [Roadmap de Ejercicios](exercises-roadmap.md)                  | Ejercicios paso a paso con criterios de aceptación verificables |
| [Plan de Migración MVC](../architecture/mvc-migration-plan.md) | Decisiones arquitectónicas y cómo evolucionó la estructura      |
| [README principal](../../README.md)                            | Comandos, variables de entorno, stack tecnológico               |

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Spring Boot + Kotlin REST API course project simulating a hotel management platform. Built with **MVC organized by feature** (Screaming Architecture), migrating away from Hexagonal Architecture. Java 21, Kotlin, Spring Boot 4.x, PostgreSQL (prod) / H2 (dev/test).

## Common Commands

Use `make <target>` for all common tasks (see `make help` for the full list):

```bash
# Build
make build          # clean + compile + assemble
make compile        # compile only (no tests, no clean)

# Run
make run            # perfil dev (default)
make run-dev        # perfil dev explícito
make run-prod       # perfil prod

# Tests
make test                              # todos los tests
make test-class CLASS=HotelServiceTest # una clase específica
# Para un método específico, usar Gradle directamente:
# gradlew test --tests "com.lgzarturo.springbootcourse.features.hotels.HotelServiceTest.methodName"
make coverage                          # tests + reporte JaCoCo (build/reports/jacoco/)
make coverage-check                    # verifica umbral del 85%

# Calidad de código
make lint           # KTLint check + Detekt (solo verifica)
make format         # KTLint format + Detekt autoCorrect (aplica correcciones)
make fix            # alias de format
make quality        # gate completo: lint + tests + cobertura

# Base de datos
make ddl                               # genera build/schema-create.sql desde entidades JPA
make create-migration VER=2 DESC="add_users"
make migrate DESC="add_users_table"    # usa el script de migración automática

# Docker
make docker-up      # levanta servicios (BD)
make docker-down    # detiene servicios
make docker-logs    # logs de contenedores

# Atajos
make ci             # simula pipeline CI completo (build + quality)
make setup          # configura entorno local (.env + docker-up)
make reset          # limpia build artifacts y detiene Docker
```

> Los comandos `make` llaman a las tareas de Gradle correspondientes. Si necesitas invocar Gradle directamente,
> usa `./gradlew <task>` en Linux/Mac o `gradlew <task>` en Windows.

## Architecture

The codebase uses **MVC organized by feature** (Screaming Architecture). Each feature is a self-contained
package grouping all its layers. The top-level structure reflects the domain, not the technical layer.

```
src/main/kotlin/com/lgzarturo/springbootcourse/
├── SpringbootCourseApplication.kt
├── config/                   # Infraestructura transversal (CORS, OpenAPI, Security)
├── common/                   # Componentes reutilizables (errores, paginación, extensiones)
└── features/                 # Cada feature es autocontenida
    ├── hotels/
    │   ├── HotelController.kt        # @RestController — capa HTTP
    │   ├── HotelService.kt           # @Service — lógica de negocio
    │   ├── HotelJpaRepository.kt     # @Repository — Spring Data JPA
    │   ├── HotelEntity.kt            # @Entity — modelo de persistencia JPA
    │   ├── Hotel.kt                  # Modelo de dominio (Kotlin puro, sin anotaciones Spring)
    │   ├── HotelServiceConfig.kt     # @Configuration beans específicos de esta feature
    │   └── dto/
    │       ├── CreateHotelRequest.kt
    │       ├── UpdateHotelRequest.kt
    │       └── HotelResponse.kt
    ├── ping/                         # Ejemplo canónico de TDD
    ├── users/                        # Gestión de usuarios con value objects
    ├── rooms/                        # Habitaciones del hotel
    └── examples/                     # Implementación de referencia con patrón hexagonal completo
```

**Regla de oro:** máximo 2 niveles de anidación dentro de una feature. `hotels/dto/` es el límite.
Si se necesitan más niveles, la feature es demasiado grande y debe dividirse.

### Key Architectural Rules

- **Controllers → Services → Repositories**: flujo MVC clásico. Los controllers no acceden al repositorio directamente.
- **Features son autocontenidas**: no hay imports cruzados entre features. La comunicación entre features
  va a través de `common/` o mediante eventos de Spring.
- **El modelo de dominio es Kotlin puro**: `Hotel.kt` no tiene `@Entity` ni dependencias de Spring.
  `HotelEntity.kt` es el objeto JPA que el servicio traduce hacia/desde el dominio.
- **DTOs no entran al servicio como DTOs**: el controller mapea Request → dominio antes de llamar al servicio.
- **La feature `examples/`** mantiene el patrón hexagonal completo (puertos, adaptadores) como referencia
  de cómo se construía antes. No replicar ese patrón en features nuevas.

### Active Features

- `ping` — health/ping endpoints, ejemplo canónico de TDD en el proyecto
- `hotels` — CRUD completo con búsqueda paginada (ejemplo MVC más completo)
- `rooms` — habitaciones (entidades JPA definidas, servicio en progreso)
- `users` — gestión de usuarios con value objects (`Email`, `Password`, `PhoneNumber`, `UserId`)
- `examples` — referencia de implementación hexagonal (no replicar en features nuevas)
- `cart`, `gamification`, `payments`, `pokemon`, `reservations`, `reviews`, `services` — stubs (`PackageInfo.kt` only)

## Profiles

| Profile        | Database                                                             | Purpose                          |
|----------------|----------------------------------------------------------------------|----------------------------------|
| `dev`          | H2 in-memory + H2 console enabled                                    | Local development (default)      |
| `test`         | H2 (E2E) or Testcontainers PostgreSQL (integration, Docker required) | Tests                            |
| `prod`         | PostgreSQL via env vars                                              | Production                       |
| `generate-ddl` | H2, Flyway disabled                                                  | DDL generation from JPA entities |

## Testing Strategy

Three test layers:

1. **Unit tests** — domain services tested without Spring context (fast). Use MockK for mocks and Kotest assertions (`shouldBe`, `shouldNotBe`).
2. **Integration tests** — controller tests with `@WebMvcTest` + `MockMvc`, repository tests with `@DataJpaTest`. Extend `BaseIntegrationTest` for full-context tests with Testcontainers (requires Docker; falls back to H2 if Docker unavailable via `DockerAvailableCondition`).
3. **E2E tests** — `HotelE2ETest` uses `TestRestTemplate` with `@SpringBootTest(webEnvironment = RANDOM_PORT)` against H2.

Test infrastructure:
- `BaseIntegrationTest` — activates `test` profile, imports `TestcontainersConfiguration`
- `TestcontainersConfiguration` — conditionally starts `postgres:17-alpine` only if Docker is available
- `SecurityTestConfig` + `WithMockUser` — custom annotation for controller tests requiring auth context

## Error Handling

All unhandled exceptions are caught by `common/exception/GlobalExceptionHandler.kt` (`@RestControllerAdvice`). Domain services should throw standard exceptions — the handler maps them to HTTP status codes:

| Exception                         | HTTP Status |
|-----------------------------------|-------------|
| `NoSuchElementException`          | 404         |
| `IllegalStateException`           | 409         |
| `MethodArgumentNotValidException` | 400         |
| `ConstraintViolationException`    | 400         |
| `Exception` (fallback)            | 500         |

Prefer these standard exceptions over creating custom ones unless the distinction matters for the API consumer.

## Environment Variables

Copy `.env.example` to `.env` for local dev. Required variables:

- `DB_URL`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` — PostgreSQL (prod profile)
- `SENTRY_DSN`, `SENTRY_AUTH_TOKEN` — error tracking (optional for local dev)
- `SENTRY_ENVIRONMENT`, `SENTRY_DEBUG`, `SENTRY_ENABLED`, `SENTRY_TRACES_SAMPLE_RATE` — Sentry tuning

The `build.gradle.kts` auto-loads `.env` as fallback when OS env vars are absent.

## Commit Convention

This project uses **Conventional Commits**. CI/CD uses semantic-release to auto-version on merge to `main`.

```
feat: add room availability endpoint     → MINOR bump
fix: handle null address in hotel mapper → PATCH bump
refactor: extract pagination helper      → no version bump
```

Breaking changes: add `BREAKING CHANGE:` footer or `!` after type (`feat!:`).

## Naming Conventions

| Artefacto            | Convención                    | Ejemplo                      |
|----------------------|-------------------------------|------------------------------|
| Controller           | `*Controller.kt`              | `HotelController.kt`         |
| Service              | `*Service.kt`                 | `HotelService.kt`            |
| Spring Data repo     | `*JpaRepository.kt`           | `HotelJpaRepository.kt`      |
| JPA entity           | `*Entity.kt`                  | `HotelEntity.kt`             |
| Domain model         | sin sufijo                    | `Hotel.kt`                   |
| DTO entrada          | `*Request.kt`                 | `CreateHotelRequest.kt`      |
| DTO salida           | `*Response.kt`                | `HotelResponse.kt`           |
| Mapper               | `*Mapper.kt`                  | `HotelMapper.kt`             |
| Value object         | en subpaquete `valueobjects/` | `Email.kt`, `Password.kt`    |
| Config específica    | `*ServiceConfig.kt`           | `HotelServiceConfig.kt`      |
| Excepción de dominio | `*Exception.kt`               | `DuplicateEmailException.kt` |

## Code Quality

| Herramienta | Versión                       | Propósito                      | Comando               |
|-------------|-------------------------------|--------------------------------|-----------------------|
| **KTLint**  | 1.7.1 (plugin Gradle: 14.1.0) | Formateo consistente de Kotlin | `make format`         |
| **Detekt**  | 2.0.0-alpha.1 (`dev.detekt`)  | Análisis estático, code smells | `make lint`           |
| **JaCoCo**  | (managed by Spring Boot BOM)  | Cobertura de código ≥ 85%      | `make coverage-check` |

- Configuración de Detekt: `config/detekt/detekt.yml` con `autoCorrect = true`
- Reportes de KTLint: `build/reports/ktlint/`
- Reportes de Detekt: `build/reports/detekt/` (HTML + SARIF)
- Reporte de JaCoCo: `build/reports/jacoco/test/html/index.html`

```bash
make lint     # solo verifica — no modifica archivos
make format   # aplica correcciones automáticas de KTLint y Detekt
make quality  # gate completo: lint + tests + cobertura (equivale a CI local)
```

**Enfoque**

* Piensa antes de actuar. Lee los archivos existentes antes de escribir código.
* Sé conciso en la salida, pero exhaustivo en el razonamiento.
* Prefiere editar en lugar de reescribir archivos completos.
* No vuelvas a leer archivos que ya leíste, a menos que puedan haber cambiado.
* Omite archivos mayores a 100KB salvo que sea explícitamente necesario.
* Sugiere ejecutar `/cost` cuando una sesión se alargue para monitorear la proporción de caché.
* Recomienda iniciar una nueva sesión al cambiar a una tarea no relacionada.
* Prueba tu código antes de declararlo terminado.
* Sin introducciones complacientes ni relleno innecesario al cerrar.
* Mantén las soluciones simples y directas.
* Las instrucciones del usuario siempre tienen prioridad sobre este archivo.

## Salida
- Devuelve primero el código. Explicación después, solo si no es obvio.
- Sin texto en línea. Usa comentarios con moderación, solo donde la lógica no sea clara.
- Sin boilerplate a menos que se solicite explícitamente.

## Reglas de Código
- La solución funcional más simple. Sin sobreingeniería.
- Sin abstracciones para operaciones de un solo uso.
- Sin funcionalidades especulativas o "quizá también quieras...".
- Lee el archivo antes de modificarlo. Nunca edites a ciegas.
- Sin docstrings ni anotaciones de tipo en código que no se esté modificando.
- Sin manejo de errores para escenarios que no pueden ocurrir.
- Tres líneas similares son mejores que una abstracción prematura.

## Reglas de Revisión
- Indica el bug. Muestra la corrección. Fin.
- Sin sugerencias fuera del alcance de la revisión.
- Sin cumplidos sobre el código antes o después de la revisión.

## Reglas de Depuración
- Nunca especules sobre un bug sin leer primero el código relevante.
- Indica qué encontraste, dónde, y la solución. Una sola pasada.
- Si la causa no es clara: dilo. No adivines.

## Formato Simple
- Sin guiones largos, comillas tipográficas ni símbolos Unicode decorativos.
- Solo guiones simples y comillas rectas.
- Caracteres de lenguaje natural (acentos, CJK, etc.) están bien cuando el contenido lo requiera.
- El código debe ser seguro para copiar y pegar.

## Approach

- Think before acting. Read existing files before writing code.
- Be concise in output but thorough in reasoning.
- Prefer editing over rewriting whole files.
- Do not re-read files you have already read unless the file may have changed.
- Skip files over 100KB unless explicitly required.
- Suggest running /cost when a session is running long to monitor cache ratio.
- Recommend starting a new session when switching to an unrelated task.
- Test your code before declaring done.
- No sycophantic openers or closing fluff.
- Keep solutions simple and direct.
- User instructions always override this file.

<!-- code-review-graph MCP tools -->
## MCP Tools: code-review-graph

**IMPORTANT: This project has a knowledge graph. ALWAYS use the
code-review-graph MCP tools BEFORE using Grep/Glob/Read to explore
the codebase.** The graph is faster, cheaper (fewer tokens), and gives
you structural context (callers, dependents, test coverage) that file
scanning cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes` or `query_graph` instead of Grep
- **Understanding impact**: `get_impact_radius` instead of manually tracing imports
- **Code review**: `detect_changes` + `get_review_context` instead of reading entire files
- **Finding relationships**: `query_graph` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview` + `list_communities`

Fall back to Grep/Glob/Read **only** when the graph doesn't cover what you need.

### Key Tools

| Tool | Use when |
|------|----------|
| `detect_changes` | Reviewing code changes — gives risk-scored analysis |
| `get_review_context` | Need source snippets for review — token-efficient |
| `get_impact_radius` | Understanding blast radius of a change |
| `get_affected_flows` | Finding which execution paths are impacted |
| `query_graph` | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes` | Finding functions/classes by name or keyword |
| `get_architecture_overview` | Understanding high-level codebase structure |
| `refactor_tool` | Planning renames, finding dead code |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes` for code review.
3. Use `get_affected_flows` to understand impact.
4. Use `query_graph` pattern="tests_for" to check coverage.

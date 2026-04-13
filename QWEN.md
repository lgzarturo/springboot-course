# QWEN.md — Spring Boot Course Context

## Project Overview

**springboot-course** is a Spring Boot + Kotlin REST API course project that simulates a **hotel management and ecommerce platform** (Hotel Pokémon). The project is organized using **MVC by Features** (Screaming Architecture), having migrated away from Hexagonal Architecture.

- **Language:** Kotlin 2.2.20
- **Framework:** Spring Boot 4.0.3
- **JVM:** Java 21
- **Build Tool:** Gradle (Kotlin DSL)
- **Database:** PostgreSQL (prod) / H2 (dev/test)
- **License:** CC-BY-4.0

The project follows a Pokémon-themed learning narrative where a developer named Kai progresses through 6 "gyms," each representing a technical domain (JPA, Security, Validation, Testing, Observability, DevOps).

## Architecture

### Source Structure

```
src/main/kotlin/com/lgzarturo/springbootcourse/
├── SpringbootCourseApplication.kt       # Application entry point
├── config/                              # Cross-cutting infrastructure
│   ├── OpenApiConfig.kt                 # Swagger/OpenAPI configuration
│   └── WebConfig.kt                     # CORS, global interceptors
├── common/                              # Reusable components
│   ├── exception/                       # Global error handling
│   ├── pagination/                      # Pagination utilities
│   ├── constants/                       # Application constants
│   └── extensions/                      # Kotlin extensions
└── features/                            # Business features (Screaming Architecture)
    ├── ping/                            # Health check endpoint (TDD canonical example)
    ├── hotels/                          # Full CRUD with paginated search
    ├── rooms/                           # Hotel rooms (JPA entities, WIP)
    ├── users/                           # User management with value objects
    ├── examples/                        # Hexagonal architecture reference (do NOT replicate)
    ├── sentry/                          # Error monitoring
    └── [cart, gamification, payments, pokemon, reservations, reviews, services] # Stubs
```

### Architectural Rules

- **One package = one feature**: Everything related to a feature lives together
- **Maximum 2 nesting levels**: `hotels/dto/` is the limit
- **No artificial ports/adapters**: `@Service` is the business layer, `@Repository` is the data interface
- **Domain model is pure Kotlin**: `Hotel.kt` has no `@Entity` or Spring dependencies
- **Features are self-contained**: No cross-feature imports; communication via `common/` or Spring events
- **Controllers → Services → Repositories**: Classic MVC flow; controllers never access repositories directly

### Naming Conventions

| Artifact            | Convention                    | Example                      |
|---------------------|-------------------------------|------------------------------|
| Controller          | `*Controller.kt`              | `HotelController.kt`         |
| Service             | `*Service.kt`                 | `HotelService.kt`            |
| Spring Data repo    | `*JpaRepository.kt`           | `HotelJpaRepository.kt`      |
| JPA entity          | `*Entity.kt`                  | `HotelEntity.kt`             |
| Domain model        | No suffix                     | `Hotel.kt`                   |
| Input DTO           | `*Request.kt`                 | `CreateHotelRequest.kt`      |
| Output DTO          | `*Response.kt`                | `HotelResponse.kt`           |
| Mapper              | `*Mapper.kt`                  | `HotelMapper.kt`             |
| Value object        | In `valueobjects/` subpackage | `Email.kt`, `Password.kt`    |
| Feature config      | `*ServiceConfig.kt`           | `HotelServiceConfig.kt`      |
| Domain exception    | `*Exception.kt`               | `DuplicateEmailException.kt` |

## Building and Running

### Prerequisites

- Java 21 (JDK 21)
- Docker and Docker Compose (for PostgreSQL and Testcontainers)
- Git
- IntelliJ IDEA (recommended)

### Setup

```bash
# 1. Clone and configure environment
cp .env.example .env
# Edit .env with your values

# 2. Start PostgreSQL (optional, for local dev)
make docker-up
# or: docker compose up -d
```

### Common Commands

#### Build & Run

| Command         | Description                            |
|-----------------|----------------------------------------|
| `make build`    | Clean + compile + assemble             |
| `make compile`  | Compile only (no tests, no clean)      |
| `make run`      | Run application (dev profile, default) |
| `make run-dev`  | Run with explicit dev profile          |
| `make run-prod` | Run with prod profile                  |
| `make version`  | Print project version                  |

#### Tests

| Command                                  | Description                        |
|------------------------------------------|------------------------------------|
| `make test`                              | Run all tests                      |
| `make test-class CLASS=HotelServiceTest` | Run a single test class            |
| `make coverage`                          | Run tests + generate JaCoCo report |
| `make coverage-check`                    | Verify coverage ≥ 85% threshold    |

#### Code Quality

| Command        | Description                                           |
|----------------|-------------------------------------------------------|
| `make lint`    | Verify style with KTLint + Detekt (read-only)         |
| `make format`  | Apply auto-fixes with KTLint + Detekt                 |
| `make fix`     | Alias for `format`                                    |
| `make quality` | Full gate: lint + tests + coverage (equivalent to CI) |

#### Database

| Command                                        | Description                                                |
|------------------------------------------------|------------------------------------------------------------|
| `make ddl`                                     | Generate DDL from JPA entities (`build/schema-create.sql`) |
| `make create-migration VER=2 DESC="add_rooms"` | Create Flyway migration from DDL                           |
| `make migrate DESC="add_users_table"`          | Auto-generate migration using script                       |
| `make diff-migration`                          | Show instructions for incremental migration                |

#### Docker

| Command            | Description                        |
|--------------------|------------------------------------|
| `make docker-up`   | Start Docker services (PostgreSQL) |
| `make docker-down` | Stop Docker services               |
| `make docker-logs` | Tail container logs                |
| `make docker-ps`   | List active containers             |

#### Combined

| Command      | Description                                    |
|--------------|------------------------------------------------|
| `make ci`    | Simulate full CI pipeline (build + quality)    |
| `make setup` | Configure local environment (.env + docker-up) |
| `make reset` | Clean build artifacts and stop Docker          |

> On Windows, use `gradlew.bat` instead of `./gradlew` if not using `make`.

## Spring Profiles

| Profile        | Database                                                             | Purpose                          |
|----------------|----------------------------------------------------------------------|----------------------------------|
| `dev`          | H2 in-memory + H2 console                                            | Local development (default)      |
| `test`         | H2 (E2E) or Testcontainers PostgreSQL (integration, requires Docker) | Tests                            |
| `prod`         | PostgreSQL via env vars                                              | Production                       |
| `generate-ddl` | H2, Flyway disabled                                                  | DDL generation from JPA entities |

## Testing Strategy

Three test layers:

1. **Unit tests** — Domain services without Spring context (fast). Use MockK for mocks.
2. **Integration tests** — Controller tests with `@WebMvcTest` + `MockMvc`, repository tests with `@DataJpaTest`. Extend `BaseIntegrationTest` for full-context tests with Testcontainers.
3. **E2E tests** — `HotelE2ETest` uses `TestRestTemplate` with `@SpringBootTest(webEnvironment = RANDOM_PORT)` against H2.

**Test dependencies:** JUnit 5, Kotest Assertions, MockK, SpringMockK, Testcontainers.

## Environment Variables

| Variable                 | Description                          | Default         |
|--------------------------|--------------------------------------|-----------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile                | `dev`           |
| `DB_NAME`                | Database name                        | `springboot_db` |
| `DB_USERNAME`            | Database user                        | `postgres`      |
| `DB_PASSWORD`            | Database password                    | `postgres`      |
| `DB_PORT`                | Database port                        | `5432`          |
| `SENTRY_DSN`             | Sentry DSN for error tracking        | —               |
| `SENTRY_AUTH_TOKEN`      | Sentry auth token for source uploads | —               |

## API Documentation

Once running:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`
- **Actuator Health:** `http://localhost:8080/actuator/health`
- **Prometheus Metrics:** `http://localhost:8080/actuator/prometheus`

## Code Quality Tools

| Tool       | Version                    | Purpose                      | Config                                     |
|------------|----------------------------|------------------------------|--------------------------------------------|
| **KTLint** | 1.7.1                      | Kotlin formatting            | `version.set("1.7.1")` in build.gradle.kts |
| **Detekt** | 2.0.0-alpha.1              | Static analysis, code smells | `config/detekt/detekt.yml`                 |
| **JaCoCo** | Managed by Spring Boot BOM | Coverage ≥ 85%               | Minimum 0.85 threshold                     |

- KTLint reports: `build/reports/ktlint/`
- Detekt reports: `build/reports/detekt/` (HTML + SARIF)
- JaCoCo report: `build/reports/jacoco/test/html/index.html`

## Commit Convention

This project uses **Conventional Commits**. CI/CD uses `semantic-release` to auto-version on merge to `main`.

```
feat: add room availability endpoint     → MINOR bump
fix: handle null address in hotel mapper → PATCH bump
refactor: extract pagination helper      → no version bump
```

Breaking changes: add `BREAKING CHANGE:` footer or `!` after type (`feat!:`).

## Key Dependencies

- Spring Boot Starters: actuator, data-jpa, validation, flyway, webmvc, restclient
- Jackson Module: kotlin
- OpenAPI: springdoc-openapi-starter-webmvc-ui 3.0.2
- Monitoring: Sentry, Micrometer Prometheus
- Database: PostgreSQL driver, H2 (runtime only)
- Testing: spring-boot-test, Testcontainers, MockK, Kotest, SpringMockK

## Active Features Status

| Feature                                                                              | Status       | Description                                                               |
|--------------------------------------------------------------------------------------|--------------|---------------------------------------------------------------------------|
| `ping`                                                                               | ✅ Complete   | Health/ping endpoints, canonical TDD example                              |
| `hotels`                                                                             | ✅ Complete   | Full CRUD with paginated search (most complete MVC example)               |
| `users`                                                                              | ✅ Complete   | User management with value objects (Email, Password, PhoneNumber, UserId) |
| `rooms`                                                                              | 🚧 WIP       | Hotel rooms (JPA entities defined, service in progress)                   |
| `examples`                                                                           | 📚 Reference | Hexagonal pattern reference (do NOT replicate in new features)            |
| `sentry`                                                                             | ✅ Complete   | Error monitoring setup                                                    |
| `cart`, `gamification`, `payments`, `pokemon`, `reservations`, `reviews`, `services` | 📋 Stubs     | `PackageInfo.kt` only                                                     |

## Important Notes

- **Do NOT replicate Hexagonal Architecture** in new features. The `examples/` feature shows the old pattern for reference only. New features should follow the MVC-by-Features pattern.
- The `build.gradle.kts` auto-loads `.env` as fallback when OS environment variables are absent.
- Coverage threshold is **85%** — enforced by `jacocoTestCoverageVerification`.
- `open-in-view: false` is set to avoid lazy loading issues.
- `ddl-auto: update` is used; Flyway migrations should be the primary schema management tool.

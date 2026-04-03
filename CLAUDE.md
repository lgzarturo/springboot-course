# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Spring Boot + Kotlin REST API course project simulating a hotel management platform. Built with Hexagonal Architecture (Ports & Adapters) following Clean Architecture and DDD principles. Java 21, Kotlin, Spring Boot 4.x, PostgreSQL (prod) / H2 (dev/test).

## Common Commands

```bash
# Build
./gradlew clean build

# Run (uses 'dev' profile by default)
./gradlew bootRun

# Run with explicit profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.lgzarturo.springbootcourse.hotels.service.HotelServiceTest"

# Run tests with coverage report (output: build/reports/jacoco/test/html/index.html)
./gradlew jacocoTestReport

# Lint and static analysis
./gradlew checkCodeStyle      # runs ktlintCheck + detekt
./gradlew formatCode          # auto-fix with ktlintFormat + detektAutoCorrect
./gradlew fixAll              # alias for formatCode

# Full quality gate (lint + tests + coverage)
./gradlew codeQuality

# Database migrations
./gradlew generateDDL         # generates build/schema-create.sql from JPA entities
./gradlew createMigration -Pmigration_version=2 -Pdescription="add_users"
```

## Architecture

The codebase is organized **by domain module** first, then by layer within each module. Each domain (e.g., `hotels`, `ping`, `users`, `example`) follows this internal structure:

```
<domain>/
├── adapters/
│   ├── persistence/        # JPA entities, Spring Data repositories, repository adapters
│   │   └── entity/
│   └── rest/               # Spring MVC controllers, DTOs (request/response), mappers
│       └── dto/
├── application/
│   └── ports/
│       ├── input/          # Use case interfaces (what the domain exposes)
│       └── output/         # Repository port interfaces (what the domain needs)
├── domain/                 # Pure domain models, value objects, domain exceptions
├── config/                 # Spring @Configuration beans specific to this domain
└── service/                # Use case implementations
```

Global cross-cutting code lives in `shared/` (config, exception handling, extensions, constants).

### Key Architectural Rules

- **Domain layer has zero Spring/JPA dependencies** — pure Kotlin classes only.
- **Controllers depend on use case ports** (`*UseCasePort`), never on services directly.
- **Services implement use case ports** and depend on repository ports.
- **JPA entities are separate from domain models**; persistence adapters translate between them using mappers.
- **DTOs are never passed into the domain** — controllers map DTOs → domain before calling use cases.

### Active Domains

- `ping` — simple health/ping endpoints, the canonical TDD example
- `hotels` — hotel CRUD with search/pagination (most complete example)
- `rooms` — hotel rooms (persistence entities defined, domain in progress)
- `users` — user management with value objects (`Email`, `Password`, `PhoneNumber`, `UserId`)
- `example` — reference CRUD implementation with full adapter stack
- `cart`, `gamification`, `payments`, `pokemon`, `reservations`, `reviews`, `services` — stubs/placeholders (`PackageInfo.kt` only)

## Profiles

| Profile | Database | Purpose |
|---------|----------|---------|
| `dev` | H2 in-memory + H2 console enabled | Local development (default) |
| `test` | H2 (E2E) or Testcontainers PostgreSQL (integration, Docker required) | Tests |
| `prod` | PostgreSQL via env vars | Production |
| `generate-ddl` | H2, Flyway disabled | DDL generation from JPA entities |

## Testing Strategy

Three test layers:

1. **Unit tests** — domain services tested without Spring context (fast). Use MockK for mocks.
2. **Integration tests** — controller tests with `@WebMvcTest` + `MockMvc`, repository tests with `@DataJpaTest`. Extend `BaseIntegrationTest` for full-context tests with Testcontainers (requires Docker; falls back to H2 if Docker unavailable via `DockerAvailableCondition`).
3. **E2E tests** — `HotelE2ETest` uses `TestRestTemplate` with `@SpringBootTest(webEnvironment = RANDOM_PORT)` against H2.

Test infrastructure:
- `BaseIntegrationTest` — activates `test` profile, imports `TestcontainersConfiguration`
- `TestcontainersConfiguration` — conditionally starts `postgres:17-alpine` only if Docker is available
- `SecurityTestConfig` + `WithMockUser` — custom annotation for controller tests requiring auth context

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

- Controllers: `*Controller.kt`
- Use case ports: `*UseCasePort.kt`
- Repository ports: `*RepositoryPort.kt`
- Services: `*Service.kt`
- DTOs: `*Request.kt`, `*Response.kt`
- Mappers: `*Mapper.kt`
- JPA repositories: `*JpaRepository.kt`
- JPA entities: no suffix (e.g., `HotelEntity.kt`)

## Code Quality

- **KTLint 1.7.1** — enforces formatting; run `./gradlew ktlintFormat` to auto-fix
- **Detekt 1.23.8** — static analysis; config at `config/detekt/detekt.yml`; `autoCorrect = true` is enabled
- **JaCoCo** — 85% coverage minimum enforced by `jacocoTestCoverageVerification`
- Lint reports: `build/reports/ktlint/` and `build/reports/detekt/`

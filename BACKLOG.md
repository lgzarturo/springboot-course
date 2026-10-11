# BACKLOG

## Global

- Product: Gran Hotel Pokemon - API REST de gestion hotelera
- Strategy: FIFO by priority, then dependency graph
- Policy: OpenSpec delivery uses validate, scan, plan, status, next, start, done, block, and archive; no item larger than 1-2 implementation sessions; archive requires a PASS scorecard
- Review required: yes
- TDD required: yes

## OpenSpec Scorecards

Every item produces one scorecard for its final `review` card. The scorecard is
created after the implementation diff and before the item is archived.

- Record path: `.codeconductor/evaluation/scorecards/<scorecard-id>.json`
- Create: `npx cc-codeconductor scorecard create --task <BC-id> --from-diff`
- Record: `npx cc-codeconductor scorecard record --task <BC-id> --verdict <PASS|REVISE|REJECT> --score <0-3>`
- PASS threshold: weighted score `>= 2.0` and no critical criterion scored `0`
- REVISE: correct the findings and repeat the review scorecard
- REJECT: return the item to `task-coach` for re-scoping and create a new card

### Scorecard criteria

| ID | Criterion | Weight | Evidence required |
| --- | --- | ---: | --- |
| acceptance | Acceptance criteria met | 30% | Each criterion mapped to code, spec, or test evidence |
| minimal_diff | Minimal diff and no scope creep | 20% | Diff is limited to the declared scope |
| tests | Tests present and passing | 15% | TDD evidence and passing verification command |
| regressions | No regressions introduced | 15% | Full applicable test suite passes |
| conventions | Code follows project conventions | 5% | Architecture, naming, and style checks |
| documentation | Documentation updated if required | 5% | Public API or decision documentation evidence |
| context_discipline | Context discipline | 5% | Work stayed within declared scope and boundaries |
| cc_gain | Complexity diffusion (cc-gain) | 5% | Complexity-auditor result or explicit N/A rationale |

Scores use `0 = not met`, `1 = partial`, `2 = met`, and `3 = exceeded`.
The weighted score is `sum(score * weight)`. The evaluator must record the
agent, contract version, date, findings, weighted score, verdict, and evidence.

### OpenSpec phase evidence

| Phase | Required deliverable | Gate |
| --- | --- | --- |
| discover | Repository map, affected files, dependencies, and risks | Scope is explicit |
| design | Technical plan with approach, files, risks, and edge cases | Human review when risk is high |
| test | Tests that cover every acceptance criterion and fail for the intended reason | Red evidence exists |
| implement | Minimal implementation within declared boundaries | Green evidence exists |
| review | Acceptance audit, regression result, and scorecard | PASS scorecard before archive |

## Items

### BC-007 | Implement stateless JWT authentication

- Priority: P1
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-006
- Description: Add login and registration authentication with signed, expiring JWTs and protect API endpoints through Spring Security.
- Scope: `build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/config/security/`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/auth/`, `src/test/kotlin/com/lgzarturo/springbootcourse/config/security/`
- Out of scope: OAuth2 providers, refresh tokens, multi-factor authentication, and frontend login screens
- Business value: Prevents unauthenticated access to user and hotel management operations.
- Acceptance:
  - [ ] Valid credentials at `POST /api/auth/login` return `200 OK` with a signed JWT
  - [ ] Invalid credentials return `401 Unauthorized`
  - [ ] Protected endpoints return `401 Unauthorized` without a valid token
  - [ ] A valid token with an allowed role reaches the protected endpoint, while an insufficient role returns `403 Forbidden`
  - [ ] Expired or invalidly signed tokens return `401 Unauthorized`
  - [ ] JWT claims include the username, roles, and expiration timestamp
  - [ ] MockMvc integration tests cover public, authenticated, forbidden, and expired-token requests
- Risks: Token secrets and expiration settings must come from configuration and must not be committed.
- Progress: 0%
- Branch: `feature/milestone-05-security`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-008 | Add roles and granular authorities

- Priority: P1
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-007
- Description: Persist roles and authorities and apply them to API operations so access is controlled by explicit permissions.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/main/kotlin/com/lgzarturo/springbootcourse/config/security/`, `src/main/resources/db/migration/V4__Add_Roles_Authorities.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`
- Out of scope: Organization-level tenancy, external identity providers, and UI permission management
- Business value: Enables least-privilege access for guests, staff, and administrators.
- Acceptance:
  - [ ] `ROLE_GUEST`, `ROLE_STAFF`, and `ROLE_ADMIN` are persisted and loaded into the security context
  - [ ] Authorities include user read/write, hotel management, and reservation creation permissions
  - [ ] A guest can read hotels but cannot create or modify them
  - [ ] Staff can manage reservations and administrators retain access to all declared operations
  - [ ] Flyway seeds are idempotent and the user-to-role relationship is persisted
  - [ ] Authorization tests cover every declared role and protected endpoint combination
- Risks: Role and authority naming must match JWT claims and existing controller annotations.
- Progress: 0%
- Branch: `feature/milestone-05-security`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-009 | Establish layered feature testing

- Priority: P1
- Status: TODO
- Type: tech-debt
- Owner: spring-kotlin team
- Depends on: BC-006
- Description: Establish a repeatable test strategy for hotels and users across domain, web, persistence, integration, and end-to-end boundaries.
- Scope: `src/test/kotlin/com/lgzarturo/springbootcourse/features/hotels/`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/test/kotlin/com/lgzarturo/springbootcourse/support/`
- Out of scope: Load testing, production observability, and mutation-testing infrastructure
- Business value: Detects regressions at the cheapest test layer and documents the testing approach for future features.
- Acceptance:
  - [ ] Hotels and users each have unit, controller, and full-context integration coverage
  - [ ] Repository behavior is covered with `@DataJpaTest` where persistence is used
  - [ ] End-to-end tests use the configured Testcontainers PostgreSQL path when Docker is available
  - [ ] Unit tests execute without a Spring context and each test completes in less than 100 ms on the test runner
  - [ ] Tests use isolated fixtures and pass reproducibly in two consecutive runs
  - [ ] Scenario names follow `debe_resultado_cuando_condicion` or an equivalent documented convention
- Risks: Docker availability and H2/PostgreSQL differences can make integration results diverge.
- Progress: 0%
- Branch: `feature/milestone-06-testing`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-010 | Verify protected endpoint behavior

- Priority: P1
- Status: TODO
- Type: tech-debt
- Owner: spring-kotlin team
- Depends on: BC-007, BC-009
- Description: Add reusable security test support and integration coverage for JWT authentication, authorization, and isolated feature tests.
- Scope: `src/test/kotlin/com/lgzarturo/springbootcourse/config/security/`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/`, `src/test/kotlin/com/lgzarturo/springbootcourse/support/SecurityTestConfig.kt`
- Out of scope: Changes to production authorization rules unless a failing test identifies a defect
- Business value: Prevents security regressions from silently exposing protected resources.
- Acceptance:
  - [ ] Requests without authentication are asserted as `401 Unauthorized`
  - [ ] Requests with insufficient roles are asserted as `403 Forbidden`
  - [ ] Requests with valid JWT credentials are asserted as successful for each permitted role
  - [ ] Expired and malformed tokens are asserted as `401 Unauthorized`
  - [ ] Custom `WithMockUser` fixtures represent the domain roles used by the API
  - [ ] `SecurityTestConfig` lets isolated controller tests run without the complete production security chain
- Risks: Test-only security configuration must not be component-scanned into production behavior.
- Progress: 0%
- Branch: `feature/milestone-06-testing`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-011 | Enforce automated quality gates

- Priority: P1
- Status: TODO
- Type: tech-debt
- Owner: spring-kotlin team
- Depends on: BC-009
- Description: Make formatting, static analysis, tests, coverage, and build validation executable through one documented quality gate.
- Scope: `build.gradle.kts`, `config/detekt/detekt.yml`, `Makefile`, `README.md`, `.github/workflows/ci.yml`
- Out of scope: Rewriting existing production code solely to increase the target threshold
- Business value: Makes pull-request quality checks deterministic and repeatable.
- Acceptance:
  - [ ] `make quality` runs KtLint, Detekt, tests, JaCoCo verification, and build in the declared order
  - [ ] The gate fails when coverage is below 85 percent
  - [ ] The gate fails on a KtLint violation or Detekt violation
  - [ ] JaCoCo HTML output is generated under `build/reports/jacoco/`
  - [ ] README documents the local quality command and its required prerequisites
  - [ ] The current test suite passes through the complete gate
- Risks: Existing CI task names and the project-level 85 percent policy must remain aligned.
- Progress: 0%
- Branch: `feature/milestone-06-testing`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-012 | Complete OpenAPI documentation

- Priority: P2
- Status: TODO
- Type: tech-debt
- Owner: spring-kotlin team
- Depends on: BC-008, BC-011
- Description: Document every public REST endpoint, DTO schema, response code, and JWT security requirement in the generated OpenAPI contract.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/config/OpenApiConfig.kt`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/`, `src/main/kotlin/com/lgzarturo/springbootcourse/common/`, `src/test/kotlin/com/lgzarturo/springbootcourse/`
- Out of scope: Changing endpoint semantics or introducing a separate API documentation service
- Business value: Lets external developers integrate with the API using a single executable contract.
- Acceptance:
  - [ ] Swagger UI lists every controller endpoint grouped by feature
  - [ ] Every endpoint has a summary, description, request schema, response schema, and applicable status codes
  - [ ] DTO schemas expose validation constraints and representative examples
  - [ ] Swagger UI exposes an `Authorize` control for JWT bearer tokens
  - [ ] Error responses include the documented `ErrorResponse` schema and examples
  - [ ] `/api-docs` returns a valid OpenAPI 3 JSON document
- Risks: Documentation annotations must stay synchronized with security and validation behavior.
- Progress: 0%
- Branch: `feature/milestone-07-docs`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-013 | Implement room management CRUD

- Priority: P1
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-006, BC-012
- Description: Complete the rooms feature with domain logic, persistence fields, validation, pagination, filtering, REST endpoints, and OpenAPI documentation.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/rooms/`, `src/main/resources/db/migration/V5__Enrich_Rooms_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/rooms/`
- Out of scope: Reservations, payments, and room availability synchronization with external systems
- Business value: Makes hotel inventory manageable and provides the resource required for reservations.
- Acceptance:
  - [ ] Room create, list, detail, update, and delete endpoints work under a hotel resource
  - [ ] DTO validation rejects blank names, non-positive capacities, and invalid room values with `400 Bad Request`
  - [ ] A room without a valid hotel association returns `400 Bad Request`
  - [ ] Room listing supports pagination and filtering by status and Pokemon theme
  - [ ] Room type, status, theme, capacity, and description are persisted by the migration and entity
  - [ ] Unit, controller, persistence, and integration tests cover the complete room feature
  - [ ] All room endpoints are represented in the generated OpenAPI document
- Risks: Room state transitions must remain compatible with future reservation rules.
- Progress: 0%
- Branch: `feature/milestone-07-docs`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-014 | Package the application with Docker

- Priority: P2
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-013
- Description: Produce a reproducible multi-stage application image and a local Compose environment for the API and its database dependencies.
- Scope: `Dockerfile`, `.dockerignore`, `docker-compose.yml`, `build.gradle.kts`, `README.md`
- Out of scope: Kubernetes manifests, cloud-specific deployment, and production secret provisioning
- Business value: Reduces environment drift and makes local and CI execution reproducible.
- Acceptance:
  - [ ] `docker build -t hotel-pokemon .` creates an image that starts the application
  - [ ] `docker compose up` starts the application with PostgreSQL using configured environment variables
  - [ ] The Dockerfile uses separate build and runtime stages with a non-root runtime user
  - [ ] The resulting image is smaller than 200 MB
  - [ ] No database password or signing secret is embedded in the image layers
  - [ ] The container health check reports application readiness
- Risks: The JDK 25 runtime and layered JAR layout must remain compatible with the selected base image.
- Progress: 0%
- Branch: `feature/milestone-08-deploy`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-015 | Expose production observability

- Priority: P2
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-014
- Description: Expose secured health probes and business metrics for operators using Actuator, Micrometer, Prometheus, and Sentry.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/config/`, `src/main/kotlin/com/lgzarturo/springbootcourse/common/`, `src/main/resources/`, `docker-compose-monitoring.yml`, `src/test/kotlin/com/lgzarturo/springbootcourse/`
- Out of scope: Building a custom monitoring dashboard or replacing Sentry
- Business value: Shortens detection time for outages, failed dependencies, and degraded reservation flows.
- Acceptance:
  - [ ] `/actuator/health` reports component status and returns `UP` when dependencies are healthy
  - [ ] `/actuator/health/liveness` and `/actuator/health/readiness` return the configured probe groups
  - [ ] `/actuator/prometheus` returns metrics in Prometheus exposition format
  - [ ] Actuator endpoints other than health require administrator authorization
  - [ ] Reservation counters, endpoint timers, and available-room gauges are visible in metrics
  - [ ] Sentry captures an unhandled production exception with the configured environment
- Risks: Health details and metric labels must not expose credentials or high-cardinality personal data.
- Progress: 0%
- Branch: `feature/milestone-08-deploy`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-016 | Automate CI/CD workflows

- Priority: P2
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-011, BC-014
- Description: Automate build, quality, coverage, and container publication checks for pushes and pull requests.
- Scope: `.github/workflows/ci.yml`, `.github/workflows/docker.yml`, `README.md`, `Makefile`
- Out of scope: Cloud infrastructure provisioning and deployment approval policies
- Business value: Detects regressions before merge and provides a reproducible release artifact.
- Acceptance:
  - [ ] CI runs on pushes to `main` and on pull requests
  - [ ] CI uses JDK 25 and caches Gradle dependencies
  - [ ] CI executes formatting, static analysis, tests, coverage verification, and build
  - [ ] CI fails when any required quality or test task fails
  - [ ] Coverage reports are uploaded as workflow artifacts
  - [ ] The Docker workflow builds and publishes an image only from an authorized release context
- Risks: Workflow permissions and registry credentials must follow least privilege.
- Progress: 0%
- Branch: `feature/milestone-08-deploy`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-017 | Implement reservation management

- Priority: P1
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-008, BC-013
- Description: Implement reservation creation, querying, and lifecycle transitions while preventing overlapping bookings and keeping room state consistent.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/reservations/`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/rooms/`, `src/main/resources/db/migration/V6__Add_Reservations_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/reservations/`
- Out of scope: Payment processing, notifications, and reviews
- Business value: Delivers the central hotel booking workflow.
- Acceptance:
  - [ ] Reservation creation rejects overlapping dates for the same room
  - [ ] Check-out date must be after check-in date and invalid ranges return `400 Bad Request`
  - [ ] Total price equals the number of nights multiplied by the room nightly price
  - [ ] Only valid transitions from `PENDING` to `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, or `CANCELLED` are accepted
  - [ ] Confirming a reservation marks the room `RESERVED` and cancelling it restores `AVAILABLE`
  - [ ] Reservation listing supports user, status, and date filters with pagination
  - [ ] Unit and integration tests cover every state transition and the complete booking flow
- Risks: Concurrent reservation requests require a database-safe overlap strategy.
- Progress: 0%
- Branch: `feature/milestone-09-advanced`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-018 | Publish reservation domain events

- Priority: P2
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-017
- Description: Publish reservation lifecycle events after commit and process notifications and audit records asynchronously.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/reservations/`, `src/main/kotlin/com/lgzarturo/springbootcourse/common/events/`, `src/main/kotlin/com/lgzarturo/springbootcourse/config/`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/reservations/`
- Out of scope: Kafka, RabbitMQ, external email delivery, and a persistent outbox
- Business value: Decouples notifications and audit behavior from the reservation transaction.
- Acceptance:
  - [ ] Created, confirmed, checked-in, checked-out, and cancelled reservations publish distinct events
  - [ ] Events are delivered through `@TransactionalEventListener` with `AFTER_COMMIT`
  - [ ] Listeners execute asynchronously on a configured task executor
  - [ ] A listener failure does not roll back or fail the completed reservation operation
  - [ ] Audit output records actor, action, reservation identifier, and timestamp
  - [ ] Integration tests verify event publication timing and listener execution
- Risks: Asynchronous processing is in-process and must be documented as non-durable.
- Progress: 0%
- Branch: `feature/milestone-09-advanced`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-019 | Add cache for frequent queries

- Priority: P2
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-013
- Description: Cache hotel and room availability queries with explicit TTL, invalidation, and Actuator cache metrics.
- Scope: `build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/config/cache/`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/hotels/`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/rooms/`, `src/test/kotlin/com/lgzarturo/springbootcourse/config/cache/`
- Out of scope: Redis infrastructure, distributed cache coordination, and caching mutable reservation state
- Business value: Reduces database load for repeated hotel and room availability queries.
- Acceptance:
  - [ ] Caffeine is configured as the cache provider with a five-minute TTL
  - [ ] Repeating a hotel lookup returns the cached value without a second repository call
  - [ ] Hotel and room mutations evict or update the affected cache entries
  - [ ] Actuator exposes cache get, put, and eviction metrics
  - [ ] Tests verify cache miss, hit, update, and eviction behavior
  - [ ] Cache keys do not contain credentials or unbounded user-controlled data
- Risks: Stale availability must never be used to authorize a reservation.
- Progress: 0%
- Branch: `feature/milestone-09-advanced`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-020 | Implement reservation payments

- Priority: P1
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-017
- Description: Add simulated payment processing, status queries, and refunds for confirmed reservations behind a replaceable gateway boundary.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/payments/`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/reservations/`, `src/main/resources/db/migration/V7__Add_Payments_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/payments/`
- Out of scope: Real card data, external payment provider credentials, and PCI compliance certification
- Business value: Completes the reservation confirmation path without storing sensitive payment data.
- Acceptance:
  - [ ] A payment can be created only for a reservation in `CONFIRMED` status
  - [ ] A completed payment moves the reservation to `IN_PROGRESS`
  - [ ] A refund can be created only for a `COMPLETED` payment
  - [ ] The payment amount equals the reservation total price
  - [ ] Payment status transitions cover `PENDING`, `COMPLETED`, `FAILED`, and `REFUNDED`
  - [ ] Unit and integration tests cover successful, failed, duplicate, and refund flows
- Risks: The simulated gateway must make it impossible to persist raw payment credentials.
- Progress: 0%
- Branch: `feature/milestone-10-capstone`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-021 | Implement stay reviews

- Priority: P1
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-017, BC-020
- Description: Allow the guest who completed a reservation to create one rated review and expose paginated hotel review summaries.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/reviews/`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/reservations/`, `src/main/resources/db/migration/V8__Add_Reviews_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/reviews/`
- Out of scope: Moderation workflows, image uploads, and public user profiles
- Business value: Provides trustworthy hotel feedback and a measurable guest experience signal.
- Acceptance:
  - [ ] Only the user who owns a `COMPLETED` reservation can create its review
  - [ ] A reservation can have at most one review
  - [ ] Ratings outside the inclusive range 1-5 are rejected with `400 Bad Request`
  - [ ] `GET /api/v1/hotels/{id}/reviews` returns paginated reviews and the average rating
  - [ ] Review detail returns `404 Not Found` for an unknown review identifier
  - [ ] Unit and integration tests cover authorization, uniqueness, validation, pagination, and average-rating calculation
- Risks: Review ownership must be checked from authenticated identity, not a request body field.
- Progress: 0%
- Branch: `feature/milestone-10-capstone`
- Reviewer: reviewer
- Last update: 2026-09-16

### BC-022 | Complete integrated release

- Priority: P0
- Status: TODO
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-012, BC-015, BC-016, BC-018, BC-019, BC-020, BC-021
- Description: Verify the complete hotel journey, consolidate release documentation, and publish the first production-ready application version.
- Scope: `src/test/kotlin/com/lgzarturo/springbootcourse/`, `README.md`, `HISTORIAL.md`, `BACKLOG.md`, `.github/workflows/`, `Dockerfile`, `docker-compose.yml`
- Out of scope: New business capabilities not required by the end-to-end journey and optional Pokemon companion management
- Business value: Demonstrates that the course project operates as one coherent, releasable system.
- Acceptance:
  - [ ] An automated end-to-end test passes for register, login, hotel search, room selection, reservation, payment, check-in, check-out, and review
  - [ ] `./gradlew check` passes with global coverage at or above 85 percent
  - [ ] Swagger UI documents every released endpoint and the JWT authorization flow
  - [ ] `./gradlew bootRun` starts without application errors under the documented local profile
  - [ ] Docker Compose starts the documented application and database environment
  - [ ] README and HISTORIAL document setup, endpoints, architecture, tests, and completed milestones
  - [ ] Release notes and tag `v1.0.0` identify the verified release contents
- Risks: The end-to-end flow can reveal cross-feature coupling that violates feature isolation.
- Progress: 0%
- Branch: `feature/milestone-10-capstone`
- Reviewer: reviewer
- Last update: 2026-09-16

## Archive
### BC-006 | Implement user management CRUD

- Priority: P1
- Status: DONE
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-005
- Description: Complete user persistence, application services, and REST endpoints so trainers can register, view, update, deactivate, and list users without exposing passwords.
- Scope: BACKLOG.md, build.gradle.kts, src/main/kotlin/com/lgzarturo/springbootcourse/features/users/, src/main/resources/db/migration/V3__Add_Users_Table.sql, src/test/kotlin/com/lgzarturo/springbootcourse/features/users/
- Out of scope: JWT authentication, roles, authorities, and user-facing administration UI
- Business value: Provides the account foundation required by authentication and reservations.
- Acceptance:
  - [x] `POST /api/v1/users` returns `201 Created` for valid input and never serializes a password
  - [x] A duplicate email returns `409 Conflict` and does not create a second user
  - [x] Passwords are persisted as BCrypt hashes and are never stored in plain text
  - [x] `GET /api/v1/users/{id}` returns `404 Not Found` for an unknown identifier
  - [x] `Email`, `Password`, and `PhoneNumber` reject invalid values through their domain invariants
  - [x] Unit, `@WebMvcTest`, and `@DataJpaTest` coverage exists for the implemented user flows
- Risks: Existing value objects and Flyway version ordering must remain compatible.
- Progress: 100%
- Branch: `feature/milestone-05-security`
- Reviewer: reviewer
- Last update: 2026-09-16


### BC-001 | Establish the Kotlin project foundation

- Priority: P1
- Status: DONE
- Type: feature
- Owner: spring-kotlin team
- Depends on: none
- Description: Establish the initial Kotlin and Spring Boot project, local development profile, build tooling, and course workspace.
- Scope: `build.gradle.kts`, `settings.gradle.kts`, `gradlew`, `src/main/`, `src/test/`
- Out of scope: Hotel business features and production deployment
- Business value: Provides the executable foundation for all later milestones.
- Acceptance:
  - [x] The project compiles with the documented Gradle command
  - [x] The dev profile starts with the documented local database configuration
  - [x] The test source set is configured and executable
- Progress: 100%
- Last update: 2026-09-16

### BC-002 | Add basic request handling

- Priority: P1
- Status: DONE
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-001
- Description: Add the initial ping, example, and hotel HTTP flows with controller and service tests.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/ping/`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/examples/`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/hotels/`, `src/test/`
- Out of scope: Authentication and reservations
- Business value: Establishes the first working REST examples and the course TDD loop.
- Acceptance:
  - [x] Ping and example endpoints return their documented responses
  - [x] Hotel controller and service tests pass
  - [x] Controllers delegate business operations to services
- Progress: 100%
- Last update: 2026-09-16

### BC-003 | Add JPA persistence

- Priority: P1
- Status: DONE
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-002
- Description: Add domain-to-JPA translation, repositories, migrations, and integration coverage for persistent hotel data.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/hotels/`, `src/main/resources/db/migration/`, `src/test/`
- Out of scope: User persistence and external databases beyond configured profiles
- Business value: Makes hotel data durable and demonstrates the persistence boundary.
- Acceptance:
  - [x] Hotel entities and repositories persist and retrieve hotel records
  - [x] Flyway migrations run in the configured migration profile
  - [x] Domain models remain free of Spring and JPA annotations
- Progress: 100%
- Last update: 2026-09-16

### BC-004 | Implement the hotels CRUD

- Priority: P1
- Status: DONE
- Type: feature
- Owner: spring-kotlin team
- Depends on: BC-003
- Description: Deliver the complete hotel CRUD with validation, pagination, standardized errors, and integration coverage.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/hotels/`, `src/main/kotlin/com/lgzarturo/springbootcourse/common/`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/hotels/`
- Out of scope: Rooms, users, and security
- Business value: Provides the reference MVC feature for the rest of the course.
- Acceptance:
  - [x] Hotel create, list, detail, update, and delete operations pass their integration tests
  - [x] Invalid requests return standardized validation responses
  - [x] Listing supports neutral pagination and sorting
- Progress: 100%
- Last update: 2026-09-16

### BC-005 | Establish feature architecture and validation

- Priority: P1
- Status: DONE
- Type: refactor
- Owner: spring-kotlin team
- Depends on: BC-004
- Description: Establish feature-isolated MVC structure, value objects for users, global exception handling, API configuration, and project quality tooling.
- Scope: `src/main/kotlin/com/lgzarturo/springbootcourse/features/`, `src/main/kotlin/com/lgzarturo/springbootcourse/common/`, `src/main/kotlin/com/lgzarturo/springbootcourse/config/`, `config/`, `Makefile`
- Out of scope: Completing user persistence and authentication
- Business value: Defines the conventions that keep future features maintainable and independently testable.
- Acceptance:
  - [x] Existing features follow the documented MVC-by-feature package structure
  - [x] User value objects enforce their domain invariants through unit tests
  - [x] Global exception handling maps validation, not-found, conflict, and fallback errors to the documented responses
  - [x] OpenAPI base configuration, CORS, Actuator, Sentry, KtLint, Detekt, JaCoCo, and Testcontainers are configured
- Progress: 100%
- Last update: 2026-09-16

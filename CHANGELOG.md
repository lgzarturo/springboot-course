# Changelog

Todos los cambios notables del proyecto serán documentados aquí.

El formato está basado en
[Keep a Changelog](https://keepachangelog.com/en/1.0.0/), y este proyecto se
basa en [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0-beta.2] - 2026-04-19

Milestone 4: Integración de herramientas de IA, mejoras en documentación y
automatización — Se integró `code-review-graph` en los hooks de agentes AI,
se amplió la documentación del curso con ejercicios complementarios y mejores
prácticas de JPA, y se establecieron configuraciones de estilo y auditoría de
código.

### Added

- **Integración de code-review-graph en agentes AI**:
  - Configuración de hooks para Claude utilizando `uvx` para invocar code-review-graph
  - Integración en todos los agentes AI del proyecto
- **Skills y agentes de automatización para Claude**:
  - Nuevos skills en `.claude/skills/` para automatización de tareas
  - Comandos personalizados para operaciones comunes de desarrollo
- **Configuración de estilo y formato**:
  - Reglas de estilo documentadas para mantener consistencia en el código
  - Configuración para formateo automático de archivos
- **Documentación de auditoría de código**:
  - Guía completa sobre procesos de auditoría de código
  - Documentación para AGENTS.md y QWEN.md

### Changed

- **Documentación de comandos**:
  - Mejora en las instrucciones de conventional-commit
  - Actualización de guías de estilo y convenciones
- **Arquitectura documentada**:
  - Documentación actualizada para reflejar la migración a MVC por features
  - README y guías narrativas mejoradas

### Fixed

- **Hooks de Claude**: Corrección en la invocación de code-review-graph mediante `uvx`
- **Seguridad en Sentry**: Sanitización de mensajes enviados a Sentry para prevenir XSS
- **Documentos**: Corrección de errores en documentos de guía

### Configuration

- **Carpetas ignoradas**: Configuración para ignorar borradores de documentación
- **Carpeta pending-wip**: Organización de trabajo en progreso

## [1.0.0-beta.1] - 2025-11-14

Milestone 3: Refactorización a Screaming Architecture completa, mejoras de
seguridad, soporte para migraciones automáticas y herramientas de desarrollo
con IA — Se completó la migración del dominio `example` a la nueva estructura
por features, se mejoró la persistencia de hoteles y habitaciones, se
corrigieron vulnerabilidades de seguridad y se agregó documentación extensa
del curso (semanas 4 y 5).

### Added

- **Screaming Architecture para `example`**: Migración completa del dominio
  - Reestructuración de paquetes siguiendo MVC por Features
  - `ExampleUseCasePort`, `ExampleRepositoryPort` en `application/ports/`
  - `ExampleServiceConfig` para inyección de dependencias
  - `ExampleController` migrado a `adapters/rest/`
  - `ExampleRepositoryAdapter` y `ExampleJpaRepository` en `adapters/persistence/`
- **Mejoras en Feature Hotels**:
  - `HotelRoomJpaRepository` para persistencia de habitaciones por hotel
  - `HotelServiceConfig` para configuración de dependencias
  - `PageResponse` mejorado con soporte de paginación
  - Endpoints REST ampliados con soporte de cliente REST (`RestTestClientConfig`)
- **Soporte para migraciones automáticas**:
  - Script `generate-migration.sh` para generar migraciones de BD automáticamente
  - Script `generate-migration.ps1` para entornos Windows
  - Perfil `application-migration.yaml` para generación de DDL desde entidades JPA
  - `docker-compose.migration.yml` para base de datos temporal de migración
  - `Makefile` con tareas de automatización del proyecto
- **Documentación del curso — Semana 4**:
  - Screaming Architecture: guía completa con ejemplos
  - Configuración de Swagger UI y documentación OpenAPI
  - Implementación de funcionalidades con patrones MVC
  - Clean Core: separación de dominio e infraestructura
  - Validación de datos con Bean Validation
- **Documentación del curso — Semana 5**:
  - Spring Security: autenticación y autorización
  - TDD en práctica: Red/Green/Refactor para hoteles
  - Retos y fases del curso con ejercicios prácticos
  - Cuestionario sobre Spring Boot + Kotlin
- **Guías de aprendizaje**:
  - `docs/lectures/quickstart-springboot-kotlin.md`: guía rápida de Spring Boot
    con Kotlin
  - `docs/learning-path/narrative-guide.md`: guía narrativa del camino de
    aprendizaje
  - Roadmap para aprender Java y Spring Boot con hipervínculos
- **Herramientas de desarrollo con IA**:
  - `CLAUDE.md` con instrucciones para Claude Code
  - `.junie/AGENTS.md` para Junie (JetBrains AI)
  - Skills de agentes en `.agents/skills/`: `aws-rds-spring-boot-integration`,
    `java-coding-standards`, `java-spring-boot`, `jpa-patterns`,
    `kotlin-spring`, `kotlin-spring-boot`, `langchain4j-spring-boot-integration`,
    `debug-spring-boot`, `refactor-spring-boot`
- **Utilidades de tests**:
  - `ClasspathDiagnosticTest` para diagnóstico del classpath en pruebas E2E
  - `MockkTestConfig` centralizado para configuración de mocks
  - `GenerateDdlTest` para validar generación de esquema DDL

### Changed

- **Arquitectura `ping`**: Refactorización para eliminar casos de uso
  redundantes y ajustar al patrón de features
- **`RoomEntity`**: Mejoras en la entidad con nuevos campos y relaciones
- **Detekt**: Actualización de configuración a versión 2.0.0-alpha.1 con reglas
  ajustadas para el proyecto
- **Build (`build.gradle.kts`)**: Actualización de dependencias, soporte para
  Spring Boot actualizado y nuevas tareas de Gradle
- **Gradle Wrapper**: Actualización de versión
- **Tests de integración**: Renombrado `application-tests.yaml` →
  `application-test.yaml` para consistencia
- **`HotelE2ETest`**: Refactorización completa con mejoras en cobertura y
  claridad de pruebas
- **README**: Reestructuración completa con justificación del proyecto,
  estructura de módulos y guías de contribución

### Fixed

- **Seguridad — XSS en `SentryController`**: Corrección de vulnerabilidad de
  Cross-site scripting detectada por GitHub code scanning (alerta #8)
- **Sanitización de mensajes en Sentry**: Los mensajes enviados a Sentry ahora
  son sanitizados para evitar inyección de datos sensibles o maliciosos
- **Compatibilidad de Swagger UI**: Corrección de incompatibilidad con la
  versión actual de Spring Boot
- **Imports innecesarios**: Limpieza en múltiples archivos de dominio y
  adaptadores

### Removed

- **Stubs vacíos restantes**: Eliminación definitiva de `cart/`,
  `gamification/`, `payments/`, `pokemon/`, `reservations/`, `reviews/`,
  `services/` que solo contenían `PackageInfo.kt`
- **Dependencias de infraestructura en el core**: El dominio ahora está
  completamente libre de dependencias de Spring/JPA

### Tests

- Pruebas unitarias para `UserService` con value objects
- Pruebas de integración ampliadas para `HotelController` y `HotelService`
- `HotelJpaRepositoryIntegrationTest` con Testcontainers
- Configuración mejorada de MockK para pruebas más robustas
- Soporte de Testcontainers condicional (`DockerAvailableCondition`) con
  fallback a H2

### Configuration

- Nuevo perfil `migration` para generación automática de DDL
- `docker-compose.migration.yml` para entorno de migración aislado
- `CLAUDE.md` y `.junie/AGENTS.md` para herramientas de IA
- Scripts de migración disponibles para Linux/Mac y Windows

## [0.0.3] - 2025-11-14

Milestone 2: Migración a Screaming Architecture y nuevas features de dominio —
Se implementó la reorganización del proyecto siguiendo Screaming Architecture
(MVC por Features), se eliminaron patrones hexagonales redundantes, y se
añadieron las entidades de Hotel, Room y User con sus servicios y pruebas.

### Added

- **Screaming Architecture**: Reorganización completa del proyecto siguiendo MVC
  por Features
  - Nuevo directorio `features/` con estructura autocontenida por feature
  - Migración de `hotels/`, `example/`, `ping/`, `users/`, `rooms/` a la nueva
    estructura
  - Eliminación de `shared/` y stubs vacíos (`cart/`, `gamification/`,
    `payments/`, etc.)
  - Documentación en `docs/architecture/mvc-migration-plan.md`
- **Feature Hotels**: Implementación completa CRUD
  - `HotelEntity`, `HotelService`, `HotelController` con endpoints REST
  - `HotelRepository` y `HotelJpaRepository` para persistencia
  - `HotelSearchCriteria` para búsquedas avanzadas
  - DTOs para requests y responses con validaciones
- **Feature Users**: Sistema de usuarios con value objects
  - Entidad `User` con relaciones y validaciones
  - Value objects: `Email`, `Password`, `PhoneNumber`, `UserId`
  - `UserService` con operaciones de creación y gestión
  - Excepciones específicas: `DuplicateEmailException`
- **Feature Rooms**: Persistencia de habitaciones
  - `RoomEntity` con tipos y temas Pokémon
  - `RoomJpaRepository` y modelo `Room`
- **Documentación del curso**:
  - Guía sobre Screaming Architecture
  - Roadmap para aprender Java y Spring Boot
  - Semana 5: retos y fases del curso
  - Cuestionario sobre Spring Boot + Kotlin

### Changed

- **Arquitectura**: Migración de Arquitectura Hexagonal a MVC por Features
  - Eliminación de ports redundantes (`*UseCasePort`, `*RepositoryPort`)
  - Eliminación de adapters (`adapters/rest/`, `adapters/persistence/`)
  - Eliminación de configs por feature (`*ServiceConfig.kt`)
  - Mappers convertidos a companion objects (`fromDomain()`)
- **Detekt**: Actualización a versión 2.0.0-alpha.1 con nueva configuración
- **Build**: Actualización de dependencias y soporte en `build.gradle.kts`
- **Pruebas**: Mejoras en configuración MockK y anotaciones de tests
- **OpenAPI**: Actualización de Swagger UI y `@Operation` annotations
- **README**: Actualización con justificación del proyecto y enlaces relevantes

### Fixed

- Compatibilidad de Swagger UI
- Imports innecesarios en múltiples archivos
- Configuración para adaptarse a varios entornos

### Removed

- Paquetes stubs vacíos: `cart/`, `gamification/`, `payments/`, `pokemon/`,
  `reservations/`, `reviews/`, `services/`
- Puertos redundantes con una sola implementación
- Clases de configuración por feature (`*ServiceConfig.kt`)
- Mappers separados (integrados en companion objects)

### Tests

- Tests unitarios y de integración para `HotelService` y `HotelController`
- Tests para `UserService` con value objects
- Configuración mejorada de MockK en pruebas

### Configuration

- Nueva estructura de paquetes en `src/main/kotlin/`
- Configuración de Detekt para análisis estático
- Scripts de migración Flyway con nomenclatura actualizada

## [0.0.2] - 2025-11-09

Milestone 1: Fundamentos de persistencia y configuración básica — continuación y
cierre. En esta versión se consolida la persistencia, los perfiles de Spring, la
configuración de Gradle y Sentry, y se practica TDD con un CRUD completo para la
entidad `Example`.

### Added

- Persistencia (JPA/Hibernate) y adaptación hexagonal para Example:
  - Entidad `ExampleEntity`, puerto `ExampleRepositoryPort` y adaptador
    `ExampleRepositoryAdapter`.
  - Repositorio `ExampleJpaRepository` y migración inicial con Flyway.
- Caso de uso `ExampleUseCase` y servicio de dominio `ExampleService` con
  operaciones CRUD.
- Controlador REST `ExampleController` con endpoints para CRUD de `Example`:
  - GET /api/v1/examples
  - GET /api/v1/examples/{id}
  - POST /api/v1/examples
  - PUT /api/v1/examples/{id}
  - PATCH /api/v1/examples/{id} — actualizaciones parciales
  - DELETE /api/v1/examples/{id}
- DTOs `ExampleRequest`, `ExamplePatchUpdate` y `ExampleResponse` con
  validaciones.
- Manejo de errores y validaciones específicas (incluye
  `ConstraintViolationException`).
- Perfiles de configuración (`application-dev.yaml`, `application-prod.yaml`,
  `application-tests.yaml`) y configuración por entorno.
- Integración de Sentry con endpoints de prueba en `SentryController` y
  variables de entorno en CI.
- Integración de Detekt y tareas de calidad de código.
- Archivos HTTP para pruebas (`http/example.http`, `http/sentry.http`).
- Documentación del curso:
  - Semana 2: perfiles y configuración.
  - Semana 3: CRUD con TDD y entidades.

### Changed

- Ampliación de puertos y servicio de dominio (`ExampleService`) con nuevas
  operaciones, incluyendo `findById`.
- Organización de pruebas con clases anidadas y mayor legibilidad.
- Ajustes de formato y simplificación de métodos en múltiples clases.
- Actualización de README y guías (TDD, entidades, commits, contribución).

### Fixed

- Correcciones menores en la aplicación y en validaciones de entrada.

### Tests

- Enfoque TDD: pruebas unitarias y de integración completas para
  `ExampleService` y `ExampleController`, cubriendo casos del CRUD y manejo de
  errores.

### Configuration

- Perfiles activados por entorno y variables de entorno documentadas.
- Configuración de CI ajustada para Sentry y publicación.
- Gradle: nuevas tareas de verificación estática (Detekt) y calidad.

## [0.0.1] - 2025-10-25

### Added

- Implementación inicial del proyecto Spring Boot Course
- Arquitectura Hexagonal (Ports & Adapters)
- Endpoint Ping API con implementación TDD
  - GET /api/v1/ping - Ping simple
  - GET /api/v1/ping/{message} - Ping con mensaje personalizado
  - GET /api/v1/ping/health - Health check
- Configuración de Spring Boot 3.5.6 con Kotlin 2.0.21
- Configuración de calidad de código:
  - KTLint para formateo de código
  - Detekt para análisis estático
  - JaCoCo para cobertura de tests
- Documentación del proyecto:
  - README.md completo con guía de inicio
  - ARCHITECTURE.md con detalles de arquitectura
  - DEVELOPMENT_GUIDE.md con guía de desarrollo
  - IMPLEMENTATION_CHECKLIST.md con roadmap
- Tests unitarios y de integración:
  - PingServiceTest - Tests del dominio
  - PingControllerTest - Tests de controladores
- Configuración de OpenAPI/Swagger para documentación de API
- Configuración de Actuator para monitoreo
- Integración con Sentry para tracking de errores
- Soporte para H2 (desarrollo) y PostgreSQL (producción)
- Documentación del curso en docs/course/
  - Semana 0: Historia del viaje y método de aprendizaje
  - Semana 1: Dependencias
  - Semana 2: HTTP, Spring Boot y componentes clave
- Flujo de trabajo completo para commits con Conventional Commits
- Documentación de versionado semántico
- Guías para generación automática de changelog
- Plantilla de mensajes de commit (.gitmessage)
- Guía rápida de commits (COMMIT_GUIDE.md)
- Workflow completo documentado (WORKFLOW.md)
- Flujo de trabajo para publicación de versiones
- Dependencias OpenAPI y configuración de Qodana
- Soporte para observabilidad
- Flujos de trabajo de CI/CD y changelog
- Configuración de OpenAPI y manejo de excepciones global
- Nuevas guías de workflow y commits con `.releaserc.json`

### Changed

- Actualización de documentación en docs/course/week-01/01-dependencias.md
- Actualización de GlobalExceptionHandler.kt para mejorar validaciones
- Workflows actualizados con permisos refinados

### Fixed

- Corrección de error menor de prueba en la aplicación
- Ajustes en la configuración para versión automatizada y flujo de commits

### Configuration

- Gradle 8.x con Kotlin DSL
- Java 21 toolchain
- Spring Boot DevTools para desarrollo
- CORS configurado
- Manejo global de excepciones

[Unreleased]: https://github.com/lgzarturo/springboot-course/compare/v1.0.0-beta.2...HEAD
[0.0.1]: https://github.com/lgzarturo/springboot-course/releases/tag/v0.0.1
[0.0.2]: https://github.com/lgzarturo/springboot-course/releases/tag/v0.0.2
[0.0.3]: https://github.com/lgzarturo/springboot-course/releases/tag/v0.0.3
[1.0.0-beta.1]: https://github.com/lgzarturo/springboot-course/releases/tag/v1.0.0-beta.1
[1.0.0-beta.2]: https://github.com/lgzarturo/springboot-course/releases/tag/v1.0.0-beta.2

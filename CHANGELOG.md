# Changelog

Todos los cambios notables del proyecto serán documentados aquí.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
y este proyecto se basa en [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.2] - 2025-11-09

Milestone 1: Fundamentos de persistencia y configuración básica — continuación y cierre. En esta versión se consolida la persistencia, los perfiles de Spring, la configuración de Gradle y Sentry, y se practica TDD con un CRUD completo para la entidad `Example`.

### Added
- Persistencia (JPA/Hibernate) y adaptación hexagonal para Example:
  - Entidad `ExampleEntity`, puerto `ExampleRepositoryPort` y adaptador `ExampleRepositoryAdapter`.
  - Repositorio `ExampleJpaRepository` y migración inicial con Flyway.
- Caso de uso `ExampleUseCase` y servicio de dominio `ExampleService` con operaciones CRUD.
- Controlador REST `ExampleController` con endpoints para CRUD de `Example`:
  - GET /api/v1/examples
  - GET /api/v1/examples/{id}
  - POST /api/v1/examples
  - PUT /api/v1/examples/{id}
  - PATCH /api/v1/examples/{id} — actualizaciones parciales
  - DELETE /api/v1/examples/{id}
- DTOs `ExampleRequest`, `ExamplePatchUpdate` y `ExampleResponse` con validaciones.
- Manejo de errores y validaciones específicas (incluye `ConstraintViolationException`).
- Perfiles de configuración (`application-dev.yaml`, `application-prod.yaml`, `application-tests.yaml`) y configuración por entorno.
- Integración de Sentry con endpoints de prueba en `SentryController` y variables de entorno en CI.
- Integración de Detekt y tareas de calidad de código.
- Archivos HTTP para pruebas (`http/example.http`, `http/sentry.http`).
- Documentación del curso:
  - Semana 2: perfiles y configuración.
  - Semana 3: CRUD con TDD y entidades.

### Changed
- Ampliación de puertos y servicio de dominio (`ExampleService`) con nuevas operaciones, incluyendo `findById`.
- Organización de pruebas con clases anidadas y mayor legibilidad.
- Ajustes de formato y simplificación de métodos en múltiples clases.
- Actualización de README y guías (TDD, entidades, commits, contribución).

### Fixed
- Correcciones menores en la aplicación y en validaciones de entrada.

### Tests
- Enfoque TDD: pruebas unitarias y de integración completas para `ExampleService` y `ExampleController`, cubriendo casos del CRUD y manejo de errores.

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

[0.0.1]: https://github.com/lgzarturo/springboot-course/releases/tag/v0.0.1

[0.0.2]: https://github.com/lgzarturo/springboot-course/releases/tag/v0.0.2
# Changelog

Todos los cambios notables del proyecto serán documentados aquí.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
y este proyecto se basa en [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.1] - 2025-10-20

### Added
- Flujo de trabajo completo para commits con Conventional Commits
- Documentación de versionado semántico
- Guías para generación automática de changelog
- Plantilla de mensajes de commit (.gitmessage)
- Guía rápida de commits (COMMIT_GUIDE.md)
- Workflow completo documentado (WORKFLOW.md)

## [0.0.1-SNAPSHOT] - 2025-10-20

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

### Configuration
- Gradle 8.x con Kotlin DSL
- Java 21 toolchain
- Spring Boot DevTools para desarrollo
- CORS configurado
- Manejo global de excepciones

[Unreleased]: https://github.com/lgzarturo/springboot-course/compare/v0.0.1-SNAPSHOT...HEAD

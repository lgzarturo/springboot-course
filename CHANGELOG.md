# [1.0.0-beta.2](https://github.com/lgzarturo/springboot-course/compare/v1.0.0-beta.1...v1.0.0-beta.2) (2025-10-25)


### Bug Fixes

* **doc:** update docs/course/week-01/01-dependencias.md ([185a2b0](https://github.com/lgzarturo/springboot-course/commit/185a2b0bd72d4516f5ef6404e8a80f0504d3acf6))
* **doc:** update docs/course/week-01/01-dependencias.md ([5e7c5f4](https://github.com/lgzarturo/springboot-course/commit/5e7c5f4edabd89bdb708bdfa893d0258ba7e33c4))
* **validation:** update GlobalExceptionHandler.kt ([0850fc1](https://github.com/lgzarturo/springboot-course/commit/0850fc123027ef58e971af811fb2259c3158046f))


### Features

* **build:** configurar para versión automatizada y flujo de commits ([7c4c27a](https://github.com/lgzarturo/springboot-course/commit/7c4c27a4e54556a93deac11e2bb4482eacb8e1a8)), closes [#2](https://github.com/lgzarturo/springboot-course/issues/2)
* **build:** configurar para versión automatizada y flujo de commits ([6c00404](https://github.com/lgzarturo/springboot-course/commit/6c004042254394990ccb9fc4cc27649326e57ccf)), closes [#2](https://github.com/lgzarturo/springboot-course/issues/2)
* **ci:** actualizar workflows con permisos refinados. ([3915f6c](https://github.com/lgzarturo/springboot-course/commit/3915f6c156e801dd4a7a8120dc6d3395084ea3ce))
* **ci:** agregar flujos de trabajo de CI/CD y changelog ([13eb7c3](https://github.com/lgzarturo/springboot-course/commit/13eb7c30fc71d67930f7376dceaf4ad5cedc82be)), closes [#2](https://github.com/lgzarturo/springboot-course/issues/2)
* **config:** agregar configuración de OpenAPI y manejo de excepciones ([dd0b923](https://github.com/lgzarturo/springboot-course/commit/dd0b92336370c389893dee70b09e11458ecc39bc)), closes [#5](https://github.com/lgzarturo/springboot-course/issues/5) [#6](https://github.com/lgzarturo/springboot-course/issues/6)
* **docs:** agregar nuevas guías de workflow y commits `.releaserc.json` ([616a0df](https://github.com/lgzarturo/springboot-course/commit/616a0df2a044fbc1f61d3076025b12db1a807086))

# 1.0.0-beta.1 (2025-10-21)


### Bug Fixes

* corrige error menor de prueba en la aplicación ([746a40a](https://github.com/lgzarturo/springboot-course/commit/746a40ae400e8f11d501820aa0368adc06ea5744))
* forzar próximo release a partir de 0.0.1 ([d599c6c](https://github.com/lgzarturo/springboot-course/commit/d599c6ca44f3fb4af44d80915f8a684ae2773ef3))


### Features

* agregar flujo de trabajo para publicación de versiones ([2de3737](https://github.com/lgzarturo/springboot-course/commit/2de3737389da78fcc42b9025db3397acf6a5ab49))
* **build:** agregar dependencias OpenAPI y configuración de Qodana ([20afbd2](https://github.com/lgzarturo/springboot-course/commit/20afbd2df7d0569fd35c1df98b61ec1446488c60))
* **build:** agregar soporte la observabilidad ([d3cfafc](https://github.com/lgzarturo/springboot-course/commit/d3cfafc9edbdf3feb01c897964072785ec6faea7))
* **project:** configura inicialización con SpringBoot+Kotlin ([b0e918d](https://github.com/lgzarturo/springboot-course/commit/b0e918d69d98a2bf30c2cc3af6966fc0c57f37e2))

# [1.0.0-beta.2](https://github.com/lgzarturo/springboot-course/compare/v1.0.0-beta.1...v1.0.0-beta.2) (2025-10-21)


### Bug Fixes

* corrige error menor de prueba en la aplicación ([746a40a](https://github.com/lgzarturo/springboot-course/commit/746a40ae400e8f11d501820aa0368adc06ea5744))

# 1.0.0-beta.1 (2025-10-21)


### Features

* agregar flujo de trabajo para publicación de versiones ([2de3737](https://github.com/lgzarturo/springboot-course/commit/2de3737389da78fcc42b9025db3397acf6a5ab49))
* **build:** agregar dependencias OpenAPI y configuración de Qodana ([20afbd2](https://github.com/lgzarturo/springboot-course/commit/20afbd2df7d0569fd35c1df98b61ec1446488c60))
* **build:** agregar soporte la observabilidad ([d3cfafc](https://github.com/lgzarturo/springboot-course/commit/d3cfafc9edbdf3feb01c897964072785ec6faea7))
* **project:** configura inicialización con SpringBoot+Kotlin ([b0e918d](https://github.com/lgzarturo/springboot-course/commit/b0e918d69d98a2bf30c2cc3af6966fc0c57f37e2))

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

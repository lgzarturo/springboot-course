# 🚀 Spring Boot Course — API REST Real

[![Release](https://img.shields.io/github/v/release/lgzarturo/springboot-course?label=Latest%20Release)](https://github.com/lgzarturo/springboot-course/releases/tag/v0.0.3)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.8-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue.svg)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-CC--BY--4.0-yellow.svg)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/lgzarturo/springboot-course/ci.yml?branch=main)](https://github.com/lgzarturo/springboot-course/actions)

Este repositorio contiene el código fuente y la documentación para el curso de
Spring Boot y Kotlin, enfocado en construir una API REST real siguiendo
estándares de la industria y mejores prácticas de arquitectura.

## 📋 Tabla de Contenidos

1. [🚀 Spring Boot Course — API REST Real](#-spring-boot-course--api-rest-real)
   1. [📋 Tabla de Contenidos](#-tabla-de-contenidos)
   2. [Descripción General](#descripción-general)
   3. [Stack Tecnológico](#stack-tecnológico)
   4. [Características Principales](#características-principales)
   5. [Requisitos](#requisitos)
   6. [Configuración del Entorno](#configuración-del-entorno)
   7. [Comandos y Scripts](#comandos-y-scripts)
      1. [Desarrollo](#desarrollo)
      2. [Calidad y Estilo de Código](#calidad-y-estilo-de-código)
      3. [Base de Datos](#base-de-datos)
   8. [⚠️ Nota Importante para Desarrolladores](#️-nota-importante-para-desarrolladores)
   9. [Estructura del Proyecto](#estructura-del-proyecto)
      1. [Principios de la Estructura](#principios-de-la-estructura)
   10. [Variables de Entorno](#variables-de-entorno)
   11. [Pruebas (Testing)](#pruebas-testing)
   12. [Documentación de la API](#documentación-de-la-api)
   13. [🎓 Guía de Aprendizaje: De Novato a Maestro](#-guía-de-aprendizaje-de-novato-a-maestro)
       1. [🗺️ Ruta de Aprendizaje](#️-ruta-de-aprendizaje)
       2. [📚 Documentación Detallada](#-documentación-detallada)
       3. [🚀 ¿Por dónde empezar?](#-por-dónde-empezar)
       4. [Artículos Relacionados](#artículos-relacionados)
   14. [Licencia](#licencia)
   15. [**¡Happy Coding! 🚀**](#happy-coding-)

---

## Descripción General

El proyecto consiste en una plataforma de gestión hotelera y ecommerce de
amenities. A través de este curso, aprenderás a resolver problemas de negocio
reales (gestión de habitaciones, reservas, pagos, etc.) aplicando conceptos
técnicos avanzados.

---

## Stack Tecnológico

- **Lenguaje:** Kotlin 2.2.20
- **Framework Principal:** Spring Boot 4.0.3
- **Gestor de Dependencias:** Gradle (Kotlin DSL)
- **Base de Datos:**
  - PostgreSQL (Producción/Desarrollo Local)
  - H2 Database (Pruebas/Desarrollo rápido)
- **Persistencia:** Spring Data JPA + Flyway (Migraciones)
- **Observabilidad:**
  - Sentry (Error Tracking)
  - Spring Actuator + Micrometer + Prometheus
- **Documentación:** SpringDoc OpenAPI (Swagger)

---

## Características Principales

- ✅ **Arquitectura MVC por Features** (Screaming Architecture)
- ✅ **Clean Architecture** y **Domain-Driven Design (DDD)**
- ✅ **Desarrollo Guiado por Pruebas (TDD)**
- ✅ **Calidad de Código:** Detekt (Análisis estático) y KTLint
  (Linter/Formatter)
- ✅ **CI/CD:** GitHub Actions para compilación, pruebas y análisis
- ✅ **Contenedores:** Soporte para Docker Compose y Testcontainers

---

## Requisitos

- **Java 25** (JDK 25)
- **Docker** y **Docker Compose** (para base de datos y Testcontainers)
- **Git**
- **IDE:** IntelliJ IDEA (Recomendado)

---

## Configuración del Entorno

1. **Clonar el repositorio:**

   ```bash
   git clone https://github.com/lgzarturo/springboot-course.git
   cd springboot-course
   ```

2. **Variables de Entorno:** Copia el archivo `.env.example` a `.env` en la raíz del proyecto y ajusta los valores necesarios:

   ```bash
   cp .env.example .env
   # o usando el atajo de make:
   make setup
   ```

   El archivo `.env` contiene la configuración de PostgreSQL, Sentry y parámetros de autenticación.

3. **Infraestructura (Opcional):** Si deseas usar PostgreSQL local mediante
   Docker:

   ```bash
   docker-compose up -d
   ```

---

## Comandos y Scripts

El proyecto utiliza Gradle Wrapper. Puedes ejecutar estos comandos desde la
terminal:

### Desarrollo

- `./gradlew bootRun`: Ejecuta la aplicación localmente (puerto 8080 por
  defecto).
- `./gradlew clean build`: Limpia y construye el proyecto.

### Calidad y Estilo de Código

- `./gradlew checkCodeStyle`: Ejecuta ktlint y detekt.
- `./gradlew formatCode`: Aplica correcciones automáticas de estilo.
- `./gradlew codeQuality`: Ejecuta verificaciones de estilo, tests y genera
  reportes de cobertura.
- `./gradlew fixAll`: Aplica todas las correcciones automáticas disponibles.

### Base de Datos

- `./gradlew generateDDL`: Genera scripts DDL a partir de entidades JPA.
- `./gradlew createMigration -Pmigration_version=1 -Pdescription="nombre"`: Crea
  una nueva migración de Flyway.

---

## ⚠️ Nota Importante para Desarrolladores

> **Este es uno de los cambios cruciales del curso:** El proyecto se migra de
> **Arquitectura Hexagonal** a **MVC por Features** (Screaming Architecture).

**¿Por qué este cambio?**

- La arquitectura hexagonal generaba **5-6 niveles de anidación** por feature
- Interfaces "port" con una sola implementación (ceremonia sin valor)
- Un programador nuevo necesitaba entender la arquitectura antes que el código

**La nueva estructura:**

- **Un paquete = una feature**: `hotels/`, `users/`, `ping/` — la estructura
  grita el negocio
- **Máximo 2 niveles de profundidad**: `features/hotels/dto/` es el máximo
- **Sin ports artificiales**: el service es la capa de negocio
- **Sin adapters**: Spring ya sabe que `@RestController` y `@Repository` son
  adaptadores

El detalle completo de la migración está en
[docs/architecture/mvc-migration-plan.md](docs/architecture/mvc-migration-plan.md).

---

## Estructura del Proyecto

El proyecto usa **MVC organizado por features** con Screaming Architecture: la
estructura del código grita "hotels", "users", "ping" — no "adapters", "ports"
ni "config". Cada feature es autocontenida.

```text
src/main/kotlin/com/lgzarturo/springbootcourse/
├── SpringbootCourseApplication.kt   # Punto de entrada
│
├── config/                          # Infraestructura transversal
│   ├── OpenApiConfig.kt             # Swagger/OpenAPI
│   └── WebConfig.kt                 # CORS, interceptores globales
│
├── common/                          # Componentes reutilizables
│   ├── exception/                   # Manejo global de errores
│   ├── pagination/                  # Utilidades de paginación
│   ├── constants/                   # Constantes de la app
│   └── extensions/                  # Extensiones de Kotlin
│
└── features/                        # Features de negocio (Screaming Architecture)
    ├── hotels/                      # Feature autocontenida
    │   ├── HotelController.kt       # Endpoints REST
    │   ├── HotelService.kt          # Lógica de negocio
    │   ├── HotelRepository.kt       # Acceso a datos
    │   ├── HotelJpaRepository.kt    # Spring Data JPA interface
    │   ├── HotelEntity.kt           # Entidad JPA
    │   ├── Hotel.kt                 # Modelo de dominio
    │   ├── HotelSearchCriteria.kt   # Criterios de búsqueda
    │   └── dto/                     # DTOs request/response
    │
    ├── examples/                    # Feature de referencia
    ├── ping/                        # Health check
    ├── users/                       # Gestión de usuarios
    ├── rooms/                       # Habitaciones
    └── sentry/                      # Monitoreo
```

### Principios de la Estructura

| Principio                    | Descripción                                                                |
| ---------------------------- | -------------------------------------------------------------------------- |
| **Un paquete = una feature** | Todo lo relacionado con una feature vive junto                             |
| **Máximo 2 niveles**         | `hotels/dto/` es el máximo de anidación                                    |
| **Sin ports artificiales**   | El service es la capa de negocio, el repository es la interfaz             |
| **Sin adapters**             | Spring ya separa REST de persistence con `@RestController` y `@Repository` |
| **Sin config por feature**   | Se usa `@Service`, `@Repository`, `@Component` directamente                |
| **Sin stubs vacíos**         | YAGNI — se crean cuando se necesitan                                       |

---

## Variables de Entorno

La aplicación carga automáticamente las variables definidas en el archivo `.env` en la raíz del proyecto (mediante `spring.config.import=optional:file:.env[.properties]`).

### Configuración General y Base de Datos

| Variable                 | Descripción                                                   | Valor por Defecto                               |
| ------------------------ | ------------------------------------------------------------- | ----------------------------------------------- |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring activo (`dev`, `prod`, `test`)               | `dev`                                           |
| `DB_HOST`                | Host del servidor PostgreSQL                                  | `localhost`                                     |
| `DB_PORT`                | Puerto del servidor PostgreSQL                                | `5432`                                          |
| `DB_NAME`                | Nombre de la base de datos                                    | `springboot_db`                                 |
| `DB_USERNAME`            | Usuario de la base de datos                                   | `postgres`                                      |
| `DB_PASSWORD`            | Contraseña del usuario de la base de datos                    | `postgres`                                      |
| `DB_URL`                 | URL JDBC completa de conexión a PostgreSQL                    | `jdbc:postgresql://localhost:5432/springboot_db` |

### Configuración de Sentry (Error Tracking & Observabilidad)

| Variable                    | Descripción                                                                          | Valor por Defecto                     |
| --------------------------- | ------------------------------------------------------------------------------------ | ------------------------------------- |
| `SENTRY_DSN`                | DSN del proyecto en Sentry para envío de eventos y errores                            | *(Verificado en Sentry del proyecto)* |
| `SENTRY_ORG`                | Slug de la organización en Sentry                                                    | `arthurolg-to`                        |
| `SENTRY_PROJECT`            | Slug del proyecto en Sentry                                                          | `springboot-course`                   |
| `SENTRY_AUTH_TOKEN`         | Token de autenticación para subir bundles de código fuente y sourcemaps en el build | -                                     |
| `SENTRY_ENVIRONMENT`        | Entorno reportado a Sentry (`development`, `staging`, `production`)                  | `development`                         |
| `SENTRY_DEBUG`              | Habilita logs detallados de depuración del SDK de Sentry                             | `false`                               |
| `SENTRY_ENABLED`            | Activa o desactiva el reporte de incidencias a Sentry                                | `true`                                |
| `SENTRY_LOGGING_ENABLED`    | Habilita la captura de eventos a nivel de logging                                    | `true`                                |
| `SENTRY_TRACES_SAMPLE_RATE` | Tasa de muestreo de trazas de rendimiento (`0.0` a `1.0`)                             | `1.0`                                 |

### Autenticación y OAuth2 / Auth0 (Opcional)

Si se habilita la seguridad con un proveedor de identidad (IdP) OAuth2 / JWT Resource Server:

| Variable              | Descripción                                                 | Ejemplo                             |
| --------------------- | ----------------------------------------------------------- | ----------------------------------- |
| `AUTH0_DOMAIN`        | Dominio del tenant de Auth0 (`<tenant>.auth0.com` o custom) | `your-tenant.auth0.com`             |
| `AUTH0_AUDIENCE`      | Identificador de la API (Audience) registrado en Auth0       | `https://api.springboot-course.com` |
| `AUTH0_CLIENT_ID`     | Client ID de la aplicación en Auth0 (si aplica)             | -                                   |
| `AUTH0_CLIENT_SECRET` | Client Secret de la aplicación en Auth0 (si aplica)         | -                                   |

---

### Configuración de Sentry y MCP

El proyecto está vinculado al servicio de observabilidad de Sentry bajo la organización `arthurolg-to` y el proyecto `springboot-course`:

1. **Obtención del `SENTRY_AUTH_TOKEN`**:
   - Ingresa a [Sentry > User Settings > API > Auth Tokens](https://sentry.io/settings/account/api/auth-tokens/) o en la configuración de la organización > Developer Settings.
   - Genera un token con los permisos necesarios: `project:releases` y `org:read`.
   - Agrega el token a tu archivo `.env`:
     ```env
     SENTRY_AUTH_TOKEN=sntrys_...
     ```
   *(El plugin de Gradle subirá automáticamente el bundle de código fuente para stack traces legibles en cada compilación).*

2. **Servidor MCP de Sentry (Model Context Protocol)**:
   - Endpoint: `https://mcp.sentry.dev/mcp/arthurolg-to/springboot-course`
   - Configurado en el archivo del proyecto [`.mcp.json`](.mcp.json) y en Antigravity CLI (`agy`):
     ```bash
     agy mcp add sentry https://mcp.sentry.dev/mcp/arthurolg-to/springboot-course
     ```
   - Permite consultar incidencias, trazas, logs y detalles del proyecto mediante IA directamente desde el asistente.

---

## Pruebas (Testing)

El proyecto utiliza JUnit 5, Kotest Assertions, MockK y Testcontainers.

- **Ejecutar todos los tests:**

  ```bash
  ./gradlew test
  ```

- **Reporte de Cobertura (JaCoCo):** Se genera automáticamente tras ejecutar los
  tests en `build/reports/jacoco/test/html/index.html`.

---

## Documentación de la API

Una vez iniciada la aplicación, puedes acceder a:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`
- **Actuator Health:** `http://localhost:8080/actuator/health`

---

## 🎓 Guía de Aprendizaje: De Novato a Maestro

Este curso sigue una narrativa inspirada en Pokémon donde **Kai**, un joven
entrenador, desarrolla el sistema del **Hotel Pokémon** mientras visita 6
gimnasios. Cada gimnasio representa un dominio técnico esencial.

### 🗺️ Ruta de Aprendizaje

| #   | Gimnasio         | Líder     | Insignia         | Dominio Técnico                             | Duración |
| --- | ---------------- | --------- | ---------------- | ------------------------------------------- | -------- |
| 1   | 🏔️ **Pewter**    | Brock     | Boulder Badge 💎 | JPA, Entidades, Flyway, Repositorios        | 24h      |
| 2   | ⚡ **Vermilion** | Lt. Surge | Thunder Badge ⚡ | JWT, Spring Security, OAuth2, RBAC          | 20h      |
| 3   | 🌊 **Cerulean**  | Misty     | Cascade Badge 🌊 | Bean Validation, Excepciones, Transacciones | 16h      |
| 4   | 🌱 **Celadon**   | Erika     | Rainbow Badge 🌈 | Unit Tests, Integration, E2E, Performance   | 24h      |
| 5   | 🧪 **Cinnabar**  | Blaine    | Volcano Badge 🌋 | Logging, Métricas, Distributed Tracing      | 16h      |
| 6   | 🐉 **Viridian**  | Giovanni  | Earth Badge 🌍   | Docker, CI/CD, Análisis Estático            | 20h      |
| 🏆  | **Liga Pokémon** | —         | Maestro 🎖️       | Proyecto integrador completo                | 8h       |

> **Total estimado:** ~128 horas (~3 semanas full-time)

### 📚 Documentación Detallada

| Recurso                                                                    | Contenido                                                      |
| -------------------------------------------------------------------------- | -------------------------------------------------------------- |
| [📖 Guía Narrativa](docs/learning-path/narrative-guide.md)                 | Historia completa de Kai (visión general, motivación)          |
| [🗺️ Itinerario de Desarrollo](docs/learning-path/development-itinerary.md) | Fases con código de ejemplo (entidades, seguridad, testing...) |
| [🏋️ Roadmap de Ejercicios](docs/learning-path/exercises-roadmap.md)        | Ejercicios paso a paso con criterios de aceptación             |
| [🏗️ Plan de Migración MVC](docs/architecture/mvc-migration-plan.md)        | Migración de Hexagonal a MVC por features                      |

### 🚀 ¿Por dónde empezar?

Esta es una historia de Kai, un desarrollador que busca mejorar sus habilidades
en el desarrollo de aplicaciones web con Spring Boot y Kotlin. A través de un
camino de aprendizaje estructurado, Kai se desempeñará en varios gimnasios, cada
uno con un líder y una insignia que representará su dominio técnico.
[Leer más sobre como avanzar de novato a maestro en Spring Boot](https://www.arthurolg.com/article/springboot-course_de-novato-a-maestro-springboot-guia-narrativa-pokemon).

1. **Si eres nuevo:** Lee la
   [Guía Narrativa](docs/learning-path/narrative-guide.md) para entender el
   contexto, luego sigue el
   [Roadmap de Ejercicios](docs/learning-path/exercises-roadmap.md) desde la
   Fase 1.
2. **Si tienes experiencia:** Ve directo al
   [Itinerario de Desarrollo](docs/learning-path/development-itinerary.md) para
   ver los ejemplos de código.
3. **Si quieres entender la arquitectura:** Revisa el
   [Plan de Migración MVC](docs/architecture/mvc-migration-plan.md).
4. **Guía de inicio rápido con Spring Boot y Kotlin:** Aprende los conceptos
5. básicos de
   [MVC con Spring Boot y Kotlin](docs/course/lectures/quickstart-springboot-kotlin.md).
6. Ideas de ejercicios complementarios para el sistema por niveles. Son
   prácticas adicionales que puedes realizar para reforzar lo aprendido.
   [Leer más](docs/learning-path/ideas-ejercicios.md).

---

### Artículos Relacionados

- [Arquitectura Hexagonal vs MVC en Spring Boot: Una Guía Pragmática](https://www.arthurolg.com/article/programming_arquitectura-hexagonal-vs-mvc-spring-boot-pragmatico)
- [Backend Java con Spring Boot: Lecciones y Curso](https://www.arthurolg.com/article/programming_backend-java-spring-boot-lecciones-curso)
- [TDD con CRUD en Spring Boot y Arquitectura Hexagonal](https://www.arthurolg.com/article/programming_tdd-crud-spring-boot-arquitectura-hexagonal)
- [Buenas Prácticas en Spring Boot y Kotlin](https://www.arthurolg.com/article/programming_spring-boot-kotlin-buenas-practicas)
- [Aprende a Construir APIs REST Escalables](https://www.arthurolg.com/article/programming_aprende-a-construir-apis-rest-escalables)
- [Grandes Programadores](https://www.arthurolg.com/article/programming_grandes-programadores)
- [Guía Completa Java y Spring Boot: Desarrollo](https://www.arthurolg.com/article/programming_guia-completa-java-spring-boot-desarrollo)
- [Comprender Joins SQL, JPA y Bases de Datos](https://www.arthurolg.com/article/programming_comprender-joins-sql-jpa-bases-datos)
- [Anotaciones Personalizadas en Spring Boot](https://www.arthurolg.com/article/programming_anotaciones-personalizadas-spring-boot)
- [Fundamentos Programación Orientada a Objetos](https://www.arthurolg.com/article/programming_fundamentos-programacion-orientada-objetos)
- [Optimiza API REST Java: DTOs vs Entidades JPA](https://www.arthurolg.com/article/programming_optimiza-api-rest-java-dtos-vs-entidades-jpa)
- [Recursos de Aprendizaje en GitHub](https://www.arthurolg.com/article/technology_learning-resources-github-repos)
- [Buenas prácticas desarrollo backend proyectos escalables](https://www.arthurolg.com/article/programming_buenas-practicas-desarrollo-backend-proyectos-escalables)

---

## Licencia

Este proyecto está bajo la Licencia **CC-BY-4.0**. Consulta el archivo
[LICENSE](LICENSE) para más detalles.

---

## **¡Happy Coding! 🚀**

Si este proyecto te ha sido útil, ¡no olvides darle una ⭐ en GitHub!

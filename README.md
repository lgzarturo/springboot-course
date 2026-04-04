# 🚀 Spring Boot Course — API REST Real

[![Release](https://img.shields.io/github/v/release/lgzarturo/springboot-course?label=Latest%20Release)](https://github.com/lgzarturo/springboot-course/releases/tag/v0.0.1)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue.svg)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-CC--BY--4.0-yellow.svg)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/lgzarturo/springboot-course/ci.yml?branch=main)](https://github.com/lgzarturo/springboot-course/actions)

Este repositorio contiene el código fuente y la documentación para el curso de Spring Boot y Kotlin, enfocado en construir una API REST real siguiendo estándares de la industria y mejores prácticas de arquitectura.

## 📋 Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Stack Tecnológico](#stack-tecnológico)
- [Características Principales](#características-principales)
- [Requisitos](#requisitos)
- [Configuración del Entorno](#configuración-del-entorno)
- [Comandos y Scripts](#comandos-y-scripts)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Variables de Entorno](#variables-de-entorno)
- [Pruebas (Testing)](#pruebas-testing)
- [Documentación de la API](#documentación-de-la-api)
- [Licencia](#licencia)

---

## Descripción General

El proyecto consiste en una plataforma de gestión hotelera y ecommerce de amenities. A través de este curso, aprenderás a resolver problemas de negocio reales (gestión de habitaciones, reservas, pagos, etc.) aplicando conceptos técnicos avanzados.

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

- ✅ **Arquitectura Hexagonal** (Ports & Adapters)
- ✅ **Clean Architecture** y **Domain-Driven Design (DDD)**
- ✅ **Desarrollo Guiado por Pruebas (TDD)**
- ✅ **Calidad de Código:** Detekt (Análisis estático) y KTLint (Linter/Formatter)
- ✅ **CI/CD:** GitHub Actions para compilación, pruebas y análisis
- ✅ **Contenedores:** Soporte para Docker Compose y Testcontainers

---

## Requisitos

- **Java 21** (JDK 21)
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

2. **Variables de Entorno:**
   Crea un archivo `.env` en la raíz del proyecto (puedes basarte en `.env.example` si existe):
   ```env
   DB_NAME=springboot_db
   DB_USERNAME=postgres
   DB_PASSWORD=postgres
   DB_PORT=5432
   SENTRY_DSN=tu_dsn_aqui (opcional)
   ```

3. **Infraestructura (Opcional):**
   Si deseas usar PostgreSQL local mediante Docker:
   ```bash
   docker-compose up -d
   ```

---

## Comandos y Scripts

El proyecto utiliza Gradle Wrapper. Puedes ejecutar estos comandos desde la terminal:

### Desarrollo
- `./gradlew bootRun`: Ejecuta la aplicación localmente (puerto 8080 por defecto).
- `./gradlew clean build`: Limpia y construye el proyecto.

### Calidad y Estilo de Código
- `./gradlew checkCodeStyle`: Ejecuta ktlint y detekt.
- `./gradlew formatCode`: Aplica correcciones automáticas de estilo.
- `./gradlew codeQuality`: Ejecuta verificaciones de estilo, tests y genera reportes de cobertura.
- `./gradlew fixAll`: Aplica todas las correcciones automáticas disponibles.

### Base de Datos
- `./gradlew generateDDL`: Genera scripts DDL a partir de entidades JPA.
- `./gradlew createMigration -Pmigration_version=1 -Pdescription="nombre"`: Crea una nueva migración de Flyway.

---

## Estructura del Proyecto

El proyecto sigue una organización basada en Arquitectura Hexagonal y DDD:

```text
src/main/kotlin/com/lgzarturo/springbootcourse/
├── SpringbootCourseApplication.kt   # Punto de entrada
├── [modulo]/                        # Ejemplo: hotels, rooms, users
│   ├── application/                 # Lógica de aplicación
│   │   ├── ports/
│   │   │   ├── input/               # Casos de uso (Interfaces)
│   │   │   └── output/              # Puertos de salida (Interfaces de repo)
│   │   └── service/                 # Implementación de casos de uso
│   ├── domain/                      # Lógica de dominio (Pura)
│   └── adapters/                    # Adaptadores (Detalles técnicos)
│       ├── rest/                    # Controladores, DTOs, Mappers
│       └── persistence/             # Implementaciones de repositorio, Entidades JPA
└── shared/                          # Código compartido y utilidades
```

---

## Variables de Entorno

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring activo | `dev` |
| `DB_NAME` | Nombre de la base de datos | `springboot_db` |
| `DB_USERNAME` | Usuario de BD | `postgres` |
| `DB_PASSWORD` | Contraseña de BD | `postgres` |
| `SENTRY_DSN` | DSN de Sentry para tracking | - |

---

## Pruebas (Testing)

El proyecto utiliza JUnit 5, Kotest Assertions, MockK y Testcontainers.

- **Ejecutar todos los tests:**
  ```bash
  ./gradlew test
  ```
- **Reporte de Cobertura (JaCoCo):**
  Se genera automáticamente tras ejecutar los tests en `build/reports/jacoco/test/html/index.html`.

---

## Documentación de la API

Una vez iniciada la aplicación, puedes acceder a:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`
- **Actuator Health:** `http://localhost:8080/actuator/health`

---

## Licencia

Este proyecto está bajo la Licencia **CC-BY-4.0**. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---
**¡Happy Coding! 🚀**
Si este proyecto te ha sido útil, ¡no olvides darle una ⭐ en GitHub!

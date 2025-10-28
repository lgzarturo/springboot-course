### Perfiles en Spring Boot 3 con configuración por entorno

"Se agregan 3 perfiles para la aplicación Spring Boot: `dev`, `prod` y `test`. Cada perfil tiene su propia configuración de base de datos y otras propiedades específicas, además que el archivo `application.yml` queda con la configuración base y los perfiles sobreescriben algunas propiedades y definen otras."

A continuación se detalla cómo está estructurada la configuración por perfiles, qué propiedades se sobreescriben, cómo aislar variables de entorno con `.env` y la forma correcta de cargarlas en Spring Boot y Docker, además de las dependencias necesarias para pruebas y una configuración óptima para la aplicación.

---

### 1) Estructura de configuración y principio de funcionamiento

- Base común: `application.yaml`
    - Define el nombre de aplicación, el import de `.env`, el perfil activo por defecto, configuración de Jackson, JPA/Hibernate, HikariCP, Flyway, logging, Sentry, Actuator/Micrometer, OpenAPI, propiedades de la app, servidor y ajustes de arranque.
- Overrides por perfil:
    - `application-dev.yaml`
    - `application-prod.yaml`
    - `application-tests.yaml`

Spring Boot aplica el archivo base y, si hay un perfil activo, carga el archivo `application-<perfil>.yaml` y sobreescribe/añade propiedades. Por defecto, se activa `dev` salvo que se indique lo contrario.

Fragmento clave en `application.yaml`:

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

- `spring.config.import`: permite cargar un archivo `.env` local con variables de entorno (útil en desarrollo).
- `SPRING_PROFILES_ACTIVE`: variable que define el perfil vigente; por defecto `dev`.

---

### 2) Perfil base: `application.yaml` (común para todos)

Algunos puntos más relevantes:

- Carga de `.env` en tiempo de ejecución:
  ```yaml
  spring:
    config:
      import: optional:file:.env[.properties]
  ```
- Jackson: formato ISO-8601, zona horaria UTC, `indent-output: true`, tolerancia a propiedades desconocidas.
- JPA/Hibernate:
  ```yaml
  spring:
    jpa:
      open-in-view: false
      show-sql: true
      properties:
        hibernate:
          format_sql: true
          use_sql_comments: true
        jdbc:
          batch_size: 20
          fetch_size: 50
          order_updates: true
          order_inserts: true
        query:
          in_clause_parameter_padding: true
      hibernate:
        ddl-auto: update
  ```

> Nota: en entornos reales conviene `ddl-auto: validate` y manejar el schema con Flyway (ver perfiles).

- Datasource (HikariCP) base: tamaño de pool, timeouts, etc., como default.
- Flyway: habilitado con `baseline-on-migrate: true`, `locations: classpath:db/migration`.
- Logging: niveles y patrones de consola y archivo.
- Sentry: DSN por variable, environment derivado del perfil, trazas habilitadas en base, PII habilitada en base (revise para prod).
- Actuator/Micrometer: exposición de `health, info, metrics, prometheus`, base path `/actuator`, métricas con histogramas y percentiles.
- OpenAPI/Swagger: habilitado en base (`/api-docs`, `/swagger-ui.html`).
- Servidor: puerto 8080, compresión habilitada, manejo de errores, apagado "graceful".

---

### 3) Perfiles específicos: overrides y mejores prácticas

#### 3.1) Perfil dev — `application-dev.yaml`

- Base de datos PostgreSQL por variables de entorno:
  ```yaml
  spring:
    datasource:
      url: ${DB_URL:jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:springboot_course}}
      driver-class-name: org.postgresql.Driver
      username: ${DB_USERNAME:postgres}
      password: ${DB_PASSWORD:postgres}
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
        # ...timeouts y pool-name específicos
  ```
- JPA/Hibernate:
    - `show-sql: true`
    - `hibernate.ddl-auto: validate` (Flyway maneja el schema)
    - Estadísticas habilitadas (`generate_statistics: true`)
- Flyway: habilitado con `baseline-on-migrate: true`, `clean-disabled: true`.
- Docker Compose para desarrollo:
    - Integración de Spring Boot con Docker Compose para levantar/derribar servicios automáticamente cuando se inicia/detiene la app en dev.
    - Propiedades correctas en Spring Boot 3: `spring.docker.compose.enabled=true`, `spring.docker.compose.file=docker-compose.yml`, `spring.docker.compose.lifecycle-management=start_and_stop`.
- Logging en dev: más verboso para SQL y bindings.
- Puerto:
  ```yaml
  server:
    port: ${PORT:8080}
  ```

> Cuándo usar: desarrollo local con PostgreSQL orquestado con Docker Compose y migraciones vía Flyway. SQL visible para depurar.

#### 3.2) Perfil prod — `application-prod.yaml`

- Datasource: PostgreSQL por variables, pool más grande y `leak-detection-threshold` para diagnosticar fugas de conexiones.
  ```yaml
  spring:
    datasource:
      hikari:
        maximum-pool-size: 30
        minimum-idle: 10
        leak-detection-threshold: 60000
  ```
- JPA/Hibernate:
    - `show-sql: false`
    - `format_sql: false`
    - `ddl-auto: validate`
    - Ajustes de plan cache (`plan_cache_max_size`, `plan_parameter_metadata_max_size`)
- Flyway: más estricto (`validate-on-migrate: true`, `baseline-on-migrate: false`).
- Logging: menos ruido; logs a archivo configurable `LOG_FILE`.
- Sentry:
    - `environment: prod`
    - `send-default-pii: false`
    - `traces-sample-rate: 0.1` (ajustable por `SENTRY_TRACES_SAMPLE_RATE`)
- Management/Actuator:
    - `health.show-details: when-authorized`
    - Exponer `health, info, metrics, prometheus`
- OpenAPI/Swagger:
  ```yaml
  springdoc:
    api-docs.enabled: false
    swagger-ui.enabled: false
  ```
- Servidor: compresión optimizada (tipos y tamaño mínimo).

> Cuándo usar: despliegues en producción con observabilidad y seguridad, sin exponer documentación pública ni SQL logs. Validación estricta del schema.

#### 3.3) Perfil test — `application-tests.yaml`

- Objetivo: pruebas automatizadas con Testcontainers.
- JPA/Hibernate:
  ```yaml
  spring:
    jpa:
      show-sql: false
      hibernate:
        ddl-auto: create-drop
  ```
  Esto garantiza un schema limpio por prueba/invocación de contexto.
- Flyway: deshabilitado en pruebas (`flyway.enabled: false`).
- Docker Compose: deshabilitado (`spring.docker.compose.enabled: false`) para no depender de servicios externos en tests (Testcontainers administra los contenedores).
- Sentry: totalmente deshabilitado en pruebas.
- Servidor:
  ```yaml
  server:
    port: 0
  ```
  Puerto aleatorio al levantar contexto web en pruebas.
- Logging: niveles reducidos para minimizar ruido.

> Cuándo usar: suites de prueba de integración usando contenedores efímeros, aislados y deterministas.

---

### 4) Aislamiento de variables con `.env` y carga correcta en Spring Boot y Docker

- Archivo de ejemplo: `.env.example`
  ```
  SENTRY_AUTH_TOKEN=...
  SENTRY_DSN=...
  SENTRY_ENVIRONMENT=...
  SENTRY_DEBUG=true|false
  SENTRY_ENABLED=true|false
  SENTRY_LOGGING_ENABLED=true|false
  SENTRY_TRACES_SAMPLE_RATE=0.1

  DB_URL=jdbc:postgresql://localhost:5432/springboot_course
  DB_HOST=localhost
  DB_PORT=5432
  DB_NAME=springboot_course
  DB_USERNAME=postgres
  DB_PASSWORD=postgres
  ```
  > Recomendación: no use llaves `{}` en el `.env` real; use formato `KEY=value`. Comparta en el repo solo `.env.example` y mantenga `.env` fuera de control de versiones.

- Spring Boot (runtime):
    - Carga `.env` con `spring.config.import=optional:file:.env[.properties]` en `application.yaml`.
    - Precedencia: las variables de entorno del SO tienen prioridad sobre archivos de propiedades. Perfiles sobreescriben al base.

- Gradle (build) — Sentry plugin:
    - Él `build.gradle.kts` intenta leer `.env` para obtener `SENTRY_AUTH_TOKEN` si no está presente en el entorno del SO:
      ```kotlin
      val file = rootProject.file(".env")
      // ...
      val sentryAuthToken: String? = System.getenv("SENTRY_AUTH_TOKEN") ?: dotenv["SENTRY_AUTH_TOKEN"]
      ```
    - Esto es útil para publicar Source Context a Sentry durante el build en dev local.

- Docker Compose:
    - `docker-compose.yml` usa `env_file: - .env` y mapea variables para el contenedor de PostgreSQL:
      ```yaml
      services:
        postgres:
          image: postgres:17-alpine
          env_file:
            - .env
          environment:
            POSTGRES_DB: ${DB_NAME}
            POSTGRES_USER: ${DB_USERNAME}
            POSTGRES_PASSWORD: ${DB_PASSWORD}
          ports:
            - "${DB_PORT:-5432}:5432"
          healthcheck:
            test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME} -d ${DB_NAME}"]
      ```
    - Flujo recomendado en dev:
        1) Cree `.env` con las credenciales.
        2) `docker compose up -d postgres`.
        3) Inicie la app con perfil `dev` y la app tomará las mismas variables.

- Integración Spring Boot 3 con Docker Compose en dev:
    - Configure en `application-dev.yaml`:
      ```yaml
      spring:
        docker:
          compose:
            enabled: true
            file: docker-compose.yml
            lifecycle-management: start_and_stop
      ```
    - Con esto, al iniciar la app en dev, Spring puede arrancar y gestionar el servicio `postgres` del compose.

---

### 5) Selección de perfiles y arranque

- Por variable de entorno:
    - Linux/macOS:
      ```bash
      SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
      SPRING_PROFILES_ACTIVE=prod java -jar build/libs/app.jar
      SPRING_PROFILES_ACTIVE=test ./gradlew test
      ```
    - Windows (PowerShell):
      ```powershell
      $env:SPRING_PROFILES_ACTIVE="dev"; ./gradlew bootRun
      ```

- Por argumento:
  ```bash
  ./gradlew bootRun --args='--spring.profiles.active=dev'
  java -jar build/libs/app.jar --spring.profiles.active=prod
  ```

- En pruebas:
    - Por anotación:
      ```kotlin
      @ActiveProfiles("test")
      @SpringBootTest
      class MyIntegrationTest { /* ... */ }
      ```
    - O estableciendo `spring.profiles.active=test` al ejecutar el task de test.

---

### 6) Dependencias clave para ejecución y pruebas

Del `build.gradle.kts`:

- Runtime principales:
    - `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`
    - OpenAPI: `org.springdoc:springdoc-openapi-starter-webmvc-ui`
    - Observabilidad: `io.micrometer:micrometer-registry-prometheus`
    - Sentry: `io.sentry:sentry-spring-boot-starter-jakarta`, `io.sentry:sentry-logback`
    - DB: `org.postgresql:postgresql` (runtime), `com.h2database:h2` (solo para usos locales puntuales)
    - Desarrollo: `spring-boot-devtools`, `spring-boot-docker-compose`

- Pruebas:
    - `spring-boot-starter-test`
    - `spring-boot-testcontainers`, `testcontainers`, `testcontainers:postgresql`, `testcontainers:junit-jupiter`
    - `kotlin-test-junit5`, `mockk`, `springmockk`

> Además, el proyecto integra calidad y cobertura: Ktlint, Detekt, JaCoCo.

---

### 7) Pruebas con Testcontainers (PostgreSQL)

Con Spring Boot 3.1+ y `spring-boot-testcontainers`, se puede usar `@ServiceConnection` para auto-registrar propiedades del contenedor:

```kotlin
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.boot.testcontainers.service.connection.ServiceConnection

@Testcontainers
@SpringBootTest
class PersistenceIT {

  @Container
  @ServiceConnection
  val postgres = PostgreSQLContainer("postgres:17-alpine")

  @Test
  fun contextLoads() {
    // ... sus pruebas aquí
  }
}
```

> Esto evita configurar manualmente `spring.datasource.url/username/password` durante las pruebas. El perfil `test` aporta además `ddl-auto: create-drop`, Flyway deshabilitado y `server.port=0`.

---

### 8) Resumen de propiedades sobrescritas por perfil

- Datasource (PostgreSQL):
    - `dev`: pool moderado, `ddl-auto: validate`, SQL visible.
    - `prod`: pool mayor, leak detection, SQL y formateo deshabilitados, plan cache ajustado.
    - `test`: pool pequeño, `ddl-auto: create-drop`, sin Flyway (schema efímero).
- Flyway:
    - Base habilitado; `dev` habilitado con baseline; `prod` más estricto; `test` deshabilitado.
- Logging:
    - Base detallado; `dev` más verboso (SQL/bindings); `prod` sobrio y a archivo; `test` mínimo.
- Sentry:
    - Base con tracing 1.0 y PII activado; `prod` con menos PII y muestreo reducido; `test` deshabilitado.
- Actuator:
    - Base expone métricas y health; `prod` restringe detalles de health.
- OpenAPI:
    - Base habilitado; `prod` deshabilitado.
- Servidor:
    - Base compresión habilitada; `prod` ajusta tipos y tamaño mínimo; `test` puerto aleatorio.

---

### 9) Recomendaciones operativas

- Mantenga secretos únicamente en variables de entorno o gestores de secretos. Use `.env` solo en local y no lo comprometa al repositorio.
- Use `ddl-auto=validate` con migraciones Flyway en `dev` y `prod`. No usar `update` en entornos reales.
- Deshabilite `show-sql` y el log de binds en `prod`.
- Ajuste tamaños del pool Hikari según carga.
- Restrinja endpoints sensibles de Actuator y deshabilite Swagger en `prod`.
- Habilite compresión en `prod` con límites adecuados.
- Pruebas: use Testcontainers y el perfil `test` con `server.port=0`.

---

### 10) Flujo recomendado por entorno

- Desarrollo:
    1) Crear `.env` a partir de `.env.example`.
    2) `docker compose up -d postgres` o dejar que Spring gestione Compose en `dev`.
    3) `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`.

- Producción:
    1) Definir variables de entorno (DB, Sentry, etc.) en la plataforma de despliegue.
    2) Ejecutar con `--spring.profiles.active=prod`.

- Pruebas:
    1) Tests de integración con `@ActiveProfiles("test")` y Testcontainers.
    2) `./gradlew test` (Spring arranca un PostgreSQL efímero y configura el datasource automáticamente con `@ServiceConnection`).

Con esta arquitectura, cada perfil controla únicamente lo que debe variar por entorno, el archivo base concentra la configuración compartida y las variables sensibles se aíslan correctamente mediante `.env` y/o variables del SO, tanto para ejecución local como para orquestación con Docker Compose.
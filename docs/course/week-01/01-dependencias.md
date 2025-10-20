# Descripción de las dependencias

Las dependencias seleccionadas forman un stack equilibrado para construir una API REST moderna con Spring Boot 3 y Kotlin. Cada una aporta funcionalidades clave, manteniendo el proyecto ligero y fácil de mantener.

Alineado al enfoque de la decisión arquitectónica, aquí está el "por qué" técnico de cada dependencia y cómo se usa correctamente.

## Spring Web

**Qué aporta:**

Spring MVC para construir API's REST (*DispatcherServlet, `@RestController`, `@RequestMapping`, `@GetMapping`, etc.*). Enrutamiento, serialización JSON (*Jackson*), manejo de peticiones y respuestas HTTP.

**Uso recomendado:**

- Controladores delgados, sin lógica de negocio.
- Manejo de errores con `@ControllerAdvice` y `@ExceptionHandler` para respuestas consistentes.
- **Patrón:** `Controller -> Service -> Repository`. Mantiene separación de responsabilidades.
- DTO's para entrada/salida, no exponer entidades JPA directamente.

## Spring Configuration Processor

**Qué aporta:**

Generación de metadatos para configuración basada en propiedades, útil para autocompletado en IDE y validación. Esencial para usar `@ConfigurationProperties` de forma segura.

**Uso recomendado:**

- Crear clases `@ConfigurationProperties(prefix = "app")` con propiedades tipadas.
- Validar con `@Validated` y anotaciones de Bean Validation en propiedades.
- Documentar propiedades con comentarios y ejemplos.

**Beneficio:**

Configuración segura y autodescriptiva, ideal para múltiples perfiles. Es una buena práctica para cualquier proyecto Spring Boot serio, debido a la complejidad creciente de las configuraciones.

## Spring Boot DevTools

**Qué aporta:**

Reinicio/recarga automática en desarrollo, LiveReload, tuning de cachés.

**Uso recomendado:**

Solo activar en dev (**entorno local de desarrollo**); queda en el classpath, pero Spring lo deshabilita en empaquetado. Es ideal para iterar rápido sin reiniciar manualmente la aplicación.

**Beneficio:**

Iteración rápida sin reinicios pesados. Acelera el desarrollo y pruebas locales.

## Spring Data JPA

**Qué aporta:**

Abstracción de acceso a datos sobre JPA/Hibernate. Repositorios declarativos (CrudRepository/JpaRepository). Es importante para persistencia en bases relacionales, debido a su integración con el ecosistema Spring y al soporte para transacciones.

**Uso recomendado:**

- Entidades con `@Entity` y `@Table`; ID con `@Id` y generación (*UUID o sequence*).
- Repositorios con métodos derivados o `@Query` para casos específicos.
- Servicios de dominio que orquestan la lógica de negocio.
- Mapeo de entidades a DTO para la capa de presentación.
- **Patrón:** Repository como puerto de salida; domain services que orquestan reglas.

## H2 Database

**Qué aporta:**

Base en memoria o archivo para desarrollo/pruebas rápidas.

**Uso recomendado:**

- Perfil "dev" apuntando a H2, schema auto-generado (*`spring.jpa.hibernate.ddl-auto=update/create-drop` para dev*).
- H2 console habilitada solo en dev.
- `spring.h2.console.path=/h2-console` para acceder a la consola.
- `spring.h2.console.enabled=true` para habilitar la consola.

**Beneficio:**

Feedback rápido sin infraestructura adicional. Sirve para desarrollo local y pruebas unitarias, pero realmente lo ideal es usar una base real, incluyendo para las pruebas de integración y tener un entorno de pruebas más cercano a producción.

## PostgreSQL Driver

**Qué aporta:**

Conector `JDBC` para Postgres, base recomendada para ambientes reales por robustez y tipos ricos.

**Uso recomendado:**

- Perfil "local/prod" con datasource hacia Postgres.
- Migraciones con Flyway o Liquibase para control de esquema (*altamente recomendado para entornos serios*).
- `spring.jpa.hibernate.ddl-auto=validate` para producción.

**Buenas prácticas:**

- No uses ddl-auto en prod; usa migraciones versionadas, como parte de las buenas prácticas de desarrollo y despliegue.

## Validation (Bean Validation con Jakarta Validation)

**Qué aporta:**

Validación declarativa con anotaciones (`@NotBlank`, `@Email`, `@Positive`, `@Size`, etc.).

**Uso recomendado:**

- DTO de entrada con anotaciones y `@Valid` en los endpoints.
- Mensajes de error personalizados y consistentes via ControllerAdvice.
- Validación cruzada entre campos (*ej.: fechas de inicio y fin*).
- Validación condicional (*ej.: si un campo A está presente, validar campo B*).

**Beneficio:**

Contratos de API sólidos y errores manejados de forma uniforme.

## Spring Boot Actuator

**Qué aporta:**

Endpoints de observabilidad (*health, info, metrics, env, httptrace, prometheus, etc.*). Desde el inicio, es crucial para monitoreo y operación. Tanto para tener visibilidad en desarrollo como para monitorear en producción.

**Uso recomendado:**

- Exponer solo lo necesario y proteger endpoints sensibles.
- Integrar micrómetros y Prometheus/Grafana para métricas.
- Configurar health checks personalizados (*DB, servicios externos*).
- Monitorear uso de memoria, GC, hilos, etc.

**Beneficio:**

Telemetría operativa desde el día 1, clave para producción.

## Sentry

**Qué aporta:**

Integración con Sentry para captura de excepciones, performance tracing y bread crumbs. Esencial para monitoreo de errores en producción y diagnóstico rápido.

**Uso recomendado:**

- Configurar DSN en variables de entorno/secretos.
- Capturar contextos relevantes (*user, requestId, tenant*).
- Patrón de observabilidad: logs estructurados + métricas + `traces` + errores (*Sentry*). Te ayuda a cerrar el círculo de diagnóstico.
- Configuración base sugerida (*yaml de ejemplo por perfiles*)

**application.yaml:**

```yaml
spring:
  application:
    name: springboot-course

---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:course_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

**application-prod.yaml:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<host>:5432/course_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          lob:
            non_contextual_creation: true
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

**Sentry (*cualquier perfil*):**

```yaml  
sentry:
  dsn: ${SENTRY_DSN:}
  environment: ${SPRING_PROFILES_ACTIVE:dev}
  traces-sample-rate: 0.2
```

## Hello World con Buenas Prácticas

Siempre es recomendable tener un endpoint mínimo para validar que todo el pipeline de request/response funciona correctamente.

Primer código con buenas prácticas

**PingController mínimo:**

```kotlin
@RestController
@RequestMapping("/api")
class PingController {
    @GetMapping("/ping")
    fun ping() = mapOf("status" to "ok", "time" to Instant.now().toString(), "app" to "springboot-course", "version" to "0.1.0", "say" to "Hello World!")
}
```

En este ejemplo, el endpoint `/api/ping` devuelve un JSON simple con información útil como el estado, la hora actual, el nombre de la aplicación y su versión.

**Manejo de errores global:**

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException): Map<String, Any> = mapOf("message" to "Validation failed", "errors" to ex.bindingResult.fieldErrors.map { it.field to (it.defaultMessage ?: "invalid") }.toMap())
}
```

Este controlador de excepciones captura errores de validación y devuelve una respuesta JSON estructurada con los detalles de los errores.

Además, el `Global Exception Handler` puede extenderse para manejar otros tipos de excepciones según sea necesario.

En este punto este `Handler` es suficiente para validar que la configuración de validación y manejo de errores funciona correctamente.

## Resumen de lo aprendido

Hasta ahora, has aprendido a:

- Configurar un proyecto Spring Boot 3 con Kotlin, Gradle y JDK 21.
- Seleccionar un set de dependencias equilibrado para desarrollar una API REST moderna: Web, JPA, H2 (`dev`), PostgreSQL (`prod`), Validation, Actuator, DevTools y Sentry.
- Se recomendó una arquitectura por capas/puertos, perfiles de configuración, manejo de errores y observabilidad desde el inicio.
- El proyecto queda listo para iterar rápido (**DevTools**), validar entradas (**Validation**), persistir (**JPA**), monitorear (**Actuator**) y operar con confianza (**Sentry**).
- Se proporciona el código inicial con buenas prácticas para controladores, manejo de errores y DTO validados.

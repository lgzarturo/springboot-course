# 🔒 Política de Seguridad

## Declaración de Seguridad

La seguridad es una prioridad fundamental en el proyecto Spring Boot Course.
Este documento describe nuestras políticas de seguridad, cómo reportar
vulnerabilidades y las mejores prácticas de seguridad implementadas en el
proyecto.

## 📋 Tabla de Contenidos

- [Versiones Soportadas](#versiones-soportadas)
- [Reporte de Vulnerabilidades](#reporte-de-vulnerabilidades)
- [Mejores Prácticas de Seguridad](#mejores-prácticas-de-seguridad)
- [Seguridad en Dependencias](#seguridad-en-dependencias)
- [Configuración Segura](#configuración-segura)
- [Seguridad en Desarrollo](#seguridad-en-desarrollo)
- [Seguridad en Producción](#seguridad-en-producción)
- [Auditorías de Seguridad](#auditorías-de-seguridad)

---

## Versiones Soportadas

Actualmente damos soporte de seguridad a las siguientes versiones del proyecto:

| Versión | Soportada          | Estado               |
| ------- | ------------------ | -------------------- |
| 0.0.x   | :white_check_mark: | En desarrollo activo |
| < 0.0.1 | :x:                | OK Configuración     |
| < 0.0.2 | :x:                | OK TDD               |

**Nota**: Este es un proyecto educativo en desarrollo activo. Todas las
versiones futuras recibirán actualizaciones de seguridad hasta que se marquen
como obsoletas.

---

## Reporte de Vulnerabilidades

### 🚨 Cómo Reportar una Vulnerabilidad

Si descubres una vulnerabilidad de seguridad en este proyecto, por favor
repórtala de manera responsable:

1. **NO abras un issue público** para vulnerabilidades de seguridad
2. Envía un correo electrónico a: **lgzarturo@gmail.com**
3. Incluye la siguiente información:
   - Descripción detallada de la vulnerabilidad
   - Pasos para reproducir el problema
   - Versiones afectadas
   - Impacto potencial
   - Sugerencias de mitigación (si las tienes)

### Proceso de Respuesta

1. **Confirmación**: Confirmaremos la recepción del reporte en un plazo de 48
   horas
2. **Evaluación**: Evaluaremos la vulnerabilidad y su impacto en un plazo de 7
   días
3. **Corrección**: Trabajaremos en una solución y la publicaremos según la
   severidad:
   - **Crítica**: 1-3 días
   - **Alta**: 7-14 días
   - **Media**: 14-30 días
   - **Baja**: 30-90 días
4. **Divulgación**: Una vez corregida, publicaremos un security advisory en
   GitHub
5. **Reconocimiento**: Agregaremos tu nombre al security advisory (si lo deseas)

---

## Mejores Prácticas de Seguridad

### Spring Boot 3 Security

Este proyecto usa **Spring Boot 3.5.6** que incluye mejoras de seguridad
importantes:

#### ✅ Características de Seguridad Habilitadas

- **Jakarta EE 10**: Migración completa a `jakarta.*` namespace
- **Validación de Entrada**: Uso de `spring-boot-starter-validation` con Bean
  Validation
- **Actuator Seguro**: Endpoints de Actuator con configuración restringida
- **CORS Configurado**: Política CORS definida en `WebConfig`
- **Exception Handling Global**: Manejo centralizado de errores

#### 🔐 Características de Seguridad Planificadas

- [ ] Spring Security con JWT para autenticación
- [ ] Autorización basada en roles (RBAC)
- [ ] Rate limiting para APIs
- [ ] CSRF protection
- [ ] Security headers (Content-Security-Policy, X-Frame-Options, etc.)

---

## Seguridad en Dependencias

### Dependencias Principales y Consideraciones de Seguridad

#### 1. Spring Boot Starter Actuator

```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

**Riesgos**:

- Exposición de información sensible del sistema
- Endpoints administrativos accesibles públicamente

**Mitigación**:

```yaml
# src/main/resources/application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics # Solo endpoints necesarios
  endpoint:
    health:
      show-details: when-authorized # Detalles solo con autorización
```

**Recomendaciones**:

- ✅ Limitar endpoints expuestos en producción
- ✅ Proteger endpoints sensibles con autenticación
- ✅ Usar `management.server.port` diferente en producción
- ✅ Habilitar solo los endpoints necesarios

#### 2. Bases de Datos (H2 y PostgreSQL)

```kotlin
runtimeOnly("com.h2database:h2")
runtimeOnly("org.postgresql:postgresql")
```

**H2 Console - Solo para Desarrollo**:

```yaml
spring:
  h2:
    console:
      enabled: false # SIEMPRE false en producción
```

**PostgreSQL - Producción**:

- ✅ Usar variables de entorno para credenciales
- ✅ Nunca hardcodear contraseñas en el código
- ✅ Usar conexiones SSL/TLS
- ✅ Implementar connection pooling seguro
- ✅ Aplicar principio de mínimos privilegios en la base de datos

**Ejemplo de Configuración Segura**:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      connection-timeout: 30000
  jpa:
    show-sql: false # false en producción
    properties:
      hibernate:
        format_sql: false # false en producción
```

#### 3. Spring Data JPA

```kotlin
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
```

**Prevención de SQL Injection**:

- ✅ Usar consultas parametrizadas (JPQL, Criteria API)
- ✅ Evitar construcción manual de SQL con concatenación de strings
- ✅ Validar y sanitizar todas las entradas de usuario
- ❌ NO usar `@Query` con concatenación de strings

**Ejemplo Seguro**:

```kotlin
// ✅ CORRECTO - Parámetro con nombre
@Query("SELECT r FROM Reserva r WHERE r.email = :email")
fun findByEmail(@Param("email") email: String): List<Reserva>

// ❌ INCORRECTO - Vulnerable a SQL Injection
@Query("SELECT r FROM Reserva r WHERE r.email = '${email}'")
fun findByEmailUnsafe(email: String): List<Reserva>
```

#### 4. Validación de Datos

```kotlin
implementation("org.springframework.boot:spring-boot-starter-validation")
```

**Uso de Bean Validation**:

```kotlin
data class CreateReservaRequest(
    @field:NotBlank(message = "El email es requerido")
    @field:Email(message = "Email inválido")
    val email: String,

    @field:NotNull(message = "La fecha es requerida")
    @field:Future(message = "La fecha debe ser futura")
    val fecha: LocalDate,

    @field:Positive(message = "El número de huéspedes debe ser positivo")
    @field:Max(value = 10, message = "Máximo 10 huéspedes")
    val numeroHuespedes: Int
)
```

**Recomendaciones**:

- ✅ Validar TODOS los inputs del usuario
- ✅ Usar anotaciones de validación en DTOs
- ✅ Implementar validaciones custom cuando sea necesario
- ✅ Validar tamaño de arrays y colecciones
- ✅ Validar tipos de datos y rangos

#### 5. Sentry (Monitoreo de Errores)

```kotlin
implementation("io.sentry:sentry-spring-boot-starter-jakarta")
```

**Configuración Segura**:

```yaml
sentry:
  dsn: ${SENTRY_DSN}
  environment: ${SPRING_PROFILES_ACTIVE}
  send-default-pii: false # NO enviar información personal
  traces-sample-rate: 0.1 # 10% sampling en producción
```

**Prevención de Fuga de Información**:

- ✅ NO enviar PII (Personally Identifiable Information)
- ✅ Filtrar datos sensibles en logs
- ✅ Configurar `beforeSend` para sanitizar datos
- ✅ Usar diferentes entornos (dev, staging, prod)

#### 6. Jackson (Serialización JSON)

```kotlin
implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
```

**Prevención de Vulnerabilidades**:

- ✅ Deshabilitar características peligrosas de deserialización
- ✅ Usar DTOs en lugar de entidades JPA directamente
- ✅ Validar JSON con esquemas
- ❌ NO exponer campos sensibles en respuestas

**Configuración Segura**:

```kotlin
@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY)
    }
}
```

#### 7. Spring Boot DevTools

```kotlin
developmentOnly("org.springframework.boot:spring-boot-devtools")
```

**Importante**:

- ✅ DevTools se deshabilita automáticamente en producción
- ✅ Verificar que no esté en el classpath de producción
- ✅ NO incluir `developmentOnly` dependencies en builds de producción

---

## Configuración Segura

### Variables de Entorno

**NUNCA incluir credenciales en el código fuente**. Usar variables de entorno:

```yaml
# application.yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:h2:mem:testdb}
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:}

sentry:
  dsn: ${SENTRY_DSN:}

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:3600000}
```

### Archivos de Configuración

**Estructura Recomendada**:

```
application.yaml          # Configuración base
application-dev.yaml      # Desarrollo (local)
application-test.yaml     # Tests
application-prod.yaml     # Producción (sin credenciales)
```

**En `.gitignore`**:

```
# Secrets y configuración local
application-local.yaml
application-secret.yaml
*.env
.env
.env.local
```

### CORS Configuration

```kotlin
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                System.getenv("ALLOWED_ORIGINS") ?: "http://localhost:3000"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }
}
```

---

## Seguridad en Desarrollo

### Gradle Security

#### 1. Dependency Verification

```bash
# Habilitar verificación de checksums
gradle wrapper --gradle-version 8.x --verification-mode lenient
```

#### 2. OWASP Dependency Check

Agregar plugin para detectar vulnerabilidades:

```kotlin
// build.gradle.kts (futuro)
plugins {
    id("org.owasp.dependencycheck") version "9.0.0"
}

dependencyCheck {
    formats = listOf("HTML", "JSON")
    failBuildOnCVSS = 7.0f
}
```

#### 3. Actualización de Dependencias

```bash
# Ver dependencias desactualizadas
./gradlew dependencyUpdates

# Actualizar Gradle Wrapper
./gradlew wrapper --gradle-version latest
```

### Kotlin Security

#### Null Safety

```kotlin
// ✅ CORRECTO - Usar tipos no-nulos por defecto
fun processUser(email: String, name: String) {
    // ...
}

// ❌ INCORRECTO - Evitar nulls innecesarios
fun processUser(email: String?, name: String?) {
    email?.let { /* ... */ }
}
```

#### Data Classes Inmutables

```kotlin
// ✅ CORRECTO - Inmutable
data class User(
    val id: Long,
    val email: String,
    val name: String
)

// ❌ INCORRECTO - Mutable
data class User(
    var id: Long,
    var email: String,
    var name: String
)
```

### Código Seguro

#### 1. Sanitización de Inputs

```kotlin
import org.springframework.web.util.HtmlUtils

fun sanitizeInput(input: String): String {
    return HtmlUtils.htmlEscape(input.trim())
}
```

#### 2. Prevención de Path Traversal

```kotlin
import java.nio.file.Paths

fun isValidFilePath(path: String): Boolean {
    val normalizedPath = Paths.get(path).normalize().toString()
    return !normalizedPath.contains("..")
}
```

#### 3. Rate Limiting (futuro)

```kotlin
// Con Bucket4j o similar
@RateLimiter(name = "api", fallbackMethod = "fallback")
@GetMapping("/api/resource")
fun getResource(): ResponseEntity<*> {
    // ...
}
```

---

## Seguridad en Producción

### Checklist de Seguridad Pre-Producción

- [ ] **Environment Variables**: Todas las credenciales en variables de entorno
- [ ] **H2 Console**: Deshabilitado (`spring.h2.console.enabled=false`)
- [ ] **Debug Logging**: Deshabilitado (`logging.level.root=WARN`)
- [ ] **SQL Logging**: Deshabilitado (`spring.jpa.show-sql=false`)
- [ ] **Actuator Endpoints**: Solo necesarios y protegidos
- [ ] **HTTPS**: Configurado con certificados válidos
- [ ] **CORS**: Restringido a dominios específicos
- [ ] **Error Messages**: No exponer stack traces completos
- [ ] **Database**: Credenciales seguras, conexión SSL
- [ ] **Dependencies**: Actualizadas sin vulnerabilidades conocidas
- [ ] **DevTools**: No incluido en producción

### Configuración de Producción

```yaml
# application-prod.yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: ${SSL_KEYSTORE_PATH}
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
  error:
    include-message: never
    include-binding-errors: never
    include-stacktrace: never
    include-exception: false

spring:
  h2:
    console:
      enabled: false
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

management:
  endpoints:
    web:
      exposure:
        include: health,metrics
  endpoint:
    health:
      show-details: never

logging:
  level:
    root: WARN
    com.lgzarturo.springbootcourse: INFO
```

### Monitoreo de Seguridad

1. **Sentry**: Monitoreo de errores y excepciones
2. **Actuator + Prometheus**: Métricas de aplicación
3. **Spring Boot Admin** (futuro): Dashboard de monitoreo
4. **Log Aggregation** (futuro): ELK Stack o similar

---

## Auditorías de Seguridad

### Herramientas de Análisis

#### 1. Análisis Estático

```bash
# Detekt - Análisis estático de Kotlin
./gradlew detekt

# KTLint - Estilo de código
./gradlew ktlintCheck
```

#### 2. Análisis de Dependencias

```bash
# Gradle dependency verification
./gradlew dependencies --write-verification-metadata sha256

# OWASP Dependency Check (cuando se agregue)
./gradlew dependencyCheckAnalyze
```

#### 3. Testing de Seguridad

```bash
# Tests unitarios
./gradlew test

# Tests de integración
./gradlew integrationTest

# Cobertura
./gradlew jacocoTestReport
```

### Revisión de Código

Todos los cambios deben pasar por:

1. **Revisión de pares**: Al menos un revisor
2. **CI/CD Checks**: Tests, linters, análisis estático
3. **Security Review**: Para cambios en configuración de seguridad
4. **Dependency Review**: Para actualizaciones de dependencias

---

## Recursos Adicionales

### Documentación Oficial

- [Spring Boot Security](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.security)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP Cheat Sheet Series](https://cheatsheetseries.owasp.org/)
- [Kotlin Security](https://kotlinlang.org/docs/security.html)
- [Spring Security](https://spring.io/projects/spring-security)

### Guías del Proyecto

- [CONTRIBUTING.md](CONTRIBUTING.md) - Guía de contribución
- [WORKFLOW.md](WORKFLOW.md) - Flujo de trabajo de desarrollo
- [ARCHITECTURE.md](docs/ARCHITECTURE.md) - Arquitectura del proyecto

---

## Contacto

Para reportar vulnerabilidades de seguridad:

**Email**: lgzarturo@gmail.com **GitHub**:
[@lgzarturo](https://github.com/lgzarturo)

---

## Licencia

Este proyecto está licenciado bajo [CC-BY-4.0](LICENSE).

---

**Última actualización**: 2025-10-20

**Recuerda**: La seguridad es un proceso continuo, no un destino. Mantente
actualizado con las últimas vulnerabilidades y mejores prácticas.

🔒 **Security First, Always!**

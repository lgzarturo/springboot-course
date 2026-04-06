# Lecciones aprendidas

Kai, el programador aprendiz, llega al Hotel Pokémon, donde cada módulo es una "habitación" o "gimnasio" que debe superar para ganar insignias (badges) y avanzar en su entrenamiento como Maestro Spring Boot. El hotel está dirigido por el Profesor Oak, quien asigna misiones (módulos) y evalúa el progreso. Cada insignia representa un logro en un área específica de Spring Boot/Kotlin, y al final, Kai podrá desafiar a la Liga Pokémon de Desarrolladores (proyecto final).

**Personajes clave:**

- **Profesor Oak:** Guía y mentor (es el perfil de GitHub que comente en los PRs).
- **Líderes de Gimnasio:** Cada módulo tiene un "líder" (ej.: Líder Brock para Seguridad, Líder Misty para Bases de Datos).
- **Team Rocket:** Representa los bugs, errores comunes o malas prácticas que Kai debe evitar/arreglar.

> **[HISTORIAL](HISTORIAL.md):** Revisa el listado de logros para ver las insignias que se pueden conseguir, es la base para el desarrollo del proyecto final.

---

## Misiones

### Configuración de Sentry

**Fecha:** 2025-10-26

**Logros:**

- **Integración de Sentry**: Aprendí cómo configurar y utilizar Sentry para capturar y monitorear errores en una aplicación Spring Boot. Utilicé la biblioteca Sentry para capturar excepciones y enviar informes de errores a la plataforma.
- **Excepciones personalizadas**: Aprendí a crear excepciones personalizadas para simular eventos de error y validar la integración con Sentry. Utilicé la clase SentryTestException para lanzar excepciones específicas para pruebas.
- **Captura de excepciones**: Aprendí cómo capturar excepciones utilizando bloques try-catch y enviar informes de errores a Sentry utilizando el método captureException().
- **Documentación de código**: Aprendí a documentar clases y métodos utilizando comentarios Javadoc para mejorar la legibilidad y mantenibilidad del código.

**Errores y soluciones:**

- **Detekt:** Se encontró que en el archivo SentryController.kt un error de detekt, al momento de capturar la excepción, ya que al estar capturando la excepción en el catch se puede lanzar cualquier excepción con Exception y eso no es recomendable. Lanza el error TooGenericExceptionCaught.
  - Esto se evita creando una Exception personalizada para capturar la excepción y lanzarla en el catch.
- **KLint:** Al compilar el proyecto se encontraron errores de KLint, estos errores se corrigieron. Esto es especialmente importante para mantener la calidad del código y evitar problemas de compilación.
  - Es importante corregir estos errores para que el proyecto se compile correctamente.

#### 🏆 Insignia - Reliquias de Entidades (Configuración de Sentry)

![Insignia - Configuración de Sentry](https://raw.githubusercontent.com/wiki/lgzarturo/springboot-course/badges/badget-03-reliquias-entidades_1024x1024.webp)

---

### Perfiles de Spring Boot

**Fecha:** 2025-10-27

**Logros:**

- **Definir los perfiles**: Se agregan 3 perfiles para la aplicación Spring Boot: "dev", "prod" y "test". Cada perfil tiene su propia configuración de base de datos y otras propiedades específicas, además que el archivo `application.yml` queda con la configuración base y los perfiles sobre escriben algunas propiedades y definen otras.
- **Cobertura completa de perfiles**: Se configuraron y validaron los perfiles dev, prod y test, con `SPRING_PROFILES_ACTIVE` apuntando a dev por defecto y prod utilizado en staging para reproducir el entorno real.
- **Aislamiento de configuraciones sensibles**: Se incorporó `.env` como fuente opcional para credenciales, permitiendo levantar staging sin exponer datos duros y manteniendo los defaults seguros.
- **Optimización por entorno**: Se ajustaron pools de Hikari, logging y parámetros de Hibernate según el caso de uso (diagnóstico en dev, observabilidad controlada en prod y silencio en test) para garantizar estabilidad en staging.
- **Integración con herramientas externas**: Se activó Docker Compose automático en dev y se parametrizó Sentry por entorno, verificando en staging que sólo prod envía eventos con el sample rate esperado.

**Errores y soluciones:**

- **Migraciones en staging**: Al ejecutar staging con prod, Flyway intentó recrear estructuras inexistentes por heredar `ddl-auto=update`; se corrigió usando `ddl-auto=validate` en dev/prod y `create-drop` en test para aislar el esquema.
- **Ruido de logs**: El perfil base heredaba trazas DEBUG/TRACE en staging; se normalizó en prod para dejar root en INFO y reducir la carga de almacenamiento y monitoreo.
- **Eventos duplicados en Sentry**: Con el sample rate por defecto del perfil base se generaban eventos redundantes en staging; se ajustó `traces-sample-rate` en prod y se deshabilitó completamente en test para evitar consumo innecesario.

---

### CRUD con TDD para persistir datos

**Fecha:** 2025-10-27

**Logros:**

- Modelo de dominio y puertos listos: se crearon `src/main/kotlin/com/lgzarturo/springbootcourse/domain/model/Example.kt`, `src/main/kotlin/com/lgzarturo/springbootcourse/domain/port/output/ExampleRepositoryPort.kt` y `src/main/kotlin/com/lgzarturo/springbootcourse/domain/port/input/ExampleUseCase.kt` con responsabilidades claras en la arquitectura hexagonal.
- Caso de uso implementado: `src/main/kotlin/com/lgzarturo/springbootcourse/domain/service/ExampleService.kt` orquesta la creación delegando en el puerto de persistencia.
- Endpoint de creación operativo: `POST /api/v1/examples` con DTOs `ExampleRequest` y `ExampleResponse` en `infrastructure/rest/dto` y controlador `ExampleController`.
- Persistencia real con JPA: `ExampleEntity`, `ExampleJpaRepository` y `ExampleRepositoryAdapter` mapearon dominio↔JPA y persisten en tabla `example_entity`.
- Migración de base de datos: `src/main/resources/db/migration/V1__springboot-course.sql` crea la tabla `example_entity`, alineada con la entidad JPA.
- Pruebas automatizadas: pasan `ExampleServiceTest` (dominio) y `ExampleControllerTest` (web), cubriendo el flujo de creación.
- Documentación TDD actualizada: guía detallada en `docs/course/week-02/06-crud-con-tdd.md` alineada a la rama `feature/milestone-01-persistence`.

**Errores y soluciones:**

- Ruta del endpoint inconsistente: las pruebas usaban `/api/examples` mientras el diseño establecía `/api/v1/examples`. Se normalizó a `/api/v1/examples` en controlador y tests.
- Dependencias de pruebas faltantes: Mockito Kotlin y Kotest no estaban documentadas. Se agregaron las dependencias requeridas en los ejemplos de `build.gradle.kts` dentro de la guía.
- Ajustes de DTOs: se definió `description` como nullable para evitar errores de deserialización y se añadieron métodos `toDomain()` y `fromDomain()` para mantener el mapeo consistente.
- Configuración de persistencia: se verificó que el nombre de la tabla (`@Table(name = "example_entity")`) coincidiera con la migración Flyway; se ajustó y validó.
- Estilo y linter en documentación: se corrigieron ejemplos eliminando `package`/`import` dentro de snippets para cumplir con el linter de docs.

### Ciclo 🔴 Red → 🟢 Green → 🔵 Refactor

- 🔴 Red (dominio): se creó `ExampleServiceTest` que falla al no existir modelo/puertos.
  - Archivo: `src/test/kotlin/com/lgzarturo/springbootcourse/domain/service/ExampleServiceTest.kt`
  - Commit: `test(domain): add ExampleServiceTest for create use case (falla)`
- 🟢 Green (dominio mínimo): se añadieron `Example.kt`, `ExampleRepositoryPort.kt` y `ExampleService` con `create()` delegando en `save()`.
  - Archivos: `src/main/kotlin/com/lgzarturo/springbootcourse/domain/model/Example.kt`, `src/main/kotlin/com/lgzarturo/springbootcourse/domain/port/output/ExampleRepositoryPort.kt`, `src/main/kotlin/com/lgzarturo/springbootcourse/domain/service/ExampleService.kt`
  - Commit: `feat(domain): add Example model, port and minimal ExampleService`
- 🔵 Refactor (dominio): limpieza menor de nombres/estilo.
  - Commit: `refactor(domain): minor clean-ups around Example`
- 🔴 Red (web): se añadió `ExampleControllerTest` para `POST /api/v1/examples` que falla sin controlador/DTOs.
  - Archivo: `src/test/kotlin/com/lgzarturo/springbootcourse/infrastructure/rest/controller/ExampleControllerTest.kt`
  - Commit: `test(web): add ExampleControllerTest for \"POST /api/v1/examples\" (falla)`
- 🟢 Green (web): se añadieron `ExampleUseCase.kt`, `ExampleRequest.kt`, `ExampleResponse.kt` y `ExampleController.kt`.
  - Archivos: `src/main/kotlin/com/lgzarturo/springbootcourse/domain/port/input/ExampleUseCase.kt`, `src/main/kotlin/com/lgzarturo/springbootcourse/infrastructure/rest/dto/request/ExampleRequest.kt`, `src/main/kotlin/com/lgzarturo/springbootcourse/infrastructure/rest/dto/response/ExampleResponse.kt`, `src/main/kotlin/com/lgzarturo/springbootcourse/infrastructure/rest/controller/ExampleController.kt`
  - Commit: `feat(web): add ExampleController + DTOs and wire with ExampleUseCase`
- 🔴 Red → 🟢 Green (persistencia): se agregó la capa JPA y el adaptador que implementa `ExampleRepositoryPort`.
  - Archivos: `src/main/kotlin/com/lgzarturo/springbootcourse/infrastructure/persistence/entity/ExampleEntity.kt`, `src/main/kotlin/com/lgzarturo/springbootcourse/infrastructure/persistence/repository/ExampleJpaRepository.kt`, `src/main/kotlin/com/lgzarturo/springbootcourse/infrastructure/persistence/adapter/ExampleRepositoryAdapter.kt`
  - Commits: `feat(persistence): add ExampleEntity and ExampleJpaRepository`; `feat(persistence): add ExampleRepositoryAdapter implementing ExampleRepositoryPort`
- 🔵 Refactor (docs y orden): se documentó el flujo, se normalizó la ruta y se limpiaron imports.
  - Commit: `docs: update TDD guide with commit log and full paths`

Referencias:

- Guía detallada: [docs/course/week-02/06-crud-con-tdd.md](docs/course/week-03/01-crud-con-tdd.md)
- Rama: [feature/milestone-01-persistence](https://github.com/lgzarturo/springboot-course/tree/refs/heads/feature/milestone-01-persistence)
- HTTP de prueba: [http/example.http](http/example.http) (POST `/api/v1/examples`)

---

# Contexto de clean core

La idea central es mantener la capa de dominio (y, normalmente, la capa de aplicación/uso de casos) libre de dependencias de infraestructura (Spring, JPA, web, etc.). Si anotamos las clases de dominio o puertos con cosas de Spring o se hace el uso de tipos de Spring (p. ej., `Page`, `Pageable`, `ResponseEntity`), estamos acoplando el core a la infraestructura. En esos casos, lo mejor es registrar tus servicios con `@Configuration` y `@Bean` y usa tipos propios en puertos.

De esta forma podemos mantener el core limpio y liberado de dependencias de infraestructura. Una ventaja al momento de hacer los cambios es la batería de tests unitarios que podemos tener en el core, sin necesidad de levantar el contexto de Spring.

## Qué encontré (diagnóstico puntual)

Revisando a detalle el código pude encontrar que hay fugas de infraestructura en el core:

- Capa dominio (OK):
    - `src/main/kotlin/.../example/domain/Example.kt` → sin imports de Spring. Correcto.
    - `src/main/kotlin/.../ping/domain/Ping.kt` → sin imports de Spring. Correcto.

- Puertos de entrada (input) y salida (output):
    - `src/main/kotlin/.../ping/application/ports/input/PingUseCasePort.kt` → limpio (solo depende del dominio). Correcto.
    - `src/main/kotlin/.../example/application/ports/input/ExampleUseCasePort.kt` → importa `org.springframework.data.domain.Page` y `Pageable`. Esto es una fuga de infraestructura hacia el contrato de aplicación.
    - `src/main/kotlin/.../example/application/ports/output/ExampleRepositoryPort.kt` → además de `Page`/`Pageable`, usa DTOs de REST (`ExampleRequest`, `ExamplePatchUpdate`) dentro del puerto. Esta es una fuga importante desde adapters hacia application/core. Los puertos no deben conocer DTOs de transporte.

- “Servicios” (application services) y anotaciones de Spring:
    - `src/main/kotlin/.../ping/service/PingService.kt` → anotado con `@Service`. Esto se define como “Servicio de dominio”, pero depende de Spring. Mejor dejar esta clase pura (sin anotación) y registrarla con `@Configuration`/`@Bean`.
    - `src/main/kotlin/.../example/service/ExampleServicePort.kt` → anotado con `@Service` y acoplado a `Page`/`Pageable`. Sucede la misma fuga: aplicación atada a Spring Data.

- Adapters REST y persistencia (OK que usen Spring):
    - `.../adapters/rest/*` (controladores, mappers), `.../adapters/persistence/*` (repositorios JPA) usan Spring. Esto es lo esperado en la capa de infraestructura.

Resumen de fugas detectadas:
- Fuga 1 (media): `@Service` en clases de servicio de aplicación (`PingService`, `ExampleServicePort`).
- Fuga 2 (alta): `Page`/`Pageable` en puertos (`ExampleUseCasePort`, `ExampleRepositoryPort`).
- Fuga 3 (alta): DTO's de REST usados en el puerto de salida (`ExampleRepositoryPort` usa `ExampleRequest`, `ExamplePatchUpdate`). Esto es una fuga importante desde adapters hacia application/core. Es complicado de corregir, pero es una fuga importante.

## Checklist práctico para corregir las fugas detectadas

Si algo falla en estas reglas en las capas de dominio/aplicación, hay fuga de infraestructura, por lo que es importante revisarlas:

1) Imports prohibidos en dominio y aplicación
   - No debe aparecer `import org.springframework...` (incluye `@Service`, `@Component`, `@Transactional`, `Page`, `Pageable`, `ResponseEntity`, `HttpStatus`, `RestControllerAdvice`, etc.).
   - No debe aparecer `import jakarta...`/`javax...` (validación) ni `org.hibernate...`/`jakarta.persistence...` (`@Entity`).
   - Permitidos: Kotlin stdlib, `java.time`, clases del propio dominio y tipos propios del core.
   - Si necesitas importar algo de Spring, usa un adapter que lo exponga al contenedor.

2) Puertos (input/output)
   - No deben referenciar DTOs de adapters (`...adapters.rest.dto...`) ni entidades de persistencia (`...adapters.persistence.entity...`).
   - Los puertos definen contratos en términos de dominio y tipos genéricos propios del core.
   - Los puertos no deben conocer de Spring.

3) Servicios de aplicación (o “casos de uso”)
   - No deben tener anotaciones de Spring. Se registran con `@Configuration`/`@Bean` o con un adapter que los expone al contenedor.
   - En el caso de `@Service`, no debe tener `@Transactional`.
   - Si necesitas `@Transactional`, lo coloques en el puerto de aplicación (adapter) que delega al servicio puro.

4) Excepciones y modelos de error
   - No usar excepciones HTTP de Spring en dominio/aplicación. Lanza excepciones del dominio y tradúcelas en adapters (REST) a `ResponseEntity`.
   - Los modelos de error deben ser tipos propios del core.

5) Validación
   - En dominio, usar invariantes, value objects y constructores seguros. Las anotaciones de `jakarta.validation` deben vivir en los DTOs de entrada/salida (adapters), no en el dominio.
   - Cada clase de dominio debe tener un constructor seguro.
   - En el core, usar `jakarta.validation` y `jakarta.validation.constraints`. 

6) Paginación y contratos transversales
   - Evita `Page`/`Pageable` en puertos. Define `PageRequest`/`PageResult` propios.
   - Los puertos de salida no deben devolver `Page`/`Pageable`.

Sugerencia para automatizar la revisión:

- “Find in Path” en IntelliJ sobre `src/main/kotlin` con patrón `import org.springframework` y limitar la búsqueda a rutas `*/domain/*`, `*/application/*`, `*/service/*`.
- Busca también referencias a `Page`, `Pageable`, `ResponseEntity`, `HttpStatus` en esas mismas rutas.

## Cómo corregir las fugas detectadas (patrones recomendados)

1) Quitar `@Service` de los servicios del core y registrarlos con configuración
   - Antes (acoplado):
     ```kotlin
     // PingService.kt
     @Service
     class PingService : PingUseCasePort { /* ... */ }
     ```
   - Después (puro) + configuración:
     ```kotlin
     // PingService.kt
     class PingService : PingUseCasePort { /* ... */ }

     // PingConfig.kt
     @Configuration
     class PingConfig {
         @Bean
         fun pingUseCase(): PingUseCasePort = PingService()
     }
     ```
     Aplica lo mismo para `ExampleServicePort`: clase sin anotación y un `ExampleConfig` que la expone vía `@Bean`.

2) Sustituir `Page`/`Pageable` en puertos por tipos del core
   - Define tipos neutrales:
     ```kotlin
     data class PageRequest(val page: Int, val size: Int, val sort: List<SortOrder> = emptyList())
     data class SortOrder(val property: String, val direction: Direction) {
         enum class Direction { ASC, DESC }
     }
     data class PageResult<T>(val items: List<T>, val total: Long, val page: Int, val size: Int)
     ```
   - Cambia el puerto de entrada:
     ```kotlin
     interface ExampleUseCasePort {
         fun findAll(searchText: String?, request: PageRequest): PageResult<Example>
     }
     ```
   - En el controller (adapter REST) traduces `Pageable` ⇄ `PageRequest`, y si necesitas devolver `Page<ExampleResponse>`, conviertes `PageResult` a `Page` solo en el adapter.

3) Evitar DTOs de REST en puertos
   - En `ExampleRepositoryPort`, reemplaza parámetros/retornos `ExampleRequest`/`ExamplePatchUpdate` por `Example` o por un tipo de patch del core (p. ej., `ExamplePatch` en el módulo de aplicación) y deja que el adapter REST haga la traducción DTO ⇄ dominio.
     ```kotlin
     data class ExamplePatch(val property: String, val value: String)
     ```

4) Transacciones
   - Si necesitas `@Transactional`, no lo coloques en el servicio puro. Crea un adapter de aplicación (por ejemplo, `TransactionalExampleUseCase`) que implementa el puerto, esté anotado con `@Transactional` y delegue al servicio puro. Esto mantiene el core limpio.

## Siguiendo Screaming Architecture + Hexagonal

Se busca que se use una *clean architecture* aplicada por módulos de features. 

Simplificando para explicarlo simple:

- En Screaming Architecture, cada feature debe hablar del problema que resuelve, no de la tecnología que usa.
- En la estructura actual, esto ya sucede: `bookings/`, `rooms/`, `guests/` son bounded contexts claros.
- Vale la pena señalar que la arquitectura hexagonal vive dentro de cada feature, no por encima.

> Esto ayuda a evitar debates sobre “poner capas globales” o “un solo módulo application”.
> Y se aclara que se esta usando “hexagonal por feature” y no “hexagonal global”

Debido a que Hexagonal por módulo tiene ventajas:

-  Cada feature evoluciona sin destruir otra.
-  Se puede cambiar la persistencia de `bookings` sin tocar `rooms`.
-  Los puertos de un módulo no contaminan a otro módulo.

De esta forma queda claro que el core es limpio y libre de dependencias de infraestructura:

- **Toda la configuración Spring vive en `shared/config` y en los adapters.**
- Ningún módulo de dominio o aplicación debe tener anotaciones.
- Ningún módulo de dominio debe tener dependencias directas de Spring.

## 1) Patrón “Application Service puro + Orchestrator Adapter”

Cuando un caso de uso necesita transacciones, seguridad o métricas, la solución correcta sería:

```
[application-service puro]  ←  [application-adapter con @Transactional]
```

Beneficios:

- Deja claro que el puerto define la operación
- El servicio la implementa
- El adapter envuelve con las capacidades de Spring.

## 2) Aclarar el rol del módulo `shared`

- `shared` no es un basurero de utilerías.
- Solo lleva piezas **verdaderamente transversales**.
- Nunca debe existir un “shared/domain” ni un “shared/application”.

## 3) Ejemplo concreto de traducción entre puertos y adapters

Siguiendo el patrón de tipos propios en puertos, la traducción entre `Pageable` y `PageRequest` se hace en el adapter REST:

```kotlin
// Adapter REST recibe Pageable
@GetMapping
fun find(@ParameterObject pageable: Pageable): Page<BookingResponse> {
    val request = pageRequestMapper.fromPageable(pageable)
    val result = useCase.findAll(request)
    return pageResponseMapper.toPage(result)
}
```

Con este ejemplo es imposible que alguien vuelva a meter `Pageable` en el core.

## 4. La estructura por features

La estructura por features es clara y compatible con Screaming Architecture:

```
feature/
 ├── domain
 ├── application
 └── adapters/
      ├── rest
      └── persistence
```

> Es exactamente lo que se recomienda para “hexagonal por módulo”.
> Se puede llegar a mejorar (opcional):
>  - Agregar `application/ports/input` y `application/ports/output` explícitamente en todos los módulos para mantener simetría.
>  - Si ya existe en algunos módulos, es necesario intentar ser consistente en todos.

## 5. Dividir `application` en casos de uso

En lugar de ponerlo todo en `application` suelto:

```
application/
 ├── ports
 └── usecases
      ├── CreateBooking
      ├── CancelBooking
      └── GetBooking
```

Esto te da tres ventajas:

- Navegación más limpia
- Casos de uso más explícitos
- Más fácil de detectar fugas

## 6. Crea un módulo `shared-core` independiente de Spring

- Solo si el proyecto crece mucho.
- Esto evita dependencias accidentales desde features.

## 7. Sellar el dominio con `sealed classes` o value objects

Ayuda a que los invariantes del modelo vivan ahí y no en los DTOs.
- Por ejemplo, en lugar de usar `String` para un email, crea un value object `Email`.
- Esto evita que se pierdan los invariantes.

# Dominar HTTP y la Comunicación Efectiva entre Microservicios

Cuando hablamos de arquitecturas distribuidas, una API REST no es solo un conjunto de endpoints. También es un **idioma
compartido** que permite que sistemas heterogéneos colaboren con precisión. Pero, ¿cómo asegurar que este lenguaje sea
claro, consistente y profesional? La respuesta está en entender que **HTTP no es un transporte técnico, sino un
protocolo semántico**.

En este documento abordaré cuál es su importancia, cómo usarlo de forma estratégica, por qué JSON se convierte en el
puente universal entre microservicios y por qué la documentación y los metadatos son tan críticos como el código mismo.

---

## Métodos HTTP: La Gramática de una API Profesional

Hay que entender que el protocolo HTTP define **verbos estándar** que no son meras convenciones, sino reglas de
comunicación con significado intrínseco. Usarlos de forma inadecuada es como confundir "pedir" con "ordenar" en una
conversación: genera ambigüedad y errores.

### ¿Por qué la elección del método importa?

- **GET** no solo "obtiene datos", sino que por definición debe **ser idempotente y seguro** (*no modifica el estado del
  servidor*).  
  *Ejemplo:*  
  Un `GET /reservations?status=pending` debe devolver siempre el mismo resultado si nadie modifica las reservas, sin
  efectos colaterales.

- **POST** no es "el método por defecto". Es para **crear recursos nuevos** con un *significado único*:  
  *Ejemplo:*  
  `POST /reservations` crea una nueva reserva, mientras que `POST /reservations/cancel` sería un antipatrón (*debería
  usarse `DELETE` o `PATCH`*).

- **PUT vs PATCH**:
    - **PUT** reemplaza **todo** el recurso (*ej.: actualizar todos los campos de un usuario*).
    - **PATCH** modifica **solo partes** (*ej.: cambiar el teléfono, sin tocar el correo*).  
      Usar `PUT` para actualizaciones parciales viola la semántica del protocolo y genera conflictos en sistemas
      concurrentes.

### Uno de los errores más comunes: Sobrecargar POST

Cuando una API usa `POST` para todas las operaciones, pierde la capacidad de:

- Aprovechar cachés HTTP (*GET es cacheable; POST no*).
- Definir transacciones idempotentes (*reintentar un PUT no duplica recursos*).
- Ser autoexplicativa (*¿qué hace un `POST /users/action?`?*).

> **Clave profesional:** Si tu endpoint requiere un verbo como "update" o "create" en el nombre, estás ignorando la
> semántica de HTTP. Asigna el verbo correcto y deja que el endpoint refleje el recurso, esa es la verdadera claridad.

---

## JSON: El Latín Moderno de las APIs

Si HTTP es la gramática, **JSON es el alfabeto universal** que permite que microservicios de distintas tecnologías se
entiendan. ¿Por qué no XML, Protocol Buffers o GraphQL? Aquí las ventajas que he comprobado en la práctica.

> No estoy diciendo que JSON es perfecto, pero en el contexto de APIs REST, su simplicidad y adopción lo hacen la opción
> más profesional. Para casos específicos, otras opciones pueden ser mejores, pero requieren un análisis cuidadoso y no es
> el objetivo de este documento.

### ¿Por qué JSON triunfa en APIs REST?

| **Característica**       | **JSON**                                                                   | **Alternativas**                              |
|--------------------------|----------------------------------------------------------------------------|-----------------------------------------------|
| **Legibilidad**          | Humano y máquina lo entienden a primera vista.                             | XML es verbose; Protocol Buffers es binario.  |
| **Adopción**             | Soportado por todos los lenguajes sin librerías adicionales.               | gRPC requiere generación de código.           |
| **Flexibilidad**         | Estructura dinámica (*ej.: campos opcionales en `PATCH`*).                 | XML exige esquemas rígidos.                   |
| **Integración con HTTP** | Se mapea naturalmente a los verbos HTTP (*ej.: enviar un objeto en POST*). | SOAP añade capas innecesarias de complejidad. |

### ¿Cuándo NO usar JSON?

- En sistemas con **alto rendimiento** (*ej.: transacciones financieras en tiempo real, sistemas de trading,
  aplicaciones de juegos en línea*), Protocol Buffers o gRPC son mejores por su compresión binaria y su eficiencia.
- En escenarios donde el cliente necesita **solicitar solo campos específicos**, GraphQL es más eficiente, debido a su
  capacidad de consulta flexible.

Pero en la mayoría de APIs REST, **JSON es el equilibrio perfecto** entre simplicidad, interoperabilidad y expresividad.

---

## Documentación: La Carta de Navegación de tu API

Una API bien diseñada es intuitiva, pero **la documentación no es opcional**. Es el puente entre tu sistema y quienes lo
consumen. Asumir que "el código habla por sí mismo" es un error profesional. Así que como programadores profesionales
debemos evitar malentendidos y reducir la fricción.

### Elementos clave de una documentación profesional

1. **Descripciones claras de endpoints**  
   No basta con `/users/{id}`. Debe explicar:
    - ¿Qué representa `{id}`? (UUID, número entero, etc.).
    - Ejemplos de respuesta para cada código de estado (200, 404, 422).
    - Parámetros de query con sus valores válidos (*ej.: `?status=active,archived`*).

2. **Ejemplos de solicitud y respuesta**  
   Mostrar cómo se estructura una petición `PATCH` con campos obligatorios y opcionales, no solo el esquema abstracto.

3. **Códigos de estado semánticos**  
   Explicar por qué un `409 Conflict` ocurre (*ej.: "La habitación está ocupada"*) y cómo resolverlo.

4. **Autenticación y autorización**  
   Detallar cómo se obtiene el token, su vigencia y qué endpoints requieren permisos específicos.

### Herramientas esenciales

- **OpenAPI (Swagger)**: Permite generar documentación interactiva a partir de anotaciones en el código.
- **Redoc**: Genera documentación estática a partir de especificaciones OpenAPI.
- **Documentos http**: Explicar casos de uso complejos.
- **Postman Collections**: Compartir ejemplos ejecutables de peticiones.
- **Changelog**: Registrar cambios en versiones (*ej.: "v2 elimina el campo `legacyId`"*).

> **Error fatal:** Documentar solo el "camino feliz" (200 OK). Los consumidores necesitan saber cómo manejar errores.

---

## Metadatos: El Contexto que Evita Suposiciones

Las respuestas JSON no deben ser solo "datos crudos". Los **metadatos** proporcionan el contexto necesario para que el
cliente actúe con inteligencia.

### Ejemplos críticos de metadatos

- **Paginación**:

  ```json
  {
    "data": [],
    "metadata": {
      "totalItems": 150,
      "currentPage": 3,
      "itemsPerPage": 10,
      "nextPage": "/api/v1/reservations?page=4"
    }
  }
  ```

  Sin esto, el cliente no sabe si hay más resultados o cómo navegar.

- **Fechas de expiración**:  
  Un `Cache-Control: max-age=3600` en el header HTTP o un campo `expiresAt` en el JSON ayuda a gestionar la caché.

- **Estado del recurso**:  
  En una reserva, incluir `"status": "confirmed"` y `"lastUpdated": "2024-06-01T12:00:00Z"` evita que el cliente asuma
  que los datos son actuales.

### ¿Por qué importan?

- Reducen la cantidad de peticiones innecesarias (*ej.: no se requiere un `GET /reservations/count` si el metadata ya lo
  incluye*).
- Permiten que los clientes reaccionen ante cambios (*ej.: si `expiresAt` es pasado, invalidar la caché*).

---

## Errores Semánticos: Diagnósticos, No Síntomas

Un `500 Internal Server Error` es como decir "algo falló". Un **error profesional** explica **qué falló**, **por qué** y
**cómo solucionarlo**.

### Estructura ideal de una respuesta de error

```json
{
  "errorCode": "RESERVATION_CONFLICT",
  "message": "La habitación 101 ya está reservada para las fechas solicitadas.",
  "details": {
    "conflictingReservationId": "RES-2024-001",
    "availableAlternatives": [
      "/api/v1/rooms/102",
      "/api/v1/rooms/103"
    ]
  },
  "timestamp": "2024-06-01T12:05:30Z"
}
```

### Reglas para errores profesionales

1. **Usar códigos de estado HTTP correctos**:
    - `400 Bad Request`: Solicitud mal formada (*ej.: JSON inválido).
    - `422 Unprocessable Entity`: Solicitud válida pero con lógica de negocio fallida (*ej.: 6 huéspedes en una
      habitación para 4*).
    - `409 Conflict`: Recurso en estado incompatible (*ej.: intentar cancelar una reserva ya finalizada*).

2. **Evitar mensajes genéricos**:  
   "Error 500" → "Error en el servidor".  
   "RESERVATION_CONFLICT" → "La habitación está ocupada. Alternativas: 102, 103".

3. **Incluir acciones sugeridas**:  
   Si una reserva falla por fechas, sugerir fechas disponibles en lugar de solo reportar el error.

---

## La Profesionalidad está en los Detalles

Una API REST no es profesional por su funcionalidad, sino por **cómo comunica**. Al dominar:

- La **semántica de HTTP** (*verbos, códigos de estado*),
- La **estructura de JSON** (*metadatos, errores claros*),
- La **documentación proactiva** (*ejemplos, casos de error*),

Transformas tu API en un **sistema autónomo** que minimiza la fricción para quienes la usan. No es solo "hacer que
funcione": es construir un lenguaje que **elimina la necesidad de adivinar**.

**La claridad en la comunicación no es un lujo:** es la diferencia entre un sistema que escala y uno que se derrumba
bajo su propia complejidad.

> **Recuerda:**  
> *Una API bien diseñada no requiere explicación. Su lenguaje es tan claro que habla por sí misma.*
>
> **¿Por qué tu API debe hablar como un profesional?**  
> Porque el protocolo HTTP no es solo un transporte técnico: es el *lenguaje universal* que define cómo interactúan los
> sistemas. Una API que aprovecha al máximo HTTP se vuelve intuitiva, robusta y autoexplicativa. En este documento,
> aprenderás a usarlo como un **arma estratégica** en lugar de un mero mecanismo de comunicación.

---

## ¿Qué hace una API "profesional"?

| **Característica**           | **API Amateur**             | **API Profesional**                             |
|------------------------------|-----------------------------|-------------------------------------------------|
| **Métodos HTTP**             | Usa siempre POST para todo  | Elige el método correcto según la acción        |
| **Códigos de estado**        | Siempre 200 o 500           | Usa códigos semánticos (201, 409, 422, etc.)    |
| **Estructura de respuestas** | Respuestas sin estándar     | JSON consistente con metadatos y errores claros |
| **Documentación**            | "Funciona, pero no sé cómo" | Autoexplicativa gracias a HTTP + OpenAPI        |

---

## Casos Prácticos

### Hotel: Gestión de Reservas

| Método    | Uso                               | Ejemplo Real                                                                 |
|-----------|-----------------------------------|------------------------------------------------------------------------------|
| **GET**   | Listar recursos                   | `GET /rooms?status=available` → Habitaciones libres                          |
| **POST**  | Crear un recurso nuevo            | `POST /reservations` → Nueva reserva (con validación de límite de huéspedes) |
| **PUT**   | Reemplazar recurso completo       | `PUT /rooms/101` → Actualizar estado y todos los atributos                   |
| **PATCH** | Modificar parcialmente un recurso | `PATCH /rooms/101` → Cambiar solo el estado a `"limpieza en progreso"`       |

#### Ejemplo de POST con Validación (Kotlin + Spring Boot)

```kotlin
@PostMapping("/reservations")
fun createReservation(
    @Valid @RequestBody reservation: ReservationRequest
): ResponseEntity<ReservationResponse> {
    if (reservation.guests > MAX_GUESTS) {
        throw ReservationException("Exceso de huéspedes. Límite: $MAX_GUESTS")
    }
    if (roomService.isOccupied(reservation.roomId)) {
        throw ConflictException("La habitación ya está reservada")
    }
    val newReservation = reservationService.save(reservation)
    return ResponseEntity.created(URI("/reservations/${newReservation.id}")).body(newReservation)
}
```

---

## Códigos de Estado que Cuentan Historias

### Tabla de Códigos Clave

| Código  | Significado          | Escenario en Hotel                                        | ¿Por qué es profesional?                    |
|---------|----------------------|-----------------------------------------------------------|---------------------------------------------|
| **201** | Created              | Reserva creada exitosamente                               | Confirma creación + devuelve ubicación      |
| **409** | Conflict             | Habitación ya reservada                                   | Indica conflicto lógico (no error genérico) |
| **422** | Unprocessable Entity | Solicitud inválida (ej: 5 huéspedes en habitación para 4) | Valida reglas de negocio                    |
| **304** | Not Modified         | Cliente consulta habitaciones y no hay cambios            | Ahorra ancho de banda                       |

#### Ejemplo de Manejo de Conflictos (Spring Boot)

```kotlin
@ExceptionHandler(ConflictException::class)
fun handleConflict(ex: ConflictException): ResponseEntity<ErrorResponse> {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse("CONFLICT", ex.message ?: "Recurso en conflicto"))
}
```

---

## JSON: El Idioma Universal de las APIs

### Ejemplos en Diferentes Dominios

#### Hotel: Respuesta de Habitación

```json
{
  "id": "101",
  "number": "101",
  "type": "Suite Deluxe",
  "status": "available",
  "amenities": [
    "Wi-Fi 5G",
    "Minibar",
    "Vista al mar"
  ],
  "maxGuests": 4
}
```

#### Pokémon: Entidad de Pokémon

```json
{
  "id": "pikachu",
  "name": "Pikachu",
  "type": "Electric",
  "abilities": [
    "Static",
    "Lightning Rod"
  ],
  "evolutions": [
    "Raichu"
  ],
  "isThemedRoomAvailable": true
}
```

#### E-commerce: Reserva Híbrida (Hotel + Pokémon)

```json
{
  "id": "RES-2024-PIKACHU",
  "roomId": "202",
  "theme": "Pikachu Suite",
  "guests": 2,
  "services": [
    {
      "name": "Desayuno temático",
      "price": 15.0
    },
    {
      "name": "Tour Pokémon",
      "price": 25.0
    }
  ],
  "total": 185.50
}
```

---

## Ejemplos Prácticos: De la Teoría a la Implementación

### 1. Hotel: Listar Habitaciones Disponibles

```http
GET /rooms?status=available&type=suite HTTP/1.1
Accept: application/json
```

**Respuesta:**

```json
{
  "data": [
    {
      "id": "202",
      "number": "202",
      "type": "Pikachu Suite",
      "status": "available",
      "amenities": [
        "TV 4K",
        "Zona de juego",
        "Cama temática"
      ]
    }
  ],
  "metadata": {
    "total": 1,
    "page": 1,
    "limit": 10
  }
}
```

### 2. Pokémon: Evolución de una Habitación Temática

```http
PATCH /rooms/202 HTTP/1.1
Content-Type: application/json

{
  "theme": "Charizard Suite",
  "amenities": ["Hidromasaje", "Vista al volcán"]
}
```

**Respuesta:**

```json
{
  "id": "202",
  "previousTheme": "Pikachu Suite",
  "newTheme": "Charizard Suite",
  "status": "under_maintenance",
  "eta": "2024-06-01T08:00:00Z"
}
```

### 3. E-commerce: Reserva con Pago

```http
POST /reservations HTTP/1.1
Content-Type: application/json

{
  "roomId": "202",
  "guests": 2,
  "checkIn": "2024-07-01",
  "checkOut": "2024-07-05",
  "paymentMethod": "credit_card"
}
```

**Respuesta (201 Created):**

```http
HTTP/1.1 201 Created
Location: /reservations/RES-2024-PIKACHU
```

---

## Best Practices con Spring Boot 3 + Kotlin

### 1. Configuración de Bases de Datos

**`application.yml` (Desarrollo con H2)**

```yaml
spring:
  profiles: dev
  datasource:
    url: jdbc:h2:mem:hotel_db
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

**`application-prod.yml` (Producción con PostgreSQL)**

```yaml
spring:
  profiles: prod
  datasource:
    url: jdbc:postgresql://db:5432/hotel_db
    username: admin
    password: ${DB_PASSWORD}
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 2. Validación en Kotlin

```kotlin
data class ReservationRequest(
    @field:NotNull
    @field:Size(min = 1, max = 4, message = "Máximo 4 huéspedes")
    val guests: Int,

    @field:Pattern(regexp = "^(10[1-9]|20[1-9])$", message = "Habitación inválida")
    val roomId: String
)
```

### 3. Controladores Profesionales

```kotlin
@RestController
@RequestMapping("/api/v1")
class ReservationController(
    private val reservationService: ReservationService
) {
    @PostMapping("/reservations")
    fun create(@Valid @RequestBody req: ReservationRequest): ResponseEntity<Reservation> {
        // Lógica de negocio
    }

    @GetMapping("/rooms/available")
    fun getAvailableRooms(
        @RequestParam status: String = "available",
        @PageableDefault(size = 10) page: Pageable
    ): Page<RoomResponse> {
        return roomService.findAvailable(page)
    }
}
```

### 4. Manejo de Errores

```kotlin
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class ValidationException(message: String) : RuntimeException(message)

@ExceptionHandler(ValidationException::class)
fun handleValidation(ex: ValidationException): ErrorResponse {
    return ErrorResponse("VALIDATION_ERROR", ex.message)
}
```

---

## Usa HTTP como Arma Estratégica

- **Métodos HTTP correctos** = Menos documentación necesaria (*la API se autoexplica*).
- **Documentación clara** = Menos soporte técnico requerido.
- **Metadatos útiles** = Clientes más inteligentes y eficientes.
- **Errores detallados** = Menos confusión y mejor experiencia de usuario.
- **Códigos de estado precisos** = Menos tiempo en debugging para los clientes.
- **JSON estructurado** = Integraciones más rápidas con otros sistemas.
- **Spring Boot + Kotlin** = Código seguro y legible con menos boilerplate.

> **"Una API que habla HTTP correctamente no necesita documentación: su lenguaje es universal."**  
> Domina el protocolo, no lo sobrecargues. Cada `409 Conflict` o `201 Created` es una conversación clara entre sistemas.

---

## Recursos Adicionales

- [RFC 7231 - HTTP/1.1 Semantics and Content](https://tools.ietf.org/html/rfc7231)
- [Spring Boot 3.2 Documentation](https://spring.io/projects/spring-boot)
- [Kotlin Coroutines + Spring WebFlux](https://kotlinlang.org/docs/coroutines-guide.html)

> **¿Listo para que tu API hable como un profesional?**  
> ¡Aplica estos principios y transforma tu API en un estándar de la industria!

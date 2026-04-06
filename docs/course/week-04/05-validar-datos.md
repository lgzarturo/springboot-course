# Validar datos en Spring Boot con Kotlin

Para validar payloads en Spring Boot con Kotlin, es necesario usar clases de
datos con anotaciones como `@get:NotBlank`. Para optimizar consultas JPA
dinámicas, se puede usar Specifications y técnicas como indexación y paginación.

---

## Validación de Payloads

La validación de payloads se realiza usando clases de datos y anotaciones de
validación. Aplica anotaciones como `@get:NotBlank` o `@get:Email` a las
propiedades de las clases de datos para verificar datos, y usa
`@Valid @RequestBody` en los controladores para activar la validación. Maneja
errores con `@ControllerAdvice` para devolver mensajes claros.
Sorprendentemente, en Kotlin, las anotaciones necesitan el prefijo `@get:` para
aplicarlas a los métodos getter, algo diferente a Java.

> [Ver el DTO ExampleRequest.kt para tener mayor claridad.](../../../src/main/kotlin/com/lgzarturo/springbootcourse/example/adapters/rest/dto/request/ExampleRequest.kt)

---

## Optimización de Consultas JPA para Búsquedas, Filtros y Ordenamientos Dinámicos

Para consultas JPA dinámicas, una recomendación es usar Specifications de Spring
Data JPA para construir queries flexibles y seguras. Asegúrate de indexar
columnas usadas en filtros, usa estrategias de carga (lazy o eager), y aplica
paginación con `Pageable` para mejorar el rendimiento. Es interesante que
Specifications permiten combinar criterios de forma modular, facilitando la
mantenibilidad.

---

### Uso de Clases de Datos y Anotaciones de Validación

En Kotlin, las clases de datos (`data class`) son ideales para definir la
estructura de los payloads, ya que son concisas y proporcionan funcionalidad
automática para getters, setters y otros métodos. Sin embargo, un detalle
importante es que las anotaciones de validación, como `@NotBlank`, `@Email` o
`@Size`, deben aplicarse con el prefijo `@get:` para asociarse correctamente con
los métodos getter, debido a cómo Kotlin maneja las propiedades. Por ejemplo:

```kotlin
data class UserRequest(
    @get:NotBlank(message = "El nombre es obligatorio")
    val name: String,
    @get:Email(message = "Dirección de correo inválida")
    val email: String
)
```

> Este enfoque asegura que las validaciones se apliquen a los valores de las
> propiedades, respetando las características de nulabilidad de Kotlin.

---

### Validación en Controladores

En los controladores, se utiliza la anotación `@Valid` junto con `@RequestBody`
para activar la validación automática del payload. Por ejemplo:

```kotlin
@RestController
@RequestMapping("/users")
class UserController {
    @PostMapping
    fun createUser(@Valid @RequestBody userRequest: UserRequest): ResponseEntity<User> {
        // Lógica para crear el usuario
        return ResponseEntity.ok(user)
    }
}
```

Spring Boot automáticamente válida el objeto `userRequest` antes de ejecutar el
método, lanzando una excepción `MethodArgumentNotValidException` si hay errores.

> La validación de payloads en aplicaciones RESTful es crucial para garantizar
> que los datos recibidos cumplan con los requisitos esperados. En Spring Boot
> con Kotlin, esta tarea se facilita mediante el uso de la API de Validación de
> Beans (JSR-380) y Hibernate Validator, integrados de manera nativa en Spring
> Boot.

---

### Manejo de Excepciones de Validación

Para manejar errores de validación de manera uniforme, se recomienda usar
`@ControllerAdvice`, que permite capturar excepciones globalmente y devolver
respuestas HTTP adecuadas, como códigos 400 (Bad Request) con detalles de los
errores. Un ejemplo sería:

```kotlin
@RestControllerAdvice
class GlobalValidationHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(ex: MethodArgumentNotValidException): Map<String, String> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage ?: "Error desconocido" }
        return mapOf("errors" to errors)
    }
}
```

---

### Uso de Specifications para Consultas Dinámicas

Una práctica recomendada es usar Specifications de Spring Data JPA, que permiten
construir consultas dinámicas de manera typesafe y modular. Las Specifications
son interfaces que definen predicados reutilizables, ideales para combinar
múltiples criterios de filtro. Por ejemplo, para filtrar estudiantes por nombre
y estado de inscripción:

```java
public class HotelSpecification {
    public static Specification<Hotel> hasName(String name) {
        return (root, query, cb) -> cb.equal(root.get("name"), name);
    }

    public static Specification<Hotel> isActive(Boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }
}
```

En el repositorio, extiende `JpaSpecificationExecutor` para usar `findAll` con
Specifications:

```java
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {
}
```

Luego, en el servicio, puedes combinarlas:

```java
Specification<Hotel> spec = Specification.where(HotelSpecification.hasName("Hotel Pokémon"))
    .and(HotelSpecification.isEnrolled(true));
List<Hotel> hotels = hotelRepository.findAll(spec);
```

Esto permite construir queries dinámicas sin escribir JPQL o SQL manualmente,
reduciendo el riesgo de errores y mejorando la mantenibilidad.

Otra forma de implementar busquedas es usar Query by Example (QBE) o Criteria
API, pero Specifications suelen ser más legibles y fáciles de mantener. Ver el
ejemplo de la clase `ExampleRepositoryAdapter` anterior que hace uso de
`ExampleMatcher` para realizar busquedas basadas en ejemplos.

> [Ver el archivo ExampleRepositoryAdapter.kt para tener mayor claridad.](../../../src/main/kotlin/com/lgzarturo/springbootcourse/example/adapters/persistence/ExampleRepositoryAdapter.kt)

##### Implementación de Filtros y Ordenamientos Dinámicos

Para filtros dinámicos, se puede usar un DTO como `FilterDTO` con campos como
`columnName` y `columnValue`, y construir la Specification en tiempo de
ejecución. Para ordenamientos, Spring Data JPA soporta `Sort` y `Pageable`, que
permiten especificar el criterio de orden (ascendente o descendente) y paginar
los resultados. Por ejemplo:

```java
Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
Page<Hotel> hotels = hotelRepository.findAll(spec, pageable);
```

Es importante ajustar la paginación, ya que JPA usa índices basados en cero,
mientras que las interfaces de usuario suelen empezar en uno.

---

##### Técnicas de Optimización de Rendimiento

Además de Specifications, hay varias técnicas para optimizar el rendimiento:

- **Indexación Adecuada:** Asegúrate de que las columnas usadas en WHERE, JOIN y
  ORDER BY tengan índices en la base de datos. Por ejemplo, usa
  `@org.hibernate.annotations.Index` en entidades para columnas frecuentemente
  filtradas.

- **Estrategias de Carga:** Usa lazy loading (`fetch = FetchType.LAZY`) para
  asociaciones no siempre necesarias y eager loading (`fetch = FetchType.EAGER`)
  o `JOIN FETCH` en JP QL para asociaciones frecuentemente accedidas, evitando
  el problema N+1.

- **Batch Fetching:** Aplica `@BatchSize` en entidades para cargar múltiples
  registros en una sola consulta, reduciendo consultas adicionales. Por ejemplo,
  `@Entity @BatchSize(size = 10)` en una entidad `Order` para cargar
  `OrderItem`.

- **Proyecciones y Consultas Eficientes:** Evita `SELECT *` y usa proyecciones
  para devolver solo los campos necesarios, como
  `SELECT NEW com.example.dto.ProductDTO(p.id, p.name) FROM Product p`.

- **Paginar y Ordenar:** Usa `Pageable` para limitar el número de resultados,
  mejorando el rendimiento con grandes conjuntos de datos. Por ejemplo,
  `Page<Product> findProducts(String keyword, Pageable pageable)`.

---

##### Comparación y Consideraciones

El uso de Specifications es preferible sobre Criteria API o queries nativas, ya
que ofrece mayor legibilidad y seguridad tipada. Sin embargo, para casos muy
complejos, Querydsl puede ser una alternativa, aunque requiere configuración
adicional. Es importante analizar el plan de ejecución de las consultas y
ajustar índices según el uso, especialmente en bases de datos grandes.

---

### Mensajes de Error Internacionalizados

Cuando usas la anotación `@get:NotBlank(message = "El nombre es obligatorio")`
en Spring Boot con Kotlin, hardcodear el mensaje directamente en la anotación no
es la mejor práctica si deseas soportar múltiples idiomas. Para implementar la
internacionalización (i18n) de manera eficiente y traducir los mensajes de
validación según el idioma del usuario, la mejor forma es aprovechar el soporte
de internacionalización de Spring Boot mediante archivos de propiedades y claves
de mensajes.

---

#### 1. Configurar Archivos de Mensajes

Spring Boot permite manejar mensajes traducidos a través de archivos de
propiedades ubicados en el directorio `src/main/resources`. Debes crear un
archivo base para el idioma predeterminado (como inglés) y archivos adicionales
para cada idioma que desees soportar. Por ejemplo:

- **`messages.properties`** (idioma predeterminado, inglés):

  ```properties
  validation.name.notblank=Name is required
  ```

- **`messages_es.properties`** (español):

  ```properties
  validation.name.notblank=El nombre es obligatorio
  ```

- **`messages_fr.properties`** (francés):
  ```properties
  validation.name.notblank=Le nom est obligatoire
  ```

Cada archivo contiene claves (como `validation.name.notblank`) asociadas al
mensaje traducido correspondiente al idioma.

---

### 2. Usar Claves en las Anotaciones de Validación

En lugar de escribir el mensaje directamente en la anotación, utiliza la clave
definida en los archivos de propiedades, encerrándola entre llaves `{}`. Esto le
indica a Spring Boot que debe buscar el mensaje en los archivos de mensajes
según el idioma del usuario. Por ejemplo:

```kotlin
data class UserRequest(
    @get:NotBlank(message = "{validation.name.notblank}")
    val name: String
)
```

Aquí, `{validation.name.notblank}` es la clave que Spring Boot resolverá
automáticamente, eligiendo el mensaje adecuado según el idioma del usuario.

---

### 3. Aprovechar la Configuración Automática de MessageSource

Spring Boot configura automáticamente un componente llamado `MessageSource` si
detecta archivos de mensajes en `src/main/resources`. Este componente se encarga
de cargar y resolver los mensajes de los archivos de propiedades según el idioma
(locale) del usuario. No necesitas configurarlo manualmente, ya que Spring Boot
lo hace por defecto.

---

### 4. Determinar el Idioma del Usuario

Spring Boot utiliza un `LocaleResolver` para decidir qué idioma usar. Por
defecto, emplea él `AcceptHeaderLocaleResolver`, que toma el idioma del header
`Accept-Language` enviado en la solicitud HTTP por el cliente. Por ejemplo:

- Si el header es `Accept-Language: es`, se usará `messages_es.properties`.
- Si es `Accept-Language: fr`, se usará `messages_fr.properties`.
- Si no hay header o no hay coincidencia, se usará el archivo base
  `messages.properties`.

Esto asegura que los mensajes se adapten automáticamente al idioma preferido del
usuario.

---

### 5. Manejar Errores de Validación

Cuando una validación falla (por ejemplo, si el campo `name` está vacío), Spring
Boot lanza una excepción `MethodArgumentNotValidException`. Para devolver
mensajes de error traducidos al usuario, puedes usar un `@ControllerAdvice` que
capture esta excepción y resuelva los mensajes con él `MessageSource`. Aquí
tienes un ejemplo:

```kotlin
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalValidationHandler(private val messageSource: MessageSource) {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(ex: MethodArgumentNotValidException): Map<String, Any> {
        val errors = ex.bindingResult.fieldErrors.map {
            val message = messageSource.getMessage(it, LocaleContextHolder.getLocale())
            it.field to message
        }.toMap()
        return mapOf("errors" to errors)
    }
}
```

En este código:

- `MessageSource` resuelve el mensaje de error usando la clave de la anotación y
  el locale actual.
- `LocaleContextHolder.getLocale()` obtiene el idioma del usuario.
- Los errores se devuelven en un formato claro, como un mapa con el nombre del
  campo y el mensaje traducido.

Por ejemplo, si un usuario con idioma español envía un nombre vacío, la
respuesta podría ser:

```json
{
  "errors": {
    "name": "El nombre es obligatorio"
  }
}
```

---

### 6. Ventajas de Este Enfoque

- **Centralización**: Todos los mensajes están en archivos de propiedades,
  fáciles de gestionar y actualizar.
- **Multi-Idioma**: Puedes agregar soporte para nuevos idiomas creando nuevos
  archivos de propiedades sin cambiar el código.
- **Flexibilidad**: Los mensajes se pueden modificar sin recompilar la
  aplicación.
- **Experiencia del Usuario**: Los mensajes se adaptan al idioma del usuario
  automáticamente.

---

## Referencias

- [Validar RequestParams y PathVariables en Spring](https://www.baeldung.com/spring-validate-requestparam-pathvariable)
- [Validar Cuerpo de Solicitud en Spring Boot](https://medium.com/@tericcabrel/validate-request-body-and-parameter-in-spring-boots-53ca77f97fe9)
- [Validación de Cuerpo de Solicitud en Spring Boot con Kotlin](https://stackoverflow.com/questions/49040565/kotlins-and-spring-boots-request-body-validation)
- [Búsqueda Dinámica con Hibernate, MySQL y Spring Boot](https://medium.com/geek-culture/dynamic-search-filter-query-based-on-user-inputs-with-hibernate-mysql-spring-boots-85e842dcf8d)
- [Optimización de Rendimiento con Spring Data JPA](https://medium.com/@avi.singh.iit01/optimizing-performance-with-spring-data-jpa-85583362cf3a)
- [Filtrado Dinámico con Spring Data JPA usando Specifications](https://medium.com/@thomas-a-mathew/spring-data-jpa-dynamic-filtering-using-specifications-f7f12ad27063)
- [Búsqueda Avanzada y Filtrado en Spring](https://milanbrankovic.medium.com/spring-advanced-search-filtering-5ee850f9458c)
- [Validaciones personalizadas con MessageSource en Spring Boot](https://www.baeldung.com/spring-custom-validation-message-source)

# CRUD con TDD

## 1. Estructura esperada para el CRUD con TDD

El flujo natural del CRUD bajo la arquitectura hexagonal, debe ser de la siguiente forma:

```plaintext
domain/
 ├─ model/
 │   └─ Example.kt                    ← Modelo de dominio (no JPA)
 ├─ port/
 │   ├─ input/
 │   │   └─ ExampleUseCase.kt         ← Interfaces de entrada (casos de uso)
 │   └─ output/
 │       └─ ExampleRepositoryPort.kt  ← Interfaces de salida (persistencia)
 └─ service/
     └─ ExampleService.kt             ← Implementación de casos de uso

infrastructure/
 ├─ persistence/
 │   ├─ entity/
 │   │   └─ ExampleEntity.kt          ← Entidad JPA
 │   ├─ repository/
 │   │   └─ ExampleJpaRepository.kt   ← Repository de Spring Data
 │   └─ adapter/
 │       └─ ExampleRepositoryAdapter.kt ← Implementa el puerto de salida
 └─ rest/
     ├─ controller/
     │   └─ ExampleController.kt      ← Endpoints REST
     └─ dto/
         └─ ExampleRequest/Response.kt

test/
 ├─ domain/
 │   └─ service/
 │       └─ ExampleServiceTest.kt
 └─ infrastructure/
     └─ rest/
         └─ ExampleControllerTest.kt
```

## 2. Creamos las pruebas unitarias básicas

Es importante recordar que el TDD debe ser un proceso iterativo y seguir el siguiente flujo:

```plaintext
🔴 Red → 🟢 Green → 🔵 Refactor
```

Paso 1: Crear una prueba unitaria del dominio

Empezamos con la capa del dominio, sin el framework, solo para crear las pruebas unitarias, sin base de datos ni persistencia.

```kotlin
package com.lgzarturo.springbootcourse.domain.service

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.domain.port.output.ExampleRepositoryPort
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class ExampleServiceTest {

    private val repository = mock<ExampleRepositoryPort>()
    private val service = ExampleService(repository)

    @Test
    fun `should create a new example`() {
        val example = Example(id = null, name = "Test", description = "desc")

        whenever(repository.save(any())).thenReturn(example.copy(id = 1))

        val result = service.create(example)

        assertEquals(1, result.id)
        verify(repository).save(any())
    }
}
```

> **Nota:** Aquí usamos Mockito para simular el comportamiento del repositorio, es importante tener las siguientes dependencias en nuestro `build.gradle.kts`:
>
> ```kotlin
> testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
> testImplementation("org.junit.jupiter:junit-jupiter-api")
> testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
> testImplementation("io.kotest:kotest-assertions-core:5.9.1")
> ```

Paso 2: No se puede ejecutar la prueba unitaria.

Debido a que no va a compilar el proyecto, debemos corregirlo, agregando las clases necesarias, para solucionar los errores de compilación. (🔴 Red)

Es necesario crear las siguientes clases:

- `com.lgzarturo.springbootcourse.domain.port.output.ExampleRepositoryPort` -> Interfaz del repositorio de persistencia
- `com.lgzarturo.springbootcourse.domain.model.Example` -> Data Class con el Modelo de dominio
- `com.lgzarturo.springbootcourse.domain.service.ExampleService` -> Clase de servicio para la capa de dominio que implementa el repositorio

Paso 3: Corregimos la prueba unitaria

Ahora podemos corregir la prueba unitaria, agregando las dependencias necesarias para que la prueba se ejecute correctamente, importando las clases necesarias.

Paso 4: Implementamos la lógica mínima para pasar la prueba

Sin embargo, solo con importar las clases no se puede corregir la prueba, es necesario implementar la lógica mínima para que la prueba pase correctamente.

Empezamos con el servicio, ya que en esta línea de código `private val service = ExampleService(repository)` se necesita un constructor, por lo que debemos crearlo.

Si ejecutamos la prueba, veremos que ahora el error se traslada a la línea `val example = Example(id = null, name = "Test", description = "desc")` debido a que no se puede crear un objeto de tipo `Example` sin un `id`, por lo tanto, implementamos las propiedades necesarias.

> En este punto se agregaron las propiedades `id`, `name` y `description` al modelo de dominio, además de que es una clase de datos.

Una vez implementadas las propiedades, si ejecutamos la prueba, veremos que el error se traslada a la línea `whenever(repository.save(any())).thenReturn(example.copy(id = 1))` debido a que no se puede llamar al método `save` del repositorio, por lo tanto, implementamos el método en la interfaz.

Ya que hayamos agregado el método `save` en la interfaz del repositorio, si ejecutamos la prueba, veremos que el error se traslada a la línea `val result = service.create(example)` debido a que no se puede llamar al método `create` del servicio, por lo tanto, implementamos el método en el servicio.

Con esto, ya podemos corregir la prueba unitaria y pasarla.

➡️ Ahora el test del dominio debería pasar (🟢 Green).

Paso 5. Ya tenemos la prueba unitaria pasando, ahora podemos continuar con el TDD.

Continuamos con el TDD, agregando las clases necesarias para que la prueba se ejecute correctamente. Ahora toca pasar con el refactoring para agregar más funcionalidades al CRUD, de momento solo tenemos la funcionalidad de crear un objeto, pero no se persiste en la base de datos. (🔵 Refactor)

Paso 6. Agregamos otra prueba pero ahora de la capa de infraestructura.

- `com.lgzarturo.springbootcourse.infrastructure.rest.controller.ExampleControllerTest` -> Prueba unitaria de la capa de infraestructura

Ahora al definir una nueva clase, hay nuevas dependencias que se necesitan agregar, por lo tanto, debemos agregarlas:

```kotlin
@WebMvcTest(ExampleController::class)
class ExampleControllerTest(
    @Autowired val mockMvc: MockMvc,
    @MockBean val service: ExampleUseCase
) {

    @Test
    fun `should return 201 when creating example`() {
        val request = ExampleRequest("Test", "Desc")
        val response = ExampleResponse(1, "Test", "Desc")

        whenever(service.create(any())).thenReturn(Example(1, "Test", "Desc"))

        mockMvc.perform(
            post("/api/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Test","description":"Desc"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test"))
    }
}
```

En este caso, debemos agregar las siguientes clases:

- ExampleController -> Clase de controlador de la capa de infraestructura
- ExampleUseCase -> Interfaz de entrada de la capa de dominio
- ExampleRequest -> Clase DTO de la capa de infraestructura
- ExampleResponse -> Clase DTO de la capa de infraestructura

> Las clases que ya existen no necesitan ser creadas nuevamente, solo las que no existen.

Una vez que se resuelvan las dependencias, se puede corregir la prueba unitaria, pero esta no va a pasar, debido a que no hay una implementación de la interfaz de entrada que es `ExampleUseCase`, por lo tanto, es necesario crear una implementación mínima para que la prueba pase. (🔴 Red)

Paso 7. Implementar lo mínimo para pasar la prueba

Se agrega el servicio con la implementación mínima para que la prueba pase. Para ello se define el adaptador siguiente la estructura esperada de la arquitectura hexagonal:

> Ojo: El adaptador debe implementar la interfaz de salida del repositorio. Por lo tanto, debe implementar `ExampleRepositoryPort`, y requiere una clase de dependencia la cual es `ExampleJpaRepository`.

- ExampleRepositoryAdapter ← Implementa el puerto de salida

Paso 8. Ya tenemos la prueba unitaria pasando, ahora podemos continuar con el TDD.

Con estos pasos, ya tenemos la acción de crear un objeto funcionando, ahora podemos continuar con el TDD para agregar las demás funcionalidades del CRUD (Leer, Actualizar, Eliminar). (🟢 Green)
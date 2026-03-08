package com.lgzarturo.springbootcourse.example.adapters.rest

import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExampleRequest
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.response.ExampleResponse
import com.lgzarturo.springbootcourse.example.application.ports.input.ExampleUseCasePort
import com.lgzarturo.springbootcourse.example.domain.Example
import com.lgzarturo.springbootcourse.shared.domain.PageRequest
import com.lgzarturo.springbootcourse.shared.domain.PageResult
import com.lgzarturo.springbootcourse.shared.domain.SortOrder
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador REST para operaciones sobre ejemplos de TDD
 * Punto de entrada HTTP para operaciones sobre ejemplos de TDD
 */
@RestController
@RequestMapping("/api/v1/examples")
@Tag(name = "Example", description = "Endpoints de prueba para ejemplos de TDD")
@Validated
class ExampleController(
    private val service: ExampleUseCasePort,
) {
    /**
     * Endpoint para crear un nuevo ejemplo
     * POST /api/v1/examples
     *
     * @return ExampleResponse con el ejemplo creado
     */
    @PostMapping
    @Operation(
        summary = "Crear un nuevo ejemplo",
        description = "Crea un nuevo ejemplo en la base de datos",
    )
    fun create(
        @Valid @RequestBody request: ExampleRequest,
    ): ResponseEntity<ExampleResponse> {
        val example = service.create(request.toDomain())
        return ResponseEntity.status(HttpStatus.CREATED).body(ExampleResponse.fromDomain(example))
    }

    /**
     * Endpoint para obtener un ejemplo por su ID
     * GET /api/v1/examples/{id}
     *
     * @param id ID del ejemplo a obtener
     * @return ExampleResponse con el ejemplo obtenido
     */
    @Operation(
        summary = "Obtener un ejemplo por su ID",
        description = "Obtiene un ejemplo por su ID en la base de datos",
    )
    @GetMapping("/{id}")
    fun getById(
        @PathVariable
        @Min(value = 1, message = "El ID debe ser mayor que 0")
        id: Long,
    ): ResponseEntity<ExampleResponse> {
        val example = service.findById(id)
        return ResponseEntity.ok(ExampleResponse.fromDomain(example))
    }

    /**
     * Endpoint para obtener todos los ejemplos
     * GET /api/v1/examples
     *
     * @param searchText Texto de búsqueda opcional para filtrar ejemplos
     * @param pageable Información de paginación
     * @return Página de Example con los ejemplos obtenidos
     */
    @Operation(
        summary = "Obtener todos los ejemplos",
        description = "Obtiene todos los ejemplos en la base de datos",
    )
    @GetMapping
    fun getAll(
        @RequestParam(required = false) searchText: String?,
        pageable: Pageable,
    ): ResponseEntity<PageResult<Example>> {
        val sortOrders =
            pageable.sort
                .stream()
                .map { order ->
                    SortOrder(
                        property = order.property,
                        direction = if (order.isAscending) SortOrder.Direction.ASC else SortOrder.Direction.DESC,
                    )
                }.toList()

        val domainPageRequest =
            PageRequest(
                page = pageable.pageNumber,
                size = pageable.pageSize,
                sort = sortOrders,
            )

        val examples = service.findAll(searchText, domainPageRequest)
        return ResponseEntity.ok(examples)
    }

    /**
     * Endpoint para actualizar un ejemplo
     * PUT /api/v1/examples/{id}
     *
     * @param id ID del ejemplo a actualizar
     * @param exampleRequest Datos del ejemplo a actualizar
     * @return ExampleResponse con el ejemplo actualizado
     */
    @Operation(
        summary = "Actualizar un ejemplo",
        description = "Actualiza un ejemplo en la base de datos",
    )
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody exampleRequest: ExampleRequest,
    ): ResponseEntity<ExampleResponse> {
        val example = exampleRequest.toDomain()
        val exampleUpdated = service.update(id, example)
        return ResponseEntity.ok(ExampleResponse.fromDomain(exampleUpdated))
    }

    /**
     * Endpoint para eliminar un ejemplo
     * DELETE /api/v1/examples/{id}
     *
     * @param id ID del ejemplo a eliminar
     */
    @Operation(
        summary = "Eliminar un ejemplo",
        description = "Elimina un ejemplo en la base de datos",
    )
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable
        @Min(value = 1, message = "El ID debe ser mayor que 0")
        id: Long,
    ): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    /**
     * Endpoint para actualizar parcialmente un ejemplo
     * PATCH /api/v1/examples/{id}
     *
     * @param id ID del ejemplo a actualizar
     * @param update Datos del ejemplo a actualizar
     * @return ExampleResponse con el ejemplo actualizado
     */
    @Operation(
        summary = "Actualizar parcialmente un ejemplo",
        description = "Actualiza parcialmente un ejemplo en la base de datos",
    )
    @PatchMapping("/{id}")
    fun patch(
        @PathVariable id: Long,
        @Valid @RequestBody update: ExamplePatchUpdate,
    ): ResponseEntity<ExampleResponse> {
        try {
            val exampleUpdated = service.patch(id, update)
            return ResponseEntity.ok(ExampleResponse.fromDomain(exampleUpdated))
        } catch (_: Exception) {
            return ResponseEntity.notFound().build()
        }
    }
}

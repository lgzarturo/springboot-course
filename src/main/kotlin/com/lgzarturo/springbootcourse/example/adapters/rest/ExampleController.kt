package com.lgzarturo.springbootcourse.example.adapters.rest

import com.lgzarturo.springbootcourse.example.domain.Example
import com.lgzarturo.springbootcourse.example.application.ports.input.ExampleUseCase
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExampleRequest
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.response.ExampleResponse
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

@RestController
@RequestMapping("/api/v1/examples")
@Tag(name = "Example", description = "Endpoints de prueba para ejemplos de TDD")
@Validated
class ExampleController(
    private val service: ExampleUseCase,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: ExampleRequest,
    ): ResponseEntity<ExampleResponse> {
        val example = service.create(request.toDomain())
        return ResponseEntity.status(HttpStatus.CREATED).body(ExampleResponse.Companion.fromDomain(example))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable
        @Min(value = 1, message = "El ID debe ser mayor que 0")
        id: Long,
    ): ResponseEntity<ExampleResponse> {
        val example = service.findById(id)
        return ResponseEntity.ok(ExampleResponse.Companion.fromDomain(example))
    }

    @GetMapping
    fun getAll(
        @RequestParam(required = false) searchText: String?,
        pageable: Pageable,
    ): ResponseEntity<Page<Example>> {
        val examples = service.findAll(searchText, pageable)
        return ResponseEntity.ok(examples)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody exampleRequest: ExampleRequest,
    ): ResponseEntity<ExampleResponse> {
        val example = exampleRequest.toDomain()
        val exampleUpdated = service.update(id, example)
        return ResponseEntity.ok(ExampleResponse.Companion.fromDomain(exampleUpdated))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable
        @Min(value = 1, message = "El ID debe ser mayor que 0")
        id: Long,
    ): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}")
    fun patch(
        @PathVariable id: Long,
        @Valid @RequestBody update: ExamplePatchUpdate,
    ): ResponseEntity<ExampleResponse> {
        try {
            val exampleUpdated = service.patch(id, update)
            return ResponseEntity.ok(ExampleResponse.Companion.fromDomain(exampleUpdated))
        } catch (_: Exception) {
            return ResponseEntity.notFound().build()
        }
    }
}

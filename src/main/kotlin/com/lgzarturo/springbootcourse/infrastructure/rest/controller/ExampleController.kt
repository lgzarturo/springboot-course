package com.lgzarturo.springbootcourse.infrastructure.rest.controller

import com.lgzarturo.springbootcourse.domain.port.input.ExampleUseCase
import com.lgzarturo.springbootcourse.infrastructure.rest.dto.request.ExampleRequest
import com.lgzarturo.springbootcourse.infrastructure.rest.dto.response.ExampleResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/examples")
@Tag(name = "Example", description = "Endpoints de prueba para ejemplos de TDD")
class ExampleController(
    private val service: ExampleUseCase,
) {
    @PostMapping
    fun create(@RequestBody request: ExampleRequest): ResponseEntity<ExampleResponse> {
        val example = service.create(request.toDomain())
        return ResponseEntity.status(HttpStatus.CREATED).body(ExampleResponse.fromDomain(example))
    }
}

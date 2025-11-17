package com.lgzarturo.springbootcourse.ping.adapters.rest

import com.lgzarturo.springbootcourse.ping.adapters.rest.dto.PingResponse
import com.lgzarturo.springbootcourse.ping.adapters.rest.mapper.PingMapper
import com.lgzarturo.springbootcourse.ping.application.ports.input.PingUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador REST para operaciones de Ping
 * Punto de entrada HTTP para verificar el estado de la API
 */
@RestController
@RequestMapping("/api/v1/ping")
@Tag(name = "Ping", description = "Endpoints para verificar el estado de la API")
class PingController(
    private val pingUseCase: PingUseCase,
    private val pingMapper: PingMapper,
) {
    /**
     * Endpoint simple de ping
     * GET /api/v1/ping
     *
     * @return PingResponse con mensaje "pong"
     */
    @GetMapping
    @Operation(
        summary = "Ping simple",
        description = "Retorna un pong simple para verificar que la API está funcionando",
    )
    fun ping(): ResponseEntity<PingResponse> {
        val ping = pingUseCase.getPing()
        val response = pingMapper.toResponse(ping)
        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint de ping con mensaje personalizado
     * GET /api/v1/ping/{message}
     *
     * @param message Mensaje personalizado
     * @return PingResponse con el mensaje personalizado
     */
    @GetMapping("/{message}")
    @Operation(
        summary = "Ping con mensaje personalizado",
        description = "Retorna un pong con un mensaje personalizado",
    )
    fun pingWithMessage(
        @PathVariable message: String,
    ): ResponseEntity<PingResponse> {
        val ping = pingUseCase.getPingWithMessage(message)
        val response = pingMapper.toResponse(ping)
        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint de health check
     * GET /api/v1/ping/health
     *
     * @return Status 200 OK si la API está funcionando
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Verifica el estado de salud de la API",
    )
    fun health(): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.OK).body(
            mapOf(
                "status" to "UP",
                "service" to "springboot-course",
            ),
        )
}

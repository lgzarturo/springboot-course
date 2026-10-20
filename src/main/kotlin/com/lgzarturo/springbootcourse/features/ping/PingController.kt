package com.lgzarturo.springbootcourse.features.ping

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import com.lgzarturo.springbootcourse.features.ping.dto.PingResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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
    private val pingUseCasePort: PingUseCasePort,
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
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Pong con mensaje y versión",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = PingResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    fun ping(): ResponseEntity<PingResponse> {
        val ping = pingUseCasePort.getPing()
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
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Pong con el mensaje personalizado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = PingResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    fun pingWithMessage(
        @Parameter(description = "Mensaje personalizado", example = "hello")
        @PathVariable message: String,
    ): ResponseEntity<PingResponse> {
        val ping = pingUseCasePort.getPingWithMessage(message)
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
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Estado de salud con status y servicio",
            content = [Content(mediaType = "application/json", schema = Schema(type = "object"))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    fun health(): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.OK).body(
            mapOf(
                "status" to "UP",
                "service" to "springboot-course",
            ),
        )
}

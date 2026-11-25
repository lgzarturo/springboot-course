package com.lgzarturo.springbootcourse.features.reservations

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import com.lgzarturo.springbootcourse.features.reservations.dto.CreateReservationRequest
import com.lgzarturo.springbootcourse.features.reservations.dto.PageResponse
import com.lgzarturo.springbootcourse.features.reservations.dto.ReservationResponse
import com.lgzarturo.springbootcourse.features.reservations.dto.UpdateReservationStatusRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "Reservations", description = "Endpoints de gestión de reservas")
@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
    private val reservationService: ReservationService,
) {
    @Operation(
        summary = "Crear una reserva",
        description = "Crea una reserva PENDING con el total calculado. Rechaza solapes y rangos inválidos.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Reserva creada",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReservationResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Datos inválidos, rango inválido o habitación inexistente",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "Solape con otra reserva de la habitación",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateReservationRequest,
    ): ResponseEntity<ReservationResponse> {
        val reservation =
            Reservation(
                id = "",
                roomId = request.roomId,
                hotelId = "",
                userId = requireNotNull(request.userId) { "El usuario no puede estar vacío" },
                guestName = request.guestName,
                checkIn = requireNotNull(request.checkIn) { "La fecha de check-in no puede estar vacía" },
                checkOut = requireNotNull(request.checkOut) { "La fecha de check-out no puede estar vacía" },
                totalPrice = 0.0,
                status = ReservationStatus.PENDING,
            )
        val saved = reservationService.createReservation(reservation)
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.fromDomain(saved))
    }

    @Operation(
        summary = "Obtener una reserva por ID",
        description = "Retorna la reserva indicada. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Reserva encontrada",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReservationResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Reserva no encontrada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping("/{id}")
    fun getById(
        @Parameter(description = "Identificador de la reserva", example = "res-1")
        @PathVariable id: String,
    ): ResponseEntity<ReservationResponse> {
        val reservation = reservationService.getReservationById(id)
        return reservation
            ?.let { ResponseEntity.ok(ReservationResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "Listar reservas",
        description = "Retorna reservas paginadas con filtros por usuario, estado y fechas. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Página de reservas",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = PageResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping
    fun getAll(
        @Parameter(description = "Filtro por usuario", example = "1")
        @RequestParam(required = false) userId: Long?,
        @Parameter(description = "Filtro por estado", example = "PENDING")
        @RequestParam(required = false) status: ReservationStatus?,
        @Parameter(description = "Fecha mínima (incluye reservas que terminan después)", example = "2026-11-01")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @Parameter(description = "Fecha máxima (incluye reservas que empiezan antes)", example = "2026-11-30")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
        @Parameter(description = "Número de página (base 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Tamaño de página", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<PageResponse<ReservationResponse>> {
        val criteria = ReservationSearchCriteria(userId = userId, status = status, from = from, to = to)
        val (reservations, total) = reservationService.getAllReservations(criteria, page, size)
        val responses = reservations.map { ReservationResponse.fromDomain(it) }
        return ResponseEntity.ok(PageResponse.from(responses, total, page, size))
    }

    @Operation(
        summary = "Cambiar el estado de una reserva",
        description = "Aplica una transición válida y sincroniza la habitación. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Estado actualizado",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReservationResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Datos inválidos",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Reserva no encontrada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "Transición de estado inválida",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PatchMapping("/{id}/status")
    fun updateStatus(
        @Parameter(description = "Identificador de la reserva", example = "res-1")
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateReservationStatusRequest,
    ): ResponseEntity<ReservationResponse> {
        val target = requireNotNull(request.status) { "El estado no puede estar vacío" }
        val updated = reservationService.updateStatus(id, target)
        return updated
            ?.let { ResponseEntity.ok(ReservationResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "La solicitud no es válida",
                path = request.requestURI,
            )
        return ResponseEntity.badRequest().body(error)
    }
}

package com.lgzarturo.springbootcourse.features.payments

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import com.lgzarturo.springbootcourse.features.payments.dto.CreatePaymentRequest
import com.lgzarturo.springbootcourse.features.payments.dto.PageResponse
import com.lgzarturo.springbootcourse.features.payments.dto.PaymentResponse
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Payments", description = "Endpoints de gestión de pagos")
@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @Operation(
        summary = "Crear un pago",
        description = "Cobra una reserva CONFIRMED con el gateway simulado. El importe es el total de la reserva.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Pago creado",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PaymentResponse::class),
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
            description = "Reserva no confirmada o pago duplicado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreatePaymentRequest,
    ): ResponseEntity<PaymentResponse> {
        val payment =
            paymentService.createPayment(
                reservationId = request.reservationId,
                method = request.method.ifBlank { "SIMULATED" },
                simulateFailure = request.simulateFailure,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.fromDomain(payment))
    }

    @Operation(
        summary = "Obtener un pago por ID",
        description = "Retorna el pago indicado. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Pago encontrado",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PaymentResponse::class),
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
            description = "Pago no encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping("/{id}")
    fun getById(
        @Parameter(description = "Identificador del pago", example = "pay-1")
        @PathVariable id: String,
    ): ResponseEntity<PaymentResponse> {
        val payment = paymentService.getPaymentById(id)
        return payment
            ?.let { ResponseEntity.ok(PaymentResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "Listar pagos",
        description = "Retorna pagos paginados con filtros por reserva y estado. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Página de pagos",
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
        @Parameter(description = "Filtro por reserva", example = "res-1")
        @RequestParam(required = false) reservationId: String?,
        @Parameter(description = "Filtro por estado", example = "COMPLETED")
        @RequestParam(required = false) status: PaymentStatus?,
        @Parameter(description = "Número de página (base 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Tamaño de página", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<PageResponse<PaymentResponse>> {
        val criteria = PaymentSearchCriteria(reservationId = reservationId, status = status)
        val (payments, total) = paymentService.getAllPayments(criteria, page, size)
        val responses = payments.map { PaymentResponse.fromDomain(it) }
        return ResponseEntity.ok(PageResponse.from(responses, total, page, size))
    }

    @Operation(
        summary = "Reembolsar un pago",
        description = "Reembolsa un pago COMPLETED. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Pago reembolsado",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PaymentResponse::class),
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
            description = "Pago no encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "El pago no está completado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping("/{id}/refund")
    fun refund(
        @Parameter(description = "Identificador del pago", example = "pay-1")
        @PathVariable id: String,
    ): ResponseEntity<PaymentResponse> {
        val refunded = paymentService.refundPayment(id)
        return refunded
            ?.let { ResponseEntity.ok(PaymentResponse.fromDomain(it)) }
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

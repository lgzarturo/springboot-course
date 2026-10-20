package com.lgzarturo.springbootcourse.features.reviews

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import com.lgzarturo.springbootcourse.features.reviews.dto.CreateReviewRequest
import com.lgzarturo.springbootcourse.features.reviews.dto.HotelReviewsResponse
import com.lgzarturo.springbootcourse.features.reviews.dto.ReviewResponse
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
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Reviews", description = "Endpoints de reseñas de estancias")
@RestController
class ReviewController(
    private val reviewService: ReviewService,
) {
    @Operation(
        summary = "Crear una reseña",
        description = "Crea la reseña de una reserva COMPLETED. Solo el propietario autenticado puede opinar.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Reseña creada",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReviewResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o valoración fuera de 1-5",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "403",
            description = "La reserva pertenece a otro usuario",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Reserva no encontrada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "Reserva no completada o reseña duplicada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping("/api/v1/reviews")
    fun create(
        @Valid @RequestBody request: CreateReviewRequest,
    ): ResponseEntity<ReviewResponse> {
        val email =
            SecurityContextHolder.getContext().authentication?.name
                ?: throw AccessDeniedException("Se requiere autenticación para opinar")
        val review =
            reviewService.createReview(
                reservationId = request.reservationId,
                rating = request.rating,
                comment = request.comment,
                authenticatedEmail = email,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.fromDomain(review))
    }

    @Operation(
        summary = "Obtener una reseña por ID",
        description = "Retorna la reseña indicada. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Reseña encontrada",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReviewResponse::class),
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
            description = "Reseña no encontrada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping("/api/v1/reviews/{id}")
    fun getById(
        @Parameter(description = "Identificador de la reseña", example = "rev-1")
        @PathVariable id: String,
    ): ResponseEntity<ReviewResponse> {
        val review = reviewService.getReviewById(id)
        return review
            ?.let { ResponseEntity.ok(ReviewResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "Listar reseñas de un hotel",
        description = "Retorna reseñas paginadas con la valoración media del hotel. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Resumen paginado con promedio",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = HotelReviewsResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping("/api/v1/hotels/{hotelId}/reviews")
    fun getHotelReviews(
        @Parameter(description = "Identificador del hotel", example = "hotel-1")
        @PathVariable hotelId: String,
        @Parameter(description = "Número de página (base 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Tamaño de página", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<HotelReviewsResponse> {
        val (reviews, total, average) = reviewService.getHotelReviews(hotelId, page, size)
        val responses = reviews.map { ReviewResponse.fromDomain(it) }
        return ResponseEntity.ok(HotelReviewsResponse.from(responses, total, page, size, average))
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

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                error = "Forbidden",
                message = ex.message ?: "No tiene permiso para acceder a este recurso",
                path = request.requestURI,
            )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error)
    }
}

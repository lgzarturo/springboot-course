package com.lgzarturo.springbootcourse.features.hotels

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import com.lgzarturo.springbootcourse.features.hotels.dto.CreateHotelRequest
import com.lgzarturo.springbootcourse.features.hotels.dto.HotelResponse
import com.lgzarturo.springbootcourse.features.hotels.dto.PageResponse
import com.lgzarturo.springbootcourse.features.hotels.dto.UpdateHotelRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Hotels", description = "Endpoints de gestión de hoteles")
@RestController
@RequestMapping("/api/v1/hotels")
class HotelController(
    private val hotelService: HotelService,
) {
    @Operation(
        summary = "Crear un hotel",
        description = "Crea un hotel y retorna su representación. Requiere permiso de gestión de hoteles.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Hotel creado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = HotelResponse::class))],
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
            responseCode = "403",
            description = "Sin permiso de gestión de hoteles",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateHotelRequest,
    ): ResponseEntity<HotelResponse> {
        val hotel = Hotel(id = "", name = request.name, address = request.address)
        val savedHotel = hotelService.createHotel(hotel)
        return ResponseEntity.status(HttpStatus.CREATED).body(HotelResponse.fromDomain(savedHotel))
    }

    @Operation(
        summary = "Obtener un hotel por ID",
        description = "Retorna el hotel indicado. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Hotel encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = HotelResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Hotel no encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping("/{id}")
    fun getById(
        @Parameter(description = "Identificador del hotel", example = "1")
        @PathVariable id: String,
    ): ResponseEntity<HotelResponse> {
        val hotel = hotelService.getHotelById(id)
        return hotel
            ?.let { ResponseEntity.ok(HotelResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "Listar hoteles",
        description = "Retorna hoteles paginados con filtro opcional por nombre. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Página de hoteles",
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
        @Parameter(description = "Filtro opcional por nombre", example = "Pikachu")
        @RequestParam(required = false) name: String?,
        @Parameter(description = "Número de página (base 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Tamaño de página", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<PageResponse<HotelResponse>> {
        val criteria = HotelSearchCriteria(name = name)
        val (hotels, total) = hotelService.getAllHotels(criteria, page, size)
        val hotelResponses = hotels.map { HotelResponse.fromDomain(it) }
        val pageResponse = PageResponse.from(hotelResponses, total, page, size)
        return ResponseEntity.ok(pageResponse)
    }

    @Operation(
        summary = "Actualizar un hotel",
        description = "Actualiza el hotel indicado. Requiere permiso de gestión de hoteles.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Hotel actualizado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = HotelResponse::class))],
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
            responseCode = "403",
            description = "Sin permiso de gestión de hoteles",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Hotel no encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "Identificador del hotel", example = "1")
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateHotelRequest,
    ): ResponseEntity<HotelResponse> {
        val hotel = Hotel(id = id, name = request.name, address = request.address)
        val updatedHotel = hotelService.updateHotel(id, hotel)
        return updatedHotel
            ?.let { ResponseEntity.ok(HotelResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "Eliminar un hotel",
        description = "Elimina el hotel indicado. Requiere permiso de gestión de hoteles.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Hotel eliminado"),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "403",
            description = "Sin permiso de gestión de hoteles",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Hotel no encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @Parameter(description = "Identificador del hotel", example = "1")
        @PathVariable id: String,
    ): ResponseEntity<Void> {
        val deleted = hotelService.deleteHotel(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

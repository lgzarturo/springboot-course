package com.lgzarturo.springbootcourse.features.rooms

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import com.lgzarturo.springbootcourse.features.rooms.dto.CreateRoomRequest
import com.lgzarturo.springbootcourse.features.rooms.dto.PageResponse
import com.lgzarturo.springbootcourse.features.rooms.dto.RoomResponse
import com.lgzarturo.springbootcourse.features.rooms.dto.UpdateRoomRequest
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Rooms", description = "Endpoints de gestión de habitaciones")
@RestController
@RequestMapping("/api/v1/hotels/{hotelId}/rooms")
class RoomController(
    private val roomService: RoomService,
) {
    @Operation(
        summary = "Crear una habitación",
        description = "Crea una habitación bajo el hotel indicado. Requiere permiso de gestión de hoteles.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Habitación creada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = RoomResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o hotel inexistente",
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
        @Parameter(description = "Identificador del hotel", example = "1")
        @PathVariable hotelId: String,
        @Valid @RequestBody request: CreateRoomRequest,
    ): ResponseEntity<RoomResponse> {
        val room =
            Room(
                id = "",
                number = request.number,
                type = request.type,
                price = request.price,
                hotelId = hotelId,
                status = request.status,
                theme = request.theme,
                capacity = request.capacity,
                description = request.description,
            )
        val saved = roomService.createRoom(hotelId, room)
        return ResponseEntity.status(HttpStatus.CREATED).body(RoomResponse.fromDomain(saved))
    }

    @Operation(
        summary = "Obtener una habitación por ID",
        description = "Retorna la habitación indicada del hotel. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Habitación encontrada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = RoomResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Habitación no encontrada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping("/{roomId}")
    fun getById(
        @Parameter(description = "Identificador del hotel", example = "1")
        @PathVariable hotelId: String,
        @Parameter(description = "Identificador de la habitación", example = "1")
        @PathVariable roomId: String,
    ): ResponseEntity<RoomResponse> {
        val room = roomService.getRoomById(hotelId, roomId)
        return room
            ?.let { ResponseEntity.ok(RoomResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "Listar habitaciones",
        description = "Retorna habitaciones paginadas con filtros por estado y tema. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Página de habitaciones",
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
        @Parameter(description = "Identificador del hotel", example = "1")
        @PathVariable hotelId: String,
        @Parameter(description = "Filtro por estado", example = "AVAILABLE")
        @RequestParam(required = false) status: RoomStatus?,
        @Parameter(description = "Filtro por tema Pokémon", example = "PIKACHU")
        @RequestParam(required = false) theme: PokemonTheme?,
        @Parameter(description = "Número de página (base 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Tamaño de página", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<PageResponse<RoomResponse>> {
        val criteria = RoomSearchCriteria(status = status, theme = theme)
        val (rooms, total) = roomService.getAllRooms(hotelId, criteria, page, size)
        val responses = rooms.map { RoomResponse.fromDomain(it) }
        return ResponseEntity.ok(PageResponse.from(responses, total, page, size))
    }

    @Operation(
        summary = "Actualizar una habitación",
        description = "Actualiza la habitación indicada. Requiere permiso de gestión de hoteles.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Habitación actualizada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = RoomResponse::class))],
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
            description = "Habitación no encontrada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PutMapping("/{roomId}")
    fun update(
        @Parameter(description = "Identificador del hotel", example = "1")
        @PathVariable hotelId: String,
        @Parameter(description = "Identificador de la habitación", example = "1")
        @PathVariable roomId: String,
        @Valid @RequestBody request: UpdateRoomRequest,
    ): ResponseEntity<RoomResponse> {
        val room =
            Room(
                id = roomId,
                number = request.number,
                type = request.type,
                price = request.price,
                hotelId = hotelId,
                status = request.status,
                theme = request.theme,
                capacity = request.capacity,
                description = request.description,
            )
        val updated = roomService.updateRoom(hotelId, roomId, room)
        return updated
            ?.let { ResponseEntity.ok(RoomResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "Eliminar una habitación",
        description = "Elimina la habitación indicada. Requiere permiso de gestión de hoteles.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Habitación eliminada"),
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
            description = "Habitación no encontrada",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @Parameter(description = "Identificador del hotel", example = "1")
        @PathVariable hotelId: String,
        @Parameter(description = "Identificador de la habitación", example = "1")
        @PathVariable roomId: String,
    ): ResponseEntity<Void> {
        val deleted = roomService.deleteRoom(hotelId, roomId)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
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

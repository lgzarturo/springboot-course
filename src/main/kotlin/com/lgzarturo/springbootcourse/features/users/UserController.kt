package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import com.lgzarturo.springbootcourse.features.users.dto.CreateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UpdateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Users", description = "Endpoints de gestión de usuarios")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val createUserService: CreateUserService,
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val userMapper: UserMapper,
) {
    @Operation(
        summary = "Registrar un usuario",
        description = "Crea un usuario con rol GUEST y retorna su representación pública. Endpoint público.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Usuario creado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Datos inválidos",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "El correo ya está registrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateUserRequest,
    ): ResponseEntity<UserResponse> {
        val user =
            createUserService.execute(
                CreateUserCommand(
                    email = request.email,
                    password = request.password,
                    firstName = request.firstName,
                    lastName = request.lastName,
                    phoneNumber = request.phoneNumber,
                    role = UserRole.GUEST,
                ),
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user))
    }

    @Operation(
        summary = "Obtener un usuario por ID",
        description = "Retorna la representación pública del usuario indicado. Requiere autenticación.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    @GetMapping("/{id}")
    fun getById(
        @Parameter(description = "Identificador del usuario", example = "1")
        @PathVariable id: Long,
    ): UserResponse = userMapper.toResponse(getUserUseCase.execute(id))

    @Operation(
        summary = "Listar usuarios",
        description = "Retorna todos los usuarios registrados. Solo administradores.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios",
            content = [
                Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = UserResponse::class)),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "403",
            description = "Sin permiso de lectura de usuarios",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping
    fun getAll(): List<UserResponse> = getUserUseCase.executeAll().map(userMapper::toResponse)

    @Operation(
        summary = "Actualizar un usuario",
        description = "Actualiza el perfil del usuario indicado. Solo administradores.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Usuario actualizado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = UserResponse::class))],
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
            description = "Sin permiso de escritura de usuarios",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "Identificador del usuario", example = "1")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest,
    ): UserResponse =
        userMapper.toResponse(
            updateUserUseCase.execute(
                id,
                UpdateUserCommand(
                    firstName = request.firstName,
                    lastName = request.lastName,
                    phoneNumber = request.phoneNumber,
                ),
            ),
        )

    @Operation(
        summary = "Desactivar un usuario",
        description = "Desactiva el usuario indicado. Solo administradores.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Usuario desactivado"),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "403",
            description = "Sin permiso de escritura de usuarios",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @DeleteMapping("/{id}")
    fun deactivate(
        @Parameter(description = "Identificador del usuario", example = "1")
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        updateUserUseCase.deactivate(id)
        return ResponseEntity.noContent().build()
    }
}

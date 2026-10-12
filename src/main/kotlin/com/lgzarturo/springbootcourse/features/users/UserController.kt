package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.dto.CreateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UpdateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UserResponse
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val createUserService: CreateUserService,
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val userMapper: UserMapper,
) {
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

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
    ): UserResponse = userMapper.toResponse(getUserUseCase.execute(id))

    @GetMapping
    fun getAll(): List<UserResponse> = getUserUseCase.executeAll().map(userMapper::toResponse)

    @PutMapping("/{id}")
    fun update(
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

    @DeleteMapping("/{id}")
    fun deactivate(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        updateUserUseCase.deactivate(id)
        return ResponseEntity.noContent().build()
    }
}

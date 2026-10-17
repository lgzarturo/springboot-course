package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.dto.CreateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UpdateUserRequest
import com.lgzarturo.springbootcourse.features.users.exceptions.DuplicateEmailException
import com.lgzarturo.springbootcourse.security.SecurityTestConfig
import com.lgzarturo.springbootcourse.security.WithMockUser
import com.lgzarturo.springbootcourse.shared.config.ObjectMapperConfig
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import tools.jackson.databind.ObjectMapper

/**
 * Test de integración para UserController
 * Verifica el comportamiento de los endpoints REST
 */
@WebMvcTest(UserController::class)
@Import(SecurityTestConfig::class, ObjectMapperConfig::class)
@DisplayName("UserController Integration Tests")
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var createUserService: CreateUserService

    @MockkBean
    private lateinit var getUserUseCase: GetUserUseCase

    @MockkBean
    private lateinit var updateUserUseCase: UpdateUserUseCase

    @MockkBean
    private lateinit var userMapper: UserMapper

    @Test
    @DisplayName("GET /api/v1/users/me debe retornar 200 OK con el perfil del usuario")
    @WithMockUser(userId = "1")
    fun `should get current user profile`() {
        // GIVEN
        val user = mockUser()
        val response = mockUserResponse()

        every { getUserUseCase.execute(1L) } returns user
        every { userMapper.toResponse(user) } returns response

        // WHEN & THEN
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/v1/users/1"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("ash@pokemon.com"))
    }

    @Test
    @DisplayName("PUT /api/v1/users/me debe actualizar el perfil del usuario")
    @WithMockUser(userId = "1")
    fun `should update profile successfully`() {
        // GIVEN
        val request =
            UpdateUserRequest(
                firstName = "Red",
                lastName = "Trainer",
                phoneNumber = "+14155552672",
            )
        val updatedUser = mockUser()
        val response = mockUserResponse()

        every { updateUserUseCase.execute(any(), any()) } returns updatedUser
        every { userMapper.toResponse(updatedUser) } returns response

        // WHEN & THEN
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @DisplayName("POST /api/v1/users debe crear un usuario sin exponer la contraseña")
    fun `should create a user without exposing password`() {
        val request =
            CreateUserRequest(
                email = "ash@pokemon.com",
                password = "Pikachu123!",
                firstName = "Ash",
                lastName = "Ketchum",
                phoneNumber = "+14155552671",
            )
        val user = mockUser()
        val response = mockUserResponse()

        every { createUserService.execute(any()) } returns user
        every { userMapper.toResponse(user) } returns response

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.password").doesNotExist())
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} debe retornar 404 para un usuario inexistente")
    fun `should return not found for an unknown user`() {
        every { getUserUseCase.execute(99L) } throws NoSuchElementException("User 99 not found")

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/v1/users/99"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("POST /api/v1/users debe retornar 409 para un correo duplicado")
    fun `should return conflict for a duplicate email`() {
        val request =
            CreateUserRequest(
                email = "ash@pokemon.com",
                password = "Pikachu123!",
                firstName = "Ash",
                lastName = "Ketchum",
            )
        every { createUserService.execute(any()) } throws DuplicateEmailException(request.email)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(MockMvcResultMatchers.status().isConflict)
    }
}

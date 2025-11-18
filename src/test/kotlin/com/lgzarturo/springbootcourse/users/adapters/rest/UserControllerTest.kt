package com.lgzarturo.springbootcourse.users.adapters.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.lgzarturo.springbootcourse.security.SecurityTestConfig
import com.lgzarturo.springbootcourse.security.WithMockUser
import com.lgzarturo.springbootcourse.shared.config.ObjectMapperConfig
import com.lgzarturo.springbootcourse.users.adapters.rest.dto.request.UpdateUserRequest
import com.lgzarturo.springbootcourse.users.adapters.rest.mapper.UserMapper
import com.lgzarturo.springbootcourse.users.application.ports.input.GetUserUseCase
import com.lgzarturo.springbootcourse.users.application.ports.input.UpdateUserUseCase
import com.lgzarturo.springbootcourse.users.domain.mockUser
import com.lgzarturo.springbootcourse.users.domain.mockUserResponse
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

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

    @MockBean
    private lateinit var getUserUseCase: GetUserUseCase

    @MockBean
    private lateinit var updateUserUseCase: UpdateUserUseCase

    @MockBean
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
            .perform(MockMvcRequestBuilders.get("/api/v1/users/me"))
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

        every { updateUserUseCase.execute(any()) } returns updatedUser
        every { userMapper.toResponse(updatedUser) } returns response

        // WHEN & THEN
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/v1/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}

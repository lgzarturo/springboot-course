package com.lgzarturo.springbootcourse.features.users

import com.fasterxml.jackson.databind.ObjectMapper
import com.lgzarturo.springbootcourse.features.users.dto.UpdateUserRequest
import com.lgzarturo.springbootcourse.security.SecurityTestConfig
import com.lgzarturo.springbootcourse.security.WithMockUser
import com.lgzarturo.springbootcourse.shared.config.ObjectMapperConfig
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

/**
 * Test de integración para UserController
 * Verifica el comportamiento de los endpoints REST
 */
@Disabled("Se deshabilita temporalmente hasta implementar la capa de persistencia")
@WebMvcTest(UserController::class)
@Import(SecurityTestConfig::class, ObjectMapperConfig::class)
@DisplayName("UserController Integration Tests")
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

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

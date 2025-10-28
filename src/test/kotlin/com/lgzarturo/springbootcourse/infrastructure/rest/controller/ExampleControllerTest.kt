package com.lgzarturo.springbootcourse.infrastructure.rest.controller

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.domain.port.input.ExampleUseCase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ExampleController::class)
class ExampleControllerTest(
    @Autowired val mockMvc: MockMvc,
) {
    @MockBean private lateinit var service: ExampleUseCase

    @Test
    @DisplayName("Debería retornar 201 cuando se crea un objeto en la base de datos")
    fun `should return 201 when creating example`() {
        whenever(service.create(any())).thenReturn(Example(1, "Test", "Desc"))

        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test","description":"Desc"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test"))
    }

    @Test
    @DisplayName(
        "Debería retornar 201 cuando se crea un objeto en la base de datos, aunque el campo 'description' esté ausente",
    )
    fun `should return 201 when creating example without description`() {
        whenever(service.create(any())).thenReturn(Example(1, "Test", null))

        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test"))
            .andExpect(jsonPath("$.description").doesNotExist())
    }

    @Test
    @DisplayName("Debería aceptar caracteres especiales en el campo 'name'")
    fun `should accept special characters in name`() {
        whenever(service.create(any())).thenReturn(Example(1, "Test-123_#@", "Desc"))

        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test-123_#@","description":"Desc"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Test-123_#@"))
    }

    @Test
    @DisplayName("Debería aceptar caracteres Unicode en el campo 'name'")
    fun `should accept Unicode characters in name`() {
        whenever(service.create(any())).thenReturn(Example(1, "Tëst 测试 🚀", "Desc"))

        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Tëst 测试 🚀","description":"Desc"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Tëst 测试 🚀"))
    }

    @Test
    @DisplayName("Debería aceptar el nombre con el límite máximo exacto de caracteres (100)")
    fun `should accept name with exact character limit`() {
        val exactLimitName = "a".repeat(100)
        whenever(service.create(any())).thenReturn(Example(1, exactLimitName, "Desc"))

        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"$exactLimitName","description":"Desc"}"""),
            ).andExpect(status().isCreated)
    }

    @Test
    @DisplayName("Debería aceptar la descripción con el límite máximo exacto de caracteres (500)")
    fun `should accept description with exact character limit`() {
        val exactLimitDescription = "a".repeat(500)
        whenever(service.create(any())).thenReturn(Example(1, "Test", exactLimitDescription))

        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test","description":"$exactLimitDescription"}"""),
            ).andExpect(status().isCreated)
    }

    @Test
    @DisplayName("Debería retornar 400 cuando la solicitud es inválida, por ejemplo, el cuerpo está vacío")
    fun `should return 400 when request body is empty`() {
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Debería retornar 400 cuando la solicitud es inválida, por ejemplo, falta el campo 'name'")
    fun `should return 400 when request is invalid`() {
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"description":"Desc"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Debería retornar 400 cuando la solicitud es inválida, por ejemplo, el campo 'name' está vacío")
    fun `should return 400 when name is empty`() {
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"","description":"Desc"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName(
        "Debería retornar 400 cuando la solicitud es inválida, por ejemplo, el campo 'name' excede el límite de caracteres",
    )
    fun `should return 400 when name exceeds character limit`() {
        val longName = "a".repeat(101) // Suponiendo que el límite es 100 caracteres
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"$longName","description":"Desc"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName(
        "Debería retornar 400 cuando la solicitud es inválida, por ejemplo, el campo 'description' excede el límite de caracteres",
    )
    fun `should return 400 when description exceeds character limit`() {
        val longDescription = "a".repeat(501) // Suponiendo que el límite es 500 caracteres
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test","description":"$longDescription"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Debería retornar 400 cuando el campo 'name' solo contiene espacios en blanco")
    fun `should return 400 when name contains only whitespace`() {
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"   ","description":"Desc"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Debería retornar 400 cuando el campo 'description' solo contiene espacios en blanco")
    fun `should return 400 when description contains only whitespace`() {
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test","description":"   "}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Debería retornar 400 cuando el JSON es malformado")
    fun `should return 400 when JSON is malformed`() {
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test","description":}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Debería retornar 415 cuando el Content-Type no es application/json")
    fun `should return 415 when Content-Type is not application json`() {
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("""{"name":"Test","description":"Desc"}"""),
            ).andExpect(status().isUnsupportedMediaType)
    }

    @Test
    @DisplayName("Debería retornar 201 cuando se envían campos adicionales no esperados, ignorandolos")
    fun `should handle extra fields gracefully`() {
        whenever(service.create(any())).thenReturn(Example(1, "Test", "Desc"))

        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test","description":"Desc","extraField":"value"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test"))
    }

    @Test
    @DisplayName("Debería retornar 400 cuando el campo 'name' es null")
    fun `should return 400 when name is null`() {
        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":null,"description":"Desc"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Debería retornar 500 cuando el servicio lanza una excepción inesperada")
    fun `should return 500 when service throws unexpected exception`() {
        whenever(service.create(any())).thenThrow(RuntimeException("Database error"))

        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test","description":"Desc"}"""),
            ).andExpect(status().isInternalServerError)
    }

    @Test
    @DisplayName("Debería retornar 409 cuando se intenta crear un ejemplo duplicado")
    fun `should return 409 when example already exists`() {
        whenever(service.create(any())).thenThrow(IllegalStateException("Example already exists"))

        mockMvc
            .perform(
                post("/api/v1/examples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Test","description":"Desc"}"""),
            ).andExpect(status().isConflict)
    }
}

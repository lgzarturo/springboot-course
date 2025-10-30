package com.lgzarturo.springbootcourse.infrastructure.rest.controller

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.domain.port.input.ExampleUseCase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ExampleController::class)
class ExampleControllerTest(
    @Autowired val mockMvc: MockMvc,
) {
    @MockBean private lateinit var service: ExampleUseCase

    @Nested
    @DisplayName("POST /api/v1/examples")
    inner class CreateExampleTests {
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

    @Nested
    @DisplayName("GET /api/v1/examples/{id}")
    inner class ShowExampleTests {
        @Test
        @DisplayName("Debería retornar 200 y el recurso cuando el id existe")
        fun `should return 200 when example exists`() {
            whenever(service.findById(1)).thenReturn(Example(1, "Test", "Desc"))

            mockMvc
                .perform(get("/api/v1/examples/1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.description").value("Desc"))
        }

        @Test
        @DisplayName("Debería retornar 200 y omitir 'description' cuando es null")
        fun `should return 200 and omit description when null`() {
            whenever(service.findById(1)).thenReturn(Example(1, "Test", null))

            mockMvc
                .perform(get("/api/v1/examples/1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.description").doesNotExist())
        }

        @Test
        @DisplayName("Debería retornar 404 cuando el recurso no existe")
        fun `should return 404 when example does not exist`() {
            whenever(service.findById(999)).thenThrow(NoSuchElementException("Example not found"))

            mockMvc
                .perform(get("/api/v1/examples/999"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el id no es numérico")
        fun `should return 400 when id is not numeric`() {
            mockMvc
                .perform(get("/api/v1/examples/abc"))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el id es menor o igual a 0")
        fun `should return 400 when id is less than or equal to zero`() {
            mockMvc
                .perform(get("/api/v1/examples/0"))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 500 cuando el servicio lanza una excepción inesperada")
        fun `should return 500 when service throws unexpected exception on get`() {
            whenever(service.findById(1)).thenThrow(RuntimeException("Database error"))

            mockMvc
                .perform(get("/api/v1/examples/1"))
                .andExpect(status().isInternalServerError)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/examples")
    inner class GetAllExampleTests {
        @Test
        @DisplayName("Debería retornar 200 con paginación por defecto y sin filtros")
        fun `should return 200 with default pagination and no filters`() {
            val content = listOf(Example(1, "Alpha", "A"), Example(2, "Beta", null))
            val page = PageImpl(content, PageRequest.of(0, 20, Sort.by("id").ascending()), content.size.toLong())

            whenever(service.findAll(any(), any())).thenReturn(page)

            mockMvc
                .perform(get("/api/v1/examples"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Alpha"))
                .andExpect(jsonPath("$.content[1].description").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
        }

        @Test
        @DisplayName("Debería retornar 200 con paginación y ordenación explícitas")
        fun `should return 200 with explicit pagination and sorting`() {
            val content = listOf(Example(2, "Beta", null))
            val page = PageImpl(content, PageRequest.of(1, 1, Sort.by("name").descending()), 2)

            whenever(service.findAll(any(), any())).thenReturn(page)

            mockMvc
                .perform(
                    get("/api/v1/examples")
                        .param("page", "1")
                        .param("size", "1")
                        .param("sort", "name,desc"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(1))
        }

        @Test
        @DisplayName("Debería filtrar por nombre exacto (case-insensitive)")
        fun `should filter by exact name ignoring case`() {
            val content = listOf(Example(3, "Test", "Desc"))
            val page = PageImpl(content, PageRequest.of(0, 20), 1)

            whenever(service.findAll(any(), any())).thenReturn(page)

            mockMvc
                .perform(get("/api/v1/examples").param("name", "test"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test"))
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay resultados")
        fun `should return empty list when no results`() {
            val page = PageImpl(emptyList<Example>(), PageRequest.of(0, 20), 0)
            whenever(service.findAll(any(), any())).thenReturn(page)

            mockMvc
                .perform(get("/api/v1/examples"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
        }

        @Test
        @DisplayName("Debería retornar 400 cuando los parámetros de paginación son inválidos")
        fun `should return 400 when pagination params are invalid`() {
            mockMvc
                .perform(get("/api/v1/examples").param("page", "-1"))
                .andExpect(status().isBadRequest)

            mockMvc
                .perform(get("/api/v1/examples").param("size", "0"))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 500 cuando el servicio lanza una excepción inesperada en el listado")
        fun `should return 500 when service throws unexpectedly on list`() {
            whenever(service.findAll(any(), any())).thenThrow(RuntimeException("DB error"))

            mockMvc
                .perform(get("/api/v1/examples"))
                .andExpect(status().isInternalServerError)
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/examples/{id}")
    inner class UpdateExampleTests {
        @Test
        @DisplayName("Debería retornar 200 cuando actualiza un recurso existente")
        fun `should return 200 when updating existing resource`() {
            whenever(service.update(1, Example(name = "Updated", description = "New")))
                .thenReturn(Example(1, "Updated", "New"))

            mockMvc
                .perform(
                    put("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Updated","description":"New"}"""),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.description").value("New"))
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el body es inválido (name vacío)")
        fun `should return 400 when body is invalid`() {
            mockMvc
                .perform(
                    put("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"","description":"New"}"""),
                ).andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 404 cuando el recurso a actualizar no existe")
        fun `should return 404 when updating non-existent resource`() {
            whenever(service.update(999, Example(name = "Updated", description = "New")))
                .thenThrow(NoSuchElementException("Not found"))

            mockMvc
                .perform(
                    put("/api/v1/examples/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Updated","description":"New"}"""),
                ).andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("Debería retornar 409 cuando existe un conflicto (duplicado)")
        fun `should return 409 when conflict occurs`() {
            whenever(service.update(1, Example(name = "Duplicated", description = "New")))
                .thenThrow(IllegalStateException("Duplicate name"))

            mockMvc
                .perform(
                    put("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Duplicated","description":"New"}"""),
                ).andExpect(status().isConflict)
        }

        @Test
        @DisplayName("Debería retornar 415 cuando el Content-Type no es application/json")
        fun `should return 415 when content type is not json on put`() {
            mockMvc
                .perform(
                    put("/api/v1/examples/1")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=Updated&description=New"),
                ).andExpect(status().isUnsupportedMediaType)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/examples/{id}")
    inner class PatchExampleTests {
        @Test
        @DisplayName("Debería retornar 200 cuando actualiza parcialmente (solo description)")
        fun `should return 200 when patching only description`() {
            whenever(service.patch(1, Example(name = "Ignored", description = "New Desc")))
                .thenReturn(Example(1, "Existing Name", "New Desc"))

            mockMvc
                .perform(
                    patch("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"description":"New Desc"}"""),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Existing Name"))
                .andExpect(jsonPath("$.description").value("New Desc"))
        }

        @Test
        @DisplayName("Debería retornar 200 cuando actualiza parcialmente (solo name)")
        fun `should return 200 when patching only name`() {
            whenever(service.patch(1, Example(name = "New Name", description = null)))
                .thenReturn(Example(1, "New Name", "Old Desc"))

            mockMvc
                .perform(
                    patch("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"New Name"}"""),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Name"))
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el payload del PATCH es inválido (vacío)")
        fun `should return 400 when patch payload is invalid`() {
            mockMvc
                .perform(
                    patch("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{}"""),
                ).andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 404 cuando el recurso a actualizar parcialmente no existe")
        fun `should return 404 when patching non-existent resource`() {
            whenever(service.patch(999, Example(name = null, description = "New")))
                .thenThrow(NoSuchElementException("Not found"))

            mockMvc
                .perform(
                    patch("/api/v1/examples/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"description":"New"}"""),
                ).andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("Debería retornar 500 cuando el servicio lanza una excepción inesperada (PATCH)")
        fun `should return 500 when service throws unexpectedly on patch`() {
            whenever(service.patch(1, Example(name = null, description = "X")))
                .thenThrow(RuntimeException("DB error"))

            mockMvc
                .perform(
                    patch("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"description":"X"}"""),
                ).andExpect(status().isInternalServerError)
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/examples/{id}")
    inner class DeleteExampleTest {
        @Test
        @DisplayName("Debería retornar 204 cuando elimina un recurso existente")
        fun `should return 204 when deleting existing resource`() {
            mockMvc
                .perform(delete("/api/v1/examples/1"))
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("Debería retornar 404 cuando el recurso a eliminar no existe")
        fun `should return 404 when deleting non-existent resource`() {
            whenever(service.delete(999)).thenThrow(NoSuchElementException("Not found"))

            mockMvc
                .perform(delete("/api/v1/examples/999"))
                .andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el id no es numérico (DELETE)")
        fun `should return 400 when id is not numeric on delete`() {
            mockMvc
                .perform(delete("/api/v1/examples/abc"))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el id es menor o igual a 0 (DELETE)")
        fun `should return 400 when id is less than or equal to zero on delete`() {
            mockMvc
                .perform(delete("/api/v1/examples/0"))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 500 cuando el servicio lanza error inesperado (DELETE)")
        fun `should return 500 when service throws unexpectedly on delete`() {
            whenever(service.delete(1)).thenThrow(RuntimeException("DB error"))

            mockMvc
                .perform(delete("/api/v1/examples/1"))
                .andExpect(status().isInternalServerError)
        }
    }
}

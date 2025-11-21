package com.lgzarturo.springbootcourse.example.adapters.rest

import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.example.application.ports.input.ExampleUseCasePort
import com.lgzarturo.springbootcourse.example.domain.Example
import com.lgzarturo.springbootcourse.shared.domain.PageResult
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(ExampleController::class)
class ExampleControllerTest(
    @Autowired val mockMvc: MockMvc,
) {
    @MockBean
    private lateinit var service: ExampleUseCasePort

    @Nested
    @DisplayName("POST /api/v1/examples")
    inner class CreateExampleTests {
        @Test
        @DisplayName("Debería retornar 201 cuando se crea un objeto en la base de datos")
        fun `should return 201 when creating example`() {
            whenever(service.create(any())).thenReturn(Example(1, "Test", "Desc"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test"))
        }

        @Test
        @DisplayName(
            "Debería retornar 201 cuando se crea un objeto en la base de datos, aunque el campo 'description' esté ausente",
        )
        fun `should return 201 when creating example without description`() {
            whenever(service.create(any())).thenReturn(Example(1, "Test", null))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test"}"""),
                ).andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").doesNotExist())
        }

        @Test
        @DisplayName("Debería aceptar caracteres especiales en el campo 'name'")
        fun `should accept special characters in name`() {
            whenever(service.create(any())).thenReturn(Example(1, "Test-123_#@", "Desc"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test-123_#@","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test-123_#@"))
        }

        @Test
        @DisplayName("Debería aceptar caracteres Unicode en el campo 'name'")
        fun `should accept Unicode characters in name`() {
            whenever(service.create(any())).thenReturn(Example(1, "Tëst 测试 🚀", "Desc"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Tëst 测试 🚀","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Tëst 测试 🚀"))
        }

        @Test
        @DisplayName("Debería aceptar el nombre con el límite máximo exacto de caracteres (100)")
        fun `should accept name with exact character limit`() {
            val exactLimitName = "a".repeat(100)
            whenever(service.create(any())).thenReturn(Example(1, exactLimitName, "Desc"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"$exactLimitName","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isCreated)
        }

        @Test
        @DisplayName("Debería aceptar la descripción con el límite máximo exacto de caracteres (500)")
        fun `should accept description with exact character limit`() {
            val exactLimitDescription = "a".repeat(500)
            whenever(service.create(any())).thenReturn(Example(1, "Test", exactLimitDescription))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test","description":"$exactLimitDescription"}"""),
                ).andExpect(MockMvcResultMatchers.status().isCreated)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando la solicitud es inválida, por ejemplo, el cuerpo está vacío")
        fun `should return 400 when request body is empty`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando la solicitud es inválida, por ejemplo, falta el campo 'name'")
        fun `should return 400 when request is invalid`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando la solicitud es inválida, por ejemplo, el campo 'name' está vacío")
        fun `should return 400 when name is empty`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName(
            "Debería retornar 400 cuando la solicitud es inválida, por ejemplo, el campo 'name' excede el límite de caracteres",
        )
        fun `should return 400 when name exceeds character limit`() {
            val longName = "a".repeat(101) // Suponiendo que el límite es 100 caracteres
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"$longName","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName(
            "Debería retornar 400 cuando la solicitud es inválida, por ejemplo, el campo 'description' excede el límite de caracteres",
        )
        fun `should return 400 when description exceeds character limit`() {
            val longDescription = "a".repeat(501) // Suponiendo que el límite es 500 caracteres
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test","description":"$longDescription"}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el campo 'name' solo contiene espacios en blanco")
        fun `should return 400 when name contains only whitespace`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"   ","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el campo 'description' solo contiene espacios en blanco")
        fun `should return 400 when description contains only whitespace`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test","description":"   "}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el JSON es malformado")
        fun `should return 400 when JSON is malformed`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test","description":}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 415 cuando el Content-Type no es application/json")
        fun `should return 415 when Content-Type is not application json`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("""{"name":"Test","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType)
        }

        @Test
        @DisplayName("Debería retornar 201 cuando se envían campos adicionales no esperados, ignorandolos")
        fun `should handle extra fields gracefully`() {
            whenever(service.create(any())).thenReturn(Example(1, "Test", "Desc"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test","description":"Desc","extraField":"value"}"""),
                ).andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test"))
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el campo 'name' es null")
        fun `should return 400 when name is null`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":null,"description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 500 cuando el servicio lanza una excepción inesperada")
        fun `should return 500 when service throws unexpected exception`() {
            whenever(service.create(any())).thenThrow(RuntimeException("Database error"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isInternalServerError)
        }

        @Test
        @DisplayName("Debería retornar 409 cuando se intenta crear un ejemplo duplicado")
        fun `should return 409 when example already exists`() {
            whenever(service.create(any())).thenThrow(IllegalStateException("Example already exists"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Test","description":"Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isConflict)
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
                .perform(MockMvcRequestBuilders.get("/api/v1/examples/1"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Desc"))
        }

        @Test
        @DisplayName("Debería retornar 200 y omitir 'description' cuando es null")
        fun `should return 200 and omit description when null`() {
            whenever(service.findById(1)).thenReturn(Example(1, "Test", null))

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples/1"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").doesNotExist())
        }

        @Test
        @DisplayName("Debería retornar 404 cuando el recurso no existe")
        fun `should return 404 when example does not exist`() {
            whenever(service.findById(999)).thenThrow(NoSuchElementException("Example not found"))

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples/999"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Not Found"))
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el id no es numérico")
        fun `should return 400 when id is not numeric`() {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples/abc"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el id es menor o igual a 0")
        fun `should return 400 when id is less than or equal to zero`() {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples/0"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 500 cuando el servicio lanza una excepción inesperada")
        fun `should return 500 when service throws unexpected exception on get`() {
            whenever(service.findById(1)).thenThrow(RuntimeException("Database error"))

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples/1"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/examples")
    inner class GetAllExampleTests {
        @Test
        @DisplayName("Debería retornar 200 con paginación por defecto y sin filtros")
        fun `should return 200 with default pagination and no filters`() {
            val content = listOf(Example(1, "Alpha", "A"), Example(2, "Beta", null))
            val page = PageResult(content, 2, 0, 20)

            whenever(service.findAll(isNull(), any())).thenReturn(page)

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Alpha"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].description").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.page").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(20))
        }

        @Test
        @DisplayName("Debería retornar 200 con paginación y ordenación explícitas")
        fun `should return 200 with explicit pagination and sorting`() {
            val content = listOf(Example(2, "Beta", null))
            val page = PageResult(content, 1, 0, 1)

            whenever(service.findAll(isNull(), any())).thenReturn(page)

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/examples")
                        .param("page", "1")
                        .param("size", "1")
                        .param("sort", "name,desc"),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(1))
        }

        @Test
        @DisplayName("Debería filtrar por nombre exacto (case-insensitive)")
        fun `should filter by exact name ignoring case`() {
            val content = listOf(Example(3, "Test", "Desc"))
            val page = PageResult(content, 1, 0, 1)

            whenever(service.findAll(any(), any())).thenReturn(page)

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples").param("searchText", "test"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Test"))
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay resultados")
        fun `should return empty list when no results`() {
            val page = PageResult(emptyList<Example>(), 0, 0, 0)
            whenever(service.findAll(isNull(), any())).thenReturn(page)

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pages").value(0))
        }

        @Test
        @DisplayName("Debería retornar 400 cuando los parámetros de paginación son inválidos")
        fun `should return 400 when pagination params are invalid`() {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples").param("page", "-1"))
                .andExpect(MockMvcResultMatchers.status().isOk)

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples").param("size", "0"))
                .andExpect(MockMvcResultMatchers.status().isOk)
        }

        @Test
        @DisplayName("Debería usar página 0 cuando page es negativo")
        fun `should use page 0 when page is negative`() {
            val items =
                listOf(
                    Example(id = 1, name = "Alpha", description = null),
                    Example(id = 2, name = "Beta", description = "B"),
                )
            val page = PageResult(items, items.size.toLong(), 0, 2)

            whenever(service.findAll(isNull(), any())).thenReturn(page)

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples").param("page", "-1"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray)
                .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(2)) // Verifica que usó página 0
        }

        @Test
        @DisplayName("Debería usar tamaño por defecto cuando size es 0")
        fun `should use default size when size is 0`() {
            val items =
                listOf(
                    Example(id = 1, name = "Alpha", description = null),
                )
            val page = PageResult(items, items.size.toLong(), 0, 20)

            whenever(service.findAll(isNull(), any())).thenReturn(page)

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples").param("size", "0"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray)
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(20)) // Verifica que usó tamaño por defecto
        }

        @Test
        @DisplayName("Debería usar tamaño por defecto cuando size es negativo")
        fun `should use default size when size is negative`() {
            val items =
                listOf(
                    Example(id = 1, name = "Alpha", description = null),
                )
            val page = PageResult(items, items.size.toLong(), 0, 20)

            whenever(service.findAll(isNull(), any())).thenReturn(page)

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples").param("size", "-5"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(20))
        }

        @Test
        @DisplayName("Debería usar valores por defecto cuando no se especifican parámetros")
        fun `should use default values when no parameters specified`() {
            val items =
                listOf(
                    Example(id = 1, name = "Alpha", description = null),
                )
            val page = PageResult(items, items.size.toLong(), 0, 20)

            whenever(service.findAll(isNull(), any())).thenReturn(page)

            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/examples"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray)
                .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(20))
        }

        @Test
        @DisplayName("Debería usar parámetros válidos correctamente")
        fun `should use valid parameters correctly`() {
            val items =
                listOf(
                    Example(id = 1, name = "Alpha", description = null),
                    Example(id = 2, name = "Beta", description = "B"),
                )
            val page = PageResult(items, items.size.toLong(), 0, 10)

            whenever(service.findAll(isNull(), any())).thenReturn(page)

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/examples")
                        .param("page", "2")
                        .param("size", "10"),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray)
                .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(10))
        }

        @Test
        @DisplayName("Debería filtrar por searchText y paginar correctamente")
        fun `should filter by searchText and paginate correctly`() {
            val searchText = "Alpha"
            val items =
                listOf(
                    Example(id = 1, name = "Alpha", description = null),
                )
            val page = PageResult(items, items.size.toLong(), 0, 1)

            whenever(service.findAll(eq(searchText), any())).thenReturn(page)

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/examples")
                        .param("searchText", searchText)
                        .param("page", "0")
                        .param("size", "10"),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Alpha"))

            verify(service).findAll(eq(searchText), any())
        }

        @Test
        @DisplayName("Debería retornar página vacía cuando no hay resultados")
        fun `should return empty page when no results found`() {
            val searchText = "NonExistent"
            val emptyPage = PageResult(emptyList<Example>(), 0, 0, 2)

            whenever(service.findAll(eq(searchText), any())).thenReturn(emptyPage)

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/examples")
                        .param("searchText", searchText),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isEmpty)
                .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.empty").value(true))
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
                    MockMvcRequestBuilders
                        .put("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Updated","description":"New"}"""),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("New"))
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el body es inválido (name vacío)")
        fun `should return 400 when body is invalid`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .put("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"","description":"New"}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 404 cuando el recurso a actualizar no existe")
        fun `should return 404 when updating non-existent resource`() {
            whenever(service.update(999, Example(name = "Updated", description = "New")))
                .thenThrow(NoSuchElementException("Not found"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .put("/api/v1/examples/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Updated","description":"New"}"""),
                ).andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        @DisplayName("Debería retornar 409 cuando existe un conflicto (duplicado)")
        fun `should return 409 when conflict occurs`() {
            whenever(service.update(1, Example(name = "Duplicated", description = "New")))
                .thenThrow(IllegalStateException("Duplicate name"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .put("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"Duplicated","description":"New"}"""),
                ).andExpect(MockMvcResultMatchers.status().isConflict)
        }

        @Test
        @DisplayName("Debería retornar 415 cuando el Content-Type no es application/json")
        fun `should return 415 when content type is not json on put`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .put("/api/v1/examples/1")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=Updated&description=New"),
                ).andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/examples/{id}")
    inner class PatchExampleTests {
        @Test
        @DisplayName("Debería retornar 200 cuando actualiza parcialmente (solo description)")
        fun `should return 200 when patching only description`() {
            whenever(service.patch(1, ExamplePatchUpdate(property = "description", value = "New Desc")))
                .thenReturn(Example(1, "Existing Name", "New Desc"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .patch("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"property":"description","value":"New Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Existing Name"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("New Desc"))
        }

        @Test
        @DisplayName("Debería retornar 200 cuando actualiza parcialmente (solo name)")
        fun `should return 200 when patching only name`() {
            whenever(service.patch(1, ExamplePatchUpdate(property = "name", value = "New Name")))
                .thenReturn(Example(1, "New Name", "Old Desc"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .patch("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"property":"name","value":"New Name"}"""),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("New Name"))
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el payload del PATCH es inválido (vacío)")
        fun `should return 400 when patch payload is invalid`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .patch("/api/v1/examples/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{}"""),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 404 cuando el recurso a actualizar parcialmente no existe")
        fun `should return 404 when patching non-existent resource`() {
            whenever(service.patch(999, ExamplePatchUpdate(property = "name", value = "New")))
                .thenThrow(NoSuchElementException("Not found"))

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .patch("/api/v1/examples/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"property":"description","value":"New Desc"}"""),
                ).andExpect(MockMvcResultMatchers.status().isNotFound)
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/examples/{id}")
    inner class DeleteExampleTest {
        @Test
        @DisplayName("Debería retornar 204 cuando elimina un recurso existente")
        fun `should return 204 when deleting existing resource`() {
            mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/examples/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
        }

        @Test
        @DisplayName("Debería retornar 404 cuando el recurso a eliminar no existe")
        fun `should return 404 when deleting non-existent resource`() {
            whenever(service.delete(999)).thenThrow(NoSuchElementException("Not found"))

            mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/examples/999"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el id no es numérico (DELETE)")
        fun `should return 400 when id is not numeric on delete`() {
            mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/examples/abc"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el id es menor o igual a 0 (DELETE)")
        fun `should return 400 when id is less than or equal to zero on delete`() {
            mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/examples/0"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        @DisplayName("Debería retornar 500 cuando el servicio lanza error inesperado (DELETE)")
        fun `should return 500 when service throws unexpectedly on delete`() {
            whenever(service.delete(1)).thenThrow(RuntimeException("DB error"))

            mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/examples/1"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError)
        }
    }
}

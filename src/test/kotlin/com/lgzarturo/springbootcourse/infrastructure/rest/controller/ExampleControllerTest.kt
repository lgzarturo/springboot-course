package com.lgzarturo.springbootcourse.infrastructure.rest.controller

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.domain.port.input.ExampleUseCase
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
}

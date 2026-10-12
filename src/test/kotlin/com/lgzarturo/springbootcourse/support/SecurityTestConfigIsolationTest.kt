package com.lgzarturo.springbootcourse.support

import com.lgzarturo.springbootcourse.features.ping.Ping
import com.lgzarturo.springbootcourse.features.ping.PingController
import com.lgzarturo.springbootcourse.features.ping.PingMapper
import com.lgzarturo.springbootcourse.features.ping.PingUseCasePort
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PingController::class)
@Import(PingMapper::class, SecurityTestConfig::class)
@DisplayName("SecurityTestConfig Isolation Tests")
class SecurityTestConfigIsolationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var pingUseCasePort: PingUseCasePort

    @Test
    @DisplayName("debe_permitir_ejecutar_slice_sin_cadena_produccion_cuando_se_importa_security_test_config")
    fun `debe_permitir_ejecutar_slice_sin_cadena_produccion_cuando_se_importa_security_test_config`() {
        every { pingUseCasePort.getPing() } returns Ping(message = "pong")

        mockMvc.perform(get("/api/v1/ping"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("pong"))
    }
}

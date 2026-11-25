package com.lgzarturo.springbootcourse.config.security

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Con proxy no confiable (defecto) X-Forwarded-For debe ignorarse:
 * rotarlo no elude el límite.
 */
@SpringBootTest(
    properties = [
        "auth.rate-limit.enabled=true",
        "auth.rate-limit.per-minute=3",
        "auth.rate-limit.trust-forwarded-headers=false",
    ],
)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Rate Limit Untrusted Proxy Tests")
class RateLimitUntrustedProxyTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("Rotar X-Forwarded-For no debe eludir el límite")
    fun `rotating forwarded header does not bypass limit`() {
        repeat(3) { index ->
            mockMvc
                .perform(
                    post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "10.0.0.$index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("ratelimit.spoof@pokemon.com")),
                ).andExpect(status().isUnauthorized)
        }

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .header("X-Forwarded-For", "10.9.9.9")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("ratelimit.spoof@pokemon.com")),
            ).andExpect(status().isTooManyRequests)
    }

    private fun loginBody(email: String): String = """{"email":"$email","password":"WrongPass1"}"""
}

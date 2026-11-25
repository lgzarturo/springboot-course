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
 * Con proxy confiable cada IP de X-Forwarded-For tiene su propio bucket.
 */
@SpringBootTest(
    properties = [
        "auth.rate-limit.enabled=true",
        "auth.rate-limit.per-minute=3",
        "auth.rate-limit.trust-forwarded-headers=true",
    ],
)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Rate Limit Trusted Proxy Tests")
class RateLimitTrustedProxyTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("IPs distintas deben tener buckets independientes")
    fun `distinct forwarded ips have independent buckets`() {
        repeat(4) { index ->
            mockMvc
                .perform(
                    post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "10.1.0.$index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("ratelimit.trusted@pokemon.com")),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Test
    @DisplayName("La misma IP de X-Forwarded-For debe limitarse")
    fun `same forwarded ip is rate limited`() {
        repeat(3) {
            mockMvc
                .perform(
                    post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "10.2.0.7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("ratelimit.trusted@pokemon.com")),
                ).andExpect(status().isUnauthorized)
        }

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .header("X-Forwarded-For", "10.2.0.7")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("ratelimit.trusted@pokemon.com")),
            ).andExpect(status().isTooManyRequests)
    }

    private fun loginBody(email: String): String = """{"email":"$email","password":"WrongPass1"}"""
}

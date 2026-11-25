package com.lgzarturo.springbootcourse.features.auth

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

/**
 * TDD Tests for WS6 (M3): logging de eventos de seguridad sin PII.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Security Logging Integration Tests")
class SecurityLoggingIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val appenders = mutableListOf<Pair<Logger, ListAppender<ILoggingEvent>>>()

    @AfterEach
    fun detachAppenders() {
        appenders.forEach { (logger, appender) -> logger.detachAppender(appender) }
        appenders.clear()
    }

    @Test
    @DisplayName("Login exitoso y fallido deben quedar en INFO y WARN sin filtrar secretos")
    fun `login attempts are logged without secrets`() {
        val events = attach(AuthService::class.java)
        register("logs.auth@pokemon.com")

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("logs.auth@pokemon.com", "Pikachu123!")),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("logs.auth@pokemon.com", "WrongPass1")),
            ).andExpect(status().isUnauthorized)

        val messages = events.list.map { it.formattedMessage }
        assertTrue(messages.any { it.contains("Authentication successful") }, "success INFO missing: $messages")
        assertTrue(messages.any { it.contains("Authentication failed") }, "failure WARN missing: $messages")
        assertTrue(events.list.any { it.level == Level.INFO })
        assertTrue(events.list.any { it.level == Level.WARN })
        assertFalse(messages.any { it.contains("Pikachu123!") }, "password leaked in logs")
        assertFalse(messages.any { it.contains("logs.auth@pokemon.com") }, "full email leaked in logs")
    }

    @Test
    @DisplayName("Logout debe quedar en INFO con jti y userId")
    fun `logout is logged`() {
        val events = attach(LogoutService::class.java)
        register("logs.logout@pokemon.com")
        val token = loginToken("logs.logout@pokemon.com", "Pikachu123!")

        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isNoContent)

        val messages = events.list.map { it.formattedMessage }
        assertTrue(messages.any { it.contains("logged out") }, "logout INFO missing: $messages")
        assertFalse(messages.any { it.contains(token) }, "token leaked in logs")
    }

    private fun attach(clazz: Class<*>): ListAppender<ILoggingEvent> {
        val logger = LoggerFactory.getLogger(clazz) as Logger
        val appender = ListAppender<ILoggingEvent>()
        appender.start()
        logger.addAppender(appender)
        appenders.add(logger to appender)
        return appender
    }

    private fun register(email: String) {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"$email","password":"Pikachu123!","firstName":"Test","lastName":"Trainer"}"""),
            ).andExpect(status().isCreated)
    }

    private fun loginToken(
        email: String,
        password: String,
    ): String {
        val body =
            mockMvc
                .perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, password)),
                ).andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
        return objectMapper.readTree(body).get("token").asText()
    }

    private fun loginBody(
        email: String,
        password: String,
    ): String = """{"email":"$email","password":"$password"}"""
}

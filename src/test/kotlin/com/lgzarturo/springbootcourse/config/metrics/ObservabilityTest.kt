package com.lgzarturo.springbootcourse.config.metrics

import com.lgzarturo.springbootcourse.features.hotels.HotelEntity
import com.lgzarturo.springbootcourse.features.hotels.HotelJpaRepository
import com.lgzarturo.springbootcourse.features.rooms.RoomEntity
import com.lgzarturo.springbootcourse.features.rooms.RoomJpaRepository
import com.lgzarturo.springbootcourse.features.rooms.RoomStatus
import com.lgzarturo.springbootcourse.features.users.UserRole
import com.lgzarturo.springbootcourse.security.JwtTestHelper
import io.micrometer.core.instrument.MeterRegistry
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

/**
 * Pruebas de observabilidad productiva (BC-015): probes de salud, autorización
 * de Actuator, formato Prometheus, métricas de negocio y entorno de Sentry.
 */
@SpringBootTest(
    properties = [
        "management.endpoints.web.exposure.include=health,info,metrics,prometheus",
    ],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Observability Actuator Tests")
class ObservabilityTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var registry: MeterRegistry

    @Autowired
    private lateinit var hotelRepository: HotelJpaRepository

    @Autowired
    private lateinit var roomRepository: RoomJpaRepository

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.issuer}")
    private lateinit var jwtIssuer: String

    @Value("\${sentry.environment}")
    private lateinit var sentryEnvironment: String

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc =
            MockMvcBuilders
                .webAppContextSetup(context)
                .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(springSecurity())
                .build()
    }

    private fun tokenFor(role: UserRole): String =
        JwtTestHelper.mintToken(
            secret = jwtSecret,
            subject = "observability@${role.name.lowercase()}.pokemon.com",
            roles = role.allGrantedAuthorities(),
            issuer = jwtIssuer,
            ttlMillis = 60_000,
        )

    @Test
    @DisplayName("debe_retornar_up_cuando_health_es_consultado_sin_autenticacion")
    fun `debe_retornar_up_cuando_health_es_consultado_sin_autenticacion`() {
        mockMvc
            .perform(get("/actuator/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
    }

    @Test
    @DisplayName("debe_retornar_up_cuando_probes_liveness_readiness_son_consultados")
    fun `debe_retornar_up_cuando_probes_liveness_readiness_son_consultados`() {
        mockMvc
            .perform(get("/actuator/health/liveness"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))

        mockMvc
            .perform(get("/actuator/health/readiness"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
    }

    @Test
    @DisplayName("debe_retornar_prometheus_cuando_admin_consulta_formato_exposicion")
    fun `debe_retornar_prometheus_cuando_admin_consulta_formato_exposicion`() {
        mockMvc
            .perform(
                get("/actuator/prometheus")
                    .header("Authorization", "Bearer ${tokenFor(UserRole.ADMIN)}"),
            ).andExpect(status().isOk)
            .andExpect(content().string(containsString("# HELP")))
            .andExpect(content().string(containsString("jvm_memory_used_bytes")))
    }

    @Test
    @DisplayName("debe_rechazar_actuator_cuando_no_hay_autorizacion_de_administrador")
    fun `debe_rechazar_actuator_cuando_no_hay_autorizacion_de_administrador`() {
        mockMvc
            .perform(get("/actuator/prometheus"))
            .andExpect(status().isUnauthorized)

        mockMvc
            .perform(
                get("/actuator/metrics")
                    .header("Authorization", "Bearer ${tokenFor(UserRole.GUEST)}"),
            ).andExpect(status().isForbidden)

        mockMvc
            .perform(get("/actuator/info"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("debe_incrementar_contador_cuando_reserva_es_creada")
    fun `debe_incrementar_contador_cuando_reserva_es_creada`() {
        val room = saveRoom("OBS-201", RoomStatus.AVAILABLE)
        val before = registry.counter("reservations.created", "outcome", "success").count()

        mockMvc
            .perform(
                post("/api/v1/reservations")
                    .header("Authorization", "Bearer ${tokenFor(UserRole.GUEST)}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationPayload(room.id!!)),
            ).andExpect(status().isCreated)

        assertEquals(before + 1.0, registry.counter("reservations.created", "outcome", "success").count(), 0.001)
    }

    @Test
    @DisplayName("debe_incrementar_contador_error_cuando_reserva_es_invalida")
    fun `debe_incrementar_contador_error_cuando_reserva_es_invalida`() {
        val before = registry.counter("reservations.created", "outcome", "error").count()

        mockMvc
            .perform(
                post("/api/v1/reservations")
                    .header("Authorization", "Bearer ${tokenFor(UserRole.GUEST)}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationPayload("habitacion-inexistente")),
            ).andExpect(status().isBadRequest)

        assertEquals(before + 1.0, registry.counter("reservations.created", "outcome", "error").count(), 0.001)
    }

    @Test
    @DisplayName("debe_exponer_gauge_cuando_habitaciones_disponibles_existen")
    fun `debe_exponer_gauge_cuando_habitaciones_disponibles_existen`() {
        val before = registry.get("rooms.available").gauge().value()
        saveRoom("OBS-301", RoomStatus.AVAILABLE)
        saveRoom("OBS-302", RoomStatus.MAINTENANCE)

        assertEquals(before + 1.0, registry.get("rooms.available").gauge().value(), 0.001)
    }

    @Test
    @DisplayName("debe_registrar_timer_cuando_reserva_es_creada")
    fun `debe_registrar_timer_cuando_reserva_es_creada`() {
        val room = saveRoom("OBS-401", RoomStatus.AVAILABLE)
        val before = timerCount("POST", "success")

        mockMvc
            .perform(
                post("/api/v1/reservations")
                    .header("Authorization", "Bearer ${tokenFor(UserRole.GUEST)}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationPayload(room.id!!)),
            ).andExpect(status().isCreated)

        assertEquals(before + 1, timerCount("POST", "success"))
        mockMvc
            .perform(
                get("/actuator/metrics/reservations.request.duration")
                    .header("Authorization", "Bearer ${tokenFor(UserRole.ADMIN)}"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("reservations.request.duration"))
    }

    @Test
    @DisplayName("debe_configurar_entorno_cuando_sentry_es_cableado")
    fun `debe_configurar_entorno_cuando_sentry_es_cableado`() {
        assertEquals("tests", sentryEnvironment)
    }

    private fun timerCount(
        method: String,
        outcome: String,
    ): Long =
        registry
            .find("reservations.request.duration")
            .tags("method", method, "outcome", outcome)
            .timer()
            ?.count() ?: 0

    private fun saveRoom(
        number: String,
        status: RoomStatus,
    ): RoomEntity {
        val hotel =
            hotelRepository.save(
                HotelEntity(name = "Hotel Observabilidad $number", address = "Ruta $number"),
            )
        return roomRepository.save(
            RoomEntity(
                number = number,
                type = "STANDARD",
                price = 50.0,
                hotel = hotel,
                status = status,
            ),
        )
    }

    private fun reservationPayload(roomId: String): String =
        """
        {"roomId":"$roomId","userId":1,"guestName":"Ash Ketchum",
        "checkIn":"2026-11-01","checkOut":"2026-11-03"}
        """.trimIndent()
}

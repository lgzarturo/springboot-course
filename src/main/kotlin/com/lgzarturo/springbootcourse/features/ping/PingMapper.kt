package com.lgzarturo.springbootcourse.features.ping

import com.lgzarturo.springbootcourse.features.ping.dto.PingResponse
import org.springframework.stereotype.Component

/**
 * Mapper para convertir entre el modelo de dominio Ping y el DTO PingResponse
 * Separa la representación interna del dominio de la representación externa (API)
 */
@Component
class PingMapper {
    /**
     * Convierte un modelo de dominio Ping a un DTO PingResponse
     * @param ping Modelo de dominio
     * @return DTO de respuesta
     */
    fun toResponse(ping: Ping): PingResponse =
        PingResponse(
            message = ping.message,
            timestamp = ping.timestamp,
            version = ping.version,
        )
}

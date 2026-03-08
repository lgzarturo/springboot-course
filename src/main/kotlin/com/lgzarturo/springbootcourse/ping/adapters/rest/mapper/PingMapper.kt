package com.lgzarturo.springbootcourse.ping.adapters.rest.mapper

import com.lgzarturo.springbootcourse.ping.adapters.rest.dto.PingResponse
import com.lgzarturo.springbootcourse.ping.domain.Ping
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

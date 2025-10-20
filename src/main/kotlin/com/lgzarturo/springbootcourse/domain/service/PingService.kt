package com.lgzarturo.springbootcourse.domain.service

import com.lgzarturo.springbootcourse.domain.model.Ping
import com.lgzarturo.springbootcourse.domain.port.input.PingUseCase
import org.springframework.stereotype.Service

/**
 * Servicio de dominio que implementa la lógica de negocio para Ping
 * Esta clase contiene la lógica de negocio pura, independiente de la infraestructura
 */
@Service
class PingService : PingUseCase {
    override fun getPing(): Ping = Ping(message = "pong")

    override fun getPingWithMessage(message: String): Ping = Ping(message = "pong: $message")
}

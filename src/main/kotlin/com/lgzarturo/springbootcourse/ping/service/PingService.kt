package com.lgzarturo.springbootcourse.ping.service

import com.lgzarturo.springbootcourse.ping.application.ports.input.PingUseCase
import com.lgzarturo.springbootcourse.ping.domain.Ping
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

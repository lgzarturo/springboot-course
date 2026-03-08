package com.lgzarturo.springbootcourse.ping.service

import com.lgzarturo.springbootcourse.ping.application.ports.input.PingUseCasePort
import com.lgzarturo.springbootcourse.ping.domain.Ping

/**
 * Servicio de dominio que implementa la lógica de negocio para Ping
 * Esta clase contiene la lógica de negocio pura, independiente de la infraestructura
 */
class PingService : PingUseCasePort {
    override fun getPing(): Ping = Ping(message = "pong")

    override fun getPingWithMessage(message: String): Ping = Ping(message = "pong: $message")
}

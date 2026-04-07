package com.lgzarturo.springbootcourse.features.ping

/**
 * Servicio de dominio que implementa la lógica de negocio para Ping
 * Esta clase contiene la lógica de negocio pura, independiente de la infraestructura
 */
class PingService : PingUseCasePort {
    override fun getPing(): Ping = Ping(message = "pong")

    override fun getPingWithMessage(message: String): Ping = Ping(message = "pong: $message")
}

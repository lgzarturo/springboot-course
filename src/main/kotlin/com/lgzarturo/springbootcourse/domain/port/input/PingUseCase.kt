package com.lgzarturo.springbootcourse.domain.port.input

import com.lgzarturo.springbootcourse.domain.model.Ping

/**
 * Puerto de entrada (Use Case) para operaciones de Ping
 * Define el contrato de lo que se puede hacer con Ping
 */
interface PingUseCase {
    /**
     * Obtiene un ping simple
     * @return Ping con información básica
     */
    fun getPing(): Ping

    /**
     * Obtiene un ping personalizado con un mensaje
     * @param message Mensaje personalizado
     * @return Ping con el mensaje personalizado
     */
    fun getPingWithMessage(message: String): Ping
}

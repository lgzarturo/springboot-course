package com.lgzarturo.springbootcourse.config.metrics

import com.lgzarturo.springbootcourse.features.rooms.RoomJpaRepository
import com.lgzarturo.springbootcourse.features.rooms.RoomStatus
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import org.springframework.stereotype.Component

/**
 * Expone el gauge de habitaciones disponibles. El proveedor consulta el
 * repositorio en cada muestreo; aceptable a esta escala de inventario.
 */
@Component
class RoomMetrics(
    private val roomJpaRepository: RoomJpaRepository,
) : MeterBinder {
    override fun bindTo(registry: MeterRegistry) {
        Gauge
            .builder(GAUGE_NAME, roomJpaRepository, this::countAvailable)
            .description("Habitaciones disponibles")
            .register(registry)
    }

    private fun countAvailable(repository: RoomJpaRepository): Double =
        repository.findAll().count { it.status == RoomStatus.AVAILABLE }.toDouble()

    companion object {
        private const val GAUGE_NAME = "rooms.available"
    }
}

package com.lgzarturo.springbootcourse.hotels.adapters.persistence

import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity
import com.lgzarturo.springbootcourse.hotels.application.ports.output.HotelRepositoryPort
import com.lgzarturo.springbootcourse.hotels.domain.Hotel
import com.lgzarturo.springbootcourse.hotels.domain.HotelSearchCriteria
import com.lgzarturo.springbootcourse.rooms.adapters.persistence.RoomJpaRepository
import com.lgzarturo.springbootcourse.rooms.adapters.persistence.entity.RoomEntity
import com.lgzarturo.springbootcourse.rooms.domain.Room
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class HotelRoomJpaRepository(
    private val hotelJpaRepository: HotelJpaRepository,
    private val roomJpaRepository: RoomJpaRepository,
) : HotelRepositoryPort {
    override fun save(hotel: Hotel): Hotel {
        val entity = hotel.toEntity()
        val savedEntity = hotelJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: String): Hotel? = hotelJpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findAll(
        criteria: HotelSearchCriteria,
        page: Int,
        size: Int,
    ): Pair<List<Hotel>, Long> {
        val spec = buildSpecification(criteria)
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1), Sort.by("name"))
        val pageResult = hotelJpaRepository.findAll(spec, pageable)
        return pageResult.content.map { it.toDomain() } to pageResult.totalElements
    }

    override fun update(hotel: Hotel): Hotel? {
        if (!hotelJpaRepository.existsById(hotel.id)) return null
        val entity = hotel.toEntity()
        val savedEntity = hotelJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun deleteById(id: String): Boolean {
        if (!hotelJpaRepository.existsById(id)) return false
        hotelJpaRepository.deleteById(id)
        return true
    }

    override fun findRoomById(
        hotelId: String,
        roomId: String,
    ): Room? = roomJpaRepository.findByHotelIdAndId(hotelId, roomId)?.toDomain(hotelId)

    private fun buildSpecification(criteria: HotelSearchCriteria): Specification<HotelEntity> =
        Specification<HotelEntity> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            criteria.name?.let {
                predicates.add(cb.like(cb.lower(root.get("name")), "%${it.lowercase()}%"))
            }
            criteria.address?.let {
                predicates.add(cb.like(cb.lower(root.get("address")), "%${it.lowercase()}%"))
            }
            cb.and(*predicates.toTypedArray())
        }

    private fun Hotel.toEntity(): HotelEntity =
        HotelEntity(
            id = id.ifEmpty { null },
            name = name,
            address = address,
            rooms = rooms.map { it.toEntity() },
        )

    private fun HotelEntity.toDomain(): Hotel = Hotel(id ?: "", name, address, rooms.map { it.toDomain(id ?: "") })

    private fun Room.toEntity(): RoomEntity = RoomEntity(id, number, type, price, null)

    private fun RoomEntity.toDomain(hotelId: String): Room = Room(id, number, type, price, hotelId)
}

package com.lgzarturo.springbootcourse.features.hotels

import com.lgzarturo.springbootcourse.features.rooms.Room
import com.lgzarturo.springbootcourse.features.rooms.RoomEntity
import com.lgzarturo.springbootcourse.features.rooms.RoomJpaRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component

@Component
class HotelRoomJpaRepository(
    private val hotelJpaRepository: HotelJpaRepository,
    private val roomJpaRepository: RoomJpaRepository,
) : HotelRepositoryPort {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun save(hotel: Hotel): Hotel {
        val entityWithoutRooms =
            HotelEntity(
                id = hotel.id.ifEmpty { null },
                name = hotel.name,
                address = hotel.address,
                rooms = emptyList(),
            )
        val savedHotel = hotelJpaRepository.save(entityWithoutRooms)
        if (hotel.rooms.isNotEmpty()) {
            val savedRooms =
                hotel.rooms.map {
                    val roomEntity =
                        RoomEntity(
                            id = it.id.ifEmpty { null },
                            number = it.number,
                            type = it.type,
                            price = it.price,
                            hotel = savedHotel,
                        )
                    roomJpaRepository.save(roomEntity)
                }
            entityManager.flush()
            entityManager.clear()
            return hotelJpaRepository.findByIdWithRooms(savedHotel.id!!).map { it.toDomain() }.orElse(
                Hotel(
                    id = savedHotel.id!!,
                    name = savedHotel.name,
                    address = savedHotel.address,
                    rooms = savedRooms.map { it.toDomain(savedHotel.id!!) },
                ),
            )
        }
        return savedHotel.toDomain()
    }

    override fun findById(id: String): Hotel? =
        hotelJpaRepository.findByIdWithRooms(id).map { it.toDomain() }.orElse(null)

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

    private fun Hotel.toEntity(): HotelEntity {
        val hotelEntity =
            HotelEntity(
                id = id.ifEmpty { null },
                name = name,
                address = address,
                rooms = emptyList(),
            )
        val roomEntities = rooms.map { it.toEntity(hotelEntity) }
        return hotelEntity.copy(rooms = roomEntities)
    }

    private fun HotelEntity.toDomain(): Hotel = Hotel(id ?: "", name, address, rooms.map { it.toDomain(id ?: "") })

    private fun Room.toEntity(hotel: HotelEntity? = null): RoomEntity =
        RoomEntity(
            id.ifEmpty {
                null
            },
            number,
            type,
            price,
            hotel,
        )

    private fun RoomEntity.toDomain(hotelId: String): Room = Room(id ?: "", number, type, price, hotelId)
}

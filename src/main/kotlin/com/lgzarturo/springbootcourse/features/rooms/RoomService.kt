package com.lgzarturo.springbootcourse.features.rooms

import com.lgzarturo.springbootcourse.features.hotels.HotelEntity
import com.lgzarturo.springbootcourse.features.hotels.HotelJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomService(
    private val roomJpaRepository: RoomJpaRepository,
    private val hotelJpaRepository: HotelJpaRepository,
) {
    @Transactional
    fun createRoom(
        hotelId: String,
        room: Room,
    ): Room {
        val hotel =
            hotelJpaRepository.findById(hotelId).orElseThrow {
                IllegalArgumentException("Hotel no encontrado: $hotelId")
            }
        val saved = roomJpaRepository.save(room.toEntity(hotel))
        return saved.toDomain()
    }

    @Transactional(readOnly = true)
    fun getRoomById(
        hotelId: String,
        roomId: String,
    ): Room? = roomJpaRepository.findByHotelIdAndId(hotelId, roomId)?.toDomain()

    @Transactional(readOnly = true)
    fun getAllRooms(
        hotelId: String,
        criteria: RoomSearchCriteria,
        page: Int,
        size: Int,
    ): Pair<List<Room>, Long> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1), Sort.by("number"))
        val pageResult =
            when {
                criteria.status != null && criteria.theme != null ->
                    roomJpaRepository.findByHotelIdAndStatusAndTheme(
                        hotelId,
                        criteria.status,
                        criteria.theme,
                        pageable,
                    )
                criteria.status != null ->
                    roomJpaRepository.findByHotelIdAndStatus(hotelId, criteria.status, pageable)
                criteria.theme != null ->
                    roomJpaRepository.findByHotelIdAndTheme(hotelId, criteria.theme, pageable)
                else -> roomJpaRepository.findByHotelId(hotelId, pageable)
            }
        return pageResult.content.map { it.toDomain() } to pageResult.totalElements
    }

    @Transactional
    fun updateRoom(
        hotelId: String,
        roomId: String,
        room: Room,
    ): Room? {
        val existing = roomJpaRepository.findByHotelIdAndId(hotelId, roomId) ?: return null
        val updated =
            existing.copy(
                number = room.number,
                type = room.type,
                price = room.price,
                status = room.status,
                theme = room.theme,
                capacity = room.capacity,
                description = room.description,
            )
        return roomJpaRepository.save(updated).toDomain()
    }

    @Transactional
    fun deleteRoom(
        hotelId: String,
        roomId: String,
    ): Boolean {
        val existing = roomJpaRepository.findByHotelIdAndId(hotelId, roomId) ?: return false
        roomJpaRepository.delete(existing)
        return true
    }

    private fun Room.toEntity(hotel: HotelEntity): RoomEntity =
        RoomEntity(
            id = id.ifEmpty { null },
            number = number,
            type = type,
            price = price,
            hotel = hotel,
            status = status,
            theme = theme,
            capacity = capacity,
            description = description,
        )

    private fun RoomEntity.toDomain(): Room =
        Room(
            id = id ?: "",
            number = number,
            type = type,
            price = price,
            hotelId = hotel?.id ?: "",
            status = status,
            theme = theme,
            capacity = capacity,
            description = description,
        )
}

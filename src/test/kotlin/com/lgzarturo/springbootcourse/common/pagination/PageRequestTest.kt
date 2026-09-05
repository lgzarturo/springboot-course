package com.lgzarturo.springbootcourse.common.pagination

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

/**
 * PageRequest Tests
 * Pruebas unitarias para la conversión de PageRequest de dominio a Pageable de Spring
 */
@DisplayName("PageRequest Tests")
class PageRequestTest {
    @Test
    @DisplayName("Debería convertir a Pageable sin ordenamiento")
    fun `should convert to pageable without sorting`() {
        val pageRequest = PageRequest(page = 2, size = 10)

        val pageable = pageRequest.toPageable()

        assertEquals(2, pageable.pageNumber)
        assertEquals(10, pageable.pageSize)
        assertTrue(pageable.sort.isUnsorted)
    }

    @Test
    @DisplayName("Debería convertir a Pageable con ordenamiento ascendente")
    fun `should convert to pageable with ascending sort`() {
        val pageRequest =
            PageRequest(
                page = 0,
                size = 20,
                sort = listOf(SortOrder(property = "name", direction = SortOrder.Direction.ASC)),
            )

        val pageable = pageRequest.toPageable()

        assertEquals(0, pageable.pageNumber)
        assertEquals(20, pageable.pageSize)
        val order = pageable.sort.getOrderFor("name")
        assertEquals(Sort.Direction.ASC, order?.direction)
    }

    @Test
    @DisplayName("Debería convertir a Pageable con ordenamiento descendente")
    fun `should convert to pageable with descending sort`() {
        val pageRequest =
            PageRequest(
                page = 1,
                size = 5,
                sort = listOf(SortOrder(property = "createdAt", direction = SortOrder.Direction.DESC)),
            )

        val pageable = pageRequest.toPageable()

        assertEquals(1, pageable.pageNumber)
        assertEquals(5, pageable.pageSize)
        val order = pageable.sort.getOrderFor("createdAt")
        assertEquals(Sort.Direction.DESC, order?.direction)
    }

    @Test
    @DisplayName("Debería convertir a Pageable con múltiples ordenamientos")
    fun `should convert to pageable with multiple sort orders`() {
        val pageRequest =
            PageRequest(
                page = 0,
                size = 10,
                sort =
                    listOf(
                        SortOrder(property = "name", direction = SortOrder.Direction.ASC),
                        SortOrder(property = "id", direction = SortOrder.Direction.DESC),
                    ),
            )

        val pageable = pageRequest.toPageable()

        assertEquals(Sort.Direction.ASC, pageable.sort.getOrderFor("name")?.direction)
        assertEquals(Sort.Direction.DESC, pageable.sort.getOrderFor("id")?.direction)
    }
}

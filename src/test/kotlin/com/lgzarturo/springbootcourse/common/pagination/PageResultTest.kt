package com.lgzarturo.springbootcourse.common.pagination

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest as SpringPageRequest

/**
 * PageResult Tests
 * Pruebas unitarias para el resultado de paginación de dominio
 */
@DisplayName("PageResult Tests")
class PageResultTest {
    @Test
    @DisplayName("Debería crear un PageResult desde un Page de Spring")
    fun `should create PageResult from Spring Page`() {
        val content = listOf("a", "b", "c")
        val springPage = PageImpl(content, SpringPageRequest.of(1, 3), 10)

        val result = PageResult.fromPage(springPage)

        assertEquals(content, result.items)
        assertEquals(10, result.total)
        assertEquals(1, result.page)
        assertEquals(3, result.size)
        assertFalse(result.empty)
        assertEquals(4, result.pages)
    }

    @Test
    @DisplayName("Debería mapear una página vacía")
    fun `should map empty page`() {
        val springPage = PageImpl<String>(emptyList(), SpringPageRequest.of(0, 5), 0)

        val result = PageResult.fromPage(springPage)

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.total)
        assertTrue(result.empty)
        assertEquals(0, result.pages)
    }

    @Test
    @DisplayName("Debería calcular el número de páginas correctamente")
    fun `should calculate pages correctly`() {
        val result =
            PageResult(
                items = listOf("a", "b", "c"),
                total = 7,
                page = 0,
                size = 3,
            )

        assertEquals(3, result.pages)
        assertFalse(result.empty)
    }

    @Test
    @DisplayName("Debería retornar 0 páginas cuando size es 0")
    fun `should return zero pages when size is zero`() {
        val result =
            PageResult(
                items = emptyList<String>(),
                total = 0,
                page = 0,
                size = 0,
            )

        assertEquals(0, result.pages)
        assertTrue(result.empty)
    }
}

package com.lgzarturo.springbootcourse.features.users

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

@DataJpaTest
class UserJpaRepositoryTest(
    @Autowired private val userJpaRepository: UserJpaRepository,
) {
    @Test
    @DisplayName("debe_persistir_y_encontrar_usuario_por_correo_cuando_datos_son_validos")
    fun `should persist and find a user by email`() {
        userJpaRepository.save(userEntity())

        assertTrue(userJpaRepository.existsByEmail("ash@pokemon.com"))
    }

    @Test
    @DisplayName("debe_rechazar_insercion_cuando_correo_es_duplicado")
    fun `should reject duplicate emails`() {
        userJpaRepository.saveAndFlush(userEntity())

        assertThrows<DataIntegrityViolationException> {
            userJpaRepository.saveAndFlush(userEntity())
        }
    }

    private fun userEntity(): UserEntity =
        UserEntity(
            email = "ash@pokemon.com",
            passwordHash = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            firstName = "Ash",
            lastName = "Ketchum",
            phoneNumber = "+14155552671",
            role = UserRole.GUEST,
            isActive = true,
        )
}

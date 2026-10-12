package com.lgzarturo.springbootcourse.features.users

enum class UserRole(
    val authorities: Set<String>,
) {
    GUEST(
        setOf(
            UserAuthority.HOTELS_READ.value,
            UserAuthority.RESERVATIONS_CREATE_OWN.value,
        ),
    ), // Cliente que hace reservas
    STAFF(
        setOf(
            UserAuthority.HOTELS_READ.value,
            UserAuthority.HOTELS_MANAGE.value,
            UserAuthority.RESERVATIONS_MANAGE.value,
        ),
    ), // Personal del hotel
    ADMIN(
        setOf(
            UserAuthority.USERS_READ.value,
            UserAuthority.USERS_WRITE.value,
            UserAuthority.HOTELS_READ.value,
            UserAuthority.HOTELS_MANAGE.value,
            UserAuthority.RESERVATIONS_MANAGE.value,
        ),
    ), // Administrador con acceso completo
    ;

    fun allGrantedAuthorities(): List<String> = listOf("ROLE_$name") + authorities
}

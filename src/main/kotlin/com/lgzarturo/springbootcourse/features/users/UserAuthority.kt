package com.lgzarturo.springbootcourse.features.users

/**
 * Granular authorities for domain operations.
 */
enum class UserAuthority(val value: String) {
    USERS_READ("users:read"),
    USERS_WRITE("users:write"),
    HOTELS_READ("hotels:read"),
    HOTELS_MANAGE("hotels:manage"),
    RESERVATIONS_CREATE_OWN("reservations:create_own"),
    RESERVATIONS_MANAGE("reservations:manage"),
}

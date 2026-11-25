package com.lgzarturo.springbootcourse.config.security

import java.time.Instant
import java.util.UUID

/**
 * Claims verificados de un JWT de acceso. Solo existe tras validar firma,
 * emisor y expiración; el filtro decide la autenticación con estos datos.
 */
data class ParsedToken(
    val userId: Long,
    val email: String?,
    val authorities: List<String>,
    val tokenId: UUID,
    val tokenVersion: Long,
    val expiresAt: Instant,
)

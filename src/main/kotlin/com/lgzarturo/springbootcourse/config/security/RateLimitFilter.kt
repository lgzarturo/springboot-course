package com.lgzarturo.springbootcourse.config.security

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit

/**
 * Limita los intentos en endpoints de autenticación por IP (ventana deslizante).
 * Responde 429 con Retry-After al exceder el máximo.
 */
@Component
class RateLimitFilter(
    private val properties: RateLimitProperties,
) : OncePerRequestFilter() {
    private val hits: Cache<String, ArrayDeque<Long>> =
        Caffeine.newBuilder()
            .expireAfterWrite(properties.windowSeconds * 2, TimeUnit.SECONDS)
            .maximumSize(MAX_TRACKED_KEYS)
            .build()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (properties.enabled && isProtected(request) && !tryConsume(key(request))) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.setHeader(RETRY_AFTER_HEADER, properties.windowSeconds.toString())
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun isProtected(request: HttpServletRequest): Boolean =
        request.method == "POST" &&
            (request.requestURI.startsWith(AUTH_PATH_PREFIX) || request.requestURI == USERS_PATH)

    private fun key(request: HttpServletRequest): String {
        val forwarded = request.getHeader(FORWARDED_FOR_HEADER)?.split(",")?.firstOrNull()?.trim().orEmpty()
        val ip = forwarded.ifEmpty { request.remoteAddr ?: UNKNOWN_IP }
        return "$ip:${request.method}:${request.requestURI}"
    }

    private fun tryConsume(key: String): Boolean {
        val now = System.currentTimeMillis()
        val windowStart = now - properties.windowSeconds * MILLIS_PER_SECOND
        val deque = hits.get(key) { ArrayDeque() }
        synchronized(deque) {
            while (deque.isNotEmpty() && deque.first() <= windowStart) {
                deque.removeFirst()
            }
            if (deque.size >= properties.perMinute) {
                return false
            }
            deque.addLast(now)
            return true
        }
    }

    companion object {
        private const val AUTH_PATH_PREFIX = "/api/v1/auth/"
        private const val USERS_PATH = "/api/v1/users"
        private const val RETRY_AFTER_HEADER = "Retry-After"
        private const val FORWARDED_FOR_HEADER = "X-Forwarded-For"
        private const val UNKNOWN_IP = "unknown"
        private const val MAX_TRACKED_KEYS = 10_000L
        private const val MILLIS_PER_SECOND = 1000L
    }
}

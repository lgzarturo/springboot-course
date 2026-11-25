package com.lgzarturo.springbootcourse.config.security

import com.lgzarturo.springbootcourse.features.auth.TokenRevocationPort
import com.lgzarturo.springbootcourse.features.users.UserDomainRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val tokenRevocationPort: TokenRevocationPort,
    private val userRepository: UserDomainRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header != null && header.startsWith(BEARER_PREFIX) && SecurityContextHolder.getContext().authentication == null) {
            jwtService.parseToken(header.substring(BEARER_PREFIX.length))?.let { parsed ->
                authenticate(parsed)?.let { SecurityContextHolder.getContext().authentication = it }
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun authenticate(parsed: ParsedToken): Authentication? {
        if (tokenRevocationPort.isRevoked(parsed.tokenId)) {
            return null
        }
        val user = userRepository.findById(parsed.userId) ?: return null
        if (!user.isActive || user.tokenVersion != parsed.tokenVersion) {
            return null
        }
        val authorities = parsed.authorities.map(::SimpleGrantedAuthority)
        return UsernamePasswordAuthenticationToken(parsed.userId, null, authorities).also { it.details = parsed }
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}

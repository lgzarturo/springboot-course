package com.lgzarturo.springbootcourse.support

import com.lgzarturo.springbootcourse.config.security.JwtService
import com.lgzarturo.springbootcourse.features.users.UserRole
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@TestConfiguration
class SecurityTestConfig {
    // Los slices @WebMvcTest incluyen el filtro JWT real pero no la cadena de
    // seguridad ni JwtService: se sustituye por un mock que deja pasar todo.
    @Bean
    fun jwtService(): JwtService = mockk(relaxed = true)
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@WithSecurityContext(factory = WithMockDomainUserSecurityContextFactory::class)
annotation class WithMockDomainUser(
    val userId: Long = 1L,
    val username: String = "trainer@pokemon.com",
    val role: UserRole = UserRole.GUEST,
)

class WithMockDomainUserSecurityContextFactory : WithSecurityContextFactory<WithMockDomainUser> {
    override fun createSecurityContext(annotation: WithMockDomainUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val authorities = annotation.role.allGrantedAuthorities().map { SimpleGrantedAuthority(it) }
        val principal = org.springframework.security.core.userdetails.User(
            annotation.username,
            "password",
            authorities,
        )
        val auth = UsernamePasswordAuthenticationToken(principal, "password", authorities)
        context.authentication = auth
        return context
    }
}

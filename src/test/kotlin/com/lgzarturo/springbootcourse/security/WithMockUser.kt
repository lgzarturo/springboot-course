package com.lgzarturo.springbootcourse.security

import com.lgzarturo.springbootcourse.features.users.UserRole
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

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
        val principal =
            org.springframework.security.core.userdetails.User(
                annotation.username,
                "password",
                authorities,
            )
        val auth = UsernamePasswordAuthenticationToken(principal, "password", authorities)
        context.authentication = auth
        return context
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@WithSecurityContext(factory = WithMockUserSecurityContextFactory::class)
annotation class WithMockUser(
    val userId: String = "1",
    val username: String = "test-user@pokemon.com",
    val roles: Array<String> = ["USER"],
    val authorities: Array<String> = [],
)

class WithMockUserSecurityContextFactory : WithSecurityContextFactory<WithMockUser> {
    override fun createSecurityContext(annotation: WithMockUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val grantedAuthorities =
            (
                annotation.roles.map { if (it.startsWith("ROLE_")) it else "ROLE_$it" } +
                    annotation.authorities
            ).distinct().map { SimpleGrantedAuthority(it) }

        val principal =
            org.springframework.security.core.userdetails.User(
                annotation.username,
                "password",
                grantedAuthorities,
            )
        val auth = UsernamePasswordAuthenticationToken(principal, "password", grantedAuthorities)
        context.authentication = auth
        return context
    }
}

package com.lgzarturo.springbootcourse.config.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authenticationEntryPoint: AuthenticationEntryPoint,
    private val accessDeniedHandler: AccessDeniedHandler,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                auth.requestMatchers(*PUBLIC_PATHS).permitAll()
                auth.requestMatchers(HttpMethod.GET, "/api/v1/hotels/**").hasAnyAuthority("hotels:read", "ROLE_GUEST", "ROLE_STAFF", "ROLE_ADMIN")
                auth.requestMatchers(HttpMethod.GET, "/api/v1/users/*").authenticated()
                auth.requestMatchers("/api/v1/hotels/**").hasAnyAuthority("hotels:manage", "ROLE_STAFF", "ROLE_ADMIN")
                auth.requestMatchers(HttpMethod.GET, "/api/v1/users").hasAnyAuthority("users:read", "ROLE_ADMIN")
                auth.requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyAuthority("users:write", "ROLE_ADMIN")
                auth.requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasAnyAuthority("users:write", "ROLE_ADMIN")
                auth.anyRequest().authenticated()
            }.exceptionHandling { ex ->
                ex.authenticationEntryPoint(authenticationEntryPoint)
                ex.accessDeniedHandler(accessDeniedHandler)
            }.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    companion object {
        private val PUBLIC_PATHS =
            arrayOf(
                "/actuator/health",
                "/actuator/info",
                "/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/error",
            )
    }
}

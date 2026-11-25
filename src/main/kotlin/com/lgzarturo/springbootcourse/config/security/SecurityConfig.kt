package com.lgzarturo.springbootcourse.config.security

import org.springframework.beans.factory.annotation.Value
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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties::class, RateLimitProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val rateLimitFilter: RateLimitFilter,
    private val authenticationEntryPoint: AuthenticationEntryPoint,
    private val accessDeniedHandler: AccessDeniedHandler,
    @Value("\${app.security.csp-policy:default-src 'self'}")
    private val cspPolicy: String,
    @Value("\${app.security.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private val allowedOrigins: List<String>,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .headers { headers ->
                headers.frameOptions { frame -> frame.deny() }
                headers.contentTypeOptions { }
                headers.httpStrictTransportSecurity { hsts -> hsts.includeSubDomains(true).preload(true) }
                headers.referrerPolicy { referrer ->
                    referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                }
                headers.contentSecurityPolicy { csp -> csp.policyDirectives(cspPolicy) }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                auth.requestMatchers(*PUBLIC_PATHS).permitAll()
                auth
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/hotels/**",
                    ).hasAnyAuthority("hotels:read", "ROLE_GUEST", "ROLE_STAFF", "ROLE_ADMIN")
                auth.requestMatchers(HttpMethod.GET, "/api/v1/users/*").authenticated()
                auth.requestMatchers("/api/v1/hotels/**").hasAnyAuthority("hotels:manage", "ROLE_STAFF", "ROLE_ADMIN")
                auth.requestMatchers(HttpMethod.GET, "/api/v1/users").hasAnyAuthority("users:read", "ROLE_ADMIN")
                auth.requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyAuthority("users:write", "ROLE_ADMIN")
                auth.requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasAnyAuthority("users:write", "ROLE_ADMIN")
                auth.requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                auth.anyRequest().authenticated()
            }.exceptionHandling { ex ->
                ex.authenticationEntryPoint(authenticationEntryPoint)
                ex.accessDeniedHandler(accessDeniedHandler)
            }.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration =
            CorsConfiguration().apply {
                allowedOrigins = this@SecurityConfig.allowedOrigins
                allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
                allowedHeaders = listOf("Authorization", "Content-Type")
                allowCredentials = true
                maxAge = CORS_MAX_AGE_SECONDS
            }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", configuration)
        }
    }

    companion object {
        private const val CORS_MAX_AGE_SECONDS = 3600L
        private val PUBLIC_PATHS =
            arrayOf(
                "/actuator/health",
                "/actuator/health/**",
                "/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/error",
            )
    }
}

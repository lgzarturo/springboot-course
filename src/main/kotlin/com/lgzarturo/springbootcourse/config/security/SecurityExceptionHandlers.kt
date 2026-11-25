package com.lgzarturo.springbootcourse.config.security

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    private val logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint::class.java)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        logger.warn("Unauthorized access attempt to path={}", request.requestURI)
        writeError(objectMapper, request, response, HttpStatus.UNAUTHORIZED)
    }
}

@Component
class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    private val logger = LoggerFactory.getLogger(JwtAccessDeniedHandler::class.java)

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        logger.warn("Access denied to path={}", request.requestURI)
        writeError(objectMapper, request, response, HttpStatus.FORBIDDEN)
    }
}

private fun writeError(
    objectMapper: ObjectMapper,
    request: HttpServletRequest,
    response: HttpServletResponse,
    status: HttpStatus,
) {
    response.status = status.value()
    response.contentType = MediaType.APPLICATION_JSON_VALUE
    objectMapper.writeValue(
        response.outputStream,
        ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = status.reasonPhrase,
            path = request.requestURI,
        ),
    )
}

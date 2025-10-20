package com.lgzarturo.springbootcourse.infrastructure.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * Manejador global de excepciones para toda la aplicación
 * Captura y formatea las excepciones de manera consistente
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Maneja excepciones de validación de argumentos
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Validation error: ${ex.message}", ex)

        val errors =
            ex.bindingResult.fieldErrors.map { error ->
                "${error.field}: ${error.defaultMessage}"
            }

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Error",
                message = "Los datos proporcionados no son válidos",
                path = request.requestURI,
                details = errors,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Maneja excepciones de tipo de argumento incorrecto
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Type mismatch error: ${ex.message}", ex)

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Type Mismatch",
                message = "El tipo de dato proporcionado no es válido: ${ex.name}",
                path = request.requestURI,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Maneja excepciones genéricas no capturadas
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: ${ex.message}", ex)

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = "Ha ocurrido un error inesperado en el servidor",
                path = request.requestURI,
            )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

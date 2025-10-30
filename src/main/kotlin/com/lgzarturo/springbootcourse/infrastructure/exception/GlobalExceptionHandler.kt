package com.lgzarturo.springbootcourse.infrastructure.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
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
                error = "Error de validación",
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

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupportedException(
        ex: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = handleSpecificStatusCodeException(ex, request, HttpStatus.UNSUPPORTED_MEDIA_TYPE)

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = handleSpecificStatusCodeException(ex, request, HttpStatus.CONFLICT)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = handleSpecificStatusCodeException(ex, request, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.associate {
            val propertyPath = it.propertyPath.toString()
            val fieldName = propertyPath.substringAfterLast('.')
            fieldName to it.message
        }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Error de validación en parámetros",
            path = request.requestURI,
            errors = errors
        )
        return ResponseEntity.badRequest().body(errorResponse)
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

    private fun handleSpecificStatusCodeException(
        ex: Exception,
        request: HttpServletRequest,
        status: HttpStatus,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: ${ex.message}", ex)
        val (error, message) =
            when (status) {
                HttpStatus.BAD_REQUEST -> "Bad Request" to "La solicitud no se pudo entender o fue malformada"
                HttpStatus.UNAUTHORIZED -> "Unauthorized" to "No se ha proporcionado autenticación válida"
                HttpStatus.FORBIDDEN -> "Forbidden" to "No tiene permiso para acceder a este recurso"
                HttpStatus.NOT_FOUND -> "Not Found" to "El recurso solicitado no fue encontrado"
                HttpStatus.CONFLICT -> "Conflict" to "La solicitud no pudo ser completada debido a un conflicto"
                HttpStatus.UNSUPPORTED_MEDIA_TYPE ->
                    "Unsupported Media Type" to
                        "El tipo de medio de la solicitud no es compatible"
                else -> "Internal Server Error" to "Ha ocurrido un error inesperado en el servidor"
            }

        val errorResponse =
            ErrorResponse(
                status = status.value(),
                error = error,
                message = message,
                path = request.requestURI,
            )

        return ResponseEntity.status(status).body(errorResponse)
    }
}

package ru.digitalpaper.server.controller.base

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.exception.CustomException

@RestControllerAdvice
class GlobalExceptionHandler {


    private val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)


    @ExceptionHandler(CustomException::class)
    fun handleCustomException(
        e: CustomException
    ): ResponseEntity<ErrorResponse> {

        log.warn(
            "Application exception: code={}, message={}",
            e.code,
            e.message,
            e
        )

        return ResponseEntity
            .status(e.code)
            .body(
                ErrorResponse(
                    e.code,
                    e.message ?: "Непредвиденная ошибка",
                    emptyMap()
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }

        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponse(
                    code = HttpStatus.BAD_REQUEST.value(),
                    message = "Ошибка валидации",
                    errors
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        ex: Exception,
    ): ResponseEntity<ErrorResponse> {
        log.error("Unexpected exception", ex)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "Ошибка сервера",
                    errors = emptyMap()
                )
            )
    }

}

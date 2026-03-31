package ru.digitalpaper.server.exception.common

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.exception.CustomException
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.util.*

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(
        e: CustomException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val traceId = request.getHeader("X-Trace-Id") ?: UUID.randomUUID().toString()

        logger.error(
            ServerLogUtil.error(
                "GlobalExceptionHandler.handleCustomException",
                traceId,
                "ERROR on response: ${e.message}",
                e
            )
        )

        return ResponseEntity
            .status(e.code)
            .header("X-Trace-Id", traceId)
            .body(
                ErrorResponse(
                    e.code,
                    message = e.message ?: "Непридвиденная ошибка",
                    reason = e.message ?: "Непридвиденная ошибка",
                    errors = emptyMap()
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val traceId = request.getHeader("X-Trace-Id") ?: UUID.randomUUID().toString()

        logger.error(
            ServerLogUtil.error(
                "GlobalExceptionHandler.handleException",
                traceId,
                "ERROR on response: ${e.message}",
                e
            )
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("X-Trace-Id", traceId)
            .body(
                ErrorResponse(
                    code = 500,
                    message = e.message ?: "Непредвиденная ошибка",
                    reason = "Ошибка сервера",
                    errors = emptyMap()
                )
            )
    }
}
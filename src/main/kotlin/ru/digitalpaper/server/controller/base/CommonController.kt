package ru.digitalpaper.server.controller.base

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import ru.digitalpaper.server.dto.internal.PagedRequest
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.exception.CustomException
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.nio.charset.StandardCharsets
import java.util.*

open class CommonController {

    companion object {
        val logger: Logger = LoggerFactory.getLogger("grayLog")
    }

    fun handleRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        traceId: UUID,
        doIt: (rs: RequestSatellites) -> Response
    ): Response {
        val bearerToken = getBearerToken(request)
        val userTimezone = request.getHeader("X-User-Timezone")

        val requestSatellites = RequestSatellites(traceId.toString()).apply {
            bearerToken?.let { attributes["bearerToken"] = it }
            request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)
                ?.let { attributes[HttpHeaders.ACCEPT_LANGUAGE] = it }
            userTimezone?.let { attributes["userTimezone"] = it }
        }

        val result = try {
            doIt.invoke(requestSatellites)
        } catch (e: CustomException) {
            logger.error(
                ServerLogUtil.error(
                    "CommonController.handleRequest",
                    traceId.toString(),
                    "ERROR on response: ${e.message}",
                    e
                )
            )
            ErrorResponse(
                code = e.code,
                message = e.message ?: "Непредвиденная ошибка",
                reason = e.message ?: "Непредвиденная ошибка",
                errors = emptyMap()
            )
        } catch (e: Exception) {
            logger.error(
                ServerLogUtil.error(
                    "CommonController.handleRequest",
                    traceId.toString(),
                    "ERROR on response: ${e.message}",
                    e
                )
            )
            ErrorResponse(
                code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = e.message ?: "Непредвиденная ошибка",
                reason = e.message ?: "Ошибка сервера",
                mapOf()
            )
        }

        if (result is ErrorResponse) {
            response.status = result.code
        }

        response.addHeader("X-Trace-Id", traceId.toString())
        return result
    }

    fun handleFileRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        traceId: UUID,
        doIt: (rs: RequestSatellites) -> Unit
    ) {
        val bearerToken = getBearerToken(request)
        val userTimezone = request.getHeader("X-User-Timezone")

        val requestSatellites = RequestSatellites(traceId.toString()).apply {
            bearerToken?.let { attributes["bearerToken"] = it }

            request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)
                ?.let { attributes[HttpHeaders.ACCEPT_LANGUAGE] = it }

            userTimezone?.let { attributes["userTimezone"] = it }
        }

        response.addHeader("X-Trace-Id", traceId.toString())

        try {
            doIt.invoke(requestSatellites)
        } catch (e: CustomException) {
            logger.error(
                ServerLogUtil.error(
                    "CommonController.handleFileRequest",
                    traceId.toString(),
                    "ERROR on file response: ${e.message}",
                    e
                )
            )

            writeErrorResponse(
                response = response,
                error = ErrorResponse(
                    code = e.code,
                    message = e.message ?: "Непредвиденная ошибка",
                    reason = e.message ?: "Непредвиденная ошибка",
                    errors = emptyMap()
                )
            )
        } catch (e: Exception) {
            logger.error(
                ServerLogUtil.error(
                    "CommonController.handleFileRequest",
                    traceId.toString(),
                    "ERROR on file response: ${e.message}",
                    e
                )
            )

            writeErrorResponse(
                response = response,
                error = ErrorResponse(
                    code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = e.message ?: "Непредвиденная ошибка",
                    reason = "Ошибка сервера",
                    errors = emptyMap()
                )
            )
        }
    }

    fun getTraceIdOrGenerate(request: HttpServletRequest): UUID {
        return if (request.getHeader("X-Trace-Id") != null)
            UUID.fromString(request.getHeader("X-Trace-Id").toString())
        else
            UUID.randomUUID()
    }

    private fun getBearerToken(request: HttpServletRequest): String? {
        var bearerToken: String? = null

        val authorizationHeader = request.getHeader("Authorization")

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            bearerToken = authorizationHeader.substring(7)
        }

        return bearerToken
    }

    fun buildPagedRequest(
        page: Int,
        size: Int,
        sortField: String,
        sortDirection: String
    ): PagedRequest =
        PagedRequest(page, size, sortField, sortDirection)

    private fun writeErrorResponse(
        response: HttpServletResponse,
        error: ErrorResponse
    ) {
        if (response.isCommitted) {
            return
        }

        response.resetBuffer()
        response.status = error.code
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()

        val objectMapper = ObjectMapper()
            .registerKotlinModule()

        response.writer.write(objectMapper.writeValueAsString(error))
        response.writer.flush()
    }
}

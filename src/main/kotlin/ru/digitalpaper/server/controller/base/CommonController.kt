package ru.digitalpaper.server.controller.base

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.exception.CustomException
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.util.UUID

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
}
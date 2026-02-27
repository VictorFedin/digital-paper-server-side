package ru.digitalpaper.server.controller.user

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.controller.base.CommonController
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.service.UserService
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil

@RestController
@RequestMapping(value = ["/api/v1/user"])
class UserController(
    private val userService: UserService
) : CommonController() {

    @Operation(
        summary = "Get user profile",
        description = "Returns user profile by token"
    )
    @GetMapping(value = ["/profile"])
    fun getUserProfile(
        @AuthenticationPrincipal payload: UserPayload,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "UserController.getUserProfile",
                traceId.toString(),
                "Enter",
                Pair("payload", payload)
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            userService.getUserProfile(payload, rs)
        }
    }

    @Operation(
        summary = "Сохранить аватар пользователя",
        description = "Вовзращает аватар текущего пользователя"
    )
    @PostMapping(value = ["/avatar"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun saveUserAvatar(
        @AuthenticationPrincipal payload: UserPayload,
        @RequestBody file: MultipartFile,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "UserController.saveUserAvatar",
                traceId.toString(),
                "Enter"
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            userService.saveUserAvatar(file, payload, rs)
        }
    }
}
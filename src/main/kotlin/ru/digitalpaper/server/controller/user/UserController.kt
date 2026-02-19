package ru.digitalpaper.server.controller.user

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.digitalpaper.server.controller.base.CommonController
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.service.UserService
import ru.digitalpaper.server.util.common.RequestSatellites

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
        @AuthenticationPrincipal user: UserPayload,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info("")

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            userService.getUserProfile(user, rs)
        }
    }
}
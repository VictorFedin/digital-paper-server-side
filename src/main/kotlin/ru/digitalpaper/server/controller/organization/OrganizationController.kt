package ru.digitalpaper.server.controller.organization

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.digitalpaper.server.controller.base.CommonController
import ru.digitalpaper.server.dto.request.organization.AddOrganizationRequest
import ru.digitalpaper.server.dto.request.organization.AddUserToOrganizationRequest
import ru.digitalpaper.server.dto.request.organization.UpdateOrganizationRequest
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.service.OrganizationService
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.util.UUID

@RestController
@RequestMapping(value = ["/api/v1/organizations"])
@Validated
class OrganizationController(
    private val organizationService: OrganizationService
) : CommonController() {

    @Operation(
        summary = "Получить детали организации",
        description = "Возвращает детали организации по айди"
    )
    @GetMapping(value = ["/{id}"])
    fun getOrganizationDetails(
        @PathVariable id: UUID,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "OrganizationController.getOrganizationDetails",
                traceId.toString(),
                "Enter",
                Pair("id", "$id")
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            organizationService.getOrganizationDetails(id, rs)
        }
    }

    @Operation(
        summary = "Получить список организаций пользователя",
        description = "Возвращает список организаций текущего пользователя"
    )
    @GetMapping(value = ["/my"])
    fun getMyOrganizationsList(
        @AuthenticationPrincipal payload: UserPayload,
        @RequestParam page: Int = 1,
        @RequestParam size: Int = 10,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "OrganizationController.getMyOrganizationsList",
                traceId.toString(),
                "Enter",
                mapOf(
                    "page" to page.toString(),
                    "size" to size.toString()
                )
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            organizationService.getMyOrganizationsList(payload, page, size, rs)
        }
    }

    @Operation(
        summary = "Создать новую организацию",
        description = "Возвращает детали организации"
    )
    @PostMapping(value = [""])
    fun addOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @Valid @RequestBody addOrganizationRequest: AddOrganizationRequest,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "OrganizationController.addOrganization",
                traceId.toString(),
                "Enter",
                Pair("request", addOrganizationRequest)
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            organizationService.addOrganization(addOrganizationRequest, payload, rs)
        }
    }

    @Operation(
        summary = "Обновить детали организации",
        description = "Возвращает детали организации"
    )
    @PutMapping(value = ["/{id}"])
    fun updateOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
        @Valid @RequestBody updateOrganizationRequest: UpdateOrganizationRequest,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "OrganizationController.updateOrganization",
                traceId.toString(),
                "Enter",
                Pair("id", "$id"),
                Pair("request", "$updateOrganizationRequest")
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            organizationService.updateOrganization(id, updateOrganizationRequest, payload, rs)
        }
    }

    @Operation(
        summary = "Удалить организацию",
        description = "Возращает результат удаления организации"
    )
    @DeleteMapping(value = ["/{id}"])
    fun deleteOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "OrganizationController.deleteOrganization",
                traceId.toString(),
                "Enter",
                Pair("id", "$id")
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            organizationService.deleteOrganization(id, payload, rs)
        }
    }

    @Operation(
        summary = "Добавить пользователя к организации",
        description = "Возвращает результат добавления пользователя"
    )
    @PostMapping(value = ["/{id}/users/add"])
    fun addUserToOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
        @Valid @RequestBody addUserToOrganizationRequest: AddUserToOrganizationRequest,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "OrganizationController.addUserToOrganization",
                traceId.toString(),
                "Enter",
                Pair("request", addUserToOrganizationRequest)
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            organizationService.addUserToOrganization(payload, id, addUserToOrganizationRequest, rs)
        }
    }
}
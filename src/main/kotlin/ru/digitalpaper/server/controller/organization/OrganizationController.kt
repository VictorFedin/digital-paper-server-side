package ru.digitalpaper.server.controller.organization

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.digitalpaper.server.controller.base.CommonController
import ru.digitalpaper.server.dto.request.organization.AddOrganizationRequest
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.service.OrganizationService
import ru.digitalpaper.server.util.common.RequestSatellites
import java.util.UUID

@RestController
@RequestMapping(value = ["/api/v1/organization"])
@Validated
class OrganizationController(
    private val organizationService: OrganizationService
) : CommonController() {

    @Operation(
        summary = "Get organization details",
        description = "Returns organization details by id"
    )
    @GetMapping(value = ["/{id}"])
    fun getOrganizationDetails(
        @PathVariable id: UUID,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info("")

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            organizationService.getOrganizationDetails(id, rs)
        }
    }

    @Operation(
        summary = "Get user's organization list",
        description = "Returns user's organization list"
    )
    @GetMapping(value = ["/my"])
    fun getMyOrganizationsList(
        @AuthenticationPrincipal payload: UserPayload,
        @RequestParam page: Int = 1,
        @RequestParam size: Int = 10,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info("")

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            Response() // TODO
        }
    }

    @Operation(
        summary = "Create new organization",
        description = "Returns created organization"
    )
    @PostMapping(value = [""])
    fun addOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @Valid @RequestBody addOrganizationRequest: AddOrganizationRequest,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        println(addOrganizationRequest)

        logger.info("")

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            organizationService.addOrganization(addOrganizationRequest, payload, rs)
        }
    }
}
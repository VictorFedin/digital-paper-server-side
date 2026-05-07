package ru.digitalpaper.server.controller.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.controller.base.CommonController
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.service.TemplateService
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil

@RestController
@RequestMapping(value = ["/api/v1/templates"])
@Validated
class DocumentTemplateController(private val templateService: TemplateService) : CommonController() {

    @Operation(
        summary = "Загрузить шаблон документа",
        description = "Возращает шаблон документа"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadTemplate(
        @AuthenticationPrincipal payload: UserPayload,
        @RequestPart("file") file: MultipartFile,
        @RequestParam("name") name: String,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "DocumentTemplateController.uploadTemplate",
                traceId.toString(),
                "Enter"
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            templateService.upload(payload, file, name, rs)
        }
    }
}

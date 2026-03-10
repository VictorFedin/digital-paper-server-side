package ru.digitalpaper.server.controller.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.digitalpaper.server.controller.base.CommonController
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.service.DocumentService
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil

@RestController
@RequestMapping(value = ["/api/v1/documents"])
@Validated
class DocumentController(
    private val documentService: DocumentService,
) : CommonController() {

    @Operation(
        summary = "Получить список документов",
        description = "Возвращает список документов"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = DocumentsPagedListResponse::class)
                )]
            ),
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
    @GetMapping(value = ["/list"])
    fun getDocumentsPagedList(
        @RequestParam page: Int = 1,
        @RequestParam size: Int = 10,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "DocumentController.getDocumentsPagedList",
                traceId.toString(),
                "Enter",
                Pair("page", "$page"),
                Pair("size", "$size")
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            documentService.getDocumentsPagedList(page, size, rs)
        }
    }
}
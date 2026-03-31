package ru.digitalpaper.server.controller.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.controller.base.CommonController
import ru.digitalpaper.server.dto.request.document.CreateDocumentRequest
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.model.document.holder.DocumentType
import ru.digitalpaper.server.service.DocumentService
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.util.*

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
        @AuthenticationPrincipal payload: UserPayload,
        @RequestParam page: Int = 1,
        @RequestParam size: Int = 10,
        @RequestParam sortField: String = "created_at",
        @RequestParam sortDirection: Sort.Direction = Sort.Direction.DESC,
        @RequestParam type: DocumentType? = null,
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
            documentService.getDocumentsPagedList(page, size, payload, sortField, sortDirection, type, rs)
        }
    }

    @Operation(
        summary = "Загрузить документ",
        description = "Возвращает детали созданного документа"
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
    @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadDocument(
        @AuthenticationPrincipal payload: UserPayload,
        @RequestPart("file") file: MultipartFile,
        @RequestPart("request") createDocumentRequest: CreateDocumentRequest,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "DocumentController.uploadDocument",
                traceId.toString(),
                "Enter",
                mapOf("request" to "$createDocumentRequest")
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            documentService.uploadDocument(payload, createDocumentRequest, file, rs)
        }
    }

    @Operation(
        summary = "Скачать документ",
        description = "Возвращает документ как файл по уникальному идентификатору"
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
    @GetMapping(value = ["/{id}/download"])
    fun downloadDocument(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
        request: HttpServletRequest, response: HttpServletResponse
    ) {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "DocumentController.downloadDocument",
                traceId.toString(),
                "Enter",
                mapOf("id" to "$id")
            )
        )

//        handleRequest(request, response, traceId) { rs: RequestSatellites ->
//            documentService.downloadDocument(id, payload, rs)
//        }
    }

}
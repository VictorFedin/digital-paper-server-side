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
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.document.DocumentResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.model.document.holder.DocumentStatus
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
        @RequestParam sortField: String = "createdAt",
        @RequestParam sortDirection: Sort.Direction = Sort.Direction.DESC,
        @RequestParam type: DocumentType? = null,
        @RequestParam search: String? = null,
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
            documentService.getDocumentsPagedList(page, size, payload, sortField, sortDirection, type, search, rs)
        }
    }

    @Operation(
        summary = "Получить корзину документов",
        description = "Возвращает корзину документов"
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
    @GetMapping(value = ["/deleted"])
    fun getDeletedDocumentsPagedList(
        @AuthenticationPrincipal payload: UserPayload,
        @RequestParam page: Int = 1,
        @RequestParam size: Int = 10,
        @RequestParam sortField: String = "createdAt",
        @RequestParam sortDirection: Sort.Direction = Sort.Direction.DESC,
        @RequestParam type: DocumentType? = null,
        @RequestParam search: String? = null,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "DocumentController.getDeletedDocumentsPagedList",
                traceId.toString(),
                "Enter",
                Pair("page", "$page"),
                Pair("size", "$size")
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            documentService.getDeletedDocumentsPagedList(
                page,
                size,
                payload,
                sortField,
                sortDirection,
                type,
                search,
                rs
            )
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
                    schema = Schema(implementation = DocumentResponse::class)
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
        @RequestParam("name") name: String,
        @RequestParam("type") type: DocumentType,
        @RequestParam("folderId") folderId: UUID? = null,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "DocumentController.uploadDocument",
                traceId.toString(),
                "Enter"
            )
        )

        val createDocumentRequest = CreateDocumentRequest(
            name = name,
            type = type,
            folderId = folderId
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

        handleFileRequest(request, response, traceId) { rs: RequestSatellites ->
            documentService.downloadDocument(id, payload, response, rs)
        }
    }

    @Operation(
        summary = "Удалить документ",
        description = "Возвращает результат удаления документа"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
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
    @PostMapping(value = ["/{id}/delete"])
    fun deleteDocument(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "DocumentController.deleteDocument",
                traceId.toString(),
                "Enter"
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            documentService.changeDocumentStatus(id, DocumentStatus.DELETED, payload, rs)
        }
    }

    @Operation(
        summary = "Восстановить документ",
        description = "Возвращает результат восстановления документа"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
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
    @PostMapping(value = ["/{id}/restore"])
    fun restoreDocument(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
        request: HttpServletRequest, response: HttpServletResponse
    ): Response {
        val traceId = getTraceIdOrGenerate(request)

        logger.info(
            ServerLogUtil.info(
                "DocumentController.restoreDocument",
                traceId.toString(),
                "Enter"
            )
        )

        return handleRequest(request, response, traceId) { rs: RequestSatellites ->
            documentService.changeDocumentStatus(id, DocumentStatus.CREATED, payload, rs)
        }
    }

}

package ru.digitalpaper.server.controller.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.Sort
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.dto.request.document.CreateDocumentRequest
import ru.digitalpaper.server.dto.request.document.DocumentListRequest
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.document.DocumentResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.model.document.holder.DocumentType
import ru.digitalpaper.server.service.DocumentService
import java.nio.charset.StandardCharsets
import java.util.*

@RestController
@RequestMapping(value = ["/api/v1/documents"])
@Validated
class DocumentController(
    private val documentService: DocumentService,
) {

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
        @RequestParam search: String? = null
    ): DocumentsPagedListResponse {
        val request = DocumentListRequest(
            page,
            size,
            sortField,
            sortDirection,
            type,
            search
        )
        return documentService.getDocumentsPagedList(payload, request)
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
    ): DocumentsPagedListResponse {
        val request = DocumentListRequest(
            page,
            size,
            sortField,
            sortDirection,
            type,
            search
        )

        return documentService.getDeletedDocumentsPagedList(
            payload,
            request
        )
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
    ): DocumentResponse {
        val createDocumentRequest = CreateDocumentRequest(
            name = name,
            type = type,
            folderId = folderId
        )

        return documentService.uploadDocument(payload, createDocumentRequest, file)
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
    ): ResponseEntity<InputStreamResource> {
        val file = documentService.downloadDocument(id, payload)

        val builder = ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(file.contentType))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(file.filename, StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )

        file.contentLength?.let(builder::contentLength)

        return builder.body(file.resource)
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
    ): MessageResponse {
        return documentService.deleteDocument(id, payload)
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
    ): MessageResponse {
        return documentService.restoreDocument(id, payload)
    }

}

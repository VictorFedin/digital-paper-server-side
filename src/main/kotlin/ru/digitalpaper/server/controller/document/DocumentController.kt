package ru.digitalpaper.server.controller.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
import ru.digitalpaper.server.controller.base.CommonApiResponses
import ru.digitalpaper.server.dto.request.document.CreateDocumentRequest
import ru.digitalpaper.server.dto.request.document.ChangeDocumentStatusRequest
import ru.digitalpaper.server.dto.request.document.DocumentListRequest
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.document.DocumentResponse
import ru.digitalpaper.server.dto.response.document.DocumentStatusTransitionsResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.model.document.holder.DocumentType
import ru.digitalpaper.server.service.DocumentService
import java.nio.charset.StandardCharsets
import java.util.*

@RestController
@RequestMapping(value = ["/api/v1/documents"])
@Validated
@CommonApiResponses
@Tag(name = "Документы", description = "Загрузка, получение и управление жизненным циклом документов")
class DocumentController(
    private val documentService: DocumentService,
) {

    @Operation(
        summary = "Получить список документов",
        description = "Возвращает страницу активных документов текущей организации с фильтрацией и сортировкой"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Страница документов получена",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentsPagedListResponse::class)
        )]
    )
    @GetMapping(value = ["/list"])
    fun getDocumentsPagedList(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Номер страницы, начиная с 1", example = "1")
        @RequestParam page: Int = 1,
        @Parameter(description = "Количество элементов на странице", example = "10")
        @RequestParam size: Int = 10,
        @Parameter(
            description = "Поле сортировки",
            example = "createdAt",
            schema = Schema(allowableValues = ["id", "name", "type", "status", "createdAt", "updatedAt"])
        )
        @RequestParam sortField: String = "createdAt",
        @Parameter(description = "Направление сортировки", example = "DESC")
        @RequestParam sortDirection: Sort.Direction = Sort.Direction.DESC,
        @Parameter(description = "Фильтр по категории документа", example = "JURIDICAL")
        @RequestParam type: DocumentType? = null,
        @Parameter(description = "Поиск по названию документа", example = "договор")
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
        description = "Возвращает страницу документов текущей организации со статусом DELETED"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Страница удалённых документов получена",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentsPagedListResponse::class)
        )]
    )
    @GetMapping(value = ["/deleted"])
    fun getDeletedDocumentsPagedList(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Номер страницы, начиная с 1", example = "1")
        @RequestParam page: Int = 1,
        @Parameter(description = "Количество элементов на странице", example = "10")
        @RequestParam size: Int = 10,
        @Parameter(
            description = "Поле сортировки",
            example = "createdAt",
            schema = Schema(allowableValues = ["id", "name", "type", "status", "createdAt", "updatedAt"])
        )
        @RequestParam sortField: String = "createdAt",
        @Parameter(description = "Направление сортировки", example = "DESC")
        @RequestParam sortDirection: Sort.Direction = Sort.Direction.DESC,
        @Parameter(description = "Фильтр по категории документа", example = "FINANCIAL")
        @RequestParam type: DocumentType? = null,
        @Parameter(description = "Поиск по названию документа", example = "отчёт")
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
        description = "Загружает файл в защищённое хранилище и создаёт документ в текущей организации"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ загружен",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadDocument(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(
            description = "PDF, DOC, DOCX, JPEG или PNG размером до 50 МБ",
            required = true,
            schema = Schema(type = "string", format = "binary")
        )
        @RequestPart("file") file: MultipartFile,
        @Parameter(description = "Название документа", example = "Договор поставки №42", required = true)
        @RequestParam("name") name: String,
        @Parameter(description = "Категория документа", example = "JURIDICAL", required = true)
        @RequestParam("type") type: DocumentType,
        @Parameter(description = "Идентификатор папки", schema = Schema(format = "uuid"))
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
        description = "Скачивает файл документа, если пользователь состоит в организации-владельце"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Содержимое файла документа",
        content = [Content(
            mediaType = "application/octet-stream",
            schema = Schema(type = "string", format = "binary")
        )]
    )
    @GetMapping(value = ["/{id}/download"])
    fun downloadDocument(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
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
        summary = "Получить доступные статусы документа",
        description = "Возвращает текущий статус документа и разрешённые бизнес-процессом следующие статусы"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Доступные переходы получены",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentStatusTransitionsResponse::class)
        )]
    )
    @GetMapping(value = ["/{id}/status/transitions"])
    fun getAvailableStatusTransitions(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
    ): DocumentStatusTransitionsResponse {
        return documentService.getAvailableStatusTransitions(id, payload)
    }

    @Operation(
        summary = "Изменить статус документа",
        description = "Переводит документ в новый статус, если переход разрешён текущим бизнес-процессом"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Статус документа изменён",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PatchMapping(value = ["/{id}/status"])
    fun changeDocumentStatus(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeDocumentStatusRequest,
    ): DocumentResponse {
        return documentService.changeDocumentStatus(id, payload, request)
    }

    @Operation(
        summary = "Удалить документ",
        description = "Выполняет мягкое удаление документа, переводя его в статус DELETED"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ перемещён в корзину",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = MessageResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/delete"])
    fun deleteDocument(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
    ): MessageResponse {
        return documentService.deleteDocument(id, payload)
    }

    @Operation(
        summary = "Восстановить документ",
        description = "Восстанавливает документ из корзины и переводит его в статус CREATED"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ восстановлен",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = MessageResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/restore"])
    fun restoreDocument(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
    ): MessageResponse {
        return documentService.restoreDocument(id, payload)
    }

}

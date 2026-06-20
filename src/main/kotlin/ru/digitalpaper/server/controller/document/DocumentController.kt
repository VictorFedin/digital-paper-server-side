package ru.digitalpaper.server.controller.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.Sort
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.config.decorator.CurrentOrganization
import ru.digitalpaper.server.context.OrganizationContext
import ru.digitalpaper.server.controller.base.CommonApiResponses
import ru.digitalpaper.server.dto.request.document.AssignDocumentResponsibleRequest
import ru.digitalpaper.server.dto.request.document.CreateDocumentRequest
import ru.digitalpaper.server.dto.request.document.ChangeDocumentStatusRequest
import ru.digitalpaper.server.dto.request.document.DocumentListRequest
import ru.digitalpaper.server.dto.request.document.DocumentStatusActionRequest
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.document.DocumentResponse
import ru.digitalpaper.server.dto.response.document.DocumentStatusTransitionsResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
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
        @CurrentOrganization context: OrganizationContext,
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
        return documentService.getDocumentsPagedList(context, request)
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
        @CurrentOrganization context: OrganizationContext,
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
            context,
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
        @CurrentOrganization context: OrganizationContext,
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

        return documentService.uploadDocument(context, createDocumentRequest, file)
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
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
    ): ResponseEntity<InputStreamResource> {
        val file = documentService.downloadDocument(id, context)

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
        summary = "Получить PDF-представление документа",
        description = "Формирует PDF-preview для DOCX-документа. Для PDF-документа возвращает исходный файл"
    )
    @ApiResponse(
        responseCode = "200",
        description = "PDF-представление документа",
        content = [Content(
            mediaType = "application/pdf",
            schema = Schema(type = "string", format = "binary")
        )]
    )
    @GetMapping(value = ["/{id}/preview/pdf"], produces = [MediaType.APPLICATION_PDF_VALUE])
    fun previewDocumentAsPdf(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
    ): ResponseEntity<ByteArrayResource> {
        val file = documentService.previewDocumentAsPdf(id, context)
        val bytes = file.resource.inputStream.use { it.readBytes() }

        val builder = ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.inline()
                    .filename(file.filename, StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")

        builder.contentLength(bytes.size.toLong())

        return builder.body(ByteArrayResource(bytes))
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
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
    ): DocumentStatusTransitionsResponse {
        return documentService.getAvailableStatusTransitions(id, context)
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
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeDocumentStatusRequest,
    ): DocumentResponse {
        return documentService.changeDocumentStatus(id, context, request)
    }

    @Operation(
        summary = "Взять документ в работу",
        description = "Переводит документ в статус IN_PROGRESS"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ взят в работу",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/start"])
    fun startDocument(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @RequestBody(required = false) request: DocumentStatusActionRequest?,
    ): DocumentResponse {
        return documentService.startDocument(id, context, request.orEmpty())
    }

    @Operation(
        summary = "Отправить документ на проверку",
        description = "Переводит документ в статус PENDING_REVIEW. Статус IN_PROGRESS можно пропустить"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ отправлен на проверку",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/submit-review"])
    fun submitDocumentForReview(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @RequestBody(required = false) request: DocumentStatusActionRequest?,
    ): DocumentResponse {
        return documentService.submitDocumentForReview(id, context, request.orEmpty())
    }

    @Operation(
        summary = "Запросить правки по документу",
        description = "Переводит документ в статус CHANGES_REQUESTED. Доступно только владельцу организации"
    )
    @ApiResponse(
        responseCode = "200",
        description = "По документу запрошены правки",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/request-changes"])
    fun requestDocumentChanges(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @RequestBody request: DocumentStatusActionRequest,
    ): DocumentResponse {
        return documentService.requestDocumentChanges(id, context, request)
    }

    @Operation(
        summary = "Утвердить документ",
        description = "Переводит документ в статус APPROVED. Доступно только владельцу организации"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ утверждён",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/approve"])
    fun approveDocument(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @RequestBody(required = false) request: DocumentStatusActionRequest?,
    ): DocumentResponse {
        return documentService.approveDocument(id, context, request.orEmpty())
    }

    @Operation(
        summary = "Отклонить документ",
        description = "Переводит документ в статус REJECTED. Доступно только владельцу организации"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ отклонён",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/reject"])
    fun rejectDocument(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @RequestBody request: DocumentStatusActionRequest,
    ): DocumentResponse {
        return documentService.rejectDocument(id, context, request)
    }

    @Operation(
        summary = "Подписать документ",
        description = "Переводит документ в статус SIGNED"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ подписан",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/sign"])
    fun signDocument(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @RequestBody(required = false) request: DocumentStatusActionRequest?,
    ): DocumentResponse {
        return documentService.signDocument(id, context, request.orEmpty())
    }

    @Operation(
        summary = "Завершить документ",
        description = "Переводит документ в статус DONE"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ завершён",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/complete"])
    fun completeDocument(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @RequestBody(required = false) request: DocumentStatusActionRequest?,
    ): DocumentResponse {
        return documentService.completeDocument(id, context, request.orEmpty())
    }

    @Operation(
        summary = "Отменить документ",
        description = "Переводит документ в статус CANCELLED"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ отменён",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/cancel"])
    fun cancelDocument(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @RequestBody request: DocumentStatusActionRequest,
    ): DocumentResponse {
        return documentService.cancelDocument(id, context, request)
    }

    @Operation(
        summary = "Пометить документ просроченным",
        description = "Переводит документ в статус EXPIRED. Доступно только владельцу организации"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ помечен просроченным",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/expire"])
    fun expireDocument(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @RequestBody(required = false) request: DocumentStatusActionRequest?,
    ): DocumentResponse {
        return documentService.expireDocument(id, context, request.orEmpty())
    }

    @Operation(
        summary = "Назначить ответственного за документ",
        description = "Назначает ответственным пользователя текущей организации"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ответственный назначен",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PatchMapping(value = ["/{id}/responsible"])
    fun assignResponsible(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: AssignDocumentResponsibleRequest,
    ): DocumentResponse {
        return documentService.assignResponsible(id, context, request)
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
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
    ): MessageResponse {
        return documentService.deleteDocument(id, context)
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
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор документа", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
    ): MessageResponse {
        return documentService.restoreDocument(id, context)
    }

}

private fun DocumentStatusActionRequest?.orEmpty(): DocumentStatusActionRequest =
    this ?: DocumentStatusActionRequest()

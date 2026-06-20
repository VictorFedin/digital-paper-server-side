package ru.digitalpaper.server.service

import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.context.OrganizationContext
import ru.digitalpaper.server.dto.internal.DownloadedFile
import ru.digitalpaper.server.dto.request.document.AssignDocumentResponsibleRequest
import ru.digitalpaper.server.dto.request.document.CreateDocumentRequest
import ru.digitalpaper.server.dto.request.document.ChangeDocumentStatusRequest
import ru.digitalpaper.server.dto.request.document.DocumentListFilter
import ru.digitalpaper.server.dto.request.document.DocumentListRequest
import ru.digitalpaper.server.dto.request.document.DocumentStatusActionRequest
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.common.PagedResponse
import ru.digitalpaper.server.dto.response.document.DocumentListItem
import ru.digitalpaper.server.dto.response.document.DocumentResponse
import ru.digitalpaper.server.dto.response.document.DocumentStatusTransitionsResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.ForbiddenException
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.document.Document
import ru.digitalpaper.server.model.document.holder.DocumentStatus
import ru.digitalpaper.server.repository.DocumentRepo
import ru.digitalpaper.server.repository.UserOrganizationRepo
import ru.digitalpaper.server.repository.UserRepo
import ru.digitalpaper.server.type.StorageObjectType
import ru.digitalpaper.server.util.Utils
import java.io.ByteArrayInputStream
import java.util.*

@Service
class DocumentService(
    private val documentRepo: DocumentRepo,
    private val storageService: StorageService,
    private val statusTransitionPolicy: DocumentStatusTransitionPolicy,
    private val userRepo: UserRepo,
    private val userOrganizationRepo: UserOrganizationRepo,
    private val docxPdfPreviewService: DocxPdfPreviewService
) {

    @Transactional(readOnly = true)
    fun getDocumentsPagedList(
        context: OrganizationContext,
        request: DocumentListRequest,
    ): DocumentsPagedListResponse {
        return getDocumentsPagedListInternal(
            request = request,
            context = context,
            deleted = false,
        )
    }

    @Transactional(readOnly = true)
    fun getDeletedDocumentsPagedList(
        context: OrganizationContext,
        request: DocumentListRequest,
    ): DocumentsPagedListResponse {
        return getDocumentsPagedListInternal(
            request = request,
            context = context,
            deleted = true,
        )
    }

    private fun getDocumentsPagedListInternal(
        request: DocumentListRequest,
        context: OrganizationContext,
        deleted: Boolean,
    ): DocumentsPagedListResponse {
        val pageable = buildDocumentPageable(
            page = request.page,
            size = request.size,
            sortField = request.sortField,
            sortDirection = request.sortDirection,
        )

        val search = request.search?.trim()?.takeIf { it.isNotBlank() }

        val filter = DocumentListFilter(
            organizationId = context.organization.id,
            type = request.type,
            search = search,
            deleted = deleted,
        )

        val docsPage = documentRepo.getDocumentsPagedList(filter, pageable)

        return DocumentsPagedListResponse(
            page = PagedResponse(
                page = request.page,
                size = request.size,
                totalItems = docsPage.totalElements,
                sortField = request.sortField,
                sortDirection = request.sortDirection.name,
            ),
            list = docsPage.content.map { it.toListItem() }
        )
    }

    private fun buildDocumentPageable(
        page: Int,
        size: Int,
        sortField: String,
        sortDirection: Sort.Direction,
    ): PageRequest {
        val pageNumber = Utils.safePage(page)
        val pageSize = Utils.safeSize(size)
        val pageSort = resolveDocumentSortField(sortField)

        return PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(sortDirection, pageSort)
        )
    }

    @Transactional
    fun uploadDocument(
        context: OrganizationContext,
        request: CreateDocumentRequest,
        file: MultipartFile,
    ): DocumentResponse {
        val storedFileInfo = storageService.upload(
            file,
            StorageObjectType.DOCUMENT,
            context.organization.id.toString(),
        )

        val document = Document(
            name = request.name,
            path = storedFileInfo.objectKey,
            type = request.type,
            contentType = storedFileInfo.contentType,
            createdBy = context.user,
            responsibleUser = context.user,
            organization = context.organization
        )

        val result = documentRepo.save(document)

        return result.toResponse()
    }

    @Transactional(readOnly = true)
    fun downloadDocument(
        id: UUID,
        context: OrganizationContext
    ): DownloadedFile {
        val document = getAccessibleDocument(id, context)

        val downloadObject = storageService.download(
            document.path,
            StorageObjectType.DOCUMENT,
        )

        return DownloadedFile(
            filename = document.name,
            contentType = document.contentType,
            resource = InputStreamResource(downloadObject.inputStream),
            contentLength = downloadObject.size,
        )
    }

    @Transactional(readOnly = true)
    fun previewDocumentAsPdf(
        id: UUID,
        context: OrganizationContext
    ): DownloadedFile {
        val document = getAccessibleDocument(id, context)

        if (document.contentType == PDF_CONTENT_TYPE) {
            return downloadDocument(id, context)
        }

        if (document.contentType != DOCX_CONTENT_TYPE) {
            throw BadRequestException("PDF-представление доступно только для DOCX-документов")
        }

        val downloadObject = storageService.download(
            document.path,
            StorageObjectType.DOCUMENT,
        )

        val pdfBytes = downloadObject.inputStream.use { input ->
            docxPdfPreviewService.render(input)
        }

        return DownloadedFile(
            filename = document.name.withExtension("pdf"),
            contentType = PDF_CONTENT_TYPE,
            resource = InputStreamResource(ByteArrayInputStream(pdfBytes)),
            contentLength = pdfBytes.size.toLong()
        )
    }

    @Transactional(readOnly = true)
    fun getAvailableStatusTransitions(
        id: UUID,
        context: OrganizationContext
    ): DocumentStatusTransitionsResponse {
        val document = getAccessibleDocument(id, context)

        return DocumentStatusTransitionsResponse(
            currentStatus = document.status,
            availableStatuses = statusTransitionPolicy.availableFrom(document.status)
        )
    }

    @Transactional
    fun changeDocumentStatus(
        id: UUID,
        context: OrganizationContext,
        request: ChangeDocumentStatusRequest
    ): DocumentResponse {
        val targetStatus = request.status
            ?: throw BadRequestException("Необходимо указать новый статус документа")

        val reason = if (targetStatus in REASON_REQUIRED_STATUS_ACTIONS) {
            requireReason(request.reason)
        } else {
            request.reason
        }

        return transitionDocument(id, context, targetStatus, reason = reason)
    }

    @Transactional
    fun startDocument(
        id: UUID,
        context: OrganizationContext,
        request: DocumentStatusActionRequest = DocumentStatusActionRequest()
    ): DocumentResponse {
        return transitionDocument(id, context, DocumentStatus.IN_PROGRESS, reason = request.reason)
    }

    @Transactional
    fun submitDocumentForReview(
        id: UUID,
        context: OrganizationContext,
        request: DocumentStatusActionRequest = DocumentStatusActionRequest()
    ): DocumentResponse {
        return transitionDocument(id, context, DocumentStatus.PENDING_REVIEW, reason = request.reason)
    }

    @Transactional
    fun requestDocumentChanges(
        id: UUID,
        context: OrganizationContext,
        request: DocumentStatusActionRequest
    ): DocumentResponse {
        val reason = requireReason(request.reason)
        return transitionDocument(
            id = id,
            context = context,
            targetStatus = DocumentStatus.CHANGES_REQUESTED,
            ownerRequired = true,
            reason = reason
        )
    }

    @Transactional
    fun approveDocument(
        id: UUID,
        context: OrganizationContext,
        request: DocumentStatusActionRequest = DocumentStatusActionRequest()
    ): DocumentResponse {
        return transitionDocument(
            id = id,
            context = context,
            targetStatus = DocumentStatus.APPROVED,
            ownerRequired = true,
            reason = request.reason
        )
    }

    @Transactional
    fun rejectDocument(
        id: UUID,
        context: OrganizationContext,
        request: DocumentStatusActionRequest
    ): DocumentResponse {
        val reason = requireReason(request.reason)
        return transitionDocument(
            id = id,
            context = context,
            targetStatus = DocumentStatus.REJECTED,
            ownerRequired = true,
            reason = reason
        )
    }

    @Transactional
    fun signDocument(
        id: UUID,
        context: OrganizationContext,
        request: DocumentStatusActionRequest = DocumentStatusActionRequest()
    ): DocumentResponse {
        return transitionDocument(id, context, DocumentStatus.SIGNED, reason = request.reason)
    }

    @Transactional
    fun completeDocument(
        id: UUID,
        context: OrganizationContext,
        request: DocumentStatusActionRequest = DocumentStatusActionRequest()
    ): DocumentResponse {
        return transitionDocument(id, context, DocumentStatus.DONE, reason = request.reason)
    }

    @Transactional
    fun cancelDocument(
        id: UUID,
        context: OrganizationContext,
        request: DocumentStatusActionRequest
    ): DocumentResponse {
        return transitionDocument(id, context, DocumentStatus.CANCELLED, reason = requireReason(request.reason))
    }

    @Transactional
    fun expireDocument(
        id: UUID,
        context: OrganizationContext,
        request: DocumentStatusActionRequest = DocumentStatusActionRequest()
    ): DocumentResponse {
        return transitionDocument(
            id = id,
            context = context,
            targetStatus = DocumentStatus.EXPIRED,
            ownerRequired = true,
            reason = request.reason
        )
    }

    @Transactional
    fun assignResponsible(
        id: UUID,
        context: OrganizationContext,
        request: AssignDocumentResponsibleRequest
    ): DocumentResponse {
        val document = getAccessibleDocument(id, context)
        ensureCanAssignResponsible(document, context)

        val userId = request.userId
            ?: throw BadRequestException("Необходимо указать пользователя")

        if (!userOrganizationRepo.existUserInOrganization(userId, context.organization.id)) {
            throw BadRequestException("Пользователь не состоит в организации")
        }

        val user = userRepo.getUserById(userId)
            ?: throw NotFoundException("Пользователь не найден")

        document.responsibleUser = user
        document.lastModifiedBy = context.user

        return document.toResponse()
    }

    @Transactional
    fun restoreDocument(
        id: UUID,
        context: OrganizationContext
    ): MessageResponse {
        val document = getAccessibleDocument(id, context)

        if (document.status != DocumentStatus.DELETED) {
            throw BadRequestException("Документ не находится в статусе удаления")
        }

        document.status = DocumentStatus.CREATED
        document.statusReason = null
        document.lastModifiedBy = context.user

        return MessageResponse("Документ восстановлен")
    }

    @Transactional
    fun deleteDocument(
        id: UUID,
        context: OrganizationContext
    ): MessageResponse {
        val document = getAccessibleDocument(id, context)
        ensureCanDeleteDocument(document, context)

        if (document.status == DocumentStatus.DELETED) {
            return MessageResponse("Документ уже удален")
        }

        document.status = DocumentStatus.DELETED
        document.statusReason = null
        document.lastModifiedBy = context.user

        return MessageResponse("Документ удален")
    }

    private fun getAccessibleDocument(
        id: UUID,
        context: OrganizationContext
    ): Document =
        documentRepo.getDocumentByIdAndOrganizationId(id, context.organization.id)
            ?: throw NotFoundException("Документ не найден")

    private fun transitionDocument(
        id: UUID,
        context: OrganizationContext,
        targetStatus: DocumentStatus,
        ownerRequired: Boolean = targetStatus in OWNER_ONLY_STATUS_ACTIONS,
        reason: String? = null
    ): DocumentResponse {
        if (ownerRequired) {
            ensureOwner(context)
        }

        val document = getAccessibleDocument(id, context)

        statusTransitionPolicy.validate(
            currentStatus = document.status,
            targetStatus = targetStatus
        )

        document.status = targetStatus
        document.statusReason = reason?.trim()?.takeIf { it.isNotBlank() }
        document.lastModifiedBy = context.user

        return document.toResponse()
    }

    private fun requireReason(reason: String?): String {
        return reason?.trim()?.takeIf { it.isNotBlank() }
            ?: throw BadRequestException("Необходимо указать причину действия")
    }

    private fun ensureCanDeleteDocument(
        document: Document,
        context: OrganizationContext
    ) {
        if (context.role.isOwner() || document.createdBy.id == context.user.id) {
            return
        }

        throw ForbiddenException("Удалять чужие документы может только владелец организации")
    }

    private fun ensureCanAssignResponsible(
        document: Document,
        context: OrganizationContext
    ) {
        if (context.role.isOwner() || document.createdBy.id == context.user.id || document.responsibleUser.id == context.user.id) {
            return
        }

        throw ForbiddenException("Назначать ответственного может владелец, автор или текущий ответственный")
    }

    private fun ensureOwner(context: OrganizationContext) {
        if (!context.role.isOwner()) {
            throw ForbiddenException("Действие доступно только владельцу организации")
        }
    }

    fun Document.toResponse(): DocumentResponse =
        DocumentResponse(
            id = id,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            type = type,
            status = status,
            statusReason = statusReason,
            contentType = contentType,
            responsible = responsibleUser.getShortName(),
            createdBy = createdBy.getShortName()
        )

    fun Document.toListItem(): DocumentListItem =
        DocumentListItem(
            id = id,
            name = name,
            createdAt = createdAt,
            status = status,
            responsible = responsibleUser.getShortName(),
            contentType = contentType,
            updatedAt = updatedAt
        )

    private fun resolveDocumentSortField(sortField: String): String {
        return when (sortField) {
            "id" -> "id"
            "name" -> "name"
            "type" -> "type"
            "status" -> "status"
            "createdAt" -> "createdAt"
            "updatedAt" -> "updatedAt"
            else -> "createdAt"
        }
    }

    companion object {
        private val OWNER_ONLY_STATUS_ACTIONS = setOf(
            DocumentStatus.CHANGES_REQUESTED,
            DocumentStatus.APPROVED,
            DocumentStatus.REJECTED,
            DocumentStatus.EXPIRED
        )

        private val REASON_REQUIRED_STATUS_ACTIONS = setOf(
            DocumentStatus.CHANGES_REQUESTED,
            DocumentStatus.REJECTED,
            DocumentStatus.CANCELLED
        )

        private const val DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        private const val PDF_CONTENT_TYPE = "application/pdf"
    }
}

private fun String.withExtension(extension: String): String {
    val baseName = substringBeforeLast('.', this)
    return "$baseName.$extension"
}

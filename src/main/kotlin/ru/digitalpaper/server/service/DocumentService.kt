package ru.digitalpaper.server.service

import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.context.OrganizationContext
import ru.digitalpaper.server.dto.internal.DownloadedFile
import ru.digitalpaper.server.dto.request.document.CreateDocumentRequest
import ru.digitalpaper.server.dto.request.document.ChangeDocumentStatusRequest
import ru.digitalpaper.server.dto.request.document.DocumentListFilter
import ru.digitalpaper.server.dto.request.document.DocumentListRequest
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.common.PagedResponse
import ru.digitalpaper.server.dto.response.document.DocumentListItem
import ru.digitalpaper.server.dto.response.document.DocumentResponse
import ru.digitalpaper.server.dto.response.document.DocumentStatusTransitionsResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.document.Document
import ru.digitalpaper.server.model.document.holder.DocumentStatus
import ru.digitalpaper.server.repository.DocumentRepo
import ru.digitalpaper.server.type.StorageObjectType
import ru.digitalpaper.server.util.Utils
import java.util.*

@Service
class DocumentService(
    private val documentRepo: DocumentRepo,
    private val storageService: StorageService,
    private val statusTransitionPolicy: DocumentStatusTransitionPolicy
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
        val document = getAccessibleDocument(id, context)
        val targetStatus = request.status
            ?: throw BadRequestException("Необходимо указать новый статус документа")

        statusTransitionPolicy.validate(
            currentStatus = document.status,
            targetStatus = targetStatus
        )

        document.status = targetStatus
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
        document.lastModifiedBy = context.user

        return MessageResponse("Документ восстановлен")
    }

    @Transactional
    fun deleteDocument(
        id: UUID,
        context: OrganizationContext
    ): MessageResponse {
        val document = getAccessibleDocument(id, context)

        if (document.status == DocumentStatus.DELETED) {
            return MessageResponse("Документ уже удален")
        }

        document.status = DocumentStatus.DELETED
        document.lastModifiedBy = context.user

        return MessageResponse("Документ удален")
    }

    private fun getAccessibleDocument(
        id: UUID,
        context: OrganizationContext
    ): Document =
        documentRepo.getDocumentByIdAndOrganizationId(id, context.organization.id)
            ?: throw NotFoundException("Документ не найден")

    fun Document.toResponse(): DocumentResponse =
        DocumentResponse(
            id = id,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            type = type,
            status = status,
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
}

package ru.digitalpaper.server.service

import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
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
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.ForbiddenException
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.document.Document
import ru.digitalpaper.server.model.document.holder.DocumentStatus
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.repository.DocumentRepo
import ru.digitalpaper.server.repository.UserOrganizationRepo
import ru.digitalpaper.server.type.StorageObjectType
import ru.digitalpaper.server.util.Utils
import java.util.*

@Service
class DocumentService(
    private val documentRepo: DocumentRepo,
    private val organizationService: OrganizationService,
    private val storageService: StorageService,
    private val userOrganizationRepo: UserOrganizationRepo,
    private val statusTransitionPolicy: DocumentStatusTransitionPolicy
) {

    @Transactional(readOnly = true)
    fun getDocumentsPagedList(
        payload: UserPayload,
        request: DocumentListRequest,
    ): DocumentsPagedListResponse {
        return getDocumentsPagedListInternal(
            request = request,
            payload = payload,
            deleted = false,
        )
    }

    @Transactional(readOnly = true)
    fun getDeletedDocumentsPagedList(
        payload: UserPayload,
        request: DocumentListRequest,
    ): DocumentsPagedListResponse {
        return getDocumentsPagedListInternal(
            request = request,
            payload = payload,
            deleted = true,
        )
    }

    private fun getDocumentsPagedListInternal(
        request: DocumentListRequest,
        payload: UserPayload,
        deleted: Boolean,
    ): DocumentsPagedListResponse {
        val relation = organizationService.getRelationByUserId(payload.id)

        val pageable = buildDocumentPageable(
            page = request.page,
            size = request.size,
            sortField = request.sortField,
            sortDirection = request.sortDirection,
        )

        val search = request.search?.trim()?.takeIf { it.isNotBlank() }

        val filter = DocumentListFilter(
            organizationId = relation.organization.id,
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
        payload: UserPayload,
        request: CreateDocumentRequest,
        file: MultipartFile,
    ): DocumentResponse {
        val relation = organizationService.getRelationByUserId(payload.id)

        val storedFileInfo = storageService.upload(
            file,
            StorageObjectType.DOCUMENT,
            relation.organization.id.toString(),
        )

        val document = Document(
            name = request.name,
            path = storedFileInfo.objectKey,
            type = request.type,
            contentType = storedFileInfo.contentType,
            createdBy = relation.user,
            responsibleUser = relation.user,
            organization = relation.organization
        )

        val result = documentRepo.save(document)

        return result.toResponse()
    }

    @Transactional(readOnly = true)
    fun downloadDocument(
        id: UUID,
        payload: UserPayload
    ): DownloadedFile {
        val relation = organizationService.getRelationByUserId(payload.id)

        val document = documentRepo.getDocumentByIdAndOrganizationId(id, relation.organization.id)
            ?: throw NotFoundException("Документ не найден")

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
        payload: UserPayload
    ): DocumentStatusTransitionsResponse {
        val document = getAccessibleDocument(id, payload)

        return DocumentStatusTransitionsResponse(
            currentStatus = document.status,
            availableStatuses = statusTransitionPolicy.availableFrom(document.status)
        )
    }

    @Transactional
    fun changeDocumentStatus(
        id: UUID,
        payload: UserPayload,
        request: ChangeDocumentStatusRequest
    ): DocumentResponse {
        val (document, membership) = getAccessibleDocumentWithMembership(id, payload)
        val targetStatus = request.status
            ?: throw BadRequestException("Необходимо указать новый статус документа")

        statusTransitionPolicy.validate(
            currentStatus = document.status,
            targetStatus = targetStatus
        )

        document.status = targetStatus
        document.lastModifiedBy = membership.user

        return document.toResponse()
    }

    @Transactional
    fun restoreDocument(
        id: UUID,
        payload: UserPayload
    ): MessageResponse {
        val document = documentRepo.getDocument(id)
            ?: throw NotFoundException("Документ не найден")

        userOrganizationRepo.findMembership(payload.id, document.organization.id)
            ?: throw ForbiddenException("У вас нет доступа к этому документу")

        if (document.status != DocumentStatus.DELETED) {
            throw BadRequestException("Документ не находится в статусе удаления")
        }

        document.status = DocumentStatus.CREATED

        return MessageResponse("Документ восстановлен")
    }

    @Transactional
    fun deleteDocument(
        id: UUID,
        payload: UserPayload
    ): MessageResponse {
        val document = documentRepo.getDocument(id)
            ?: throw NotFoundException("Документ не найден")

        userOrganizationRepo.findMembership(payload.id, document.organization.id)
            ?: throw ForbiddenException("У вас нет доступа к этому документу")

        if (document.status == DocumentStatus.DELETED) {
            return MessageResponse("Документ уже удален")
        }

        document.status = DocumentStatus.DELETED

        return MessageResponse("Документ удален")
    }

    private fun getAccessibleDocument(
        id: UUID,
        payload: UserPayload
    ): Document = getAccessibleDocumentWithMembership(id, payload).first

    private fun getAccessibleDocumentWithMembership(
        id: UUID,
        payload: UserPayload
    ): Pair<Document, UserOrganization> {
        val document = documentRepo.getDocument(id)
            ?: throw NotFoundException("Документ не найден")

        val membership = userOrganizationRepo.findMembership(payload.id, document.organization.id)
            ?: throw ForbiddenException("У вас нет доступа к этому документу")

        return document to membership
    }

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

package ru.digitalpaper.server.service

import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.dto.internal.DownloadedObject
import ru.digitalpaper.server.dto.request.document.CreateDocumentRequest
import ru.digitalpaper.server.dto.request.document.DocumentListFilter
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.common.PagedResponse
import ru.digitalpaper.server.dto.response.document.DocumentListItem
import ru.digitalpaper.server.dto.response.document.DocumentResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.document.Document
import ru.digitalpaper.server.model.document.holder.DocumentStatus
import ru.digitalpaper.server.model.document.holder.DocumentType
import ru.digitalpaper.server.repository.DocumentRepo
import ru.digitalpaper.server.repository.UserOrganizationRepo
import ru.digitalpaper.server.type.StorageObjectType
import ru.digitalpaper.server.util.Utils
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.util.*

@Service
class DocumentService(
    private val documentRepo: DocumentRepo,
    private val organizationService: OrganizationService,
    private val storageService: StorageService,
    private val userOrganizationRepo: UserOrganizationRepo
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")

    }

    @Transactional
    fun getDocumentsPagedList(
        page: Int,
        size: Int,
        payload: UserPayload,
        sortField: String,
        sortDirection: Sort.Direction,
        type: DocumentType? = null,
        search: String? = null,
        rs: RequestSatellites
    ): DocumentsPagedListResponse {
        logger.info(
            ServerLogUtil.info(
                "DocumentService.getDocumentsPagedList",
                rs.traceId,
                "Enter",
                Pair("page", "$page"),
                Pair("size", "$size")
            )
        )

        val relation = organizationService.getRelationByUserId(payload.id, rs)

        val pageNumber = Utils.safePage(page)
        val pageSize = Utils.safeSize(size)
        val pageSort = resolveDocumentSortField(sortField)

        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(sortDirection, pageSort)
        )

        val filter = buildFilter(relation.organization.id, type, search, false)

        val docsPage = documentRepo.getDocumentsPagedList(filter, pageable)

        return DocumentsPagedListResponse(
            page = PagedResponse(
                page = page,
                size = size,
                totalItems = docsPage.totalElements,
                sortField = sortField,
                sortDirection = sortDirection.name
            ),
            list = docsPage.content.map { it.toListItem() }.toList()
        )
    }

    @Transactional
    fun getDeletedDocumentsPagedList(
        page: Int,
        size: Int,
        payload: UserPayload,
        sortField: String,
        sortDirection: Sort.Direction,
        type: DocumentType? = null,
        search: String? = null,
        rs: RequestSatellites
    ): DocumentsPagedListResponse {
        logger.info(
            ServerLogUtil.info(
                "DocumentService.getDeletedDocumentsPagedList",
                rs.traceId,
                "Enter"
            )
        )

        val relation = organizationService.getRelationByUserId(payload.id, rs)

        val pageNumber = Utils.safePage(page)
        val pageSize = Utils.safeSize(size)
        val pageSort = resolveDocumentSortField(sortField)

        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(sortDirection, pageSort)
        )

        val filter = buildFilter(relation.organization.id, type, search, true)

        val docsPage = documentRepo.getDocumentsPagedList(filter, pageable)

        return DocumentsPagedListResponse(
            page = PagedResponse(
                page = page,
                size = size,
                totalItems = docsPage.totalElements,
                sortField = sortField,
                sortDirection = sortDirection.name
            ),
            list = docsPage.content.map { it.toListItem() }.toList()
        )
    }

    @Transactional
    fun uploadDocument(
        payload: UserPayload,
        request: CreateDocumentRequest,
        file: MultipartFile,
        rs: RequestSatellites
    ): DocumentResponse {
        logger.info(
            ServerLogUtil.info(
                "DocumentService.uploadDocument",
                rs.traceId,
                "Enter",
                mapOf("request" to "$request")
            )
        )

        val relation = organizationService.getRelationByUserId(payload.id, rs)

        val storedFileInfo = storageService.upload(
            file,
            StorageObjectType.DOCUMENT,
            relation.organization.id.toString(),
            rs
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

    @Transactional
    fun downloadDocument(
        id: UUID,
        payload: UserPayload,
        response: HttpServletResponse,
        rs: RequestSatellites
    ) {
        logger.info(
            ServerLogUtil.info(
                "DocumentService.downloadDocument",
                rs.traceId,
                "Enter",
                mapOf("id" to "$id")
            )
        )

        val relation = organizationService.getRelationByUserId(payload.id, rs)

        val document = documentRepo.getDocumentByIdAndOrganizationId(id, relation.organization.id)
            ?: throw NotFoundException("Документ не найден")

        val downloadObject = storageService.download(
            document.path,
            StorageObjectType.DOCUMENT,
            rs
        )

        writeFileResponse(downloadObject, response)
    }

    fun Document.toResponse(): DocumentResponse =
        DocumentResponse(
            id = id,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt
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

    private fun buildFilter(
        organizationId: UUID? = null,
        type: DocumentType? = null,
        search: String? = null,
        deleted: Boolean? = null,
    ): DocumentListFilter =
        DocumentListFilter(
            organizationId = organizationId,
            type = type,
            search = search,
            deleted = deleted
        )

    private fun resolveDocumentSortField(sortField: String): String {
        return when (sortField) {
            "id" -> "id"
            "title" -> "title"
            "name" -> "name"
            "type" -> "type"
            "createdAt" -> "createdAt"
            "updatedAt" -> "updatedAt"
            else -> "createdAt"
        }
    }

    private fun writeFileResponse(
        downloadObject: DownloadedObject,
        response: HttpServletResponse
    ) {
        response.contentType = downloadObject.contentType

        response.setHeader(
            "Content-Disposition",
            "attachment; filename=\"${downloadObject.originalFileName}\""
        )

        downloadObject.size.let {
            response.setContentLengthLong(it)
        }

        downloadObject.inputStream.use { input ->
            response.outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    @Transactional
    fun changeDocumentStatus(
        id: UUID,
        status: DocumentStatus,
        payload: UserPayload,
        rs: RequestSatellites
    ): MessageResponse {
        logger.info(
            ServerLogUtil.info(
                "DocumentService.changeDocumentStatus",
                rs.traceId,
                "Enter"
            )
        )

        val document = documentRepo.getDocument(id)
            ?: throw NotFoundException("Документ не найден")

        userOrganizationRepo.findMembership(payload.id, document.organization.id)
            ?: throw BadRequestException("У вас нет доступа к этому документу")

        when (status) {
            DocumentStatus.CREATED -> {
                document.status = DocumentStatus.CREATED
                documentRepo.save(document)

                return MessageResponse("Документ восстановлен")
            }
            DocumentStatus.DELETED -> {
                document.status = DocumentStatus.DELETED
                documentRepo.save(document)

                return MessageResponse("Документ удален")
            }
            else -> throw BadRequestException("В разработке")
        }
    }

}

package ru.digitalpaper.server.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.digitalpaper.server.dto.response.common.PagedResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.repository.DocumentRepo
import ru.digitalpaper.server.util.Utils
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil

@Service
class DocumentService(
    private val documentRepo: DocumentRepo,
    private val organizationService: OrganizationService
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
        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(sortDirection, sortField)
        )

        val docsPage = documentRepo.getDocumentsByOrganizationId(relation.organization.id, pageable)

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

}
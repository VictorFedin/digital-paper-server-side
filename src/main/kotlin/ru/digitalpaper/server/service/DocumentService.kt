package ru.digitalpaper.server.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.digitalpaper.server.dto.response.common.PagedResponse
import ru.digitalpaper.server.dto.response.document.DocumentsPagedListResponse
import ru.digitalpaper.server.repository.DocumentRepo
import ru.digitalpaper.server.util.Utils
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil

@Service
class DocumentService(
    private val documentRepo: DocumentRepo
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")

        private const val DEFAULT_SORT_FIELD = "createdAt"
    }

    fun getDocumentsPagedList(
        page: Int,
        size: Int,
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

        val pageNumber = Utils.safePage(page)
        val pageSize = Utils.safeSize(size)
        val direction = Sort.Direction.DESC
        val sortField = DEFAULT_SORT_FIELD
        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(direction, sortField)
        )

        val docsPage = documentRepo.getDocuments(pageable)

        return DocumentsPagedListResponse(
            page = PagedResponse(
                page = page,
                size = size,
                totalItems = docsPage.totalElements,
                sortField = sortField,
                sortDirection = direction.name
            ),
            list = docsPage.content.map { it.toListItem() }.toList()
        )
    }

}
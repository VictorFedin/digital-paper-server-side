package ru.digitalpaper.server.dto.response.document

import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.common.PagedResponse

data class DocumentsPagedListResponse(
    val page: PagedResponse,
    val list: List<DocumentListItem>
) : Response()

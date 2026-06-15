package ru.digitalpaper.server.dto.response.document

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.dto.response.common.PagedResponse

@Schema(description = "Страница документов")
data class DocumentsPagedListResponse(
    @field:Schema(description = "Метаданные пагинации")
    val page: PagedResponse,

    @field:Schema(description = "Документы текущей страницы")
    val list: List<DocumentListItem>
)

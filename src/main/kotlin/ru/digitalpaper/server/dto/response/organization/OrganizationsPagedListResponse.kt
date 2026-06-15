package ru.digitalpaper.server.dto.response.organization

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.dto.response.common.PagedResponse

@Schema(description = "Страница организаций")
data class OrganizationsPagedListResponse(
    @field:Schema(description = "Метаданные пагинации")
    val page: PagedResponse,

    @field:Schema(description = "Организации текущей страницы")
    val list: List<OrganizationListItem>
)

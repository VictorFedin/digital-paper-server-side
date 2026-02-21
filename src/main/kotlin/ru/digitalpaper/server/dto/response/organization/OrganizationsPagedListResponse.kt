package ru.digitalpaper.server.dto.response.organization

import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.common.PagedResponse

data class OrganizationsPagedListResponse(
    val page: PagedResponse,
    val list: List<OrganizationResponse>
) : Response()
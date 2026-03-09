package ru.digitalpaper.server.dto.response.user

import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.common.PagedResponse

data class UsersPagedListResponse(
    val page: PagedResponse,
    val list: List<UserListItem>
) : Response()

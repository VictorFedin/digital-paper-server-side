package ru.digitalpaper.server.dto.response.user

import ru.digitalpaper.server.dto.response.Response

data class UsersListResponse(
    val list: List<UserListItem>
) : Response()

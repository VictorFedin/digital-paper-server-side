package ru.digitalpaper.server.dto.response.user

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Список пользователей")
data class UsersListResponse(
    @field:Schema(description = "Пользователи, соответствующие запросу")
    val list: List<UserListItem>
)

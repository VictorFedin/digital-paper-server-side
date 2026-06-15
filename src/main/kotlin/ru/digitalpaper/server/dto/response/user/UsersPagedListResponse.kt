package ru.digitalpaper.server.dto.response.user

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.dto.response.common.PagedResponse

@Schema(description = "Страница пользователей")
data class UsersPagedListResponse(
    @field:Schema(description = "Метаданные пагинации")
    val page: PagedResponse,

    @field:Schema(description = "Пользователи текущей страницы")
    val list: List<UserListItem>
)

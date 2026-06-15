package ru.digitalpaper.server.dto.response.common

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Метаданные страницы результатов")
data class PagedResponse(
    @field:Schema(description = "Номер текущей страницы", example = "1")
    val page: Int,

    @field:Schema(description = "Запрошенный размер страницы", example = "10")
    val size: Int,

    @field:Schema(description = "Общее количество найденных элементов", example = "42")
    val totalItems: Long,

    @field:Schema(description = "Поле сортировки", example = "createdAt")
    val sortField: String,

    @field:Schema(description = "Направление сортировки", example = "DESC", allowableValues = ["ASC", "DESC"])
    val sortDirection: String
)

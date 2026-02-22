package ru.digitalpaper.server.dto.response.common

data class PagedResponse(
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val sortField: String,
    val sortDirection: String
)

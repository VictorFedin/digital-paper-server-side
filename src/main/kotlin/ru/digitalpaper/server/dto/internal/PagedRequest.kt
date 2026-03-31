package ru.digitalpaper.server.dto.internal

data class PagedRequest(
    val page: Int,
    val size: Int,
    val sortField: String,
    val sortDirection: String
)

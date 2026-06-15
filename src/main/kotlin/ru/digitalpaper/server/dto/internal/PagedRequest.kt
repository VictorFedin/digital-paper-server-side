package ru.digitalpaper.server.dto.internal

import io.swagger.v3.oas.annotations.media.Schema

@Schema(hidden = true)
data class PagedRequest(
    val page: Int,
    val size: Int,
    val sortField: String,
    val sortDirection: String
)

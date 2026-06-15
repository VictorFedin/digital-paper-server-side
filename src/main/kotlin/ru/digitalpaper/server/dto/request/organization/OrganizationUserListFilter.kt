package ru.digitalpaper.server.dto.request.organization

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(hidden = true)
data class OrganizationUserListFilter(
    val organizationId: UUID? = null,
    val search: String? = null
)

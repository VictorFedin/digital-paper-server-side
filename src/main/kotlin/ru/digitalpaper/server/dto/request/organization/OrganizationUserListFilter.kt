package ru.digitalpaper.server.dto.request.organization

import java.util.UUID

data class OrganizationUserListFilter(
    val organizationId: UUID? = null,
    val search: String? = null
)

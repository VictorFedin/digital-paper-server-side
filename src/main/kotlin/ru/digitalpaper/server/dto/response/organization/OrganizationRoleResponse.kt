package ru.digitalpaper.server.dto.response.organization

import ru.digitalpaper.server.model.user.holder.UserRole
import java.util.UUID

data class OrganizationRoleResponse(
    val organizationId: UUID,
    val userId: UUID,
    val role: UserRole
)

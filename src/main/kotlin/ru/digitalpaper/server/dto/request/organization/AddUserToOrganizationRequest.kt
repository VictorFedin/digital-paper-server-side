package ru.digitalpaper.server.dto.request.organization

import ru.digitalpaper.server.model.user.holder.UserRole

data class AddUserToOrganizationRequest(
    val email: String,
    val role: UserRole
)

package ru.digitalpaper.server.dto.request.organization

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Данные сотрудника для добавления или приглашения в организацию")
data class AddUserToOrganizationRequest(
    @field:Schema(description = "Email сотрудника", example = "employee@example.com", format = "email")
    val email: String
)

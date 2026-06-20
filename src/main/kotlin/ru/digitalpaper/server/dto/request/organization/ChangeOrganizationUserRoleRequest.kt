package ru.digitalpaper.server.dto.request.organization

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import ru.digitalpaper.server.model.user.holder.UserRole

@Schema(description = "Запрос на изменение роли пользователя в организации")
data class ChangeOrganizationUserRoleRequest(
    @field:NotNull(message = "Необходимо указать роль пользователя")
    @field:Schema(description = "Новая роль пользователя в организации", example = "ADMIN")
    val role: UserRole?
)

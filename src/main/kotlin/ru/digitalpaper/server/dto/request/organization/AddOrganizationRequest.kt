package ru.digitalpaper.server.dto.request.organization

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.digitalpaper.server.model.organization.holder.Industry

data class AddOrganizationRequest(
    @field:NotBlank(message = "Параметр name пуст")
    val name: String,

    val description: String? = null,

    val phoneNumber: String? = null,

    @Email(message = "Неверный формат email")
    val email: String? = null,

    @field:NotNull(message = "Параметр industry не передан")
    var industry: Industry
)

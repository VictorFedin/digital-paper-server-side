package ru.digitalpaper.server.dto.request.organization

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.digitalpaper.server.model.organization.holder.Industry

@Schema(description = "Данные для создания организации")
data class AddOrganizationRequest(
    @field:NotBlank(message = "Параметр name пуст")
    @field:Schema(description = "Краткое название организации", example = "Digital Paper")
    val name: String,

    @field:Schema(description = "Описание деятельности организации", example = "Разработка корпоративных решений", nullable = true)
    val description: String? = null,

    @field:Schema(description = "Контактный телефон", example = "+7 999 123-45-67", nullable = true)
    val phoneNumber: String? = null,

    @field:Email(message = "Неверный формат email")
    @field:Schema(description = "Контактный email", example = "info@example.com", format = "email", nullable = true)
    val email: String? = null,

    @field:NotNull(message = "Параметр industry не передан")
    @field:Schema(description = "Отрасль организации", example = "FINANCE")
    var industry: Industry
)

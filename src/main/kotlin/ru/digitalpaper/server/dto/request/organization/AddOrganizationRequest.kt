package ru.digitalpaper.server.dto.request.organization

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.digitalpaper.server.model.organization.holder.Industry

data class AddOrganizationRequest(
    @JsonProperty(value = "name")
    @field:NotBlank(message = "Параметр name пуст")
    val name: String,

    @JsonProperty(value = "industry")
    @field:NotNull(message = "Параметр industry не передан")
    val industry: Industry
)

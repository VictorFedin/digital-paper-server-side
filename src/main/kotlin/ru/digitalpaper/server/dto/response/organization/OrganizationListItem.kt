package ru.digitalpaper.server.dto.response.organization

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.organization.holder.OrganizationType
import java.time.ZonedDateTime
import java.util.*

@Schema(description = "Краткая информация об организации в списке")
data class OrganizationListItem(
    @field:Schema(description = "Идентификатор организации", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Название организации", example = "Digital Paper")
    val name: String,

    @field:Schema(
        description = "Ссылка на изображение организации через backend",
        example = "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/avatar",
        nullable = true
    )
    val avatarUrl: String?,

    @field:Schema(description = "Организационно-правовая форма", example = "LIMITED_LIABILITY_COMPANY")
    val type: OrganizationType,

    @field:Schema(description = "Дата и время создания", format = "date-time", example = "2026-06-15T12:00:00+03:00")
    val createdAt: ZonedDateTime
)

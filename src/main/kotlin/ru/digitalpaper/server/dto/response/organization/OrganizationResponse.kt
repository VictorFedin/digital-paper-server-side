package ru.digitalpaper.server.dto.response.organization

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.organization.holder.Industry
import ru.digitalpaper.server.model.organization.holder.ModerationStatus
import ru.digitalpaper.server.model.organization.holder.OrganizationType
import java.time.ZonedDateTime
import java.util.*

@Schema(description = "Подробная информация об организации")
data class OrganizationResponse(
    @field:Schema(description = "Идентификатор организации", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Название организации", example = "Digital Paper")
    val name: String,

    @field:Schema(description = "Полное юридическое название", example = "ООО «Цифровая бумага»")
    val fullName: String,

    @field:Schema(description = "Описание организации", example = "Разработка систем электронного документооборота", nullable = true)
    val description: String? = null,

    @field:Schema(description = "Контактный телефон", example = "+7 999 123-45-67", nullable = true)
    val phone: String? = null,

    @field:Schema(description = "Контактный email", example = "info@example.com", format = "email", nullable = true)
    val email: String? = null,

    @field:Schema(description = "Отрасль организации", example = "FINANCE")
    val industry: Industry,

    @field:Schema(description = "Организационно-правовая форма", example = "LIMITED_LIABILITY_COMPANY")
    val type: OrganizationType,

    @field:Schema(description = "Регистрационный номер организации", example = "1234567890123", nullable = true)
    val regNumber: String? = null,

    @field:Schema(description = "Идентификационный номер налогоплательщика", example = "7701234567", nullable = true)
    val identificationNumber: String? = null,

    @field:Schema(description = "Код причины постановки на учёт", example = "770101001", nullable = true)
    val regReasonCode: String? = null,

    @field:Schema(description = "Юридический адрес", example = "г. Москва, ул. Примерная, д. 1", nullable = true)
    val address: String? = null,

    @field:Schema(description = "Статус модерации организации", example = "NEW")
    val status: ModerationStatus,

    @field:Schema(description = "Дата и время создания", format = "date-time", example = "2026-06-15T12:00:00+03:00")
    val createdAt: ZonedDateTime,

    @field:Schema(description = "Дата и время последнего изменения", format = "date-time", example = "2026-06-15T13:30:00+03:00")
    val updatedAt: ZonedDateTime
)

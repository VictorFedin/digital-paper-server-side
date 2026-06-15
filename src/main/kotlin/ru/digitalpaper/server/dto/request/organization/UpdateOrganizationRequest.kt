package ru.digitalpaper.server.dto.request.organization

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.organization.holder.OrganizationType

@Schema(description = "Обновляемые реквизиты организации")
data class UpdateOrganizationRequest(
    @field:Schema(description = "Краткое название организации", example = "Digital Paper")
    val name: String,

    @field:Schema(description = "Полное юридическое название", example = "ООО «Цифровая бумага»")
    val fullName: String,

    @field:Schema(description = "Описание организации", example = "Разработка систем электронного документооборота", nullable = true)
    val description: String? = null,

    @field:Schema(description = "Контактный телефон", example = "+7 999 123-45-67", nullable = true)
    val phone: String? = null,

    @field:Schema(description = "Организационно-правовая форма", example = "LIMITED_LIABILITY_COMPANY")
    val type: OrganizationType,

    @field:Schema(description = "Регистрационный номер организации", example = "1234567890123")
    val regNumber: String,

    @field:Schema(description = "Идентификационный номер налогоплательщика", example = "7701234567")
    val identificationNumber: String,

    @field:Schema(description = "Код причины постановки на учёт", example = "770101001")
    val regReasonCode: String,

    @field:Schema(description = "Юридический адрес", example = "г. Москва, ул. Примерная, д. 1")
    val address: String
)

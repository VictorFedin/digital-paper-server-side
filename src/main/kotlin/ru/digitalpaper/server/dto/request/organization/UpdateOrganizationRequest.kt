package ru.digitalpaper.server.dto.request.organization

import ru.digitalpaper.server.model.organization.holder.OrganizationType

data class UpdateOrganizationRequest(
    val name: String,
    val fullName: String,
    val description: String? = null,
    val phone: String? = null,
    val type: OrganizationType,
    val regNumber: String,
    val identificationNumber: String,
    val regReasonCode: String,
    val address: String
)

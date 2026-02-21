package ru.digitalpaper.server.util.converter.domain

import ru.digitalpaper.server.dto.response.organization.OrganizationResponse
import ru.digitalpaper.server.model.organization.Organization

class OrganizationConverter {

    companion object {

        fun convert(organization: Organization): OrganizationResponse =
            OrganizationResponse(
                id = organization.id,
                name = organization.name,
                description = organization.description,
                phone = organization.phone,
                email = organization.email,
                industry = organization.industry,
                status = organization.status,
                createdAt = organization.createdAt,
                updatedAt = organization.updatedAt
            )
    }
}
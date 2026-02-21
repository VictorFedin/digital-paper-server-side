package ru.digitalpaper.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.organization.Organization
import java.util.UUID

@Repository
interface OrganizationRepo : JpaRepository<Organization, UUID> {
    fun getOrganizationById(id: UUID): Organization?
}
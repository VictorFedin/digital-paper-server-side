package ru.digitalpaper.server.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.organization.Organization
import java.util.*

@Repository
interface OrganizationRepo : JpaRepository<Organization, UUID> {

    @Query(
        value = """
            SELECT o
            FROM Organization o
        """,
        countQuery = """
            SELECT count(o)
            FROM Organization o
        """
    )
    fun getOrganizations(
        pageable: Pageable
    ): Page<Organization>
}
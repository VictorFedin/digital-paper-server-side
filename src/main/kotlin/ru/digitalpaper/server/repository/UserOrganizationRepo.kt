package ru.digitalpaper.server.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import java.util.UUID

@Repository
interface UserOrganizationRepo : JpaRepository<UserOrganization, UUID> {

    @Query(
        value = """
            SELECT uo.organization
            FROM UserOrganization uo
            WHERE uo.user.id = :userId
        """,
        countQuery = """
            SELECT count(uo)
            FROM UserOrganization uo
            WHERE uo.user.id = :userId
        """
    )
    fun getOrganizationsByUserId(
        userId: UUID,
        pageable: Pageable
    ): Page<Organization>
}
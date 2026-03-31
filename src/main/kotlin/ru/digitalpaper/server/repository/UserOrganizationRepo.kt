package ru.digitalpaper.server.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.user.User
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

    @Query(
        value = """
            SELECT uo.organization
            FROM UserOrganization uo
        """
    )
    fun getOrganizations(pageable: Pageable): Page<Organization>

    @Query(
        value = """
            SELECT uo
            FROM UserOrganization uo
            WHERE uo.user.id = :userId AND uo.organization.id = :organizationId
        """
    )
    fun findMembership(userId: UUID, organizationId: UUID): UserOrganization?

    @Query(
        value = """
            SELECT (count(uo) > 0)
            FROM UserOrganization uo
            WHERE uo.user.id = :userId AND uo.organization.id = :organizationId
        """
    )
    fun existUserInOrganization(userId: UUID, organizationId: UUID): Boolean

    @Query(
        value = """
            SELECT uo.user
            FROM UserOrganization uo
            WHERE uo.organization.id = :organizationId
        """,
        countQuery = """
            SELECT count(uo)
            FROM UserOrganization uo
            WHERE uo.organization.id = :organizationId
        """
    )
    fun getUsersByOrganizationId(
        organizationId: UUID,
        pageable: Pageable
    ): Page<User>

    @Query(
        value = """
            SELECT uo
            FROM UserOrganization uo
            WHERE uo.user.id = :id
        """
    )
    fun getRelationByUserId(id: UUID): UserOrganization?
}
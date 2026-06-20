package ru.digitalpaper.server.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.model.user.holder.UserRole
import java.util.*

@Repository
interface UserOrganizationRepo : JpaRepository<UserOrganization, UUID>, UserOrganizationCustomRepo {

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
            SELECT uo
            FROM UserOrganization uo
            JOIN FETCH uo.user
            JOIN FETCH uo.organization
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
            SELECT uo
            FROM UserOrganization uo
            JOIN FETCH uo.user
            JOIN FETCH uo.organization
            WHERE uo.organization.id = :organizationId AND uo.user.id = :targetUserId
        """
    )
    fun findOrganizationUser(
        organizationId: UUID,
        targetUserId: UUID
    ): UserOrganization?

    fun countByOrganizationIdAndRole(organizationId: UUID, role: UserRole): Long

    @Query(
        value = """
                SELECT uo.user
                FROM UserOrganization uo
                WHERE uo.organization.id = :organizationId
                AND FUNCTION('date_part', 'month', uo.user.birthday) = :month
                ORDER BY FUNCTION('date_part', 'day', uo.user.birthday) ASC
            """
    )
    fun getUsersWithBirthdayInMonth(
        organizationId: UUID,
        month: Int
    ): List<User>

}

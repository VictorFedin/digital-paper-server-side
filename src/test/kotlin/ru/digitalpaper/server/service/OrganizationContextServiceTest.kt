package ru.digitalpaper.server.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.ForbiddenException
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.model.user.holder.UserRole
import ru.digitalpaper.server.repository.UserOrganizationRepo
import java.util.UUID
import kotlin.test.assertSame

class OrganizationContextServiceTest {

    private val userOrganizationRepo = mock(UserOrganizationRepo::class.java)
    private val service = OrganizationContextService(userOrganizationRepo)

    @Test
    fun `rejects missing organization header`() {
        assertThrows<BadRequestException> {
            service.resolve(UUID.randomUUID(), null)
        }
    }

    @Test
    fun `rejects malformed organization header`() {
        assertThrows<BadRequestException> {
            service.resolve(UUID.randomUUID(), "not-a-uuid")
        }
    }

    @Test
    fun `rejects organization without membership`() {
        val userId = UUID.randomUUID()
        val organizationId = UUID.randomUUID()

        `when`(userOrganizationRepo.findMembership(userId, organizationId))
            .thenReturn(null)

        assertThrows<ForbiddenException> {
            service.resolve(userId, organizationId.toString())
        }
    }

    @Test
    fun `resolves membership for selected organization`() {
        val userId = UUID.randomUUID()
        val organizationId = UUID.randomUUID()
        val user = User(email = "user@example.com")
        val organization = Organization(name = "Organization")
        val membership = UserOrganization(
            user = user,
            organization = organization,
            role = UserRole.OWNER
        )

        `when`(userOrganizationRepo.findMembership(userId, organizationId))
            .thenReturn(membership)

        val context = service.resolve(userId, " $organizationId ")

        assertSame(membership, context.membership)
        assertSame(user, context.user)
        assertSame(organization, context.organization)
        verify(userOrganizationRepo).findMembership(userId, organizationId)
    }
}

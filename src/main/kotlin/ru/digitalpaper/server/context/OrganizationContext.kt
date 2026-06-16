package ru.digitalpaper.server.context

import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.model.user.holder.UserRole

class OrganizationContext(
    val membership: UserOrganization
) {
    val organization: Organization = membership.organization
    val user: User = membership.user
    val role: UserRole = membership.role
}

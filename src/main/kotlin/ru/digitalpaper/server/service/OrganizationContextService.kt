package ru.digitalpaper.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.digitalpaper.server.context.OrganizationContext
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.ForbiddenException
import ru.digitalpaper.server.repository.UserOrganizationRepo
import java.util.UUID

@Service
class OrganizationContextService(
    private val userOrganizationRepo: UserOrganizationRepo
) {

    @Transactional(readOnly = true)
    fun resolve(
        userId: UUID,
        organizationHeader: String?
    ): OrganizationContext {
        val organizationId = parseOrganizationId(organizationHeader)
        val membership = userOrganizationRepo.findMembership(userId, organizationId)
            ?: throw ForbiddenException("Нет доступа к организации")

        return OrganizationContext(membership)
    }

    private fun parseOrganizationId(value: String?): UUID {
        val header = value?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw BadRequestException("Не указан заголовок X-Organization-Id")

        return try {
            UUID.fromString(header)
        } catch (_: IllegalArgumentException) {
            throw BadRequestException("Заголовок X-Organization-Id должен содержать UUID")
        }
    }
}

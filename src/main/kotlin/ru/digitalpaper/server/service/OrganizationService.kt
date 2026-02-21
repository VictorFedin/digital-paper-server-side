package ru.digitalpaper.server.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.digitalpaper.server.dto.request.organization.AddOrganizationRequest
import ru.digitalpaper.server.dto.response.organization.OrganizationResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.holder.ModerationStatus
import ru.digitalpaper.server.model.user.holder.UserRole
import ru.digitalpaper.server.repository.OrganizationRepo
import ru.digitalpaper.server.repository.UserRepo
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.converter.domain.OrganizationConverter
import java.util.UUID

@Service
class OrganizationService(
    private val organizationRepo: OrganizationRepo,
    private val userRepo: UserRepo
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")

        private const val DEFAULT_SORT_FIELD = "createdAt"
    }

    @Transactional
    fun addOrganization(
        request: AddOrganizationRequest,
        payload: UserPayload,
        rs: RequestSatellites
    ): OrganizationResponse {
        val user = userRepo.getUserBySub(payload.sub)
            ?: throw NotFoundException("Пользователь не найден")

        val newOrganization = Organization(
            name = request.name,
            industry = request.industry,
            status = ModerationStatus.NEW,
            createdBy = user.id
        )

        newOrganization.addMember(user = user, role = UserRole.OWNER)

        val organization = organizationRepo.save(newOrganization)

        return OrganizationConverter.convert(organization)
    }

    fun getOrganizationDetails(
        id: UUID,
        rs: RequestSatellites
    ): OrganizationResponse {
        val organization = organizationRepo.getOrganizationById(id)
            ?: throw NotFoundException("Организация не найдена")

        return OrganizationConverter.convert(organization)
    }
}
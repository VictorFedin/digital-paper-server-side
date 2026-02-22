package ru.digitalpaper.server.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.digitalpaper.server.dto.request.organization.AddOrganizationRequest
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.common.PagedResponse
import ru.digitalpaper.server.dto.response.organization.OrganizationResponse
import ru.digitalpaper.server.dto.response.organization.OrganizationsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.holder.ModerationStatus
import ru.digitalpaper.server.model.user.holder.UserRole
import ru.digitalpaper.server.repository.OrganizationRepo
import ru.digitalpaper.server.repository.UserOrganizationRepo
import ru.digitalpaper.server.repository.UserRepo
import ru.digitalpaper.server.util.Utils
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.converter.domain.OrganizationConverter
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.util.UUID

@Service
class OrganizationService(
    private val organizationRepo: OrganizationRepo,
    private val userRepo: UserRepo,
    private val userOrganizationRepo: UserOrganizationRepo
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
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.addOrganization",
                rs.traceId,
                "Enter",
                mapOf("request" to "$request")
            )
        )

        val user = userRepo.getUserBySub(payload.sub)
            ?: throw NotFoundException("Пользователь не найден")

        val newOrganization = Organization(
            name = request.name,
            industry = request.industry,
            status = ModerationStatus.NEW
        )

        newOrganization.addMember(user = user, role = UserRole.OWNER)

        val organization = organizationRepo.save(newOrganization)

        return OrganizationConverter.convert(organization)
    }

    fun getOrganizationDetails(
        id: UUID,
        rs: RequestSatellites
    ): OrganizationResponse {
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.getOrganizationDetails",
                rs.traceId,
                "Enter",
                mapOf("id" to "$id")
            )
        )

        val organization = organizationRepo.getOrganizationById(id)
            ?: throw NotFoundException("Организация не найдена")

        return OrganizationConverter.convert(organization)
    }

    fun getMyOrganizationsList(
        payload: UserPayload,
        page: Int,
        size: Int,
        rs: RequestSatellites
    ): Response {
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.getMyOrganizationsList",
                rs.traceId,
                "Enter",
                Pair("request", "page = '$page'; size = '$size'")
            )
        )

        val user = userRepo.getUserBySub(payload.sub)
            ?: throw NotFoundException("Пользователь не найден")

        val pageNumber = Utils.safePage(page)
        val pageSize = Utils.safeSize(size)
        val direction = Sort.Direction.DESC
        val sortField = DEFAULT_SORT_FIELD
        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(direction, sortField)
        )

        val orgPage = userOrganizationRepo.getOrganizationsByUserId(user.id, pageable)

        return OrganizationsPagedListResponse(
            page = PagedResponse(
                page = page,
                size = size,
                totalItems = orgPage.totalElements,
                sortField = sortField,
                sortDirection = direction.name
            ),
            list = orgPage.content.map { OrganizationConverter.convert(it) }.toList()
        )
    }
}
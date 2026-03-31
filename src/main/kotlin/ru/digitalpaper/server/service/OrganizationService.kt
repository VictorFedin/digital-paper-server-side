package ru.digitalpaper.server.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.digitalpaper.server.dto.internal.PagedRequest
import ru.digitalpaper.server.dto.request.organization.AddOrganizationRequest
import ru.digitalpaper.server.dto.request.organization.AddUserToOrganizationRequest
import ru.digitalpaper.server.dto.request.organization.UpdateOrganizationRequest
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.common.PagedResponse
import ru.digitalpaper.server.dto.response.organization.OrganizationResponse
import ru.digitalpaper.server.dto.response.organization.OrganizationsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.dto.response.user.UsersPagedListResponse
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.ForbiddenException
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.organization.holder.ModerationStatus
import ru.digitalpaper.server.model.user.holder.UserRole
import ru.digitalpaper.server.repository.OrganizationRepo
import ru.digitalpaper.server.repository.UserOrganizationRepo
import ru.digitalpaper.server.repository.UserRepo
import ru.digitalpaper.server.util.Utils
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.util.*

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
            description = request.description,
            phone = request.phoneNumber,
            email = request.email,
            industry = request.industry,
            status = ModerationStatus.NEW
        )

        newOrganization.addMember(user = user, role = UserRole.OWNER)

        val organization = organizationRepo.save(newOrganization)

        return organization.toResponse()
    }

    @Transactional
    fun getOrganizationDetails(
        id: UUID,
        payload: UserPayload,
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

        val actor = userRepo.getUserBySub(payload.sub)
            ?: throw NotFoundException("Пользователь не найден")

        val membership = userOrganizationRepo.findMembership(actor.id, id)
            ?: throw ForbiddenException("Нет доступа к организации")

        return membership.organization.toResponse()
    }

    @Transactional
    fun getMyOrganizationsList(
        payload: UserPayload,
        pagedRequest: PagedRequest,
        rs: RequestSatellites
    ): OrganizationsPagedListResponse {
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.getMyOrganizationsList",
                rs.traceId,
                "Enter",
                mapOf("request" to "$pagedRequest")
            )
        )

        val user = userRepo.getUserById(payload.id)
            ?: throw NotFoundException("Пользователь не найден")

        val pageNumber = Utils.safePage(pagedRequest.page)
        val pageSize = Utils.safeSize(pagedRequest.size)
        val direction = Utils.safeDirection(pagedRequest.sortDirection)
        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(direction, pagedRequest.sortField)
        )

        val orgPage = userOrganizationRepo.getOrganizationsByUserId(user.id, pageable)

        return OrganizationsPagedListResponse(
            page = PagedResponse(
                page = pagedRequest.page,
                size = pagedRequest.size,
                totalItems = orgPage.totalElements,
                sortField = pagedRequest.sortField,
                sortDirection = direction.name
            ),
            list = orgPage.content.map { it.toListItem() }.toList()
        )
    }

    @Transactional
    fun getOrganizationsList(
        pagedRequest: PagedRequest,
        rs: RequestSatellites
    ): OrganizationsPagedListResponse {
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.getOrganizationsList",
                rs.traceId,
                "Enter",
                mapOf("request" to "$pagedRequest")
            )
        )

        val pageNumber = Utils.safePage(pagedRequest.page)
        val pageSize = Utils.safeSize(pagedRequest.size)
        val direction = Utils.safeDirection(pagedRequest.sortDirection)
        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(direction, pagedRequest.sortField)
        )

        val orgPage = organizationRepo.getOrganizations(pageable)

        return OrganizationsPagedListResponse(
            page = PagedResponse(
                page = pagedRequest.page,
                size = pagedRequest.size,
                totalItems = orgPage.totalElements,
                sortField = pagedRequest.sortField,
                sortDirection = direction.name
            ),
            list = orgPage.content.map { it.toListItem() }.toList()
        )
    }

    @Transactional
    fun addUserToOrganization(
        payload: UserPayload,
        id: UUID,
        request: AddUserToOrganizationRequest,
        rs: RequestSatellites
    ): MessageResponse {
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.addUserToOrganization",
                rs.traceId,
                "Enter",
                Pair("id", "$id"),
                Pair("request", "$request")
            )
        )

        val actor = userRepo.getUserBySub(payload.sub)
            ?: throw NotFoundException("Пользователь не найден")

        val membership = userOrganizationRepo.findMembership(actor.id, id)
            ?: throw ForbiddenException("Нет доступа к организации")

        if (!membership.role.canManageOrganization())
            throw ForbiddenException("Недостаточно прав")

        val userToAdd = userRepo.getUserByEmail(request.email)
            ?: throw NotFoundException("Пользователь с email = '${request.email}' не найден")

        if (userOrganizationRepo.existUserInOrganization(userToAdd.id, membership.organization.id))
            throw BadRequestException("Пользователь уже состоит в организации")

        val organization = membership.organization

        organization.addMember(user = userToAdd, role = request.role)
        organizationRepo.save(organization)

        return MessageResponse("Пользователь добавлен в организацию '${organization.name}'")
    }

    @Transactional
    fun updateOrganization(
        id: UUID,
        request: UpdateOrganizationRequest,
        payload: UserPayload,
        rs: RequestSatellites
    ): OrganizationResponse {
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.updateOrganization",
                rs.traceId,
                "Enter",
                Pair("id", "$id"),
                Pair("request", "$request")
            )
        )

        val actor = userRepo.getUserBySub(payload.sub)
            ?: throw NotFoundException("Пользователь не найден")

        val membership = userOrganizationRepo.findMembership(actor.id, id)
            ?: throw ForbiddenException("Нет доступа к организации")

        if (!membership.role.canManageOrganization())
            throw ForbiddenException("Недостаточно прав")

        val organization = membership.organization

        if (!organization.canBeEdited())
            throw BadRequestException("Статус модерации - '${organization.status}'. В редактировании отказано")

        organization.updateDetails(
            name = request.name,
            fullName = request.fullName,
            description = request.description,
            phone = request.phone,
            regNumber = request.regNumber,
            identificationNumber = request.identificationNumber,
            regReasonCode = request.regReasonCode,
            address = request.address,
            type = request.type
        )

        return organization.toResponse()
    }

    @Transactional
    fun deleteOrganization(
        id: UUID,
        payload: UserPayload,
        rs: RequestSatellites
    ): MessageResponse {
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.deleteOrganization",
                rs.traceId,
                "Enter",
                Pair("id", "$id"),
            )
        )

        val actor = userRepo.getUserBySub(payload.sub)
            ?: throw NotFoundException("Пользователь не найден")

        val membership = userOrganizationRepo.findMembership(actor.id, id)
            ?: throw ForbiddenException("Нет доступа к организации")

        if (!membership.role.canManageOrganization())
            throw ForbiddenException("Недостаточно прав")

        organizationRepo.delete(membership.organization)

        return MessageResponse("Организация удалена")
    }

    @Transactional
    fun getOrganizationUsers(
        id: UUID,
        page: Int,
        size: Int,
        rs: RequestSatellites
    ): UsersPagedListResponse {
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.getOrganizationUsers",
                rs.traceId,
                "Enter",
                Pair("id", "$id"),
                Pair("page", "$page"),
                Pair("size", "$size"),
            )
        )

        val pageNumber = Utils.safePage(page)
        val pageSize = Utils.safeSize(size)
        val direction = Sort.Direction.DESC
        val sortField = DEFAULT_SORT_FIELD
        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(direction, sortField)
        )

        val usersPage = userOrganizationRepo.getUsersByOrganizationId(id, pageable)

        return UsersPagedListResponse(
            page = PagedResponse(
                page = page,
                size = size,
                totalItems = usersPage.totalElements,
                sortField = sortField,
                sortDirection = direction.name
            ),
            list = usersPage.content.map { it.toListItem() }.toList()
        )
    }

    @Transactional
    fun getRelationByUserId(id: UUID, rs: RequestSatellites): UserOrganization {
        logger.info(
            ServerLogUtil.info(
                "OrganizationService.getRelationByUserId",
                rs.traceId,
                "Enter",
                Pair("id", "$id")
            )
        )

        return userOrganizationRepo.getRelationByUserId(id)
            ?: throw NotFoundException("Пользователь не состоит ни в одной организации")
    }
}
package ru.digitalpaper.server.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import ru.digitalpaper.server.dto.internal.DownloadedFile
import ru.digitalpaper.server.dto.internal.PagedRequest
import ru.digitalpaper.server.dto.request.organization.AddOrganizationRequest
import ru.digitalpaper.server.dto.request.organization.AddUserToOrganizationRequest
import ru.digitalpaper.server.dto.request.organization.OrganizationUserListFilter
import ru.digitalpaper.server.dto.request.organization.UpdateOrganizationRequest
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.common.PagedResponse
import ru.digitalpaper.server.dto.response.organization.OrganizationResponse
import ru.digitalpaper.server.dto.response.organization.OrganizationsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.dto.response.user.UsersListResponse
import ru.digitalpaper.server.dto.response.user.UsersPagedListResponse
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.ForbiddenException
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.organization.holder.ModerationStatus
import ru.digitalpaper.server.model.user.holder.Avatar
import ru.digitalpaper.server.model.user.holder.UserRole
import ru.digitalpaper.server.repository.OrganizationRepo
import ru.digitalpaper.server.repository.UserOrganizationRepo
import ru.digitalpaper.server.repository.UserRepo
import ru.digitalpaper.server.type.StorageObjectType
import ru.digitalpaper.server.util.Utils
import java.time.ZonedDateTime
import java.util.*

@Service
class OrganizationService(
    private val organizationRepo: OrganizationRepo,
    private val userRepo: UserRepo,
    private val userOrganizationRepo: UserOrganizationRepo,
    private val invitationService: InvitationService,
    private val userService: UserService,
    private val storageService: StorageService
) {
    @Transactional
    fun addOrganization(
        request: AddOrganizationRequest,
        payload: UserPayload
    ): OrganizationResponse {
        val user = userService.getCurrentUser(payload)

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

    @Transactional(readOnly = true)
    fun getOrganizationDetails(
        id: UUID,
        payload: UserPayload
    ): OrganizationResponse {
        val membership = getMembershipWithAccessOrThrow(payload, id)

        return membership.organization.toResponse()
    }

    @Transactional(readOnly = true)
    fun getMyOrganizationsList(
        payload: UserPayload,
        request: PagedRequest,
    ): OrganizationsPagedListResponse {
        val user = userService.getCurrentUser(payload)

        val pageNumber = Utils.safePage(request.page)
        val pageSize = Utils.safeSize(request.size)
        val direction = Utils.safeDirection(request.sortDirection)
        val sortField = resolveOrganizationSortField(request.sortField)

        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(direction, sortField)
        )

        val orgPage = userOrganizationRepo.getOrganizationsByUserId(user.id, pageable)

        return OrganizationsPagedListResponse(
            page = PagedResponse(
                page = pageNumber,
                size = pageSize,
                totalItems = orgPage.totalElements,
                sortField = sortField,
                sortDirection = direction.name
            ),
            list = orgPage.content.map { it.toListItem(buildAvatarUrl(it)) }
        )
    }

    @Transactional(readOnly = true)
    fun getOrganizationsList(
        request: PagedRequest,
    ): OrganizationsPagedListResponse {
        val pageNumber = Utils.safePage(request.page)
        val pageSize = Utils.safeSize(request.size)
        val direction = Utils.safeDirection(request.sortDirection)
        val sortField = resolveOrganizationSortField(request.sortField)

        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(direction, sortField)
        )

        val orgPage = organizationRepo.getOrganizations(pageable)

        return OrganizationsPagedListResponse(
            page = PagedResponse(
                page = pageNumber,
                size = pageSize,
                totalItems = orgPage.totalElements,
                sortField = sortField,
                sortDirection = direction.name
            ),
            list = orgPage.content.map { it.toListItem(buildAvatarUrl(it)) }
        )
    }

    @Transactional(readOnly = true)
    fun getOrganizationAvatar(id: UUID): DownloadedFile {
        val organization = organizationRepo.findById(id)
            .orElseThrow { NotFoundException("Организация не найдена") }
        val avatar = organization.avatar
            ?: throw NotFoundException("Изображение организации не найдено")
        val downloadedObject = storageService.download(
            objectKey = avatar.objectKey,
            type = StorageObjectType.ORGANIZATION_IMAGE
        )

        return DownloadedFile(
            filename = avatar.fileName,
            contentType = avatar.contentType ?: downloadedObject.contentType,
            resource = InputStreamResource(downloadedObject.inputStream),
            contentLength = downloadedObject.size
        )
    }

    @Transactional
    fun saveOrganizationAvatar(
        id: UUID,
        payload: UserPayload,
        file: MultipartFile
    ): String {
        val membership = getManageableMembershipOrThrow(payload, id)
        val organization = membership.organization
        val oldAvatar = organization.avatar

        val storedFileInfo = storageService.upload(
            file = file,
            type = StorageObjectType.ORGANIZATION_IMAGE,
            ownerId = organization.id.toString()
        )

        organization.avatar = Avatar(
            id = UUID.randomUUID(),
            bucket = storedFileInfo.bucket,
            objectKey = storedFileInfo.objectKey,
            fileName = storedFileInfo.originalFileName
                ?: file.originalFilename
                ?: "avatar",
            fileSize = storedFileInfo.size,
            contentType = storedFileInfo.contentType,
            createdAt = ZonedDateTime.now()
        )

        oldAvatar?.let {
            storageService.delete(
                objectKey = it.objectKey,
                type = StorageObjectType.ORGANIZATION_IMAGE
            )
        }

        return buildAvatarUrl(organization)
            ?: throw IllegalStateException("Не удалось сформировать ссылку на аватар организации")
    }

    @Transactional
    fun addUserToOrganization(
        payload: UserPayload,
        id: UUID,
        request: AddUserToOrganizationRequest,
    ): MessageResponse {
        val membership = getManageableMembershipOrThrow(payload, id)
        val actor = membership.user
        val organization = membership.organization
        val email = request.email.trim().lowercase()

        val userToAdd = userRepo.getUserByEmail(email)

        if (userToAdd != null) {
            if (userOrganizationRepo.existUserInOrganization(userToAdd.id, organization.id)) {
                throw BadRequestException("Пользователь уже состоит в организации")
            }

            organization.addMember(
                user = userToAdd,
                role = UserRole.EMPLOYEE
            )

            return MessageResponse("Пользователь добавлен в организацию '${organization.name}'")
        }

        invitationService.invite(
            email = email,
            organization = organization,
            inviter = actor
        )

        return MessageResponse("Пользователь приглашен в организацию '${organization.name}'")

    }

    @Transactional
    fun updateOrganization(
        id: UUID,
        request: UpdateOrganizationRequest,
        payload: UserPayload,
    ): OrganizationResponse {
        val membership = getManageableMembershipOrThrow(payload, id)
        val organization = membership.organization

        if (!organization.canBeEdited()) {
            throw BadRequestException("Статус модерации - '${organization.status}'. В редактировании отказано")
        }

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
    ): MessageResponse {
        val membership = getManageableMembershipOrThrow(payload, id)
        val organization = membership.organization

        if (organization.status == ModerationStatus.DELETED) {
            return MessageResponse("Организация уже удалена")
        }

        organization.status = ModerationStatus.DELETED

        return MessageResponse("Организация удалена")
    }

    @Transactional
    fun restoreOrganization(
        id: UUID,
        payload: UserPayload,
    ): MessageResponse {
        val membership = getManageableMembershipOrThrow(payload, id)
        val organization = membership.organization

        if (organization.status != ModerationStatus.DELETED) {
            throw BadRequestException("Организация не находится в статусе удаления")
        }

        organization.status = ModerationStatus.NEW

        return MessageResponse("Организация восстановлена")
    }

    @Transactional(readOnly = true)
    fun getOrganizationUsers(
        id: UUID,
        payload: UserPayload,
        page: Int,
        size: Int,
        sortField: String,
        sortDirection: Sort.Direction,
        search: String? = null,
    ): UsersPagedListResponse {
        getMembershipWithAccessOrThrow(payload, id)

        val pageNumber = Utils.safePage(page)
        val pageSize = Utils.safeSize(size)
        val pageSort = resolveUserSortField(sortField)

        val pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(sortDirection, pageSort)
        )

        val filter = buildFilter(
            organizationId = id,
            search = search?.trim()?.takeIf { it.isNotBlank() }
        )

        val usersPage = userOrganizationRepo.getUsersByOrganizationId(filter, pageable)

        return UsersPagedListResponse(
            page = PagedResponse(
                page = pageNumber,
                size = pageSize,
                totalItems = usersPage.totalElements,
                sortField = pageSort,
                sortDirection = sortDirection.name
            ),
            list = usersPage.content.map { it.toListItem() }
        )
    }

    @Transactional(readOnly = true)
    fun getOrganizationUsersBirthdays(
        payload: UserPayload,
        organizationId: UUID,
        month: Int,
    ): UsersListResponse {
        getMembershipWithAccessOrThrow(payload, organizationId)

        if (month !in 1..12) {
            throw BadRequestException("Месяц должен быть в диапазоне от 1 до 12")
        }

        val users = userOrganizationRepo.getUsersWithBirthdayInMonth(
            organizationId = organizationId,
            month = month
        )

        return UsersListResponse(
            list = users.map { it.toListItem() }
        )
    }

    private fun resolveUserSortField(sortField: String): String {
        return when (sortField) {
            "id" -> "id"
            "email" -> "email"
            "name" -> "firstName"
            "createdAt" -> "createdAt"
            "updatedAt" -> "updatedAt"
            else -> "createdAt"
        }
    }

    private fun buildFilter(
        organizationId: UUID? = null,
        search: String? = null
    ): OrganizationUserListFilter =
        OrganizationUserListFilter(
            organizationId = organizationId,
            search = search
        )

    private fun getMembershipOrThrow(
        userId: UUID,
        organizationId: UUID,
    ): UserOrganization {
        return userOrganizationRepo.findMembership(userId, organizationId)
            ?: throw ForbiddenException("Нет доступа к организации")
    }

    private fun getManageableMembershipOrThrow(
        payload: UserPayload,
        organizationId: UUID,
    ): UserOrganization {
        val actor = userService.getCurrentUser(payload)

        val membership = getMembershipOrThrow(
            userId = actor.id,
            organizationId = organizationId,
        )

        if (!membership.role.canManageOrganization()) {
            throw ForbiddenException("Недостаточно прав")
        }

        return membership
    }

    private fun getMembershipWithAccessOrThrow(
        payload: UserPayload,
        organizationId: UUID,
    ): UserOrganization {
        val actor = userService.getCurrentUser(payload)

        return getMembershipOrThrow(
            userId = actor.id,
            organizationId = organizationId,
        )
    }

    private fun resolveOrganizationSortField(sortField: String): String {
        return when (sortField) {
            "id" -> "id"
            "name" -> "name"
            "email" -> "email"
            "status" -> "status"
            "createdAt" -> "createdAt"
            "updatedAt" -> "updatedAt"
            else -> "createdAt"
        }
    }

    private fun buildAvatarUrl(organization: Organization): String? {
        val avatar = organization.avatar ?: return null

        return ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/v1/organizations/{id}/avatar")
            .queryParam("v", avatar.id)
            .buildAndExpand(organization.id)
            .toUriString()
    }

}

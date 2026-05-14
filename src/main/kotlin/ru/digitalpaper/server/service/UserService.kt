package ru.digitalpaper.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.dto.request.user.UpdateUserProfileRequest
import ru.digitalpaper.server.dto.response.user.AvatarResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.dto.response.user.UserProfileResponse
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.model.user.holder.Avatar
import ru.digitalpaper.server.repository.UserRepo
import ru.digitalpaper.server.type.StorageObjectType
import java.time.ZonedDateTime
import java.util.*

@Service
class UserService(
    private val userRepo: UserRepo,
    private val storageService: StorageService
) {

    @Transactional(readOnly = true)
    fun getUserProfile(
        payload: UserPayload
    ): UserProfileResponse {
        val user = getCurrentUser(payload)

        return user.toResponse()
    }

    @Transactional
    fun updateUserProfile(
        payload: UserPayload,
        request: UpdateUserProfileRequest
    ): UserProfileResponse {
        val user = getCurrentUser(payload)

        user.firstName = request.firstName ?: user.firstName
        user.lastName = request.lastName ?: user.lastName
        user.middleName = request.middleName ?: user.middleName
        user.birthday = request.birthday ?: user.birthday

        return user.toResponse()
    }

    @Transactional
    fun saveUserAvatar(
        file: MultipartFile,
        payload: UserPayload,
    ): AvatarResponse {
        val user = getCurrentUser(payload)

        val oldAvatar = user.avatar

        val storedFileInfo = storageService.upload(
            file = file,
            type = StorageObjectType.USER_AVATAR,
            ownerId = payload.id.toString(),
        )

        val avatar = Avatar(
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

        user.avatar = avatar

        oldAvatar?.let {
            storageService.delete(
                objectKey = it.objectKey,
                type = StorageObjectType.USER_AVATAR
            )
        }

        return avatar.toResponse()
    }

    @Transactional
    fun deleteUserAvatar(
        payload: UserPayload
    ) {
        val user = getCurrentUser(payload)

        val avatar = user.avatar
            ?: return

        storageService.delete(
            avatar.objectKey,
            StorageObjectType.USER_AVATAR
        )

        user.avatar = null
    }

    fun getCurrentUser(payload: UserPayload): User {
        return userRepo.getUserById(payload.id)
            ?: throw NotFoundException("Пользователь не найден")
    }

    fun Avatar.toResponse(): AvatarResponse =
        AvatarResponse(
            id = id,
            link = storageService.getPublicOrResolvableUrl(bucket, objectKey),
            fileName = fileName,
            contentType = contentType,
            fileSize = fileSize,
            createdAt = createdAt,
        )

    fun User.toResponse(): UserProfileResponse =
        UserProfileResponse(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            birthday = birthday,
            avatar = avatar?.toResponse(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )

}

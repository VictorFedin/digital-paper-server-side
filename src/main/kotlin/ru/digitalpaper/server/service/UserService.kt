package ru.digitalpaper.server.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.dto.response.user.AvatarResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.dto.response.user.UserProfileResponse
import ru.digitalpaper.server.exception.InternalErrorException
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.user.holder.Avatar
import ru.digitalpaper.server.repository.UserRepo
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.time.ZonedDateTime
import java.util.*

@Service
class UserService(
    private val userRepo: UserRepo,
    private val storageService: StorageService
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")
        private const val AVATAR_BASE_DIR = "avatars/users"
    }

    @Transactional(readOnly = true)
    fun getUserProfile(
        payload: UserPayload,
        rs: RequestSatellites
    ): UserProfileResponse {
        logger.info(
            ServerLogUtil.info(
                "UserService.getUserProfile",
                rs.traceId,
                "Enter"
            )
        )

        val user = userRepo.getUserById(payload.id)
            ?: throw NotFoundException("Пользователь не найден")

        return user.toResponse()
    }

    @Transactional
    fun saveUserAvatar(
        file: MultipartFile,
        payload: UserPayload,
        rs: RequestSatellites
    ): AvatarResponse {
        logger.info(
            ServerLogUtil.info(
                "UserService.saveUserAvatar",
                rs.traceId,
                "Enter"
            )
        )

        val user = userRepo.getUserBySub(payload.sub)
            ?: throw NotFoundException("Пользователь не найден")

        val documentId = UUID.randomUUID()
        val originalName = file.originalFilename!!
        val fileExtension: String = originalName.substringAfterLast('.')
        val subPath = "${user.id}"

        logger.info(
            ServerLogUtil.info(
                "UserService.saveUserAvatar",
                rs.traceId,
                "Saving user avatar"
            )
        )

        val documentPath = storageService.saveFile(
            AVATAR_BASE_DIR,
            subPath,
            documentId,
            fileExtension,
            file.inputStream,
            rs.traceId
        ) ?: throw InternalErrorException("Ошибка сохранения файла")


        val avatar = Avatar(
            id = documentId,
            link = documentPath,
            fileName = originalName,
            fileSize = file.size,
            contentType = file.contentType,
            createdAt = ZonedDateTime.now()
        )

        user.avatar = avatar
        userRepo.save(user)

        return avatar.toResponse()
    }

}
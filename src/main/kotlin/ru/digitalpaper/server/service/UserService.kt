package ru.digitalpaper.server.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.dto.response.user.UserProfileResponse
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.repository.UserRepo
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.converter.domain.UserConverter

@Service
class UserService(
    private val userRepo: UserRepo
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")
    }

    @Transactional
    fun getUserProfile(
        payload: UserPayload,
        rs: RequestSatellites
    ): UserProfileResponse {
        val user = userRepo.getUserBySub(payload.sub)
        return if (user != null) {
            UserConverter.convert(user)
        } else {
            initiateUser(payload, rs)
        }
    }

    private fun initiateUser(
        payload: UserPayload,
        rs: RequestSatellites
    ): UserProfileResponse {
        val user = User(
            sub = payload.sub,
            email = payload.email,
            firstName = payload.firstName,
            lastName = payload.lastName
        )

        val finalUser = userRepo.save(user)

        return UserConverter.convert(finalUser)
    }
}
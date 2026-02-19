package ru.digitalpaper.server.util.converter.domain

import ru.digitalpaper.server.dto.response.user.UserProfileResponse
import ru.digitalpaper.server.model.user.User

class UserConverter {

    companion object {

        /* User*/
        fun convert(user: User): UserProfileResponse =
            UserProfileResponse(
                id = user.id,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
    }
}
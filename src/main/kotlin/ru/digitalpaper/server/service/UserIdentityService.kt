package ru.digitalpaper.server.service

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.repository.UserRepo

@Service
class UserIdentityService(
    private val userRepo: UserRepo
) {

    @Transactional
    fun getOrCreateUser(
        sub: String,
        email: String,
        firstName: String,
        lastName: String,
        middleName: String
    ): User {
        val existingUser = userRepo.getUserBySub(sub)
        if (existingUser != null) return existingUser

        return try {
            userRepo.save(
                User(
                    sub = sub,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    middleName = middleName
                )
            )
        } catch (ex: DataIntegrityViolationException) {
            userRepo.getUserBySub(sub) ?: throw ex
        }
    }
}
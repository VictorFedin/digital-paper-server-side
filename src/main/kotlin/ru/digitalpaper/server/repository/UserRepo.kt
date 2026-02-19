package ru.digitalpaper.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.user.User
import java.util.UUID

@Repository
interface UserRepo : JpaRepository<User, UUID> {
    fun getUserBySub(sub: String): User?
}

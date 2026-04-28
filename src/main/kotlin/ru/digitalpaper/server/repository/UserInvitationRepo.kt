package ru.digitalpaper.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.invitation.UserInvitation
import ru.digitalpaper.server.model.invitation.holder.UserInvitationStatus
import java.util.*

@Repository
interface UserInvitationRepo : JpaRepository<UserInvitation, UUID> {

    @Query(
        value = """
            SELECT ui
            FROM UserInvitation ui
            WHERE ui.email = :email
            AND ui.status != :status
        """
    )
    fun getUserInvitationByEmail(email: String, status: UserInvitationStatus = UserInvitationStatus.JOINED): UserInvitation?
}
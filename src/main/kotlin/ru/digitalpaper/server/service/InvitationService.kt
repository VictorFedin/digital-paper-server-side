package ru.digitalpaper.server.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import ru.digitalpaper.server.model.invitation.UserInvitation
import ru.digitalpaper.server.model.invitation.holder.UserInvitationStatus
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.repository.UserInvitationRepo
import ru.digitalpaper.server.repository.UserOrganizationRepo

@Service
class InvitationService(
    private val userInvitationRepo: UserInvitationRepo,
    private val notificationService: NotificationService,
    private val userOrganizationRepo: UserOrganizationRepo
) {

    @Transactional
    fun invite(
        email: String,
        organization: Organization,
        inviter: User,
    ) {
        val invitation = UserInvitation(
            email = email,
            organization = organization,
            inviter = inviter
        )

        userInvitationRepo.save(invitation)

        notificationService.sendInvitation(invitation.email, organization.name)
    }

    @Transactional
    fun acceptInvitation(user: User) {
        val invitation = userInvitationRepo.getUserInvitationByEmail(user.email)

        if (invitation != null) {
            val exist = userOrganizationRepo.existUserInOrganization(user.id, invitation.organization.id)

            if (!exist) {
                val membership = UserOrganization(user, invitation.organization)

                userOrganizationRepo.save(
                    membership
                )
            }

            invitation.status = UserInvitationStatus.JOINED

            userInvitationRepo.save(invitation)
        }
    }
}

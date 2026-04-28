package ru.digitalpaper.server.model.invitation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.invitation.holder.UserInvitationStatus
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.user.User

@Entity
@Table(
    name = "user_invitations"
)
class UserInvitation(
    @Column(name = "email", nullable = false)
    var email: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, updatable = false)
    var organization: Organization,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false, updatable = false)
    var inviter: User,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserInvitationStatus = UserInvitationStatus.PENDING,
) : UniqueEntity()
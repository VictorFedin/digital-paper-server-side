package ru.digitalpaper.server.model.organization

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.model.user.holder.UserRole

@Entity
@Table(
    name = "user_organization",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_organization_user_org",
            columnNames = ["user_id", "organization_id"]
        )
    ],
    indexes = [
        Index(name = "idx_user_organization_user_id", columnList = "user_id"),
        Index(name = "idx_user_organization_organization_id", columnList = "organization_id"),
        Index(name = "idx_user_organization_org_role", columnList = "organization_id,role")
    ]
)
class UserOrganization(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, updatable = false)
    var organization: Organization,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: UserRole = UserRole.EMPLOYEE
) : UniqueEntity()
package ru.digitalpaper.server.model.user

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ru.digitalpaper.server.dto.response.user.UserListItem
import ru.digitalpaper.server.dto.response.user.UserProfileResponse
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.user.holder.Avatar

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_users_sub", columnNames = ["sub"]),
        UniqueConstraint(name = "uk_users_email", columnNames = ["email"])
    ],
    indexes = [
        Index(name = "idx_users_email", columnList = "email")
    ]
)
class User(
    @Column(name = "sub", nullable = false, updatable = false, length = 255)
    var sub: String = "",

    @Column(name = "email", nullable = false, length = 255)
    var email: String = "",

    @Column(name = "first_name", length = 100)
    var firstName: String = "",

    @Column(name = "last_name", length = 100)
    var lastName: String = "",

    @Column(name = "middle_name")
    var middleName: String = "",

    @JdbcTypeCode(value = SqlTypes.JSON)
    @Column(name = "avatar")
    var avatar: Avatar? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var organizations: MutableSet<UserOrganization> = mutableSetOf()
) : UniqueEntity() {

    fun toResponse(): UserProfileResponse =
        UserProfileResponse(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    fun toListItem(): UserListItem =
        UserListItem(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}
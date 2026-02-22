package ru.digitalpaper.server.model.organization

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.organization.holder.Industry
import ru.digitalpaper.server.model.organization.holder.ModerationStatus
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.model.user.holder.UserRole

@Entity
@Table(name = "organizations")
class Organization(
    @Column(name = "name", nullable = false, length = 100)
    var name: String = "",

    @Column(name = "description", length = 1024)
    var description: String? = null,

    @Column(name = "phone", length = 100)
    var phone: String? = null,

    @Column(name = "email", length = 100)
    var email: String? = null,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "industry")
    var industry: Industry = Industry.NONE,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    var status: ModerationStatus = ModerationStatus.NEW,

    @OneToMany(mappedBy = "organization", cascade = [CascadeType.ALL], orphanRemoval = true)
    var users: MutableSet<UserOrganization> = mutableSetOf()
) : UniqueEntity() {

    fun addMember(user: User, role: UserRole) {
        val link = UserOrganization(user = user, organization = this, role = role)
        users.add(link)
        user.organizations.add(link)
    }
}

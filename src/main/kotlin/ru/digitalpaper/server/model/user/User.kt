package ru.digitalpaper.server.model.user

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.organization.UserOrganization

@Entity
@Table(name = "users")
class User(
    @Column(name = "sub", nullable = false, updatable = false, length = 255)
    var sub: String = "",

    @Column(name = "email", nullable = false, length = 255)
    var email: String = "",

    @Column(name = "first_name", length = 100)
    var firstName: String = "",

    @Column(name = "last_name", length = 100)
    var lastName: String = "",

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var organizations: MutableSet<UserOrganization> = mutableSetOf()
) : UniqueEntity()
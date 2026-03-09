package ru.digitalpaper.server.model.organization

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ru.digitalpaper.server.dto.response.organization.OrganizationListItem
import ru.digitalpaper.server.dto.response.organization.OrganizationResponse
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.department.Department
import ru.digitalpaper.server.model.organization.holder.Industry
import ru.digitalpaper.server.model.organization.holder.ModerationStatus
import ru.digitalpaper.server.model.organization.holder.OrganizationType
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.model.user.holder.Avatar
import ru.digitalpaper.server.model.user.holder.UserRole

@Entity
@Table(
    name = "organizations",
    indexes = [
        Index(name = "idx_organizations_status", columnList = "status"),
        Index(name = "idx_organizations_industry", columnList = "industry")
    ]
)
class Organization(
    @Column(name = "name", nullable = false, length = 100)
    var name: String = "",

    @Column(name = "full_name")
    var fullName: String = "",

    @Column(name = "description", length = 1024)
    var description: String? = null,

    @Column(name = "phone", length = 100)
    var phone: String? = null,

    @Column(name = "email", length = 100)
    var email: String? = null,

    @Column(name = "reg_number")
    var regNumber: String? = null,

    @Column(name = "identification_number")
    var identificationNumber: String? = null,

    @Column(name = "reg_reason_code")
    var regReasonCode: String? = null,

    @Column(name = "address")
    var address: String? = null,

    @JdbcTypeCode(value = SqlTypes.JSON)
    @Column(name = "avatar")
    var avatar: Avatar? = null,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "industry", nullable = false)
    var industry: Industry = Industry.NONE,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ModerationStatus = ModerationStatus.NEW,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: OrganizationType = OrganizationType.NONE,

    @OneToMany(mappedBy = "organization", cascade = [CascadeType.ALL], orphanRemoval = true)
    var users: MutableSet<UserOrganization> = mutableSetOf(),

    @OneToMany(mappedBy = "organization", cascade = [CascadeType.ALL], orphanRemoval = true)
    var departments: MutableSet<Department> = mutableSetOf()
) : UniqueEntity() {

    fun addMember(user: User, role: UserRole) {
        val link = UserOrganization(user = user, organization = this, role = role)
        users.add(link)
        user.organizations.add(link)
    }

    fun addDepartment(department: Department) {
        departments.add(department)
    }

    fun updateDetails(
        name: String,
        fullName: String,
        description: String?,
        phone: String?,
        regNumber: String?,
        identificationNumber: String?,
        regReasonCode: String?,
        address: String?,
        type: OrganizationType
    ) {
        this.name = name.trim()
        this.fullName = fullName.trim()
        this.description = description?.trim()?.takeIf { it.isNotBlank() }
        this.phone = phone?.trim()?.takeIf { it.isNotBlank() }
        this.regNumber = regNumber?.trim()?.takeIf { it.isNotBlank() }
        this.identificationNumber = identificationNumber?.trim()?.takeIf { it.isNotBlank() }
        this.regReasonCode = regReasonCode?.trim()?.takeIf { it.isNotBlank() }
        this.address = address?.trim()?.takeIf { it.isNotBlank() }
        this.type = type
    }

    fun canBeEdited(): Boolean =
        when (this.status) {
            ModerationStatus.NEW, ModerationStatus.REVISION_NEEDED -> true
            ModerationStatus.PENDING_REVIEW, ModerationStatus.APPROVED, ModerationStatus.REJECTED -> false
        }

    fun toResponse(): OrganizationResponse =
        OrganizationResponse(
            id = id,
            name = name,
            description = description,
            phone = phone,
            email = email,
            industry = industry,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    fun toListItem(): OrganizationListItem =
        OrganizationListItem(
            name = name,
            avatar = avatar,
            type = type,
            createdAt = createdAt,
        )
}

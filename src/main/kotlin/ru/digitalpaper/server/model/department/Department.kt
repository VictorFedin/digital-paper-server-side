package ru.digitalpaper.server.model.department

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.nomenclature.Nomenclature
import ru.digitalpaper.server.model.organization.Organization

@Entity
@Table(
    name = "departments",
    indexes = [
        Index(name = "idx_departments_organization_id", columnList = "organization_id"),
        Index(name = "idx_departments_org_name", columnList = "organization_id,name")
    ]
)
class Department(
    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @Column(name = "email")
    var email: String? = null,

    @Column(name = "category")
    var category: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: Organization? = null,

    @OneToMany(mappedBy = "department", cascade = [CascadeType.ALL], orphanRemoval = true)
    var nomenclatures: MutableSet<Nomenclature> = mutableSetOf()
) : UniqueEntity() {

    fun addNomenclature(nomenclature: Nomenclature) {
        nomenclatures.add(nomenclature)
    }
}
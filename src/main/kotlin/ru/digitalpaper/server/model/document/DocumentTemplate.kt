package ru.digitalpaper.server.model.document

import jakarta.persistence.*
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.user.User

@Entity
@Table(name = "document_templates")
class DocumentTemplate(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "path", nullable = false)
    var path: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: Organization,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    var author: User,

    @OneToMany(mappedBy = "document_template_fields", cascade = [CascadeType.ALL], orphanRemoval = true)
    var fields: MutableSet<DocumentTemplateField> = mutableSetOf()
) : UniqueEntity()

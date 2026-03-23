package ru.digitalpaper.server.model.document

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
import ru.digitalpaper.server.model.document.holder.DocumentUserRole
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.user.User

@Entity
@Table(
    name = "document_users",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_document_users_document_user",
            columnNames = ["document_id", "user_id"]
        )
    ],
    indexes = [
        Index(name = "idx_document_users_document_id", columnList = "document_id"),
        Index(name = "idx_document_users_user_id", columnList = "user_id"),
        Index(name = "idx_document_users_organization_id", columnList = "organization_id"),
        Index(name = "idx_document_users_role", columnList = "role")
    ]
)
class DocumentUser(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, updatable = false)
    var document: Document,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, updatable = false)
    var organization: Organization,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    var role: DocumentUserRole = DocumentUserRole.EDITOR
) : UniqueEntity()
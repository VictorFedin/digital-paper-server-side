package ru.digitalpaper.server.model.document

import jakarta.persistence.*
import ru.digitalpaper.server.dto.response.document.DocumentListItem
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.document.holder.DocumentStatus
import ru.digitalpaper.server.model.document.holder.DocumentType
import ru.digitalpaper.server.model.folder.Folder
import ru.digitalpaper.server.model.nomenclature.Nomenclature
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.user.User

@Entity
@Table(
    name = "documents",
    indexes = [
        Index(name = "idx_documents_nomenclature_id", columnList = "nomenclature_id"),
        Index(name = "idx_documents_folder_id", columnList = "folder_id"),
        Index(name = "idx_documents_type", columnList = "type"),
        Index(name = "idx_documents_nom_type", columnList = "nomenclature_id,type"),
        Index(name = "idx_documents_folder_type", columnList = "folder_id,type")
    ]
)
class Document(
    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "path", nullable = false)
    var path: String = "",

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: DocumentType = DocumentType.NONE,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: DocumentStatus = DocumentStatus.CREATED,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id")
    var nomenclature: Nomenclature? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    var folder: Folder? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    var createdBy: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsible_user_id", nullable = false)
    var responsibleUser: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, updatable = false)
    var organization: Organization,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    var lastModifiedBy: User? = null,

    @Version
    @Column(name = "entity_version", nullable = false)
    var entityVersion: Long = 0

) : UniqueEntity() {

    fun toListItem(): DocumentListItem =
        DocumentListItem(
            id = id,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}

package ru.digitalpaper.server.model.document

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.user.User

@Entity
@Table(
    name = "document_revisions",
    indexes = [
        Index(name = "idx_document_revisions_document_id", columnList = "document_id"),
        Index(name = "idx_document_revisions_created_by", columnList = "created_by"),
        Index(name = "idx_document_revisions_document_version", columnList = "document_id,version")
    ]
)
class DocumentRevision(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, updatable = false)
    var document: Document,

    @Column(name = "version", nullable = false)
    var version: Long,

    @Column(name = "snapshot_key", nullable = false, length = 1024)
    var snapshotKey: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    var createdBy: User
) : UniqueEntity()
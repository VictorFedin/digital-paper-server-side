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
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.document.holder.DocumentType
import ru.digitalpaper.server.model.folder.Folder
import ru.digitalpaper.server.model.nomenclature.Nomenclature

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id", nullable = false)
    var nomenclature: Nomenclature? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    var folder: Folder? = null
) : UniqueEntity()

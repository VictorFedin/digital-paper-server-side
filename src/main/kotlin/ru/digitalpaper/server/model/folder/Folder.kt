package ru.digitalpaper.server.model.folder

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
import ru.digitalpaper.server.model.document.Document
import ru.digitalpaper.server.model.nomenclature.Nomenclature

@Entity
@Table(
    name = "folders",
    indexes = [
        Index(name = "idx_folders_nomenclature_id", columnList = "nomenclature_id"),
        Index(name = "idx_folders_parent_id", columnList = "parent_id"),
        Index(name = "idx_folders_nom_parent", columnList = "nomenclature_id,parent_id")
    ]
)
class Folder(
    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "description")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id", nullable = false)
    var nomenclature: Nomenclature? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Folder? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true)
    var children: MutableSet<Folder> = mutableSetOf(),

    @OneToMany(mappedBy = "folder")
    var documents: MutableSet<Document> = mutableSetOf()

) : UniqueEntity() {

    fun addChild(child: Folder) {
        children.add(child)
        child.parent = this
    }

    fun removeChild(child: Folder) {
        children.remove(child)
        child.parent = null
    }

    fun addDocument(document: Document) {
        documents.add(document)
        document.folder = this
    }

    fun removeDocument(document: Document) {
        documents.remove(document)
        if (document.folder == this) document.folder = null
    }

}
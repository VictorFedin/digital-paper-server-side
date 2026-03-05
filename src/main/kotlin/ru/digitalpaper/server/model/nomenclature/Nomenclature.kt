package ru.digitalpaper.server.model.nomenclature

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import ru.digitalpaper.server.model.base.UniqueEntity
import ru.digitalpaper.server.model.department.Department
import ru.digitalpaper.server.model.document.Document
import ru.digitalpaper.server.model.folder.Folder
import ru.digitalpaper.server.model.nomenclature.holder.NomenclatureType

@Entity
@Table(
    name = "nomenclatures",
    indexes = [
        Index(name = "idx_nomenclatures_department_id", columnList = "department_id"),
        Index(name = "idx_nomenclatures_dept_type", columnList = "department_id,type"),
        Index(name = "idx_nomenclatures_dept_name", columnList = "department_id,name")
    ]
)
class Nomenclature(
    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "description")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: NomenclatureType = NomenclatureType.NONE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    var department: Department? = null,

    @OneToMany(mappedBy = "nomenclature", cascade = [CascadeType.ALL], orphanRemoval = true)
    var folders: MutableSet<Folder> = mutableSetOf(),

    @OneToMany(mappedBy = "nomenclature", cascade = [CascadeType.ALL], orphanRemoval = true)
    var documents: MutableSet<Document> = mutableSetOf()

) : UniqueEntity() {

    fun addFolder(folder: Folder) {
        folders.add(folder)
    }

    fun addDocument(document: Document) {
        documents.add(document)
    }
}

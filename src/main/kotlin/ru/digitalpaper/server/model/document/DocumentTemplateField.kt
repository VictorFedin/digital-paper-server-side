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
import ru.digitalpaper.server.model.document.holder.TemplateFieldType

@Entity
@Table(
    name = "document_template_fields",
    indexes = [
        Index(name = "idx_template_fields_template_id", columnList = "template_id"),
        Index(name = "idx_template_fields_key", columnList = "field_key")
    ]
)
class DocumentTemplateField(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    var template: DocumentTemplate,

    @Column(name = "field_key", nullable = false)
    var key: String = "",

    @Column(name = "label", nullable = false)
    var label: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    var type: TemplateFieldType = TemplateFieldType.TEXT,

    @Column(name = "required", nullable = false)
    var required: Boolean = false,

    @Column(name = "default_value")
    var defaultValue: String? = null,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    @Column(name = "metadata", columnDefinition = "text")
    var metadata: String? = null
) : UniqueEntity()

CREATE TABLE document_template_fields
(
    id            UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    template_id   UUID                        NOT NULL,
    field_key     VARCHAR(255)                NOT NULL,
    label         VARCHAR(255)                NOT NULL,
    field_type    VARCHAR(255)                NOT NULL,
    required      BOOLEAN                     NOT NULL,
    default_value VARCHAR(255),
    sort_order    INTEGER                     NOT NULL,
    metadata      TEXT,
    CONSTRAINT pk_document_template_fields PRIMARY KEY (id)
);

CREATE TABLE document_templates
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name            VARCHAR(255)                NOT NULL,
    path            VARCHAR(255)                NOT NULL,
    organization_id UUID                        NOT NULL,
    created_by      UUID                        NOT NULL,
    CONSTRAINT pk_document_templates PRIMARY KEY (id)
);

CREATE INDEX idx_template_fields_key ON document_template_fields (field_key);

ALTER TABLE document_templates
    ADD CONSTRAINT FK_DOCUMENT_TEMPLATES_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES users (id);

ALTER TABLE document_templates
    ADD CONSTRAINT FK_DOCUMENT_TEMPLATES_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

ALTER TABLE document_template_fields
    ADD CONSTRAINT FK_DOCUMENT_TEMPLATE_FIELDS_ON_TEMPLATE FOREIGN KEY (template_id) REFERENCES document_templates (id);

ALTER TABLE documents
    ADD template_id UUID;

ALTER TABLE documents
    ADD CONSTRAINT FK_DOCUMENTS_ON_TEMPLATE FOREIGN KEY (template_id) REFERENCES document_templates (id);

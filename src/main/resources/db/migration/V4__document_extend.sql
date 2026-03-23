ALTER TABLE documents
    ADD status VARCHAR(255);

ALTER TABLE documents
    ALTER COLUMN status SET NOT NULL;

CREATE TABLE document_revisions
(
    id           UUID                        NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    document_id  UUID                        NOT NULL,
    version      BIGINT                      NOT NULL,
    snapshot_key VARCHAR(1024)               NOT NULL,
    created_by   UUID                        NOT NULL,
    CONSTRAINT pk_document_revisions PRIMARY KEY (id)
);

CREATE TABLE document_users
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    document_id     UUID                        NOT NULL,
    user_id         UUID                        NOT NULL,
    organization_id UUID                        NOT NULL,
    role            VARCHAR(50)                 NOT NULL,
    CONSTRAINT pk_document_users PRIMARY KEY (id)
);

ALTER TABLE documents
    ADD created_by UUID;

ALTER TABLE documents
    ADD entity_version BIGINT;

ALTER TABLE documents
    ADD last_modified_by UUID;

ALTER TABLE documents
    ADD organization_id UUID;

ALTER TABLE documents
    ADD responsible_user_id UUID;

ALTER TABLE documents
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE documents
    ALTER COLUMN entity_version SET NOT NULL;

ALTER TABLE documents
    ALTER COLUMN organization_id SET NOT NULL;

ALTER TABLE documents
    ALTER COLUMN responsible_user_id SET NOT NULL;

ALTER TABLE document_users
    ADD CONSTRAINT uk_document_users_document_user UNIQUE (document_id, user_id);

CREATE INDEX idx_document_revisions_document_version ON document_revisions (document_id, version);

CREATE INDEX idx_document_users_role ON document_users (role);

ALTER TABLE documents
    ADD CONSTRAINT FK_DOCUMENTS_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES users (id);

ALTER TABLE documents
    ADD CONSTRAINT FK_DOCUMENTS_ON_LAST_MODIFIED_BY FOREIGN KEY (last_modified_by) REFERENCES users (id);

ALTER TABLE documents
    ADD CONSTRAINT FK_DOCUMENTS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

ALTER TABLE documents
    ADD CONSTRAINT FK_DOCUMENTS_ON_RESPONSIBLE_USER FOREIGN KEY (responsible_user_id) REFERENCES users (id);

ALTER TABLE document_revisions
    ADD CONSTRAINT FK_DOCUMENT_REVISIONS_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES users (id);

CREATE INDEX idx_document_revisions_created_by ON document_revisions (created_by);

ALTER TABLE document_revisions
    ADD CONSTRAINT FK_DOCUMENT_REVISIONS_ON_DOCUMENT FOREIGN KEY (document_id) REFERENCES documents (id);

CREATE INDEX idx_document_revisions_document_id ON document_revisions (document_id);

ALTER TABLE document_users
    ADD CONSTRAINT FK_DOCUMENT_USERS_ON_DOCUMENT FOREIGN KEY (document_id) REFERENCES documents (id);

CREATE INDEX idx_document_users_document_id ON document_users (document_id);

ALTER TABLE document_users
    ADD CONSTRAINT FK_DOCUMENT_USERS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

CREATE INDEX idx_document_users_organization_id ON document_users (organization_id);

ALTER TABLE document_users
    ADD CONSTRAINT FK_DOCUMENT_USERS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_document_users_user_id ON document_users (user_id);
CREATE TABLE departments
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name            VARCHAR(255)                NOT NULL,
    description     VARCHAR(255),
    phone_number    VARCHAR(255),
    email           VARCHAR(255),
    category        VARCHAR(255),
    organization_id UUID                        NOT NULL,
    CONSTRAINT pk_departments PRIMARY KEY (id)
);

CREATE TABLE documents
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name            VARCHAR(255)                NOT NULL,
    description     VARCHAR(255),
    path            VARCHAR(255)                NOT NULL,
    type            VARCHAR(255)                NOT NULL,
    nomenclature_id UUID                        NOT NULL,
    folder_id       UUID,
    CONSTRAINT pk_documents PRIMARY KEY (id)
);

CREATE TABLE folders
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name            VARCHAR(255)                NOT NULL,
    description     VARCHAR(255),
    nomenclature_id UUID                        NOT NULL,
    parent_id       UUID,
    CONSTRAINT pk_folders PRIMARY KEY (id)
);

CREATE TABLE nomenclatures
(
    id            UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name          VARCHAR(255)                NOT NULL,
    description   VARCHAR(255),
    type          VARCHAR(255)                NOT NULL,
    department_id UUID                        NOT NULL,
    CONSTRAINT pk_nomenclatures PRIMARY KEY (id)
);

CREATE TABLE organizations
(
    id          UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name        VARCHAR(100)                NOT NULL,
    description VARCHAR(1024),
    phone       VARCHAR(100),
    email       VARCHAR(100),
    industry    VARCHAR(255)                NOT NULL,
    status      VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_organizations PRIMARY KEY (id)
);

CREATE TABLE user_organization
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id         UUID                        NOT NULL,
    organization_id UUID                        NOT NULL,
    role            VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_user_organization PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    sub        VARCHAR(255)                NOT NULL,
    email      VARCHAR(255)                NOT NULL,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    avatar     JSONB,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE user_organization
    ADD CONSTRAINT uk_user_organization_user_org UNIQUE (user_id, organization_id);

ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uk_users_sub UNIQUE (sub);

CREATE INDEX idx_departments_org_name ON departments (organization_id, name);

CREATE INDEX idx_documents_folder_type ON documents (folder_id, type);

CREATE INDEX idx_documents_nom_type ON documents (nomenclature_id, type);

CREATE INDEX idx_documents_type ON documents (type);

CREATE INDEX idx_folders_nom_parent ON folders (nomenclature_id, parent_id);

CREATE INDEX idx_nomenclatures_dept_name ON nomenclatures (department_id, name);

CREATE INDEX idx_nomenclatures_dept_type ON nomenclatures (department_id, type);

CREATE INDEX idx_organizations_industry ON organizations (industry);

CREATE INDEX idx_organizations_status ON organizations (status);

CREATE INDEX idx_user_organization_org_role ON user_organization (organization_id, role);

CREATE INDEX idx_users_email ON users (email);

ALTER TABLE departments
    ADD CONSTRAINT FK_DEPARTMENTS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

CREATE INDEX idx_departments_organization_id ON departments (organization_id);

ALTER TABLE documents
    ADD CONSTRAINT FK_DOCUMENTS_ON_FOLDER FOREIGN KEY (folder_id) REFERENCES folders (id);

CREATE INDEX idx_documents_folder_id ON documents (folder_id);

ALTER TABLE documents
    ADD CONSTRAINT FK_DOCUMENTS_ON_NOMENCLATURE FOREIGN KEY (nomenclature_id) REFERENCES nomenclatures (id);

CREATE INDEX idx_documents_nomenclature_id ON documents (nomenclature_id);

ALTER TABLE folders
    ADD CONSTRAINT FK_FOLDERS_ON_NOMENCLATURE FOREIGN KEY (nomenclature_id) REFERENCES nomenclatures (id);

CREATE INDEX idx_folders_nomenclature_id ON folders (nomenclature_id);

ALTER TABLE folders
    ADD CONSTRAINT FK_FOLDERS_ON_PARENT FOREIGN KEY (parent_id) REFERENCES folders (id);

CREATE INDEX idx_folders_parent_id ON folders (parent_id);

ALTER TABLE nomenclatures
    ADD CONSTRAINT FK_NOMENCLATURES_ON_DEPARTMENT FOREIGN KEY (department_id) REFERENCES departments (id);

CREATE INDEX idx_nomenclatures_department_id ON nomenclatures (department_id);

ALTER TABLE user_organization
    ADD CONSTRAINT FK_USER_ORGANIZATION_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

CREATE INDEX idx_user_organization_organization_id ON user_organization (organization_id);

ALTER TABLE user_organization
    ADD CONSTRAINT FK_USER_ORGANIZATION_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_user_organization_user_id ON user_organization (user_id);
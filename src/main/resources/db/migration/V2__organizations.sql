CREATE TABLE organizations
(
    id          UUID                        NOT NULL,
    name        VARCHAR(100)                NOT NULL,
    description VARCHAR(1024),
    phone       VARCHAR(100),
    email       VARCHAR(100),
    industry    VARCHAR(255),
    status      VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_organizations PRIMARY KEY (id)
);

CREATE TABLE user_organization
(
    id              UUID                        NOT NULL,
    user_id         UUID                        NOT NULL,
    organization_id UUID                        NOT NULL,
    role            VARCHAR(255)                NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_user_organization PRIMARY KEY (id)
);

ALTER TABLE user_organization
    ADD CONSTRAINT FK_USER_ORGANIZATION_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

ALTER TABLE user_organization
    ADD CONSTRAINT FK_USER_ORGANIZATION_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
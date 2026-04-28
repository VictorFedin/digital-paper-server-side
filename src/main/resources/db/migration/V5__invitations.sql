CREATE TABLE user_invitations
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    email           VARCHAR(255)                NOT NULL,
    organization_id UUID                        NOT NULL,
    invited_by      UUID                        NOT NULL,
    status          VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_user_invitations PRIMARY KEY (id)
);

ALTER TABLE user_invitations
    ADD CONSTRAINT FK_USER_INVITATIONS_ON_INVITED_BY FOREIGN KEY (invited_by) REFERENCES users (id);

ALTER TABLE user_invitations
    ADD CONSTRAINT FK_USER_INVITATIONS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

ALTER TABLE departments
    ALTER COLUMN category SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN first_name SET NOT NULL;

ALTER TABLE organizations
    ALTER COLUMN full_name SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN last_name SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN middle_name SET NOT NULL;

ALTER TABLE documents
    ALTER COLUMN nomenclature_id DROP NOT NULL;
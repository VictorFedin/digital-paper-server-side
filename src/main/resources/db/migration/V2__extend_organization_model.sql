ALTER TABLE organizations
    ADD address VARCHAR(255);

ALTER TABLE organizations
    ADD avatar JSONB;

ALTER TABLE organizations
    ADD full_name VARCHAR(255);

ALTER TABLE organizations
    ADD identification_number VARCHAR(255);

ALTER TABLE organizations
    ADD reg_number VARCHAR(255);

ALTER TABLE organizations
    ADD reg_reason_code VARCHAR(255);

ALTER TABLE organizations
    ADD type VARCHAR(255);

ALTER TABLE organizations
    ALTER COLUMN type SET NOT NULL;
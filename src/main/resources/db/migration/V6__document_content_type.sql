ALTER TABLE documents
    ADD content_type VARCHAR(255);

ALTER TABLE documents
    ALTER COLUMN content_type SET NOT NULL;

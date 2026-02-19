CREATE TABLE users
(
    id         UUID                        NOT NULL,
    sub        VARCHAR(255)                NOT NULL,
    email      VARCHAR(255)                NOT NULL,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);
CREATE TABLE roles
(
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    role_name VARCHAR(30) NOT NULL,

    CONSTRAINT uk_role_name
        UNIQUE(role_name)
);
CREATE TABLE refresh_tokens
(
    refresh_token_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    token VARCHAR(500) NOT NULL UNIQUE,

    user_id BIGINT NOT NULL UNIQUE,

    expiry_date DATETIME NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
);

CREATE INDEX idx_refresh_token
    ON refresh_tokens(token);

CREATE INDEX idx_refresh_user
    ON refresh_tokens(user_id);
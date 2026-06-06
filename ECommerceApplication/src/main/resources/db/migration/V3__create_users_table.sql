CREATE TABLE users (
                       user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       first_name VARCHAR(50),
                       last_name VARCHAR(50),
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255),
                       mobile_number VARCHAR(15),
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP,
                       deleted BOOLEAN DEFAULT FALSE,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       account_locked BOOLEAN NOT NULL DEFAULT FALSE,
                       failed_login_attempts INT NOT NULL DEFAULT 0,
                       lock_time DATETIME NULL,
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE
);